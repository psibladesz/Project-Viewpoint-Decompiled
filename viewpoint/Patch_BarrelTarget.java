package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.core.physics.BallisticsController;

@Patch(className = "zombie.core.physics.BallisticsController", methodName = "isTarget")
public class Patch_BarrelTarget {
   @OnExit
   public static void exit(@This BallisticsController var0, @Argument(0) int var1, @Return(readOnly = false) boolean var2) {
      var2 = CombatHooks.barrelTarget(var0, var1, var2);
   }
}
