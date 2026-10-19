package viewpoint.visibility;

import zombie.core.properties.PropertyContainer;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.objects.GridSquareEdgeFacingDirection;
import zombie.iso.sprite.IsoSprite;
import zombie.util.list.PZArrayList;

public final class Edges {
   public static final int OPEN = 0;
   public static final int WALL = 1;
   public static final int OPENING = 2;
   public static final int DOOR = 3;
   public static final int WINDOW = 4;

   public static int ownerKind(IsoSprite var0) {
      if (var0.solidfloor) {
         return 3;
      } else {
         PropertyContainer var1 = var0.getProperties();
         boolean var2 = var1.has(IsoFlagType.doorN)
            || var1.has(IsoFlagType.WindowN)
            || var1.has(IsoFlagType.windowN)
            || var1.has(IsoFlagType.attachedN)
            || var1.has(IsoFlagType.collideN)
            || var1.has(IsoFlagType.transparentN)
            || var1.has(IsoFlagType.WallNTrans)
            || var1.has(IsoFlagType.HoppableN)
            || var1.has(IsoFlagType.TallHoppableN)
            || var1.has(IsoFlagType.DoorWallN)
            || var1.has(IsoFlagType.WallN);
         boolean var3 = var1.has(IsoFlagType.doorW)
            || var1.has(IsoFlagType.WindowW)
            || var1.has(IsoFlagType.windowW)
            || var1.has(IsoFlagType.attachedW)
            || var1.has(IsoFlagType.collideW)
            || var1.has(IsoFlagType.transparentW)
            || var1.has(IsoFlagType.WallWTrans)
            || var1.has(IsoFlagType.HoppableW)
            || var1.has(IsoFlagType.TallHoppableW)
            || var1.has(IsoFlagType.DoorWallW)
            || var1.has(IsoFlagType.WallW);
         if ((!var2 || !var3) && !var1.has(IsoFlagType.WallNW) && !var1.has(IsoFlagType.attachedNW)) {
            return var2 ? 1 : (var3 ? 2 : 0);
         } else {
            return -1;
         }
      }
   }

   public static GridSquareEdgeFacingDirection facing(boolean var0) {
      return var0 ? GridSquareEdgeFacingDirection.NORTH_SOUTH : GridSquareEdgeFacingDirection.EAST_WEST;
   }

   public static int edge(IsoGridSquare var0, boolean var1) {
      GridSquareEdgeFacingDirection var2 = facing(var1);
      if (var0.getDoor(var2) != null) {
         return 3;
      }

      if (var0.getWindow(var2) != null) {
         return 4;
      }

      if (var0.getWindowFrame(var2) != null) {
         return 2;
      }

      boolean var3 = false;
      boolean var4 = false;
      boolean var5 = false;
      PZArrayList var6 = var0.getObjects();

      for (int var7 = 0; var7 < var6.size(); var7++) {
         IsoSprite var8 = ((IsoObject)var6.get(var7)).getSprite();
         if (var8 != null) {
            PropertyContainer var9 = var8.getProperties();
            var3 |= var9.has(IsoFlagType.WallNW) || var9.has(var1 ? IsoFlagType.WallN : IsoFlagType.WallW);
            var4 |= var9.has(var1 ? IsoFlagType.WindowN : IsoFlagType.WindowW)
               || var9.has(var1 ? IsoFlagType.windowN : IsoFlagType.windowW)
               || var9.has(var1 ? IsoFlagType.DoorWallN : IsoFlagType.DoorWallW)
               || var9.has(var1 ? IsoFlagType.doorN : IsoFlagType.doorW);
            var5 |= var9.has(var1 ? IsoFlagType.transparentN : IsoFlagType.transparentW)
               || var9.has(var1 ? IsoFlagType.WallNTrans : IsoFlagType.WallWTrans)
               || var9.has(var1 ? IsoFlagType.HoppableN : IsoFlagType.HoppableW)
               || var9.has(var1 ? IsoFlagType.TallHoppableN : IsoFlagType.TallHoppableW);
         }
      }

      return var4 ? 2 : (var3 && !var5 ? 1 : 0);
   }

   private Edges() {
   }
}
