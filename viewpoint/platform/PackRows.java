package viewpoint.platform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

final class PackRows {
   private static final String[] LEVELS = new String[]{"Off", "Medium", "High", "Ultra", "Extreme"};
   private static final String[] EFFECT_LEVELS = new String[]{"Off", "Low", "Medium", "High", "Ultra"};
   private static final String[] SHADOWS = new String[]{
           "sunShadows", "shadowResolution", "shadowCascades", "farShadows", "lampShadows", "lampShadowResolution", "flashlightShadow", "shadowSamples"
   };
   private static final String[] REACHES = new String[]{"3", "6", "9", "12"};
   private static final String[] QUALITIES = new String[]{"low", "medium", "high", "ultra"};
   private static final String[] SAMPLES = new String[]{"4", "6", "10", "16"};
   private static final Map<String, String> STARTING_SCALE = Map.of("off", "100", "fsr3", "67", "xess", "59", "dlss", "67");
   private static final Map<String, String[][]> MODES = Map.of(
           "off",
           new String[][]{{"Native", "100"}},
           "fsr3",
           new String[][]{{"Ultra Performance", "33"}, {"Performance", "50"}, {"Balanced", "59"}, {"Quality", "67"}, {"Native AA", "100"}},
           "xess",
           new String[][]{
                   {"Ultra Performance", "33"},
                   {"Performance", "43"},
                   {"Balanced", "50"},
                   {"Quality", "59"},
                   {"Ultra Quality", "67"},
                   {"Ultra Quality Plus", "77"},
                   {"Native AA", "100"}
           },
           "dlss",
           new String[][]{{"Ultra Performance", "33"}, {"Performance", "50"}, {"Balanced", "59"}, {"Quality", "67"}, {"DLAA", "100"}}
   );
   private static final String SHADOWS_TEXT = "Sun, moon, lamp and flashlight shadows: how sharp, how far, and how many lamps cast them.";
   private static final String AIR_TEXT = "Light in the air: haze, lamp halos, the flashlight's beam and the sun's shafts.";
   private static final String CLOUDS_TEXT = "Volumetric clouds, and how finely they are drawn.";
   private static final String SHADOWS_BRIEF = "Shadows from the sun, the moon, the lamps and the flashlight.";
   private static final String AIR_BRIEF = "Light in the air: haze, halos and light shafts.";
   private static final String CLOUDS_BRIEF = "3D clouds, and their detail.";
   private static final String GI_TEXT = "Light that bounces off the lit floors and walls onto what is round them, and off the place the flashlight lights. A higher level takes the bounced light farther.";
   private static final String GI_BRIEF = "Light that bounces off lit surfaces.";
   private static final String AO_TEXT = "Shade in corners, in creases and under things. A higher level makes the shade smoother and more exact.";
   private static final String AO_BRIEF = "Shade in corners and under objects.";
   private static final String BLUR_TEXT = "Blurs the picture as you turn and as things move. A higher level makes the blur smoother.";
   private static final String BLUR_BRIEF = "Blurs the picture when you turn or things move.";
   private static final String ANTIALIASING_TEXT = "Smooths jagged edges. TAA+ blends each frame with the ones before: the steadiest image. TAA uses fewer frames. FXAA and SMAA smooth each frame on its own: sharp, but edges can still shimmer. When resolution scaling is on, this setting is off: the upscaler smooths the edges.";
   private static final String ANTIALIASING_BRIEF = "Smooths jagged edges and shimmer.";
   private static final String SCALING_TEXT = "Uses an upscaler from a graphics card maker in place of the anti-aliasing. The upscaler smooths edges and makes a lower render resolution fill the screen. FSR 3 is from AMD, XeSS is from Intel, DLSS is from NVIDIA (RTX cards only). Choose its mode below.";
   private static final String SCALING_BRIEF = "An upscaler from AMD, Intel or NVIDIA.";
   private static final String MODE_TEXT = "The resolution the upscaler draws the world at before it fills the screen. Performance modes draw fewer pixels and are faster; Quality modes are sharper. Native AA and DLAA draw at the screen's resolution and only smooth the edges.";
   private static final String MODE_BRIEF = "Faster with fewer pixels, or sharper with more.";
   private static final String DOF_TEXT = "Blurs what lies well behind the surface you look at, as a camera's lens does.";
   private static final String DOF_BRIEF = "Blurs what is far behind the point you look at.";
   private static final String FLARE_TEXT = "Glare, a halo and ghosts where the sun or a bright light shines into the view.";
   private static final String FLARE_BRIEF = "Glare from the sun and from bright lights.";
   private static final String MATERIALS_TEXT = "Gives the ground and the walls the bumps and the shine of what they are made of: rough grass, soil and brick, smooth tiles and concrete. Off, they show the game's flat art, which is faster.";
   private static final String MATERIALS_BRIEF = "Bumps and shine on the ground and the walls.";

