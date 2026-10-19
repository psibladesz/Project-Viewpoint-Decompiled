package viewpoint.light;

import viewpoint.core.View;

public final class Visibility {
   public static boolean lightingUpdate;

   public static float viewDistance(float var0) {
      return View.enabled && lightingUpdate ? Math.max(var0, drawDistance()) : var0;
   }

   public static float drawDistance() {
      return (View.radiusChunks() + 1) * 8;
   }

   private Visibility() {
   }
}
