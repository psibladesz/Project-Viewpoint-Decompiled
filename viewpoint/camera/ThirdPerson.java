package viewpoint.camera;

import org.joml.Vector3f;
import viewpoint.core.CameraShot;
import viewpoint.core.Frame;
import viewpoint.core.View;
import viewpoint.input.Controls;
import viewpoint.input.CrosshairAim;
import viewpoint.input.HeadLook;
import viewpoint.input.HeadLookDebug;
import viewpoint.input.Look;
import viewpoint.platform.Keys;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.PadBinds;
import viewpoint.platform.SettingsWindow;
import zombie.characters.IsoGameCharacter;
import zombie.iso.IsoCell;
import zombie.vehicles.BaseVehicle;

public final class ThirdPerson {
   static final float MAX_SECONDS = 0.1F;
   static final float BEHIND_SPEED = 5.0F;
   static final float CHASE_PITCH = (float)Math.toRadians(-8.0);
   private static final CameraStep step = new CameraStep();
   private static final GunKick gun = new GunKick();
   private static final Director director = new Director();
   private static final CameraInputs in = new CameraInputs();
   private static final double[] pivot = new double[3];
   private static final Vector3f heading = new Vector3f();
   private static IsoCell cell;
   private static final CameraStep.Squares LIVE = (var0, var1, var3, var5, var6) -> CameraCapture.squares(var0, cell, var1, var3, var5, var6);
   private static long lastNanos;
   private static boolean wasOn;
   private static boolean flipping;

   public static void declare() {
      CameraSettings.motion();
      HeadLook.declare();
      HeadLookDebug.declare();
   }

   public static void poll(IsoGameCharacter var0) {
      LiveSettings.Toggle var1 = var0 != null && var0.getVehicle() != null ? CameraSettings.SEATED : CameraSettings.ON_FOOT;
      if (Keys.THIRD_PERSON.pressed() || PadBinds.THIRD_PERSON.pressed() || flipping) {
         flipping = false;
         var1.set(!var1.get());
         SettingsWindow.edited();
      }

      if (View.thirdPerson != var1.get()) {
         View.thirdPerson = var1.get();
         CrosshairAim.switched();
         System.out.println("[Viewpoint] third person " + (View.thirdPerson ? "on" : "off"));
      }

      CameraTrace.poll();
      if (View.thirdPerson) {
         if (Keys.SHOULDER_SWAP.pressed() || PadBinds.SHOULDER_SWAP.pressed()) {
            CameraSettings.SHOULDER.set(1 - CameraSettings.SHOULDER.get());
            SettingsWindow.edited();
         }

         if ((Keys.CAMERA_BEHIND.pressed() || PadBinds.CAMERA_BEHIND.pressed()) && var0 != null) {
            Look.turnTo(var0.getDirectionAngleRadians());
         }
      }
   }

   public static void flip() {
      flipping = true;
   }

   public static void capture(Frame var0, IsoCell var1, IsoGameCharacter var2, float var3) {
      CameraShot var4 = var0.cameraShot;
      boolean var5 = View.thirdPerson && !var0.onCamera;
      var0.cameraSquares.on = var5;
      long var6 = System.nanoTime();
      float var8 = lastNanos == 0L ? 0.0F : Math.min(0.1F, (float)(var6 - lastNanos) * 1.0E-9F);
      lastNanos = var6;
      Additive var9 = step.rig.additive;
      director.read(var2, var0, var1, var8, in, var9);
      HitStop.tick(var8);
      if (var5) {
         thirdPerson(var0, var1, var2, var8, var3);
      } else {
         var0.scene.cameraCut = wasOn;
         var0.scene.cameraFocus = var0.scene.cameraAim = var0.scene.cameraTrauma = 0.0F;
         if (wasOn) {
            wasOn = false;
            off();
         }

         firstPerson(var0, var4, var2, var8, var3);
         behind(var0.onCamera ? null : var2, false);
      }

      gun.update(var8);
      HeadLookDebug.capture(var0, var5);
   }

   private static void thirdPerson(Frame var0, IsoCell var1, IsoGameCharacter var2, float var3, float var4) {
      CameraShot var5 = var0.cameraShot;
      wasOn = true;
      Director.pivot(var0, var2, step.seatAim(), pivot);
      Impulses.apply(step.rig.additive, gun, var2.getX(), var2.getY(), var2.getZ(), step.aim());
      cell = var1;
      step.run(var0, in, pivot, var3, var4, preferredSide(), LIVE);
      float var6 = 1.0F + (CameraSettings.AIM_SENSITIVITY.get() - 1.0F) * var5.aim;
      Look.zoom = (float)(Math.tan(var5.fovY / 2.0) / Math.tan(View.fovY(true) / 2.0)) * var6;
      Look.aimScale = 1.0F;
      hand(var0, var2, var5);
      CameraTrace.record(var0, in, pivot, var3, var4);
   }

   private static void off() {
      View.shoulder = 0;
      step.stop();
   }

   private static void firstPerson(Frame var0, CameraShot var1, IsoGameCharacter var2, float var3, float var4) {
      Additive var5 = step.rig.additive;
      Impulses.apply(var5, gun, var2.getX(), var2.getY(), var2.getZ(), var2.isAiming() ? 1.0F : 0.0F);
      var5.update(var3, step.rig.mix, in, 0.0F, CameraSettings.SHAKE.get());
      var1.clear();
      var1.kicked = !var0.onCamera;
      var1.addYaw = var5.yaw;
      var1.addPitch = var5.pitch;
      var1.addRoll = var5.roll;
      var1.trauma = var5.trauma();
      View.cameraUp = 0.0F;
      View.cameraY = 0.0F;
      View.cameraX = 0.0F;
      float var6 = Math.max(CameraStep.MIN_FOV, View.fovY(false) - (float)Math.toRadians(var5.zoom));
      View.frameFovY = var6;
      var0.scene.projection.setPerspective(var6, var4, 0.05F, 400.0F);
      Look.zoom = 1.0F;
      Look.aimScale = var2.isAiming() ? CameraSettings.AIM_SENSITIVITY.get() : 1.0F;
   }

   private static void hand(Frame var0, IsoGameCharacter var1, CameraShot var2) {
      var2.handYaw = Controls.handYaw(var1);
      var2.handPitch = Controls.handPitch(var1);
      var2.clearModels = true;
      var2.clearPlants = CameraSettings.DISSOLVE_PLANTS.get();
      var0.scene.cameraAim = var2.aim;
      var0.scene.cameraTrauma = var2.trauma;
      View.shoulder = var2.side < 0.0F ? -1 : 1;
      behind(var1, true);
   }

   private static void behind(IsoGameCharacter var0, boolean var1) {
      BaseVehicle var2 = var0 != null ? var0.getVehicle() : null;
      boolean var3 = var2 != null && CameraSettings.VEHICLE_BEHIND.get();
      if (var3 && !(Math.abs(var2.getCurrentSpeedKmHour()) <= 5.0F)) {
         var2.getForwardVector(heading);
         Look.behind((float)Math.atan2(heading.z, heading.x), var1 ? CHASE_PITCH : Float.NaN, var1);
      } else {
         Look.behind(Float.NaN, Float.NaN, false);
      }
   }

   static int preferredSide() {
      return CameraSettings.SHOULDER.get() == 0 ? 1 : -1;
   }

   private ThirdPerson() {
   }
}
