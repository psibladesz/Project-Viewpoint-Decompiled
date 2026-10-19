package viewpoint.iris;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class Macros {
   private final Map<String, Macros.Macro> table = new HashMap<>();

   void define(String var1, List<String> var2, String var3) {
      this.table.put(var1, new Macros.Macro(var1, var2, var3.strip()));
   }

   void define(String var1, String var2) {
      this.define(var1, null, var2);
   }

   void undefine(String var1) {
      this.table.remove(var1);
   }

   boolean defined(String var1) {
      return this.table.containsKey(var1);
   }

   Macros.Macro get(String var1) {
      return this.table.get(var1);
   }

   Map<String, String> objectLike() {
      HashMap var1 = new HashMap();

      for (Macros.Macro var3 : this.table.values()) {
         if (var3.params == null) {
            var1.put(var3.name, var3.body);
         }
      }

      return var1;
   }

   void defineFrom(String var1) {
      int var2 = 0;
      int var3 = var1.length();

      while (var2 < var3 && Character.isWhitespace(var1.charAt(var2))) {
         var2++;
      }

      int var4 = var2;

      while (var2 < var3 && isIdentifierPart(var1.charAt(var2))) {
         var2++;
      }

      String var5 = var1.substring(var4, var2);
      if (!var5.isEmpty()) {
         if (var2 < var3 && var1.charAt(var2) == '(') {
            int var6 = var1.indexOf(41, var2);
            if (var6 >= 0) {
               ArrayList var7 = new ArrayList();

               for (String var11 : var1.substring(var2 + 1, var6).split(",")) {
                  if (!var11.isBlank()) {
                     var7.add(var11.strip());
                  }
               }

               this.define(var5, var7, stripComments(var1.substring(var6 + 1)));
            }
         } else {
            this.define(var5, null, stripComments(var1.substring(var2)));
         }
      }
   }

   String expand(String var1) {
      return this.expand(var1, new HashSet<>());
   }

   private String expand(String var1, Set<String> var2) {
      StringBuilder var3 = new StringBuilder(var1.length() + 16);
      int var4 = var1.length();
      int var5 = 0;

      while (var5 < var4) {
         char var6 = var1.charAt(var5);
         if (var6 == '/' && var5 + 1 < var4 && (var1.charAt(var5 + 1) == '/' || var1.charAt(var5 + 1) == '*')) {
            int var9 = commentEnd(var1, var5);
            var3.append(var1, var5, var9);
            var5 = var9;
         } else if (isIdentifierStart(var6)) {
            int var8 = var5;

            while (var5 < var4 && isIdentifierPart(var1.charAt(var5))) {
               var5++;
            }

            var5 = this.identifier(var1, var8, var5, var2, var3);
         } else if (Character.isDigit(var6) || var6 == '.' && var5 + 1 < var4 && Character.isDigit(var1.charAt(var5 + 1))) {
            int var7 = var5++;

            while (
               var5 < var4
                  && (
                     isIdentifierPart(var1.charAt(var5))
                        || var1.charAt(var5) == '.'
                        || (var1.charAt(var5) == '+' || var1.charAt(var5) == '-') && (var1.charAt(var5 - 1) == 'e' || var1.charAt(var5 - 1) == 'E')
                  )
            ) {
               var5++;
            }

            var3.append(var1, var7, var5);
         } else {
            var3.append(var6);
            var5++;
         }
      }

      return var3.toString();
   }

   private int identifier(String var1, int var2, int var3, Set<String> var4, StringBuilder var5) {
      String var6 = var1.substring(var2, var3);
      Macros.Macro var7 = this.table.get(var6);
      if (var7 == null || var4.contains(var6)) {
         var5.append(var6);
         return var3;
      }

      if (var7.params == null) {
         var4.add(var6);
         var5.append(this.expand(var7.body, var4));
         var4.remove(var6);
         return var3;
      }

      int var8 = skipSpace(var1, var3);
      if (var8 < var1.length() && var1.charAt(var8) == '(') {
         ArrayList var9 = new ArrayList();
         int var10 = arguments(var1, var8, var9);
         if (var10 < 0) {
            var5.append(var6);
            return var3;
         }

         String var11 = this.substitute(var7, var9, var4);
         var4.add(var6);
         var5.append(this.expand(var11, var4));
         var4.remove(var6);
         int var12 = count(var1, var8, var10, '\n');

         for (int var13 = 0; var13 < var12; var13++) {
            var5.append('\n');
         }

         return var10 + 1;
      } else {
         var5.append(var6);
         return var3;
      }
   }

   private static int arguments(String var0, int var1, List<String> var2) {
      int var3 = 0;
      int var4 = var1 + 1;

      for (int var5 = var1; var5 < var0.length(); var5++) {
         char var6 = var0.charAt(var5);
         if (var6 != '/' || var5 + 1 >= var0.length() || var0.charAt(var5 + 1) != '/' && var0.charAt(var5 + 1) != '*') {
            if (var6 == '(') {
               var3++;
            } else if (var6 == ')') {
               if (--var3 == 0) {
                  String var7 = var0.substring(var4, var5);
                  if (!var2.isEmpty() || !var7.isBlank()) {
                     var2.add(var7);
                  }

                  return var5;
               }
            } else if (var6 == ',' && var3 == 1) {
               var2.add(var0.substring(var4, var5));
               var4 = var5 + 1;
            }
         } else {
            var5 = commentEnd(var0, var5) - 1;
         }
      }

      return -1;
   }

   private String substitute(Macros.Macro var1, List<String> var2, Set<String> var3) {
      String var4 = var1.body;
      StringBuilder var5 = new StringBuilder(var4.length() + 16);
      int var6 = var4.length();
      int var7 = 0;
      boolean var8 = false;

      while (var7 < var6) {
         char var9 = var4.charAt(var7);
         if (var9 == '#' && var7 + 1 < var6 && var4.charAt(var7 + 1) == '#') {
            trimEnd(var5);
            var7 = skipSpace(var4, var7 + 2);
            var8 = true;
         } else {
            if (var9 == '#') {
               int var10 = skipSpace(var4, var7 + 1);
               int var11 = var10;

               while (var11 < var6 && isIdentifierPart(var4.charAt(var11))) {
                  var11++;
               }

               int var12 = var1.params.indexOf(var4.substring(var10, var11));
               if (var12 >= 0) {
                  var5.append('"').append(var12 < var2.size() ? ((String)var2.get(var12)).strip() : "").append('"');
                  var7 = var11;
                  continue;
               }
            }

            if (!isIdentifierStart(var9)) {
               var8 = false;
               var5.append(var9);
               var7++;
            } else {
               int var15 = var7;

               while (var15 < var6 && isIdentifierPart(var4.charAt(var15))) {
                  var15++;
               }

               String var16 = var4.substring(var7, var15);
               int var17 = var1.params.indexOf(var16);
               boolean var13 = var8
                  || skipSpace(var4, var15) + 1 < var6 && var4.charAt(skipSpace(var4, var15)) == '#' && var4.charAt(skipSpace(var4, var15) + 1) == '#';
               if (var17 >= 0) {
                  String var14 = var17 < var2.size() ? ((String)var2.get(var17)).strip() : "";
                  var5.append(var13 ? var14 : this.expand(var14, var3));
               } else {
                  var5.append(var16);
               }

               var8 = false;
               var7 = var15;
            }
         }
      }

      return var5.toString();
   }

   private static void trimEnd(StringBuilder var0) {
      while (var0.length() > 0 && Character.isWhitespace(var0.charAt(var0.length() - 1))) {
         var0.setLength(var0.length() - 1);
      }
   }

   static int commentEnd(String var0, int var1) {
      if (var0.charAt(var1 + 1) == '/') {
         int var3 = var0.indexOf(10, var1);
         return var3 < 0 ? var0.length() : var3;
      } else {
         int var2 = var0.indexOf("*/", var1 + 2);
         return var2 < 0 ? var0.length() : var2 + 2;
      }
   }

   static String stripComments(String var0) {
      StringBuilder var1 = new StringBuilder(var0.length());
      int var2 = 0;
      int var3 = var0.length();

      while (var2 < var3) {
         char var4 = var0.charAt(var2);
         if (var4 == '/' && var2 + 1 < var3 && (var0.charAt(var2 + 1) == '/' || var0.charAt(var2 + 1) == '*')) {
            int var5 = commentEnd(var0, var2);
            if (var0.charAt(var2 + 1) == '*') {
               var1.append(' ');
            }

            var2 = var5;
         } else {
            var1.append(var4);
            var2++;
         }
      }

      return var1.toString();
   }

   private static int skipSpace(String var0, int var1) {
      while (var1 < var0.length() && Character.isWhitespace(var0.charAt(var1))) {
         var1++;
      }

      return var1;
   }

   private static int count(String var0, int var1, int var2, char var3) {
      int var4 = 0;

      for (int var5 = var1; var5 < var2; var5++) {
         if (var0.charAt(var5) == var3) {
            var4++;
         }
      }

      return var4;
   }

   static boolean isIdentifierStart(char var0) {
      return Character.isLetter(var0) || var0 == '_';
   }

   static boolean isIdentifierPart(char var0) {
      return Character.isLetterOrDigit(var0) || var0 == '_';
   }

   record Macro(String name, List<String> params, String body) {
   }
}
