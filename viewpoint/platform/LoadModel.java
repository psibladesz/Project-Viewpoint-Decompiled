package viewpoint.platform;

import java.util.List;

final class LoadModel {
   static final double FRAME_MS = 16.666666666666668;
   private static final double GEOMETRY_MS = 2.3;
   private static final double SCREEN_MS = 1.25;
   private static final double VANILLA_SCREEN_MS = 0.45;
   private static final double SHELL_MS = 0.4;
   private static final double BOXES_MS = 0.1;
   private static final double TREES_MS = 0.2;
   private static final double CASCADE_MS = 0.25;
   private static final double FAR_MAPS_MS = 0.1;
   private static final double LAMP_MS = 0.05;
   private static final double FLASHLIGHT_MS = 0.05;
   private static final double AIR_MS = 0.35;
   private static final double AIR_STEP_MS = 0.035;
   private static final double CLOUD_MS = 0.05;
   private static final double CLOUD_BODY_MS = 0.2;
   private static final double BLUR_MS = 0.12;
   private static final double BLUR_TAP_MS = 0.02;
   private static final double DOF_MS = 0.15;
   private static final double DOF_TAP_MS = 0.015;
   private static final double FLARE_MS = 0.08;
   private static final double BOUNCE_MS = 0.15;
   private static final double NO_GODRAYS = 0.7;
   private static final double[] ANTIALIASING_MS = new double[]{0.0, 0.15, 0.3, 0.4, 0.55, 0.42};
   private static final double[] UPSCALING_MS = new double[]{0.0, 0.9, 1.1, 0.9};
   private static final double[] OCCLUSION_MS = new double[]{0.25, 0.37, 0.56, 1.3};
   private static final String[] OCCLUSION_QUALITIES = new String[]{"low", "medium", "high", "ultra"};
   private static final double REF_SHELL = 12.0;
   private static final double REF_FAR = 32.0;
   private static final double REF_TREES = 48.0;
   private static final double REF_SHARE = 0.5;
   private static final double REF_CASCADE = 2048.0;
   private static final double REF_LAMP = 512.0;
   private static final double REF_CLOUD_RES = 50.0;
   private static final double REF_CLOUD_STEPS = 40.0;
   private static final double REF_FLOOR_CHUNKS = 8.0;
   private static final double REF_FLOOR_TEXELS = 48.0;
   private static final double ART_FLOOR_TEXELS = 64.0;
   private static final double TARGETS_MIB = 220.0;
   private static final double VANILLA_TARGETS_MIB = 90.0;
   private static final double NEAR_MIB = 250.0;
   private static final double LIGHT_MIB = 20.0;
   private static final double SHELL_MIB = 500.0;
   private static final double FAR_CELLS_MIB = 130.0;
   private static final double FLOORS_MIB = 320.0;
   private static final double MODELS_MIB = 40.0;
   private static final double FAR_MAPS_MIB = 32.0;
   private static final double MIB = 1048576.0;
   private static final double DESKTOP_MIB = 400.0;
   private static final double GAME_VRAM_MIB = 300.0;
   static final double COMPRESSED_SHARE = 0.35;
   private static final double[] CASCADE_LAYERS = new double[]{3.56, 7.12, 8.12};
   private static final double NATIVE_MIB = 150.0;
   private static final double FAR_DATA_MIB = 150.0;
   private static final double LIGHT_HEAP_MIB = 100.0;
   private static final double CACHE_MIB = 95.0;
   private static final double MIN_REACH = 0.3;
   private static final double GAME_HEAP_MIB = 1800.0;
   private static final double MIN_HEAP_MIB = 256.0;
   private static final double RENDER_MS = 1.5;
   private static final double GBUFFER_CPU_MS = 1.8;
   private static final double FAR_CPU_MS = 0.7;
   private static final double FLOORS_CPU_MS = 0.2;
   private static final double SHADOW_CPU_MS = 0.9;
   private static final double FAR_SHADOW_CPU_MS = 0.15;
   private static final double LAMP_CPU_MS = 0.1;
   private static final double MAIN_MS = 0.8;
   private static final double MAIN_FAR_MS = 0.35;
   private static final double CHARACTERS_MS = 0.2;
   private static final double PACKS_CPU_MS = 0.2;
   private static final double GAME_MAIN_MS = 6.0;
   private static final double GAME_RENDER_MS = 2.0;
   private static final double REF_CHARACTERS = 32.0;
   private static final double REF_RING = 6.0;

