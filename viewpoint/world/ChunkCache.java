package viewpoint.world;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import viewpoint.core.Frame;
import viewpoint.light.LightSources;
import viewpoint.light.OwnedLight;
import viewpoint.models.Corpses;
import viewpoint.packs.ModelPacks;
import viewpoint.platform.LongMap;
import viewpoint.platform.Pipeline;
import viewpoint.platform.Profile;
import viewpoint.platform.ShaderPacks;
import viewpoint.render.ChunkMeshData;
import viewpoint.render.FarRelief;
import viewpoint.render.WorldRenderer;
import viewpoint.visibility.Rooms;
import zombie.erosion.season.ErosionIceQueen;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoChunkMap;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.fboRenderChunk.FBORenderLevels;
import zombie.iso.objects.IsoWindow;
import zombie.iso.objects.IsoWorldInventoryObject;

public final class ChunkCache {
   public static final int RING = 5;
   private static final int EVICT_PERIOD_FRAMES = 240;
   static final int LEVEL_OFFSET = 32;
   static final int LEVEL_COUNT = 64;
   private static int lastSnowTarget = Integer.MIN_VALUE;
   private static boolean lastWinter;
   private static boolean lastBaked = true;
   private static boolean lastRoofs = true;
   private static boolean lastVanilla = true;
   private static boolean lastTrimmed;
   private static boolean lastPerBuilding = true;
   private static int lastGrass;
   private static int lastCut;
   private static FarRelief.Field lastWalls;
   private static int lastPacks;
   static final int REASON_NEW = 0;
   static final int REASON_GAME = 1;
   static final int REASON_RETRY = 2;
   static final int REASON_RING = 3;
   static final int REASON_PACKS = 4;
   static long frameCounter;
   static final MutationInbox INBOX = new MutationInbox();
   private static final ArrayList<MutationInbox.Waiting> signals = new ArrayList<>();
   private static final HashMap<IsoChunk, ChunkCache.Entry> entries = new HashMap<>();
   private static final LongMap<ChunkCache.Entry> holders = new LongMap<>(256);
   private static final long GEOMETRY_DIRTY_FLAGS = 6110L;
   private static final long BLOOD_DIRTY_FLAG = 1L;

   private static void watchSnow(IsoCell var0) {
      int var1 = var0.getSnowTarget();
      boolean var2 = ErosionIceQueen.instance != null && ErosionIceQueen.instance.isSnow();
      boolean var3 = FloorGather.BAKED.get();
      boolean var4 = RoofGather.BUILT.get();
      boolean var5 = RoofGather.VANILLA.get();
      boolean var6 = RoofGather.TRIMMED.get();
      boolean var7 = WindowLevels.PER_BUILDING.get();
      int var8 = TileMeshes.GRASS_CARDS.get();
      int var9 = FarRelief.cut();
      FarRelief.Field var10 = FarRelief.walls();
      if (var1 != lastSnowTarget
         || var2 != lastWinter
         || var3 != lastBaked
         || var4 != lastRoofs
         || var5 != lastVanilla
         || var6 != lastTrimmed
         || var7 != lastPerBuilding
         || var8 != lastGrass
         || var9 != lastCut
         || var10 != lastWalls) {
         lastSnowTarget = var1;
         lastWinter = var2;
         lastBaked = var3;
         lastRoofs = var4;
         lastVanilla = var5;
         lastTrimmed = var6;
         lastPerBuilding = var7;
         lastGrass = var8;
         lastCut = var9;
         lastWalls = var10;
         markAll(1);
      }
   }

   private static void watchPacks() {
      ModelPacks.frame();
      int var0 = ModelPacks.generation();
      if (var0 != lastPacks) {
         lastPacks = var0;
         markAll(4);
      }

      ModelWaits.frame();
   }

   public static void update(Frame var0, IsoCell var1, int var2) {
      frameCounter++;
      ChunkBuilds.unchanged = 0;
      ChunkBuilds.builds = 0;
      ChunkBuilds.buildNanos = 0L;
      long var3 = System.nanoTime();
      IsoChunkMap var5 = var1.getChunkMap(var2);
      boolean var6 = takeSignals(var5);
      LightSources.update(var1, var2);
      watchSnow(var1);
      watchPacks();
      RoofDebug.update(var2);
      MapFacades.around((int)Math.floor(var0.camX), (int)Math.floor(var0.camY));
      int var7 = (int)Math.floor(var0.camZ);
      ChunkBuilds.installCooked(var3);
      var0.scene.shadowReach = WorldRenderer.farShadowRadius(var0.scene.nearEnd());
      Pipeline var8 = ShaderPacks.drawn().pipeline;
      boolean var9 = var0.scene.sunStrength > 0.02F && var8.shadows;
      DrawList.begin(var0.scene, var9);
      ChunkBudget.begin();
      Corpses.begin(var0);
      int var10 = ChunkWalk.walk(var0, var1, var2, var7, var3, var9);
      ChunkBuilds.buildQueued(System.nanoTime() + ChunkBuilds.buildBudget(var10), var2, var1, var3);
      int var11 = Math.floorDiv((int)Math.floor(var0.camX), 8);
      int var12 = Math.floorDiv((int)Math.floor(var0.camY), 8);
      OwnedLight.update(var11, var12, var7, (var1x, var2x, var3x) -> rewriteLight(var5, var1x, var2x, var3x));
      Rooms.select(var0);
      Corpses.pick(var0);
      DrawList.commit(var0.scene, var9, var8.shadowCascades);
      Rooms.endFrame();
      makeRoom();
      Profile.chunks(ChunkBuilds.builds, ChunkBuilds.buildNanos, var10);
      Profile.chunkSignals(INBOX.takeSignalCount(), var6, ChunkBuilds.unchanged);
      if (frameCounter % 240L == 0L) {
         evictUnseen();
         Rooms.sweep();
      }
   }

