package viewpoint.visibility;

import viewpoint.core.Frame;
import viewpoint.core.View;
import viewpoint.platform.LiveSettings;
import viewpoint.render.SceneData;

public final class Glass {
   public static final int REACH = 48;
   private static final String SECTION = "World/Glass";
   private static final LiveSettings.Toggle ON = LiveSettings.toggle("glass.far", "Glass opaque far away, hiding the rooms behind it", "World/Glass", true);
   private static final LiveSettings.Number START = LiveSettings.number(
      "glass.fadeStart", "Glass starts turning opaque at (squares)", "World/Glass", 1.0F, 48.0F, 1.0F, 16.0F
   );
   private static final LiveSettings.Number OPAQUE = LiveSettings.number(
      "glass.opaqueDistance", "Glass opaque from (squares)", "World/Glass", 2.0F, 48.0F, 1.0F, 48.0F
   );
   private static final LiveSettings.Number STOREYS = LiveSettings.number(
      "glass.opaqueStoreys", "Glass opaque from storeys above or below", "World/Glass", 1.0F, 32.0F, 1.0F, 6.0F
   );
   private static final float DISTANCE_MARGIN = 1.0F;
   private static final float STOREY_MARGIN = 0.25F;

   public static void snapshot(Frame var0) {
      SceneData var1 = var0.scene;
      var1.viewerX = var0.onCamera ? 0.0F : var0.eyeX - View.cameraX;
      var1.viewerY = var0.onCamera ? 0.0F : var0.eyeY + View.cameraUp;
      var1.viewerZ = var0.onCamera ? 0.0F : var0.eyeZ - View.cameraY;
      var1.glassFar = ON.get();
      var1.glassOpaque = OPAQUE.get();
      var1.glassStart = Math.min(START.get(), var1.glassOpaque - 1.0F);
      var1.glassStoreys = STOREYS.get();
   }

   static boolean opaque(SceneData var0, float[] var1, int var2, Portals.View var3) {
      if (!var0.glassFar) {
         return false;
      }

      float var4 = var1[var2 + 4] * 2.4494896F + var3.lift(var1, var2);
      float var5 = outside(var3.x, Math.min(var1[var2], var1[var2 + 2]), Math.max(var1[var2], var1[var2 + 2]));
      float var6 = outside(var3.y, Math.min(var1[var2 + 1], var1[var2 + 3]), Math.max(var1[var2 + 1], var1[var2 + 3]));
      float var7 = outside(var3.h, var4, var4 + 2.4494896F);
      return var5 * var5 + var6 * var6 + var7 * var7 >= square(var0.glassOpaque + 1.0F) || var7 >= var0.glassStoreys * 2.4494896F + 0.25F;
   }

   private static float outside(float var0, float var1, float var2) {
      return Math.max(Math.max(var1 - var0, var0 - var2), 0.0F);
   }

   private static float square(float var0) {
      return var0 * var0;
   }

   private Glass() {
   }
}
