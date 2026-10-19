package viewpoint.input;

import java.util.Set;
import viewpoint.core.View;
import viewpoint.platform.LiveSettings;
import zombie.characters.CharacterInputMode;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.core.Core;
import zombie.core.physics.BallisticsController;
import zombie.iso.IsoCamera;
import zombie.iso.IsoUtils;
import zombie.iso.Vector2;
import zombie.iso.Vector3;

public final class Controls {
   private static final String MOVEMENT = "Controls/Movement";
   private static final LiveSettings.Choice MODEL = LiveSettings.choice(
      "movement.model", "Walking", "Controls/Movement", new String[]{"Turn to move", "Strafe"}, 1
   );
   private static final LiveSettings.Number DIRECTION_SMOOTHING = LiveSettings.number(
      "movement.directionSmoothing", "Direction smoothing (s)", "Controls/Movement", 0.0F, 0.15F, 0.01F, 0.05F
   );
   private static final LiveSettings.Number TURN_SMOOTHING = LiveSettings.number(
      "movement.turnSmoothing", "Body turn smoothing (s)", "Controls/Movement", 0.0F, 0.2F, 0.01F, 0.07F
   );
   private static final LiveSettings.Number BACKPEDAL_SPEED = LiveSettings.number(
      "movement.backpedalSpeed", "Backpedal speed (of the walk's)", "Controls/Movement", 0.5F, 1.0F, 0.05F, 0.8F
   );
   private static final float AIM_DISTANCE = 8.0F;
   private static final long TRACE_MS = 1000L;
   private static final float MAX_UPDATE_SECONDS = 0.1F;
   private static final String WALK_STATE = "movement";
   private static final String STRAFE_STATE = "strafe";
   private static final Set<String> WALK_STATES = Set.of("movement", "strafe");
   private static final MoveInput input = new MoveInput();
   private static final Locomotion locomotion = new Locomotion();
   private static final StrafePace strafePace = new StrafePace();
   private static final Vector2 probe = new Vector2();
   private static final Vector2 padStick = new Vector2();
   private static boolean inputDue;
   private static float updateSeconds;
   private static long updateNanos;
   private static float bodyAngle;
   private static boolean turned;
   private static boolean moved;
   private static float moveX;
   private static float moveY;
   private static float steerSeconds;
   private static float lastX;
   private static float lastY;
   private static int stillFrames;
   private static String tracedState;
   private static boolean attacking;
   private static boolean reticleCentred;
   private static long tracedAt;

   static boolean owns(IsoGameCharacter var0) {
      return View.enabled && var0 != null && var0 == IsoPlayer.players[0] && var0.getVehicle() == null && !var0.isDead();
   }

   static boolean aimsByCrosshair(IsoGameCharacter var0) {
      return owns(var0)
         || View.enabled
            && var0 != null
            && (!View.thirdPerson || var0.isAiming())
            && var0 == IsoPlayer.players[0]
            && var0.getVehicle() != null
            && !var0.isDead();
   }

   public static void moveVector(IsoPlayer var0, Vector2 var1) {
      moved = false;
      if (owns(var0)) {
         float var2 = var1.getLength();
         if (inputDue) {
            inputDue = false;
            boolean var3 = var2 > 0.0F && PadLook.move(var0, padStick);
            float var4 = var3 ? -padStick.y : -var1.y;
            float var5 = var3 ? padStick.x : var1.x;
            input.update(updateSeconds, var4, var5, DIRECTION_SMOOTHING.get());
            int var6 = View.thirdPerson && !var0.isAiming() ? 2 : MODEL.get();
            locomotion.update(var6, input.moving(), input.direction());
            if (locomotion.switched()) {
               float var7 = locomotion.strafing() ? FreeLook.yaw() : FreeLook.yaw() + input.direction();
               boolean var8 = Locomotion.steersAfterSwitch(animatedAngle(var0), var7);
               steerSeconds = var8 ? 0.4F : 0.0F;
            }
         }

         if (var2 != 0.0F && input.moving()) {
            float var9 = FreeLook.yaw() + input.direction();
            moveX = (float)Math.cos(var9);
            moveY = (float)Math.sin(var9);
            moved = true;
            var1.set((moveX - moveY) * 0.5F, (moveX + moveY) * 0.5F);
            var1.setLength(var2);
         }
      }
   }

