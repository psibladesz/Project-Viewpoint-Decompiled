package viewpoint.input;

import java.util.Arrays;
import org.lwjgl.util.vector.Matrix4f;
import viewpoint.core.View;
import viewpoint.platform.LiveSettings;
import zombie.characters.IsoPlayer;
import zombie.core.skinnedmodel.animation.AnimationPlayer;
import zombie.core.skinnedmodel.model.Model;
import zombie.core.skinnedmodel.model.SkinningBone;
import zombie.core.skinnedmodel.model.SkinningData;
import zombie.iso.Vector3;

public final class HeadLook {
   static final float MOST = (float)Math.toRadians(70.0);
   static final float DOWN = (float)Math.toRadians(30.0);
   static final float UP = (float)Math.toRadians(25.0);
   static final float KNEE = 0.6F;
   static final float FADE = (float)Math.toRadians(90.0);
   static final float BEHIND = (float)Math.toRadians(160.0);
   static final float NECK = 0.35F;
   static final float TAU = 0.15F;
   static final float WEIGHT_TAU = 0.3F;
   static final float TURNING = (float)Math.toRadians(45.0);
   private static final float OFF = 0.001F;
   static final LiveSettings.Toggle ON = LiveSettings.toggle("camera.headLook", "Character looks where you look", "Controls/Third person", true);
   private static final String NECK_BONE = "Bip01_Neck";
   private static final String HEAD_BONE = "Bip01_Head";
   private static final Matrix4f turn = new Matrix4f();
   private static final float[] facing = new float[2];
   private static final float[] looking = new float[2];
   private static final float[] values = new float[5];
   private static final Vector3 world = new Vector3();
   private static float gaze;
   private static float lookPitch;
   private static float weight;
   private static float yaw;
   private static float pitch;
   private static long lastNanos;
   private static SkinningData bound;
   private static int neck = -1;
   private static int head = -1;
   private static int[] neckBones = new int[0];
   private static int[] headBones = new int[0];

   public static void declare() {
      ON.get();
   }

   public static void posed(AnimationPlayer var0) {
      IsoPlayer var1 = IsoPlayer.players[0];
      if (var1 != null && var0.getIsoGameCharacter() == var1) {
         long var2 = System.nanoTime();
         float var4 = lastNanos == 0L ? 0.0F : Math.min(0.1F, (float)(var2 - lastNanos) * 1.0E-9F);
         lastNanos = var2;
         float var5 = var0.getAngle();
         float var6 = Ease.between(var5, Look.yaw);
         boolean var7 = var1.isTurningAround() || Math.abs(Ease.between(var5, var0.getTargetAngle())) > TURNING;
         boolean var8 = ON.get() && View.thirdPerson && Controls.owns(var1) && !var1.isAiming() && Eye.steady(var1) && !var7;
         weight = Ease.toward(weight, var8 ? 1.0F : 0.0F, var4, 0.3F);
         gaze = weight < 0.001F ? Look.yaw : Ease.angleToward(gaze, Look.yaw, var4, 0.15F);
         lookPitch = Ease.toward(lookPitch, upDown(var6, Look.pitch), var4, 0.15F);
         float var9 = across(Ease.between(var5, gaze));
         yaw = weight * var9;
         pitch = weight * lookPitch;
         boolean var10 = bind(var0);
         boolean var11 = var10 && HeadLookDebug.tracing();
         if (var11) {
            HeadLookDebug.before(var0, neck, head);
         }

         if (weight >= 0.001F && var10) {
            turn(var0, var5 + var9);
         }

         if (var11) {
            values[0] = var5;
            values[1] = gaze;
            values[2] = var6;
            values[3] = var9;
            values[4] = weight;
            HeadLookDebug.after(var0, var1, neck, head, values, var8, var7);
         }
      }
   }

   static float across(float var0) {
      return Math.copySign(soft(Math.abs(var0), MOST), var0) * ahead(Math.abs(var0));
   }

   static float upDown(float var0, float var1) {
      float var2 = var1 >= 0.0F ? UP : DOWN;
      return Math.copySign(soft(Math.abs(var1), var2), var1) * ahead(Math.abs(var0));
   }

   static float soft(float var0, float var1) {
      float var2 = var1 * 0.6F;
      float var3 = var1 - var2;
      return var0 <= var2 ? var0 : var2 + var3 * (1.0F - (float)Math.exp(-(var0 - var2) / var3));
   }

