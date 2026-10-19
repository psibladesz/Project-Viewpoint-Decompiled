package viewpoint.light;

import java.util.Arrays;
import java.util.List;

final class SourceLight {
   static final SourceLight NONE = new SourceLight(0, 0, 0, 0, 0, 0, new int[0]);
   final int x0;
   final int y0;
   final int z0;
   final int width;
   final int height;
   final int levels;
   private final int[] light;

   private SourceLight(int var1, int var2, int var3, int var4, int var5, int var6, int[] var7) {
      this.x0 = var1;
      this.y0 = var2;
      this.z0 = var3;
      this.width = var4;
      this.height = var5;
      this.levels = var6;
      this.light = var7;
   }

   int at(int var1, int var2, int var3) {
      int var4 = var1 - this.x0;
      int var5 = var2 - this.y0;
      int var6 = var3 - this.z0;
      return var4 >= 0 && var5 >= 0 && var6 >= 0 && var4 < this.width && var5 < this.height && var6 < this.levels
         ? this.light[(var6 * this.height + var5) * this.width + var4]
         : 0;
   }

   long bytes() {
      return this.light.length * 4L + 64L;
   }

   boolean same(SourceLight var1) {
      return this.x0 == var1.x0
         && this.y0 == var1.y0
         && this.z0 == var1.z0
         && this.width == var1.width
         && this.height == var1.height
         && this.levels == var1.levels
         && Arrays.equals(this.light, var1.light);
   }

   static SourceLight.Box box(SourceRegister.Lamp var0) {
      int var1 = var0.radius() - 1;
      int var2 = LightModel.levelsReached(var0.radius());
      return new SourceLight.Box(var0.x() - var1, var0.y() - var1, var0.z() - var2, var0.x() + var1, var0.y() + var1, var0.z() + var2);
   }

   static SourceLight.Box box(SourceRegister.RoomLight var0) {
      byte var1 = 6;
      int var2 = LightModel.levelsReached(7);
      return new SourceLight.Box(
         var0.x() - var1, var0.y() - var1, var0.z() - var2, var0.x() + var0.width() - 1 + var1, var0.y() + var0.height() - 1 + var1, var0.z() + var2
      );
   }

   static SourceLight lamp(LightWork var0, SourceRegister.Lamp var1, SourceLight.Box var2, List<LightLayouts.Level> var3) {
      if (var1.radius() <= 0) {
         return NONE;
      }

      LightLayout var4 = var0.layout(var2.x0(), var2.y0(), var2.z0(), var2.x1(), var2.y1(), var2.z1(), var3);
      int[] var5 = var0.starts(1);
      var5[0] = var4.index(var1.x(), var1.y(), var1.z());
      var0.flood.run(var5, 1, var1.radius());
      return paint(var0, 255.0 * var1.r(), 255.0 * var1.g(), 255.0 * var1.b(), var1.radius());
   }

   static SourceLight room(LightWork var0, SourceRegister.RoomLight var1, SourceLight.Box var2, List<LightLayouts.Level> var3) {
      LightLayout var4 = var0.layout(var2.x0(), var2.y0(), var2.z0(), var2.x1(), var2.y1(), var2.z1(), var3);
      int[] var5 = var0.starts(var1.width() * var1.height());
      int var6 = 0;

      for (int var7 = var1.y(); var7 < var1.y() + var1.height(); var7++) {
         for (int var8 = var1.x(); var8 < var1.x() + var1.width(); var8++) {
            var5[var6++] = var4.index(var8, var7, var1.z());
         }
      }

      var0.flood.run(var5, var6, 7.0);
      return paint(var0, 229.0, 216.0, 209.0, 7.0);
   }

   private static SourceLight paint(LightWork var0, double var1, double var3, double var5, double var7) {
      LightLayout var9 = var0.layout;
      LightFlood var10 = var0.flood;
      int var11 = Integer.MAX_VALUE;
      int var12 = Integer.MAX_VALUE;
      int var13 = Integer.MAX_VALUE;
      int var14 = Integer.MIN_VALUE;
      int var15 = Integer.MIN_VALUE;
      int var16 = Integer.MIN_VALUE;

      for (int var17 = 0; var17 < var10.touchedCount(); var17++) {
         int var18 = var10.touched(var17);
         int var19 = LightModel.light(var10, var18, var1, var3, var5, var7);
         var0.values[var17] = var19;
         if (var19 != 0) {
            int var20 = var18 % var9.width;
            int var21 = var18 / var9.width % var9.height;
            int var22 = var18 / (var9.width * var9.height);
            var11 = Math.min(var11, var20);
            var12 = Math.min(var12, var21);
            var13 = Math.min(var13, var22);
            var14 = Math.max(var14, var20);
            var15 = Math.max(var15, var21);
            var16 = Math.max(var16, var22);
         }
      }

      if (var11 > var14) {
         return NONE;
      }

      int var25 = var14 - var11 + 1;
      int var26 = var15 - var12 + 1;
      int[] var27 = new int[var25 * var26 * (var16 - var13 + 1)];

      for (int var28 = 0; var28 < var10.touchedCount(); var28++) {
         int var29 = var10.touched(var28);
         int var30 = var29 % var9.width;
         int var23 = var29 / var9.width % var9.height;
         int var24 = var29 / (var9.width * var9.height);
         if (var0.values[var28] != 0) {
            var27[((var24 - var13) * var26 + var23 - var12) * var25 + var30 - var11] = var0.values[var28];
         }
      }

      return new SourceLight(var9.x0 + var11, var9.y0 + var12, var9.z0 + var13, var25, var26, var16 - var13 + 1, var27);
   }

   record Box(int x0, int y0, int z0, int x1, int y1, int z1) {
   }
}
