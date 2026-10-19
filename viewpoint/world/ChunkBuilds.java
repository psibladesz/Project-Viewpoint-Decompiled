package viewpoint.world;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import viewpoint.light.LightLayouts;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Profile;
import viewpoint.render.ChunkMeshData;
import viewpoint.visibility.Rooms;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;

final class ChunkBuilds {
   static final LiveSettings.Number BUILD_MIN = LiveSettings.number("chunks.buildBudgetMs", "Build budget (ms)", "World/Chunks", 0.5F, 20.0F, 0.5F, 3.0F);
   static final LiveSettings.Number BUILD_PER_WAITING = LiveSettings.number(
      "chunks.buildPerWaitingMs", "More per level waiting (ms)", "World/Chunks", 0.0F, 1.0F, 0.05F, 0.15F
   );
   static final LiveSettings.Number BUILD_MAX = LiveSettings.number("chunks.buildMaxMs", "Build budget at most (ms)", "World/Chunks", 1.0F, 30.0F, 0.5F, 9.0F);
   private static final int RETRY_FIRST = 10;
   private static final int RETRY_PICTURE = 2;
   private static final int RETRY_MAX = 240;
   static final long APPEAR_NANOS = 600000000L;
   static final long CROSS_NANOS = 400000000L;
   static final long NEAR_CROSS_NANOS = 150000000L;
   private static int cooking;
   private static final LiveSettings.Number MAX_COOKING = LiveSettings.number(
      "chunks.cookingMax", "Levels cooking at once, at most", "World/Chunks", 16.0F, 1024.0F, 16.0F, 256.0F
   );
   static int builds;
   static int unchanged;
   static long buildNanos;
   private static final ArrayList<ChunkBuilds.Queued> queue = new ArrayList<>();
   private static int queued;
   private static final LiveSettings.Number COOK_THREADS = LiveSettings.number(
      "chunks.cookThreads",
      "Mesher worker threads",
      "World/Chunks",
      1.0F,
      8.0F,
      1.0F,
      Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() / 2 - 1))
   );
   private static final ThreadPoolExecutor COOKS = new ThreadPoolExecutor(
      COOK_THREADS.getInt(), COOK_THREADS.getInt(), 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), var0 -> {
         Thread var1 = new Thread(var0, "Viewpoint mesher");
         var1.setDaemon(true);
         var1.setPriority(4);
         return var1;
      }
   );
   private static final ConcurrentLinkedQueue<ChunkBuilds.Cooked> COOKED = new ConcurrentLinkedQueue<>();
   private static volatile boolean cookFailed;

   static long buildBudget(int var0) {
      return (long)(Math.min(BUILD_MAX.get(), BUILD_MIN.get() + var0 * BUILD_PER_WAITING.get()) * 1000000.0);
   }

   static void queue(ChunkCache.Level var0, IsoChunk var1, int var2, boolean var3, float var4) {
      if (queued == queue.size()) {
         queue.add(new ChunkBuilds.Queued());
      }

      ChunkBuilds.Queued var5 = queue.get(queued++);
      var5.l = var0;
      var5.chunk = var1;
      var5.level = var2;
      var5.modelsNear = var3;
      var5.priority = var4;
   }

   static void buildQueued(long var0, int var2, IsoCell var3, long var4) {
      queue.subList(0, queued).sort((var0x, var1) -> Float.compare(var0x.priority, var1.priority));
      resizeCooks();
      int var6 = MAX_COOKING.getInt();

      for (int var7 = 0; var7 < queued; var7++) {
         ChunkBuilds.Queued var8 = queue.get(var7);
         if ((var7 == 0 || System.nanoTime() < var0) && cooking < var6) {
            rebuild(var8.l, var8.chunk, var8.level, var2, var8.modelsNear, var3, var4, 400000000L, false);
         }

         var8.l = null;
         var8.chunk = null;
      }

      queued = 0;
   }

   static void rebuild(ChunkCache.Level var0, IsoChunk var1, int var2, int var3, boolean var4, IsoCell var5, long var6, long var8, boolean var10) {
      long var11 = System.nanoTime();
      Profile.rebuilds++;
      Profile.rebuildReason(var0.data == null && var0.fading == null ? 0 : var0.reason);
      var0.dirty = false;
      boolean var13 = var0.changed;
      var0.changed = false;
      var0.gatherStamp = ChunkCache.INBOX.stamp();
      var0.adjacentLoads = var1.adjacentChunkLoadedCounter;
      var0.mapGeneration = MapFacades.generation();
      WallMesher.begin();
      MapFacades.begin();
      Recipe var14 = WorldMesher.gather(var1, var2, var3, var4, var0.packsNear, var13);
      var0.acrossUnloaded = WallMesher.guessed();
      var0.missedCell = MapFacades.missed();
      var0.gatheredPacksNear = var0.packsNear;
      var0.modelsMatter = var14.modelsMatter;
      var0.packsMatter = var14.packsMatter;
      RoofLevels.markWaiting(var5.getChunkMap(var3));
      if (var13) {
         Rooms.captured(var1, var2);
         LightLayouts.captured(var5, var1, var2);
      }

      long var15 = ++var0.version;
      var0.gatheredModelsNear = var4;
      ModelWaits.gathered(var0, var14.waiting);
      if (var14.pending) {
         var0.dirty = true;
         var0.reason = 2;
         int var17 = var14.picturePending ? 2 : 10;
         var0.retryDelay = var0.retryDelay == 0 ? var17 : Math.min(240, var0.retryDelay * 2);
         var0.retryFrame = ChunkCache.frameCounter + var0.retryDelay;
      } else {
         var0.retryDelay = 0;
      }

      if (var10 && var14.fingerprint() == var0.fingerprint) {
         keep(var0, var1, var2, var14, var4);
      } else if (var10) {
         install(var0, var1, var2, var14, Cook.mesh(var14), var4, var6, var8);
      } else {
         cookLater(var0, var1, var2, var15, var14, var4, var8);
      }

      builds++;
      buildNanos = buildNanos + (System.nanoTime() - var11);
   }

   static void installCooked(long var0) {
      ChunkBuilds.Cooked var2;
      while ((var2 = COOKED.poll()) != null) {
         cooking--;
         boolean var3 = !var2.failed() && !var2.l().evicted && var2.version() == var2.l().version;
         if (var3 && var2.recipe().fingerprint() == var2.l().fingerprint) {
            keep(var2.l(), var2.chunk(), var2.level(), var2.recipe(), var2.modelsNear());
         } else if (var3) {
            install(var2.l(), var2.chunk(), var2.level(), var2.recipe(), var2.data(), var2.modelsNear(), var0, var2.crossNanos());
         } else if (var2.data() != null) {
            var2.data().discard();
         }
      }

      Profile.cooking = cooking;
   }

   private static void install(ChunkCache.Level var0, IsoChunk var1, int var2, Recipe var3, ChunkMeshData var4, boolean var5, long var6, long var8) {
      ChunkBudget.letGo(var0.fading);
      if (var0.fading == null && var0.fadeNanos > 0L && var6 - var0.fadeStart < var0.fadeNanos) {
         ChunkBudget.letGo(var0.data);
         var0.fading = null;
         var0.fadingModelled = null;
         var0.fadingModelledItems = null;
         var0.fadingBodies = null;
      } else {
         var0.fading = var0.data;
         var0.fadingModelled = var0.modelsNear ? var0.modelled : null;
         var0.fadingModelledItems = var0.modelsNear ? var0.modelledItems : null;
         var0.fadingBodies = var0.bodies;
         var0.fadeStart = var6;
         var0.fadeNanos = var0.fading == null ? 600000000L : var8;
      }

      var0.modelsNear = var5;
      var0.modelled = var3.modelled;
      var0.modelledItems = var3.modelledItems;
      var0.bodies = var3.bodies;
      var0.data = var4;
      ChunkBudget.hold(var4);
      var0.fingerprint = var3.fingerprint();
      FloorPages.put(var1.wx, var1.wy, var2, var3.floor);
      if (var4 != null) {
         ChunkCache.ownLight(var0, var1.wx, var1.wy, var2);
      }
   }

   private static void keep(ChunkCache.Level var0, IsoChunk var1, int var2, Recipe var3, boolean var4) {
      var0.modelsNear = var4;
      var0.modelled = var3.modelled;
      var0.modelledItems = var3.modelledItems;
      var0.bodies = var3.bodies;
      FloorPages.put(var1.wx, var1.wy, var2, var3.floor);
      unchanged++;
   }

   private static void cookLater(ChunkCache.Level var0, IsoChunk var1, int var2, long var3, Recipe var5, boolean var6, long var7) {
      long var9 = var0.fingerprint;
      cooking++;
      COOKS.execute(() -> {
         ChunkMeshData var11 = null;
         boolean var12 = false;

         try {
            var11 = var5.fingerprint() == var9 ? null : Cook.mesh(var5);
         } catch (Throwable var14) {
            var12 = true;
            if (!cookFailed) {
               cookFailed = true;
               System.out.println("[Viewpoint] mesher: a level failed to cook (it keeps its old mesh):");
               var14.printStackTrace(System.out);
            }
         }

         COOKED.add(new ChunkBuilds.Cooked(var0, var1, var2, var3, var5, var11, var6, var7, var12));
      });
   }

   private static void resizeCooks() {
      int var0 = COOK_THREADS.getInt();
      if (var0 > COOKS.getMaximumPoolSize()) {
         COOKS.setMaximumPoolSize(var0);
         COOKS.setCorePoolSize(var0);
      } else if (var0 < COOKS.getCorePoolSize()) {
         COOKS.setCorePoolSize(var0);
         COOKS.setMaximumPoolSize(var0);
      }
   }

   private ChunkBuilds() {
   }

   private record Cooked(
      ChunkCache.Level l, IsoChunk chunk, int level, long version, Recipe recipe, ChunkMeshData data, boolean modelsNear, long crossNanos, boolean failed
   ) {
   }

   private static final class Queued {
      ChunkCache.Level l;
      IsoChunk chunk;
      int level;
      boolean modelsNear;
      float priority;
   }
}