   static LoadModel.Load estimate(LoadModel.Values var0, Hardware var1) {
      LoadModel.Settings var2 = new LoadModel.Settings(var0);
      double var3 = vram(var2, var1);
      return new LoadModel.Load(cpu(var2, var1), ram(var2, var1, var3), gpu(var2, var1), var3);
   }

   static LoadModel.Load budget(Hardware var0) {
      double var1 = slower(var0.threads());
      double var3 = Math.max(16.666666666666668 - 2.0 * var1, 1.0);
      return new LoadModel.Load(var3, Math.max(var0.ramFreeMiB(), 256.0), 16.666666666666668, vramRoom(var0));
   }

   static LoadModel.Shares shares(LoadModel.Part var0, LoadModel.Load var1, Hardware var2) {
      return switch (var0) {
         case CPU -> new LoadModel.Shares(16.666666666666668, 16.666666666666668 - budget(var2).cpuMs(), 0.0, var1.cpuMs());
         case MEMORY -> memoryShares(var1, var2);
         case GPU -> new LoadModel.Shares(16.666666666666668, 0.0, 0.0, var1.gpuMs());
         case VIDEO_MEMORY -> videoShares(var1, var2);
      };
   }

   private static LoadModel.Shares memoryShares(LoadModel.Load var0, Hardware var1) {
      double var2 = var1.ramMiB();
      double var4 = Math.max(0.0, var2 - var1.ramFreeMiB());
      double var6 = Math.min(var1.processMiB(), var4);
      return new LoadModel.Shares(var2, var6, var4 - var6, var0.ramMiB());
   }

   private static LoadModel.Shares videoShares(LoadModel.Load var0, Hardware var1) {
      double var2 = vramRoom(var1);
      double var4 = var1.vramMiB();
      if (var4 <= 0.0) {
         return new LoadModel.Shares(var2, 0.0, 0.0, var0.vramMiB());
      }

      double var6 = Math.max(0.0, var4 - var2);
      double var8 = var1.gameTexturesMiB() > 0L ? Math.min(gameTextures(var1) + 300.0, var6) : 0.0;
      return new LoadModel.Shares(var4, var8, var6 - var8, var0.vramMiB());
   }

   static double vramRoom(Hardware var0) {
      if (var0.vramFreeMiB() > 0) {
         return var0.vramFreeMiB() + var0.isoChunksMiB();
      } else if (var0.vramMiB() > 0 && var0.gameTexturesMiB() > 0L) {
         double var1 = gameTextures(var0) + 300.0 + 400.0;
         return Math.max(0.0, var0.vramMiB() - var1);
      } else {
         return var0.vramMiB() > 0 ? var0.vramMiB() * 0.6 : (var0.integrated() ? 1024.0 : 2048.0);
      }
   }

   static double gameTextures(Hardware var0) {
      return var0.gameTexturesMiB() * (var0.textureCompression() ? 0.35 : 1.0);
   }

   static double stress(LoadModel.Load var0, LoadModel.Load var1, LoadModel.Part var2, LoadModel.Values var3, Hardware var4) {
      double var5 = var0.of(var2) / Math.max(var1.of(var2), 0.001);
      if (var2 == LoadModel.Part.MEMORY) {
         double var11 = Math.max(var4.heapMiB() - 1800.0, 256.0);
         return Math.max(var5, heap(new LoadModel.Settings(var3)) / var11);
      } else if (var2 == LoadModel.Part.CPU) {
         double var7 = slower(var4.threads());
         double var9 = Math.max(16.666666666666668 - 6.0 * var7, 1.0);
         return Math.max(var5, main(new LoadModel.Settings(var3)) * var7 / var9);
      } else {
         return var5;
      }
   }

   static double score(Hardware var0) {
      double var1 = var0.gpuScore();
      if (!Double.isNaN(var1)) {
         return var1;
      }

      int var3 = var0.vramMiB();
      return var0.integrated() ? 0.1 : (var3 >= 8000 ? 1.0 : (var3 >= 6000 ? 0.8 : (var3 >= 3500 ? 0.45 : 0.3)));
   }

   static double slower(int var0) {
      return var0 >= 12 ? 1.0 : (var0 >= 8 ? 1.15 : (var0 >= 6 ? 1.3 : (var0 >= 4 ? 1.6 : 2.2)));
   }

