package viewpoint.iris;

import java.util.List;
import java.util.Set;

final class BlockGrid {
   private static final Set<String> BEFORE = Set.of("=", "(", ",", "return");
   private static final Set<String> NOT_AFTER = Set.of(".", "[", "(");

   private BlockGrid() {
   }

   static int unround(List<GlslTokens.Token> var0) {
      int var1 = 0;

      for (int var2 = 0; var2 < var0.size(); var2++) {
         if (((GlslTokens.Token)var0.get(var2)).isName() && ((GlslTokens.Token)var0.get(var2)).is("floor") && unround(var0, var2)) {
            var1++;
         }
      }

      return var1;
   }

   private static boolean unround(List<GlslTokens.Token> var0, int var1) {
      int var2 = GlslTokens.previous(var0, var1);
      int var3 = GlslTokens.next(var0, var1 + 1);
      if (var2 >= 0 && BEFORE.contains(((GlslTokens.Token)var0.get(var2)).text) && is(var0, var3, "(")) {
         int var4 = GlslTokens.close(var0, var3);
         int var5 = GlslTokens.previous(var0, var4);
         int var6 = GlslTokens.previous(var0, var5);
         int var7 = GlslTokens.previous(var0, var6);
         if (var4 >= 0
            && var5 >= 0
            && isHalf((GlslTokens.Token)var0.get(var5))
            && is(var0, var6, "+")
            && var7 >= 0
            && ((GlslTokens.Token)var0.get(var7)).isName()
            && is(var0, GlslTokens.previous(var0, var7), "+")) {
            int var8 = GlslTokens.next(var0, var4 + 1);
            int var9 = GlslTokens.next(var0, var8 + 1);
            int var10 = GlslTokens.next(var0, var9 + 1);
            if (is(var0, var8, "-")
               && is(var0, var9, ((GlslTokens.Token)var0.get(var7)).text)
               && (var10 >= var0.size() || !NOT_AFTER.contains(((GlslTokens.Token)var0.get(var10)).text))) {
               ((GlslTokens.Token)var0.get(var1)).text = "";
               ((GlslTokens.Token)var0.get(var6)).text = "";
               ((GlslTokens.Token)var0.get(var5)).text = "";
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean is(List<GlslTokens.Token> var0, int var1, String var2) {
      return var1 >= 0 && var1 < var0.size() && ((GlslTokens.Token)var0.get(var1)).is(var2);
   }

   private static boolean isHalf(GlslTokens.Token var0) {
      if (var0.kind != GlslTokens.Kind.NUMBER) {
         return false;
      }

      String var1 = !var0.text.endsWith("f") && !var0.text.endsWith("F") ? var0.text : var0.text.substring(0, var0.text.length() - 1);

      try {
         return Double.parseDouble(var1) == 0.5;
      } catch (NumberFormatException var3) {
         return false;
      }
   }
}
