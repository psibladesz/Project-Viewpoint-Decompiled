package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import zombie.iso.IsoGridSquare;

@Patch(className = "zombie.iso.objects.IsoFireManager", methodName = "explode")
public class Patch_Explosion {
   @OnEnter
   public static void enter(@Argument(1) IsoGridSquare var0) {
      CombatHooks.explosion(var0);
   }
}
