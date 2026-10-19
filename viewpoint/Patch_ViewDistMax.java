package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;

@Patch(className = "zombie.GameTime", methodName = "getViewDistMax")
public class Patch_ViewDistMax {
   @OnExit
   public static void exit(@Return(readOnly = false) float var0) {
      var0 = Hooks.viewDistMax(var0);
   }
}
