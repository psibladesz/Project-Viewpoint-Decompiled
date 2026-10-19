package viewpoint.far;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

final class ErosionPlants {
   static final String BUSHES = "f_bushes_1_";
   static final String PLANTS = "d_plants_1_";
   static final String GRASS = "e_newgrass_1_";
   static final String GENERIC = "d_generic_1_";
   private static final String FOLIAGE = "vegetation_foliage";
   private static final String GROUNDCOVER = "vegetation_groundcover_01_";
   private static final String OVERLAYS = "blends_grassoverlays_01_";
   private static final String NATURAL = "blends_natural_01_";
   private static final int CHANCE_DRAW = 101;
   private static final int SPAWN = 100;
   private static final int CYCLE = 60;
   private static final float BUSH_STAGE = 60.0F;
   private static final int BUSH_KINDS = 16;
   private static final int STEMS = 8;
   private static final int GROWN_BUSH = 1;
   private static final int SPRING = 32;
   private static final int AUTUMN = 48;
   private static final int SUMMER = 64;
   private static final int SUMMER_STAGE = 32;
   private static final int PLANT_KINDS = 24;
   private static final int GROUNDCOVER_PLANT = 21;
   private static final int GROUNDCOVER_FIRST = 16;
   private static final int GROUNDCOVER_DRAWN = 18;
   private static final int GROUNDCOVER_LAST = 23;
   private static final int SPRING_PLANTS = 0;
   private static final int AUTUMN_PLANTS = 8;
   private static final int PLANTS_A_SEASON = 8;
   private static final int GRASS_KINDS = 6;
   private static final int GRASS_STAGES = 3;
   private static final int GRASS_ROW = 8;
   private static final int GRASS_SEASON = 24;
   private static final int GRASS_DISPLAYS = 5;
   private static final int LITTER = 16;
   private static final int FERNS = 48;
   private static final int FERN_STAGE = 32;
   private static final int FERN_KINDS = 8;
   private static final int[][] OVERLAY_GRASS = new int[][]{{0, 21, 24}, {22, 45, 48}, {48, 69, 72}};
   private static final int BARE = 0;
   private static final int SPRING_DISPLAY = 1;
   private static final int SUMMER_DISPLAY = 2;
   private static final int LATE_SUMMER = 3;
   private static final int AUTUMN_DISPLAY = 4;
   private final int[][] bushSoil;
   private final int[][] plantSoil;
   private final int[] bushChance;
   private final int[] plantChance;
   private final Set<String> twigs;
   private final Map<String, FarTiles.Tile> tiles;

   ErosionPlants(int[][] var1, int[] var2, int[][] var3, int[] var4, Set<String> var5) {
      this(var1, var2, var3, var4, var5, Map.of());
   }

   private ErosionPlants(int[][] var1, int[] var2, int[][] var3, int[] var4, Set<String> var5, Map<String, FarTiles.Tile> var6) {
      this.bushSoil = var1;
      this.bushChance = var2;
      this.plantSoil = var3;
      this.plantChance = var4;
      this.twigs = var5;
      this.tiles = var6;
   }

   ErosionPlants withTiles() {
      HashMap var1 = new HashMap();

      for (String var3 : sprites()) {
         FarTiles.Tile var4 = var3.startsWith("f_bushes_1_") && frame(var3, "f_bushes_1_") >= 32 ? TreeSeasons.leafTile(var3, true) : FarTiles.of(var3);
         if (var4 != null) {
            var1.put(var3, var4);
         }
      }

      return new ErosionPlants(this.bushSoil, this.bushChance, this.plantSoil, this.plantChance, this.twigs, var1);
   }

   static List<String> sprites() {
      ArrayList var0 = new ArrayList();

      for (int var1 = 0; var1 < 128; var1++) {
         var0.add("f_bushes_1_" + var1);
      }

      for (int var2 = 0; var2 < 64; var2++) {
         var0.add("d_plants_1_" + var2);
      }

      for (int var3 = 0; var3 < 120; var3++) {
         var0.add("e_newgrass_1_" + var3);
      }

      for (int var4 = 0; var4 < 112; var4++) {
         var0.add("d_generic_1_" + var4);
      }

      return var0;
   }

