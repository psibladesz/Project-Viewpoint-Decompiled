package viewpoint.iris;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class Biomes {
   private static final List<String> BIOMES = List.of(
      "THE_VOID",
      "PLAINS",
      "SUNFLOWER_PLAINS",
      "SNOWY_PLAINS",
      "ICE_SPIKES",
      "DESERT",
      "SWAMP",
      "MANGROVE_SWAMP",
      "FOREST",
      "FLOWER_FOREST",
      "BIRCH_FOREST",
      "DARK_FOREST",
      "OLD_GROWTH_BIRCH_FOREST",
      "OLD_GROWTH_PINE_TAIGA",
      "OLD_GROWTH_SPRUCE_TAIGA",
      "TAIGA",
      "SNOWY_TAIGA",
      "SAVANNA",
      "SAVANNA_PLATEAU",
      "WINDSWEPT_HILLS",
      "WINDSWEPT_GRAVELLY_HILLS",
      "WINDSWEPT_FOREST",
      "WINDSWEPT_SAVANNA",
      "JUNGLE",
      "SPARSE_JUNGLE",
      "BAMBOO_JUNGLE",
      "BADLANDS",
      "ERODED_BADLANDS",
      "WOODED_BADLANDS",
      "MEADOW",
      "CHERRY_GROVE",
      "GROVE",
      "SNOWY_SLOPES",
      "FROZEN_PEAKS",
      "JAGGED_PEAKS",
      "STONY_PEAKS",
      "RIVER",
      "FROZEN_RIVER",
      "BEACH",
      "SNOWY_BEACH",
      "STONY_SHORE",
      "WARM_OCEAN",
      "LUKEWARM_OCEAN",
      "DEEP_LUKEWARM_OCEAN",
      "OCEAN",
      "DEEP_OCEAN",
      "COLD_OCEAN",
      "DEEP_COLD_OCEAN",
      "FROZEN_OCEAN",
      "DEEP_FROZEN_OCEAN",
      "MUSHROOM_FIELDS",
      "DRIPSTONE_CAVES",
      "LUSH_CAVES",
      "DEEP_DARK",
      "NETHER_WASTES",
      "WARPED_FOREST",
      "CRIMSON_FOREST",
      "SOUL_SAND_VALLEY",
      "BASALT_DELTAS",
      "THE_END",
      "END_HIGHLANDS",
      "END_MIDLANDS",
      "SMALL_END_ISLANDS",
      "END_BARRENS",
      "PALE_GARDEN"
   );
   private static final List<String> CATEGORIES = List.of(
      "NONE",
      "TAIGA",
      "EXTREME_HILLS",
      "JUNGLE",
      "MESA",
      "PLAINS",
      "SAVANNA",
      "ICY",
      "THE_END",
      "BEACH",
      "FOREST",
      "OCEAN",
      "DESERT",
      "RIVER",
      "SWAMP",
      "MUSHROOM",
      "NETHER",
      "MOUNTAIN",
      "UNDERGROUND"
   );
   private static final Map<String, Double> CONSTANTS = new HashMap<>();
   public static final int PLAINS;
   public static final int CATEGORY_PLAINS;

   static Double constant(String var0) {
      return CONSTANTS.get(var0);
   }

   static Map<String, String> macros() {
      HashMap var0 = new HashMap();

      for (Entry var2 : CONSTANTS.entrySet()) {
         var0.put((String)var2.getKey(), Integer.toString(((Double)var2.getValue()).intValue()));
      }

      return var0;
   }

   private Biomes() {
   }

   static {
      for (int var0 = 0; var0 < BIOMES.size(); var0++) {
         CONSTANTS.put("BIOME_" + BIOMES.get(var0), (double)var0);
      }

      for (int var1 = 0; var1 < CATEGORIES.size(); var1++) {
         CONSTANTS.put("CAT_" + CATEGORIES.get(var1), (double)var1);
      }

      CONSTANTS.put("PPT_NONE", 0.0);
      CONSTANTS.put("PPT_RAIN", 1.0);
      CONSTANTS.put("PPT_SNOW", 2.0);
      PLAINS = BIOMES.indexOf("PLAINS");
      CATEGORY_PLAINS = CATEGORIES.indexOf("PLAINS");
   }
}