   private static double gpu(LoadModel.Settings var0, Hardware var1) {
      double var2 = var1.pixels() * square(var0.num("renderScale", 100.0) / 100.0);
      double var4 = 2.3 * (0.5 + 0.5 * var2) + far(var0);
      if (!var0.modern()) {
         return (var4 + (0.45 + antialiasing(var0)) * var2) / score(var1);
      }

      double var6 = 1.25 + air(var0) + clouds(var0) + effects(var0);
      return (var4 + shadows(var0) + var6 * var2) / score(var1);
   }

   private static double far(LoadModel.Settings var0) {
      double var1 = square(var0.num("lod.shellLiteBlocks", 10.0) / 12.0);
      double var3 = square(var0.num("lod.farBlocks", 32.0) / 32.0);
      double var5 = var0.num("lod.treeShare", 0.5) / 0.5 * var0.num("lod.treesGoneBlocks", 36.0) / 48.0;
      return 0.4 * var1 + 0.1 * var3 + 0.2 * var5;
   }

   private static double shadows(LoadModel.Settings var0) {
      double var1 = var0.on("sunShadows", true) ? 0.25 * var0.num("shadowCascades", 3.0) * Math.pow(var0.num("shadowResolution", 2048.0) / 2048.0, 1.5) : 0.0;
      double var3 = var0.num("lampShadows", 2.0) * 0.05 * square(var0.num("lampShadowResolution", 512.0) / 512.0);
      return var1 + var3 + (var0.on("farShadows", true) ? 0.1 : 0.0) + (var0.on("flashlightShadow", true) ? 0.05 : 0.0);
   }

   private static double air(LoadModel.Settings var0) {
      if (!var0.on("volumetrics", true)) {
         return 0.0;
      }

      double var1 = 0.35 + 0.035 * var0.num("volumetricSteps", 16.0);
      return var0.on("godrays", true) ? var1 : var1 * 0.7;
   }

   private static double clouds(LoadModel.Settings var0) {
      if (!var0.on("clouds", true)) {
         return 0.0;
      }

      double var1 = square(var0.num("cloudResolution", 50.0) / 50.0);
      return 0.05 + 0.2 * var1 * var0.num("cloudSteps", 30.0) / 40.0;
   }

   private static double effects(LoadModel.Settings var0) {
      double var1 = antialiasing(var0);
      double var3 = var0.on("motionBlur", true) ? 0.12 + 0.02 * var0.num("motionBlurSamples", 6.0) : 0.0;
      double var5 = var0.on("depthOfField", false) ? 0.15 + 0.015 * var0.num("dofSamples", 24.0) : 0.0;
      return var1
         + var3
         + var5
         + (var0.on("lensFlare", true) ? 0.08 : 0.0)
         + (var0.on("occlusion", false) ? occlusion(var0) : 0.0)
         + (var0.on("bounceLight", false) ? 0.15 : 0.0);
   }

   private static double occlusion(LoadModel.Settings var0) {
      int var1 = List.of(OCCLUSION_QUALITIES).indexOf(var0.word("occlusionQuality", "high"));
      return OCCLUSION_MS[var1 < 0 ? 2 : var1];
   }

   private static double vram(LoadModel.Settings var0, Hardware var1) {
      double var2 = square(var0.num("floors.detailChunks", 8.0) / 8.0) * square(var0.num("floors.detailShare", 1.0) * 64.0 / 48.0);
      double var4 = buffers(var0) + Math.min(var0.num("memory.lightAtlasMiB", 64.0), 20.0) + Math.min(var0.num("memory.floorMiB", 512.0), 320.0 * var2);
      double var6 = (var0.modern() ? 220.0 : 90.0) * var1.pixels() * square(var0.num("renderScale", 100.0) / 100.0);
      return var6 + var4 + (var0.modern() ? shadowMemory(var0) : 0.0);
   }

   private static double buffers(LoadModel.Settings var0) {
      double var1 = square(var0.num("lod.farBlocks", 32.0) / 32.0);
      double var3 = square(var0.num("lod.shellLiteBlocks", 10.0) / 12.0);
      return Math.min(var0.num("memory.nearMeshMiB", 512.0), 250.0)
         + Math.min(var0.num("memory.shellMiB", 768.0), 500.0 * var3)
         + Math.min(var0.num("memory.farMiB", 192.0), 130.0 * var1)
         + Math.min(var0.num("memory.modelMeshMiB", 256.0), 40.0);
   }

   private static double shadowMemory(LoadModel.Settings var0) {
      double var1 = 0.0;
      if (var0.on("sunShadows", true)) {
         int var3 = (int)Math.max(1.0, Math.min(3.0, var0.num("shadowCascades", 3.0)));
         var1 = square(var0.num("shadowResolution", 2048.0)) * 4.0 * CASCADE_LAYERS[var3 - 1] / 1048576.0;
      }

      double var5 = var0.num("lampShadows", 2.0) * 12.0 * square(var0.num("lampShadowResolution", 512.0)) * 4.0 / 1048576.0;
      return var1 + var5 + (var0.on("farShadows", true) ? 32.0 : 0.0);
   }

