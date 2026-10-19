package viewpoint.render;

public final class FloorPage implements FloorBakeSource {
   public static final int GUTTER = 1;
   public static final int TILES = 10;
   public final int wx;
   public final int wy;
   public final int level;
   private final FloorArt own;
   private final FloorArt[] neighbours;
   public static final int[] NEIGHBOURS = new int[]{-1, -1, 0, -1, 1, -1, -1, 0, 1, 0, -1, 1, 0, 1, 1, 1};

   public FloorPage(int var1, int var2, int var3, FloorArt var4, FloorArt[] var5) {
      this.wx = var1;
      this.wy = var2;
      this.level = var3;
      this.own = var4;
      this.neighbours = (FloorArt[])var5.clone();
   }

   public FloorArt own() {
      return this.own;
   }

   public long key() {
      return key(this.wx, this.wy, this.level);
   }

   public static long key(int var0, int var1, int var2) {
      return (long)(var0 & 16777215) << 40 | (long)(var1 & 16777215) << 16 | var2 + 32 & 65535;
   }

   @Override
   public int tiles() {
      return 10;
   }

   public FloorArt artAt(int var1, int var2) {
      int var3 = var1 < 1 ? -1 : (var1 >= 9 ? 1 : 0);
      int var4 = var2 < 1 ? -1 : (var2 >= 9 ? 1 : 0);
      if (var3 == 0 && var4 == 0) {
         return this.own;
      }

      for (int var5 = 0; var5 < NEIGHBOURS.length / 2; var5++) {
         if (NEIGHBOURS[var5 * 2] == var3 && NEIGHBOURS[var5 * 2 + 1] == var4) {
            return this.neighbours[var5];
         }
      }

      return null;
   }

   public static int squareAt(int var0, int var1) {
      int var2 = Math.floorMod(var0 - 1, 8);
      int var3 = Math.floorMod(var1 - 1, 8);
      return var3 * 8 + var2;
   }
}
