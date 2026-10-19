package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Tuning;
import zombie.core.textures.Texture;

final class PostPass {
   private final GlProgram bloom = GlProgram.create("bloom", "screen.vert", "bloom.frag");
   private final GlProgram post = GlProgram.create("post", "screen.vert", "post.frag");
   private final ReliefView relief = new ReliefView();
   private final Targets targets;
   private final SunShafts shafts;

   PostPass(Targets var1) {
      this.targets = var1;
      this.shafts = new SunShafts(var1);
      this.post.sampler("uColor", 0);
      this.post.sampler("uSunShafts", 116);
      this.post.sampler("uDepth", 12);
      this.post.sampler("uVolume", 15);
      this.post.sampler("uBloom", 16);
      this.bloom.sampler("uColor", 0);
   }

   void bloom(FrameContext var1) {
      this.bloom.use();
      GL11.glDisable(2929);
      GL11.glDisable(3042);
      GL13.glActiveTexture(33984);
      int var2 = this.bloom.location("uTexel");
      int var3 = this.bloom.location("uMode");

      for (int var4 = 0; var4 < 5; var4++) {
         GL30.glBindFramebuffer(36160, this.targets.bloomFbo[var4]);
         GL11.glViewport(0, 0, this.targets.bloomWidth[var4], this.targets.bloomHeight[var4]);
         GL11.glBindTexture(3553, var4 == 0 ? this.targets.hdr : this.targets.bloom[var4 - 1]);
         GL20.glUniform2f(
            var2, 1.0F / (var4 == 0 ? var1.width : this.targets.bloomWidth[var4 - 1]), 1.0F / (var4 == 0 ? var1.height : this.targets.bloomHeight[var4 - 1])
         );
         GL20.glUniform1i(var3, var4 == 0 ? 0 : 1);
         Gl.screenTriangle();
      }

      GL11.glEnable(3042);
      GL11.glBlendFunc(1, 1);

      for (int var5 = 3; var5 >= 0; var5--) {
         GL30.glBindFramebuffer(36160, this.targets.bloomFbo[var5]);
         GL11.glViewport(0, 0, this.targets.bloomWidth[var5], this.targets.bloomHeight[var5]);
         GL11.glBindTexture(3553, this.targets.bloom[var5 + 1]);
         GL20.glUniform2f(var2, 1.0F / this.targets.bloomWidth[var5 + 1], 1.0F / this.targets.bloomHeight[var5 + 1]);
         GL20.glUniform1i(var3, 2);
         Gl.screenTriangle();
      }

      GL11.glDisable(3042);
      GL11.glBindTexture(3553, 0);
      Texture.lastTextureID = -1;
   }

   void shafts(FrameContext var1) {
      GlDebug.push("sun shafts");
      this.shafts.draw(var1);
      GlDebug.pop();
   }

   void meter(FrameContext var1) {
      if (!var1.pipeline.eye) {
         EyeMeter.off();
      } else {
         if (!this.post.has("uMeter")) {
            throw new IllegalStateException("pipeline.eye needs a post.frag that declares uMeter (lib/eye_meter.glsl)");
         }

         EyeMeter.collect();
         if (WorldRenderer.debugView == 0 && EyeMeter.begin()) {
            this.inputs(var1);
            this.post.setInt("uMeter", 1);
            Gl.screenTriangle();
            this.post.setInt("uMeter", 0);
            EyeMeter.read();
            this.release();
         }

         EyeMeter.adapt(var1);
      }
   }

   void composite(FrameContext var1) {
      this.inputs(var1);
      Gl.screenTriangle();
      this.relief.draw(var1);
      this.release();
   }

   private void inputs(FrameContext var1) {
      SceneData var2 = var1.scene;
      GL11.glDisable(2929);
      GL11.glDisable(3042);
      GL11.glDisable(2884);
      GL11.glDepthMask(false);
      this.post.use();
      var1.camera(this.post);
      this.post.set("uDesaturation", var2.desaturation);
      this.post.setInt("uDebugView", WorldRenderer.debugView);
      this.post.set("uNight", var2.night);
      this.post.set("uFogColor", var2.fogR, var2.fogG, var2.fogB);
      this.post.set("uFogRange", var2.fogStart, var2.fogEnd);
      SkyPass.fogUniforms(this.post, var2);
      this.post.set("uHaze", Tuning.farHaze, var2.nearEnd());
      this.post.set("uVolumeOn", WorldRenderer.off(4) ? 0.0F : 1.0F);
      this.post.set("uBloomStrength", var1.pipeline.bloom ? Tuning.bloomStrength : 0.0F);
      this.post.set("uExposure", EyeMeter.exposure(var1));
      Gl.bind(15, this.targets.volume);
      Gl.bind(16, this.targets.bloom[0]);
      this.post.set("uShaftStrength", this.shafts.strength);
      Gl.bind(116, this.shafts.strength > 0.0F ? this.shafts.texture() : 0);
      this.post.set("uHalfSize", var1.halfWidth, var1.halfHeight);
      Gl.bind(12, this.targets.depth);
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, this.targets.hdr);
   }

   private void release() {
      Gl.bind(12, 0);
      Gl.bind(15, 0);
      Gl.bind(16, 0);
      Gl.bind(116, 0);
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, 0);
   }
}
