package viewpoint.input;

import viewpoint.platform.LiveSettings;

public final class AimAssist {
   private static final LiveSettings.Toggle ON = LiveSettings.toggle("pad.assist", "Aim assist", "Controller/Aim assist", true);
   private static final LiveSettings.Toggle SNAP = LiveSettings.toggle("pad.assistSnap", "Snap as aiming starts", "Controller/Aim assist", true);
   private static final LiveSettings.Number TRACKING = LiveSettings.number("pad.assistTracking", "Tracking", "Controller/Aim assist", 0.0F, 1.0F, 0.05F, 0.5F);
   private static final LiveSettings.Number SLOWDOWN = LiveSettings.number("pad.assistSlowdown", "Slowdown", "Controller/Aim assist", 0.0F, 0.9F, 0.05F, 0.4F);
   static final double STALE = 0.25;
   static final double SNAP_TAU = 0.06;
   static final double TRACK_FULL = 1.5;
   static final double TRACK_NONE = 3.0;
   static final double SLOW_FULL = 1.0;
   static final double SLOW_NONE = 2.5;
   private static volatile AimAssist.Target target;

   public static void declare() {
   }

   public static boolean wanted() {
      return ON.get() && PadLook.holds();
   }

   public static boolean snaps() {
      return SNAP.get();
   }

   public static void publish(AimAssist.Target var0) {
      target = var0;
   }

   static boolean turn(long var0, float var2, float var3, float var4, float var5, float var6, float[] var7) {
      return ON.get() && PadLook.holds() && apply(target, var0 * 1.0E-9, var2, var3, var4, var5, var6, TRACKING.get(), SLOWDOWN.get(), var7);
   }

   static boolean apply(
      AimAssist.Target var0, double var1, double var3, double var5, double var7, double var9, double var11, double var13, double var15, float[] var17
   ) {
      if (var0 != null && !(var3 <= 0.0) && !(var1 - var0.at() > 0.25)) {
         double var18 = var1 - var0.at();
         double var20 = Math.IEEEremainder(var0.yaw() + var0.yawRate() * var18 - var5, Math.PI * 2);
         double var22 = var0.pitch() + var0.pitchRate() * var18 - var7;
         double var24 = Math.hypot(var20 * Math.cos(var7), var22) / var0.width();
         double var26 = 1.0 - var15 * fade(var24, 1.0, 2.5);
         double var28 = var13 * fade(var24, 1.5, 3.0) * var3;
         double var30 = var9 * var26 + var0.yawRate() * var28;
         double var32 = var11 * var26 + var0.pitchRate() * var28;
         if (var1 < var0.snapUntil()) {
            double var34 = 1.0 - Math.exp(-var3 / 0.06);
            var30 += var20 * var34;
            var32 += var22 * var34;
         }

         var17[0] = (float)var30;
         var17[1] = (float)var32;
         return true;
      } else {
         return false;
      }
   }

   static double fade(double var0, double var2, double var4) {
      return Math.max(0.0, Math.min(1.0, (var4 - var0) / (var4 - var2)));
   }

   private AimAssist() {
   }

   static {
      ON.describe(
         "Aiming a gun on a pad, in first and third person, the view is helped onto a zombie near the crosshair. The shot still goes exactly where the crosshair is. Never onto a player."
      );
      SNAP.describe("As you start to aim, the view eases onto the chest of a zombie near the crosshair.");
      TRACKING.describe(
         "How much of a zombie's movement across the view the view follows while the crosshair is on it: 0 none, 1 all of it. A zombie standing still is never pulled to."
      );
      SLOWDOWN.describe("How much slower the right stick turns the view while the crosshair is over a zombie's body, so it stops on it: 0 not at all.");
   }

   public record Target(double yaw, double pitch, double width, double yawRate, double pitchRate, double at, double snapUntil) {
   }
}
