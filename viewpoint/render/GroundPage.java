package viewpoint.render;

public final class GroundPage implements FloorBakeSource {
   public static final int CHUNKS = 8;
   public static final int TILES = 66;
   public static final int SPAN = 10;
   private final GroundArt[] arts;

   public GroundPage(GroundArt[] var1) {
      this.arts = (GroundArt[])var1.clone();
   }

   @Override
   public int tiles() {
      return 66;
   }

   @Override
   public FloorLayers artAt(int var1, int var2) {
      int var3 = Math.floorDiv(var1 - 1, 8) + 1;
      int var4 = Math.floorDiv(var2 - 1, 8) + 1;
      return this.arts[var4 * 10 + var3];
   }

   public long bytes() {
      long var1 = 16L + this.arts.length * 8L;

      for (GroundArt var6 : this.arts) {
         var1 += var6 == null ? 0L : var6.bytes();
      }

      return var1;
   }
}
