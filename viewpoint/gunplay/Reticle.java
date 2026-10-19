package viewpoint.gunplay;

import java.lang.reflect.Field;
import viewpoint.core.View;
import viewpoint.render.Panini;
import zombie.core.Core;
import zombie.iso.IsoCamera;
import zombie.iso.sprite.IsoReticle;

final class Reticle {
   private static final float LEAST = 5.0F;
   private static final float PENALTY_AT_MOST = 20.0F;
   private static final float PERCENT = 100.0F;
   private static final int BY_PENALTY = 1;
   private static final Field CHANCE;
   private static final Field PENALTY;
   private static boolean hidden;
   private static boolean reticleTexture;
   private static boolean targetTexture;
   private static boolean aimTexture;
   private static final float[] colour;

   static void set(IsoReticle var0, double var1) {
      Core var3 = Core.getInstance();
      int var4 = IsoCamera.getScreenWidth(0);
      int var5 = IsoCamera.getScreenHeight(0);
      float var6 = Panini.ofView(View.frameFovY, (float)var4 / Math.max(1, var5), Panini.strength()).centreScale();
      double var7 = var1 * var6 * var5 / 2.0 / Math.tan(View.frameFovY / 2.0);
      float var9 = var4 * var3.getOptionMaxCrosshairOffset() / 100.0F;
      float var10 = (float)((var7 - 5.0) / Math.max(1.0F, var9 - 5.0F));

      try {
         if (var3.getOptionReticleMode() == 1) {
            PENALTY.setFloat(var0, Math.max(0.0F, var10 * 20.0F));
         } else {
            CHANCE.setFloat(var0, Math.min(100.0F, 100.0F * (1.0F - var10)));
         }
      } catch (IllegalAccessException var12) {
         throw new IllegalStateException("[Viewpoint] gunplay: IsoReticle's target chance could not be set", var12);
      }

      if (!hidden) {
         hidden = true;
         reticleTexture = var3.getOptionShowReticleTexture();
         targetTexture = var3.getOptionShowValidTargetReticleTexture();
         aimTexture = var3.getOptionShowAimTexture();
         var3.setOptionShowReticleTexture(false);
         var3.setOptionShowValidTargetReticleTexture(false);
         var3.setOptionShowAimTexture(false);
      }
   }

   static float[] arm(float var0, float var1, float var2, float var3) {
      boolean var4 = hidden && View.crosshairOnTarget;
      colour[0] = var4 ? View.ON_TARGET[0] : var0;
      colour[1] = var4 ? View.ON_TARGET[1] : var1;
      colour[2] = var4 ? View.ON_TARGET[2] : var2;
      colour[3] = hidden ? var3 * Gunplay.RING_OPACITY.get() : var3;
      return colour;
   }

   static void drawn() {
      if (hidden) {
         hidden = false;
         Core var0 = Core.getInstance();
         var0.setOptionShowReticleTexture(reticleTexture);
         var0.setOptionShowValidTargetReticleTexture(targetTexture);
         var0.setOptionShowAimTexture(aimTexture);
      }
   }

   private Reticle() {
   }

   static {
      try {
         CHANCE = IsoReticle.class.getDeclaredField("targetChance");
         PENALTY = IsoReticle.class.getDeclaredField("targetAimPenalty");
         CHANCE.setAccessible(true);
         PENALTY.setAccessible(true);
      } catch (ReflectiveOperationException | RuntimeException var1) {
         throw new IllegalStateException("[Viewpoint] gunplay: IsoReticle's target chance changed", var1);
      }

      colour = new float[4];
   }
}
