package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import zombie.characters.IsoGameCharacter;

@Patch(className = "zombie.EffectsManager", methodName = "startMuzzleFlash")
public class Patch_MuzzleFlash {
   @OnEnter
   public static void enter(@Argument(0) IsoGameCharacter var0) {
      CombatHooks.muzzleFlash(var0);
   }
}
