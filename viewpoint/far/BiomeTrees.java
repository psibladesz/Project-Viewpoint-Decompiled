package viewpoint.far;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import zombie.core.properties.PropertyContainer;
import zombie.iso.IsoWorld;
import zombie.iso.LotHeader;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteManager;
import zombie.iso.worldgen.WorldGenChunk;
import zombie.iso.worldgen.WorldGenParams;
import zombie.iso.worldgen.biomes.Feature;
import zombie.iso.worldgen.biomes.FeatureType;
import zombie.iso.worldgen.biomes.IBiome;
import zombie.iso.worldgen.biomes.TileGroup;
import zombie.iso.worldgen.maps.BiomeMap;
import zombie.iso.worldgen.maps.BiomeMapEntry;

final class BiomeTrees {
   private static final int VALUES = 256;
   private static final String RANDOM = "$random";
   private static BiomeTrees.Table table;
   private static final IdentityHashMap<LotHeader, BiomeTrees.Legend> legends = new IdentityHashMap<>();
   private static final HashMap<String, Integer> kinds = new HashMap<>();

   static byte[] image(String var0, int var1, int var2) {
      File var3 = new File(var0, "maps/biomemap_" + var1 + "_" + var2 + ".png");
      if (!var3.isFile()) {
         return null;
      }

      try {
         BufferedImage var4 = ImageIO.read(var3);
         if (var4 != null && var4.getWidth() == 256 && var4.getHeight() == 256) {
            WritableRaster var5 = var4.getRaster();
            IndexColorModel var6 = var4.getColorModel() instanceof IndexColorModel var7 ? var7 : null;
            byte[] var12 = new byte[65536];
            int[] var13 = new int[256];

            for (int var9 = 0; var9 < 256; var9++) {
               var5.getSamples(0, var9, 256, 1, 0, var13);

               for (int var10 = 0; var10 < 256; var10++) {
                  var12[var9 * 256 + var10] = (byte)(var6 == null ? var13[var10] : var6.getRed(var13[var10]));
               }
            }

            return var12;
         } else {
            return null;
         }
      } catch (Exception var11) {
         return null;
      }
   }

   static BiomeTrees.Table table() {
      Erosion var0 = Erosion.current();
      if (table != null) {
         if (table.erosion != var0) {
            table = new BiomeTrees.Table(table.biomes, table.seed, var0);
         }

         return table;
      } else {
         IsoWorld var1 = IsoWorld.instance;
         BiomeMap var2 = var1 == null ? null : var1.getBiomeMap();
         WorldGenChunk var3 = var1 == null ? null : var1.getWgChunk();
         if (var2 != null && var3 != null) {
            WorldGen.Biome[] var4 = new WorldGen.Biome[256];
            IdentityHashMap var5 = new IdentityHashMap();

            for (int var6 = 0; var6 < 256; var6++) {
               var4[var6] = biome(var2, var3, var6, var5);
            }

            table = new BiomeTrees.Table(var4, WorldGenParams.INSTANCE.getSeed(), var0);
            return table;
         } else {
            return new BiomeTrees.Table(new WorldGen.Biome[256], 0L, null);
         }
      }
   }

   static BiomeTrees.Legend legend(LotHeader var0) {
      BiomeTrees.Legend var1 = legends.get(var0);
      if (var1 == null) {
         String[] var2 = var0.tilesUsed.toArray(new String[0]);
         int[] var3 = new int[var2.length];

         for (int var4 = 0; var4 < var2.length; var4++) {
            var3[var4] = kind(var2[var4]);
         }

         var1 = new BiomeTrees.Legend(var2, var3);
         legends.put(var0, var1);
      }

      return var1;
   }

   static void forget() {
      table = null;
      legends.clear();
      Erosion.forget();
   }

