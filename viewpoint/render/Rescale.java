package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;

final class Rescale {
   private final GlProgram easu = GlProgram.create("fsr easu", "screen.vert", "fsr_easu.frag");
   private final GlProgram ssaa = GlProgram.create("ssaa", "screen.vert", "ssaa_down.frag");
   private final GlProgram layer = GlProgram.create("layer over", "screen.vert", "layer_over.frag");
   private int width;
   private int height;
   private int upscaled;
   private int upscaledFbo;
   private int outlines;
   private int outlinesFbo;
   private boolean outlined;

   Rescale() {
      this.easu.sampler("uColor", 0);
      this.ssaa.sampler("uColor", 0);
      this.layer.sampler("uLayer", 0);
   }

   static boolean scaled(FrameContext var0) {
      return var0.width != var0.viewport[2] || var0.height != var0.viewport[3];
   }

   void present(FrameContext var1, TemporalPass var2, int var3, float var4, float var5) {
      if (var1.outputWidth == var1.viewport[2] && var1.outputHeight == var1.viewport[3]) {
         var2.present(var1, var3, var4);
      } else if (var1.width < var1.viewport[2]) {
         var2.present(var1, this.upscaled(var1, var3), Math.max(var4, var5));
      } else {
         this.downscaled(var1, var3);
      }

      this.overlay(var1);
   }

   private int upscaled(FrameContext var1, int var2) {
      this.fit(var1.viewport[2], var1.viewport[3]);
      GL30.glBindFramebuffer(36160, this.upscaledFbo);
      GL11.glViewport(0, 0, this.width, this.height);
      this.easu.use();
      Gl.bind(0, var2);
      Gl.screenTriangle();
      Gl.bind(0, 0);
      return this.upscaled;
   }

   private void downscaled(FrameContext var1, int var2) {
      output(var1);
      this.ssaa.use();
      this.ssaa.set("uFootprint", (float)var1.width / var1.viewport[2], (float)var1.height / var1.viewport[3]);
      Gl.bind(0, var2);
      Gl.screenTriangle();
      Gl.bind(0, 0);
   }

   int outlineTarget(FrameContext var1, int var2) {
      if (!scaled(var1)) {
         return var2;
      }

      this.fit(var1.viewport[2], var1.viewport[3]);
      if (!this.outlined) {
         int var3 = GL11.glGetInteger(36006);
         GL30.glBindFramebuffer(36160, this.outlinesFbo);
         GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         GL11.glClear(16384);
         GL30.glBindFramebuffer(36160, var3);
         this.outlined = true;
      }

      return this.outlinesFbo;
   }

   private void overlay(FrameContext var1) {
      if (this.outlined) {
         this.outlined = false;
         output(var1);
         GL11.glEnable(3042);
         GL11.glBlendFunc(1, 771);
         this.layer.use();
         Gl.bind(0, this.outlines);
         Gl.screenTriangle();
         Gl.bind(0, 0);
         GL11.glDisable(3042);
      }
   }

   private static void output(FrameContext var0) {
      GL30.glBindFramebuffer(36009, var0.outputDrawFbo);
      GL30.glBindFramebuffer(36008, var0.outputReadFbo);
      GL11.glViewport(var0.viewport[0], var0.viewport[1], var0.viewport[2], var0.viewport[3]);
   }

   private static int framebuffer(int var0) {
      int var1 = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, var1);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, var0, 0);
      int var2 = GL30.glCheckFramebufferStatus(36160);
      if (var2 != 36053) {
         throw new IllegalStateException("the rescale's target is incomplete: 0x" + Integer.toHexString(var2));
      } else {
         return var1;
      }
   }

   private void fit(int var1, int var2) {
      if (var1 != this.width || var2 != this.height) {
         if (this.width != 0) {
            GL11.glDeleteTextures(new int[]{this.upscaled, this.outlines});
            GL30.glDeleteFramebuffers(new int[]{this.upscaledFbo, this.outlinesFbo});
         }

         this.width = var1;
         this.height = var2;
         int var3 = GL11.glGetInteger(36006);
         this.upscaled = Gl.texture(34842, var1, var2, 6408, 5126, 9729);
         this.outlines = Gl.texture(32856, var1, var2, 6408, 5121, 9729);
         GL11.glBindTexture(3553, 0);
         this.upscaledFbo = framebuffer(this.upscaled);
         this.outlinesFbo = framebuffer(this.outlines);
         GL30.glBindFramebuffer(36160, var3);
      }
   }
}
