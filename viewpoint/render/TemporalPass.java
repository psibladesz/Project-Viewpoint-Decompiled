package viewpoint.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.Tuning;

final class TemporalPass {
   static final int JITTER_PHASES = 16;
   private static final float UPSCALER_PHASES = 8.0F;
   private final GlProgram resolve = GlProgram.create("taa", "screen.vert", "taa.frag");
   private final GlProgram output = GlProgram.create("taa out", "screen.vert", "taa_out.frag");
   private final Targets targets;
   private int historyIndex;
   boolean historyValid;
   private long lastFrameNanos;
   private double previousOriginX;
   private double previousOriginY;
   private double previousOriginZ;
   private double previousSink;
   private final Matrix4f previousViewProjection = new Matrix4f();

   TemporalPass(Targets var1) {
      this.targets = var1;
      this.resolve.sampler("uCurrent", 0);
      this.resolve.sampler("uHistory", 19);
      this.resolve.sampler("uDepth", 12);
      this.resolve.sampler("uFarDepth", 29);
      this.resolve.sampler("uVelocity", 39);
      this.resolve.sampler("uHistoryCount", 132);
      this.output.sampler("uColor", 0);
   }

   void begin(FrameContext var1) {
      SceneData var2 = var1.scene;
      var1.panini = Panini.of(var2.projection, var2.panini);
      long var3 = WorldRenderer.clock.getAsLong();
      float var5 = (float)(FarRelief.sink(var2) - this.previousSink);
      double var6 = Math.abs(var2.originX - this.previousOriginX)
         + Math.abs(var2.originY - this.previousOriginY)
         + Math.abs(var2.originZ - this.previousOriginZ)
         + Math.abs(var5);
      var1.previousValid = var3 - this.lastFrameNanos <= 250000000L && var6 <= 4.0 && !var2.cameraCut;
      if (!var1.previousValid) {
         this.historyValid = false;
         var1.jitterPhase = jitterPhases(var1) - 1;
         var1.noiseFrame = -1;
      }

      boolean var8 = var1.outputWidth > var1.width;
      TextureFilter.bias(var8 ? (float)(Math.log((double)var1.width / var1.outputWidth) / Math.log(2.0)) : 0.0F);
      var1.frameSeconds = this.lastFrameNanos == 0L ? 0.0F : (float)(var3 - this.lastFrameNanos) * 1.0E-9F;
      this.lastFrameNanos = var3;
      var1.previousViewProjection.set(this.previousViewProjection).translate(0.0F, var5, 0.0F);
      var1.reprojection
         .set(this.previousViewProjection)
         .translate(
            (float)(var2.originX - this.previousOriginX), (float)(var2.originY - this.previousOriginY) + var5, (float)(var2.originZ - this.previousOriginZ)
         );
      var1.jitteredProjection.set(var2.projection);
      var1.jitterX = var1.jitterY = 0.0F;
      if (!WorldRenderer.off(1)) {
         var1.jitterPhase = (var1.jitterPhase + 1) % jitterPhases(var1);
         var1.noiseFrame++;
         float var9 = halton(var1.jitterPhase + 1, 2) - 0.5F;
         float var10 = halton(var1.jitterPhase + 1, 3) - 0.5F;
         var1.jitteredProjection.m20(var1.jitteredProjection.m20() + var9 * 2.0F / var1.width);
         var1.jitteredProjection.m21(var1.jitteredProjection.m21() + var10 * 2.0F / var1.height);
         var1.jitterX = var9;
         var1.jitterY = var10;
      }

      var2.projection.mul(var1.view, var1.plainViewProjection);
      var1.jitteredProjection.mul(var1.view, var1.viewProjection);
      var1.viewProjection.invert(var1.invViewProjection);
      var1.plainViewProjection.invert(var1.invPlainViewProjection);
   }

   int run(FrameContext var1, int var2, boolean var3) {
      if (!var3) {
         this.historyValid = false;
         this.remember(var1);
         return var2;
      } else {
         GlDebug.push("taa");
         GpuParts.begin(16);
         int var4 = this.resolve(var1, var2);
         GpuParts.end(16);
         GlDebug.pop();
         return var4;
      }
   }

   int resolve(FrameContext var1, int var2) {
      SceneData var3 = var1.scene;
      int var4 = this.historyIndex ^ 1;
      GL30.glBindFramebuffer(36160, this.targets.historyFbo[var4]);
      GL11.glViewport(0, 0, var1.outputWidth, var1.outputHeight);
      this.resolve.use();
      var1.camera(this.resolve);
      this.resolve.set("uUnproject", var1.invPlainViewProjection);
      this.resolve.set("uPrevious", var1.reprojection);
      this.resolve.set("uHistoryValid", this.historyValid ? 1.0F : 0.0F);
      this.resolve.set("uFeedback", Tuning.taaFeedback);
      this.resolve.setInt("uCounts", var1.pipeline.taaCounts ? 1 : 0);
      this.resolve.set("uJitter", var1.jitterX, var1.jitterY);
      this.resolve.set("uTexel", 1.0F / var1.width, 1.0F / var1.height);
      this.resolve.set("uHistoryTexel", 1.0F / var1.outputWidth, 1.0F / var1.outputHeight);
      this.resolve.set("uFarUnproject", var1.farInvPlainViewProjection);
      this.resolve.set("uFarRange", var1.farNear, var1.farFar);
      Gl.bind(19, this.targets.history[this.historyIndex]);
      Gl.bind(12, this.targets.depth);
      Gl.bind(29, this.targets.farDepth);
      Gl.bind(39, this.targets.velocity);
      Gl.bind(132, this.targets.historyCount[this.historyIndex]);
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, var2);
      Gl.screenTriangle();
      Gl.bind(19, 0);
      Gl.bind(12, 0);
      Gl.bind(29, 0);
      Gl.bind(39, 0);
      Gl.bind(132, 0);
      GL13.glActiveTexture(33984);
      this.historyIndex = var4;
      this.historyValid = true;
      this.remember(var1);
      return this.targets.history[var4];
   }

   void remember(FrameContext var1) {
      this.previousViewProjection.set(var1.plainViewProjection);
      this.previousOriginX = var1.scene.originX;
      this.previousOriginY = var1.scene.originY;
      this.previousOriginZ = var1.scene.originZ;
      this.previousSink = FarRelief.sink(var1.scene);
   }

   void present(FrameContext var1, int var2, float var3) {
      GL30.glBindFramebuffer(36009, var1.outputDrawFbo);
      GL30.glBindFramebuffer(36008, var1.outputReadFbo);
      GL11.glViewport(var1.viewport[0], var1.viewport[1], var1.viewport[2], var1.viewport[3]);
      this.output.use();
      this.output.set("uSharpen", var3);
      var1.panini.uniforms(this.output);
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, var2);
      Gl.screenTriangle();
      GL11.glBindTexture(3553, 0);
   }

   static int jitterPhases(FrameContext var0) {
      if (!var0.upscaled) {
         return 16;
      }

      float var1 = (float)var0.outputWidth / var0.width;
      return Math.max(1, (int)(8.0F * var1 * var1));
   }

   static float halton(int var0, int var1) {
      float var2 = 0.0F;
      float var3 = 1.0F;
      int var4 = var0;

      while (var4 > 0) {
         var3 /= var1;
         var2 += var3 * (var4 % var1);
         var4 /= var1;
      }

      return var2;
   }
}
