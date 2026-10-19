package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;

@Patch(className = "zombie.core.textures.MultiTextureFBO2", methodName = "setTargetZoom")
public class Patch_TargetZoom {
   @OnEnter(skipOn = true)
   public static boolean enter(@Argument(0) int var0) {
      return Hooks.skipTargetZoom(var0);
   }
}
