package viewpoint.far;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import zombie.core.properties.IsoPropertyType;
import zombie.erosion.ErosionMain;
import zombie.erosion.ErosionRegions;
import zombie.erosion.categories.ErosionCategory;
import zombie.erosion.season.ErosionSeason;
import zombie.erosion.utils.Noise2D;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteManager;

final class Erosion {
   static final String NATURAL = "blends_natural_01";
   static final int STAGES = 8;
   static final int NONE = -1;
   private static final int FREE = -2;
   private static final int CYCLE = 60;
   private static final int SPAWN = 130;
   private static final int PLANT_NOISE = 50;
   private static final int PLANT_STEP = 17;
   private static final float SIZE_STEP = 51.0F;
   private static final int GROWN = 3;
   private static final int JUMBO = 5;
   private static final int FIRST_JUMBO = 4;
   private static final int XL = 6;
   private static final int XXL = 7;
   private static final int XL_SHEET = 2;
   private static final int CHANCE_DRAW = 101;
   private static final int CHUNK = 8;
   private static final float SOIL_SCALE = 5.0F;
   private static final float MAIN_SCALE = 10.0F;
   private static final float TENTHS = 10.0F;
   private static final float HUNDREDTHS = 100.0F;
   private static final int LAST_TENTH = 9;
   private static final String PLACEHOLDER = "vegetation_trees";
   private static final String JUMBO_PLACEHOLDER = "jumbo_tree_01";
   private final Erosion.Noise main;
   private final Erosion.Noise moisture;
   private final Erosion.Noise minerals;
   private final int[][] soilTable;
   private final int[][] soilRef;
   private final int[] spawnChance;
   private final String[][] sheets;
   final int ticks;
   private final FarTiles.Tile[][] tiles;
   private final ErosionPlants plants;
   private static Erosion current;
   private static boolean unreadable;

   Erosion(Erosion.Noise var1, Erosion.Noise var2, Erosion.Noise var3, int[][] var4, int[][] var5, int[] var6, String[] var7, int var8) {
      this.main = var1;
      this.moisture = var2;
      this.minerals = var3;
      this.soilTable = var4;
      this.soilRef = var5;
      this.spawnChance = var6;
      this.ticks = var8;
      this.sheets = new String[var7.length][];

      for (int var9 = 0; var9 < var7.length; var9++) {
         String var10 = var7[var9];
         this.sheets[var9] = new String[]{var10, var10.replace("_1", "JUMBO_1"), var10.replace("_1", "JUMBOXL_1"), var10.replace("_1", "JUMBOXXL_1")};
      }

      this.tiles = null;
      this.plants = null;
   }

   private Erosion(Erosion var1, int var2, FarTiles.Tile[][] var3, ErosionPlants var4) {
      this.main = var1.main;
      this.moisture = var1.moisture;
      this.minerals = var1.minerals;
      this.soilTable = var1.soilTable;
      this.soilRef = var1.soilRef;
      this.spawnChance = var1.spawnChance;
      this.sheets = var1.sheets;
      this.ticks = var2;
      this.tiles = var3;
      this.plants = var4;
   }

   Erosion withPlants(ErosionPlants var1) {
      return new Erosion(this, this.ticks, this.tiles, var1);
   }

   int tree(WorldGen.Source var1, List<WorldGen.Thing> var2, int var3, int var4) {
      return this.square(var1, var2, var3, var4, null);
   }

