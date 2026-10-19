package viewpoint.iris;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class GlslDeclarations {
   static final Set<String> STORAGE = Set.of("in", "out", "varying", "attribute", "uniform", "const", "buffer", "shared");
   static final Set<String> INTERPOLATION = Set.of("flat", "smooth", "noperspective", "centroid", "sample", "invariant");
   private static final Set<String> OTHER_QUALIFIERS = Set.of(
      "highp", "mediump", "lowp", "precise", "coherent", "volatile", "restrict", "readonly", "writeonly"
   );
   final List<GlslDeclarations.Declaration> declarations = new ArrayList<>();
   final List<GlslDeclarations.Function> functions = new ArrayList<>();

   static GlslDeclarations read(List<GlslTokens.Token> var0) {
      GlslDeclarations var1 = new GlslDeclarations();
      int var2 = GlslTokens.next(var0, 0);

      while (var2 < var0.size()) {
         var2 = var1.statement(var0, var2);
         var2 = GlslTokens.next(var0, var2);
      }

      return var1;
   }

   private int statement(List<GlslTokens.Token> var1, int var2) {
      int var3 = var2;

      for (int var4 = var2; var4 < var1.size(); var4++) {
         GlslTokens.Token var5 = (GlslTokens.Token)var1.get(var4);
         if (var5.kind == GlslTokens.Kind.PUNCT) {
            if (var5.is(";")) {
               this.declaration(var1, var3, var4);
               return var4 + 1;
            }

            if (var5.is("{")) {
               int var9 = GlslTokens.close(var1, var4);
               if (var9 < 0) {
                  return var1.size();
               }

               this.function(var1, var3, var4, var9);
               int var7 = GlslTokens.next(var1, var9 + 1);
               if (var7 < var1.size() && !isFunctionHead(var1, var3, var4)) {
                  for (int var8 = var7; var8 < var1.size(); var8++) {
                     if (((GlslTokens.Token)var1.get(var8)).is(";")) {
                        return var8 + 1;
                     }
                  }
               }

               return var9 + 1;
            }

            if (var5.is("(") || var5.is("[")) {
               int var6 = GlslTokens.close(var1, var4);
               var4 = var6 < 0 ? var1.size() : var6;
            }
         }
      }

      return var1.size();
   }

   private static boolean isFunctionHead(List<GlslTokens.Token> var0, int var1, int var2) {
      int var3 = GlslTokens.previous(var0, var2);
      return var3 >= var1 && ((GlslTokens.Token)var0.get(var3)).is(")");
   }

   private void function(List<GlslTokens.Token> var1, int var2, int var3, int var4) {
      int var5 = GlslTokens.previous(var1, var3);
      if (var5 >= var2 && ((GlslTokens.Token)var1.get(var5)).is(")")) {
         int var6 = -1;
         int var7 = 0;

         for (int var8 = var5; var8 >= var2; var8--) {
            GlslTokens.Token var9 = (GlslTokens.Token)var1.get(var8);
            if (var9.is(")")) {
               var7++;
            } else if (var9.is("(")) {
               if (--var7 == 0) {
                  var6 = var8;
                  break;
               }
            }
         }

         int var10 = var6 < 0 ? -1 : GlslTokens.previous(var1, var6);
         if (var10 >= var2 && ((GlslTokens.Token)var1.get(var10)).isName()) {
            this.functions.add(new GlslDeclarations.Function(((GlslTokens.Token)var1.get(var10)).text, var10, var3, var4, parameters(var1, var6, var5)));
         }
      }
   }

   private static List<String> parameters(List<GlslTokens.Token> var0, int var1, int var2) {
      ArrayList var3 = new ArrayList();
      String var4 = null;
      int var5 = 0;

      for (int var6 = var1 + 1; var6 < var2; var6++) {
         GlslTokens.Token var7 = (GlslTokens.Token)var0.get(var6);
         if (var7.is("(") || var7.is("[")) {
            var5++;
         } else if (var7.is(")") || var7.is("]")) {
            var5--;
         } else if (var7.is(",") && var5 == 0) {
            var3.add(var4);
            var4 = null;
         } else if (var7.isName() && var5 == 0) {
            var4 = var7.text;
         }
      }

      if (var4 != null && !var4.equals("void")) {
         var3.add(var4);
      }

      return var3;
   }

   private void declaration(List<GlslTokens.Token> var1, int var2, int var3) {
      GlslDeclarations.Declaration var4 = new GlslDeclarations.Declaration(var2, var3);
      int var5 = var2;

      while (var5 < var3) {
         GlslTokens.Token var6 = (GlslTokens.Token)var1.get(var5);
         if (!var6.significant()) {
            var5++;
         } else if (!var6.is("layout")) {
            if (!STORAGE.contains(var6.text) && !INTERPOLATION.contains(var6.text) && !OTHER_QUALIFIERS.contains(var6.text)) {
               break;
            }

            var4.qualifiers.add(var6.text);
            var5++;
         } else {
            int var7 = GlslTokens.next(var1, var5 + 1);
            int var8 = var7 < var3 && ((GlslTokens.Token)var1.get(var7)).is("(") ? GlslTokens.close(var1, var7) : -1;
            var5 = var8 < 0 ? var5 + 1 : var8 + 1;
            var4.qualifiers.add("layout");
         }
      }

      if (var5 < var3
         && ((GlslTokens.Token)var1.get(var5)).isName()
         && !((GlslTokens.Token)var1.get(var5)).is("struct")
         && !((GlslTokens.Token)var1.get(var5)).is("precision")) {
         var4.type = ((GlslTokens.Token)var1.get(var5)).text;
         names(var1, GlslTokens.next(var1, var5 + 1), var3, var4);
         if (!var4.names.isEmpty()) {
            this.declarations.add(var4);
         }
      }
   }

   private static void names(List<GlslTokens.Token> var0, int var1, int var2, GlslDeclarations.Declaration var3) {
      if (var1 < var2 && ((GlslTokens.Token)var0.get(var1)).is("[")) {
         int var4 = GlslTokens.close(var0, var1);
         var1 = GlslTokens.next(var0, var4 + 1);
      }

      while (var1 < var2) {
         if (!((GlslTokens.Token)var0.get(var1)).isName()) {
            return;
         }

         String var8 = ((GlslTokens.Token)var0.get(var1)).text;
         String var5 = "";
         var1 = GlslTokens.next(var0, var1 + 1);
         if (var1 < var2 && ((GlslTokens.Token)var0.get(var1)).is("[")) {
            int var6 = GlslTokens.close(var0, var1);
            var5 = GlslTokens.join(var0.subList(var1, var6 + 1));
            var1 = GlslTokens.next(var0, var6 + 1);
         }

         var3.names.add(var8);
         var3.arrays.add(var5);
         if (var1 < var2 && ((GlslTokens.Token)var0.get(var1)).is("=")) {
            var1 = skipInitializer(var0, var1 + 1, var2);
         }

         if (var1 >= var2 || !((GlslTokens.Token)var0.get(var1)).is(",")) {
            return;
         }

         var1 = GlslTokens.next(var0, var1 + 1);
      }
   }

   private static int skipInitializer(List<GlslTokens.Token> var0, int var1, int var2) {
      while (var1 < var2) {
         GlslTokens.Token var3 = (GlslTokens.Token)var0.get(var1);
         if (!var3.is("(") && !var3.is("[") && !var3.is("{")) {
            if (var3.is(",")) {
               return var1;
            }

            var1++;
         } else {
            var1 = GlslTokens.close(var0, var1) + 1;
         }
      }

      return var2;
   }

   List<GlslDeclarations.Function> functions(String var1) {
      ArrayList var2 = new ArrayList();

      for (GlslDeclarations.Function var4 : this.functions) {
         if (var4.name.equals(var1)) {
            var2.add(var4);
         }
      }

      return var2;
   }

   GlslDeclarations.Declaration global(String var1, Set<String> var2) {
      for (GlslDeclarations.Declaration var4 : this.declarations) {
         if (var4.names.contains(var1)) {
            for (String var6 : var4.qualifiers) {
               if (var2.contains(var6)) {
                  return var4;
               }
            }
         }
      }

      return null;
   }

   boolean insideFunction(int var1) {
      for (GlslDeclarations.Function var3 : this.functions) {
         if (var1 > var3.open && var1 < var3.close) {
            return true;
         }
      }

      return false;
   }

   static final class Declaration {
      final int first;
      final int last;
      final List<String> qualifiers = new ArrayList<>();
      String type;
      final List<String> names = new ArrayList<>();
      final List<String> arrays = new ArrayList<>();

      Declaration(int var1, int var2) {
         this.first = var1;
         this.last = var2;
      }

      boolean has(String var1) {
         return this.qualifiers.contains(var1);
      }

      String interpolation() {
         StringBuilder var1 = new StringBuilder();

         for (String var3 : this.qualifiers) {
            if (GlslDeclarations.INTERPOLATION.contains(var3)) {
               var1.append(var3).append(' ');
            }
         }

         return var1.toString();
      }
   }

   static final class Function {
      final String name;
      final int nameToken;
      final int open;
      final int close;
      final List<String> params;

      Function(String var1, int var2, int var3, int var4, List<String> var5) {
         this.name = var1;
         this.nameToken = var2;
         this.open = var3;
         this.close = var4;
         this.params = var5;
      }
   }
}
