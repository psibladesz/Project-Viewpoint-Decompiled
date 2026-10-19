package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.core.physics.BallisticsController;

@Patch(className = "zombie.core.physics.BallisticsController", methodName = "spreadCount")
public class Patch_PelletCount {
   @OnExit
   public static void exit(@This BallisticsController var0, @Argument(0) int var1, @Return(readOnly = false) int var2) {
      var2 = CombatHooks.pelletCount(var0, var1, var2);
   }
}
