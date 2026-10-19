package viewpoint.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44;
import org.lwjgl.system.MemoryUtil;
import viewpoint.platform.DrawCounts;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Pool;
import zombie.core.textures.TextureID;

final class ShellArena {
   static final int VERTEX_BYTES = 40;
   private static final int RECORD_FLOATS = 8;
   private static final int LOCATION_DRAW = 6;
   private static final int DRAWS = 6;
   private static final int SPAN_INTS = 5;
   private static final long PAGE_BYTES = 67108864L;
   private static ShellArena.Page[] pages = new ShellArena.Page[0];
   private static int pageVertices;
   private static int recordBuffer;
   private static int commandBuffer;
   private static int instanceBuffer;
   private static int recordTexture;
   private static long recordsAt;
   private static long recordBytes;
   private static long commandsAt;
   private static long instancesAt;
   private static FloatBuffer records = BufferUtils.createFloatBuffer(8192);
   private static int recordCount;
   static IntBuffer commands = BufferUtils.createIntBuffer(16384);
   static IntBuffer instances = BufferUtils.createIntBuffer(4096);
   static final ShellArena.IntList spans = new ShellArena.IntList();
   static final ArrayList<Object> keys = new ArrayList<>();
   private static int atlasUnit;
   private static int atlasesBound;

   static ByteBuffer pack(FloatBuffer var0) {
      int var1 = var0.remaining() / 14;
      ByteBuffer var2 = MemoryUtil.memAlloc(var1 * 40).order(ByteOrder.LITTLE_ENDIAN);
      int var3 = var0.position();

      for (int var4 = 0; var4 < var1; var4++) {
         int var5 = var3 + var4 * 14;
         var2.putFloat(var0.get(var5)).putFloat(var0.get(var5 + 1)).putFloat(var0.get(var5 + 2));
         var2.putFloat(var0.get(var5 + 3)).putFloat(var0.get(var5 + 4));

         for (int var6 = 5; var6 < 9; var6++) {
            var2.putShort((short)Math.round(Math.max(0.0F, Math.min(1.0F, var0.get(var5 + var6))) * 65535.0F));
         }

         for (int var7 = 9; var7 < 12; var7++) {
            var2.put((byte)Math.round(Math.max(-1.0F, Math.min(1.0F, var0.get(var5 + var7))) * 127.0F));
         }

         var2.put((byte)0);
         var2.putFloat(var0.get(var5 + 12));
         var2.putInt(Float.floatToRawIntBits(var0.get(var5 + 13)));
      }

      return var2.flip();
   }

   static int upload(ByteBuffer var0, int var1) {
      if (pageVertices == 0) {
         pageVertices = (int)Math.min(67108864L, Pool.SHELL.cap()) / 40;
      }

      int var2 = (int)Math.max(1L, Pool.SHELL.cap() / pageBytes());
      if (var2 > pages.length) {
         pages = Arrays.copyOf(pages, var2);
      }

      ShellArena.Page var3 = null;
      int var4 = -1;

      for (int var5 = 0; var5 < pages.length && var4 < 0; var5++) {
         var3 = pages[var5];
         var4 = var3 == null ? -1 : var3.free.take(var1);
      }

      for (int var6 = 0; var6 < pages.length && var4 < 0 && var1 <= pageVertices; var6++) {
         if (pages[var6] == null && madeBytes() + pageBytes() <= Pool.SHELL.cap()) {
            var3 = newPage(var6);
            var4 = var3.free.take(var1);
         }
      }

      if (var4 < 0) {
         return -1;
      }

      GL15.glBindBuffer(34962, var3.vertexBuffer);
      GL15.glBufferSubData(34962, var4 * 40L, var0);
      GL15.glBindBuffer(34962, 0);
      var3.used += var1;
      return var3.slot * pageVertices + var4;
   }

   static void free(int var0, int var1) {
      ShellArena.Page var2 = pages[var0 / pageVertices];
      var2.free.add(var0 % pageVertices, var1);
      var2.used -= var1;
      if (var2.used == 0 && emptyPages() > 1) {
         letGo(var2);
      }
   }

   private static ShellArena.Page newPage(int var0) {
      int var1 = GL11.glGetInteger(34964);
      int var2 = GL15.glGenBuffers();
      GL15.glBindBuffer(34962, var2);
      GL44.glBufferStorage(34962, pageBytes(), 256);
      int var3 = GL30.glGenVertexArrays();
      pointAttributes(var3, var2);
      GL15.glBindBuffer(34962, var1);
      pages[var0] = new ShellArena.Page(var0, var2, var3);
      System.out.println("[Viewpoint] shell arena: page " + var0 + " made (" + (pageBytes() >> 20) + " MB), " + (madeBytes() >> 20) + " MB in all");
      return pages[var0];
   }

