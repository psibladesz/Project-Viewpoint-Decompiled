package viewpoint.far;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import viewpoint.core.Frame;
import viewpoint.platform.Caches;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.LongMap;
import viewpoint.platform.Pool;
import viewpoint.platform.Profile;
import viewpoint.render.FarGpu;
import viewpoint.render.GroundTiles;
import viewpoint.render.SceneData;
import viewpoint.world.GridRoofs;

public final class FarWorld {
   static final String LODS = "World/Levels of detail";
   static final String WORK = "World/Far world";
   private static final LiveSettings.Number REACH = LiveSettings.number(
           "lod.farBlocks", "Far world up to (blocks)", "World/Levels of detail", 0.0F, 64.0F, 1.0F, 32.0F
   );
   private static final LiveSettings.Number DETAIL_1 = LiveSettings.number(
           "lod.boxesFineBlocks", "Boxes of 1 square up to (blocks)", "World/Levels of detail", 0.0F, 64.0F, 1.0F, 3.0F
   );
   private static final LiveSettings.Number DETAIL_2 = LiveSettings.number(
           "lod.boxesMidBlocks", "Boxes of 2 squares up to (blocks)", "World/Levels of detail", 0.0F, 64.0F, 1.0F, 6.0F
   );
   private static final LiveSettings.Number DETAIL_4 = LiveSettings.number(
           "lod.boxesCoarseBlocks", "Boxes of 4 squares up to (blocks)", "World/Levels of detail", 0.0F, 64.0F, 1.0F, 12.0F
   );
   private static final LiveSettings.Number[] DETAIL_REACH = new LiveSettings.Number[]{DETAIL_1, DETAIL_2, DETAIL_4};
   private static final int[] DETAIL = new int[]{1, 2, 4, 8};
   private static final LiveSettings.Number JOBS = LiveSettings.number("far.cellJobs", "Cells read at once", "World/Far world", 1.0F, 16.0F, 1.0F, 4.0F);
   private static final LiveSettings.Number BUDGET = LiveSettings.number(
           "far.budgetMs", "Main thread budget (ms)", "World/Far world", 0.25F, 10.0F, 0.25F, 1.0F
   );
   private static final float KEEP_MARGIN = 256.0F;
   private static final double RELAX = 0.6;
   private static float reachLimit = Float.MAX_VALUE;
   static final LongMap<FarWorld.Slot> slots = new LongMap<>(256);
   private static final HashSet<Long> missing = new HashSet<>();
   private static final ArrayList<FarWorld.Slot> wanted = new ArrayList<>();
   private static long frameCounter;
   private static long now;
   private static boolean broken;

   public static float reach() {
      return REACH.get() * 64.0F;
   }

   public static void update(Frame var0) {
      var0.scene.farCount = 0;
      if (!broken) {
         try {
            updateWorld(var0);
         } catch (RuntimeException var2) {
            broken = true;
            var0.scene.farCount = 0;
            System.out.println("[Viewpoint] far world off after an error: " + var2);
            var2.printStackTrace(System.out);
            clear();
         }
      }
   }

   private static void updateWorld(Frame var0) {
      SceneData var1 = var0.scene;
      float var2 = reach();
      if (var2 <= 0.0F) {
         if (!slots.isEmpty()) {
            clear();
         }
      } else {
         FarWorkers.resize();
         FarColours.load(FarWorkers.POOL);
         if (FarColours.ready()) {
            frameCounter++;
            now = System.nanoTime();

            CellJobs.Done var3;
            while ((var3 = CellJobs.poll()) != null) {
               install(var3);
            }

            float var14 = var0.camX;
            float var4 = var0.camY;
            int var5 = (int)Math.floor((var14 - var2) / 256.0F);
            int var6 = (int)Math.floor((var14 + var2) / 256.0F);
            int var7 = (int)Math.floor((var4 - var2) / 256.0F);
            int var8 = (int)Math.floor((var4 + var2) / 256.0F);
            wanted.clear();

            for (int var9 = var7; var9 <= var8; var9++) {
               for (int var10 = var5; var10 <= var6; var10++) {
                  float var11 = Math.max(0.0F, Math.max(var10 * 256 - var14, var14 - (var10 + 1) * 256));
                  float var12 = Math.max(0.0F, Math.max(var9 * 256 - var4, var4 - (var9 + 1) * 256));
                  float var13 = (float)Math.sqrt(var11 * var11 + var12 * var12);
                  if (var13 <= var2 && var13 < reachLimit) {
                     visit(var1, var0, var10, var9, var13);
                  }
               }
            }

            wanted.sort((var0x, var1x) -> Float.compare(var0x.distance, var1x.distance));
            long var15 = System.nanoTime() + (long)(BUDGET.get() * 1000000.0F);
            startJobs(var15);
            FarShell.update(var0, var14, var4, frameCounter, var15);
            letGo(var14, var4, var2);
            makeRoom();
         }
      }
   }

