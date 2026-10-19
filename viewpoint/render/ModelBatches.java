package viewpoint.render;

import java.nio.IntBuffer;
import java.util.Arrays;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import viewpoint.platform.DrawCounts;
import zombie.core.textures.Texture;

final class ModelBatches {
   private static final int COMMAND = 5;
   private static final int RUN = 5;
   private ModelDraws draws;
   private ModelMeshes.Held[] held;
   private Texture[] textures;
   private long[] keys = new long[256];
   private int[] order = new int[256];
   private int[] runs = new int[320];
   private int[] slotDraws = new int[64];
   private int runCount;
   private int slotTotal;
   private int[] rank = new int[256];
   private int[] picked = new int[64];
   private int count;
   int runsDrawn;
   long elementsDrawn;
   private IntBuffer instances = BufferUtils.createIntBuffer(1024);
   private IntBuffer commands = BufferUtils.createIntBuffer(1280);

   void order(ModelDraws var1, ModelMeshes.Held[] var2, Texture[] var3) {
      this.draws = var1;
      this.held = var2;
      this.textures = var3;
      if (this.keys.length < var1.count) {
         this.keys = new long[var1.meshes.length];
         this.order = new int[var1.meshes.length];
         this.rank = new int[var1.meshes.length];
      }

      this.count = 0;

      for (int var4 = 0; var4 < var1.count; var4++) {
         Texture var5 = var3[var4];
         if (var2[var4] != null && var5 != null && var5.isReady()) {
            this.keys[this.count++] = (long)Math.min(var1.material[var4] + 1, 255) << 55
               | (long)(var2[var4].layout.index & 7) << 52
               | (long)(var1.cull[var4] == 0 ? 0 : (var1.cull[var4] == 1028 ? 1 : 2)) << 50
               | (long)(System.identityHashCode(var5) & 16383) << 36
               | (long)(var2[var4].id & 65535) << 20
               | var4;
         }
      }

      Arrays.sort(this.keys, 0, this.count);
      Arrays.fill(this.rank, 0, var1.count, -1);

      for (int var6 = 0; var6 < this.count; this.rank[this.order[var6]] = var6++) {
         this.order[var6] = (int)(this.keys[var6] & 1048575L);
      }
   }

   int draw(ModelBatches.Filter var1, ModelBatches.State var2) {
      return this.inOrder(this.order, this.count, var1, var2);
   }

   int draw(int[] var1, int var2, ModelBatches.Filter var3, ModelBatches.State var4) {
      if (var1 == null) {
         return this.draw(var3, var4);
      }

      this.picked = grow(this.picked, var2);
      int var5 = 0;

      for (int var6 = 0; var6 < var2; var6++) {
         int var7 = this.rank[var1[var6]];
         if (var7 >= 0) {
            this.picked[var5++] = var7;
         }
      }

      Arrays.sort(this.picked, 0, var5);

      for (int var8 = 0; var8 < var5; var8++) {
         this.picked[var8] = this.order[this.picked[var8]];
      }

      return this.inOrder(this.picked, var5, var3, var4);
   }

   private int inOrder(int[] var1, int var2, ModelBatches.Filter var3, ModelBatches.State var4) {
      this.instances.clear();
      this.commands.clear();
      this.runCount = this.slotTotal = 0;
      int var5 = 0;
      int var6 = -1;
      int var7 = -1;
      long var8 = 0L;

      for (int var10 = 0; var10 < var2; var10++) {
         int var11 = var1[var10];
         if (var3.take(var11)) {
            boolean var12 = var6 < 0 || !this.alike(var6, var11);
            int var13 = var12 ? -1 : this.slot(var11);
            if (var13 < 0) {
               this.run(var11);
               var13 = this.slot(var11);
               var12 = true;
            }

            this.room(1, 1);
            if (!var12 && this.held[var6] == this.held[var11] && var13 == var7) {
               int var15 = this.commands.position() - 5 + 1;
               this.commands.put(var15, this.commands.get(var15) + 1);
            } else {
               ModelMeshes.Held var14 = this.held[var11];
               this.commands.put(var14.elements).put(1).put(var14.firstIndex).put(var14.firstVertex).put(this.instances.position());
               this.runs[(this.runCount - 1) * 5 + 1]++;
            }

            this.instances.put(var11 | var13 << 24);
            var8 += this.held[var11].elements;
            var5++;
            var6 = var11;
            var7 = var13;
         }
      }

      this.runsDrawn = this.runCount;
      this.elementsDrawn = var8;
      if (var5 == 0) {
         return 0;
      }

      this.issue(var4);
      return var5;
   }

