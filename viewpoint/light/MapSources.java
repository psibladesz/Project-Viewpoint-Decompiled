package viewpoint.light;

import java.util.ArrayList;
import java.util.Locale;
import viewpoint.platform.LongMap;
import zombie.GameTime;
import zombie.SandboxOptions;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoChunkMap;
import zombie.iso.IsoMetaGrid;
import zombie.iso.IsoWorld;
import zombie.iso.MetaObject;
import zombie.iso.RoomDef;
import zombie.iso.RoomDef.RoomRect;
import zombie.iso.zones.Zone;

final class MapSources {
   private static final long SAME_SQUARE = 72057594037927936L;
   private static final long REMEMBERED = Long.MIN_VALUE;
   private static final int BLOCK_CHUNKS = 8;
   private static final int LIGHT_SWITCH = 7;
   private static final int LISTED_MARGIN = 25;
   private static final LongMap<MapSources.Block> blocks = new LongMap<>(128);
   private static final LongMap<Boolean> listedRooms = new LongMap<>(256);
   private static final ArrayList<Zone> zones = new ArrayList<>();
   private static boolean grid;
   private static boolean lampGrid;
   private static boolean night;
   private static int readings = -1;
   private static int evictions;
   private static LongMap<Boolean> spare = new LongMap<>(8);

   static void add(int var0, int var1, int[] var2, ArrayList<RoomDef> var3) {
      drop(var0, var1);
      IsoMetaGrid var4 = IsoWorld.instance.getMetaGrid();
      MapSources.Lamp[] var5 = new MapSources.Lamp[var2.length / 4];

      for (int var6 = 0; var6 < var5.length; var6++) {
         var5[var6] = lamp(var4, var2, var6 * 4, var5, var6);
      }

      ArrayList<MapSources.Room> var9 = new ArrayList<>();

      for (RoomDef var8 : var3) {
         rooms(var4, var8, var9);
      }

      MapSources.Block var10 = new MapSources.Block(var0, var1, var5, var9.toArray(new MapSources.Room[0]));
      blocks.put(key(var0, var1), var10);
      IsoCell var11 = IsoWorld.instance.getCell();
      if (var11 != null) {
         decide(var10, var11, window(var11));
      }
   }

   static void drop(int var0, int var1) {
      MapSources.Block var2 = blocks.remove(key(var0, var1));
      if (var2 != null) {
         SourceRegister var3 = LightSources.register();

         for (MapSources.Lamp var7 : var2.lamps) {
            var3.unmap(var7.key, true, LightJobs::mark);
         }

         for (MapSources.Room var12 : var2.rooms) {
            var3.unmap(var12.key, false, LightJobs::mark);
         }

         for (int var9 = 0; var9 < var2.remembered.capacity(); var9++) {
            if (var2.remembered.valueAt(var9) != null) {
               var3.unmap(var2.remembered.keyAt(var9), true, LightJobs::mark);
            }
         }
      }
   }

   static void update(IsoCell var0) {
      boolean var1 = IsoWorld.instance.isHydroPowerOn();
      boolean var2 = SandboxOptions.instance.doesPowerGridExist();
      boolean var3 = GameTime.getInstance().getNight() >= 0.5F;
      int var4 = LightSources.readings();
      int var5 = LightMemory.evictions();
      boolean var6 = var1 != grid || var2 != lampGrid || var3 != night || var5 != evictions;
      if (var6 || var4 != readings) {
         grid = var1;
         lampGrid = var2;
         night = var3;
         readings = var4;
         evictions = var5;
         listedRooms.clear();
         LightSources.register().engineRooms(listedRooms);
         int[] var7 = window(var0);

         for (int var8 = 0; var8 < blocks.capacity(); var8++) {
            MapSources.Block var9 = blocks.valueAt(var8);
            if (var9 != null && (var6 || nearWindow(blocks.keyAt(var8), var7))) {
               decide(var9, var0, var7);
            }
         }

         LightSources.publish();
         MapCheck.sources(var0);
      }
   }

   static void clear() {
      blocks.clear();
      readings = -1;
   }

   private static boolean powered(MapSources.Lamp var0) {
      return gridPowered(lampGrid, night, var0.street, var0.inBuilding, var0.noPower);
   }

   static boolean gridPowered(boolean var0, boolean var1, boolean var2, boolean var3, boolean var4) {
      return var0 && !var4 && (var2 ? var1 : var3);
   }

