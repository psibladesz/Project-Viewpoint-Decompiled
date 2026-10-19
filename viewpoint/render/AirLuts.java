package viewpoint.render;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.EarthAir;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;

final class AirLuts {
   static final int VIEW_WIDTH = 192;
   static final int VIEW_HEIGHT = 108;
   static final int SKY_LIGHT_TEXELS = 10;
   static final int AROUND = 32;
   static final int RINGS = 16;
   private volatile AirTables made;
   private volatile Throwable failed;
   final int transmittance;
   final int scattering;
   final int skyView;
   final int skyLight;
   private final GlProgram program = GlProgram.create("sky view", "screen.vert", "sky_view.frag");
   private final GlProgram lightProgram = GlProgram.create("sky light", "screen.vert", "sky_light.frag");
   private final GlProgram directionsProgram = GlProgram.create("sky light directions", "screen.vert", "sky_light_directions.frag");
   private final GlProgram ringsProgram = GlProgram.create("sky light rings", "screen.vert", "sky_light_rings.frag");
   private final int directions;
   private final int rings;
   private final int fbo;
   private final int lightFbo;
   private final int directionsFbo;
   private final int ringsFbo;
   private final Thread maker;
   private float exposure;
   private boolean uploaded;

   AirLuts() {
      int var1 = GL11.glGetInteger(32873);
      int var2 = GL11.glGetInteger(36006);
      this.transmittance = table(256, 64, 34836);
      this.scattering = table(32, 32, 34836);
      this.skyView = table(192, 108, 34842);
      this.skyLight = Gl.texture(34836, 10, 1, 6408, 5126, 9728);
      this.directions = Gl.texture(34836, 32, 16, 6408, 5126, 9728);
      this.rings = Gl.texture(34836, 10, 16, 6408, 5126, 9728);
      GL11.glBindTexture(3553, var1);
      this.fbo = framebuffer(this.skyView);
      this.lightFbo = framebuffer(this.skyLight);
      this.directionsFbo = framebuffer(this.directions);
      this.ringsFbo = framebuffer(this.rings);
      GL30.glBindFramebuffer(36160, var2);
      this.ringsProgram.sampler("uSkyDirections", 131);
      this.lightProgram.sampler("uSkyRings", 131);
      this.maker = new Thread(this::make, "Viewpoint air");
      this.maker.setDaemon(true);
      this.maker.start();
   }

   private static int framebuffer(int var0) {
      int var1 = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, var1);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, var0, 0);
      return var1;
   }

   private static int table(int var0, int var1, int var2) {
      return Gl.texture(var2, var0, var1, 6408, 5126, 9729);
   }

   void update() {
      if (!this.uploaded) {
         if (CloudNoise.waits) {
            try {
               this.maker.join();
            } catch (InterruptedException var6) {
               Thread.currentThread().interrupt();
               throw new IllegalStateException("interrupted waiting for the air's tables", var6);
            }
         }

         if (this.failed != null) {
            throw new IllegalStateException("the air's tables could not be made", this.failed);
         }

         AirTables var1 = this.made;
         if (var1 != null) {
            int var2 = GL11.glGetInteger(35055);
            int var3 = GL11.glGetInteger(3317);
            int var4 = GL11.glGetInteger(3314);
            int var5 = GL11.glGetInteger(32873);
            GL15.glBindBuffer(35052, 0);
            GL11.glPixelStorei(3317, 4);
            GL11.glPixelStorei(3314, 0);
            upload(this.transmittance, 256, 64, var1.transmittance);
            upload(this.scattering, 32, 32, var1.scattering);
            GL11.glBindTexture(3553, var5);
            GL15.glBindBuffer(35052, var2);
            GL11.glPixelStorei(3317, var3);
            GL11.glPixelStorei(3314, var4);
            this.exposure = 0.289F / var1.zenith(EarthAir.NOON_SINE);
            this.uploaded = true;
            System.out.println("[Viewpoint] the air's tables uploaded");
         }
      }
   }

   private static void upload(int var0, int var1, int var2, float[] var3) {
      FloatBuffer var4 = BufferUtils.createFloatBuffer(var3.length).put(var3);
      var4.flip();
      GL11.glBindTexture(3553, var0);
      GL11.glTexSubImage2D(3553, 0, 0, 0, var1, var2, 6408, 5126, var4);
   }

   void draw(FrameContext var1) {
      if (this.uploaded && var1.pipeline.atmosphere) {
         GL30.glBindFramebuffer(36160, this.fbo);
         GL11.glViewport(0, 0, 192, 108);
         GL11.glDisable(2929);
         GL11.glDisable(3042);
         Gl.bind(103, 0);
         this.program.use();
         this.program.set("uEyeHeight", (float)(var1.scene.originY + var1.eyeY));
         this.program.set("uAirExposure", this.exposure);
         GpuParts.begin(39);
         Gl.screenTriangle();
         GpuParts.end(39);
         this.bind();
         Gl.bind(104, 0);
         GpuParts.begin(40);
         this.skyLight();
         GpuParts.end(40);
         Gl.bind(104, this.skyLight);
         GL13.glActiveTexture(33984);
      }
   }

   private void skyLight() {
      GL30.glBindFramebuffer(36160, this.directionsFbo);
      GL11.glViewport(0, 0, 32, 16);
      this.directionsProgram.use();
      this.directionsProgram.set("uAirExposure", this.exposure);
      Gl.screenTriangle();
      Gl.bind(131, this.directions);
      GL30.glBindFramebuffer(36160, this.ringsFbo);
      GL11.glViewport(0, 0, 10, 16);
      this.ringsProgram.use();
      Gl.screenTriangle();
      Gl.bind(131, this.rings);
      GL30.glBindFramebuffer(36160, this.lightFbo);
      GL11.glViewport(0, 0, 10, 1);
      this.lightProgram.use();
      this.lightProgram.set("uAirExposure", this.exposure);
      Gl.screenTriangle();
      Gl.bind(131, 0);
   }

   void bind() {
      Gl.bind(101, this.transmittance);
      Gl.bind(102, this.scattering);
      Gl.bind(103, this.skyView);
      Gl.bind(104, this.skyLight);
      GL13.glActiveTexture(33984);
   }

   private void make() {
      try {
         this.made = AirTables.get();
      } catch (RuntimeException | Error var2) {
         this.failed = var2;
      }
   }
}
