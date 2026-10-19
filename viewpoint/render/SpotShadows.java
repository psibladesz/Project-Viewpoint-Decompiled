package viewpoint.render;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;
import viewpoint.platform.GlProgram;

final class SpotShadows {
   static final int SIZE = 2048;
   private static final byte CONE_MESH = 32;
   private final GlProgram program;
   private final FloatBuffer matrices = BufferUtils.createFloatBuffer(80);
   private final FloatBuffer texels = BufferUtils.createFloatBuffer(5);
   private final FloatBuffer layers = BufferUtils.createFloatBuffer(5);
   private final FloatBuffer tiles = BufferUtils.createFloatBuffer(20);
   private final Matrix4f view = new Matrix4f();
   private final Matrix4f viewProjection = new Matrix4f();
   private final Matrix4f matrix = new Matrix4f();
   private final Matrix4f[] drawn = new Matrix4f[5];
   private final double[] drawnOrigin = new double[15];
   private int texture;
   private int layerCount;
   private int[] fbos = new int[0];

   SpotShadows(GlProgram var1) {
      this.program = var1;

      for (int var2 = 0; var2 < this.drawn.length; var2++) {
         this.drawn[var2] = new Matrix4f();
      }
   }

   boolean fit(int var1) {
      if (var1 == this.layerCount) {
         return false;
      }

      this.release();
      this.layerCount = var1;
      if (var1 == 0) {
         return true;
      }

      this.texture = GL45.glCreateTextures(35866);
      GL45.glTextureParameteri(this.texture, 10241, 9729);
      GL45.glTextureParameteri(this.texture, 10240, 9729);
      GL45.glTextureParameteri(this.texture, 10242, 33071);
      GL45.glTextureParameteri(this.texture, 10243, 33071);
      GL45.glTextureParameteri(this.texture, 34892, 34894);
      GL45.glTextureParameteri(this.texture, 34893, 515);
      GL45.glTextureStorage3D(this.texture, 1, 33190, 2048, 2048, var1);
      this.fbos = new int[var1];

      for (int var2 = 0; var2 < var1; var2++) {
         this.fbos[var2] = GL45.glCreateFramebuffers();
         GL45.glNamedFramebufferTextureLayer(this.fbos[var2], 36096, this.texture, 0, var2);
         GL45.glNamedFramebufferDrawBuffer(this.fbos[var2], 0);
         GL45.glNamedFramebufferReadBuffer(this.fbos[var2], 0);
      }

      return true;
   }

   Matrix4f place(int var1, SpotShadows.MapView var2, SceneData var3) {
      float var4 = var2.lens[0];
      float var5 = var2.lens[1];
      float var6 = var2.lens[2];
      float var7 = var2.dir[0];
      float var8 = var2.dir[1];
      float var9 = var2.dir[2];
      boolean var10 = Math.abs(var8) > 0.99F;
      this.view.setLookAt(var4, var5, var6, var4 + var7, var5 + var8, var6 + var9, 0.0F, var10 ? 0.0F : 1.0F, var10 ? 1.0F : 0.0F);
      this.viewProjection.setPerspective(var2.fovY, var2.aspect, 0.05F, var2.reach + 1.0F).mul(this.view);
      float var11 = var2.side / 2.0F;
      this.matrix.translation(var2.u + var11, var2.v + var11, 0.5F).scale(var11, var11, 0.5F).mul(this.viewProjection);
      this.matrix.get(16 * var1, this.matrices);
      this.drawn[var1].set(this.matrix);
      this.drawnOrigin[3 * var1] = var3.originX;
      this.drawnOrigin[3 * var1 + 1] = var3.originY;
      this.drawnOrigin[3 * var1 + 2] = var3.originZ;
      this.texels.put(var1, var2.texel);
      this.layers.put(var1, var2.layer);
      this.tiles.put(4 * var1, var2.u).put(4 * var1 + 1, var2.v).put(4 * var1 + 2, var2.u + var2.side).put(4 * var1 + 3, var2.v + var2.side);
      return this.viewProjection;
   }

