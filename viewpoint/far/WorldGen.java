package viewpoint.far;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

final class WorldGen {
   static final int GROUND = 0;
   static final int PLANT = 1;
   static final int BUSH = 2;
   static final int TREE = 3;
   static final int ORE = 4;
   static final int TYPES = 5;
   static final int FLOOR = 1;
   static final int IS_TREE = 2;
   static final int IS_BUSH = 4;
   static final int GRASS = 8;
   static final int GRASS_LIKE = 16;
   static final int IS_ORE = 32;
   static final int BLOCK = 16;
   private static final int CHUNK = 8;
   private static final String ANY = "$any";
   private static final String SUBBIOME = "$subbiome";
   private static final String NO_TREE = "$no_tree";
   private static final String NO_BUSH = "$no_bush";
   private static final String NO_GRASS = "$no_grass";
   private static final ThreadLocal<Random> GENERATOR = ThreadLocal.withInitial(Random::new);
   private final WorldGen.Source source;
   private final ArrayList<WorldGen.Thing>[] squares;
   private final String[][] pending = new String[5][256];
   private final int x0;
   private final int y0;
   Consumer<String> trace;
   private final WorldGen.Biome[] override = new WorldGen.Biome[5];
   private final Random squareRandom = new Random();
   private final Random subRandom = new Random();
   private static final int SUCCESS = 0;
   private static final int PENDING = 1;
   private static final int DELETE = 2;
   private static final int FAILURE = 3;
   private static final int FOUND = 0;
   private static final int LIST_EMPTY = 1;
   private static final int NO_FEATURE = 2;
   private static final int ELSEWHERE = 3;
   private final WorldGen.Thing[][] pendingThings = new WorldGen.Thing[5][256];

   static Random generator(long var0) {
      Random var2 = GENERATOR.get();
      var2.setSeed(var0);
      return var2;
   }

   WorldGen(WorldGen.Source var1, ArrayList<WorldGen.Thing>[] var2, int var3, int var4) {
      this.source = var1;
      this.squares = var2;
      this.x0 = var3;
      this.y0 = var4;
   }

   void run() {
      for (int var1 = 0; var1 < 2; var1++) {
         for (int var2 = 0; var2 < 2; var2++) {
            for (int var3 = 0; var3 < 8; var3++) {
               for (int var4 = 0; var4 < 8; var4++) {
                  this.square(var1, var2, var3, var4);
               }
            }
         }
      }
   }

   WorldGen.Thing tree(int var1, int var2) {
      return first(this.squares[var1 + var2 * 16], 2);
   }

   private void square(int var1, int var2, int var3, int var4) {
      int var5 = var1 * 8 + var3;
      int var6 = var2 * 8 + var4;
      int var7 = this.x0 + var5;
      int var8 = this.y0 + var6;
      ArrayList var9 = this.squares[var5 + var6 * 16];
      WorldGen.Thing var10 = var9 == null ? null : first(var9, 1);
      WorldGen.Biome var11 = var10 == null ? null : this.source.biome(var7, var8);
      if (var11 != null) {
         int var12 = (this.x0 >> 3) + var1;
         int var13 = (this.y0 >> 3) + var2;
         Random var14 = this.squareRandom;
         var14.setSeed(this.source.seed(var12 * 8 + var7, var13 * 8 + var8));
         Arrays.fill(this.override, null);
         this.pending(var11, var9, var5, var6, var12, var13, var7, var8);
         WorldGen.Thing[] var15 = new WorldGen.Thing[]{null, first(var9, 8), first(var9, 4), first(var9, 2), null};
         if (this.trace != null) {
            this.trace.accept("square " + var7 + "," + var8 + " " + var11.name + " " + var9 + " override " + Arrays.toString(this.override));
         }

         for (int var16 = 1; var16 <= 3; var16++) {
            WorldGen.Thing var17 = var15[var16];
            if (var17 != null && !this.replace(var11, var16, var17, var9, var10, var5, var6, var3, var4, var12, var13, var7, var8, var14)) {
               return;
            }
         }
      }
   }

