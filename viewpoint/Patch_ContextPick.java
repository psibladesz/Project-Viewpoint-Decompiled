package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import zombie.iso.IsoObjectPicker.ClickObject;

@Patch(className = "zombie.iso.IsoObjectPicker", methodName = "ContextPick")
public class Patch_ContextPick {
   @OnExit
   public static void exit(@Argument(0) int var0, @Argument(1) int var1, @Return(readOnly = false) ClickObject var2) {
      var2 = Hooks.contextPick(var0, var1, var2);
   }
}
