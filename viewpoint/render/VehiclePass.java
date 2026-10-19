package viewpoint.render;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Tuning;
import zombie.core.textures.Texture;

final class VehiclePass {
   private static final String[] SAMPLERS = new String[]{
      "uRustMap", "uZones", "uLamps", "uDamage1Overlay", "uDamage1Shell", "uDamage2Overlay", "uDamage2Shell"
   };
   private final ModelPass models;
   private final GlProgram gbuffer = GlProgram.create("vehicle", "model.vert", "vehicle_gbuffer.frag");
   private final GlProgram shadow = GlProgram.create("vehicle shadow", "model.vert", "vehicle_shadow.frag");
   private final GlProgram glass = GlProgram.create("vehicle glass", "model.vert", "vehicle_glass.frag");
   private final FloatBuffer switches = BufferUtils.createFloatBuffer(192);
   private final int blank;

   VehiclePass(ModelPass var1) {
      this.models = var1;

      for (GlProgram var5 : new GlProgram[]{this.gbuffer, this.shadow, this.glass}) {
         samplers(var5);
      }

      ModelPass.forwardSamplers(this.glass);
      int var6 = GL11.glGetInteger(32873);
      this.blank = Gl.texture(32856, 1, 1, 6408, 5121, 9728);
      GL11.glTexSubImage2D(3553, 0, 0, 0, 1, 1, 6408, 5121, BufferUtils.createByteBuffer(4));
      GL11.glBindTexture(3553, var6);
   }

   static void samplers(GlProgram var0) {
      ModelPass.samplers(var0);

      for (int var1 = 0; var1 < SAMPLERS.length; var1++) {
         var0.sampler(SAMPLERS[var1], 32 + var1);
      }
   }

   void iris(GlProgram var1, ModelDraws var2, boolean var3) {
      var1.setInt("uIrisGlass", var3 ? 1 : 0);
      var1.set("uIrisGlassAlpha", Tuning.vehicleGlass);
      var1.setInt("uIrisUntinted", 1);
      TextureFilter.MODELS.apply(var1);
      this.models.batches.draw(var2x -> var2.material[var2x] >= 0 && this.models.shown(var2, var2x), var3x -> this.material(var1, var2, var3x));
      unbind();
   }

   int gbuffer(FrameContext var1) {
      ModelDraws var2 = var1.scene.models;
      this.gbuffer.use();
      this.gbuffer.set("uFadeNoise", var1.fadeNoise());
      CameraClear.apply(this.gbuffer);
      TextureFilter.MODELS.apply(this.gbuffer);
      this.gbuffer.set("uViewProjection", var1.viewProjection);
      this.gbuffer.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
      ModelPass.views(this.gbuffer, var1);
      int var3 = this.models
         .batches
         .draw(var2x -> var2.material[var2x] >= 0 && this.models.shown(var2, var2x), var2x -> this.material(this.gbuffer, var2, var2x));
      unbind();
      return var3;
   }

   void shadow(ModelDraws var1, Matrix4f var2, int[] var3, int var4, ModelBatches.Filter var5, boolean var6) {
      this.shadow.use();
      this.shadow.set("uViewProjection", var2);
      this.shadow.setInt("uShadowPalette", 1);
      this.shadow.setInt("uGlass", var6 ? 1 : 0);
      this.models.batches.draw(var3, var4, var2x -> var1.material[var2x] >= 0 && var5.take(var2x), var2x -> this.material(this.shadow, var1, var2x));
      unbind();
   }

   void glass(FrameContext var1, ShadowPass var2) {
      ModelDraws var3 = var1.scene.models;
      this.glass.use();
      this.glass.set("uFadeNoise", var1.fadeNoise());
      CameraClear.apply(this.glass);
      this.glass.set("uViewProjection", var1.viewProjection);
      this.glass.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
      ModelPass.forwardLight(this.glass, var1, var2);
      this.glass.set("uGlassAlpha", Tuning.vehicleGlass);
      var2.bind(true);
      GL11.glDepthMask(false);
      this.models.batches.draw(var2x -> var3.material[var2x] >= 0 && this.models.shown(var3, var2x), var2x -> this.material(this.glass, var3, var2x));
      GL11.glDepthMask(true);
      var2.bind(false);
      unbind();
   }

   private void material(GlProgram var1, ModelDraws var2, int var3) {
      VehicleMaterials var4 = var2.vehicles;
      int var5 = var2.material[var3] * 197;
      this.switches.clear();
      this.switches.put(var4.values, var5, 192).flip();
      var1.setMatrices("uSwitches", this.switches);
      var1.set("uRust", var4.values[var5 + 192]);
      var1.set("uPaint", var4.values[var5 + 193], var4.values[var5 + 193 + 1], var4.values[var5 + 193 + 2]);
      var1.setInt("uKind", (int)var4.values[var5 + 196]);

      for (int var6 = 0; var6 < 7; var6++) {
         Texture var7 = var4.textures[var2.material[var3] * 7 + var6];
         GL13.glActiveTexture(34016 + var6);
         if (var7 != null && var7.isReady()) {
            Texture.lastTextureID = -1;
            var7.bind();
         } else {
            GL11.glBindTexture(3553, this.blank);
         }
      }

      this.models.surface(var3);
   }

   private static void unbind() {
      for (int var0 = 0; var0 < 7; var0++) {
         Gl.bind(32 + var0, 0);
      }

      GL13.glActiveTexture(33984);
      Texture.lastTextureID = -1;
   }
}
