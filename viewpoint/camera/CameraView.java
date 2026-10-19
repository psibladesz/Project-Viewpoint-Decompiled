package viewpoint.camera;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import viewpoint.core.CameraShot;
import viewpoint.core.CameraSquares;
import viewpoint.core.Frame;
import viewpoint.input.Camera;

public final class CameraView {
   private static final float FAR_AHEAD = 1000.0F;
   private static final float LEVEL = 1.0E-4F;

   public static boolean place(Frame var0, float var1, float var2, float[] var3) {
      CameraShot var4 = var0.cameraShot;
      CameraSquares var5 = var0.cameraSquares;
      if (!var4.on) {
         return false;
      }

      float var6 = var0.camX - var5.x;
      float var7 = var0.camY - var5.y;
      float var8 = (var0.camZ - var5.level) * 2.4494896F;
      float var9 = (float)Math.tan(var4.fovY / 2.0F);
      float var10 = var0.scene.projection.m11() / var0.scene.projection.m00();
      float var11 = Math.max(0.0F, Math.min(var4.distance * PitchCurves.distance(var2), var4.limit) + var4.addBack);
      float var12 = Framing.right(var4.fromLeft, var4.distance, var9, var10) * var4.side + var4.addRight;
      float var13 = Framing.up(PitchCurves.fromTop(var4.fromTop, var2), var4.distance, var9) + PitchCurves.height(var2) + var4.addUp;
      float[] var14 = new float[3];
      Boom.place(var5, var6 - var4.pivotX, var7 - var4.pivotZ, var8 + var4.pivotY, var12, var13, var11, var1, var2, var14);
      var3[0] = var6 - var14[0];
      var3[1] = var14[2] - var8;
      var3[2] = var7 - var14[1];
      return true;
   }

   static float wanted(Frame var0, float var1, float var2, float[] var3, float[] var4, float[] var5) {
      CameraShot var6 = var0.cameraShot;
      float var7 = (float)Math.tan(var6.fovY / 2.0F);
      float var8 = var0.scene.projection.m11() / var0.scene.projection.m00();
      float var9 = Framing.right(var6.fromLeft, var6.distance, var7, var8) * var6.side + var6.addRight;
      float var10 = Framing.up(PitchCurves.fromTop(var6.fromTop, var2), var6.distance, var7) + PitchCurves.height(var2) + var6.addUp;
      float var11 = var6.distance * PitchCurves.distance(var2) + var6.addBack;
      float[] var12 = new float[3];
      Camera.direction(var1, var2, var12);
      float var13 = -var12[2];
      float var14 = var12[0];
      float var15 = (float)Math.sqrt(var13 * var13 + var14 * var14);
      var3[0] = var6.pivotX;
      var3[1] = var6.pivotY;
      var3[2] = var6.pivotZ;
      var4[0] = var3[0] + var13 / var15 * var9;
      var4[1] = var3[1] + var10;
      var4[2] = var3[2] + var14 / var15 * var9;

      for (int var16 = 0; var16 < 3; var16++) {
         var5[var16] = var4[var16] - var12[var16] * var11;
      }

      return var11;
   }

   public static void direction(CameraShot var0, float var1, float var2, float[] var3, float[] var4) {
      Camera.direction(var1 + var0.addYaw, var2 + var0.addPitch, var3);
      float var5 = -var3[2];
      float var6 = var3[0];
      float var7 = (float)Math.sqrt(var5 * var5 + var6 * var6);
      float var8 = (float)Math.cos(var0.addRoll);
      float var9 = (float)Math.sin(var0.addRoll) / Math.max(1.0E-6F, var7);
      var4[0] = var5 * var9;
      var4[1] = var8;
      var4[2] = var6 * var9;
   }

   public static boolean steady(Matrix4f var0, float[] var1, float var2, float var3, float var4, float var5, float[] var6) {
      float[] var7 = new float[3];
      Camera.direction(var2, var3, var7);
      float var8 = -var7[2];
      float var9 = var7[0];
      float var10 = (float)Math.sqrt(var8 * var8 + var9 * var9);
      float var11 = -var7[0] * var7[1];
      float var12 = 1.0F - var7[1] * var7[1];
      float var13 = -var7[2] * var7[1];
      if (var10 > 1.0E-4F) {
         float var14 = var4 / var10;
         float var15 = var5 / var10;
         var7[0] += var8 * var14 + var11 * var15;
         var7[1] += var12 * var15;
         var7[2] += var9 * var14 + var13 * var15;
      }

      Vector4f var16 = new Vector4f(var1[0] + var7[0] * 1000.0F, var1[1] + var7[1] * 1000.0F, var1[2] + var7[2] * 1000.0F, 1.0F);
      var0.transform(var16);
      if (var16.w <= 0.0F) {
         return false;
      }

      var6[0] = var16.x / var16.w;
      var6[1] = var16.y / var16.w;
      return true;
   }

   private CameraView() {
   }
}