   static void lamps(LongMap<SourceRegister.Lamp> var0, LongMap<Boolean> var1) {
      for (MapSources.Block var3 : blocks.values()) {
         for (MapSources.Lamp var7 : var3.lamps) {
            var0.put(var7.key, var7.light);
            var1.put(var7.key, powered(var7));
         }
      }
   }

   static void rooms(LongMap<Boolean> var0) {
      for (MapSources.Block var2 : blocks.values()) {
         for (MapSources.Room var6 : var2.rooms) {
            var0.put(var6.def.id, grid && !var6.noPower && var6.def.lightsActive);
         }
      }
   }

   static long lampKey(int var0, int var1, int var2) {
      return (long)(var0 & 16777215) << 32 | (long)(var1 & 16777215) << 8 | var2 + 32 & 0xFF;
   }

   private static void decide(MapSources.Block var0, IsoCell var1, int[] var2) {
      SourceRegister var3 = LightSources.register();

      for (MapSources.Lamp var7 : var0.lamps) {
         SourceRegister.Lamp var8 = var7.light;
         boolean var9 = within(var8.x(), var8.y(), var2, 0) && loaded(var1, var8.x(), var8.y(), var8.z());
         boolean var10 = LightMemory.seen(Math.floorDiv(var8.x(), 8), Math.floorDiv(var8.y(), 8));
         var3.mapLamp(var7.key, var8, !var9 && !var10 && powered(var7), LightJobs::mark);
      }

      remembered(var0, var1, var2, var3);

      for (MapSources.Room var14 : var0.rooms) {
         boolean var15 = grid && !var14.noPower || LightMemory.ownPower(var14.def.id);
         boolean var16 = listedRooms.get(var14.def.id) == null && var15 && var14.def.lightsActive;
         var3.mapRoom(var14.key, var14.light, var16, LightJobs::mark);
      }
   }

   private static void remembered(MapSources.Block var0, IsoCell var1, int[] var2, SourceRegister var3) {
      LongMap var4 = spare;
      var4.clear();

      for (int var5 = var0.by * 8; var5 < (var0.by + 1) * 8; var5++) {
         for (int var6 = var0.bx * 8; var6 < (var0.bx + 1) * 8; var6++) {
            LightMemory.Lamp[] var7 = LightMemory.lamps(var6, var5);
            if (var7 != null && var7.length != 0 && (!within(var6 * 8, var5 * 8, var2, 0) || !loaded(var1, var6 * 8, var5 * 8, 0))) {
               long var8 = 0L;

               for (int var10 = 0; var10 < var7.length; var10++) {
                  SourceRegister.Lamp var11 = var7[var10].light();
                  var8 = var10 > 0 && sameSquare(var7[var10 - 1].light(), var11)
                     ? var8 + 72057594037927936L
                     : Long.MIN_VALUE | lampKey(var11.x(), var11.y(), var11.z());
                  var3.mapLamp(var8, var11, var7[var10].on(lampGrid, night), LightJobs::mark);
                  var4.put(var8, Boolean.TRUE);
               }
            }
         }
      }

      for (int var12 = 0; var12 < var0.remembered.capacity(); var12++) {
         long var13 = var0.remembered.keyAt(var12);
         if (var0.remembered.valueAt(var12) != null && var4.get(var13) == null) {
            var3.unmap(var13, true, LightJobs::mark);
         }
      }

      spare = var0.remembered;
      var0.remembered = var4;
   }

   private static boolean sameSquare(SourceRegister.Lamp var0, SourceRegister.Lamp var1) {
      return var0.x() == var1.x() && var0.y() == var1.y() && var0.z() == var1.z();
   }

   private static MapSources.Lamp lamp(IsoMetaGrid var0, int[] var1, int var2, MapSources.Lamp[] var3, int var4) {
      int var5 = var1[var2];
      int var6 = var1[var2 + 1];
      int var7 = var1[var2 + 2];
      int var8 = var1[var2 + 3];
      long var9 = lampKey(var5, var6, var7);

      for (int var11 = 0; var11 < var4; var11++) {
         var9 = var3[var11].key == var9 ? var9 + 72057594037927936L : var9;
      }

      int var13 = Math.min(var8 >>> 24 & 63, 20);
      SourceRegister.Lamp var12 = new SourceRegister.Lamp(var5, var6, var7, var13, doubled(var8), doubled(var8 >> 8), doubled(var8 >> 16), -1L);
      return new MapSources.Lamp(var9, var12, (var8 & 1073741824) != 0, inBuilding(var0, var5, var6, var7), noPower(var0, var5, var6));
   }

