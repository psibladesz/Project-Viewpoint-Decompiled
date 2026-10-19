package viewpoint.light;

import java.util.ArrayList;
import java.util.List;
import viewpoint.platform.LongMap;

final class SourceRegister {
   static final int GRID_BORDER = 2;
   private final LongMap<SourceRegister.Entry> lamps = new LongMap<>(512);
   private final LongMap<SourceRegister.Entry> rooms = new LongMap<>(2048);
   private final LongMap<SourceRegister.Entry> mapLamps = new LongMap<>(512);
   private final LongMap<SourceRegister.Entry> mapRooms = new LongMap<>(2048);
   private final LongMap<ArrayList<SourceRegister.Entry>> byChunk = new LongMap<>(1024);
   private long update;
   private LightSources.Marks marks;
   private int lampsOn;
   private int roomsOn;
   private int mapLampsOn;
   private int mapRoomsOn;
   private int changes;
   private int marked;
   private long lightBytes;

   void begin(LightSources.Marks var1) {
      this.marks = var1;
      this.update++;
   }

   void lamp(int var1, int var2, int var3, int var4, int var5, float var6, float var7, float var8, long var9, boolean var11) {
      SourceRegister.Entry var12 = this.lamps.get(var1);
      int var13 = Math.min(var5, 20);
      if (var12 != null && var12.on == var11 && samePlace(var12.lamp, var2, var3, var4, var9)) {
         SourceRegister.Lamp var14 = var12.lamp;
         if (var13 <= var14.radius() && var6 <= var14.r() && var7 <= var14.g() && var8 <= var14.b()) {
            var12.seen = this.update;
            return;
         }

         var13 = Math.max(var13, var14.radius());
         var6 = Math.max(var6, var14.r());
         var7 = Math.max(var7, var14.g());
         var8 = Math.max(var8, var14.b());
      }

      var12 = this.replace(this.lamps, var1, var12, var11);
      var12.lamp = new SourceRegister.Lamp(var2, var3, var4, var13, var6, var7, var8, var9);
      int var16 = LightModel.levelsReached(var13);
      this.reach(var12, var2 - var13, var3 - var13, var2 + var13, var3 + var13, var4 - var16, var4 + var16);
      this.lampsOn += var11 ? 1 : 0;
   }

   void fixture(int var1, LampFixture var2) {
      SourceRegister.Entry var3 = this.lamps.get(var1);
      if (var3 != null) {
         var3.fixture = var2;
      }
   }

   void room(int var1, int var2, int var3, int var4, int var5, int var6, long var7, long var9, boolean var11) {
      SourceRegister.Entry var12 = this.rooms.get(var1);
      if (var12 != null && var12.on == var11 && same(var12.room, var2, var3, var4, var5, var6, var7, var9)) {
         var12.seen = this.update;
      } else {
         var12 = this.replace(this.rooms, var1, var12, var11);
         var12.room = new SourceRegister.RoomLight(var2, var3, var4, var5, var6, var7, var9);
         byte var13 = 7;
         int var14 = LightModel.levelsReached(var13);
         this.reach(var12, var2 - var13, var3 - var13, var2 + var5 - 1 + var13, var3 + var6 - 1 + var13, var4 - var14, var4 + var14);
         this.roomsOn += var11 ? 1 : 0;
      }
   }

   void finish() {
      this.lamps.removeIf(this::gone);
      this.rooms.removeIf(this::gone);
      this.marks = null;
   }

   void mapLamp(long var1, SourceRegister.Lamp var3, boolean var4, LightSources.Marks var5) {
      SourceRegister.Entry var6 = this.mapLamps.get(var1);
      if (var6 == null || var6.on != var4 || !var6.lamp.equals(var3)) {
         this.marks = var5;
         var6 = this.replace(this.mapLamps, var1, var6, var4);
         var6.map = true;
         var6.lamp = var3;
         int var7 = LightModel.levelsReached(var3.radius());
         this.reach(
            var6, var3.x() - var3.radius(), var3.y() - var3.radius(), var3.x() + var3.radius(), var3.y() + var3.radius(), var3.z() - var7, var3.z() + var7
         );
         this.mapLampsOn += var4 ? 1 : 0;
         this.marks = null;
      }
   }

   void mapRoom(long var1, SourceRegister.RoomLight var3, boolean var4, LightSources.Marks var5) {
      SourceRegister.Entry var6 = this.mapRooms.get(var1);
      if (var6 == null || var6.on != var4 || !var6.room.equals(var3)) {
         this.marks = var5;
         var6 = this.replace(this.mapRooms, var1, var6, var4);
         var6.map = true;
         var6.room = var3;
         byte var7 = 7;
         int var8 = LightModel.levelsReached(var7);
         this.reach(
            var6, var3.x() - var7, var3.y() - var7, var3.x() + var3.width() - 1 + var7, var3.y() + var3.height() - 1 + var7, var3.z() - var8, var3.z() + var8
         );
         this.mapRoomsOn += var4 ? 1 : 0;
         this.marks = null;
      }
   }

