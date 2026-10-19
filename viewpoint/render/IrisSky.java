package viewpoint.render;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

final class IrisSky {
   private static final int FLOATS = 12;
   private static final int DOME_RINGS = 16;
   private static final int DOME_SEGMENTS = 32;
   private static final int STARS = 1500;
   private int vao;
   private int vbo;
   private int domeCount;
   private int starFirst;
   private int starCount;
   private int sunFirst;
   private int sunTexture;
   private int moonTexture;

   void init() {
      GL13.glActiveTexture(34079);
      FloatBuffer var1 = BufferUtils.createFloatBuffer(145008);
      dome(var1);
      this.domeCount = var1.position() / 12;
      this.starFirst = this.domeCount;
      stars(var1);
      this.starCount = var1.position() / 12 - this.starFirst;
      this.sunFirst = var1.position() / 12;

      for (int var2 = 0; var2 < 144; var2++) {
         var1.put(0.0F);
      }

      var1.flip();
      this.vao = GL30.glGenVertexArrays();
      this.vbo = GL15.glGenBuffers();
      GL30.glBindVertexArray(this.vao);
      GL15.glBindBuffer(34962, this.vbo);
      GL15.glBufferData(34962, var1, 35048);
      byte var4 = 48;
      GL20.glVertexAttribPointer(0, 3, 5126, false, var4, 0L);
      GL20.glVertexAttribPointer(1, 4, 5126, false, var4, 12L);
      GL20.glVertexAttribPointer(2, 2, 5126, false, var4, 28L);
      GL20.glVertexAttribPointer(3, 3, 5126, false, var4, 36L);

      for (int var3 = 0; var3 < 4; var3++) {
         GL20.glEnableVertexAttribArray(var3);
      }

      GL30.glBindVertexArray(0);
      GL15.glBindBuffer(34962, 0);
      this.sunTexture = disc(1.0F, 0.95F, 0.85F, false);
      this.moonTexture = disc(0.85F, 0.88F, 0.95F, true);
      GL13.glActiveTexture(33984);
   }

   private static void dome(FloatBuffer var0) {
      for (int var1 = 0; var1 < 16; var1++) {
         for (int var2 = 0; var2 < 32; var2++) {
            float[][] var3 = new float[][]{{var1, var2}, {var1 + 1, var2}, {var1 + 1, var2 + 1}, {var1, var2}, {var1 + 1, var2 + 1}, {var1, var2 + 1}};

            for (float[] var7 : var3) {
               double var8 = Math.PI * (var7[0] / 16.0F - 0.5);
               double var10 = (Math.PI * 2) * var7[1] / 32.0;
               float var12 = (float)(Math.cos(var8) * Math.cos(var10));
               float var13 = (float)Math.sin(var8);
               float var14 = (float)(Math.cos(var8) * Math.sin(var10));
               vertex(var0, var12 * 100.0F, var13 * 100.0F, var14 * 100.0F, 1.0F, 1.0F, 1.0F, 1.0F, var7[1] / 32.0F, var7[0] / 16.0F, -var12, -var13, -var14);
            }
         }
      }
   }

   private static void stars(FloatBuffer var0) {
      Random var1 = new Random(10842L);

      for (int var2 = 0; var2 < 1500; var2++) {
         double var3 = var1.nextFloat() * 2.0F - 1.0F;
         double var5 = var1.nextFloat() * 2.0F - 1.0F;
         double var7 = var1.nextFloat() * 2.0F - 1.0F;
         double var9 = 0.15F + var1.nextFloat() * 0.1F;
         double var11 = var3 * var3 + var5 * var5 + var7 * var7;
         if (!(var11 >= 1.0) && !(var11 <= 0.01)) {
            var11 = 1.0 / Math.sqrt(var11);
            var3 *= var11;
            var5 *= var11;
            var7 *= var11;
            double[] var13 = perpendicular(var3, var5, var7);
            double[] var14 = cross(var3, var5, var7, var13);
            float var15 = 0.5F + var1.nextFloat() * 0.5F;
            float[][] var16 = new float[][]{{-1.0F, -1.0F}, {1.0F, -1.0F}, {1.0F, 1.0F}, {-1.0F, -1.0F}, {1.0F, 1.0F}, {-1.0F, 1.0F}};

            for (float[] var20 : var16) {
               double var21 = var3 * 100.0 + (var13[0] * var20[0] + var14[0] * var20[1]) * var9 * 2.0;
               double var23 = var5 * 100.0 + (var13[1] * var20[0] + var14[1] * var20[1]) * var9 * 2.0;
               double var25 = var7 * 100.0 + (var13[2] * var20[0] + var14[2] * var20[1]) * var9 * 2.0;
               vertex(
                  var0,
                  (float)var21,
                  (float)var23,
                  (float)var25,
                  var15,
                  var15,
                  var15,
                  var15,
                  0.5F + var20[0] * 0.5F,
                  0.5F + var20[1] * 0.5F,
                  (float)(-var3),
                  (float)(-var5),
                  (float)(-var7)
               );
            }
         }
      }
   }

