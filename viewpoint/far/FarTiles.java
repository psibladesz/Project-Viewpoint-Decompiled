package viewpoint.far;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import viewpoint.render.FloorArt;
import viewpoint.render.GroundTiles;
import viewpoint.render.TreeAtlas;
import viewpoint.world.FacadeColours;
import viewpoint.world.Facades;
import viewpoint.world.MapRoofs;
import viewpoint.world.RoofPiece;
import viewpoint.world.TileMesh;
import viewpoint.world.TileMeshes;
import viewpoint.world.WorldMesher;
import zombie.core.Core;
import zombie.core.properties.PropertyContainer;
import zombie.core.textures.Texture;
import zombie.core.textures.TextureID;
import zombie.iso.IsoDirections;
import zombie.iso.LotHeader;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteManager;

final class FarTiles {
   static final byte NONE = 0;
   static final byte FLOOR = 1;
   static final byte WALL = 2;
   static final byte TREE = 3;
   static final byte SOLID = 4;
   static final byte S_NONE = 0;
   static final byte S_WALL = 1;
   static final byte S_FLOOR = 2;
   static final byte S_THING = 3;
   static final byte S_TREE = 4;
   static final byte S_PLANT = 5;
   static final byte ROOF = 1;
   static final byte SOLID_FLOOR = 2;
   static final byte WATER = 4;
   static final byte TREE_SPOT = 8;
   static final byte PLACEHOLDER = 16;
   static final byte LEAVES = 32;
   private static final float OBJECT_OFFSET_X = 32.0F;
   private static final float OBJECT_OFFSET_Y = 96.0F;
   static final float LOW = 0.5F;
   static final int CARD_LEFT = 0;
   static final int CARD_RIGHT = 1;
   static final int CARD_BOTTOM = 2;
   static final int CARD_TOP = 3;
   static final FarTiles.Tile EMPTY = new FarTiles.Tile((byte)0, 0.0F, -1);
   static final FarTiles.Tile SPOT = new FarTiles.Tile((byte)0, 0.0F, -1, (byte)0, null, null, null, (byte)0, (byte)0, (byte)8, null);
   static final FarTiles.Tile PLACEHOLDER_SPOT = new FarTiles.Tile((byte)0, 0.0F, -1, (byte)0, null, null, null, (byte)0, (byte)0, (byte)24, null);
   private static final float SHELL_LOW = 0.2F;
   private static final float THIN = 0.35F;
   private static final HashMap<String, FarTiles.Tile> byName = new HashMap<>();
   private static final IdentityHashMap<LotHeader, FarTiles.Tile[]> byHeader = new IdentityHashMap<>();
   private static final IdentityHashMap<LotHeader, Integer> filled = new IdentityHashMap<>();

   static FarTiles.Tile[] table(LotHeader var0, long var1) {
      FarTiles.Tile[] var3 = byHeader.get(var0);
      Integer var4 = filled.get(var0);
      if (var3 != null && var4 == null) {
         return var3;
      }

      ArrayList var5 = var0.tilesUsed;
      if (var3 == null) {
         var3 = new FarTiles.Tile[var5.size()];
         byHeader.put(var0, var3);
      }

      int var6 = var4 == null ? 0 : var4;

      while (var6 < var3.length) {
         var3[var6] = of((String)var5.get(var6));
         var6++;
         if ((var6 & 31) == 0 && var6 < var3.length && System.nanoTime() > var1) {
            filled.put(var0, var6);
            return null;
         }
      }

      filled.remove(var0);
      return var3;
   }

   static FarTiles.Tile of(String var0) {
      FarTiles.Tile var1 = byName.get(var0);
      if (var1 != null) {
         return var1;
      }

      try {
         boolean var2 = var0 != null && (var0.startsWith("vegetation_trees") || var0.startsWith("jumbo_tree_01"));
         var1 = var2 ? PLACEHOLDER_SPOT : classify(var0);
      } catch (RuntimeException var3) {
         var1 = EMPTY;
      }

      byName.put(var0, var1);
      return var1;
   }

   static Map<String, FarTiles.Tile> byName() {
      return Collections.unmodifiableMap(byName);
   }

   static void forget() {
      byHeader.clear();
      filled.clear();
      TreeSeasons.forget();
      BiomeTrees.forget();
   }

