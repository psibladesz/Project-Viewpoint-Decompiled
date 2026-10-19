package viewpoint.iris;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

final class AlbedoCalls {
   private static final Map<String, String> REPLACEMENTS = new HashMap<>();
   static final String ATLAS = "vp_albedoTex";
   private final List<GlslTokens.Token> tokens;
   private final Set<String> samplers;
   final List<String> notes = new ArrayList<>();
   int rewritten;

   private AlbedoCalls(List<GlslTokens.Token> var1, Set<String> var2) {
      this.tokens = var1;
      this.samplers = var2;
   }

   static AlbedoCalls rewrite(List<GlslTokens.Token> var0, Set<String> var1) {
      AlbedoCalls var2 = new AlbedoCalls(var0, var1);
      if (!var1.isEmpty()) {
         var2.helpers();
         var2.calls(0, var0.size(), var1);
         var2.leftovers();
      }

      return var2;
   }

   private void calls(int var1, int var2, Set<String> var3) {
      for (int var4 = var1; var4 < var2 && var4 < this.tokens.size(); var4++) {
         GlslTokens.Token var5 = this.tokens.get(var4);
         if (var5.isName()) {
            int var6 = GlslTokens.next(this.tokens, var4 + 1);
            if (var6 < this.tokens.size() && this.tokens.get(var6).is("(")) {
               int var7 = GlslTokens.close(this.tokens, var6);
               List<int[]> var8 = this.arguments(var6, var7);
               if (!var8.isEmpty() && var3.contains(this.single((int[])var8.get(0)))) {
                  String var9 = REPLACEMENTS.get(var5.text + "/" + var8.size());
                  if (var9 == null) {
                     this.tokens.get(((int[])var8.get(0))[0]).text = "vp_albedoTex";
                  } else {
                     var5.text = var9;
                     int var10 = GlslTokens.next(this.tokens, ((int[])var8.get(0))[1] + 1);

                     for (int var11 = ((int[])var8.get(0))[0]; var11 <= var10; var11++) {
                        this.tokens.get(var11).text = "";
                     }

                     this.rewritten++;
                  }
               }
            }
         }
      }
   }

   private List<int[]> arguments(int var1, int var2) {
      ArrayList<int[]> var3 = new ArrayList<>();
      if (var2 < 0) {
         return var3;
      }

      int var4 = -1;
      int var5 = -1;
      int var6 = 0;

      for (int var7 = var1 + 1; var7 < var2; var7++) {
         GlslTokens.Token var8 = this.tokens.get(var7);
         if (var8.significant()) {
            if (var8.is("(") || var8.is("[")) {
               var6++;
            } else if (var8.is(")") || var8.is("]")) {
               var6--;
            }

            if (var8.is(",") && var6 == 0) {
               var3.add(new int[]{var4, var5});
               var4 = -1;
            } else {
               if (var4 < 0) {
                  var4 = var7;
               }

               var5 = var7;
            }
         }
      }

      if (var4 >= 0) {
         var3.add(new int[]{var4, var5});
      }

      return var3;
   }

   private String single(int[] var1) {
      return var1[0] == var1[1] && var1[0] >= 0 && this.tokens.get(var1[0]).isName() ? this.tokens.get(var1[0]).text : "";
   }

   private void helpers() {
      GlslDeclarations var1 = GlslDeclarations.read(this.tokens);
      HashMap<String, Set<Integer>> var2 = new HashMap<>();

      for (int var3 = 0; var3 < this.tokens.size(); var3++) {
         GlslTokens.Token var4 = this.tokens.get(var3);
         if (var4.isName() && !var1.functions(var4.text).isEmpty()) {
            int var5 = GlslTokens.next(this.tokens, var3 + 1);
            if (var5 < this.tokens.size() && this.tokens.get(var5).is("(") && !isDefinition(var1, var3)) {
               List<int[]> var6 = this.arguments(var5, GlslTokens.close(this.tokens, var5));

               for (int var7 = 0; var7 < var6.size(); var7++) {
                  if (this.samplers.contains(this.single((int[])var6.get(var7)))) {
                     var2.computeIfAbsent(var4.text, var0 -> new HashSet<>()).add(var7);
                     var4.text = var4.text + "_vp" + var7;
                     break;
                  }
               }
            }
         }
      }

      TreeMap<Integer, List<GlslTokens.Token>> var10 = new TreeMap<>(Comparator.reverseOrder());

      for (Entry<String, Set<Integer>> var13 : var2.entrySet()) {
         for (GlslDeclarations.Function var16 : var1.functions((String)var13.getKey())) {
            for (int var9 : var13.getValue()) {
               if (var9 < var16.params.size()) {
                  var10.computeIfAbsent(var16.close + 1, var0 -> new ArrayList<>()).addAll(this.clone(var16, var9));
               }
            }
         }
      }

      for (Entry<Integer, List<GlslTokens.Token>> var14 : var10.entrySet()) {
         this.tokens.add((Integer)var14.getKey(), new GlslTokens.Token(GlslTokens.Kind.SPACE, "\n"));
         this.tokens.addAll((Integer)var14.getKey() + 1, (Collection<? extends GlslTokens.Token>)var14.getValue());
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

   private List<GlslTokens.Token> clone(GlslDeclarations.Function var1, int var2) {
      int var3;
      for (var3 = var1.nameToken; var3 > 0; var3--) {
         GlslTokens.Token var4 = this.tokens.get(var3 - 1);
         if (var4.is(";") || var4.is("}") || var4.kind == GlslTokens.Kind.DIRECTIVE) {
            break;
         }
      }

      ArrayList<GlslTokens.Token> var7 = new ArrayList<>();

      for (int var5 = var3; var5 <= var1.close; var5++) {
         GlslTokens.Token var6 = this.tokens.get(var5);
         var7.add(new GlslTokens.Token(var6.kind, var5 == var1.nameToken ? var1.name + "_vp" + var2 : var6.text));
      }

      var7.add(new GlslTokens.Token(GlslTokens.Kind.SPACE, "\n"));
      AlbedoCalls var8 = new AlbedoCalls(var7, Set.of(var1.params.get(var2)));
      var8.calls(0, var7.size(), Set.of(var1.params.get(var2)));
      this.rewritten = this.rewritten + var8.rewritten;
      return var7;
   }

   private void leftovers() {
      GlslDeclarations var1 = GlslDeclarations.read(this.tokens);

      for (int var2 = 0; var2 < this.tokens.size(); var2++) {
         GlslTokens.Token var3 = this.tokens.get(var2);
         if (var3.isName() && this.samplers.contains(var3.text) && var1.insideFunction(var2)) {
            int var4 = GlslTokens.previous(this.tokens, var2);
            if (var4 < 0 || !this.tokens.get(var4).is(".")) {
               var3.text = "vp_albedoTex";
               this.notes.add("the albedo sampler is read otherwise than by a texture call: it samples the atlas page");
            }
         }
      }
   }

   static {
      for (String var3 : new String[]{"texture2D", "texture"}) {
         REPLACEMENTS.put(var3 + "/2", "vp_albedo");
         REPLACEMENTS.put(var3 + "/3", "vp_albedoBias");
      }

      for (String var10 : new String[]{"texture2DLod", "textureLod"}) {
         REPLACEMENTS.put(var10 + "/3", "vp_albedoLod");
      }

      for (String var11 : new String[]{"textureGrad", "texture2DGrad", "texture2DGradARB"}) {
         REPLACEMENTS.put(var11 + "/4", "vp_albedoGrad");
      }

      REPLACEMENTS.put("texelFetch/3", "vp_albedoFetch");
   }
}