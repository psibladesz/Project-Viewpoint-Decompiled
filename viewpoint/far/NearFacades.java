package viewpoint.far;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import viewpoint.platform.Caches;
import viewpoint.platform.LongMap;
import viewpoint.world.Facades;
import viewpoint.world.MapFacades;
import zombie.iso.BuildingDef;
import zombie.iso.IsoMetaCell;
import zombie.iso.LotHeader;
import zombie.iso.MapFiles;
import zombie.iso.RoomDef;
import zombie.iso.RoomDef.RoomRect;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteManager;

final class NearFacades implements MapFacades.Source, Caches.Cache {
   private static final int AHEAD = 128;
   static final int RECT = 6;
   private static final long BUDGET_NANOS = 500000L;
   private static final ConcurrentLinkedQueue<NearFacades.Counted> DONE = new ConcurrentLinkedQueue<>();
   private static final LongMap<NearFacades.Cell> cells = new LongMap<>(64);
   private static final ArrayDeque<NearFacades.Prep> preparing = new ArrayDeque<>();
   private static int generation;
   private static long held;

   @Override
   public HashMap<Integer, HashMap<String, Integer>> building(BuildingDef var1) {
      NearFacades.Cell var2 = ready(MapFacades.cell(var1.getX(), var1.getY()));
      return var2 == null ? null : var2.buildings.get(var1);
   }

   @Override
   public MapFacades.RoomWalls room(RoomDef var1) {
      NearFacades.Cell var2 = ready(MapFacades.cell(var1.getX(), var1.getY()));
      return var2 == null ? null : var2.rooms.get(var1);
   }

   @Override
   public boolean counted(long var1) {
      NearFacades.Cell var3 = cells.get(var1);
      return var3 != null && var3.buildings != null;
   }

   @Override
   public int generation() {
      return generation;
   }

   @Override
   public void around(int var1, int var2) {
      takeIn();
      long var3 = Caches.tick();

      for (int var5 = Math.floorDiv(var2 - 128, 256); var5 <= Math.floorDiv(var2 + 128, 256); var5++) {
         for (int var6 = Math.floorDiv(var1 - 128, 256); var6 <= Math.floorDiv(var1 + 128, 256); var6++) {
            long var7 = FarWorld.key(var6, var5);
            NearFacades.Cell var9 = cells.get(var7);
            if (var9 == null) {
               start(var6, var5, var7);
            } else {
               var9.used = var3;
            }
         }
      }

      prepare(System.nanoTime() + 500000L);
   }

   static void clear() {
      cells.clear();
      preparing.clear();
      held = 0L;
   }

   private static NearFacades.Cell ready(long var0) {
      NearFacades.Cell var2 = cells.get(var0);
      if (var2 == null) {
         start((int)(var0 >> 32), (int)var0, var0);
         return null;
      }

      if (var2.buildings == null) {
         return null;
      }

      var2.used = Caches.tick();
      return var2;
   }

   private static void takeIn() {
      NearFacades.Counted var0;
      while ((var0 = DONE.poll()) != null) {
         NearFacades.Cell var1 = cells.get(var0.key());
         if (var1 != null) {
            install(var1, var0);
            held = held + var1.bytes;
            generation++;
         }
      }
   }

   private static void start(int var0, int var1, long var2) {
      NearFacades.Cell var4 = new NearFacades.Cell();
      var4.used = Caches.tick();
      cells.put(var2, var4);
      MapFiles var5 = CellJobs.mapFiles(var0, var1);
      LotHeader var6 = var5 == null ? null : (LotHeader)var5.infoHeaders.get(var0 + "_" + var1 + ".lotheader");
      String var7 = var5 == null ? null : (String)var5.infoFileNames.get("world_" + var0 + "_" + var1 + ".lotpack");
      IsoMetaCell var8 = CellRooms.metaCell(var0, var1);
      if (var6 != null && var7 != null && var8 != null) {
         preparing.add(new NearFacades.Prep(var4, var2, var0, var1, var6, var7, var8));
      } else {
         var4.buildings = new HashMap<>();
         var4.rooms = new HashMap<>();
      }
   }

   private static void prepare(long var0) {
      while (!preparing.isEmpty() && System.nanoTime() < var0) {
         NearFacades.Prep var2 = preparing.peek();
         if (cells.get(var2.key) != var2.cell) {
            preparing.poll();
         } else if (var2.advance(var0)) {
            preparing.poll();
            toWorker(var2);
         }
      }
   }

