package viewpoint.iris;

import java.util.List;
import java.util.Set;

final class LampHooks {
   static final Set<String> NAMES = Set.of("blocklightCol", "blockLightCol", "blocklight_color");
   static final Set<String> FIRST_ARGUMENT = Set.of("doBlockLightLighting");

   private LampHooks() {
   }

   static int tint(List<GlslTokens.Token> var0) {
      GlslDeclarations var1 = GlslDeclarations.read(var0);
      int var2 = 0;

      for (int var3 = 0; var3 < var0.size(); var3++) {
         GlslTokens.Token var4 = (GlslTokens.Token)var0.get(var3);
         if (var4.isName() && var1.insideFunction(var3)) {
            if (NAMES.contains(var4.text) && isRead(var0, var3)) {
               var4.text = "vp_lampTint(" + var4.text + ")";
               var2++;
            } else if (FIRST_ARGUMENT.contains(var4.text) && !isDefinition(var1, var3)) {
               var2 += firstArgument(var0, var3);
            }
         }
      }

      return var2;
   }

   private static boolean isRead(List<GlslTokens.Token> var0, int var1) {
      int var2 = GlslTokens.previous(var0, var1);
      int var3 = GlslTokens.next(var0, var1 + 1);
      return var2 < 0 || !((GlslTokens.Token)var0.get(var2)).is(".") && !((GlslTokens.Token)var0.get(var2)).isName()
         ? var3 >= var0.size() || !((GlslTokens.Token)var0.get(var3)).is("=") && !((GlslTokens.Token)var0.get(var3)).is("(")
         : false;
   }

   private static int firstArgument(List<GlslTokens.Token> var0, int var1) {
      int var2 = GlslTokens.next(var0, var1 + 1);
      if (var2 < var0.size() && ((GlslTokens.Token)var0.get(var2)).is("(")) {
         int var3 = GlslTokens.close(var0, var2);
         int var4 = 0;

         for (int var5 = var2 + 1; var5 < var3; var5++) {
            GlslTokens.Token var6 = (GlslTokens.Token)var0.get(var5);
            if (var6.is("(") || var6.is("[")) {
               var4++;
            } else if (!var6.is(")") && !var6.is("]")) {
               if (var6.is(",") && var4 == 0) {
                  int var7 = GlslTokens.next(var0, var2 + 1);
                  ((GlslTokens.Token)var0.get(var7)).text = "vp_lampTint(" + ((GlslTokens.Token)var0.get(var7)).text;
                  var6.text = ")" + var6.text;
                  return 1;
               }
            } else {
               var4--;
            }
         }

         return 0;
      } else {
         return 0;
      }
   }

   private static boolean isDefinition(GlslDeclarations var0, int var1) {
      for (GlslDeclarations.Function var3 : var0.functions) {
         if (var3.nameToken == var1) {
            return true;
         }
      }

      return false;
   }
}
