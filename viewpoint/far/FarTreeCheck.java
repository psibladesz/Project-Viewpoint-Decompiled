package viewpoint.far;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map.Entry;
import viewpoint.core.View;
import viewpoint.platform.LiveSettings;
import zombie.characters.IsoPlayer;
import zombie.erosion.ErosionData.Square;
import zombie.erosion.categories.ErosionCategory.Data;
import zombie.iso.IsoChunk;
import zombie.iso.IsoChunkMap;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoWorld;
import zombie.iso.objects.IsoTree;

final class FarTreeCheck {
   private static final LiveSettings.Toggle RUN = LiveSettings.toggle(
      "debug.checkFarTrees", "Compare the far trees with the chunks' (console, once)", "Debug/Views", false
   );
   private static final int EXAMPLES = 12;
   private static final int REACH = 4;

   static void update() {
      if (RUN.get()) {
         RUN.set(false);

         try {
            run();
         } catch (IOException | RuntimeException var1) {
            System.out.println("[Viewpoint] far tree check failed: " + var1);
         }
      }
   }

   private static void run() throws IOException {
      IsoPlayer var0 = IsoPlayer.getInstance();
      IsoChunkMap var1 = IsoWorld.instance.getCell().getChunkMap(0);
      int var2 = View.radiusChunks();
      int var3 = Math.floorDiv((int)var0.getX(), 8);
      int var4 = Math.floorDiv((int)var0.getY(), 8);
      HashMap var5 = new HashMap();
      HashSet var6 = new HashSet();

      for (int var7 = var4 - var2; var7 <= var4 + var2; var7++) {
         for (int var8 = var3 - var2; var8 <= var3 + var2; var8++) {
            IsoChunk var9 = var1.getChunkForGridSquare(var8 * 8, var7 * 8);
            if (var9 != null && var9.loaded) {
               gameTrees(var9, var8, var7, var5, var6);
            }
         }
      }

      HashMap var10 = new HashMap();
      HashSet var11 = new HashSet();
      int[] var12 = farTrees(var3, var4, var2, var10, var11);
      report(var5, var6, var10, var11, var12);
      TreeDump.write(var1, var3, var4, var2);
   }

   private static void gameTrees(IsoChunk var0, int var1, int var2, HashMap<Long, FarTreeCheck.GameTree> var3, HashSet<Long> var4) {
      for (int var5 = 0; var5 < 8; var5++) {
         for (int var6 = 0; var6 < 8; var6++) {
            int var7 = var1 * 8 + var6;
            int var8 = var2 * 8 + var5;
            var4.add(FarWorld.key(var7, var8));
            IsoGridSquare var9 = var0.getGridSquare(var6, var5, 0);
            IsoTree var10 = var9 == null ? null : var9.getTree();
            if (var10 != null && var10.getSprite() != null && var10.getSprite().getName() != null) {
               Square var11 = var9.getErosionData();
               int var12 = -1;
               boolean var13 = false;

               for (int var14 = 0; var11 != null && var14 < var11.regions.size(); var14++) {
                  Data var15 = (Data)var11.regions.get(var14);
                  if (var15.getClass().getName().endsWith("NatureTrees$CategoryData")) {
                     var12 = var15.dispSeason;
                     var13 = var15.hasSpawned;
                  }
               }

               int var16 = var11 != null && var11.init ? Math.round(var11.magicNum * 100.0F) : -1;
               var3.put(FarWorld.key(var7, var8), new FarTreeCheck.GameTree(var10.getSprite().getName(), var16, var12, var13));
            }
         }
      }
   }

   private static int[] farTrees(int var0, int var1, int var2, HashMap<Long, FarTiles.Tile> var3, HashSet<Long> var4) throws IOException {
      byte var5 = 32;
      int var6 = 0;
      int var7 = 0;
      BiomeTrees.Table var8 = BiomeTrees.table();

      for (int var9 = Math.floorDiv(var1 - var2, var5); var9 <= Math.floorDiv(var1 + var2, var5); var9++) {
         for (int var10 = Math.floorDiv(var0 - var2, var5); var10 <= Math.floorDiv(var0 + var2, var5); var10++) {
            FarWorld.Slot var11 = FarWorld.readCell(var10, var9);
            if (var11 == null) {
               var7++;
            } else {
               var6++;
               int var12 = var10 * 256;
               int var13 = var9 * 256;
               FarTiles.Tile[] var14 = var11.tiles;
               BiomeTrees.Ground var15 = new BiomeTrees.Ground(0, 0, 256);
               byte[] var16 = Lotpacks.read(var11.lotpack, Lotpacks.get(var11.lotpack));
               ShellMesher.read(var16, var11.minLevel, var11.maxLevel, 0, 0, 32, (var5x, var6x, var7x, var8x) -> {
                  if (var15.add(var5x, var14, var6x, var7x, var8x)) {
                     var4.add(FarWorld.key(var12 + var6x, var13 + var7x));
                  }
               });
               int var17 = var10;
               int var18 = var9;
               var15.grow(
                  var8,
                  var11.biomes,
                  var17,
                  var18,
                  var14,
                  var11.legend,
                  null,
                  (var3x, var4x, var5x) -> var3.put(FarWorld.key(var12 + var4x, var13 + var5x), var3x),
                  null
               );
            }
         }
      }

      return new int[]{var6, var7};
   }

