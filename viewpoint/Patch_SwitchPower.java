package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.iso.objects.IsoLightSwitch;

@Patch(className = "zombie.iso.objects.IsoLightSwitch", methodName = "hasElectricityAround")
public class Patch_SwitchPower {
   @OnEnter(skipOn = true)
   public static boolean enter(@This IsoLightSwitch var0) {
      return Hooks.skipSwitchPower(var0);
   }

   @OnExit
   public static void exit(@This IsoLightSwitch var0, @Return(readOnly = false) boolean var1) {
      var1 = Hooks.switchPower(var0, var1);
   }
}