   static int kind(String var0) {
      Integer var1 = kinds.get(var0);
      if (var1 != null) {
         return var1;
      }

      IsoSprite var2 = var0 == null ? null : (IsoSprite)IsoSpriteManager.instance.getNamedMap().get(var0);
      int var3;
      if (var2 != null && !var2.getProperties().has(IsoFlagType.FloorOverlay)) {
         PropertyContainer var4 = var2.getProperties();
         boolean var5 = var0.startsWith("e_newgrass_") || var0.startsWith("blends_grassoverlays_");
         boolean var6 = var5
                 || var0.startsWith("d_plants_")
                 || var0.startsWith("d_generic_1_")
                 || var0.startsWith("d_floorleaves_")
                 || var0.startsWith("vegetation_groundcover_");
         var3 = (var4.has(IsoFlagType.solidfloor) ? 1 : 0)
                 | (var2.getTileType() == IsoObjectType.tree ? 2 : 0)
                 | (!"f_bushes_1".equals(var2.tilesetName) && var4.get("Bush") == null ? 0 : 4)
                 | (var5 ? 8 : 0)
                 | (var6 ? 16 : 0)
                 | (!var0.startsWith("boulders_") && !var0.startsWith("crafting_ore_") ? 0 : 32);
      } else {
         var3 = -1;
      }

      kinds.put(var0, var3);
      return var3;
   }

   private static WorldGen.Biome biome(BiomeMap var0, WorldGenChunk var1, int var2, IdentityHashMap<IBiome, WorldGen.Biome> var3) {
      try {
         BiomeMapEntry var4 = var0.getEntry(var2);
         String var5 = var4 == null ? null : var4.biome();
         if (var5 != null && !"$random".equals(var5)) {
            IBiome var6 = var1.getMapBiome(0, 0, var5);
            return var6 == null ? null : copy(var6, var3);
         } else {
            return null;
         }
      } catch (RuntimeException var7) {
         return null;
      }
   }

   private static WorldGen.Biome copy(IBiome var0, IdentityHashMap<IBiome, WorldGen.Biome> var1) {
      WorldGen.Biome var2 = (WorldGen.Biome)var1.get(var0);
      if (var2 != null) {
         return var2;
      }

      Map var3 = var0.getFeatures();
      Map var4 = var0.placements();
      WorldGen.Feature[][] var5 = var3 == null ? null : new WorldGen.Feature[5][];
      WorldGen.Patterns[] var6 = new WorldGen.Patterns[5];

      for (FeatureType var10 : FeatureType.values()) {
         int var11 = var10.ordinal();
         List var12 = var3 == null ? null : (List)var3.get(var10);
         if (var12 != null) {
            var5[var11] = new WorldGen.Feature[var12.size()];

            for (int var13 = 0; var13 < var12.size(); var13++) {
               var5[var11][var13] = feature((Feature)var12.get(var13));
            }
         }

         if (var4 != null && var4.get(var10) != null) {
            var6[var11] = new WorldGen.Patterns((List<String>)var4.get(var10));
         }
      }

      List var15 = var0.protectedList();
      var2 = new WorldGen.Biome(var0.name(), var5, var6, var15 == null ? null : new WorldGen.Patterns(var15));
      var1.put(var0, var2);
      subs(var0, var2, var1);
      return var2;
   }

   private static void subs(IBiome var0, WorldGen.Biome var1, IdentityHashMap<IBiome, WorldGen.Biome> var2) {
      Map var3 = var0.subBiomes();
      if (var3 != null && !var3.isEmpty()) {
         var1.subs = new WorldGen.Biome[5][][];
         var1.subCount = var3.size();

         for (Object o1 : var3.entrySet()) {
            Entry<?, ?> var5 = (Entry<?, ?>)o1;
            WorldGen.Biome[][] var6 = new WorldGen.Biome[5][];

            for (Object o2 : ((Map<?, ?>)var5.getValue()).entrySet()) {
               Entry<?, ?> var8 = (Entry<?, ?>)o2;
               List<?> var9 = (List<?>)var8.getValue();
               var6[((FeatureType)var8.getKey()).ordinal()] = new WorldGen.Biome[var9.size()];

               for (int var10 = 0; var10 < var9.size(); var10++) {
                  var6[((FeatureType)var8.getKey()).ordinal()][var10] = copy((IBiome)var9.get(var10), var2);
               }
            }

            var1.subs[((FeatureType)var5.getKey()).ordinal()] = var6;
         }
      }
   }