   void unmap(long var1, boolean var3, LightSources.Marks var4) {
      SourceRegister.Entry var5 = (var3 ? this.mapLamps : this.mapRooms).remove(var1);
      if (var5 != null) {
         this.marks = var4;
         this.leave(var5);
         this.changes++;
         this.marks = null;
      }
   }

   void engineRooms(LongMap<Boolean> var1) {
      for (SourceRegister.Entry var3 : this.rooms.values()) {
         var1.put(var3.room.room(), Boolean.TRUE);
      }
   }

   void engineLamps(List<SourceRegister.Entry> var1) {
      for (SourceRegister.Entry var3 : this.lamps.values()) {
         var1.add(var3);
      }
   }

   SourceRegister.Reaching reaching(int var1, int var2, int var3) {
      ArrayList var4 = new ArrayList();
      ArrayList var5 = new ArrayList();
      ArrayList<SourceRegister.Entry> var6 = this.byChunk.get(key(var1, var2));
      if (var6 != null) {
         for (SourceRegister.Entry var8 : var6) {
            if (var8.on && var3 >= var8.minLevel && var3 <= var8.maxLevel) {
               if (var8.lamp != null) {
                  var4.add(var8.lamp);
               } else {
                  var5.add(var8.room);
               }
            }
         }
      }

      return new SourceRegister.Reaching(List.copyOf(var4), List.copyOf(var5));
   }

   boolean lights(int var1, int var2, int var3, List<SourceLight> var4) {
      ArrayList<SourceRegister.Entry> var5 = this.byChunk.get(key(var1, var2));
      if (var5 == null) {
         return true;
      }

      for (SourceRegister.Entry var7 : var5) {
         if (var7.on && var3 >= var7.minLevel && var3 <= var7.maxLevel) {
            if (var7.light == null) {
               return false;
            }

            var4.add(var7.light);
         }
      }

      return true;
   }

   void on(List<SourceRegister.Entry> var1) {
      on(this.lamps, var1);
      on(this.rooms, var1);
      on(this.mapLamps, var1);
      on(this.mapRooms, var1);
   }

   private static void on(LongMap<SourceRegister.Entry> var0, List<SourceRegister.Entry> var1) {
      for (int var2 = 0; var2 < var0.capacity(); var2++) {
         SourceRegister.Entry var3 = (SourceRegister.Entry)var0.valueAt(var2);
         if (var3 != null && var3.on) {
            var1.add(var3);
         }
      }
   }

   void due(List<SourceRegister.Entry> var1) {
      due(this.lamps, var1);
      due(this.rooms, var1);
      due(this.mapLamps, var1);
      due(this.mapRooms, var1);
   }

   private static void due(LongMap<SourceRegister.Entry> var0, List<SourceRegister.Entry> var1) {
      for (SourceRegister.Entry var3 : var0.values()) {
         if (var3.due && !var3.pending) {
            var1.add(var3);
         }
      }
   }

   void worked(SourceRegister.Entry var1, int var2, int var3, SourceLight var4, LightSources.Marks var5) {
      var1.pending = false;
      if (var1.on && var1.sourceVersion == var3 && (var1.version == var2 || var1.light == null)) {
         SourceLight var6 = var1.light;
         this.lightBytes = this.lightBytes + (var4.bytes() - (var6 == null ? 0L : var6.bytes()));
         var1.light = var4;
         var1.due = var1.version != var2;
         if (var6 != null && !var6.same(var4)) {
            markReach(var1, var5);
         }
      }
   }

   void relayout(int var1, int var2, int var3) {
      ArrayList<SourceRegister.Entry> var4 = this.byChunk.get(key(var1, var2));
      if (var4 != null) {
         for (SourceRegister.Entry var6 : var4) {
            if (var6.on && var3 >= var6.minLevel && var3 <= var6.maxLevel) {
               var6.due = true;
               var6.version++;
            }
         }
      }
   }

   long lightBytes() {
      return this.lightBytes;
   }

   void clear() {
      this.lamps.clear();
      this.rooms.clear();
      this.mapLamps.clear();
      this.mapRooms.clear();
      this.byChunk.clear();
      this.lampsOn = this.roomsOn = this.mapLampsOn = this.mapRoomsOn = this.changes = this.marked = 0;
      this.lightBytes = 0L;
   }

   int lamps() {
      return this.lamps.size();
   }

   int rooms() {
      return this.rooms.size();
   }

   int lampsOn() {
      return this.lampsOn;
   }

   int roomsOn() {
      return this.roomsOn;
   }

   int mapLamps() {
      return this.mapLamps.size();
   }

   int mapRooms() {
      return this.mapRooms.size();
   }

   int mapLampsOn() {
      return this.mapLampsOn;
   }

