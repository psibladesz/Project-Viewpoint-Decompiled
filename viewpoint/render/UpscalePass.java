package viewpoint.render;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GpuParts;
import viewpoint.platform.Pipeline;
import viewpoint.upscale.NativeFiles;
import viewpoint.upscale.Upscaling;

final class UpscalePass {
   private final VendorInputs inputs;
   private Upscaling upscaling;
   private int fbo;
   private boolean fresh;
   private Upscaling.Kind refused;
   private int refusedAt;

   UpscalePass(VendorInputs var1) {
      this.inputs = var1;
   }

   boolean runs(FrameContext var1) {
      Upscaling.Kind var2 = Upscaling.Kind.values()[var1.pipeline.upscaler - 1];
      int var3 = NativeFiles.installs();
      if (var2 == this.refused && this.refusedAt == var3) {
         return false;
      }

      String var4 = var2.unavailable();
      if (var4 == null) {
         this.refused = null;
         return true;
      }

      if (var2 != this.refused) {
         System.out.println("[Viewpoint] " + var2.label + " cannot run here (" + var4 + "): the frame is drawn with the TAA meanwhile");
      }

      this.refused = var2;
      this.refusedAt = var3;
      return false;
   }

   int run(FrameContext var1, int var2) {
      Upscaling var3 = this.upscaling;
      if (var3 != null && var3.frameWidth == var1.width && var3.frameHeight == var1.height) {
         GlDebug.push("upscale");
         GpuParts.begin(16);
         GL43.glCopyImageSubData(var2, 3553, 0, 0, 0, 0, var3.color(), 3553, 0, 0, 0, 0, var1.width, var1.height, 1);
         this.inputs.draw(var1, this.fbo);
         var3.run(this.inputs.describe(var1, this.fresh || !var1.previousValid));
         this.fresh = false;
         GpuParts.end(16);
         GlDebug.pop();
         return var3.output();
      } else {
         throw new IllegalStateException("the upscaler was not fitted to the frame of " + var1.width + " x " + var1.height);
      }
   }

   void release() {
      if (this.upscaling != null) {
         this.upscaling.close();
         GL30.glDeleteFramebuffers(this.fbo);
         this.upscaling = null;
      }
   }

   Upscaling fit(Pipeline var1, int var2, int var3) {
      Upscaling.Kind var4 = Upscaling.Kind.values()[var1.upscaler - 1];
      int var5 = Math.min(var1.renderScale, 100);
      if (this.upscaling != null
         && this.upscaling.kind == var4
         && this.upscaling.scale == var5
         && this.upscaling.outputWidth == var2
         && this.upscaling.outputHeight == var3) {
         return this.upscaling;
      }

      this.release();
      this.upscaling = Upscaling.open(var4, var5, var2, var3);
      this.fbo = VendorInputs.framebuffer(this.upscaling.depth(), this.upscaling.motion(), "the upscaler's inputs");
      this.fresh = true;
      return this.upscaling;
   }
}
