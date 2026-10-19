package viewpoint.camera;

import viewpoint.core.CameraShot;
import viewpoint.core.CameraSquares;
import viewpoint.core.Frame;
import viewpoint.core.View;
import viewpoint.input.Look;

final class CameraStep {
   static final float TIGHT_OPEN = 2.4F;
   static final float TIGHT_RANGE = 1.2F;
   static final float REACH_FACTOR = 1.15F;
   static final float REACH_MORE = 1.5F;
   static final float MIN_FOV = (float)Math.toRadians(15.0);
   static final float MAX_FOV = (float)Math.toRadians(150.0);
   static final float REFERENCE_FOV = (float)Math.toRadians(60.0);
   static final float CHEST_SHARE = 0.33333334F;
   final CameraRig rig = new CameraRig();
   private final float[] placed = new float[3];
   private float lastYaw;
   private float tight;
   private float base;
   private float farther;
   private float raise;
   private double pivotHeight;
   private boolean wasSeated;

   void stop() {
      this.rig.stop();
   }

   void run(Frame var1, CameraInputs var2, double[] var3, float var4, float var5, int var6, CameraStep.Squares var7) {
      CameraShot var8 = var1.cameraShot;
      var2.yawRate = var4 > 0.0F ? Look.wrap(var1.viewYaw - this.lastYaw) / var4 : 0.0F;
      this.lastYaw = var1.viewYaw;
      var2.tight = this.tight;
      var8.cut = !this.rig.started() || var2.seated != this.wasSeated;
      this.wasSeated = var2.seated;
      if (var8.cut) {
         this.rig.start(var2, var6);
      }

      this.rig.weights(var2, var4);
      this.rig.beats.update(var2.state, var4);
      Profiles.Mix var9 = this.rig.mix;
      this.pivotHeight = var3[2] + var9.height;
      Follow var10 = this.rig.follow;
      var8.cut = var8.cut | var10.update(var3[0], var3[1], this.pivotHeight, var9.across, var9.up, var9.ratio, var9.trail, var4);
      var1.scene.cameraCut = var8.cut;
      this.rig.additive.update(var4, var9, var2, CameraSettings.motion(), CameraSettings.SHAKE.get());
      this.base = View.fovY(true);
      this.settings(var2.seated, this.aim());
      this.lens(var1, var8, var5);
      this.boom(var1, var2, var3, var8, var5, var4, var6, var7);
      this.layers(var8);
      CameraView.place(var1, var1.viewYaw, var1.viewPitch, this.placed);
      View.cameraX = var1.eyeX - this.placed[0];
      View.cameraY = var1.eyeZ - this.placed[2];
      View.cameraUp = this.placed[1] - var1.eyeY;
      this.chest(var1, var8);
   }

   private void settings(boolean var1, float var2) {
      this.farther = (var1 ? CameraSettings.VEHICLE_DISTANCE : CameraSettings.DISTANCE).get();
      float var3 = CameraSettings.HEIGHT.get();
      this.raise = var1 ? CameraSettings.VEHICLE_HEIGHT.get() + 0.5F * this.rig.weight[14] : var3 + (CameraSettings.AIM_HEIGHT.get() - var3) * var2;
   }

   private void lens(Frame var1, CameraShot var2, float var3) {
      float var4 = CameraSettings.FOV_EFFECTS.get();
      float var5 = 1.0F + (this.rig.mix.fov * PitchCurves.fov(var1.viewPitch) - 1.0F) * var4;
      float var6 = this.rig.beats.values[6] - this.rig.additive.zoom;
      float var7 = 2.0F * (float)Math.atan(Math.tan(this.base / 2.0F) * var5) + (float)Math.toRadians(var6);
      var2.fovY = Math.max(MIN_FOV, Math.min(MAX_FOV, var7));
      View.frameFovY = var2.fovY;
      var1.scene.projection.setPerspective(var2.fovY, var3, 0.05F, 400.0F);
      var2.aim = this.aim();
   }