   private static FarTiles.Tile classify(String var0) {
      IsoSprite var1 = var0 == null ? null : (IsoSprite)IsoSpriteManager.instance.getNamedMap().get(var0);
      if (var1 == null) {
         return EMPTY;
      }

      boolean var2 = var1.getType() == IsoObjectType.tree;
      Texture var3 = var1.getTextureForCurrentFrame(IsoDirections.N);
      boolean var4 = var3 != null && var3.isReady() && var3.getTextureId() != null;
      TextureID var5 = var4 ? var3.getTextureId() : null;
      float[] var6 = var4 ? WorldMesher.textureMapping(var3) : null;
      TileMesh var7 = var3 == null ? null : TileMeshes.peek(var1, var3, var2 ? var1 : null);
      if (!var1.solidfloor && !var1.getProperties().has(IsoFlagType.FloorOverlay)) {
         PropertyContainer var12 = var1.getProperties();
         boolean var9 = var12.has(IsoFlagType.WallNW);
         int var10 = !var9 && !var12.has(IsoFlagType.WallN) ? (var12.has(IsoFlagType.WindowN) ? 1 : (var12.has(IsoFlagType.DoorWallN) ? 2 : -1)) : 0;
         int var11 = !var9 && !var12.has(IsoFlagType.WallW) ? (var12.has(IsoFlagType.WindowW) ? 1 : (var12.has(IsoFlagType.DoorWallW) ? 2 : -1)) : 0;
         if (var2 || var10 < 0 && var11 < 0) {
            if (var3 == null) {
               return var2 ? SPOT : EMPTY;
            } else {
               return classifyThing(var0, var1, var3, var2, var5, var6, var7, var12);
            }
         } else {
            return wall(var0, var1, var5, var6, var7, var10, var11);
         }
      } else {
         FarTiles.Tile var8 = new FarTiles.Tile(
            (byte)1,
            0.0F,
            FarColours.of(var0),
            (byte)2,
            var7 != null && var7.vertCount != 0 ? var5 : null,
            var6,
            var7,
            (byte)0,
            (byte)0,
            (byte)((var1.solidfloor ? 2 : 0) | (var1.getProperties().has(IsoFlagType.water) ? 4 : 0)),
            null
         );
         var8.floor = var4 ? floorLayer(var1, var3) : null;
         var8.floorName = var0;
         roof(var8, var0, var1);
         return var8;
      }
   }

   private static FarTiles.Tile wall(String var0, IsoSprite var1, TextureID var2, float[] var3, TileMesh var4, int var5, int var6) {
      int var7 = FacadeColours.of(var0);
      FarTiles.Tile var8 = new FarTiles.Tile(
         (byte)2,
         1.0F,
         var7 >= 0 ? var7 : 9210501,
         (byte)1,
         var4 != null && var4.vertCount != 0 ? var2 : null,
         var3,
         var4,
         (byte)var5,
         (byte)var6,
         (byte)0,
         null
      );
      roof(var8, var0, var1);
      var8.wall = facade(var0, var1, var7);
      return var8;
   }

   private static ShellFacades.Wall facade(String var0, IsoSprite var1, int var2) {
      int var3 = Facades.kindOf(var1);
      byte var4 = (byte)((Facades.ownArtOutside(var1) ? 1 : 0) | (Facades.interior(var1) ? 2 : 0));
      boolean var5 = var3 >= 0 && var3 / 2 == 0;
      FarTiles.Tile var6 = var5 ? kit(var1, 2 + var3 % 2) : null;
      FarTiles.Tile var7 = var5 ? kit(var1, 4 + var3 % 2) : null;
      return new ShellFacades.Wall(var0, (byte)var3, var4, var2, var6, var7);
   }

   private static FarTiles.Tile kit(IsoSprite var0, int var1) {
      String var2 = Facades.sheetTile(var0, var1);
      return var2 == null ? null : of(var2);
   }

   private static void roof(FarTiles.Tile var0, String var1, IsoSprite var2) {
      var0.roof = MapRoofs.piece(var1);
      var0.roofBits = (byte)(MapRoofs.bits(var1, var2.solidfloor) | MapRoofs.edges(var2));
      var0.flatRoof = MapRoofs.flat(var1);
   }

   private static float[] floorLayer(IsoSprite var0, Texture var1) {
      float[] var2 = new float[15];
      FloorArt.numbers(
         var2,
         var1,
         -32.0F * Core.tileScale + var0.soffX + var1.getOffsetX(),
         -96.0F * Core.tileScale + var0.soffY + var1.getOffsetY(),
         1.0F,
         1.0F,
         var0.tintMod.r,
         var0.tintMod.g,
         var0.tintMod.b,
         1.0F
      );
      return var2;
   }

