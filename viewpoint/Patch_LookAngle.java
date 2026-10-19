package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.IsoGameCharacter;

@Patch(className = "zombie.characters.IsoGameCharacter", methodName = "getLookAngleRadians")
public class Patch_LookAngle {
   @OnExit
   public static void exit(@This IsoGameCharacter var0, @Return(readOnly = false) float var1) {
      var1 = Hooks.lookAngle(var0, var1);
   }
}