   private static void toWorker(NearFacades.Prep var0) {
      NearFacades.Rooms var1 = var0.numbered();
      ArrayList<String> var2 = var0.names;
      byte[] var3 = var0.kinds;
      String var4 = var0.lotpack;
      byte[] var5 = Lotpacks.get(var4);
      int var6 = var0.cellX;
      int var7 = var0.cellY;
      int var8 = var0.minLevel;
      int var9 = var0.maxLevel;
      int var10 = var0.buildings.size();
      long var11 = var0.key;
      BuildingDef[] var13 = var0.buildings.toArray(new BuildingDef[0]);
      RoomDef[] var14 = var0.rooms.toArray(new RoomDef[0]);
      FarWorkers.jobs.incrementAndGet();
      FarWorkers.POOL.execute(() -> {
         NearFacades.Counts var14x = null;

         try {
            byte[] var15 = var5 != null ? var5 : Files.readAllBytes(new File(var4).toPath());
            var14x = count(var3xx -> ShellMesher.read(var15, var8, var9, 0, 0, 32, var3xx), var3, var1, var10);
         } catch (Throwable var19) {
            System.out.println("[Viewpoint] far world: the near facades of cell " + var6 + "," + var7 + " failed: " + var19);
         } finally {
            DONE.add(new NearFacades.Counted(var11, var13, var14, var2, var14x));
            FarWorkers.jobs.decrementAndGet();
         }
      });
   }

   static NearFacades.Counts count(Consumer<ShellMesher.Visit> var0, byte[] var1, NearFacades.Rooms var2, int var3) {
      short[][] var4 = rasterise(var2);
      HashMap[] var5 = new HashMap[var3];
      HashMap[] var6 = new HashMap[var2.building().length];
      var0.accept((var5x, var6x, var7, var8) -> {
         byte var9 = var5x < var1.length ? var1[var5x] : -1;
         int var10 = var9 < 0 ? -1 : roomAt(var4, var6x, var7, var8);
         if (var10 > 0) {
            add(var6, var10 - 1, var9, var5x);
         } else if (var10 == 0) {
            int var11 = var9 % 2 == 1 ? roomAt(var4, var6x, var7 - 1, var8) : roomAt(var4, var6x - 1, var7, var8);
            int var12 = var11 > 0 ? var2.building()[var11 - 1] : -1;
            if (var12 >= 0) {
               add(var5, var12, var8 * 6 + var9, var5x);
               add(var5, var12, Integer.MIN_VALUE + var9, var5x);
            }
         }
      });
      return new NearFacades.Counts(var5, var6);
   }

   private static short[][] rasterise(NearFacades.Rooms var0) {
      short[][] var1 = new short[var0.levels()][];
      int[] var2 = var0.rects();

      for (int var3 = 0; var3 + 6 <= var2.length; var3 += 6) {
         int var4 = var2[var3 + 1];
         if (var4 >= 0 && var4 < var1.length) {
            if (var1[var4] == null) {
               var1[var4] = new short[65536];
            }

            for (int var5 = Math.max(0, var2[var3 + 3]); var5 < Math.min(256, var2[var3 + 3] + var2[var3 + 5]); var5++) {
               for (int var6 = Math.max(0, var2[var3 + 2]); var6 < Math.min(256, var2[var3 + 2] + var2[var3 + 4]); var6++) {
                  var1[var4][var5 * 256 + var6] = (short)(var2[var3] + 1);
               }
            }
         }
      }

      return var1;
   }

   private static int roomAt(short[][] var0, int var1, int var2, int var3) {
      boolean var4 = var1 >= 0 && var2 >= 0 && var1 < 256 && var2 < 256 && var3 >= 0 && var3 < var0.length;
      return var4 && var0[var3] != null ? var0[var3][var2 * 256 + var1] : 0;
   }

   private static void add(HashMap<Integer, HashMap<Integer, Integer>>[] var0, int var1, int var2, int var3) {
      if (var0[var1] == null) {
         var0[var1] = new HashMap();
      }

      var0[var1].computeIfAbsent(var2, var0x -> new HashMap<>()).merge(var3, 1, Integer::sum);
   }

   private static void install(NearFacades.Cell var0, NearFacades.Counted var1) {
      var0.buildings = new HashMap<>();
      var0.rooms = new HashMap<>();
      var0.bytes = 64L;
      NearFacades.Counts var2 = var1.counts();

      for (int var3 = 0; var2 != null && var3 < var2.buildings().length; var3++) {
         if (var2.buildings()[var3] != null) {
            var0.buildings.put(var1.buildings()[var3], named(var2.buildings()[var3], var1.names()));
            var0.bytes = var0.bytes + 128L + 112L * var2.buildings()[var3].size();
         }
      }

      for (int var4 = 0; var2 != null && var4 < var2.rooms().length; var4++) {
         if (var2.rooms()[var4] != null) {
            var0.rooms.put(var1.rooms()[var4], new MapFacades.RoomWalls(named(var2.rooms()[var4], var1.names())));
            var0.bytes = var0.bytes + 120L + 112L * var2.rooms()[var4].size();
         }
      }
   }

