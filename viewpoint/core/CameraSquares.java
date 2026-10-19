package viewpoint.core;

public final class CameraSquares {
   public static final int NORTH = 1;
   public static final int WEST = 2;
   public static final int FLOOR = 4;
   public static final int LEVELS = 2;
   public boolean on;
   public int x;
   public int y;
   public int level;
   public int side;
   public float nearCorner;
   public byte[] blocks = new byte[0];

   public int at(int var1, int var2, int var3) {
      return var1 >= 0 && var2 >= 0 && var3 >= 0 && var1 < this.side && var2 < this.side && var3 < 2
         ? this.blocks[(var3 * this.side + var2) * this.side + var1]
         : -1;
   }
}
