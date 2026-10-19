package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;

@Patch(className = "zombie.characters.IsoZombie", methodName = "getScreenProperY")
public class Patch_ZombieScreenY {
   @OnExit
   public static void exit(@Argument(0) int var0, @Return(readOnly = false) int var1) {
      var1 = Hooks.zombieScreenY(var0, var1);
   }
}