   static void scaling(List<OnboardingRows.Row> var0, ShaderPack var1, ShaderPack var2) {
      slot(var0, var1, var2, PackRows::resolutionScaling);
      List<OnboardingRows.Row> var3 = var1 == null ? List.of() : modes(var1);
      List<OnboardingRows.Row> var4 = var2 == null ? List.of() : modes(var2);
      if (!var3.isEmpty()) {
         var0.addAll(var3);
      } else if (!var4.isEmpty()) {
         var0.add(off(var4.get(0)));
      }
   }

   static void antialiasing(List<OnboardingRows.Row> var0, ShaderPack var1, ShaderPack var2) {
      slot(
              var0,
              var1,
              var2,
              var0x -> choice(
                      var0x,
                      "antialiasing",
                      "Anti-aliasing",
                      "Smooths jagged edges. TAA+ blends each frame with the ones before: the steadiest image. TAA uses fewer frames. FXAA and SMAA smooth each frame on its own: sharp, but edges can still shimmer. When resolution scaling is on, this setting is off: the upscaler smooths the edges.",
                      "Smooths jagged edges and shimmer."
              )
      );
   }

   static void light(List<OnboardingRows.Row> var0, ShaderPack var1, ShaderPack var2) {
      slot(
              var0,
              var1,
              var2,
              var0x -> graded(
                      var0x,
                      "Shadows",
                      "Sun, moon, lamp and flashlight shadows: how sharp, how far, and how many lamps cast them.",
                      "Shadows from the sun, the moon, the lamps and the flashlight.",
                      OnboardingRows.DRAWN,
                      SHADOWS
              )
      );
      slot(
              var0,
              var1,
              var2,
              var0x -> graded(
                      var0x,
                      "Volumetric light",
                      "Light in the air: haze, lamp halos, the flashlight's beam and the sun's shafts.",
                      "Light in the air: haze, halos and light shafts.",
                      OnboardingRows.GPU,
                      "volumetrics",
                      "godrays",
                      "volumetricSteps"
              )
      );
      slot(
              var0,
              var1,
              var2,
              var0x -> levels(
                      var0x,
                      "Global illumination",
                      "Light that bounces off the lit floors and walls onto what is round them, and off the place the flashlight lights. A higher level takes the bounced light farther.",
                      "Light that bounces off lit surfaces.",
                      "bounceLight",
                      "bounceReach",
                      REACHES,
                      LEVELS
              )
      );
      slot(
              var0,
              var1,
              var2,
              var0x -> levels(
                      var0x,
                      "Ambient occlusion",
                      "Shade in corners, in creases and under things. A higher level makes the shade smoother and more exact.",
                      "Shade in corners and under objects.",
                      "occlusion",
                      "occlusionQuality",
                      QUALITIES,
                      LEVELS
              )
      );
      slot(
              var0,
              var1,
              var2,
              var0x -> graded(
                      var0x,
                      "Clouds",
                      "Volumetric clouds, and how finely they are drawn.",
                      "3D clouds, and their detail.",
                      OnboardingRows.GPU,
                      "clouds",
                      "cloudResolution",
                      "cloudSteps",
                      "softClouds"
              )
      );
   }

   static void effects(List<OnboardingRows.Row> var0, ShaderPack var1, ShaderPack var2) {
      slot(
              var0,
              var1,
              var2,
              var0x -> levels(
                      var0x,
                      "Motion blur",
                      "Blurs the picture as you turn and as things move. A higher level makes the blur smoother.",
                      "Blurs the picture when you turn or things move.",
                      "motionBlur",
                      "motionBlurSamples",
                      SAMPLES,
                      EFFECT_LEVELS
              )
      );
      slot(
              var0,
              var1,
              var2,
              var0x -> toggle(
                      var0x,
                      "Depth of field",
                      "Blurs what lies well behind the surface you look at, as a camera's lens does.",
                      "Blurs what is far behind the point you look at.",
                      "depthOfField"
              )
      );
      slot(
              var0,
              var1,
              var2,
              var0x -> toggle(
                      var0x,
                      "Lens flare",
                      "Glare, a halo and ghosts where the sun or a bright light shines into the view.",
                      "Glare from the sun and from bright lights.",
                      "lensFlare"
              )
      );
      slot(var0, var1, var2, var0x -> toggle(var0x, "Bloom", "Bright lights glow over what is round them.", "Bright lights glow.", "bloom"));
   }

