package viewpoint.render;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.system.MemoryUtil;
import viewpoint.platform.DrawCounts;
import viewpoint.platform.Gl;
import viewpoint.platform.GpuParts;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Profile;
import zombie.core.textures.TextureID;

public final class FarGpu {
   public static final float WHOLE = 2.0F;
   private static final LiveSettings.Number UPLOAD_MB = LiveSettings.number(
      "far.shellUploadMb", "Shell uploaded a frame (MB)", "World/Far world", 0.25F, 32.0F, 0.25F, 4.0F
   );
   private static final LiveSettings.Number CELL_UPLOAD_MB = LiveSettings.number(
      "far.cellUploadMb", "Far cells uploaded a frame (MB)", "World/Far world", 0.25F, 32.0F, 0.25F, 2.0F
   );
   private static final long MEGABYTE = 1048576L;
   private static final ConcurrentLinkedQueue<FarGpu.Shell> UPLOADS = new ConcurrentLinkedQueue<>();
   private static final ConcurrentLinkedQueue<FarGpu.Upload> CELL_UPLOADS = new ConcurrentLinkedQueue<>();

   public static void retire(Retirement.Freeable var0) {
      Retirement.retire(var0);
   }

   public static void upload(FarGpu.Upload var0) {
      if (var0 != null) {
         CELL_UPLOADS.add(var0);
      }
   }

   public static void upload(FarGpu.Shell var0) {
      if (var0 != null) {
         UPLOADS.add(var0);
      }
   }

   static void uploadQueued() {
      GpuParts.begin(42);
      uploadShells();
      uploadCells();
      GpuParts.end(42);
   }

   private static void uploadShells() {
      long var0 = (long)(UPLOAD_MB.get() * 1048576.0F);
      long var2 = 0L;

      FarGpu.Shell var4;
      while ((var4 = UPLOADS.peek()) != null && (var2 == 0L || var2 + var4.bytes() <= var0) && var4.upload()) {
         UPLOADS.poll();
         var2 += var4.uploaded() ? var4.bytes() : 0L;
      }

      Profile.shellUploads = UPLOADS.size();
   }

   private static void uploadCells() {
      long var0 = (long)(CELL_UPLOAD_MB.get() * 1048576.0F);
      long var2 = 0L;

      FarGpu.Upload var4;
      while ((var4 = CELL_UPLOADS.peek()) != null && (var2 == 0L || var2 + var4.bytes() <= var0)) {
         CELL_UPLOADS.poll();
         var4.upload();
         var2 += Math.max(1L, var4.bytes());
      }
   }

   static void draw(FarGpu.Mesh var0) {
      var0.upload();
      GL30.glBindVertexArray(var0.vao);
      GL11.glDrawArrays(Gl.triangles(), 0, var0.vertices);
      DrawCounts.draw(2);
   }

   static void bindTrees(FarGpu.Trees var0) {
      var0.upload();
      GL13.glActiveTexture(34027);
      GL11.glBindTexture(35882, var0.texture);
      GL13.glActiveTexture(33984);
   }

   static void textures(FarGpu.Colours var0) {
      var0.upload();
   }

   private static int colourTexture(int[] var0, int var1) {
      IntBuffer var2 = MemoryUtil.memAllocInt(var0.length);

      for (int var6 : var0) {
         var2.put(0xFF000000 | (var6 & 0xFF) << 16 | var6 & 0xFF00 | var6 >> 16 & 0xFF);
      }

      var2.flip();
      int var7 = GL11.glGenTextures();
      GL11.glBindTexture(3553, var7);
      GL11.glTexImage2D(3553, 0, 32856, var1, var1, 0, 6408, 5121, var2);
      colourMips();
      MemoryUtil.memFree(var2);
      return var7;
   }

   static void colourMips() {
      GL30.glGenerateMipmap(3553);
      GL11.glTexParameteri(3553, 10241, 9987);
      GL11.glTexParameteri(3553, 10240, 9729);
      GL11.glTexParameteri(3553, 10242, 33071);
      GL11.glTexParameteri(3553, 10243, 33071);
   }

