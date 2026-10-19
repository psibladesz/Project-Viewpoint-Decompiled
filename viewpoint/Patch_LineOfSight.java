package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.IsoPlayer;

@Patch(className = "zombie.characters.IsoPlayer", methodName = "updateLOS")
public class Patch_LineOfSight {
   @OnEnter
   public static void enter(@This IsoPlayer var0) {
      Hooks.lineOfSightStarting(var0);
   }
}