   private static boolean takeSignals(IsoChunkMap var0) {
      if (!INBOX.drain(signals)) {
         markAll(1);
         Blood.staleAll();
         return true;
      }

      for (MutationInbox.Waiting var2 : signals) {
         if ((var2.kinds & 2) != 0) {
            Blood.stale(var2.chunkX, var2.chunkY, var2.level);
         }

         IsoChunk var3 = var0.getChunkForGridSquare(var2.chunkX * 8, var2.chunkY * 8);
         ChunkCache.Entry var4 = var3 == null ? null : entries.get(var3);
         if (var4 != null && var4.wx == var2.chunkX && var4.wy == var2.chunkY) {
            if (var2.level == Integer.MIN_VALUE) {
               for (ChunkCache.Level var8 : var4.levels) {
                  if (var8 != null) {
                     apply(var8, var2);
                  }
               }
            } else if (var2.level + 32 >= 0 && var2.level + 32 < 64 && var4.levels[var2.level + 32] != null) {
               apply(var4.levels[var2.level + 32], var2);
            }
         }
      }

      signals.clear();
      return false;
   }

   private static void apply(ChunkCache.Level var0, MutationInbox.Waiting var1) {
      if ((var1.kinds & 1) != 0 && var1.geometryStamp > var0.gatherStamp) {
         mark(var0, 1);
      }
   }

   private static void rewriteLight(IsoChunkMap var0, int var1, int var2, int var3) {
      ChunkCache.Level var4 = level(var0, var1, var2, var3);
      if (var4 != null && var4.data != null) {
         ownLight(var4, var1, var2, var3);
      }
   }

   static void ownLight(ChunkCache.Level var0, int var1, int var2, int var3) {
      ByteBuffer var4 = var0.data.spareLight();
      if (OwnedLight.write(var4, var1, var2, var3)) {
         var0.data.publishLight(var4);
      }
   }

   static void markLevel(IsoChunkMap var0, int var1, int var2, int var3) {
      ChunkCache.Level var4 = level(var0, var1, var2, var3);
      if (var4 != null) {
         mark(var4, 1);
      }
   }

   private static ChunkCache.Level level(IsoChunkMap var0, int var1, int var2, int var3) {
      IsoChunk var4 = var0.getChunkForGridSquare(var1 * 8, var2 * 8);
      ChunkCache.Entry var5 = var4 == null ? null : entries.get(var4);
      return var5 != null && var5.wx == var1 && var5.wy == var2 && var3 + 32 >= 0 && var3 + 32 < 64 ? var5.levels[var3 + 32] : null;
   }

   private static void markAll(int var0) {
      for (ChunkCache.Entry var2 : entries.values()) {
         for (ChunkCache.Level var6 : var2.levels) {
            if (var6 != null) {
               mark(var6, var0);
            }
         }
      }
   }

   static float fadeOf(ChunkCache.Level var0, long var1) {
      if (var0.fadeNanos <= 0L) {
         return 1.0F;
      }

      float var3 = (float)(var1 - var0.fadeStart) / (float)var0.fadeNanos;
      if (var3 < 1.0F) {
         return Math.max(var3, 0.0F);
      }

      var0.fadeNanos = 0L;
      ChunkBudget.letGo(var0.fading);
      var0.fading = null;
      var0.fadingModelled = null;
      var0.fadingModelledItems = null;
      var0.fadingBodies = null;
      return 1.0F;
   }

   public static void clear() {
      Facades.clear();
      INBOX.clear();
      Blood.clear();
      Rooms.clear();
      OwnedLight.clear();
      FloorPages.clear();
      ModelWaits.clear();
      RoofLevels.clear();
      Iterator var0 = entries.values().iterator();

      while (var0.hasNext()) {
         retire((ChunkCache.Entry)var0.next());
         var0.remove();
      }

      ChunkWalk.clear();
   }

   public static ChunkMeshData meshOn(IsoGridSquare var0) {
      IsoChunk var1 = var0.getChunk();
      ChunkCache.Entry var2 = var1 == null ? null : existing(var1);
      int var3 = var0.z + 32;
      ChunkCache.Level var4 = var2 != null && var3 >= 0 && var3 < 64 ? var2.levels[var3] : null;
      return var4 == null ? null : var4.data;
   }

