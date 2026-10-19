package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;

@Patch(className = "zombie.iso.IsoWorld", methodName = "sceneCullZombies")
public class Patch_ZombieCull {
   @OnEnter
   public static void enter() {
      Hooks.zombieCullStarted();
   }

   @OnExit
   public static void exit() {
      Hooks.zombieCullEnded();
   }
}