   static float ahead(float var0) {
      float var1 = Math.max(0.0F, Math.min(1.0F, (BEHIND - var0) / (BEHIND - FADE)));
      return var1 * var1 * (3.0F - 2.0F * var1);
   }

   public static float drawnYaw(AnimationPlayer var0, float var1) {
      return var0 != null && bind(var0) ? boneFacing(var0.getModelTransformAt(head), var0.getRenderedAngle(), var1) : var1;
   }

   public static float yaw() {
      return yaw;
   }

   public static float pitch() {
      return pitch;
   }

   private static void turn(AnimationPlayer var0, float var1) {
      float var2 = var0.getRenderedAngle();
      float var3 = boneFacing(var0.getModelTransformAt(head), var2, var1);
      float var4 = weight * Ease.between(var3, var1);
      float var5 = var3 + var4 * 0.35F;
      float var6 = var3 + var4;
      ArmsPitch.forward((float)Math.cos(var3), (float)Math.sin(var3), var2, facing);
      ArmsPitch.forward((float)Math.cos(var5), (float)Math.sin(var5), var2, looking);
      about(var0, neck, neckBones, facing, looking, pitch * 0.35F);
      float[] var7 = new float[]{looking[0], looking[1]};
      ArmsPitch.forward((float)Math.cos(var6), (float)Math.sin(var6), var2, looking);
      about(var0, head, headBones, var7, looking, pitch * 0.65F);
   }

   private static float boneFacing(Matrix4f var0, float var1, float var2) {
      float var3 = var0.m02;
      float var4 = var0.m22;
      if (var3 * var3 + var4 * var4 < 1.0E-6F) {
         return var2;
      }

      world.set(var3, 0.0F, var4);
      Model.vectorToWorldCoords(0.0F, 0.0F, 0.0F, var1, world);
      return (float)Math.atan2(world.y, world.x);
   }

   private static void about(AnimationPlayer var0, int var1, int[] var2, float[] var3, float[] var4, float var5) {
      Matrix4f var6 = var0.getModelTransformAt(var1);
      float var7 = var6.m03;
      float var8 = var6.m13;
      float var9 = var6.m23;
      float var10 = (float)(-Math.atan2(var3[0] * var4[1] - var3[1] * var4[0], var3[0] * var4[0] + var3[1] * var4[1]));
      upright(var7, var8, var9, var10, turn);
      apply(var0, var2);
      ArmsPitch.pivot(var4[0], var4[1], var7, var8, var9, var5, turn);
      apply(var0, var2);
   }

   private static void apply(AnimationPlayer var0, int[] var1) {
      for (int var5 : var1) {
         Matrix4f var6 = var0.getModelTransformAt(var5);
         Matrix4f.mul(var6, turn, var6);
      }
   }

   static void upright(float var0, float var1, float var2, float var3, Matrix4f var4) {
      float var5 = (float)Math.cos(var3);
      float var6 = (float)Math.sin(var3);
      var4.setIdentity();
      var4.m00 = var5;
      var4.m02 = var6;
      var4.m20 = -var6;
      var4.m22 = var5;
      var4.m03 = var0 - (var5 * var0 + var6 * var2);
      var4.m13 = 0.0F;
      var4.m23 = var2 - (-var6 * var0 + var5 * var2);
   }

   private static boolean bind(AnimationPlayer var0) {
      SkinningData var1 = var0.getSkinningData();
      if (var1 != bound) {
         bound = var1;
         neck = var0.getSkinningBoneIndex("Bip01_Neck", -1);
         head = var0.getSkinningBoneIndex("Bip01_Head", -1);
         if (var1 != null && neck >= 0 && head >= 0) {
            neckBones = below(var1, neck);
            headBones = below(var1, head);
            return true;
         } else {
            return false;
         }
      } else {
         return neck >= 0 && head >= 0;
      }
   }

   private static int[] below(SkinningData var0, int var1) {
      int[] var2 = new int[var0.numBones()];
      int var3 = 0;

      for (int var4 = 0; var4 < var2.length; var4++) {
         for (SkinningBone var5 = var0.getBoneAt(var4); var5 != null; var5 = var5.parent) {
            if (var5.index == var1) {
               var2[var3++] = var4;
               break;
            }
         }
      }

      return Arrays.copyOf(var2, var3);
   }

   private HeadLook() {
   }

   static {
      ON.describe("In third person the character's head turns to where you look, standing and walking.");
   }
}
