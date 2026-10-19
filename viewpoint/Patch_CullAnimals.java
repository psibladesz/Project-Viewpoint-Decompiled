package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.iso.IsoWorld;

@Patch(className = "zombie.iso.IsoWorld", methodName = "sceneCullAnimals")
public class Patch_CullAnimals {
   @OnEnter(skipOn = true)
   public static boolean enter(@This IsoWorld var0) {
      return Hooks.skipAnimalCull(var0);
   }
}
