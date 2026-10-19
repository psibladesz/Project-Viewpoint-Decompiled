package viewpoint.far;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map.Entry;
import viewpoint.render.TreeKinds;
import viewpoint.world.TileMesh;
import viewpoint.world.TileMeshes;
import viewpoint.world.WorldMesher;
import zombie.core.textures.Texture;
import zombie.erosion.ErosionMain;
import zombie.erosion.season.ErosionSeason;
import zombie.iso.IsoDirections;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteManager;

final class TreeSeasons {
   static final int TURNS = 100;
   static final int DISPLAYS = 5;
   static final int TREES = 0;
   static final int BUSHES = 1;
   static final int GRASS = 2;
   static final int TABLES = 3;
   private static final int SUMMER = 2;
   private static final String[] EVERGREENS = new String[]{"e_americanholly", "e_canadianhemlock", "e_virginiapine"};
   private static final int[][] FIRST_DISPLAY = new int[][]{{0, 1, 2, 0, 4, 0}, {0, 1, 2, 0, 4, 0}, {0, 1, 2, 0, 4, 5}};
   private static final int[][] SECOND_DISPLAY = new int[][]{{-1, -1, 3, -1, 0, -1}, {-1, -1, 2, -1, 0, -1}, {-1, -1, 3, -1, 5, -1}};
   private static final int[] BEFORE = new int[]{2, 5, 1, 2, 2, 4};
   private static int shown = -1;
   private static volatile byte[][] displays = new byte[3][100];
   private static final byte[][] next = new byte[3][100];
   private static final IdentityHashMap<FarTiles.Tile, Texture> seasonal = new IdentityHashMap<>();

   static boolean update() {
      ErosionMain var0 = ErosionMain.getInstance();
      ErosionSeason var1 = var0 == null ? null : var0.getSeasons();

      for (int var2 = 0; var2 < 3; var2++) {
         for (int var3 = 0; var3 < 100; var3++) {
            next[var2][var3] = (byte)(var1 == null ? 2 : display(var2, var1.getSeason(), var1.getSeasonDay(), var1.getSeasonDays(), var3 / 100.0F));
         }
      }

      byte var7 = next[0][50];
      if (var7 == shown && Arrays.deepEquals(next, displays)) {
         return false;
      }

      boolean var8 = var7 != shown;
      shown = var7;
      byte[][] var4 = new byte[3][];

      for (int var5 = 0; var5 < 3; var5++) {
         var4[var5] = (byte[])next[var5].clone();
      }

      displays = var4;
      TreeKinds.displays(var4[0]);

      for (Entry var6 : seasonal.entrySet()) {
         if (var8) {
            ((FarTiles.Tile)var6.getKey()).leaves = leaves(((FarTiles.Tile)var6.getKey()).tree, shown);
         }

         variants((FarTiles.Tile)var6.getKey(), (Texture)var6.getValue());
      }

      if (var8) {
         FarTrees.seasonChanged();
      }

      return true;
   }

   static byte[][] displays() {
      return displays;
   }

   static int display(int var0, float var1, float var2, float var3) {
      return display(0, var0, var1, var2, var3);
   }

   static int display(int var0, int var1, float var2, float var3, float var4) {
      int[] var5 = FIRST_DISPLAY[var0];
      int[] var6 = SECOND_DISPLAY[var0];
      float var7 = var3 / 2.0F;
      boolean var8 = var6[var1] >= 0;
      if (var8 && var2 >= var7 + var7 * var4) {
         return var6[var1];
      }

      if (var2 >= (var8 ? var7 : var3) * var4) {
         return var5[var1];
      }

      int var9 = BEFORE[var1];
      return var6[var9] >= 0 ? var6[var9] : var5[var9];
   }

   static boolean seasonal(String var0) {
      return leafSheet(var0) != null;
   }

   static void add(FarTiles.Tile var0, Texture var1) {
      var0.leaves = shown < 0 ? null : leaves(var0.tree, shown);
      seasonal.put(var0, var1);
      variants(var0, var1);
   }

   static void forget() {
      shown = -1;
   }

   private static void variants(FarTiles.Tile var0, Texture var1) {
      FarTiles.Tile[] var2 = var0.seasons;
      FarTiles.Tile[] var3 = var2 == null ? new FarTiles.Tile[5] : (FarTiles.Tile[])var2.clone();
      boolean var4 = false;

      for (int var8 : displays[0]) {
         if (var8 != shown && var3[var8] == null && shown >= 0) {
            FarTiles.Tile var9 = new FarTiles.Tile(
               var0.kind, var0.height, var0.colour, var0.shell, var0.page, var0.map, var0.mesh, var0.north, var0.west, var0.flags, null
            );
            var9.leaves = leaves(var0.tree, var8);
            var9.card = var0.card;
            if (var0.kind == 3) {
               FarTrees.add(var9, var1);
            }

            var3[var8] = var9;
            var4 = true;
         }
      }

      if (var4) {
         var0.seasons = var3;
      }
   }

   private static String[] leafSheet(String var0) {
      if (var0 != null && var0.startsWith("e_") && !var0.startsWith("e_newgrass_1_")) {
         for (String var4 : EVERGREENS) {
            if (var0.startsWith(var4)) {
               return null;
            }
         }

         int var5 = var0.lastIndexOf(95);
         return var5 > 0 ? new String[]{var0.substring(0, var5), var0.substring(var5 + 1)} : null;
      } else {
         return null;
      }
   }

   private static FarTiles.Tile leaves(String var0, int var1) {
      String var2 = leavesName(var0, var1);
      return var2 == null ? null : leafTile(var2, false);
   }

   static String leavesName(String var0, int var1) {
      String[] var2 = leafSheet(var0);
      if (var2 != null && var1 > 0) {
         int var3;
         try {
            var3 = Integer.parseInt(var2[1]);
         } catch (NumberFormatException var7) {
            return null;
         }

         String var4 = var2[0];
         int var5 = var1 + 1;
         int var6 = var4.contains("JUMBOX") ? var5 : (var4.contains("JUMBO") ? var5 * 2 + var3 % 2 : (var3 < 4 ? var5 * 4 + var3 : -1));
         return var6 < 0 ? null : var4 + "_" + var6;
      } else {
         return null;
      }
   }

   static FarTiles.Tile leafTile(String var0, boolean var1) {
      IsoSprite var2 = (IsoSprite)IsoSpriteManager.instance.getNamedMap().get(var0);
      Texture var3 = var2 == null ? null : var2.getTextureForCurrentFrame(IsoDirections.N);
      if (var3 != null && var3.isReady() && var3.getTextureId() != null) {
         TileMesh var4 = var1 ? null : TileMeshes.crossed(var3, 2);
         if (var4 != null && var4.vertCount == 0) {
            return null;
         }

         FarTiles.Tile var5 = new FarTiles.Tile(
            (byte)(var1 ? 0 : 3),
            0.0F,
            FarColours.of(var0),
            (byte)(var1 ? 5 : 4),
            var3.getTextureId(),
            WorldMesher.textureMapping(var3),
            var4,
            (byte)0,
            (byte)0,
            (byte)(var1 ? 32 : 0),
            null
         );
         var5.card = var1 ? TileMeshes.plantCard(var3) : null;
         return var5;
      } else {
         return null;
      }
   }

   private TreeSeasons() {
   }
}
