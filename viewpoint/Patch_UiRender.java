package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.ui.UIElement;

@Patch(className = "zombie.ui.UIElement", methodName = "render")
public class Patch_UiRender {
   @OnEnter
   public static void enter(@This UIElement var0) {
      Hooks.uiRenderStarting(var0);
   }

   @OnExit
   public static void exit(@This UIElement var0) {
      Hooks.uiRenderDone(var0);
   }
}