   void drawWorld(SceneData var1, SpotShadows.MapView var2) {
      markCone(var1, var2);
      GL30.glBindFramebuffer(36160, this.fbos[var2.layer]);
      int var3 = Math.round(var2.u * 2048.0F);
      int var4 = Math.round(var2.v * 2048.0F);
      int var5 = Math.round(var2.side * 2048.0F);
      GL11.glViewport(var3, var4, var5, var5);
      boolean var6 = var5 < 2048;
      if (var6) {
         GL11.glEnable(3089);
         GL11.glScissor(var3, var4, var5, var5);
      }

      GL11.glClear(256);
      if (var6) {
         GL11.glDisable(3089);
      }

      GL11.glEnable(2929);
      GL11.glDepthFunc(513);
      GL11.glEnable(32823);
      GL11.glPolygonOffset(1.5F, 3.0F);
      this.program.use();
      ShadowPass.casters(this.program);
      this.program.set("uViewProjection", this.viewProjection);
      this.program.set("uEye", var2.lens[0], var2.lens[1], var2.lens[2]);
      Meshes.draw(var1, this.program, (byte)32);
   }

   void keep(int var1, SceneData var2) {
      float var3 = (float)(var2.originX - this.drawnOrigin[3 * var1]);
      float var4 = (float)(var2.originY - this.drawnOrigin[3 * var1 + 1]);
      float var5 = (float)(var2.originZ - this.drawnOrigin[3 * var1 + 2]);
      this.matrix.set(this.drawn[var1]).translate(var3, var4, var5).get(16 * var1, this.matrices);
   }

   void end() {
      this.program.use();
      GL11.glDisable(32823);
   }

   private static void markCone(SceneData var0, SpotShadows.MapView var1) {
      float[] var2 = var1.lens;
      float[] var3 = var1.dir;
      float var4 = (float)Math.sin(var1.cone);
      float var5 = (float)Math.cos(var1.cone);
      float var6 = var1.reach;

      for (int var7 = 0; var7 < var0.meshCount; var7++) {
         var0.meshFlags[var7] = (byte)(var0.meshFlags[var7] & -33);
         float var8 = var0.meshOffsets[var7 * 3] - 4.0F - var2[0];
         float var9 = var0.meshOffsets[var7 * 3 + 1] + var0.meshes[var7].level * 2.4494896F + 1.5F - var2[1];
         float var10 = var0.meshOffsets[var7 * 3 + 2] - 4.0F - var2[2];
         float var11 = 8.0F;
         float var12 = (float)Math.sqrt(var8 * var8 + var9 * var9 + var10 * var10);
         float var13 = var8 * var3[0] + var9 * var3[1] + var10 * var3[2];
         float var14 = (float)Math.sqrt(Math.max(var12 * var12 - var13 * var13, 0.0F));
         boolean var15 = var0.meshes[var7].pages.length > 0 && var0.meshes[var7].pages[0] == null;
         if (var15 || var12 <= var6 + var11 && var13 >= -var11 && var5 * var14 - var4 * var13 <= var11) {
            var0.meshFlags[var7] = (byte)(var0.meshFlags[var7] | 32);
         }
      }
   }

   void uniforms(GlProgram var1) {
      var1.setMatrices("uSpotMaps", this.matrices);
      var1.setFloats("uSpotTexel", this.texels);
      var1.setFloats("uSpotLayer", this.layers);
      var1.setVec4s("uSpotTiles", this.tiles);
   }

   void bind(boolean var1) {
      GL13.glActiveTexture(34005);
      GL11.glBindTexture(35866, var1 ? this.texture : 0);
      GL13.glActiveTexture(33984);
   }

   int texture() {
      return this.texture;
   }

   private void release() {
      for (int var4 : this.fbos) {
         GL30.glDeleteFramebuffers(var4);
      }

      if (this.texture != 0) {
         GL11.glDeleteTextures(this.texture);
      }

      this.fbos = new int[0];
      this.texture = 0;
   }

   static final class MapView {
      int layer;
      float u;
      float v;
      float side = 1.0F;
      float fovY;
      float aspect = 1.0F;
      float cone;
      float reach;
      float texel;
      final float[] lens = new float[3];
      final float[] dir = new float[3];
   }
}
