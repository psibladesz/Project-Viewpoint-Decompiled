package viewpoint.interact;

import viewpoint.input.PadLook;
import viewpoint.input.PadVanilla;
import viewpoint.platform.PadBind;
import viewpoint.platform.PadBinds;
import viewpoint.platform.PadInput;
import zombie.characters.CharacterJoypadButtonBinding;
import zombie.characters.IsoPlayer;

public final class LootPad {
   private static final boolean[] decided = new boolean[17];
   private static final PadBind[] kept = new PadBind[17];
   private static final boolean[] down = new boolean[17];

   public static void frame() {
      PadInput.sample(PadLook.bind(IsoPlayer.players[0]), down);

      for (int var0 = 0; var0 < 17; var0++) {
         if (!down[var0]) {
            decided[var0] = false;
            kept[var0] = null;
         }
      }
   }

   public static boolean gameSees(CharacterJoypadButtonBinding var0, int var1, boolean var2) {
      if (!var2) {
         return false;
      }

      int var3 = PadVanilla.input(var0);
      return var3 < 0 || keeps(var1, var3) == null;
   }

   public static boolean press(int var0, int var1) {
      PadBind var2 = keeps(var0, var1);
      if (var2 != null) {
         LootMenu.padAsk(var2);
      }

      return var2 != null;
   }

   private static PadBind keeps(int var0, int var1) {
      IsoPlayer var2 = IsoPlayer.players[0];
      if (var2 != null && var0 >= 0 && var0 == PadLook.bind(var2)) {
         if (!decided[var1]) {
            decided[var1] = true;
            PadInput.sample(var0, down);
            down[var1] = true;
            PadBind var3 = var2.isJoypadButtonsActive() ? PadInput.firing(var1, down, var2.isAiming(), PadBinds.loot()) : null;
            kept[var1] = var3 != null && LootMenu.padAsks(var3) ? var3 : null;
         }

         return kept[var1];
      } else {
         return null;
      }
   }

   private LootPad() {
   }
}
