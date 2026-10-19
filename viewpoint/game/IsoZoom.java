package viewpoint.game;

import viewpoint.core.View;

public final class IsoZoom {
   public static boolean held(int var0) {
      return View.enabled && var0 == 0;
   }

   private IsoZoom() {
   }
}