   private static HashMap<Integer, HashMap<String, Integer>> named(HashMap<Integer, HashMap<Integer, Integer>> var0, ArrayList<String> var1) {
      HashMap<Integer, HashMap<String, Integer>> var2 = new HashMap<>();
      var0.forEach((var2x, var3) -> {
         HashMap<String, Integer> var4 = new HashMap<>();
         var3.forEach((var2xx, var3x) -> var4.merge(var1.get(var2xx), var3x, Integer::sum));
         var2.put(var2x, var4);
      });
      return var2;
   }

   @Override
   public long bytes() {
      return held;
   }

   @Override
   public void list(Caches.Survey var1) {
      for (NearFacades.Cell var3 : cells.values()) {
         var1.add(var3.used, var3.bytes);
      }
   }

   @Override
   public void evictBefore(long var1) {
      cells.removeIf(var2 -> {
         boolean var3 = var2.used < var1;
         held = held - (var3 ? var2.bytes : 0L);
         return var3;
      });
   }

   static {
      NearFacades var0 = new NearFacades();
      MapFacades.provide(var0);
      Caches.register(var0);
   }

   private static final class Cell {
      HashMap<BuildingDef, HashMap<Integer, HashMap<String, Integer>>> buildings;
      HashMap<RoomDef, MapFacades.RoomWalls> rooms;
      long used;
      long bytes;
   }

   private record Counted(long key, BuildingDef[] buildings, RoomDef[] rooms, ArrayList<String> names, NearFacades.Counts counts) {
   }

   record Counts(HashMap<Integer, HashMap<Integer, Integer>>[] buildings, HashMap<Integer, HashMap<Integer, Integer>>[] rooms) {
   }

   static final class Prep {
      final NearFacades.Cell cell;
      final long key;
      final int cellX;
      final int cellY;
      final int minLevel;
      final int maxLevel;
      final String lotpack;
      final ArrayList<String> names;
      final ArrayList<RoomDef> list;
      final ArrayList<RoomDef> rooms = new ArrayList<>();
      final ArrayList<BuildingDef> buildings = new ArrayList<>();
      final IdentityHashMap<BuildingDef, Integer> numbers = new IdentityHashMap<>();
      final ArrayList<int[]> rects = new ArrayList<>();
      final ArrayList<Integer> owners = new ArrayList<>();
      final byte[] kinds;
      int room;
      int name;

      Prep(NearFacades.Cell var1, long var2, int var4, int var5, LotHeader var6, String var7, IsoMetaCell var8) {
         this.cell = var1;
         this.key = var2;
         this.cellX = var4;
         this.cellY = var5;
         this.minLevel = var6.minLevel;
         this.maxLevel = var6.maxLevel;
         this.lotpack = var7;
         this.names = var6.tilesUsed;
         this.list = new ArrayList<>(var8.roomList);
         this.kinds = new byte[this.names.size()];
      }

      boolean advance(long var1) {
         while (this.room < this.list.size()) {
            if ((this.room & 31) == 31 && System.nanoTime() > var1) {
               return false;
            }

            this.number(this.list.get(this.room));
            this.room++;
         }

         while (this.name < this.kinds.length) {
            if ((this.name & 31) == 31 && System.nanoTime() > var1) {
               return false;
            }

            IsoSprite var3 = (IsoSprite)IsoSpriteManager.instance.getNamedMap().get(this.names.get(this.name));
            this.kinds[this.name] = (byte)(var3 == null ? -1 : Facades.kindOf(var3));
            this.name++;
         }

         return true;
      }

      private void number(RoomDef var1) {
         if (CellRooms.indoors(var1) && var1.level <= this.maxLevel) {
            int var2 = this.rooms.size();
            int var3 = this.cellX * 256;
            int var4 = this.cellY * 256;
            this.rooms.add(var1);
            this.owners.add(var1.building == null ? -1 : this.numbers.computeIfAbsent(var1.building, var1x -> {
               this.buildings.add(var1x);
               return this.buildings.size() - 1;
            }));

            for (RoomRect var6 : var1.rects) {
               this.rects.add(new int[]{var2, var1.level, var6.x - var3, var6.y - var4, var6.w, var6.h});
            }
         }
      }

      NearFacades.Rooms numbered() {
         int[] var1 = new int[this.rects.size() * 6];
         int[] var2 = new int[this.owners.size()];

         for (int var3 = 0; var3 < this.rects.size(); var3++) {
            System.arraycopy(this.rects.get(var3), 0, var1, var3 * 6, 6);
         }

         for (int var4 = 0; var4 < var2.length; var4++) {
            var2[var4] = this.owners.get(var4);
         }

         return new NearFacades.Rooms(Math.max(1, this.maxLevel + 1), var1, var2);
      }
   }

   record Rooms(int levels, int[] rects, int[] building) {
   }
}