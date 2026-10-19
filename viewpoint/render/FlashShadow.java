package viewpoint.render;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import viewpoint.platform.Tuning;

final class FlashShadow {
   private static final float[] COLOR = new float[]{1.0F, 0.8235294F, 0.7058824F};
   private static final float LENS = 0.03F;
   private final SpotShadows maps;
   private final ModelPass models;
   private final SpotShadows.MapView map = new SpotShadows.MapView();
   private final Matrix4f viewProjection = new Matrix4f();
   boolean on;

   FlashShadow(SpotShadows var1, ModelPass var2) {
      this.maps = var1;
      this.models = var2;
   }

   boolean aim(FrameContext var1) {
      this.on = var1.scene.spots.flashOn && !WorldRenderer.off(7);
      if (!this.on) {
         return false;
      }

      SceneData var2 = var1.scene;
      boolean var3 = WorldRenderer.handFromPlayer;
      Matrix4f var4 = var3 ? WorldRenderer.handView : var1.view;
      float var5 = var3 ? -(var4.m00() * var4.m30() + var4.m01() * var4.m31() + var4.m02() * var4.m32()) : var1.eyeX;
      float var6 = var3 ? -(var4.m10() * var4.m30() + var4.m11() * var4.m31() + var4.m12() * var4.m32()) : var1.eyeY;
      float var7 = var3 ? -(var4.m20() * var4.m30() + var4.m21() * var4.m31() + var4.m22() * var4.m32()) : var1.eyeZ;
      float var8 = -var4.m02();
      float var9 = -var4.m12();
      float var10 = -var4.m22();
      boolean var11 = var3 && var2.spots.flashAtItem;
      float[] var12 = this.map.lens;
      float[] var13 = this.map.dir;
      var12[0] = var11 ? var2.spots.flashItemX : var5 + var4.m00() * Tuning.handRight - var4.m01() * Tuning.handDown + var8 * Tuning.handForward;
      var12[1] = var11 ? var2.spots.flashItemY : var6 + var4.m10() * Tuning.handRight - var4.m11() * Tuning.handDown + var9 * Tuning.handForward;
      var12[2] = var11 ? var2.spots.flashItemZ : var7 + var4.m20() * Tuning.handRight - var4.m21() * Tuning.handDown + var10 * Tuning.handForward;
      float var14 = var5 + var8 * Tuning.flashAim - var12[0];
      float var15 = var6 + var9 * Tuning.flashAim - var12[1];
      float var16 = var7 + var10 * Tuning.flashAim - var12[2];
      float var17 = (float)Math.sqrt(var14 * var14 + var15 * var15 + var16 * var16);
      var13[0] = var14 / var17;
      var13[1] = var15 / var17;
      var13[2] = var16 / var17;
      this.map.reach = var2.spots.flashRange;
      this.map.cone = Math.min((float)Math.acos(var2.spots.flashCos) + 0.08F, 1.3F);
      this.map.fovY = 2.0F * this.map.cone;
      this.map.texel = 2.0F * (float)Math.tan(this.map.cone) / 2048.0F;
      return true;
   }

   void write(FloatBuffer var1, SceneData var2, int var3) {
      float var4 = var2.spots.flashStrength * Tuning.flashIntensity;
      float[] var5 = this.map.lens;
      float[] var6 = this.map.dir;
      var1.put(var5[0]).put(var5[1]).put(var5[2]).put(this.map.reach);
      var1.put(var6[0]).put(var6[1]).put(var6[2]).put(0.0F);
      var1.put(COLOR[0] * var4).put(COLOR[1] * var4).put(COLOR[2] * var4).put(var3);
      var1.put(0.0F).put(1.0F).put(0.0F).put(var2.spots.flashCos);
      var1.put(var6[0]).put(var6[1]).put(var6[2]).put(0.03F);
      var1.put(Tuning.flashHot).put(0.0F).put(Tuning.flashScatter).put(0.0F);
   }

   void drawMap(SceneData var1, int var2) {
      this.map.layer = var2;
      this.viewProjection.set(this.maps.place(var2, this.map, var1));
      this.maps.drawWorld(var1, this.map);
      this.models.shadow(var1, this.viewProjection, 0.5F, this.map.reach + 1.0F);
      this.maps.end();
   }

   Matrix4f viewProjection() {
      return this.viewProjection;
   }
}
