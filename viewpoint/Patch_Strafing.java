package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.IsoGameCharacter;

@Patch(className = "zombie.characters.IsoGameCharacter", methodName = "isStrafing")
public class Patch_Strafing {
   @OnExit
   public static void exit(@This IsoGameCharacter var0, @Return(readOnly = false) boolean var1) {
      var1 = Hooks.strafing(var0, var1);
   }
}
