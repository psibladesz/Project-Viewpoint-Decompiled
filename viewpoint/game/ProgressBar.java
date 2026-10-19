package viewpoint.game;

import viewpoint.core.View;
import viewpoint.platform.Tuning;
import zombie.IndieGL;
import zombie.core.Color;
import zombie.core.Core;
import zombie.core.textures.Texture;
import zombie.ui.ActionProgressBar;
import zombie.ui.UIManager;

public final class ProgressBar {
   private static final float SWEEP = 1.2F;
   private static Texture back;
   private static Texture fill;

   public static void placed(ActionProgressBar var0, int var1) {
      if (View.enabled && var1 == 0 && textures()) {
         float var2 = scale();
         double var3 = back.getWidth() * var2;
         double var5 = back.getHeight() * var2;
         var0.setWidth(var3);
         var0.setHeight(var5);
         var0.setX(Core.getInstance().getScreenWidth() / 2.0 - var3 / 2.0);
         var0.setY(Core.getInstance().getScreenHeight() * (0.5 + Tuning.progressBarDrop));
      }
   }

   public static boolean drawn(ActionProgressBar var0) {
      if (!View.enabled || var0 != UIManager.getProgressBar(0) || !textures()) {
         return false;
      }

      if (var0.isVisible() && UIManager.visibleAllUi) {
         float var1 = scale();
         IndieGL.glBlendFuncSeparate(770, 771, 1, 771);
         var0.DrawUVSliceTexture(back, 0.0, 0.0, back.getWidth() * var1, back.getHeight() * var1, Color.white, 0.0, 0.0, 1.0, 1.0);
         double var2 = fill.offsetY * var1 - fill.offsetY;
         double var4 = (fill.getWidth() + 1) * var1;
         double var6 = (fill.getHeight() + 1) * var1;
         double var8 = 3.0 * var1;
         float var10 = var0.getValue();
         if (var10 == Float.POSITIVE_INFINITY) {
            float var11 = (float)(System.nanoTime() * 1.0E-9 * 1.2F % 1.0);
            double var12 = var11 < 0.5F ? 0.0 : (var11 - 0.5F) * 2.0F;
            double var14 = var11 < 0.5F ? var11 * 2.0F : 1.0;
            var0.DrawUVSliceTexture(fill, var8, var2, var4, var6, Color.white, var12, 0.0, var14, 1.0);
         } else {
            var0.DrawUVSliceTexture(fill, var8, var2, var4, var6, Color.white, 0.0, 0.0, var10, 1.0);
         }

         return true;
      } else {
         return true;
      }
   }

   private static float scale() {
      return Core.getInstance().getOptionActionProgressBarSize() * Tuning.progressBarScale;
   }

   private static boolean textures() {
      if (back == null || fill == null) {
         back = Texture.getSharedTexture("BuildBar_Bkg");
         fill = Texture.getSharedTexture("BuildBar_Bar");
      }

      return back != null && fill != null;
   }

   private ProgressBar() {
   }
}
