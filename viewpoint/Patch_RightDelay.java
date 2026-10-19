package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;

@Patch(className = "zombie.input.Mouse", methodName = "isRightDelay")
public class Patch_RightDelay {
   @OnExit
   public static void exit(@Return(readOnly = false) boolean var0) {
      var0 = Hooks.rightHeld(var0);
   }
}
