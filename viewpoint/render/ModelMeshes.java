package viewpoint.render;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL43;
import viewpoint.platform.Pool;
import zombie.core.skinnedmodel.model.ModelMesh;
import zombie.core.skinnedmodel.model.VertexBufferObject;
import zombie.core.skinnedmodel.model.VertexBufferObject.Vbo;
import zombie.core.skinnedmodel.model.VertexBufferObject.VertexElement;
import zombie.core.skinnedmodel.model.VertexBufferObject.VertexFormat;

final class ModelMeshes {
   static final int POSITION = 0;
   static final int NORMAL = 1;
   static final int WEIGHTS = 2;
   static final int BONES = 3;
   static final int UV = 4;
   static final int UV2 = 5;
   static final int DRAW = 6;
   private static final int INDEX_BYTES = 4;
   private static final int START_VERTICES = 65536;
   private static final int START_INDICES = 262144;
   private static final IdentityHashMap<ModelMesh, ModelMeshes.Held> held = new IdentityHashMap<>();
   private static final HashMap<Long, ModelMeshes.Layout> layouts = new HashMap<>();
   static final Set<ModelMesh> refused = ConcurrentHashMap.newKeySet();
   private static final Ranges ids = new Ranges();
   private static int nextId;
   private static long used;
   private static long capacity;
   private static final Field HANDLE;
   private static final Field FORMAT;

   static boolean available() {
      return HANDLE != null;
   }

   static ModelMeshes.Held get(ModelMesh var0, long var1) {
      VertexBufferObject var3 = var0.vb;
      Vbo var4 = var3 == null ? null : handle(var3);
      if (var4 != null && var4.vboId != 0 && var4.eboId != 0 && var4.numElements > 0) {
         ModelMeshes.Held var5 = held.get(var0);
         if (var5 != null && var5.sourceVbo == var4.vboId && var5.sourceEbo == var4.eboId && var5.modification == var0.modificationCount) {
            var5.used = var1;
            return var5;
         }

         if (var5 != null) {
            letGo(held.remove(var0));
         }

         var5 = copy(var0, var3, var4);
         if (var5 != null) {
            var5.used = var1;
            held.put(var0, var5);
         }

         return var5;
      } else {
         return null;
      }
   }

   static ModelMeshes.Source source(ModelMesh var0) {
      VertexBufferObject var1 = var0.vb;
      Vbo var2 = var1 != null && HANDLE != null ? handle(var1) : null;
      if (var2 != null && var2.vboId != 0 && var2.eboId != 0 && var2.numElements > 0) {
         int[] var3;
         try {
            var3 = offsets((VertexFormat)FORMAT.get(var1));
         } catch (ReflectiveOperationException | RuntimeException var5) {
            return null;
         }

         return var3[0] >= 0 && var3[4] >= 0 && var2.vertexStride > 0
            ? new ModelMeshes.Source(var2.vboId, var2.eboId, var2.vertexStride, var2.numElements, var3[0], var3[4])
            : null;
      } else {
         return null;
      }
   }

   private static Vbo handle(VertexBufferObject var0) {
      try {
         return (Vbo)HANDLE.get(var0);
      } catch (ReflectiveOperationException | RuntimeException var2) {
         return null;
      }
   }

   private static ModelMeshes.Held copy(ModelMesh var0, VertexBufferObject var1, Vbo var2) {
      int[] var3;
      try {
         var3 = offsets((VertexFormat)FORMAT.get(var1));
      } catch (ReflectiveOperationException | RuntimeException var10) {
         return null;
      }

      if (var3[0] >= 0 && var3[4] >= 0 && var2.vertexStride > 0) {
         ModelMeshes.Layout var4 = layout(var2.vertexStride, var3);
         int var5 = (int)(size(var2.vboId) / var2.vertexStride);
         int var6 = var2.numElements;
         int var7 = take(var4, var5, true);
         int var8 = var7 < 0 ? -1 : take(var4, var6, false);
         if (var8 < 0) {
            if (var7 >= 0) {
               var4.vertexRoom.add(var7, var5);
            }

            return null;
         } else {
            copyInto(var2.vboId, var4.vbo, (long)var7 * var4.stride, (long)var5 * var4.stride);
            copyInto(var2.eboId, var4.ebo, var8 * 4L, var6 * 4L);
            ModelMeshes.Held var9 = new ModelMeshes.Held();
            var9.layout = var4;
            var9.firstVertex = var7;
            var9.vertices = var5;
            var9.firstIndex = var8;
            var9.elements = var6;
            var9.sourceVbo = var2.vboId;
            var9.sourceEbo = var2.eboId;
            var9.modification = var0.modificationCount;
            var9.id = id();
            used = used + (long)var5 * var4.stride + var6 * 4L;
            return var9;
         }
      } else {
         refused.add(var0);
         return null;
      }
   }

