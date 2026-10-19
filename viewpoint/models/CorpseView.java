package viewpoint.models;

import viewpoint.core.Frame;
import viewpoint.render.SceneData;

final class CorpseView {
   private static final double MARGIN = Math.toRadians(10.0);
   private static float apexX;
   private static float apexY;
   private static float levelX;
   private static float levelY;
   private static float eyeX;
   private static float eyeY;
   private static float eyeUp;
   private static float forwardX;
   private static float forwardY;
   private static float forwardUp;
   private static float rightX;
   private static float rightY;
   private static float upX;
   private static float upY;
   private static float upUp;
   private static float sinX;
   private static float cosX;
   private static float sinY;
   private static float cosY;

   static void begin(Frame var0) {
      SceneData var1 = var0.scene;
      set(
         var0.camX - var1.viewerX,
         var0.camY - var1.viewerZ,
         var0.camZ * 2.4494896F + var1.viewerY,
         var0.viewYaw,
         var0.viewPitch,
         1.0F / var1.projection.m00(),
         1.0F / var1.projection.m11()
      );
      apex(var0.camX, var0.camY);
   }

   static void set(float var0, float var1, float var2, float var3, float var4, float var5, float var6) {
      eyeX = var0;
      eyeY = var1;
      eyeUp = var2;
      float var7 = (float)Math.cos(var4);
      levelX = (float)Math.cos(var3);
      levelY = (float)Math.sin(var3);
      forwardX = levelX * var7;
      forwardY = levelY * var7;
      forwardUp = (float)Math.sin(var4);
      float var8 = (float)Math.sqrt(forwardX * forwardX + forwardY * forwardY);
      rightX = var8 > 1.0E-6F ? forwardY / var8 : 1.0F;
      rightY = var8 > 1.0E-6F ? -forwardX / var8 : 0.0F;
      upX = rightY * forwardUp;
      upY = -rightX * forwardUp;
      upUp = rightX * forwardY - rightY * forwardX;
      double var9 = Math.atan(var5) + MARGIN;
      double var11 = Math.atan(var6) + MARGIN;
      sinX = (float)Math.sin(var9);
      cosX = (float)Math.cos(var9);
      sinY = (float)Math.sin(var11);
      cosY = (float)Math.cos(var11);
   }

   static void apex(float var0, float var1) {
      apexX = var0;
      apexY = var1;
   }

   static boolean sees(float var0, float var1, float var2) {
      float var3 = -1.2F;
      float var4 = var0 - apexX;
      float var5 = var1 - apexY;
      float var6 = var4 * levelX + var5 * levelY;
      float var7 = Math.abs(var4 * levelY - var5 * levelX);
      if (!(var6 <= var3) && !(var6 * sinX - var7 * cosX <= var3)) {
         float var8 = var0 - eyeX;
         float var9 = var1 - eyeY;
         float var10 = var2 * 2.4494896F + 0.2F - eyeUp;
         float var11 = var8 * forwardX + var9 * forwardY + var10 * forwardUp;
         float var12 = Math.abs(var8 * rightX + var9 * rightY);
         float var13 = Math.abs(var8 * upX + var9 * upY + var10 * upUp);
         return var11 > var3 && var11 * sinX - var12 * cosX > var3 && var11 * sinY - var13 * cosY > var3;
      } else {
         return false;
      }
   }

   private CorpseView() {
   }
}
