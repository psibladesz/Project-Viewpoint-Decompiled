package viewpoint.platform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class ShaderPacks {
   public static final String NORMAL = "normal";
   public static final String LIBRARY = "library";
   public static final String DEFAULT = "default";
   public static final String VIVID = "vivid";
   static final List<String> BUILT_IN = List.of("normal", "library", "default", "vivid");
   static final Map<String, String> REMOVED = Map.of("dreamy", "vivid");
   public static final String FOLDER = "viewpoint-shaderpacks";
   private static final Pattern ID = Pattern.compile("[A-Za-z0-9_-]+");
   private static final Map<String, ShaderPack> packs = new LinkedHashMap<>();
   private static final Map<String, String> broken = new LinkedHashMap<>();
   private static volatile ShaderPacks.Active active = atDefaults(builtIn("library"), 0);
   private static volatile ShaderPacks.Active drawn = active;
   private static volatile ShaderPacks.Failure failure;

   public static ShaderPacks.Active active() {
      return active;
   }

   public static ShaderPacks.Active drawn() {
      return drawn;
   }

   public static ShaderPacks.Failure failure() {
      return failure;
   }

   static void discover(File var0) {
      packs.clear();
      broken.clear();

      for (String var2 : BUILT_IN) {
         packs.put(var2, builtIn(var2));
      }

      TreeMap<String, File> var8 = new TreeMap<>();
      File[] var9 = var0 == null ? null : var0.listFiles(File::isDirectory);

      for (File var6 : var9 == null ? new File[0] : var9) {
         String var7 = var6.getName();
         if (new File(var6, "pack.properties").isFile()) {
            if (ID.matcher(var7).matches() && !packs.containsKey(var7)) {
               var8.put(var7, var6);
            } else {
               broken.put(var7, "a folder name of letters, digits, _ and -, and none of the built-in packs' names");
            }
         }
      }

      for (String var12 : var8.keySet()) {
         player(var12, var8, new HashSet<>());
      }

      for (Entry<String, String> var13 : broken.entrySet()) {
         System.out.println("[Viewpoint] shader pack " + var13.getKey() + " left out: " + var13.getValue());
      }

      System.out.println("[Viewpoint] shader packs: " + String.join(", ", packs.keySet()));
   }

   private static ShaderPack player(String var0, Map<String, File> var1, Set<String> var2) {
      ShaderPack var3 = packs.get(var0);
      File var4 = var1.get(var0);
      if (var3 == null && var4 != null && !broken.containsKey(var0) && var2.add(var0)) {
         try {
            ShaderPack var5 = ShaderPack.parse(var0, ShaderPack.folder(var4), var2x -> player(var2x, var1, var2), false);
            packs.put(var0, var5);
            return var5;
         } catch (IOException | IllegalArgumentException var10) {
            broken.put(var0, var10.getMessage());
            return null;
         } finally {
            var2.remove(var0);
         }
      } else {
         return var3;
      }
   }

   static List<ShaderPack> all() {
      return new ArrayList<>(packs.values());
   }

   static ShaderPack find(String var0) {
      return packs.get(var0);
   }

   static Map<String, String> leftOut() {
      return new LinkedHashMap<>(broken);
   }

   static void use(ShaderPack var0, Function<PackOption, String> var1) {
      Pipeline var2 = var0.pipeline(var1);
      String var3 = var0.defines(var1, var2);
      ShaderPacks.Active var4 = active;
      if (!var4.pack.id.equals(var0.id) || !var4.defines.equals(var3)) {
         active = new ShaderPacks.Active(var0, var2, var3, var4.generation + 1, var1);
      }
   }

   public static void useBuiltIn(String var0, String var1) {
      useBuiltIn(var0, var1, Map.of());
   }

   public static void useBuiltIn(String var0, String var1, Map<String, String> var2) {
      ShaderPack var3 = builtIn(var0);
      PackPreset var4 = var3.preset(var1);
      use(var3, var2x -> var2.getOrDefault(var2x.id, var4.valueFor(var2x)));
   }

   public static void linked(ShaderPacks.Active var0) {
      drawn = var0;
   }

   public static void failed(ShaderPacks.Active var0, String var1) {
      failure = new ShaderPacks.Failure(var0.pack.id, var0.generation, var1);
      System.out.println("[Viewpoint] shader pack " + var0.pack.id + " did not compile, drawn with " + drawn.pack.id + " meanwhile: " + var1);
   }

   public static ShaderPacks.Active library(int var0) {
      return atDefaults(builtIn("library"), var0);
   }

   private static ShaderPacks.Active atDefaults(ShaderPack var0, int var1) {
      Pipeline var2 = var0.pipeline(var0x -> var0x.fallback);
      return new ShaderPacks.Active(var0, var2, var0.defines(var0x -> var0x.fallback, var2), var1);
   }

   private static ShaderPack builtIn(String var0) {
      try {
         return ShaderPack.parse(var0, ShaderPack.jar(var0), var0x -> BUILT_IN.contains(var0x) ? builtIn(var0x) : null, true);
      } catch (IOException | IllegalArgumentException var2) {
         throw new IllegalStateException("built-in shader pack " + var0 + ": " + var2.getMessage(), var2);
      }
   }

   private ShaderPacks() {
   }

   public static final class Active {
      public final ShaderPack pack;
      public final Pipeline pipeline;
      final String defines;
      public final int generation;
      public final List<PackPass> passes;

      Active(ShaderPack var1, Pipeline var2, String var3, int var4) {
         this(var1, var2, var3, var4, var0 -> var0.fallback);
      }

      Active(ShaderPack var1, Pipeline var2, String var3, int var4, Function<PackOption, String> var5) {
         this.pack = var1;
         this.pipeline = var2;
         this.defines = var3;
         this.generation = var4;
         this.passes = var1.passes.stream().filter(var2x -> var2x.when == null || var5.apply(var1.option(var2x.when)).equals("true")).toList();
      }
   }

   public record Failure(String packId, int generation, String log) {
   }
}