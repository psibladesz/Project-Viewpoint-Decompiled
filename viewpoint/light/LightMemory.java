package viewpoint.light;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import viewpoint.platform.Caches;
import viewpoint.platform.LongMap;
import zombie.GameTime;
import zombie.SandboxOptions;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoChunkMap;
import zombie.iso.IsoLightSource;
import zombie.iso.IsoMetaGrid;
import zombie.iso.IsoRoomLight;
import zombie.iso.IsoWorld;
import zombie.iso.objects.IsoLightSwitch;

final class LightMemory {
   static final int CELL_CHUNKS = 32;
   private static final LightMemory.Lamp[] NONE = new LightMemory.Lamp[0];
   private static final long CAPTURE_NANOS = 1000000000L;
   private static final long CHUNK_BYTES = 64L;
   private static final long LAMP_BYTES = 160L;
   private static final long ROOM_BYTES = 48L;
   private static final int CHUNK_SHIFT = 34;
   private static final int INDEX_BITS = 20;
   private static final LongMap<LightMemory.Chunk> chunks = new LongMap<>(1024);
   private static final LongMap<LightMemory.Cell> cells = new LongMap<>(64);
   private static final LongMap<LightMemory.Room> roomPower = new LongMap<>(256);
   private static IsoCell lastCell;
   private static long nextCapture;
   private static long[] order = new long[512];
   private static int lampCount;
   private static int evictions;
   private static long captureNanos;

   static void capture(IsoCell var0) {
      if (var0 != lastCell) {
         clear();
         lastCell = var0;
      }

      long var1 = System.nanoTime();
      IsoChunkMap var3 = var0.getChunkMap(0);
      if (var1 >= nextCapture && var3 != null) {
         nextCapture = var1 + 1000000000L;
         int var4 = Math.floorDiv(var3.getWorldXMinTiles(), 8);
         int var5 = Math.floorDiv(var3.getWorldYMinTiles(), 8);
         int var6 = Math.floorDiv(var3.getWorldXMaxTiles() - 1, 8) - var4 + 1;
         int var7 = Math.floorDiv(var3.getWorldYMaxTiles() - 1, 8) - var5 + 1;
         Stack var8 = var0.getLamppostPositions();
         int var9 = sortByChunk(var8, var4, var5, var6, var7);
         boolean var10 = SandboxOptions.instance.doesPowerGridExist();
         boolean var11 = GameTime.getInstance().getNight() >= 0.5F;
         IsoMetaGrid var12 = IsoWorld.instance.getMetaGrid();
         int var13 = 0;

         for (int var14 = 0; var14 < var6 * var7; var14++) {
            int var15 = var13;

            while (var15 < var9 && order[var15] >>> 34 == var14) {
               var15++;
            }

            int var16 = var4 + var14 % var6;
            int var17 = var5 + var14 / var6;
            IsoChunk var18 = var0.getChunk(var16, var17);
            if (var18 != null && var18.loaded) {
               seen(var16, var17, var8, var13, var15, var10, var11, var12);
            }

            var13 = var15;
         }

         rooms(var0);
         captureNanos = captureNanos + (System.nanoTime() - var1);
      }
   }

   static LightMemory.Lamp[] lamps(int var0, int var1) {
      LightMemory.Chunk var2 = chunks.get(key(var0, var1));
      return var2 == null ? null : var2.lamps;
   }

   static boolean seen(int var0, int var1) {
      return chunks.get(key(var0, var1)) != null;
   }

   static boolean cellSeen(int var0, int var1) {
      return cells.get(key(var0, var1)) != null;
   }

   static LightMemory.Lamp[] outdoorLamps(int var0, int var1) {
      LightMemory.Cell var2 = cells.get(key(var0, var1));
      if (var2 == null) {
         return NONE;
      }

      if (var2.stale) {
         ArrayList<LightMemory.Lamp> var3 = new ArrayList<>();

         for (int var4 = var1 * 32; var4 < (var1 + 1) * 32; var4++) {
            for (int var5 = var0 * 32; var5 < (var0 + 1) * 32; var5++) {
               LightMemory.Chunk var6 = chunks.get(key(var5, var4));

               for (int var7 = 0; var6 != null && var7 < var6.lamps.length; var7++) {
                  if (var6.lamps[var7].outdoor) {
                     var3.add(var6.lamps[var7]);
                  }
               }
            }
         }

         var2.outdoor = var3.toArray(NONE);
         var2.stale = false;
      }

      return var2.outdoor;
   }

   static boolean ownPower(long var0) {
      LightMemory.Room var2 = roomPower.get(var0);
      return var2 != null && var2.ownPower;
   }

   static int evictions() {
      return evictions;
   }

