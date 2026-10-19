package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;

@Patch(className = "zombie.iso.sprite.IsoCursor", methodName = "render")
public class Patch_IsoCursor {
   @OnEnter(skipOn = true)
   public static boolean enter() {
      return Hooks.skipIsoCursor();
   }
}
