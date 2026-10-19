package viewpoint.platform;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

final class ProcessMemory {
   private static final long MIB = 1048576L;
   private static final long SAMPLE_NANOS = 500000000L;
   private static final long JUMP_BYTES = 268435456L;
   private static final OperatingSystemMXBean OS = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
   private static final List<GarbageCollectorMXBean> COLLECTORS = ManagementFactory.getGarbageCollectorMXBeans();
   private static final BufferPoolMXBean DIRECT = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)
      .stream()
      .filter(var0 -> var0.getName().equals("direct"))
      .findFirst()
      .orElse(null);
   private static long lastSample;
   private static long committed;
   private static long peak;
   private static final long[] gcCount = new long[COLLECTORS.size()];
   private static final long[] gcMs = new long[COLLECTORS.size()];

   static void sample(long var0) {
      if (lastSample == 0L || var0 - lastSample >= 500000000L) {
         long var2 = OS.getCommittedVirtualMemorySize();
         if (committed > 0L && var2 - committed >= 268435456L) {
            System.out
               .println(
                  String.format(
                     "[Viewpoint] process memory jumped %+d MiB in %.1f s: %d MiB committed, %d MiB of the system's commit left",
                     (var2 - committed) / 1048576L,
                     (var0 - lastSample) * 1.0E-9,
                     var2 / 1048576L,
                     OS.getFreeSwapSpaceSize() / 1048576L
                  )
               );
         }

         lastSample = var0;
         committed = var2;
         peak = Math.max(peak, var2);
      }
   }

   static long committed() {
      return committed;
   }

   static String report() {
      Runtime var0 = Runtime.getRuntime();
      long var1 = var0.totalMemory();
      long var3 = var1 - var0.freeMemory();
      StringBuilder var5 = new StringBuilder(
         String.format(
            " | process MiB committed %d (peak %d), heap used %d of %d, direct buffers %d | system MiB free physical %d, commit left %d | gc",
            committed / 1048576L,
            peak / 1048576L,
            var3 / 1048576L,
            var1 / 1048576L,
            DIRECT == null ? 0L : DIRECT.getMemoryUsed() / 1048576L,
            OS.getFreeMemorySize() / 1048576L,
            OS.getFreeSwapSpaceSize() / 1048576L
         )
      );

      for (int var6 = 0; var6 < COLLECTORS.size(); var6++) {
         GarbageCollectorMXBean var7 = COLLECTORS.get(var6);
         long var8 = var7.getCollectionCount();
         long var10 = var7.getCollectionTime();
         var5.append(String.format(" %s %d in %d ms,", var7.getName(), var8 - gcCount[var6], var10 - gcMs[var6]));
         gcCount[var6] = var8;
         gcMs[var6] = var10;
      }

      if (!COLLECTORS.isEmpty()) {
         var5.setLength(var5.length() - 1);
      }

      peak = committed;
      return var5.toString();
   }

   private ProcessMemory() {
   }
}
