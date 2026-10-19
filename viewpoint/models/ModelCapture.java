package viewpoint.models;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import org.joml.Matrix4f;
import viewpoint.core.Frame;
import viewpoint.light.OwnedLight;
import viewpoint.render.FarRelief;
import viewpoint.render.ModelDraws;
import zombie.characters.IsoGameCharacter;
import zombie.core.skinnedmodel.ModelManager.ModelSlot;
import zombie.core.skinnedmodel.animation.AnimationPlayer;
import zombie.core.skinnedmodel.model.Model;
import zombie.core.skinnedmodel.model.ModelInstance;
import zombie.core.skinnedmodel.model.ModelInstanceRenderData;
import zombie.core.skinnedmodel.model.ModelMesh;
import zombie.core.skinnedmodel.model.ModelSlotRenderData;
import zombie.core.skinnedmodel.model.SkinningData;
import zombie.core.textures.ColorInfo;
import zombie.core.textures.Texture;
import zombie.iso.IsoGridSquare;
import zombie.scripting.objects.ModelScript;

final class ModelCapture {
   private static final String[] HEAD_BONES = new String[]{"Bip01_Head", "Bip01_HeadNub"};
   private static final float MIDDLE = 0.9F;
   private static final float RADIUS = 1.3F;
   private static final Matrix4f place = new Matrix4f();
   private static final Matrix4f model = new Matrix4f();
   private static final Matrix4f attachment = new Matrix4f();
   private static final Field OUTLINED = field("characterOutline");
   private static final Field COLOUR = field("outlineColor");
   private static final Field BEHIND = field("outlineBehindPlayer");

   static boolean capture(
      Frame var0,
      ModelSlotRenderData var1,
      IsoGameCharacter var2,
      boolean var3,
      boolean var4,
      ModelCapture.Pose var5,
      float var6,
      float var7,
      float var8,
      float var9
   ) {
      if (var1.modelData.isEmpty()) {
         return false;
      }

      for (int var10 = 0; var10 < var1.modelData.size(); var10++) {
         if (!drawable((ModelInstanceRenderData)var1.modelData.get(var10))) {
            return false;
         }
      }

      ModelDraws var23 = var0.scene.models;
      placed(var0, var1, var2, var6, var7, var8, var9);
      boolean var11 = var3 && !var1.inVehicle && var5 != ModelCapture.Pose.LAST && legs(var0, var2);
      float var12 = place.m30();
      float var13 = place.m31();
      float var14 = place.m32();
      var23.slot(var1, (float)Math.sqrt(var12 * var12 + var14 * var14));
      IsoGridSquare var15 = var2.getCurrentSquare();
      int var16 = var15 == null ? -1 : OwnedLight.modelLightAt(var15.x, var15.y, var15.z);
      boolean var17 = var15 != null && var15.isOutside() && !var15.haveRoof;
      float var18 = var15 == null ? 0.0F : OwnedLight.modelSky(var15.x, var15.y, var15.z, var17);
      int var19 = var23.count();

      for (int var20 = 0; var20 < var1.modelData.size(); var20++) {
         ModelInstanceRenderData var21 = (ModelInstanceRenderData)var1.modelData.get(var20);
         int var22 = add(var23, var21, var21.model.tex, var2, var4, var5, var11);
         var23.stand(var22, !var1.inVehicle && !var11 ? 1 : 2, var12, var13, var14);
         ModelMotion.follow(var23, var22, var21.modelInstance, var21.model.isStatic ? 0 : var21.matrixPalette.limit() / 16, var5 == ModelCapture.Pose.LAST);
         var23.material(var22, var21.modelInstance.tintR, var21.modelInstance.tintG, var21.modelInstance.tintB, var21.hue);
         light(var23, var22, var1, var16, var18);
         var23.bounds(var22, var12, var13 + 0.9F, var14, 1.3F * Math.max(1.0F, var1.finalScale));
      }

      outline(var23, var1, var19);
      return true;
   }

