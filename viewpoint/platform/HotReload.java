package viewpoint.platform;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class HotReload {
   public static volatile int generation;
   public static final String DIR_PROPERTY = "viewpoint.hotReloadDir";
   private static final String PREFIX = "viewpoint/";
   private static final String CLASS = ".class";
   private static final String ROOT = "viewpoint.";
   private static final long POLL_MILLIS = 250L;
   private static boolean started;

   public static void start() {
      String var0 = System.getProperty("viewpoint.hotReloadDir");
      if (var0 != null && !var0.isBlank() && !started) {
         started = true;

         try {
            Path var1 = Path.of(var0.trim()).toAbsolutePath().normalize();
            Path var2 = var1.resolve("requests");
            Path var3 = var1.resolve("results");
            empty(Files.createDirectories(var2));
            empty(Files.createDirectories(var3));
            Instrumentation var4 = instrumentation();
            Map<String, byte[]> var5 = classes(Path.of(HotReload.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
            Thread var6 = new Thread(() -> watch(var4, var5, var2, var3), "Viewpoint reload");
            var6.setDaemon(true);
            var6.start();
            System.out.println("[Viewpoint] hot reload ready: scripts/Reload-Mod.ps1 sends the changed classes to " + var1);
         } catch (Throwable var7) {
            System.out.println("[Viewpoint] hot reload unavailable: " + var7);
         }
      }
   }

   private static Instrumentation instrumentation() throws ReflectiveOperationException {
      Field var0 = Class.forName("me.zed_0xff.zombie_buddy.Loader").getDeclaredField("g_instrumentation");
      var0.setAccessible(true);
      if (var0.get(null) instanceof Instrumentation var1 && var1.isRedefineClassesSupported()) {
         return var1;
      } else {
         throw new IllegalStateException("ZombieBuddy gives no instrumentation that can redefine classes");
      }
   }

   private static void watch(Instrumentation var0, Map<String, byte[]> var1, Path var2, Path var3) {
      TreeMap<String, byte[]> var4 = new TreeMap<>(var1);

      while (true) {
         try {
            List<Path> var5;
            try (Stream<Path> var6 = Files.list(var2)) {
               var5 = var6.filter(var0x -> var0x.toString().endsWith(".jar")).sorted().toList();
            }

            for (Path var7 : var5) {
               String var8 = var7.getFileName().toString();
               String var9 = var8.substring(0, var8.length() - 4);
               HotReload.Outcome var10 = apply(var0, var1, var4, var7);
               write(var3.resolve(var9 + ".json"), var10.json(var9));
               Files.deleteIfExists(var7);
               System.out
                       .println(
                               "[Viewpoint] hot reload "
                                       + var10.status
                                       + " ("
                                       + var9
                                       + "): "
                                       + var10.message
                                       + (var10.classes.isEmpty() ? "" : ": " + String.join(", ", var10.classes))
                       );
            }

            Thread.sleep(250L);
         } catch (InterruptedException var13) {
            return;
         } catch (Throwable var14) {
            System.out.println("[Viewpoint] hot reload watcher: " + var14);
            pause();
         }
      }
   }

   private static HotReload.Outcome apply(Instrumentation var0, Map<String, byte[]> var1, Map<String, byte[]> var2, Path var3) {
      try {
         Map<String, byte[]> var4 = classes(var3);
         Map<String, Class<?>> var5 = loaded(var0);
         List<String> var6 = problems(var1, var2, var4, var2x -> var5.containsKey(var2x) && var0.isModifiableClass((Class<?>)var5.get(var2x)));
         if (!var6.isEmpty()) {
            return new HotReload.Outcome("RESTART_REQUIRED", String.join("; ", var6), List.of());
         }

         ArrayList<ClassDefinition> var7 = new ArrayList<>();
         ArrayList<String> var8 = new ArrayList<>();

         for (Entry<String, byte[]> var10 : var4.entrySet()) {
            if (!Arrays.equals((byte[])var2.get(var10.getKey()), (byte[])var10.getValue())) {
               String var11 = binaryName((String)var10.getKey());
               var7.add(new ClassDefinition((Class<?>)var5.get(var11), (byte[])var10.getValue()));
               var8.add(var11);
            }
         }

         if (var7.isEmpty()) {
            return new HotReload.Outcome("NO_CHANGES", "the classes match those running", List.of());
         }

         var0.redefineClasses(var7.toArray(ClassDefinition[]::new));
         var2.clear();
         var2.putAll(var4);
         generation++;
         return new HotReload.Outcome("APPLIED", var7.size() + " classes redefined; the caches start over", var8);
      } catch (UnsupportedOperationException | LinkageError var12) {
         return new HotReload.Outcome("RESTART_REQUIRED", "the JVM refused a change it cannot make while running: " + var12.getMessage(), List.of());
      } catch (Throwable var13) {
         return new HotReload.Outcome("FAILED", String.valueOf(var13), List.of());
      }
   }

   static List<String> problems(Map<String, byte[]> var0, Map<String, byte[]> var1, Map<String, byte[]> var2, Predicate<String> var3) {
      ArrayList<String> var4 = new ArrayList<>();

      for (String var6 : var0.keySet()) {
         if (!var2.containsKey(var6)) {
            var4.add("a class removed: " + binaryName(var6));
         }
      }

      for (Entry<String, byte[]> var9 : var2.entrySet()) {
         String var7 = binaryName((String)var9.getKey());
         if (!var0.containsKey(var9.getKey())) {
            var4.add("a class added: " + var7);
         } else if (!Arrays.equals((byte[])var1.get(var9.getKey()), (byte[])var9.getValue())) {
            if (restartOnly(var7)) {
               var4.add("a patch or bootstrap class changed: " + var7);
            } else if (!var3.test(var7)) {
               var4.add("changed but not loaded yet (it would load from the jar the game started with): " + var7);
            }
         }
      }

      return var4;
   }

   static boolean restartOnly(String var0) {
      return var0.startsWith("viewpoint.Patch_")
              || var0.equals("viewpoint.Main")
              || var0.equals(HotReload.class.getName())
              || var0.startsWith(HotReload.class.getName() + "$");
   }

   private static Map<String, byte[]> classes(Path var0) throws IOException {
      TreeMap<String, byte[]> var1 = new TreeMap<>();

      try (JarFile var2 = new JarFile(var0.toFile())) {
         Enumeration<JarEntry> var3 = var2.entries();

         while (var3.hasMoreElements()) {
            JarEntry var4 = (JarEntry)var3.nextElement();
            if (var4.getName().startsWith("viewpoint/") && var4.getName().endsWith(".class")) {
               try (InputStream var5 = var2.getInputStream(var4)) {
                  var1.put(var4.getName(), var5.readAllBytes());
               }
            }
         }
      }

      return var1;
   }

   private static Map<String, Class<?>> loaded(Instrumentation var0) {
      HashMap<String, Class<?>> var1 = new HashMap<>();

      for (Class<?> var5 : var0.getAllLoadedClasses()) {
         if (var5.getName().startsWith("viewpoint.")) {
            var1.put(var5.getName(), var5);
         }
      }

      return var1;
   }

   static String binaryName(String var0) {
      return var0.substring(0, var0.length() - ".class".length()).replace('/', '.');
   }

   private static void write(Path var0, String var1) throws IOException {
      Path var2 = var0.resolveSibling(var0.getFileName() + ".part");
      Files.writeString(var2, var1 + "\n", StandardCharsets.UTF_8);
      Files.move(var2, var0, StandardCopyOption.REPLACE_EXISTING);
   }

   private static void empty(Path var0) throws IOException {
      try (Stream<Path> var1 = Files.list(var0)) {
         for (Path var3 : var1.toList()) {
            Files.deleteIfExists(var3);
         }
      }
   }

   private static void pause() {
      try {
         Thread.sleep(1000L);
      } catch (InterruptedException var1) {
         Thread.currentThread().interrupt();
      }
   }

   private HotReload() {
   }

   record Outcome(String status, String message, List<String> classes) {
      String json(String var1) {
         StringBuilder var2 = new StringBuilder();

         for (String var4 : this.classes) {
            var2.append(var2.isEmpty() ? "" : ",").append('"').append(escape(var4)).append('"');
         }

         return "{\"requestId\":\""
                 + escape(var1)
                 + "\",\"status\":\""
                 + this.status
                 + "\",\"message\":\""
                 + escape(this.message)
                 + "\",\"classes\":["
                 + var2
                 + "]}";
      }

      private static String escape(String var0) {
         return var0.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
      }
   }
}