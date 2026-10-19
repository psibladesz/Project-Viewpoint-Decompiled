package viewpoint.far;

import java.util.ArrayList;
import java.util.Arrays;
import zombie.iso.IsoMetaCell;
import zombie.iso.IsoMetaGrid;
import zombie.iso.IsoWorld;
import zombie.iso.RoomDef;
import zombie.iso.RoomDef.RoomRect;

final class BlockRooms {
   private static final int SIZE = 64;
   final long[][] ids;
   final ArrayList<RoomDef> lit = new ArrayList<>();

   private BlockRooms(int var1) {
      this.ids = new long[var1][];
   }

   static BlockRooms of(int var0, int var1, int var2) {
      BlockRooms var3 = new BlockRooms(var2);
      byte var4 = 4;
      IsoMetaGrid var5 = IsoWorld.instance.getMetaGrid();
      int var6 = Math.floorDiv(var0, var4) - var5.minX;
      int var7 = Math.floorDiv(var1, var4) - var5.minY;
      IsoMetaCell var8 = var6 >= 0 && var7 >= 0 && var6 <= var5.maxX - var5.minX && var7 <= var5.maxY - var5.minY ? var5.getCell(var6, var7) : null;
      if (var8 != null) {
         for (RoomDef var10 : var8.roomList) {
            var3.add(var10, var0 * 64, var1 * 64);
         }
      }

      return var3;
   }

   private void add(RoomDef var1, int var2, int var3) {
      if (var1.level >= 0 && var1.level < this.ids.length && !var1.isEmptyOutside() && !var1.rects.isEmpty()) {
         RoomRect var4 = (RoomRect)var1.rects.get(0);
         if (var4.x >= var2 && var4.y >= var3 && var4.x < var2 + 64 && var4.y < var3 + 64) {
            this.lit.add(var1);
         }

         for (RoomRect var6 : var1.rects) {
            for (int var7 = Math.max(0, var6.y - var3); var7 < Math.min(64, var6.y + var6.h - var3); var7++) {
               for (int var8 = Math.max(0, var6.x - var2); var8 < Math.min(64, var6.x + var6.w - var2); var8++) {
                  this.level(var1.level)[var7 * 64 + var8] = var1.id;
               }
            }
         }
      }
   }

   private long[] level(int var1) {
      if (this.ids[var1] == null) {
         this.ids[var1] = new long[4096];
         Arrays.fill(this.ids[var1], -1L);
      }

      return this.ids[var1];
   }
}
