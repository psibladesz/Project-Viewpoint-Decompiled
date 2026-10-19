package viewpoint.iris;

import java.util.Set;

final class Functions {
   private static final Set<String> NAMES = Set.of(
      "sin",
      "cos",
      "tan",
      "asin",
      "acos",
      "atan",
      "atan2",
      "torad",
      "todeg",
      "min",
      "max",
      "clamp",
      "abs",
      "floor",
      "ceil",
      "exp",
      "exp2",
      "frac",
      "fract",
      "log",
      "log2",
      "pow",
      "round",
      "signum",
      "sign",
      "sqrt",
      "fmod",
      "mod",
      "between",
      "equals",
      "in",
      "random",
      "mix",
      "lerp",
      "smoothstep",
      "step",
      "length",
      "dot",
      "normalize",
      "hsv",
      "not"
   );

   static boolean known(String var0) {
      return NAMES.contains(var0);
   }

   static double[] apply(String var0, double[][] var1) {
      return switch (var0) {
         case "min" -> fold(var1, true);
         case "max" -> fold(var1, false);
         case "clamp" -> each3(var1, (var0x, var2, var4) -> Math.max(var2, Math.min(var4, var0x)));
         case "pow" -> each2(var1, Math::pow);
         case "atan2" -> each2(var1, Math::atan2);
         case "fmod", "mod" -> each2(var1, (var0x, var2) -> var2 == 0.0 ? 0.0 : var0x - var2 * Math.floor(var0x / var2));
         case "step" -> each2(var1, (var0x, var2) -> var2 < var0x ? 0.0 : 1.0);
         case "mix", "lerp" -> each3(var1, (var0x, var2, var4) -> var0x + (var2 - var0x) * var4);
         case "smoothstep" -> each3(var1, (var0x, var2, var4) -> smoothstep(var0x, var2, var4));
         case "between" -> scalar(var1[0][0] >= var1[1][0] && var1[0][0] <= var1[2][0]);
         case "equals" -> scalar(Math.abs(var1[0][0] - var1[1][0]) <= (var1.length > 2 ? var1[2][0] : 0.0));
         case "in" -> in(var1);
         case "random" -> new double[]{Math.random()};
         case "length" -> new double[]{Math.sqrt(dot(var1[0], var1[0]))};
         case "dot" -> new double[]{dot(var1[0], var1[1])};
         case "normalize" -> normalize(var1[0]);
         case "not" -> scalar(var1[0][0] == 0.0);
         default -> each1(var0, var1[0]);
      };
   }

   private static double[] each1(String var0, double[] var1) {
      double[] var2 = new double[var1.length];

      for (int var3 = 0; var3 < var1.length; var3++) {
         double var4 = var1[var3];

         var2[var3] = switch (var0) {
            case "sin" -> Math.sin(var4);
            case "cos" -> Math.cos(var4);
            case "tan" -> Math.tan(var4);
            case "asin" -> Math.asin(var4);
            case "acos" -> Math.acos(var4);
            case "atan" -> Math.atan(var4);
            case "torad" -> Math.toRadians(var4);
            case "todeg" -> Math.toDegrees(var4);
            case "abs" -> Math.abs(var4);
            case "floor" -> Math.floor(var4);
            case "ceil" -> Math.ceil(var4);
            case "exp" -> Math.exp(var4);
            case "exp2" -> Math.pow(2.0, var4);
            case "frac", "fract" -> var4 - Math.floor(var4);
            case "log" -> Math.log(var4);
            case "log2" -> Math.log(var4) / Math.log(2.0);
            case "round" -> Math.rint(var4);
            case "signum", "sign" -> Math.signum(var4);
            case "sqrt" -> Math.sqrt(var4);
            default -> throw new IllegalArgumentException("unknown function " + var0);
         };
      }

      return var2;
   }

   private static double[] each2(double[][] var0, Functions.Op2 var1) {
      int var2 = Math.max(var0[0].length, var0[1].length);
      double[] var3 = new double[var2];

      for (int var4 = 0; var4 < var2; var4++) {
         var3[var4] = var1.apply(at(var0[0], var4), at(var0[1], var4));
      }

      return var3;
   }

   private static double[] each3(double[][] var0, Functions.Op3 var1) {
      int var2 = Math.max(var0[0].length, Math.max(var0[1].length, var0[2].length));
      double[] var3 = new double[var2];

      for (int var4 = 0; var4 < var2; var4++) {
         var3[var4] = var1.apply(at(var0[0], var4), at(var0[1], var4), at(var0[2], var4));
      }

      return var3;
   }

   private static double[] fold(double[][] var0, boolean var1) {
      double[] var2 = (double[])var0[0].clone();

      for (int var3 = 1; var3 < var0.length; var3++) {
         for (int var4 = 0; var4 < var2.length; var4++) {
            double var5 = at(var0[var3], var4);
            var2[var4] = var1 ? Math.min(var2[var4], var5) : Math.max(var2[var4], var5);
         }
      }

      return var2;
   }

   private static double[] in(double[][] var0) {
      for (int var1 = 1; var1 < var0.length; var1++) {
         if (var0[var1][0] == var0[0][0]) {
            return scalar(true);
         }
      }

      return scalar(false);
   }

   private static double at(double[] var0, int var1) {
      return var0[Math.min(var1, var0.length - 1)];
   }

   private static double dot(double[] var0, double[] var1) {
      double var2 = 0.0;

      for (int var4 = 0; var4 < Math.min(var0.length, var1.length); var4++) {
         var2 += var0[var4] * var1[var4];
      }

      return var2;
   }

   private static double[] normalize(double[] var0) {
      double var1 = Math.sqrt(dot(var0, var0));
      double[] var3 = (double[])var0.clone();

      for (int var4 = 0; var4 < var3.length && var1 > 0.0; var4++) {
         var3[var4] /= var1;
      }

      return var3;
   }

   private static double smoothstep(double var0, double var2, double var4) {
      double var6 = Math.max(0.0, Math.min(1.0, (var4 - var0) / (var2 - var0)));
      return var6 * var6 * (3.0 - 2.0 * var6);
   }

   private static double[] scalar(boolean var0) {
      return new double[]{var0 ? 1.0 : 0.0};
   }

   private Functions() {
   }

   private interface Op2 {
      double apply(double var1, double var3);
   }

   private interface Op3 {
      double apply(double var1, double var3, double var5);
   }
}
