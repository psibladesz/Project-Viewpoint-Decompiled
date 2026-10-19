package viewpoint.light;

import java.util.ArrayList;
import java.util.Arrays;

public final class LampGlass {
   public static final int HEAD_LEFT = 0;
   public static final int HEAD_RIGHT = 1;
   public static final int REAR_LEFT = 2;
   public static final int REAR_RIGHT = 3;
   public static final int LAMPS = 4;
   static final float[][] ZONES = new float[][]{
      {0.25F, 0.0F, 0.0F}, {0.75F, 0.0F, 0.0F}, {0.0F, 0.75F, 0.0F}, {0.0F, 0.25F, 0.0F}, {0.5F, 0.25F, 0.0F}, {0.5F, 0.75F, 0.0F}
   };
   static final int HEAD_R = 0;
   static final int HEAD_L = 1;
   static final int TAIL_R = 2;
   static final int TAIL_L = 3;
   static final int STOP_R = 4;
   static final int STOP_L = 5;
   static final float MIDDLE = 0.1F;
   private static final float MATCH = 0.01F;
   private static final float LIT = 0.1F;
   private static final float SPECK = 0.1F;
   static final float NEAR = 0.4F;
   private static final int MOST_TEXTURES = 4;

   public static LampGlass.Glass[] find(float[] var0, float[] var1, int[] var2, float[] var3, LampGlass.Image var4, LampGlass.Image var5) {
      float[] var6 = placed(var0, var3);
      LampGlass.Samples[] var7 = samples(var6, var1, var2, var4, var5);
      GlassPieces[] var8 = new GlassPieces[ZONES.length];

      for (int var9 = 0; var9 < ZONES.length; var9++) {
         var8[var9] = new GlassPieces(var7[var9]);
      }

      return new LampGlass.Glass[]{
         either(var8[1], var8[0], 1.0F, false), either(var8[0], var8[1], -1.0F, false), rear(var8, 3, 5, 2, 4, 1.0F), rear(var8, 2, 4, 3, 5, -1.0F)
      };
   }

   static boolean any(LampGlass.Glass[] var0) {
      if (var0 == null) {
         return false;
      }

      for (LampGlass.Glass var4 : var0) {
         if (var4 != null) {
            return true;
         }
      }

      return false;
   }

   private static float[] placed(float[] var0, float[] var1) {
      float[] var2 = new float[var0.length];

      for (int var3 = 0; var3 + 2 < var0.length; var3 += 3) {
         float var4 = var0[var3];
         float var5 = var0[var3 + 1];
         float var6 = var0[var3 + 2];
         var2[var3] = var1[0] * var4 + var1[4] * var5 + var1[8] * var6 + var1[12];
         var2[var3 + 1] = var1[1] * var4 + var1[5] * var5 + var1[9] * var6 + var1[13];
         var2[var3 + 2] = var1[2] * var4 + var1[6] * var5 + var1[10] * var6 + var1[14];
      }

      return var2;
   }

   private static LampGlass.Samples[] samples(float[] var0, float[] var1, int[] var2, LampGlass.Image var3, LampGlass.Image var4) {
      LampGlass.Samples[] var5 = new LampGlass.Samples[ZONES.length];

      for (int var6 = 0; var6 < ZONES.length; var6++) {
         var5[var6] = new LampGlass.Samples();
      }

      LampGlass.Texels var9 = new LampGlass.Texels(var3, var4);
      if (var9.none()) {
         return var5;
      }

      float[] var7 = middle(var0);

      for (int var8 = 0; var8 + 2 < var2.length; var8 += 3) {
         var9.cover(var0, var1, var2[var8], var2[var8 + 1], var2[var8 + 2], var7, var5);
      }

      return var5;
   }

   private static float[] middle(float[] var0) {
      float[] var1 = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
      float[] var2 = new float[]{-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};

      for (int var3 = 0; var3 < var0.length; var3++) {
         var1[var3 % 3] = Math.min(var1[var3 % 3], var0[var3]);
         var2[var3 % 3] = Math.max(var2[var3 % 3], var0[var3]);
      }

      return new float[]{(var1[0] + var2[0]) * 0.5F, (var1[1] + var2[1]) * 0.5F, (var1[2] + var2[2]) * 0.5F};
   }

   private static LampGlass.Glass either(GlassPieces var0, GlassPieces var1, float var2, boolean var3) {
      LampGlass.Glass var4 = one(var0, var2, var3, false);
      return var4 != null ? var4 : one(var1, var2, var3, true);
   }

