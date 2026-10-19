package viewpoint.input;

import java.util.Arrays;
import org.lwjgl.util.vector.Matrix4f;
import zombie.GameTime;
import zombie.characters.IsoPlayer;
import zombie.core.skinnedmodel.animation.AnimationPlayer;
import zombie.core.skinnedmodel.model.Model;
import zombie.core.skinnedmodel.model.SkinningBone;
import zombie.core.skinnedmodel.model.SkinningData;
import zombie.inventory.types.HandWeapon;
import zombie.iso.Vector3;

public final class ArmsPitch {
   private static final float STEP = 0.08F;
   private static final float MOST = (float) (Math.PI / 2);
   private static final String[] ROOTS = new String[]{"Bip01_L_Clavicle", "Bip01_R_Clavicle", "Bip01_Prop1", "Bip01_Prop2"};
   private static final String[] HELD = new String[]{"Bip01_L_Hand", "Bip01_R_Hand", "Bip01_Prop1", "Bip01_Prop2"};
   private static final String GUN_HAND = "Bip01_R_Hand";
   private static final float STILL = 1.0E-4F;
   private static final Matrix4f turn = new Matrix4f();
   private static final Matrix4f parentInverse = new Matrix4f();
   private static final Vector3 across = new Vector3();
   private static final Vector3 along = new Vector3();
   private static final float[] facing = new float[2];
   private static float pitch;
   private static float kickRise;
   private static float kickAcross;
   private static float kickRoll;
   private static float kickBack;
   private static float kickFlip;
   private static SkinningData bound;
   private static int leftClavicle = -1;
   private static int rightClavicle = -1;
   private static int gunHand = -1;
   private static int[] turned = new int[0];
   private static int[] held = new int[0];
   private static int[] tops = new int[0];
   private static int[] topParents = new int[0];

   public static void posed(AnimationPlayer var0) {
      IsoPlayer var1 = IsoPlayer.players[0];
      if (var1 != null && var0.getIsoGameCharacter() == var1) {
         if (!Controls.owns(var1)) {
            pitch = 0.0F;
         } else {
            float var2 = aimsFirearm(var1) ? clamp(CrosshairAim.pitch(var1), (float) (Math.PI / 2)) : 0.0F;
            GameTime var3 = GameTime.getInstance();
            pitch = pitch + clamp(var2 - pitch, 0.08F * var3.getMultiplierFromTimeDelta(var3.getTimeDelta()));
            boolean var4 = Math.abs(kickRise) + Math.abs(kickAcross) + Math.abs(kickRoll) + Math.abs(kickFlip) > 1.0E-4F || Math.abs(kickBack) > 1.0E-4F;
            if ((pitch != 0.0F || var4) && bind(var0)) {
               turn(var0, var1, var4);
            }
         }
      }
   }

   public static void kick(float var0, float var1, float var2, float var3, float var4) {
      kickRise = var0;
      kickAcross = var1;
      kickRoll = var2;
      kickBack = var3;
      kickFlip = var4;
   }

   static float pitch() {
      return pitch;
   }

   static void pivot(float var0, float var1, float var2, float var3, float var4, float var5, Matrix4f var6) {
      about(-var1, 0.0F, var0, var2, var3, var4, var5, var6);
   }

   static void about(float var0, float var1, float var2, float var3, float var4, float var5, float var6, Matrix4f var7) {
      float var8 = (float)Math.cos(var6);
      float var9 = (float)Math.sin(var6);
      float var10 = 1.0F - var8;
      var7.m00 = var8 + var10 * var0 * var0;
      var7.m01 = var10 * var0 * var1 - var9 * var2;
      var7.m02 = var10 * var0 * var2 + var9 * var1;
      var7.m10 = var10 * var1 * var0 + var9 * var2;
      var7.m11 = var8 + var10 * var1 * var1;
      var7.m12 = var10 * var1 * var2 - var9 * var0;
      var7.m20 = var10 * var2 * var0 - var9 * var1;
      var7.m21 = var10 * var2 * var1 + var9 * var0;
      var7.m22 = var8 + var10 * var2 * var2;
      var7.m30 = 0.0F;
      var7.m31 = 0.0F;
      var7.m32 = 0.0F;
      var7.m33 = 1.0F;
      var7.m03 = var3 - (var7.m00 * var3 + var7.m01 * var4 + var7.m02 * var5);
      var7.m13 = var4 - (var7.m10 * var3 + var7.m11 * var4 + var7.m12 * var5);
      var7.m23 = var5 - (var7.m20 * var3 + var7.m21 * var4 + var7.m22 * var5);
   }

   static void forward(float var0, float var1, float var2, float[] var3) {
      across.set(1.0F, 0.0F, 0.0F);
      Model.vectorToWorldCoords(0.0F, 0.0F, 0.0F, var2, across);
      along.set(0.0F, 0.0F, 1.0F);
      Model.vectorToWorldCoords(0.0F, 0.0F, 0.0F, var2, along);
      float var4 = var0 * across.x + var1 * across.y;
      float var5 = var0 * along.x + var1 * along.y;
      float var6 = (float)Math.hypot(var4, var5);
      var3[0] = var4 / var6;
      var3[1] = var5 / var6;
   }

