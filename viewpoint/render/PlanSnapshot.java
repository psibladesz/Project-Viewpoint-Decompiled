package viewpoint.render;

public final class PlanSnapshot {
   public static final int CHUNKS = 16;
   public static final int LEVELS = 5;
   public static final int SLOTS = 1280;
   public final int[][] passes = new int[1280][];
   public final byte[][] flags = new byte[1280][];
   public final int[][] walls = new int[1280][];
   public final long[][] rooms = new long[1280][];
   public long[] interior = new long[0];
   public int chunkX;
   public int chunkY;
   public int level;
}
