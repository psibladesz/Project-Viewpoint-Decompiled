package viewpoint.far;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import zombie.ZomboidFileSystem;
import zombie.iso.IsoChunk;
import zombie.iso.IsoChunkMap;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.iso.worldgen.WorldGenChunk;
import zombie.iso.worldgen.WorldGenParams;
import zombie.iso.worldgen.biomes.Feature;
import zombie.iso.worldgen.biomes.IBiome;
import zombie.iso.worldgen.biomes.TileGroup;
import zombie.iso.worldgen.maps.BiomeMap;
import zombie.iso.worldgen.maps.BiomeMapEntry;

final class TreeDump {
   private static final String FILE = "viewpoint-tree-dump.txt";
   private static final int VALUES = 256;

   static void write(IsoChunkMap var0, int var1, int var2, int var3) throws IOException {
      File var4 = new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-tree-dump.txt");

      try (PrintWriter var5 = new PrintWriter(var4, StandardCharsets.UTF_8)) {
         var5.println("seed " + WorldGenParams.INSTANCE.getSeed());
         Erosion var6 = Erosion.current();
         if (var6 != null) {
            var6.dump(var5);
         }

         biomes(var5);

         for (int var7 = var2 - var3; var7 <= var2 + var3; var7++) {
            for (int var8 = var1 - var3; var8 <= var1 + var3; var8++) {
               IsoChunk var9 = var0.getChunkForGridSquare(var8 * 8, var7 * 8);
               if (var9 != null && var9.loaded) {
                  squares(var5, var9, var8, var7);
               }
            }
         }
      }

      System.out.println("[Viewpoint] far tree check: ground truth written to " + var4);
   }

   private static void biomes(PrintWriter var0) {
      BiomeMap var1 = IsoWorld.instance.getBiomeMap();
      WorldGenChunk var2 = IsoWorld.instance.getWgChunk();
      ArrayDeque<IBiome> var3 = new ArrayDeque<>();
      HashSet<String> var4 = new HashSet<>();

      for (int var5 = 0; var5 < 256; var5++) {
         BiomeMapEntry var6 = var1.getEntry(var5);
         if (var6 != null) {
            var0.println("value " + var5 + " " + var6.biome() + " " + var6.ore() + " " + var6.zone());

            for (String var10 : new String[]{var6.biome(), var6.ore()}) {
               IBiome var11 = var10 != null && !var10.startsWith("$") ? var2.getMapBiome(0, 0, var10) : null;
               if (var11 != null && var4.add(var11.name())) {
                  var3.add(var11);
               }
            }
         }
      }

      while (!var3.isEmpty()) {
         IBiome var14 = (IBiome)var3.poll();
         biome(var0, var14);
         var var15 = var14.subBiomes();
         if (var15 == null) {
            continue;
         }

         for (var var17 : var15.entrySet()) {
            for (var var19 : var17.getValue().entrySet()) {
               StringBuilder var20 = new StringBuilder(" sub " + var17.getKey() + " " + var19.getKey());

               for (IBiome var13 : var19.getValue()) {
                  var20.append(' ').append(var13.name());
                  if (var4.add(var13.name())) {
                     var3.add(var13);
                  }
               }

               var0.println(var20);
            }
         }
      }
   }

   private static void biome(PrintWriter var0, IBiome var1) {
      var0.println("biome " + var1.name() + " parent " + var1.parent() + " zombies " + var1.zombies());
      var var2 = var1.placements();
      if (var2 != null) {
         for (var var4 : var2.entrySet()) {
            var0.println(" place " + var4.getKey() + " " + String.join(" ", var4.getValue()));
         }
      }

      var var10 = var1.getFeatures();
      if (var10 == null) {
         return;
      }

      for (var var5 : var10.entrySet()) {
         for (Feature var7 : var5.getValue()) {
            var0.println(" feature " + var5.getKey() + " " + var7.probability().getValue() + " " + var7.minSize() + " " + var7.maxSize());

            for (TileGroup var9 : var7.tileGroups()) {
               var0.println("  group " + var9.sx() + " " + var9.sy() + " " + String.join(" ", var9.tiles()));
            }
         }
      }
   }

   private static void squares(PrintWriter var0, IsoChunk var1, int var2, int var3) {
      for (int var4 = 0; var4 < 8; var4++) {
         for (int var5 = 0; var5 < 8; var5++) {
            IsoGridSquare var6 = var1.getGridSquare(var5, var4, 0);
            StringBuilder var7 = new StringBuilder("square " + (var2 * 8 + var5) + " " + (var3 * 8 + var4));

            for (int var8 = 0; var6 != null && var8 < var6.getObjects().size(); var8++) {
               IsoObject var9 = (IsoObject)var6.getObjects().get(var8);
               var7.append(' ').append(var9.getSprite() == null ? "-" : var9.getSprite().getName());
            }

            var0.println(var7);
         }
      }
   }

   private TreeDump() {
   }
}