   static void materials(List<OnboardingRows.Row> var0, ShaderPack var1, ShaderPack var2) {
      slot(
              var0,
              var1,
              var2,
              var0x -> toggle(
                      var0x,
                      "Material textures",
                      "Gives the ground and the walls the bumps and the shine of what they are made of: rough grass, soil and brick, smooth tiles and concrete. Off, they show the game's flat art, which is faster.",
                      "Bumps and shine on the ground and the walls.",
                      "groundMaterials",
                      "wallMaterials"
              )
      );
   }

   private static void slot(List<OnboardingRows.Row> var0, ShaderPack var1, ShaderPack var2, Function<ShaderPack, OnboardingRows.Row> var3) {
      OnboardingRows.Row var4 = var1 == null ? null : (OnboardingRows.Row)var3.apply(var1);
      if (var4 == null && var2 != null) {
         OnboardingRows.Row var5 = (OnboardingRows.Row)var3.apply(var2);
         var4 = var5 == null ? null : off(var5);
      }

      if (var4 != null) {
         var0.add(var4);
      }
   }

   private static OnboardingRows.Row off(OnboardingRows.Row var0) {
      return new OnboardingRows.Row(var0.label(), var0.description(), var0.brief(), List.of(), var0.parts(), true);
   }

   private static OnboardingRows.Row resolutionScaling(ShaderPack var0) {
      PackOption var1 = var0.option("upscaling");
      PackOption var2 = var0.option("renderScale");
      if (var1 == null) {
         return null;
      }

      String var3 = Graphics.key(var0, var1);
      ArrayList<OnboardingRows.Level> var4 = new ArrayList<>();

      for (int var5 = 0; var5 < var1.values.size(); var5++) {
         String var6 = var1.values.get(var5);
         LinkedHashMap<String, String> var7 = new LinkedHashMap<>();
         var7.put(var3, var6);
         if (var2 != null) {
            var7.put(Graphics.key(var0, var2), STARTING_SCALE.getOrDefault(var6, var2.fallback));
         }

         var4.add(new OnboardingRows.Level(var1.labels.get(var5), var7, Set.of(var3)));
      }

      return new OnboardingRows.Row(
              "Resolution scaling",
              "Uses an upscaler from a graphics card maker in place of the anti-aliasing. The upscaler smooths edges and makes a lower render resolution fill the screen. FSR 3 is from AMD, XeSS is from Intel, DLSS is from NVIDIA (RTX cards only). Choose its mode below.",
              "An upscaler from AMD, Intel or NVIDIA.",
              var4,
              OnboardingRows.GPU,
              false
      );
   }

   private static List<OnboardingRows.Row> modes(ShaderPack var0) {
      PackOption var1 = var0.option("upscaling");
      PackOption var2 = var0.option("renderScale");
      if (var1 != null && var2 != null) {
         String var3 = Graphics.key(var0, var1);
         String var4 = Graphics.key(var0, var2);
         ArrayList<OnboardingRows.Row> var5 = new ArrayList<>();

         for (String var7 : var1.values) {
            ArrayList<OnboardingRows.Level> var8 = new ArrayList<>();

            for (String[] var12 : MODES.getOrDefault(var7, new String[0][])) {
               if (var2.values.contains(var12[1])) {
                  var8.add(OnboardingRows.level(var12[0], var4, var12[1]));
               }
            }

            boolean var13 = var7.equals("off");
            var5.add(
                    new OnboardingRows.Row(
                            "Scaling mode",
                            "The resolution the upscaler draws the world at before it fills the screen. Performance modes draw fewer pixels and are faster; Quality modes are sharper. Native AA and DLAA draw at the screen's resolution and only smooth the edges.",
                            "Faster with fewer pixels, or sharper with more.",
                            var8,
                            OnboardingRows.GPU,
                            false,
                            var1x -> var13,
                            var3x -> var7.equals(Objects.requireNonNullElse(var3x.get(var3), var1.fallback))
                    )
            );
         }

         return var5;
      } else {
         return List.of();
      }
   }

