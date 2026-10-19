package viewpoint.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;

final class SkyPass {
   private static final double OLD_CLOUD_PERIOD = 1100.0;
   private static final double OLD_DRIFT_SQUARES = 440.0;
   private final GlProgram clouds = GlProgram.create("clouds", "screen.vert", "cloud.frag");
   private final GlProgram sky = GlProgram.create("sky", "sky.vert", "sky.frag");
   private final Targets targets;
   private final WeatherMap weatherMap;
   private final CloudHistory history = new CloudHistory();
   private boolean carried;

   SkyPass(Targets var1, WeatherMap var2) {
      this.targets = var1;
      this.weatherMap = var2;
      WeatherMap.samplers(this.clouds);
      this.sky.sampler("uClouds", 17);
   }

   void prepare(FrameContext var1) {
      if (var1.pipeline.clouds) {
         GlDebug.push("weather map");
         GpuParts.begin(20);
         this.weatherMap.draw(var1);
         GpuParts.end(20);
         GlDebug.pop();
      }

      GlDebug.push("sky view");
      this.targets.air.draw(var1);
      GlDebug.pop();
   }

   void clouds(FrameContext var1) {
      SceneData var2 = var1.scene;
      Matrix4f var3 = var1.view;
      GL11.glDisable(2929);
      GL11.glDisable(3042);
      GL11.glDepthMask(false);
      this.clouds.use();
      this.carried = this.clouds.has("uCloudPick");
      if (this.carried) {
         this.history.trace(this.clouds, this.targets);
      } else {
         GL30.glBindFramebuffer(36160, this.targets.cloudFbo);
         GL11.glViewport(0, 0, this.targets.cloudWidth, this.targets.cloudHeight);
      }

      float var4 = 1.0F / var2.projection.m00();
      float var5 = 1.0F / var2.projection.m11();
      this.clouds.set("uRight", var3.m00() * var4, var3.m10() * var4, var3.m20() * var4);
      this.clouds.set("uUp", var3.m01() * var5, var3.m11() * var5, var3.m21() * var5);
      this.clouds.set("uForward", -var3.m02(), -var3.m12(), -var3.m22());
      this.clouds.set("uHorizon", var2.fogR, var2.fogG, var2.fogB);
      this.clouds.set("uZenith", var2.zenithR, var2.zenithG, var2.zenithB);
      this.clouds.set("uSunDir", var2.skySunX, var2.skySunY, var2.skySunZ);
      this.clouds.set("uSunColor", var2.sunR, var2.sunG, var2.sunB);
      this.clouds.set("uSunStrength", var2.skyGlow);
      this.clouds.set("uPixelAngle", 2.0F * var5 / this.targets.cloudHeight);
      this.clouds.set("uNight", var2.night);
      if (this.clouds.has("uCloudPhase")) {
         this.clouds.set("uCloudPhase", var1.noiseIndex());
      }

      eye(this.clouds, var1);
      cloudUniforms(this.clouds, var2);
      weatherUniforms(this.clouds, var2);
      oldCloudNames(this.clouds, var2);
      this.weatherMap.bind(true);
      GpuParts.begin(37);
      Gl.screenTriangle();
      GpuParts.end(37);
      this.weatherMap.bind(false);
      if (this.carried) {
         GpuParts.begin(38);
         this.history.resolve(var1, this.targets);
         GpuParts.end(38);
      }

      GL11.glDepthMask(true);
   }

   void sky(FrameContext var1) {
      SceneData var2 = var1.scene;
      Matrix4f var3 = var1.view;
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      this.sky.use();
      float var4 = 1.0F / var2.projection.m00();
      float var5 = 1.0F / var2.projection.m11();
      this.sky.set("uRight", var3.m00() * var4, var3.m10() * var4, var3.m20() * var4);
      this.sky.set("uUp", var3.m01() * var5, var3.m11() * var5, var3.m21() * var5);
      this.sky.set("uForward", -var3.m02(), -var3.m12(), -var3.m22());
      this.sky.set("uHorizon", var2.fogR, var2.fogG, var2.fogB);
      this.sky.set("uZenith", var2.zenithR, var2.zenithG, var2.zenithB);
      this.sky.set("uSunDir", var2.skySunX, var2.skySunY, var2.skySunZ);
      this.sky.set("uSunColor", var2.sunR, var2.sunG, var2.sunB);
      this.sky.set("uSunStrength", var2.skyGlow);
      this.sky.set("uCloudCover", var2.cloudCover);
      this.sky.set("uNight", var2.night);
      this.sky.set("uTime", var2.time);
      this.sky.set("uCloudDrift", oldDriftX(var2), oldDriftZ(var2));
      this.sky.set("uCloudsOn", WorldRenderer.off(6) ? 0.0F : 1.0F);
      weatherUniforms(this.sky, var2);
      eye(this.sky, var1);
      Gl.bind(17, this.carried ? this.history.image(this.targets) : this.targets.clouds);
      GL13.glActiveTexture(33984);
      Gl.screenTriangle();
      Gl.bind(17, 0);
      GL13.glActiveTexture(33984);
      GL11.glDepthMask(true);
   }