   private boolean replace(
      WorldGen.Biome var1,
      int var2,
      WorldGen.Thing var3,
      ArrayList<WorldGen.Thing> var4,
      WorldGen.Thing var5,
      int var6,
      int var7,
      int var8,
      int var9,
      int var10,
      int var11,
      int var12,
      int var13,
      Random var14
   ) {
      WorldGen.Biome var15 = this.override[var2] != null ? this.override[var2] : var1;
      if (var15.protect != null && var15.protect.matchesAny(var3.name())) {
         return true;
      }

      WorldGen.Patterns var16 = var15.placements[var2];
      if (var16 != null && var16.places(var5.name())) {
         WorldGen.Result var17 = this.apply(var15, var2, var4, var6, var7, var8, var9, var14);
         if (var17.outcome == 1) {
            this.pending(var1, var4, var6, var7, var10, var11, var12, var13);
            var15 = this.override[var2] != null ? this.override[var2] : var1;
            var17 = this.apply(var15, var2, var4, var6, var7, var8, var9, var14);
         }

         if (this.trace != null) {
            this.trace.accept("  type " + var2 + " by " + var15.name + ": " + var17);
         }

         if (var17.outcome == 0) {
            for (int var18 = 1; var18 < var4.size(); var18++) {
               WorldGen.Thing var19 = (WorldGen.Thing)var4.get(var18);
               if (!var19.name().equals(var17.placed) && (var19.kind() & 22) != 0) {
                  var4.remove(var18);
               }
            }
         } else if (var17.outcome == 2) {
            for (int var20 = 1; var20 < var4.size(); var20++) {
               if ((((WorldGen.Thing)var4.get(var20)).kind() & 54) != 0) {
                  var4.remove(var20);
               }
            }
         }

         return true;
      } else {
         remove(var4, var3);
         return true;
      }
   }

   private WorldGen.Result apply(WorldGen.Biome var1, int var2, ArrayList<WorldGen.Thing> var3, int var4, int var5, int var6, int var7, Random var8) {
      for (int var9 = 0; var9 < 16; var9++) {
         int var10 = 8 >> var9;
         if (var10 == 0) {
            return new WorldGen.Result(3, null);
         }

         WorldGen.Lookup var11 = this.tile(var1, var2, var4, var5, var6, var7, var8, var10, 0);
         if (var11.tile == null && (var11.reason == 1 || var11.reason == 2)) {
            return new WorldGen.Result(2, null);
         }

         if (var11.tile != null) {
            String var12 = var11.tile.name();
            if (!var12.equals("$any") && !var12.equals("$subbiome") && !var12.equals("$no_bush") && !var12.equals("$no_grass") && !var12.equals("$no_tree")) {
               var3.add(var11.tile);
               var8.nextFloat();
               return new WorldGen.Result(0, var12);
            }

            return new WorldGen.Result(1, null);
         }
      }

      return new WorldGen.Result(3, null);
   }

