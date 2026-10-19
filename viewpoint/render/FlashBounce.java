package viewpoint.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;

final class FlashBounce {
   private static final int LIGHTS = 16;
   private static final int ROWS = 3;
   private final GlProgram program = GlProgram.create("flash bounce", "screen.vert", "flash_bounce.frag");
   private final Targets targets;
   private final ShadowPass shadows;
   private final int texture;
   private final int fbo;
   private final int rawDepthSampler;
   private final Matrix4f unproject = new Matrix4f();

   FlashBounce(Targets var1, ShadowPass var2) {
      this.targets = var1;
      this.shadows = var2;
      this.program.sampler("uFlashDepth", 22);
      this.program.sampler("uAlbedo", 9);
      this.program.sampler("uDepth", 12);
      Spots.sampler(this.program);
      int var3 = GL11.glGetInteger(32873);
      int var4 = GL11.glGetInteger(36006);
      this.texture = Gl.texture(34836, 16, 3, 6408, 5126, 9728);
      GL11.glBindTexture(3553, var3);
      this.fbo = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, this.fbo);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, this.texture, 0);
      GL30.glBindFramebuffer(36160, var4);
      this.rawDepthSampler = GL33.glGenSamplers();
      GL33.glSamplerParameteri(this.rawDepthSampler, 10241, 9728);
      GL33.glSamplerParameteri(this.rawDepthSampler, 10240, 9728);
      GL33.glSamplerParameteri(this.rawDepthSampler, 10242, 33071);
      GL33.glSamplerParameteri(this.rawDepthSampler, 10243, 33071);
      GL33.glSamplerParameteri(this.rawDepthSampler, 34892, 0);
   }

   void draw(FrameContext var1, boolean var2) {
      Gl.bind(108, 0);
      GL30.glBindFramebuffer(36160, this.fbo);
      GL11.glViewport(0, 0, 16, 3);
      if (var2 && var1.pipeline.flashlightShadow && !WorldRenderer.off(7)) {
         this.program.use();
         var1.camera(this.program);
         Spots var3 = this.shadows.spots;
         var3.uniforms(this.program);
         this.program.set("uFlashBounceOn", var3.flash.on ? 1.0F : 0.0F);
         var3.flash.viewProjection().invert(this.unproject);
         this.program.set("uFlashUnproject", this.unproject);
         this.program.set("uViewProjection", var1.viewProjection);
         var3.bind(true);
         GL13.glActiveTexture(34006);
         GL11.glBindTexture(35866, var3.mapsTexture());
         GL33.glBindSampler(22, this.rawDepthSampler);
         Gl.bind(9, this.targets.albedo);
         Gl.bind(12, this.targets.depth);
         Gl.screenTriangle();
         GL33.glBindSampler(22, 0);
         GL13.glActiveTexture(34006);
         GL11.glBindTexture(35866, 0);
         var3.bind(false);
         Gl.bind(9, 0);
         Gl.bind(12, 0);
      } else {
         GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         GL11.glClear(16384);
      }

      Gl.bind(108, this.texture);
      GL13.glActiveTexture(33984);
   }
}