   private static float doubled(int var0) {
      return Math.min(1.0F, (var0 & 0xFF) / 255.0F * 2.0F);
   }

   static boolean inBuilding(IsoMetaGrid var0, int var1, int var2, int var3) {
      for (int var4 = 0; var4 >= -1 && var3 + var4 >= 0; var4--) {
         for (int var5 = -1; var5 <= 1; var5++) {
            for (int var6 = -1; var6 <= 1; var6++) {
               if (var0.getRoomAt(var1 + var6, var2 + var5, var3 + var4) != null || var0.getEmptyOutsideAt(var1 + var6, var2 + var5, var3 + var4) != null) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static void rooms(IsoMetaGrid var0, RoomDef var1, ArrayList<MapSources.Room> var2) {
      if (hasLights(var1)) {
         boolean var3 = noPower(var0, var1);
         long var4 = var1.building == null ? -1L : var1.building.getID();

         for (int var6 = 0; var6 < var1.rects.size() && var6 < 256; var6++) {
            RoomRect var7 = (RoomRect)var1.rects.get(var6);
            SourceRegister.RoomLight var8 = new SourceRegister.RoomLight(var7.x, var7.y, var1.level, var7.w, var7.h, var1.id, var4);
            var2.add(new MapSources.Room(var1.id & -4294967296L | (var1.id & 16777215L) << 8 | var6, var8, var1, var3));
         }
      }
   }

   static boolean hasLights(RoomDef var0) {
      for (int var1 = 0; var1 < var0.objects.size(); var1++) {
         if (((MetaObject)var0.objects.get(var1)).getType() == 7) {
            return !var0.rects.isEmpty();
         }
      }

      return false;
   }

   static boolean noPower(IsoMetaGrid var0, RoomDef var1) {
      RoomRect var2 = (RoomRect)var1.rects.get(0);
      return var1.name != null && var1.name.toLowerCase(Locale.ROOT).contains("derelict") || noPower(var0, var2.x, var2.y);
   }

   static boolean noPower(IsoMetaGrid var0, int var1, int var2) {
      zones.clear();
      var0.getZonesAt(var1, var2, 0, zones);

      for (Zone var4 : zones) {
         if ("NoPower".equals(var4.type) || "NoPowerOrWater".equals(var4.type)) {
            return true;
         }
      }

      return false;
   }

   private static boolean loaded(IsoCell var0, int var1, int var2, int var3) {
      IsoChunk var4 = var0.getChunkForGridSquare(var1, var2, var3);
      return var4 != null && var4.loaded;
   }

   private static int[] window(IsoCell var0) {
      IsoChunkMap var1 = var0.getChunkMap(0);
      return var1 == null ? null : new int[]{var1.getWorldXMinTiles(), var1.getWorldYMinTiles(), var1.getWorldXMaxTiles(), var1.getWorldYMaxTiles()};
   }

   private static boolean within(int var0, int var1, int[] var2, int var3) {
      return var2 != null && var0 >= var2[0] - var3 && var1 >= var2[1] - var3 && var0 < var2[2] + var3 && var1 < var2[3] + var3;
   }

   private static boolean nearWindow(long var0, int[] var2) {
      int var3 = (int)(var0 >> 32);
      int var4 = (int)var0;
      int var5 = var3 * 64;
      int var6 = var4 * 64;
      return var2 != null && var5 + 64 + 25 > var2[0] && var6 + 64 + 25 > var2[1] && var5 - 25 < var2[2] && var6 - 25 < var2[3];
   }

   private static long key(int var0, int var1) {
      return (long)var0 << 32 | var1 & 4294967295L;
   }

   private MapSources() {
   }

   private static final class Block {
      final int bx;
      final int by;
      final MapSources.Lamp[] lamps;
      final MapSources.Room[] rooms;
      LongMap<Boolean> remembered = new LongMap<>(8);

      Block(int var1, int var2, MapSources.Lamp[] var3, MapSources.Room[] var4) {
         this.bx = var1;
         this.by = var2;
         this.lamps = var3;
         this.rooms = var4;
      }
   }

   private record Lamp(long key, SourceRegister.Lamp light, boolean street, boolean inBuilding, boolean noPower) {
   }

   private record Room(long key, SourceRegister.RoomLight light, RoomDef def, boolean noPower) {
   }
}
