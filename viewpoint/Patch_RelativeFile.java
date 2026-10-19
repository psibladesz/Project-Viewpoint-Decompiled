package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.ZomboidFileSystem;

@Patch(className = "zombie.ZomboidFileSystem", methodName = "getRelativeFile")
public class Patch_RelativeFile {
   @OnEnter(skipOn = true)
   public static boolean enter(@This ZomboidFileSystem var0, @Argument(0) String var1) {
      return Hooks.skipRelativeFile(var0, var1);
   }

   @OnExit
   public static void exit(@This ZomboidFileSystem var0, @Argument(0) String var1, @Return(readOnly = false) String var2) {
      var2 = Hooks.relativeFile(var0, var1, var2);
   }
}