   private static void visit(SceneData var0, Frame var1, int var2, int var3, float var4) {
      long var5 = key(var2, var3);
      FarWorld.Slot var7 = slots.get(var5);
      if (var7 == null) {
         if (missing.contains(var5)) {
            return;
         }

         if (CellJobs.mapFiles(var2, var3) == null) {
            missing.add(var5);
            return;
         }

         var7 = new FarWorld.Slot(var2, var3);
         slots.put(var5, var7);
      }

      var7.seen = frameCounter;
      var7.distance = var4;
      settle(var7);
      shrink(var7, var4);
      if (!var7.busy
              && (var7.mesh == null || var7.mesh.detail != detail(var4) || var7.height == null && var7.wantsData || CellColours.tooSmall(var7.colours, var4))) {
         wanted.add(var7);
      }

      if (var7.mesh != null && var7.colours != null) {
         add(var0, var7, var1);
      }
   }

   private static void shrink(FarWorld.Slot var0, float var1) {
      if (var0.shrinking == null) {
         if (!var0.busy && var0.oldColours == null && CellColours.tooLarge(var0.colours, var1)) {
            var0.shrinking = FarGpu.Colours.shrunk(var0.colours, CellColours.size(var1));
            FarGpu.upload(var0.shrinking);
         }
      } else {
         if (var0.shrinking.uploaded()) {
            if (var0.shrinking.drawable() && var0.oldColours == null && var0.coming == null) {
               FarGpu.retire(var0.colours);
               var0.colours = var0.shrinking;
            } else {
               FarGpu.retire(var0.shrinking);
            }

            var0.shrinking = null;
         }
      }
   }

   private static void makeRoom() {
      long var0 = 0L;
      int var2 = 0;
      int var3 = 0;
      int var4 = 0;

      for (FarWorld.Slot var6 : slots.values()) {
         var0 += gpuBytes(var6);
         int var7 = var6.colours == null ? 0 : var6.colours.size;
         var2 += var7 == 256 ? 1 : 0;
         var3 += var7 == 128 ? 1 : 0;
         var4 += var7 == 64 ? 1 : 0;
      }

      Profile.farFullColours = var2;
      Profile.farHalfColours = var3;
      Profile.farQuarterColours = var4;
      Pool.FAR.use(var0);
      if (!Pool.FAR.over(0.9)) {
         if (!Pool.FAR.over(0.6)) {
            reachLimit = Float.MAX_VALUE;
         }
      } else {
         ArrayList<FarWorld.Slot> var8 = new ArrayList<>();

         for (FarWorld.Slot var11 : slots.values()) {
            if (gpuBytes(var11) > 0L) {
               var8.add(var11);
            }
         }

         var8.sort((var0x, var1) -> Float.compare(var1.distance, var0x.distance));

         for (int var10 = 0; var10 < var8.size() && Pool.FAR.over(0.8); var10++) {
            FarWorld.Slot var12 = (FarWorld.Slot)var8.get(var10);
            reachLimit = Math.min(reachLimit, var12.distance);
            var0 -= gpuBytes(var12);
            retire(var12);
            slots.remove(key(var12.cellX, var12.cellY));
            Pool.FAR.use(var0);
         }
      }
   }

