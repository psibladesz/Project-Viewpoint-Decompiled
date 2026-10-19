package viewpoint.render;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryUtil;
import zombie.core.skinnedmodel.model.ModelMesh;
import zombie.core.textures.Texture;

public final class LampReadbacks {
   private static final int IN_FLIGHT = 2;
   private static final ConcurrentLinkedQueue<LampReadbacks.Read> read = new ConcurrentLinkedQueue<>();
   private final ArrayList<LampReadbacks.Flight> flights = new ArrayList<>();

   void update(SceneData var1) {
      for (int var2 = this.flights.size() - 1; var2 >= 0; var2--) {
         LampReadbacks.Flight var3 = this.flights.get(var2);
         int var4 = GL32.glClientWaitSync(var3.fence, 0, 0L);
         if (var4 == 37146 || var4 == 37148) {
            this.flights.remove(var2);
            read.add(take(var3));
         }
      }

      for (LampReadbacks.Ask var6 : var1.spots.lampAsks) {
         if (this.flights.size() >= 2) {
            break;
         }

         LampReadbacks.Flight var7 = start(var6);
         if (var7 == null) {
            read.add(LampReadbacks.Read.none(var6.key()));
         } else {
            this.flights.add(var7);
         }
      }
   }

   private static LampReadbacks.Flight start(LampReadbacks.Ask var0) {
      ModelMeshes.Source var1 = ModelMeshes.source(var0.mesh());
      int var2 = ready(var0.mask());
      int var3 = ready(var0.lights());
      if (var1 != null && var2 != 0) {
         LampReadbacks.Flight var4 = new LampReadbacks.Flight();
         var4.ask = var0;
         var4.source = var1;
         long var5 = ModelMeshes.size(var1.vbo());
         var4.vertices = (int)(var5 / var1.stride());
         var4.indexAt = var5;
         var4.maskWidth = GL45.glGetTextureLevelParameteri(var2, 0, 4096);
         var4.maskHeight = GL45.glGetTextureLevelParameteri(var2, 0, 4097);
         var4.maskAt = var4.indexAt + var1.elements() * 4L;
         var4.lightsWidth = var3 == 0 ? 0 : GL45.glGetTextureLevelParameteri(var3, 0, 4096);
         var4.lightsHeight = var3 == 0 ? 0 : GL45.glGetTextureLevelParameteri(var3, 0, 4097);
         var4.lightsAt = var4.maskAt + 4L * var4.maskWidth * var4.maskHeight;
         var4.bytes = var4.lightsAt + 4L * var4.lightsWidth * var4.lightsHeight;
         if (var4.vertices > 0 && var4.maskWidth > 0 && var4.maskHeight > 0 && var4.bytes <= 2147483647L) {
            var4.buffer = GL45.glCreateBuffers();
            GL45.glNamedBufferStorage(var4.buffer, var4.bytes, 512);
            GL45.glCopyNamedBufferSubData(var1.vbo(), var4.buffer, 0L, 0L, var5);
            GL45.glCopyNamedBufferSubData(var1.ebo(), var4.buffer, 0L, var4.indexAt, var4.maskAt - var4.indexAt);
            GL15.glBindBuffer(35051, var4.buffer);
            GL45.glGetTextureImage(var2, 0, 6408, 5121, (int)(var4.lightsAt - var4.maskAt), var4.maskAt);
            if (var3 != 0) {
               GL45.glGetTextureImage(var3, 0, 6408, 5121, (int)(var4.bytes - var4.lightsAt), var4.lightsAt);
            }

            GL15.glBindBuffer(35051, 0);
            var4.fence = GL32.glFenceSync(37143, 0);
            return var4;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static int ready(Texture var0) {
      return var0 != null && var0.isReady() ? Math.max(0, var0.getID()) : 0;
   }

   private static LampReadbacks.Read take(LampReadbacks.Flight var0) {
      GL32.glDeleteSync(var0.fence);
      ByteBuffer var1 = MemoryUtil.memAlloc((int)var0.bytes);

      try {
         GL45.glGetNamedBufferSubData(var0.buffer, 0L, var1);
         GL15.glDeleteBuffers(var0.buffer);
         return parse(var0, var1);
      } finally {
         MemoryUtil.memFree(var1);
      }
   }

   private static LampReadbacks.Read parse(LampReadbacks.Flight var0, ByteBuffer var1) {
      ModelMeshes.Source var2 = var0.source;
      float[] var3 = new float[3 * var0.vertices];
      float[] var4 = new float[2 * var0.vertices];

      for (int var5 = 0; var5 < var0.vertices; var5++) {
         int var6 = var5 * var2.stride();

         for (int var7 = 0; var7 < 3; var7++) {
            var3[3 * var5 + var7] = var1.getFloat(var6 + var2.position() + 4 * var7);
         }

         var4[2 * var5] = var1.getFloat(var6 + var2.uv());
         var4[2 * var5 + 1] = var1.getFloat(var6 + var2.uv() + 4);
      }

      int[] var8 = new int[var2.elements()];

      for (int var9 = 0; var9 < var8.length; var9++) {
         var8[var9] = var1.getInt((int)var0.indexAt + 4 * var9);
         if (var8[var9] < 0 || var8[var9] >= var0.vertices) {
            return LampReadbacks.Read.none(var0.ask.key());
         }
      }

      byte[] var10 = copy(var1, var0.maskAt, var0.lightsAt);
      byte[] var11 = var0.lightsWidth == 0 ? null : copy(var1, var0.lightsAt, var0.bytes);
      return new LampReadbacks.Read(var0.ask.key(), var3, var4, var8, var10, var0.maskWidth, var0.maskHeight, var11, var0.lightsWidth, var0.lightsHeight);
   }

   private static byte[] copy(ByteBuffer var0, long var1, long var3) {
      byte[] var5 = new byte[(int)(var3 - var1)];
      var0.get((int)var1, var5);
      return var5;
   }

   public static LampReadbacks.Read taken() {
      return read.poll();
   }

   public record Ask(Object key, ModelMesh mesh, Texture mask, Texture lights) {
   }

   private static final class Flight {
      LampReadbacks.Ask ask;
      ModelMeshes.Source source;
      int buffer;
      int vertices;
      int maskWidth;
      int maskHeight;
      int lightsWidth;
      int lightsHeight;
      long fence;
      long indexAt;
      long maskAt;
      long lightsAt;
      long bytes;
   }

   public record Read(
      Object key, float[] positions, float[] uvs, int[] indices, byte[] mask, int maskWidth, int maskHeight, byte[] lights, int lightsWidth, int lightsHeight
   ) {
      static LampReadbacks.Read none(Object var0) {
         return new LampReadbacks.Read(var0, null, null, null, null, 0, 0, null, 0, 0);
      }
   }
}
