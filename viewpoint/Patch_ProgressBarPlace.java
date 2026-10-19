package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.ui.ActionProgressBar;

@Patch(className = "zombie.ui.ActionProgressBar", methodName = "updateScreenPos")
public class Patch_ProgressBarPlace {
   @OnExit
   public static void exit(@This ActionProgressBar var0, @Argument(0) int var1) {
      Hooks.progressBarPlaced(var0, var1);
   }
}