   private static boolean aimsFirearm(IsoPlayer var0) {
      HandWeapon var1 = var0.getUseHandWeapon();
      return var0.isAiming() && var1 != null && var1.isAimedFirearm();
   }

   private static void turn(AnimationPlayer var0, IsoPlayer var1, boolean var2) {
      Matrix4f var3 = var0.getModelTransformAt(leftClavicle);
      Matrix4f var4 = var0.getModelTransformAt(rightClavicle);
      forward(var1.getForwardDirectionX(), var1.getForwardDirectionY(), var0.getRenderedAngle(), facing);
      float var5 = (var3.m03 + var4.m03) * 0.5F;
      float var6 = (var3.m13 + var4.m13) * 0.5F;
      float var7 = (var3.m23 + var4.m23) * 0.5F;
      if (pitch != 0.0F) {
         pivot(facing[0], facing[1], var5, var6, var7, pitch, turn);
         apply(var0, turned);

         for (int var8 = 0; var8 < tops.length; var8++) {
            Matrix4f.invert(var0.getModelTransformAt(topParents[var8]), parentInverse);
            var0.boneTransforms[tops[var8]].mul(var0.getModelTransformAt(tops[var8]), parentInverse);
         }
      }

      if (var2) {
         kick(var0, var5, var6, var7);
      }
   }

   private static void kick(AnimationPlayer var0, float var1, float var2, float var3) {
      float var4 = facing[0];
      float var5 = facing[1];
      float var6 = (float)Math.cos(pitch);
      float var7 = var4 * var6;
      float var8 = (float)Math.sin(pitch);
      float var9 = var5 * var6;
      pivot(var4, var5, var1, var2, var3, kickRise, turn);
      apply(var0, turned);
      about(0.0F, 1.0F, 0.0F, var1, var2, var3, kickAcross, turn);
      apply(var0, turned);
      turn.setIdentity();
      turn.m03 = -var7 * kickBack;
      turn.m13 = -var8 * kickBack;
      turn.m23 = -var9 * kickBack;
      apply(var0, turned);
      if (gunHand >= 0) {
         Matrix4f var10 = var0.getModelTransformAt(gunHand);
         float var11 = var10.m03;
         float var12 = var10.m13;
         float var13 = var10.m23;
         pivot(var4, var5, var11, var12, var13, kickFlip, turn);
         apply(var0, held);
         about(var7, var8, var9, var11, var12, var13, kickRoll, turn);
         apply(var0, held);
      }
   }

   private static void apply(AnimationPlayer var0, int[] var1) {
      for (int var5 : var1) {
         Matrix4f var6 = var0.getModelTransformAt(var5);
         Matrix4f.mul(var6, turn, var6);
      }
   }

   private static boolean bind(AnimationPlayer var0) {
      SkinningData var1 = var0.getSkinningData();
      if (var1 == bound) {
         return turned.length > 0;
      }

      bound = var1;
      turned = held = tops = topParents = new int[0];
      int[] var2 = indices(var0, ROOTS);
      leftClavicle = var2[0];
      rightClavicle = var2[1];
      gunHand = var0.getSkinningBoneIndex("Bip01_R_Hand", -1);
      if (var1 != null && leftClavicle >= 0 && rightClavicle >= 0) {
         turned = below(var1, var2);
         held = below(var1, indices(var0, HELD));
         int[] var3 = new int[var2.length];
         int[] var4 = new int[var2.length];
         int var5 = 0;

         for (int var9 : var2) {
            SkinningBone var10 = var9 < 0 ? null : var1.getBoneAt(var9).parent;
            if (var10 != null && !hangsFrom(var10, var2)) {
               var3[var5] = var9;
               var4[var5++] = var10.index;
            }
         }

         tops = Arrays.copyOf(var3, var5);
         topParents = Arrays.copyOf(var4, var5);
         return turned.length > 0;
      } else {
         return false;
      }
   }

   private static int[] indices(AnimationPlayer var0, String[] var1) {
      int[] var2 = new int[var1.length];

      for (int var3 = 0; var3 < var1.length; var3++) {
         var2[var3] = var0.getSkinningBoneIndex(var1[var3], -1);
      }

      return var2;
   }

   private static int[] below(SkinningData var0, int[] var1) {
      int[] var2 = new int[var0.numBones()];
      int var3 = 0;

      for (int var4 = 0; var4 < var2.length; var4++) {
         if (hangsFrom(var0.getBoneAt(var4), var1)) {
            var2[var3++] = var4;
         }
      }

      return Arrays.copyOf(var2, var3);
   }

   private static boolean hangsFrom(SkinningBone var0, int[] var1) {
      for (SkinningBone var2 = var0; var2 != null; var2 = var2.parent) {
         for (int var6 : var1) {
            if (var2.index == var6) {
               return true;
            }
         }
      }

      return false;
   }

   private static float clamp(float var0, float var1) {
      return Math.max(-var1, Math.min(var1, var0));
   }

   private ArmsPitch() {
   }
}
