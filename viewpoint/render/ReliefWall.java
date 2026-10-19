package viewpoint.render;

public final class ReliefWall {
   public static final int NONE = 0;
   public static final int THROUGH = 1;
   public static final int FROM_LOW = 2;
   public static final int FROM_HIGH = 3;
   public static final float EPS = 0.015625F;

   private ReliefWall() {
   }

   public static byte code(int var0, int var1, int var2, int var3) {
      int var4 = var0 == 0 ? 0 : var0 | var1 - 1 << 2;
      int var5 = var2 == 0 ? 0 : var2 | var3 - 1 << 2;
      return (byte)(var4 | var5 << 4);
   }

   public static int state(int var0, int var1) {
      return var0 >> 4 * var1 & 3;
   }

   public static int line(int var0, int var1) {
      return (var0 >> 4 * var1 + 2 & 3) + 1;
   }

   public static float bend(int var0, int var1, int var2, double var3, float var5, float var6) {
      int var7 = state(var0, var1);
      if (var7 == 0) {
         return var5;
      }

      double var8 = var2 * 4.0 + line(var0, var1);
      float var10 = (float)Math.max(0.0, Math.min(1.0, (var3 - (var8 - 0.015625)) / 0.015625));
      if (var7 == 1) {
         return var10;
      }

      float var11 = var7 == 2 ? var6 : 1.0F - var6;
      return var10 + (var5 - var10) * var11;
   }
}
