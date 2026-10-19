package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;

@Patch(className = "zombie.characters.IsoZombie", methodName = "couldSeeHeadSquare")
public class Patch_HeadSquare {
   @OnExit
   public static void exit(@This IsoZombie var0, @Argument(0) IsoPlayer var1, @Return(readOnly = false) boolean var2) {
      var2 = Hooks.couldSeeHeadSquare(var0, var1, var2);
   }
}
