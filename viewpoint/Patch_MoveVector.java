package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.IsoPlayer;
import zombie.iso.Vector2;

@Patch(className = "zombie.characters.IsoPlayer", methodName = "getInputMoveVector")
public class Patch_MoveVector {
   @OnExit
   public static void exit(@This IsoPlayer var0, @Argument(0) Vector2 var1) {
      Hooks.inputMoveVector(var0, var1);
   }
}
