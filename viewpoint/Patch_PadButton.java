package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.CharacterJoypadButtonBinding;

@Patch(className = "zombie.characters.CharacterJoypadButtonBinding", methodName = "isDown")
public class Patch_PadButton {
   @OnExit
   public static void exit(@This CharacterJoypadButtonBinding var0, @Argument(0) int var1, @Return(readOnly = false) boolean var2) {
      var2 = Hooks.padButtonDown(var0, var1, var2);
   }
}
