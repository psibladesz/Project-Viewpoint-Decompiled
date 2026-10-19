package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;

@Patch(className = "zombie.core.Core", methodName = "RenderOffScreenBuffer")
public class Patch_OffscreenRendered {
   @OnExit
   public static void exit() {
      Hooks.offscreenRendered();
   }
}
