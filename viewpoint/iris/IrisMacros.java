package viewpoint.iris;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class IrisMacros {
   public static final String MC_VERSION = "12104";
   public static final String IRIS_VERSION = "10812";
   public static final String VIEWPOINT_VERSION = "1";
   public static final String[] RENDER_STAGES = new String[]{
      "NONE",
      "SKY",
      "SUNSET",
      "CUSTOM_SKY",
      "SUN",
      "MOON",
      "STARS",
      "VOID",
      "TERRAIN_SOLID",
      "TERRAIN_CUTOUT_MIPPED",
      "TERRAIN_CUTOUT",
      "ENTITIES",
      "BLOCK_ENTITIES",
      "DESTROY",
      "OUTLINE",
      "DEBUG",
      "HAND_SOLID",
      "TERRAIN_TRANSLUCENT",
      "TRIPWIRE",
      "PARTICLES",
      "CLOUDS",
      "RAIN_SNOW",
      "WORLD_BORDER",
      "HAND_TRANSLUCENT"
   };
   public static final String[] DH_BLOCKS = new String[]{
      "UNKNOWN",
      "LEAVES",
      "STONE",
      "WOOD",
      "METAL",
      "DIRT",
      "LAVA",
      "DEEPSLATE",
      "SNOW",
      "SAND",
      "TERRACOTTA",
      "NETHER_STONE",
      "WATER",
      "GRASS",
      "AIR",
      "ILLUMINATED"
   };

   private IrisMacros() {
   }

   public static Map<String, String> standard(int var0, String var1, String var2, Set<String> var3, boolean var4) {
      LinkedHashMap var5 = new LinkedHashMap();
      var5.put("MC_VERSION", "12104");
      var5.put("MC_GL_VERSION", Integer.toString(var0));
      var5.put("MC_GLSL_VERSION", Integer.toString(Math.min(var0, 460)));
      var5.put("IS_IRIS", "");
      var5.put("IRIS_VERSION", "10812");
      var5.put("VIEWPOINT", "");
      var5.put("VIEWPOINT_VERSION", "1");
      var5.put("MC_RENDER_QUALITY", "1.0");
      var5.put("MC_SHADOW_QUALITY", "1.0");
      var5.put("MC_HAND_DEPTH", "0.125");
      var5.put("MC_NORMAL_MAP", "");
      var5.put("MC_SPECULAR_MAP", "");
      var5.put("MC_OS_" + os(), "");
      String var6 = var1 == null ? "" : var1.toLowerCase(Locale.ROOT);
      var5.put(
         var6.contains("nvidia")
            ? "MC_GL_VENDOR_NVIDIA"
            : (!var6.contains("ati") && !var6.contains("amd") ? (var6.contains("intel") ? "MC_GL_VENDOR_INTEL" : "MC_GL_VENDOR_OTHER") : "MC_GL_VENDOR_ATI"),
         ""
      );
      if (var6.contains("amd")) {
         var5.put("MC_GL_VENDOR_AMD", "");
      }

      String var7 = var2 == null ? "" : var2.toLowerCase(Locale.ROOT);
      var5.put(
         var7.contains("geforce")
            ? "MC_GL_RENDERER_GEFORCE"
            : (var7.contains("radeon") ? "MC_GL_RENDERER_RADEON" : (var7.contains("intel") ? "MC_GL_RENDERER_INTEL" : "MC_GL_RENDERER_OTHER")),
         ""
      );

      for (String var9 : var3) {
         var5.put("IRIS_FEATURE_" + var9, "");
      }

      for (int var10 = 0; var10 < RENDER_STAGES.length; var10++) {
         var5.put("MC_RENDER_STAGE_" + RENDER_STAGES[var10], Integer.toString(var10));
      }

      for (int var11 = 0; var11 < DH_BLOCKS.length; var11++) {
         var5.put("DH_BLOCK_" + DH_BLOCKS[var11], Integer.toString(var11));
      }

      if (var4) {
         var5.put("DISTANT_HORIZONS", "");
      }

      var5.putAll(Biomes.macros());
      return var5;
   }

   public static int renderStage(String var0) {
      for (int var1 = 0; var1 < RENDER_STAGES.length; var1++) {
         if (RENDER_STAGES[var1].equals(var0)) {
            return var1;
         }
      }

      throw new IllegalArgumentException("no render stage " + var0);
   }

   private static String os() {
      String var0 = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
      return var0.contains("win") ? "WINDOWS" : (var0.contains("mac") ? "MAC" : (var0.contains("bsd") ? "BSD" : "LINUX"));
   }
}
