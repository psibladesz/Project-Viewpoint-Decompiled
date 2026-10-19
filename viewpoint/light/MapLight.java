package viewpoint.light;

import java.util.ArrayList;
import java.util.List;
import viewpoint.platform.LongMap;
import zombie.iso.IsoCell;
import zombie.iso.IsoWorld;
import zombie.iso.RoomDef;

public final class MapLight {
   public static final int MARGIN = 37;
   private static final LongMap<List<LightLayouts.Level>> blocks = new LongMap<>(128);
   private static int generation;
   private static boolean band;

   public static int generation() {
      return generation;
   }

   public static void install(MapLayout.Block var0, ArrayList<RoomDef> var1) {
      long var2 = key(var0.bx, var0.by);
      List<LightLayouts.Level> var4 = blocks.get(var2);
      if (var4 != null) {
         for (LightLayouts.Level var6 : var4) {
            if (!has(var0.levels, var6)) {
               LightLayouts.unmapped(var6.chunkX, var6.chunkY, var6.level);
            }
         }
      }

      IsoCell var8 = IsoWorld.instance.getCell();

      for (LightLayouts.Level var7 : var0.levels) {
         LightLayouts.mapped(var8, var7);
      }

      blocks.put(var2, var0.levels);
      MapSources.add(var0.bx, var0.by, var0.lamps, var1);
   }

   public static void drop(int var0, int var1) {
      List<LightLayouts.Level> var2 = blocks.remove(key(var0, var1));
      if (var2 != null) {
         for (LightLayouts.Level var4 : var2) {
            LightLayouts.unmapped(var4.chunkX, var4.chunkY, var4.level);
         }

         MapSources.drop(var0, var1);
      }
   }

   public static void update(IsoCell var0) {
      band = true;
      MapSources.update(var0);
   }

   public static void off() {
      band = false;
   }

   static boolean band() {
      return band;
   }

   static void clear() {
      blocks.clear();
      MapSources.clear();
      generation++;
   }

   private static boolean has(List<LightLayouts.Level> var0, LightLayouts.Level var1) {
      for (LightLayouts.Level var3 : var0) {
         if (var3.chunkX == var1.chunkX && var3.chunkY == var1.chunkY && var3.level == var1.level) {
            return true;
         }
      }

      return false;
   }

   private static long key(int var0, int var1) {
      return (long)var0 << 32 | var1 & 4294967295L;
   }

   private MapLight() {
   }
}
