package viewpoint.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL33;
import viewpoint.platform.DrawCounts;
import viewpoint.platform.GlProgram;
import zombie.core.textures.Texture;
import zombie.core.textures.TextureID;

public final class Meshes {
   private static final float[] BANDS = new float[]{12.0F, 32.0F, Float.MAX_VALUE};
   static FloorBakes floors;
   private static boolean[] chosen;
   public static int offlineAtlas;
   private static final ArrayList<Meshes.Bucket> buckets = new ArrayList<>();
   private static final IdentityHashMap<Object, Meshes.Bucket> byPage = new IdentityHashMap<>();
   private static final Object NO_PAGE = new Object();
   private static int bucketCount;
   private static TextureID[] segmentPages = new TextureID[256];
   private static int[] segmentFirst = new int[256];
   private static int[] segmentLength = new int[256];
   private static int[] segmentFloor = new int[256];
   private static int[] segmentSurfaces = new int[256];
   private static int segmentCount;
   private static int plantSegments;

   static void init() {
      MeshArena.init();
   }

   static void prepare(SceneData var0) {
      FloatBuffer var1 = MeshArena.records(var0.meshCount);

      for (int var2 = 0; var2 < var0.meshCount; var2++) {
         var0.meshes[var2].prepare();
      }

      for (int var6 = 0; var6 < var0.meshCount; var6++) {
         ChunkMeshData var3 = var0.meshes[var6];
         ChunkMeshData var4 = var0.meshBelow[var6];
         ChunkMeshData var5 = var0.meshAbove[var6];
         var1.put(var0.meshOffsets[var6 * 3])
            .put(var0.meshOffsets[var6 * 3 + 1])
            .put(var0.meshOffsets[var6 * 3 + 2])
            .put(var3.level * 2.4494896F)
            .put(var0.meshFade[var6 * 2])
            .put(var0.meshFade[var6 * 2 + 1])
            .put(var3.lightSlot)
            .put(floorSlice(var6) < 0 ? 0.0F : FloorBakes.recordLayer(floorSlice(var6), floors.meshShare(var6)))
            .put(var4 != null && var4.lightSlot >= 0 ? var4.lightSlot : var3.lightSlot)
            .put(var5 != null && var5.lightSlot >= 0 ? var5.lightSlot : MeshArena.openSkySlot())
            .put(0.0F)
            .put(0.0F);
      }

      MeshArena.uploadRecords();
   }

   static void draw(SceneData var0, GlProgram var1, byte var2) {
      draw(var0, var1, var2, Integer.MAX_VALUE, false, true);
   }

   static void draw(SceneData var0, GlProgram var1, byte var2, boolean[] var3) {
      chosen = var3;

      try {
         draw(var0, var1, var2);
      } finally {
         chosen = null;
      }
   }

   static void drawPlanned(SceneData var0, GlProgram var1, byte var2, boolean var3) {
      draw(var0, var1, var2, Integer.MAX_VALUE, true, var3);
   }

   static void draw(SceneData var0, GlProgram var1, byte var2, int var3, boolean var4, boolean var5) {
      int var6 = 0;

      for (int var7 = 0; var7 < var0.meshCount; var7++) {
         if (drawn(var0, var7, var2, var3)) {
            ChunkMeshData var8 = var0.meshes[var7];
            var6 += var4 && var0.meshPlan[var7] >= 0 ? var0.meshPlanLength[var7] : var8.pages.length;
            var6 += var5 && var8.plantFirst != null ? var8.pages.length : 0;
            var6 += var8.hasModels() ? 1 : 0;
         }
      }

      if (var6 != 0) {
         IntBuffer var17 = MeshArena.commands(var6);
         segmentCount = 0;

         for (int var18 = 0; var18 < BANDS.length; var18++) {
            float var9 = var18 == 0 ? -1.0F : BANDS[var18 - 1];
            float var10 = BANDS[var18];
            bucketCount = 0;
            byPage.clear();

            for (int var11 = 0; var11 < var0.meshCount; var11++) {
               if (drawn(var0, var11, var2, var3)) {
                  float var12 = var0.meshOffsets[var11 * 3] - 4.0F;
                  float var13 = var0.meshOffsets[var11 * 3 + 2] - 4.0F;
                  float var14 = (float)Math.sqrt(var12 * var12 + var13 * var13);
                  if (var14 > var9 && var14 <= var10) {
                     collect(var0, var11, var4);
                  }
               }
            }

            flushBuckets(var17);
         }

         plantSegments = segmentCount;
         if (var5) {
            collectPlants(var0, var2, var3);
            flushBuckets(var17);
         }

         MeshArena.beginDraws();
         boolean var19 = var1.has("uLight");
         if (var19) {
            MeshArena.bindLight(5, var1);
         }

         issue(var1);
         MeshArena.endDraws();
         PackDraws.draw(var0, var1, var2, var3, var4);
         CorpseCards.draw(var0, var1, var2, var3, var4);
         if (var19) {
            MeshArena.unbindLight(5);
         }

         GL33.glBindSampler(0, 0);
         Arrays.fill(segmentPages, 0, segmentCount, null);
      }
   }

