package viewpoint.platform;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GLCapabilities;

public final class FrameTimes {
   static final int KEPT = 4096;
   static final int SAFE = 4064;
   private static final int PAIRS = 6;
   private static final float NANOS_A_MS = 1000000.0F;
   static final long[] at = new long[4096];
   static final float[] intervalMs = new float[4096];
   static final float[] gpuMs = new float[4096];
   static volatile long count;
   static volatile long renderThreadId = -1L;
   static volatile Boolean gpuTimed;
   private static long lastSwap;
   private static int[] starts;
   private static int[] ends;
   private static final long[] pairFrame = new long[]{-1L, -1L, -1L, -1L, -1L, -1L};
   private static int open = -1;

   private FrameTimes() {
   }

   public static void renderStarted() {
      if (!Boolean.FALSE.equals(gpuTimed)) {
         try {
            if (gpuTimed == null && !prepare()) {
               return;
            }

            takeResults();
            open = freePair();
            if (open >= 0) {
               GL33.glQueryCounter(starts[open], 36392);
            }
         } catch (RuntimeException var1) {
            gpuTimed = false;
            open = -1;
         }
      }
   }

   public static void renderEnded() {
      if (open >= 0) {
         try {
            GL33.glQueryCounter(ends[open], 36392);
            pairFrame[open] = count;
         } catch (RuntimeException var1) {
            gpuTimed = false;
         }

         open = -1;
      }
   }

   public static void swapped() {
      long var0 = System.nanoTime();
      if (renderThreadId < 0L) {
         renderThreadId = Thread.currentThread().getId();
      }

      if (lastSwap > 0L) {
         long var2 = count;
         int var4 = (int)(var2 & 4095L);
         at[var4] = var0;
         intervalMs[var4] = (float)(var0 - lastSwap) / 1000000.0F;
         gpuMs[var4] = 0.0F;
         count = var2 + 1L;
      }

      lastSwap = var0;
   }

   private static boolean prepare() {
      GLCapabilities var0 = GL.getCapabilities();
      gpuTimed = var0.OpenGL33 || var0.GL_ARB_timer_query;
      if (gpuTimed) {
         starts = new int[6];
         ends = new int[6];
         GL15.glGenQueries(starts);
         GL15.glGenQueries(ends);
      }

      return gpuTimed;
   }

   private static int freePair() {
      for (int var0 = 0; var0 < 6; var0++) {
         if (pairFrame[var0] < 0L) {
            return var0;
         }
      }

      return -1;
   }

   private static void takeResults() {
      long var0 = count;

      for (int var2 = 0; var2 < 6; var2++) {
         long var3 = pairFrame[var2];
         if (var3 >= 0L && var3 < var0 && GL15.glGetQueryObjecti(ends[var2], 34919) != 0) {
            long var5 = GL33.glGetQueryObjecti64(ends[var2], 34918) - GL33.glGetQueryObjecti64(starts[var2], 34918);
            pairFrame[var2] = -1L;
            if (var0 - var3 < 4064L) {
               gpuMs[(int)(var3 & 4095L)] = (float)var5 / 1000000.0F;
            }
         }
      }
   }

   static FrameStats.Frames recent(long var0, long var2) {
      long var4 = count;
      int var6 = (int)Math.min(var4, 4064L);
      float[] var7 = new float[var6];
      long[] var8 = new long[var6];

      int var9;
      for (var9 = 0; var9 < var6; var9++) {
         int var10 = (int)(var4 - 1L - var9 & 4095L);
         if (var0 - at[var10] > var2) {
            break;
         }

         var7[var9] = intervalMs[var10];
         var8[var9] = at[var10];
      }

      return new FrameStats.Frames(var7, var8, var9, var0);
   }
}
