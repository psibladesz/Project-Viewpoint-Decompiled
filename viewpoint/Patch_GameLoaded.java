package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;

@Patch(className = "zombie.gameStates.GameLoadingState", methodName = "enter")
public class Patch_GameLoaded {
   @OnExit
   public static void exit() {
      Hooks.gameLoaded();
   }
}
