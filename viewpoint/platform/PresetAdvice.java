package viewpoint.platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

final class PresetAdvice {
   static final int POTATO = 0;
   static final int LOW = 1;
   static final int DEFAULT = 2;
   static final int HIGH = 3;
   static final int ULTRA = 4;
   private static final double LOW_FPS = 45.0;
   private static final double FPS = 60.0;
   private static final double VRAM_ROOM = 0.9;
   private static final long[] RAM_NEEDS = new long[]{5500L, 7000L, 14000L, 28000L};
   private static final long[] HEAP_NEEDS = new long[]{1800L, 2800L, 2800L, 3900L};
   private static final int[] THREAD_NEEDS = new int[]{2, 4, 8, 12};
   private static final int THREADS_PER_WORKER = 4;
   private static final int MAX_WORKERS = 4;
   static final int COMPRESSION_BELOW_MIB = 11776;

   static PresetAdvice.Advice of(Hardware var0) {
      ArrayList<String> var1 = new ArrayList<>();
      byte var2 = 0;
      byte var3 = 0;
      byte var4 = 0;
      LoadModel.Load var5 = LoadModel.budget(var0);

      for (int var6 = 1; var6 <= 4; var6++) {
         LoadModel.Values var7 = values(var6);
         LoadModel.Load var8 = LoadModel.estimate(var7, var0);
         var2 = (byte)(var8.gpuMs() <= 1000.0 / (var6 == 1 ? 45.0 : 60.0) ? var6 : var2);
         var3 = (byte)(var8.vramMiB() <= var5.vramMiB() * 0.9 ? var6 : var3);
         boolean var9 = LoadModel.stress(var8, var5, LoadModel.Part.CPU, var7, var0) <= 1.0
                 && LoadModel.stress(var8, var5, LoadModel.Part.MEMORY, var7, var0) <= 1.0;
         var4 = (byte)(var9 ? var6 : var4);
      }

      int var13 = needed(var0.ramMiB(), RAM_NEEDS);
      int var14 = needed(var0.heapMiB(), HEAP_NEEDS);
      int var15 = needed(var0.threads(), THREAD_NEEDS);
      double var16 = LoadModel.score(var0);
      var1.add(
              String.format(
                      Locale.ROOT,
                      "Graphics card (GPU): %s, %s at %d x %d: %s",
                      var0.renderer().replaceAll("/.*", ""),
                      Double.isNaN(var0.gpuScore()) ? "not one we know, judged by its memory" : "about " + Math.round(var16 * 100.0) + "% of an RTX 3060",
                      var0.width(),
                      var0.height(),
                      allows(var2)
              )
      );
      var1.add(vramLine(var0, var3));
      var1.add(
              String.format(
                      Locale.ROOT,
                      "Memory (RAM): %s, %s free; the game's heap %s: %s",
                      gib(var0.ramMiB()),
                      gib(var0.ramFreeMiB()),
                      gib(var0.heapMiB()),
                      allows(Math.min(Math.min(var13, var14), var4))
              )
      );
      var1.add(String.format(Locale.ROOT, "Processor (CPU): %d threads: %s", var0.threads(), allows(var15)));
      int var11 = Math.min(Math.min(var2, var3), Math.min(Math.min(var13, var14), Math.min(var15, var4)));
      int var12 = Math.max(1, Math.min(4, var0.threads() / 4));
      return new PresetAdvice.Advice(var11, var2, List.copyOf(var1), var12);
   }

   static String look(PresetAdvice.Advice var0) {
      return var0.gpu() >= 3 ? "default" : "vivid";
   }

   static boolean compressionNeeded(Hardware var0) {
      int var1 = var0.vramMiB() > 0 ? var0.vramMiB() : var0.vramFreeMiB();
      return !var0.textureCompression() && var1 > 0 && var1 < 11776;
   }

   private static String vramLine(Hardware var0, int var1) {
      String var2 = var0.vramMiB() > 0
              ? gib(var0.vramMiB()) + (var0.vramFreeMiB() > 0 ? ", " + gib(var0.vramFreeMiB()) + " free" : "")
              : (var0.vramFreeMiB() > 0 ? gib(var0.vramFreeMiB()) + " free" : "not told by the driver");
      String var3 = var0.gameTexturesMiB() == 0L
              ? ""
              : String.format(
              Locale.ROOT,
              "; the game's textures %s, its Texture compression %s",
              gib(Math.round(LoadModel.gameTextures(var0))),
              var0.textureCompression() ? "on" : "off"
      );
      return "Video memory (VRAM): " + var2 + var3 + ": " + allows(var1);
   }

   static LoadModel.Values values(int var0) {
      HashMap<String, String> var1 = new HashMap<>();

      for (Entry<String, String[]> var3 : GraphicsPresets.TABLE.entrySet()) {
         var1.put(var3.getKey(), var3.getValue()[var0]);
      }

      boolean var7 = "Vanilla".equals(var1.get("graphics.mode"));
      ShaderPack var8 = ShaderPacks.find(var7 ? "normal" : var1.get("graphics.pack"));
      PackPreset var4 = var8 == null ? null : presetNamed(var8, GraphicsPresets.NAMES[var0]);
      if (var4 != null) {
         for (PackOption var6 : var8.options) {
            if (!var6.kept) {
               var1.put("pack." + var8.id + "." + var6.id, var4.valueFor(var6));
            }
         }
      }

      return var1::get;
   }

   static PackPreset presetNamed(ShaderPack var0, String var1) {
      for (PackPreset var3 : var0.presets) {
         if (var3.id.equalsIgnoreCase(var1) || var3.label.equalsIgnoreCase(var1)) {
            return var3;
         }
      }

      return null;
   }

   private static int needed(long var0, long[] var2) {
      byte var3 = 0;

      for (int var4 = 0; var4 < var2.length; var4++) {
         var3 = (byte)(var0 >= var2[var4] ? 1 + var4 : var3);
      }

      return var3;
   }

   private static int needed(int var0, int[] var1) {
      long[] var2 = new long[var1.length];

      for (int var3 = 0; var3 < var1.length; var3++) {
         var2[var3] = var1[var3];
      }

      return needed(var0, var2);
   }

   private static String allows(int var0) {
      return var0 == 4 ? "any preset" : "up to " + GraphicsPresets.label(var0);
   }

   private static String gib(long var0) {
      return String.format(Locale.ROOT, "%.1f GB", var0 / 1024.0);
   }

   private PresetAdvice() {
   }

   record Advice(int preset, int gpu, List<String> reasons, int farWorkers) {
   }
}