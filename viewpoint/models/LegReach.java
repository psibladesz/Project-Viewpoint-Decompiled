package viewpoint.models;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import viewpoint.core.Frame;
import viewpoint.render.FarRelief;
import viewpoint.render.ModelDraws;
import zombie.core.skinnedmodel.animation.AnimationPlayer;
import zombie.core.skinnedmodel.model.SkinningData;

final class LegReach {
   private static final String[][] LEGS = new String[][]{{"Bip01_L_Thigh", "Bip01_L_Calf", "Bip01_L_Foot"}, {"Bip01_R_Thigh", "Bip01_R_Calf", "Bip01_R_Foot"}};
   private static final float MOST = 0.5F;
   private static final float SLACK = 0.002F;
   private static final int THIGH = 0;
   private static final int CALF = 1;
   private static final int FOOT = 2;
   private static final Matrix4f[][] legs = new Matrix4f[][]{
      {new Matrix4f(), new Matrix4f(), new Matrix4f()}, {new Matrix4f(), new Matrix4f(), new Matrix4f()}
   };
   private static final Matrix4f body = new Matrix4f();
   private static final Matrix4f turn = new Matrix4f();
   private static final Vector3f hip = new Vector3f();
   private static final Vector3f knee = new Vector3f();
   private static final Vector3f ankle = new Vector3f();
   private static final Vector3f target = new Vector3f();
   private static final Vector3f placed = new Vector3f();
   private static final Vector3f bent = new Vector3f();
   private static final Vector3f reach = new Vector3f();
   private static final Quaternionf rotation = new Quaternionf();
   private static final float[] lift = new float[2];

   private LegReach() {
   }

   static float solve(AnimationPlayer var0, Matrix4f var1, Frame var2) {
      FarRelief.Lift var3 = var2.scene.relief;
      if (var3 != null && var0 != null) {
         float var4 = (float)Math.sqrt(var1.m10() * var1.m10() + var1.m11() * var1.m11() + var1.m12() * var1.m12());
         float var5 = var2.camX - var1.m30();
         float var6 = var2.camY - var1.m32();
         float var7 = var3.scene(var5, var6);

         for (int var8 = 0; var8 < 2; var8++) {
            int var9 = var0.getSkinningBoneIndex(LEGS[var8][2], -1);
            if (var9 < 0 || var0.getSkinningBoneIndex(LEGS[var8][0], -1) < 0) {
               return Float.NaN;
            }

            position(var0, var9, ankle);
            var1.transformPosition(ankle, placed);
            float var10 = (var3.scene(var2.camX - placed.x, var2.camY - placed.z) - var7) / var4;
            lift[var8] = Math.max(-0.5F, Math.min(0.5F, var10));
         }

         float var11 = Math.max(0.0F, -Math.min(lift[0], lift[1]));
         body.translation(0.0F, -var11, 0.0F);

         for (int var12 = 0; var12 < 2; var12++) {
            leg(var0, var12, var11);
         }

         return var11 * var4;
      } else {
         return Float.NaN;
      }
   }

   private static void leg(AnimationPlayer var0, int var1, float var2) {
      position(var0, var0.getSkinningBoneIndex(LEGS[var1][0], -1), hip);
      position(var0, var0.getSkinningBoneIndex(LEGS[var1][1], -1), knee);
      position(var0, var0.getSkinningBoneIndex(LEGS[var1][2], -1), ankle);
      float var3 = hip.distance(knee);
      float var4 = knee.distance(ankle);
      target.set(ankle).add(0.0F, lift[var1], 0.0F);
      hip.y -= var2;
      knee.y -= var2;
      Vector3f var5 = knee(hip, target, var3, var4, knee, bent);
      about(hip, rotation.rotationTo(new Vector3f(knee).sub(hip), new Vector3f(var5).sub(hip)));
      Matrix4f var6 = legs[var1][0].set(turn).mul(body);
      Vector3f var7 = var6.transformPosition(new Vector3f(ankle));
      about(var5, rotation.rotationTo(var7.sub(var5), new Vector3f(reach).sub(var5)));
      legs[var1][1].set(turn).mul(var6);
      legs[var1][2].translation(reach.x - ankle.x, reach.y - ankle.y, reach.z - ankle.z);
   }

