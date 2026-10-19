package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import zombie.characters.IsoGameCharacter;

@Patch(className = "zombie.CombatManager", methodName = "missedShot")
public class Patch_MissedShot {
   @OnEnter
   public static void enter(
      @Argument(0) IsoGameCharacter var0,
      @Argument(value = 2, readOnly = false) float var1,
      @Argument(value = 3, readOnly = false) float var2,
      @Argument(value = 4, readOnly = false) float var3
   ) {
      float[] var4 = CombatHooks.missedShot(var0, var1, var2, var3);
      var1 = var4[0];
      var2 = var4[1];
      var3 = var4[2];
   }
}
