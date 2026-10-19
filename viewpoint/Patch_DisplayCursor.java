package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;

@Patch(className = "org.lwjglx.opengl.Display", methodName = "updateMouseCursor")
public class Patch_DisplayCursor {
   @OnEnter(skipOn = true)
   public static boolean enter() {
      return Hooks.skipMouseCursorUpdate();
   }
}
