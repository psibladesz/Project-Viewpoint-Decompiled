package viewpoint.iris;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class ProgramSet {
   private static final Map<String, String> FALLBACK = new LinkedHashMap<>();
   private static final String[] GEOMETRY;
   public final String folder;
   private final Map<String, ProgramSet.Program> geometry = new LinkedHashMap<>();
   private final Map<ProgramSet.Stage, List<ProgramSet.Program>> passes = new LinkedHashMap<>();

   private ProgramSet(String var1) {
      this.folder = var1;
   }

   public static ProgramSet find(PackSource var0, String var1, Predicate<String> var2) {
      ProgramSet var3 = new ProgramSet(var1);

      for (String var7 : GEOMETRY) {
         ProgramSet.Program var8 = var3.program(var0, var7, var2);
         if (var8 != null && var8.drawable()) {
            var3.geometry.put(var7, var8);
         }
      }

      for (ProgramSet.Stage var16 : ProgramSet.Stage.values()) {
         ArrayList var17 = new ArrayList();
         int var9 = var16 == ProgramSet.Stage.FINAL ? 1 : 100;

         for (int var10 = 0; var10 < var9; var10++) {
            String var11 = var10 == 0 ? var16.prefix : var16.prefix + var10;
            ProgramSet.Program var12 = var3.program(var0, var11, var2);
            if (var12 != null && (var12.fragment != null || !var12.computes.isEmpty())) {
               var17.add(var12);
            }
         }

         var3.passes.put(var16, List.copyOf(var17));
      }

      return var3;
   }

   public static String overworld(PackSource var0, String var1) {
      if (var1 != null) {
         for (String var5 : var1.split("\n")) {
            String var6 = Macros.stripComments(var5).strip();
            int var7 = var6.indexOf(61);
            if (var6.startsWith("dimension.") && var7 >= 0) {
               List var8 = ShadersProperties.words(var6.substring(var7 + 1));
               if (var8.contains("minecraft:overworld") || var8.contains("overworld") || var8.contains("*")) {
                  return var6.substring(10, var7).strip() + "/";
               }
            }
         }
      }

      for (String var10 : var0.paths()) {
         if (var10.startsWith("world0/") && var10.endsWith(".fsh")) {
            return "world0/";
         }
      }

      return "";
   }

   private ProgramSet.Program program(PackSource var1, String var2, Predicate<String> var3) {
      String var4 = this.folder + var2;
      if (!var3.test(var2)) {
         return null;
      }

      String var5 = file(var1, var4 + ".vsh");
      String var6 = file(var1, var4 + ".fsh");
      String var7 = file(var1, var4 + ".gsh");
      ArrayList var8 = new ArrayList();
      String var9 = file(var1, var4 + ".csh");
      if (var9 != null) {
         var8.add(var9);
      }

      for (char var10 = 'a'; var10 <= 'z'; var10++) {
         String var11 = file(var1, var4 + "_" + var10 + ".csh");
         if (var11 != null) {
            var8.add(var11);
         }
      }

      return var5 == null && var6 == null && var8.isEmpty() ? null : new ProgramSet.Program(var2, var5, var7, var6, List.copyOf(var8));
   }

   private static String file(PackSource var0, String var1) {
      return var0.exists(var1) ? var1 : null;
   }

   public ProgramSet.Program geometry(String var1) {
      for (String var2 = var1; var2 != null; var2 = FALLBACK.get(var2)) {
         ProgramSet.Program var3 = this.geometry.get(var2);
         if (var3 != null) {
            return var3;
         }
      }

      return null;
   }

   public Map<String, ProgramSet.Program> geometryPrograms() {
      return this.geometry;
   }

   public List<ProgramSet.Program> passes(ProgramSet.Stage var1) {
      return this.passes.getOrDefault(var1, List.of());
   }

   static {
      String[][] var0 = new String[][]{
         {"gbuffers_line", "gbuffers_basic"},
         {"gbuffers_textured", "gbuffers_basic"},
         {"gbuffers_textured_lit", "gbuffers_textured"},
         {"gbuffers_skybasic", "gbuffers_basic"},
         {"gbuffers_skytextured", "gbuffers_textured"},
         {"gbuffers_clouds", "gbuffers_textured"},
         {"gbuffers_terrain", "gbuffers_textured_lit"},
         {"gbuffers_terrain_solid", "gbuffers_terrain"},
         {"gbuffers_terrain_cutout", "gbuffers_terrain"},
         {"gbuffers_damagedblock", "gbuffers_terrain"},
         {"gbuffers_block", "gbuffers_terrain"},
         {"gbuffers_block_translucent", "gbuffers_block"},
         {"gbuffers_beaconbeam", "gbuffers_textured"},
         {"gbuffers_entities", "gbuffers_textured_lit"},
         {"gbuffers_entities_translucent", "gbuffers_entities"},
         {"gbuffers_entities_glowing", "gbuffers_entities"},
         {"gbuffers_armor_glint", "gbuffers_textured"},
         {"gbuffers_spidereyes", "gbuffers_textured"},
         {"gbuffers_hand", "gbuffers_textured_lit"},
         {"gbuffers_hand_water", "gbuffers_hand"},
         {"gbuffers_weather", "gbuffers_textured_lit"},
         {"gbuffers_water", "gbuffers_terrain"},
         {"gbuffers_particles", "gbuffers_textured_lit"},
         {"gbuffers_particles_translucent", "gbuffers_particles"},
         {"gbuffers_lightning", "gbuffers_entities"},
         {"shadow_solid", "shadow"},
         {"shadow_cutout", "shadow"},
         {"shadow_water", "shadow"},
         {"shadow_entities", "shadow"},
         {"shadow_block", "shadow"},
         {"shadow_lightning", "shadow_entities"},
         {"dh_water", "dh_terrain"}
      };

      for (String[] var4 : var0) {
         FALLBACK.put(var4[0], var4[1]);
      }

      GEOMETRY = new String[]{
         "gbuffers_basic",
         "gbuffers_line",
         "gbuffers_textured",
         "gbuffers_textured_lit",
         "gbuffers_skybasic",
         "gbuffers_skytextured",
         "gbuffers_clouds",
         "gbuffers_terrain",
         "gbuffers_terrain_solid",
         "gbuffers_terrain_cutout",
         "gbuffers_damagedblock",
         "gbuffers_block",
         "gbuffers_block_translucent",
         "gbuffers_beaconbeam",
         "gbuffers_entities",
         "gbuffers_entities_translucent",
         "gbuffers_entities_glowing",
         "gbuffers_armor_glint",
         "gbuffers_spidereyes",
         "gbuffers_hand",
         "gbuffers_hand_water",
         "gbuffers_weather",
         "gbuffers_water",
         "gbuffers_particles",
         "gbuffers_particles_translucent",
         "gbuffers_lightning",
         "shadow",
         "shadow_solid",
         "shadow_cutout",
         "shadow_water",
         "shadow_entities",
         "shadow_block",
         "shadow_lightning",
         "dh_terrain",
         "dh_water",
         "dh_shadow"
      };
   }

   public record Program(String name, String vertex, String geometry, String fragment, List<String> computes) {
      public boolean drawable() {
         return this.vertex != null && this.fragment != null;
      }
   }

   public enum Stage {
      SETUP("setup"),
      BEGIN("begin"),
      SHADOWCOMP("shadowcomp"),
      PREPARE("prepare"),
      DEFERRED("deferred"),
      COMPOSITE("composite"),
      FINAL("final");

      public final String prefix;

      Stage(String nullxx) {
         this.prefix = nullxx;
      }
   }
}
