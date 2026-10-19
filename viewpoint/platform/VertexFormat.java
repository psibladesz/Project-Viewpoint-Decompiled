package viewpoint.platform;

public final class VertexFormat {
   public static final int[] ATTRIBUTE_SIZES = new int[]{3, 2, 4, 3, 1, 1};
   public static final int STRIDE = 14;
   public static final int FLAGS_ATTRIBUTE = 5;
   public static final int SINGLE_SIDED = 1;
   public static final int DECAL = 2;
   public static final int SOLID_FLOOR = 4;
   public static final int COVER = 8;
   public static final int LAYER = 16;
   public static final int BAKED_FLOOR = 64;
   public static final int BILLBOARD = 128;
   public static final int ROOTED = 256;
   public static final int ROOT_X_SHIFT = 16;
   public static final int ROOT_Y_SHIFT = 24;
   public static final int FACING = 512;
   public static final int GLASS = 1024;
   public static final int WATER = 2048;
   public static final int GRASS = 4096;
   public static final int STANDING = 8192;
   public static final int STANDING_BITS = -57344;
   public static final int DOOR = 16384;
   public static final int ROOF = 32768;
   public static final int LIFTED = 98304;
   public static final int SHADED_ART = 163840;
   public static final int WALL_FAMILY_SHIFT = 16;
   public static final int WALL_FAMILY_MASK = 15;
   public static final int PLANT_FLOATS = 12;
   public static final int PLANT_VERTICES = 6;
   public static final int PLANE_SHIFT = 16;
   public static final int PLANE_BITS = 3;
   public static final int PLANE_MASK = 7;
   public static final int MODEL_FLOATS = 9;
   public static final int MODEL_FLAGS = 6;
   public static final int MODEL_SQUARE = 7;
   public static final int MODEL_INDEX = 8;
   public static final int NO_SQUARE = 64;
   public static final int CARD_TEXELS = 7;
   public static final int CARD_FLOATS = 28;
   public static final int CARD_HEIGHT = 16;
   public static final int CARD_FLAGS = 17;
   public static final int CARD_RECORD = 18;
   public static final int CARD_SQUARE = 19;
   public static final int CARD_RECT = 20;
   public static final int CARD_FADE = 24;
   public static final int FAR_FLOATS = 4;

   public static int standing(int var0, int var1) {
      return 8192 | var0 << 16 | var1 << 24;
   }

   public static int rooted(int var0, int var1) {
      return 256 | var0 << 16 | var1 << 24;
   }

   private VertexFormat() {
   }
}
