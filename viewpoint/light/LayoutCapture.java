package viewpoint.light;

import java.util.List;
import zombie.core.properties.PropertyContainer;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.LosUtil.TestResults;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.iso.objects.IsoCurtain;
import zombie.iso.sprite.IsoSprite;
import zombie.util.list.PZArrayList;

final class LayoutCapture {
   static LightLayouts.Level level(IsoChunk var0, int var1) {
      byte[] var2 = new byte[64];
      int[] var3 = new int[64];
      long[] var4 = new long[64];
      int[] var5 = new int[64];
      boolean var6 = false;

      for (int var7 = 0; var7 < 64; var7++) {
         IsoGridSquare var8 = var0.getGridSquare(var7 % 8, var7 / 8, var1);
         var4[var7] = -1L;
         if (var8 != null) {
            var2[var7] = (byte)(flags(var8) | 1);
            var3[var7] = passes(var8);
            var4[var7] = var8.getRoomID();
            var5[var7] = walls(var8);
            var6 = true;
         }
      }

      return var6 ? new LightLayouts.Level(var0.wx, var0.wy, var1, var2, var3, var4, var5) : null;
   }

   static LightLayouts.Level facing(IsoCell var0, LightLayouts.Level var1, int var2, int var3) {
      int[] var4 = null;
      int var5 = var1.chunkX * 8;
      int var6 = var1.chunkY * 8;
      int var7 = var2 * 8;
      int var8 = var3 * 8;

      for (int var9 = 0; var9 < 64; var9++) {
         int var10 = var5 + var9 % 8;
         int var11 = var6 + var9 / 8;
         boolean var12 = var10 >= var7 - 1 && var10 <= var7 + 8 && var11 >= var8 - 1 && var11 <= var8 + 8;
         if (var12 && (var1.flags[var9] & 1) != 0) {
            IsoGridSquare var13 = var0.getGridSquare(var10, var11, var1.level);
            int var14 = var13 == null ? var1.passes[var9] : passes(var13);
            if (var14 != var1.passes[var9]) {
               var4 = var4 == null ? (int[])var1.passes.clone() : var4;
               var4[var9] = var14;
            }
         }
      }

      return var4 == null ? var1 : new LightLayouts.Level(var1.chunkX, var1.chunkY, var1.level, var1.flags, var4, var1.rooms, var1.walls);
   }

   static LightLayouts.Level edges(IsoCell var0, LightLayouts.Level var1, LightLayouts.Level var2) {
      if (var1 != null && var2 != null) {
         int[] var3 = null;
         int var4 = var1.chunkX * 8;
         int var5 = var1.chunkY * 8;

         for (int var6 = 0; var6 < 64; var6++) {
            int var7 = var6 % 8;
            int var8 = var6 / 8;
            boolean var9 = var7 == 0 || var8 == 0 || var7 == 7 || var8 == 7;
            if (var9 && (var1.flags[var6] & 1) != 0 && (var2.flags[var6] & 1) != 0) {
               for (int var10 = 0; var10 < 8; var10++) {
                  int var11 = var7 + LightLayout.DX[var10];
                  int var12 = var8 + LightLayout.DY[var10];
                  boolean var13 = var11 < 0 || var12 < 0 || var11 >= 8 || var12 >= 8;
                  int var14 = var3 == null ? var1.passes[var6] : var3[var6];
                  int var15 = 7 << var10 * 3;
                  if (var13 && var0.getGridSquare(var4 + var11, var5 + var12, var1.level) == null && (var14 & var15) != (var2.passes[var6] & var15)) {
                     var3 = var3 == null ? (int[])var1.passes.clone() : var3;
                     var3[var6] = var14 & ~var15 | var2.passes[var6] & var15;
                  }
               }
            }
         }

         return var3 == null ? var1 : new LightLayouts.Level(var1.chunkX, var1.chunkY, var1.level, var1.flags, var3, var1.rooms, var1.walls);
      } else {
         return var1;
      }
   }

   private static int walls(IsoGridSquare var0) {
      int var1 = 0;
      int var2 = 0;
      PZArrayList var3 = var0.getObjects();

      for (int var4 = 0; var4 < var3.size(); var4++) {
         IsoSprite var5 = ((IsoObject)var3.get(var4)).getSprite();
         if (var5 != null) {
            PropertyContainer var6 = var5.getProperties();
            boolean var7 = var6.has(IsoFlagType.WallNW);
            if (var1 == 0 && (var7 || var6.has(IsoFlagType.WallN) || var6.has(IsoFlagType.WindowN) || var6.has(IsoFlagType.DoorWallN))) {
               var1 = TileNumbers.of(var5);
            }

            if (var2 == 0 && (var7 || var6.has(IsoFlagType.WallW) || var6.has(IsoFlagType.WindowW) || var6.has(IsoFlagType.DoorWallW))) {
               var2 = TileNumbers.of(var5);
            }
         }
      }

      return var1 | var2 << 16;
   }

   private static int flags(IsoGridSquare var0) {
      int var1 = (var0.getOpenAir() ? 2 : 0) | (var0.isOutside() && !var0.haveRoof ? 64 : 0) | (var0.getRoomID() == -1L ? 128 : 0);
      List var2 = var0.getSpecialObjects();

      for (int var3 = 0; var3 < var2.size(); var3++) {
         if (var2.get(var3) instanceof IsoCurtain var4 && !var4.IsOpen()) {
            var1 |= curtain(var4.getType());
         }
      }

      return var1;
   }

   private static int curtain(IsoObjectType var0) {
      return var0 == IsoObjectType.curtainN
         ? 4
         : (var0 == IsoObjectType.curtainE ? 8 : (var0 == IsoObjectType.curtainS ? 16 : (var0 == IsoObjectType.curtainW ? 32 : 0)));
   }

   static int passes(IsoGridSquare var0) {
      int var1 = 0;

      for (int var2 = 0; var2 < 10; var2++) {
         TestResults var3 = var0.testVisionAdjacent(LightLayout.DX[var2], LightLayout.DY[var2], LightLayout.DZ[var2], true, false);

         byte var4 = switch (var3) {
            case Clear -> 0;
            case ClearThroughOpenDoor -> 1;
            case ClearThroughWindow -> 2;
            case ClearThroughClosedDoor -> 3;
            case Blocked -> 4;
            default -> throw new IncompatibleClassChangeError();
         };
         var1 |= var4 << var2 * 3;
      }

      return var1;
   }

   private LayoutCapture() {
   }
}
