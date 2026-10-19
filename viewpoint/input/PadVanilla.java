package viewpoint.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import viewpoint.platform.PadBinds;
import zombie.characters.CharacterJoypadButtonBinding;
import zombie.core.Core;
import zombie.core.Translator;
import zombie.input.JoypadAxis1d;
import zombie.input.JoypadButton;

public final class PadVanilla {
   private static final Map<Integer, String> LUA = Map.of(
      JoypadButton.Back.ordinal(),
      "the Back-button wheel",
      JoypadButton.Start.ordinal(),
      "the pause menu",
      JoypadButton.DPadLeft.ordinal(),
      "the weapons and light radial",
      JoypadButton.DPadRight.ordinal(),
      "the maps radial",
      JoypadButton.DPadUp.ordinal(),
      "the vehicle and animal radial",
      JoypadButton.DPadDown.ordinal(),
      "the emotes radial"
   );

   public static void install() {
      PadBinds.uses(PadVanilla::uses);
   }

   static List<String> uses(int var0, boolean var1) {
      ArrayList var2 = new ArrayList();

      for (CharacterJoypadButtonBinding var6 : CharacterJoypadButtonBinding.allBindings()) {
         if (input(var6) == var0 && (!var1 || !quietWhileAiming(var6))) {
            var2.add(name(var6));
         }
      }

      String var7 = LUA.get(var0);
      if (var7 != null) {
         var2.add(var7 + " (game)");
      }

      return var2;
   }

   public static int input(CharacterJoypadButtonBinding var0) {
      JoypadButton var1 = var0.getJoypadButton();
      JoypadAxis1d var2 = var0.getJoypadAxis1d();
      if (var1 != null) {
         return var1.ordinal();
      } else {
         return var2 == JoypadAxis1d.LeftTrigger ? 15 : (var2 == JoypadAxis1d.RightTrigger ? 16 : -1);
      }
   }

   private static boolean quietWhileAiming(CharacterJoypadButtonBinding var0) {
      return var0 == CharacterJoypadButtonBinding.Sprint && !Core.getInstance().isToggleToSprint();
   }

   private static String name(CharacterJoypadButtonBinding var0) {
      String var1 = Translator.getTextOrNull(var0.getNameTranslationKey(), new Object[0]);
      return (var1 != null ? var1 : var0.name()) + " (game)";
   }

   private PadVanilla() {
   }
}
