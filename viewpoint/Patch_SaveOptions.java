package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;

@Patch(className = "zombie.core.Core", methodName = "saveOptions")
public class Patch_SaveOptions {
   @OnEnter
   public static void enter() {
      Hooks.optionsSaving();
   }

   @OnExit
   public static void exit() {
      Hooks.optionsSaved();
   }
}
