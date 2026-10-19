package viewpoint.world;

import viewpoint.render.FarRelief;
import zombie.core.textures.Texture;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteManager;

final class DeckWater {
   private static final float CLEAR = 0.25F;
   private static final String WATER = "blends_natural_02_0";

   private DeckWater() {
   }

   static void under(IsoGridSquare var0, float var1, float var2, float var3, boolean[] var4) {
      FarRelief.Field var5 = FarRelief.walls();
      if (var5 != null && var0.getZ() == 0 && !var0.getProperties().has(IsoFlagType.water)) {
         double var6 = var0.x + 0.5;
         double var8 = var0.y + 0.5;
         if (var5.wet(var6, var8) && !(var5.metresAt(var6, var8) - var5.waterAt(var6, var8) < 0.25F)) {
            IsoSprite var10 = IsoSpriteManager.instance.getSprite("blends_natural_02_0");
            Texture var11 = var10 == null ? null : var10.getTextureForCurrentFrame(IsoDirections.N);
            if (var11 != null && var11.isReady() && var11.getTextureId() != null) {
               WorldMesher.emit(WorldMesher.batch(var11.getTextureId()), TileMeshes.floorMesh(), var11, var1, var2, var3, 2052);
            } else {
               var4[0] |= var11 != null;
            }
         }
      }
   }
}
