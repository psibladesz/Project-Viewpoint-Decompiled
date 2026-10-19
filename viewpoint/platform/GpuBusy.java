package viewpoint.platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

final class GpuBusy {
   private static final int RUN_SAMPLES = 30;
   private static volatile float busy = Float.NaN;
   private static volatile boolean started;

   static float percent() {
      if (!started) {
         started = true;
         if (System.getProperty("os.name", "").startsWith("Windows")) {
            Thread var0 = new Thread(GpuBusy::run, "Viewpoint overlay GPU");
            var0.setDaemon(true);
            var0.start();
         }
      }

      return busy;
   }

   private static void run() {
      String var0 = "\\GPU Engine(pid_" + ProcessHandle.current().pid() + "_*engtype_3D)\\Utilization Percentage";

      try {
         while (true) {
            while (PerformanceOverlay.mode() != PerformanceOverlay.Mode.OFF) {
               if (!sample(var0) && Float.isNaN(busy) && PerformanceOverlay.mode() != PerformanceOverlay.Mode.OFF) {
                  System.out.println("[Viewpoint] overlay: the GPU's busy share cannot be read (typeperf " + var0 + " gave nothing)");
                  return;
               }
            }

            busy = Float.NaN;
            Thread.sleep(1000L);
         }
      } catch (InterruptedException var2) {
         Thread.currentThread().interrupt();
      }
   }

   private static boolean sample(String var0) throws InterruptedException {
      ProcessBuilder var1 = new ProcessBuilder("typeperf", var0, "-si", "1", "-sc", Integer.toString(30));
      var1.redirectErrorStream(true);
      boolean var2 = false;

      Process var3;
      try {
         var3 = var1.start();
      } catch (IOException var14) {
         return false;
      }

      try (BufferedReader var4 = new BufferedReader(new InputStreamReader(var3.getInputStream(), StandardCharsets.UTF_8))) {
         for (String var5 = var4.readLine(); var5 != null; var5 = var4.readLine()) {
            float var6 = sum(var5);
            if (!Float.isNaN(var6)) {
               busy = Math.min(100.0F, var6);
               var2 = true;
            }

            if (PerformanceOverlay.mode() == PerformanceOverlay.Mode.OFF) {
               break;
            }
         }
      } catch (IOException var16) {
      } finally {
         var3.destroy();
         var3.waitFor();
      }

      return var2;
   }

   static float sum(String var0) {
      String[] var1 = var0.split("\",\"");
      if (var1.length >= 2 && var0.startsWith("\"") && !var0.startsWith("\"(PDH")) {
         float var2 = 0.0F;

         for (int var3 = 1; var3 < var1.length; var3++) {
            String var4 = var1[var3].replace("\"", "").trim();

            try {
               var2 += var4.isEmpty() ? 0.0F : Float.parseFloat(var4);
            } catch (NumberFormatException var6) {
               return Float.NaN;
            }
         }

         return var2;
      } else {
         return Float.NaN;
      }
   }

   private GpuBusy() {
   }
}
