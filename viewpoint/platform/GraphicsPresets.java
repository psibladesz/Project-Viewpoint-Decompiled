package viewpoint.platform;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class GraphicsPresets {
   static final String[] NAMES = new String[]{"Potato", "Low", "Default", "High", "Ultra", "Custom"};
   static final String[] LABELS = new String[]{"Low", "Medium", "High", "Ultra", "Extreme", "Custom"};
   static final int DEFAULT = 2;
   static final int CUSTOM = 5;
   static final Map<String, String[]> TABLE = new LinkedHashMap<>();
   private static LiveSettings.Choice preset;
   private static int applied = -1;

   private static void row(String var0, String... var1) {
      TABLE.put(var0, var1);
   }

   static void start() {
      preset = LiveSettings.choice("graphics.preset", "Preset", "Graphics/General", NAMES, 2);
      preset.shownAs(LABELS);
      preset.describe(
         "Sets the mode and the settings that trade the world's detail for speed together; changing any of them makes it Custom. The shader pack's options have their own presets."
      );
   }

   static boolean follow() {
      int var0 = preset.get();
      if (var0 == 5) {
         applied = 5;
         return false;
      }

      boolean var1 = applied < 0;
      boolean var2 = var1 && !matches(var0);
      if (var2) {
         System.out.println("[Viewpoint] graphics preset " + NAMES[var0] + ": its values changed, written again");
      }

      if (var2 || !var1 && var0 != applied) {
         applied = var0;

         for (Entry var4 : TABLE.entrySet()) {
            LiveSettings.put((String)var4.getKey(), ((String[])var4.getValue())[var0]);
         }

         return true;
      } else {
         applied = var0;
         return stray(var0);
      }
   }

   static String label(int var0) {
      return LABELS[var0];
   }

   static int index(String var0) {
      for (int var1 = 0; var1 < 5; var1++) {
         if (NAMES[var1].equalsIgnoreCase(String.valueOf(var0))) {
            return var1;
         }
      }

      return 5;
   }

   static int current() {
      return preset.get();
   }

   private static boolean stray(int var0) {
      if (matches(var0)) {
         return false;
      }

      preset.set(5);
      applied = 5;
      return true;
   }

   static boolean matches(int var0) {
      for (Entry var2 : TABLE.entrySet()) {
         if (!holds((String)var2.getKey(), ((String[])var2.getValue())[var0], var0)) {
            return false;
         }
      }

      return true;
   }

   private static boolean holds(String var0, String var1, int var2) {
      LiveSettings.Setting var3 = LiveSettings.find(var0);
      if (var3 != null) {
         return var3.matches(var1);
      }

      String var4 = LiveSettings.value(var0);
      return var4 == null ? var2 == 2 : var4.trim().equalsIgnoreCase(var1) || same(var4, var1);
   }

   private static boolean same(String var0, String var1) {
      try {
         return Float.parseFloat(var0.trim()) == Float.parseFloat(var1.trim());
      } catch (NumberFormatException var3) {
         return false;
      }
   }

   private GraphicsPresets() {
   }

   static {
      row("graphics.mode", "Vanilla", "Modern", "Modern", "Modern", "Modern");
      row("graphics.pack", "default", "default", "default", "default", "default");
      row("lod.farBlocks", "8", "20", "32", "48", "64");
      row("lod.boxesFineBlocks", "2", "3", "3", "6", "8");
      row("lod.boxesMidBlocks", "4", "6", "6", "12", "16");
      row("lod.boxesCoarseBlocks", "8", "12", "12", "24", "32");
      row("lod.farColoursFullBlocks", "8", "16", "24", "32", "48");
      row("lod.farColoursHalfBlocks", "16", "32", "48", "64", "64");
      row("lod.shellFullBlocks", "1", "2", "3", "6", "8");
      row("lod.shellLiteBlocks", "4", "6", "10", "16", "24");
      row("lod.treeShare", "0.25", "0.375", "0.5", "0.75", "1.0");
      row("lod.treesFadeBlocks", "6", "8", "12", "20", "24");
      row("lod.treesGoneBlocks", "12", "16", "36", "56", "64");
      row("lod.interiorFurniture", "false", "false", "false", "false", "true");
      row("lod.groundTexelsFull", "4", "4", "8", "8", "16");
      row("lod.groundTexelsLite", "0", "1", "2", "2", "4");
      row("lod.fullRateCharacters", "8", "16", "32", "64", "128");
      row("lod.packRing", "0", "3", "6", "8", "10");
      row("floors.detailShare", "0.5", "0.5", "1.0", "1.0", "1.5");
      row("floors.detailChunks", "4", "6", "8", "10", "12");
      row("floors.filter", "Sharp", "Sharp", "Sharp", "Sharp", "Sharp");
      row("sprites.filter", "Sharp", "Sharp", "Sharp", "Sharp", "Sharp");
      row("models.filter", "Sharp", "Sharp", "Sharp", "Sharp", "Sharp");
      row("far.shellUploadMb", "2.0", "3.0", "4.0", "6.0", "8.0");
      row("far.cellUploadMb", "1.0", "1.5", "2.0", "3.0", "4.0");
      row("memory.nearMeshMiB", "384", "384", "512", "640", "768");
      row("memory.lightAtlasMiB", "48", "48", "64", "96", "128");
      row("memory.shellMiB", "192", "384", "768", "1536", "2048");
      row("memory.farMiB", "64", "96", "192", "320", "640");
      row("memory.modelMeshMiB", "160", "192", "256", "384", "512");
      row("memory.cpuCacheMiB", "160", "192", "320", "512", "768");
      row("memory.floorMiB", "128", "256", "512", "768", "1024");
      row("memory.packMiB", "64", "128", "768", "1024", "1536");
      row("memory.minecraftMiB", "2048", "3072", "4096", "6144", "8192");
      row("memory.corpseMiB", "256", "256", "256", "512", "1024");
      row("corpses.models", "false", "false", "true", "true", "true");
      row("corpses.limit", "40", "40", "40", "40", "40");
      row("corpses.most", "256", "256", "256", "512", "512");
      row("corpses.every", "false", "false", "false", "false", "true");
      row("corpses.inView", "true", "true", "true", "true", "false");
   }
}
