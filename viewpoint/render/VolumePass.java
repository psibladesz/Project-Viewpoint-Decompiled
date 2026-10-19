package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.Tuning;

final class VolumePass {
   private final GlProgram volume = GlProgram.create("volume", "screen.vert", "volume.frag");
   private final Targets targets;
   private final ShadowPass shadows;
   private final BlurPass blur;
   private final FroxelPass froxels;

   VolumePass(Targets var1, ShadowPass var2, BlurPass var3) {
      this.targets = var1;
      this.shadows = var2;
      this.blur = var3;
      this.froxels = new FroxelPass(var1, var2);
      this.volume.sampler("uDepth", 12);
      this.volume.sampler("uNoise", 18);
      this.volume.sampler("uExposure", 8);
      this.volume.sampler("uFarDepth", 29);
      ShadowPass.samplers(this.volume);
      Spots.sampler(this.volume);
   }

   void draw(FrameContext var1) {
      GpuParts.begin(26);
      if (var1.pipeline.froxels) {
         this.froxels.draw(var1);
      } else {
         this.march(var1);
         this.blur.run(var1, this.targets.depth, this.targets.volume, this.targets.volumeFbo, this.targets.volumeSpare, this.targets.volumeSpareFbo);
      }

      GpuParts.end(26);
      GL11.glBindTexture(3553, 0);
      Gl.bind(12, 0);
      GL13.glActiveTexture(33984);
   }

   static void air(GlProgram var0, FrameContext var1, ShadowPass var2) {
      SceneData var3 = var1.scene;
      boolean var4 = WorldRenderer.off(4);
      var1.camera(var0);
      var2.sunUniforms(var0, var1);
      var0.set("uFloorY", var1.floorY());
      var0.set("uVolumeSun", var4 ? 0.0F : Tuning.volumeSun);
      var0.set("uGlowDir", var3.skySunX, var3.skySunY, var3.skySunZ);
      var0.set("uDensity", Tuning.airDensity + Tuning.mistDensity * var3.mist + Tuning.rainDensity * var3.rain);
      var0.set("uClearDensity", Tuning.airDensity);
      var0.set("uDust", Tuning.dust);
      var0.set("uDustSun", Tuning.dustSun);
      var0.set("uLampHalo", !var4 && !WorldRenderer.off(3) ? Tuning.lampHalo : 0.0F);
      var0.set("uTime", var3.time);
      var0.set("uNoiseOrigin", Gl.wrap(var3.originX, 48.0), Gl.wrap(var3.originY, 48.0), Gl.wrap(var3.originZ, 48.0));
      SurfacePass.lamps(var0, var3);
      var2.spots.uniforms(var0);
      var0.set("uSpotAirOn", var4 ? 0.0F : 1.0F);
   }

   private void march(FrameContext var1) {
      SceneData var2 = var1.scene;
      GL30.glBindFramebuffer(36160, this.targets.volumeFbo);
      GL11.glViewport(0, 0, var1.halfWidth, var1.halfHeight);
      GL11.glDisable(2929);
      GL11.glDisable(3042);
      GL11.glDepthMask(false);
      this.volume.use();
      air(this.volume, var1, this.shadows);
      this.volume.set("uFogColor", var2.fogR, var2.fogG, var2.fogB);
      this.volume.set("uFogRange", var2.fogStart, var2.nearEnd());
      this.volume.set("uFarOn", var1.farFar > 0.0F ? 1.0F : 0.0F);
      this.volume.set("uAmbientHaze", Tuning.ambientHaze);
      this.volume.set("uFrameNoise", var1.noiseIndex());
      this.volume.set("uWind", var2.windX, var2.windZ);
      this.volume.set("uExposureA", var2.exposureAX, var2.exposureAY);
      this.volume.set("uExposureSize", 64.0F);
      this.targets.uploadGrids(var2, var1.index);
      Gl.bind(8, this.targets.exposure);
      GL13.glActiveTexture(34002);
      GL11.glBindTexture(32879, this.targets.noise);
      Gl.bind(12, this.targets.depth);
      Gl.bind(29, this.targets.farDepth);
      this.shadows.bind(true);
      GL13.glActiveTexture(33984);
      Gl.screenTriangle();
      Gl.bind(12, 0);
      Gl.bind(29, 0);
      Gl.bind(8, 0);
      GL13.glActiveTexture(34002);
      GL11.glBindTexture(32879, 0);
      this.shadows.bind(false);
   }
}
