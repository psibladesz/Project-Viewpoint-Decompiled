package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import zombie.vehicles.BaseVehicle;

@Patch(className = "zombie.iso.IsoObjectPicker", methodName = "PickVehicle")
public class Patch_PickVehicle {
   @OnExit
   public static void exit(@Return(readOnly = false) BaseVehicle var0) {
      var0 = Hooks.pickVehicle(var0);
   }
}
