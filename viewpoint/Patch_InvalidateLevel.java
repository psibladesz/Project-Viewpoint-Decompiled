package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.iso.fboRenderChunk.FBORenderLevels;

@Patch(className = "zombie.iso.fboRenderChunk.FBORenderLevels", methodName = "invalidateLevel")
public class Patch_InvalidateLevel {
   @OnEnter
   public static void enter(@This FBORenderLevels var0, @Argument(0) int var1, @Argument(1) long var2) {
      Hooks.levelInvalidated(var0, var1, var2);
   }
}
