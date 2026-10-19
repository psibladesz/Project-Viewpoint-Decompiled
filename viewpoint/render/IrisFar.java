package viewpoint.render;

import java.util.Map;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Tuning;

final class IrisFar {
   private static final float NEAR_DITHER = 16.0F;
   private final FrustumIntersection frustum = new FrustumIntersection();
   private final Matrix4f viewProjection = new Matrix4f();

   void terrain(IrisPipeline var1, Map<String, float[]> var2, FrameContext var3, FarPass var4, IrisGeometry var5) {
      IrisProgram var6 = var1.programs.draws.get(IrisPrograms.Draw.DH_TERRAIN);
      SceneData var7 = var3.scene;
      if (var6 != null && var7.farCount != 0) {
         this.viewProjection.set(var1.uniforms.farProjection);
         this.frustum.set(this.viewProjection.mul(WorldRenderer.cullFromPlayer ? WorldRenderer.cullView : var3.view));
         var5.begin(var1, var6, var2, var1.uniforms.modelView, var1.uniforms.farProjection, "NONE", var3, false);
         common(var1, var6, var3, var4);
         GL11.glEnable(2929);
         GL11.glDepthFunc(513);
         GL11.glDepthMask(true);
         GL11.glEnable(2884);
         GL11.glCullFace(1029);
         this.boxes(var6.gl, var3, 0.0F);
         GL11.glDisable(2884);
         var1.bindings.unbind(var6);
         this.shells(var1, var2, var3, var4, var5);
         IrisProgram var8 = var1.programs.draws.get(IrisPrograms.Draw.DH_TREES);
         if (var8 != null && TreeKinds.bind()) {
            var5.begin(var1, var8, var2, var1.uniforms.modelView, var1.uniforms.farProjection, "NONE", var3, false);
            common(var1, var8, var3, var4);
            TreeKinds.uniforms(var8.gl);
            this.trees(var8.gl, var3);
            TreeKinds.unbind();
            var1.bindings.unbind(var8);
         }

         GL30.glBindVertexArray(0);
         Gl.bind(27, 0);
         Gl.bind(28, 0);
         Gl.bind(25, 0);
         Gl.bind(26, 0);
         FarRelief.unbind();
         GL13.glActiveTexture(33984);
      }
   }

   void shadow(IrisPipeline var1, Map<String, float[]> var2, FrameContext var3, FarPass var4, IrisGeometry var5) {
      IrisProgram var6 = var1.programs.draws.get(IrisPrograms.Draw.DH_SHADOW);
      if (var6 != null && var3.scene.farCount != 0) {
         var5.begin(var1, var6, var2, var1.uniforms.shadowView, var1.uniforms.shadowProjection, "NONE", var3, true);
         common(var1, var6, var3, var4);
         Gl.patches(true);
         this.boxes(var6.gl, var3, var1.shadowDistance);
         Gl.patches(false);
         GL30.glBindVertexArray(0);
         var1.bindings.unbind(var6);
         Gl.bind(27, 0);
         FarRelief.unbind();
      }
   }

   private void shells(IrisPipeline var1, Map<String, float[]> var2, FrameContext var3, FarPass var4, IrisGeometry var5) {
      IrisProgram var6 = var1.programs.draws.get(IrisPrograms.Draw.DH_SHELL);
      SceneData var7 = var3.scene;
      if (var6 != null && var7.shellCount != 0) {
         int var8 = GL11.glGetInteger(32873);
         var5.begin(var1, var6, var2, var1.uniforms.modelView, var1.uniforms.farProjection, "NONE", var3, false);
         common(var1, var6, var3, var4);
         var6.gl.set("uEye", var3.eyeX, var3.eyeY, var3.eyeZ);
         var6.gl.setInt("uIrisShadow", 0);
         var6.gl.set("uPullScale", 0.05F / var1.uniforms.farNear);
         FloorFilter.apply(var6.gl);
         TextureFilter.SPRITES.apply(var6.gl);
         var4.trees.bind(true);
         GL13.glActiveTexture(33984);
         ShellArena.begin();

         for (int var9 = 0; var9 < var7.shellCount; var9++) {
            FarGpu.BlockDraw var10 = var7.shells[var9];
            if (var10.shell != null && this.blockInView(var10)) {
               FarPass.command(var10);
            }
         }

         ShellArena.draw(var6.gl);
         var4.trees.bind(false);
         GL13.glActiveTexture(33984);
         GL11.glBindTexture(3553, var8);
         var1.bindings.unbind(var6);
      }
   }