   void update(double[] var1, double[] var2) {
      FloatBuffer var3 = BufferUtils.createFloatBuffer(144);
      quad(var3, var1, 30.0);
      quad(var3, var2, 20.0);
      var3.flip();
      GL15.glBindBuffer(34962, this.vbo);
      GL15.glBufferSubData(34962, this.sunFirst * 12L * 4L, var3);
      GL15.glBindBuffer(34962, 0);
   }

   private static void quad(FloatBuffer var0, double[] var1, double var2) {
      double[] var4 = perpendicular(var1[0], var1[1], var1[2]);
      double[] var5 = cross(var1[0], var1[1], var1[2], var4);
      float[][] var6 = new float[][]{{-1.0F, -1.0F}, {1.0F, -1.0F}, {1.0F, 1.0F}, {-1.0F, -1.0F}, {1.0F, 1.0F}, {-1.0F, 1.0F}};

      for (float[] var10 : var6) {
         double var11 = var2 * 0.5;
         vertex(
            var0,
            (float)(var1[0] * 100.0 + (var4[0] * var10[0] + var5[0] * var10[1]) * var11),
            (float)(var1[1] * 100.0 + (var4[1] * var10[0] + var5[1] * var10[1]) * var11),
            (float)(var1[2] * 100.0 + (var4[2] * var10[0] + var5[2] * var10[1]) * var11),
            1.0F,
            1.0F,
            1.0F,
            1.0F,
            0.5F + var10[0] * 0.5F,
            0.5F + var10[1] * 0.5F,
            (float)(-var1[0]),
            (float)(-var1[1]),
            (float)(-var1[2])
         );
      }
   }

   void bind() {
      GL30.glBindVertexArray(this.vao);
   }

   void drawDome() {
      GL11.glDrawArrays(4, 0, this.domeCount);
   }

   void drawStars() {
      GL11.glDrawArrays(4, this.starFirst, this.starCount);
   }

   void drawSun() {
      GL11.glBindTexture(3553, this.sunTexture);
      GL11.glDrawArrays(4, this.sunFirst, 6);
   }

   void drawMoon() {
      GL11.glBindTexture(3553, this.moonTexture);
      GL11.glDrawArrays(4, this.sunFirst + 6, 6);
   }

   void unbind() {
      GL30.glBindVertexArray(0);
   }

   private static void vertex(
      FloatBuffer var0,
      float var1,
      float var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11,
      float var12
   ) {
      var0.put(var1).put(var2).put(var3).put(var4).put(var5).put(var6).put(var7).put(var8).put(var9).put(var10).put(var11).put(var12);
   }

   private static double[] perpendicular(double var0, double var2, double var4) {
      double[] var6 = Math.abs(var2) < 0.9 ? cross(var0, var2, var4, new double[]{0.0, 1.0, 0.0}) : cross(var0, var2, var4, new double[]{1.0, 0.0, 0.0});
      double var7 = Math.sqrt(var6[0] * var6[0] + var6[1] * var6[1] + var6[2] * var6[2]);
      return new double[]{var6[0] / var7, var6[1] / var7, var6[2] / var7};
   }

   private static double[] cross(double var0, double var2, double var4, double[] var6) {
      return new double[]{var2 * var6[2] - var4 * var6[1], var4 * var6[0] - var0 * var6[2], var0 * var6[1] - var2 * var6[0]};
   }

   private static int disc(float var0, float var1, float var2, boolean var3) {
      byte var4 = 64;
      ByteBuffer var5 = BufferUtils.createByteBuffer(var4 * var4 * 4);
      Random var6 = new Random(3L);

      for (int var7 = 0; var7 < var4; var7++) {
         for (int var8 = 0; var8 < var4; var8++) {
            double var9 = (var8 + 0.5) / var4 * 2.0 - 1.0;
            double var11 = (var7 + 0.5) / var4 * 2.0 - 1.0;
            double var13 = Math.sqrt(var9 * var9 + var11 * var11);
            float var15 = (float)Math.max(0.0, Math.min(1.0, (0.62 - var13) * 20.0));
            float var16 = var3 ? 0.8F + 0.2F * var6.nextFloat() : 1.0F;
            var5.put((byte)(255.0F * var0 * var16)).put((byte)(255.0F * var1 * var16)).put((byte)(255.0F * var2 * var16)).put((byte)(255.0F * var15));
         }
      }

      var5.flip();
      int var17 = GL11.glGenTextures();
      GL11.glBindTexture(3553, var17);
      GL11.glTexImage2D(3553, 0, 32856, var4, var4, 0, 6408, 5121, var5);
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      GL11.glBindTexture(3553, 0);
      return var17;
   }

   void release() {
      GL30.glDeleteVertexArrays(this.vao);
      GL15.glDeleteBuffers(this.vbo);
      GL11.glDeleteTextures(this.sunTexture);
      GL11.glDeleteTextures(this.moonTexture);
   }
}
