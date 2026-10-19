package viewpoint.render;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;

final class WeatherMap {
   static final int SIZE = 512;
   private static final double TEXEL = 64.0;
   private static final double SPAN = 32768.0;
   private static final double NOISE_PERIOD = 131072.0;
   private static final double CLOUD_PERIOD = 1280.0;
   private static final FloatBuffer ALONG = BufferUtils.createFloatBuffer(97);
   private final GlProgram program = GlProgram.create("weather map", "screen.vert", "weather_map.frag");
   private final int texture;
   private final int fbo;
   private final int cloudNoise;
   private final CloudShadowMap shadow = new CloudShadowMap();
   private long drawnFrame = -1L;

   WeatherMap(int var1) {
      this.cloudNoise = var1;
      int var2 = GL11.glGetInteger(32873);
      int var3 = GL11.glGetInteger(36006);
      this.texture = Gl.texture(32856, 512, 512, 6408, 5121, 9729);
      GL11.glTexParameteri(3553, 10242, 33648);
      GL11.glTexParameteri(3553, 10243, 33648);
      GL11.glBindTexture(3553, var2);
      this.fbo = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, this.fbo);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, this.texture, 0);
      GL30.glBindFramebuffer(36160, var3);
      this.program.sampler("uCloudNoise", 18);
   }

   void draw(FrameContext var1) {
      SceneData var2 = var1.scene;
      GL30.glBindFramebuffer(36160, this.fbo);
      GL11.glViewport(0, 0, 512, 512);
      GL11.glDisable(2929);
      GL11.glDisable(3042);
      this.program.use();
      centre(this.program, var2);
      this.program.set("uCloudTime", var2.cloudTime);
      SkyPass.weatherUniforms(this.program, var2);
      this.bindNoise(true);
      Gl.screenTriangle();
      this.bindNoise(false);
      if (var1.pipeline.cloudShadowMap) {
         Gl.bind(14, this.texture);
         this.bindNoise(true);
         this.shadow.draw(var1);
         this.bind(false);
      }

      this.drawnFrame = var1.index;
   }

   boolean drawn(FrameContext var1) {
      return this.drawnFrame == var1.index;
   }

   int texture() {
      return this.texture;
   }

   void bind(boolean var1) {
      Gl.bind(14, var1 ? this.texture : 0);
      this.shadow.bind(var1);
      this.bindNoise(var1);
   }

   void cloudShadow(GlProgram var1, SceneData var2) {
      this.shadow.place(var1, var2);
   }

   private void bindNoise(boolean var1) {
      GL13.glActiveTexture(34002);
      GL11.glBindTexture(32879, var1 ? this.cloudNoise : 0);
      GL13.glActiveTexture(33984);
   }

   static void samplers(GlProgram var0) {
      var0.sampler("uCloudNoise", 18);
      var0.sampler("uWeatherMap", 14);
   }

   static void centre(GlProgram var0, SceneData var1) {
      var0.set("uMapCentre", Gl.wrap(centre(carriedX(var1)), 131072.0), Gl.wrap(centre(carriedZ(var1)), 131072.0));
   }

   static void placement(GlProgram var0, SceneData var1) {
      double var2 = carriedX(var1);
      double var4 = carriedZ(var1);
      if (var0.has("uWeatherOffset")) {
         var0.set("uWeatherOffset", (float)(var2 - centre(var2)), (float)(var4 - centre(var4)));
      }

      if (var0.has("uWeatherSpan")) {
         var0.set("uWeatherSpan", 32768.0F);
      }
   }

   static void along(GlProgram var0, SceneData var1) {
      if (var0.has("uCoverAlong")) {
         ALONG.clear().put(var1.coverAlong).flip();
         var0.setFloats("uCoverAlong", ALONG);
      }

      if (var0.has("uTypeAlong")) {
         ALONG.clear().put(var1.typeAlong).flip();
         var0.setFloats("uTypeAlong", ALONG);
      }

      if (var0.has("uAlongStep")) {
         var0.set("uAlongStep", 512.0F);
      }

      if (var0.has("uRainAlong")) {
         ALONG.clear().put(var1.rainAlong).flip();
         var0.setFloats("uRainAlong", ALONG);
      }

      if (var0.has("uAirOrigin")) {
         var0.set("uAirOrigin", (float)(var1.originX - var1.cloudCarriedX), (float)(var1.originZ - var1.cloudCarriedZ));
      }
   }

   static float cloudOffsetX(SceneData var0) {
      return Gl.wrap(carriedX(var0), 1280.0);
   }

   static float cloudOffsetZ(SceneData var0) {
      return Gl.wrap(carriedZ(var0), 1280.0);
   }

   private static double carriedX(SceneData var0) {
      return var0.originX - var0.cloudCarriedX;
   }

   private static double carriedZ(SceneData var0) {
      return var0.originZ - var0.cloudCarriedZ;
   }

   private static double centre(double var0) {
      return Math.floor(var0 / 64.0 + 0.5) * 64.0;
   }
}
