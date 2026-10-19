package viewpoint.iris;

import java.util.Locale;

final class IfExpression {
   private final String text;
   private int at;
   private static final String[][] LEVELS = new String[][]{
      {"||"}, {"&&"}, {"|"}, {"^"}, {"&"}, {"==", "!="}, {"<=", ">=", "<", ">"}, {"<<", ">>"}, {"+", "-"}, {"*", "/", "%"}
   };

   private IfExpression(String var1) {
      this.text = var1;
   }

   static boolean test(String var0, Macros var1) {
      return evaluate(var0, var1) != 0.0;
   }

   static double evaluate(String var0, Macros var1) {
      String var2 = var1.expand(definedAnswered(Macros.stripComments(var0), var1));
      IfExpression var3 = new IfExpression(var2);
      double var4 = var3.ternary();
      var3.space();
      if (var3.at < var3.text.length()) {
         throw new IllegalArgumentException("cannot read the condition '" + var0.strip() + "' at '" + var3.text.substring(var3.at).strip() + "'");
      } else {
         return var4;
      }
   }

   private static String definedAnswered(String var0, Macros var1) {
      StringBuilder var2 = new StringBuilder(var0.length());
      int var3 = 0;
      int var4 = var0.length();

      while (var3 < var4) {
         if (var0.startsWith("defined", var3)
            && (var3 == 0 || !Macros.isIdentifierPart(var0.charAt(var3 - 1)))
            && (var3 + 7 >= var4 || !Macros.isIdentifierPart(var0.charAt(var3 + 7)))) {
            int var5 = var3 + 7;

            while (var5 < var4 && Character.isWhitespace(var0.charAt(var5))) {
               var5++;
            }

            boolean var6 = var5 < var4 && var0.charAt(var5) == '(';
            if (var6) {
               var5++;
            }

            while (var5 < var4 && Character.isWhitespace(var0.charAt(var5))) {
               var5++;
            }

            int var7 = var5;

            while (var5 < var4 && Macros.isIdentifierPart(var0.charAt(var5))) {
               var5++;
            }

            String var8 = var0.substring(var7, var5);
            if (var6) {
               while (var5 < var4 && var0.charAt(var5) != ')') {
                  var5++;
               }

               var5++;
            }

            var2.append(var1.defined(var8) ? " 1 " : " 0 ");
            var3 = var5;
         } else {
            var2.append(var0.charAt(var3));
            var3++;
         }
      }

      return var2.toString();
   }

   private double ternary() {
      double var1 = this.binary(0);
      this.space();
      if (this.peek('?')) {
         this.at++;
         double var3 = this.ternary();
         this.space();
         this.expect(':');
         double var5 = this.ternary();
         return var1 != 0.0 ? var3 : var5;
      } else {
         return var1;
      }
   }

   private double binary(int var1) {
      if (var1 == LEVELS.length) {
         return this.unary();
      }

      double var2 = this.binary(var1 + 1);

      while (true) {
         this.space();
         String var4 = this.operator(LEVELS[var1]);
         if (var4 == null) {
            return var2;
         }

         double var5 = this.binary(var1 + 1);
         var2 = apply(var4, var2, var5);
      }
   }

   private String operator(String[] var1) {
      String[] var2 = var1;
      int var3 = var2.length;
      int var4 = 0;

      String var5;
      while (true) {
         if (var4 >= var3) {
            return null;
         }

         var5 = var2[var4];
         if (this.text.startsWith(var5, this.at)) {
            if (var5.length() != 1 || this.at + 1 >= this.text.length()) {
               break;
            }

            char var6 = this.text.charAt(this.at + 1);
            if ((!var5.equals("|") || var6 != '|')
               && (!var5.equals("&") || var6 != '&')
               && (!var5.equals("<") || var6 != '<' && var6 != '=')
               && (!var5.equals(">") || var6 != '>' && var6 != '=')) {
               break;
            }
         }

         var4++;
      }

      this.at = this.at + var5.length();
      return var5;
   }