   private static void placed(Frame var0, ModelSlotRenderData var1, IsoGameCharacter var2, float var3, float var4, float var5, float var6) {
      if (var1.inVehicle) {
         Characters.seatMatrix(place, var1, var0.camX, var0.camY, var0.camZ).scale(1.5F);
         VehicleTilt.onRelief(place, var0, var2.getVehicle(), var1.x, var1.y, var1.z);
      } else {
         place.translation(-(var3 - var0.camX), (var5 - var0.camZ) * 2.4494896F, -(var4 - var0.camY)).scale(-1.5F, 1.5F, 1.5F).rotateY(var6 + (float) Math.PI);
      }

      place.scale(var1.finalScale);
   }

   private static void light(ModelDraws var0, int var1, ModelSlotRenderData var2, int var3, float var4) {
      var0.light(
         var1,
         var3 < 0 ? Math.min(1.0F, var2.ambientR) : (var3 & 0xFF) / 255.0F,
         var3 < 0 ? Math.min(1.0F, var2.ambientG) : (var3 >> 8 & 0xFF) / 255.0F,
         var3 < 0 ? Math.min(1.0F, var2.ambientB) : (var3 >> 16 & 0xFF) / 255.0F,
         var4
      );
   }

   private static void outline(ModelDraws var0, ModelSlotRenderData var1, int var2) {
      if (OUTLINED != null && COLOUR != null && BEHIND != null) {
         try {
            if (!OUTLINED.getBoolean(var1)) {
               return;
            }

            ColorInfo var3 = (ColorInfo)COLOUR.get(var1);
            boolean var4 = BEHIND.getBoolean(var1);

            for (int var5 = var2; var5 < var0.count(); var5++) {
               var0.outline(var5, var3.r, var3.g, var3.b, var3.a, var4);
            }
         } catch (ReflectiveOperationException | RuntimeException var6) {
         }
      }
   }

   private static Field field(String var0) {
      try {
         Field var1 = ModelSlotRenderData.class.getDeclaredField(var0);
         var1.setAccessible(true);
         return var1;
      } catch (ReflectiveOperationException | RuntimeException var2) {
         System.out.println("[Viewpoint] models: owned characters are not outlined (" + var0 + "): " + var2);
         return null;
      }
   }

   static boolean loaded(Model var0) {
      ModelMesh var1 = var0 == null ? null : var0.mesh;
      return var1 != null && var1.vb != null && var1.isReady() && !ModelDraws.refused(var1);
   }

   private static boolean drawable(ModelInstanceRenderData var0) {
      return !loaded(var0.model) ? false : var0.model.isStatic || var0.matrixPalette != null && var0.matrixPalette.limit() >= 16;
   }

   private static boolean legs(Frame var0, IsoGameCharacter var1) {
      if (Float.isNaN(LegReach.solve(var1.getAnimationPlayer(), place, var0))) {
         return false;
      }

      FarRelief.Lift var2 = var0.scene.relief;
      place.m31(place.m31() + var2.scene(var0.camX - place.m30(), var0.camY - place.m32()));
      return true;
   }

   private static int add(
      ModelDraws var0, ModelInstanceRenderData var1, Texture var2, IsoGameCharacter var3, boolean var4, ModelCapture.Pose var5, boolean var6
   ) {
      ModelScript var7 = var1.modelInstance == null ? null : var1.modelInstance.modelScript;
      int var8 = var7 != null && var7.cullFace != -1 ? var7.cullFace : 1028;
      if (var1.model.isStatic) {
         String var15 = var1.modelInstance == null ? null : var1.modelInstance.parentBoneName;
         model.set(place);
         if (var6) {
            model.mul(LegReach.carrying(var3.getAnimationPlayer(), var15));
         }

         model.mul(attachment.set(var1.xfrm).transpose());
         boolean var16 = var4 && var15 != null && var15.contains("Head");
         return var0.add(var1.model.mesh, var1.modelInstance, var2, model, -1, -1, var8, var16 ? 2 : 3);
      } else {
         FloatBuffer var9 = var1.matrixPalette;
         int var10 = var9.limit() / 16;
         int var11 = palette(var0, var1, var5, var10);
         int var12 = var11;
         if (var4 && var1.model.tag instanceof SkinningData var13 && var13.boneIndices != null) {
            var12 = var0.palette(var9, var10);
            collapse(var0, var12, var10, var13, var3.getAnimationPlayer());
         }

         if (var6 && var1.model.tag instanceof SkinningData var17) {
            LegReach.apply(var0, var11, var10, var17);
            if (var12 != var11) {
               LegReach.apply(var0, var12, var10, var17);
            }
         }

         return var0.add(var1.model.mesh, var1.modelInstance, var2, model.set(place), var12, var11, var8, 3);
      }
   }