   void erode(Random var1, List<WorldGen.Thing> var2, int var3, int var4, int var5, int var6, byte[][] var7, int var8, int var9) {
      byte var10 = var7[1][var5];
      if (!this.replaceBushes(var1, var2, var3, var4, var10)
         && !this.spawnBush(var1, var2, var3, var4, var6, var10)
         && !this.replacePlants(var1, var2, var10, var8, var9)
         && !this.spawnPlant(var1, var2, var3, var4, var6, var10, var8, var9)) {
         this.replaceGeneric(var2, var6, var7[2][var5]);
      }
   }

   private boolean replaceBushes(Random var1, List<WorldGen.Thing> var2, int var3, int var4, int var5) {
      int var6 = -1;
      int var7 = 0;

      for (int var8 = var2.size() - 1; var8 >= 1; var8--) {
         String var9 = ((WorldGen.Thing)var2.get(var8)).name();
         if (var9.startsWith("vegetation_foliage")) {
            int var10 = var3 >= 0 && var3 < this.bushSoil.length ? var3 : next(var1, this.bushSoil.length);
            int[] var11 = this.bushSoil[var10];
            var6 = var11[next(var1, var11.length)] - 1;
            var7 = (int)Math.floor(var4 / 60.0F);
            var2.remove(var8);
         } else if (var9.startsWith("f_bushes_1_")) {
            var6 = frame(var9, "f_bushes_1_") % 16;
            var7 = 1;
            var2.remove(var8);
         }
      }

      if (var6 < 0) {
         return false;
      }

      this.bush(var2, var6, var7, var5);
      return true;
   }

   private boolean spawnBush(Random var1, List<WorldGen.Thing> var2, int var3, int var4, int var5, int var6) {
      if (var2.size() <= 1 && var3 >= 0 && var3 < this.bushSoil.length) {
         int[] var7 = this.bushSoil[var3];
         if (next(var1, 101) >= this.bushChance[var4]) {
            return false;
         }

         int var8 = var7[next(var1, var7.length)] - 1;
         int var9 = (int)Math.floor(var4 / 60.0F);
         int var10 = 100 - var4;
         if (var5 >= var10) {
            int var11 = (int)Math.floor((var5 - var10) / (60.0F / (var9 + 1.0F)));
            this.bush(var2, var8, Math.min(Math.max(var11, 0), var9), var6);
         }

         return true;
      } else {
         return false;
      }
   }

   private void bush(List<WorldGen.Thing> var1, int var2, int var3, int var4) {
      if (var2 >= 0 && var2 < 16) {
         int var5 = var2 % 8;
         this.put(var1, "f_bushes_1_" + (var5 + 8 * var3));

         int var6 = switch (var4) {
            case 1 -> var5 + 32 + 8 * var3;
            case 2, 3 -> 64 + var2 + 32 * var3;
            case 4 -> var5 + 48 + 8 * var3;
            default -> -1;
         };
         if (var6 >= 0) {
            this.put(var1, "f_bushes_1_" + var6);
         }
      }
   }

   private boolean replacePlants(Random var1, List<WorldGen.Thing> var2, int var3, int var4, int var5) {
      int var6 = var2.size() - 1;

      int var8;
      label50:
      while (true) {
         if (var6 < 1) {
            return false;
         }

         String var7 = ((WorldGen.Thing)var2.get(var6)).name();
         if (var7.startsWith("d_plants_1_")) {
            int var10 = frame(var7, "d_plants_1_");
            var8 = var10 < 32 ? var10 % 8 : (var10 < 48 ? var10 % 8 + 8 : var10 % 8 + 16);
            var2.remove(var6);
            break;
         }

         int var9 = var7.startsWith("vegetation_groundcover_01_") ? frame(var7, "vegetation_groundcover_01_") : -1;
         if (var9 >= 16 && var9 <= 23) {
            var8 = var9 < 18 ? 21 : next(var1, 24);
            var2.remove(var6);

            while (true) {
               if (--var6 <= 0) {
                  break label50;
               }

               if (((WorldGen.Thing)var2.get(var6)).name().startsWith("vegetation_groundcover_01_")) {
                  var2.remove(var6);
               }
            }
         }

         var6--;
      }

      this.plant(var2, var8, var3, var4, var5);
      return true;
   }

   private boolean spawnPlant(Random var1, List<WorldGen.Thing> var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      if (var2.size() <= 1 && var3 >= 0 && var3 < this.plantSoil.length) {
         int[] var9 = this.plantSoil[var3];
         if (next(var1, 101) >= this.plantChance[var4]) {
            return false;
         }

         int var10 = var9[next(var1, var9.length)] - 1;
         if (var5 >= 100 - var4) {
            this.plant(var2, var10, var6, var7, var8);
         }

         return true;
      } else {
         return false;
      }
   }

