package viewpoint.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import viewpoint.platform.Caches;
import zombie.iso.BuildingDef;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.areas.IsoBuilding;
import zombie.iso.areas.IsoRoom;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteManager;
import zombie.util.list.PZArrayList;

final class BuildingFacades {
   private static final int SLOTS = 6;
   private static final int ALL = Integer.MIN_VALUE;
   private static final int UNSET = -2;
   private static final IdentityHashMap<BuildingDef, BuildingFacades.Shown> shown = new IdentityHashMap<>();
   private static long bytes;

   static int mappedColour(IsoBuilding var0) {
      BuildingFacades.Shown var1 = of(var0);
      if (var1 != null && var1.mapped) {
         if (var1.colour == -2) {
            var1.colour = colourOf(var1.counts, Integer.MIN_VALUE);
         }

         return var1.colour;
      } else {
         return -1;
      }
   }

   static IsoSprite most(IsoBuilding var0, int var1, int var2) {
      BuildingFacades.Shown var3 = of(var0);
      if (var3 == null) {
         return null;
      }

      String var4 = mostCounted(var3.counts.get(var1 * 6 + var2));
      return sprite(var4 != null ? var4 : mostCounted(var3.counts.get(Integer.MIN_VALUE + var2)));
   }

   static IsoSprite sheetTile(IsoBuilding var0, int var1, int var2) {
      BuildingFacades.Shown var3 = of(var0);
      IsoSprite var4 = var3 == null ? null : most(var0, var1, 0 + var2 % 2);
      if (var4 != null && var4.tilesetName != null) {
         String var5 = var3.kits.get(var1 * 6 + var2);
         if (var5 == null) {
            String var6 = Facades.sheetTile(var4, var2);
            var5 = var6 == null ? "" : var6;
            var3.kits.put(var1 * 6 + var2, var5);
         }

         return sprite(var5);
      } else {
         return null;
      }
   }

   static void forget() {
      shown.clear();
      bytes = 0L;
   }

   private static BuildingFacades.Shown of(IsoBuilding var0) {
      BuildingDef var1 = var0 == null ? null : var0.def;
      if (var1 == null) {
         return null;
      }

      BuildingFacades.Shown var2 = shown.get(var1);
      if (var2 == null || !var2.mapped) {
         HashMap<Integer, HashMap<String, Integer>> var3 = MapFacades.building(var1);
         if (var3 != null) {
            var2 = keep(var1, var2, mapped(var3));
         } else {
            int var4 = loaded(var0);
            if (var2 == null || var4 > var2.squares) {
               var2 = keep(var1, var2, read(var0, var4));
            }
         }
      }

      var2.used = Caches.tick();
      return var2;
   }

   private static BuildingFacades.Shown keep(BuildingDef var0, BuildingFacades.Shown var1, BuildingFacades.Shown var2) {
      bytes = bytes + (var2.bytes - (var1 == null ? 0L : var1.bytes));
      shown.put(var0, var2);
      return var2;
   }

   private static BuildingFacades.Shown mapped(HashMap<Integer, HashMap<String, Integer>> var0) {
      BuildingFacades.Shown var1 = new BuildingFacades.Shown();
      var1.mapped = true;
      var1.counts.putAll(var0);
      var1.bytes = 128L + 112L * var1.counts.size();
      return var1;
   }

   static int colourOf(HashMap<Integer, HashMap<String, Integer>> var0, int var1) {
      long var2 = 0L;
      long var4 = 0L;
      long var6 = 0L;
      long var8 = 0L;

      for (int var10 = 0; var10 < 6; var10++) {
         HashMap<String, Integer> var11 = var0.get(var1 + var10);
         if (var11 != null) {
            for (Entry<String, Integer> var13 : var11.entrySet()) {
               int var14 = FacadeColours.of(var13.getKey());
               if (var14 >= 0) {
                  var2 += (long)(var14 >> 16 & 0xFF) * ((Integer)var13.getValue()).intValue();
                  var4 += (long)(var14 >> 8 & 0xFF) * ((Integer)var13.getValue()).intValue();
                  var6 += (long)(var14 & 0xFF) * ((Integer)var13.getValue()).intValue();
                  var8 += ((Integer)var13.getValue()).intValue();
               }
            }
         }
      }

      return var8 == 0L ? -1 : (int)(var2 / var8) << 16 | (int)(var4 / var8) << 8 | (int)(var6 / var8);
   }

