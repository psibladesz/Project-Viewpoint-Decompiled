package viewpoint.render;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;
import viewpoint.platform.DrawCounts;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Pool;
import viewpoint.platform.Profile;
import viewpoint.platform.VertexFormat;

public final class MeshArena {
   static final int RECORD_FLOATS = 12;
   private static final int LOCATION_OFFSET = 6;
   private static final int LOCATION_FADE = 7;
   private static final int LOCATION_BELOW = 8;
   private static final int RECORDS = 6;
   private static final int VERTEX_BYTES = 56;
   private static final int START_VERTICES = 1048576;
   private static final int START_ROWS = 16;
   private static int vao;
   private static int vertexBuffer;
   private static int lightArray;
   private static int plantVao;
   private static int slotTexture;
   private static int recordBuffer;
   private static int commandBuffer;
   private static long recordsAt;
   private static long commandsAt;
   private static long recordBytes;
   private static int recordsTexture;
   private static int capacity;
   private static int rows;
   private static int nextSlot;
   private static int used;
   private static int openSky = -1;
   private static final TreeMap<Integer, Integer> free = new TreeMap<>();
   private static final ArrayDeque<Integer> freeSlots = new ArrayDeque<>();
   private static FloatBuffer records = BufferUtils.createFloatBuffer(12288);
   private static IntBuffer commands = BufferUtils.createIntBuffer(16384);

   public static void init() {
      Profile.arenaUsage = MeshArena::usage;
      limitToSlots();
      int var0 = GL11.glGetInteger(34964);
      int var1 = GL11.glGetInteger(32873);
      vertexBuffer = GL15.glGenBuffers();
      GL15.glBindBuffer(34962, vertexBuffer);
      capacity = (int)Math.min(1048576L, Pool.NEAR_MESHES.cap() / 56L);
      GL15.glBufferData(34962, capacity * 56L, 35048);
      free.put(0, capacity);
      vao = GL30.glGenVertexArrays();
      pointAttributes();
      plantVao = GL30.glGenVertexArrays();
      GL30.glBindVertexArray(plantVao);
      recordAttributes();
      GL30.glBindVertexArray(0);
      slotTexture = GL11.glGenTextures();
      pointSlots();
      rows = (int)Math.min(16L, Pool.LIGHT_GRIDS.cap() / 652800L);
      lightArray = lightTexture(rows);
      openSky = -1;
      GL15.glBindBuffer(34962, var0);
      GL11.glBindTexture(3553, var1);
   }

   private static void limitToSlots() {
      long var0 = GL11.glGetInteger(35883);
      long var2 = var0 / 7L;
      if (var2 < 1048576L) {
         throw new IllegalStateException("[Viewpoint] this GPU's buffer textures reach " + var0 + " texels: the mesh arena's plants need 7340032");
      }

      Pool.NEAR_MESHES.limit(var2 * 56L);
      System.out.println("[Viewpoint] mesh arena: buffer textures reach " + var0 + " texels, " + (var2 * 56L >> 20) + " MiB of arena");
   }

   static int upload(FloatBuffer var0, int var1) {
      int var2 = allocate(var1);
      if (var2 < 0) {
         return -1;
      }

      GL15.glBindBuffer(34962, vertexBuffer);
      GL15.glBufferSubData(34962, var2 * 56L, var0);
      GL15.glBindBuffer(34962, 0);
      used += var1;
      return var2;
   }

   static void free(int var0, int var1) {
      used -= var1;
      release(var0, var1);
   }

   private static void release(int var0, int var1) {
      int var2 = var0;
      int var3 = var1;
      Entry<Integer, Integer> var4 = free.floorEntry(var0);
      if (var4 != null && (Integer)var4.getKey() + (Integer)var4.getValue() == var0) {
         var2 = (Integer)var4.getKey();
         var3 += var4.getValue();
         free.remove(var4.getKey());
      }

      Integer var5 = free.get(var0 + var1);
      if (var5 != null) {
         var3 += var5;
         free.remove(var0 + var1);
      }

      free.put(var2, var3);
   }

