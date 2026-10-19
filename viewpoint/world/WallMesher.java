package viewpoint.world;

import viewpoint.visibility.Owner;
import zombie.core.properties.PropertyContainer;
import zombie.core.textures.Texture;
import zombie.iso.IsoChunk;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoWorld;
import zombie.iso.RoomDef;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.areas.IsoRoom;
import zombie.iso.sprite.IsoSprite;

final class WallMesher {
   static final int ACROSS_WEST = 1;
   static final int ACROSS_NORTH = 2;
   private static int guessed;

   static void begin() {
      guessed = 0;
   }

   static int guessed() {
      return guessed;
   }

   static boolean wall(IsoSprite var0, TileMesh var1, Texture var2, IsoGridSquare var3, float var4, float var5, float var6, boolean var7, boolean[] var8) {
      if (var0.getName() == null) {
         return false;
      }

      PropertyContainer var9 = var0.getProperties();
      boolean var10 = var9.has(IsoFlagType.WallNW);
      int var11 = !var10 && !var9.has(IsoFlagType.WallN) ? (var9.has(IsoFlagType.WindowN) ? 1 : (var9.has(IsoFlagType.DoorWallN) ? 2 : -1)) : 0;
      int var12 = !var10 && !var9.has(IsoFlagType.WallW) ? (var9.has(IsoFlagType.WindowW) ? 1 : (var9.has(IsoFlagType.DoorWallW) ? 2 : -1)) : 0;
      if (var11 < 0 && var12 < 0) {
         return false;
      }

      Recipe.Batch var13 = WorldMesher.batch(var2.getTextureId());
      IsoGridSquare var14 = var3.getAdjacentSquare(IsoDirections.N);
      IsoGridSquare var15 = var3.getAdjacentSquare(IsoDirections.W);
      if (var11 >= 0) {
         WorldMesher.owner = Owner.of(WorldMesher.square, 1);
         WorldMesher.emitFace(var13, var1, var2, var4, var5, var6, 0.0F, 1.0F, 0, 0.0F, art(var0, var11));
         RoomDef var16 = roomAcross(var3, var14, 0, -1, 2);
         back(var0, var1, var2, var3, var14, var16, true, var11, var7, 0.0F, 1.0F, var4, var5, var6, var8);
      }

      if (var12 >= 0) {
         WorldMesher.owner = Owner.of(WorldMesher.square, 2);
         WorldMesher.emitFace(var13, var1, var2, var4, var5, var6, 1.0F, 0.0F, 0, 0.0F, art(var0, var12));
         RoomDef var17 = roomAcross(var3, var15, -1, 0, 1);
         back(var0, var1, var2, var3, var15, var17, false, var12, var7, 1.0F, 0.0F, var4, var5, var6, var8);
      }

      if (var11 >= 0 && var12 >= 0) {
         WorldMesher.owner = -1;
         emitCaps(var13, var1, var2, var4, var5, var6);
      } else {
         WorldMesher.emitFace(var13, var1, var2, var4, var5, var6, var11 >= 0 ? 0.0F : 1.0F, var11 >= 0 ? 1.0F : 0.0F, 2, 0.0F);
      }

      return true;
   }

   private static RoomDef roomAcross(IsoGridSquare var0, IsoGridSquare var1, int var2, int var3, int var4) {
      if (var1 != null) {
         IsoRoom var6 = var1.getRoom();
         return var6 == null ? null : var6.def;
      }

      if (!unloaded(var0, var2, var3)) {
         return null;
      }

      RoomDef var5 = IsoWorld.instance.getMetaGrid().getRoomAt(var0.getX() + var2, var0.getY() + var3, var0.getZ());
      guessed = guessed | (var5 != null && MapFacades.room(var5) == null ? var4 : 0);
      return var5;
   }

   private static boolean unloaded(IsoGridSquare var0, int var1, int var2) {
      IsoChunk var3 = var0.getChunk();
      int var4 = Math.floorDiv(var0.getX() + var1, 8);
      int var5 = Math.floorDiv(var0.getY() + var2, 8);
      return var3 != null && (var4 != var3.wx || var5 != var3.wy) && var0.getCell().getChunk(var4, var5) == null;
   }

   private static void emitCaps(Recipe.Batch var0, TileMesh var1, Texture var2, float var3, float var4, float var5) {
      WorldMesher.op(var0, 2, var1, var2, var3, var4, var5);
   }

   private static void back(
      IsoSprite var0,
      TileMesh var1,
      Texture var2,
      IsoGridSquare var3,
      IsoGridSquare var4,
      RoomDef var5,
      boolean var6,
      int var7,
      boolean var8,
      float var9,
      float var10,
      float var11,
      float var12,
      float var13,
      boolean[] var14
   ) {
      IsoRoom var15 = var4 == null ? null : var4.getRoom();
      int var16 = var8 ? -1 : Facades.fill(var3, var5, var15, var0);
      if (var16 < 0) {
         WorldMesher.emitFace(WorldMesher.batch(var2.getTextureId()), var1, var2, var11, var12, var13, var9, var10, 1, 0.0F, art(var0, var7));
      } else {
         IsoSprite var17 = Facades.roomTile(var5, var15, var6, var7);
         if (var17 == null && var5 == null && var15 == null) {
            var17 = Facades.facadeTile(var3, var6, var7);
         }

         if (var17 != null) {
            Texture var18 = var17.getTextureForCurrentFrame(IsoDirections.N);
            if (var18 == null || var18.isReady() && var18.getTextureId() != null) {
               if (var18 != null) {
                  TileMesh var19 = TileMeshes.get(var17, var18, null);
                  if (var19.vertCount > 0) {
                     WorldMesher.emitFace(WorldMesher.batch(var18.getTextureId()), var19, var18, var11, var12, var13, var9, var10, 1, 0.0F, art(var17, var7));
                     return;
                  }
               }
            } else {
               var14[0] = true;
            }
         }

         WorldMesher.emitFace(WorldMesher.batch(var2.getTextureId()), var1, var2, var11, var12, var13, var9, var10, 1, var16 + 1.0F, glass(var7));
      }
   }

   private static int glass(int var0) {
      return var0 == 1 ? 1024 : 0;
   }

   private static int art(IsoSprite var0, int var1) {
      return glass(var1) | WallMaterials.flags(var0.getName());
   }

   private WallMesher() {
   }
}
