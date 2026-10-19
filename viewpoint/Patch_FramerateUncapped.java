package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;

@Patch(className = "zombie.core.PerformanceSettings", methodName = "isFramerateUncapped")
public class Patch_FramerateUncapped {
   @OnExit
   public static void exit(@Return(readOnly = false) boolean var0) {
      var0 = Hooks.framerateUncapped(var0);
   }
}
