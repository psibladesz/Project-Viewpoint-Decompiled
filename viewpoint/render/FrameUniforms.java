package viewpoint.render;

import java.util.IdentityHashMap;
import org.joml.Matrix3f;
import org.lwjgl.opengl.GL13;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;

final class FrameUniforms {
   private static final String[] NAMES = new String[]{
      "uRainStrength",
      "uSnowStrength",
      "uWet",
      "uPuddles",
      "uMist",
      "uNight",
      "uCloudCover",
      "uOvercast",
      "uTime",
      "uFrame",
      "uDayFraction",
      "uSunSine",
      "uMoonPhase",
      "uEyeSky",
      "uSceneOrigin",
      "uHorizon",
      "uZenith",
      "uSkySun",
      "uSkyMoon",
      "uSunTrue",
      "uMoonDisc",
      "uMoonLight",
      "uAdapted",
      "uSkySunColor",
      "uSkyGlow",
      "uSkyLevel",
      "uWind",
      "uExposureA",
      "uExposureSize",
      "uNoiseTex",
      "uCloudShape",
      "uCloudDetail",
      "uCloudCurl",
      "uCloudSheets",
      "uStars",
      "uMoonMap",
      "uToneLut",
      "uFloorPlan",
      "uPlanCorner",
      "uLampFields",
      "uTilePalette",
      "uStarFrame",
      "uMoonFace",
      "uTransmittance",
      "uMultipleScattering",
      "uSkyView",
      "uSkyLight",
      "uOcclusion",
      "uBounce",
      "uBounceFloor",
      "uFlashBounce",
      "uAirLight",
      "uSkyBaseZenith",
      "uSkyBaseHorizon",
      "uWaterGrid",
      "uRainTextures",
      "uRainClock",
      "uWaves",
      "uWaveTiles",
      "uWaveTime",
      "uWaveDrift",
      "uSceneFog",
      "uShadowDepth0",
      "uShadowDepth1",
      "uShadowDepth2",
      "uCameraFocus",
      "uCameraAim",
      "uCameraTrauma"
   };
   private static final float EYE_EASE = 1.5F;
   private static final Matrix3f STAR_FRAME = new Matrix3f();
   private static final IdentityHashMap<GlProgram, Boolean> declares = new IdentityHashMap<>();
   private static int links = -1;
   private static float eyeSky = -1.0F;

   static void set(FrameContext var0, Targets var1) {
      SceneData var2 = var0.scene;
      WaveDrift.advance(var2.cloudTime, var2.windX, var2.windZ);
      float var3 = 1.0F - (float)Math.exp(-var0.frameSeconds / 1.5F);
      eyeSky = eyeSky < 0.0F ? var2.eyeSky : eyeSky + (var2.eyeSky - eyeSky) * var3;
      if (links != GlProgram.links()) {
         declares.clear();
         links = GlProgram.links();
      }

      var1.cloudNoise.update();
      var1.starField.update();
      var1.air.update();
      var1.uploadGrids(var2, var0.index);
      var1.floorPlan.update(var2.floorPlan);
      TilePalette.update();
      GlProgram.forEach(var1x -> {
         if (declares.computeIfAbsent(var1x, FrameUniforms::declaresAny)) {
            var1x.use();
            values(var1x, var0);
         }
      });
      var1.lampFields.draw(var2);
      bind(var1);
   }

   static void bind(Targets var0) {
      var0.cloudNoise.bind();
      var0.starField.bind();
      var0.toneLut.bind();
      var0.floorPlan.bind();
      var0.lampFields.bind();
      TilePalette.bind();
      var0.air.bind();
      var0.rainTextures.bind();
      Gl.bind(84, var0.noise2d);
      Gl.bind(80, var0.water);
      GL13.glActiveTexture(33984);
   }

   private static boolean declaresAny(GlProgram var0) {
      for (String var4 : NAMES) {
         if (var0.has(var4)) {
            return true;
         }
      }

      return false;
   }