   static int loaded(IsoBuilding var0) {
      int var1 = 0;

      for (int var2 = 0; var2 < var0.rooms.size(); var2++) {
         var1 += ((IsoRoom)var0.rooms.get(var2)).getSquares().size();
      }

      return var1;
   }

   private static BuildingFacades.Shown read(IsoBuilding var0, int var1) {
      BuildingFacades.Shown var2 = new BuildingFacades.Shown();
      var2.squares = var1;

      for (int var3 = 0; var3 < var0.rooms.size(); var3++) {
         var var4 = ((IsoRoom)var0.rooms.get(var3)).getSquares();

         for (int var5 = 0; var5 < var4.size(); var5++) {
            IsoGridSquare var6 = (IsoGridSquare)var4.get(var5);
            count(var2, var6.getAdjacentSquare(IsoDirections.S), var6.getZ(), 1);
            count(var2, var6.getAdjacentSquare(IsoDirections.E), var6.getZ(), 0);
         }
      }

      var2.bytes = 128L + 112L * var2.counts.size();
      return var2;
   }

   private static void count(BuildingFacades.Shown var0, IsoGridSquare var1, int var2, int var3) {
      if (var1 != null && var1.getRoom() == null) {
         PZArrayList var4 = var1.getObjects();

         for (int var5 = 0; var5 < var4.size(); var5++) {
            IsoSprite var6 = ((IsoObject)var4.get(var5)).getSprite();
            int var7 = var6 != null && var6.getName() != null ? Facades.kindOf(var6) : -1;
            if (var7 >= 0 && var7 % 2 == var3) {
               var0.counts.computeIfAbsent(var2 * 6 + var7, var0x -> new HashMap<>()).merge(var6.getName(), 1, Integer::sum);
               var0.counts.computeIfAbsent(Integer.MIN_VALUE + var7, var0x -> new HashMap<>()).merge(var6.getName(), 1, Integer::sum);
            }
         }
      }
   }

   static String mostCounted(HashMap<String, Integer> var0) {
      if (var0 == null) {
         return null;
      }

      String var1 = null;
      int var2 = 0;

      for (Entry<String, Integer> var4 : var0.entrySet()) {
         if (var4.getValue() > var2 || var4.getValue() == var2 && var4.getKey().compareTo(var1) < 0) {
            var1 = var4.getKey();
            var2 = var4.getValue();
         }
      }

      return var1;
   }

   static IsoSprite sprite(String var0) {
      return var0 != null && !var0.isEmpty() && IsoSpriteManager.instance.getNamedMap().containsKey(var0) ? IsoSpriteManager.instance.getSprite(var0) : null;
   }

   private BuildingFacades() {
   }

   static {
      Caches.register(new Caches.Cache() {
         @Override
         public long bytes() {
            return BuildingFacades.bytes;
         }

         @Override
         public void list(Caches.Survey var1) {
            for (BuildingFacades.Shown var3 : BuildingFacades.shown.values()) {
               var1.add(var3.used, var3.bytes);
            }
         }

         @Override
         public void evictBefore(long var1) {
            Iterator<BuildingFacades.Shown> var3 = BuildingFacades.shown.values().iterator();

            while (var3.hasNext()) {
               BuildingFacades.Shown var4 = var3.next();
               if (var4.used < var1) {
                  BuildingFacades.bytes = BuildingFacades.bytes - var4.bytes;
                  var3.remove();
               }
            }
         }
      });
   }

   private static final class Shown {
      final HashMap<Integer, HashMap<String, Integer>> counts = new HashMap<>();
      final HashMap<Integer, String> kits = new HashMap<>();
      long used;
      long bytes;
      int squares;
      boolean mapped;
      int colour = -2;
   }
}