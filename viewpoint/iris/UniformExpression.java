package viewpoint.iris;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class UniformExpression {
   private final UniformExpression.Node root;
   private final String text;
   private int at;
   private int smooths;
   private final String owner;
   private static final String[][] LEVELS = new String[][]{{"||"}, {"&&"}, {"==", "!="}, {"<=", ">=", "<", ">"}, {"+", "-"}, {"*", "/", "%"}};

   private UniformExpression(String var1, String var2) {
      this.text = var1;
      this.owner = var2;
      this.root = this.parseAll();
   }

   public static UniformExpression parse(String var0, String var1) {
      return new UniformExpression(var0, var1);
   }

   public double[] evaluate(UniformExpression.Names var1, UniformExpression.State var2) {
      return this.root.eval(var1, var2);
   }

   private UniformExpression.Node parseAll() {
      UniformExpression.Node var1 = this.ternary();
      this.space();
      if (this.at < this.text.length()) {
         throw this.error("unexpected '" + this.text.substring(this.at) + "'");
      } else {
         return var1;
      }
   }

   private IllegalArgumentException error(String var1) {
      return new IllegalArgumentException(this.owner + ": " + var1 + " in '" + this.text + "'");
   }

   private UniformExpression.Node ternary() {
      final UniformExpression.Node var1 = this.binary(0);
      this.space();
      if (this.peek('?')) {
         this.at++;
         final UniformExpression.Node var2 = this.ternary();
         this.space();
         this.expect(':');
         final UniformExpression.Node var3 = this.ternary();
         return new UniformExpression.Node() {
            @Override
            double[] eval(UniformExpression.Names var1x, UniformExpression.State var2x) {
               return var1.eval(var1x, var2x)[0] != 0.0 ? var2.eval(var1x, var2x) : var3.eval(var1x, var2x);
            }
         };
      } else {
         return var1;
      }
   }

   private UniformExpression.Node binary(int var1) {
      if (var1 == LEVELS.length) {
         return this.unary();
      }

      UniformExpression.Node var2 = this.binary(var1 + 1);

      while (true) {
         this.space();
         String var3 = null;

         for (String var7 : LEVELS[var1]) {
            if (this.text.startsWith(var7, this.at)
               && (var7.length() != 1 || this.at + 1 >= this.text.length() || this.text.charAt(this.at + 1) != '=' || !var7.equals("<") && !var7.equals(">"))) {
               var3 = var7;
               break;
            }
         }

         if (var3 == null) {
            return var2;
         }

         this.at = this.at + var3.length();
         UniformExpression.Node var8 = this.binary(var1 + 1);
         var2 = operation(var3, var2, var8);
      }
   }

   private static UniformExpression.Node operation(final String var0, final UniformExpression.Node var1, final UniformExpression.Node var2) {
      return new UniformExpression.Node() {
         @Override
         double[] eval(UniformExpression.Names var1x, UniformExpression.State var2x) {
            double[] var3 = var1.eval(var1x, var2x);
            double[] var4 = var2.eval(var1x, var2x);
            if (var0.equals("&&")) {
               return UniformExpression.scalar(var3[0] != 0.0 && var4[0] != 0.0 ? 1.0 : 0.0);
            }

            if (var0.equals("||")) {
               return UniformExpression.scalar(var3[0] == 0.0 && var4[0] == 0.0 ? 0.0 : 1.0);
            }

            int var5 = Math.max(var3.length, var4.length);
            double[] var6 = new double[var5];

            for (int var7 = 0; var7 < var5; var7++) {
               var6[var7] = UniformExpression.apply(var0, var3[Math.min(var7, var3.length - 1)], var4[Math.min(var7, var4.length - 1)]);
            }

            return var5 > 1 && UniformExpression.isComparison(var0) ? UniformExpression.scalar(UniformExpression.all(var6)) : var6;
         }
      };
   }

   private static boolean isComparison(String var0) {
      return var0.equals("==") || var0.equals("!=") || var0.startsWith("<") || var0.startsWith(">");
   }

   private static double all(double[] var0) {
      for (double var4 : var0) {
         if (var4 == 0.0) {
            return 0.0;
         }
      }

      return 1.0;
   }

   private static double apply(String var0, double var1, double var3) {
      return switch (var0) {
         case "==" -> var1 == var3 ? 1.0 : 0.0;
         case "!=" -> var1 != var3 ? 1.0 : 0.0;
         case "<=" -> var1 <= var3 ? 1.0 : 0.0;
         case ">=" -> var1 >= var3 ? 1.0 : 0.0;
         case "<" -> var1 < var3 ? 1.0 : 0.0;
         case ">" -> var1 > var3 ? 1.0 : 0.0;
         case "+" -> var1 + var3;
         case "-" -> var1 - var3;
         case "*" -> var1 * var3;
         case "/" -> var3 == 0.0 ? 0.0 : var1 / var3;
         case "%" -> var3 == 0.0 ? 0.0 : var1 % var3;
         default -> throw new IllegalStateException(var0);
      };
   }

   private UniformExpression.Node unary() {
      this.space();
      if (this.peek('!')) {
         this.at++;
         final UniformExpression.Node var2 = this.unary();
         return new UniformExpression.Node() {
            @Override
            double[] eval(UniformExpression.Names var1, UniformExpression.State var2x) {
               return UniformExpression.scalar(var2.eval(var1, var2x)[0] == 0.0 ? 1.0 : 0.0);
            }
         };
      } else if (this.peek('-')) {
         this.at++;
         final UniformExpression.Node var1 = this.unary();
         return new UniformExpression.Node() {
            @Override
            double[] eval(UniformExpression.Names var1x, UniformExpression.State var2) {
               double[] var3 = (double[])var1.eval(var1x, var2).clone();

               for (int var4 = 0; var4 < var3.length; var4++) {
                  var3[var4] = -var3[var4];
               }

               return var3;
            }
         };
      } else if (this.peek('+')) {
         this.at++;
         return this.unary();
      } else {
         return this.postfix(this.primary());
      }
   }

   private UniformExpression.Node postfix(UniformExpression.Node var1) {
      UniformExpression.Node var2 = var1;

      while (this.peek('.') && this.at + 1 < this.text.length() && !Character.isWhitespace(this.text.charAt(this.at + 1))) {
         this.at++;
         int var3 = this.at;

         while (this.at < this.text.length() && Macros.isIdentifierPart(this.text.charAt(this.at))) {
            this.at++;
         }

         String var4 = this.text.substring(var3, this.at);
         int var5 = -1;
         if (var4.matches("\\d+") && this.peek('.')) {
            var5 = Integer.parseInt(var4);
            this.at++;
            var3 = this.at;

            while (this.at < this.text.length() && Character.isDigit(this.text.charAt(this.at))) {
               this.at++;
            }

            var4 = this.text.substring(var3, this.at);
         }

         var2 = this.component(var2, var5, var4);
      }

      return var2;
   }

   private UniformExpression.Node component(final UniformExpression.Node var1, int var2, String var3) {
      int var4 = var2 >= 0 ? var2 * 4 + Integer.parseInt(var3) : "xyzwrgbastpq".indexOf(var3.charAt(0)) % 4;
      if (!var3.isEmpty() && var4 >= 0) {
         final int var5 = var4;
         return new UniformExpression.Node() {
            @Override
            double[] eval(UniformExpression.Names var1x, UniformExpression.State var2x) {
               double[] var3x = var1.eval(var1x, var2x);
               return UniformExpression.scalar(var5 < var3x.length ? var3x[var5] : 0.0);
            }
         };
      } else {
         throw this.error("unknown component ." + var3);
      }
   }

   private UniformExpression.Node primary() {
      this.space();
      if (this.peek('(')) {
         this.at++;
         UniformExpression.Node var4 = this.ternary();
         this.space();
         this.expect(')');
         return var4;
      }

      int var1 = this.at;
      if (this.at >= this.text.length() || !Character.isDigit(this.text.charAt(this.at)) && this.text.charAt(this.at) != '.') {
         if (this.at < this.text.length() && Macros.isIdentifierStart(this.text.charAt(this.at))) {
            while (this.at < this.text.length() && Macros.isIdentifierPart(this.text.charAt(this.at))) {
               this.at++;
            }

            String var5 = this.text.substring(var1, this.at);
            this.space();
            if (this.peek('(')) {
               this.at++;
               return this.function(var5.toLowerCase(Locale.ROOT), this.arguments());
            } else {
               return this.name(var5);
            }
         } else {
            throw this.error("cannot read '" + this.text.substring(this.at) + "'");
         }
      } else {
         while (
            this.at < this.text.length()
               && (
                  Character.isDigit(this.text.charAt(this.at))
                     || this.text.charAt(this.at) == '.'
                     || this.text.charAt(this.at) == 'e'
                     || (this.text.charAt(this.at) == '-' || this.text.charAt(this.at) == '+') && this.text.charAt(this.at - 1) == 'e'
               )
         ) {
            this.at++;
         }

         double var2 = Double.parseDouble(this.text.substring(var1, this.at));
         if (this.at < this.text.length() && (this.text.charAt(this.at) == 'f' || this.text.charAt(this.at) == 'F')) {
            this.at++;
         }

         return constant(var2);
      }
   }

   private List<UniformExpression.Node> arguments() {
      ArrayList var1 = new ArrayList();
      this.space();
      if (this.peek(')')) {
         this.at++;
         return var1;
      }

      while (true) {
         var1.add(this.ternary());
         this.space();
         if (!this.peek(',')) {
            this.expect(')');
            return var1;
         }

         this.at++;
      }
   }

   private UniformExpression.Node name(final String var1) {
      switch (var1) {
         case "true":
            return constant(1.0);
         case "false":
            return constant(0.0);
         case "pi":
            return constant(Math.PI);
         default:
            Double var4 = Biomes.constant(var1);
            return var4 != null ? constant(var4) : new UniformExpression.Node() {
               @Override
               double[] eval(UniformExpression.Names var1x, UniformExpression.State var2) {
                  double[] var3 = var1x.value(var1);
                  return var3 == null ? UniformExpression.scalar(0.0) : var3;
               }
            };
      }
   }

   private UniformExpression.Node function(String var1, List<UniformExpression.Node> var2) {
      return switch (var1) {
         case "if" -> this.ifNode(var2);
         case "smooth" -> this.smoothNode(var2);
         case "vec2", "vec3", "vec4" -> this.vector(var1.charAt(3) - '0', var2);
         default -> this.math(var1, var2);
      };
   }

   private UniformExpression.Node ifNode(final List<UniformExpression.Node> var1) {
      return new UniformExpression.Node() {
         @Override
         double[] eval(UniformExpression.Names var1x, UniformExpression.State var2) {
            byte var3;
            for (var3 = 0; var3 + 1 < var1.size(); var3 += 2) {
               if (((UniformExpression.Node)var1.get(var3)).eval(var1x, var2)[0] != 0.0) {
                  return ((UniformExpression.Node)var1.get(var3 + 1)).eval(var1x, var2);
               }
            }

            return var3 < var1.size() ? ((UniformExpression.Node)var1.get(var3)).eval(var1x, var2) : UniformExpression.scalar(0.0);
         }
      };
   }

   private UniformExpression.Node smoothNode(List<UniformExpression.Node> var1) {
      final String var2 = this.owner + "#" + this.smooths++;
      final List var3 = var1.size() == 4 ? var1.subList(1, var1.size()) : var1;
      return new UniformExpression.Node() {
         @Override
         double[] eval(UniformExpression.Names var1, UniformExpression.State var2x) {
            double var3x = ((UniformExpression.Node)var3.get(0)).eval(var1, var2x)[0];
            double var5 = var3.size() > 1 ? ((UniformExpression.Node)var3.get(1)).eval(var1, var2x)[0] : 1.0;
            double var7 = var3.size() > 2 ? ((UniformExpression.Node)var3.get(2)).eval(var1, var2x)[0] : var5;
            Double var9 = var2x.eased.get(var2);
            if (var9 == null) {
               var2x.eased.put(var2, var3x);
               return UniformExpression.scalar(var3x);
            } else {
               double var10 = var3x > var9 ? var5 : var7;
               double var12 = var10 <= 0.0 ? 1.0 : 1.0 - Math.exp(-var2x.frameSeconds * 3.0 / var10);
               double var14 = var9 + (var3x - var9) * var12;
               var2x.eased.put(var2, var14);
               return UniformExpression.scalar(var14);
            }
         }
      };
   }

   private UniformExpression.Node vector(final int var1, final List<UniformExpression.Node> var2) {
      return new UniformExpression.Node() {
         @Override
         double[] eval(UniformExpression.Names var1x, UniformExpression.State var2x) {
            double[] var3 = new double[var1];
            int var4 = 0;

            for (UniformExpression.Node var6 : var2) {
               for (double var10 : var6.eval(var1x, var2x)) {
                  if (var4 < var1) {
                     var3[var4++] = var10;
                  }
               }
            }

            while (var4 < var1 && var4 > 0) {
               var3[var4] = var3[var4 - 1];
               var4++;
            }

            return var3;
         }
      };
   }

   private UniformExpression.Node math(final String var1, final List<UniformExpression.Node> var2) {
      if (!Functions.known(var1)) {
         throw this.error("unknown function " + var1 + "()");
      } else {
         return new UniformExpression.Node() {
            @Override
            double[] eval(UniformExpression.Names var1x, UniformExpression.State var2x) {
               double[][] var3 = new double[var2.size()][];

               for (int var4 = 0; var4 < var3.length; var4++) {
                  var3[var4] = ((UniformExpression.Node)var2.get(var4)).eval(var1x, var2x);
               }

               return Functions.apply(var1, var3);
            }
         };
      }
   }

   private static UniformExpression.Node constant(double var0) {
      final double[] var2 = scalar(var0);
      return new UniformExpression.Node() {
         @Override
         double[] eval(UniformExpression.Names var1, UniformExpression.State var2x) {
            return var2;
         }
      };
   }

   static double[] scalar(double var0) {
      return new double[]{var0};
   }

   private boolean peek(char var1) {
      return this.at < this.text.length() && this.text.charAt(this.at) == var1;
   }

   private void expect(char var1) {
      if (!this.peek(var1)) {
         throw this.error("expected '" + var1 + "'");
      }

      this.at++;
   }

   private void space() {
      while (this.at < this.text.length() && Character.isWhitespace(this.text.charAt(this.at))) {
         this.at++;
      }
   }

   public interface Names {
      double[] value(String var1);
   }

   private abstract static class Node {
      abstract double[] eval(UniformExpression.Names var1, UniformExpression.State var2);
   }

   public static final class State {
      final Map<String, Double> eased = new HashMap<>();
      double frameSeconds;

      public void frame(double var1) {
         this.frameSeconds = var1;
      }
   }
}