   private static void letGo(ShellArena.Page var0) {
      GL15.glDeleteBuffers(var0.vertexBuffer);
      GL30.glDeleteVertexArrays(var0.vao);
      pages[var0.slot] = null;
      System.out.println("[Viewpoint] shell arena: page " + var0.slot + " let go, " + (madeBytes() >> 20) + " MB in all");
   }

   private static long pageBytes() {
      return pageVertices * 40L;
   }

   private static long madeBytes() {
      long var0 = 0L;

      for (ShellArena.Page var5 : pages) {
         var0 += var5 == null ? 0L : pageBytes();
      }

      return var0;
   }

   private static int emptyPages() {
      byte var0 = 0;

      for (ShellArena.Page var4 : pages) {
         var0 += var4 != null && var4.used == 0 ? 1 : 0;
      }

      return var0;
   }

   private static void pointAttributes(int var0, int var1) {
      GL30.glBindVertexArray(var0);
      GL15.glBindBuffer(34962, var1);
      GL20.glEnableVertexAttribArray(0);
      GL20.glVertexAttribPointer(0, 3, 5126, false, 40, 0L);
      GL20.glEnableVertexAttribArray(1);
      GL20.glVertexAttribPointer(1, 2, 5126, false, 40, 12L);
      GL20.glEnableVertexAttribArray(2);
      GL20.glVertexAttribPointer(2, 4, 5123, true, 40, 20L);
      GL20.glEnableVertexAttribArray(3);
      GL20.glVertexAttribPointer(3, 3, 5120, true, 40, 28L);
      GL20.glEnableVertexAttribArray(4);
      GL20.glVertexAttribPointer(4, 1, 5126, false, 40, 32L);
      GL20.glEnableVertexAttribArray(5);
      GL30.glVertexAttribIPointer(5, 1, 5124, 40, 36L);
      GL20.glEnableVertexAttribArray(6);
      GL43.glVertexAttribIFormat(6, 1, 5124, 0);
      GL43.glVertexAttribBinding(6, 6);
      GL43.glVertexBindingDivisor(6, 1);
      GL30.glBindVertexArray(0);
      GL15.glBindBuffer(34962, 0);
   }

   static void begin() {
      records.clear();
      recordCount = 0;

      for (ShellArena.Page var3 : pages) {
         if (var3 != null) {
            for (ShellArena.IntList var5 : var3.byAtlas.values()) {
               var5.n = 0;
            }

            var3.atlases.clear();
            var3.arrays.clear();
         }
      }
   }

   static int record(float var0, float var1, float var2, float var3, float var4, int var5, float var6, float var7) {
      if (records.remaining() < 8) {
         FloatBuffer var8 = BufferUtils.createFloatBuffer(records.capacity() * 2);
         records.flip();
         var8.put(records);
         records = var8;
      }

      records.put(var0).put(var1).put(var2).put(var5).put(var3).put(var4).put(var6).put(var7);
      return recordCount++;
   }

   static void command(FarGpu.Shell var0, int var1, int var2, int var3, int var4) {
      ShellArena.Page var5 = pages[var2 / pageVertices];
      if (var0.lists == null) {
         var0.lists = new ShellArena.IntList[var0.pages.length];
      }

      if (var0.lists[var1] == null) {
         var0.lists[var1] = list(var5, var0.pages[var1]);
      }

      add(var0.lists[var1], var5, var0.pages[var1], 0, var2, var3, var4);
   }

   static void ground(Object var0, int var1, int var2, int var3, int var4) {
      ShellArena.Page var5 = pages[var2 / pageVertices];
      add(list(var5, var0), var5, var0, var1, var2, var3, var4);
   }

   private static ShellArena.IntList list(ShellArena.Page var0, Object var1) {
      ShellArena.IntList var2 = var0.byAtlas.get(var1);
      if (var2 == null) {
         var2 = new ShellArena.IntList();
         var0.byAtlas.put(var1, var2);
      }

      return var2;
   }

   private static void add(ShellArena.IntList var0, ShellArena.Page var1, Object var2, int var3, int var4, int var5, int var6) {
      if (var0.n == 0) {
         var1.atlases.add(var2);
         if (var3 != 0) {
            var1.arrays.put(var2, var3);
         }
      }

      var0.add(var5);
      var0.add(1);
      var0.add(var4 % pageVertices);
      var0.add(var6);
   }

   static void samplers(GlProgram var0) {
      var0.sampler("uShellRecords", 94);
      AtlasGroups.samplers(var0);
   }

