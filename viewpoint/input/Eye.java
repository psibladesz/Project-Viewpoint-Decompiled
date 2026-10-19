package viewpoint.input;

import java.util.Set;
import viewpoint.core.Frame;
import viewpoint.core.WallSide;
import viewpoint.platform.LiveSettings;
import zombie.characters.IsoGameCharacter;
import zombie.core.skinnedmodel.animation.AnimationPlayer;
import zombie.core.skinnedmodel.model.Model;
import zombie.iso.Vector3;

public final class Eye {
   private static final String CAMERA = "Controls/Camera";
   private static final LiveSettings.Number HEAD_MOVEMENT = LiveSettings.number(
      "camera.headMovement", "Head movement", "Controls/Camera", 0.0F, 1.0F, 0.01F, 0.66F
   );
   private static final Set<String> STEADY_STATES = Set.of(
      "idle",
      "movement",
      "strafe",
      "run",
      "sprint",
      "aim",
      "maskingleft",
      "maskingright",
      "turning",
      "turning180",
      "turningAim180",
      "turningIdle180",
      "turningMovement180"
   );
   private static final float EYE_ABOVE_HEAD_BONE = 0.1F;
   private static final float EYE_FALLBACK_HEIGHT = 1.38F;
   private static final float WALL_MARGIN = 0.15F;
   private static final Vector3 headBone = new Vector3();
   private static final float[] kept = new float[2];
   private static final EyeMotion eyeMotion = new EyeMotion();
   private static long eyeNanos;

   private Eye() {
   }

   public static void declare() {
   }

   public static boolean steady(IsoGameCharacter var0) {
      return STEADY_STATES.contains(var0.getCurrentActionContextStateName());
   }

   public static void place(IsoGameCharacter var0, Frame var1) {
      if (var0.getVehicle() != null) {
         put(var0, var1, eyeMotion.offsetX, eyeMotion.offsetZ);
      } else {
         float var2 = 0.0F;
         float var3 = 0.0F;
         float var4 = 1.38F;
         float var5 = 0.0F;
         if (var0.hasAnimationPlayer() && var0.getAnimationPlayer().isReady()) {
            AnimationPlayer var6 = var0.getAnimationPlayer();
            int var7 = var6.getSkinningBoneIndex("Bip01_Head", -1);
            if (var7 >= 0) {
               Model.boneToWorldCoords(var0, var7, headBone);
               float var8 = headBone.x - var0.getX();
               float var9 = headBone.y - var0.getY();
               float var10 = var6.getAngle();
               var2 = -var8;
               var3 = -var9;
               var4 = (headBone.z - var0.getZ()) * 2.4494896F + 0.1F;
               var5 = var8 * (float)Math.cos(var10) + var9 * (float)Math.sin(var10);
            }
         }

         boolean var11 = STEADY_STATES.contains(var0.getCurrentActionContextStateName());
         eyeMotion.update(eyeSeconds(), var2, var3, var4, var5, var11, HEAD_MOVEMENT.get());
         WallSide.keep(var0, var0.getX() - eyeMotion.offsetX, var0.getY() - eyeMotion.offsetZ, 0.15F, kept);
         put(var0, var1, var0.getX() - kept[0], var0.getY() - kept[1]);
      }
   }

   public static void atHead(IsoGameCharacter var0, Frame var1, float var2, float var3, float var4) {
      float var5 = var0.getX() - var1.camX;
      float var6 = var0.getY() - var1.camY;
      float var7 = (var0.getZ() - var1.camZ) * 2.4494896F;
      float var8 = SeatedEye.offset(var0.getVehicle().getScript().getFullName(), var1.number);
      float var9 = var3 - var7 + 0.1F + var8;
      eyeMotion.update(eyeSeconds(), var2 + var5, var4 + var6, var9, 0.0F, false, 1.0F);
      put(var0, var1, eyeMotion.offsetX, eyeMotion.offsetZ);
   }

   private static float eyeSeconds() {
      long var0 = System.nanoTime();
      float var2 = eyeNanos == 0L ? Float.MAX_VALUE : (float)(var0 - eyeNanos) * 1.0E-9F;
      eyeNanos = var0;
      return var2;
   }

   private static void put(IsoGameCharacter var0, Frame var1, float var2, float var3) {
      var1.eyeX = var2 - (var0.getX() - var1.camX);
      var1.eyeY = eyeMotion.height + (var0.getZ() - var1.camZ) * 2.4494896F;
      var1.eyeZ = var3 - (var0.getY() - var1.camY);
      var1.eyeLean = eyeMotion.lean;
   }

   public static float headBob() {
      return eyeMotion.bob;
   }

   static {
      HEAD_MOVEMENT.describe(
         "How much the view moves with the head as you stand, walk and turn: 0 keeps it steady (the height still follows a crouch), 1 follows the head's every step and sway."
      );
   }
}
