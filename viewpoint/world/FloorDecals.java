package viewpoint.world;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import viewpoint.render.CorpseCards;
import zombie.core.Core;
import zombie.core.skinnedmodel.DeadBodyAtlas;
import zombie.core.skinnedmodel.DeadBodyAtlas.BodyParams;
import zombie.core.skinnedmodel.DeadBodyAtlas.BodyTexture;
import zombie.core.textures.Texture;
import zombie.inventory.InventoryItem;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoWorld;
import zombie.iso.IsoGridSquare.ResultLight;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.objects.IsoDeadBody;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.iso.sprite.IsoSprite;

final class FloorDecals {
   private static final float BODY_REACH = 1.3F;
   private static final BodyParams PARAMS = new BodyParams();
   private static Field bodyEntry;
   private static Field bodyReady;
   private static final String SNOW_TILES = "e_newsnow_ground_1_";

   static void onSquare(IsoGridSquare var0, float var1, float var2, float var3, boolean[] var4, boolean var5) {
      List var6 = var0.getDeadBodys();
      if (var6 != null) {
         WorldMesher.modelsMatter = WorldMesher.modelsMatter | !var6.isEmpty();

         for (int var7 = 0; var7 < var6.size(); var7++) {
            body((IsoDeadBody)var6.get(var7), var0, var1, var2, var3, var4, var5);
         }
      }

      items(var0, var1, var2, var3, var4, var5);
      snow(var0, var1, var2, var3, var4);
   }

   static void items(IsoGridSquare var0, float var1, float var2, float var3, boolean[] var4, boolean var5) {
      ArrayList var6 = var0.getWorldObjects();

      for (int var7 = 0; var7 < var6.size(); var7++) {
         IsoWorldInventoryObject var8 = (IsoWorldInventoryObject)var6.get(var7);
         InventoryItem var9 = var8.getItem();
         if (var9 != null && var9.getScriptItem().isWorldRender()) {
            boolean var10 = WorldMesher.hasModel(var9);
            WorldMesher.modelsMatter |= var10;
            if (var5 && var10) {
               WorldMesher.lastModelledItems.add(var8);
            } else {
               IsoSprite var11 = var8.getSprite();
               Texture var12 = var11 == null ? null : var11.getTextureForCurrentFrame(var8.getForwardIsoDirection(), var8);
               if (var12 != null) {
                  if (var12.isReady() && var12.getTextureId() != null) {
                     decal(
                        WorldMesher.batch(var12.getTextureId()),
                        var12,
                        var1 + (var8.getX() + var8.xoff - var0.getX() - 0.5F),
                        var2 + (var8.getY() + var8.yoff - var0.getY() - 0.5F),
                        var3 + var8.zoff * 2.4494896F + 0.006F,
                        var12.getWidthOrig() / 128.0F,
                        var12.getHeightOrig() / 128.0F
                     );
                  } else {
                     var4[0] = true;
                  }
               }
            }
         }
      }
   }

   private static void decal(Recipe.Batch var0, Texture var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = 1.0F / var1.getWidthHW();
      float var8 = 1.0F / var1.getHeightHW();
      float var9 = var1.getXStart() + 0.5F * var7;
      float var10 = var1.getYStart() + 0.5F * var8;
      float var11 = var1.getXEnd() - 0.5F * var7;
      float var12 = var1.getYEnd() - 0.5F * var8;
      float[] var13 = new float[84];
      int var14 = 0;
      int[] var15 = new int[]{0, 1, 2, 0, 2, 3};

      for (int var16 = 0; var16 < 6; var16++) {
         int var17 = var15[var16];
         float var18 = var17 != 1 && var17 != 2 ? -1.0F : 1.0F;
         float var19 = var17 >= 2 ? 1.0F : -1.0F;
         var14 = Cook.put(
            var13,
            var14,
            -(var2 + var18 * var5),
            var4,
            -(var3 + var19 * var6),
            var18 > 0.0F ? var11 : var9,
            var19 > 0.0F ? var12 : var10,
            var9,
            var10,
            var11,
            var12,
            0.0F,
            1.0F,
            0.0F,
            0.0F,
            2 + WorldMesher.lift
         );
      }

      WorldMesher.raw(var0, var13);
   }

   static void snow(IsoGridSquare var0, float var1, float var2, float var3, boolean[] var4) {
      IsoCell var5 = IsoWorld.instance.currentCell;
      if (var5.getSnowTarget() > 0 && snowFloor(var0)) {
         int var6 = var0.getX();
         int var7 = var0.getY();
         int var8 = var0.getZ();
         int var9 = -1;
         int var10 = -1;
         if (var5.gridSquareIsSnow(var6, var7, var8)) {
            var9 = 40 + Math.floorMod(var6 + var7, 4);
         } else {
            boolean var11 = var5.gridSquareIsSnow(var6, var7 - 1, var8);
            boolean var12 = var5.gridSquareIsSnow(var6, var7 + 1, var8);
            boolean var13 = var5.gridSquareIsSnow(var6 + 1, var7, var8);
            boolean var14 = var5.gridSquareIsSnow(var6 - 1, var7, var8);
            int var15 = (var11 ? 1 : 0) + (var12 ? 1 : 0) + (var13 ? 1 : 0) + (var14 ? 1 : 0);
            int var16 = Math.floorMod(var6 + var7, 3) * 4;
            if (var15 == 1) {
               var9 = (var11 ? 28 : (var12 ? 31 : (var13 ? 30 : 29))) + var16;
            } else if (var15 == 2) {
               if (var11 && var12) {
                  var9 = 28 + var16;
                  var10 = 31 + var16;
               } else if (var13 && var14) {
                  var9 = 29 + var16;
                  var10 = 30 + var16;
               } else if (var11) {
                  var9 = (var14 ? 16 : 19) + var16;
               } else {
                  var9 = (var14 ? 18 : 17) + var16;
               }
            } else if (var15 == 3) {
               var9 = (!var11 ? 5 : (!var12 ? 7 : (!var13 ? 4 : 6))) + var16;
            } else if (var15 == 4) {
               var9 = Math.floorMod(var6 + var7, 4);
            }
         }

         if (var9 >= 0) {
            snowTile(var9, var1, var2, var3, var4);
         }

         if (var10 >= 0) {
            snowTile(var10, var1, var2, var3, var4);
         }
      }
   }

