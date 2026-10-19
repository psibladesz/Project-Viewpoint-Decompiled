package viewpoint;

import fmod.fmod.SoundListener;
import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;

@Patch(className = "fmod.fmod.SoundListener", methodName = "tick")
public class Patch_SoundListener {
   @OnExit
   public static void exit(@This SoundListener var0) {
      Hooks.soundListener(var0);
   }
}