   private static LampGlass.Glass rear(GlassPieces[] var0, int var1, int var2, int var3, int var4, float var5) {
      boolean var6 = heaviest(var0[var1], var5, true, null) <= 0.0F;
      GlassPieces var7 = var0[var6 ? var3 : var1];
      GlassPieces var8 = var0[var6 ? var4 : var2];
      LampGlass.Glass var9 = one(var7, var5, true, var6);
      if (var9 == null) {
         return either(var0[var2], var0[var4], var5, true);
      }

      LampGlass.Moments var10 = new LampGlass.Moments();
      collect(var10, var7, var5, true, null);
      collect(var10, var8, var5, true, var9);
      return var10.glass(var6);
   }

   private static LampGlass.Glass one(GlassPieces var0, float var1, boolean var2, boolean var3) {
      LampGlass.Moments var4 = new LampGlass.Moments();
      collect(var4, var0, var1, var2, null);
      return var4.empty() ? null : var4.glass(var3);
   }

   private static void collect(LampGlass.Moments var0, GlassPieces var1, float var2, boolean var3, LampGlass.Glass var4) {
      float var5 = heaviest(var1, var2, var3, var4);

      for (int var6 = 0; var6 < var1.count; var6++) {
         if (fits(var1, var6, var2, var3, var4) && var1.weight[var6] >= var5 * 0.1F) {
            var0.add(var1, var6);
         }
      }
   }

   private static float heaviest(GlassPieces var0, float var1, boolean var2, LampGlass.Glass var3) {
      float var4 = 0.0F;

      for (int var5 = 0; var5 < var0.count; var5++) {
         if (fits(var0, var5, var1, var2, var3)) {
            var4 = Math.max(var4, var0.weight[var5]);
         }
      }

      return var4;
   }

   private static boolean fits(GlassPieces var0, int var1, float var2, boolean var3, LampGlass.Glass var4) {
      if (var0.x(var1) * var2 <= 0.1F || var0.z(var1) * (var3 ? -1.0F : 1.0F) <= 0.0F) {
         return false;
      }

      if (var4 == null) {
         return true;
      }

      float var5 = var0.x(var1) - var4.x();
      float var6 = var0.y(var1) - var4.y();
      float var7 = var0.z(var1) - var4.z();
      return var5 * var5 + var6 * var6 + var7 * var7 <= 0.16000001F;
   }

   private LampGlass() {
   }

   public record Glass(float x, float y, float z, float nx, float ny, float nz, float size, int pieces, boolean shared) {
   }

   public record Image(byte[] rgba, int width, int height) {
   }

   private static final class Moments {
      private final ArrayList<GlassPieces> from = new ArrayList<>();
      private final ArrayList<Integer> which = new ArrayList<>();
      private float weight;
      private float x;
      private float y;
      private float z;
      private float nx;
      private float ny;
      private float nz;

      void add(GlassPieces var1, int var2) {
         this.from.add(var1);
         this.which.add(var2);
         float var3 = var1.weight[var2];
         this.weight += var3;
         this.x = this.x + var1.place[3 * var2];
         this.y = this.y + var1.place[3 * var2 + 1];
         this.z = this.z + var1.place[3 * var2 + 2];
         this.nx = this.nx + var1.facing[3 * var2];
         this.ny = this.ny + var1.facing[3 * var2 + 1];
         this.nz = this.nz + var1.facing[3 * var2 + 2];
      }

      boolean empty() {
         return this.from.isEmpty();
      }

      LampGlass.Glass glass(boolean var1) {
         float var2 = this.x / this.weight;
         float var3 = this.y / this.weight;
         float var4 = this.z / this.weight;
         float var5 = (float)Math.sqrt(this.nx * this.nx + this.ny * this.ny + this.nz * this.nz);
         float[] var6 = var5 > 0.0F ? new float[]{this.nx / var5, this.ny / var5, this.nz / var5} : new float[]{0.0F, 0.0F, 1.0F};
         float[] var7 = new float[]{var6[2], 0.0F, -var6[0]};
         float var8 = (float)Math.hypot(var7[0], var7[2]);
         if (var8 < 0.001F) {
            var7[0] = 1.0F;
            var7[2] = 0.0F;
            var8 = 1.0F;
         }

         var7[0] /= var8;
         var7[2] /= var8;
         float[] var9 = new float[]{var6[1] * var7[2], var6[2] * var7[0] - var6[0] * var7[2], -var6[1] * var7[0]};
         float[] var10 = new float[2];

         for (int var11 = 0; var11 < this.from.size(); var11++) {
            this.from.get(var11).spread(this.which.get(var11), var2, var3, var4, var7, var9, var10);
         }

         float var13 = (float)Math.sqrt(3.0F * var10[0] / this.weight);
         float var12 = (float)Math.sqrt(3.0F * var10[1] / this.weight);
         return new LampGlass.Glass(var2, var3, var4, var6[0], var6[1], var6[2], 2.0F * (float)Math.sqrt(var13 * var12), this.from.size(), var1);
      }
   }

