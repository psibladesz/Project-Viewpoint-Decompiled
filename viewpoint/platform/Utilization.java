package viewpoint.platform;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

final class Utilization {
   private static final long SAMPLE_NANOS = 500000000L;
   private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();
   private static final OperatingSystemMXBean OS;
   static volatile float game;
   static volatile float render;
   static volatile float process;
   static volatile float system;
   static volatile float gpu;
   private static long sampledAt;
   private static long gameCpu;
   private static long renderCpu;
   private static boolean samplerStarted;

   static void sample(long var0) {
      if (var0 - sampledAt >= 500000000L) {
         threads(var0 - sampledAt);
         sampledAt = var0;
         gpu = GpuBusy.percent();
         if (!samplerStarted && OS != null) {
            samplerStarted = true;
            Thread var2 = new Thread(Utilization::runSampler, "Viewpoint overlay CPU");
            var2.setDaemon(true);
            var2.start();
         }
      }
   }

   private static void threads(long var0) {
      long var2 = 0L;
      long var4 = 0L;

      try {
         if (!THREADS.isThreadCpuTimeEnabled()) {
            THREADS.setThreadCpuTimeEnabled(true);
         }

         var2 = THREADS.getThreadCpuTime(Thread.currentThread().getId());
         long var6 = FrameTimes.renderThreadId;
         var4 = var6 >= 0L ? THREADS.getThreadCpuTime(var6) : 0L;
      } catch (UnsupportedOperationException | SecurityException var8) {
      }

      if (sampledAt != 0L && var0 > 0L) {
         if (var2 > 0L && gameCpu > 0L) {
            game = Math.min(100.0F, 100.0F * (float)(var2 - gameCpu) / (float)var0);
         }

         if (var4 > 0L && renderCpu > 0L) {
            render = Math.min(100.0F, 100.0F * (float)(var4 - renderCpu) / (float)var0);
         }
      }

      gameCpu = var2;
      renderCpu = var4;
   }

   private static void runSampler() {
      try {
         while (true) {
            Thread.sleep(500L);
            if (PerformanceOverlay.mode() != PerformanceOverlay.Mode.OFF) {
               double var0 = OS.getProcessCpuLoad();
               double var2 = OS.getCpuLoad();
               if (var0 >= 0.0) {
                  process = (float)(var0 * 100.0);
               }

               if (var2 >= 0.0) {
                  system = (float)(var2 * 100.0);
               }
            }
         }
      } catch (InterruptedException var4) {
         Thread.currentThread().interrupt();
      }
   }

   private Utilization() {
   }

   static {
      OS = ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean var0 ? var0 : null;
   }
}
