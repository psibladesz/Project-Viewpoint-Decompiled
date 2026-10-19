package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;

@Patch(className = "zombie.input.Mouse", methodName = "update")
public class Patch_MouseUpdate {
   @OnExit
   public static void exit() {
      Hooks.mouseUpdated();
   }
}