   private static WorldGen.Feature feature(Feature var0) {
      WorldGen.Group[] var1 = new WorldGen.Group[var0.tileGroups().size()];

      for (int var2 = 0; var2 < var1.length; var2++) {
         TileGroup var3 = (TileGroup)var0.tileGroups().get(var2);
         WorldGen.Thing[] var4 = new WorldGen.Thing[var3.tiles().size()];

         for (int var5 = 0; var5 < var4.length; var5++) {
            String var6 = (String)var3.tiles().get(var5);
            boolean var7 = var6 != null && !var6.startsWith("$");
            var4[var5] = new WorldGen.Thing(var6, var7 ? Math.max(kind(var6), 0) : 0, var7 ? FarTiles.of(var6) : null);
         }

         var1[var2] = new WorldGen.Group(var3.sx(), var3.sy(), var4);
      }

      return new WorldGen.Feature(var0.probability().getValue(), var0.minSize(), var1);
   }

   private BiomeTrees() {
   }

   static final class Ground {
      private final int x0;
      private final int y0;
      private final int size;
      private final int[] head;
      private final int[] tail;
      private int[] next = new int[1024];
      private int[] index = new int[1024];
      private int count;

      Ground(int var1, int var2, int var3) {
         this.x0 = var1;
         this.y0 = var2;
         this.size = var3;
         this.head = new int[var3 * var3];
         this.tail = new int[var3 * var3];
         Arrays.fill(this.head, -1);
      }

      boolean add(int var1, FarTiles.Tile[] var2, int var3, int var4, int var5) {
         int var6 = var3 - this.x0;
         int var7 = var4 - this.y0;
         if (var5 == 0 && var6 >= 0 && var7 >= 0 && var6 < this.size && var7 < this.size) {
            if (this.count == this.next.length) {
               this.next = Arrays.copyOf(this.next, this.count * 2);
               this.index = Arrays.copyOf(this.index, this.count * 2);
            }

            int var8 = var7 * this.size + var6;
            this.next[this.count] = -1;
            this.index[this.count] = var1;
            if (this.head[var8] < 0) {
               this.head[var8] = this.count;
            } else {
               this.next[this.tail[var8]] = this.count;
            }

            this.tail[var8] = this.count++;
            return var1 < var2.length && (var2[var1].flags & 8) != 0;
         } else {
            return false;
         }
      }

      void grow(
              BiomeTrees.Table var1,
              byte[] var2,
              int var3,
              int var4,
              FarTiles.Tile[] var5,
              BiomeTrees.Legend var6,
              byte[][] var7,
              BiomeTrees.Grown var8,
              BiomeTrees.Grown var9
      ) {
         BiomeTrees.Table var10 = var1 == null ? null : var1.forCell(var2, var3, var4);

         for (int var11 = 0; var11 < this.size; var11 += 16) {
            for (int var12 = 0; var12 < this.size; var12 += 16) {
               this.block(var10, var12, var11, var3, var4, var5, var6, var7, var8, var9);
            }
         }
      }

      private void block(
              BiomeTrees.Table var1,
              int var2,
              int var3,
              int var4,
              int var5,
              FarTiles.Tile[] var6,
              BiomeTrees.Legend var7,
              byte[][] var8,
              BiomeTrees.Grown var9,
              BiomeTrees.Grown var10
      ) {
         int var11 = var4 * 256 + this.x0 + var2;
         int var12 = var5 * 256 + this.y0 + var3;
         ArrayList[] var13 = new ArrayList[256];

         for (int var14 = 0; var14 < 16; var14++) {
            for (int var15 = 0; var15 < 16; var15++) {
               var13[var15 + var14 * 16] = this.objects((var3 + var14) * this.size + var2 + var15, var6, var7);
            }
         }

         WorldGen var22 = var1 == null ? null : new WorldGen(var1, var13, var11, var12);
         if (var22 != null) {
            var22.run();
         }

         Erosion var23 = var1 == null ? null : var1.erosion;
         byte[][] var16 = var10 == null ? null : var8;

         for (int var17 = 0; var17 < 16; var17++) {
            for (int var18 = 0; var18 < 16; var18++) {
               WorldGen.Thing var19 = var22 != null ? var22.tree(var18, var17) : first(var13[var18 + var17 * 16]);
               FarTiles.Tile var20 = var19 == null ? null : var19.tile();
               int var21 = var23 == null ? -1 : var23.square(var1, var13[var18 + var17 * 16], var11 + var18, var12 + var17, var16);
               if (var21 != -1) {
                  var20 = var23.tile(var21);
               }

               if (var20 != null && var20.treeKind >= 0) {
                  if (var1 != null && var8 != null) {
                     var20 = var20.inSeason(var8[0][var1.turn(var11 + var18, var12 + var17)]);
                  }

                  var9.put(var20, this.x0 + var2 + var18, this.y0 + var3 + var17);
               }
            }
         }

         if (var10 != null) {
            plants(var13, this.x0 + var2, this.y0 + var3, var10);
         }
      }

