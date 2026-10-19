package viewpoint.render;

import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.Tuning;

final class SunShafts {
   private static final int SCALE = 4;
   private static final int TAPS = 8;
   private static final int PASSES = 3;
   private static final float OFF_SCREEN = 0.6F;
   private static final float RISE = 0.05F;
   private final GlProgram mask = GlProgram.create("sun shafts mask", "screen.vert", "sun_shafts_mask.frag");
   private final GlProgram blur = GlProgram.create("sun shafts", "screen.vert", "sun_shafts.frag");
   private final Targets targets;
   private final Vector4f body = new Vector4f();
   private final int[] textures = new int[2];
   private final int[] fbos = new int[2];
   private int width;
   private int height;
   private int result;
   float strength;

   SunShafts(Targets var1) {
      this.targets = var1;
      this.mask.sampler("uColor", 0);
      this.mask.sampler("uDepth", 12);
      this.mask.sampler("uFarDepth", 29);
      this.blur.sampler("uSource", 0);
   }

   void draw(FrameContext var1) {
      this.strength = 0.0F;
      if (var1.pipeline.sunShafts) {
         SceneData var2 = var1.scene;
         boolean var3 = var2.skySunY > 0.0F;
         float var4 = var3 ? var2.skySunX : var2.skyMoonX;
         float var5 = var3 ? var2.skySunY : var2.skyMoonY;
         float var6 = var3 ? var2.skySunZ : var2.skyMoonZ;
         this.body.set(var4, var5, var6, 0.0F).mul(var1.plainViewProjection);
         if (!(this.body.w <= 0.0F)) {
            float var7 = this.body.x / this.body.w;
            float var8 = this.body.y / this.body.w;
            float var9 = Math.max(Math.abs(var7), Math.abs(var8)) - 1.0F;
            float var10 = (1.0F - smoothstep(0.0F, 0.6F, var9)) * smoothstep(0.0F, 0.05F, var5);
            if (!(var10 <= 0.0F)) {
               GpuParts.begin(27);
               this.fit(var1);
               this.drawMask(var1, var4, var5, var6);
               this.drawBlurs(var1, var7 * 0.5F + 0.5F, var8 * 0.5F + 0.5F);
               GpuParts.end(27);
               this.strength = Tuning.shaftStrength * var10;
            }
         }
      }
   }

   int texture() {
      return this.result;
   }

   private void drawMask(FrameContext var1, float var2, float var3, float var4) {
      GL30.glBindFramebuffer(36160, this.fbos[0]);
      GL11.glViewport(0, 0, this.width, this.height);
      GL11.glDisable(2929);
      GL11.glDisable(3042);
      this.mask.use();
      var1.camera(this.mask);
      this.mask.set("uFarOn", var1.farFar > 0.0F ? 1.0F : 0.0F);
      this.mask.set("uBody", var2, var3, var4);
      this.mask.set("uLobe", Tuning.shaftLobe);
      this.mask.set("uCap", Tuning.shaftCap / Math.max(EyeMeter.exposure(var1), 1.0E-6F));
      Gl.bind(12, this.targets.depth);
      Gl.bind(29, this.targets.farDepth);
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, this.targets.hdr);
      Gl.screenTriangle();
      Gl.bind(29, 0);
      Gl.bind(12, 0);
   }

   private void drawBlurs(FrameContext var1, float var2, float var3) {
      this.blur.use();
      this.blur.set("uBodyUv", var2, var3);
      this.blur.set("uDecay", Tuning.shaftDecay);
      this.blur.set("uAspect", (float)var1.width / var1.height);
      this.blur.setInt("uFrame", (int)var1.index);
      GL13.glActiveTexture(33984);
      int var4 = 0;
      float var5 = 0.125F;

      for (int var6 = 0; var6 < 3; var6++) {
         GL30.glBindFramebuffer(36160, this.fbos[1 - var4]);
         GL11.glBindTexture(3553, this.textures[var4]);
         this.blur.set("uStep", var5);
         this.blur.set("uFalloff", var6 == 2 ? Tuning.shaftFalloff : 0.0F);
         Gl.screenTriangle();
         var4 = 1 - var4;
         var5 /= 8.0F;
      }

      GL11.glBindTexture(3553, 0);
      this.result = this.textures[var4];
   }

   private void fit(FrameContext var1) {
      int var2 = (var1.width + 4 - 1) / 4;
      int var3 = (var1.height + 4 - 1) / 4;
      if (var2 != this.width || var3 != this.height) {
         this.release();
         this.width = var2;
         this.height = var3;
         int var4 = GL11.glGetInteger(32873);

         for (int var5 = 0; var5 < 2; var5++) {
            this.textures[var5] = Gl.texture(34842, this.width, this.height, 6408, 5126, 9729);
            this.fbos[var5] = BounceField.framebuffer(this.textures[var5]);
         }

         GL11.glBindTexture(3553, var4);
      }
   }

   private void release() {
      for (int var1 = 0; var1 < 2; var1++) {
         if (this.textures[var1] != 0) {
            GL30.glDeleteFramebuffers(this.fbos[var1]);
            GL11.glDeleteTextures(this.textures[var1]);
         }
      }
   }

   private static float smoothstep(float var0, float var1, float var2) {
      float var3 = Math.max(0.0F, Math.min(1.0F, (var2 - var0) / (var1 - var0)));
      return var3 * var3 * (3.0F - 2.0F * var3);
   }
}