   private static void flushBuckets(IntBuffer var0) {
      for (int var1 = 0; var1 < bucketCount; var1++) {
         Meshes.Bucket var2 = buckets.get(var1);
         segment(var2.page, var2.floorArray, var2.floorSurfaces, var0.position() / 4, var2.count);
         var0.put(var2.commands, 0, var2.count * 4);
      }
   }

   private static void collectPlants(SceneData var0, byte var1, int var2) {
      bucketCount = 0;
      byPage.clear();

      for (int var3 = 0; var3 < var0.meshCount; var3++) {
         ChunkMeshData var4 = var0.meshes[var3];
         if (drawn(var0, var3, var1, var2) && var4.plantFirst != null) {
            for (int var5 = 0; var5 < var4.pages.length; var5++) {
               if (var4.plantCount[var5] > 0) {
                  int var6 = var4.arenaFirst + var4.plantBase + var4.plantFirst[var5];
                  bucket(var4.pages[var5], var4.pages[var5], 0, 0).add(var4.plantCount[var5] * 6, var6 * 6, var3);
               }
            }
         }
      }
   }

   private static void issue(GlProgram var0) {
      Wireframe.apply(var0);
      LodView.apply(var0);
      TextureFilter.SPRITES.apply(var0);
      if (var0.has("uFloorPages")) {
         var0.sampler("uFloorPages", 41);
         FloorFilter.apply(var0);
      }

      if (var0.has("uFloorSurfaces")) {
         var0.sampler("uFloorSurfaces", 136);
      }

      TextureID var1 = null;
      int var2 = 0;
      DrawCounts.pass(0);

      for (int var3 = 0; var3 < segmentCount; var3++) {
         TextureID var4 = segmentPages[var3];
         if (var3 == plantSegments) {
            MeshArena.beginPlants(48);
            var0.sampler("uPlantSlots", 48);
            var0.setInt("uPlants", 1);
         }

         if (segmentFloor[var3] != 0 && segmentFloor[var3] != var2) {
            var2 = segmentFloor[var3];
            bindFloorArray(var2, segmentSurfaces[var3]);
         } else if (var4 != null && var4 != var1) {
            var1 = var4;
            bindPage(var4);
            DrawCounts.page(0, var4);
         } else if (var4 == null && var2 == 0) {
            GL11.glBindTexture(3553, offlineAtlas);
         }

         MeshArena.multiDraw(segmentFirst[var3], segmentLength[var3]);
      }

      if (var2 != 0) {
         bindFloorArray(0, 0);
      }

      if (plantSegments < segmentCount) {
         var0.setInt("uPlants", 0);
         MeshArena.endPlants(48);
      }
   }

   private static void collect(SceneData var0, int var1, boolean var2) {
      ChunkMeshData var3 = var0.meshes[var1];
      if (var2 && var0.meshPlan[var1] >= 0) {
         int[] var8 = var0.plan;
         int var9 = -1;
         Meshes.Bucket var6 = null;

         for (int var7 = var0.meshPlan[var1]; var7 < var0.meshPlan[var1] + var0.meshPlanLength[var1]; var7++) {
            if (var8[var7 * 3] != var9) {
               var9 = var8[var7 * 3];
               var6 = bucket(var3, var1, var9);
            }

            if (var6 != null) {
               var6.add(var8[var7 * 3 + 2], var3.arenaFirst + var8[var7 * 3 + 1], var1);
            }
         }
      } else {
         for (int var4 = 0; var4 < var3.pages.length; var4++) {
            Meshes.Bucket var5 = bucket(var3, var1, var4);
            if (var5 != null) {
               var5.add(var3.count[var4], var3.arenaFirst + var3.first[var4], var1);
            }
         }
      }
   }

