package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Tuning;
import zombie.core.textures.Texture;

final class WeatherPass {
   private static final int RAIN_DROPS = 24000;
   private static final int RAIN_DROPS_FAR = 144000;
   private static final int SNOW_FLAKES = 8000;
   private static final int SNOW_FLAKES_FAR = 32000;
   private static final int SPLASHES = 3000;
   static final float PERIOD = 600.0F;
   private static final double ORIGIN_WRAP = 1536.0;
   private static final double WIND_WRAP = 12288.0;
   private final GlProgram puddles = GlProgram.create("puddle", "mesh.vert", "puddle.frag");
   private final GlProgram precip = GlProgram.create("precip", "precip.vert", "precip.frag");
   private final GlProgram over = GlProgram.create("rain over", "screen.vert", "rain_over.frag");
   private final Targets targets;
   private final ShadowPass shadows;
   private final DistantRain distantRain;
   private boolean layer;
   private boolean due;

   WeatherPass(Targets var1, ShadowPass var2, WeatherMap var3) {
      this.targets = var1;
      this.shadows = var2;
      this.distantRain = new DistantRain(var1, var3);
      this.precip.sampler("uRainMap", 24);
      this.precip.sampler("uScene", 0);
      Spots.sampler(this.precip);
      this.over.sampler("uRain", 0);
      this.puddles.sampler("uTexture", 0);
      this.puddles.sampler("uLight", 5);
   }

   static boolean any(SceneData var0) {
      return var0.wetGround > 0.001F || var0.puddles > 0.001F || falling(var0) || DistantRain.any(var0);
   }

   static boolean falling(SceneData var0) {
      return var0.rain > 0.001F || var0.snow > 0.001F;
   }

   boolean raining(FrameContext var1) {
      return this.due && falling(var1.scene) && this.shadows.rain.on;
   }

   static float fallSpeed(SceneData var0) {
      return var0.snow > var0.rain ? Tuning.snowFall : Tuning.rainFall;
   }

   static void air(GlProgram var0, SceneData var1) {
      if (var0.has("uWindOffset")) {
         var0.set("uWindOffset", Gl.wrap(var1.originX - var1.windCarriedX, 12288.0), Gl.wrap(var1.originZ - var1.windCarriedZ, 12288.0));
      }

      if (var0.has("uRainClock")) {
         var0.set("uRainClock", var1.time % 600.0F);
      }

      if (var0.has("uSheets")) {
         var0.set("uSheets", var1.sheets);
      }

      if (var0.has("uGusts")) {
         var0.set("uGusts", var1.gusts);
      }

      if (var0.has("uWeatherNoise")) {
         var0.setInt("uWeatherNoise", 84);
      }
   }

   static int drawn(float var0, int var1, float var2, int var3) {
      return Math.round(var0 * (var1 / 100.0F) * (1.0F + var2) * var3);
   }

   void draw(FrameContext var1) {
      SceneData var2 = var1.scene;
      boolean var3 = (var2.wetGround > 0.001F || var2.puddles > 0.001F) && var1.pipeline.puddles && !var1.pipeline.rainGround;
      this.due = true;
      this.targets.uploadGrids(var2, var1.index);
      GL13.glActiveTexture(33984);
      GL11.glEnable(2929);
      GL11.glDepthMask(false);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glDisable(2884);
      GL11.glDisable(3008);
      GL11.glDisable(3089);
      if (var3) {
         this.drawPuddles(var1, var2);
      }

      if (DistantRain.any(var2)) {
         this.distantRain.draw(var1);
      }

      GL11.glBindTexture(3553, 0);
      Texture.lastTextureID = -1;
      GL11.glDepthMask(true);
      GL11.glDepthFunc(513);
      GL11.glDisable(3042);
   }

   private void drawPuddles(FrameContext var1, SceneData var2) {
      float var3 = (float)(-var2.originX - Math.floor(-var2.originX / 1024.0) * 1024.0);
      float var4 = (float)(-var2.originZ - Math.floor(-var2.originZ / 1024.0) * 1024.0);
      GL11.glDepthFunc(515);
      this.puddles.use();
      this.puddles.set("uViewProjection", var1.viewProjection);
      this.puddles.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
      this.puddles.set("uSunDir", var2.skySunX, var2.skySunY, var2.skySunZ);
      this.puddles.set("uSunColor", var2.sunR, var2.sunG, var2.sunB);
      this.puddles.set("uHorizon", var2.fogR, var2.fogG, var2.fogB);
      this.puddles.set("uZenith", var2.zenithR, var2.zenithG, var2.zenithB);
      this.puddles.set("uSunStrength", var2.sunStrength);
      this.puddles.set("uPuddles", var2.puddles);
      this.puddles.set("uWet", var2.wetGround);
      SkyPass.weatherUniforms(this.puddles, var2);
      this.puddles.set("uFadeNoise", var1.fadeNoise());
      this.puddles.set("uFogRange", 1000000.0F, 2000000.0F);
      this.puddles.set("uCam", var3, var4);
      GL13.glActiveTexture(33984);
      Meshes.draw(var2, this.puddles, (byte)1, Math.max(0, (int)Math.floor(var2.originY / 2.4494896F + 0.01)), false, false);
      GL13.glActiveTexture(33984);
   }

