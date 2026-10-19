package viewpoint;

import viewpoint.render.FrameGenPass;
import zombie.core.textures.TextureDraw.GenericDrawer;

final class HudlessDrawer extends GenericDrawer {
   static final HudlessDrawer ONE = new HudlessDrawer();

   public void render() {
      FrameGenPass.hudless();
   }
}
