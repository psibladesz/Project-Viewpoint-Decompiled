package viewpoint.render;

import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;

final class SunAir {
   static final int SLICES = 512;
   static final int SAMPLES = 256;
   private static final float NEAR_W = 1.0E-4F;
   private static final float FAR_OFF = 10000.0F;
   private static final int[] STRIDES = new int[]{16, 4, 1};
   private final GlProgram coords = GlProgram.create("sun air places", "screen.vert", "sun_air_coords.frag");
   private final GlProgram mark = GlProgram.create("sun air mark", "screen.vert", "sun_air_mark.frag");
   private final GlProgram march = GlProgram.create("sun air march", "screen.vert", "sun_air_march.frag");
   private final GlProgram fill = GlProgram.create("sun air fill", "screen.vert", "sun_air_fill.frag");
   private final Targets targets;
   private final ShadowPass shadows;
   private final Vector4f light = new Vector4f();
   private final int coordsTexture;
   private final int marchedTexture;
   private final int filledTexture;
   private final int coordsFbo;
   private final int marchedFbo;
   private final int filledFbo;
   private final int stencil;
   private final int markFbo;
   private float epipoleU;
   private float epipoleV;

   SunAir(Targets var1, ShadowPass var2) {
      this.targets = var1;
      this.shadows = var2;
      int var3 = GL11.glGetInteger(32873);
      int var4 = GL11.glGetInteger(36006);
      int var5 = GL11.glGetInteger(36010);
      this.coordsTexture = Gl.texture(34836, 256, 512, 6408, 5126, 9728);
      this.marchedTexture = Gl.texture(34842, 256, 512, 6408, 5126, 9728);
      this.filledTexture = Gl.texture(34842, 256, 512, 6408, 5126, 9728);
      GL11.glBindTexture(3553, var3);
      this.coordsFbo = BounceField.framebuffer(this.coordsTexture);
      this.filledFbo = BounceField.framebuffer(this.filledTexture);
      this.marchedFbo = BounceField.framebuffer(this.marchedTexture);
      this.stencil = GL30.glGenRenderbuffers();
      GL30.glBindRenderbuffer(36161, this.stencil);
      GL30.glRenderbufferStorage(36161, 35056, 256, 512);
      GL30.glBindRenderbuffer(36161, 0);
      GL30.glFramebufferRenderbuffer(36160, 33306, 36161, this.stencil);
      this.markFbo = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, this.markFbo);
      GL30.glFramebufferRenderbuffer(36160, 33306, 36161, this.stencil);
      GL11.glDrawBuffer(0);
      GL11.glReadBuffer(0);
      complete("marks");
      GL30.glBindFramebuffer(36009, var4);
      GL30.glBindFramebuffer(36008, var5);
      this.coords.sampler("uDepth", 12);
      this.mark.sampler("uCoords", 117);
      this.mark.sampler("uMarched", 118);
      this.march.sampler("uCoords", 117);
      this.march.sampler("uNoise", 18);
      ShadowPass.samplers(this.march);
      Spots.sampler(this.march);
      this.fill.sampler("uMarched", 118);
   }

   void draw(FrameContext var1) {
      SceneData var2 = var1.scene;
      this.epipole(var1);
      GpuParts.begin(34);
      GL11.glViewport(0, 0, 256, 512);
      GL30.glBindFramebuffer(36160, this.coordsFbo);
      this.coords.use();
      var1.camera(this.coords);
      this.coords.set("uEpipole", this.epipoleU, this.epipoleV);
      this.coords.set("uRange", var2.nearEnd());
      Gl.bind(12, this.targets.depth);
      Gl.screenTriangle();
      Gl.bind(12, 0);
      GpuParts.end(34);
      GpuParts.begin(35);
      this.marchAll(var1);
      GpuParts.end(35);
      GpuParts.begin(36);
      GL30.glBindFramebuffer(36160, this.filledFbo);
      this.fill.use();
      this.fill.set("uEpipole", this.epipoleU, this.epipoleV);
      Gl.bind(118, this.marchedTexture);
      Gl.screenTriangle();
      GpuParts.end(36);
      GL13.glActiveTexture(33984);
   }

   private void marchAll(FrameContext var1) {
      boolean var2 = GL11.glIsEnabled(2929);
      GL11.glDisable(2929);
      GL30.glBindFramebuffer(36160, this.marchedFbo);
      GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      GL11.glClear(16384);
      this.march.use();
      VolumePass.air(this.march, var1, this.shadows);
      this.mark.use();
      Gl.bind(117, this.coordsTexture);
      Gl.bind(118, this.marchedTexture);
      GL13.glActiveTexture(34002);
      GL11.glBindTexture(32879, this.targets.noise);
      this.shadows.bind(true);
      GL11.glEnable(2960);
      GL11.glStencilMask(255);

      for (int var6 : STRIDES) {
         GL30.glBindFramebuffer(36160, this.markFbo);
         GL11.glClearStencil(0);
         GL11.glClear(1024);
         GL11.glStencilFunc(519, 1, 255);
         GL11.glStencilOp(7680, 7680, 7681);
         this.mark.use();
         this.mark.setInt("uStride", var6);
         Gl.screenTriangle();
         GL30.glBindFramebuffer(36160, this.marchedFbo);
         GL11.glStencilFunc(514, 1, 255);
         GL11.glStencilOp(7680, 7680, 7680);
         this.march.use();
         Gl.screenTriangle();
      }

      GL11.glDisable(2960);
      if (var2) {
         GL11.glEnable(2929);
      }

      this.shadows.bind(false);
      GL13.glActiveTexture(34002);
      GL11.glBindTexture(32879, 0);
   }

   private static void complete(String var0) {
      int var1 = GL30.glCheckFramebufferStatus(36160);
      if (var1 != 36053) {
         throw new IllegalStateException("the sun air's " + var0 + " target is incomplete: 0x" + Integer.toHexString(var1));
      }
   }

   void bind(GlProgram var1) {
      var1.set("uEpipole", this.epipoleU, this.epipoleV);
      Gl.bind(117, this.coordsTexture);
      Gl.bind(118, this.filledTexture);
   }

   void release() {
      Gl.bind(117, 0);
      Gl.bind(118, 0);
   }

   private void epipole(FrameContext var1) {
      SceneData var2 = var1.scene;
      this.light.set(var2.sunX, var2.sunY, var2.sunZ, 0.0F).mul(var1.plainViewProjection);
      if (Math.abs(this.light.w) < 1.0E-4F) {
         float var3 = (float)Math.max(Math.hypot(this.light.x, this.light.y), 1.0E-6);
         this.epipoleU = 0.5F + this.light.x / var3 * 10000.0F;
         this.epipoleV = 0.5F + this.light.y / var3 * 10000.0F;
      } else {
         this.epipoleU = this.light.x / this.light.w * 0.5F + 0.5F;
         this.epipoleV = this.light.y / this.light.w * 0.5F + 0.5F;
      }
   }
}
