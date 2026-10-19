package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.iso.fboRenderChunk.FBORenderLevels;

@Patch(className = "zombie.iso.fboRenderChunk.FBORenderLevels", methodName = "invalidateAll")
public class Patch_InvalidateAll {
   @OnEnter
   public static void enter(@This FBORenderLevels var0, @Argument(0) long var1) {
      Hooks.allLevelsInvalidated(var0, var1);
   }
}