   void falling(FrameContext var1, int var2) {
      boolean var3 = this.raining(var1);
      this.due = false;
      if (var3) {
         GlDebug.push("rain");
         SceneData var4 = var1.scene;
         boolean var5 = var4.snow > var4.rain;
         float var6 = var5 ? var4.snow : var4.rain;
         GL30.glBindFramebuffer(36160, this.targets.rainFbo);
         GL11.glViewport(0, 0, var1.width, var1.height);
         GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         GL11.glClear(16384);
         GL11.glEnable(2929);
         GL11.glDepthMask(false);
         GL11.glDepthFunc(513);
         GL11.glDisable(2884);
         GL11.glEnable(3042);
         GL11.glBlendFunc(1, 771);
         this.uniforms(var1);
         Gl.bind(0, var2);
         this.precip.set("uMode", var5 ? 1.0F : 0.0F);
         this.precip.set("uFar", 0.0F);
         this.field(var1, var6, var5 ? 8000 : 24000);
         this.precip.set("uFar", 1.0F);
         this.field(var1, var6, var5 ? 32000 : 144000);
         this.precip.set("uFar", 0.0F);
         if (!var5) {
            this.precip.set("uMode", 2.0F);
            this.field(var1, var6, 3000);
            this.precip.set("uMode", 3.0F);
            this.field(var1, var6, 3000);
         }

         this.shadows.rain.uniforms(this.precip, false);
         this.shadows.spots.bind(false);
         Gl.bind(0, 0);
         GL11.glBlendFunc(770, 771);
         GL11.glDisable(3042);
         GL11.glDepthMask(true);
         GL11.glDisable(2929);
         this.layer = true;
         GlDebug.pop();
      }
   }

   private void uniforms(FrameContext var1) {
      SceneData var2 = var1.scene;
      this.precip.use();
      this.precip.set("uViewProjection", var1.plainViewProjection);
      this.precip.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
      this.precip
         .set(
            "uOrigin",
            (float)(var2.originX - Math.floor(var2.originX / 1536.0) * 1536.0),
            (float)var2.originY,
            (float)(var2.originZ - Math.floor(var2.originZ / 1536.0) * 1536.0)
         );
      SkyPass.weatherUniforms(this.precip, var2);
      this.precip.set("uFall", fallSpeed(var2));
      this.precip.set("uExposure", Tuning.rainExposure);
      this.precip.set("uPixel", 2.0F / (var1.height * var2.projection.m11()));
      float var3 = var2.snow > var2.rain ? 1.0F : Tuning.rainSky;
      this.precip.set("uSky", var2.fogR * var3, var2.fogG * var3, var2.fogB * var3);
      this.precip.set("uFogRange", var2.fogStart, var2.fogEnd);
      this.precip.set("uHaze", Tuning.farHaze, var2.nearEnd());
      this.precip.set("uLampGain", Tuning.rainLampGain);
      this.precip.set("uViewport", var1.width, var1.height);
      this.precip.set("uViewExposure", EyeMeter.exposure(var1));
      SurfacePass.lamps(this.precip, var2);
      this.shadows.spots.uniforms(this.precip);
      this.shadows.spots.bind(true);
      this.shadows.rain.uniforms(this.precip, true);
   }

   private void field(FrameContext var1, float var2, int var3) {
      int var4 = drawn(var2, var1.pipeline.rainDensity, var1.scene.sheets, var3);
      this.precip.set("uMean", var2 * (var1.pipeline.rainDensity / 100.0F) * var3);
      this.precip.set("uCount", var4);
      Gl.generated(var4 * 6);
   }

   void overlay(FrameContext var1) {
      if (this.layer) {
         this.layer = false;
         GL11.glDisable(2929);
         GL11.glEnable(3042);
         GL14.glBlendFuncSeparate(1, 771, 0, 1);
         this.over.use();
         var1.panini.uniforms(this.over);
         GL13.glActiveTexture(33984);
         GL11.glBindTexture(3553, this.targets.rain);
         Gl.screenTriangle();
         GL11.glBindTexture(3553, 0);
         Texture.lastTextureID = -1;
         GL11.glBlendFunc(770, 771);
         GL11.glDisable(3042);
      }
   }
}
