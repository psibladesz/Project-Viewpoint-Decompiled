package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import zombie.characters.IsoGameCharacter;
import zombie.inventory.types.HandWeapon;
import zombie.network.fields.hit.HitInfo;
import zombie.util.list.PZArrayList;

@Patch(className = "zombie.CombatManager", methodName = "processBallisticsTargets")
public class Patch_ShotCandidates {
   @OnEnter
   public static void enter(@Argument(0) IsoGameCharacter var0, @Argument(1) HandWeapon var1, @Argument(2) PZArrayList<HitInfo> var2) {
      CombatHooks.shotCandidates(var0, var1, var2);
   }
}
