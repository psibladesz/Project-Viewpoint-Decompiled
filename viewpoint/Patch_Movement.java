package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.IsoPlayer;

@Patch(className = "zombie.characters.IsoPlayer", methodName = "updateMovementFromInput")
public class Patch_Movement {
   @OnEnter
   public static void enter(@This IsoPlayer var0) {
      Hooks.movementStarting(var0);
   }

   @OnExit
   public static void exit(@This IsoPlayer var0) {
      Hooks.movementDone(var0);
   }
}
