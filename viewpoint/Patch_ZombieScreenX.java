package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.IsoZombie;

@Patch(className = "zombie.characters.IsoZombie", methodName = "getScreenProperX")
public class Patch_ZombieScreenX {
   @OnExit
   public static void exit(@This IsoZombie var0, @Argument(0) int var1, @Return(readOnly = false) int var2) {
      var2 = Hooks.zombieScreenX(var0, var1, var2);
   }
}
