package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.iso.PlayerCamera;

@Patch(className = "zombie.iso.PlayerCamera", methodName = "update")
public class Patch_PadPan {
   @OnExit
   public static void exit(@This PlayerCamera var0) {
      Hooks.padPan(var0);
   }
}
