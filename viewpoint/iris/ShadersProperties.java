package viewpoint.iris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ShadersProperties {
   public final Map<String, List<String>> screens = new LinkedHashMap<>();
   public final Map<String, Integer> screenColumns = new LinkedHashMap<>();
   public final Set<String> sliders = new LinkedHashSet<>();
   public final Map<String, List<String>> profiles = new LinkedHashMap<>();
   public final Map<String, String> blend = new LinkedHashMap<>();
   public final Map<String, String> alphaTest = new LinkedHashMap<>();
   public final Map<String, String> programEnabled = new LinkedHashMap<>();
   public final Map<String, String> sizeBuffer = new LinkedHashMap<>();
   public final Map<String, Boolean> flip = new LinkedHashMap<>();
   public final Map<String, String> textures = new LinkedHashMap<>();
   public final Map<String, String> customTextures = new LinkedHashMap<>();
   public final Map<String, String> images = new LinkedHashMap<>();
   public final Map<Integer, String> bufferObjects = new LinkedHashMap<>();
   public final List<ShadersProperties.CustomUniform> uniforms = new ArrayList<>();
   public final Map<String, String> flags = new LinkedHashMap<>();
   public final Set<String> unknown = new LinkedHashSet<>();
   private static final Set<String> KNOWN_FLAGS = Set.of(
           "shadow.enabled",
           "dhShadow.enabled",
           "shadow.culling",
           "shadowTerrain",
           "shadowTranslucent",
           "shadowEntities",
           "shadowPlayer",
           "shadowBlockEntities",
           "shadowLightBlockEntities",
           "oldLighting",
           "oldHandLight",
           "dynamicHandLight",
           "separateAo",
           "clouds",
           "cloudShadow",
           "sun",
           "moon",
           "stars",
           "sky",
           "weather",
           "weather.particles",
           "particles.ordering",
           "particles.before.deferred",
           "voxelizeLightBlocks",
           "beacon.beam.depth",
           "rain.depth",
           "underwaterOverlay",
           "vignette",
           "frustum.culling",
           "occlusion.culling",
           "iris.features.required",
           "iris.features.optional",
           "allowConcurrentCompute",
           "supportsColorCorrection",
           "prepareBeforeShadow",
           "shadow.ignoreFrustum",
           "backFace.solid",
           "backFace.cutout",
           "backFace.cutoutMipped",
           "backFace.translucent",
           "texture.noise",
           "version.1.12.2",
           "dimension",
           "screen.columns"
   );

   private ShadersProperties() {
   }

   public static ShadersProperties parse(String var0) {
      ShadersProperties var1 = new ShadersProperties();

      for (String var5 : var0.split("\n")) {
         String var6 = Macros.stripComments(var5).strip();
         int var7 = var6.indexOf(61);
         if (!var6.isEmpty() && !var6.startsWith("#") && var7 > 0) {
            var1.entry(var6.substring(0, var7).strip(), var6.substring(var7 + 1).strip());
         }
      }

      return var1;
   }

   private void entry(String var1, String var2) {
      if (var1.equals("screen")) {
         this.screens.put("", words(var2));
      } else if (var1.equals("screen.columns")) {
         this.screenColumns.put("", number(var2, 2));
      } else if (var1.startsWith("screen.") && var1.endsWith(".columns")) {
         this.screenColumns.put(var1.substring(7, var1.length() - 8), number(var2, 2));
      } else if (var1.startsWith("screen.")) {
         this.screens.put(var1.substring(7), words(var2));
      } else if (var1.equals("sliders")) {
         this.sliders.addAll(words(var2));
      } else if (var1.startsWith("profile.")) {
         this.profiles.put(var1.substring(8), words(var2));
      } else if (var1.startsWith("blend.")) {
         this.blend.put(var1.substring(6), var2);
      } else if (var1.startsWith("alphaTest.")) {
         this.alphaTest.put(var1.substring(10), var2);
      } else if (var1.startsWith("program.") && var1.endsWith(".enabled")) {
         this.programEnabled.put(var1.substring(8, var1.length() - 8), var2);
      } else if (var1.startsWith("size.buffer.")) {
         this.sizeBuffer.put(var1.substring(12), var2);
      } else if (var1.startsWith("flip.")) {
         this.flip.put(var1.substring(5), var2.equalsIgnoreCase("true"));
      } else if (var1.startsWith("texture.") && !var1.equals("texture.noise")) {
         this.textures.put(var1.substring(8), var2);
      } else if (var1.startsWith("customTexture.")) {
         this.customTextures.put(var1.substring(14), var2);
      } else if (var1.startsWith("image.")) {
         this.images.put(var1.substring(6), var2);
      } else if (var1.startsWith("bufferObject.")) {
         this.bufferObjects.put(number(var1.substring(13), -1), var2);
      } else if (!var1.startsWith("uniform.") && !var1.startsWith("variable.")) {
         this.flags.put(var1, var2);
         if (!KNOWN_FLAGS.contains(var1) && !var1.startsWith("dimension.") && !var1.startsWith("scale.") && !var1.startsWith("indirect.")) {
            this.unknown.add(var1);
         }
      } else {
         this.uniform(var1, var2);
      }
   }

   private void uniform(String var1, String var2) {
      String[] var3 = var1.split("\\.", 3);
      if (var3.length < 3) {
         this.unknown.add(var1);
      } else {
         String var4 = var3[2];
         this.uniforms.removeIf(var1x -> var1x.name.equals(var4));
         this.uniforms.add(new ShadersProperties.CustomUniform(var3[1], var4, var2, var3[0].equals("variable")));
      }
   }

   public String flag(String var1, String var2) {
      return this.flags.getOrDefault(var1, var2);
   }

   public boolean flag(String var1, boolean var2) {
      String var3 = this.flags.get(var1);
      return var3 == null ? var2 : var3.strip().equalsIgnoreCase("true");
   }

   public Set<String> features(boolean var1) {
      return new LinkedHashSet<>(words(this.flag(var1 ? "iris.features.required" : "iris.features.optional", "")));
   }

   public static List<String> words(String var0) {
      ArrayList<String> var1 = new ArrayList<>(Arrays.asList(var0.strip().split("\\s+")));
      var1.removeIf(String::isEmpty);
      return var1;
   }

   private static int number(String var0, int var1) {
      try {
         return Integer.parseInt(var0.strip());
      } catch (NumberFormatException var3) {
         return var1;
      }
   }

   public record CustomUniform(String type, String name, String expression, boolean variable) {
   }
}