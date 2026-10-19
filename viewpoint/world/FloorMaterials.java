package viewpoint.world;

public final class FloorMaterials {
   public static final int UNKNOWN = 0;
   public static final int NATURAL = 1;
   public static final int GRASS = 2;
   public static final int STREET = 3;
   public static final int PAVING = 4;
   public static final int INTERIOR = 5;
   public static final int TILES = 6;
   public static final int WOOD = 7;
   public static final int CARPET = 8;
   public static final int BURNT = 9;
   public static final int DECAL = 10;
   private static final String[] PREFIXES = new String[]{
      "blends_natural_",
      "floors_exterior_natural_",
      "blends_grassoverlays_",
      "blends_street_",
      "blends_streetoverlays_",
      "floors_exterior_street_",
      "floors_overlay_street_",
      "floors_exterior_tilesandstone_",
      "floors_interior_tilesandwood_",
      "floors_overlay_tiles_",
      "floors_overlay_wood_",
      "floors_interior_carpet_",
      "floors_rugs_",
      "floors_burnt_"
   };
   private static final int[] FAMILIES = new int[]{1, 1, 2, 3, 3, 3, 3, 4, 5, 6, 7, 8, 8, 9};

   private FloorMaterials() {
   }

   public static int of(String var0) {
      if (var0 == null) {
         return 0;
      }

      for (int var1 = 0; var1 < PREFIXES.length; var1++) {
         if (var0.startsWith(PREFIXES[var1])) {
            return FAMILIES[var1];
         }
      }

      return 0;
   }
}
