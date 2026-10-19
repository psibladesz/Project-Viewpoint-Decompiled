package viewpoint.game;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;
import viewpoint.input.FreeCam;
import viewpoint.platform.BuildPin;
import zombie.characters.CharacterStat;
import zombie.characters.Stats;

public final class GameFixes {
   public static final String SECTION = "Debug/Fixes";
   private static final VarHandle STATS = statsMap();
   public static final boolean statsReady = BuildPin.supported();
   private static final boolean[] kept = new boolean[256];

   @SuppressWarnings("unchecked")
   public static float stat(Stats var0, CharacterStat var1) {
      Map<CharacterStat, Float> var3 = (Map<CharacterStat, Float>)STATS.get(var0);
      Float var2 = var3.get(var1);
      return var2 != null ? var2 : var1.getDefaultValue();
   }

   public static boolean keyDown(int var0, boolean var1, boolean var2) {
      if (!var2 && var0 >= 0 && var0 < kept.length) {
         kept[var0] = var1 && (kept[var0] || FreeCam.active);
         return var1 && !kept[var0];
      } else {
         return var1;
      }
   }

   private static VarHandle statsMap() {
      try {
         return MethodHandles.privateLookupIn(Stats.class, MethodHandles.lookup()).findVarHandle(Stats.class, "stats", Map.class);
      } catch (ReflectiveOperationException var1) {
         throw new IllegalStateException("[Viewpoint] Stats.stats is not this build's", var1);
      }
   }

   private GameFixes() {
   }
}