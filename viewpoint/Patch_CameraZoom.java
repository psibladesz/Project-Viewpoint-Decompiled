package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;

@Patch(className = "zombie.audio.parameters.ParameterCameraZoom", methodName = "calculateCurrentValue")
public class Patch_CameraZoom {
   @OnExit
   public static void exit(@Return(readOnly = false) float var0) {
      var0 = Hooks.cameraZoom(var0);
   }
}
