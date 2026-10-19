package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;

@Patch(className = "zombie.input.KeyboardState", methodName = "isKeyDown")
public class Patch_KeyDown {
   @OnExit
   public static void exit(@Argument(0) int var0, @Return(readOnly = false) boolean var1) {
      var1 = Hooks.keyDown(var0, var1);
   }
}