   static void remember(int var0, int var1, LightMemory.Lamp[] var2, long var3) {
      LightMemory.Chunk var5 = chunks.get(key(var0, var1));
      if (var5 == null) {
         var5 = new LightMemory.Chunk();
         chunks.put(key(var0, var1), var5);
         cell(var0, var1).chunks++;
      }

      if (!Arrays.equals(var5.lamps, var2)) {
         lampCount = lampCount + (var2.length - var5.lamps.length);
         var5.lamps = var2;
         cell(var0, var1).stale = true;
      }

      var5.used = var3;
   }

   static void rememberRoom(long var0, boolean var2, long var3) {
      LightMemory.Room var5 = roomPower.get(var0);
      if (var5 == null) {
         var5 = new LightMemory.Room();
         roomPower.put(var0, var5);
      }

      var5.ownPower = var2;
      var5.used = var3;
   }

   static int chunks() {
      return chunks.size();
   }

   static int lampsHeld() {
      return lampCount;
   }

   static long takeCaptureNanos() {
      long var0 = captureNanos;
      captureNanos = 0L;
      return var0;
   }

   static void clear() {
      chunks.clear();
      cells.clear();
      roomPower.clear();
      lampCount = 0;
      nextCapture = 0L;
   }

   static void evict(long var0) {
      boolean var2 = false;

      for (int var3 = 0; var3 < chunks.capacity(); var3++) {
         LightMemory.Chunk var4 = chunks.valueAt(var3);
         if (var4 != null && var4.used < var0) {
            long var5 = chunks.keyAt(var3);
            int var7 = (int)(var5 >> 32);
            int var8 = (int)var5;
            chunks.removeAt(var3);
            lampCount = lampCount - var4.lamps.length;
            LightMemory.Cell var9 = cells.get(key(Math.floorDiv(var7, 32), Math.floorDiv(var8, 32)));
            if (--var9.chunks == 0) {
               cells.remove(key(Math.floorDiv(var7, 32), Math.floorDiv(var8, 32)));
            } else {
               var9.stale = true;
            }

            var2 = true;
         }
      }

      for (int var10 = 0; var10 < roomPower.capacity(); var10++) {
         LightMemory.Room var11 = roomPower.valueAt(var10);
         if (var11 != null && var11.used < var0) {
            roomPower.removeAt(var10);
            var2 = true;
         }
      }

      evictions += var2 ? 1 : 0;
   }

   private static int sortByChunk(Stack<IsoLightSource> var0, int var1, int var2, int var3, int var4) {
      int var5 = 0;

      for (int var6 = 0; var6 < var0.size(); var6++) {
         IsoLightSource var7 = (IsoLightSource)var0.get(var6);
         if (var7.id != 0 && var7.life == -1 && var7.hydroPowered && !var7.switches.isEmpty()) {
            int var8 = Math.floorDiv(var7.x, 8) - var1;
            int var9 = Math.floorDiv(var7.y, 8) - var2;
            if (var8 >= 0 && var9 >= 0 && var8 < var3 && var9 < var4) {
               order = var5 == order.length ? Arrays.copyOf(order, var5 * 2) : order;
               long var10 = var7.x & 7 | (var7.y & 7) << 3 | (long)(var7.z + 32 & 0xFF) << 6;
               order[var5++] = (long)(var9 * var3 + var8) << 34 | var10 << 20 | var6;
            }
         }
      }

      Arrays.sort(order, 0, var5);
      return var5;
   }

   private static void seen(int var0, int var1, Stack<IsoLightSource> var2, int var3, int var4, boolean var5, boolean var6, IsoMetaGrid var7) {
      LightMemory.Chunk var8 = chunks.get(key(var0, var1));
      LightMemory.Lamp[] var9 = var8 == null ? NONE : var8.lamps;
      boolean var10 = var9.length == var4 - var3;

      for (int var11 = var3; var10 && var11 < var4; var11++) {
         var10 = same(var9[var11 - var3], source(var2, var11), var5, var6);
      }

      if (var10 && var8 != null) {
         var8.used = Caches.tick();
      } else {
         LightMemory.Lamp[] var13 = var4 == var3 ? NONE : new LightMemory.Lamp[var4 - var3];

         for (int var12 = var3; var12 < var4; var12++) {
            var13[var12 - var3] = lamp(source(var2, var12), var9, var5, var6, var7);
         }

         remember(var0, var1, var13, Caches.tick());
      }
   }

   private static IsoLightSource source(Stack<IsoLightSource> var0, int var1) {
      return (IsoLightSource)var0.get((int)(order[var1] & 1048575L));
   }

   private static boolean same(LightMemory.Lamp var0, IsoLightSource var1, boolean var2, boolean var3) {
      SourceRegister.Lamp var4 = var0.light;
      IsoLightSwitch var5 = (IsoLightSwitch)var1.switches.get(0);
      return var4.x() == var1.x
              && var4.y() == var1.y
              && var4.z() == var1.z
              && var4.radius() == Math.min(var1.radius, 20)
              && var4.r() == LightSources.engine(var1.r)
              && var4.g() == LightSources.engine(var1.g)
              && var4.b() == LightSources.engine(var1.b)
              && var4.building() == LightSources.building(var1)
              && var0.street == var5.streetLight
              && var0.switchedOn == var5.isActivated()
              && var0.ownPower == ownPower(var1, var5, var2, var3, var0.inBuilding, var0.noPower);
   }

