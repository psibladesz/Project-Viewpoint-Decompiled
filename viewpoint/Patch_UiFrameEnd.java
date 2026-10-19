package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;

@Patch(className = "org.lwjglx.opengl.Display", methodName = "imguiEndFrame")
public class Patch_UiFrameEnd {
   @OnExit
   public static void exit() {
      Hooks.uiFrameEnding();
   }
}
