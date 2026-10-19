package viewpoint.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import viewpoint.platform.Gl;

final class CascadeLayer {
   private int texture;
   private int fbo;
   private int side;
   private boolean valid;
   private int size;
   private int margin;
   private float sunX;
   private float sunY;
   private float sunZ;
   private float sunDistance;
   private float radius;
   private double originX;
   private double originY;
   private double originZ;
   private final Matrix4f view = new Matrix4f();
   private final Matrix4f drawnWith = new Matrix4f();
   private long cellX;
   private long cellY;
   private float fractionX;
   private float fractionY;
   int windowX;
   int windowY;

   static int margin(int var0) {
      return var0 / 8;
   }

   static double marginSquares(int var0, float var1) {
      return margin(var0) * 2.0 * var1 / var0;
   }

   boolean holds(SceneData var1, int var2, float var3, float var4) {
      if (this.valid
         && var2 == this.size
         && var3 == this.radius
         && var4 == this.sunDistance
         && var1.sunX == this.sunX
         && var1.sunY == this.sunY
         && var1.sunZ == this.sunZ) {
         double var5 = 2.0 * var3 / var2;
         double var7 = this.margin * var5;
         double var9 = var1.originX - this.originX;
         double var11 = ShadowPass.originY(var1) - this.originY;
         double var13 = var1.originZ - this.originZ;
         if (var9 * var9 + var11 * var11 + var13 * var13 > var7 * var7) {
            return false;
         }

         this.windowX = (int)(cell(this.view.m00(), this.view.m10(), this.view.m20(), var1, var5) - this.cellX) + this.margin;
         this.windowY = (int)(cell(this.view.m01(), this.view.m11(), this.view.m21(), var1, var5) - this.cellY) + this.margin;
         return this.windowX >= 0 && this.windowY >= 0 && this.windowX <= 2 * this.margin && this.windowY <= 2 * this.margin;
      } else {
         return false;
      }
   }

   void aim(SceneData var1, int var2, float var3, Matrix4f var4, float var5, Matrix4f var6) {
      this.size = var2;
      this.radius = var3;
      this.sunDistance = var5;
      this.margin = margin(var2);
      this.sunX = var1.sunX;
      this.sunY = var1.sunY;
      this.sunZ = var1.sunZ;
      this.originX = var1.originX;
      this.originY = ShadowPass.originY(var1);
      this.originZ = var1.originZ;
      this.view.set(var4);
      double var7 = 2.0 * var3 / var2;
      this.cellX = cell(this.view.m00(), this.view.m10(), this.view.m20(), var1, var7);
      this.cellY = cell(this.view.m01(), this.view.m11(), this.view.m21(), var1, var7);
      this.fractionX = (float)(light(this.view.m00(), this.view.m10(), this.view.m20(), var1) - this.cellX * var7);
      this.fractionY = (float)(light(this.view.m01(), this.view.m11(), this.view.m21(), var1) - this.cellY * var7);
      float var9 = var3 + (float)(this.margin * var7);
      var6.setOrtho(-var9 - this.fractionX, var9 - this.fractionX, -var9 - this.fractionY, var9 - this.fractionY, 1.0F, var5 * 2.0F).mul(this.view);
      this.drawnWith.set(var6);
      this.windowX = this.margin;
      this.windowY = this.margin;
      this.valid = true;
   }

   void cascade(SceneData var1, Matrix4f var2) {
      float var3 = 2.0F * this.radius / this.size;
      float var4 = -this.radius - this.fractionX + (this.windowX - this.margin) * var3;
      float var5 = -this.radius - this.fractionY + (this.windowY - this.margin) * var3;
      var2.setOrtho(var4, var4 + 2.0F * this.radius, var5, var5 + 2.0F * this.radius, 1.0F, this.sunDistance * 2.0F)
         .mul(this.view)
         .translate((float)(var1.originX - this.originX), (float)(ShadowPass.originY(var1) - this.originY), (float)(var1.originZ - this.originZ));
   }

   Matrix4f drawnWith(SceneData var1, Matrix4f var2) {
      return var2.set(this.drawnWith)
         .translate((float)(var1.originX - this.originX), (float)(ShadowPass.originY(var1) - this.originY), (float)(var1.originZ - this.originZ));
   }

   float movedX(SceneData var1) {
      return (float)(var1.originX - this.originX);
   }

   float movedZ(SceneData var1) {
      return (float)(var1.originZ - this.originZ);
   }

   private static double light(float var0, float var1, float var2, SceneData var3) {
      return var0 * var3.originX + var1 * ShadowPass.originY(var3) + var2 * var3.originZ;
   }

   private static long cell(float var0, float var1, float var2, SceneData var3, double var4) {
      return (long)Math.floor(light(var0, var1, var2, var3) / var4);
   }

   void begin() {
      int var1 = this.size + 2 * this.margin;
      if (this.texture == 0 || this.side != var1) {
         this.free();
         int var2 = GL11.glGetInteger(32873);
         this.texture = Gl.texture(33190, var1, var1, 6402, 5126, 9728);
         this.fbo = ShadowPass.depthFbo(this.texture);
         this.side = var1;
         GL11.glBindTexture(3553, var2);
      }

      GL30.glBindFramebuffer(36160, this.fbo);
      GL11.glViewport(0, 0, this.side, this.side);
      GL11.glClear(256);
   }

   void copyTo(int var1) {
      GL43.glCopyImageSubData(this.texture, 3553, 0, this.windowX, this.windowY, 0, var1, 3553, 0, 0, 0, 0, this.size, this.size, 1);
   }

   void release() {
      this.free();
      this.valid = false;
   }

   private void free() {
      if (this.texture != 0) {
         GL30.glDeleteFramebuffers(this.fbo);
         GL11.glDeleteTextures(this.texture);
         this.texture = 0;
         this.fbo = 0;
         this.side = 0;
      }
   }
}
