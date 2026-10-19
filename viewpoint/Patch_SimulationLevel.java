package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import zombie.UpdateSchedulerSimulationLevel;
import zombie.iso.IsoMovingObject;

@Patch(className = "zombie.MovingObjectUpdateScheduler", methodName = "getUpdateSchedulerSimulationLevelForObject")
public class Patch_SimulationLevel {
   @OnExit
   public static void exit(@Argument(0) IsoMovingObject var0, @Return(readOnly = false) UpdateSchedulerSimulationLevel var1) {
      var1 = Hooks.simulationLevel(var0, var1);
   }
}