   int square(WorldGen.Source var1, List<WorldGen.Thing> var2, int var3, int var4, byte[][] var5) {
      if (!var2.isEmpty() && (((WorldGen.Thing)var2.get(0)).kind() & 1) != 0 && ((WorldGen.Thing)var2.get(0)).name().startsWith("blends_natural_01")) {
         Random var6 = WorldGen.generator(var1.seed(var3, var4));
         int var7 = var6.nextInt(100);

         for (int var8 = var2.size() - 1; var8 >= 1; var8--) {
            String var9 = ((WorldGen.Thing)var2.get(var8)).name();
            if (var9.startsWith("jumbo_tree_01")) {
               return this.placeholder(var6, var3, var4, 5);
            }

            if (var9.startsWith("vegetation_trees")) {
               return this.placeholder(var6, var3, var4, 3);
            }

            int var10 = this.own(var9);
            if (var10 != -1) {
               return var10;
            }
         }

         int var11 = this.soil(var3, var4);
         int var12 = this.noise(var3, var4);
         int var13 = this.planted(var6, var2.size(), var11, var12);
         if (var13 == -2 && this.plants != null && var5 != null) {
            this.plants.erode(var6, var2, var11, var12, var7, this.ticks, var5, var3, var4);
         }

         return var13 == -2 ? -1 : var13;
      } else {
         return -1;
      }
   }

   FarTiles.Tile tile(int var1) {
      return this.tiles == null ? null : this.tiles[var1 / 8][var1 % 8];
   }

   String name(int var1) {
      String[] var2 = this.sheets[var1 / 8];
      int var3 = var1 % 8;
      if (var3 >= 6) {
         return var2[2 + var3 - 6] + "_0";
      } else {
         return var3 < 4 ? var2[0] + "_" + var3 : var2[1] + "_" + (var3 - 4);
      }
   }

   private int placeholder(Random var1, int var2, int var3, int var4) {
      int var5 = this.soil(var2, var3);
      if (var5 < 0 || var5 >= this.soilRef.length) {
         var5 = next(var1, this.soilRef.length);
      }

      int[] var6 = this.soilRef[var5];
      int var7 = var6[next(var1, var6.length)] - 1;
      return this.code(var7, var4 + (int)Math.floor(this.noise(var2, var3) / 51.0F) - 1);
   }

   private int planted(Random var1, int var2, int var3, int var4) {
      if (var2 <= 1 && var3 >= 0 && var3 < this.soilRef.length) {
         int var5 = var4 < this.spawnChance.length ? this.spawnChance[var4] : 0;
         if (var5 > 0 && next(var1, 101) < var5) {
            int[] var6 = this.soilRef[var3];
            int var7 = var6[next(var1, var6.length)] - 1;
            int var8 = 1 + (var4 - 50) / 17;
            int var9 = 130 - var4;
            if (this.ticks < var9) {
               return -1;
            }

            int var10 = (int)Math.floor((this.ticks - var9) / (60.0F / (var8 + 1.0F)));
            return this.code(var7, Math.min(Math.max(var10, 0), var8));
         } else {
            return -2;
         }
      } else {
         return -2;
      }
   }

   private int code(int var1, int var2) {
      return var1 >= 0 && var1 < this.sheets.length && var2 >= 0 && var2 < 8 ? var1 * 8 + var2 : -1;
   }

   private int own(String var1) {
      for (int var2 = 0; var2 < this.sheets.length; var2++) {
         String[] var3 = this.sheets[var2];
         if (var1.startsWith(var3[0])) {
            return this.code(var2, 3);
         }

         if (var1.startsWith(var3[1])) {
            return this.code(var2, frame(var1) % 2 == 0 ? 4 : 5);
         }

         if (var1.startsWith(var3[2]) || var1.startsWith(var3[3])) {
            return this.code(var2, var1.startsWith(var3[2]) ? 6 : 7);
         }
      }

      return -1;
   }

   private static int frame(String var0) {
      try {
         return Integer.parseInt(var0.substring(var0.lastIndexOf(95) + 1));
      } catch (NumberFormatException var2) {
         return 0;
      }
   }

   private int soil(int var1, int var2) {
      float var3 = Math.floorDiv(var1, 8) / 5.0F;
      float var4 = Math.floorDiv(var2, 8) / 5.0F;
      float var5 = this.moisture.at(var3, var4);
      float var6 = this.minerals.at(var3, var4);
      int var7 = var5 < 1.0F ? (int)Math.floor(var5 * 10.0F) : 9;
      int var8 = var6 < 1.0F ? (int)Math.floor(var6 * 10.0F) : 9;
      return this.soilTable[var7][var8] - 1;
   }

