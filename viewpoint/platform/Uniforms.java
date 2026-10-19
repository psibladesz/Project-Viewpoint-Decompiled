package viewpoint.platform;

import java.nio.FloatBuffer;
import java.util.Arrays;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

final class Uniforms {
   private static final int FLOATS = 4;
   private static final int MATRIX = 16;
   private final FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
   private final float[] scratch = new float[16];
   private float[][] floats = new float[0][];
   private Integer[] ints = new Integer[0];

   void set(int var1, float var2) {
      if (this.changed(var1, 1, var2, 0.0F, 0.0F, 0.0F)) {
         GL20.glUniform1f(var1, var2);
      }
   }

   void set(int var1, float var2, float var3) {
      if (this.changed(var1, 2, var2, var3, 0.0F, 0.0F)) {
         GL20.glUniform2f(var1, var2, var3);
      }
   }

   void set(int var1, float var2, float var3, float var4) {
      if (this.changed(var1, 3, var2, var3, var4, 0.0F)) {
         GL20.glUniform3f(var1, var2, var3, var4);
      }
   }

   void set(int var1, float var2, float var3, float var4, float var5) {
      if (this.changed(var1, 4, var2, var3, var4, var5)) {
         GL20.glUniform4f(var1, var2, var3, var4, var5);
      }
   }

   void setInt(int var1, int var2) {
      if (var1 >= 0) {
         this.grow(var1);
         Integer var3 = this.ints[var1];
         if (var3 == null || var3 != var2) {
            this.ints[var1] = var2;
            GL20.glUniform1i(var1, var2);
         }
      }
   }

   void set(int var1, Matrix4f var2) {
      if (var1 >= 0) {
         this.grow(var1);
         float[] var3 = this.floats[var1];
         var2.get(this.scratch);
         if (var3 == null || !Arrays.equals(var3, this.scratch)) {
            this.floats[var1] = var3 != null && var3.length == 16 ? var3 : new float[16];
            System.arraycopy(this.scratch, 0, this.floats[var1], 0, 16);
            this.matrix.clear();
            this.matrix.put(this.scratch).flip();
            GL20.glUniformMatrix4fv(var1, false, this.matrix);
         }
      }
   }

   void forget(int var1) {
      if (var1 >= 0 && var1 < this.floats.length) {
         this.floats[var1] = null;
         this.ints[var1] = null;
      }
   }

   void clear() {
      Arrays.fill(this.floats, null);
      Arrays.fill(this.ints, null);
   }

   private boolean changed(int var1, int var2, float var3, float var4, float var5, float var6) {
      if (var1 < 0) {
         return false;
      }

      this.grow(var1);
      float[] var7 = this.floats[var1];
      if (var7 == null
         || var7.length != var2
         || var7[0] != var3
         || var2 >= 2 && var7[1] != var4
         || var2 >= 3 && var7[2] != var5
         || var2 >= 4 && var7[3] != var6) {
         float[] var8 = var7 != null && var7.length == var2 ? var7 : new float[var2];
         var8[0] = var3;
         if (var2 > 1) {
            var8[1] = var4;
         }

         if (var2 > 2) {
            var8[2] = var5;
         }

         if (var2 > 3) {
            var8[3] = var6;
         }

         this.floats[var1] = var8;
         return true;
      } else {
         return false;
      }
   }

   private void grow(int var1) {
      if (var1 >= this.floats.length) {
         int var2 = Math.max(var1 + 1, this.floats.length * 2);
         this.floats = Arrays.copyOf(this.floats, var2);
         this.ints = Arrays.copyOf(this.ints, var2);
      }
   }
}