   private WorldGen.Lookup tile(WorldGen.Biome var1, int var2, int var3, int var4, int var5, int var6, Random var7, int var8, int var9) {
      int var10 = var3 + var4 * 16;
      String var11 = this.pending[var2][var10];
      if (var11 != null && !var11.isEmpty() && !var11.equals("$any") && !var11.equals("$subbiome")) {
         return new WorldGen.Lookup(0, this.pendingThing(var2, var10));
      }

      WorldGen.Feature[] var12 = var1.features == null ? null : var1.features[var2];
      if (var12 == null) {
         return new WorldGen.Lookup(3, null);
      }

      if (var12.length == 0) {
         return new WorldGen.Lookup(1, null);
      }

      WorldGen.Feature var13 = feature(var12, var8, var7);
      if (var13 == null) {
         return new WorldGen.Lookup(2, null);
      }

      ArrayList var14 = new ArrayList();

      for (WorldGen.Group var18 : var13.groups()) {
         if (var18.sx() <= var8 && var18.sy() <= var8) {
            var14.add(var18);
         }
      }

      if (var14.isEmpty()) {
         return new WorldGen.Lookup(3, null);
      }

      WorldGen.Group var19 = (WorldGen.Group)var14.get(var7.nextInt(var14.size()));
      if (var3 + var19.sx() - 1 < 16 && var4 + var19.sy() - 1 < 16 && (var1.placements[var2] == null || this.fits(var19, var5, var6, var1.placements[var2]))) {
         for (int var20 = 0; var20 < var19.sx(); var20++) {
            for (int var23 = 0; var23 < var19.sy(); var23++) {
               String var25 = this.pending[var2][var3 + var20 + (var4 + var23) * 16];
               if (var25 != null && !var25.startsWith("$")) {
                  return new WorldGen.Lookup(3, null);
               }
            }
         }

         for (int var21 = 0; var21 < var19.sx(); var21++) {
            for (int var24 = 0; var24 < var19.sy(); var24++) {
               this.pending[var2][var3 + var21 + (var4 + var24) * 16] = var19.tiles()[var21 + var24 * var19.sx()].name();
               this.pendingThings[var2][var3 + var21 + (var4 + var24) * 16] = var19.tiles()[var21 + var24 * var19.sx()];
            }
         }

         String var22 = this.pending[var2][var10];
         if ("$any".equals(var22)) {
            this.pending[var2][var10] = null;
            if (var9 < 1) {
               return this.tile(var1, var2, var3, var4, var5, var6, var7, var8, var9 + 1);
            }
         }

         return new WorldGen.Lookup(0, this.thing(var2, var10, var22));
      } else {
         return new WorldGen.Lookup(3, null);
      }
   }

   private WorldGen.Thing pendingThing(int var1, int var2) {
      return this.thing(var1, var2, this.pending[var1][var2]);
   }

   private WorldGen.Thing thing(int var1, int var2, String var3) {
      WorldGen.Thing var4 = this.pendingThings[var1][var2];
      return var4 != null && var4.name().equals(var3) ? var4 : new WorldGen.Thing(var3, 0, null);
   }

   private static WorldGen.Feature feature(WorldGen.Feature[] var0, int var1, Random var2) {
      float var3 = 0.0F;
      float var4 = 0.0F;
      boolean var5 = false;

      for (WorldGen.Feature var9 : var0) {
         var3 += var9.p();
      }

      for (WorldGen.Feature var18 : var0) {
         if (var18.minSize() <= var1) {
            var4 += var18.p();
            var5 = true;
         }
      }

      if (!var5) {
         return null;
      }

      float var13 = var2.nextFloat();
      float var15 = 0.0F;

      for (WorldGen.Feature var11 : var0) {
         if (var11.minSize() <= var1) {
            var15 += var11.p() / var4 * var3;
            if (var13 < var15) {
               return var11;
            }
         }
      }

      return null;
   }