   private static double ram(LoadModel.Settings var0, Hardware var1, double var2) {
      double var4 = Math.max(0.0, var2 - budget(var1).vramMiB());
      return buffers(var0) + 150.0 + var4;
   }

   private static double heap(LoadModel.Settings var0) {
      double var1 = square(var0.num("lod.farBlocks", 32.0) / 32.0);
      return Math.min(var0.num("memory.cpuCacheMiB", 320.0), 95.0 * Math.max(var1, 0.3)) + 150.0 * var1 + 100.0;
   }

   private static double cpu(LoadModel.Settings var0, Hardware var1) {
      double var2 = square(var0.num("lod.farBlocks", 32.0) / 32.0);
      double var4 = square(var0.num("lod.shellLiteBlocks", 10.0) / 12.0);
      double var6 = 3.5 + 0.7 * (var2 + var4) / 2.0;
      if (var0.modern() && var0.on("sunShadows", true)) {
         var6 += 0.9 * var0.num("shadowCascades", 3.0) / 3.0 + (var0.on("farShadows", true) ? 0.15 : 0.0);
      }

      if (var0.modern()) {
         var6 += 0.1 * var0.num("lampShadows", 2.0);
      }

      return var6 * slower(var1.threads());
   }

   private static double main(LoadModel.Settings var0) {
      double var1 = square(var0.num("lod.farBlocks", 32.0) / 32.0);
      return 0.8
         + 0.35 * var1
         + 0.2 * var0.num("lod.fullRateCharacters", 32.0) / 32.0
         + (var0.on(Graphics.MODEL_PACKS.key, true) ? 0.2 * square(var0.num("lod.packRing", 6.0) / 6.0) : 0.0);
   }

   private static double antialiasing(LoadModel.Settings var0) {
      int var1 = Pipeline.UPSCALING_MODES.indexOf(var0.word("upscaling", "off"));
      if (var1 > 0) {
         return UPSCALING_MS[var1];
      }

      int var2 = Pipeline.ANTIALIASING_MODES.indexOf(var0.word("antialiasing", "taaPlus"));
      return ANTIALIASING_MS[Math.max(var2, 0)];
   }

   private static double square(double var0) {
      return var0 * var0;
   }

   private LoadModel() {
   }

   record Load(double cpuMs, double ramMiB, double gpuMs, double vramMiB) {
      double of(LoadModel.Part var1) {
         return switch (var1) {
            case CPU -> this.cpuMs;
            case MEMORY -> this.ramMiB;
            case GPU -> this.gpuMs;
            case VIDEO_MEMORY -> this.vramMiB;
         };
      }
   }

   enum Part {
      CPU("Processor (CPU)"),
      MEMORY("Memory (RAM)"),
      GPU("Graphics card (GPU)"),
      VIDEO_MEMORY("Video memory (VRAM)");

      final String label;

      Part(String nullxx) {
         this.label = nullxx;
      }
   }

   private record Settings(LoadModel.Values values) {
      boolean modern() {
         return !"Vanilla".equalsIgnoreCase(this.text("graphics.mode"));
      }

      double num(String var1, double var2) {
         try {
            String var4 = this.text(var1.indexOf(46) < 0 ? this.option(var1) : var1);
            return var4 == null ? var2 : Double.parseDouble(var4.trim());
         } catch (NumberFormatException var5) {
            return var2;
         }
      }

      boolean on(String var1, boolean var2) {
         String var3 = this.text(var1.indexOf(46) < 0 ? this.option(var1) : var1);
         return var3 == null ? var2 : Boolean.parseBoolean(var3.trim());
      }

      String word(String var1, String var2) {
         String var3 = this.text(this.option(var1));
         return var3 == null ? var2 : var3.trim();
      }

      private String option(String var1) {
         String var2 = this.modern() ? this.text("graphics.pack") : "normal";
         return "pack." + (var2 == null ? "default" : var2) + "." + var1;
      }

      private String text(String var1) {
         return this.values.get(var1);
      }
   }

   record Shares(double whole, double game, double others, double ours) {
      double free() {
         return Math.max(0.0, this.whole - this.game - this.others);
      }
   }

   interface Values {
      String get(String var1);
   }
}