   public static void beforeMovement(IsoPlayer var0) {
      turned = false;
      if (owns(var0)) {
         long var1 = System.nanoTime();
         updateSeconds = updateNanos == 0L ? 0.0F : Math.min(0.1F, (float)(var1 - updateNanos) * 1.0E-9F);
         updateNanos = var1;
         inputDue = true;
         steerSeconds = Math.max(0.0F, steerSeconds - updateSeconds);
         if (!var0.isBlockMovement() && (moved || !gameTurns(var0))) {
            var0.getInputMoveVector(probe);
            boolean var3 = Math.abs(var0.getX() - lastX) + Math.abs(var0.getY() - lastY) < 0.001F;
            lastX = var0.getX();
            lastY = var0.getY();
            stillFrames = var3 ? stillFrames + 1 : 0;
            float var4 = FreeLook.held() ? FreeLook.yaw() : CrosshairAim.yaw(var0);
            float var5 = (float)Math.cos(var4);
            float var6 = (float)Math.sin(var4);
            boolean var7 = var0.getForwardDirectionX() * var5 + var0.getForwardDirectionY() * var6 > 0.9F;
            boolean var8 = var0.isStrafing();
            if (View.thirdPerson && attacking(var0)) {
               turn(var0, var4, true);
            } else if (moved && !var8 && !locomotion.backwards()) {
               walkTowards(var0, (float)Math.atan2(moveY, moveX));
            } else {
               if (orbits(var0)) {
                  return;
               }

               if (var7 || stillFrames >= 2 || var8) {
                  face(var0, var4);
               }
            }
         }
      }
   }

   private static void walkTowards(IsoPlayer var0, float var1) {
      if (!View.thirdPerson && locomotion.keyTurning()) {
         turn(var0, var1, false);
         locomotion.faced(bodyAngle, var1);
      } else {
         var0.setForwardDirection((float)Math.cos(var1), (float)Math.sin(var1));
      }
   }

   private static void face(IsoPlayer var0, float var1) {
      if (View.thirdPerson) {
         var0.setForwardDirection((float)Math.cos(var1), (float)Math.sin(var1));
      } else {
         turn(var0, var1, false);
      }
   }

   private static boolean gameTurns(IsoPlayer var0) {
      return !var0.getCharacterActions().isEmpty() || var0.isSitOnGround() || var0.isSittingOnFurniture();
   }

   private static boolean orbits(IsoPlayer var0) {
      return View.thirdPerson && !var0.isAiming() && !attacking(var0);
   }

   private static boolean attacking(IsoGameCharacter var0) {
      boolean var1 = var0 instanceof IsoPlayer var2 && var2.getInputMode() == CharacterInputMode.GAMEPAD && var2.isAiming() && var2.isAttackButtonDown();
      return MouseButtons.attackHeld() || var1 || var0.isPerformingAttackAnimation();
   }

   private static void turn(IsoPlayer var0, float var1, boolean var2) {
      float var3 = !var2 && !var0.isAiming() ? TURN_SMOOTHING.get() : 0.0F;
      bodyAngle = Locomotion.turn(animatedAngle(var0), var1, updateSeconds, var3);
      var0.setTargetAndCurrentDirection((float)Math.cos(bodyAngle), (float)Math.sin(bodyAngle));
      turned = true;
   }

   private static float animatedAngle(IsoPlayer var0) {
      return var0.hasAnimationPlayer() && var0.getAnimationPlayer().isReady() ? var0.getAnimationPlayer().getAngle() : var0.getDirectionAngleRadians();
   }

   public static void rootMotion(IsoPlayer var0, Vector2 var1, boolean var2) {
      if (owns(var0)) {
         if (var2) {
            pace(var0, var1.getLength());
         }

         if (!(steerSeconds <= 0.0F) && moved && !var0.isBlockMovement()) {
            float var3 = var1.getLength();
            if (var3 > 0.0F && WALK_STATES.contains(var0.getCurrentActionContextStateName())) {
               var1.set(moveX * var3, moveY * var3);
            }
         }
      }
   }

   private static void pace(IsoPlayer var0, float var1) {
      boolean var2 = moved && !var0.isBlockMovement() && !var0.isAiming();
      String var3 = var0.getCurrentActionContextStateName();
      boolean var4 = var2 && !locomotion.strafing() && "movement".equals(var3);
      boolean var5 = var2 && locomotion.strafing() && "strafe".equals(var3);
      strafePace.update(var0, var1, updateSeconds, var4, var5, BACKPEDAL_SPEED.get());
   }

   public static void afterMovement(IsoPlayer var0) {
      if (turned && owns(var0) && !var0.isBlockMovement()) {
         var0.setTargetAndCurrentDirection((float)Math.cos(bodyAngle), (float)Math.sin(bodyAngle));
      }
   }

   public static boolean strafing(IsoGameCharacter var0, boolean var1) {
      return !var1 && moved && locomotion.strafing() && owns(var0) && !var0.isRunning() && !var0.isSprinting()
         ? !"run".equals(var0.getCurrentActionContextStateName())
         : var1;
   }

   public static boolean running(IsoGameCharacter var0, boolean var1) {
      return var1 && (!moved || !locomotion.backwards() || !owns(var0));
   }

   public static float visionCone(IsoGameCharacter var0, float var1) {
      return owns(var0) ? 1.0F : var1;
   }

   public static void traceState(IsoGameCharacter var0) {
      if (var0 instanceof IsoPlayer var1) {
         String var2 = var1.getCurrentActionContextStateName();
         if (!var2.equals(tracedState)) {
            tracedState = var2;
            System.out
               .println(
                  "[Viewpoint] action state "
                     + var2
                     + " moving="
                     + var1.isPlayerMoving()
                     + " turning="
                     + var1.isTurning()
                     + " strafing="
                     + var1.isStrafing()
                     + " aiming="
                     + var1.isAiming()
                     + " blocked="
                     + var1.isBlockMovement()
               );
         }
      }
   }

