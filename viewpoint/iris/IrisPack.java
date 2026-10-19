package viewpoint.iris;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class IrisPack {
   private static final Pattern CONST_ROTATION = Pattern.compile("\\s*const\\s+float\\s+sunPathRotation\\b");
   private static final Pattern FORMATTING = Pattern.compile("§.?");
   public static final String PREFIX = "minecraft-";
   public final String id;
   public final File file;
   public final PackSource source;
   public final IrisOptions options;
   public final Map<String, String> lang;
   public final ShadersProperties defaults;
   public final String folder;
   public final boolean clock;

   private IrisPack(String var1, File var2, PackSource var3, IrisOptions var4, Map<String, String> var5, ShadersProperties var6, String var7, boolean var8) {
      this.id = var1;
      this.file = var2;
      this.source = var3;
      this.options = var4;
      this.lang = var5;
      this.defaults = var6;
      this.folder = var7;
      this.clock = var8;
   }

   public static IrisPack load(File var0, Map<String, String> var1) throws IOException {
      PackSource var2 = PackSource.open(var0);
      IrisOptions var3 = IrisOptions.scan(var2);
      Map var4 = lang(var2);
      ShadersProperties var5 = properties(var2, var1, var3, Map.of());
      String var6 = ProgramSet.overworld(var2, var2.read("dimension.properties"));
      return new IrisPack(id(var2.name()), var0, var2, var3, var4, var5, var6, clock(var2));
   }

   public IrisPack.Configured configure(Map<String, String> var1, Map<String, String> var2) throws IOException {
      Map var3 = this.options.values(var1);
      Map var4 = propertyMacros(var2, this.options.macros(var3));
      ShadersProperties var5 = properties(this.source, var2, this.options, var3);
      ArrayList var6 = new ArrayList();
      IdTable var7 = IdTable.parse(this.preprocessed("block.properties", var4), "block");
      IdTable var8 = IdTable.parse(this.preprocessed("entity.properties", var4), "entity");
      IdTable var9 = IdTable.parse(this.preprocessed("item.properties", var4), "item");
      Set var10 = this.options.toggles();
      ProgramSet var11 = ProgramSet.find(this.source, this.folder, var5x -> this.enabled(var5, var5x, var4, var10, var6));
      return new IrisPack.Configured(this, var3, var4, var5, var7, var8, var9, var11, var6);
   }

   public Preprocessor.Result preprocess(IrisPack.Configured var1, String var2, Map<String, String> var3) throws IOException {
      return this.preprocess(var1, var2, var3, Map.of());
   }

   public Preprocessor.Result preprocess(IrisPack.Configured var1, String var2, Map<String, String> var3, Map<String, String> var4) throws IOException {
      LinkedHashMap var5 = new LinkedHashMap<>(this.options.changed(var1.values()));
      var5.putAll(var4);
      Preprocessor var6 = new Preprocessor(this.source, var3, var5, this.options.toggles());
      return var6.run(var2);
   }

   private static boolean clock(PackSource var0) throws IOException {
      for (String var2 : IrisOptions.sources(var0)) {
         for (String var6 : var0.read(var2).split("\n")) {
            if (var6.contains("sunPathRotation") && !CONST_ROTATION.matcher(var6).lookingAt()) {
               return true;
            }
         }
      }

      return false;
   }

   public Map<String, String> profile(String var1) {
      return this.profile(var1, new HashSet<>());
   }

   private Map<String, String> profile(String var1, Set<String> var2) {
      LinkedHashMap var3 = new LinkedHashMap();
      List<String> var4 = this.defaults.profiles.get(var1);
      if (var4 != null && var2.add(var1)) {
         for (String var6 : var4) {
            if (var6.startsWith("profile.")) {
               var3.putAll(this.profile(var6.substring(8), var2));
            } else if (var6.startsWith("!")) {
               this.put(var3, var6.substring(1), "false");
            } else if (!var6.contains("=") && !var6.contains(":")) {
               this.put(var3, var6, "true");
            } else {
               int var7 = var6.indexOf(61) >= 0 ? var6.indexOf(61) : var6.indexOf(58);
               this.put(var3, var6.substring(0, var7), var6.substring(var7 + 1));
            }
         }

         return var3;
      } else {
         return var3;
      }
   }

   private void put(Map<String, String> var1, String var2, String var3) {
      if (this.options.get(var2) != null) {
         var1.put(var2, var3);
      }
   }

   public String label(String var1, String var2) {
      String var3 = this.text(var1).strip();
      return var3.isEmpty() ? var2 : var3;
   }

   public String text(String var1) {
      return FORMATTING.matcher(this.lang.getOrDefault(var1, "")).replaceAll("");
   }

   private String preprocessed(String var1, Map<String, String> var2) throws IOException {
      String var3 = this.source.read(var1);
      return var3 == null ? null : new Preprocessor(this.source, var2, Map.of(), Set.of()).runProperties(var1, var3);
   }

   private static ShadersProperties properties(PackSource var0, Map<String, String> var1, IrisOptions var2, Map<String, String> var3) throws IOException {
      String var4 = var0.read("shaders.properties");
      if (var4 == null) {
         return ShadersProperties.parse("");
      }

      Map var5 = propertyMacros(var1, var2.macros(var2.values(var3)));
      return ShadersProperties.parse(new Preprocessor(var0, var5, Map.of(), Set.of()).runProperties("shaders.properties", var4));
   }

   private static Map<String, String> propertyMacros(Map<String, String> var0, Map<String, String> var1) {
      LinkedHashMap var2 = new LinkedHashMap();
      var0.forEach((var1x, var2x) -> var2.put(var1x, var2x.isEmpty() ? "1" : var2x));
      var2.putAll(var1);
      return var2;
   }

   private boolean enabled(ShadersProperties var1, String var2, Map<String, String> var3, Set<String> var4, List<String> var5) {
      String var6 = var1.programEnabled.get(this.folder + var2);
      if (var6 == null) {
         var6 = var1.programEnabled.get(var2);
      }

      if (var6 == null) {
         return true;
      }

      StringBuilder var7 = new StringBuilder();

      for (String var11 : var6.split("(?<=[^\\w])|(?=[^\\w])")) {
         var7.append(var4.contains(var11) ? "defined(" + var11 + ")" : var11);
      }

      try {
         Macros var13 = new Macros();
         var3.forEach(var13::define);
         return IfExpression.test(var7.toString(), var13);
      } catch (IllegalArgumentException var12) {
         var5.add("program." + var2 + ".enabled: " + var12.getMessage());
         return true;
      }
   }

   private static Map<String, String> lang(PackSource var0) throws IOException {
      LinkedHashMap var1 = new LinkedHashMap();

      for (String var3 : var0.paths()) {
         if (var3.equalsIgnoreCase("lang/en_us.lang")) {
            for (String var7 : var0.read(var3).split("\n")) {
               String var8 = var7.strip();
               int var9 = var8.indexOf(61);
               if (var9 > 0 && !var8.startsWith("#")) {
                  var1.put(var8.substring(0, var9).strip(), var8.substring(var9 + 1).strip());
               }
            }
         }
      }

      return var1;
   }

   static String id(String var0) {
      return "minecraft-" + var0.replaceAll("[^A-Za-z0-9_-]+", "_");
   }

   public record Configured(
      IrisPack pack,
      Map<String, String> values,
      Map<String, String> macros,
      ShadersProperties properties,
      IdTable blocks,
      IdTable entities,
      IdTable items,
      ProgramSet programs,
      List<String> notes
   ) {
   }
}
