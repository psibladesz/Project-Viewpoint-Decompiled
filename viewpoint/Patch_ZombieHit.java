package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoZombie;
import zombie.inventory.types.HandWeapon;

@Patch(className = "zombie.characters.IsoZombie", methodName = "Hit")
public class Patch_ZombieHit {
   @OnExit
   public static void exit(@This IsoZombie var0, @Argument(0) HandWeapon var1, @Argument(1) IsoGameCharacter var2, @Argument(3) boolean var3) {
      CombatHooks.zombieHit(var0, var1, var2, var3);
   }
}
