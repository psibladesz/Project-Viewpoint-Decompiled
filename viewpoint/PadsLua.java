package viewpoint;

import me.zed_0xff.zombie_buddy.Exposer.LuaClass;
import org.lwjglx.input.Controller;
import viewpoint.core.View;
import viewpoint.input.AimAssist;
import viewpoint.input.MouseUse;
import viewpoint.input.PadLook;
import viewpoint.input.PadVanilla;
import viewpoint.interact.LootPad;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.PadBind;
import viewpoint.platform.PadBinds;
import viewpoint.platform.PadChord;
import viewpoint.platform.PadInput;
import zombie.GameWindow;
import zombie.characters.CharacterInputMode;
import zombie.characters.IsoPlayer;
import zombie.core.Core;
import zombie.input.JoypadButton;
import zombie.input.JoypadManager;
import zombie.input.JoypadManager.Joypad;

@LuaClass(name = "Viewpoint.Pads")
public final class PadsLua {
   private static final LiveSettings.Toggle HOT_SWAP = LiveSettings.toggle("pad.hotSwap", "Switch to the pad as it is used", "Controller/Switching", true);

   public static int count() {
      PadLook.declare();
      AimAssist.declare();
      PadVanilla.install();
      return PadBinds.count();
   }

   public static String id(int var0) {
      return PadBinds.id(var0);
   }

   public static String label(String var0) {
      return PadBinds.label(var0);
   }

   public static String tooltip(String var0) {
      return PadBinds.tooltip(var0);
   }

   public static String get(String var0) {
      return PadBinds.text(var0);
   }

   public static String fallback(String var0) {
      return PadBinds.fallbackText(var0);
   }

   public static String display(String var0) {
      return PadBinds.display(var0, style());
   }

   public static String glyph(String var0) {
      return PadBinds.glyph(var0, style());
   }

   public static boolean hold(String var0) {
      return PadBinds.hold(var0);
   }

   public static boolean aiming(String var0) {
      return PadBinds.aiming(var0);
   }

   public static String withAiming(String var0, boolean var1) {
      return PadBinds.withAiming(var0, var1);
   }

   public static String warning(String var0, String var1) {
      return PadBinds.warning(var0, var1);
   }

   public static void set(String var0, String var1) {
      PadBinds.set(var0, var1);
   }

   public static void capture(String var0) {
      poll();
      PadBinds.capture(var0);
   }

   public static boolean capturing(String var0) {
      return PadBinds.capturing(var0);
   }

   public static String caught() {
      return PadBinds.caught();
   }

   public static void cancel() {
      PadInput.cancel();
   }

   public static void poll() {
      IsoPlayer var0 = IsoPlayer.players[0];
      PadInput.update(PadLook.bind(var0), var0 != null && var0.isAiming(), var0 == null || var0.isJoypadButtonsActive());
   }

   public static boolean menuPress(int var0, JoypadButton var1) {
      return LootPad.press(var0, var1.ordinal());
   }

   public static boolean hotSwap() {
      return HOT_SWAP.get();
   }

   public static boolean mouseUsed() {
      return MouseUse.used();
   }

   public static boolean usable(int var0) {
      Controller var1 = GameWindow.GameInput.getController(var0);
      if (var1 == null) {
         return false;
      }

      if (JoypadManager.instance.activeControllerGuids.contains(var1.getGUID())) {
         return true;
      }

      for (int var2 = 0; var2 < GameWindow.GameInput.getControllerCount(); var2++) {
         Controller var3 = GameWindow.GameInput.getController(var2);
         if (var3 != null && JoypadManager.instance.activeControllerGuids.contains(var3.getGUID())) {
            return false;
         }
      }

      JoypadManager.instance.setControllerActive(var1.getGUID(), true);
      System.out.println("[Viewpoint] pad switch: pad " + var0 + " enabled, none connected was");
      return true;
   }

   public static void activate(int var0) {
      Joypad var1 = JoypadManager.instance.getFromControllerID(var0);
      if (var1 != null) {
         GameWindow.activatedJoyPad = var1;
      }

      PadInput.took(var0);
   }

   public static int lookCount() {
      return PadBinds.lookCount();
   }

   public static String lookId(int var0) {
      return PadBinds.lookId(var0);
   }

   public static String lookLabel(String var0) {
      return PadBinds.lookLabel(var0);
   }

   public static String lookTooltip(String var0) {
      return PadBinds.lookTooltip(var0);
   }

   public static boolean lookToggle(String var0) {
      return PadBinds.lookToggle(var0);
   }

   public static float lookMin(String var0) {
      return PadBinds.lookMin(var0);
   }

   public static float lookMax(String var0) {
      return PadBinds.lookMax(var0);
   }

   public static float lookStep(String var0) {
      return PadBinds.lookStep(var0);
   }

   public static float lookValue(String var0) {
      return PadBinds.lookValue(var0);
   }

   public static void lookSet(String var0, float var1) {
      PadBinds.lookSet(var0, var1);
   }

   public static String prompts() {
      IsoPlayer var0 = IsoPlayer.players[0];
      if (View.enabled && var0 != null && var0.getInputMode() == CharacterInputMode.GAMEPAD) {
         boolean var1 = var0.isAiming();
         StringBuilder var2 = new StringBuilder();
         prompt(var2, PadBinds.SHOULDER_SWAP, View.thirdPerson, var1, "Swap shoulder");
         prompt(var2, PadBinds.CAMERA_BEHIND, View.thirdPerson && !var1, var1, "Camera behind");
         prompt(var2, PadBinds.THIRD_PERSON, true, var1, View.thirdPerson ? "First person" : "Third person");
         prompt(var2, PadBinds.FIRST_PERSON, true, var1, "Isometric view");
         prompt(var2, PadBinds.SCREENSHOT, true, var1, "Screenshot");
         return var2.toString();
      } else {
         return "";
      }
   }

   private static void prompt(StringBuilder var0, PadBind var1, boolean var2, boolean var3, String var4) {
      PadChord var5 = var1.chord();
      if (var2 && var5 != null && (!var5.aiming() || var3)) {
         StringBuilder var6 = new StringBuilder();

         for (int var8 : var5.buttons()) {
            var6.append(var6.length() == 0 ? "" : ",").append(PadChord.glyph(style(), var8));
         }

         var0.append(var0.length() == 0 ? "" : "\n").append(var6).append('|').append(var4);
      }
   }

   private static String style() {
      return Core.getInstance().getOptionControllerButtonStyleString();
   }

   private PadsLua() {
   }

   static {
      HOT_SWAP.describe(
         "Playing alone, a pad's button or stick makes it the player's, and a key or the mouse gives the player back to the keyboard and mouse. Turn it off for split-screen with the keyboard: a second pad's press then joins as the game's."
      );
   }
}
