package viewpoint.input;

import viewpoint.core.SplitScreen;
import viewpoint.core.View;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.PadInput;
import zombie.GameWindow;
import zombie.characters.CharacterInputMode;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.component.CharacterInputComponent;
import zombie.input.JoypadManager;
import zombie.input.JoypadManager.Joypad;
import zombie.iso.Vector2;

public final class PadLook {
   private static final int RIGHT_X = 2;
   private static final int RIGHT_Y = 3;
   private static final LiveSettings.Number YAW_SPEED = LiveSettings.number(
      "pad.yawSpeed", "Turn speed (degrees a second)", "Controller/Look", 60.0F, 400.0F, 10.0F, 220.0F
   );
   private static final LiveSettings.Number PITCH_SPEED = LiveSettings.number(
      "pad.pitchSpeed", "Up and down speed (degrees a second)", "Controller/Look", 40.0F, 300.0F, 10.0F, 160.0F
   );
   private static final LiveSettings.Number DEAD_ZONE = LiveSettings.number("pad.deadZone", "Dead zone", "Controller/Look", 0.0F, 0.4F, 0.01F, 0.15F);
   private static final LiveSettings.Number CURVE = LiveSettings.number("pad.curve", "Response curve", "Controller/Look", 1.0F, 3.0F, 0.1F, 2.0F);
   private static final LiveSettings.Toggle INVERT_Y = LiveSettings.toggle("pad.invertY", "Invert vertical look", "Controller/Look", false);
   private static final LiveSettings.Toggle BOOST = LiveSettings.toggle("pad.turnBoost", "Turn boost", "Controller/Look", true);
   private static volatile StickCurve.Turn turn = StickCurve.Turn.NONE;
   private static volatile boolean holds;
   private static final StickCurve curve = new StickCurve();

   public static void declare() {
   }

   public static void update(IsoGameCharacter var0) {
      IsoPlayer var1 = IsoPlayer.players[0];
      int var2 = bind(var1);
      PadInput.update(var2, var1 != null && var1.isAiming(), var1 == null || var1.isJoypadButtonsActive());
      boolean var3 = var2 >= 0
         && var1 != null
         && active(
            var0 == var1 && owns(var1),
            var1.getInputMode() == CharacterInputMode.GAMEPAD,
            var1.isJoypadButtonsActive(),
            aimIgnored(var1),
            SplitScreen.count(IsoPlayer.players)
         );
      holds = var3;
      if (!var3) {
         curve.reset();
         turn = StickCurve.Turn.NONE;
      } else {
         float var4 = axis(var2, 2);
         float var5 = axis(var2, 3);
         turn = curve.sample(var4, var5, System.nanoTime() * 1.0E-9, settings());
      }
   }

   static boolean active(boolean var0, boolean var1, boolean var2, boolean var3, int var4) {
      return var0 && var1 && var2 && !var3 && var4 <= 1;
   }

   static boolean owns(IsoGameCharacter var0) {
      return View.enabled && var0 != null && var0 == IsoPlayer.players[0] && !var0.isDead() && !FreeCam.active;
   }

   public static int bind(IsoPlayer var0) {
      if (var0 != null) {
         return var0.getInputMode() == CharacterInputMode.GAMEPAD ? var0.getJoypadBind() : -1;
      }

      Joypad var1 = GameWindow.activatedJoyPad;
      return var1 != null ? var1.getID() : -1;
   }

   public static boolean move(IsoPlayer var0, Vector2 var1) {
      int var2 = var0.getInputMode() == CharacterInputMode.GAMEPAD ? var0.getJoypadBind() : -1;
      Joypad var3 = var2 >= 0 ? JoypadManager.instance.getFromControllerID(var2) : null;
      if (var3 == null) {
         return false;
      }

      var1.set(var3.getMovementAxisXRaw(), var3.getMovementAxisYRaw());
      return true;
   }

   private static boolean aimIgnored(IsoPlayer var0) {
      CharacterInputComponent var1 = var0.getCharacterInputComponent();
      return var1 != null && (var1.isJoypadIgnoreAim() || var1.isJoypadIgnoreAimUntilCentered());
   }

   private static float axis(int var0, int var1) {
      return GameWindow.GameInput.getAxisCount(var0) > var1 ? GameWindow.GameInput.getAxisValue(var0, var1) : 0.0F;
   }

   private static StickCurve.Settings settings() {
      return new StickCurve.Settings(
         DEAD_ZONE.get(), CURVE.get(), (float)Math.toRadians(YAW_SPEED.get()), (float)Math.toRadians(PITCH_SPEED.get()), INVERT_Y.get(), BOOST.get()
      );
   }

   static boolean holds() {
      return holds;
   }

   static boolean turn(long var0, long var2, float[] var4) {
      StickCurve.Turn var5 = turn;
      if (var5.still()) {
         return false;
      }

      double var6 = var0 * 1.0E-9;
      double var8 = var2 * 1.0E-9;
      var4[0] = var5.yaw(var6, var8);
      var4[1] = var5.pitch(var6, var8);
      return true;
   }

   private PadLook() {
   }

   static {
      YAW_SPEED.describe(
         "How fast the right stick turns the view across, pushed all the way, in first and third person. While aiming, Aim sensitivity (Controls, Third person) scales it."
      );
      PITCH_SPEED.describe("How fast the right stick turns the view up and down, pushed all the way.");
      DEAD_ZONE.describe("How far the right stick moves before the view turns: raise it if the view drifts with the stick let go.");
      CURVE.describe("How the turn grows as the stick goes over: 1 evenly, higher for finer aim near the centre and the full speed at the rim.");
      INVERT_Y.describe("Pushing the right stick up looks down, and down looks up.");
      BOOST.describe("Held all the way across a moment, the turn quickens, to turn round faster.");
   }
}
