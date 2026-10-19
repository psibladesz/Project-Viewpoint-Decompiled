package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.iso.objects.IsoWindow;

@Patch(className = "zombie.iso.objects.IsoWindow", methodName = "setGlassRemoved")
public class Patch_WindowGlass {
   @OnExit
   public static void exit(@This IsoWindow var0) {
      Hooks.windowGlassChanged(var0);
   }
}
