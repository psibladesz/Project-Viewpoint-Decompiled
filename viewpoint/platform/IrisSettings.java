package viewpoint.platform;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import viewpoint.iris.IrisOptions;
import viewpoint.iris.IrisPack;

final class IrisSettings {
   private static final String CUSTOM = "Custom";
   static final SettingsWindow.Button APPLY = SettingsWindow.button(
           "Graphics/Advanced", "Apply", "Links the shader pack again with the options as they are now: it takes seconds."
   );
   private static final SettingsWindow.Button RESCAN = SettingsWindow.button(
           "Graphics/Shaders", "Rescan", "Finds the packs dropped into Zomboid/viewpoint-shaderpacks since the game started."
   );
   private static final Map<String, Map<String, LiveSettings.Setting>> settings = new HashMap<>();
   private static final Map<String, LiveSettings.Choice> profiles = new HashMap<>();
   private static final Map<String, Integer> applied = new HashMap<>();
   private static File folder;
   private static IrisPack published;
   private static Map<String, String> publishedValues = Map.of();

   private IrisSettings() {
   }

   static List<IrisPack> start(File var0, BooleanSupplier var1) {
      folder = var0;
      IrisPacks.discover(folder);
      APPLY.shownWhen(var1);
      RESCAN.beside("graphics.pack");
      return declareNew();
   }

   private static List<IrisPack> declareNew() {
      List<IrisPack> var0 = IrisPacks.all();

      for (IrisPack var2 : var0) {
         if (!settings.containsKey(var2.id)) {
            declare(var2);
         }
      }

      return var0;
   }

   private static void declare(IrisPack var0) {
      BooleanSupplier var1 = () -> Graphics.irisInUse() == var0;
      LinkedHashMap<String, LiveSettings.Setting> var2 = new LinkedHashMap<>();
      settings.put(var0.id, var2);
      ArrayList<String> var3 = new ArrayList<>(var0.defaults.profiles.keySet());
      if (!var3.isEmpty()) {
         var3.add("Custom");
         int var4 = var3.indexOf("Custom");

         for (int var5 = 0; var5 < var3.size() - 1; var5++) {
            var4 = matches(var0, var0.profile((String)var3.get(var5)), defaults(var0)) ? var5 : var4;
         }

         LiveSettings.Choice var8 = LiveSettings.choice("minecraftProfile." + var0.id, "Profile", "Graphics/Advanced", var3.toArray(new String[0]), var4);
         var8.describe("The pack's own profiles of its options; changing any of them makes it Custom.");
         String[] var6 = var3.toArray(new String[0]);

         for (int var7 = 0; var7 < var6.length - 1; var7++) {
            var6[var7] = var0.label("profile." + var6[var7], var6[var7]);
         }

         var8.shownAs(var6);
         var8.shownWhen(var1);
         profiles.put(var0.id, var8);
      }

      if (var0.defaults.screens.containsKey("")) {
         screen(var0, "", "Graphics/Advanced", var1, var2, new HashSet<>());
      } else {
         var0.options.all().forEach(var3x -> option(var0, var3x, "Graphics/Advanced", var1, var2));
      }

      var0.options.all().forEach(var2x -> option(var0, var2x, "Graphics/Advanced", () -> false, var2));
   }

   private static void screen(IrisPack var0, String var1, String var2, BooleanSupplier var3, Map<String, LiveSettings.Setting> var4, Set<String> var5) {
      List<String> var6 = var0.defaults.screens.get(var1);
      if (var6 != null && var5.add(var1)) {
         for (String var8 : var6) {
            if (var8.startsWith("[") && var8.endsWith("]")) {
               String var12 = var8.substring(1, var8.length() - 1);
               String var13 = var0.label("screen." + var12, var12);
               String var11 = var2 + " > " + var13;
               screen(var0, var12, var11, var3, var4, var5);
            } else if (var8.equals("*")) {
               for (IrisOptions.Option var10 : var0.options.all()) {
                  option(var0, var10, var2, var3, var4);
               }
            } else if (var0.options.get(var8) != null) {
               option(var0, var0.options.get(var8), var2, var3, var4);
            }
         }
      }
   }