      private static void plants(ArrayList<WorldGen.Thing>[] var0, int var1, int var2, BiomeTrees.Grown var3) {
         for (int var4 = 0; var4 < var0.length; var4++) {
            for (WorldGen.Thing var6 : var0[var4]) {
               FarTiles.Tile var7 = var6.tile();
               if (var7 != null && var7.shell == 5) {
                  var3.put(var7, var1 + var4 % 16, var2 + var4 / 16);
               }
            }
         }
      }

      private ArrayList<WorldGen.Thing> objects(int var1, FarTiles.Tile[] var2, BiomeTrees.Legend var3) {
         ArrayList<WorldGen.Thing> var4 = new ArrayList<>(4);
         String var5 = null;

         for (int var6 = this.head[var1]; var6 >= 0; var6 = this.next[var6]) {
            int var7 = this.index[var6];
            if (var3 != null && var7 < var3.kinds.length && var3.kinds[var7] >= 0) {
               String var8 = var3.names[var7];
               int var9 = var3.kinds[var7];
               if (!var8.startsWith("vegetation_trees") || var5 != null && var5.startsWith("blends_natural")) {
                  if (var5 == null && (var9 & 1) != 0) {
                     var5 = var8;
                  }

                  var4.add(new WorldGen.Thing(var8, var9, var7 < var2.length ? var2[var7] : null));
               }
            }
         }

         return var4;
      }

      private static WorldGen.Thing first(ArrayList<WorldGen.Thing> var0) {
         for (WorldGen.Thing var2 : var0) {
            if ((var2.kind() & 2) != 0) {
               return var2;
            }
         }

         return null;
      }
   }

   interface Grown {
      void put(FarTiles.Tile var1, int var2, int var3);
   }

   record Legend(String[] names, int[] kinds) {
   }

   static final class Table implements WorldGen.Source {
      final WorldGen.Biome[] biomes;
      final Erosion erosion;
      private final long seed;
      private final long mixX;
      private final long mixY;
      private byte[] values;
      private int cellX0;
      private int cellY0;

      Table(WorldGen.Biome[] var1, long var2, Erosion var4) {
         this.biomes = var1;
         this.seed = var2;
         this.erosion = var4;
         Random var5 = new Random(var2);
         this.mixX = var5.nextLong();
         this.mixY = var5.nextLong();
      }

      BiomeTrees.Table forCell(byte[] var1, int var2, int var3) {
         BiomeTrees.Table var4 = new BiomeTrees.Table(this.biomes, this.seed, this.erosion);
         var4.values = var1;
         var4.cellX0 = var2 * 256;
         var4.cellY0 = var3 * 256;
         return var4;
      }

      @Override
      public WorldGen.Biome biome(int var1, int var2) {
         return this.values == null ? null : this.biomes[this.values[(var2 - this.cellY0) * 256 + var1 - this.cellX0] & 0xFF];
      }

      @Override
      public long seed(int var1, int var2) {
         return var1 * this.mixX ^ var2 * this.mixY ^ this.seed;
      }

      int turn(int var1, int var2) {
         return WorldGen.generator(this.seed(var1, var2)).nextInt(100);
      }
   }
}