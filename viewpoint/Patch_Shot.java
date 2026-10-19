package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import zombie.characters.IsoGameCharacter;
import zombie.inventory.types.HandWeapon;

@Patch(className = "zombie.CombatManager", methodName = "attackCollisionCheck")
public class Patch_Shot {
   @OnEnter
   public static void enter(@Argument(0) IsoGameCharacter var0, @Argument(1) HandWeapon var1) {
      CombatHooks.shotStarting(var0, var1);
   }

   @OnExit(onThrowable = Throwable.class)
   public static void exit() {
      CombatHooks.shotEnded();
   }
}
