package viewpoint.render;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import viewpoint.iris.Biomes;
import viewpoint.iris.IrisMacros;

final class IrisUniforms {
   static final double GROUND = 64.0;
   static final float NEAR = 0.05F;
   private static final float DEPTH_FAR = 4.0F;
   private static final float FREE_FAR_NEAR = 2.0F;
   final Map<String, float[]> values = new HashMap<>();
   final IrisSun sun = new IrisSun();
   final Matrix4f modelView = new Matrix4f();
   final Matrix4f projection = new Matrix4f();
   final Matrix4f shadowView = new Matrix4f();
   final Matrix4f shadowProjection = new Matrix4f();
   final Matrix4f farProjection = new Matrix4f();
   float far = 256.0F;
   float farFar = 2048.0F;
   float farNear = 128.0F;
   double worldX;
   double worldY;
   double worldZ;
   private final Matrix4f previousModelView = new Matrix4f();
   private final Matrix4f previousProjection = new Matrix4f();
   private final Matrix4f previousFarProjection = new Matrix4f();
   private double previousX;
   private double previousY;
   private double previousZ;
   private boolean hasPrevious;
   private float eyeSkySmooth = -1.0F;
   private float wetness;
   private boolean distant;
   private long frames;
   private double seconds;

   void update(FrameContext var1, boolean var2, float var3, float var4, float var5, float var6, boolean var7, float var8) {
      SceneData var9 = var1.scene;
      this.distant = var7;
      this.frames++;
      this.seconds = this.seconds + var1.frameSeconds;
      this.far = Math.max(64.0F, Math.min(1024.0F, var9.nearEnd() > 0.0F ? var9.nearEnd() : 128.0F));
      this.farFar = Math.max(this.far * 2.0F, var8);
      this.matrices(var1);
      this.sun.update(var9, var2);
      this.shadow(var3);
      this.values.clear();
      this.camera(var1);
      this.celestial();
      this.weather(var9, var1.frameSeconds, var4, var5, var6);
      this.put(
         "lightningBoltPosition",
         -(var9.lightningX - var1.eyeX),
         var9.lightningY - var1.eyeY,
         -(var9.lightningZ - var1.eyeZ),
         var9.lightning > 0.0F ? 1.0F : 0.0F
      );
      this.world(var9, var1);
      this.neutral();
      this.remember();
   }

   private void matrices(FrameContext var1) {
      Matrix4f var2 = var1.scene.projection;
      float var3 = 1.0F / var2.m11();
      float var4 = var2.m11() / var2.m00();
      float var5 = 2.0F * (float)Math.atan(var3);
      float var6 = WorldRenderer.freeCamera ? (float)Math.sqrt(var1.eyeX * var1.eyeX + var1.eyeY * var1.eyeY + var1.eyeZ * var1.eyeZ) : 0.0F;
      this.projection.setPerspective(var5, var4, 0.05F, this.far * 4.0F + var6);
      this.farNear = WorldRenderer.freeCamera ? 2.0F : this.far * 0.5F;
      this.farProjection.setPerspective(var5, var4, this.farNear, this.farFar);
      this.modelView.set(var1.view).translate(var1.eyeX, var1.eyeY, var1.eyeZ).scale(-1.0F, 1.0F, -1.0F);
      SceneData var7 = var1.scene;
      this.worldX = -(var7.originX + var1.eyeX);
      this.worldY = 64.0 + var7.originY + var1.eyeY;
      this.worldZ = -(var7.originZ + var1.eyeZ);
   }

   private void shadow(float var1) {
      double[] var2 = this.sun.sunLights ? this.sun.sun : this.sun.moon;
      float var3 = (float)var2[0];
      float var4 = (float)Math.max(var2[1], 0.05);
      float var5 = (float)var2[2];
      float var6 = (float)Math.sqrt(var3 * var3 + var4 * var4 + var5 * var5);
      var3 /= var6;
      var4 /= var6;
      var5 /= var6;
      float var7 = Math.abs(var4) > 0.99F ? 1.0F : 0.0F;
      float var8 = Math.abs(var4) > 0.99F ? 0.0F : 1.0F;
      this.shadowView.setLookAt(var3 * 100.0F, var4 * 100.0F, var5 * 100.0F, 0.0F, 0.0F, 0.0F, var7, var8, 0.0F);
      float var9 = (float)(this.worldX - Math.floor(this.worldX / 2.0) * 2.0);
      float var10 = (float)(this.worldY - Math.floor(this.worldY / 2.0) * 2.0);
      float var11 = (float)(this.worldZ - Math.floor(this.worldZ / 2.0) * 2.0);
      this.shadowView.translate(var9 - 1.0F, var10 - 1.0F, var11 - 1.0F);
      this.shadowProjection.setOrtho(-var1, var1, -var1, var1, 0.05F, 256.0F);
   }

