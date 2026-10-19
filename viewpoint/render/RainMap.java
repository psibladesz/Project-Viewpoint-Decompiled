package viewpoint.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;

final class RainMap {
   static final float RADIUS = 48.0F;
   private static final int SIZE = 2048;
   private static final float HEIGHT = 150.0F;
   private final GlProgram program;
   private final ModelPass models;
   private final int fbo;
   private final int texture;
   private final Matrix4f view = new Matrix4f();
   private final Matrix4f viewProjection = new Matrix4f();
   private final Matrix4f matrix = new Matrix4f();
   private final Matrix4f unproject = new Matrix4f();
   boolean on;

   RainMap(GlProgram var1, ModelPass var2) {
      this.program = var1;
      this.models = var2;
      this.texture = Gl.texture(33190, 2048, 2048, 6402, 5126, 9728);
      this.fbo = ShadowPass.depthFbo(this.texture);
   }

   void draw(SceneData var1) {
      this.on = WeatherPass.falling(var1);
      if (this.on) {
         GlDebug.push("rain map");
         GpuParts.begin(7);
         this.drawMap(var1);
         GpuParts.end(7);
         GlDebug.pop();
      }
   }

   private void drawMap(SceneData var1) {
      float var2 = WeatherPass.fallSpeed(var1);
      float var3 = var1.windX / var2;
      float var4 = var1.windZ / var2;
      float var5 = (float)Math.sqrt(var3 * var3 + 1.0F + var4 * var4);
      var3 /= var5;
      var4 /= var5;
      float var6 = -1.0F / var5;
      boolean var7 = Math.abs(var4) > 0.7F;
      this.view.setLookAt(-var3 * 150.0F, -var6 * 150.0F, -var4 * 150.0F, 0.0F, 0.0F, 0.0F, var7 ? 1.0F : 0.0F, 0.0F, var7 ? 0.0F : 1.0F);
      double var8 = 0.046875;
      double var10 = this.view.m00() * var1.originX + this.view.m10() * var1.originY + this.view.m20() * var1.originZ;
      double var12 = this.view.m01() * var1.originX + this.view.m11() * var1.originY + this.view.m21() * var1.originZ;
      float var14 = (float)(var10 - Math.floor(var10 / var8) * var8);
      float var15 = (float)(var12 - Math.floor(var12 / var8) * var8);
      this.viewProjection.setOrtho(-48.0F - var14, 48.0F - var14, -48.0F - var15, 48.0F - var15, 1.0F, 300.0F).mul(this.view);
      this.matrix.translation(0.5F, 0.5F, 0.5F).scale(0.5F).mul(this.viewProjection);
      this.matrix.invert(this.unproject);
      GL30.glBindFramebuffer(36160, this.fbo);
      GL11.glViewport(0, 0, 2048, 2048);
      GL11.glClear(256);
      GL11.glEnable(2929);
      GL11.glDepthFunc(513);
      this.program.use();
      ShadowPass.casters(this.program);
      this.program.set("uViewProjection", this.viewProjection);
      float var16 = 100000.0F;
      this.program.set("uEye", -var3 * var16, -var6 * var16, -var4 * var16);
      Meshes.draw(var1, this.program, (byte)64);
      this.models.rain(var1, this.viewProjection, 52.0F);
      this.program.use();
   }

   void uniforms(GlProgram var1, boolean var2) {
      Gl.bind(24, var2 ? this.texture : 0);
      if (var2) {
         var1.set("uRainMatrix", this.matrix);
         var1.set("uRainUnproject", this.unproject);
      }
   }
}
