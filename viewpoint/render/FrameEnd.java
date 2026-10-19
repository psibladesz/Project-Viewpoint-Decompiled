package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GpuParts;
import viewpoint.platform.PackPass;
import viewpoint.platform.Profile;
import viewpoint.platform.Tuning;
import viewpoint.upscale.Upscaling;

final class FrameEnd {
   private final Targets targets;
   private final VolumePass volume;
   private final PostPass post;
   private final PackStages stages;
   private final TemporalPass temporal;
   private final AntiAliasPass antiAlias;
   private final Rescale rescale;
   private final WeatherPass weather;
   private final TargetOutline target;
   private final UpscalePass upscale;
   private final FrameGenPass frameGen;

   FrameEnd(Targets var1, VolumePass var2, PostPass var3, PackStages var4, TemporalPass var5, Rescale var6, WeatherPass var7, TargetOutline var8) {
      this.targets = var1;
      this.volume = var2;
      this.post = var3;
      this.stages = var4;
      this.temporal = var5;
      this.rescale = var6;
      this.weather = var7;
      this.target = var8;
      this.antiAlias = new AntiAliasPass(var1);
      VendorInputs var9 = new VendorInputs(var1);
      this.upscale = new UpscalePass(var9);
      this.frameGen = new FrameGenPass(var9);
   }

   boolean upscales(FrameContext var1) {
      return var1.pipeline.upscaler != 0 && this.upscale.runs(var1);
   }

   Upscaling upscaler(FrameContext var1, int var2, int var3) {
      return this.upscale.fit(var1.pipeline, var2, var3);
   }

   void draw(FrameContext var1) {
      if (!WorldRenderer.off(4)) {
         GlDebug.push("volumetrics");
         this.volume.draw(var1);
         GlDebug.pop();
      }

      Profile.mark(10);
      if (var1.pipeline.bloom) {
         GlDebug.push("bloom");
         this.post.bloom(var1);
         GlDebug.pop();
      }

      Profile.mark(11);
      this.post.shafts(var1);
      this.stages.run(PackPass.Stage.COMPOSITE, var1, this.targets.hdr);
      this.post.meter(var1);
      boolean var2 = !WorldRenderer.off(1);
      boolean var3 = this.stages.any(PackPass.Stage.FINAL);
      boolean var4 = this.through(var1, var2, var3);
      if (var4) {
         GL30.glBindFramebuffer(36160, this.targets.taaInputFbo);
         GL11.glViewport(0, 0, var1.width, var1.height);
      } else {
         GL30.glBindFramebuffer(36009, var1.outputDrawFbo);
         GL30.glBindFramebuffer(36008, var1.outputReadFbo);
         GL11.glViewport(var1.viewport[0], var1.viewport[1], var1.viewport[2], var1.viewport[3]);
      }

      GlDebug.push("composite");
      GpuParts.begin(15);
      this.post.composite(var1);
      GpuParts.end(15);
      GlDebug.pop();
      int var5 = this.antiAlias.over(var1, this.targets.taaInput);
      if (var1.upscaled) {
         var5 = this.upscale.run(var1, var5);
         this.temporal.remember(var1);
      } else {
         this.upscale.release();
         var5 = this.temporal.run(var1, var5, var2);
      }

      if (var4) {
         var5 = this.stages.run(PackPass.Stage.FINAL, var1, var5);
         this.weather.falling(var1, var5);
         float var6 = var2 ? (float)Math.pow(2.0, -Tuning.sharpenStops) : 0.0F;
         this.rescale.present(var1, this.temporal, var5, var6, (float)Math.pow(2.0, -Tuning.upscaleSharpenStops));
      }

      GlDebug.push("rain over");
      this.weather.overlay(var1);
      GlDebug.pop();
      GlDebug.push("target outline");
      this.target.composite(var1);
      GlDebug.pop();
      GlDebug.push("frame generation inputs");
      this.frameGen.capture(var1);
      GlDebug.pop();
   }

   private boolean through(FrameContext var1, boolean var2, boolean var3) {
      return var2 || var3 || var1.pipeline.fxaa || var1.pipeline.smaa || Rescale.scaled(var1) || this.weather.raining(var1) || var1.panini.on();
   }
}
