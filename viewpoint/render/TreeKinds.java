package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.Arrays;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import org.lwjgl.system.MemoryUtil;
import viewpoint.platform.GlProgram;

public final class TreeKinds {
   public static final int DISPLAYS = 5;
   private static final int KIND_TEXELS = 6;
   private static final int FLOATS = 24;
   private static float[] kinds = new float[6144];
   private static int count;
   private static volatile float[] published;
   private static volatile byte[] displays;
   private static float[] uploaded;
   private static int buffer;
   private static int texture;
   private static FloatBuffer displayValues;

   public static void set(int var0, int[] var1, boolean[] var2, float var3, float var4) {
      if (var0 * 24 >= kinds.length) {
         kinds = Arrays.copyOf(kinds, Math.max(kinds.length * 2, (var0 + 1) * 24));
      }

      int var5 = var0 * 24;
      kinds[var5] = var3;
      kinds[var5 + 1] = var4;

      for (int var6 = 0; var6 < 5; var6++) {
         int var7 = var1[var6];
         int var8 = var5 + 4 * (1 + var6);
         kinds[var8] = (var7 >> 16 & 0xFF) / 255.0F;
         kinds[var8 + 1] = (var7 >> 8 & 0xFF) / 255.0F;
         kinds[var8 + 2] = (var7 & 0xFF) / 255.0F;
         kinds[var8 + 3] = var2[var6] ? 1.0F : 0.0F;
      }

      count = Math.max(count, var0 + 1);
      published = Arrays.copyOf(kinds, count * 24);
   }

   public static void displays(byte[] var0) {
      displays = var0;
   }

   static boolean bind() {
      float[] var0 = published;
      if (var0 != null && displays != null) {
         if (var0 != uploaded) {
            upload(var0);
         }

         GL13.glActiveTexture(34028);
         GL11.glBindTexture(35882, texture);
         GL13.glActiveTexture(33984);
         return true;
      } else {
         return false;
      }
   }

   static void uniforms(GlProgram var0) {
      byte[] var1 = displays;
      if (displayValues == null || displayValues.capacity() != var1.length) {
         displayValues = BufferUtils.createFloatBuffer(var1.length);
      }

      displayValues.clear();

      for (int var5 : var1) {
         displayValues.put(var5);
      }

      var0.setFloats("uDisplays", displayValues.flip());
   }

   static void unbind() {
      GL13.glActiveTexture(34028);
      GL11.glBindTexture(35882, 0);
      GL13.glActiveTexture(33984);
   }

   private static void upload(float[] var0) {
      if (buffer == 0) {
         buffer = GL15.glGenBuffers();
         texture = GL11.glGenTextures();
      }

      FloatBuffer var1 = MemoryUtil.memAllocFloat(var0.length).put(var0).flip();
      GL15.glBindBuffer(35882, buffer);
      GL15.glBufferData(35882, var1, 35044);
      GL15.glBindBuffer(35882, 0);
      MemoryUtil.memFree(var1);
      GL13.glActiveTexture(34028);
      GL11.glBindTexture(35882, texture);
      GL31.glTexBuffer(35882, 34836, buffer);
      GL13.glActiveTexture(33984);
      uploaded = var0;
   }

   private TreeKinds() {
   }
}
