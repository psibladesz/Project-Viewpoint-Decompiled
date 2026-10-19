package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;

@Patch(className = "org.lwjglx.opengl.Display", methodName = "update")
public class Patch_FrameSwap {
   @OnEnter
   public static void enter() {
      Hooks.frameSwapping();
   }

   @OnExit
   public static void exit() {
      Hooks.frameSwapped();
   }
}