   private void camera(FrameContext var1) {
      this.matrix("gbufferModelView", this.modelView);
      this.matrix("gbufferModelViewInverse", new Matrix4f(this.modelView).invert());
      this.matrix("gbufferProjection", this.projection);
      this.matrix("gbufferProjectionInverse", new Matrix4f(this.projection).invert());
      this.matrix("gbufferPreviousModelView", this.hasPrevious ? this.previousModelView : this.modelView);
      this.matrix("gbufferPreviousProjection", this.hasPrevious ? this.previousProjection : this.projection);
      this.matrix("shadowModelView", this.shadowView);
      this.matrix("shadowModelViewInverse", new Matrix4f(this.shadowView).invert());
      this.matrix("shadowProjection", this.shadowProjection);
      this.matrix("shadowProjectionInverse", new Matrix4f(this.shadowProjection).invert());
      this.matrix("dhProjection", this.farProjection);
      this.matrix("dhProjectionInverse", new Matrix4f(this.farProjection).invert());
      this.matrix("dhPreviousProjection", this.hasPrevious ? this.previousFarProjection : this.farProjection);
      this.position("cameraPosition", this.worldX, this.worldY, this.worldZ);
      this.position(
         "previousCameraPosition",
         this.hasPrevious ? this.previousX : this.worldX,
         this.hasPrevious ? this.previousY : this.worldY,
         this.hasPrevious ? this.previousZ : this.worldZ
      );
      this.put("eyeAltitude", (float)this.worldY);
      this.put("eyePosition", (float)this.worldX, (float)this.worldY, (float)this.worldZ);
      this.put("relativeEyePosition", 0.0F, 0.0F, 0.0F);
      Vector4f var2 = new Vector4f(0.0F, 0.0F, -1.0F, 0.0F).mul(new Matrix4f(this.modelView).invert());
      this.put("playerLookVector", var2.x, var2.y, var2.z);
      this.put("playerBodyVector", var2.x, 0.0F, var2.z);
      this.put("near", 0.05F);
      this.put("far", this.far);
      this.put("dhNearPlane", this.farNear);
      this.put("dhFarPlane", this.farFar);
      this.put("dhRenderDistance", this.farFar);
      this.put("viewWidth", var1.width);
      this.put("viewHeight", var1.height);
      this.put("aspectRatio", (float)var1.width / Math.max(1, var1.height));
      this.put("frameCounter", (float)(this.frames % 720720L));
      this.put("frameTime", var1.frameSeconds);
      this.put("frameTimeCounter", (float)(this.seconds % 3600.0));
      this.put("firstPersonCamera", 1.0F);
   }

   private void celestial() {
      this.direction("sunPosition", this.sun.sun);
      this.direction("moonPosition", this.sun.moon);
      this.direction("shadowLightPosition", this.sun.sunLights ? this.sun.sun : this.sun.moon);
      this.direction("upPosition", new double[]{0.0, 1.0, 0.0});
      this.put("sunAngle", (float)this.sun.sunAngle);
      this.put("shadowAngle", (float)this.sun.shadowAngle);
      this.put("worldTime", this.sun.worldTime);
   }

   private void weather(SceneData var1, float var2, float var3, float var4, float var5) {
      float var6 = Math.max(var1.rain, var1.snow);
      this.put("rainStrength", var6);
      float var7 = (var6 > this.wetness ? var4 : var5) / 20.0F;
      this.wetness = this.wetness + (var6 - this.wetness) * (1.0F - (float)Math.pow(0.5, var2 / Math.max(var7, 0.01F)));
      this.put("wetness", this.wetness);
      this.put("thunderStrength", var1.thunder);
      this.put("biome", Biomes.PLAINS);
      this.put("biome_category", Biomes.CATEGORY_PLAINS);
      this.put("biome_precipitation", var6 <= 0.0F ? 0.0F : (var1.snow > var1.rain ? 2.0F : 1.0F));
      this.put("temperature", var1.snow > var1.rain ? 0.0F : 0.2F + 0.8F * var1.warmth);
      this.put("rainfall", var1.humidity);
      float var8 = var1.eyeSky;
      float var9 = 1.0F - (float)Math.pow(0.5, var2 / Math.max(var3 / 20.0F, 0.01F));
      this.eyeSkySmooth = this.eyeSkySmooth < 0.0F ? var8 : this.eyeSkySmooth + (var8 - this.eyeSkySmooth) * var9;
      this.put("eyeBrightness", 0.0F, 240.0F * var8);
      this.put("eyeBrightnessSmooth", 0.0F, 240.0F * this.eyeSkySmooth);
      this.put("fogColor", srgb(var1.fogR), srgb(var1.fogG), srgb(var1.fogB));
      this.put("skyColor", srgb(var1.zenithR), srgb(var1.zenithG), srgb(var1.zenithB));
      float var10 = this.distant ? Float.MAX_VALUE : this.far;
      this.put("fogStart", Math.min(var1.fogStart, var10));
      this.put("fogEnd", Math.min(var1.fogEnd, var10));
      this.put("fogDensity", var1.mist);
      this.put("fogMode", 9729.0F);
      this.put("fogShape", 1.0F);
   }

