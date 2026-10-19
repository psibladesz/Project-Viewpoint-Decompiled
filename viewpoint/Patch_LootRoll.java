package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import zombie.iso.IsoObject;

@Patch(className = "zombie.LoadGridsquarePerformanceWorkaround$ItemPicker", methodName = "checkObject")
public class Patch_LootRoll {
   @OnEnter(skipOn = true)
   public static boolean enter(@Argument(0) IsoObject var0) {
      return Hooks.deferLootRoll(var0);
   }
}
