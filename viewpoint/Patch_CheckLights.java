package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;

@Patch(className = "zombie.iso.LightingJNI", methodName = "checkLights")
public class Patch_CheckLights {
   @OnEnter(skipOn = true)
   public static boolean enter() {
      return Hooks.skipLightCheck();
   }
}
