package viewpoint.platform;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

final class GraphicsEdits {
   static final String PRESET = "graphics.preset";
   private static final String PACK_PRESET = "packPreset.";
   private final Map<String, String> staged = new LinkedHashMap<>();

   LoadModel.Values over(LoadModel.Values var1) {
      if (this.staged.isEmpty()) {
         return var1;
      }

      String var2 = GraphicsPresets.NAMES[this.preset(var1)];
      return var3 -> var3.equals("graphics.preset") ? var2 : (this.staged.containsKey(var3) ? this.staged.get(var3) : var1.get(var3));
   }

   void put(Map<String, String> var1, LoadModel.Values var2) {
      LinkedHashMap<String, String> var3 = new LinkedHashMap<>(var1);
      int var4 = GraphicsPresets.index((String)var1.get("graphics.preset"));
      if (var4 != 5) {
         presetValues(var4).forEach(var3::putIfAbsent);
      }

      var3.forEach((var2x, var3x) -> {
         if (OnboardingRows.same(var2.get(var2x), var3x)) {
            this.staged.remove(var2x);
         } else {
            this.staged.put(var2x, var3x);
         }
      });
   }

   boolean any() {
      return !this.staged.isEmpty();
   }

   void apply(LoadModel.Values var1) {
      int var2 = GraphicsPresets.index(this.staged.get("graphics.preset"));
      if (var2 == 5) {
         Onboarding.putAll(this.staged);
         this.staged.clear();
      } else {
         Map<String, String> var3 = presetValues(var2);
         LinkedHashMap<String, String> var4 = new LinkedHashMap<>();
         LinkedHashMap<String, String> var5 = new LinkedHashMap<>();
         LinkedHashSet<String> var6 = new LinkedHashSet<>(var3.keySet());
         var6.addAll(this.staged.keySet());

         for (String var8 : var6) {
            String var9 = this.staged.containsKey(var8) ? this.staged.get(var8) : var1.get(var8);
            if (var8.equals("graphics.preset") || var8.startsWith("packPreset.")) {
               var4.put(var8, var9);
            } else if (var9 != null && !OnboardingRows.same((String)var3.get(var8), var9)) {
               var5.put(var8, var9);
            }
         }

         Onboarding.putAll(var4);
         if (!var5.isEmpty()) {
            Onboarding.barrier();
            Onboarding.putAll(var5);
         }

         this.staged.clear();
      }
   }

   void discard() {
      this.staged.clear();
   }

   private int preset(LoadModel.Values var1) {
      int var2 = GraphicsPresets.index(this.staged.getOrDefault("graphics.preset", var1.get("graphics.preset")));
      if (var2 == 5) {
         return var2;
      }

      for (Entry<String, String> var4 : this.staged.entrySet()) {
         String[] var5 = GraphicsPresets.TABLE.get(var4.getKey());
         if (var5 != null && !OnboardingRows.same(var4.getValue(), var5[var2])) {
            return 5;
         }
      }

      return var2;
   }

   private static Map<String, String> presetValues(int var0) {
      LinkedHashMap<String, String> var1 = new LinkedHashMap<>(OnboardingWindow.presetWrites(var0));
      GraphicsPresets.TABLE.forEach((var2, var3x) -> var1.put(var2, var3x[var0]));

      for (String var3 : List.of("default", "normal")) {
         ShaderPack var4 = ShaderPacks.find(var3);
         PackPreset var5 = var4 == null ? null : PresetAdvice.presetNamed(var4, GraphicsPresets.NAMES[var0]);
         if (var5 != null) {
            var4.options.stream().filter(var0x -> !var0x.kept).forEach(var3x -> var1.put(Graphics.key(var4, var3x), var5.valueFor(var3x)));
         }
      }

      return var1;
   }
}