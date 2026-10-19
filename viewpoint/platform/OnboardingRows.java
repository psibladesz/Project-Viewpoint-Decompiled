package viewpoint.platform;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

final class OnboardingRows {
   static final String MODE = "Shader mode";
   private static final String[] QUALITY = new String[]{"Low", "Medium", "High", "Ultra", "Extreme"};
   private static final String[] CORPSES = new String[]{"Off", "Off", "High", "Ultra", "Extreme"};
   private static final String[] CORPSE_KEYS = new String[]{
      "corpses.models", "corpses.limit", "corpses.most", "corpses.every", "corpses.inView", "memory.corpseMiB"
   };
   static final Set<LoadModel.Part> ALL = EnumSet.allOf(LoadModel.Part.class);
   static final Set<LoadModel.Part> GPU = EnumSet.of(LoadModel.Part.GPU);
   static final Set<LoadModel.Part> DRAWN = EnumSet.of(LoadModel.Part.GPU, LoadModel.Part.VIDEO_MEMORY, LoadModel.Part.CPU);
   static final Set<LoadModel.Part> PACKS = EnumSet.of(LoadModel.Part.CPU, LoadModel.Part.VIDEO_MEMORY);
   private static final String LATENCY_TEXT = "The game shows your mouse movements sooner. The processor does not prepare frames far ahead of the graphics card. When the graphics card is the slowest part, the frame rate can be a little lower.";
   private static final String LATENCY_BRIEF = "The game shows your mouse movements sooner.";
   private static final String GENERATION_TEXT = "Makes one more frame between each two frames the game draws (AMD FSR 3), so the movement on the screen is smoother. Each frame the game draws comes a little later, so the mouse feels a little slower. It is best when the game is at 60 frames a second or more.";
   private static final String GENERATION_BRIEF = "Smoother movement from frames made between the frames drawn.";
   private static final String PACKS_TEXT = "The 3D models that model pack mods give the game's objects (furniture, cars, signs and more), drawn near you in place of the flat pictures. Off, every object is its flat picture again.";
   private static final String PACKS_BRIEF = "Model pack mods' 3D models near you.";
   private static final String[] FILTERS = new String[]{"Nearest", "Trilinear", "Anisotropic", "Lanczos", "Sharp"};
   private static final String[] FILTER_KEYS = new String[]{"floors.filter", "sprites.filter", "models.filter"};
   private static final double BLOCK_KM = 0.064;

   static List<OnboardingRows.Row> all(ShaderPack var0, boolean var1) {
      ShaderPack var2 = var1 ? ShaderPacks.find("default") : null;
      ArrayList var3 = new ArrayList();
      var3.add(mode());
      PackRows.scaling(var3, var0, var2);
      var3.add(
         onOff(
            "Frame generation",
            "Makes one more frame between each two frames the game draws (AMD FSR 3), so the movement on the screen is smoother. Each frame the game draws comes a little later, so the mouse feels a little slower. It is best when the game is at 60 frames a second or more.",
            "Smoother movement from frames made between the frames drawn.",
            Graphics.FRAME_GENERATION,
            GPU
         )
      );
      var3.add(
         onOff(
            "Low latency",
            "The game shows your mouse movements sooner. The processor does not prepare frames far ahead of the graphics card. When the graphics card is the slowest part, the frame rate can be a little lower.",
            "The game shows your mouse movements sooner.",
            Graphics.LOW_LATENCY,
            EnumSet.noneOf(LoadModel.Part.class)
         )
      );
      PackRows.antialiasing(var3, var0, var2);
      var3.add(reach());
      PackRows.light(var3, var0, var2);
      var3.add(corpses());
      var3.add(
         onOff(
            "3D model packs",
            "The 3D models that model pack mods give the game's objects (furniture, cars, signs and more), drawn near you in place of the flat pictures. Off, every object is its flat picture again.",
            "Model pack mods' 3D models near you.",
            Graphics.MODEL_PACKS,
            PACKS
         )
      );
      var3.add(distantDetail());
      var3.add(
         table(
            "Vegetation detail",
            "How many of the distant trees are drawn, and how far out they reach before they fade.",
            "How many distant trees are drawn, and how far.",
            EnumSet.of(LoadModel.Part.GPU, LoadModel.Part.CPU),
            "lod.treeShare",
            "lod.treesFadeBlocks",
            "lod.treesGoneBlocks"
         )
      );
      var3.add(
         table(
            "Floor detail",
            "How sharp the baked floors and ground are, and how far round you the sharp ones reach.",
            "How sharp the floors and the ground are.",
            EnumSet.of(LoadModel.Part.GPU, LoadModel.Part.VIDEO_MEMORY),
            "floors.detailShare",
            "floors.detailChunks",
            "memory.floorMiB"
         )
      );
      var3.add(textures());
      var3.add(filtering());
      PackRows.materials(var3, var0, var2);
      PackRows.effects(var3, var0, var2);
      return var3;
   }

