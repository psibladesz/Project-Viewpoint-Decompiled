package viewpoint.camera;

import org.joml.Vector3f;
import viewpoint.core.Frame;
import viewpoint.input.Eye;
import zombie.characters.CharacterStat;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoZombie;
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoCell;
import zombie.iso.IsoMovingObject;
import zombie.vehicles.BaseVehicle;

final class Director {
   static final float MOVING = 0.3F;
   static final float SPEED_TAU = 0.1F;
   static final float THREAT_REACH = 6.0F;
   static final float FALL_START = 0.35F;
   static final float LAND_LEAST = 0.2F;
   static final float HIT_TRAUMA = 0.35F;
   static final float BITE_TRAUMA = 0.6F;
   static final float HEALTH_TRAUMA = 0.03F;
   static final float HEALTH_MOST = 0.3F;
   static final float LAND_TRAUMA = 0.35F;
   static final float LAND_MOST = 0.5F;
   static final Kicks.Kick LANDING = new Kicks.Kick(-1.5F, 0.0F, 0.0F, 0.0F, -0.12F, 0.0F, 0.5F, 0.03F, 0.18F);
   static final float CRASH_LEAST = 15.0F;
   static final float CRASH_TRAUMA = 0.02F;
   private final Vector3f heading = new Vector3f();
   private double lastX;
   private double lastY;
   private boolean started;
   private float speed;
   private float lastHealth = -1.0F;
   private float lastFall;
   private float lastVehicleSpeed;
   private String lastState = "";
   private boolean wasAttacking;

   void reset() {
      this.started = false;
      this.lastHealth = -1.0F;
      this.lastFall = 0.0F;
   }

   void read(IsoGameCharacter var1, Frame var2, IsoCell var3, float var4, CameraInputs var5, Additive var6) {
      this.motion(var1, var4, var5);
      HandWeapon var7 = var1.getUseHandWeapon();
      boolean var8 = var7 != null && var7.isAimedFirearm();
      var5.aim = !var1.isAiming() ? 0 : (!var8 ? 1 : (var7.isTwoHandWeapon() ? 3 : 2));
      var5.attacking = var1.isPerformingAttackAnimation();
      if (var5.attacking && !this.wasAttacking && (!var8 || !var1.isAiming())) {
         Melee.swing(var6, var1);
      }

      this.wasAttacking = var5.attacking;
      var5.state = var1.getCurrentActionContextStateName();
      var5.fallTime = var1.getFallTime();
      var5.special = special(var1, var5.state, var5.fallTime);
      var5.panic = Math.min(1.0F, var1.getStats().get(CharacterStat.PANIC) / 100.0F);
      var5.tired = 1.0F - Math.min(1.0F, var1.getStats().get(CharacterStat.ENDURANCE));
      var5.headBob = Eye.headBob();
      var5.facing = var1.getDirectionAngleRadians();
      var5.health = var1.getBodyDamage().getOverallBodyHealth();
      this.vehicle(var1.getVehicle(), var5, var6);
      this.events(var1, var5, var6);
      var5.threat = threat(var1, var3);
   }

   private void motion(IsoGameCharacter var1, float var2, CameraInputs var3) {
      double var4 = var1.getX();
      double var6 = var1.getY();
      if (this.started && var2 > 0.0F) {
         float var8 = (float)(Math.hypot(var4 - this.lastX, var6 - this.lastY) / var2);
         this.speed = this.speed + (var8 - this.speed) * (1.0F - (float)Math.exp(-var2 / 0.1F));
      } else {
         this.speed = 0.0F;
      }

      this.started = true;
      this.lastX = var4;
      this.lastY = var6;
      var3.speed = this.speed;
      var3.moving = this.speed > 0.3F;
      var3.running = var1.isRunning();
      var3.sprinting = var1.isSprinting();
      var3.sneaking = var1.isSneaking();
   }

   static int special(IsoGameCharacter var0, String var1, float var2) {
      if (var0.isDead()) {
         return 6;
      } else if (var1.startsWith("climb")) {
         return 1;
      } else if ("falling".equals(var1) || var2 > 0.35F) {
         return 2;
      } else if (var1.startsWith("hitreaction-bite") || "hitreaction-crawlerBite".equals(var1)) {
         return 4;
      } else if (!"falldown".equals(var1) && !"knockeddown".equals(var1) && !"onground".equals(var1) && !"getup".equals(var1)) {
         return !var1.startsWith("sit") && !"onbed".equals(var1) ? 0 : 5;
      } else {
         return 3;
      }
   }

   private void vehicle(BaseVehicle var1, CameraInputs var2, Additive var3) {
      var2.seated = var1 != null;
      if (var1 == null) {
         this.lastVehicleSpeed = 0.0F;
      } else {
         var2.vehicleSpeed = var1.getCurrentSpeedKmHour();
         var1.getForwardVector(this.heading);
         var2.vehicleYaw = (float)Math.atan2(this.heading.z, this.heading.x);
         float var4 = Math.abs(this.lastVehicleSpeed) - Math.abs(var2.vehicleSpeed);
         if (var4 > 15.0F) {
            var3.trauma(Math.min(1.0F, var4 * 0.02F));
         }

         this.lastVehicleSpeed = var2.vehicleSpeed;
      }
   }

   private void events(IsoGameCharacter var1, CameraInputs var2, Additive var3) {
      if (!var2.state.equals(this.lastState)) {
         if (var2.state.startsWith("hitreaction-bite") || "hitreaction-crawlerBite".equals(var2.state)) {
            var3.trauma(0.6F);
         } else if (var2.state.startsWith("hitreaction")) {
            var3.trauma(0.35F);
         }

         this.lastState = var2.state;
      }

      if (this.lastHealth >= 0.0F && var2.health < this.lastHealth) {
         var3.trauma(Math.min(0.3F, (this.lastHealth - var2.health) * 0.03F));
      }

      this.lastHealth = var2.health;
      if (var2.fallTime <= 0.0F && this.lastFall > 0.2F) {
         float var4 = Math.min(1.0F, this.lastFall);
         var3.trauma(Math.min(0.5F, this.lastFall * 0.35F));
         var3.kicks.strike(LANDING, var4 * CameraSettings.SHAKE.get());
      }

      this.lastFall = var2.fallTime;
   }

   private static int threat(IsoGameCharacter var0, IsoCell var1) {
      int var2 = 0;

      for (IsoMovingObject var4 : var1.getObjectList()) {
         if (var4 instanceof IsoZombie var5 && !var5.isDead() && var5.getTarget() == var0) {
            float var6 = var5.getX() - var0.getX();
            float var7 = var5.getY() - var0.getY();
            if (var6 * var6 + var7 * var7 < 36.0F) {
               var2++;
            }
         }
      }

      return var2;
   }

   static void pivot(Frame var0, IsoGameCharacter var1, float var2, double[] var3) {
      BaseVehicle var4 = var1.getVehicle();
      var3[0] = var4 != null ? var4.getX() + (var1.getX() - var4.getX()) * var2 : var1.getX();
      var3[1] = var4 != null ? var4.getY() + (var1.getY() - var4.getY()) * var2 : var1.getY();
      var3[2] = var4 != null ? var0.camZ * 2.4494896F + var0.eyeY : var1.getZ() * 2.4494896F;
   }
}
