package viewpoint.input;

import viewpoint.platform.PadBinds;
import viewpoint.platform.PadChord;
import viewpoint.platform.PadInput;
import zombie.characters.CharacterInputMode;
import zombie.characters.CharacterJoypadButtonBinding;
import zombie.characters.IsoPlayer;
import zombie.characters.component.CharacterInputComponent;
import zombie.input.JoypadAxis2d;

public final class PadAim {
   private static CharacterInputComponent aimer;
   private static int keptPad = -1;
   private static int keptButton = -1;

   public static void update() {
      IsoPlayer var0 = IsoPlayer.players[0];
      int var1 = PadLook.bind(var0);
      PadChord var2 = PadBinds.AIM.chord();
      boolean var3 = var0 != null && var1 >= 0 && var2 != null && canAim(var0);
      boolean var4 = !var3 && keptButton >= 0 && var1 == keptPad && PadInput.isDown(keptButton);
      keptPad = var3 ? var1 : (var4 ? keptPad : -1);
      keptButton = var3 ? var2.button() : (var4 ? keptButton : -1);
      aimer = var3 && PadBinds.AIM.held() ? var0.getCharacterInputComponent() : null;
   }

   private static boolean canAim(IsoPlayer var0) {
      return PadLook.owns(var0) && var0.getInputMode() == CharacterInputMode.GAMEPAD && var0.getVehicle() == null && var0.isJoypadButtonsActive();
   }

   public static boolean axisApplied(CharacterInputComponent var0, boolean var1) {
      IsoPlayer var2 = var1 ? (IsoPlayer)var0.tryGetECSOwnerEntityAs(IsoPlayer.class) : null;
      return decide(var1, var2 != null && PadLook.owns(var2));
   }

   public static boolean precisionAimDown(CharacterInputComponent var0, boolean var1) {
      if (var0 == aimer) {
         return true;
      }

      boolean var2 = var1 && onLookStick(var0.getInputMode(), CharacterJoypadButtonBinding.PrecisionAim);
      IsoPlayer var3 = var2 ? (IsoPlayer)var0.tryGetECSOwnerEntityAs(IsoPlayer.class) : null;
      return decide(var1, var3 != null && PadLook.owns(var3));
   }

   public static boolean gameSees(CharacterJoypadButtonBinding var0, int var1, boolean var2) {
      return var2 && !kept(PadVanilla.input(var0), var1, keptButton, keptPad);
   }

   static boolean kept(int var0, int var1, int var2, int var3) {
      return var2 >= 0 && var0 == var2 && var1 == var3;
   }

   static boolean onLookStick(CharacterInputMode var0, CharacterJoypadButtonBinding var1) {
      return var0 == CharacterInputMode.GAMEPAD && var1.getJoypadAxis2d() == JoypadAxis2d.RightStick;
   }

   static boolean decide(boolean var0, boolean var1) {
      return var0 && !var1;
   }

   private PadAim() {
   }
}
