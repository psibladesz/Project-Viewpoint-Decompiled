package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.iso.objects.IsoWindow;

@Patch(className = "zombie.iso.objects.IsoWindow", methodName = "ToggleWindow")
public class Patch_WindowToggle {
   @OnExit
   public static void exit(@This IsoWindow var0) {
      Hooks.windowToggled(var0);
   }
}
