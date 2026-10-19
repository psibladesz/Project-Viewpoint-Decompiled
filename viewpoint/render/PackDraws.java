package viewpoint.render;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;
import viewpoint.platform.DrawCounts;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Profile;
import zombie.core.textures.Texture;

final class PackDraws {
   private static final int COMMAND_INTS = 5;
   private static final ArrayList<PackDraws.Bucket> buckets = new ArrayList<>();
   private static final HashMap<Long, PackDraws.Bucket> byPages = new HashMap<>();
   private static int bucketCount;
   private static int runCount;
   private static int[] runs = new int[1024];
   private static IntBuffer upload = BufferUtils.createIntBuffer(4096);

   static void draw(SceneData var0, GlProgram var1, byte var2, int var3, boolean var4) {
      if (var1.has("uModels") && PackModels.table() != 0) {
         begin();

         for (int var5 = 0; var5 < var0.meshCount; var5++) {
            if (Meshes.drawn(var0, var5, var2, var3)) {
               collect(var0, var5, var4 ? var0.meshModelSquares[var5] : -1L);
            }
         }

         issue(var1);
      }
   }

   static void drawTarget(SceneData var0, GlProgram var1, int var2, long var3) {
      if (var1.has("uModels") && PackModels.table() != 0 && var0.meshes[var2].arenaFirst >= 0) {
         begin();
         collect(var0, var2, var3);
         issue(var1);
      }
   }

   private static void begin() {
      bucketCount = 0;
      runCount = 0;
      byPages.clear();
   }

   private static void issue(GlProgram var0) {
      if (bucketCount != 0) {
         room(runCount * 4);
         upload.put(runs, 0, runCount * 4).flip();
         long var1 = FrameStream.put(upload, 16);
         int var3 = FrameStream.buffer();
         int var4 = 0;

         for (int var5 = 0; var5 < bucketCount; var5++) {
            var4 += buckets.get(var5).count * 5;
         }

         room(var4);

         for (int var7 = 0; var7 < bucketCount; var7++) {
            upload.put(buckets.get(var7).commands, 0, buckets.get(var7).count * 5);
         }

         long var8 = FrameStream.put(upload.flip(), 4);
         GL15.glBindBuffer(36671, FrameStream.buffer());
         multiDraw(var0, var3, var1, var8);
         GL15.glBindBuffer(36671, 0);
      }
   }

   private static void multiDraw(GlProgram var0, int var1, long var2, long var4) {
      boolean var6 = var0.has("uSpriteFilter");
      var0.setInt("uModels", 1);
      if (var6) {
         var0.setInt("uSpriteFilter", TextureFilter.MODELS.get());
      }

      var0.sampler("uPackTable", 88);
      var0.sampler("uMeshRecords", 89);
      var0.sampler("uPlantSlots", 48);
      MeshArena.bindForModels(89, 48);
      GL13.glActiveTexture(34072);
      GL11.glBindTexture(35882, PackModels.table());
      GL13.glActiveTexture(33984);
      long var7 = var4;

      for (int var9 = 0; var9 < bucketCount; var9++) {
         PackDraws.Bucket var10 = buckets.get(var9);
         PackArena.bind(var10.meshPage, var1, var2);
         int var11 = PackArt.texture(var10.artPage);
         GL11.glBindTexture(3553, var11);
         Texture.lastTextureID = -1;
         GL33.glBindSampler(0, TextureFilter.MODELS.sampler(var11));
         GL43.glMultiDrawElementsIndirect(Gl.triangles(), 5125, var7, var10.count, 20);
         var7 += var10.count * 5L * 4L;
         DrawCounts.draw(4);
         Profile.meshDraws = Profile.meshDraws + var10.count;
      }

      GL30.glBindVertexArray(0);
      GL33.glBindSampler(0, 0);
      GL11.glBindTexture(3553, Meshes.offlineAtlas);
      GL13.glActiveTexture(34072);
      GL11.glBindTexture(35882, 0);
      GL13.glActiveTexture(33984);
      MeshArena.unbindForModels(89, 48);
      var0.setInt("uModels", 0);
      if (var6) {
         TextureFilter.SPRITES.apply(var0);
      }
   }

   private static void collect(SceneData var0, int var1, long var2) {
      ChunkMeshData var4 = var0.meshes[var1];
      if (var4.modelRuns != null && var2 != 0L) {
         for (int var5 = 0; var5 < var4.modelRuns.length; var5 += 3) {
            PackModel var6 = PackModels.get(var4.modelRuns[var5]);
            if (var6 != null && var6.state() == 3) {
               int var7 = var4.arenaFirst + var4.modelBase + var4.modelRuns[var5 + 1];
               bucket(var6).add(var6, var4.modelRuns[var5 + 2], run(var1, var7, (int)var2, (int)(var2 >>> 32)));
            }
         }
      }
   }

   private static int run(int var0, int var1, int var2, int var3) {
      if ((runCount + 1) * 4 > runs.length) {
         runs = Arrays.copyOf(runs, runs.length * 2);
      }

      int var4 = runCount * 4;
      runs[var4] = var0;
      runs[var4 + 1] = var1;
      runs[var4 + 2] = var2;
      runs[var4 + 3] = var3;
      return runCount++;
   }

   private static PackDraws.Bucket bucket(PackModel var0) {
      long var1 = (long)var0.meshPage << 32 | var0.artPage;
      PackDraws.Bucket var3 = byPages.get(var1);
      if (var3 == null) {
         if (bucketCount == buckets.size()) {
            buckets.add(new PackDraws.Bucket());
         }

         var3 = buckets.get(bucketCount++);
         var3.meshPage = var0.meshPage;
         var3.artPage = var0.artPage;
         var3.count = 0;
         byPages.put(var1, var3);
      }

      return var3;
   }

   private static void room(int var0) {
      if (upload.capacity() < var0) {
         upload = BufferUtils.createIntBuffer(var0 * 2);
      }

      upload.clear();
   }

   private PackDraws() {
   }

   private static final class Bucket {
      int meshPage;
      int artPage;
      int count;
      int[] commands = new int[320];

      void add(PackModel var1, int var2, int var3) {
         if ((this.count + 1) * 5 > this.commands.length) {
            this.commands = Arrays.copyOf(this.commands, this.commands.length * 2);
         }

         int var4 = this.count * 5;
         this.commands[var4] = var1.indices;
         this.commands[var4 + 1] = var2;
         this.commands[var4 + 2] = var1.firstIndex;
         this.commands[var4 + 3] = var1.firstVertex;
         this.commands[var4 + 4] = var3;
         this.count++;
      }
   }
}