   private static double apply(String var0, double var1, double var3) {
      return switch (var0) {
         case "||" -> var1 == 0.0 && var3 == 0.0 ? 0.0 : 1.0;
         case "&&" -> var1 != 0.0 && var3 != 0.0 ? 1.0 : 0.0;
         case "|" -> (long)var1 | (long)var3;
         case "^" -> (long)var1 ^ (long)var3;
         case "&" -> (long)var1 & (long)var3;
         case "==" -> var1 == var3 ? 1.0 : 0.0;
         case "!=" -> var1 != var3 ? 1.0 : 0.0;
         case "<=" -> var1 <= var3 ? 1.0 : 0.0;
         case ">=" -> var1 >= var3 ? 1.0 : 0.0;
         case "<" -> var1 < var3 ? 1.0 : 0.0;
         case ">" -> var1 > var3 ? 1.0 : 0.0;
         case "<<" -> (long)var1 << (int)((long)var3);
         case ">>" -> (long)var1 >> (int)((long)var3);
         case "+" -> var1 + var3;
         case "-" -> var1 - var3;
         case "*" -> var1 * var3;
         case "/" -> var3 == 0.0 ? 0.0 : (isWhole(var1) && isWhole(var3) ? (long)var1 / (long)var3 : var1 / var3);
         case "%" -> var3 == 0.0 ? 0.0 : var1 % var3;
         default -> throw new IllegalStateException(var0);
      };
   }

   private static boolean isWhole(double var0) {
      return var0 == Math.rint(var0);
   }

   private double unary() {
      this.space();
      if (this.peek('!')) {
         this.at++;
         return this.unary() == 0.0 ? 1.0 : 0.0;
      } else if (this.peek('-')) {
         this.at++;
         return -this.unary();
      } else if (this.peek('+')) {
         this.at++;
         return this.unary();
      } else if (this.peek('~')) {
         this.at++;
         return ~((long)this.unary());
      } else {
         return this.primary();
      }
   }

   private double primary() {
      this.space();
      if (this.peek('(')) {
         this.at++;
         double var3 = this.ternary();
         this.space();
         this.expect(')');
         return var3;
      }

      int var1 = this.at;
      if (this.at >= this.text.length() || !Character.isDigit(this.text.charAt(this.at)) && this.text.charAt(this.at) != '.') {
         if (this.at < this.text.length() && Macros.isIdentifierStart(this.text.charAt(this.at))) {
            while (this.at < this.text.length() && Macros.isIdentifierPart(this.text.charAt(this.at))) {
               this.at++;
            }

            String var2 = this.text.substring(var1, this.at);
            return var2.equals("true") ? 1.0 : 0.0;
         } else {
            throw new IllegalArgumentException("cannot read the condition '" + this.text.strip() + "'");
         }
      } else {
         while (
            this.at < this.text.length()
               && (
                  Macros.isIdentifierPart(this.text.charAt(this.at))
                     || this.text.charAt(this.at) == '.'
                     || (this.text.charAt(this.at) == '+' || this.text.charAt(this.at) == '-')
                        && (this.text.charAt(this.at - 1) == 'e' || this.text.charAt(this.at - 1) == 'E')
                        && !this.text.substring(var1, this.at).startsWith("0x")
               )
         ) {
            this.at++;
         }

         return number(this.text.substring(var1, this.at));
      }
   }

   private static double number(String var0) {
      String var1 = var0.toLowerCase(Locale.ROOT);
      if (var1.startsWith("0x")) {
         return Long.parseLong(var1.substring(2).replaceAll("[ul]+$", ""), 16);
      }

      var1 = var1.replaceAll("(lf|[ulf])+$", "");
      return Double.parseDouble(var1);
   }

   private boolean peek(char var1) {
      return this.at < this.text.length() && this.text.charAt(this.at) == var1;
   }

   private void expect(char var1) {
      if (!this.peek(var1)) {
         throw new IllegalArgumentException("expected '" + var1 + "' in the condition '" + this.text.strip() + "'");
      }

      this.at++;
   }

   private void space() {
      while (this.at < this.text.length() && Character.isWhitespace(this.text.charAt(this.at))) {
         this.at++;
      }
   }
}
