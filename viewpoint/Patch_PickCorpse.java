package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import zombie.iso.IsoObject;

@Patch(className = "zombie.iso.IsoObjectPicker", methodName = "PickCorpse")
public class Patch_PickCorpse {
   @OnExit
   public static void exit(@Return(readOnly = false) IsoObject var0) {
      var0 = Hooks.pickCorpse(var0);
   }
}