   private static ModelMeshes.Layout layout(int var0, int[] var1) {
      long var2 = var0;

      for (int var7 : var1) {
         var2 = var2 * 257L + var7 + 1L;
      }

      ModelMeshes.Layout var8 = layouts.get(var2);
      if (var8 == null) {
         var8 = new ModelMeshes.Layout(var0, var1, layouts.size());
         var8.vbo = GL15.glGenBuffers();
         var8.ebo = GL15.glGenBuffers();
         var8.vao = GL30.glGenVertexArrays();
         layouts.put(var2, var8);
      }

      return var8;
   }

   private static int take(ModelMeshes.Layout var0, int var1, boolean var2) {
      Ranges var3 = var2 ? var0.vertexRoom : var0.indexRoom;
      int var4 = var3.take(var1);
      if (var4 >= 0) {
         return var4;
      }

      int var5 = var2 ? var0.vertexCapacity : var0.indexCapacity;
      int var6 = var2 ? var0.stride : 4;
      long var7 = Math.max(Math.max(var5 * 2L, (long)var5 + var1), var2 ? 65536L : 262144L);
      int var9 = (int)Math.min(var7, var5 + (Pool.MODEL_MESHES.cap() - capacity) / var6);
      if (var9 < var5 + var1) {
         return -1;
      }

      grow(var0, var2, var5, var9);
      capacity += (long)(var9 - var5) * var6;
      var3.add(var5, var9 - var5);
      return var3.take(var1);
   }

   private static void grow(ModelMeshes.Layout var0, boolean var1, int var2, int var3) {
      int var4 = var1 ? var0.stride : 4;
      int var5 = var1 ? var0.vbo : var0.ebo;
      int var6 = GL15.glGenBuffers();
      GL15.glBindBuffer(36663, var6);
      GL15.glBufferData(36663, (long)var3 * var4, 35044);
      if (var2 > 0) {
         GL15.glBindBuffer(36662, var5);
         GL31.glCopyBufferSubData(36662, 36663, 0L, 0L, (long)var2 * var4);
         GL15.glBindBuffer(36662, 0);
      }

      GL15.glBindBuffer(36663, 0);
      GL15.glDeleteBuffers(var5);
      if (var1) {
         var0.vbo = var6;
         var0.vertexCapacity = var3;
      } else {
         var0.ebo = var6;
         var0.indexCapacity = var3;
      }

      point(var0);
   }

   private static void point(ModelMeshes.Layout var0) {
      GL30.glBindVertexArray(var0.vao);
      GL15.glBindBuffer(34962, var0.vbo);
      int var1 = var0.stride;
      int[] var2 = var0.offsets;
      boolean var3 = var2[2] >= 0 && var2[3] >= 0;
      attribute(0, 3, var1, var2[0]);
      attribute(1, 3, var1, var2[1]);
      attribute(2, 4, var1, var3 ? var2[2] : -1);
      attribute(3, 4, var1, var3 ? var2[3] : -1);
      attribute(4, 2, var1, var2[4]);
      attribute(5, 2, var1, var2[5]);
      GL43.glVertexAttribIFormat(6, 1, 5124, 0);
      GL43.glVertexAttribBinding(6, 6);
      GL43.glVertexBindingDivisor(6, 1);
      GL20.glEnableVertexAttribArray(6);
      GL15.glBindBuffer(34963, var0.ebo);
      GL30.glBindVertexArray(0);
      GL15.glBindBuffer(34962, 0);
   }

   static int[] offsets(VertexFormat var0) {
      int[] var1 = new int[]{-1, -1, -1, -1, -1, -1};

      for (int var2 = 0; var2 < var0.getNumElements(); var2++) {
         VertexElement var3 = var0.getElement(var2);

         byte var4 = switch (var3.type) {
            case VertexArray -> 0;
            case NormalArray -> 1;
            case BlendWeightArray -> 2;
            case BlendIndexArray -> 3;
            case TextureCoordArray -> 4;
            default -> -1;
         };
         if (var4 == 4 && var1[4] >= 0) {
            var4 = 5;
         }

         if (var4 >= 0 && var1[var4] < 0) {
            var1[var4] = var3.byteOffset;
         }
      }

      return var1;
   }

