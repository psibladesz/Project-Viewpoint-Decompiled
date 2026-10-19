package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.iso.objects.IsoWindow;

@Patch(className = "zombie.iso.objects.IsoWindow", methodName = "smashWindow")
public class Patch_WindowSmash {
   @OnExit
   public static void exit(@This IsoWindow var0, @Argument(0) boolean var1, @Argument(1) boolean var2) {
      Hooks.windowSmashed(var0);
   }
}
