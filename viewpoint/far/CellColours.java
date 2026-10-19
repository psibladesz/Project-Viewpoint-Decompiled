package viewpoint.far;

import viewpoint.platform.LiveSettings;
import viewpoint.render.FarGpu;

final class CellColours {
   private static final LiveSettings.Number FULL = LiveSettings.number(
      "lod.farColoursFullBlocks", "Far colours at full size up to (blocks)", "World/Levels of detail", 0.0F, 64.0F, 1.0F, 24.0F
   );
   private static final LiveSettings.Number HALF = LiveSettings.number(
      "lod.farColoursHalfBlocks", "Far colours at half size up to (blocks)", "World/Levels of detail", 0.0F, 64.0F, 1.0F, 48.0F
   );
   private static final float HOLD = 128.0F;

   static int size(float var0) {
      if (var0 < FULL.get() * 64.0F) {
         return 256;
      } else {
         return var0 < HALF.get() * 64.0F ? 128 : 64;
      }
   }

   static boolean tooSmall(FarGpu.Colours var0, float var1) {
      return var0 != null && var0.size < size(var1);
   }

   static boolean tooLarge(FarGpu.Colours var0, float var1) {
      return var0 != null && var0.size > size(Math.max(var1 - 128.0F, 0.0F));
   }

   static int[] shrink(int[] var0, int var1) {
      int var2 = 256 / var1;
      if (var2 <= 1) {
         return var0;
      }

      int[] var3 = new int[var1 * var1];
      int var4 = var2 * var2;

      for (int var5 = 0; var5 < var1; var5++) {
         for (int var6 = 0; var6 < var1; var6++) {
            int var7 = 0;
            int var8 = 0;
            int var9 = 0;

            for (int var10 = 0; var10 < var2; var10++) {
               int var11 = (var5 * var2 + var10) * 256 + var6 * var2;

               for (int var12 = 0; var12 < var2; var12++) {
                  int var13 = var0[var11 + var12];
                  var7 += var13 & 0xFF;
                  var8 += var13 >> 8 & 0xFF;
                  var9 += var13 >> 16 & 0xFF;
               }
            }

            var3[var5 * var1 + var6] = var7 / var4 | var8 / var4 << 8 | var9 / var4 << 16;
         }
      }

      return var3;
   }

   private CellColours() {
   }

   static {
      FULL.describe(
         "Nearer than this the far world's ground and walls have a colour for every square; further, one for every two squares, a quarter of the memory."
      );
      HALF.describe("Further than this, one colour for every four squares, a sixteenth of the memory.");
   }
}
