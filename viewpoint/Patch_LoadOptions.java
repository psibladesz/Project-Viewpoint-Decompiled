package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;

@Patch(className = "zombie.core.Core", methodName = "loadOptions")
public class Patch_LoadOptions {
   @OnEnter
   public static void enter() {
      Hooks.optionsLoading();
   }

   @OnExit
   public static void exit() {
      Hooks.optionsLoaded();
   }
}
