package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;

@Patch(className = "zombie.core.SpriteRenderer", methodName = "renderClamped")
public class Patch_ReticleArm {
   @OnEnter
   public static void enter(
      @Argument(value = 9, readOnly = false) float var0,
      @Argument(value = 10, readOnly = false) float var1,
      @Argument(value = 11, readOnly = false) float var2,
      @Argument(value = 12, readOnly = false) float var3
   ) {
      float[] var4 = CombatHooks.reticleArm(var0, var1, var2, var3);
      var0 = var4[0];
      var1 = var4[1];
      var2 = var4[2];
      var3 = var4[3];
   }
}
