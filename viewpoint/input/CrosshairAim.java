package viewpoint.input;

import org.joml.Vector3f;
import viewpoint.core.Frame;
import viewpoint.core.View;
import viewpoint.render.FarRelief;
import viewpoint.render.MousePick;
import zombie.characters.IsoGameCharacter;
import zombie.core.Core;
import zombie.vehicles.BaseVehicle;

public final class CrosshairAim {
   static final float NEAREST = 1.0F;
   private static final double NONE = Double.POSITIVE_INFINITY;
   private static final long HOLD_NANOS = 1000000000L;
   private static final double RESTED = 0.002;
   private static final int RESTED_SNAPSHOTS = 3;
   private static final float VEHICLE_REACH = 12.0F;
   private static float turn;
   private static float lift;
   private static double cosYaw;
   private static double sinYaw;
   private static double level;
   private static double rise;
   private static double cameraX;
   private static double cameraY;
   private static double cameraUp;
   private static double chestUp;
   private static boolean on;
   private static double placeX;
   private static double placeY;
   private static double placeUp;
   private static double shown = Double.POSITIVE_INFINITY;
   private static double past;
   private static final Vector3f from = new Vector3f();
   private static final Vector3f to = new Vector3f();
   private static final Vector3f met = new Vector3f();
   private static double heldX;
   private static double heldY;
   private static double heldUp;
   private static long heldSince;
   private static boolean holding;
   private static int rested;

   public static void snapshot(Frame var0, IsoGameCharacter var1, FarRelief.Lift var2) {
      boolean var3 = Look.looking() && !FreeCam.active && Controls.aimsByCrosshair(var1);
      on = var3;
      if (!var3) {
         off();
      } else {
         int var4 = Core.getInstance().getScreenWidth();
         int var5 = Core.getInstance().getScreenHeight();
         MousePick.Ask var6 = MousePick.aim;
         if (var6 == null || var6.screenWidth() != var4 || var6.screenHeight() != var5) {
            var6 = new MousePick.Ask(var4 / 2, var5 / 2, var4, var5);
            MousePick.aim = var6;
         }

         double var7 = var1.getAimOriginPosX();
         double var9 = var1.getAimOriginPosY();
         double var11 = placeX;
         double var13 = placeY;
         double var15 = placeUp;
         placeX = var0.camX - var0.eyeX + View.cameraX;
         placeY = var0.camY - var0.eyeZ + View.cameraY;
         placeUp = var0.camZ * 2.4494896F + var0.eyeY + View.cameraUp;
         hold(Math.abs(placeX - var11) + Math.abs(placeY - var13) + Math.abs(placeUp - var15));
         sight(var0.viewYaw, var0.viewPitch, placeX - var7, placeY - var9, placeUp, var1.getAimOriginPosZ() * 2.4494896F);
         past = var1.getVehicle() == null ? 0.0 : leaves(var1.getVehicle(), var2, var7, var9);
         MousePick.Hit var17 = MousePick.aimHit;
         shown = var17 != null && var17.ask() == var6 ? along(var17.x() - var7, var17.y() - var9, var17.z() * 2.4494896F) : Double.POSITIVE_INFINITY;
         shown = shown < past ? Double.POSITIVE_INFINITY : shown;
         turn = turn(shown);
         lift = lift(shown);
      }
   }

   public static void off() {
      MousePick.aim = null;
      lift = 0.0F;
      turn = 0.0F;
      on = false;
      shown = Double.POSITIVE_INFINITY;
      past = 0.0;
      holding = false;
   }

   public static void switched() {
      holding = on && shown != Double.POSITIVE_INFINITY;
      if (holding) {
         heldX = placeX + cosYaw * level * shown;
         heldY = placeY + sinYaw * level * shown;
         heldUp = placeUp + rise * shown;
         heldSince = System.nanoTime();
         rested = 0;
      }
   }

   private static void hold(double var0) {
      long var2 = System.nanoTime();
      if (holding && Look.movedNanos() <= heldSince && var2 - heldSince <= 1000000000L) {
         double var4 = heldX - placeX;
         double var6 = heldY - placeY;
         double var8 = heldUp - placeUp;
         Look.hold((float)Math.atan2(var6, var4), (float)Math.atan2(var8, Math.hypot(var4, var6)));
         rested = var0 < 0.002 ? rested + 1 : 0;
         holding = rested < 3;
      } else {
         holding = false;
      }
   }

   public static boolean camera(double[] var0) {
      var0[0] = placeX;
      var0[1] = placeY;
      var0[2] = placeUp;
      return on;
   }

   public static double depth() {
      return shown;
   }

   public static double pastVehicle() {
      return past;
   }

   private static double leaves(BaseVehicle var0, FarRelief.Lift var1, double var2, double var4) {
      double var6 = var1 == null ? 0.0 : var1.scene(var0.getX(), var0.getY());
      double var8 = placeX + cosYaw * level * 12.0;
      double var10 = placeY + sinYaw * level * 12.0;
      from.set((float)var8, (float)var10, (float)((placeUp + rise * 12.0 - var6) / 2.4494896F));
      to.set((float)placeX, (float)placeY, (float)((placeUp - var6) / 2.4494896F));
      if (var0.getIntersectPoint(from, to, met) == null) {
         return 0.0;
      }

      double var12 = along(met.x - var2, met.y - var4, met.z * 2.4494896F + var6);
      return var12 == Double.POSITIVE_INFINITY ? 0.0 : var12;
   }

   static float yaw(IsoGameCharacter var0) {
      return turn != 0.0F && var0.isAiming() ? Look.wrap(Look.yaw + turn) : Look.yaw;
   }

   static float pitch(IsoGameCharacter var0) {
      return lift != 0.0F && var0.isAiming() ? Look.pitch + lift : Look.pitch;
   }

   static void sight(float var0, float var1, double var2, double var4, double var6, double var8) {
      cosYaw = Math.cos(var0);
      sinYaw = Math.sin(var0);
      level = Math.cos(var1);
      rise = Math.sin(var1);
      cameraX = var2;
      cameraY = var4;
      cameraUp = var6;
      chestUp = var8;
   }

   static double along(double var0, double var2, double var4) {
      double var6 = ((var0 - cameraX) * cosYaw + (var2 - cameraY) * sinYaw) * level + (var4 - cameraUp) * rise;
      return var6 > 0.0 ? var6 : Double.POSITIVE_INFINITY;
   }

   static float turn(double var0) {
      return (float)Math.atan2(right(), ahead(var0));
   }

   static float lift(double var0) {
      if (var0 == Double.POSITIVE_INFINITY) {
         return 0.0F;
      }

      double var2 = ahead(var0);
      double var4 = cameraX * cosYaw + cameraY * sinYaw;
      double var6 = cameraUp + (var2 - var4) / level * rise;
      return (float)(Math.atan2(var6 - chestUp, Math.hypot(right(), var2)) - Math.atan2(rise, level));
   }

   private static double right() {
      return cameraY * cosYaw - cameraX * sinYaw;
   }

   private static double ahead(double var0) {
      return Math.max(1.0, cameraX * cosYaw + cameraY * sinYaw + var0 * level);
   }

   private CrosshairAim() {
   }
}