   private boolean blockInView(FarGpu.BlockDraw var1) {
      float var2 = var1.originX - var1.blockX;
      float var3 = var2 - 64.0F;
      float var4 = var1.originZ - var1.blockY;
      float var5 = var4 - 64.0F;
      return this.frustum.testAab(var3, var1.bottom(), var5, var2, var1.originY + var1.top * 2.4494896F, var4);
   }

   private static void common(IrisPipeline var0, IrisProgram var1, FrameContext var2, FarPass var3) {
      SceneData var4 = var2.scene;
      GlProgram var5 = var1.gl;
      if (WorldRenderer.freeCamera) {
         var1.set("far", var0.uniforms.farNear);
      }

      var5.set("uIrisEye", var2.eyeX, var2.eyeY, var2.eyeZ);
      var5.set("uIrisNearKept", var0.uniforms.far - 16.0F);
      var5.set("uMaskGrid", var4.farMaskX, var4.farMaskY, var4.farMaskSize);
      var5.set("uShellGrid", var4.shellMaskX, var4.shellMaskY, 53.0F);
      var5.set("uFadeNoise", var2.fadeNoise());
      Gl.bind(27, var3.mask);
      Gl.bind(28, var3.shellMask);
   }

   private void boxes(GlProgram var1, FrameContext var2, float var3) {
      SceneData var4 = var2.scene;

      for (int var5 = 0; var5 < var4.farCount; var5++) {
         FarGpu.CellDraw var6 = var4.far[var5];
         float var7 = var6.originY + var6.maxHeight * 2.4494896F;
         float var8 = var6.originX - 256.0F;
         float var9 = var6.originZ - 256.0F;
         boolean var10 = var3 > 0.0F
            ? nearest(var6, var2.eyeX, var2.eyeZ) < var3
            : this.frustum.testAab(var8, var6.bottom(), var9, var6.originX, var7, var6.originZ);
         if (var10) {
            FarGpu.textures(var6.colours);
            Gl.bind(25, var6.colours.topTexture);
            Gl.bind(26, var6.colours.sideTexture);
            var1.set("uOrigin", var6.originX, var6.originY, var6.originZ);
            var1.set("uCellWorld", var6.worldX, var6.worldY);
            var1.set("uFade", var6.fadeLo, var6.fadeHi);
            FarGpu.draw(var6.mesh);
         }
      }
   }

   private void trees(GlProgram var1, FrameContext var2) {
      SceneData var3 = var2.scene;
      float var4 = FarPass.TREES_GONE.get() * 64.0F;
      var1.set("uTreeFade", var4, var4);
      var1.set("uTreeVariation", Tuning.farTreeVariation, Tuning.farTreeRelief);
      var1.set("uBareCrown", Tuning.farBareCrown);
      var1.set("uPixel", 2.0F / (var2.scene.projection.m11() * var2.height));

      for (int var5 = 0; var5 < var3.farCount; var5++) {
         FarGpu.CellDraw var6 = var3.far[var5];
         float var7 = var6.originY + var6.maxHeight * 2.4494896F;
         float var8 = var6.originX - 256.0F;
         float var9 = var6.originZ - 256.0F;
         if (var6.trees != null
            && var6.trees.count != 0
            && !(nearest(var6, var2.eyeX, var2.eyeZ) >= var4)
            && this.frustum.testAab(var8, var6.bottom(), var9, var6.originX, var7, var6.originZ)) {
            int var10 = var6.trees.kept(var6.treeKeep);
            if (var10 != 0) {
               var1.set("uGrow", var6.treeGrow);
               var1.set("uOrigin", var6.originX, var6.originY, var6.originZ);
               var1.set("uCellWorld", var6.worldX, var6.worldY);
               var1.set("uFade", var6.fadeLo, var6.fadeHi);
               FarGpu.bindTrees(var6.trees);
               Gl.generated(var10 * 6);
            }
         }
      }

      GL13.glActiveTexture(34027);
      GL11.glBindTexture(35882, 0);
      GL13.glActiveTexture(33984);
   }

   private static float nearest(FarGpu.CellDraw var0, float var1, float var2) {
      float var3 = Math.max(0.0F, Math.max(var0.originX - 256.0F - var1, var1 - var0.originX));
      float var4 = Math.max(0.0F, Math.max(var0.originZ - 256.0F - var2, var2 - var0.originZ));
      return (float)Math.sqrt(var3 * var3 + var4 * var4);
   }
}
