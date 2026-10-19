package viewpoint.far;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

final class ShellFacades {
   static final byte OWN_ART = 1;
   static final byte INTERIOR = 2;
   static final ShellFacades NONE = new ShellFacades(1, new int[0], new int[0], new int[0]);
   private static final int SLOTS = 6;
   private final int levels;
   private final int[] keys;
   private final int[] tiles;
   private final int[] colours;

   private ShellFacades(int var1, int[] var2, int[] var3, int[] var4) {
      this.levels = var1;
      this.keys = var2;
      this.tiles = var3;
      this.colours = var4;
   }

   static ShellFacades cell(byte[] var0, int var1, int var2, FarTiles.Tile[] var3, short[][] var4, int var5) {
      return count(var3x -> ShellMesher.read(var0, var1, var2, 0, 0, 32, var3x), var3, var4, var5);
   }

   static ShellFacades count(Consumer<ShellMesher.Visit> var0, FarTiles.Tile[] var1, short[][] var2, int var3) {
      int var4 = var2.length;
      HashMap var5 = new HashMap();
      HashMap var6 = new HashMap();
      long[] var7 = new long[(var3 + 1) * 4];
      var0.accept((var6x, var7x, var8, var9) -> {
         ShellFacades.Wall var10 = var6x < var1.length ? var1[var6x].wall : null;
         if (var10 != null && var10.slot() >= 0 && ShellMesher.building(var2, var7x, var8, var9) == 0) {
            boolean var11 = var10.slot() % 2 == 1;
            int var12 = var11 ? ShellMesher.building(var2, var7x, var8 - 1, var9) : ShellMesher.building(var2, var7x - 1, var8, var9);
            if (var12 != 0) {
               add(var5, var6, var1, key(var4, var12, var9, var10.slot()), var6x);
               add(var5, var6, var1, key(var4, var12, var4, var10.slot()), var6x);
               if (var10.colour() >= 0) {
                  var7[var12 * 4] = var7[var12 * 4] + (var10.colour() >> 16 & 0xFF);
                  var7[var12 * 4 + 1] = var7[var12 * 4 + 1] + (var10.colour() >> 8 & 0xFF);
                  var7[var12 * 4 + 2] = var7[var12 * 4 + 2] + (var10.colour() & 0xFF);
                  var7[var12 * 4 + 3]++;
               }
            }
         }
      });
      return made(var4, var6, var7);
   }

   private static void add(HashMap<Long, Integer> var0, HashMap<Integer, int[]> var1, FarTiles.Tile[] var2, int var3, int var4) {
      int var5 = var0.merge((long)var3 << 32 | var4, 1, Integer::sum);
      int[] var6 = var1.computeIfAbsent(var3, var1x -> new int[]{var4, 0});
      if (var5 > var6[1] || var5 == var6[1] && var2[var4].wall.name().compareTo(var2[var6[0]].wall.name()) < 0) {
         var6[0] = var4;
         var6[1] = var5;
      }
   }

   private static ShellFacades made(int var0, HashMap<Integer, int[]> var1, long[] var2) {
      int[] var3 = new int[var1.size()];
      int[] var4 = new int[var3.length];
      int[] var5 = new int[var2.length / 4];
      int var6 = 0;

      for (int var8 : var1.keySet()) {
         var3[var6++] = var8;
      }

      Arrays.sort(var3);

      for (int var10 = 0; var10 < var3.length; var10++) {
         var4[var10] = ((int[])var1.get(var3[var10]))[0];
      }

      for (int var11 = 0; var11 < var5.length; var11++) {
         long var12 = var2[var11 * 4 + 3];
         var5[var11] = var12 == 0L ? -1 : (int)(var2[var11 * 4] / var12) << 16 | (int)(var2[var11 * 4 + 1] / var12) << 8 | (int)(var2[var11 * 4 + 2] / var12);
      }

      return new ShellFacades(var0, var3, var4, var5);
   }

   private static int key(int var0, int var1, int var2, int var3) {
      return (var1 * (var0 + 1) + var2) * 6 + var3;
   }

   FarTiles.Tile art(FarTiles.Tile var1, FarTiles.Tile[] var2, int var3, int var4, int var5, int var6) {
      ShellFacades.Wall var7 = var1.wall;
      if (var3 == 0 && var4 == 0) {
         return (var7.looks() & 2) != 0 ? null : var1;
      }

      if (var4 == 0 && (var7.looks() & 1) == 0) {
         byte var8 = var6 == 0 ? var1.north : var1.west;
         int var9 = var8 * 2 + (var6 == 0 ? 1 : 0);
         int var10 = this.tile(var3, var5, var9);
         if (var10 >= 0) {
            return var2[var10].shell == 1 ? var2[var10] : null;
         }

         int var11 = this.tile(var3, var5, 0 + var9 % 2);
         ShellFacades.Wall var12 = var11 >= 0 ? var2[var11].wall : null;
         FarTiles.Tile var13 = var12 == null ? null : (var8 == 1 ? var12.window() : var12.door());
         return var13 != null && var13.shell == 1 ? var13 : null;
      } else {
         return var1;
      }
   }

   int paint(int var1) {
      int var2 = var1 > 0 && var1 < this.colours.length ? this.colours[var1] : -1;
      return var2 >= 0 ? var2 : 9210501;
   }

   int tile(int var1, int var2, int var3) {
      int var4 = var2 >= 0 && var2 < this.levels ? Arrays.binarySearch(this.keys, key(this.levels, var1, var2, var3)) : -1;
      if (var4 < 0) {
         var4 = Arrays.binarySearch(this.keys, key(this.levels, var1, this.levels, var3));
      }

      return var4 >= 0 ? this.tiles[var4] : -1;
   }

   long bytes() {
      return 64L + this.keys.length * 8L + this.colours.length * 4L;
   }

   record Wall(String name, byte slot, byte looks, int colour, FarTiles.Tile window, FarTiles.Tile door) {
   }
}