   private static int allocate(int var0) {
      for (Entry<Integer, Integer> var2 : free.entrySet()) {
         if ((Integer)var2.getValue() >= var0) {
            int var3 = (Integer)var2.getKey();
            int var4 = (Integer)var2.getValue() - var0;
            free.remove(var3);
            if (var4 > 0) {
               free.put(var3 + var0, var4);
            }

            return var3;
         }
      }

      int var5 = (int)Math.min(2147483647L, Pool.NEAR_MESHES.cap() / 56L);
      if (capacity >= var5) {
         return -1;
      }

      grow(Math.min(var5, Math.max(capacity * 2, capacity + var0)));
      return allocate(var0);
   }

   private static void grow(int var0) {
      int var1 = capacity;
      capacity = var0;
      int var2 = GL15.glGenBuffers();
      GL15.glBindBuffer(36663, var2);
      GL15.glBufferData(36663, capacity * 56L, 35048);
      GL15.glBindBuffer(36662, vertexBuffer);
      GL31.glCopyBufferSubData(36662, 36663, 0L, 0L, var1 * 56L);
      GL15.glBindBuffer(36662, 0);
      GL15.glBindBuffer(36663, 0);
      GL15.glDeleteBuffers(vertexBuffer);
      vertexBuffer = var2;
      release(var1, capacity - var1);
      pointAttributes();
      pointSlots();
      System.out.println("[Viewpoint] mesh arena grown to " + capacity / 1024 + "k vertices (" + (capacity * 56L >> 20) + " MB)");
   }

   private static void pointAttributes() {
      GL30.glBindVertexArray(vao);
      GL15.glBindBuffer(34962, vertexBuffer);
      long var0 = 0L;

      for (int var2 = 0; var2 < VertexFormat.ATTRIBUTE_SIZES.length; var2++) {
         GL20.glEnableVertexAttribArray(var2);
         if (var2 == 5) {
            GL30.glVertexAttribIPointer(var2, VertexFormat.ATTRIBUTE_SIZES[var2], 5124, 56, var0);
         } else {
            GL20.glVertexAttribPointer(var2, VertexFormat.ATTRIBUTE_SIZES[var2], 5126, false, 56, var0);
         }

         var0 += VertexFormat.ATTRIBUTE_SIZES[var2] * 4L;
      }

      recordAttributes();
      GL30.glBindVertexArray(0);
      GL15.glBindBuffer(34962, 0);
   }

   private static void recordAttributes() {
      GL20.glEnableVertexAttribArray(6);
      GL43.glVertexAttribFormat(6, 4, 5126, false, 0);
      GL43.glVertexAttribBinding(6, 6);
      GL20.glEnableVertexAttribArray(7);
      GL43.glVertexAttribFormat(7, 4, 5126, false, 16);
      GL43.glVertexAttribBinding(7, 6);
      GL20.glEnableVertexAttribArray(8);
      GL43.glVertexAttribFormat(8, 4, 5126, false, 32);
      GL43.glVertexAttribBinding(8, 6);
      GL43.glVertexBindingDivisor(6, 1);
   }

   private static void pointSlots() {
      GL11.glBindTexture(35882, slotTexture);
      GL31.glTexBuffer(35882, 33328, vertexBuffer);
      GL11.glBindTexture(35882, 0);
   }

   static int takeSlot() {
      if (!freeSlots.isEmpty()) {
         return freeSlots.pop();
      }

      if (nextSlot == rows * 204) {
         int var0 = (int)Math.min(GL11.glGetInteger(3379) / 40, Pool.LIGHT_GRIDS.cap() / 652800L);
         int var1 = Math.min(rows * 2, var0);
         if (var1 <= rows) {
            return -1;
         }

         int var2 = lightTexture(var1);
         GL43.glCopyImageSubData(lightArray, 3553, 0, 0, 0, 0, var2, 3553, 0, 0, 0, 0, 4080, rows * 40, 1);
         GL11.glDeleteTextures(lightArray);
         lightArray = var2;
         rows = var1;
         System.out.println("[Viewpoint] light atlas grown to " + rows * 204 + " grids");
      }

      return nextSlot++;
   }

   static void freeSlot(int var0) {
      freeSlots.push(var0);
   }

   static int openSkySlot() {
      if (openSky < 0) {
         openSky = takeSlot();
         if (openSky < 0) {
            return 0;
         }

         ByteBuffer var0 = BufferUtils.createByteBuffer(3200);

         for (int var1 = 3; var1 < 1600; var1 += 4) {
            var0.put(var1, (byte)-1);
         }

         uploadLight(openSky, var0);
      }

      return openSky;
   }