   static GroundTiles ground(FarTiles.Tile[] var0) {
      TextureID[] var1 = new TextureID[var0.length];
      float[] var2 = new float[var0.length * 15];

      for (int var3 = 0; var3 < var0.length; var3++) {
         FarTiles.Tile var4 = var0[var3];
         if (var4 != null && var4.shell == 2 && var4.floor != null) {
            var1[var3] = var4.page;
            System.arraycopy(var4.floor, 0, var2, var3 * 15, 15);
         }
      }

      return new GroundTiles(var1, var2);
   }

   private static FarTiles.Tile classifyThing(
      String var0, IsoSprite var1, Texture var2, boolean var3, TextureID var4, float[] var5, TileMesh var6, PropertyContainer var7
   ) {
      float[] var8 = var6.data;
      float var9 = 0.0F;
      float var10 = Float.MAX_VALUE;
      float var11 = -Float.MAX_VALUE;
      float var12 = Float.MAX_VALUE;
      float var13 = -Float.MAX_VALUE;

      for (int var14 = 0; var14 + 2 < var8.length; var14 += 8) {
         var10 = Math.min(var10, var8[var14]);
         var11 = Math.max(var11, var8[var14]);
         var9 = Math.max(var9, var8[var14 + 1]);
         var12 = Math.min(var12, var8[var14 + 2]);
         var13 = Math.max(var13, var8[var14 + 2]);
      }

      float var26 = var9 / 2.4494896F;
      boolean var15 = Math.max(var11 - var10, var13 - var12) < 0.35F;
      boolean var16 = !var3 && TileMeshes.isPlant(var1);
      int var17 = !(var26 < 0.5F) && (var3 || !var15) && !var16 ? (var3 ? 3 : 4) : 0;
      IsoObjectType var18 = var1.getType();
      boolean var19 = var1.cutN || var7.has(IsoFlagType.collideN) || var18 == IsoObjectType.doorN || var18 == IsoObjectType.windowFN;
      boolean var20 = var1.cutW || var7.has(IsoFlagType.collideW) || var18 == IsoObjectType.doorW || var18 == IsoObjectType.windowFW;
      boolean var21 = var0.startsWith("roofs_") || MapRoofs.roofTile(var0);
      float[] var22 = var16 ? TileMeshes.plantCard(var2) : null;
      int var23 = var3 ? 4 : (var22 != null ? 5 : (MapRoofs.trim(var1) ? 0 : (!var21 && (!(var26 >= 0.2F) || var15 && !var19 && !var20) ? 0 : 3)));
      boolean var24 = var3 && TreeSeasons.seasonal(var0);
      FarTiles.Tile var25 = new FarTiles.Tile(
         (byte)var17,
         var26,
         FarColours.of(var0),
         (byte)(var6.vertCount == 0 ? 0 : var23),
         var4,
         var5,
         var3 ? TileMeshes.crossed(var2, 2) : var6,
         (byte)(var19 ? 1 : 0),
         (byte)(var20 ? 1 : 0),
         (byte)((var21 ? 1 : 0) | (var3 ? 8 : 0)),
         var24 ? var0 : null
      );
      var25.card = var22;
      roof(var25, var0, var1);
      if (var24) {
         TreeSeasons.add(var25, var2);
      }

      if (var17 == 3) {
         FarTrees.add(var25, var2);
      }

      return var25;
   }

   private FarTiles() {
   }

   static final class Tile {
      final byte kind;
      final float height;
      final int colour;
      final byte shell;
      final TextureID page;
      final float[] map;
      final TileMesh mesh;
      final byte north;
      final byte west;
      final byte flags;
      final String tree;
      float[] floor;
      String floorName;
      float[] card;
      volatile FarTiles.Tile leaves;
      volatile FarTiles.Tile[] seasons;
      int treeKind = -1;
      volatile TreeAtlas.Slot billboard;
      RoofPiece roof;
      byte roofBits;
      boolean flatRoof;
      ShellFacades.Wall wall;

      Tile(byte var1, float var2, int var3) {
         this(var1, var2, var3, (byte)0, null, null, null, (byte)0, (byte)0, (byte)0, null);
      }

      Tile(byte var1, float var2, int var3, byte var4, TextureID var5, float[] var6, TileMesh var7, byte var8, byte var9, byte var10, String var11) {
         this.kind = var1;
         this.height = var2;
         this.colour = var3;
         this.shell = var5 == null ? 0 : var4;
         this.page = var5;
         this.map = var6;
         this.mesh = var7;
         this.north = var8;
         this.west = var9;
         this.flags = var10;
         this.tree = var11;
      }

      FarTiles.Tile inSeason(int var1) {
         FarTiles.Tile[] var2 = this.seasons;
         FarTiles.Tile var3 = var2 == null ? null : var2[var1];
         return var3 != null ? var3 : this;
      }
   }
}
