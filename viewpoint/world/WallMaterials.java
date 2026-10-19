package viewpoint.world;

public final class WallMaterials {
   public static final int NONE = 0;
   public static final int UNKNOWN = 1;
   public static final int EXTERIOR = 2;
   public static final int WOOD = 3;
   public static final int COMMERCIAL = 4;
   public static final int GARAGE = 5;
   public static final int INTERIOR = 6;
   public static final int TILED = 7;
   public static final int BURNT = 8;
   private static final String[] PREFIXES = new String[]{
      "walls_exterior_house_",
      "walls_exterior_roofs_",
      "walls_exterior_wooden_",
      "walls_logs",
      "walls_commercial_",
      "walls_garage_",
      "walls_interior_house_",
      "walls_interior_bathroom_",
      "walls_interior_detailing_bathroom_",
      "walls_burnt_"
   };
   private static final int[] FAMILIES = new int[]{2, 2, 3, 3, 4, 5, 6, 7, 7, 8};

   private WallMaterials() {
   }

   public static int of(String var0) {
      if (var0 == null) {
         return 1;
      }

      for (int var1 = 0; var1 < PREFIXES.length; var1++) {
         if (var0.startsWith(PREFIXES[var1])) {
            return FAMILIES[var1];
         }
      }

      return 1;
   }

   public static int flags(String var0) {
      return of(var0) << 16;
   }
}
