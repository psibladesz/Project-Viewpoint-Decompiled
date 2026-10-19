package viewpoint.light;

import java.util.ArrayList;
import viewpoint.platform.LongMap;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;

final class MapCheck {
   private static final int REPORTED = 40;
   private static final long CHECK_NANOS = 5000000000L;
   private static long levels;
   private static long squares;
   private static long sameExists;
   private static long sameFlags;
   private static long samePasses;
   private static long sameRooms;
   private static int reported;
   private static int gameLamps;
   private static int foundLamps;
   private static int sameLamps;
   private static int sameLampsOn;
   private static int gameRooms;
   private static int foundRooms;
   private static int sameRoomsOn;
   private static long lastSources;
   private static final LongMap<SourceRegister.Lamp> mapLamps = new LongMap<>(512);
   private static final LongMap<Boolean> mapLampsOn = new LongMap<>(512);
   private static final LongMap<Boolean> mapRoomsOn = new LongMap<>(512);
   private static final ArrayList<SourceRegister.Entry> entries = new ArrayList<>();

   static void layout(LightLayouts.Level var0, LightLayouts.Level var1) {
      if (var0 != null && var1 != null) {
         levels++;

         for (int var2 = 0; var2 < 64; var2++) {
            boolean var3 = (var0.flags[var2] & 1) != 0;
            boolean var4 = (var1.flags[var2] & 1) != 0;
            if (var3 || var4) {
               squares++;
               boolean var5 = var3 == var4;
               boolean var6 = var0.flags[var2] == var1.flags[var2];
               boolean var7 = var0.passes[var2] == var1.passes[var2];
               boolean var8 = var0.rooms[var2] == var1.rooms[var2];
               sameExists += var5 ? 1L : 0L;
               sameFlags += var6 ? 1L : 0L;
               samePasses += var7 ? 1L : 0L;
               sameRooms += var8 ? 1L : 0L;
               if ((!var5 || !var6 || !var7 || !var8) && reported < 40) {
                  reported++;
                  System.out
                     .println(
                        "[Viewpoint] map layout differs at "
                           + (var0.chunkX * 8 + var2 % 8)
                           + ","
                           + (var0.chunkY * 8 + var2 / 8)
                           + ","
                           + var0.level
                           + ": flags map "
                           + var0.flags[var2]
                           + " game "
                           + var1.flags[var2]
                           + ", passes map "
                           + passes(var0.passes[var2])
                           + " game "
                           + passes(var1.passes[var2])
                           + ", room map "
                           + var0.rooms[var2]
                           + " game "
                           + var1.rooms[var2]
                     );
               }
            }
         }
      }
   }

   static void sources(IsoCell var0) {
      long var1 = System.nanoTime();
      if (var1 - lastSources >= 5000000000L) {
         lastSources = var1;
         mapLamps.clear();
         mapLampsOn.clear();
         mapRoomsOn.clear();
         MapSources.lamps(mapLamps, mapLampsOn);
         MapSources.rooms(mapRoomsOn);
         entries.clear();
         SourceRegister var3 = LightSources.register();
         var3.engineLamps(entries);
         sameLampsOn = 0;
         sameLamps = 0;
         foundLamps = 0;
         gameLamps = 0;

         for (SourceRegister.Entry var5 : entries) {
            SourceRegister.Lamp var6 = var5.lamp;
            IsoChunk var7 = var0.getChunkForGridSquare(var6.x(), var6.y(), var6.z());
            if (var7 != null && var6.z() >= 0) {
               gameLamps++;
               long var8 = MapSources.lampKey(var6.x(), var6.y(), var6.z());
               SourceRegister.Lamp var10 = mapLamps.get(var8);
               if (var10 != null) {
                  foundLamps++;
                  sameLamps = sameLamps
                     + (var10.radius() == var6.radius() && same(var10.r(), var6.r()) && same(var10.g(), var6.g()) && same(var10.b(), var6.b()) ? 1 : 0);
                  sameLampsOn = sameLampsOn + (mapLampsOn.get(var8) == var5.on ? 1 : 0);
               }
            }
         }

         rooms(var3);
      }
   }

   private static void rooms(SourceRegister var0) {
      LongMap var1 = new LongMap(256);
      var0.engineRooms(var1);
      entries.clear();
      var0.on(entries);
      LongMap var2 = new LongMap(256);

      for (SourceRegister.Entry var4 : entries) {
         if (!var4.map && var4.room != null) {
            var2.put(var4.room.room(), Boolean.TRUE);
         }
      }

      sameRoomsOn = 0;
      foundRooms = 0;
      gameRooms = 0;

      for (int var7 = 0; var7 < var1.capacity(); var7++) {
         if (var1.valueAt(var7) != null) {
            long var8 = var1.keyAt(var7);
            Boolean var6 = mapRoomsOn.get(var8);
            gameRooms++;
            if (var6 != null) {
               foundRooms++;
               sameRoomsOn = sameRoomsOn + (var6 == (var2.get(var8) != null) ? 1 : 0);
            }
         }
      }
   }

   static String report() {
      return "map layouts "
         + levels
         + " levels (exist "
         + share(sameExists)
         + ", flags "
         + share(sameFlags)
         + ", passes "
         + share(samePasses)
         + ", rooms "
         + share(sameRooms)
         + "), map lamps "
         + foundLamps
         + "/"
         + gameLamps
         + " (light "
         + sameLamps
         + ", on "
         + sameLampsOn
         + "), map rooms "
         + foundRooms
         + "/"
         + gameRooms
         + " (on "
         + sameRoomsOn
         + ")";
   }

   static void clear() {
      sameRooms = 0L;
      samePasses = 0L;
      sameFlags = 0L;
      sameExists = 0L;
      squares = 0L;
      levels = 0L;
      sameRoomsOn = 0;
      foundRooms = 0;
      gameRooms = 0;
      sameLampsOn = 0;
      sameLamps = 0;
      foundLamps = 0;
      gameLamps = 0;
   }

   private static String share(long var0) {
      return squares == 0L ? "-" : String.format("%.1f%%", 100.0 * var0 / squares);
   }

   private static boolean same(float var0, float var1) {
      return Math.abs(var0 - var1) < 0.01F;
   }

   private static String passes(int var0) {
      StringBuilder var1 = new StringBuilder(10);

      for (int var2 = 0; var2 < 10; var2++) {
         var1.append(LightLayout.passIn(var0, var2));
      }

      return var1.toString();
   }

   private MapCheck() {
   }
}