   private void world(SceneData var1, FrameContext var2) {
      this.put("worldDay", var1.worldDay);
      this.put("moonPhase", var1.moonPhase);
      this.put("cloudHeight", 192.0F);
      this.put("cloudTime", var1.cloudTime * 20.0F);
      this.put("heightLimit", 384.0F);
      this.put("logicalHeightLimit", 384.0F);
      this.put("bedrockLevel", -64.0F);
      this.put("hasSkylight", 1.0F);
      this.put("ambientLight", 0.0F);
      this.put("screenBrightness", 0.5F);
      this.put("renderStage", IrisMacros.renderStage("NONE"));
      this.put("atlasSize", 4096.0F, 4096.0F);
      this.put("alphaTestRef", 0.1F);
      this.put("isRightHanded", 1.0F);
      this.put("currentColorSpace", 0.0F);
      this.put("textureFilteringMode", 0.0F);
      this.put("centerDepthSmooth", 0.5F);
      this.put("chunkOffset", 0.0F, 0.0F, 0.0F);
      LocalDateTime var3 = LocalDateTime.now();
      this.put("currentDate", var3.getYear(), var3.getMonthValue(), var3.getDayOfMonth());
      this.put("currentTime", var3.getHour(), var3.getMinute(), var3.getSecond());
      this.put("currentYearTime", var3.getDayOfYear() * 86400.0F, 0.0F);
   }

   private void neutral() {
      for (String var4 : new String[]{
         "blindness",
         "nightVision",
         "darknessFactor",
         "darknessLightFactor",
         "playerMood",
         "constantMood",
         "isEyeInWater",
         "is_burning",
         "is_hurt",
         "is_invisible",
         "is_sneaking",
         "is_sprinting",
         "hideGUI",
         "isSpectator",
         "isRiding",
         "inSwimmingAnimation",
         "feetInWater",
         "vehicleInWater",
         "isElytraFlying",
         "heldItemId",
         "heldItemId2",
         "heldBlockLightValue",
         "heldBlockLightValue2",
         "entityId",
         "blockEntityId",
         "currentRenderedItemId",
         "currentSelectedBlockId",
         "vehicleId",
         "hasCeiling"
      }) {
         this.put(var4, 0.0F);
      }

      this.put("is_on_ground", 1.0F);
      this.put("entityColor", 0.0F, 0.0F, 0.0F, 0.0F);

      for (String var8 : new String[]{"Health", "Hunger", "Air", "Armor"}) {
         this.put("currentPlayer" + var8, 1.0F);
         this.put("maxPlayer" + var8, var8.equals("Air") ? 300.0F : 20.0F);
      }
   }

   private void remember() {
      this.previousModelView.set(this.modelView);
      this.previousProjection.set(this.projection);
      this.previousFarProjection.set(this.farProjection);
      this.previousX = this.worldX;
      this.previousY = this.worldY;
      this.previousZ = this.worldZ;
      this.hasPrevious = true;
   }

   void reset() {
      this.hasPrevious = false;
   }

   private void direction(String var1, double[] var2) {
      Vector4f var3 = new Vector4f((float)var2[0] * 100.0F, (float)var2[1] * 100.0F, (float)var2[2] * 100.0F, 0.0F).mul(this.modelView);
      this.put(var1, var3.x, var3.y, var3.z);
   }

   private void position(String var1, double var2, double var4, double var6) {
      this.put(var1, (float)var2, (float)var4, (float)var6);
      double var8 = var2 - Math.floor(var2);
      double var10 = var4 - Math.floor(var4);
      double var12 = var6 - Math.floor(var6);
      this.put(var1 + "Fract", (float)var8, (float)var10, (float)var12);
      this.put(var1 + "Int", (float)Math.floor(var2), (float)Math.floor(var4), (float)Math.floor(var6));
   }

   private void matrix(String var1, Matrix4f var2) {
      float[] var3 = new float[16];
      var2.get(var3);
      this.values.put(var1, var3);
   }

   void put(String var1, float... var2) {
      this.values.put(var1, var2);
   }

   private static float srgb(float var0) {
      float var1 = Math.max(0.0F, Math.min(1.0F, var0));
      return var1 <= 0.0031308F ? var1 * 12.92F : 1.055F * (float)Math.pow(var1, 0.4166666666666667) - 0.055F;
   }
}
