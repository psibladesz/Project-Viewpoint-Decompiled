package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

final class IrisLightning {
   private static final int FLOATS = 12;
   private static final int SEGMENTS = 8;
   private static final int LAYERS = 4;
   private static final int QUADS = 4;
   private static final float HEIGHT = 128.0F;
   private static final float STEP = 16.0F;
   private static final float WANDER = 4.0F;
   private static final float[] COLOUR = new float[]{0.45F, 0.45F, 0.5F, 0.3F};
   private final FloatBuffer data = BufferUtils.createFloatBuffer(9216);
   private int vao;
   private int vbo;
   private int count;

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
   }

   boolean update(SceneData var1, float var2, float var3, float var4) {
      this.count = 0;
      if (var1.lightning <= 0.0F) {
         return false;
      }

      float var5 = -(var1.lightningX - var2);
      float var6 = var1.lightningY - var3;
      float var7 = -(var1.lightningZ - var4);
      Random var8 = new Random(Float.floatToIntBits(var1.lightningX) * 31L + Float.floatToIntBits(var1.lightningZ));
      float[] var9 = new float[9];
      float[] var10 = new float[9];

      for (int var11 = 7; var11 >= 0; var11--) {
         var9[var11] = var9[var11 + 1] + (var8.nextFloat() - 0.5F) * 4.0F;
         var10[var11] = var10[var11 + 1] + (var8.nextFloat() - 0.5F) * 4.0F;
      }

      this.data.clear();

      for (int var16 = 0; var16 < 4; var16++) {
         float var12 = 0.1F + var16 * 0.1F;

         for (int var13 = 0; var13 < 8; var13++) {
            float var14 = var6 + 128.0F - var13 * 16.0F;
            float var15 = var14 - 16.0F;
            this.column(var5 + var9[var13], var14, var7 + var10[var13], var5 + var9[var13 + 1], var15, var7 + var10[var13 + 1], var12);
         }
      }

      this.data.flip();
      this.count = this.data.remaining() / 12;
      GL15.glBindBuffer(34962, this.vbo);
      GL15.glBufferSubData(34962, 0L, this.data);
      GL15.glBindBuffer(34962, 0);
      return true;
   }

   private void column(float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      float[][] var8 = new float[][]{{-var7, -var7}, {var7, -var7}, {var7, var7}, {-var7, var7}};

      for (int var9 = 0; var9 < 4; var9++) {
         float[] var10 = var8[var9];
         float[] var11 = var8[(var9 + 1) % 4];
         this.vertex(var1 + var10[0], var2, var3 + var10[1]);
         this.vertex(var1 + var11[0], var2, var3 + var11[1]);
         this.vertex(var4 + var11[0], var5, var6 + var11[1]);
         this.vertex(var1 + var10[0], var2, var3 + var10[1]);
         this.vertex(var4 + var11[0], var5, var6 + var11[1]);
         this.vertex(var4 + var10[0], var5, var6 + var10[1]);
      }
   }

   private void vertex(float var1, float var2, float var3) {
      this.data.put(var1).put(var2).put(var3).put(COLOUR).put(0.0F).put(0.0F).put(0.0F).put(1.0F).put(0.0F);
   }

   void draw() {
      if (this.count != 0) {
         GL30.glBindVertexArray(this.vao);
         GL11.glDrawArrays(4, 0, this.count);
         GL30.glBindVertexArray(0);
      }
   }

   void release() {
      GL30.glDeleteVertexArrays(this.vao);
      GL15.glDeleteBuffers(this.vbo);
   }
}
