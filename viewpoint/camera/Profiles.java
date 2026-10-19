package viewpoint.camera;

import viewpoint.platform.LiveSettings;

final class Profiles {
   private static final String TUNE = "Debug/Camera profiles";
   static final Profiles.Profile STANDING = new Profiles.Profile(
      "standing", "Standing", f(0.42F, 0.6F, 2.1F, 1.0F), m(0.08F, 0.15F, 1.0F, 0.7F, 0.35F, 0.0F, 0.0F)
   );
   static final Profiles.Profile WALKING = new Profiles.Profile(
      "walking", "Walking", f(0.42F, 0.6F, 2.05F, 1.0F), m(0.08F, 0.15F, 1.0F, 0.8F, 0.6F, 0.1F, 0.0F)
   );
   static final Profiles.Profile RUNNING = new Profiles.Profile(
      "running", "Running", f(0.43F, 0.58F, 1.9F, 1.06F), m(0.1F, 0.15F, 1.0F, 1.1F, 1.4F, 0.15F, 0.4F)
   );
   static final Profiles.Profile SPRINTING = new Profiles.Profile(
      "sprinting", "Sprinting", f(0.44F, 0.57F, 1.75F, 1.12F), m(0.12F, 0.18F, 0.85F, 1.6F, 2.2F, 0.2F, 0.7F)
   );
   static final Profiles.Profile CROUCHING = new Profiles.Profile(
      "crouching", "Crouching", f(0.4F, 0.63F, -0.25F, 0.97F), m(0.1F, 0.2F, 1.0F, 0.7F, 0.4F, 0.1F, 0.0F)
   );
   static final Profiles.Profile AIM_MELEE = new Profiles.Profile(
      "aimMelee", "Aiming a melee weapon", f(0.34F, 0.58F, 1.35F, 0.88F), m(0.05F, 0.08F, 1.0F, 0.7F, 0.4F, 0.0F, 0.0F)
   );
   static final Profiles.Profile AIM_FIREARM = new Profiles.Profile(
      "aimFirearm", "Aiming a firearm", f(0.28F, 0.56F, 1.0F, 0.83F), m(0.03F, 0.05F, 1.0F, 0.7F, 0.3F, 0.0F, 0.0F)
   );
   static final Profiles.Profile CLIMBING = new Profiles.Profile(
      "climbing", "Climbing", f(0.47F, 0.56F, 2.3F, 1.05F), m(0.35F, 0.3F, 1.0F, 0.8F, 0.8F, 0.0F, 0.0F)
   );
   static final Profiles.Profile FALLING = new Profiles.Profile(
      "falling", "Falling", f(0.48F, 0.52F, 2.3F, 1.05F), m(0.1F, 0.25F, 1.0F, 1.0F, 1.5F, 0.0F, 0.0F)
   );
   static final Profiles.Profile ON_GROUND = new Profiles.Profile(
      "onGround", "On the ground", f(0.5F, 0.66F, 1.9F, 1.0F), m(0.1F, 0.2F, 1.0F, 0.7F, 0.5F, 0.0F, 0.0F)
   );
   static final Profiles.Profile GRABBED = new Profiles.Profile(
      "grabbed", "Grabbed or bitten", f(0.4F, 0.55F, 1.2F, 0.9F), m(0.05F, 0.08F, 1.0F, 1.0F, 1.2F, 0.0F, 0.0F)
   );
   static final Profiles.Profile SITTING = new Profiles.Profile("sitting", "Sitting", f(0.4F, 0.62F, 1.7F, 0.95F), m(0.1F, 0.2F, 1.0F, 0.6F, 0.3F, 0.0F, 0.0F));
   static final Profiles.Profile SEATED = new Profiles.Profile("vehicle", "Seated", f(0.5F, 0.7F, 6.0F, 1.0F), m(0.15F, 0.2F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F));
   static final Profiles.Profile SEAT_AIMING = new Profiles.Profile(
      "seatAim", "Aiming from a vehicle", f(0.4F, 0.58F, 2.4F, 0.85F), m(0.03F, 0.05F, 1.0F, 0.5F, 0.3F, 0.0F, 0.0F)
   );
   static final int WALK = 0;
   static final int RUN = 1;
   static final int SPRINT = 2;
   static final int CROUCH = 3;
   static final int THREAT = 4;
   static final int TIGHT = 5;
   static final int MELEE = 6;
   static final int FIREARM = 7;
   static final int CLIMB = 8;
   static final int FALL = 9;
   static final int GROUND = 10;
   static final int GRAB = 11;
   static final int SIT = 12;
   static final int SEAT = 13;
   static final int SEAT_AIM = 14;
   static final int LAYERS = 15;
   static final String[] NAMES = new String[]{
      "walk", "run", "sprint", "crouch", "threat", "tight", "melee", "firearm", "climb", "fall", "ground", "grab", "sit", "seat", "seat aim"
   };
   static final float[] IN = new float[]{0.3F, 0.35F, 0.4F, 0.35F, 0.8F, 0.4F, 0.3F, 0.35F, 0.3F, 0.2F, 0.35F, 0.15F, 0.5F, 0.4F, 0.35F};
   static final float[] OUT = new float[]{0.35F, 0.45F, 0.55F, 0.4F, 1.5F, 0.6F, 0.35F, 0.4F, 0.5F, 0.35F, 0.45F, 0.45F, 0.5F, 0.4F, 0.4F};
   static final float THREAT_COUNT = 3.0F;
   static final float THREAT_DISTANCE = 1.12F;
   static final float THREAT_FOV = 1.06F;
   static final float THREAT_HANDHELD = 0.5F;
   static final float TIGHT_DISTANCE = 0.75F;
   static final float TIGHT_SHOULDER = 0.8F;
   static final float LONG_GUN_FOV = 0.78F;
   static final float SEAT_SPEED = 100.0F;
   static final float SEAT_DISTANCE = 1.5F;
   static final float SEAT_FOV = 1.1F;
   static final float SEAT_AIM_RAISE = 0.5F;
   static final float FALL_FOV = 1.1F;
   static final float FALL_SECONDS = 1.5F;
   static final float STAND_HEIGHT = 1.35F;
   static final float CROUCH_HEIGHT = 0.95F;
   static final float GROUND_HEIGHT = 0.3F;
   static final float SIT_HEIGHT = 0.8F;
   static final float CLIMB_TRAIL = 1.0F;