   private static Meshes.Bucket bucket(ChunkMeshData var0, int var1, int var2) {
      if (var2 != var0.floorPage) {
         return bucket(var0.pages[var2] == null ? NO_PAGE : var0.pages[var2], var0.pages[var2], 0, 0);
      }

      int var3 = floorSlice(var1);
      return var3 < 0 ? null : bucket(floors.array(var3), null, floors.texture(var3), floors.surface(var3));
   }

   private static int floorSlice(int var0) {
      return floors == null ? -1 : floors.meshSlice(var0);
   }

   static void bindFloorArray(int var0) {
      bindFloorArray(var0, 0);
   }

   static void bindFloorArray(int var0, int var1) {
      GL13.glActiveTexture(34120);
      GL11.glBindTexture(35866, var1);
      GL13.glActiveTexture(34025);
      GL11.glBindTexture(35866, var0);
      FloorFilter.bind(var0);
      GL13.glActiveTexture(33984);
   }

   public static void bindPage(TextureID var0) {
      bindPage(0, var0);
   }

   static void bindPage(int var0, TextureID var1) {
      if (var1.getID() == -1) {
         Texture.lastTextureID = -1;
         var1.bind();
      } else {
         GL11.glBindTexture(3553, var1.getID());
      }

      Texture.lastTextureID = -1;
      GL33.glBindSampler(var0, TextureFilter.SPRITES.sampler(var1.getID()));
   }

   private static Meshes.Bucket bucket(Object var0, TextureID var1, int var2, int var3) {
      Meshes.Bucket var4 = byPage.get(var0);
      if (var4 == null) {
         if (bucketCount == buckets.size()) {
            buckets.add(new Meshes.Bucket());
         }

         var4 = buckets.get(bucketCount++);
         var4.page = var1;
         var4.floorArray = var2;
         var4.floorSurfaces = var3;
         var4.count = 0;
         byPage.put(var0, var4);
      }

      return var4;
   }

   static boolean drawn(SceneData var0, int var1, byte var2, int var3) {
      ChunkMeshData var4 = var0.meshes[var1];
      return (var0.meshFlags[var1] & var2) != 0 && var4.level <= var3 && var4.arenaFirst >= 0 && (chosen == null || chosen[var1]);
   }

   private static void segment(TextureID var0, int var1, int var2, int var3, int var4) {
      if (segmentCount == segmentPages.length) {
         segmentPages = Arrays.copyOf(segmentPages, segmentCount * 2);
         segmentFirst = Arrays.copyOf(segmentFirst, segmentCount * 2);
         segmentLength = Arrays.copyOf(segmentLength, segmentCount * 2);
         segmentFloor = Arrays.copyOf(segmentFloor, segmentCount * 2);
         segmentSurfaces = Arrays.copyOf(segmentSurfaces, segmentCount * 2);
      }

      segmentPages[segmentCount] = var0;
      segmentFloor[segmentCount] = var1;
      segmentSurfaces[segmentCount] = var2;
      segmentFirst[segmentCount] = var3;
      segmentLength[segmentCount] = var4;
      segmentCount++;
   }

   private Meshes() {
   }

   private static final class Bucket {
      TextureID page;
      int floorArray;
      int floorSurfaces;
      int[] commands = new int[256];
      int count;

      void add(int var1, int var2, int var3) {
         if (this.count * 4 == this.commands.length) {
            this.commands = Arrays.copyOf(this.commands, this.commands.length * 2);
         }

         int var4 = this.count * 4;
         this.commands[var4] = var1;
         this.commands[var4 + 1] = 1;
         this.commands[var4 + 2] = var2;
         this.commands[var4 + 3] = var3;
         this.count++;
      }
   }
}
