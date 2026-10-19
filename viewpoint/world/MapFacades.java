package viewpoint.world;

import java.util.HashMap;
import zombie.iso.BuildingDef;
import zombie.iso.RoomDef;

public final class MapFacades {
   public static final int SLOTS = 6;
   public static final int ALL = Integer.MIN_VALUE;
   static final long NONE = Long.MIN_VALUE;
   static final long SEVERAL = Long.MAX_VALUE;
   private static MapFacades.Source source;
   private static long missed = Long.MIN_VALUE;

   public static void provide(MapFacades.Source var0) {
      source = var0;
   }

   public static long cell(int var0, int var1) {
      int var2 = Math.floorDiv(var0, 256);
      int var3 = Math.floorDiv(var1, 256);
      return (long)var2 << 32 | var3 & 4294967295L;
   }

   static HashMap<Integer, HashMap<String, Integer>> building(BuildingDef var0) {
      HashMap var1 = source != null && var0 != null ? source.building(var0) : null;
      if (var1 == null && var0 != null) {
         miss(cell(var0.getX(), var0.getY()));
      }

      return var1;
   }

   static MapFacades.RoomWalls room(RoomDef var0) {
      MapFacades.RoomWalls var1 = source != null && var0 != null ? source.room(var0) : null;
      if (var1 == null && var0 != null) {
         miss(cell(var0.getX(), var0.getY()));
      }

      return var1;
   }

   static void begin() {
      missed = Long.MIN_VALUE;
   }

   static long missed() {
      return missed;
   }

   static boolean arrived(long var0, int var2) {
      if (source != null && var0 != Long.MIN_VALUE) {
         return var0 == Long.MAX_VALUE ? source.generation() != var2 : source.counted(var0);
      } else {
         return false;
      }
   }

   static int generation() {
      return source == null ? 0 : source.generation();
   }

   static void around(int var0, int var1) {
      if (source != null) {
         source.around(var0, var1);
      }
   }

   private static void miss(long var0) {
      if (source != null && !source.counted(var0)) {
         missed = missed != Long.MIN_VALUE && missed != var0 ? Long.MAX_VALUE : var0;
      }
   }

   private MapFacades() {
   }

   public static final class RoomWalls {
      private static final int UNSET = -2;
      final HashMap<Integer, HashMap<String, Integer>> counts;
      private int colour = -2;

      public RoomWalls(HashMap<Integer, HashMap<String, Integer>> var1) {
         this.counts = var1;
      }

      int colour() {
         if (this.colour == -2) {
            this.colour = BuildingFacades.colourOf(this.counts, 0);
         }

         return this.colour;
      }
   }

   public interface Source {
      HashMap<Integer, HashMap<String, Integer>> building(BuildingDef var1);

      MapFacades.RoomWalls room(RoomDef var1);

      boolean counted(long var1);

      int generation();

      void around(int var1, int var2);
   }
}
