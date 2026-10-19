package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;

@Patch(className = "zombie.gameStates.IngameState", methodName = "enter")
public class Patch_PlayStarted {
   @OnEnter
   public static void enter() {
      Hooks.playStarting();
   }

   @OnExit
   public static void exit() {
      Hooks.playStarted();
   }
}