   private void run(int var1) {
      this.runs = grow(this.runs, (this.runCount + 1) * 5);
      int var2 = this.runCount++ * 5;
      this.runs[var2] = this.commands.position() / 5;
      this.runs[var2 + 1] = 0;
      this.runs[var2 + 2] = var1;
      this.runs[var2 + 3] = this.slotTotal;
      this.runs[var2 + 4] = 0;
   }

   private int slot(int var1) {
      int var2 = (this.runCount - 1) * 5;
      int var3 = this.runs[var2 + 3];
      int var4 = this.runs[var2 + 4];

      for (int var5 = 0; var5 < var4; var5++) {
         if (this.textures[this.slotDraws[var3 + var5]] == this.textures[var1]) {
            return var5;
         }
      }

      if (var4 == (this.draws.material[var1] < 0 ? 16 : 1)) {
         return -1;
      }

      this.slotDraws = grow(this.slotDraws, var3 + var4 + 1);
      this.slotDraws[var3 + var4] = var1;
      this.runs[var2 + 4]++;
      this.slotTotal++;
      return var4;
   }

   private void issue(ModelBatches.State var1) {
      this.instances.flip();
      this.commands.flip();
      long var2 = FrameStream.put(this.instances, 4);
      int var4 = FrameStream.buffer();
      long var5 = FrameStream.put(this.commands, 4);
      GL15.glBindBuffer(36671, FrameStream.buffer());

      for (int var7 = 0; var7 < this.runCount; var7++) {
         int var8 = var7 * 5;
         int var9 = this.runs[var8];
         int var10 = this.runs[var8 + 1];
         int var11 = this.runs[var8 + 2];
         GL30.glBindVertexArray(this.held[var11].layout.vao);
         GL43.glBindVertexBuffer(6, var4, var2, 4);
         var1.set(var11);

         for (int var12 = 0; var12 < this.runs[var8 + 4]; var12++) {
            var1.texture(var12, this.slotDraws[this.runs[var8 + 3] + var12]);
         }

         GL43.glMultiDrawElementsIndirect(4, 5125, var5 + var9 * 5L * 4L, var10, 20);
         DrawCounts.draw(3);
      }

      GL30.glBindVertexArray(0);
      GL15.glBindBuffer(36671, 0);
   }

   private boolean alike(int var1, int var2) {
      return this.draws.material[var1] == this.draws.material[var2]
         && this.held[var1].layout == this.held[var2].layout
         && this.draws.cull[var1] == this.draws.cull[var2]
         && (this.draws.material[var1] < 0 || this.textures[var1] == this.textures[var2]);
   }

   private void room(int var1, int var2) {
      if (this.instances.remaining() < var1) {
         IntBuffer var3 = BufferUtils.createIntBuffer(this.instances.capacity() * 2);
         this.instances.flip();
         this.instances = var3.put(this.instances);
      }

      if (this.commands.remaining() < var2 * 5) {
         IntBuffer var4 = BufferUtils.createIntBuffer(this.commands.capacity() * 2);
         this.commands.flip();
         this.commands = var4.put(this.commands);
      }
   }

   private static int[] grow(int[] var0, int var1) {
      return var0.length >= var1 ? var0 : Arrays.copyOf(var0, Math.max(var1, var0.length * 2));
   }

   interface Filter {
      boolean take(int var1);
   }

   interface State {
      void set(int var1);

      default void texture(int var1, int var2) {
      }
   }
}
