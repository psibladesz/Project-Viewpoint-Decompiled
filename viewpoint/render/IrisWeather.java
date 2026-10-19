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

final class IrisWeather {
   private static final int RADIUS = 6;
   private static final int FLOATS = 12;
   private static final int SIDE = 13;
   private static final float BELOW = 6.0F;
   private static final float ABOVE = 10.0F;
   private static final float RAIN_FALL = 2.2F;
   private static final float SNOW_FALL = 0.25F;
   private final FloatBuffer data = BufferUtils.createFloatBuffer(24336);
   private int vao;
   private int vbo;
   private int rainTexture;
   private int snowTexture;
   private int rainCount;
   private int snowCount;

   void init() {
      this.vao = GL30.glGenVertexArrays();
      this.vbo = GL15.glGenBuffers();
      GL30.glBindVertexArray(this.vao);
      GL15.glBindBuffer(34962, this.vbo);
      GL15.glBufferData(34962, this.data.capacity() * 4L, 35040);
      byte var1 = 48;
      GL20.glVertexAttribPointer(0, 3, 5126, false, var1, 0L);
      GL20.glVertexAttribPointer(1, 4, 5126, false, var1, 12L);
      GL20.glVertexAttribPointer(2, 2, 5126, false, var1, 28L);
      GL20.glVertexAttribPointer(3, 3, 5126, false, var1, 36L);

      for (int var2 = 0; var2 < 4; var2++) {
         GL20.glEnableVertexAttribArray(var2);
      }

      GL30.glBindVertexArray(0);
      GL15.glBindBuffer(34962, 0);
      GL13.glActiveTexture(34079);
      this.rainTexture = streaks(false);
      this.snowTexture = streaks(true);
      GL11.glBindTexture(3553, 0);
      GL13.glActiveTexture(33984);
   }

   boolean update(SceneData var1, float var2, float var3, double var4) {
      this.rainCount = this.snowCount = 0;
      if (var1.rain <= 0.0F && var1.snow <= 0.0F) {
         return false;
      }

      this.data.clear();
      this.rainCount = this.columns(var1.rain, var2, var3, var4 * 2.2F, false);
      this.snowCount = this.columns(var1.snow, var2, var3, var4 * 0.25, true);
      this.data.flip();
      GL15.glBindBuffer(34962, this.vbo);
      GL15.glBufferSubData(34962, 0L, this.data);
      GL15.glBindBuffer(34962, 0);
      return this.rainCount + this.snowCount > 0;
   }

   private int columns(float var1, float var2, float var3, double var4, boolean var6) {
      if (var1 <= 0.0F) {
         return 0;
      }

      int var7 = this.data.position();
      int var8 = (int)Math.floor(-var2);
      int var9 = (int)Math.floor(-var3);
      float var10 = -var2 - var8;
      float var11 = -var3 - var9;

      for (int var12 = -6; var12 <= 6; var12++) {
         for (int var13 = -6; var13 <= 6; var13++) {
            float var14 = var13 + 0.5F - var10;
            float var15 = var12 + 0.5F - var11;
            float var16 = (float)Math.sqrt(var14 * var14 + var15 * var15);
            if (!(var16 > 6.0F) && !(var16 < 0.01F)) {
               long var17 = (var8 + var13) * 3121L + (var9 + var12) * 45238971L;
               float var19 = (float)((var17 * 418711L ^ var17 >> 7) & 1023L) / 1024.0F;
               float var20 = var1 * ((1.0F - var16 * var16 / 36.0F) * 0.5F + 0.5F);
               this.quad(var14, var15, var16, scroll(var4 * (var6 ? 1.0 : 1.0 + var19)) + var19, var20, var6 ? var19 : 0.0F);
            }
         }
      }

      return (this.data.position() - var7) / 12;
   }

   static float scroll(double var0) {
      return (float)(-(var0 - Math.floor(var0)));
   }

   private void quad(float var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = var2 / var3 * 0.5F;
      float var8 = -var1 / var3 * 0.5F;
      float var9 = var4;
      float var10 = var4 + 4.0F;
      float[][] var11 = new float[][]{{-1.0F, -6.0F, 0.0F, var10}, {1.0F, -6.0F, 1.0F, var10}, {1.0F, 10.0F, 1.0F, var9}, {-1.0F, 10.0F, 0.0F, var9}};
      int[] var12 = new int[]{0, 1, 2, 0, 2, 3};

      for (int var16 : var12) {
         float[] var17 = var11[var16];
         this.data.put(var1 + var7 * var17[0]).put(var17[1]).put(var2 + var8 * var17[0]);
         this.data.put(1.0F).put(1.0F).put(1.0F).put(var5);
         this.data.put(var17[2] + var6).put(var17[3]);
         this.data.put(var1).put(0.0F).put(var2);
      }
   }

   void draw() {
      GL30.glBindVertexArray(this.vao);
      GL13.glActiveTexture(33984);
      if (this.rainCount > 0) {
         GL11.glBindTexture(3553, this.rainTexture);
         GL11.glDrawArrays(4, 0, this.rainCount);
      }

      if (this.snowCount > 0) {
         GL11.glBindTexture(3553, this.snowTexture);
         GL11.glDrawArrays(4, this.rainCount, this.snowCount);
      }

      GL30.glBindVertexArray(0);
   }

   private static int streaks(boolean var0) {
      byte var1 = 64;
      short var2 = 256;
      ByteBuffer var3 = BufferUtils.createByteBuffer(var1 * var2 * 4);
      Random var4 = new Random(var0 ? 11L : 7L);
      byte[] var5 = new byte[var1 * var2];

      for (int var6 = 0; var6 < (var0 ? 180 : 90); var6++) {
         int var7 = var4.nextInt(var1);
         int var8 = var4.nextInt(var2);
         int var9 = var0 ? 2 : 6 + var4.nextInt(10);

         for (int var10 = 0; var10 < var9; var10++) {
            var5[(var8 + var10) % var2 * var1 + var7] = (byte)(var0 ? 230 : 110 + var4.nextInt(80));
            if (var0) {
               var5[(var8 + var10) % var2 * var1 + (var7 + 1) % var1] = -56;
            }
         }
      }

      for (int var15 : var5) {
         var3.put((byte)-1).put((byte)-1).put((byte)-1).put((byte)var15);
      }

      var3.flip();
      int var12 = GL11.glGenTextures();
      GL11.glBindTexture(3553, var12);
      GL11.glTexImage2D(3553, 0, 32856, var1, var2, 0, 6408, 5121, var3);
      GL11.glTexParameteri(3553, 10241, 9728);
      GL11.glTexParameteri(3553, 10240, 9728);
      return var12;
   }

   void release() {
      GL30.glDeleteVertexArrays(this.vao);
      GL15.glDeleteBuffers(this.vbo);
      GL11.glDeleteTextures(this.rainTexture);
      GL11.glDeleteTextures(this.snowTexture);
   }
}
