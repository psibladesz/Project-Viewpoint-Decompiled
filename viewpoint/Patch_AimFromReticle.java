package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.IsoPlayer;

@Patch(className = "zombie.characters.IsoPlayer", methodName = "setAngleFromAim")
public class Patch_AimFromReticle {
   @OnExit
   public static void exit(@This IsoPlayer var0) {
      CombatHooks.aimedFromReticle(var0);
   }
}
