package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.ui.ActionProgressBar;

@Patch(className = "zombie.ui.ActionProgressBar", methodName = "render")
public class Patch_ProgressBarDraw {
   @OnEnter(skipOn = true)
   public static boolean enter(@This ActionProgressBar var0) {
      return Hooks.skipProgressBar(var0);
   }
}