   private int noise(int var1, int var2) {
      return (int)Math.floor(this.main.at(var1 / 10.0F, var2 / 10.0F) * 100.0F);
   }

   void dump(PrintWriter var1) {
      var1.println("erosion ticks " + this.ticks);
      var1.println("erosion perm " + join(this.main.perm));
      String[] var2 = new String[]{"main", "moisture", "minerals"};
      Erosion.Noise[] var3 = new Erosion.Noise[]{this.main, this.moisture, this.minerals};

      for (int var4 = 0; var4 < var3.length; var4++) {
         for (int var5 = 0; var5 < var3[var4].freq.length; var5++) {
            var1.println("erosion layer " + var2[var4] + " " + var3[var4].freq[var5] + " " + var3[var4].amp[var5] + " " + join(var3[var4].p[var5]));
         }
      }

      for (int[] var7 : this.soilTable) {
         var1.println("erosion soiltable " + join(var7));
      }

      for (int[] var19 : this.soilRef) {
         var1.println("erosion soilref " + join(var19));
      }

      var1.println("erosion chance " + join(this.spawnChance));
      StringBuilder var11 = new StringBuilder("erosion trees");

      for (String[] var8 : this.sheets) {
         var11.append(' ').append(var8[0]);
      }

      var1.println(var11);
      if (this.plants != null) {
         this.plants.dump(var1);
      }

      ErosionMain var15 = ErosionMain.getInstance();
      ErosionSeason var18 = var15 == null ? null : var15.getSeasons();
      if (var18 != null) {
         var1.println("erosion season " + var18.getSeason() + " " + var18.getSeasonDay() + " " + var18.getSeasonDays());
      }
   }

   static String join(int[] var0) {
      StringBuilder var1 = new StringBuilder();

      for (int var5 : var0) {
         var1.append(var1.isEmpty() ? "" : " ").append(var5);
      }

      return var1.toString();
   }

   private static int next(Random var0, int var1) {
      return var1 > 0 ? var0.nextInt(var1) : 0;
   }

   static Erosion current() {
      ErosionMain var0 = ErosionMain.getInstance();
      if (current == null && !unreadable && var0 != null) {
         try {
            Erosion var1 = read(var0);
            if (var1 == null) {
               return null;
            }

            FarTiles.Tile[][] var2 = new FarTiles.Tile[var1.sheets.length][8];

            for (int var3 = 0; var3 < var2.length; var3++) {
               for (int var4 = 0; var4 < 8; var4++) {
                  var2[var3][var4] = FarTiles.of(var1.name(var3 * 8 + var4));
               }
            }

            current = new Erosion(var1, var1.ticks, var2, var1.plants.withTiles());
         } catch (ReflectiveOperationException | RuntimeException var5) {
            unreadable = true;
            System.out.println("[Viewpoint] far world: the erosion's trees left out, its numbers unreadable: " + var5);
         }

         return current;
      } else {
         return current;
      }
   }

   static boolean update() {
      Erosion var0 = current();
      ErosionMain var1 = ErosionMain.getInstance();
      if (var0 != null && var1 != null && var1.getEtick() != var0.ticks) {
         current = new Erosion(var0, var1.getEtick(), var0.tiles, var0.plants);
         return Math.min(var0.ticks, current.ticks) <= 140;
      } else {
         return false;
      }
   }

   static void forget() {
      current = null;
      unreadable = false;
   }

