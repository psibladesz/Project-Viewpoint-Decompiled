package viewpoint.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

final class FarCasters {
   private static final float TREE_LEVELS = 4.0F;
   private final Vector3f boxMin = new Vector3f();
   private final Vector3f boxMax = new Vector3f();
   private final FarFootprint footprint = new FarFootprint();
   private float served;
   private float runX;
   private float runZ;
   private float servedX;
   private float servedZ;

   void serve(SceneData var1, boolean var2, float var3, float var4, float var5, float var6) {
      this.servedX = var5;
      this.servedZ = var6;
      this.served = var2 && var1.sunY > 0.0F && var3 > 0.0F ? var3 + var4 : 0.0F;
      this.runX = this.served > 0.0F ? -var1.sunX / var1.sunY : 0.0F;
      this.runZ = this.served > 0.0F ? -var1.sunZ / var1.sunY : 0.0F;
   }

   boolean meets(SceneData var1, Matrix4f var2, FarGpu.BlockDraw var3) {
      float var4 = var3.originX - var3.blockX;
      float var5 = var3.originZ - var3.blockY;
      float var6 = (var3.top - var3.reliefLow) * 2.4494896F;
      return this.casts(var2, var4 - 64.0F, var5 - 64.0F, var4, var5, var3.bottom(), var6)
         && this.footprint.block(var1, var3)
         && this.casts(var2, this.footprint, var3.bottom(), var6);
   }

   boolean meets(SceneData var1, Matrix4f var2, FarGpu.CellDraw var3) {
      float var4 = (var3.maxHeight + 4.0F - var3.reliefLow) * 2.4494896F;
      return this.casts(var2, var3.originX - 256.0F, var3.originZ - 256.0F, var3.originX, var3.originZ, var3.bottom(), var4)
         && this.footprint.cell(var1, var3)
         && this.casts(var2, this.footprint, var3.bottom(), var4);
   }

   private boolean casts(Matrix4f var1, FarFootprint var2, float var3, float var4) {
      return this.casts(var1, var2.x0, var2.z0, var2.x1, var2.z1, var3, var4);
   }

   private boolean casts(Matrix4f var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      float var8 = var2 + this.servedX;
      float var9 = var3 + this.servedZ;
      float var10 = var4 + this.servedX;
      float var11 = var5 + this.servedZ;
      return FarFootprint.reaches(this.served, this.runX, this.runZ, var8, var9, var10, var11, var7)
         && this.meets(var1, var2, var6, var3, var4, var6 + var7, var5);
   }

   private boolean meets(Matrix4f var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      var1.transformAab(var2, var3, var4, var5, var6, var7, this.boxMin, this.boxMax);
      return this.boxMax.x >= -1.0F && this.boxMin.x <= 1.0F && this.boxMax.y >= -1.0F && this.boxMin.y <= 1.0F && this.boxMin.z <= 1.0F;
   }
}