   static final class Samples {
      static final int STRIDE = 7;
      float[] data = new float[448];
      int count;

      private void add(LampGlass.Triangle var1, float var2, float var3, float var4, float var5) {
         float var6 = var1.perTexel * var5;
         if (!(var6 <= 0.0F)) {
            if ((this.count + 1) * 7 > this.data.length) {
               this.data = Arrays.copyOf(this.data, this.data.length * 2);
            }

            int var7 = this.count++ * 7;
            this.data[var7] = var1.at(0, var2, var3, var4);
            this.data[var7 + 1] = var1.at(1, var2, var3, var4);
            this.data[var7 + 2] = var1.at(2, var2, var3, var4);
            this.data[var7 + 3] = var1.nx;
            this.data[var7 + 4] = var1.ny;
            this.data[var7 + 5] = var1.nz;
            this.data[var7 + 6] = var6;
         }
      }
   }

   private static final class Texels {
      private final int width;
      private final int height;
      private final byte[] zone;
      private final float[] shows;
      private final int[] lit;

      Texels(LampGlass.Image var1, LampGlass.Image var2) {
         this.width = var1.width();
         this.height = var1.height();
         this.zone = new byte[this.width * this.height];
         this.shows = new float[this.width * this.height];
         this.lit = new int[(this.width + 1) * (this.height + 1)];

         for (int var3 = 0; var3 < this.height; var3++) {
            for (int var4 = 0; var4 < this.width; var4++) {
               int var5 = var3 * this.width + var4;
               int var6 = zoneOf(var1.rgba(), var5 * 4);
               this.shows[var5] = var6 < 0 ? 0.0F : lightsAt(var2, (var4 + 0.5F) / this.width, (var3 + 0.5F) / this.height);
               this.zone[var5] = (byte)(this.shows[var5] < 0.1F ? -1 : var6);
               this.lit[(var3 + 1) * (this.width + 1) + var4 + 1] = (this.zone[var5] >= 0 ? 1 : 0)
                  + this.lit[var3 * (this.width + 1) + var4 + 1]
                  + this.lit[(var3 + 1) * (this.width + 1) + var4]
                  - this.lit[var3 * (this.width + 1) + var4];
            }
         }
      }

      boolean none() {
         return this.lit[this.lit.length - 1] == 0;
      }

      private static int zoneOf(byte[] var0, int var1) {
         float var2 = (var0[var1] & 0xFF) / 255.0F;
         float var3 = (var0[var1 + 1] & 0xFF) / 255.0F;
         float var4 = (var0[var1 + 2] & 0xFF) / 255.0F;

         for (int var5 = 0; var5 < LampGlass.ZONES.length; var5++) {
            float var6 = var2 - LampGlass.ZONES[var5][0];
            float var7 = var3 - LampGlass.ZONES[var5][1];
            float var8 = var4 - LampGlass.ZONES[var5][2];
            if (var6 * var6 + var7 * var7 + var8 * var8 < 1.0E-4F) {
               return var5;
            }
         }

         return -1;
      }

      private static float lightsAt(LampGlass.Image var0, float var1, float var2) {
         if (var0 == null) {
            return 1.0F;
         }

         int var3 = Math.min(var0.width() - 1, (int)(var1 * var0.width()));
         int var4 = Math.min(var0.height() - 1, (int)(var2 * var0.height()));
         return (var0.rgba()[(var4 * var0.width() + var3) * 4 + 3] & 0xFF) / 255.0F;
      }

