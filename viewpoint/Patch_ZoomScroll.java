package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;

@Patch(className = "zombie.core.textures.MultiTextureFBO2", methodName = "doZoomScroll")
public class Patch_ZoomScroll {
   @OnEnter(skipOn = true)
   public static boolean enter(@Argument(0) int var0) {
      return Hooks.skipZoomScroll(var0);
   }
}
