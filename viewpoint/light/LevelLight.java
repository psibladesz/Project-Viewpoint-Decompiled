package viewpoint.light;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

final class LevelLight {
   private static final int RING = 12;
   final int chunkX;
   final int chunkY;
   final int level;
   final byte[] grid;
   final int[] light;
   final byte[] steps;

   private LevelLight(int var1, int var2, int var3, byte[] var4, int[] var5, byte[] var6) {
      this.chunkX = var1;
      this.chunkY = var2;
      this.level = var3;
      this.grid = var4;
      this.light = var5;
      this.steps = var6;
   }

   long bytes() {
      return this.grid.length + this.light.length * 4L + this.steps.length + 64L;
   }

   static int firstSquare(int var0) {
      return var0 * 8 - 2 - 15;
   }

   static int lastSquare(int var0) {
      return firstSquare(var0) + 12 - 1 + 30;
   }

   static LevelLight work(LightWork var0, int var1, int var2, int var3, List<LightLayouts.Level> var4, List<SourceLight> var5) {
      LightLayout var6 = var0.layout(firstSquare(var1), firstSquare(var2), var3, lastSquare(var1), lastSquare(var2), var3, var4);
      LightModel.skySteps(var6, var0.steps, var0.queue);
      LevelLight.Ring var7 = new LevelLight.Ring(var1, var2, var3, var4);
      int var8 = var1 * 8 - 2;
      int var9 = var2 * 8 - 2;

      for (int var10 = 0; var10 < 144; var10++) {
         int var11 = var8 + var10 % 12;
         int var12 = var9 + var10 / 12;
         int var13 = var6.index(var11, var12, var3);
         var7.exists[var10] = var6.exists(var13);
         var7.outdoors[var10] = !var7.exists[var10] || var6.has(var13, 64);
         var7.steps[var10] = var7.exists[var10] ? var0.steps[var13] : 0;
         var7.light[var10] = 0;

         for (SourceLight var15 : var5) {
            var7.light[var10] = LightModel.brighter(var7.light[var10], var15.at(var11, var12, var3));
         }
      }

      byte[] var16 = new byte[3200];
      var7.write(ByteBuffer.wrap(var16));
      int[] var17 = new int[64];
      byte[] var18 = new byte[64];

      for (int var19 = 0; var19 < 64; var19++) {
         int var20 = (var19 / 8 + 2) * 12 + var19 % 8 + 2;
         var17[var19] = var7.light[var20];
         var18[var19] = var7.steps[var20];
      }

      return new LevelLight(var1, var2, var3, var16, var17, var18);
   }

   private static final class Ring {
      final boolean[] exists = new boolean[144];
      final boolean[] outdoors = new boolean[144];
      final long[] rooms = new long[144];
      final int[] light = new int[144];
      final byte[] steps = new byte[144];
      final int[] corners = new int[4];
      final int[] darker = new int[4];

      Ring(int var1, int var2, int var3, List<LightLayouts.Level> var4) {
         int var5 = var1 * 8 - 2;
         int var6 = var2 * 8 - 2;
         Arrays.fill(this.rooms, -1L);

         for (LightLayouts.Level var8 : var4) {
            if (var8.level == var3 && Math.abs(var8.chunkX - var1) <= 1 && Math.abs(var8.chunkY - var2) <= 1) {
               for (int var9 = 0; var9 < 64; var9++) {
                  int var10 = var8.chunkX * 8 + var9 % 8 - var5;
                  int var11 = var8.chunkY * 8 + var9 / 8 - var6;
                  if (var10 >= 0 && var10 < 12 && var11 >= 0 && var11 < 12) {
                     this.rooms[var11 * 12 + var10] = var8.rooms[var9];
                  }
               }
            }
         }
      }

      void write(ByteBuffer var1) {
         for (int var2 = 0; var2 < 10; var2++) {
            for (int var3 = 0; var3 < 10; var3++) {
               int var4 = (var2 + 1) * 12 + var3 + 1;

               for (int var5 = 0; var5 < 4; var5++) {
                  this.corner(var4, var5);
               }

               GridTexels.light(var1, var3, var2, this.corners, this.outdoors[var4]);
               GridTexels.sky(var1, var3, var2, this.darker);
            }
         }
      }

      private void corner(int var1, int var2) {
         if (!this.exists[var1]) {
            this.corners[var2] = this.darker[var2] = 0;
         } else {
            int var3 = var2 != 1 && var2 != 2 ? -1 : 1;
            int var4 = var2 >= 2 ? 12 : -12;
            int var5 = 0;
            int var6 = 0;
            int var7 = 0;
            int var8 = 0;
            int var9 = 0;

            for (int var10 = 0; var10 < 4; var10++) {
               int var11 = var1 + (var10 & 1) * var3 + (var10 >> 1) * var4;
               if (var10 == 0 || this.exists[var11] && this.rooms[var11] == this.rooms[var1]) {
                  var5 += this.light[var11] & 0xFF;
                  var6 += this.light[var11] >> 8 & 0xFF;
                  var7 += this.light[var11] >> 16 & 0xFF;
                  var8 += Math.min(255, 16 * this.steps[var11]);
                  var9++;
               }
            }

            this.corners[var2] = (var5 + var9 / 2) / var9 | (var6 + var9 / 2) / var9 << 8 | (var7 + var9 / 2) / var9 << 16;
            this.darker[var2] = (var8 + var9 / 2) / var9;
         }
      }
   }
}