   private static LightMemory.Lamp lamp(IsoLightSource var0, LightMemory.Lamp[] var1, boolean var2, boolean var3, IsoMetaGrid var4) {
      LightMemory.Lamp var5 = null;

      for (int var6 = 0; var5 == null && var6 < var1.length; var6++) {
         SourceRegister.Lamp var7 = var1[var6].light;
         var5 = var7.x() == var0.x && var7.y() == var0.y && var7.z() == var0.z ? var1[var6] : null;
      }

      boolean var12 = var5 != null ? var5.inBuilding : MapSources.inBuilding(var4, var0.x, var0.y, var0.z);
      boolean var13 = var5 != null ? var5.noPower : MapSources.noPower(var4, var0.x, var0.y);
      boolean var8 = var5 != null ? var5.outdoor : var0.z >= 0 && var4.getRoomAt(var0.x, var0.y, var0.z) == null;
      IsoLightSwitch var9 = (IsoLightSwitch)var0.switches.get(0);
      SourceRegister.Lamp var10 = new SourceRegister.Lamp(
              var0.x,
              var0.y,
              var0.z,
              Math.min(var0.radius, 20),
              LightSources.engine(var0.r),
              LightSources.engine(var0.g),
              LightSources.engine(var0.b),
              LightSources.building(var0)
      );
      int var11 = channel(var0.r) | channel(var0.g) << 8 | channel(var0.b) << 16 | Math.min(var0.radius, 63) << 24 | (var9.streetLight ? 1073741824 : 0);
      return new LightMemory.Lamp(var10, var11, var9.streetLight, var9.isActivated(), ownPower(var0, var9, var2, var3, var12, var13), var12, var13, var8);
   }

   private static boolean ownPower(IsoLightSource var0, IsoLightSwitch var1, boolean var2, boolean var3, boolean var4, boolean var5) {
      return var0.active && !MapSources.gridPowered(var2, var3, var1.streetLight, var4, var5);
   }

   private static int channel(float var0) {
      return Math.max(0, Math.min(255, Math.round(var0 * 255.0F)));
   }

   private static void rooms(IsoCell var0) {
      if (!IsoWorld.instance.isHydroPowerOn()) {
         ArrayList var1 = var0.roomLights;

         for (int var2 = 0; var2 < var1.size(); var2++) {
            IsoRoomLight var3 = (IsoRoomLight)var1.get(var2);
            if (var3.room != null && var3.room.def != null && var3.room.def.lightsActive) {
               rememberRoom(var3.room.def.getID(), var3.active, Caches.tick());
            }
         }
      }
   }

   private static LightMemory.Cell cell(int var0, int var1) {
      long var2 = key(Math.floorDiv(var0, 32), Math.floorDiv(var1, 32));
      LightMemory.Cell var4 = cells.get(var2);
      if (var4 == null) {
         var4 = new LightMemory.Cell();
         cells.put(var2, var4);
      }

      return var4;
   }

   private static long key(int var0, int var1) {
      return (long)var0 << 32 | var1 & 4294967295L;
   }

   private LightMemory() {
   }

   static {
      Caches.register(new Caches.Cache() {
         @Override
         public long bytes() {
            return LightMemory.chunks.size() * 64L + LightMemory.lampCount * 160L + LightMemory.roomPower.size() * 48L;
         }

         @Override
         public void list(Caches.Survey var1) {
            for (LightMemory.Chunk var3 : LightMemory.chunks.values()) {
               var1.add(var3.used, 64L + var3.lamps.length * 160L);
            }

            for (LightMemory.Room var5 : LightMemory.roomPower.values()) {
               var1.add(var5.used, 48L);
            }
         }

         @Override
         public void evictBefore(long var1) {
            LightMemory.evict(var1);
         }
      });
   }

   private static final class Cell {
      int chunks;
      LightMemory.Lamp[] outdoor = LightMemory.NONE;
      boolean stale;
   }

   private static final class Chunk {
      LightMemory.Lamp[] lamps = LightMemory.NONE;
      long used;
   }

   record Lamp(SourceRegister.Lamp light, int tile, boolean street, boolean switchedOn, boolean ownPower, boolean inBuilding, boolean noPower, boolean outdoor) {
      boolean on(boolean var1, boolean var2) {
         return this.switchedOn && (this.ownPower || MapSources.gridPowered(var1, var2, this.street, this.inBuilding, this.noPower));
      }
   }

   private static final class Room {
      boolean ownPower;
      long used;
   }
}