   static void draw(GlProgram var0) {
      if (recordCount != 0) {
         records.flip();
         recordBytes = records.remaining() * 4L;
         recordsAt = FrameStream.put(records, Math.max(16, FrameStream.textureAlignment()));
         recordBuffer = FrameStream.buffer();
         int var1 = 0;

         for (ShellArena.Page var5 : pages) {
            for (int var6 = 0; var5 != null && var6 < var5.atlases.size(); var6++) {
               var1 += var5.byAtlas.get(var5.atlases.get(var6)).n / 4;
            }
         }

         if (instances.capacity() < var1) {
            commands = BufferUtils.createIntBuffer(var1 * 8);
            instances = BufferUtils.createIntBuffer(var1 * 2);
         }

         commands.clear();
         instances.clear();
         spans.n = 0;
         keys.clear();
         atlasUnit = AtlasGroups.unit(var0);
         int var7 = AtlasGroups.size(var0);

         for (ShellArena.Page var11 : pages) {
            if (var11 != null) {
               arrange(var11, var7);
            }
         }

         commands.flip();
         instances.flip();
         commandsAt = FrameStream.put(commands, 4);
         commandBuffer = FrameStream.buffer();
         instancesAt = FrameStream.put(instances, 4);
         instanceBuffer = FrameStream.buffer();
         issue();
      }
   }

   static void arrange(ShellArena.Page var0, int var1) {
      for (Object var3 : var0.atlases) {
         if (!(var3 instanceof TextureID)) {
            span(var0.slot);
            place(var3, var0.byAtlas.get(var3), 0);
         }
      }

      int var5 = 0;

      for (Object var4 : var0.atlases) {
         if (var4 instanceof TextureID) {
            if (var5 == 0) {
               span(var0.slot);
            }

            place(var4, var0.byAtlas.get(var4), var5);
            var5 = (var5 + 1) % var1;
         }
      }
   }

   private static void span(int var0) {
      spans.add(var0);
      spans.add(keys.size());
      spans.add(0);
      spans.add(commands.position() / 4);
      spans.add(0);
   }

   private static void place(Object var0, ShellArena.IntList var1, int var2) {
      keys.add(var0);
      spans.a[spans.n - 3]++;
      spans.a[spans.n - 1] = spans.a[spans.n - 1] + var1.n / 4;

      for (int var3 = 0; var3 < var1.n; var3 += 4) {
         commands.put(var1.a[var3]).put(1).put(var1.a[var3 + 2]).put(instances.position());
         instances.put(var1.a[var3 + 3] | var2 << 24);
      }
   }

   static void drawAgain() {
      if (spans.n > 0) {
         issue();
      }
   }

   private static void issue() {
      GL15.glBindBuffer(36671, commandBuffer);
      bindRecords(recordBytes);
      DrawCounts.pass(1);

      for (int var0 = 0; var0 < spans.n; var0 += 5) {
         ShellArena.Page var1 = pages[spans.a[var0]];
         int var2 = spans.a[var0 + 1];
         int var3 = spans.a[var0 + 2];
         GL30.glBindVertexArray(var1.vao);
         GL43.glBindVertexBuffer(6, instanceBuffer, instancesAt, 4);
         if (keys.get(var2) instanceof TextureID) {
            AtlasGroups.bind(atlasUnit, keys, var2, var3, 1);
            atlasesBound = Math.max(atlasesBound, var3);
         } else {
            Meshes.bindFloorArray(var1.arrays.get(keys.get(var2)));
         }

         GL43.glMultiDrawArraysIndirect(4, commandsAt + spans.a[var0 + 3] * 16L, spans.a[var0 + 4], 16);
         DrawCounts.draw(1);
      }

      Meshes.bindFloorArray(0);
      AtlasGroups.release(atlasUnit, atlasesBound);
      atlasesBound = 0;
      bindRecords(0L);
      GL30.glBindVertexArray(0);
      GL15.glBindBuffer(36671, 0);
   }

   private static void bindRecords(long var0) {
      if (recordTexture == 0) {
         recordTexture = GL11.glGenTextures();
      }

      GL13.glActiveTexture(34078);
      GL11.glBindTexture(35882, var0 == 0L ? 0 : recordTexture);
      if (var0 > 0L) {
         GL43.glTexBufferRange(35882, 34836, recordBuffer, recordsAt, var0);
      }

      GL13.glActiveTexture(33984);
   }

   private ShellArena() {
   }

   static final class IntList {
      int[] a = new int[64];
      int n;

      void add(int var1) {
         if (this.n == this.a.length) {
            this.a = Arrays.copyOf(this.a, this.n * 2);
         }

         this.a[this.n++] = var1;
      }
   }

   static final class Page {
      final int slot;
      final int vertexBuffer;
      final int vao;
      final Ranges free = new Ranges();
      int used;
      final IdentityHashMap<Object, ShellArena.IntList> byAtlas = new IdentityHashMap<>();
      final ArrayList<Object> atlases = new ArrayList<>();
      final IdentityHashMap<Object, Integer> arrays = new IdentityHashMap<>();

      Page(int var1, int var2, int var3) {
         this.slot = var1;
         this.vertexBuffer = var2;
         this.vao = var3;
         this.free.add(0, ShellArena.pageVertices);
      }
   }
}
