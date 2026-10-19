package viewpoint.platform;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public final class VideoMemory {
   private static final long KIB = 1024L;
   private static final long SAMPLE_NANOS = 500000000L;
   private static Boolean available;
   private static long lastSample;
   private static int count;
   private static int freeKib;
   private static int totalKib;
   private static int lineEvictions;
   private static long evictedKib;
   private static long lineEvictedKib;

   public static void sample(long var0) {
      if (available == null) {
         available = GL.getCapabilities().GL_NVX_gpu_memory_info;
         totalKib = available ? GL11.glGetInteger(36935) : 0;
      }

      if (available && (lastSample == 0L || var0 - lastSample >= 500000000L)) {
         int var2 = GL11.glGetInteger(36938);
         long var3 = Integer.toUnsignedLong(GL11.glGetInteger(36939));
         freeKib = GL11.glGetInteger(36937);
         if (lastSample != 0L && var2 > count) {
            lineEvictions = lineEvictions + (var2 - count);
            lineEvictedKib = lineEvictedKib + (var3 - evictedKib);
            System.out
               .println(
                  String.format(
                     "[Viewpoint] video memory: %d evictions (%d MiB) in %.1f s, %d MiB of %d free",
                     var2 - count,
                     (var3 - evictedKib) / 1024L,
                     (var0 - lastSample) * 1.0E-9,
                     freeKib / 1024L,
                     totalKib / 1024L
                  )
               );
         }

         count = var2;
         evictedKib = var3;
         lastSample = var0;
      }
   }

   static String report() {
      if (available != null && available) {
         String var0 = String.format(
            " | video memory MiB free %d of %d, evictions %d (%d MiB)", freeKib / 1024L, totalKib / 1024L, lineEvictions, lineEvictedKib / 1024L
         );
         lineEvictions = 0;
         lineEvictedKib = 0L;
         return var0;
      } else {
         return " | video memory: the driver does not tell it";
      }
   }

   static int freeMiB() {
      return (int)(freeKib / 1024L);
   }

   static int totalMiB() {
      return (int)(totalKib / 1024L);
   }

   static int evictions() {
      return count;
   }

   private VideoMemory() {
   }
}