   private static void option(IrisPack var0, IrisOptions.Option var1, String var2, BooleanSupplier var3, Map<String, LiveSettings.Setting> var4) {
      if (!var4.containsKey(var1.name())) {
         String var5 = key(var0, var1.name());
         String var6 = var0.label("option." + var1.name(), var1.name());
         LiveSettings.Setting var7 = var1.kind() == IrisOptions.Kind.TOGGLE
                 ? LiveSettings.toggle(var5, var6, var2, var1.fallback().equals("true"))
                 : LiveSettings.choice(var5, var6, var2, var1.values().toArray(new String[0]), var1.values().indexOf(var1.fallback()));
         var7.describe(var0.label("option." + var1.name() + ".comment", ""));
         if (var7 instanceof LiveSettings.Choice var8) {
            var8.shownAs(shown(var0, var1));
            if (var0.defaults.sliders.contains(var1.name())) {
               var8.asSlider();
            }
         }

         var7.shownWhen(var3);
         var4.put(var1.name(), var7);
      }
   }

   static String[] shown(IrisPack var0, IrisOptions.Option var1) {
      String var2 = var1.name();
      String var3 = var0.text("prefix." + var2);
      String var4 = var0.text("suffix." + var2);
      String[] var5 = new String[var1.values().size()];

      for (int var6 = 0; var6 < var5.length; var6++) {
         String var7 = var1.values().get(var6);
         var5[var6] = var3 + var0.label("value." + var2 + "." + var7, var7) + var4;
      }

      return var5;
   }

   static String key(IrisPack var0, String var1) {
      return "minecraft." + var0.id + "." + var1;
   }

   static boolean follow(IrisPack var0) {
      boolean var1 = var0 != null && profile(var0);
      Map<String, String> var2 = var0 == null ? Map.of() : values(var0);
      if (var0 != published) {
         publish(var0, var2);
      }

      APPLY.status(var0 != null && !var2.equals(publishedValues) ? "Changed: Apply to see it" : "");
      return var1;
   }

   static boolean update(IrisPack var0) {
      if (APPLY.take() && var0 != null) {
         publish(var0, values(var0));
         APPLY.status("");
      }

      if (!RESCAN.take()) {
         return false;
      }

      boolean var1 = IrisPacks.discover(folder);
      declareNew();
      RESCAN.status(IrisPacks.all().size() + " Minecraft shader packs");
      return var1;
   }

   private static void publish(IrisPack var0, Map<String, String> var1) {
      published = var0;
      publishedValues = var1;
      IrisPacks.use(var0, var1);
   }

   private static Map<String, String> values(IrisPack var0) {
      LinkedHashMap<String, String> var1 = new LinkedHashMap<>();
      settings.get(var0.id).forEach((var1x, var2) -> var1.put(var1x, var2.text()));
      return var1;
   }

   private static boolean profile(IrisPack var0) {
      LiveSettings.Choice var1 = profiles.get(var0.id);
      if (var1 == null) {
         return false;
      }

      int var2 = var1.get();
      int var3 = var1.options.length - 1;
      Integer var4 = applied.put(var0.id, var2);
      if (var4 != null && var2 != var4 && var2 != var3) {
         Map<String, String> var5 = var0.profile(var1.options[var2]);

         for (IrisOptions.Option var7 : var0.options.all()) {
            LiveSettings.put(key(var0, var7.name()), var5.getOrDefault(var7.name(), var7.fallback()));
         }

         return true;
      } else if (var2 != var3 && !matches(var0, var0.profile(var1.options[var2]), values(var0))) {
         var1.set(var3);
         applied.put(var0.id, var3);
         return true;
      } else {
         return false;
      }
   }

   private static boolean matches(IrisPack var0, Map<String, String> var1, Map<String, String> var2) {
      for (IrisOptions.Option var4 : var0.options.all()) {
         String var5 = (String)var2.get(var4.name());
         if (var5 != null && !var5.equals(var1.getOrDefault(var4.name(), var4.fallback()))) {
            return false;
         }
      }

      return true;
   }

   private static Map<String, String> defaults(IrisPack var0) {
      LinkedHashMap<String, String> var1 = new LinkedHashMap<>();

      for (IrisOptions.Option var3 : var0.options.all()) {
         var1.put(var3.name(), var3.fallback());
      }

      return var1;
   }
}