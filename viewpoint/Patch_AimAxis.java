package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.component.CharacterInputComponent;

@Patch(className = "zombie.characters.component.CharacterInputComponent", methodName = "isJoypadAimingAxisApplied")
public class Patch_AimAxis {
   @OnExit
   public static void exit(@This CharacterInputComponent var0, @Return(readOnly = false) boolean var1) {
      var1 = CombatHooks.aimAxisApplied(var0, var1);
   }
}
