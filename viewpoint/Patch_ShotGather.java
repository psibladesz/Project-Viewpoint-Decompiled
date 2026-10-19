package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import zombie.characters.IsoGameCharacter;
import zombie.network.fields.hit.AttackVars;
import zombie.network.fields.hit.HitInfo;
import zombie.util.list.PZArrayList;

@Patch(className = "zombie.CombatManager", methodName = "calculateHitListWeapon")
public class Patch_ShotGather {
   @OnEnter
   public static void enter(@Argument(0) IsoGameCharacter var0, @Argument(1) AttackVars var1, @Argument(2) PZArrayList<HitInfo> var2) {
      CombatHooks.shotGather(var0, var1, var2);
   }
}
