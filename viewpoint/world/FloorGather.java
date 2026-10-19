package viewpoint.world;

import java.util.ArrayList;
import viewpoint.platform.LiveSettings;
import viewpoint.render.FloorArt;
import zombie.GameTime;
import zombie.core.Core;
import zombie.core.math.PZMath;
import zombie.core.textures.ColorInfo;
import zombie.core.textures.Texture;
import zombie.debug.DebugOptions;
import zombie.iso.IsoChunk;
import zombie.iso.IsoDirections;
import zombie.iso.IsoFloorBloodSplat;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteInstance;
import zombie.iso.sprite.IsoSpriteManager;

final class FloorGather {
   static final LiveSettings.Toggle BAKED = LiveSettings.toggle("floors.baked", "Floors baked", "World/Floors", true);
   private static final float ISO_HALF_WIDTH = 32.0F;
   private static final float ISO_HALF_HEIGHT = 16.0F;
   private static final float ISO_LEVEL_HEIGHT = 96.0F;
   private static final float BLOOD_ALPHA = 0.27F;
   private static final float BLOOD_AGED = 0.2F;
   private static final float BLOOD_AGED_ALPHA = 0.25F;
   private static final float BLOOD_HOURS = 72.0F;
   private static final float VARY_DARK = 42367.543F;
   private static final float VARY_RED = 6367.123F;
   private static final float VARY_BLUE = 23367.133F;
   private static final float VARY_RANGE = 1000.0F;
   private static final float MOST_DARK = 0.25F;
   private static final int THINNED = 10;
   private static final float[] values = new float[15];
   private static final FloorArt.Builder art = new FloorArt.Builder();
   private static final ArrayList<ArrayList<IsoFloorBloodSplat>> bloodBySquare = new ArrayList<>();

   static boolean capture(IsoGridSquare var0, int var1, IsoObject var2, boolean[] var3) {
      if (!isFloor(var2)) {
         return false;
      }

      art.square(var1);
      object(art, var0, var2, var3);
      return true;
   }

   static FloorArt build() {
      FloorArt var0 = art.build();
      art.begin();
      return var0;
   }

   static void bloodOf(IsoChunk var0, int var1) {
      for (ArrayList var3 : bloodBySquare) {
         var3.clear();
      }

      int var7 = Core.getInstance().getOptionBloodDecals();
      if (var7 != 0 && DebugOptions.instance.terrain.renderTiles.bloodDecals.getValue()) {
         for (int var8 = 0; var8 < var0.floorBloodSplats.size(); var8++) {
            IsoFloorBloodSplat var4 = (IsoFloorBloodSplat)var0.floorBloodSplats.get(var8);
            int var5 = PZMath.fastfloor(var4.x);
            int var6 = PZMath.fastfloor(var4.y);
            if ((var4.index < 1 || var4.index > 10 || IsoChunk.renderByIndex[var7 - 1][var4.index - 1] != 0)
               && PZMath.fastfloor(var4.z) == var1
               && var4.type >= 0
               && var4.type < IsoFloorBloodSplat.FLOOR_BLOOD_TYPES.length
               && var5 >= 0
               && var5 < 8
               && var6 >= 0
               && var6 < 8) {
               bloodBySquare.get(var6 * 8 + var5).add(var4);
            }
         }
      }
   }

   static void blood(int var0, boolean[] var1) {
      ArrayList var2 = bloodBySquare.get(var0);
      if (!var2.isEmpty()) {
         art.square(var0);
         float var3 = (float)GameTime.getInstance().getWorldAgeHours();

         for (int var4 = 0; var4 < var2.size(); var4++) {
            IsoFloorBloodSplat var5 = (IsoFloorBloodSplat)var2.get(var4);
            Texture var6 = bloodSprite(IsoFloorBloodSplat.FLOOR_BLOOD_TYPES[var5.type]).getTextureForCurrentFrame(IsoDirections.N);
            if (var6 != null) {
               if (var6.isReady() && var6.getTextureId() != null) {
                  float var7 = var5.x - PZMath.fastfloor(var5.x);
                  float var8 = var5.y - PZMath.fastfloor(var5.y);
                  float var9 = var5.z - PZMath.fastfloor(var5.z);
                  float var10 = Core.tileScale;
                  float var11 = (var7 - var8) * 32.0F * var10 - var6.getWidth() * 0.5F * var10 + var6.getOffsetX();
                  float var12 = (var7 + var8) * 16.0F * var10 - var9 * 96.0F * var10 - var6.getHeight() * 0.5F * var10 + var6.getOffsetY();
                  bloodTint(var5, var3);
                  FloorArt.numbers(values, var6, var11, var12, 1.0F, 1.0F, values[10], values[11], values[12], values[13]);
                  art.layer(var6.getTextureId(), values, FloorArt.withMaterial(0, 10));
               } else {
                  var1[0] = true;
               }
            }
         }
      }
   }

   private static IsoSprite bloodSprite(String var0) {
      IsoSprite var1 = (IsoSprite)IsoFloorBloodSplat.spriteMap.get(var0);
      if (var1 == null) {
         var1 = IsoSprite.CreateSprite(IsoSpriteManager.instance);
         var1.LoadFramesPageSimple(var0, var0, var0, var0);
         IsoFloorBloodSplat.spriteMap.put(var0, var1);
      }

      return var1;
   }

