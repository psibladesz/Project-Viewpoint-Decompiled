package viewpoint.input;

import viewpoint.core.View;
import viewpoint.platform.KeyInput;
import viewpoint.platform.Keys;
import viewpoint.platform.LiveSettings;
import zombie.characters.IsoGameCharacter;

public final class FreeLook {
   private static final LiveSettings.Number ANGLE = LiveSettings.number(
      "camera.freeLookAngle", "Free look's angle (degrees)", "Controls/Camera", 30.0F, 180.0F, 5.0F, 100.0F
   );
   private static volatile float anchor = Float.NaN;
   private static volatile boolean bounded;

   public static void update(IsoGameCharacter var0) {
      boolean var1 = Controls.owns(var0) && Look.captured && !FreeCam.active && !var0.isAiming();
      boolean var2 = var1 && Keys.FREE_LOOK.down(KeyInput::physicallyDown);
      float var3 = anchor;
      bounded = !View.thirdPerson;
      if (var2 && Float.isNaN(var3)) {
         anchor = Look.yaw;
      } else if (!var2 && !Float.isNaN(var3)) {
         anchor = Float.NaN;
         if (var1) {
            Look.turnTo(var3);
         }
      }
   }

   static boolean held() {
      return !Float.isNaN(anchor);
   }

   static float yaw() {
      float var0 = anchor;
      return Float.isNaN(var0) ? Look.yaw : var0;
   }

   static float clamp(float var0) {
      float var1 = anchor;
      if (!Float.isNaN(var1) && bounded) {
         float var2 = (float)Math.toRadians(ANGLE.get());
         float var3 = Look.wrap(var0 - var1);
         return Math.abs(var3) <= var2 ? var0 : Look.wrap(var1 + Math.copySign(var2, var3));
      } else {
         return var0;
      }
   }

   private FreeLook() {
   }

   static {
      ANGLE.describe(
         "In first person, how far the view may turn each way from where the body faces while the free look's key is held. In third person the view goes all round."
      );
   }
}
