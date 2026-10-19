package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;

@Patch(className = "zombie.iso.IsoWorld", methodName = "renderWeatherFX")
public class Patch_WeatherFX {
   @OnEnter(skipOn = true)
   public static boolean enter() {
      return Hooks.skipWeatherFx();
   }
}