   private static void report(HashMap<Long, FarTreeCheck.GameTree> var0, HashSet<Long> var1, HashMap<Long, FarTiles.Tile> var2, HashSet<Long> var3, int[] var4) {
      IdentityHashMap var5 = new IdentityHashMap();

      for (Entry var7 : FarTiles.byName().entrySet()) {
         var5.putIfAbsent((FarTiles.Tile)var7.getValue(), (String)var7.getKey());
      }

      BiomeTrees.Table var23 = BiomeTrees.table();
      byte[] var24 = TreeSeasons.displays()[0];
      int var8 = 0;
      int var9 = 0;
      int var10 = 0;
      int var11 = 0;
      int var12 = 0;
      ArrayList var13 = new ArrayList();
      ArrayList var14 = new ArrayList();
      ArrayList var15 = new ArrayList();

      for (Entry var17 : var0.entrySet()) {
         int var18 = (int)((Long)var17.getKey() >> 32);
         int var19 = (int)((Long)var17.getKey()).longValue();
         FarTreeCheck.GameTree var20 = (FarTreeCheck.GameTree)var17.getValue();
         int var21 = var23.turn(var18, var19);
         if (var20.turn >= 0) {
            var9++;
            var10 += var20.turn == var21 ? 1 : 0;
         }

         if (var20.season >= 0) {
            var11++;
            var12 += var20.season == var24[var21] ? 1 : 0;
         }

         FarTiles.Tile var22 = (FarTiles.Tile)var2.get(var17.getKey());
         if (var22 == null) {
            var14.add(
               var18
                  + ","
                  + var19
                  + " "
                  + var20.name
                  + (var20.spawned ? " erosion-spawned" : "")
                  + (spotNear(var3, var18, var19) ? " (a spot within reach)" : " (no spot)")
            );
         } else if (var20.name.equals(var5.get(var22))) {
            var8++;
         } else {
            var13.add(var18 + "," + var19 + " game " + var20.name + ", far " + (String)var5.get(var22));
         }
      }

      for (Entry var26 : var2.entrySet()) {
         if (var1.contains(var26.getKey()) && !var0.containsKey(var26.getKey())) {
            var15.add((int)((Long)var26.getKey() >> 32) + "," + (int)((Long)var26.getKey()).longValue() + " " + (String)var5.get(var26.getValue()));
         }
      }

      System.out
         .println(
            "[Viewpoint] far tree check: "
               + var0.size()
               + " trees on the loaded ground, cells read "
               + var4[0]
               + " (not read "
               + var4[1]
               + "); same "
               + var8
               + ", other kind "
               + var13.size()
               + ", game only "
               + var14.size()
               + ", far only "
               + var15.size()
               + "; erosion turn right "
               + var10
               + " of "
               + var9
               + ", season right "
               + var12
               + " of "
               + var11
         );
      examples("other kind", var13);
      examples("game only", var14);
      examples("far only", var15);
   }

   private static boolean spotNear(HashSet<Long> var0, int var1, int var2) {
      for (int var3 = 0; var3 <= 4; var3++) {
         for (int var4 = 0; var4 <= 4; var4++) {
            if (var0.contains(FarWorld.key(var1 - var4, var2 - var3))) {
               return true;
            }
         }
      }

      return false;
   }

   private static void examples(String var0, ArrayList<String> var1) {
      for (int var2 = 0; var2 < Math.min(12, var1.size()); var2++) {
         System.out.println("[Viewpoint] far tree check, " + var0 + ": " + (String)var1.get(var2));
      }
   }

   private FarTreeCheck() {
   }

   private record GameTree(String name, int turn, int season, boolean spawned) {
   }
}