   static void targets(CameraInputs var0, float[] var1) {
      boolean var2 = !var0.seated;
      var1[0] = var2 && var0.moving ? 1.0F : 0.0F;
      var1[1] = !var2 || !var0.moving || !var0.running && !var0.sprinting ? 0.0F : 1.0F;
      var1[2] = var2 && var0.moving && var0.sprinting ? 1.0F : 0.0F;
      var1[3] = var2 && var0.sneaking && var0.aim == 0 ? 1.0F : 0.0F;
      var1[4] = var2 ? Math.min(1.0F, var0.threat / 3.0F) : 0.0F;
      var1[5] = var2 ? var0.tight : 0.0F;
      var1[6] = var2 && var0.aim == 1 ? 1.0F : 0.0F;
      var1[7] = var2 && var0.aim >= 2 ? 1.0F : 0.0F;
      var1[8] = var2 && var0.special == 1 ? 1.0F : 0.0F;
      var1[9] = var2 && var0.special == 2 ? 1.0F : 0.0F;
      var1[10] = var2 && var0.special == 3 ? 1.0F : 0.0F;
      var1[11] = var2 && var0.special == 4 ? 1.0F : 0.0F;
      var1[12] = var2 && var0.special == 5 ? 1.0F : 0.0F;
      var1[13] = var0.seated ? 1.0F : 0.0F;
      var1[14] = var0.seated && var0.aim >= 2 ? 1.0F : 0.0F;
   }

   static void mix(float[] var0, CameraInputs var1, Profiles.Mix var2) {
      var2.set(STANDING);
      var2.toward(WALKING, var0[0]);
      var2.toward(RUNNING, var0[1]);
      var2.toward(SPRINTING, var0[2]);
      crouch(var2, var0[3]);
      var2.distance = var2.distance * (1.0F + 0.120000005F * var0[4]);
      var2.fov = var2.fov * (1.0F + 0.059999943F * var0[4]);
      var2.handheld = var2.handheld + 0.5F * var0[4];
      var2.distance = var2.distance * (1.0F + -0.25F * var0[5]);
      var2.fromLeft = var2.fromLeft + (0.5F - (0.5F - var2.fromLeft) * 0.8F - var2.fromLeft) * var0[5];
      var2.toward(AIM_MELEE, var0[6]);
      var2.toward(AIM_FIREARM, var0[7]);
      if (var1.aim == 3) {
         var2.fov = var2.fov * (1.0F + (0.78F / AIM_FIREARM.fov() - 1.0F) * var0[7]);
      }

      var2.toward(CLIMBING, var0[8]);
      var2.toward(FALLING, var0[9]);
      float var3 = Math.min(1.0F, var1.fallTime / 1.5F);
      var2.fov = var2.fov * (1.0F + (1.1F / FALLING.fov() - 1.0F) * var3 * var0[9]);
      var2.toward(ON_GROUND, var0[10]);
      var2.toward(GRABBED, var0[11]);
      var2.toward(SITTING, var0[12]);
      seated(var2, var0[13], var1.vehicleSpeed);
      var2.toward(SEAT_AIMING, var0[14]);
      stance(var0, var2);
   }

   private static void stance(float[] var0, Profiles.Mix var1) {
      float var2 = 1.35F + -0.40000004F * var0[3];
      var2 += (0.3F - var2) * var0[10];
      var2 += (0.8F - var2) * var0[12];
      var1.height = var2 * (1.0F - var0[13]);
      var1.trail = 0.35F + 0.65F * var0[8];
   }

