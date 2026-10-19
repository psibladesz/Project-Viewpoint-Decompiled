package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import zombie.characters.IsoGameCharacter;

@Patch(className = "zombie.iso.LightingJNI", methodName = "calculateVisionCone")
public class Patch_VisionCone {
   @OnExit
   public static void exit(@Argument(0) IsoGameCharacter var0, @Return(readOnly = false) float var1) {
      var1 = Hooks.visionCone(var0, var1);
   }
}
