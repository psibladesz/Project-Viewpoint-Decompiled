package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.IsoGameCharacter;

@Patch(className = "zombie.characters.IsoGameCharacter", methodName = "isRunning")
public class Patch_Running {
   @OnExit
   public static void exit(@This IsoGameCharacter var0, @Return(readOnly = false) boolean var1) {
      var1 = Hooks.running(var0, var1);
   }
}