   private static OnboardingRows.Row levels(ShaderPack var0, String var1, String var2, String var3, String var4, String var5, String[] var6, String[] var7) {
      PackOption var8 = var0.option(var4);
      PackOption var9 = var0.option(var5);
      if (var8 == null) {
         return null;
      }

      String var10 = Graphics.key(var0, var8);
      if (var9 == null) {
         return toggle(var0, var1, var2, var3, var4);
      }

      String var11 = Graphics.key(var0, var9);
      ArrayList<OnboardingRows.Level> var12 = new ArrayList<>(List.of(OnboardingRows.level(var7[0], var10, "false")));

      for (int var13 = 0; var13 < var6.length; var13++) {
         if (var9.values.contains(var6[var13])) {
            LinkedHashMap<String, String> var14 = new LinkedHashMap<>();
            var14.put(var10, "true");
            var14.put(var11, var6[var13]);
            var12.add(new OnboardingRows.Level(var7[var13 + 1], var14));
         }
      }

      return new OnboardingRows.Row(var1, var2, var3, var12, OnboardingRows.GPU, false);
   }

   private static OnboardingRows.Row graded(ShaderPack var0, String var1, String var2, String var3, Set<LoadModel.Part> var4, String... var5) {
      List<PackOption> var6 = options(var0, var5);
      if (var6.isEmpty()) {
         return null;
      }

      ArrayList<OnboardingRows.Level> var7 = new ArrayList<>();

      for (int var8 = 0; var8 < LEVELS.length; var8++) {
         PackPreset var9 = PresetAdvice.presetNamed(var0, GraphicsPresets.NAMES[var8]);
         if (var9 != null) {
            LinkedHashMap<String, String> var10 = new LinkedHashMap<>();
            var6.forEach(var3x -> var10.put(Graphics.key(var0, var3x), var9.valueFor(var3x)));
            OnboardingRows.distinct(var7, new OnboardingRows.Level(LEVELS[var8], var10));
         }
      }

      return var7.size() < 2 ? null : new OnboardingRows.Row(var1, var2, var3, var7, var4, false);
   }

   private static OnboardingRows.Row choice(ShaderPack var0, String var1, String var2, String var3, String var4) {
      PackOption var5 = var0.option(var1);
      if (var5 == null) {
         return null;
      }

      String var6 = Graphics.key(var0, var5);
      ArrayList<OnboardingRows.Level> var7 = new ArrayList<>();

      for (int var8 = 0; var8 < var5.values.size(); var8++) {
         var7.add(OnboardingRows.level(var5.labels.get(var8), var6, var5.values.get(var8)));
      }

      PackOption var10 = var5.disabledBy.isEmpty() ? null : var0.option(var5.disabledBy);
      if (var10 == null) {
         return new OnboardingRows.Row(var2, var3, var4, var7, OnboardingRows.GPU, false);
      }

      String var9 = Graphics.key(var0, var10);
      return new OnboardingRows.Row(
              var2, var3, var4, var7, OnboardingRows.GPU, false, var2x -> var10.on(Objects.requireNonNullElse(var2x.get(var9), var10.fallback))
      );
   }

   private static OnboardingRows.Row toggle(ShaderPack var0, String var1, String var2, String var3, String... var4) {
      List<PackOption> var5 = options(var0, var4);
      if (var5.isEmpty()) {
         return null;
      }

      LinkedHashMap<String, String> var6 = new LinkedHashMap<>();
      LinkedHashMap<String, String> var7 = new LinkedHashMap<>();

      for (PackOption var9 : var5) {
         var6.put(Graphics.key(var0, var9), "false");
         var7.put(Graphics.key(var0, var9), "true");
      }

      return new OnboardingRows.Row(
              var1, var2, var3, List.of(new OnboardingRows.Level("Off", var6), new OnboardingRows.Level("On", var7)), OnboardingRows.GPU, false
      );
   }

   private static List<PackOption> options(ShaderPack var0, String... var1) {
      ArrayList<PackOption> var2 = new ArrayList<>();

      for (String var6 : var1) {
         PackOption var7 = var0.option(var6);
         if (var7 != null) {
            var2.add(var7);
         }
      }

      return var2;
   }

   private PackRows() {
   }
}