   private static void crouch(Profiles.Mix var0, float var1) {
      if (!(var1 <= 0.0F)) {
         var0.fromLeft = var0.fromLeft + (CROUCHING.fromLeft() - var0.fromLeft) * var1;
         var0.fromTop = var0.fromTop + (CROUCHING.fromTop() - var0.fromTop) * var1;
         var0.distance = var0.distance + CROUCHING.distance() * var1;
         var0.fov = var0.fov * (1.0F + (CROUCHING.fov() - 1.0F) * var1);
         var0.across = var0.across + (CROUCHING.across - var0.across) * var1;
         var0.up = var0.up + (CROUCHING.up - var0.up) * var1;
      }
   }

   private static void seated(Profiles.Mix var0, float var1, float var2) {
      if (!(var1 <= 0.0F)) {
         var0.toward(SEATED, var1);
         float var3 = Math.min(1.0F, Math.abs(var2) / 100.0F);
         var0.distance += 1.5F * var3 * var1;
         var0.fov *= 1.0F + 0.100000024F * var3 * var1;
         var0.handheld += var3 * var1;
      }
   }

   private static float[] f(float var0, float var1, float var2, float var3) {
      return new float[]{var0, var1, var2, var3};
   }

   private static float[] m(float var0, float var1, float var2, float var3, float var4, float var5, float var6) {
      return new float[]{var0, var1, var2, var3, var4, var5, var6};
   }

   private Profiles() {
   }

   static final class Mix {
      float fromLeft;
      float fromTop;
      float distance;
      float fov;
      float across;
      float up;
      float ratio;
      float handheld;
      float hertz;
      float gait;
      float lean;
      float height;
      float trail;

      void set(Profiles.Profile var1) {
         this.fromLeft = var1.fromLeft();
         this.fromTop = var1.fromTop();
         this.distance = var1.distance();
         this.fov = var1.fov();
         this.across = var1.across;
         this.up = var1.up;
         this.ratio = var1.ratio;
         this.handheld = var1.handheld;
         this.hertz = var1.hertz;
         this.gait = var1.gait;
         this.lean = var1.lean;
      }

      void toward(Profiles.Profile var1, float var2) {
         if (!(var2 <= 0.0F)) {
            this.fromLeft = this.fromLeft + (var1.fromLeft() - this.fromLeft) * var2;
            this.fromTop = this.fromTop + (var1.fromTop() - this.fromTop) * var2;
            this.distance = this.distance + (var1.distance() - this.distance) * var2;
            this.fov = this.fov + (var1.fov() - this.fov) * var2;
            this.across = this.across + (var1.across - this.across) * var2;
            this.up = this.up + (var1.up - this.up) * var2;
            this.ratio = this.ratio + (var1.ratio - this.ratio) * var2;
            this.handheld = this.handheld + (var1.handheld - this.handheld) * var2;
            this.hertz = this.hertz + (var1.hertz - this.hertz) * var2;
            this.gait = this.gait + (var1.gait - this.gait) * var2;
            this.lean = this.lean + (var1.lean - this.lean) * var2;
         }
      }
   }

   static final class Profile {
      private final LiveSettings.Number fromLeft;
      private final LiveSettings.Number fromTop;
      private final LiveSettings.Number distance;
      private final LiveSettings.Number fov;
      final float across;
      final float up;
      final float ratio;
      final float handheld;
      final float hertz;
      final float gait;
      final float lean;

      Profile(String var1, String var2, float[] var3, float[] var4) {
         String var5 = "camera.framing." + var1 + ".";
         this.fromLeft = LiveSettings.number(var5 + "fromLeft", var2 + ": from the left", "Debug/Camera profiles", 0.0F, 1.0F, 0.01F, var3[0]).unsaved();
         this.fromTop = LiveSettings.number(var5 + "fromTop", var2 + ": from the top", "Debug/Camera profiles", 0.0F, 1.0F, 0.01F, var3[1]).unsaved();
         this.distance = LiveSettings.number(var5 + "distance", var2 + ": distance", "Debug/Camera profiles", -1.0F, 8.0F, 0.05F, var3[2]).unsaved();
         this.fov = LiveSettings.number(var5 + "fov", var2 + ": field of view", "Debug/Camera profiles", 0.5F, 1.5F, 0.01F, var3[3]).unsaved();
         this.across = var4[0];
         this.up = var4[1];
         this.ratio = var4[2];
         this.handheld = var4[3];
         this.hertz = var4[4];
         this.gait = var4[5];
         this.lean = var4[6];
      }

      float fromLeft() {
         return this.fromLeft.get();
      }

      float fromTop() {
         return this.fromTop.get();
      }

      float distance() {
         return this.distance.get();
      }

      float fov() {
         return this.fov.get();
      }
   }
}
