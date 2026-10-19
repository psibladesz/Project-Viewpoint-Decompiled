package viewpoint.iris;

import java.util.ArrayList;
import java.util.List;

final class GlslTokens {
   private static final String[] OPERATORS = new String[]{
      "<<=", ">>=", "++", "--", "+=", "-=", "*=", "/=", "%=", "&=", "^=", "|=", "&&", "||", "^^", "==", "!=", "<=", ">=", "<<", ">>"
   };

   static List<GlslTokens.Token> lex(String var0) {
      ArrayList var1 = new ArrayList(var0.length() / 3);
      int var2 = var0.length();
      int var3 = 0;
      boolean var4 = true;

      while (var3 < var2) {
         char var5 = var0.charAt(var3);
         int var6 = var3;
         if (var5 == '#' && var4) {
            int var8 = var0.indexOf(10, var3);
            var8 = var8 < 0 ? var2 : var8;
            var1.add(new GlslTokens.Token(GlslTokens.Kind.DIRECTIVE, var0.substring(var3, var8)));
            var3 = var8;
         } else if (Character.isWhitespace(var5)) {
            for (; var3 < var2 && Character.isWhitespace(var0.charAt(var3)); var3++) {
               if (var0.charAt(var3) == '\n') {
                  var4 = true;
               }
            }

            var1.add(new GlslTokens.Token(GlslTokens.Kind.SPACE, var0.substring(var6, var3)));
         } else {
            var4 = false;
            if (var5 != '/' || var3 + 1 >= var2 || var0.charAt(var3 + 1) != '/' && var0.charAt(var3 + 1) != '*') {
               if (Macros.isIdentifierStart(var5)) {
                  while (var3 < var2 && Macros.isIdentifierPart(var0.charAt(var3))) {
                     var3++;
                  }

                  var1.add(new GlslTokens.Token(GlslTokens.Kind.NAME, var0.substring(var6, var3)));
               } else if (Character.isDigit(var5) || var5 == '.' && var3 + 1 < var2 && Character.isDigit(var0.charAt(var3 + 1))) {
                  var3++;

                  while (
                     var3 < var2
                        && (
                           Macros.isIdentifierPart(var0.charAt(var3))
                              || var0.charAt(var3) == '.'
                              || (var0.charAt(var3) == '+' || var0.charAt(var3) == '-')
                                 && (var0.charAt(var3 - 1) == 'e' || var0.charAt(var3 - 1) == 'E')
                                 && !var0.startsWith("0x", var6)
                        )
                  ) {
                     var3++;
                  }

                  var1.add(new GlslTokens.Token(GlslTokens.Kind.NUMBER, var0.substring(var6, var3)));
               } else {
                  String var7 = operator(var0, var3);
                  var3 += var7.length();
                  var1.add(new GlslTokens.Token(GlslTokens.Kind.PUNCT, var7));
               }
            } else {
               var3 = Macros.commentEnd(var0, var3);
               var1.add(new GlslTokens.Token(GlslTokens.Kind.COMMENT, var0.substring(var6, var3)));
            }
         }
      }

      return var1;
   }

   private static String operator(String var0, int var1) {
      for (String var5 : OPERATORS) {
         if (var0.startsWith(var5, var1)) {
            return var5;
         }
      }

      return String.valueOf(var0.charAt(var1));
   }

   static String join(List<GlslTokens.Token> var0) {
      StringBuilder var1 = new StringBuilder(var0.size() * 4);

      for (GlslTokens.Token var3 : var0) {
         var1.append(var3.text);
      }

      return var1.toString();
   }

   static int next(List<GlslTokens.Token> var0, int var1) {
      while (var1 < var0.size() && !((GlslTokens.Token)var0.get(var1)).significant()) {
         var1++;
      }

      return var1;
   }

   static int previous(List<GlslTokens.Token> var0, int var1) {
      var1--;

      while (var1 >= 0 && !((GlslTokens.Token)var0.get(var1)).significant()) {
         var1--;
      }

      return var1;
   }

   static int close(List<GlslTokens.Token> var0, int var1) {
      String var2 = ((GlslTokens.Token)var0.get(var1)).text;
      String var3 = var2.equals("(") ? ")" : (var2.equals("[") ? "]" : "}");
      int var4 = 0;

      for (int var5 = var1; var5 < var0.size(); var5++) {
         GlslTokens.Token var6 = (GlslTokens.Token)var0.get(var5);
         if (var6.kind == GlslTokens.Kind.PUNCT) {
            if (var6.is(var2)) {
               var4++;
            } else if (var6.is(var3)) {
               if (--var4 == 0) {
                  return var5;
               }
            }
         }
      }

      return -1;
   }

   private GlslTokens() {
   }

   enum Kind {
      NAME,
      NUMBER,
      PUNCT,
      SPACE,
      COMMENT,
      DIRECTIVE;
   }

   static final class Token {
      final GlslTokens.Kind kind;
      String text;

      Token(GlslTokens.Kind var1, String var2) {
         this.kind = var1;
         this.text = var2;
      }

      boolean is(String var1) {
         return this.text.equals(var1);
      }

      boolean isName() {
         return this.kind == GlslTokens.Kind.NAME;
      }

      boolean significant() {
         return this.kind != GlslTokens.Kind.SPACE && this.kind != GlslTokens.Kind.COMMENT && this.kind != GlslTokens.Kind.DIRECTIVE;
      }

      @Override
      public String toString() {
         return this.text;
      }
   }
}
