package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.core.skinnedmodel.model.Model;

@Patch(className = "zombie.core.skinnedmodel.model.Model", methodName = "CreateShader")
public class Patch_ModelShader {
   @OnEnter(skipOn = true)
   public static boolean enter(@This Model var0, @Argument(0) String var1) {
      return AssetHooks.skipModelShader(var0, var1);
   }

   @OnExit
   public static void exit(@This Model var0, @Argument(0) String var1) {
      AssetHooks.modelShaderMade(var0, var1);
   }
}
