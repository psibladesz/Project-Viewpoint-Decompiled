package viewpoint.world;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import zombie.core.properties.PropertyContainer;
import zombie.iso.BuildingDef;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.RoomDef;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.areas.IsoBuilding;
import zombie.iso.areas.IsoRoom;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteManager;
import zombie.util.list.PZArrayList;

public final class Facades {
   public static final int SOLID = 0;
   public static final int WINDOW = 1;
   public static final int DOOR = 2;
   public static final int NEUTRAL = 9210501;
   private static final int KINDS = 6;
   private static final int SHEET_REACH = 64;
   private static final int WINDOW_AT = 8;
   private static final int DOOR_AT = 10;
   private static final IdentityHashMap<BuildingDef, Facades.Average> buildingColours = new IdentityHashMap<>();
   private static final IdentityHashMap<RoomDef, Facades.Average> roomColours = new IdentityHashMap<>();
   private static final IdentityHashMap<IsoRoom, String[]> roomTiles = new IdentityHashMap<>();

   static int fill(IsoGridSquare var0, RoomDef var1, IsoRoom var2, IsoSprite var3) {
      IsoRoom var4 = var0.getRoom();
      if (var2 == null && var1 == null) {
         if (var4 != null) {
            return ownArtOutside(var3) ? -1 : buildingColour(var4);
         } else {
            return interior(var3) ? 9210501 : -1;
         }
      } else {
         boolean var5 = var2 != null ? var2 == var4 : var4 != null && var4.def == var1;
         return var5 ? -1 : roomColour(var1, var2);
      }
   }

   public static boolean ownArtOutside(IsoSprite var0) {
      return var0.getName() != null && var0.getName().contains("exterior") || shopFront(var0);
   }

   public static boolean interior(IsoSprite var0) {
      return var0.getName() != null && var0.getName().contains("interior");
   }

   static int outside(IsoGridSquare var0) {
      return buildingColour(var0.getRoom());
   }

   static IsoSprite roomTile(RoomDef var0, IsoRoom var1, boolean var2, int var3) {
      if (var0 == null && var1 == null) {
         return null;
      }

      int var4 = var3 * 2 + (var2 ? 1 : 0);
      MapFacades.RoomWalls var5 = MapFacades.room(var0);
      if (var5 != null) {
         return BuildingFacades.sprite(BuildingFacades.mostCounted(var5.counts.get(var4)));
      }

      if (var1 == null) {
         return null;
      }

      String[] var6 = roomTiles.get(var1);
      if (var6 == null) {
         var6 = new String[6];
         ArrayList var7 = var1.getSquares();

         for (int var8 = 0; var8 < var7.size(); var8++) {
            PZArrayList var9 = ((IsoGridSquare)var7.get(var8)).getObjects();

            for (int var10 = 0; var10 < var9.size(); var10++) {
               IsoSprite var11 = ((IsoObject)var9.get(var10)).getSprite();
               int var12 = var11 != null && var11.getName() != null ? kindOf(var11) : -1;
               if (var12 >= 0 && var6[var12] == null) {
                  var6[var12] = var11.getName();
               }
            }
         }

         roomTiles.put(var1, var6);
      }

      return BuildingFacades.sprite(var6[var4]);
   }

   static IsoSprite facadeTile(IsoGridSquare var0, boolean var1, int var2) {
      IsoRoom var3 = var0.getRoom();
      if (var3 == null) {
         return null;
      }

      int var4 = var2 * 2 + (var1 ? 1 : 0);
      IsoSprite var5 = BuildingFacades.most(var3.building, var0.getZ(), var4);
      return var5 != null ? var5 : BuildingFacades.sheetTile(var3.building, var0.getZ(), var4);
   }

   static IsoSprite plainFacade(IsoGridSquare var0, boolean var1) {
      return var0.getRoom() == null ? null : BuildingFacades.most(var0.getRoom().building, var0.getZ(), 0 + (var1 ? 1 : 0));
   }

   static void clear() {
      roomTiles.clear();
   }

   static void forgetColours() {
      buildingColours.clear();
      roomColours.clear();
      BuildingFacades.forget();
   }

   private static int buildingColour(IsoRoom var0) {
      IsoBuilding var1 = var0.building;
      int var2 = BuildingFacades.mappedColour(var1);
      if (var2 >= 0) {
         return var2;
      }

      BuildingDef var3 = var1 == null ? null : var1.def;
      int var4 = var1 == null ? var0.getSquares().size() : BuildingFacades.loaded(var1);
      Facades.Average var5 = var3 == null ? null : buildingColours.get(var3);
      if (var5 == null || var4 > var5.squares) {
         var5 = new Facades.Average();
         var5.squares = var4;
         if (var1 == null) {
            facadeAround(var0, var5);
         } else {
            for (int var6 = 0; var6 < var1.rooms.size(); var6++) {
               facadeAround((IsoRoom)var1.rooms.get(var6), var5);
            }
         }

         if (var3 != null) {
            buildingColours.put(var3, var5);
         }
      }

      int var7 = var5.colour();
      return var7 < 0 ? 9210501 : var7;
   }

