package viewpoint;

import fmod.fmod.FMODSoundEmitter;
import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;

@Patch(className = "fmod.fmod.FMODSoundEmitter", methodName = "tick")
public class Patch_SoundEmitter {
   @OnExit
   public static void exit(@This FMODSoundEmitter var0) {
      Hooks.soundEmitter(var0);
   }
}