   public static float lookAngle(IsoGameCharacter var0, float var1) {
      if (!owns(var0)) {
         return aimsByCrosshair(var0) && var0.isAiming() ? CrosshairAim.yaw(var0) : var1;
      } else {
         return View.thirdPerson && !var0.isAiming() && !attacking(var0) ? Look.wrap(var0.getDirectionAngleRadians() + HeadLook.yaw()) : CrosshairAim.yaw(var0);
      }
   }

   public static float handYaw(IsoGameCharacter var0) {
      float var1 = lookAngle(var0, var0.getDirectionAngleRadians());
      boolean var2 = View.thirdPerson && owns(var0) && !var0.isAiming() && !attacking(var0) && var0.hasActiveModel();
      return var2 ? HeadLook.drawnYaw(var0.getAnimationPlayer(), var1) : var1;
   }

   public static float handPitch(IsoGameCharacter var0) {
      if (aimsByCrosshair(var0) && var0.isAiming()) {
         return CrosshairAim.pitch(var0);
      } else {
         return View.thirdPerson ? HeadLook.pitch() : Look.pitch;
      }
   }

   public static int reticle(int var0, int var1, boolean var2) {
      if (reticleCentred && var0 == 0) {
         int var9 = var2 ? IsoCamera.getScreenWidth(0) : IsoCamera.getScreenHeight(0);
         float var10 = var2 ? View.ringX : -View.ringY;
         return Math.round(var9 * (1.0F + var10) / 2.0F);
      }

      IsoPlayer var3 = var0 == 0 ? IsoPlayer.players[0] : null;
      if (!owns(var3)) {
         return var1;
      }

      float var4 = CrosshairAim.yaw(var3);
      float var5 = var3.getAimOriginPosX() + (float)Math.cos(var4) * 8.0F;
      float var6 = var3.getAimOriginPosY() + (float)Math.sin(var4) * 8.0F;
      float var7 = var3.getAimOriginPosZ();
      float var8 = var2 ? IsoUtils.XToScreen(var5, var6, var7, 0) - IsoCamera.getOffX(var0) : IsoUtils.YToScreen(var5, var6, var7, 0) - IsoCamera.getOffY(var0);
      return Math.round(var8 / Core.getInstance().getZoom(var0));
   }

   public static boolean skipReticle(boolean var0) {
      IsoPlayer var1 = IsoPlayer.players[0];
      boolean var2 = var1 != null && var1.getVehicle() != null;
      reticleCentred = var0 && View.enabled && var1 != null && Look.looking() && !FreeCam.active && (!View.thirdPerson || !var2 || var1.isAiming());
      return View.enabled && !reticleCentred;
   }

   public static void reticleDone() {
      reticleCentred = false;
   }

   public static void aim(IsoPlayer var0) {
      if (aimsByCrosshair(var0)) {
         float var1 = CrosshairAim.yaw(var0);
         var0.setForwardDirection((float)Math.cos(var1), (float)Math.sin(var1));
         var0.setTargetVerticalAimAngle(owns(var0) ? 0.0F : (float)Math.toDegrees(CrosshairAim.pitch(var0)));
         traceShot(var0);
      }
   }

   private static void traceShot(IsoPlayer var0) {
      boolean var1 = var0.isPerformingAttackAnimation() && !attacking;
      attacking = var0.isPerformingAttackAnimation();
      long var2 = System.currentTimeMillis();
      BallisticsController var4 = var0.getBallisticsController();
      if (var1 && var4 != null && var2 - tracedAt >= 1000L) {
         tracedAt = var2;
         Vector3 var5 = var4.getMuzzleDirection();
         double var6 = Math.toDegrees(Math.atan2(var5.z * 2.4494896F, Math.hypot(var5.x, var5.y)));
         System.out
            .printf(
               "[Viewpoint] shot pitch: aim %.1f, arms %.1f, barrel %.1f%n", Math.toDegrees(CrosshairAim.pitch(var0)), Math.toDegrees(ArmsPitch.pitch()), var6
            );
      }
   }

   private Controls() {
   }

   static {
      MODEL.describe(
         "Turn to move: the body turns to where it walks, as with a controller, and backpedals facing the view when you walk backwards. Strafe: the body always faces the view, and walks sideways and backwards on the game's strafe steps, at the Backpedal speed."
      );
      DIRECTION_SMOOTHING.describe(
         "How long the walking direction takes to follow the keys, sweeping between them as a controller's stick does: 0 jumps at once. Turning back the other way is always at once."
      );
      TURN_SMOOTHING.describe("How long the body takes to turn to where it should face: 0 turns it at once. Aiming always turns it at once.");
      BACKPEDAL_SPEED.describe(
         "How fast the body walks facing the view off forward (backwards with Turn to move, any way but forwards with Strafe), as a share of walking forwards: its steps play faster to keep pace. Never slower than the game's own strafe; aiming keeps the game's."
      );
   }
}
