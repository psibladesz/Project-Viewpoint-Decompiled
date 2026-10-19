package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;

@Patch(className = "zombie.iso.LightingJNI", methodName = "update")
public class Patch_LightingUpdate {
   @OnEnter
   public static void enter() {
      Hooks.lightingUpdateStarted();
   }

   @OnExit
   public static void exit() {
      Hooks.lightingUpdateEnded();
   }
}
