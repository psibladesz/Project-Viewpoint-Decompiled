package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.ui.UIElement;

@Patch(className = "zombie.ui.UIElement", methodName = "update")
public class Patch_UiUpdate {
   @OnEnter
   public static void enter(@This UIElement var0) {
      Hooks.uiUpdateStarting(var0);
   }

   @OnExit
   public static void exit(@This UIElement var0) {
      Hooks.uiUpdateDone(var0);
   }
}
