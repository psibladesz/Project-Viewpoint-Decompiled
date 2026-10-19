package viewpoint.far;

import java.util.IdentityHashMap;
import viewpoint.platform.Caches;
import viewpoint.world.MapRoofs;
import zombie.iso.IsoMetaCell;
import zombie.iso.IsoMetaGrid;
import zombie.iso.IsoWorld;
import zombie.iso.RoomDef;
import zombie.iso.RoomDef.RoomRect;

final class CellRooms {
   static short[][] of(FarWorld.Slot var0) {
      var0.used = Caches.tick();
      if (var0.rooms != null) {
         return var0.rooms;
      }

      short[][] var1 = new short[Math.max(1, var0.maxLevel + 1)][];
      IdentityHashMap var2 = new IdentityHashMap();
      IsoMetaCell var3 = metaCell(var0.cellX, var0.cellY);
      if (var3 != null) {
         int var4 = var0.cellX * 256;
         int var5 = var0.cellY * 256;

         for (RoomDef var7 : var3.roomList) {
            if (indoors(var7) && var7.level < var1.length) {
               short[] var8 = var1[var7.level];
               if (var8 == null) {
                  var8 = var1[var7.level] = new short[65536];
               }

               Object var9 = var7.building != null ? var7.building : var7;
               Integer var10 = (Integer)var2.get(var9);
               if (var10 == null) {
                  var10 = var2.size() + 1;
                  var2.put(var9, var10);
               }

               for (RoomRect var12 : var7.rects) {
                  for (int var13 = Math.max(0, var12.y - var5); var13 < Math.min(256, var12.y + var12.h - var5); var13++) {
                     for (int var14 = Math.max(0, var12.x - var4); var14 < Math.min(256, var12.x + var12.w - var4); var14++) {
                        var8[var13 * 256 + var14] = (short)var10.intValue();
                     }
                  }
               }
            }
         }
      }

      var0.buildings = var2.size();
      var0.rooms = var1;
      return var1;
   }

   static IsoMetaCell metaCell(int var0, int var1) {
      IsoMetaGrid var2 = IsoWorld.instance.getMetaGrid();
      int var3 = var0 - var2.minX;
      int var4 = var1 - var2.minY;
      return var3 >= 0 && var4 >= 0 && var3 <= var2.maxX - var2.minX && var4 <= var2.maxY - var2.minY ? var2.getCell(var3, var4) : null;
   }

   static boolean indoors(RoomDef var0) {
      return var0.level >= 0 && !var0.isEmptyOutside() && !MapRoofs.outdoors(var0.getName());
   }

   private CellRooms() {
   }
}
