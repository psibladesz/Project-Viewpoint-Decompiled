package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;

@Patch(className = "zombie.iso.weather.ThunderStorm", methodName = "enqueueThunderEvent")
public class Patch_Thunder {
   @OnEnter
   public static void enter(@Argument(0) int var0, @Argument(1) int var1, @Argument(3) boolean var2) {
      Hooks.thunder(var0, var1, var2);
   }
}
