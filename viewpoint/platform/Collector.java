package viewpoint.platform;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Collector {
   private static final long TRIAL_SECONDS = 30L;
   private static final String BACKUP = "ProjectZomboid64.json.viewpoint-backup";
   private static volatile Collector.Offer offered;
   private static volatile boolean done;
   private static Thread checking;

   private Collector() {
   }

   public static synchronized void start() {
      if (checking == null) {
         checking = new Thread(Collector::check, "Viewpoint collector check");
         checking.setDaemon(true);
         checking.start();
      }
   }

   static boolean done() {
      return done;
   }

   static Collector.Offer offered() {
      return offered;
   }

   private static void check() {
      try {
         HotSpotDiagnosticMXBean var0 = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
         if (!var0.getVMOption("UseZGC").getValue().equals("true")) {
            System.out.println("[Viewpoint] collector: not ZGC, nothing to switch");
            return;
         }

         Path var1 = Path.of(System.getProperty("user.dir"), "ProjectZomboid64.json");
         if (!Files.isRegularFile(var1)) {
            System.out.println("[Viewpoint] collector: ZGC, but no " + var1 + " to switch it in");
            return;
         }

         String var2 = Files.readString(var1, StandardCharsets.UTF_8);
         LauncherCollector.Plan var3 = LauncherCollector.plan(var2);
         if (var3 == null) {
            System.out.println("[Viewpoint] collector: ZGC, but not from " + var1 + " (the game's launch options?): left as it is");
            return;
         }

         String var4 = trial(var3.tried());
         if (var4 == null) {
            offered = new Collector.Offer(var1, var2, var3);
            System.out.println("[Viewpoint] collector: ZGC; Shenandoah offered, tried with " + var3.tried() + "; the game's memory settings kept as they are");
            return;
         }

         System.out.println("[Viewpoint] collector: the game's Java does not start with " + var3.tried() + ": " + var4 + "; the switch is not offered");
      } catch (IOException | RuntimeException var8) {
         System.out.println("[Viewpoint] collector check failed: " + var8 + "; the switch is not offered");
         return;
      } finally {
         done = true;
      }
   }

   private static String trial(List<String> var0) throws IOException {
      boolean var1 = System.getProperty("os.name").startsWith("Windows");
      String var2 = Path.of(System.getProperty("java.home"), "bin", var1 ? "java.exe" : "java").toString();
      ArrayList var3 = new ArrayList();
      var3.add(var2);
      var3.addAll(var0);
      var3.add("-version");
      Process var4 = new ProcessBuilder(var3).redirectErrorStream(true).start();
      byte[] var5 = var4.getInputStream().readAllBytes();

      try {
         if (!var4.waitFor(30L, TimeUnit.SECONDS)) {
            var4.destroyForcibly();
            return "no answer in 30 s";
         }
      } catch (InterruptedException var7) {
         Thread.currentThread().interrupt();
         var4.destroyForcibly();
         return "interrupted";
      }

      int var6 = var4.exitValue();
      return var6 == 0 ? null : "exit " + var6 + ": " + new String(var5, StandardCharsets.UTF_8).strip();
   }

   static void apply() throws IOException {
      Collector.Offer var0 = offered;
      Path var1 = var0.file();
      if (!Files.readString(var1, StandardCharsets.UTF_8).equals(var0.read())) {
         throw new IOException(var1 + " changed since the game started");
      }

      Files.writeString(var1.resolveSibling("ProjectZomboid64.json.viewpoint-backup"), var0.read(), StandardCharsets.UTF_8);

      try {
         overwrite(var1, var0.plan().json());
      } catch (IOException var5) {
         try {
            overwrite(var1, var0.read());
         } catch (IOException var4) {
            throw new IOException(var5 + "; the old text not put back either (" + var4 + "), it is in ProjectZomboid64.json.viewpoint-backup", var5);
         }

         throw var5;
      }

      System.out
         .println(
            "[Viewpoint] collector: " + var1 + " switched to Shenandoah, the old one kept as ProjectZomboid64.json.viewpoint-backup; the game saves and quits"
         );
   }

   private static void overwrite(Path var0, String var1) throws IOException {
      Files.writeString(var0, var1, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
   }

   record Offer(Path file, String read, LauncherCollector.Plan plan) {
   }
}
