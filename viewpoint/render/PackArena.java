package viewpoint.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44;
import org.lwjgl.system.MemoryUtil;

final class PackArena {
   static final int PAGE_VERTICES = 131072;
   static final int PAGE_INDICES = 1048576;
   static final int VERTEX_BYTES = 32;
   static final int INDEX_BYTES = 4;
   static final long PAGE_BYTES = 8388608L;
   static final int POSITION = 0;
   static final int UV = 1;
   static final int NORMAL = 3;
   static final int RUN = 9;
   static final int RUN_INTS = 4;
   private static final int RUNS = 9;
   private static final int ONE_RECORD = 1073741824;
   private static final ArrayList<PackArena.Page> pages = new ArrayList<>();

   static boolean put(PackModel var0, PackData var1, long var2) {
      int var4 = var1.vertices().length / 8;
      int var5 = var1.indices().length;
      if (var4 <= 131072 && var5 <= 1048576) {
         for (int var6 = 0; var6 < pages.size(); var6++) {
            if (place(var0, var1, var6, var4, var5)) {
               return true;
            }
         }

         if (var2 < 8388608L) {
            return false;
         }

         pages.add(page());
         return place(var0, var1, pages.size() - 1, var4, var5);
      } else {
         return false;
      }
   }

   static void free(PackModel var0) {
      PackArena.Page var1 = pages.get(var0.meshPage);
      var1.vertexRoom.add(var0.firstVertex, var0.vertices);
      var1.indexRoom.add(var0.firstIndex, var0.indices);
   }

   static long bytes() {
      return pages.size() * 8388608L;
   }

   static int pages() {
      return pages.size();
   }

   static void bind(int var0, int var1, long var2) {
      GL30.glBindVertexArray(pages.get(var0).vao);
      GL43.glBindVertexBuffer(9, var1, var2, 16);
   }

   private static boolean place(PackModel var0, PackData var1, int var2, int var3, int var4) {
      PackArena.Page var5 = pages.get(var2);
      int var6 = var5.vertexRoom.take(var3);
      if (var6 < 0) {
         return false;
      }

      int var7 = var5.indexRoom.take(var4);
      if (var7 < 0) {
         var5.vertexRoom.add(var6, var3);
         return false;
      }

      FloatBuffer var8 = MemoryUtil.memAllocFloat(var1.vertices().length).put(var1.vertices()).flip();
      IntBuffer var9 = MemoryUtil.memAllocInt(var4).put(var1.indices()).flip();

      try {
         GL15.glBindBuffer(34962, var5.vertices);
         GL15.glBufferSubData(34962, var6 * 32L, var8);
         GL15.glBindBuffer(34962, var5.indices);
         GL15.glBufferSubData(34962, var7 * 4L, var9);
         GL15.glBindBuffer(34962, 0);
      } finally {
         MemoryUtil.memFree(var8);
         MemoryUtil.memFree(var9);
      }

      var0.meshPage = var2;
      var0.firstVertex = var6;
      var0.vertices = var3;
      var0.firstIndex = var7;
      var0.indices = var4;
      return true;
   }

   private static PackArena.Page page() {
      int var0 = GL15.glGenBuffers();
      int var1 = GL15.glGenBuffers();
      int var2 = GL30.glGenVertexArrays();
      GL15.glBindBuffer(34962, var0);
      GL44.glBufferStorage(34962, 4194304L, 256);
      GL15.glBindBuffer(34962, var1);
      GL44.glBufferStorage(34962, 4194304L, 256);
      GL30.glBindVertexArray(var2);
      GL15.glBindBuffer(34962, var0);
      attribute(0, 3, 0L);
      attribute(3, 3, 12L);
      attribute(1, 2, 24L);
      GL15.glBindBuffer(34963, var1);
      GL20.glEnableVertexAttribArray(9);
      GL43.glVertexAttribIFormat(9, 4, 5124, 0);
      GL43.glVertexAttribBinding(9, 9);
      GL43.glVertexBindingDivisor(9, 1073741824);
      GL30.glBindVertexArray(0);
      GL15.glBindBuffer(34962, 0);
      System.out.println("[Viewpoint] model packs: mesh page " + (pages.size() + 1) + " made");
      return new PackArena.Page(var2, var0, var1);
   }

   private static void attribute(int var0, int var1, long var2) {
      GL20.glEnableVertexAttribArray(var0);
      GL20.glVertexAttribPointer(var0, var1, 5126, false, 32, var2);
   }

   private PackArena() {
   }

   private static final class Page {
      final int vao;
      final int vertices;
      final int indices;
      final Ranges vertexRoom = new Ranges();
      final Ranges indexRoom = new Ranges();

      Page(int var1, int var2, int var3) {
         this.vao = var1;
         this.vertices = var2;
         this.indices = var3;
         this.vertexRoom.add(0, 131072);
         this.indexRoom.add(0, 1048576);
      }
   }
}
