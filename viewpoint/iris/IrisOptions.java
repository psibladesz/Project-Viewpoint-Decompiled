package viewpoint.iris;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IrisOptions {
   private static final Pattern VALUE = Pattern.compile("^\\s*#\\s*define\\s+(\\w+)\\s+([^\\s/]+)\\s*//[^\\[]*\\[([^\\]]*)\\].*$");
   private static final Pattern TOGGLE = Pattern.compile("^\\s*(//+)?\\s*#\\s*define\\s+(\\w+)\\s*(//.*)?$");
   private static final Pattern CONST = Pattern.compile("^\\s*const\\s+(?:int|float|bool)\\s+(\\w+)\\s*=\\s*([^;]+?)\\s*;\\s*//[^\\[]*\\[([^\\]]*)\\].*$");
   private static final Pattern INCLUDED = Pattern.compile("(?m)^\\s*#\\s*include\\s*[\"<]([^\">]+)[\">]");
   private static final Pattern TESTED = Pattern.compile("#\\s*(?:ifdef|ifndef)\\s+(\\w+)|defined\\s*\\(?\\s*(\\w+)");
   private final Map<String, IrisOptions.Option> options = new LinkedHashMap<>();

   private IrisOptions() {
   }

   public static IrisOptions scan(PackSource var0) throws IOException {
      IrisOptions var1 = new IrisOptions();
      HashSet<String> var2 = new HashSet<>();
      ArrayList<String[]> var3 = new ArrayList<>();

      for (String var5 : sources(var0)) {
         String var6 = var0.read(var5);
         Matcher var7 = TESTED.matcher(var6);

         while (var7.find()) {
            var2.add(var7.group(1) != null ? var7.group(1) : var7.group(2));
         }

         for (String var11 : var6.split("\n")) {
            var1.line(var11, var5, var3);
         }
      }

      for (String[] var13 : var3) {
         if (var2.contains(var13[0]) && !var1.options.containsKey(var13[0])) {
            var1.options.put(var13[0], new IrisOptions.Option(var13[0], IrisOptions.Kind.TOGGLE, var13[1], List.of(), var13[2]));
         }
      }

      return var1;
   }

   private void line(String var1, String var2, List<String[]> var3) {
      if (var1.contains("define") || var1.contains("const")) {
         Matcher var4 = VALUE.matcher(var1);
         if (var4.matches()) {
            this.add(var4.group(1), IrisOptions.Kind.VALUE, var4.group(2), var4.group(3), var2);
         } else {
            Matcher var5 = CONST.matcher(var1);
            if (var5.matches()) {
               this.add(var5.group(1), IrisOptions.Kind.CONST, var5.group(2).strip(), var5.group(3), var2);
            } else {
               Matcher var6 = TOGGLE.matcher(var1);
               if (var6.matches()) {
                  var3.add(new String[]{var6.group(2), var6.group(1) == null ? "true" : "false", var2});
               }
            }
         }
      }
   }

   private void add(String var1, IrisOptions.Kind var2, String var3, String var4, String var5) {
      if (!this.options.containsKey(var1)) {
         ArrayList<String> var6 = new ArrayList<>();

         for (String var10 : var4.strip().split("\\s+")) {
            if (!var10.isEmpty()) {
               var6.add(var10);
            }
         }

         if (!var6.contains(var3)) {
            var6.add(var3);
         }

         this.options.put(var1, new IrisOptions.Option(var1, var2, var3, List.copyOf(var6), var5));
      }
   }

   public List<IrisOptions.Option> all() {
      return List.copyOf(this.options.values());
   }

   public IrisOptions.Option get(String var1) {
      return this.options.get(var1);
   }

   public Set<String> toggles() {
      HashSet<String> var1 = new HashSet<>();

      for (IrisOptions.Option var3 : this.options.values()) {
         if (var3.kind == IrisOptions.Kind.TOGGLE) {
            var1.add(var3.name);
         }
      }

      return Collections.unmodifiableSet(var1);
   }

   public Map<String, String> changed(Map<String, String> var1) {
      LinkedHashMap<String, String> var2 = new LinkedHashMap<>();

      for (Entry<String, String> var4 : var1.entrySet()) {
         IrisOptions.Option var5 = this.options.get(var4.getKey());
         if (var5 != null && !var5.fallback.equals(var4.getValue())) {
            var2.put(var4.getKey(), var4.getValue());
         }
      }

      return var2;
   }

   public Map<String, String> macros(Map<String, String> var1) {
      LinkedHashMap<String, String> var2 = new LinkedHashMap<>();

      for (IrisOptions.Option var4 : this.options.values()) {
         String var5 = var1.getOrDefault(var4.name, var4.fallback);
         if (var4.kind == IrisOptions.Kind.TOGGLE) {
            if (var5.equals("true")) {
               var2.put(var4.name, "1");
            }
         } else {
            var2.put(var4.name, var5);
         }
      }

      return var2;
   }

   public Map<String, String> values(Map<String, String> var1) {
      LinkedHashMap<String, String> var2 = new LinkedHashMap<>();

      for (IrisOptions.Option var4 : this.options.values()) {
         var2.put(var4.name, var1.getOrDefault(var4.name, var4.fallback));
      }

      return var2;
   }

   static Set<String> sources(PackSource var0) throws IOException {
      LinkedHashSet<String> var1 = new LinkedHashSet<>();
      ArrayDeque<String> var2 = new ArrayDeque<>();

      for (String var4 : var0.paths()) {
         if (isSource(var4)) {
            var2.add(var4);
         }
      }

      while (!var2.isEmpty()) {
         String var8 = var2.poll();
         if (var1.add(var8)) {
            Matcher var9 = INCLUDED.matcher(var0.read(var8));
            String var5 = var8.contains("/") ? var8.substring(0, var8.lastIndexOf(47) + 1) : "";

            while (var9.find()) {
               String var6 = var9.group(1);
               String var7 = PackSource.normalize(var6.startsWith("/") ? var6 : var5 + var6);
               if (var7 != null && !var1.contains(var7) && var0.exists(var7)) {
                  var2.add(var7);
               }
            }
         }
      }

      return var1;
   }

   private static boolean isSource(String var0) {
      return var0.endsWith(".glsl")
              || var0.endsWith(".fsh")
              || var0.endsWith(".vsh")
              || var0.endsWith(".gsh")
              || var0.endsWith(".csh")
              || var0.endsWith(".tcs")
              || var0.endsWith(".tes");
   }

   public enum Kind {
      TOGGLE,
      VALUE,
      CONST;
   }

   public record Option(String name, IrisOptions.Kind kind, String fallback, List<String> values, String file) {
   }
}