package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;

@Patch(className = "zombie.input.AimingReticle", methodName = "getXA")
public class Patch_ReticleX {
   @OnExit
   public static void exit(@Argument(0) int var0, @Return(readOnly = false) int var1) {
      var1 = CombatHooks.reticleX(var0, var1);
   }
}