   private static void eye(GlProgram var0, FrameContext var1) {
      if (var0.has("uEye")) {
         var0.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
      }
   }

   static void cloudUniforms(GlProgram var0, SceneData var1) {
      if (var0.has("uCloudCover")) {
         var0.set("uCloudCover", var1.cloudCover);
      }

      if (var0.has("uCloudTime")) {
         var0.set("uCloudTime", var1.cloudTime);
      }

      if (var0.has("uCloudOffset")) {
         var0.set("uCloudOffset", WeatherMap.cloudOffsetX(var1), WeatherMap.cloudOffsetZ(var1));
      }

      if (var0.has("uCloudOrigin")) {
         var0.set("uCloudOrigin", Gl.wrap(var1.originX, 1100.0), Gl.wrap(var1.originZ, 1100.0));
      }

      if (var0.has("uCloudDrift")) {
         var0.set("uCloudDrift", oldDriftX(var1), oldDriftZ(var1));
      }
   }

   private static float oldDriftX(SceneData var0) {
      return (float)(-var0.cloudCarriedX / 440.0);
   }

   private static float oldDriftZ(SceneData var0) {
      return (float)(-var0.cloudCarriedZ / 440.0);
   }

   static void weatherUniforms(GlProgram var0, SceneData var1) {
      if (var0.has("uRain")) {
         var0.set("uRain", var1.rain);
      }

      if (var0.has("uSnow")) {
         var0.set("uSnow", var1.snow);
      }

      if (var0.has("uWind")) {
         var0.set("uWind", var1.windX, var1.windZ);
      }

      if (var0.has("uCloudType")) {
         var0.set("uCloudType", var1.cloudType);
      }

      if (var0.has("uCloudWind")) {
         var0.set("uCloudWind", var1.cloudWindX, var1.cloudWindZ);
      }

      if (var0.has("uCloudCarried")) {
         var0.set("uCloudCarried", (float)var1.cloudCarriedX, (float)var1.cloudCarriedZ);
      }

      sheet(var0, "uCloudSheetMid", var1.cloudSheetMid);
      sheet(var0, "uCloudSheetHigh", var1.cloudSheetHigh);
      if (var0.has("uLightning")) {
         var0.set("uLightning", var1.lightningX, var1.lightningY, var1.lightningZ, var1.lightning);
      }

      WeatherMap.placement(var0, var1);
      WeatherMap.along(var0, var1);
      WeatherPass.air(var0, var1);
   }

   private static void sheet(GlProgram var0, String var1, float[] var2) {
      if (var0.has(var1)) {
         var0.set(var1, var2[0], var2[1], var2[2], var2[3]);
      }
   }

   private static void oldCloudNames(GlProgram var0, SceneData var1) {
      if (var0.has("uNoise")) {
         var0.setInt("uNoise", 18);
      }

      if (var0.has("uTime")) {
         var0.set("uTime", var1.cloudTime);
      }
   }

   static void fogUniforms(GlProgram var0, SceneData var1) {
      weatherUniforms(var0, var1);
      if (var0.has("uZenith")) {
         var0.set("uZenith", var1.zenithR, var1.zenithG, var1.zenithB);
      }

      if (var0.has("uSkySun")) {
         var0.set("uSkySun", var1.skySunX, var1.skySunY, var1.skySunZ);
      }

      if (var0.has("uSkySunColor")) {
         var0.set("uSkySunColor", var1.sunR, var1.sunG, var1.sunB);
      }

      if (var0.has("uSkyGlow")) {
         var0.set("uSkyGlow", var1.skyGlow);
      }
   }
}
