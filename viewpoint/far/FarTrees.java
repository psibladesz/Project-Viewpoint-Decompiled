package viewpoint.far;

import java.util.ArrayList;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Tuning;
import viewpoint.render.TreeAtlas;
import viewpoint.render.TreeKinds;
import viewpoint.world.TileMeshes;
import viewpoint.world.WorldMesher;
import zombie.core.textures.Texture;

final class FarTrees {
   static final int CARDS = 2;
   static final LiveSettings.Number LITE_SHARE = LiveSettings.number(
      "lod.treeShare", "Trees kept past the full shell (share)", "World/Levels of detail", 0.0625F, 1.0F, 0.0625F, 0.5F
   );
   private static final int FOLIAGE = 3032358;
   private static final ArrayList<FarTrees.Kind> kinds = new ArrayList<>();

   static void add(FarTiles.Tile var0, Texture var1) {
      float var2 = WorldMesher.pixelScale(var1);
      float[] var3 = new float[]{var1.getWidthOrig() / var2, var1.getHeightOrig() / var2, TileMeshes.plantHalfWidth(var1), TileMeshes.plantHeight(var1)};
      var0.treeKind = kinds.size();
      FarTrees.Kind var4 = new FarTrees.Kind(var0, var3);
      kinds.add(var4);
      refresh(var4);
   }

   static void seasonChanged() {
      TreeAtlas.reset();

      for (FarTrees.Kind var1 : kinds) {
         refresh(var1);
      }
   }

   private static void refresh(FarTrees.Kind var0) {
      FarTiles.Tile var1 = var0.tile;
      FarTiles.Tile var2 = var1.leaves;
      var1.billboard = var1.page == null
         ? null
         : TreeAtlas.slot(var1.page, var1.map, var2 == null ? null : var2.page, var2 == null ? null : var2.map, var0.size);
      int var3 = var1.colour >= 0 ? var1.colour : 3032358;
      int var4 = var2 != null && var2.colour >= 0 ? var2.colour : var3;
      int[] var5 = new int[5];
      boolean[] var6 = new boolean[5];

      for (int var7 = 0; var7 < var5.length; var7++) {
         String var8 = var1.tree == null ? null : TreeSeasons.leavesName(var1.tree, var7);
         int var9 = var8 == null ? -1 : FarColours.of(var8);
         var5[var7] = var1.tree == null ? var4 : (var9 >= 0 ? var9 : var3);
         var6[var7] = var1.tree != null && var8 == null;
      }

      TreeKinds.set(var1.treeKind, var5, var6, var0.size[2], var0.size[3]);
   }

   static int rank(int var0, int var1) {
      return hash(var0, var1) & 0xFF;
   }

   static int liteKeep() {
      return Math.max(1, Math.round(LITE_SHARE.get() * 256.0F));
   }

   static int boxKeep(int var0) {
      return Math.max(1, liteKeep() / var0);
   }

   static float grow(int var0) {
      return (float)Math.pow(256.0 / var0, Tuning.farTreeGrowth);
   }

   static int hash(int var0, int var1) {
      int var2 = var0 * 668265261 ^ var1 * 374761393;
      var2 ^= var2 >>> 15;
      var2 *= -2048144789;
      return (var2 ^ var2 >>> 13) & 2147483647;
   }

   private FarTrees() {
   }

   private record Kind(FarTiles.Tile tile, float[] size) {
   }

   record Thinning(int cellX, int cellY, int keep, float grow) {
      static FarTrees.Thinning of(int var0, int var1, int var2) {
         int var3 = var0 == 0 ? 256 : FarTrees.liteKeep();
         return new FarTrees.Thinning(var1, var2, var3, FarTrees.grow(var3));
      }

      boolean keeps(int var1, int var2) {
         return this.keep >= 256 || FarTrees.rank(this.cellX * 256 + var1, this.cellY * 256 + var2) < this.keep;
      }
   }
}
