package viewpoint.platform;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

final class PackPresets {
   private static final String CUSTOM = "Custom";
   private static final Map<String, LiveSettings.Choice> choices = new HashMap<>();
   private static final Map<String, Integer> applied = new HashMap<>();

   static void declare(ShaderPack var0, String var1, BooleanSupplier var2) {
      if (!var0.presets.isEmpty()) {
         String[] var3 = new String[var0.presets.size() + 1];
         byte var4 = 0;

         for (int var5 = 0; var5 < var0.presets.size(); var5++) {
            var3[var5] = var0.presets.get(var5).label;
            var4 = (byte)(var0.presets.get(var5).id.equals(var0.firstPreset) ? var5 : var4);
         }

         var3[var0.presets.size()] = "Custom";
         LiveSettings.Choice var6 = LiveSettings.choice(key(var0), "Quality", var1, var3, var4);
         var6.shownAs(shown(var0, var3));
         var6.describe("The pack's own presets of its options; changing any of them makes it Custom.");
         var6.shownWhen(var2);
         choices.put(var0.id, var6);
      }
   }

   private static String[] shown(ShaderPack var0, String[] var1) {
      String[] var2 = (String[])var1.clone();

      for (int var3 = 0; var3 < var0.presets.size(); var3++) {
         int var4 = PackOption.PRESETS.indexOf(var0.presets.get(var3).id.toLowerCase(Locale.ROOT));
         if (var4 >= 0 && var1[var3].equalsIgnoreCase(GraphicsPresets.NAMES[var4])) {
            var2[var3] = GraphicsPresets.label(var4);
         }
      }

      return var2;
   }

   static String key(ShaderPack var0) {
      return "packPreset." + var0.id;
   }

   static boolean follow(ShaderPack var0, Map<String, LiveSettings.Setting> var1) {
      LiveSettings.Choice var2 = choices.get(var0.id);
      if (var2 == null) {
         return false;
      }

      int var3 = var2.get();
      int var4 = var0.presets.size();
      Integer var5 = applied.get(var0.id);
      applied.put(var0.id, var3);
      if (var3 == var4) {
         return false;
      }

      PackPreset var6 = var0.presets.get(var3);
      boolean var7 = var5 == null && !matches(var6, var0, var1);
      if (var7) {
         System.out.println("[Viewpoint] " + var0.id + "'s preset " + var6.label + ": its values changed, written again");
      }

      if (var7 || var5 != null && var3 != var5) {
         for (PackOption var9 : var0.options) {
            if (!var9.kept) {
               LiveSettings.put(Graphics.key(var0, var9), var6.valueFor(var9));
            }
         }

         return true;
      } else {
         if (matches(var6, var0, var1)) {
            return false;
         }

         var2.set(var4);
         applied.put(var0.id, var4);
         return true;
      }
   }

   static boolean matches(PackPreset var0, ShaderPack var1, Map<String, LiveSettings.Setting> var2) {
      for (PackOption var4 : var1.options) {
         if (!var4.kept && !((LiveSettings.Setting)var2.get(var4.id)).matches(var0.valueFor(var4))) {
            return false;
         }
      }

      return true;
   }

   private PackPresets() {
   }
}
