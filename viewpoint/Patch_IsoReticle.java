package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.iso.sprite.IsoReticle;

@Patch(className = "zombie.iso.sprite.IsoReticle", methodName = "render")
public class Patch_IsoReticle {
   @OnEnter(skipOn = true)
   public static boolean enter(@This IsoReticle var0) {
      return CombatHooks.skipIsoReticle(var0);
   }

   @OnExit(onThrowable = Throwable.class)
   public static void exit() {
      CombatHooks.isoReticleDone();
   }
}
