package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.Tuning;

final class FroxelPass {
   private static final int PIXELS = 12;
   private static final int COLUMNS = 8;
   private static final int JITTER_FRAMES = 16;
   private final GlProgram inject = GlProgram.create("froxel inject", "screen.vert", "froxel_inject.frag");
   private final GlProgram scan = GlProgram.create("froxel scan", "screen.vert", "froxel_scan.frag");
   private final GlProgram resolve = GlProgram.create("froxel resolve", "screen.vert", "froxel_resolve.frag");
   private final Targets targets;
   private final ShadowPass shadows;
   private final SunAir sun;
   private int across;
   private int up;
   private int slices;
   private int atlasWidth;
   private int atlasHeight;
   private final int[] injected = new int[2];
   private final int[] injectedFbo = new int[2];
   private final int[] runs = new int[2];
   private final int[] runsFbo = new int[2];
   private int current;
   private boolean historyValid;

   FroxelPass(Targets var1, ShadowPass var2) {
      this.targets = var1;
      this.shadows = var2;
      this.sun = new SunAir(var1, var2);
      this.inject.sampler("uNoise", 18);
      this.inject.sampler("uHistory", 114);
      ShadowPass.samplers(this.inject);
      Spots.sampler(this.inject);
      this.scan.sampler("uSource", 114);
      this.resolve.sampler("uDepth", 12);
      this.resolve.sampler("uFarDepth", 29);
      this.resolve.sampler("uFroxelVolume", 115);
      this.resolve.sampler("uSunAirCoords", 117);
      this.resolve.sampler("uSunAir", 118);
      this.resolve.sampler("uNoise", 18);
      ShadowPass.samplers(this.resolve);
      Spots.sampler(this.resolve);
   }

   void draw(FrameContext var1) {
      this.fit(var1);
      int var2 = this.current;
      this.current ^= 1;
      this.injectLight(var1, var2);
      int var3 = this.integrate(var1.scene);
      GpuParts.begin(28);
      this.sun.draw(var1);
      GpuParts.end(28);
      this.resolveInto(var1, var3);
      this.historyValid = true;
   }

   private void injectLight(FrameContext var1, int var2) {
      SceneData var3 = var1.scene;
      GL30.glBindFramebuffer(36160, this.injectedFbo[this.current]);
      GL11.glViewport(0, 0, this.atlasWidth, this.atlasHeight);
      this.inject.use();
      VolumePass.air(this.inject, var1, this.shadows);
      this.layout(this.inject, var3);
      this.inject.set("uFroxelInverse", var1.invPlainViewProjection);
      this.inject.set("uReprojection", var1.reprojection);
      this.inject.set("uHistoryOn", this.historyValid && var1.previousValid ? 1.0F : 0.0F);
      int var4 = (int)(var1.index % 16L) + 1;
      this.inject.set("uJitter", TemporalPass.halton(var4, 2), TemporalPass.halton(var4, 3), TemporalPass.halton(var4, 5));
      GL13.glActiveTexture(34002);
      GL11.glBindTexture(32879, this.targets.noise);
      Gl.bind(114, this.injected[var2]);
      this.shadows.bind(true);
      Gl.screenTriangle();
      this.shadows.bind(false);
      Gl.bind(114, 0);
      GL13.glActiveTexture(34002);
      GL11.glBindTexture(32879, 0);
      GL13.glActiveTexture(33984);
   }

   private int integrate(SceneData var1) {
      this.scan.use();
      this.layout(this.scan, var1);
      int var2 = this.injected[this.current];
      byte var3 = 0;

      for (int var4 = 0; var4 == 0 || var4 < this.slices; var4 = (byte)(var4 == 0 ? 1 : var4 * 2)) {
         GL30.glBindFramebuffer(36160, this.runsFbo[var3]);
         this.scan.setInt("uOffset", var4);
         Gl.bind(114, var2);
         Gl.screenTriangle();
         var2 = this.runs[var3];
         var3 ^= 1;
      }

      Gl.bind(114, 0);
      return var2;
   }

   private void resolveInto(FrameContext var1, int var2) {
      SceneData var3 = var1.scene;
      GL30.glBindFramebuffer(36160, this.targets.volumeFbo);
      GL11.glViewport(0, 0, var1.halfWidth, var1.halfHeight);
      this.resolve.use();
      VolumePass.air(this.resolve, var1, this.shadows);
      this.layout(this.resolve, var3);
      this.resolve.set("uFarOn", var1.farFar > 0.0F ? 1.0F : 0.0F);
      this.resolve.set("uFogColor", var3.fogR, var3.fogG, var3.fogB);
      this.resolve.set("uAmbientHaze", Tuning.ambientHaze);
      Gl.bind(12, this.targets.depth);
      Gl.bind(29, this.targets.farDepth);
      Gl.bind(115, var2);
      this.sun.bind(this.resolve);
      GL13.glActiveTexture(34002);
      GL11.glBindTexture(32879, this.targets.noise);
      this.shadows.bind(true);
      Gl.screenTriangle();
      this.shadows.bind(false);
      GL13.glActiveTexture(34002);
      GL11.glBindTexture(32879, 0);
      this.sun.release();
      Gl.bind(115, 0);
      Gl.bind(29, 0);
      Gl.bind(12, 0);
      GL13.glActiveTexture(33984);
   }

   private void layout(GlProgram var1, SceneData var2) {
      var1.set("uFroxels", this.across, this.up, this.slices);
      var1.set("uFroxelRange", var2.nearEnd());
   }

   private void fit(FrameContext var1) {
      int var2 = (var1.width + 12 - 1) / 12;
      int var3 = (var1.height + 12 - 1) / 12;
      int var4 = 2 * var1.pipeline.froxelSteps;
      if (var2 != this.across || var3 != this.up || var4 != this.slices) {
         this.release();
         this.across = var2;
         this.up = var3;
         this.slices = var4;
         this.atlasWidth = this.across * 8;
         this.atlasHeight = this.up * ((this.slices + 8 - 1) / 8);
         int var5 = GL11.glGetInteger(32873);

         for (int var6 = 0; var6 < 2; var6++) {
            this.injected[var6] = this.atlas();
            this.injectedFbo[var6] = BounceField.framebuffer(this.injected[var6]);
            this.runs[var6] = this.atlas();
            this.runsFbo[var6] = BounceField.framebuffer(this.runs[var6]);
         }

         GL11.glBindTexture(3553, var5);
         this.historyValid = false;
      }
   }

   private int atlas() {
      return Gl.texture(34842, this.atlasWidth, this.atlasHeight, 6408, 5126, 9729);
   }

   private void release() {
      for (int var1 = 0; var1 < 2; var1++) {
         if (this.injected[var1] != 0) {
            GL30.glDeleteFramebuffers(this.injectedFbo[var1]);
            GL30.glDeleteFramebuffers(this.runsFbo[var1]);
            GL11.glDeleteTextures(this.injected[var1]);
            GL11.glDeleteTextures(this.runs[var1]);
         }
      }
   }
}