   int mapRoomsOn() {
      return this.mapRoomsOn;
   }

   int takeChanges() {
      int var1 = this.changes;
      this.changes = 0;
      return var1;
   }

   int takeMarked() {
      int var1 = this.marked;
      this.marked = 0;
      return var1;
   }

   private static boolean samePlace(SourceRegister.Lamp var0, int var1, int var2, int var3, long var4) {
      return var0.x() == var1 && var0.y() == var2 && var0.z() == var3 && var0.building() == var4;
   }

   private static boolean same(SourceRegister.RoomLight var0, int var1, int var2, int var3, int var4, int var5, long var6, long var8) {
      return var0.x() == var1
         && var0.y() == var2
         && var0.z() == var3
         && var0.width() == var4
         && var0.height() == var5
         && var0.room() == var6
         && var0.building() == var8;
   }

   private SourceRegister.Entry replace(LongMap<SourceRegister.Entry> var1, long var2, SourceRegister.Entry var4, boolean var5) {
      if (var4 == null) {
         var4 = new SourceRegister.Entry();
         var1.put(var2, var4);
      } else {
         this.leave(var4);
      }

      var4.on = var5;
      var4.due = var5;
      var4.seen = this.update;
      this.changes++;
      return var4;
   }

   private void reach(SourceRegister.Entry var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      var1.minX = Math.floorDiv(var2 - 2, 8);
      var1.minY = Math.floorDiv(var3 - 2, 8);
      var1.maxX = Math.floorDiv(var4 + 2, 8);
      var1.maxY = Math.floorDiv(var5 + 2, 8);
      var1.minLevel = var6;
      var1.maxLevel = var7;

      for (int var8 = var1.minY; var8 <= var1.maxY; var8++) {
         for (int var9 = var1.minX; var9 <= var1.maxX; var9++) {
            ArrayList var10 = this.byChunk.get(key(var9, var8));
            if (var10 == null) {
               var10 = new ArrayList(4);
               this.byChunk.put(key(var9, var8), var10);
            }

            var10.add(var1);
         }
      }

      this.mark(var1);
   }

   private void leave(SourceRegister.Entry var1) {
      this.mark(var1);
      this.letGoLight(var1);
      if (var1.on && var1.lamp != null) {
         if (var1.map) {
            this.mapLampsOn--;
         } else {
            this.lampsOn--;
         }
      } else if (var1.on) {
         if (var1.map) {
            this.mapRoomsOn--;
         } else {
            this.roomsOn--;
         }
      }

      for (int var2 = var1.minY; var2 <= var1.maxY; var2++) {
         for (int var3 = var1.minX; var3 <= var1.maxX; var3++) {
            ArrayList var4 = this.byChunk.get(key(var3, var2));
            if (var4 != null) {
               var4.remove(var1);
               if (var4.isEmpty()) {
                  this.byChunk.remove(key(var3, var2));
               }
            }
         }
      }
   }

   private boolean gone(SourceRegister.Entry var1) {
      if (var1.seen == this.update) {
         return false;
      }

      this.leave(var1);
      this.changes++;
      return true;
   }

   private void mark(SourceRegister.Entry var1) {
      if (var1.on && this.marks != null) {
         this.marked = this.marked + markReach(var1, this.marks);
      }
   }

   private static int markReach(SourceRegister.Entry var0, LightSources.Marks var1) {
      for (int var2 = var0.minLevel; var2 <= var0.maxLevel; var2++) {
         for (int var3 = var0.minY; var3 <= var0.maxY; var3++) {
            for (int var4 = var0.minX; var4 <= var0.maxX; var4++) {
               var1.mark(var4, var3, var2);
            }
         }
      }

      return (var0.maxLevel - var0.minLevel + 1) * (var0.maxY - var0.minY + 1) * (var0.maxX - var0.minX + 1);
   }

   private void letGoLight(SourceRegister.Entry var1) {
      this.lightBytes = this.lightBytes - (var1.light == null ? 0L : var1.light.bytes());
      var1.light = null;
      var1.version++;
      var1.sourceVersion++;
   }

   private static long key(int var0, int var1) {
      return (long)var0 << 32 | var1 & 4294967295L;
   }

   static final class Entry {
      SourceRegister.Lamp lamp;
      LampFixture fixture = LampFixture.MIDDLE;
      SourceRegister.RoomLight room;
      boolean map;
      boolean on;
      long seen;
      int minX;
      int minY;
      int maxX;
      int maxY;
      int minLevel;
      int maxLevel;
      SourceLight light;
      boolean due;
      boolean pending;
      int version;
      int sourceVersion;
   }

   record Lamp(int x, int y, int z, int radius, float r, float g, float b, long building) {
   }

   record Reaching(List<SourceRegister.Lamp> lamps, List<SourceRegister.RoomLight> rooms) {
   }

   record RoomLight(int x, int y, int z, int width, int height, long room, long building) {
   }
}
