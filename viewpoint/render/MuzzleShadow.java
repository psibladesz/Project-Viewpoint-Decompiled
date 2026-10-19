package viewpoint.render;

import java.util.Arrays;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;

final class MuzzleShadow {
   private static final int SIZE = 512;
   private static final float NEAR = 0.6F;
   private static final float MESH_RADIUS = 8.0F;
   private static final byte ANY_MESH = 65;
   private final GlProgram program = GlProgram.create("muzzle shadow", "shadow.vert", "shadow.frag");
   private final ModelPass models;
   private final int texture;
   private final int fbo;
   private final FrustumIntersection frustum = new FrustumIntersection();
   private final Matrix4f projection = new Matrix4f();
   private final Matrix4f faceViewProjection = new Matrix4f();
   private boolean on;
   private boolean[] chosen = new boolean[512];
   private int[] faceModels = new int[16];
   private float far;

   MuzzleShadow(ModelPass var1) {
      this.models = var1;
      this.program.sampler("uTexture", 0);
      this.texture = GL45.glCreateTextures(35866);
      GL45.glTextureParameteri(this.texture, 10241, 9729);
      GL45.glTextureParameteri(this.texture, 10240, 9729);
      GL45.glTextureParameteri(this.texture, 10242, 33071);
      GL45.glTextureParameteri(this.texture, 10243, 33071);
      GL45.glTextureParameteri(this.texture, 34892, 34894);
      GL45.glTextureParameteri(this.texture, 34893, 515);
      GL45.glTextureStorage3D(this.texture, 1, 33189, 512, 512, 6);
      this.fbo = GL45.glCreateFramebuffers();
      GL45.glNamedFramebufferDrawBuffer(this.fbo, 0);
      GL45.glNamedFramebufferReadBuffer(this.fbo, 0);
   }

   static void sampler(GlProgram var0) {
      var0.sampler("uMuzzleShadow", 139);
   }

   void draw(FrameContext var1) {
      SceneData var2 = var1.scene;
      this.on = var2.muzzleShadow && var2.torchCount > 0 && var1.pipeline.flashlightShadow;
      if (this.on) {
         float var3 = var2.torches[0];
         float var4 = var2.torches[1];
         float var5 = var2.torches[2];
         float var6 = var2.torches[3];
         this.far = var6 + 1.0F;
         GlDebug.push("muzzle shadow");
         GpuParts.begin(45);
         this.projection.setPerspective((float)Math.toRadians(90.0), 1.0F, 0.6F, this.far);
         this.chosen = this.chosen.length < var2.meshCount ? new boolean[var2.meshCount * 2] : this.chosen;
         GL11.glEnable(2929);
         GL11.glDepthFunc(513);
         GL11.glDepthMask(true);
         GL11.glEnable(32823);
         GL11.glPolygonOffset(1.5F, 3.0F);

         for (int var7 = 0; var7 < 6; var7++) {
            float[] var8 = LampShadows.FORWARD[var7];
            float[] var9 = LampShadows.UP[var7];
            this.faceViewProjection.set(this.projection).lookAt(var3, var4, var5, var3 + var8[0], var4 + var8[1], var5 + var8[2], var9[0], var9[1], var9[2]);
            this.frustum.set(this.faceViewProjection);
            this.face(var2, var7, var3, var4, var5, var6);
         }

         GL11.glDisable(32823);
         GpuParts.end(45);
         GlDebug.pop();
      }
   }

   private void face(SceneData var1, int var2, float var3, float var4, float var5, float var6) {
      GL45.glNamedFramebufferTextureLayer(this.fbo, 36096, this.texture, 0, var2);
      GL30.glBindFramebuffer(36160, this.fbo);
      GL11.glViewport(0, 0, 512, 512);
      GL11.glClear(256);
      this.chooseMeshes(var1, var3, var4, var5, var6);
      this.program.use();
      ShadowPass.casters(this.program);
      this.program.set("uViewProjection", this.faceViewProjection);
      this.program.set("uEye", var3, var4, var5);
      this.program.set("uOwnFixture", 0.0F);
      Meshes.draw(var1, this.program, (byte)65, this.chosen);
      int var7 = this.chooseModels(var1, var3, var4, var5, var6);
      if (var7 > 0) {
         this.models.shadow(var1, this.faceViewProjection, this.faceModels, var7);
      }
   }

   private void chooseMeshes(SceneData var1, float var2, float var3, float var4, float var5) {
      for (int var6 = 0; var6 < var1.meshCount; var6++) {
         float var7 = var1.meshOffsets[var6 * 3] - 4.0F;
         float var8 = var1.meshOffsets[var6 * 3 + 2] - 4.0F;
         float var9 = var1.meshOffsets[var6 * 3 + 1] + var1.meshes[var6].level * 2.4494896F + 1.5F;
         float var10 = var7 - var2;
         float var11 = var9 - var3;
         float var12 = var8 - var4;
         float var13 = var5 + 8.0F;
         boolean var14 = var1.meshes[var6].pages.length > 0 && var1.meshes[var6].pages[0] == null;
         this.chosen[var6] = var14 || var10 * var10 + var11 * var11 + var12 * var12 < var13 * var13 && this.frustum.testSphere(var7, var9, var8, 8.0F);
      }
   }

   private int chooseModels(SceneData var1, float var2, float var3, float var4, float var5) {
      ModelDraws var6 = var1.models;
      int var7 = 0;

      for (int var8 = 0; var8 < var6.count; var8++) {
         int var9 = var8 * 68 + 24;
         float var10 = var6.values[var9];
         float var11 = var6.values[var9 + 1];
         float var12 = var6.values[var9 + 2];
         float var13 = var6.values[var9 + 3];
         float var14 = var10 - var2;
         float var15 = var11 - var3;
         float var16 = var12 - var4;
         float var17 = var5 + var13;
         if ((var6.flags[var8] & 2) != 0
            && var14 * var14 + var15 * var15 + var16 * var16 < var17 * var17
            && this.frustum.testSphere(var10, var11, var12, var13)) {
            this.faceModels = var7 == this.faceModels.length ? Arrays.copyOf(this.faceModels, var7 * 2) : this.faceModels;
            this.faceModels[var7++] = var8;
         }
      }

      return var7;
   }

   void bind(boolean var1) {
      GL13.glActiveTexture(34123);
      GL11.glBindTexture(35866, var1 && this.on ? this.texture : 0);
      GL13.glActiveTexture(33984);
   }

   void uniforms(GlProgram var1) {
      if (var1.has("uMuzzleShadowOn")) {
         var1.set("uMuzzleShadowOn", this.on ? 1.0F : 0.0F);
         var1.set("uMuzzleMaps", 0.6F, this.far, 512.0F);
      }
   }
}