   private FarGpu() {
   }

   public static final class BlockDraw {
      public FarGpu.Shell shell;
      public FarGpu.Mesh inside;
      public float originX;
      public float originY;
      public float originZ;
      public int worldX;
      public int worldY;
      public int blockX;
      public int blockY;
      public float top;
      public float reliefLow;
      public GroundPage ground;
      public boolean lite;
      public float distance;
      public int maskIndex = -1;
      public float fadeLo;
      public float fadeHi = 2.0F;
      Object groundArray;
      int groundTexture;
      int groundLayer;

      public float bottom() {
         return this.originY + this.reliefLow * 2.4494896F;
      }
   }

   public static final class CellDraw {
      public FarGpu.Mesh mesh;
      public FarGpu.Colours colours;
      public FarGpu.Trees trees;
      public float originX;
      public float originY;
      public float originZ;
      public int worldX;
      public int worldY;
      public float maxHeight;
      public float reliefLow;
      public float fadeLo;
      public float fadeHi = 2.0F;
      public int treeKeep = 256;
      public float treeGrow = 1.0F;

      public float bottom() {
         return this.originY + this.reliefLow * 2.4494896F;
      }
   }

   public static final class Colours implements Retirement.Freeable, FarGpu.Upload {
      public final int size;
      int[] top;
      int[] side;
      private FarGpu.Colours from;
      int topTexture;
      int sideTexture;
      private volatile boolean uploaded;

      public Colours(int[] var1, int[] var2, int var3) {
         this.top = var1;
         this.side = var2;
         this.size = var3;
      }

      public static FarGpu.Colours shrunk(FarGpu.Colours var0, int var1) {
         FarGpu.Colours var2 = new FarGpu.Colours(null, null, var1);
         var2.from = var0;
         return var2;
      }

      public static long bytes(int var0) {
         return 2L * var0 * var0 * 4L * 4L / 3L;
      }

      @Override
      public long bytes() {
         return bytes(this.size);
      }

      public boolean drawable() {
         return this.topTexture != 0;
      }

      @Override
      public boolean uploaded() {
         return this.uploaded;
      }

      @Override
      public void upload() {
         if (this.topTexture == 0 && (this.top != null || this.from != null)) {
            GL13.glActiveTexture(34009);
            if (this.from != null) {
               this.from.upload();
               if (this.from.topTexture != 0) {
                  this.topTexture = ColourShrink.shrink(this.from.topTexture, this.from.size, this.size);
                  this.sideTexture = ColourShrink.shrink(this.from.sideTexture, this.from.size, this.size);
               }

               this.from = null;
            } else {
               this.topTexture = FarGpu.colourTexture(this.top, this.size);
               this.sideTexture = FarGpu.colourTexture(this.side, this.size);
               this.top = this.side = null;
            }

            GL11.glBindTexture(3553, 0);
            GL13.glActiveTexture(33984);
            this.uploaded = true;
         }
      }

      @Override
      public void free() {
         this.top = this.side = null;
         this.from = null;
         if (this.topTexture != 0) {
            GL11.glDeleteTextures(this.topTexture);
            GL11.glDeleteTextures(this.sideTexture);
            this.topTexture = this.sideTexture = 0;
         }
      }
   }

   public static final class Mesh implements Retirement.Freeable, FarGpu.Upload {
      public final int detail;
      FloatBuffer verts;
      int vertices;
      int vao;
      int vbo;
      private volatile boolean uploaded;

      public Mesh(int var1, FloatBuffer var2) {
         this.detail = var1;
         this.verts = var2;
         this.vertices = var2.remaining() / 4;
      }

      @Override
      public long bytes() {
         return this.vertices * 4L * 4L;
      }

      @Override
      public boolean uploaded() {
         return this.uploaded;
      }

