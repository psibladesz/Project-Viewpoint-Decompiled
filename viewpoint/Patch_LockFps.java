package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;

@Patch(className = "zombie.core.PerformanceSettings", methodName = "getLockFPS")
public class Patch_LockFps {
   @OnExit
   public static void exit(@Return(readOnly = false) int var0) {
      var0 = Hooks.lockFps(var0);
   }
}