   private static OnboardingRows.Row mode() {
      return new OnboardingRows.Row(
         "Shader mode",
         "Modern draws the Default look: sun, lamp and flashlight shadows, clouds, light in the air, bloom and the effects below. Vanilla is a plain look close to the game's own, light on any graphics card.",
         "Modern: the full look. Vanilla: a plain look for all graphics cards.",
         List.of(level("Vanilla", "graphics.mode", "Vanilla"), level("Modern", "graphics.mode", "Modern")),
         DRAWN,
         false
      );
   }

   private static OnboardingRows.Row distantDetail() {
      return table(
         "Distant detail",
         "How much of the distant world is drawn as real buildings rather than boxes, and how fine its ground is. More detail fills more video memory.",
         "How detailed the distant buildings and ground are.",
         ALL,
         "lod.shellFullBlocks",
         "lod.shellLiteBlocks",
         "lod.boxesFineBlocks",
         "lod.boxesMidBlocks",
         "lod.boxesCoarseBlocks",
         "lod.groundTexelsFull",
         "lod.groundTexelsLite",
         "lod.interiorFurniture",
         "memory.shellMiB"
      );
   }

   private static OnboardingRows.Row filtering() {
      ArrayList var0 = new ArrayList();

      for (String var4 : FILTERS) {
         LinkedHashMap var5 = new LinkedHashMap();

         for (String var9 : FILTER_KEYS) {
            var5.put(var9, var4);
         }

         var0.add(new OnboardingRows.Level(var4, var5));
      }

      return new OnboardingRows.Row(
         "Texture filtering",
         "How the textures of the floors, the walls and objects and the 3D models are drawn. Sharp keeps each texel crisp near you and smooth far off. Anisotropic keeps the surfaces sharp at a slant. Lanczos is sharper on the graphics card's time. Nearest shows each texel as a hard square. The World page sets each kind apart.",
         "How sharp the textures are, near you and at a slant.",
         var0,
         GPU,
         false
      );
   }

   private static OnboardingRows.Row textures() {
      String var0 = GameTextures.HALF.key;
      return new OnboardingRows.Row(
         "Texture resolution",
         "How fine the game's own art is, which the walls, the objects and the floors are drawn from. Half loads the game's 1x art: a quarter of the texels, far less video memory and system memory, softer up close. The game takes it as it starts: quit and start it again after you change it.",
         "How fine the game's art is. It changes when the game starts again.",
         List.of(level("Full", var0, "false"), level("Half", var0, "true")),
         EnumSet.of(LoadModel.Part.VIDEO_MEMORY, LoadModel.Part.MEMORY),
         false
      );
   }

   private static OnboardingRows.Row onOff(String var0, String var1, String var2, LiveSettings.Toggle var3, Set<LoadModel.Part> var4) {
      List var5 = List.of(level("Off", var3.key, "false"), level("On", var3.key, "true"));
      return new OnboardingRows.Row(var0, var1, var2, var5, var4, false);
   }

   private static OnboardingRows.Row reach() {
      ArrayList var0 = new ArrayList();

      for (int var1 = 0; var1 < QUALITY.length; var1++) {
         Map var2 = column(var1, "lod.farBlocks", "memory.farMiB");
         double var3 = Double.parseDouble((String)var2.get("lod.farBlocks")) * 0.064;
         distinct(var0, new OnboardingRows.Level(String.format(Locale.ROOT, "%.1f km", var3), var2));
      }

      return new OnboardingRows.Row(
         "View distance",
         "How far past the loaded area the world is drawn, as simpler buildings, boxes and trees out to the fog. The costliest setting for the processor and memory.",
         "How far past the loaded area the world is drawn.",
         var0,
         ALL,
         false
      );
   }

