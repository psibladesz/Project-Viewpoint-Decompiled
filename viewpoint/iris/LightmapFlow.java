package viewpoint.iris;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class LightmapFlow {
   private LightmapFlow() {
   }

   static LightmapFlow.Found find(List<GlslTokens.Token> var0, GlslDeclarations var1, Set<String> var2) {
      return find(var0, var1, var2, false);
   }

   static LightmapFlow.Found findColour(List<GlslTokens.Token> var0, GlslDeclarations var1, Set<String> var2) {
      return find(var0, var1, var2, true);
   }

   private static LightmapFlow.Found find(List<GlslTokens.Token> var0, GlslDeclarations var1, Set<String> var2, boolean var3) {
      HashSet var4 = new HashSet(var2);
      HashSet var5 = new HashSet();
      boolean var6 = true;

      while (var6) {
         var6 = false;

         for (GlslDeclarations.Function var8 : var1.functions) {
            if (!var5.contains(var8.name) && reads(var0, var8.open, var8.close, var4, var5)) {
               var5.add(var8.name);
               var6 = true;
            }
         }

         var6 |= assignments(var0, var1, var4, var5);
      }

      LightmapFlow.Found var16 = null;
      int var17 = -1;

      for (GlslDeclarations.Declaration var10 : var1.declarations) {
         if (var10.has("out") || var10.has("varying")) {
            for (String var12 : var10.names) {
               String var13 = components(var0, var1, var12, var4, var5, var10.type, var3);
               if (var13 != null) {
                  int var14 = var3 ? colourScore(var12) : score(var12);
                  if (var14 > var17) {
                     var17 = var14;
                     var16 = new LightmapFlow.Found(var12, var10.type, var13, var10.interpolation());
                  }
               }
            }
         }
      }

      return var16;
   }

   private static String components(
      List<GlslTokens.Token> var0, GlslDeclarations var1, String var2, Set<String> var3, Set<String> var4, String var5, boolean var6
   ) {
      for (int var7 = 0; var7 < var0.size(); var7++) {
         GlslTokens.Token var8 = (GlslTokens.Token)var0.get(var7);
         if (var8.isName() && var8.is(var2) && var1.insideFunction(var7)) {
            int var9 = GlslTokens.next(var0, var7 + 1);
            String var10 = "";
            if (var9 < var0.size() && ((GlslTokens.Token)var0.get(var9)).is(".")) {
               int var11 = GlslTokens.next(var0, var9 + 1);
               var10 = ((GlslTokens.Token)var0.get(var11)).text;
               var9 = GlslTokens.next(var0, var11 + 1);
            }

            if (var9 < var0.size() && ((GlslTokens.Token)var0.get(var9)).is("=")) {
               int var13 = statementEnd(var0, var9);
               if (reads(var0, var9 + 1, var13, var3, var4)) {
                  if (!var6) {
                     if (var5.equals("vec2") && (var10.isEmpty() || var10.equals("xy"))) {
                        return "xy";
                     }

                     if (var5.equals("vec4") && var10.length() == 2) {
                        return var10;
                     }
                  } else {
                     boolean var12 = var10.isEmpty() || var10.equals("rgb") || var10.equals("xyz");
                     if (var12 && (var5.equals("vec3") || var5.equals("vec4"))) {
                        return "rgb";
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private static boolean assignments(List<GlslTokens.Token> var0, GlslDeclarations var1, Set<String> var2, Set<String> var3) {
      boolean var4 = false;

      for (int var5 = 0; var5 < var0.size(); var5++) {
         if (((GlslTokens.Token)var0.get(var5)).is("=") && var1.insideFunction(var5)) {
            int var6 = GlslTokens.previous(var0, var5);

            while (var6 >= 0 && !((GlslTokens.Token)var0.get(var6)).isName()) {
               var6 = GlslTokens.previous(var0, var6);
            }

            int var7 = var6;

            while (var7 >= 2 && ((GlslTokens.Token)var0.get(GlslTokens.previous(var0, var7))).is(".")) {
               var7 = GlslTokens.previous(var0, GlslTokens.previous(var0, var7));
            }

            if (var7 >= 0 && !var2.contains(((GlslTokens.Token)var0.get(var7)).text) && reads(var0, var5 + 1, statementEnd(var0, var5), var2, var3)) {
               var2.add(((GlslTokens.Token)var0.get(var7)).text);
               var4 = true;
            }
         }
      }

      return var4;
   }

   private static int statementEnd(List<GlslTokens.Token> var0, int var1) {
      int var2 = 0;

      for (int var3 = var1; var3 < var0.size(); var3++) {
         GlslTokens.Token var4 = (GlslTokens.Token)var0.get(var3);
         if (var4.is("(") || var4.is("[")) {
            var2++;
         } else if (!var4.is(")") && !var4.is("]")) {
            if (var4.is(";") || var2 < 0 || var4.is(",") && var2 == 0) {
               return var3;
            }
         } else {
            var2--;
         }
      }

      return var0.size();
   }

   private static boolean reads(List<GlslTokens.Token> var0, int var1, int var2, Set<String> var3, Set<String> var4) {
      for (int var5 = var1; var5 < var2 && var5 < var0.size(); var5++) {
         GlslTokens.Token var6 = (GlslTokens.Token)var0.get(var5);
         if (var6.isName()) {
            int var7 = GlslTokens.previous(var0, var5);
            if (var7 < 0 || !((GlslTokens.Token)var0.get(var7)).is(".")) {
               if (var3.contains(var6.text)) {
                  return true;
               }

               if (var4.contains(var6.text)) {
                  int var8 = GlslTokens.next(var0, var5 + 1);
                  if (var8 < var0.size() && ((GlslTokens.Token)var0.get(var8)).is("(")) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private static int colourScore(String var0) {
      String var1 = var0.toLowerCase(Locale.ROOT);
      return !var1.contains("glcolor") && !var1.equals("color") ? (var1.contains("col") ? 2 : (var1.contains("tint") ? 1 : 0)) : 3;
   }

   private static int score(String var0) {
      String var1 = var0.toLowerCase(Locale.ROOT);
      return !var1.startsWith("lm") && !var1.contains("lightmap") ? (var1.contains("light") ? 2 : (var1.contains("lm") ? 1 : 0)) : 3;
   }

   record Found(String name, String type, String components, String interpolation) {
   }
}
