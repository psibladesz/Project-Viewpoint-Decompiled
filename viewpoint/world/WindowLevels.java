package viewpoint.world;

import viewpoint.platform.LiveSettings;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoWorld;

final class WindowLevels {
   static final LiveSettings.Toggle PER_BUILDING = LiveSettings.toggle("roofs.perBuilding", "One roof per building, over all its levels", "World/Roofs", true);
   private static final int SIDE = 8;
   private static final int SQUARES = 64;
   private static final int ABOVE = 2;

   private WindowLevels() {
   }

   static RoofWindow attach(RoofWindow var0, int var1, IsoChunk var2, int var3, int var4, int var5, int var6) {
      if (!PER_BUILDING.get()) {
         return var0;
      }

      IsoCell var7 = IsoWorld.instance.getCell();
      int var8 = var2.wx + var3;
      int var9 = var2.wy + var4;
      int var10 = var1 + 2;
      int var11 = var5 * 8;
      int var12 = var6 * 8;

      for (int var13 = 0; var13 < var6; var13++) {
         for (int var14 = 0; var14 < var5; var14++) {
            IsoChunk var15 = var7.getChunk(var8 + var14, var9 + var13);
            var10 = var15 == null ? var10 : Math.max(var10, var15.maxLevel + 2);
         }
      }

      RoofPiece[][] var25 = new RoofPiece[var10 + 1][var11 * var12];
      boolean[][] var26 = new boolean[var10 + 1][var11 * var12];
      boolean[][] var27 = new boolean[var10 + 1][var11 * var12];
      boolean[][] var16 = new boolean[var10 + 1][var11 * var12];
      boolean[][] var17 = new boolean[var10 + 1][var11 * var12];
      boolean[][] var18 = new boolean[var10 + 1][var11 * var12];

      for (int var19 = 0; var19 <= var10; var19++) {
         for (int var20 = 0; var20 < var6; var20++) {
            for (int var21 = 0; var21 < var5; var21++) {
               RoofLevels.Level var22 = RoofLevels.get(var7, var8 + var21, var9 + var20, var19);

               for (int var23 = 0; var22 != null && var23 < 64; var23++) {
                  int var24 = (var20 * 8 + var23 / 8) * var11 + var21 * 8 + var23 % 8;
                  var25[var19][var24] = var22.pieces[var23];
                  var26[var19][var24] = (var22.bits[var23] & 1) != 0;
                  var27[var19][var24] = (var22.bits[var23] & 2) != 0;
                  var16[var19][var24] = var22.lid[var23] == 1;
                  var17[var19][var24] = (var22.bits[var23] & 4) != 0;
                  var18[var19][var24] = var22.porch[var23];
               }
            }
         }
      }

      return var0.perBuilding(new RoofBuildings.Levels(var11, var12, var25, var26, var27, var16, var17, var18), var1);
   }
}
