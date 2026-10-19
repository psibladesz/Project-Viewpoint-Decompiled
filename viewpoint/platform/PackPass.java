package viewpoint.platform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public final class PackPass {
   public static final String COLOR = "color";
   public static final String PREVIOUS = ".previous";
   public static final int MAX_TARGETS = 8;
   public static final int MAX_INPUTS = 12;
   private static final Map<String, PackPass.Stage> RENDERER = Map.of(
           "color",
           PackPass.Stage.TRANSLUCENT,
           "depth",
           PackPass.Stage.TRANSLUCENT,
           "albedo",
           PackPass.Stage.TRANSLUCENT,
           "normal",
           PackPass.Stage.TRANSLUCENT,
           "light",
           PackPass.Stage.TRANSLUCENT,
           "velocity",
           PackPass.Stage.TRANSLUCENT,
           "farDepth",
           PackPass.Stage.TRANSLUCENT,
           "occlusion",
           PackPass.Stage.TRANSLUCENT,
           "volume",
           PackPass.Stage.COMPOSITE,
           "bloom",
           PackPass.Stage.COMPOSITE
   );
   public final String id;
   public final String vertex;
   public final String fragment;
   public final PackPass.Stage stage;
   public final List<String> inputs;
   public final String target;
   public final String when;

   private PackPass(String var1, PackPass.Stage var2, String var3, String var4, List<String> var5, String var6, String var7) {
      this.id = var1;
      this.stage = var2;
      this.vertex = var3;
      this.fragment = var4;
      this.inputs = List.copyOf(var5);
      this.target = var6;
      this.when = var7;
   }

   public static String sampler(String var0) {
      String var1 = var0.endsWith(".previous") ? var0.substring(0, var0.length() - ".previous".length()) + "Previous" : var0;
      return "u" + Character.toUpperCase(var1.charAt(0)) + var1.substring(1);
   }

   static PackPass.Graph parse(Properties var0, PackPass.Graph var1, List<PackOption> var2) {
      LinkedHashMap<String, PackTarget> var3 = new LinkedHashMap<>();
      var1.targets.forEach(var1x -> var3.put(var1x.name, var1x));

      for (String var5 : words(var0.getProperty("targets", ""))) {
         String var6 = var0.getProperty("target." + var5);
         if (var6 == null) {
            throw new IllegalArgumentException("target." + var5 + " is missing");
         }

         var3.put(var5, PackTarget.parse(var5, var6));
      }

      if (var3.size() > 8) {
         throw new IllegalArgumentException("targets: at most 8, not " + var3.size());
      }

      LinkedHashMap<String, PackPass> var7 = new LinkedHashMap<>();
      var1.passes.forEach(var1x -> var7.put(var1x.id, var1x));

      for (String var9 : words(var0.getProperty("passes", ""))) {
         var7.put(var9, parse(var9, var0, var3, var2));
      }

      return new PackPass.Graph(List.copyOf(var3.values()), List.copyOf(var7.values()));
   }

   private static PackPass parse(String var0, Properties var1, Map<String, PackTarget> var2, List<PackOption> var3) {
      String var4 = "pass." + var0 + ".";
      if (!PackOption.ID.matcher(var0).matches()) {
         throw new IllegalArgumentException("passes: " + var0 + " is not letters and digits");
      }

      PackPass.Stage var5 = stage(var4, var1.getProperty(var4 + "stage"));
      String var6 = var1.getProperty(var4 + "fragment");
      if (var6 != null && !var6.isBlank()) {
         ArrayList<String> var7 = new ArrayList<>(words(var1.getProperty(var4 + "inputs", "")));
         if (var7.size() > 12) {
            throw new IllegalArgumentException(var4 + "inputs: at most 12");
         }

         for (String var9 : var7) {
            checkInput(var4, var9, var5, var2);
         }

         String var11 = var1.getProperty(var4 + "target", "color").trim();
         if (!var11.equals("color") && !var2.containsKey(var11)) {
            throw new IllegalArgumentException(var4 + "target: " + var11 + " is neither color nor a target");
         }

         if (!var11.equals("color") && var7.contains(var11)) {
            throw new IllegalArgumentException(var4 + "inputs: it cannot read " + var11 + " as it writes it; " + var11 + ".previous is last frame's");
         }

         String var12 = var1.getProperty(var4 + "when");
         if (var12 != null) {
            var12 = var12.trim();
            String var10 = var12;
            if (var3.stream().noneMatch(var1x -> var1x.id.equals(var10) && var1x.kind == PackOption.Kind.TOGGLE)) {
               throw new IllegalArgumentException(var4 + "when: " + var12 + " is no toggle option of the pack");
            }
         }

         String var13 = var1.getProperty(var4 + "vertex", "screen.vert").trim();
         return new PackPass(var0, var5, var13, var6.trim(), var7, var11, var12);
      } else {
         throw new IllegalArgumentException(var4 + "fragment is missing");
      }
   }

   private static PackPass.Stage stage(String var0, String var1) {
      if (var1 == null) {
         throw new IllegalArgumentException(var0 + "stage is missing");
      }

      for (PackPass.Stage var5 : PackPass.Stage.values()) {
         if (var5.name().toLowerCase(Locale.ROOT).equals(var1.trim())) {
            return var5;
         }
      }

      throw new IllegalArgumentException(var0 + "stage: " + var1 + " is not translucent, composite or final");
   }

   private static void checkInput(String var0, String var1, PackPass.Stage var2, Map<String, PackTarget> var3) {
      PackPass.Stage var4 = RENDERER.get(var1);
      if (var4 != null) {
         if (var4.compareTo(var2) > 0) {
            throw new IllegalArgumentException(var0 + "inputs: " + var1 + " is not there yet at " + var2);
         }
      } else {
         boolean var5 = var1.endsWith(".previous");
         String var6 = var5 ? var1.substring(0, var1.length() - ".previous".length()) : var1;
         PackTarget var7 = var3.get(var6);
         if (var7 == null) {
            throw new IllegalArgumentException(var0 + "inputs: " + var1 + " is neither the renderer's nor a target");
         }

         if (var5 && !var7.history) {
            throw new IllegalArgumentException(var0 + "inputs: " + var6 + " keeps no history (target." + var6 + ")");
         }
      }
   }

   private static List<String> words(String var0) {
      ArrayList<String> var1 = new ArrayList<>();

      for (String var5 : var0.trim().split("\\s+")) {
         if (!var5.isEmpty()) {
            var1.add(var5);
         }
      }

      return var1;
   }

   @Override
   public String toString() {
      return this.id;
   }

   record Graph(List<PackTarget> targets, List<PackPass> passes) {
   }

   public enum Stage {
      TRANSLUCENT,
      COMPOSITE,
      FINAL;
   }
}