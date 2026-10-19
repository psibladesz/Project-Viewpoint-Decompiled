package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Tuning;

final class SpotCoronas {
   private static final int VERTICES = 6;
   private final GlProgram program = GlProgram.create("spot coronas", "spot_corona.vert", "spot_corona.frag");

   SpotCoronas() {
      Spots.sampler(this.program);
      this.program.sampler("uDepth", 12);
   }

   void draw(FrameContext var1, Spots var2, int var3, int var4) {
      if (var3 != 0 && !(Tuning.coronaStrength <= 0.0F)) {
         Gl.resetState();
         GL30.glBindFramebuffer(36009, var1.outputDrawFbo);
         GL30.glBindFramebuffer(36008, var1.outputReadFbo);
         GL11.glViewport(var1.viewport[0], var1.viewport[1], var1.viewport[2], var1.viewport[3]);
         GL11.glDisable(2929);
         GL11.glDepthMask(false);
         GL11.glEnable(3042);
         GL11.glBlendFunc(1, 1);
         this.program.use();
         var2.uniforms(this.program);
         this.program.set("uViewProjection", var1.plainViewProjection);
         this.program.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
         this.program.set("uPixel", 2.0F / var1.viewport[2], 2.0F / var1.viewport[3]);
         this.program.set("uFocal", var1.scene.projection.m11() * 0.5F * var1.viewport[3]);
         this.program.set("uCoronaScale", 1.0F / Tuning.headlampIntensity, 1.0F / Tuning.tailIntensity);
         this.program.set("uCorona", Tuning.coronaStrength, Tuning.coronaSize / Tuning.lampGlass);
         this.program.set("uCoronaDepth", var4 == 0 ? 0.0F : 1.0F);
         var2.bind(true);
         Gl.bind(12, var4);
         Gl.generated(var3 * 6);
         Gl.bind(12, 0);
         var2.bind(false);
         GL11.glBlendFunc(770, 771);
         GL11.glDisable(3042);
         GL11.glDepthMask(true);
      }
   }
}
