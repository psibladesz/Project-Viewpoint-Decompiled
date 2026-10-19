package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.AllArguments;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;

@Patch(className = "fmod.fmod.FMODManager", methodName = "loadSound")
public class Patch_LoadSound {
   @OnEnter(skipOn = true)
   public static boolean enter(@AllArguments Object[] var0) {
      return AssetHooks.skipLoadSound(var0);
   }

   @OnExit
   public static void exit(@AllArguments Object[] var0, @Return(readOnly = false) long var1) {
      var1 = AssetHooks.loadedSound(var0, var1);
   }
}
