package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import zombie.characters.IsoGameCharacter;

@Patch(className = "zombie.CombatManager", methodName = "calculateHitInfoList")
public class Patch_HitList {
   @OnExit
   public static void exit(@Argument(0) IsoGameCharacter var0) {
      CombatHooks.hitListMade(var0);
   }
}
