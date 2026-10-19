package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.core.skinnedmodel.animation.AnimationPlayer;

@Patch(className = "zombie.core.skinnedmodel.animation.AnimationPlayer", methodName = "updateModelTransforms")
public class Patch_SkeletonPosed {
   @OnExit
   public static void exit(@This AnimationPlayer var0) {
      Hooks.skeletonPosed(var0);
   }
}
