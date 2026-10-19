package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;

final class DistantRain {
   private static final int DIRECTIONS = 512;
   private static final int ROWS = 64;
   private static final float LEAST = 0.001F;
   private final GlProgram stretches = GlProgram.create("distant rain stretches", "screen.vert", "distant_rain_table.frag");
   private final GlProgram sum = GlProgram.create("distant rain table", "screen.vert", "distant_rain_sum.frag");
   private final GlProgram veil = GlProgram.create("distant rain", "screen.vert", "distant_rain.frag");
   private final Targets targets;
   private final WeatherMap weatherMap;
   private final int stretchTexture;
   private final int stretchFbo;
   private final int texture;
   private final int fbo;

   DistantRain(Targets var1, WeatherMap var2) {
      this.targets = var1;
      this.weatherMap = var2;
      int var3 = GL11.glGetInteger(32873);
      int var4 = GL11.glGetInteger(36006);
      this.stretchTexture = Gl.texture(33325, 512, 64, 6403, 5126, 9728);
      this.texture = Gl.texture(33325, 512, 64, 6403, 5126, 9729);
      GL11.glTexParameteri(3553, 10242, 10497);
      GL11.glBindTexture(3553, var3);
      this.stretchFbo = framebuffer(this.stretchTexture);
      this.fbo = framebuffer(this.texture);
      GL30.glBindFramebuffer(36160, var4);
      this.stretches.sampler("uWeatherMap", 14);
      this.sum.sampler("uStretches", 85);
      this.veil.sampler("uDepth", 12);
      this.veil.sampler("uFarDepth", 29);
      this.veil.sampler("uDistantRain", 85);
   }

   private static int framebuffer(int var0) {
      int var1 = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, var1);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, var0, 0);
      return var1;
   }

   static boolean any(SceneData var0) {
      for (float var4 : var0.rainAlong) {
         if (var4 > 0.001F) {
            return true;
         }
      }

      return false;
   }

   void draw(FrameContext var1) {
      if (this.weatherMap.drawn(var1)) {
         SceneData var2 = var1.scene;
         GpuParts.begin(24);
         GL30.glBindFramebuffer(36160, this.stretchFbo);
         GL11.glViewport(0, 0, 512, 64);
         GL11.glDisable(2929);
         GL11.glDisable(3042);
         this.stretches.use();
         this.stretches.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
         WeatherMap.centre(this.stretches, var2);
         SkyPass.weatherUniforms(this.stretches, var2);
         Gl.bind(14, this.weatherMap.texture());
         Gl.screenTriangle();
         GL30.glBindFramebuffer(36160, this.fbo);
         this.sum.use();
         Gl.bind(85, this.stretchTexture);
         Gl.screenTriangle();
         GL30.glBindFramebuffer(36160, this.targets.lightFbo);
         GL11.glViewport(0, 0, var1.width, var1.height);
         GL11.glEnable(3042);
         GL11.glBlendFunc(1, 771);
         this.veil.use();
         var1.camera(this.veil);
         this.veil.set("uForward", -var1.view.m02(), -var1.view.m12(), -var1.view.m22());
         this.veil.set("uFarRange", var1.farNear, var1.farFar);
         this.veil.set("uHorizon", var2.fogR, var2.fogG, var2.fogB);
         this.veil.set("uSunDir", var2.skySunX, var2.skySunY, var2.skySunZ);
         this.veil.set("uSunStrength", var2.skyGlow);
         SkyPass.fogUniforms(this.veil, var2);
         Gl.bind(12, this.targets.depth);
         Gl.bind(29, this.targets.farDepth);
         Gl.bind(85, this.texture);
         Gl.screenTriangle();
         Gl.bind(85, 0);
         Gl.bind(29, 0);
         Gl.bind(12, 0);
         Gl.bind(14, 0);
         GL13.glActiveTexture(33984);
         GL11.glBlendFunc(770, 771);
         GL11.glDisable(3042);
         GL11.glEnable(2929);
         GpuParts.end(24);
      }
   }
}
