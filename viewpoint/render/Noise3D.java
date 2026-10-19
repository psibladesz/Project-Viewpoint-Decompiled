package viewpoint.render;

import java.nio.ByteBuffer;
import java.util.Arrays;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

final class Noise3D {
   static final int SIZE = 64;

   static byte[] values() {
      float[] var0 = new float[262144];
      float var1 = 0.015625F;
      int var2 = 0;

      for (int var3 = 0; var3 < 64; var3++) {
         for (int var4 = 0; var4 < 64; var4++) {
            for (int var5 = 0; var5 < 64; var5++) {
               float var6 = var5 * var1;
               float var7 = var4 * var1;
               float var8 = var3 * var1;
               float var9 = perlinFbm(var6, var7, var8, 4, 3);
               float var10 = worleyFbm(var6, var7, var8, 4);
               var0[var2++] = remap(var9, -(1.0F - var10), 1.0F, 0.0F, 1.0F);
            }
         }
      }

      float[] var11 = (float[])var0.clone();
      Arrays.sort(var11);
      float var12 = var11[(int)(var11.length * 0.03)];
      float var13 = var11[(int)(var11.length * 0.97)];
      byte[] var14 = new byte[var0.length];

      for (int var15 = 0; var15 < var0.length; var15++) {
         var14[var15] = (byte)Math.round(clamp((var0[var15] - var12) / Math.max(var13 - var12, 1.0E-4F)) * 255.0F);
      }

      return var14;
   }

   static int upload(byte[] var0) {
      ByteBuffer var1 = BufferUtils.createByteBuffer(var0.length).put(var0).flip();
      int var2 = GL11.glGenTextures();
      GL11.glBindTexture(32879, var2);
      GL11.glPixelStorei(3317, 1);
      GL12.glTexImage3D(32879, 0, 33321, 64, 64, 64, 0, 6403, 5121, var1);
      GL11.glPixelStorei(3317, 4);
      GL30.glGenerateMipmap(32879);
      GL11.glTexParameteri(32879, 10241, 9987);
      GL11.glTexParameteri(32879, 10240, 9729);
      GL11.glTexParameteri(32879, 10242, 10497);
      GL11.glTexParameteri(32879, 10243, 10497);
      GL11.glTexParameteri(32879, 32882, 10497);
      GL11.glBindTexture(32879, 0);
      return var2;
   }

   private static float remap(float var0, float var1, float var2, float var3, float var4) {
      return var3 + (var0 - var1) / (var2 - var1) * (var4 - var3);
   }

   private static float clamp(float var0) {
      return Math.max(0.0F, Math.min(1.0F, var0));
   }

   private static float perlinFbm(float var0, float var1, float var2, int var3, int var4) {
      float var5 = 0.0F;
      float var6 = 0.5F;
      float var7 = 0.0F;

      for (int var8 = 0; var8 < var4; var8++) {
         var5 += var6 * perlin(var0, var1, var2, var3);
         var7 += var6;
         var6 *= 0.5F;
         var3 *= 2;
      }

      return clamp(0.5F + 0.5F * var5 / var7);
   }

   private static float perlin(float var0, float var1, float var2, int var3) {
      float var4 = var0 * var3;
      float var5 = var1 * var3;
      float var6 = var2 * var3;
      int var7 = (int)Math.floor(var4);
      int var8 = (int)Math.floor(var5);
      int var9 = (int)Math.floor(var6);
      float var10 = var4 - var7;
      float var11 = var5 - var8;
      float var12 = var6 - var9;
      float var13 = fade(var10);
      float var14 = fade(var11);
      float var15 = fade(var12);
      float var16 = 0.0F;

      for (int var17 = 0; var17 < 8; var17++) {
         int var18 = var17 & 1;
         int var19 = var17 >> 1 & 1;
         int var20 = var17 >> 2 & 1;
         int var21 = hash(Math.floorMod(var7 + var18, var3), Math.floorMod(var8 + var19, var3), Math.floorMod(var9 + var20, var3));
         float var22 = (var21 & 1) == 0 ? 1.0F : -1.0F;
         float var23 = (var21 & 2) == 0 ? 1.0F : -1.0F;
         float var24 = (var21 & 4) == 0 ? 1.0F : -1.0F;
         int var25 = var21 >> 3 & 3;
         if (var25 == 0) {
            var22 = 0.0F;
         } else if (var25 == 1) {
            var23 = 0.0F;
         } else {
            var24 = 0.0F;
         }

         float var26 = var22 * (var10 - var18) + var23 * (var11 - var19) + var24 * (var12 - var20);
         float var27 = (var18 == 0 ? 1.0F - var13 : var13) * (var19 == 0 ? 1.0F - var14 : var14) * (var20 == 0 ? 1.0F - var15 : var15);
         var16 += var26 * var27;
      }

      return var16;
   }

   private static float fade(float var0) {
      return var0 * var0 * var0 * (var0 * (var0 * 6.0F - 15.0F) + 10.0F);
   }

   private static float worleyFbm(float var0, float var1, float var2, int var3) {
      return clamp(0.625F * worley(var0, var1, var2, var3) + 0.25F * worley(var0, var1, var2, var3 * 2) + 0.125F * worley(var0, var1, var2, var3 * 4));
   }

   private static float worley(float var0, float var1, float var2, int var3) {
      float var4 = var0 * var3;
      float var5 = var1 * var3;
      float var6 = var2 * var3;
      int var7 = (int)Math.floor(var4);
      int var8 = (int)Math.floor(var5);
      int var9 = (int)Math.floor(var6);
      float var10 = 1.0E9F;

      for (int var11 = -1; var11 <= 1; var11++) {
         for (int var12 = -1; var12 <= 1; var12++) {
            for (int var13 = -1; var13 <= 1; var13++) {
               int var14 = var7 + var13;
               int var15 = var8 + var12;
               int var16 = var9 + var11;
               int var17 = hash(Math.floorMod(var14, var3), Math.floorMod(var15, var3), Math.floorMod(var16, var3));
               float var18 = var14 + (var17 & 0xFF) / 255.0F;
               float var19 = var15 + (var17 >> 8 & 0xFF) / 255.0F;
               float var20 = var16 + (var17 >> 16 & 0xFF) / 255.0F;
               float var21 = (var18 - var4) * (var18 - var4) + (var19 - var5) * (var19 - var5) + (var20 - var6) * (var20 - var6);
               var10 = Math.min(var10, var21);
            }
         }
      }

      return clamp(1.0F - (float)Math.sqrt(var10));
   }

   private static int hash(int var0, int var1, int var2) {
      int var3 = var0 * 374761393 + var1 * 668265263 + var2 * Integer.MAX_VALUE;
      var3 = (var3 ^ var3 >>> 13) * 1274126177;
      return var3 ^ var3 >>> 16;
   }

   private Noise3D() {
   }
}