   private static void attribute(int var0, int var1, int var2, int var3) {
      if (var3 < 0) {
         GL20.glDisableVertexAttribArray(var0);
         GL20.glVertexAttrib4f(var0, 0.0F, var0 == 1 ? 1.0F : 0.0F, 0.0F, 0.0F);
      } else {
         GL20.glVertexAttribPointer(var0, var1, 5126, false, var2, var3);
         GL20.glEnableVertexAttribArray(var0);
      }
   }

   private static void copyInto(int var0, int var1, long var2, long var4) {
      GL15.glBindBuffer(36662, var0);
      GL15.glBindBuffer(36663, var1);
      GL31.glCopyBufferSubData(36662, 36663, 0L, var2, var4);
      GL15.glBindBuffer(36662, 0);
      GL15.glBindBuffer(36663, 0);
   }

   static long size(int var0) {
      GL15.glBindBuffer(36662, var0);
      long var1 = GL15.glGetBufferParameteri(36662, 34660);
      GL15.glBindBuffer(36662, 0);
      return var1;
   }

   private static int id() {
      int var0 = ids.take(1);
      return var0 >= 0 ? var0 : nextId++;
   }

   private static void letGo(ModelMeshes.Held var0) {
      used = used - ((long)var0.vertices * var0.layout.stride + var0.elements * 4L);
      Retirement.retireDrawing(() -> {
         var0.layout.vertexRoom.add(var0.firstVertex, var0.vertices);
         var0.layout.indexRoom.add(var0.firstIndex, var0.elements);
         ids.add(var0.id, 1);
      });
   }

   static void clear() {
      for (ModelMeshes.Held var1 : held.values()) {
         letGo(var1);
      }

      held.clear();
      refused.clear();
      Pool.MODEL_MESHES.use(used);
   }

   static void makeRoom(long var0) {
      Pool.MODEL_MESHES.use(used);
      if (Pool.MODEL_MESHES.over(0.9)) {
         ArrayList var2 = new ArrayList();

         for (Entry var4 : held.entrySet()) {
            if (((ModelMeshes.Held)var4.getValue()).used < var0) {
               var2.add((ModelMesh)var4.getKey());
            }
         }

         var2.sort((var0x, var1) -> Long.compare(held.get(var0x).used, held.get(var1).used));

         for (int var5 = 0; var5 < var2.size() && Pool.MODEL_MESHES.over(0.8); var5++) {
            letGo(held.remove(var2.get(var5)));
            Pool.MODEL_MESHES.use(used);
         }
      }
   }

   private ModelMeshes() {
   }

   static {
      Field var0 = null;
      Field var1 = null;

      try {
         var0 = VertexBufferObject.class.getDeclaredField("handle");
         var1 = VertexBufferObject.class.getDeclaredField("vertexFormat");
         var0.setAccessible(true);
         var1.setAccessible(true);
      } catch (ReflectiveOperationException | RuntimeException var3) {
         System.out.println("[Viewpoint] models: the game's vertex buffers cannot be read (models stay the game's): " + var3);
         var1 = null;
         var0 = null;
      }

      HANDLE = var0;
      FORMAT = var1;
   }

   static final class Held {
      ModelMeshes.Layout layout;
      int firstVertex;
      int vertices;
      int firstIndex;
      int elements;
      int sourceVbo;
      int sourceEbo;
      int modification;
      int id;
      long used;
   }

   static final class Layout {
      final int stride;
      final int index;
      final int[] offsets;
      final Ranges vertexRoom = new Ranges();
      final Ranges indexRoom = new Ranges();
      int vao;
      int vbo;
      int ebo;
      int vertexCapacity;
      int indexCapacity;

      Layout(int var1, int[] var2, int var3) {
         this.stride = var1;
         this.offsets = var2;
         this.index = var3;
      }

      long bytes() {
         return (long)this.vertexCapacity * this.stride + this.indexCapacity * 4L;
      }
   }

   record Source(int vbo, int ebo, int stride, int elements, int position, int uv) {
   }
}
