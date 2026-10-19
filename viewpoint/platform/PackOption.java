package viewpoint.platform;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public final class PackOption {
   public static final List<String> PRESETS = List.of("potato", "low", "default", "high", "ultra");
   static final Pattern ID = Pattern.compile("[A-Za-z][A-Za-z0-9]*");
   private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");
   private static final Pattern VALUE = Pattern.compile("[A-Za-z0-9_.-]+");
   public final String id;
   public final String label;
   public final String tooltip;
   public final PackOption.Kind kind;
   final List<String> values;
   final List<String> labels;
   final float min;
   final float max;
   final float step;
   final String fallback;
   public final String disabledBy;
   public final boolean kept;

   private PackOption(
           String var1, String var2, String var3, PackOption.Kind var4, List<String> var5, List<String> var6, float[] var7, String var8, String var9, boolean var10
   ) {
      this.id = var1;
      this.label = var2;
      this.tooltip = var3;
      this.kind = var4;
      this.values = var5;
      this.labels = var6;
      this.min = var7[0];
      this.max = var7[1];
      this.step = var7[2];
      this.fallback = var8;
      this.disabledBy = var9;
      this.kept = var10;
   }

   static PackOption parse(String var0, Properties var1) {
      if (!ID.matcher(var0).matches()) {
         throw new IllegalArgumentException("option id " + var0 + " is not letters and digits");
      }

      String var2 = "option." + var0 + ".";
      String var3 = var1.getProperty(var2 + "values");
      String var4 = var1.getProperty(var2 + "range");
      String var5 = var1.getProperty(var2 + "default");
      if (var5 == null) {
         throw new IllegalArgumentException(var2 + "default is missing");
      }

      PackOption.Kind var6 = var3 != null ? PackOption.Kind.CHOICE : (var4 != null ? PackOption.Kind.NUMBER : PackOption.Kind.TOGGLE);
      List<String> var7 = var3 == null ? List.<String>of() : List.of(var3.trim().split("\\s+"));
      if (var7.stream().anyMatch(var0x -> !VALUE.matcher(var0x).matches())) {
         throw new IllegalArgumentException(var2 + "values are words of letters, digits, _, . and -: " + var3);
      }

      float[] var8 = var4 == null ? new float[3] : range(var2, var4);
      LinkedHashMap<String, String> var9 = new LinkedHashMap<>();

      for (String var11 : PRESETS) {
         String var12 = var1.getProperty(var2 + "preset." + var11);
         if (var12 != null) {
            var9.put(var11, var12.trim());
         }
      }

      for (String var15 : var1.stringPropertyNames()) {
         if (var15.startsWith(var2 + "preset.") && !var9.containsKey(var15.substring(var2.length() + 7))) {
            throw new IllegalArgumentException(var15 + ": no such preset (" + String.join(", ", PRESETS) + ")");
         }
      }

      PackOption var14 = new PackOption(
              var0,
              var1.getProperty(var2 + "label", var0).trim(),
              var1.getProperty(var2 + "tooltip", "").trim(),
              var6,
              var7,
              var7.stream().map(var2x -> var1.getProperty(var2 + "value." + var2x, var2x).trim()).toList(),
              var8,
              var5.trim(),
              var1.getProperty(var2 + "disabledBy", "").trim(),
              Boolean.parseBoolean(var1.getProperty(var2 + "kept", "false").trim())
      );
      var14.check(var2 + "default", var14.fallback);
      if (var14.kept && !var9.isEmpty()) {
         throw new IllegalArgumentException(var2 + "kept: no preset sets it, so it has no preset values");
      }

      for (Entry<String, String> var17 : var9.entrySet()) {
         var14.check(var2 + "preset." + var17.getKey(), var17.getValue());
      }

      return var14;
   }

   private static float[] range(String var0, String var1) {
      String[] var2 = var1.trim().split("\\s+");

      try {
         float[] var3 = new float[]{Float.parseFloat(var2[0]), Float.parseFloat(var2[1]), Float.parseFloat(var2[2])};
         if (var2.length == 3 && var3[0] < var3[1] && var3[2] > 0.0F) {
            return var3;
         }
      } catch (NumberFormatException | ArrayIndexOutOfBoundsException var4) {
      }

      throw new IllegalArgumentException(var0 + "range is min max step, not " + var1);
   }

   void check(String var1, String var2) {
      boolean var3 = switch (this.kind) {
         case TOGGLE -> var2.equals("true") || var2.equals("false");
         case NUMBER -> NUMBER.matcher(var2).matches() && Float.parseFloat(var2) >= this.min && Float.parseFloat(var2) <= this.max;
         case CHOICE -> this.values.contains(var2);
      };
      if (!var3) {
         throw new IllegalArgumentException(var1 + ": " + var2 + " is not one of " + this.describe());
      }
   }

   private String describe() {
      return switch (this.kind) {
         case TOGGLE -> "true, false";
         case NUMBER -> this.min + " to " + this.max;
         case CHOICE -> String.join(", ", this.values);
      };
   }

   List<String> extremes() {
      return switch (this.kind) {
         case TOGGLE -> List.of("true", "false");
         case NUMBER -> List.of(text(this.min), text(this.max));
         case CHOICE -> this.values;
      };
   }

   public boolean on(String var1) {
      return this.kind == PackOption.Kind.TOGGLE ? var1.equals("true") : !var1.equals(this.values.get(0));
   }

   boolean same(String var1, String var2) {
      return this.kind == PackOption.Kind.NUMBER ? Float.parseFloat(var1) == Float.parseFloat(var2) : var1.equals(var2);
   }

   void define(StringBuilder var1, String var2) {
      String var3 = "OPTION_" + constant(this.id);
      switch (this.kind) {
         case TOGGLE:
            line(var1, var3, var2.equals("true") ? "1" : "0");
            break;
         case NUMBER:
            line(var1, var3, this.step >= 1.0F ? text(Math.round(Float.parseFloat(var2))) : var2);
            break;
         case CHOICE:
            if (this.values.stream().allMatch(var0 -> NUMBER.matcher(var0).matches())) {
               line(var1, var3, var2);
            } else {
               line(var1, var3, Integer.toString(this.values.indexOf(var2)));

               for (int var4 = 0; var4 < this.values.size(); var4++) {
                  line(var1, var3 + "_" + constant(this.values.get(var4)), Integer.toString(var4));
               }
            }
      }
   }

   LiveSettings.Setting declare(String var1, String var2) {
      return switch (this.kind) {
         case TOGGLE -> LiveSettings.toggle(var1, this.label, var2, this.fallback.equals("true"));
         case NUMBER -> LiveSettings.number(var1, this.label, var2, this.min, this.max, this.step, Float.parseFloat(this.fallback));
         case CHOICE -> {
            LiveSettings.Choice var3 = LiveSettings.choice(var1, this.label, var2, this.values.toArray(new String[0]), this.values.indexOf(this.fallback));
            var3.shownAs(this.labels.toArray(new String[0]));
            yield var3;
         }
      };
   }

   static String constant(String var0) {
      StringBuilder var1 = new StringBuilder();

      for (int var2 = 0; var2 < var0.length(); var2++) {
         char var3 = var0.charAt(var2);
         boolean var4 = var2 > 0 && Character.isUpperCase(var3) && !Character.isUpperCase(var0.charAt(var2 - 1));
         var1.append(var4 ? "_" : "").append(Character.isLetterOrDigit(var3) ? Character.toUpperCase(var3) : '_');
      }

      return var1.toString();
   }

   private static void line(StringBuilder var0, String var1, String var2) {
      var0.append("#define ").append(var1).append(' ').append(var2).append('\n');
   }

   private static String text(float var0) {
      return var0 == Math.rint(var0) ? Integer.toString((int)var0) : Float.toString(var0);
   }

   public enum Kind {
      TOGGLE,
      NUMBER,
      CHOICE;
   }
}