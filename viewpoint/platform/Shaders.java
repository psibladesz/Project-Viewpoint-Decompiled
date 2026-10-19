package viewpoint.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Shaders {
   private static final String ROOT = "/viewpoint/shaders/";
   static final String CONSTANTS = "constants.glsl";
   static final Set<String> DRIVERS = Set.of("lib/weather.glsl", "precip.vert");
   private static final Pattern INCLUDE = Pattern.compile("^\\s*#include\\s+\"([^\"]+)\"\\s*$");

   static Shaders.Source load(String var0) {
      return load(var0, ShaderPacks.active());
   }

   static Shaders.Source load(String var0, ShaderPacks.Active var1) {
      ArrayList var2 = new ArrayList();
      StringBuilder var3 = new StringBuilder(8192);
      boolean[] var4 = new boolean[1];
      expand(var0, var1, var2, new ArrayList<>(), var3, var4);
      return new Shaders.Source(var3.toString(), var2, var4[0]);
   }

   private static void expand(String var0, ShaderPacks.Active var1, List<String> var2, List<String> var3, StringBuilder var4, boolean[] var5) {
      if (var3.contains(var0)) {
         throw new IllegalStateException("shader include cycle: " + String.join(" > ", var3) + " > " + var0);
      }

      var3.add(var0);
      int var6 = var2.size();
      var2.add(var0);
      String var7 = read(var0, var1, var5);
      int var8 = 0;
      int var9 = 0;

      while (var9 < var7.length()) {
         int var10 = var7.indexOf(10, var9);
         var10 = var10 < 0 ? var7.length() : var10 + 1;
         String var11 = var7.substring(var9, var10);
         var8++;
         Matcher var12 = INCLUDE.matcher(var11.stripTrailing());
         if (var12.matches()) {
            var4.append("#line 1 ").append(var2.size()).append('\n');
            expand(var12.group(1), var1, var2, var3, var4, var5);
            var4.append("#line ").append(var8 + 1).append(' ').append(var6).append('\n');
         } else if (var6 == 0 && var8 == 1 && var11.startsWith("#version")) {
            var4.append(var11).append(var1.defines).append("#line 2 0\n");
         } else {
            var4.append(var11);
         }

         var9 = var10;
      }

      var3.remove(var3.size() - 1);
   }

   private static String read(String var0, ShaderPacks.Active var1, boolean[] var2) {
      boolean var3 = var0.equals("constants.glsl") || DRIVERS.contains(var0);

      for (ShaderPack var4 = var1.pack; var4 != null && !var3; var4 = var4.base) {
         String var5 = var4.shader(var0);
         if (var5 != null) {
            var2[0] = true;
            return var5;
         }
      }

      return read(var0);
   }

   static String read(String var0) {
      if (var0.equals("constants.glsl")) {
         return constants();
      }

      File var1 = devFile(var0);

      try {
         if (var1 != null) {
            return Files.readString(var1.toPath(), StandardCharsets.UTF_8).replace("\r\n", "\n");
         }

         try (InputStream var2 = Shaders.class.getResourceAsStream("/viewpoint/shaders/" + var0)) {
            if (var2 == null) {
               throw new IllegalStateException("shader file missing from the jar: " + var0);
            } else {
               return new String(var2.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
            }
         }
      } catch (IOException var7) {
         throw new IllegalStateException("cannot read shader " + var0 + ": " + var7, var7);
      }
   }

   private static String constants() {
      return "#ifndef VIEWPOINT_CONSTANTS\n#define VIEWPOINT_CONSTANTS\n// Generated from Java (Shaders.constants): the chunk mesh vertex's flag bits (VertexFormat).\n#define FLAG_SINGLE_SIDED 1\n#define FLAG_DECAL 2\n#define FLAG_SOLID_FLOOR 4\n#define FLAG_COVER 8\n#define FLAG_BAKED_FLOOR 64\n#define FLAG_BILLBOARD 128\n#define FLAG_FACING 512\n#define FLAG_GLASS 1024\n#define FLAG_WATER 2048\n#define FLAG_GRASS 4096\n#define FLAG_DOOR 16384\n#define FLAG_ROOF 32768\n#define FLAG_LIFTED 98304\n#define FLAG_SHADED_ART 163840\n#define ROOF_EAST_ART "
         + Tuning.roofEastArt
         + "\n#define PLANE_SHIFT 16\n#define PLANE_MASK 7\n#define PLANE_BITS 3\n#define PLANT_VERTICES 6\n#define PLANT_TEXELS 7\n#define FLAG_LAYER_SHIFT "
         + Integer.numberOfTrailingZeros(16)
         + "\n#define FLAG_ROOTED 256\n#define FLAG_STANDING 8192\n// A model pack's instance's slot (VertexFormat.MODEL_*): where its flags, square and model sit.\n#define MODEL_FLAGS 6\n#define MODEL_SQUARE 7\n#define MODEL_INDEX 8\n#define NO_SQUARE 64\n// A dead body's card's texels (VertexFormat.CARD_*).\n#define CARD_TEXELS 7\n// The owned models' textures a group binds, and the slot's shift in the instance's draw index (Gl).\n#define MODEL_TEXTURES 16\n#define MODEL_SLOT_SHIFT 24\n// The game's atlas pages a world draw binds, and a command's slot's shift in its instance (Gl).\n#define ATLAS_SLOTS 8\n#define ATLAS_SLOT_SHIFT 24\n#define ROOT_X_SHIFT 16\n#define ROOT_Y_SHIFT 24\n#define WALL_FAMILY_SHIFT 16\n#define WALL_FAMILY_MASK 15\n// The spotlights' records (SpotFormat): texels a record, shadow maps at most, the beams' kinds.\n#define SPOT_TEXELS 6\n#define SPOT_MAPS 5\n#define SPOT_TORCH 0\n#define SPOT_LOW_BEAM 1\n#define SPOT_REAR 2\n"
         + spaceConstants()
         + EarthAir.defines()
         + "#endif\n";
   }

   private static String spaceConstants() {
      return "// A storey's height in scene units (Space.SQRT6).\n#define SQRT6 2.4494896\n// Squares a metre, across and up (Space.SQUARES_A_METRE).\n#define SQUARES_A_METRE 0.81649655\n// Squares from the eye the far shell is drawn into the G-buffer (Space.SHELL_BAND).\n#define SHELL_BAND 256.0\n// Squares between the terrain relief's nodes (Space.RELIEF_SPACING).\n#define RELIEF_SPACING 4.0\n// Squares between the nodes of the water's levels on the relief (Space.RELIEF_WATER_SPACING).\n#define RELIEF_WATER_SPACING 4.0\n// Squares between the cells of the water's kinds and flow on the relief (Space.RELIEF_FLOW_SPACING).\n#define RELIEF_FLOW_SPACING 16.0\n#define RELIEF_FLOW_MOST 2.0\n// Squares a wall's step on the relief takes, short of its line (Space.RELIEF_WALL_EPS).\n#define RELIEF_WALL_EPS 0.015625\n// Scene units over a character's feet up to which it bends onto the relief (Space.RELIEF_KNEE).\n#define RELIEF_KNEE 0.5\n// Scene units over its floor a lamp's light starts from (Space.LAMP_HEIGHT).\n#define LAMP_HEIGHT 2.1\n// The least strength of the sun or the moon its maps are drawn for (Space.SUN_LEAST).\n#define SUN_LEAST 0.02\n// Light grids across the light atlas (LightGridFormat), each LIGHT_GRID texels.\n#define LIGHT_COLUMNS 204.0\n#define LIGHT_GRID vec2(20.0, 40.0)\n// Levels of the sky's light lost a square from open sky, in a grid's bottom half (LightGridFormat).\n#define SKY_STEP 16.0\n// The far shell's band's table of grids (render.BandLight): chunks a side, and levels.\n#define BAND_WINDOW 72\n#define BAND_LEVELS 32\n// The far cells' light pages' table (render.CellLightPages): cells a side.\n#define CELL_WINDOW 16\n";
   }

   static long modified(String var0, ShaderPacks.Active var1) {
      for (ShaderPack var2 = var1.pack; var2 != null; var2 = var2.base) {
         long var3 = var2.modified(var0);
         if (var3 >= 0L) {
            return var3;
         }
      }

      File var5 = devFile(var0);
      return var5 == null ? 0L : var5.lastModified();
   }

   private static File devFile(String var0) {
      String var1 = Settings.dev ? Settings.shaderDir : null;
      if (var1 != null && !var1.isEmpty()) {
         File var2 = new File(var1, var0);
         return var2.isFile() ? var2 : null;
      } else {
         return null;
      }
   }

   static String explain(String var0, List<String> var1) {
      Matcher var2 = Pattern.compile("\\b(\\d+)([:(])(\\d+)").matcher(var0);
      StringBuilder var3 = new StringBuilder();

      while (var2.find()) {
         int var4 = Integer.parseInt(var2.group(1));
         String var5 = var4 < var1.size() ? (String)var1.get(var4) : var2.group(1);
         var2.appendReplacement(var3, Matcher.quoteReplacement(var5 + var2.group(2) + var2.group(3)));
      }

      var2.appendTail(var3);
      return var3.toString();
   }

   private Shaders() {
   }

   static final class Source {
      final String text;
      final List<String> files;
      final boolean fromPack;

      Source(String var1, List<String> var2, boolean var3) {
         this.text = var1;
         this.files = var2;
         this.fromPack = var3;
      }
   }
}