   private static long gpuBytes(FarWorld.Slot var0) {
      return bytes(var0.mesh)
              + bytes(var0.colours)
              + bytes(var0.trees)
              + bytes(var0.oldMesh)
              + bytes(var0.oldColours)
              + bytes(var0.oldTrees)
              + bytes(var0.shrinking)
              + (var0.coming == null ? 0L : bytes(var0.coming));
   }

   private static long bytes(CellJobs.Done var0) {
      return bytes(var0.mesh()) + bytes(var0.colours()) + bytes(var0.trees());
   }

   private static long bytes(FarGpu.Upload var0) {
      return var0 == null ? 0L : var0.bytes();
   }

   private static void retire(FarWorld.Slot var0) {
      FarGpu.retire(var0.mesh);
      FarGpu.retire(var0.colours);
      FarGpu.retire(var0.trees);
      FarGpu.retire(var0.shrinking);
      retireOld(var0);
      retireComing(var0);
   }

   private static void startJobs(long var0) {
      for (FarWorld.Slot var3 : wanted) {
         if (FarWorkers.jobs.get() >= JOBS.getInt() || !CellJobs.start(var3, var0)) {
            break;
         }
      }
   }

   private static void letGo(float var0, float var1, float var2) {
      Iterator var3 = slots.values().iterator();

      while (var3.hasNext()) {
         FarWorld.Slot var4 = (FarWorld.Slot)var3.next();
         if (var4.seen != frameCounter) {
            float var5 = Math.max(0.0F, Math.max(var4.cellX * 256 - var0, var0 - (var4.cellX + 1) * 256));
            float var6 = Math.max(0.0F, Math.max(var4.cellY * 256 - var1, var1 - (var4.cellY + 1) * 256));
            if (var5 * var5 + var6 * var6 > (var2 + 256.0F) * (var2 + 256.0F)) {
               retire(var4);
               var3.remove();
            }
         }
      }
   }

   static int detail(float var0) {
      for (int var1 = 0; var1 < DETAIL_REACH.length; var1++) {
         if (var0 < DETAIL_REACH[var1].get() * 64.0F) {
            return DETAIL[var1];
         }
      }

      return DETAIL[DETAIL.length - 1];
   }

   private static void add(SceneData var0, FarWorld.Slot var1, Frame var2) {
      float var3 = var1.fade.progress(now);
      if (var3 >= 1.0F) {
         retireOld(var1);
      }

      add(var0, var1, var2, var1.mesh, var1.colours, var1.trees, 0.0F, Dissolve.top(var3));
      if (var1.oldMesh != null) {
         add(
                 var0,
                 var1,
                 var2,
                 var1.oldMesh,
                 var1.oldColours != null ? var1.oldColours : var1.colours,
                 var1.oldTrees != null ? var1.oldTrees : var1.trees,
                 var3,
                 2.0F
         );
      }
   }

   private static void add(SceneData var0, FarWorld.Slot var1, Frame var2, FarGpu.Mesh var3, FarGpu.Colours var4, FarGpu.Trees var5, float var6, float var7) {
      if (var0.farCount == var0.far.length) {
         var0.far = Arrays.copyOf(var0.far, var0.far.length * 2);
      }

      FarGpu.CellDraw var8 = var0.far[var0.farCount];
      if (var8 == null) {
         var8 = var0.far[var0.farCount] = new FarGpu.CellDraw();
      }

      var0.farCount++;
      var8.mesh = var3;
      var8.colours = var4;
      var8.trees = var5;
      var8.treeKeep = FarTrees.boxKeep(var3.detail);
      var8.treeGrow = FarTrees.grow(var8.treeKeep);
      var8.fadeLo = var6;
      var8.fadeHi = var7;
      var8.worldX = var1.cellX * 256;
      var8.worldY = var1.cellY * 256;
      var8.originX = (float)((double)var2.camX - var8.worldX);
      var8.originY = -var2.camZ * 2.4494896F;
      var8.originZ = (float)((double)var2.camY - var8.worldY);
      var8.maxHeight = var1.maxHeight + Relief.top(var0, var1.cellX, var1.cellY);
      var8.reliefLow = Relief.low(var0, var1.cellX, var1.cellY);
   }

