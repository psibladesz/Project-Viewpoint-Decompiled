package viewpoint.platform;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class Pipeline {
   public static final List<String> ANTIALIASING_MODES = List.of(Pipeline.Knob.ANTIALIASING.names);
   public static final List<String> UPSCALING_MODES = List.of(Pipeline.Knob.UPSCALING.names);
   private static final int FXAA = 1;
   private static final int SMAA = 2;
   private static final int TAA = 3;
   private static final int TAA_FXAA = 4;
   private static final int TAA_PLUS = 5;
   private static final int[] REFLECTION_SCALE = new int[]{0, 4, 2, 2, 1};
   private static final int[] REFLECTION_STEPS = new int[]{0, 24, 32, 48, 64};
   public static final Pipeline LIBRARY = of(new EnumMap<>(Pipeline.Knob.class));
   public final boolean shadows;
   public final boolean flashlightShadow;
   public final boolean farShadows;
   public final boolean farLamps;
   public final boolean clouds;
   public final boolean volumetrics;
   public final boolean bloom;
   public final boolean taa;
   public final boolean taaCounts;
   public final boolean fxaa;
   public final boolean smaa;
   public final int upscaler;
   public final int renderScale;
   public final boolean occlusion;
   public final boolean bounce;
   public final boolean puddles;
   public final boolean atmosphere;
   public final boolean floorSurfaces;
   public final boolean wallSurfaces;
   public final boolean eye;
   public final boolean froxels;
   public final int froxelSteps;
   public final boolean sunShafts;
   public final boolean cloudShadowMap;
   public final boolean reflections;
   public final boolean reflectionClouds;
   public final int reflectionScale;
   public final int reflectionSteps;
   public final boolean water;
   public final boolean waterFft;
   public final boolean rainGround;
   public final int shadowResolution;
   public final int shadowCascades;
   public final int lampShadows;
   public final int lampShadowResolution;
   public final int headlightShadows;
   public final int cloudResolution;
   public final int cloudSteps;
   public final int rainDensity;
   public final boolean softClouds;
   final String defines;

   private Pipeline(int[] var1) {
      this.shadows = var1[Pipeline.Knob.SHADOWS.ordinal()] != 0;
      this.shadowResolution = var1[Pipeline.Knob.SHADOW_RESOLUTION.ordinal()];
      this.shadowCascades = var1[Pipeline.Knob.SHADOW_CASCADES.ordinal()];
      this.flashlightShadow = var1[Pipeline.Knob.FLASHLIGHT_SHADOW.ordinal()] != 0;
      this.lampShadows = var1[Pipeline.Knob.LAMP_SHADOWS.ordinal()];
      this.lampShadowResolution = var1[Pipeline.Knob.LAMP_SHADOW_RESOLUTION.ordinal()];
      this.headlightShadows = var1[Pipeline.Knob.HEADLIGHT_SHADOWS.ordinal()];
      this.farShadows = var1[Pipeline.Knob.FAR_SHADOWS.ordinal()] != 0;
      this.farLamps = var1[Pipeline.Knob.FAR_LAMPS.ordinal()] != 0;
      this.clouds = var1[Pipeline.Knob.CLOUDS.ordinal()] != 0;
      this.cloudResolution = var1[Pipeline.Knob.CLOUD_RESOLUTION.ordinal()];
      this.cloudSteps = var1[Pipeline.Knob.CLOUD_STEPS.ordinal()];
      this.softClouds = var1[Pipeline.Knob.SOFT_CLOUDS.ordinal()] != 0;
      this.cloudShadowMap = var1[Pipeline.Knob.CLOUD_SHADOW_MAP.ordinal()] != 0;
      this.atmosphere = var1[Pipeline.Knob.ATMOSPHERE.ordinal()] != 0;
      this.eye = var1[Pipeline.Knob.EYE.ordinal()] != 0;
      this.rainDensity = var1[Pipeline.Knob.RAIN_DENSITY.ordinal()];
      this.volumetrics = var1[Pipeline.Knob.VOLUMETRICS.ordinal()] != 0;
      this.froxels = var1[Pipeline.Knob.FROXELS.ordinal()] != 0;
      this.froxelSteps = var1[Pipeline.Knob.FROXEL_STEPS.ordinal()];
      this.sunShafts = var1[Pipeline.Knob.SUN_SHAFTS.ordinal()] != 0;
      this.occlusion = var1[Pipeline.Knob.OCCLUSION.ordinal()] != 0;
      this.bounce = var1[Pipeline.Knob.BOUNCE.ordinal()] != 0;
      this.bloom = var1[Pipeline.Knob.BLOOM.ordinal()] != 0;
      this.upscaler = var1[Pipeline.Knob.UPSCALING.ordinal()];
      int var2 = this.upscaler != 0 ? 0 : var1[Pipeline.Knob.ANTIALIASING.ordinal()];
      this.taa = var2 == 3 || var2 == 4 || var2 == 5;
      this.taaCounts = var2 == 5;
      this.fxaa = var2 == 1 || var2 == 4;
      this.smaa = var2 == 2;
      this.renderScale = var1[Pipeline.Knob.RENDER_SCALE.ordinal()];
      this.puddles = var1[Pipeline.Knob.PUDDLES.ordinal()] != 0;
      this.floorSurfaces = var1[Pipeline.Knob.FLOOR_SURFACES.ordinal()] != 0;
      this.wallSurfaces = var1[Pipeline.Knob.WALL_SURFACES.ordinal()] != 0;
      int var3 = var1[Pipeline.Knob.REFLECTIONS.ordinal()];
      this.reflections = var3 != 0;
      this.reflectionScale = REFLECTION_SCALE[var3];
      this.reflectionSteps = REFLECTION_STEPS[var3];
      this.reflectionClouds = var1[Pipeline.Knob.REFLECTION_CLOUDS.ordinal()] != 0;
      this.rainGround = var1[Pipeline.Knob.RAIN_GROUND.ordinal()] != 0;
      this.water = var1[Pipeline.Knob.WATER.ordinal()] != 0;
      this.waterFft = var1[Pipeline.Knob.WATER_FFT.ordinal()] != 0;
      StringBuilder var4 = new StringBuilder();

      for (Pipeline.Knob var8 : Pipeline.Knob.values()) {
         var4.append("#define PIPELINE_").append(var8.name()).append(' ').append(var1[var8.ordinal()]).append('\n');
      }

      this.defines = var4.append("#define PIPELINE_TAA ").append(!this.taa && this.upscaler == 0 ? 0 : 1).append('\n').toString();
   }

   static Pipeline of(Map<Pipeline.Knob, String> var0) {
      int[] var1 = new int[Pipeline.Knob.values().length];

      for (Pipeline.Knob var5 : Pipeline.Knob.values()) {
         var1[var5.ordinal()] = var5.parse(var0.getOrDefault(var5, var5.library));
      }

      return new Pipeline(var1);
   }

   public enum Knob {
      SHADOWS("shadows", "true"),
      SHADOW_RESOLUTION("shadowResolution", "2048", 256, 16384),
      SHADOW_CASCADES("shadowCascades", "3", 1, 3),
      FLASHLIGHT_SHADOW("flashlightShadow", "true"),
      LAMP_SHADOWS("lampShadows", "2", 0, 8),
      LAMP_SHADOW_RESOLUTION("lampShadowResolution", "512", 128, 2048),
      HEADLIGHT_SHADOWS("headlightShadows", "1", 0, 4),
      FAR_SHADOWS("farShadows", "true"),
      FAR_LAMPS("farLamps", "true"),
      CLOUDS("clouds", "true"),
      CLOUD_RESOLUTION("cloudResolution", "50", 25, 100),
      CLOUD_STEPS("cloudSteps", "40", 8, 128),
      SOFT_CLOUDS("softClouds", "false"),
      CLOUD_SHADOW_MAP("cloudShadowMap", "false"),
      ATMOSPHERE("atmosphere", "false"),
      EYE("eye", "false"),
      VOLUMETRICS("volumetrics", "true"),
      FROXELS("froxels", "false"),
      FROXEL_STEPS("froxelSteps", "16", 4, 64),
      SUN_SHAFTS("sunShafts", "false"),
      OCCLUSION("occlusion", "true"),
      BOUNCE("bounce", "true"),
      BLOOM("bloom", "true"),
      ANTIALIASING("antialiasing", "taaPlus", new String[]{"off", "fxaa", "smaa", "taa", "taaFxaa", "taaPlus"}),
      UPSCALING("upscaling", "off", new String[]{"off", "fsr3", "xess", "dlss"}),
      RENDER_SCALE("renderScale", "100", 25, 200),
      PUDDLES("puddles", "true"),
      FLOOR_SURFACES("floorSurfaces", "false"),
      WALL_SURFACES("wallSurfaces", "false"),
      REFLECTIONS("reflections", "off", new String[]{"off", "low", "medium", "high", "ultra"}),
      REFLECTION_CLOUDS("reflectionClouds", "false"),
      RAIN_GROUND("rainGround", "false"),
      WATER("water", "false"),
      WATER_FFT("waterFft", "false"),
      RAIN_DENSITY("rainDensity", "100", 0, 200);

      final String key;
      final String library;
      private final int min;
      private final int max;
      private final boolean toggle;
      private final String[] names;

      Knob(String nullxx, String nullxxx) {
         this(nullxx, nullxxx, 0, 1, true, new String[0]);
      }

      Knob(String nullxx, String nullxxx, int nullxxxx, int nullxxxxx) {
         this(nullxx, nullxxx, nullxxxx, nullxxxxx, false, new String[0]);
      }

      Knob(String nullxx, String nullxxx, String[] nullxxxx) {
         this(nullxx, nullxxx, 0, nullxxxx.length - 1, false, nullxxxx);
      }

      Knob(String nullxx, String nullxxx, int nullxxxx, int nullxxxxx, boolean nullxxxxxx, String[] nullxxxxxxx) {
         this.key = nullxx;
         this.library = nullxxx;
         this.min = nullxxxx;
         this.max = nullxxxxx;
         this.toggle = nullxxxxxx;
         this.names = nullxxxxxxx;
      }

      static Pipeline.Knob named(String var0) {
         for (Pipeline.Knob var4 : values()) {
            if (var4.key.equals(var0)) {
               return var4;
            }
         }

         return null;
      }

      int parse(String var1) {
         String var2 = var1.trim();
         if (this.names.length > 0) {
            for (int var5 = 0; var5 < this.names.length; var5++) {
               if (this.names[var5].equals(var2)) {
                  return var5;
               }
            }

            throw new IllegalArgumentException("pipeline." + this.key + " is one of " + String.join(", ", this.names) + ", not " + var1);
         } else if (this.toggle) {
            if (!var2.equals("true") && !var2.equals("false")) {
               throw new IllegalArgumentException("pipeline." + this.key + " is true or false, not " + var1);
            } else {
               return var2.equals("true") ? 1 : 0;
            }
         } else {
            try {
               int var3 = Integer.parseInt(var2);
               if (var3 >= this.min && var3 <= this.max) {
                  return var3;
               }
            } catch (NumberFormatException var4) {
            }

            throw new IllegalArgumentException("pipeline." + this.key + " is a whole number " + this.min + " to " + this.max + ", not " + var1);
         }
      }
   }
}
