package viewpoint.input;

import viewpoint.core.Frame;
import viewpoint.platform.LiveSettings;

public final class SeatedView {
   public static final int PITCH_AND_ROLL = 0;
   public static final int PITCH = 1;
   public static final int LEVEL = 2;
   private static final LiveSettings.Choice FOLLOW = LiveSettings.choice(
      "camera.vehicleTilt", "Vehicle tilt", "Controls/Camera", new String[]{"Pitch and roll", "Pitch only", "Level horizon"}, 0
   );

   private SeatedView() {
   }

   public static int follow() {
      return FOLLOW.get();
   }

   public static void level(Frame var0) {
      var0.seatTilt[0] = 0.0F;
      var0.seatTilt[1] = 0.0F;
      var0.seatTilt[2] = 0.0F;
      var0.seatTilt[3] = 1.0F;
   }

   public static void turn(Frame var0, float[] var1) {
      float var2 = var0.seatTilt[0];
      float var3 = var0.seatTilt[1];
      float var4 = var0.seatTilt[2];
      float var5 = var0.seatTilt[3];
      float var6 = 2.0F * (var3 * var1[2] - var4 * var1[1]);
      float var7 = 2.0F * (var4 * var1[0] - var2 * var1[2]);
      float var8 = 2.0F * (var2 * var1[1] - var3 * var1[0]);
      float var9 = var1[0] + var5 * var6 + (var3 * var8 - var4 * var7);
      float var10 = var1[1] + var5 * var7 + (var4 * var6 - var2 * var8);
      var1[2] = var1[2] + var5 * var8 + (var2 * var7 - var3 * var6);
      var1[0] = var9;
      var1[1] = var10;
   }

   static {
      FOLLOW.describe(
         "Seated in a vehicle on a slope, how the first-person view turns with it: its pitch and roll, as a head in the car turns; its pitch alone, the horizon kept level across; or not at all."
      );
   }
}