   private void boom(Frame var1, CameraInputs var2, double[] var3, CameraShot var4, float var5, float var6, int var7, CameraStep.Squares var8) {
      Profiles.Mix var9 = this.rig.mix;
      Follow var10 = this.rig.follow;
      float var11 = Math.max(0.0F, var9.distance * this.farther * sized(this.base));
      float var12 = (float)Math.tan(var4.fovY / 2.0F);
      float var13 = this.further(var2.seated, var4.aim);
      var4.on = true;
      var4.seated = var2.seated;
      var4.fromLeft = Framing.shifted(var9.fromLeft, var13, var11, var12, var5);
      var4.fromTop = var9.fromTop;
      var4.distance = var11;
      double var14 = var3[0] + var10.lagX();
      double var16 = var3[1] + var10.lagY();
      double var18 = this.pivotHeight + var10.lagH();
      var4.pivotX = -((float)(var14 - var1.camX));
      var4.pivotY = (float)(var18 - var1.camZ * 2.4494896F);
      var4.pivotZ = -((float)(var16 - var1.camY));
      CameraSquares var20 = var1.cameraSquares;
      var8.fill(var20, var14, var16, (int)Math.floor(var18 / 2.4494896F), var11 * 1.15F + 1.5F);
      double var21 = Math.atan(Math.tan(var4.fovY / 2.0) * Math.sqrt(1.0 + var5 * var5));
      var20.nearCorner = 0.05F / (float)Math.cos(var21);
      float var23 = var1.camX - var20.x;
      float var24 = var1.camY - var20.y;
      float var25 = (var1.camZ - var20.level) * 2.4494896F;
      float var26 = var23 - var4.pivotX;
      float var27 = var24 - var4.pivotZ;
      float var28 = var25 + var4.pivotY;
      float var29 = var1.viewYaw;
      float var30 = var1.viewPitch;
      this.tight = Math.max(0.0F, Math.min(1.0F, (2.4F - Boom.span(var20, var26, var27, var28, var29)) / 1.2F));
      float var31 = Framing.right(var4.fromLeft, var11, var12, var5);
      float var32 = Framing.up(PitchCurves.fromTop(var9.fromTop, var30), var11, var12) + PitchCurves.height(var30) + this.raise;
      float var33 = var11 * PitchCurves.distance(var30);
      int var34 = this.rig.sideNow();
      float var35 = room(var20, var26, var27, var28, var31 * var34, var32, var33, var29, var30);
      float var36 = room(var20, var26, var27, var28, -var31 * var34, var32, var33, var29, var30);
      var4.side = this.rig.side(var7, CameraSettings.AUTO_SWAP.get(), var2.aim != 0, var35, var36, var6);
      var4.limit = this.rig.limit(Boom.free(var20, var26, var27, var28, var31 * var4.side, var32, var33, var29, var30), var6);
   }

   private float further(boolean var1, float var2) {
      if (var1) {
         return CameraSettings.VEHICLE_SIDE.get() * this.rig.sideNow();
      }

      float var3 = CameraSettings.SIDE.get();
      return var3 + (CameraSettings.AIM_SIDE.get() - var3) * var2;
   }

   float aim() {
      return Math.max(this.rig.weight[14], Math.max(this.rig.weight[7], this.rig.weight[6]));
   }

   float seatAim() {
      return this.rig.weight[14];
   }

   static float sized(float var0) {
      return (float)(Math.tan(REFERENCE_FOV / 2.0) / Math.tan(var0 / 2.0));
   }

   private static float room(CameraSquares var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float var9 = Boom.shoulder(var0, var1, var2, var3, var4, var5, var7);
      float var10 = var6 > 0.0F ? Boom.free(var0, var1, var2, var3, var4, var5, var6, var7, var8) / var6 : 1.0F;
      return var9 * var10;
   }

   private void layers(CameraShot var1) {
      Additive var2 = this.rig.additive;
      float[] var3 = this.rig.beats.values;
      var1.addYaw = var2.yaw + (float)Math.toRadians(var3[4]);
      var1.addPitch = var2.pitch + (float)Math.toRadians(var3[3]);
      var1.addRoll = var2.roll + (float)Math.toRadians(var3[5]);
      var1.addBack = var2.back + var3[0];
      var1.addUp = var2.up + var3[1] + this.raise;
      var1.addRight = var3[2];
      var1.trauma = var2.trauma();
      System.arraycopy(this.rig.weight, 0, var1.weights, 0, this.rig.weight.length);
      var1.trailAcross = (float)Math.hypot(this.rig.follow.lagX(), this.rig.follow.lagY());
      var1.trailUp = this.rig.follow.lagH();
   }

   private void chest(Frame var1, CameraShot var2) {
      var2.chestX = var2.pivotX;
      var2.chestY = var2.pivotY - this.rig.mix.height * 0.33333334F;
      var2.chestZ = var2.pivotZ;
      float var3 = this.placed[0] - var2.chestX;
      float var4 = this.placed[1] - var2.chestY;
      float var5 = this.placed[2] - var2.chestZ;
      var1.scene.cameraFocus = (float)Math.sqrt(var3 * var3 + var4 * var4 + var5 * var5);
   }

   interface Squares {
      void fill(CameraSquares var1, double var2, double var4, int var6, float var7);
   }
}
