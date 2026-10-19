package viewpoint.iris;

import java.util.List;

final class GlobalInitializers {
   private GlobalInitializers() {
   }

   static void defer(List<GlslTokens.Token> var0) {
      StringBuilder var1 = new StringBuilder();

      for (GlslTokens.Token var3 : var0) {
         var1.append(var3.text);
      }

      List var8 = GlslTokens.lex(var1.toString());
      var0.clear();
      var0.addAll(var8);
      GlslDeclarations var9 = GlslDeclarations.read(var0);
      GlslDeclarations.Function var4 = null;

      for (GlslDeclarations.Function var6 : var9.functions) {
         var4 = var6.name.equals("vp_packMain") ? var6 : var4;
      }

      if (var4 != null) {
         StringBuilder var10 = new StringBuilder();

         for (GlslDeclarations.Declaration var7 : var9.declarations) {
            if (var7.first < var4.open && !var9.insideFunction(var7.first) && !constant(var0, var7)) {
               var10.append(initializers(var0, var7));
            }
         }

         if (!var10.isEmpty()) {
            GlslTokens.Token var12 = (GlslTokens.Token)var0.get(var4.open);
            var12.text = var12.text + var10;
         }
      }
   }

   private static boolean constant(List<GlslTokens.Token> var0, GlslDeclarations.Declaration var1) {
      for (String var3 : var1.qualifiers) {
         if (GlslDeclarations.STORAGE.contains(var3)) {
            return true;
         }
      }

      for (int var4 = var1.first; var4 <= var1.last; var4++) {
         int var5 = GlslTokens.next(var0, var4 + 1);
         if (((GlslTokens.Token)var0.get(var4)).is("[") && var5 <= var1.last && ((GlslTokens.Token)var0.get(var5)).is("]")) {
            return true;
         }
      }

      return false;
   }

   private static String initializers(List<GlslTokens.Token> var0, GlslDeclarations.Declaration var1) {
      StringBuilder var2 = new StringBuilder();
      byte var3 = 0;
      String var4 = null;
      int var5 = -1;

      for (int var6 = var1.first; var6 <= var1.last; var6++) {
         GlslTokens.Token var7 = (GlslTokens.Token)var0.get(var6);
         boolean var8 = var3 == 0;
         var3 += var7.is("(") || var7.is("[") || var7.is("{") ? 1 : (!var7.is(")") && !var7.is("]") && !var7.is("}") ? 0 : -1);
         if (var8 && var7.isName() && var1.names.contains(var7.text) && var5 < 0) {
            var4 = var7.text;
         } else if (var8 && var7.is("=") && var5 < 0) {
            var5 = var6;
         } else if (var8 && (var7.is(",") || var7.is(";")) && var5 >= 0) {
            StringBuilder var9 = new StringBuilder();

            for (int var10 = var5 + 1; var10 < var6; var10++) {
               var9.append(((GlslTokens.Token)var0.get(var10)).text);
               ((GlslTokens.Token)var0.get(var10)).text = "";
            }

            ((GlslTokens.Token)var0.get(var5)).text = "";
            var2.append(' ').append(var4).append(" =").append(var9).append(';');
            var5 = -1;
         }
      }

      return var2.toString();
   }
}
