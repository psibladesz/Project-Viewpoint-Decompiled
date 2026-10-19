package viewpoint.iris;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class PackSource implements Closeable, SourceFiles {
   private static final String SHADERS = "shaders";
   private final String name;
   private final ZipFile zip;
   private final File folder;
   private final Map<String, String> entries = new TreeMap<>();
   private final Map<String, String> lowerCase = new HashMap<>();
   private final Map<String, String> texts = new HashMap<>();

   private PackSource(String var1, ZipFile var2, File var3) {
      this.name = var1;
      this.zip = var2;
      this.folder = var3;
   }

   public static PackSource open(File var0) throws IOException {
      String var1 = var0.getName();
      if (var0.isFile()) {
         String var4 = var1.toLowerCase(Locale.ROOT).endsWith(".zip") ? var1.substring(0, var1.length() - 4) : var1;
         PackSource var3 = new PackSource(var4, new ZipFile(var0, StandardCharsets.UTF_8), null);
         var3.indexZip();
         return var3;
      } else {
         PackSource var2 = new PackSource(var1, null, var0);
         var2.indexFolder();
         return var2;
      }
   }

   public String name() {
      return this.name;
   }

   public Set<String> paths() {
      return Collections.unmodifiableSet(this.entries.keySet());
   }

   public boolean exists(String var1) {
      return this.entry(var1) != null;
   }

   @Override
   public String read(String var1) throws IOException {
      String var2 = this.entry(var1);
      if (var2 == null) {
         return null;
      }

      String var3 = this.texts.get(var2);
      if (var3 == null) {
         var3 = new String(this.bytes(var2), StandardCharsets.UTF_8).replace("\r\n", "\n").replace('\r', '\n');
         if (!var3.isEmpty() && var3.charAt(0) == '\ufeff') {
            var3 = var3.substring(1);
         }

         this.texts.put(var2, var3);
      }

      return var3;
   }

   public byte[] readBytes(String var1) throws IOException {
      String var2 = this.entry(var1);
      return var2 == null ? null : this.bytes(var2);
   }

   @Override
   public void close() throws IOException {
      if (this.zip != null) {
         this.zip.close();
      }
   }

   private String entry(String var1) {
      String var2 = normalize(var1);
      if (var2 == null) {
         return null;
      } else {
         return this.entries.containsKey(var2) ? var2 : this.lowerCase.get(var2.toLowerCase(Locale.ROOT));
      }
   }

   private byte[] bytes(String var1) throws IOException {
      String var2 = this.entries.get(var1);
      if (this.zip != null) {
         ZipEntry var3 = this.zip.getEntry(var2);

         try (InputStream var4 = this.zip.getInputStream(var3)) {
            return var4.readAllBytes();
         }
      } else {
         return Files.readAllBytes(new File(var2).toPath());
      }
   }

   public static String normalize(String var0) {
      ArrayDeque var1 = new ArrayDeque();

      for (String var5 : var0.replace('\\', '/').split("/")) {
         if (!var5.isEmpty() && !var5.equals(".")) {
            if (var5.equals("..")) {
               if (var1.isEmpty()) {
                  return null;
               }

               var1.removeLast();
            } else {
               var1.addLast(var5);
            }
         }
      }

      return String.join("/", var1);
   }

   private void indexZip() throws IOException {
      String var1 = null;

      for (ZipEntry var3 : Collections.list(this.zip.entries())) {
         String var4 = var3.getName().replace('\\', '/');
         if (!var3.isDirectory() && isMarker(var4)) {
            String var5 = var4.substring(0, var4.lastIndexOf(47) + 1);
            if (var5.endsWith("shaders/") && (var1 == null || depth(var5) < depth(var1))) {
               var1 = var5;
            }
         }
      }

      if (var1 == null) {
         throw new IOException("no shaders folder with shaders.properties or a program");
      }

      for (ZipEntry var7 : Collections.list(this.zip.entries())) {
         String var8 = var7.getName().replace('\\', '/');
         if (!var7.isDirectory() && var8.startsWith(var1)) {
            this.add(var8.substring(var1.length()), var7.getName());
         }
      }
   }

   private void indexFolder() throws IOException {
      File var1 = shallowest(this.folder, 0);
      if (var1 == null) {
         throw new IOException("no shaders folder with shaders.properties or a program");
      }

      this.indexFiles(var1, "");
   }

   private static File shallowest(File var0, int var1) {
      File[] var2 = var0.listFiles();
      if (var2 != null && var1 <= 4) {
         if (var0.getName().equals("shaders")) {
            for (File var6 : var2) {
               if (var6.isFile() && isMarker("shaders/" + var6.getName())) {
                  return var0;
               }
            }
         }

         File var9 = null;

         for (File var7 : var2) {
            if (var7.isDirectory()) {
               File var8 = shallowest(var7, var1 + 1);
               if (var8 != null && (var9 == null || var8.getPath().length() < var9.getPath().length())) {
                  var9 = var8;
               }
            }
         }

         return var9;
      } else {
         return null;
      }
   }

   private void indexFiles(File var1, String var2) {
      File[] var3 = var1.listFiles();
      if (var3 != null) {
         for (File var7 : var3) {
            String var8 = var2 + var7.getName();
            if (var7.isDirectory()) {
               this.indexFiles(var7, var8 + "/");
            } else {
               this.add(var8, var7.getPath());
            }
         }
      }
   }

   private void add(String var1, String var2) {
      this.entries.put(var1, var2);
      this.lowerCase.putIfAbsent(var1.toLowerCase(Locale.ROOT), var1);
   }

   private static boolean isMarker(String var0) {
      String var1 = var0.substring(var0.lastIndexOf(47) + 1);
      return var1.equals("shaders.properties") || var1.matches("(final|composite\\d*|gbuffers_\\w+|deferred\\d*)\\.(fsh|vsh)");
   }

   private static int depth(String var0) {
      return var0.length() - var0.replace("/", "").length();
   }
}
