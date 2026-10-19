package viewpoint.models;

import org.joml.Matrix4f;
import viewpoint.core.Frame;
import viewpoint.light.OwnedLight;
import viewpoint.render.ModelDraws;
import zombie.core.skinnedmodel.model.ModelInstance;
import zombie.core.skinnedmodel.model.ModelInstanceRenderData;
import zombie.core.skinnedmodel.model.ModelSlotRenderData;
import zombie.core.skinnedmodel.model.VehicleModelInstance;
import zombie.core.skinnedmodel.shader.Shader;
import zombie.core.textures.Texture;
import zombie.iso.IsoGridSquare;
import zombie.vehicles.BaseVehicle;

final class VehicleCapture {
   private static final Matrix4f place = new Matrix4f();
   private static final Matrix4f model = new Matrix4f();
   private static final Texture[] textures = new Texture[7];
   private static final float[][] switches = new float[12][];

   static boolean capture(Frame var0, ModelSlotRenderData var1, BaseVehicle var2) {
      if (var1.modelData.isEmpty()) {
         return false;
      }

      for (int var3 = 0; var3 < var1.modelData.size(); var3++) {
         if (!drawable((ModelInstanceRenderData)var1.modelData.get(var3))) {
            return false;
         }
      }

      ModelDraws var20 = var0.scene.models;
      placed(var0, var1, var2);
      IsoGridSquare var4 = var2.getCurrentSquare();
      int var5 = var4 == null ? -1 : OwnedLight.modelLightAt(var4.x, var4.y, var4.z);
      boolean var6 = var4 != null && var4.isOutside() && !var4.haveRoof;
      float var7 = var4 == null ? 0.0F : OwnedLight.modelSky(var4.x, var4.y, var4.z, var6);
      VehicleModelInstance var8 = null;
      int var9 = -1;
      int var10 = -1;

      for (int var11 = 0; var11 < var1.modelData.size(); var11++) {
         ModelInstanceRenderData var12 = (ModelInstanceRenderData)var1.modelData.get(var11);
         ModelInstance var13 = var12.modelInstance;
         Shader var14 = var12.model.effect;
         int var15 = var13.modelScript != null && var13.modelScript.invertX ? 1029 : 1028;
         model.set(place).mul(var12.xfrm);
         int var16 = var12.model.isStatic ? -1 : var20.palette(var12.matrixPalette, var12.matrixPalette.limit() / 16);
         VehicleModelInstance var17 = var14.isVehicleShader() ? body(var13) : null;
         Texture var18 = var17 != null ? var17.tex : (var13.tex != null ? var13.tex : var12.model.tex);
         int var19 = var20.add(var12.model.mesh, null, var18, model, var16, var16, var15, 3);
         var20.stand(var19, 2, 0.0F, 0.0F, 0.0F);
         ModelMotion.follow(var20, var19, var13, var12.model.isStatic ? 0 : var12.matrixPalette.limit() / 16);
         if (var17 != null) {
            if (var17 != var8 || kind(var14) != var10) {
               var8 = var17;
               var10 = kind(var14);
               var9 = material(var20, var17, var10);
            }

            var20.vehicle(var19, var9);
         }

         var20.material(var19, var13.tintR, var13.tintG, var13.tintB, 0.0F);
         light(var20, var19, var1, var5, var7);
         var20.bounds(var19, model, var12.model.mesh.minXyz, var12.model.mesh.maxXyz, var12.model.isStatic ? 1.0F : 2.0F);
      }

      return true;
   }

   private static void placed(Frame var0, ModelSlotRenderData var1, BaseVehicle var2) {
      place.translation(-(var1.x - var0.camX), (var1.z - var0.camZ) * 2.4494896F + BaseVehicle.centerOfMassMagic, -(var1.y - var0.camY))
         .scale(-1.0F, 1.0F, 1.0F)
         .rotateY((float) Math.PI)
         .translate(0.0F, var1.centerOfMassY, 0.0F);
      VehicleTilt.onRelief(place, var0, var2, var1.x, var1.y, var1.z);
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

   private static boolean drawable(ModelInstanceRenderData var0) {
      if (!ModelCapture.loaded(var0.model) || var0.model.effect == null) {
         return false;
      } else {
         return var0.model.effect.isVehicleShader() && body(var0.modelInstance) == null
            ? false
            : var0.model.isStatic || var0.matrixPalette != null && var0.matrixPalette.limit() >= 16;
      }
   }

   private static VehicleModelInstance body(ModelInstance var0) {
      if (var0 instanceof VehicleModelInstance var3) {
         return var3;
      } else {
         return var0.parent instanceof VehicleModelInstance var1 ? var1 : null;
      }
   }

   private static int kind(Shader var0) {
      String var1 = var0.getName();
      if (var1.contains("norandom")) {
         return 2;
      } else {
         return var1.contains("multiuv") ? 1 : 0;
      }
   }

   private static int material(ModelDraws var0, VehicleModelInstance var1, int var2) {
      textures[0] = var1.textureRust;
      textures[1] = var1.textureMask;
      textures[2] = var1.textureLights;
      textures[3] = var1.textureDamage1Overlay;
      textures[4] = var1.textureDamage1Shell;
      textures[5] = var1.textureDamage2Overlay;
      textures[6] = var1.textureDamage2Shell;
      switches[0] = var1.textureUninstall1;
      switches[1] = var1.textureUninstall2;
      switches[2] = var1.textureLightsEnables1;
      switches[3] = var1.textureLightsEnables2;
      switches[4] = var1.textureDamage1Enables1;
      switches[5] = var1.textureDamage1Enables2;
      switches[6] = var1.textureDamage2Enables1;
      switches[7] = var1.textureDamage2Enables2;
      switches[8] = var1.matrixBlood1Enables1;
      switches[9] = var1.matrixBlood1Enables2;
      switches[10] = var1.matrixBlood2Enables1;
      switches[11] = var1.matrixBlood2Enables2;
      return var0.vehicles.add(textures, switches, var1.textureRustA, var1.painColor.x, var1.painColor.y, var1.painColor.z, var2);
   }

   private VehicleCapture() {
   }
}