   private static void values(GlProgram var0, FrameContext var1) {
      SceneData var2 = var1.scene;
      one(var0, "uRainStrength", var2.rain);
      one(var0, "uSnowStrength", var2.snow);
      one(var0, "uWet", var2.wetGround);
      one(var0, "uPuddles", var2.puddles);
      one(var0, "uMist", var2.mist);
      one(var0, "uNight", var2.night);
      one(var0, "uCloudCover", var2.cloudCover);
      one(var0, "uOvercast", var2.overcast);
      one(var0, "uTime", var2.time);
      one(var0, "uDayFraction", var2.dayFraction);
      one(var0, "uSunSine", var2.sunSine);
      one(var0, "uEyeSky", eyeSky);
      three(var0, "uSceneOrigin", (float)var2.originX, (float)var2.originY, (float)var2.originZ);
      three(var0, "uHorizon", var2.fogR, var2.fogG, var2.fogB);
      three(var0, "uZenith", var2.zenithR, var2.zenithG, var2.zenithB);
      three(var0, "uSkySun", var2.skySunX, var2.skySunY, var2.skySunZ);
      three(var0, "uSkyMoon", var2.skyMoonX, var2.skyMoonY, var2.skyMoonZ);
      three(var0, "uSunTrue", var2.sunTrueX, var2.sunTrueY, var2.sunTrueZ);
      one(var0, "uMoonDisc", var2.moonDisc);
      one(var0, "uMoonLight", var2.moonLight);
      one(var0, "uAdapted", var2.adapted);
      one(var0, "uBounceFloor", var1.floorY());
      one(var0, "uRainClock", var2.time % 600.0F);
      waves(var0, var2);
      PlanSnapshot var3 = var2.floorPlan;
      three(var0, "uPlanCorner", var3.chunkX * 8, var3.chunkY * 8, var3.level);
      three(var0, "uSkySunColor", var2.sunR, var2.sunG, var2.sunB);
      one(var0, "uSkyGlow", var2.skyGlow);
      three(var0, "uSkyLevel", var2.skyR, var2.skyG, var2.skyB);
      one(var0, "uCameraFocus", var2.cameraFocus);
      one(var0, "uCameraAim", var2.cameraAim);
      one(var0, "uCameraTrauma", var2.cameraTrauma);
      if (var0.has("uWind")) {
         var0.set("uWind", var2.windX, var2.windZ);
      }

      if (var0.has("uSceneFog")) {
         var0.set("uSceneFog", var2.fogStart, var2.fogEnd);
      }

      if (var0.has("uExposureA")) {
         var0.set("uExposureA", var2.exposureAX, var2.exposureAY);
      }

      one(var0, "uExposureSize", 64.0F);
      integer(var0, "uFrame", (int)var1.index);
      integer(var0, "uMoonPhase", var2.moonPhase);
      one(var0, "uMoonFace", var2.moonFace);
      three(var0, "uAirLight", var2.airLight[0], var2.airLight[1], var2.airLight[2]);
      three(var0, "uSkyBaseZenith", var2.skyBaseZenith[0], var2.skyBaseZenith[1], var2.skyBaseZenith[2]);
      three(var0, "uSkyBaseHorizon", var2.skyBaseHorizon[0], var2.skyBaseHorizon[1], var2.skyBaseHorizon[2]);
      if (var0.has("uStarFrame")) {
         var0.set("uStarFrame", STAR_FRAME.set(var2.starFrame));
      }

      units(var0);
   }

   private static void units(GlProgram var0) {
      integer(var0, "uNoiseTex", 84);
      integer(var0, "uCloudShape", 95);
      integer(var0, "uCloudDetail", 96);
      integer(var0, "uCloudCurl", 97);
      integer(var0, "uCloudSheets", 98);
      integer(var0, "uStars", 99);
      integer(var0, "uMoonMap", 100);
      integer(var0, "uToneLut", 109);
      integer(var0, "uFloorPlan", 110);
      integer(var0, "uLampFields", 111);
      integer(var0, "uTilePalette", 113);
      integer(var0, "uTransmittance", 101);
      integer(var0, "uMultipleScattering", 102);
      integer(var0, "uSkyView", 103);
      integer(var0, "uSkyLight", 104);
      integer(var0, "uOcclusion", 105);
      integer(var0, "uBounce", 107);
      integer(var0, "uFlashBounce", 108);
      integer(var0, "uWaterGrid", 80);
      integer(var0, "uRainTextures", 123);
      integer(var0, "uWaves", 122);

      for (int var1 = 0; var1 < Gl.UNIT_SHADOW_DEPTH.length; var1++) {
         integer(var0, "uShadowDepth" + var1, Gl.UNIT_SHADOW_DEPTH[var1]);
      }
   }

   private static void waves(GlProgram var0, SceneData var1) {
      one(var0, "uWaveTime", var1.cloudTime % 256.0F);
      if (var0.has("uWaveTiles")) {
         var0.set("uWaveTiles", squares(WaveTile.TILE_METRES[0]), squares(WaveTile.TILE_METRES[1]));
      }

      if (var0.has("uWaveDrift")) {
         float[] var2 = WaveDrift.value();
         var0.set("uWaveDrift", var2[0], var2[1], var2[2], var2[3]);
      }
   }

   private static float squares(double var0) {
      return (float)(var0 * 0.8164965311686198);
   }

   private static void one(GlProgram var0, String var1, float var2) {
      if (var0.has(var1)) {
         var0.set(var1, var2);
      }
   }

   private static void three(GlProgram var0, String var1, float var2, float var3, float var4) {
      if (var0.has(var1)) {
         var0.set(var1, var2, var3, var4);
      }
   }

   private static void integer(GlProgram var0, String var1, int var2) {
      if (var0.has(var1)) {
         var0.setInt(var1, var2);
      }
   }

   private FrameUniforms() {
   }
}
