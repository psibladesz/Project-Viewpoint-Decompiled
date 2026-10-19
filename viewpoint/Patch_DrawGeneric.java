package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import zombie.core.textures.TextureDraw.GenericDrawer;

@Patch(className = "zombie.core.SpriteRenderer", methodName = "drawGeneric")
public class Patch_DrawGeneric {
   @OnEnter(skipOn = true)
   public static boolean enter(@Argument(0) GenericDrawer var0) {
      return Hooks.skipDrawGeneric(var0);
   }
}
