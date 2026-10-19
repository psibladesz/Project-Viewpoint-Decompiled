package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;

@Patch(className = "zombie.core.SpriteRenderer", methodName = "postRender")
public class Patch_GameRender {
   @OnEnter
   public static void enter() {
      Hooks.gameRenderStarted();
   }

   @OnExit
   public static void exit() {
      Hooks.gameRenderEnded();
   }
}
