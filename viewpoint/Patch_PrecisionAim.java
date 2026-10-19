package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.component.CharacterInputComponent;

@Patch(className = "zombie.characters.component.CharacterInputComponent", methodName = "isPrecisionAimKeyDownInternal")
public class Patch_PrecisionAim {
   @OnExit
   public static void exit(@This CharacterInputComponent var0, @Return(readOnly = false) boolean var1) {
      var1 = CombatHooks.precisionAimDown(var0, var1);
   }
}
