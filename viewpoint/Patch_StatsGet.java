package viewpoint;

import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.Patch.Argument;
import me.zed_0xff.zombie_buddy.Patch.OnEnter;
import me.zed_0xff.zombie_buddy.Patch.OnExit;
import me.zed_0xff.zombie_buddy.Patch.Return;
import me.zed_0xff.zombie_buddy.Patch.This;
import zombie.characters.CharacterStat;
import zombie.characters.Stats;

@Patch(className = "zombie.characters.Stats", methodName = "get")
public class Patch_StatsGet {
   @OnEnter(skipOn = true)
   public static boolean enter() {
      return Hooks.skipStatsGet();
   }

   @OnExit
   public static void exit(@This Stats var0, @Argument(0) CharacterStat var1, @Return(readOnly = false) float var2) {
      var2 = Hooks.statsGet(var0, var1, var2);
   }
}
