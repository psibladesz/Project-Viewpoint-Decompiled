package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.core.skinnedmodel.ModelManager.ModelSlot;

@Patch(className = "zombie.core.skinnedmodel.ModelManager$ModelSlot", methodName = "Update")
public class Patch_ModelSlotUpdate {
   @OnEnter
   public static void enter(@This ModelSlot var0) {
      Hooks.modelSlotUpdating(var0);
   }

   @OnExit
   public static void exit() {
      Hooks.modelSlotUpdated();
   }
}