   static void uploadLight(int var0, ByteBuffer var1) {
      GL11.glPixelStorei(3317, 4);
      GL45.glTextureSubImage2D(lightArray, 0, var0 % 204 * 20, var0 / 204 * 40, 20, 40, 6408, 5121, var1);
   }

   public static void bindLight(int var0, GlProgram var1) {
      GL13.glActiveTexture(33984 + var0);
      GL11.glBindTexture(3553, lightArray);
      GL13.glActiveTexture(33984);
      var1.set("uLightAtlasSize", 4080.0F, rows * 40);
   }

   public static void unbindLight(int var0) {
      GL13.glActiveTexture(33984 + var0);
      GL11.glBindTexture(3553, 0);
      GL13.glActiveTexture(33984);
   }

   private static int lightTexture(int var0) {
      int var1 = GL45.glCreateTextures(3553);
      GL45.glTextureParameteri(var1, 10241, 9729);
      GL45.glTextureParameteri(var1, 10240, 9729);
      GL45.glTextureParameteri(var1, 10242, 33071);
      GL45.glTextureParameteri(var1, 10243, 33071);
      GL45.glTextureStorage2D(var1, 1, 32856, 4080, var0 * 40);
      return var1;
   }

   public static FloatBuffer records(int var0) {
      if (records.capacity() < var0 * 12) {
         records = BufferUtils.createFloatBuffer(var0 * 12 * 2);
      }

      records.clear();
      return records;
   }

   public static void uploadRecords() {
      records.flip();
      recordBytes = records.remaining() * 4L;
      recordsAt = FrameStream.put(records, Math.max(16, FrameStream.textureAlignment()));
      recordBuffer = FrameStream.buffer();
   }

   static void bindForModels(int var0, int var1) {
      if (recordsTexture == 0) {
         recordsTexture = GL11.glGenTextures();
      }

      GL13.glActiveTexture(33984 + var0);
      GL11.glBindTexture(35882, recordsTexture);
      if (recordBytes > 0L) {
         GL43.glTexBufferRange(35882, 34836, recordBuffer, recordsAt, recordBytes);
      }

      GL13.glActiveTexture(33984 + var1);
      GL11.glBindTexture(35882, slotTexture);
      GL13.glActiveTexture(33984);
   }

   static void unbindForModels(int var0, int var1) {
      GL13.glActiveTexture(33984 + var0);
      GL11.glBindTexture(35882, 0);
      GL13.glActiveTexture(33984 + var1);
      GL11.glBindTexture(35882, 0);
      GL13.glActiveTexture(33984);
   }

   public static IntBuffer commands(int var0) {
      if (commands.capacity() < var0 * 4) {
         commands = BufferUtils.createIntBuffer(var0 * 8);
      }

      commands.clear();
      return commands;
   }

   public static void beginDraws() {
      commands.flip();
      commandsAt = FrameStream.put(commands, 4);
      commandBuffer = FrameStream.buffer();
      GL15.glBindBuffer(36671, commandBuffer);
      GL30.glBindVertexArray(vao);
      GL43.glBindVertexBuffer(6, recordBuffer, recordsAt, 48);
   }

   static void beginPlants(int var0) {
      GL30.glBindVertexArray(plantVao);
      GL43.glBindVertexBuffer(6, recordBuffer, recordsAt, 48);
      GL13.glActiveTexture(33984 + var0);
      GL11.glBindTexture(35882, slotTexture);
      GL13.glActiveTexture(33984);
   }

   static void endPlants(int var0) {
      GL13.glActiveTexture(33984 + var0);
      GL11.glBindTexture(35882, 0);
      GL13.glActiveTexture(33984);
   }

   public static void multiDraw(int var0, int var1) {
      GL43.glMultiDrawArraysIndirect(Gl.triangles(), commandsAt + var0 * 16L, var1, 16);
      DrawCounts.draw(0);
      Profile.meshDraws += var1;
   }

   public static void endDraws() {
      GL30.glBindVertexArray(0);
      GL15.glBindBuffer(36671, 0);
   }

   private static String usage() {
      return (used >> 10) + "k/" + (capacity >> 10) + "k vertices, " + (nextSlot - freeSlots.size()) + " light grids";
   }

   private MeshArena() {
   }
}