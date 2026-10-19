package viewpoint.platform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

public final class PackPreset {
   public final String id;
   public final String label;
   private final Map<String, String> values;

   private PackPreset(String var1, String var2, Map<String, String> var3) {
      this.id = var1;
      this.label = var2;
      this.values = Map.copyOf(var3);
   }

   public String valueFor(PackOption var1) {
      return this.values.getOrDefault(var1.id, var1.fallback);
   }

   static PackPreset.Ladder parse(Properties var0, List<PackOption> var1, List<String> var2, List<PackPreset> var3, String var4) {
      Map<String, Map<String, String>> var5 = new LinkedHashMap<>();
      LinkedHashMap<String, String> var6 = new LinkedHashMap<>();

      for (PackPreset var8 : var3) {
         LinkedHashMap<String, String> var9 = new LinkedHashMap<>(var8.values);
         var9.keySet().removeAll(var2);
         var9.keySet().removeIf(var1x -> var1.stream().noneMatch(var1xx -> var1xx.id.equals(var1x)));
         var5.put(var8.id, var9);
         var6.put(var8.id, var8.label);
      }

      boolean var14 = var2.stream().anyMatch(var1x -> hasOldLines(var0, var1x));
      boolean var15 = var0.stringPropertyNames().stream().anyMatch(var0x -> var0x.equals("presets") || var0x.startsWith("preset."));
      if (var14 && var15) {
         throw new IllegalArgumentException("option.[id].preset.[name] and presets / preset.[id] are two forms of the same: use one");
      }

      if (var0.getProperty("presets") != null) {
         var5 = listed(var0.getProperty("presets"), var5);
      }

      if (var14) {
         oldForm(var0, var1, var2, var5);
      }

      for (String var10 : var0.stringPropertyNames()) {
         if (var10.startsWith("preset.") && !var10.equals("preset.default")) {
            line(var10, var0.getProperty(var10).trim(), var5, var6);
         }
      }

      if (var5.isEmpty()) {
         return new PackPreset.Ladder(List.of(), null);
      }

      String var17 = var0.getProperty("preset.default", var4 != null && var5.containsKey(var4) ? var4 : null);
      var17 = var17 != null ? var17.trim() : (var5.containsKey("default") ? "default" : var5.keySet().iterator().next());
      if (!var5.containsKey(var17)) {
         throw new IllegalArgumentException("preset.default: no preset " + var17);
      }

      ArrayList<PackPreset> var19 = new ArrayList<>();

      for (Entry<String, Map<String, String>> var12 : var5.entrySet()) {
         check(var12.getKey(), var12.getValue(), var1, var12.getKey().equals(var17));
         String var13 = var6.getOrDefault(var12.getKey(), title(var12.getKey()));
         var19.add(new PackPreset(var12.getKey(), var13, var12.getValue()));
      }

      return new PackPreset.Ladder(List.copyOf(var19), var17);
   }

   private static boolean hasOldLines(Properties var0, String var1) {
      String var2 = "option." + var1 + ".preset.";
      return var0.stringPropertyNames().stream().anyMatch(var1x -> var1x.startsWith(var2));
   }

   private static Map<String, Map<String, String>> listed(String var0, Map<String, Map<String, String>> var1) {
      LinkedHashMap<String, Map<String, String>> var2 = new LinkedHashMap<>();

      for (String var6 : var0.trim().split("\\s+")) {
         if (!var6.isEmpty()) {
            if (!PackOption.ID.matcher(var6).matches()) {
               throw new IllegalArgumentException("presets: " + var6 + " is not letters and digits");
            }

            var2.put(var6, var1.getOrDefault(var6, new LinkedHashMap<>()));
         }
      }

      return var2;
   }

   private static void oldForm(Properties var0, List<PackOption> var1, List<String> var2, Map<String, Map<String, String>> var3) {
      if (var3.isEmpty()) {
         for (String var5 : PackOption.PRESETS) {
            var3.put(var5, new LinkedHashMap<>());
         }
      }

      for (String var10 : var2) {
         for (String var7 : PackOption.PRESETS) {
            String var8 = var0.getProperty("option." + var10 + ".preset." + var7);
            if (var8 != null && var3.containsKey(var7)) {
               var3.get(var7).put(var10, var8.trim());
            }
         }
      }
   }

   private static void line(String var0, String var1, Map<String, Map<String, String>> var2, Map<String, String> var3) {
      String var4 = var0.substring("preset.".length());
      boolean var5 = var4.endsWith(".label");
      String var6 = var5 ? var4.substring(0, var4.length() - ".label".length()) : var4;
      if (!var2.containsKey(var6)) {
         throw new IllegalArgumentException(var0 + ": no such preset (" + String.join(", ", var2.keySet()) + ")");
      }

      if (var5) {
         var3.put(var6, var1);
      } else {
         for (String var10 : var1.split("\\s+")) {
            if (!var10.isEmpty()) {
               int var11 = var10.indexOf(61);
               if (var11 <= 0 || var11 == var10.length() - 1) {
                  throw new IllegalArgumentException(var0 + ": " + var10 + " is not option=value");
               }

               var2.get(var6).put(var10.substring(0, var11), var10.substring(var11 + 1));
            }
         }
      }
   }

   private static void check(String var0, Map<String, String> var1, List<PackOption> var2, boolean var3) {
      for (Entry<String, String> var5 : var1.entrySet()) {
         PackOption var6 = var2.stream()
                 .filter(var1x -> var1x.id.equals(var5.getKey()))
                 .findFirst()
                 .orElseThrow(() -> new IllegalArgumentException("preset." + var0 + ": no option " + var5.getKey()));
         if (var6.kept) {
            throw new IllegalArgumentException("preset." + var0 + ": " + var6.id + " is kept (option." + var6.id + ".kept), no preset's");
         }

         var6.check("preset." + var0 + " " + var6.id, var5.getValue());
         if (var3 && !var6.same(var5.getValue(), var6.fallback)) {
            throw new IllegalArgumentException(
                    "preset." + var0 + " is the one a fresh install starts at, so " + var6.id + " must be its default there (" + var6.fallback + ")"
            );
         }
      }
   }

   private static String title(String var0) {
      String var1 = PackOption.constant(var0).replace('_', ' ').toLowerCase(Locale.ROOT);
      return Character.toUpperCase(var1.charAt(0)) + var1.substring(1);
   }

   @Override
   public String toString() {
      return this.id;
   }

   record Ladder(List<PackPreset> presets, String first) {
   }
}