   static Vector3f knee(Vector3f var0, Vector3f var1, float var2, float var3, Vector3f var4, Vector3f var5) {
      Vector3f var6 = new Vector3f(var1).sub(var0);
      float var7 = var6.length();
      var6 = var7 > 1.0E-6F ? var6.div(var7) : new Vector3f(0.0F, -1.0F, 0.0F);
      float var8 = Math.max(Math.abs(var2 - var3) + 0.002F, Math.min(var2 + var3 - 0.002F, var7));
      float var9 = (var2 * var2 - var3 * var3 + var8 * var8) / (2.0F * var8);
      float var10 = (float)Math.sqrt(Math.max(0.0F, var2 * var2 - var9 * var9));
      Vector3f var11 = new Vector3f(var4).sub(var0);
      var11.sub(new Vector3f(var6).mul(var11.dot(var6)));
      if (var11.lengthSquared() < 1.0E-10F) {
         var6.cross(1.0F, 0.0F, 0.0F, var11);
         if (var11.lengthSquared() < 1.0E-10F) {
            var6.cross(0.0F, 0.0F, 1.0F, var11);
         }
      }

      var11.normalize();
      reach.set(var6).mul(var8).add(var0);
      return var5.set(var6).mul(var9).add(var11.mul(var10)).add(var0);
   }

   private static void about(Vector3f var0, Quaternionf var1) {
      turn.translation(var0).rotate(var1).translate(-var0.x, -var0.y, -var0.z);
   }

   private static void position(AnimationPlayer var0, int var1, Vector3f var2) {
      org.lwjgl.util.vector.Matrix4f var3 = var0.getModelTransformAt(var1);
      var2.set(var3.m03, var3.m13, var3.m23);
   }

   static void apply(ModelDraws var0, int var1, int var2, SkinningData var3) {
      if (var3.boneIndices != null && var3.skeletonHierarchy != null) {
         int[][] var4 = legBones(var3);

         for (int var5 = 0; var5 < var2; var5++) {
            var0.move(var1 + var5, carrier(var5, var4, var3));
         }
      }
   }

   static Matrix4f carrying(AnimationPlayer var0, String var1) {
      SkinningData var2 = var0 == null ? null : var0.getSkinningData();
      Integer var3 = var1 != null && var2 != null && var2.boneIndices != null ? (Integer)var2.boneIndices.get(var1) : null;
      return var3 != null && var2.skeletonHierarchy != null ? carrier(var3, legBones(var2), var2) : body;
   }

   private static int[][] legBones(SkinningData var0) {
      int[][] var1 = new int[2][3];

      for (int var2 = 0; var2 < 2; var2++) {
         for (int var3 = 0; var3 < 3; var3++) {
            Integer var4 = (Integer)var0.boneIndices.get(LEGS[var2][var3]);
            var1[var2][var3] = var4 == null ? -1 : var4;
         }
      }

      return var1;
   }

   private static Matrix4f carrier(int var0, int[][] var1, SkinningData var2) {
      int var3 = var0;

      for (int var4 = 0; var3 >= 0 && var4 < 64; var4++) {
         for (int var5 = 0; var5 < 2; var5++) {
            for (int var6 = 2; var6 >= 0; var6--) {
               if (var1[var5][var6] == var3) {
                  return legs[var5][var6];
               }
            }
         }

         var3 = var3 < var2.skeletonHierarchy.size() ? (Integer)var2.skeletonHierarchy.get(var3) : -1;
      }

      return body;
   }
}