   private static void bloodTint(IsoFloorBloodSplat var0, float var1) {
      float var2 = (var0.x + var0.y / var0.x) * (var0.type + 1);
      float var3 = var2 * var0.x / var0.y * (var0.type + 1) / (var2 + var0.y);
      float var4 = var3 * var2 * var3 * var0.x / (var0.y + 2.0F);
      var2 = Math.min(0.25F, var2 * 42367.543F % 1000.0F / 1000.0F);
      var3 = var3 * 6367.123F % 1000.0F / 1000.0F;
      var4 = var4 * 23367.133F % 1000.0F / 1000.0F;
      float var5 = 1.0F - var2 * 2.0F + var3 / 3.0F;
      float var6 = 1.0F - var2 * 2.0F - var4 / 3.0F;
      float var7 = var6;
      float var8 = var1 - var0.worldAge;
      float var9 = var8 >= 0.0F && var8 < 72.0F ? 1.0F - var8 / 72.0F : 0.0F;
      float var10 = 0.2F + var9 * 0.8F;
      values[10] = var5 * var10;
      values[11] = var6 * var10;
      values[12] = var7 * var10;
      values[13] = 0.27F * (0.25F + var9 * 0.75F);
   }

   static boolean isFloor(IsoObject var0) {
      if (var0 != null && var0.getDoRender()) {
         IsoSprite var1 = var0.getSprite();
         return var1 != null
            && (var1.solidfloor || var1.renderLayer == 1)
            && !var1.getProperties().has(IsoFlagType.invisible)
            && var1.getProperties().getSlopedSurfaceDirection() == null
            && !var1.hasActiveModel();
      } else {
         return false;
      }
   }

   static void object(FloorArt.Builder var0, IsoGridSquare var1, IsoObject var2, boolean[] var3) {
      ColorInfo var4 = var2.getCustomColor();
      boolean var5 = var2 == var1.getFloor() && !var2.hasProperty(IsoFlagType.water);
      layer(var0, var2, var2.getSprite(), null, var4, var5, var3);
      layer(var0, var2, var2.getOverlaySprite(), null, var2.getOverlaySpriteColor(), false, var3);
      ArrayList var6 = var2.getAttachedAnimSprite();
      if (var6 != null) {
         for (int var7 = 0; var7 < var6.size(); var7++) {
            IsoSpriteInstance var8 = (IsoSpriteInstance)var6.get(var7);
            if (var8 != null && !ShoreBlends.blend(var8.parentSprite)) {
               layer(var0, var2, var8.parentSprite, var8, null, false, var3);
            }
         }
      }
   }

   private static void layer(FloorArt.Builder var0, IsoObject var1, IsoSprite var2, IsoSpriteInstance var3, ColorInfo var4, boolean var5, boolean[] var6) {
      if (var2 != null) {
         IsoSpriteInstance var7 = var3 == null ? var2.def : var3;
         int var8 = var7 == null ? 0 : (int)var7.frame;
         Texture var9 = var2.getTextureForFrame(var8, var1.getForwardIsoDirection(), var1.isUseSnowSprite());
         float var10 = var7 == null ? 1.0F : var7.scaleX;
         float var11 = var7 == null ? 1.0F : var7.scaleY;
         if (var9 != null && !(var10 <= 0.0F) && !(var11 <= 0.0F)) {
            if (var9.isReady() && var9.getTextureId() != null) {
               float var12 = 32.0F * Core.tileScale;
               float var13 = 16.0F * Core.tileScale;
               float var14 = var7 == null ? 0.0F : var7.offX;
               float var15 = var7 == null ? 0.0F : var7.offY;
               float var16 = var7 == null ? 0.0F : var7.offZ;
               float var17 = -var1.offsetX + var2.soffX + var9.getOffsetX() * var10 + (var14 - var15) * var12;
               float var18 = -var1.offsetY
                  - var1.getRenderYOffset() * Core.tileScale
                  + var2.soffY
                  + var9.getOffsetY() * var11
                  + (var14 + var15) * var13
                  - var16 * 96.0F * Core.tileScale;
               tint(var4, var2, var7);
               FloorArt.numbers(values, var9, var17, var18, var10, var11, values[10], values[11], values[12], values[13]);
               int var19 = (var7 != null && var7.flip ? 1 : 0) | (var5 ? 2 : 0);
               var0.layer(var9.getTextureId(), values, FloorArt.withMaterial(var19, FloorMaterials.of(var2.getName())));
            } else {
               var6[0] = true;
            }
         }
      }
   }

   private static void tint(ColorInfo var0, IsoSprite var1, IsoSpriteInstance var2) {
      float var3 = (var0 == null ? 1.0F : var0.r) * var1.tintMod.r;
      float var4 = (var0 == null ? 1.0F : var0.g) * var1.tintMod.g;
      float var5 = (var0 == null ? 1.0F : var0.b) * var1.tintMod.b;
      float var6 = var0 == null ? 1.0F : var0.a;
      if (var2 != null) {
         var3 *= var2.tintr;
         var4 *= var2.tintg;
         var5 *= var2.tintb;
         if (!var2.copyTargetAlpha) {
            var6 *= var2.alpha;
         }
      }

      values[10] = var3;
      values[11] = var4;
      values[12] = var5;
      values[13] = var6;
   }

   private FloorGather() {
   }

   static {
      for (int var0 = 0; var0 < 64; var0++) {
         bloodBySquare.add(new ArrayList<>());
      }
   }
}