   private static OnboardingRows.Row corpses() {
      ArrayList var0 = new ArrayList();

      for (int var1 = 0; var1 < CORPSES.length; var1++) {
         Map var2 = column(var1, CORPSE_KEYS);
         boolean var3 = !Boolean.parseBoolean((String)var2.get(CORPSE_KEYS[0]));
         distinct(var0, new OnboardingRows.Level(CORPSES[var1], var3 ? Map.of(CORPSE_KEYS[0], "false") : var2));
      }

      return new OnboardingRows.Row(
         "3D corpses",
         "The dead bodies within 40 squares as the game's own 3D models rather than flat pictures, as many as the level allows, with about a megabyte of memory for each. Extreme sets no count, and makes models of the bodies all round you, not only of those ahead.",
         "Dead bodies near you as 3D models.",
         var0,
         ALL,
         false
      );
   }

   private static OnboardingRows.Row table(String var0, String var1, String var2, Set<LoadModel.Part> var3, String... var4) {
      ArrayList var5 = new ArrayList();

      for (int var6 = 0; var6 < QUALITY.length; var6++) {
         distinct(var5, new OnboardingRows.Level(QUALITY[var6], column(var6, var4)));
      }

      return new OnboardingRows.Row(var0, var1, var2, var5, var3, false);
   }

   private static Map<String, String> column(int var0, String... var1) {
      LinkedHashMap var2 = new LinkedHashMap();

      for (String var6 : var1) {
         var2.put(var6, GraphicsPresets.TABLE.get(var6)[var0]);
      }

      return var2;
   }

   static void distinct(List<OnboardingRows.Level> var0, OnboardingRows.Level var1) {
      if (var0.isEmpty() || !((OnboardingRows.Level)var0.get(var0.size() - 1)).writes().equals(var1.writes())) {
         var0.add(var1);
      }
   }

   static OnboardingRows.Level level(String var0, String var1, String var2) {
      return new OnboardingRows.Level(var0, Map.of(var1, var2));
   }

   private static boolean holds(OnboardingRows.Level var0, LoadModel.Values var1) {
      for (String var3 : var0.held()) {
         if (!same(var1.get(var3), var0.writes().get(var3))) {
            return false;
         }
      }

      return true;
   }

   static boolean same(String var0, String var1) {
      if (var0 == null) {
         return false;
      }

      if (var0.trim().equalsIgnoreCase(var1.trim())) {
         return true;
      }

      try {
         return Math.abs(Double.parseDouble(var0.trim()) - Double.parseDouble(var1.trim())) < 1.0E-4;
      } catch (NumberFormatException var3) {
         return false;
      }
   }

   private OnboardingRows() {
   }

   record Level(String label, Map<String, String> writes, Set<String> held) {
      Level(String var1, Map<String, String> var2) {
         this(var1, var2, var2.keySet());
      }
   }

   record Row(
      String label,
      String description,
      String brief,
      List<OnboardingRows.Level> levels,
      Set<LoadModel.Part> parts,
      boolean off,
      Predicate<LoadModel.Values> disabled,
      Predicate<LoadModel.Values> shown
   ) {
      Row(String var1, String var2, String var3, List<OnboardingRows.Level> var4, Set<LoadModel.Part> var5, boolean var6) {
         this(var1, var2, var3, var4, var5, var6, var0 -> false, var0 -> true);
      }

      Row(String var1, String var2, String var3, List<OnboardingRows.Level> var4, Set<LoadModel.Part> var5, boolean var6, Predicate<LoadModel.Values> var7) {
         this(var1, var2, var3, var4, var5, var6, var7, var0 -> true);
      }

      int current(LoadModel.Values var1) {
         for (int var2 = 0; var2 < this.levels.size(); var2++) {
            if (OnboardingRows.holds(this.levels.get(var2), var1)) {
               return var2;
            }
         }

         return -1;
      }
   }
}
