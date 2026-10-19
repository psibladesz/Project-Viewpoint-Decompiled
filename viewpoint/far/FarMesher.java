package viewpoint.far;

import java.nio.FloatBuffer;
import java.util.Arrays;
import org.lwjgl.system.MemoryUtil;
import viewpoint.render.FarRelief;

public final class FarMesher {
   private static final ThreadLocal<float[]> SCRATCH = ThreadLocal.withInitial(() -> new float[65536]);
   private static final int STANDING = 4;
   static final int RELIEF_SPLIT = FarRelief.SPLIT;
   private float[] out;
   private int n;
   private float x0;
   private float y0;
   private float inset;
   private int every;

   public static FloatBuffer mesh(short[] var0, int var1) {
      int var2 = 256 / var1;
      int var3 = var1 * var1;
      int var4 = var3 - Math.max(1, var3 / 4);
      short[] var5 = new short[var2 * var2];
      short[] var6 = new short[var3];

      for (int var7 = 0; var7 < var2; var7++) {
         for (int var8 = 0; var8 < var2; var8++) {
            int var9 = 0;

            for (int var10 = var7 * var1; var10 < (var7 + 1) * var1; var10++) {
               for (int var11 = var8 * var1; var11 < (var8 + 1) * var1; var11++) {
                  var6[var9++] = var0[var10 * 256 + var11];
               }
            }

            Arrays.sort(var6);
            var5[var7 * var2 + var8] = var6[var4];
         }
      }

      return boxes(var5, var2, var1, 0.0F, 0.0F, 0.0F, true, RELIEF_SPLIT / var1);
   }

   static FloatBuffer boxes(short[] var0, int var1, int var2, float var3, float var4, float var5, boolean var6, int var7) {
      FarMesher var8 = new FarMesher();
      var8.out = SCRATCH.get();
      var8.x0 = var3;
      var8.y0 = var4;
      var8.inset = var5;
      var8.every = var7;
      var8.tops(var0, var1, var2, var6);
      var8.sides(var0, var1, var2);
      SCRATCH.set(var8.out);
      return MemoryUtil.memAllocFloat(var8.n).put(var8.out, 0, var8.n).flip();
   }

   private void tops(short[] var1, int var2, int var3, boolean var4) {
      boolean[] var5 = new boolean[var2 * var2];

      for (int var6 = 0; var6 < var2; var6++) {
         for (int var7 = 0; var7 < var2; var7++) {
            if (!var5[var6 * var2 + var7]) {
               short var8 = var1[var6 * var2 + var7];
               int var9 = 1;

               while (var7 + var9 < var2 && !var5[var6 * var2 + var7 + var9] && var1[var6 * var2 + var7 + var9] == var8 && !this.line(var7 + var9)) {
                  var9++;
               }

               int var10;
               label73:
               for (var10 = 1; var6 + var10 < var2 && !this.line(var6 + var10); var10++) {
                  for (int var11 = 0; var11 < var9; var11++) {
                     int var12 = (var6 + var10) * var2 + var7 + var11;
                     if (var5[var12] || var1[var12] != var8) {
                        break label73;
                     }
                  }
               }

               for (int var16 = 0; var16 < var10; var16++) {
                  for (int var18 = 0; var18 < var9; var18++) {
                     var5[(var6 + var16) * var2 + var7 + var18] = true;
                  }
               }

               if (var8 != 0 || var4) {
                  float var17 = var8 / 8.0F;
                  float var19 = this.x0 + var7 * var3;
                  float var13 = this.x0 + (var7 + var9) * var3;
                  float var14 = this.y0 + var6 * var3;
                  float var15 = this.y0 + (var6 + var10) * var3;
                  this.quad(var19, var14, var17, var13, var14, var17, var13, var15, var17, var19, var15, var17, 0);
               }
            }
         }
      }
   }

   private void sides(short[] var1, int var2, int var3) {
      for (int var4 = -1; var4 < var2; var4++) {
         float var5 = this.x0 + (var4 + 1) * var3;
         int var6 = 0;

         while (var6 < var2) {
            short var7 = var4 >= 0 ? var1[var6 * var2 + var4] : 0;
            short var8 = var4 + 1 < var2 ? var1[var6 * var2 + var4 + 1] : 0;
            int var9 = 1;

            while (
               var6 + var9 < var2
                  && !this.line(var6 + var9)
                  && (var4 >= 0 ? var1[(var6 + var9) * var2 + var4] : 0) == var7
                  && (var4 + 1 < var2 ? var1[(var6 + var9) * var2 + var4 + 1] : 0) == var8
            ) {
               var9++;
            }

            if (var7 != var8) {
               float var10 = Math.min(var7, var8) / 8.0F;
               float var11 = Math.max(var7, var8) / 8.0F;
               float var12 = this.y0 + var6 * var3;
               float var13 = this.y0 + (var6 + var9) * var3;
               float var14 = var7 > var8 ? var5 - this.inset : var5 + this.inset;
               this.quad(var14, var12, var10, var14, var13, var10, var14, var13, var11, var14, var12, var11, var7 > var8 ? 2 : 1);
            }

            var6 += var9;
         }
      }

      for (int var15 = -1; var15 < var2; var15++) {
         float var16 = this.y0 + (var15 + 1) * var3;
         int var17 = 0;

         while (var17 < var2) {
            short var18 = var15 >= 0 ? var1[var15 * var2 + var17] : 0;
            short var19 = var15 + 1 < var2 ? var1[(var15 + 1) * var2 + var17] : 0;
            int var20 = 1;

            while (
               var17 + var20 < var2
                  && !this.line(var17 + var20)
                  && (var15 >= 0 ? var1[var15 * var2 + var17 + var20] : 0) == var18
                  && (var15 + 1 < var2 ? var1[(var15 + 1) * var2 + var17 + var20] : 0) == var19
            ) {
               var20++;
            }

            if (var18 != var19) {
               float var21 = Math.min(var18, var19) / 8.0F;
               float var22 = Math.max(var18, var19) / 8.0F;
               float var23 = this.x0 + var17 * var3;
               float var24 = this.x0 + (var17 + var20) * var3;
               float var25 = var18 > var19 ? var16 - this.inset : var16 + this.inset;
               this.quad(var23, var25, var21, var24, var25, var21, var24, var25, var22, var23, var25, var22, var18 > var19 ? 4 : 3);
            }

            var17 += var20;
         }
      }
   }

   private boolean line(int var1) {
      return this.every > 0 && var1 % this.every == 0;
   }

   private void quad(
      float var1,
      float var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11,
      float var12,
      int var13
   ) {
      if (this.n + 24 > this.out.length) {
         this.out = Arrays.copyOf(this.out, this.out.length * 2);
      }

      this.vertex(var1, var2, var3, var13);
      this.vertex(var4, var5, var6, var13);
      this.vertex(var7, var8, var9, var13);
      this.vertex(var1, var2, var3, var13);
      this.vertex(var7, var8, var9, var13);
      this.vertex(var10, var11, var12, var13);
   }

   private void vertex(float var1, float var2, float var3, int var4) {
      this.out[this.n++] = var1;
      this.out[this.n++] = var2;
      this.out[this.n++] = var3;
      this.out[this.n++] = var4;
   }
}