   private void plant(List<WorldGen.Thing> var1, int var2, int var3, int var4, int var5) {
      if (var2 >= 0 && var2 < 24) {
         int var6 = FarTrees.hash(var4, var5) % 8;

         int var7 = switch (var3) {
            case 1 -> 0 + var6;
            case 2 -> (var2 < 8 ? 16 : (var2 < 16 ? 24 : 32)) + var2;
            default -> -1;
            case 4 -> 8 + var6;
         };
         if (var7 >= 0) {
            this.put(var1, "d_plants_1_" + var7);
         }
      }
   }

   private void replaceGeneric(List<WorldGen.Thing> var1, int var2, int var3) {
      for (int var4 = var1.size() - 1; var4 >= 1; var4--) {
         String var5 = asGrass(((WorldGen.Thing)var1.get(var4)).name());
         if (!this.twigs.contains(var5)) {
            if (var5.startsWith("e_newgrass_1_")) {
               int var6 = frame(var5, "e_newgrass_1_");
               int var7 = var6 % 8;
               if (var7 < 6 && var6 < 120) {
                  int var8 = 2 - var6 % 24 / 8;
                  byte var9 = 2;
                  int var10 = var8 == var9 ? var9 : Math.min(var2 / 20, var9);
                  int var11 = Math.max(var3, dried(((WorldGen.Thing)var1.get(0)).name()));
                  var1.remove(var4);
                  if (var11 > 0) {
                     this.put(var1, var4, "e_newgrass_1_" + ((var11 - 1) * 24 + (var9 - var10) * 8 + var7));
                  }

                  return;
               }
            } else if (var5.startsWith("d_generic_1_")) {
               int var12 = frame(var5, "d_generic_1_");
               boolean var13 = var12 >= 48 && var12 < 112 && (var12 - 48) % 32 < 8;
               if (var13 || var12 < 32) {
                  int var14 = var13 ? (var12 - 48) / 32 : var12 / 16;
                  int var15 = var13 ? (var12 - 48) % 32 : var12 % 16;
                  int var16 = var14 == 1 ? 1 : Math.min(var2 / 30, 1);
                  var1.set(var4, this.thing("d_generic_1_" + (var13 ? 48 + 32 * var16 + var15 : 16 * var16 + var15)));
                  return;
               }
            }
         }
      }
   }

   private static String asGrass(String var0) {
      if (!var0.startsWith("blends_grassoverlays_01_")) {
         return var0;
      }

      int var1 = frame(var0, "blends_grassoverlays_01_");

      for (int[] var5 : OVERLAY_GRASS) {
         if (var1 >= var5[0] && var1 <= var5[1]) {
            return "e_newgrass_1_" + (var5[2] + var1 - var5[0]);
         }
      }

      return var0;
   }

   private static int dried(String var0) {
      if (!var0.startsWith("blends_natural_01_")) {
         return 0;
      }

      int var1 = frame(var0, "blends_natural_01_");
      int var2 = var1 / 8;
      int var3 = var1 % 8;
      boolean var4 = var3 == 0 || var3 >= 5;
      return var4 && var2 == 4 ? 3 : (var4 && var2 == 6 ? 4 : 0);
   }

   private void put(List<WorldGen.Thing> var1, String var2) {
      var1.add(this.thing(var2));
   }

   private void put(List<WorldGen.Thing> var1, int var2, String var3) {
      var1.add(Math.min(var2, var1.size()), this.thing(var3));
   }

   private WorldGen.Thing thing(String var1) {
      return new WorldGen.Thing(var1, 0, this.tiles.get(var1));
   }

   private static int frame(String var0, String var1) {
      try {
         return Integer.parseInt(var0.substring(var1.length()));
      } catch (NumberFormatException | IndexOutOfBoundsException var3) {
         return -1;
      }
   }

   void dump(PrintWriter var1) {
      for (int[] var5 : this.bushSoil) {
         var1.println("erosion bushsoil " + Erosion.join(var5));
      }

      var1.println("erosion bushchance " + Erosion.join(this.bushChance));

      for (int[] var9 : this.plantSoil) {
         var1.println("erosion plantsoil " + Erosion.join(var9));
      }

      var1.println("erosion plantchance " + Erosion.join(this.plantChance));
      var1.println("erosion twigs " + String.join(" ", this.twigs));
   }

   private static int next(Random var0, int var1) {
      return var1 > 0 ? var0.nextInt(var1) : 0;
   }
}