   static Erosion read(ErosionMain var0) throws ReflectiveOperationException {
      Noise2D var1 = (Noise2D)field(ErosionMain.class, "noiseMain").get(var0);
      if (var1 == null) {
         return null;
      }

      ErosionCategory var2 = ErosionRegions.getCategory(0, 0);
      Object[] var3 = (Object[])field(var2.getClass(), "trees").get(var2);
      String[] var4 = new String[var3.length];

      for (int var5 = 0; var5 < var3.length; var5++) {
         var4[var5] = (String)field(var3[var5].getClass(), "tilesetName").get(var3[var5]);
      }

      ErosionCategory var7 = ErosionRegions.getCategory(0, 1);
      ErosionCategory var6 = ErosionRegions.getCategory(0, 2);
      return new Erosion(
            noise(var1),
            noise((Noise2D)field(ErosionMain.class, "noiseMoisture").get(var0)),
            noise((Noise2D)field(ErosionMain.class, "noiseMinerals").get(var0)),
            (int[][])field(ErosionMain.class, "soilTable").get(null),
            (int[][])field(var2.getClass(), "soilRef").get(var2),
            (int[])field(var2.getClass(), "spawnChance").get(var2),
            var4,
            var0.getEtick()
         )
         .withPlants(
            new ErosionPlants(
               (int[][])field(var7.getClass(), "soilRef").get(var7),
               (int[])field(var7.getClass(), "spawnChance").get(var7),
               (int[][])field(var6.getClass(), "soilRef").get(var6),
               (int[])field(var6.getClass(), "spawnChance").get(var6),
               twigs()
            )
         );
   }

   private static Set<String> twigs() {
      HashSet var0 = new HashSet();

      for (String var2 : ErosionPlants.sprites()) {
         IsoSprite var3 = (IsoSprite)IsoSpriteManager.instance.getNamedMap().get(var2);
         String var4 = var3 == null ? null : var3.getProperties().get(IsoPropertyType.CUSTOM_NAME);
         if (var4 != null && (var4.contains("Twig") || var4.contains("Branch"))) {
            var0.add(var2);
         }
      }

      return var0;
   }

   static Erosion.Noise noise(Noise2D var0) throws ReflectiveOperationException {
      int[] var1 = (int[])field(Noise2D.class, "perm").get(null);
      ArrayList var2 = (ArrayList)field(Noise2D.class, "layers").get(var0);
      float[] var3 = new float[var2.size()];
      float[] var4 = new float[var2.size()];
      int[][] var5 = new int[var2.size()][];

      for (int var6 = 0; var6 < var5.length; var6++) {
         Object var7 = var2.get(var6);
         var3[var6] = field(var7.getClass(), "freq").getFloat(var7);
         var4[var6] = field(var7.getClass(), "amp").getFloat(var7);
         var5[var6] = (int[])((int[])field(var7.getClass(), "p").get(var7)).clone();
      }

      return new Erosion.Noise(var3, var4, var5, (int[])var1.clone());
   }

   private static Field field(Class<?> var0, String var1) throws NoSuchFieldException {
      Field var2 = var0.getDeclaredField(var1);
      var2.setAccessible(true);
      return var2;
   }

   record Noise(float[] freq, float[] amp, int[][] p, int[] perm) {
      private static final float WRAP = 255.0F;

      float at(float var1, float var2) {
         float var3 = 0.0F;
         float var4 = 0.0F;

         for (int var5 = 0; var5 < this.freq.length; var5++) {
            var4 += this.amp[var5];
            var3 += this.noise(var1 * this.freq[var5], var2 * this.freq[var5], this.p[var5]) * this.amp[var5];
         }

         return var3 / var4 / 255.0F;
      }

      private float noise(float var1, float var2, int[] var3) {
         int var4 = (int)Math.floor(var1 - Math.floor(var1 / 255.0F) * 255.0);
         int var5 = (int)Math.floor(var2 - Math.floor(var2 / 255.0F) * 255.0);
         float var6 = fade(var1 - (float)Math.floor(var1));
         float var7 = fade(var2 - (float)Math.floor(var2));
         int var8 = var3[var4] + var5;
         int var9 = var3[var4 + 1] + var5;
         return lerp(var7, lerp(var6, this.perm[var3[var8]], this.perm[var3[var9]]), lerp(var6, this.perm[var3[var8 + 1]], this.perm[var3[var9 + 1]]));
      }

      private static float fade(float var0) {
         return var0 * var0 * var0 * (var0 * (var0 * 6.0F - 15.0F) + 10.0F);
      }

      private static float lerp(float var0, float var1, float var2) {
         return var1 + var0 * (var2 - var1);
      }
   }
}