      @Override
      public void upload() {
         if (this.vao == 0 && this.verts != null) {
            this.vao = GL30.glGenVertexArrays();
            this.vbo = GL15.glGenBuffers();
            GL30.glBindVertexArray(this.vao);
            GL15.glBindBuffer(34962, this.vbo);
            GL15.glBufferData(34962, this.verts, 35044);
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 4, 5126, false, 16, 0L);
            GL30.glBindVertexArray(0);
            GL15.glBindBuffer(34962, 0);
            MemoryUtil.memFree(this.verts);
            this.verts = null;
            this.uploaded = true;
         }
      }

      @Override
      public void free() {
         if (this.verts != null) {
            MemoryUtil.memFree(this.verts);
            this.verts = null;
         }

         if (this.vbo != 0) {
            GL15.glDeleteBuffers(this.vbo);
            GL30.glDeleteVertexArrays(this.vao);
            this.vbo = this.vao = 0;
         }
      }
   }

   public static final class Shell implements Retirement.Freeable {
      final TextureID[] pages;
      final int[] first;
      final int[] count;
      public final int vertices;
      private ByteBuffer packed;
      private int arenaFirst = -1;
      private volatile boolean uploaded;
      ShellArena.IntList[] lists;

      public Shell(TextureID[] var1, int[] var2, int[] var3, FloatBuffer var4) {
         this.pages = var1;
         this.first = var2;
         this.count = var3;
         this.vertices = var4.remaining() / 14;
         this.packed = ShellArena.pack(var4);
         MemoryUtil.memFree(var4);
      }

      public long bytes() {
         return this.vertices * 40L;
      }

      public boolean uploaded() {
         return this.uploaded;
      }

      int arenaFirst() {
         return this.arenaFirst;
      }

      private boolean upload() {
         if (this.packed == null) {
            return true;
         }

         this.arenaFirst = ShellArena.upload(this.packed, this.vertices);
         if (this.arenaFirst < 0) {
            return false;
         }

         MemoryUtil.memFree(this.packed);
         this.packed = null;
         this.uploaded = true;
         return true;
      }

      @Override
      public void free() {
         if (this.packed != null) {
            MemoryUtil.memFree(this.packed);
            this.packed = null;
         }

         if (this.arenaFirst >= 0) {
            ShellArena.free(this.arenaFirst, this.vertices);
            this.arenaFirst = -1;
            this.lists = null;
         }
      }
   }

   public static final class Trees implements Retirement.Freeable, FarGpu.Upload {
      public static final int RANKS = 256;
      public final int count;
      private final int[] below;
      short[] list;
      int buffer;
      int texture;
      private volatile boolean uploaded;

      public Trees(short[] var1, int[] var2) {
         this.list = var1;
         this.below = var2;
         this.count = var1.length / 4;
      }

      public int kept(int var1) {
         return this.below[Math.max(0, Math.min(256, var1))];
      }

      @Override
      public long bytes() {
         return this.count * 8L;
      }

      @Override
      public boolean uploaded() {
         return this.uploaded;
      }

      @Override
      public void upload() {
         if (this.buffer == 0 && this.list != null) {
            ShortBuffer var1 = MemoryUtil.memAllocShort(this.list.length).put(this.list).flip();
            this.buffer = GL15.glGenBuffers();
            GL15.glBindBuffer(35882, this.buffer);
            GL15.glBufferData(35882, var1, 35044);
            GL15.glBindBuffer(35882, 0);
            MemoryUtil.memFree(var1);
            this.texture = GL11.glGenTextures();
            GL11.glBindTexture(35882, this.texture);
            GL31.glTexBuffer(35882, 36214, this.buffer);
            GL11.glBindTexture(35882, 0);
            this.list = null;
            this.uploaded = true;
         }
      }

      @Override
      public void free() {
         this.list = null;
         if (this.buffer != 0) {
            GL15.glDeleteBuffers(this.buffer);
            GL11.glDeleteTextures(this.texture);
            this.buffer = this.texture = 0;
         }
      }
   }

   public interface Upload {
      long bytes();

      boolean uploaded();

      void upload();
   }
}
