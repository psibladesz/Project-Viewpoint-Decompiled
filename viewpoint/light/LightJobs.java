package viewpoint.light;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import viewpoint.platform.Caches;
import viewpoint.platform.LongMap;

final class LightJobs implements Caches.Cache {
   private static final int SKY_CHUNKS = 3;
   private static final int MAX_IN_FLIGHT = 128;
   private static final int DISTANCES = 64;
   private static final int MAX_LOOKED = 1024;
   private static final LongMap<LightJobs.Held> levels = new LongMap<>(2048);
   private static final ArrayList<SourceRegister.Entry> dueSources = new ArrayList<>();
   private static final ArrayList<LightJobs.Held> dueLevels = new ArrayList<>();
   private static final ArrayList<SourceLight> reaching = new ArrayList<>();
   private static int inFlight;
   private static int generation;
   private static long levelBytes;
   private static final int[] counts = new int[66];
   private static LightJobs.Held[] order = new LightJobs.Held[256];
   private static int[] orderDistance = new int[256];
   static final ArrayList<LevelLight> worked = new ArrayList<>();
   static int sourceJobs;
   static int levelJobs;
   static long sourceNanos;
   static long levelNanos;
   private static final ThreadPoolExecutor WORKERS = new ThreadPoolExecutor(2, 2, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), var0 -> {
      Thread var1 = new Thread(var0, "Viewpoint light");
      var1.setDaemon(true);
      var1.setPriority(4);
      return var1;
   });
   private static final ThreadLocal<LightWork> WORK = ThreadLocal.withInitial(LightWork::new);
   private static final ConcurrentLinkedQueue<LightJobs.SourceDone> SOURCES_DONE = new ConcurrentLinkedQueue<>();
   private static final ConcurrentLinkedQueue<LightJobs.LevelDone> LEVELS_DONE = new ConcurrentLinkedQueue<>();
   private static volatile boolean jobFailed;

   static void mark(int var0, int var1, int var2) {
      LightJobs.Held var3 = levels.get(LightLayouts.key(var0, var1, var2));
      if (var3 != null && !var3.due) {
         var3.due = true;
         dueLevels.add(var3);
      }
   }

   static void layoutChanged(int var0, int var1, int var2) {
      long var3 = LightLayouts.key(var0, var1, var2);
      if (LightLayouts.get(var0, var1, var2) == null) {
         drop(levels.remove(var3));
      } else if (levels.get(var3) == null) {
         LightJobs.Held var5 = new LightJobs.Held(var0, var1, var2);
         levels.put(var3, var5);
         dueLevels.add(var5);
      }

      for (int var7 = -3; var7 <= 3; var7++) {
         for (int var6 = -3; var6 <= 3; var6++) {
            mark(var0 + var6, var1 + var7, var2);
         }
      }

      LightSources.register().relayout(var0, var1, var2);
   }

   static LevelLight light(int var0, int var1, int var2) {
      LightJobs.Held var3 = levels.get(LightLayouts.key(var0, var1, var2));
      return var3 == null ? null : var3.light;
   }

   static void update(int var0, int var1, int var2) {
      worked.clear();
      takeIn();
      SourceRegister var3 = LightSources.register();
      dueSources.clear();
      var3.due(dueSources);
      dueSources.sort((var3x, var4x) -> Integer.compare(distance(var3x, var0, var1, var2), distance(var4x, var0, var1, var2)));

      for (int var4 = 0; var4 < dueSources.size() && inFlight < 128; var4++) {
         handOut(dueSources.get(var4));
      }

      if (inFlight < 128) {
         nearestFirst(var0, var1, var2);
         int var8 = 0;
         int var5 = 0;

         for (int var6 = 0; var6 < dueLevels.size(); var6++) {
            LightJobs.Held var7 = dueLevels.get(var6);
            if (levels.get(LightLayouts.key(var7.chunkX, var7.chunkY, var7.level)) == var7) {
               reaching.clear();
               if (!var7.pending && inFlight < 128 && var5++ < 1024 && var3.lights(var7.chunkX, var7.chunkY, var7.level, reaching)) {
                  handOut(var7, List.copyOf(reaching));
               } else {
                  dueLevels.set(var8++, var7);
               }
            }
         }

         dueLevels.subList(var8, dueLevels.size()).clear();
      }
   }

   private static void handOut(SourceRegister.Entry var0) {
      SourceRegister.Lamp var1 = var0.lamp;
      SourceRegister.RoomLight var2 = var0.room;
      SourceLight.Box var3 = var1 != null ? SourceLight.box(var1) : SourceLight.box(var2);
      List var4 = layouts(var3.x0(), var3.y0(), var3.z0(), var3.x1(), var3.y1(), var3.z1());
      int var5 = var0.version;
      int var6 = var0.sourceVersion;
      int var7 = generation;
      var0.pending = true;
      inFlight++;
      WORKERS.execute(() -> {
         long var8 = System.nanoTime();
         SourceLight var10 = SourceLight.NONE;

         try {
            LightWork var11 = WORK.get();
            var10 = var1 != null ? SourceLight.lamp(var11, var1, var3, var4) : SourceLight.room(var11, var2, var3, var4);
         } catch (Throwable var12) {
            failed(var12);
         }

         SOURCES_DONE.add(new LightJobs.SourceDone(var0, var5, var6, var7, var10, System.nanoTime() - var8));
      });
   }

   private static void handOut(LightJobs.Held var0, List<SourceLight> var1) {
      int var2 = var0.chunkX;
      int var3 = var0.chunkY;
      int var4 = var0.level;
      int var5 = generation;
      List var6 = layouts(LevelLight.firstSquare(var2), LevelLight.firstSquare(var3), var4, LevelLight.lastSquare(var2), LevelLight.lastSquare(var3), var4);
      var0.due = false;
      var0.pending = true;
      inFlight++;
      WORKERS.execute(() -> {
         long var7 = System.nanoTime();
         LevelLight var9 = null;

         try {
            var9 = LevelLight.work(WORK.get(), var2, var3, var4, var6, var1);
         } catch (Throwable var11) {
            failed(var11);
         }

         LEVELS_DONE.add(new LightJobs.LevelDone(var0, var5, var9, System.nanoTime() - var7));
      });
   }

   private static List<LightLayouts.Level> layouts(int var0, int var1, int var2, int var3, int var4, int var5) {
      ArrayList var6 = new ArrayList();

      for (int var7 = var2; var7 <= var5; var7++) {
         for (int var8 = Math.floorDiv(var1, 8); var8 <= Math.floorDiv(var4, 8); var8++) {
            for (int var9 = Math.floorDiv(var0, 8); var9 <= Math.floorDiv(var3, 8); var9++) {
               LightLayouts.Level var10 = LightLayouts.get(var9, var8, var7);
               if (var10 != null) {
                  var6.add(var10);
               }
            }
         }
      }

      return var6;
   }

   private static void takeIn() {
      SourceRegister var0 = LightSources.register();

      LightJobs.SourceDone var1;
      while ((var1 = SOURCES_DONE.poll()) != null) {
         inFlight--;
         sourceJobs++;
         sourceNanos = sourceNanos + var1.nanos();
         if (var1.generation() == generation) {
            var0.worked(var1.entry(), var1.version(), var1.sourceVersion(), var1.light(), LightJobs::mark);
         } else {
            var1.entry().pending = false;
         }
      }

      LightJobs.LevelDone var4;
      while ((var4 = LEVELS_DONE.poll()) != null) {
         inFlight--;
         levelJobs++;
         levelNanos = levelNanos + var4.nanos();
         LightJobs.Held var2 = var4.held();
         var2.pending = false;
         boolean var3 = var4.generation() == generation && levels.get(LightLayouts.key(var2.chunkX, var2.chunkY, var2.level)) == var2;
         if (var3 && var4.light() != null) {
            levelBytes = levelBytes + (var4.light().bytes() - (var2.light == null ? 0L : var2.light.bytes()));
            var2.light = var4.light();
            worked.add(var4.light());
         }
      }
   }

   private static void nearestFirst(int var0, int var1, int var2) {
      int var3 = dueLevels.size();
      if (order.length < var3) {
         order = new LightJobs.Held[var3 * 2];
         orderDistance = new int[var3 * 2];
      }

      Arrays.fill(counts, 0);

      for (int var4 = 0; var4 < var3; var4++) {
         LightJobs.Held var5 = dueLevels.get(var4);
         int var6 = Math.min(64, distance(var5.chunkX, var5.chunkY, var5.level, var0, var1, var2));
         orderDistance[var4] = var6;
         counts[var6 + 1]++;
      }

      for (int var7 = 0; var7 <= 64; var7++) {
         counts[var7 + 1] = counts[var7 + 1] + counts[var7];
      }

      for (int var8 = 0; var8 < var3; var8++) {
         order[counts[orderDistance[var8]]++] = dueLevels.get(var8);
      }

      for (int var9 = 0; var9 < var3; var9++) {
         dueLevels.set(var9, order[var9]);
         order[var9] = null;
      }
   }

   private static int distance(SourceRegister.Entry var0, int var1, int var2, int var3) {
      int var4 = var0.lamp != null ? var0.lamp.x() : var0.room.x();
      int var5 = var0.lamp != null ? var0.lamp.y() : var0.room.y();
      int var6 = var0.lamp != null ? var0.lamp.z() : var0.room.z();
      return distance(Math.floorDiv(var4, 8), Math.floorDiv(var5, 8), var6, var1, var2, var3);
   }

   private static int distance(int var0, int var1, int var2, int var3, int var4, int var5) {
      return Math.max(Math.max(Math.abs(var0 - var3), Math.abs(var1 - var4)), Math.abs(var2 - var5));
   }

   private static void drop(LightJobs.Held var0) {
      if (var0 != null && var0.light != null) {
         levelBytes = levelBytes - var0.light.bytes();
         BandGrids.dropped(var0.chunkX, var0.chunkY, var0.level);
      }
   }

   static void lights(Consumer<LevelLight> var0) {
      for (LightJobs.Held var2 : levels.values()) {
         if (var2.light != null) {
            var0.accept(var2.light);
         }
      }
   }

   static void clear() {
      generation++;
      levels.clear();
      dueLevels.clear();
      levelBytes = 0L;
      worked.clear();
   }

   @Override
   public long bytes() {
      return levelBytes + LightSources.register().lightBytes();
   }

   @Override
   public void list(Caches.Survey var1) {
   }

   @Override
   public void evictBefore(long var1) {
   }

   private static void failed(Throwable var0) {
      if (!jobFailed) {
         jobFailed = true;
         System.out.println("[Viewpoint] light: a job failed (a level keeps its light, a source lights nothing):");
         var0.printStackTrace(System.out);
      }
   }

   static {
      Caches.register(new LightJobs());
   }

   private static final class Held {
      final int chunkX;
      final int chunkY;
      final int level;
      LevelLight light;
      boolean due = true;
      boolean pending;

      Held(int var1, int var2, int var3) {
         this.chunkX = var1;
         this.chunkY = var2;
         this.level = var3;
      }
   }

   private record LevelDone(LightJobs.Held held, int generation, LevelLight light, long nanos) {
   }

   private record SourceDone(SourceRegister.Entry entry, int version, int sourceVersion, int generation, SourceLight light, long nanos) {
   }
}