   private static void snowTile(int var0, float var1, float var2, float var3, boolean[] var4) {
      Texture var5 = Texture.getSharedTexture("e_newsnow_ground_1_" + var0);
      if (var5 != null) {
         if (var5.isReady() && var5.getTextureId() != null) {
            WorldMesher.emit(WorldMesher.batch(var5.getTextureId()), TileMeshes.floorMesh(), var5, var1, var2, var3 + 0.004F, 2);
         } else {
            var4[0] = true;
         }
      }
   }

   private static boolean snowFloor(IsoGridSquare var0) {
      return var0.getProperties().has(IsoFlagType.solidfloor)
         && var0.getProperties().has(IsoFlagType.exterior)
         && var0.getRoom() == null
         && !var0.isInARoom()
         && !var0.getProperties().has(IsoFlagType.water)
         && (var0.getWater() == null || !var0.getWater().isValid());
   }

   static void body(IsoDeadBody var0, IsoGridSquare var1, float var2, float var3, float var4, boolean[] var5, boolean var6) {
      PARAMS.init(var0);

      for (ResultLight var10 : PARAMS.lights) {
         var10.radius = 0;
      }

      BodyTexture var11 = DeadBodyAtlas.instance.getBodyTexture(PARAMS);
      Texture var12 = var11 == null ? null : var11.getTexture();
      if (var12 != null && var12.isReady() && var12.getTextureId() != null && ready(var11)) {
         float[] var13 = card(var0, var1, var11, var12, var2, var3, var4);
         if (var6) {
            BodyCards.add(var0, var13, var12.getTextureId(), var11);
         } else {
            WorldMesher.raw(WorldMesher.batch(var12.getTextureId()), CorpseCards.baked(var13));
         }
      } else {
         var5[0] = true;
         WorldMesher.picturePending = true;
         if (var6 && var11 != null) {
            BodyCards.add(var0, null, null, var11);
         }
      }
   }

   private static float[] card(IsoDeadBody var0, IsoGridSquare var1, BodyTexture var2, Texture var3, float var4, float var5, float var6) {
      float var7 = Core.tileScale;
      float var8 = 1.0F / var3.getWidthHW();
      float var9 = 1.0F / var3.getHeightHW();
      float var10 = var3.getXStart() - var2.getRenderX(0.0F) * var8;
      float var11 = var3.getYStart() - var2.getRenderY(0.0F) * var9;
      float var12 = var0.getX() - var1.getX() - 0.5F;
      float var13 = var0.getY() - var1.getY() - 0.5F;
      float[] var14 = new float[28];

      for (int var15 = 0; var15 < 4; var15++) {
         float var16 = var15 != 1 && var15 != 2 ? -1.3F : 1.3F;
         float var17 = var15 >= 2 ? 1.3F : -1.3F;
         int var18 = var15 * 4;
         var14[var18] = -(var4 + var12 + var16);
         var14[var18 + 1] = -(var5 + var13 + var17);
         var14[var18 + 2] = var10 + 32.0F * var7 * (var16 - var17) * var8;
         var14[var18 + 3] = var11 + 16.0F * var7 * (var16 + var17) * var9;
      }

      var14[16] = var6 + 0.012F;
      var14[17] = Float.intBitsToFloat(2 + WorldMesher.lift);
      var14[19] = Float.intBitsToFloat(WorldMesher.square);
      var14[20] = var3.getXStart() + 0.5F * var8;
      var14[21] = var3.getYStart() + 0.5F * var9;
      var14[22] = var3.getXEnd() - 0.5F * var8;
      var14[23] = var3.getYEnd() - 0.5F * var9;
      return var14;
   }

   private static boolean ready(BodyTexture var0) {
      try {
         if (bodyEntry == null) {
            bodyEntry = BodyTexture.class.getDeclaredField("entry");
            bodyEntry.setAccessible(true);
         }

         Object var1 = bodyEntry.get(var0);
         if (var1 == null) {
            return false;
         }

         if (bodyReady == null) {
            bodyReady = var1.getClass().getDeclaredField("ready");
            bodyReady.setAccessible(true);
         }

         return bodyReady.getBoolean(var1);
      } catch (ReflectiveOperationException | RuntimeException var2) {
         return true;
      }
   }

   private FloorDecals() {
   }
}
