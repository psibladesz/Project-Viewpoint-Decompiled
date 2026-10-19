package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.IsoPlayer;
import zombie.iso.Vector2;

@Patch(className = "zombie.characters.IsoPlayer", methodName = "getDeferredMovement")
public class Patch_RootMotion {
   @OnExit
   public static void exit(@This IsoPlayer var0, @Argument(0) Vector2 var1, @Argument(1) boolean var2) {
      Hooks.rootMotion(var0, var1, var2);
   }
}
