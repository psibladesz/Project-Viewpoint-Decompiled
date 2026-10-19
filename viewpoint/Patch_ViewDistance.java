package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;

@Patch(className = "zombie.core.opengl.RenderSettings$PlayerRenderSettings", methodName = "getViewDistance")
public class Patch_ViewDistance {
   @OnExit
   public static void exit(@Return(readOnly = false) float var0) {
      var0 = Hooks.viewDistance(var0);
   }
}