   private static void facadeAround(IsoRoom var0, Facades.Average var1) {
      ArrayList var2 = var0.getSquares();

      for (int var3 = 0; var3 < var2.size(); var3++) {
         IsoGridSquare var4 = (IsoGridSquare)var2.get(var3);
         IsoGridSquare var5 = var4.getAdjacentSquare(IsoDirections.S);
         IsoGridSquare var6 = var4.getAdjacentSquare(IsoDirections.E);
         if (var5 != null && var5.getRoom() == null) {
            collect(var5, var1);
         }

         if (var6 != null && var6.getRoom() == null) {
            collect(var6, var1);
         }
      }
   }

   private static int roomColour(RoomDef var0, IsoRoom var1) {
      MapFacades.RoomWalls var2 = MapFacades.room(var0);
      int var3 = var2 == null ? -1 : var2.colour();
      if (var3 < 0 && var1 != null) {
         RoomDef var4 = var1.def;
         ArrayList var5 = var1.getSquares();
         Facades.Average var6 = var4 == null ? null : roomColours.get(var4);
         if (var6 == null || var5.size() > var6.squares) {
            var6 = new Facades.Average();
            var6.squares = var5.size();

            for (int var7 = 0; var7 < var5.size(); var7++) {
               collect((IsoGridSquare)var5.get(var7), var6);
            }

            if (var4 != null) {
               roomColours.put(var4, var6);
            }
         }

         int var8 = var6.colour();
         return var8 < 0 ? 9210501 : var8;
      } else {
         return var3 >= 0 ? var3 : 9210501;
      }
   }

   private static void collect(IsoGridSquare var0, Facades.Average var1) {
      PZArrayList var2 = var0.getObjects();

      for (int var3 = 0; var3 < var2.size(); var3++) {
         IsoSprite var4 = ((IsoObject)var2.get(var3)).getSprite();
         if (var4 != null && var4.getName() != null && kindOf(var4) >= 0) {
            int var5 = FacadeColours.of(var4.getName());
            if (var5 >= 0) {
               var1.add(var5);
            }
         }
      }
   }

   public static int kindOf(IsoSprite var0) {
      PropertyContainer var1 = var0.getProperties();
      if (windowObject(var0)) {
         return -1;
      } else if (var1.has(IsoFlagType.WindowN)) {
         return 3;
      } else if (var1.has(IsoFlagType.WindowW)) {
         return 2;
      } else if (var1.has(IsoFlagType.DoorWallN)) {
         return 5;
      } else if (var1.has(IsoFlagType.DoorWallW)) {
         return 4;
      } else if (var1.has(IsoFlagType.WallNW)) {
         return -1;
      } else if (var1.has(IsoFlagType.WallN)) {
         return 1;
      } else {
         return var1.has(IsoFlagType.WallW) ? 0 : -1;
      }
   }

   private static boolean windowObject(IsoSprite var0) {
      PropertyContainer var1 = var0.getProperties();
      return var1.has(IsoFlagType.windowN) && !var1.has(IsoFlagType.collideN) || var1.has(IsoFlagType.windowW) && !var1.has(IsoFlagType.collideW);
   }

   public static String sheetTile(IsoSprite var0, int var1) {
      if (var0.tilesetName == null) {
         return null;
      }

      int var2 = var1 / 2;
      IsoSprite var3 = var2 == 0 ? null : inSheet(var0, var0.tileSheetIndex + (var2 == 1 ? 8 : 10));
      if (var3 != null && kindOf(var3) == var1) {
         return var3.getName();
      }

      for (int var4 = 0; var4 <= 64; var4++) {
         for (int var8 : new int[]{var0.tileSheetIndex + var4, var0.tileSheetIndex - var4}) {
            IsoSprite var9 = inSheet(var0, var8);
            if (var9 != null && kindOf(var9) == var1) {
               return var9.getName();
            }
         }
      }

      return null;
   }

   private static IsoSprite inSheet(IsoSprite var0, int var1) {
      return var1 < 0 ? null : (IsoSprite)IsoSpriteManager.instance.getNamedMap().get(var0.tilesetName + "_" + var1);
   }

   private static boolean shopFront(IsoSprite var0) {
      PropertyContainer var1 = var0.getProperties();
      return (var1.has(IsoFlagType.windowN) || var1.has(IsoFlagType.windowW)) && !windowObject(var0);
   }

   private Facades() {
   }

   private static final class Average {
      long r;
      long g;
      long b;
      long n;
      int squares;

      void add(int var1) {
         this.r += var1 >> 16 & 0xFF;
         this.g += var1 >> 8 & 0xFF;
         this.b += var1 & 0xFF;
         this.n++;
      }

      int colour() {
         return this.n == 0L ? -1 : (int)(this.r / this.n) << 16 | (int)(this.g / this.n) << 8 | (int)(this.b / this.n);
      }
   }
}
