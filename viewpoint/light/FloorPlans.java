package viewpoint.light;

import java.util.Arrays;
import viewpoint.core.Frame;
import viewpoint.render.PlanSnapshot;
import zombie.characters.IsoGameCharacter;
import zombie.iso.BuildingDef;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.RoomDef;
import zombie.iso.areas.IsoBuilding;
import zombie.iso.areas.IsoRoom;

public final class FloorPlans {
   private static final float WALLED = 0.5F;
   private static final int RECHECK_FRAMES = 30;
   private static final IsoDirections[] SIDES = new IsoDirections[]{IsoDirections.N, IsoDirections.E, IsoDirections.S, IsoDirections.W};
   private static IsoBuilding interiorOf;
   private static long[] interiorRooms = new long[0];
   private static int sinceCheck;

   public static void snapshot(Frame var0, IsoGameCharacter var1) {
      TileNumbers.retry();
      PlanSnapshot var2 = var0.scene.floorPlan;
      byte var3 = 8;
      var2.chunkX = Math.floorDiv((int)Math.floor(var0.camX), 8) - var3;
      var2.chunkY = Math.floorDiv((int)Math.floor(var0.camY), 8) - var3;
      var2.level = (int)Math.floor(var0.camZ) - 2;

      for (int var4 = 0; var4 < 5; var4++) {
         for (int var5 = 0; var5 < 16; var5++) {
            for (int var6 = 0; var6 < 16; var6++) {
               LightLayouts.Level var7 = LightLayouts.get(var2.chunkX + var6, var2.chunkY + var5, var2.level + var4);
               int var8 = (var4 * 16 + var5) * 16 + var6;
               var2.passes[var8] = var7 == null ? null : var7.passes;
               var2.flags[var8] = var7 == null ? null : var7.flags;
               var2.walls[var8] = var7 == null ? null : var7.walls;
               var2.rooms[var8] = var7 == null ? null : var7.rooms;
            }
         }
      }

      interior(var2, var1);
   }

   private static void interior(PlanSnapshot var0, IsoGameCharacter var1) {
      IsoGridSquare var2 = var1 == null ? null : var1.getCurrentSquare();
      IsoBuilding var3 = var2 != null && var2.getRoomID() != -1L ? var2.getBuilding() : null;
      if (var3 != interiorOf || ++sinceCheck >= 30) {
         interiorOf = var3;
         sinceCheck = 0;
         long[] var4 = walledRooms(var3);
         if (!Arrays.equals(var4, interiorRooms)) {
            interiorRooms = var4;
         }
      }

      var0.interior = interiorRooms;
   }

   private static long[] walledRooms(IsoBuilding var0) {
      BuildingDef var1 = var0 == null ? null : var0.getDef();
      if (var1 == null) {
         return new long[0];
      }

      long[] var2 = new long[var1.getRooms().size()];

      for (int var3 = 0; var3 < var2.length; var3++) {
         var2[var3] = ((RoomDef)var1.getRooms().get(var3)).getID();
      }

      Arrays.sort(var2);
      long[] var7 = new long[var2.length];
      int var4 = 0;

      for (RoomDef var6 : var1.getRooms()) {
         if (walled(var6.getIsoRoom(), var2)) {
            var7[var4++] = var6.getID();
         }
      }

      var7 = Arrays.copyOf(var7, var4);
      Arrays.sort(var7);
      return var7;
   }

   private static boolean walled(IsoRoom var0, long[] var1) {
      if (var0 == null) {
         return true;
      }

      int var2 = 0;
      byte var3 = 0;

      for (IsoGridSquare var5 : var0.getSquares()) {
         for (IsoDirections var9 : SIDES) {
            IsoGridSquare var10 = var5.getAdjacentSquare(var9);
            if (var10 != null && Arrays.binarySearch(var1, var10.getRoomID()) < 0) {
               var2++;
               var3 += !var5.isWallTo(var10) && !var5.isWindowTo(var10) && !var5.isDoorTo(var10) ? 0 : 1;
            }
         }
      }

      return var3 >= 0.5F * var2;
   }

   private FloorPlans() {
   }
}