      void cover(float[] var1, float[] var2, int var3, int var4, int var5, float[] var6, LampGlass.Samples[] var7) {
         float var8 = var2[2 * var3] * this.width;
         float var9 = var2[2 * var3 + 1] * this.height;
         float var10 = var2[2 * var4] * this.width;
         float var11 = var2[2 * var4 + 1] * this.height;
         float var12 = var2[2 * var5] * this.width;
         float var13 = var2[2 * var5 + 1] * this.height;
         float var14 = (var10 - var8) * (var13 - var9) - (var11 - var9) * (var12 - var8);
         int var15 = (int)Math.ceil(Math.min(var8, Math.min(var10, var12)) - 0.5F);
         int var16 = (int)Math.floor(Math.max(var8, Math.max(var10, var12)) - 0.5F);
         int var17 = (int)Math.ceil(Math.min(var9, Math.min(var11, var13)) - 0.5F);
         int var18 = (int)Math.floor(Math.max(var9, Math.max(var11, var13)) - 0.5F);
         if (!(Math.abs(var14) < 1.0E-9F)
            && var16 >= var15
            && var18 >= var17
            && (long)(var16 - var15 + 1) * (var18 - var17 + 1) <= 4L * this.width * this.height
            && this.anyLit(var15, var17, var16, var18)) {
            LampGlass.Triangle var19 = new LampGlass.Triangle(var1, var3, var4, var5, var6, Math.abs(var14));

            for (int var20 = var17; var20 <= var18; var20++) {
               for (int var21 = var15; var21 <= var16; var21++) {
                  float var22 = var21 + 0.5F;
                  float var23 = var20 + 0.5F;
                  float var24 = ((var10 - var22) * (var13 - var23) - (var11 - var23) * (var12 - var22)) / var14;
                  float var25 = ((var12 - var22) * (var9 - var23) - (var13 - var23) * (var8 - var22)) / var14;
                  float var26 = 1.0F - var24 - var25;
                  int var27 = Math.floorMod(var20, this.height) * this.width + Math.floorMod(var21, this.width);
                  if (var24 >= -1.0E-5F && var25 >= -1.0E-5F && var26 >= -1.0E-5F && this.zone[var27] >= 0) {
                     var7[this.zone[var27]].add(var19, var24, var25, var26, this.shows[var27]);
                  }
               }
            }
         }
      }

      private boolean anyLit(int var1, int var2, int var3, int var4) {
         if (var1 >= 0 && var2 >= 0 && var3 < this.width && var4 < this.height) {
            int var5 = this.width + 1;
            return this.lit[(var4 + 1) * var5 + var3 + 1]
                  - this.lit[var2 * var5 + var3 + 1]
                  - this.lit[(var4 + 1) * var5 + var1]
                  + this.lit[var2 * var5 + var1]
               > 0;
         } else {
            return true;
         }
      }
   }

   private static final class Triangle {
      final float[] local;
      final int a;
      final int b;
      final int c;
      final float nx;
      final float ny;
      final float nz;
      final float perTexel;

      Triangle(float[] var1, int var2, int var3, int var4, float[] var5, float var6) {
         this.local = var1;
         this.a = var2;
         this.b = var3;
         this.c = var4;
         float var7 = this.at(var3, 0) - this.at(var2, 0);
         float var8 = this.at(var3, 1) - this.at(var2, 1);
         float var9 = this.at(var3, 2) - this.at(var2, 2);
         float var10 = this.at(var4, 0) - this.at(var2, 0);
         float var11 = this.at(var4, 1) - this.at(var2, 1);
         float var12 = this.at(var4, 2) - this.at(var2, 2);
         float var13 = var8 * var12 - var9 * var11;
         float var14 = var9 * var10 - var7 * var12;
         float var15 = var7 * var11 - var8 * var10;
         float var16 = (float)Math.sqrt(var13 * var13 + var14 * var14 + var15 * var15);
         float var17 = (this.at(var2, 0) + this.at(var3, 0) + this.at(var4, 0)) / 3.0F - var5[0];
         float var18 = (this.at(var2, 1) + this.at(var3, 1) + this.at(var4, 1)) / 3.0F - var5[1];
         float var19 = (this.at(var2, 2) + this.at(var3, 2) + this.at(var4, 2)) / 3.0F - var5[2];
         float var20 = (var13 * var17 + var14 * var18 + var15 * var19 < 0.0F ? -1.0F : 1.0F) / Math.max(var16, 1.0E-12F);
         this.nx = var13 * var20;
         this.ny = var14 * var20;
         this.nz = var15 * var20;
         this.perTexel = var16 * 0.5F / var6;
      }

      private float at(int var1, int var2) {
         return this.local[3 * var1 + var2];
      }

      float at(int var1, float var2, float var3, float var4) {
         return var2 * this.at(this.a, var1) + var3 * this.at(this.b, var1) + var4 * this.at(this.c, var1);
      }
   }
}
