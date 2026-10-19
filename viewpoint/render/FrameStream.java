package viewpoint.render;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL44;

final class FrameStream {
   private static final long START_BYTES = 8388608L;
   private static int buffer;
   private static int textureAlignment;
   private static long size;
   private static long head;
   private static long frameBytes;

   static int buffer() {
      return buffer;
   }

   static void frameStarted() {
      frameBytes = 0L;
   }

   static long put(FloatBuffer var0, int var1) {
      long var2 = reserve(var0.remaining() * 4L, var1);
      GL15.glBindBuffer(36663, buffer);
      GL15.glBufferSubData(36663, var2, var0);
      GL15.glBindBuffer(36663, 0);
      return var2;
   }

   static long put(IntBuffer var0, int var1) {
      long var2 = reserve(var0.remaining() * 4L, var1);
      GL15.glBindBuffer(36663, buffer);
      GL15.glBufferSubData(36663, var2, var0);
      GL15.glBindBuffer(36663, 0);
      return var2;
   }

   static long put(ByteBuffer var0, int var1) {
      long var2 = reserve(var0.remaining(), var1);
      GL15.glBindBuffer(36663, buffer);
      GL15.glBufferSubData(36663, var2, var0);
      GL15.glBindBuffer(36663, 0);
      return var2;
   }

   static int textureAlignment() {
      if (textureAlignment == 0) {
         textureAlignment = Math.max(4, GL11.glGetInteger(37279));
      }

      return textureAlignment;
   }

   private static long reserve(long var0, int var2) {
      long var3 = frameBytes + var0 + var2;
      if (buffer == 0 || var3 * 4L > size) {
         grow(Math.max(8388608L, Long.highestOneBit(var3 * 4L) * 2L));
      }

      long var5 = (head + var2 - 1L) / var2 * var2;
      if (var5 + var0 > size) {
         var5 = 0L;
      }

      head = var5 + var0;
      frameBytes += var0 + var2;
      return var5;
   }

   private static void grow(long var0) {
      if (buffer != 0) {
         int var2 = buffer;
         Retirement.retireDrawing(() -> GL15.glDeleteBuffers(var2));
         System.out.println("[Viewpoint] frame stream grown to " + (var0 >> 20) + " MB");
      }

      buffer = GL15.glGenBuffers();
      GL15.glBindBuffer(36663, buffer);
      GL44.glBufferStorage(36663, var0, 256);
      GL15.glBindBuffer(36663, 0);
      size = var0;
      head = 0L;
   }

   private FrameStream() {
   }
}
