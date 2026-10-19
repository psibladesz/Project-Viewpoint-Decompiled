package viewpoint.camera;

import java.util.List;
import viewpoint.core.CameraSquares;
import viewpoint.visibility.Edges;
import zombie.core.properties.IsoPropertyType;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.LosUtil.TestResults;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.iso.objects.IsoDoor;
import zombie.iso.objects.IsoThumpable;

final class CameraCapture {
   static final int MARGIN = 2;

   static void squares(CameraSquares var0, IsoCell var1, double var2, double var4, int var6, float var7) {
      int var8 = (int)Math.ceil(var7) + 2;
      var0.side = var8 * 2 + 1;
      var0.x = (int)Math.floor(var2) - var8;
      var0.y = (int)Math.floor(var4) - var8;
      var0.level = var6;
      int var9 = var0.side * var0.side * 2;
      if (var0.blocks.length < var9) {
         var0.blocks = new byte[var9];
      }

      int var10 = 0;

      for (int var11 = 0; var11 < 2; var11++) {
         for (int var12 = 0; var12 < var0.side; var12++) {
            for (int var13 = 0; var13 < var0.side; var13++) {
               IsoGridSquare var14 = var1.getGridSquare(var0.x + var13, var0.y + var12, var0.level + var11);
               var0.blocks[var10++] = var14 == null ? 0 : blocks(var1, var14);
            }
         }
      }
   }

   private static byte blocks(IsoCell var0, IsoGridSquare var1) {
      int var2 = !stops(var0, var1, true) && !leaf(var1, true) ? 0 : 1;
      var2 |= !stops(var0, var1, false) && !leaf(var1, false) ? 0 : 2;
      var2 |= !var1.isSolidFloor() && !var1.TreatAsSolidFloor() && !var1.hasRainBlockingTile() ? 0 : 4;
      return (byte)var2;
   }

   private static boolean stops(IsoCell var0, IsoGridSquare var1, boolean var2) {
      if (seen(var1.testVisionAdjacent(var2 ? 0 : -1, var2 ? -1 : 0, 0, false, false))) {
         return false;
      } else {
         IsoGridSquare var3 = var0.getGridSquare(var1.x - (var2 ? 0 : 1), var1.y - (var2 ? 1 : 0), var1.z);
         if (var1.has(IsoObjectType.tree) || var3 != null && var3.has(IsoObjectType.tree)) {
            int var4 = Edges.edge(var1, var2);
            return var4 == 1 || var4 == 4 || var4 == 3 && shut(var1.getDoor(Edges.facing(var2)));
         } else {
            return true;
         }
      }
   }

   private static boolean leaf(IsoGridSquare var0, boolean var1) {
      List var2 = var0.getSpecialObjects();

      for (int var3 = 0; var3 < var2.size(); var3++) {
         IsoObject var4 = (IsoObject)var2.get(var3);
         boolean var5 = var4.getProperties() != null && var4.getProperties().has(IsoPropertyType.GARAGE_DOOR);
         if (var4 instanceof IsoDoor var6 && var6.IsOpen() && var6.getNorth() != var1 && !var5
            || var4 instanceof IsoThumpable var7 && var7.isDoor() && var7.IsOpen() && var7.getNorth() != var1) {
            return true;
         }
      }

      return false;
   }

   private static boolean shut(IsoObject var0) {
      return var0 instanceof IsoDoor var2 ? !var2.IsOpen() : !(var0 instanceof IsoThumpable var1 && var1.IsOpen());
   }

   private static boolean seen(TestResults var0) {
      return var0 == TestResults.Clear || var0 == TestResults.ClearThroughOpenDoor;
   }

   private CameraCapture() {
   }
}