   private boolean fits(WorldGen.Group var1, int var2, int var3, WorldGen.Patterns var4) {
      if (var1.sx() == 1 && var1.sy() == 1) {
         return true;
      }

      for (int var5 = 0; var5 < var1.sx(); var5++) {
         for (int var6 = 0; var6 < var1.sy(); var6++) {
            if (var5 != 0 || var6 != 0) {
               int var7 = var2 + var5;
               int var8 = var3 + var6;
               ArrayList var9 = var7 < 16 && var8 < 16 ? this.squares[var7 + var8 * 16] : null;
               WorldGen.Thing var10 = var9 == null ? null : first(var9, 1);
               if (var10 == null || humanStructure(var9) || !var4.places(var10.name())) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private static boolean humanStructure(ArrayList<WorldGen.Thing> var0) {
      byte var1 = 0;
      boolean var2 = false;

      for (WorldGen.Thing var4 : var0) {
         var2 |= (var4.kind() & 2) != 0;
         var1 += (var4.kind() & 52) == 0 ? 1 : 0;
      }

      return var1 > 1 && !var2;
   }

   private void pending(WorldGen.Biome var1, ArrayList<WorldGen.Thing> var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      int var9 = var3 + var4 * 16;

      for (int var10 = 0; var10 < 5; var10++) {
         String var11 = this.pending[var10][var9];
         if (var11 != null && !var11.equals("$any")) {
            WorldGen.Thing var12 = first(var2, 2);
            WorldGen.Thing var13 = first(var2, 4);
            WorldGen.Thing var14 = first(var2, 8);
            switch (var11) {
               case "$subbiome":
                  this.subBiome(var1, var10, var5, var6, var7, var8);
                  break;
               case "$no_tree":
                  remove(var2, var12);
                  break;
               case "$no_bush":
                  remove(var2, var13);
                  break;
               case "$no_grass":
                  remove(var2, var14);
                  break;
               default:
                  remove(var2, var12);
                  remove(var2, var13);
                  remove(var2, var14);
                  var2.add(this.pendingThing(var10, var9));
            }
         } else {
            this.pending[var10][var9] = null;
         }
      }
   }

   private void subBiome(WorldGen.Biome var1, int var2, int var3, int var4, int var5, int var6) {
      if (var2 != 4 && var1.subs != null && var1.subCount != 0) {
         for (int var7 = 0; var7 < 5; var7++) {
            if (var1.subs[var7] != null) {
               WorldGen.Biome[] var8 = var1.subs[var7][var2];
               this.subRandom.setSeed(this.source.seed(var3 * 8 + var5, var4 * 8 + var6));
               int var9 = this.subRandom.nextInt(0, var1.subCount);
               this.override[var7] = var8 != null && var9 < var8.length ? var8[var9] : null;
               return;
            }
         }
      }
   }

   private static void remove(ArrayList<WorldGen.Thing> var0, WorldGen.Thing var1) {
      for (int var2 = 0; var2 < var0.size(); var2++) {
         if (var0.get(var2) == var1) {
            var0.remove(var2);
            return;
         }
      }
   }

   private static WorldGen.Thing first(ArrayList<WorldGen.Thing> var0, int var1) {
      for (WorldGen.Thing var3 : var0) {
         if ((var3.kind() & var1) != 0) {
            return var3;
         }
      }

      return null;
   }

   static final class Biome {
      final String name;
      final WorldGen.Feature[][] features;
      final WorldGen.Patterns[] placements;
      final WorldGen.Patterns protect;
      WorldGen.Biome[][][] subs;
      int subCount;

      Biome(String var1, WorldGen.Feature[][] var2, WorldGen.Patterns[] var3, WorldGen.Patterns var4) {
         this.name = var1;
         this.features = var2;
         this.placements = var3;
         this.protect = var4;
      }
   }

   record Feature(float p, int minSize, WorldGen.Group[] groups) {
   }

   record Group(int sx, int sy, WorldGen.Thing[] tiles) {
   }

   private record Lookup(int reason, WorldGen.Thing tile) {
   }

   static final class Patterns {
      private final Pattern[] patterns;
      private final boolean[] allow;
      private final ConcurrentHashMap<String, Boolean> placeable = new ConcurrentHashMap<>();
      private final ConcurrentHashMap<String, Boolean> matched = new ConcurrentHashMap<>();

      Patterns(List<String> var1) {
         this.patterns = new Pattern[var1.size()];
         this.allow = new boolean[var1.size()];

         for (int var2 = 0; var2 < this.patterns.length; var2++) {
            String var3 = (String)var1.get(var2);
            this.allow[var2] = !var3.startsWith("!");
            String var4 = this.allow[var2] ? var3 : var3.substring(1);
            this.patterns[var2] = Pattern.compile(var4.replace(".", "\\.").replace("*", ".*").replace("?", ".?"));
         }
      }

      boolean places(String var1) {
         return this.placeable.computeIfAbsent(var1, var1x -> {
            boolean var2 = false;

            for (int var3 = 0; var3 < this.patterns.length; var3++) {
               if (this.patterns[var3].matcher(var1x).matches()) {
                  var2 = this.allow[var3];
               }
            }

            return var2;
         });
      }

      boolean matchesAny(String var1) {
         return this.matched.computeIfAbsent(var1, var1x -> {
            for (Pattern var5 : this.patterns) {
               if (var5.matcher(var1x).matches()) {
                  return true;
               }
            }

            return false;
         });
      }
   }

   private record Result(int outcome, String placed) {
   }

   interface Source {
      WorldGen.Biome biome(int var1, int var2);

      long seed(int var1, int var2);
   }

   record Thing(String name, int kind, FarTiles.Tile tile) {
   }
}