   private static void install(CellJobs.Done var0) {
      FarWorld.Slot var1 = var0.slot();
      if (!var0.failed()) {
         if (slots.get(key(var1.cellX, var1.cellY)) != var1) {
            retire(var0);
         } else {
            retireComing(var1);
            var1.coming = var0;
            FarGpu.upload(var0.mesh());
            FarGpu.upload(var0.colours());
            FarGpu.upload(var0.trees());
         }
      }
   }

   private static void settle(FarWorld.Slot var0) {
      CellJobs.Done var1 = var0.coming;
      if (var1 != null && var1.uploaded()) {
         var0.coming = null;
         var0.busy = false;
         retireOld(var0);
         var0.fade.begin(now, var0.mesh == null ? 600000000L : 400000000L, 0.0F);
         var0.oldMesh = var0.mesh;
         if (var1.cell() != null) {
            var0.wantsData = false;
            var0.used = Caches.tick();
            var0.height = var1.cell().height;
            var0.maxHeight = var1.cell().maxHeight;
            var0.biomes = var1.cell().biomes;
            var0.oldColours = var0.colours;
            var0.colours = var1.colours();
            var0.oldTrees = var0.trees;
            var0.trees = var1.trees();
         }

         var0.mesh = var1.mesh();
      }
   }

   private static void retireComing(FarWorld.Slot var0) {
      if (var0.coming != null) {
         retire(var0.coming);
         var0.coming = null;
      }
   }

   private static void retire(CellJobs.Done var0) {
      FarGpu.retire(var0.mesh());
      FarGpu.retire(var0.colours());
      FarGpu.retire(var0.trees());
   }

   private static void retireOld(FarWorld.Slot var0) {
      FarGpu.retire(var0.oldMesh);
      FarGpu.retire(var0.oldColours);
      FarGpu.retire(var0.oldTrees);
      var0.oldMesh = null;
      var0.oldColours = null;
      var0.oldTrees = null;
   }

   static long key(int var0, int var1) {
      return (long)var0 << 32 | var1 & 4294967295L;
   }

   static FarWorld.Slot readCell(int var0, int var1) {
      FarWorld.Slot var2 = slots.get(key(var0, var1));
      if (var2 == null) {
         return null;
      } else if (var2.height != null && var2.tiles != null) {
         var2.used = Caches.tick();
         return var2;
      } else {
         var2.wantsData = true;
         return null;
      }
   }

   public static void clear() {
      FarShell.clear();
      BlockLight.clear();
      CellLight.clear();
      NearFacades.clear();

      for (FarWorld.Slot var1 : slots.values()) {
         retire(var1);
      }

      slots.clear();
      missing.clear();
      FarTiles.forget();
      reachLimit = Float.MAX_VALUE;
      Pool.FAR.use(0L);
   }

   private FarWorld() {
   }

   static {
      Caches.register(new CellMemory());
   }

   static final class Slot {
      final int cellX;
      final int cellY;
      short[] height;
      float maxHeight;
      FarGpu.Mesh mesh;
      FarGpu.Colours colours;
      FarGpu.Trees trees;
      FarGpu.Mesh oldMesh;
      FarGpu.Colours oldColours;
      FarGpu.Trees oldTrees;
      FarGpu.Colours shrinking;
      CellJobs.Done coming;
      final Dissolve fade = new Dissolve();
      boolean busy;
      long seen;
      float distance;
      String lotpack;
      int minLevel;
      int maxLevel;
      FarTiles.Tile[] tiles;
      byte[] biomes;
      BiomeTrees.Legend legend;
      short[][] rooms;
      int buildings;
      ShellFacades facades;
      GridRoofs roofs;
      GroundTiles groundTiles;
      int[] buildingBounds;
      boolean facading;
      long used;
      boolean wantsData;

      Slot(int var1, int var2) {
         this.cellX = var1;
         this.cellY = var2;
      }
   }
}