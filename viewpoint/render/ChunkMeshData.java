package viewpoint.render;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;
import zombie.core.textures.TextureID;

public final class ChunkMeshData implements Retirement.Freeable {
   public final TextureID[] pages;
   public final int[] first;
   public final int[] count;
   final int vertCount;
   public final int level;
   public final int[] runs;
   public final int[] pageRuns;
   public int floorPage = -1;
   int plantBase;
   int[] plantFirst;
   int[] plantCount;
   int modelBase;
   int[] modelRuns;
   private boolean modelsHeld;
   private FloatBuffer verts;
   private final ByteBuffer lightA = BufferUtils.createByteBuffer(3200);
   private final ByteBuffer lightB = BufferUtils.createByteBuffer(3200);
   private volatile ByteBuffer published;
   private boolean lightFilled;
   private volatile boolean retired;
   private static final ByteBuffer DARK = BufferUtils.createByteBuffer(3200);
   public int arenaFirst = -1;
   public int lightSlot = -1;
   private ByteBuffer uploaded;

   public ChunkMeshData(TextureID[] var1, int[] var2, int[] var3, FloatBuffer var4, int var5, int[] var6, int[] var7) {
      this.pages = var1;
      this.first = var2;
      this.count = var3;
      this.verts = var4;
      this.vertCount = var4.remaining() / 14;
      this.level = var5;
      this.runs = var6;
      this.pageRuns = var7;
   }

   public ChunkMeshData(TextureID[] var1, int[] var2, int[] var3, float[] var4, int var5) {
      this(var1, var2, var3, MemoryUtil.memAllocFloat(var4.length).put(var4).flip(), var5, null, null);
   }

   public int vertices() {
      return this.vertCount;
   }

   public void plants(int var1, int[] var2, int[] var3) {
      for (int var7 : var3) {
         if (var7 > 0) {
            this.plantBase = var1;
            this.plantFirst = var2;
            this.plantCount = var3;
            return;
         }
      }
   }

   public void models(int var1, int[] var2) {
      if (var2 != null) {
         this.modelBase = var1;
         this.modelRuns = var2;
         this.modelsHeld = true;

         for (int var3 = 0; var3 < var2.length; var3 += 3) {
            PackModels.get(var2[var3]).hold();
         }
      }
   }

   public boolean hasModels() {
      return this.modelRuns != null;
   }

   public ByteBuffer spareLight() {
      return this.published == this.lightA ? this.lightB : this.lightA;
   }

   public void publishLight(ByteBuffer var1) {
      this.published = var1;
      this.lightFilled = true;
   }

   boolean hasLight() {
      return this.lightFilled;
   }

   public void retire() {
      this.retired = true;
      Retirement.retire(this);
   }

   boolean retired() {
      return this.retired;
   }

   public void prepare() {
      ByteBuffer var1 = this.published;
      if (this.arenaFirst < 0) {
         this.upload(var1);
      } else if (var1 != null && var1 != this.uploaded) {
         MeshArena.uploadLight(this.lightSlot, var1);
         this.uploaded = var1;
      }
   }

   private void upload(ByteBuffer var1) {
      int var2 = MeshArena.takeSlot();
      if (var2 >= 0) {
         int var3 = MeshArena.upload(this.verts, this.vertCount);
         if (var3 < 0) {
            MeshArena.freeSlot(var2);
         } else {
            this.arenaFirst = var3;
            this.lightSlot = var2;
            MemoryUtil.memFree(this.verts);
            this.verts = null;
            MeshArena.uploadLight(this.lightSlot, var1 != null ? var1 : DARK);
            this.uploaded = var1;
         }
      }
   }

   @Override
   public void free() {
      this.discard();
      if (this.arenaFirst >= 0) {
         MeshArena.free(this.arenaFirst, this.vertCount);
         MeshArena.freeSlot(this.lightSlot);
         this.arenaFirst = this.lightSlot = -1;
      }
   }

   public void discard() {
      if (this.verts != null) {
         MemoryUtil.memFree(this.verts);
         this.verts = null;
      }

      if (this.modelsHeld) {
         this.modelsHeld = false;

         for (int var1 = 0; var1 < this.modelRuns.length; var1 += 3) {
            PackModels.get(this.modelRuns[var1]).release();
         }
      }
   }

   static {
      for (short var0 = 1600; var0 < 3200; var0 += 4) {
         DARK.put(var0, (byte)-1);
      }
   }
}