   public static boolean packsOn(IsoGridSquare var0) {
      IsoChunk var1 = var0.getChunk();
      ChunkCache.Entry var2 = var1 == null ? null : existing(var1);
      int var3 = var0.z + 32;
      ChunkCache.Level var4 = var2 != null && var3 >= 0 && var3 < 64 ? var2.levels[var3] : null;
      return var4 != null && var4.gatheredPacksNear;
   }

   static ChunkCache.Entry existing(IsoChunk var0) {
      ChunkCache.Entry var1 = entries.get(var0);
      return var1 != null && var1.wx == var0.wx && var1.wy == var0.wy ? var1 : null;
   }

   static ChunkCache.Entry entryFor(IsoChunk var0) {
      ChunkCache.Entry var1 = entries.get(var0);
      if (var1 != null && (var1.wx != var0.wx || var1.wy != var0.wy)) {
         retire(var1);
         var1 = null;
      }

      if (var1 == null) {
         var1 = new ChunkCache.Entry();
         var1.wx = var0.wx;
         var1.wy = var0.wy;
         entries.put(var0, var1);
      }

      hold(var1);
      return var1;
   }

   static void hold(ChunkCache.Entry var0) {
      holders.put(place(var0.wx, var0.wy), var0);
   }

   private static long place(int var0, int var1) {
      return (long)var0 << 32 | var1 & 4294967295L;
   }

   private static void evictUnseen() {
      Facades.clear();
      drop(240L);
      LongMap var0 = new LongMap(entries.size());

      for (ChunkCache.Entry var2 : entries.values()) {
         var0.put(RoofLevels.chunkKey(var2.wx, var2.wy), var2);
      }

      RoofLevels.sweep(var0);
   }

   private static void makeRoom() {
      if (ChunkBudget.full()) {
         drop(1L);
      }

      ChunkBudget.makeRoom();
   }

   private static void drop(long var0) {
      Iterator var2 = entries.values().iterator();

      while (var2.hasNext()) {
         ChunkCache.Entry var3 = (ChunkCache.Entry)var2.next();
         if (frameCounter - var3.lastSeenFrame >= var0) {
            retire(var3);
            var2.remove();
         }
      }
   }

   static void retire(ChunkCache.Entry var0) {
      long var1 = place(var0.wx, var0.wy);
      boolean var3 = holders.get(var1) == var0;
      if (var3) {
         holders.remove(var1);
         Rooms.forget(var0.wx, var0.wy);
         OwnedLight.forget(var0.wx, var0.wy);
         RoofLevels.forget(var0.wx, var0.wy);
      }

      for (int var4 = 0; var4 < 64; var4++) {
         ChunkCache.Level var5 = var0.levels[var4];
         if (var5 != null) {
            if (var3) {
               FloorPages.remove(var0.wx, var0.wy, var4 - 32);
            }

            var5.evicted = true;
            ChunkBudget.letGo(var5.data);
            ChunkBudget.letGo(var5.fading);
            var5.data = var5.fading = null;
         }
      }
   }

   static void mark(ChunkCache.Level var0, int var1) {
      var0.dirty = true;
      var0.changed = true;
      var0.reason = var1;
      var0.retryFrame = 0L;
      var0.retryDelay = 0;
   }

   public static void invalidated(FBORenderLevels var0, int var1, long var2) {
      int var4 = ((var2 & 6110L) != 0L ? 1 : 0) | ((var2 & 1L) != 0L ? 3 : 0);
      if (var4 != 0 && var0.getPlayerIndex() == 0) {
         IsoChunk var5 = var0.getChunk();
         INBOX.record(var5.wx, var5.wy, var1, var4);
      }
   }

   public static void windowChanged(IsoWindow var0) {
      IsoGridSquare var1 = var0.getSquare();
      IsoChunk var2 = var1 == null ? null : var1.getChunk();
      if (var2 != null) {
         INBOX.record(var2.wx, var2.wy, var1.getZ(), 1);
      }
   }

   private ChunkCache() {
   }

   static final class Entry {
      int wx;
      int wy;
      long lastSeenFrame;
      long leaveStart;
      final ChunkCache.Level[] levels = new ChunkCache.Level[64];
   }

   static final class Level {
      ChunkMeshData data;
      long version;
      boolean gatheredModelsNear;
      boolean evicted;
      boolean dirty = true;
      boolean changed = true;
      boolean modelsMatter = true;
      boolean packsMatter = true;
      boolean modelsNear;
      boolean packsNear;
      boolean gatheredPacksNear;
      long fingerprint;
      long gatherStamp;
      int acrossUnloaded;
      int adjacentLoads;
      long missedCell = Long.MIN_VALUE;
      int mapGeneration;
      ArrayList<IsoObject> modelled;
      ArrayList<IsoWorldInventoryObject> modelledItems;
      BodyCards bodies;
      ChunkMeshData fading;
      ArrayList<IsoObject> fadingModelled;
      ArrayList<IsoWorldInventoryObject> fadingModelledItems;
      BodyCards fadingBodies;
      long fadeStart;
      long fadeNanos;
      long retryFrame;
      int retryDelay;
      int reason = 0;
   }
}
