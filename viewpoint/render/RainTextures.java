package viewpoint.render;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

final class RainTextures {
   static final int SIZE = 256;
   static final int DROPS = 72;
   static final float SMALLEST = 0.035F;
   static final float LARGEST = 0.09F;
   private static final int CELLS = 4;
   private static final int OCTAVES = 4;
   static final long SEED = 500181L;
   private final int texture;

   static byte[] layers() {
      byte[] var0 = ripples(256, 500181L);
      byte[] var1 = heights(256, 500181L);
      byte[] var2 = Arrays.copyOf(var0, var0.length + var1.length);
      System.arraycopy(var1, 0, var2, var0.length, var1.length);
      return var2;
   }

   RainTextures(byte[] var1) {
      int var2 = GL11.glGetInteger(35869);
      ByteBuffer var3 = BufferUtils.createByteBuffer(var1.length).put(var1).flip();
      this.texture = GL11.glGenTextures();
      GL11.glBindTexture(35866, this.texture);
      GL12.glTexImage3D(35866, 0, 32856, 256, 256, 2, 0, 6408, 5121, var3);
      GL11.glTexParameteri(35866, 10241, 9729);
      GL11.glTexParameteri(35866, 10240, 9729);
      GL11.glTexParameteri(35866, 10242, 10497);
      GL11.glTexParameteri(35866, 10243, 10497);
      GL11.glBindTexture(35866, var2);
   }

   void bind() {
      GL13.glActiveTexture(34107);
      GL11.glBindTexture(35866, this.texture);
   }

   static byte[] ripples(int var0, long var1) {
      Random var3 = new Random(var1);
      float[] var4 = new float[72];
      float[] var5 = new float[72];
      float[] var6 = new float[72];
      float[] var7 = new float[72];

      for (int var8 = 0; var8 < 72; var8++) {
         var4[var8] = var3.nextFloat();
         var5[var8] = var3.nextFloat();
         var6[var8] = 0.035F + 0.055000003F * var3.nextFloat();
         var7[var8] = var3.nextFloat();
      }

      byte[] var22 = new byte[var0 * var0 * 4];

      for (int var9 = 0; var9 < var0; var9++) {
         for (int var10 = 0; var10 < var0; var10++) {
            float var11 = (var10 + 0.5F) / var0;
            float var12 = (var9 + 0.5F) / var0;
            float var13 = 0.0F;
            float var14 = 0.0F;
            float var15 = 0.0F;
            float var16 = 0.0F;

            for (int var17 = 0; var17 < 72; var17++) {
               float var18 = wrapped(var11 - var4[var17]);
               float var19 = wrapped(var12 - var5[var17]);
               float var20 = 1.0F - (float)Math.sqrt(var18 * var18 + var19 * var19) / var6[var17];
               if (var20 > var13) {
                  var13 = var20;
                  float var21 = Math.max((float)Math.sqrt(var18 * var18 + var19 * var19), 1.0E-6F);
                  var14 = var18 / var21;
                  var15 = var19 / var21;
                  var16 = var7[var17];
               }
            }

            int var23 = (var9 * var0 + var10) * 4;
            var22[var23] = unorm(var13);
            var22[var23 + 1] = unorm(var14 * 0.5F + 0.5F);
            var22[var23 + 2] = unorm(var15 * 0.5F + 0.5F);
            var22[var23 + 3] = unorm(var16);
         }
      }

      return var22;
   }

   static byte[] heights(int var0, long var1) {
      byte[] var3 = new byte[var0 * var0 * 4];

      for (int var4 = 0; var4 < 4; var4++) {
         float[] var5 = ranked(var0, new Random(var1 ^ -7046029254386353131L ^ var4 * 7146057691288625177L));

         for (int var6 = 0; var6 < var5.length; var6++) {
            var3[var6 * 4 + var4] = unorm(var5[var6]);
         }
      }

      return var3;
   }

   private static float[] ranked(int var0, Random var1) {
      float[] var2 = new float[var0 * var0];
      float var3 = 1.0F;
      int var4 = 0;
      byte var5 = 4;

      while (var4 < 4) {
         float[] var6 = new float[var5 * var5];

         for (int var7 = 0; var7 < var6.length; var7++) {
            var6[var7] = var1.nextFloat();
         }

         for (int var13 = 0; var13 < var0; var13++) {
            for (int var8 = 0; var8 < var0; var8++) {
               var2[var13 * var0 + var8] = var2[var13 * var0 + var8] + var3 * smooth(var6, var5, (var8 + 0.5F) * var5 / var0, (var13 + 0.5F) * var5 / var0);
            }
         }

         var4++;
         var5 *= 2;
         var3 *= 0.5F;
      }

      Integer[] var9 = new Integer[var2.length];

      for (int var10 = 0; var10 < var9.length; var10++) {
         var9[var10] = var10;
      }

      Arrays.sort(var9, (var1x, var2x) -> Float.compare(var2[var1x], var2[var2x]));
      float[] var11 = new float[var2.length];

      for (int var12 = 0; var12 < var9.length; var12++) {
         var11[var9[var12]] = (float)var12 / (var9.length - 1);
      }

      return var11;
   }

   private static float smooth(float[] var0, int var1, float var2, float var3) {
      int var4 = (int)Math.floor(var2);
      int var5 = (int)Math.floor(var3);
      float var6 = fade(var2 - var4);
      float var7 = fade(var3 - var5);
      float var8 = at(var0, var1, var4, var5);
      float var9 = at(var0, var1, var4 + 1, var5);
      float var10 = at(var0, var1, var4, var5 + 1);
      float var11 = at(var0, var1, var4 + 1, var5 + 1);
      return (var8 + (var9 - var8) * var6) * (1.0F - var7) + (var10 + (var11 - var10) * var6) * var7;
   }

   private static float at(float[] var0, int var1, int var2, int var3) {
      return var0[Math.floorMod(var3, var1) * var1 + Math.floorMod(var2, var1)];
   }

   private static float fade(float var0) {
      return var0 * var0 * var0 * (var0 * (var0 * 6.0F - 15.0F) + 10.0F);
   }

   private static float wrapped(float var0) {
      return var0 - Math.round(var0);
   }

   private static byte unorm(float var0) {
      return (byte)Math.round(Math.max(0.0F, Math.min(1.0F, var0)) * 255.0F);
   }
}
