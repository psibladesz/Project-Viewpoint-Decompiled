package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.iso.IsoCell;

@Patch(className = "zombie.iso.IsoCell", methodName = "render")
public class Patch_IsoCell {
   @OnEnter(skipOn = true)
   public static boolean enter(@This IsoCell var0) {
      return Hooks.skipIsoCellRender(var0);
   }
}
