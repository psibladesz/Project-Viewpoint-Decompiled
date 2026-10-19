package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import zombie.iso.IsoObject;

@Patch(className = "zombie.iso.IsoObjectPicker", methodName = "PickThumpable")
public class Patch_PickThumpable {
   @OnExit
   public static void exit(@Return(readOnly = false) IsoObject var0) {
      var0 = Hooks.pickThumpable(var0);
   }
}