   private static int palette(ModelDraws var0, ModelInstanceRenderData var1, ModelCapture.Pose var2, int var3) {
      if (var2 == ModelCapture.Pose.PLAYER) {
         ModelInstance var5 = var1.modelInstance;
         return var0.palette(var5.animPlayer.getSkinTransforms((SkinningData)var5.model.tag));
      } else {
         int var4 = var2 == ModelCapture.Pose.LAST ? ModelMotion.lastPose(var0, var1.modelInstance, var3) : -1;
         return var4 >= 0 ? var4 : var0.palette(var1.matrixPalette, var3);
      }
   }

   static boolean posable(ModelSlotRenderData var0, ModelSlot var1) {
      if (!var0.inVehicle && OUTLINED != null) {
         try {
            if (OUTLINED.getBoolean(var0)) {
               return false;
            }
         } catch (ReflectiveOperationException var3) {
            return false;
         }

         int var2 = 0;
         if (ready(var1.model)) {
            var2 = same(var0, var2, var1.model) ? 1 : -1;
         }

         var2 = var2 < 0 ? -1 : sameInstances(var0, var2, var1.sub);
         return var2 == var0.modelData.size();
      } else {
         return false;
      }
   }

   private static int sameInstances(ModelSlotRenderData var0, int var1, ArrayList<ModelInstance> var2) {
      for (int var3 = 0; var3 < var2.size() && var1 >= 0; var3++) {
         ModelInstance var4 = (ModelInstance)var2.get(var3);
         if (ready(var4)) {
            var1 = same(var0, var1, var4) ? sameInstances(var0, var1 + 1, var4.sub) : -1;
         }
      }

      return var1;
   }

   private static boolean ready(ModelInstance var0) {
      return var0.model.isReady() && (var0.animPlayer == null || var0.animPlayer.isReady());
   }

   private static boolean same(ModelSlotRenderData var0, int var1, ModelInstance var2) {
      if (var1 >= var0.modelData.size()) {
         return false;
      }

      ModelInstanceRenderData var3 = (ModelInstanceRenderData)var0.modelData.get(var1);
      return var3.modelInstance == var2
         && var3.model == var2.model
         && !var2.model.isStatic
         && var2.animPlayer != null
         && var3.hue == var2.hue
         && !var2.hasTextureCreator()
         && var3.matrixPalette != null
         && var2.animPlayer.getSkinTransforms((SkinningData)var2.model.tag).length * 16 == var3.matrixPalette.limit();
   }

   private static void collapse(ModelDraws var0, int var1, int var2, SkinningData var3, AnimationPlayer var4) {
      int var5 = var4 == null ? -1 : var4.getSkinningBoneIndex(HEAD_BONES[0], -1);
      if (var5 >= 0) {
         org.lwjgl.util.vector.Matrix4f var6 = var4.getModelTransformAt(var5);

         for (String var10 : HEAD_BONES) {
            Integer var11 = (Integer)var3.boneIndices.get(var10);
            if (var11 != null && var11 < var2) {
               var0.collapse(var1 + var11, var6.m03, var6.m13, var6.m23);
            }
         }
      }
   }

   private ModelCapture() {
   }

   enum Pose {
      SNAPSHOT,
      LAST,
      PLAYER;
   }
}
