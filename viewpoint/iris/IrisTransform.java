package viewpoint.iris;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public final class IrisTransform {
   private static final Map<String, String> VERTEX_BUILTINS = Map.ofEntries(
      Map.entry("gl_Vertex", "vp_Vertex"),
      Map.entry("gl_Normal", "vp_Normal"),
      Map.entry("gl_Color", "vp_Color"),
      Map.entry("gl_MultiTexCoord0", "vp_MultiTexCoord0"),
      Map.entry("gl_MultiTexCoord1", "vp_MultiTexCoord1"),
      Map.entry("gl_MultiTexCoord2", "vp_MultiTexCoordZero"),
      Map.entry("gl_MultiTexCoord3", "vp_MultiTexCoordZero")
   );
   static final Map<String, String> MATRICES = Map.ofEntries(
      Map.entry("gl_ModelViewMatrix", "vp_ModelViewMatrix"),
      Map.entry("gl_ProjectionMatrix", "vp_ProjectionMatrix"),
      Map.entry("gl_ModelViewProjectionMatrix", "vp_ModelViewProjectionMatrix"),
      Map.entry("gl_NormalMatrix", "vp_NormalMatrix"),
      Map.entry("gl_TextureMatrix", "vp_TextureMatrix"),
      Map.entry("gl_ModelViewMatrixInverse", "vp_ModelViewMatrixInverse"),
      Map.entry("gl_ProjectionMatrixInverse", "vp_ProjectionMatrixInverse"),
      Map.entry("gl_Fog", "vp_Fog")
   );
   public static final Set<String> INPUTS = Set.of(
      "mc_Entity",
      "mc_midTexCoord",
      "at_tangent",
      "at_midBlock",
      "at_velocity",
      "mc_chunkFade",
      "vaPosition",
      "vaColor",
      "vaUV0",
      "vaUV1",
      "vaUV2",
      "vaNormal",
      "chunkOffset",
      "dhMaterialId"
   );
   private static final Set<String> NEWER_BUILTINS = Set.of(
      "round",
      "roundEven",
      "trunc",
      "sinh",
      "cosh",
      "tanh",
      "asinh",
      "acosh",
      "atanh",
      "isnan",
      "isinf",
      "modf",
      "inverse",
      "determinant",
      "texture",
      "textureLod",
      "textureSize",
      "texelFetch",
      "textureGrad",
      "textureOffset",
      "textureProj",
      "floatBitsToInt",
      "floatBitsToUint",
      "intBitsToFloat",
      "uintBitsToFloat",
      "packUnorm2x16",
      "unpackUnorm2x16",
      "packHalf2x16",
      "unpackHalf2x16",
      "fma",
      "frexp",
      "ldexp",
      "bitfieldExtract",
      "bitfieldInsert",
      "bitfieldReverse",
      "bitCount",
      "findLSB",
      "findMSB",
      "textureGather",
      "textureQueryLod",
      "dFdxFine",
      "dFdyFine",
      "dFdxCoarse",
      "dFdyCoarse",
      "fwidthFine",
      "fwidthCoarse"
   );
   private static final Set<String> ALBEDO = Set.of("tex", "texture", "gtexture", "gcolor", "colortex0");

   private IrisTransform() {
   }

   public static IrisTransform.Stage vertex(Preprocessor.Result var0, IrisTransform.Kind var1) {
      return vertex(var0, var1, false);
   }

   public static IrisTransform.Stage vertex(Preprocessor.Result var0, IrisTransform.Kind var1, boolean var2) {
      IrisTransform.Stage var3 = start(var0, var1);
      List<GlslTokens.Token> var4 = GlslTokens.lex(var0.text());
      renameBuiltins(var4, VERTEX_BUILTINS);
      renameBuiltins(var4, MATRICES);
      ftransform(var4);
      modernise(var4, true);
      if (var1 == IrisTransform.Kind.GEOMETRY) {
         GlslDeclarations var5 = GlslDeclarations.read(var4);
         removeInputs(var4, var5, var3);

         for (Entry var7 : Map.of("dhMaterialId", "int", "mc_chunkFade", "float").entrySet()) {
            if (!var3.inputs.containsKey(var7.getKey()) && var4.stream().anyMatch(var1x -> var1x.is((String)var7.getKey()))) {
               var3.inputs.put((String)var7.getKey(), (String)var7.getValue());
            }
         }

         var5 = GlslDeclarations.read(var4);
         var3.lightmap = LightmapFlow.find(var4, var5, Set.of("vp_MultiTexCoord1", "vaUV2"));
         if (var3.lightmap != null) {
            carry(var4, var5, var3.lightmap, "out");
         }

         if (var2) {
            colour(var4, var3);
            BlockGrid.unround(var4);
         }
      }

      renameNewerBuiltins(var4, var3);
      var3.mainRenamed = renameMain(var4);
      finish(var4, var3);
      return var3;
   }

   public static IrisTransform.Stage fragment(Preprocessor.Result var0, IrisTransform.Kind var1, LightmapFlow.Found var2) {
      return fragment(var0, var1, var2, null);
   }

   public static IrisTransform.Stage fragment(Preprocessor.Result var0, IrisTransform.Kind var1, LightmapFlow.Found var2, LightmapFlow.Found var3) {
      IrisTransform.Stage var4 = start(var0, var1);
      List var5 = GlslTokens.lex(var0.text());
      renameBuiltins(var5, MATRICES);
      modernise(var5, false);
      if (var1 == IrisTransform.Kind.GEOMETRY) {
         albedo(var5, var4);
         GlslDeclarations var6 = GlslDeclarations.read(var5);
         if (var2 != null && var6.global(var2.name(), Set.of("in", "varying")) != null) {
            carry(var5, var6, var2, "in");
            var4.lightmap = var2;
         } else if (var2 != null) {
            var4.notes.add("the lightmap's variable " + var2.name() + " is not read by the fragment stage");
         }

         var6 = GlslDeclarations.read(var5);
         if (var3 != null && var6.global(var3.name(), Set.of("in", "varying")) != null) {
            carry(var5, var6, var3, "in", "vp_col_");
            var4.colour = var3;
         }
      }

      var4.lampTints = LampHooks.tint(var5);
      renameNewerBuiltins(var5, var4);
      var4.mainRenamed = var1 == IrisTransform.Kind.GEOMETRY && renameMain(var5);
      finish(var5, var4);
      return var4;
   }

   private static void colour(List<GlslTokens.Token> var0, IrisTransform.Stage var1) {
      GlslDeclarations var2 = GlslDeclarations.read(var0);
      LightmapFlow.Found var3 = LightmapFlow.findColour(var0, var2, Set.of("vp_Color", "vaColor"));
      if (var3 != null && (var1.lightmap == null || !var3.name().equals(var1.lightmap.name()))) {
         carry(var0, var2, var3, "out", "vp_col_");
         var1.colour = var3;
      }
   }

   public static IrisTransform.Stage plain(Preprocessor.Result var0, IrisTransform.Kind var1) {
      IrisTransform.Stage var2 = start(var0, var1);
      List var3 = GlslTokens.lex(var0.text());
      renameBuiltins(var3, MATRICES);
      finish(var3, var2);
      return var2;
   }

   static IrisTransform.Stage start(Preprocessor.Result var0, IrisTransform.Kind var1) {
      IrisTransform.Stage var2 = new IrisTransform.Stage();
      var2.version = version(var0.version(), var1);
      var2.raised = !var2.version.equals(var0.version() == null ? "120" : var0.version().strip());
      var2.extensions.addAll(var0.extensions());
      return var2;
   }

   static void modernise(List<GlslTokens.Token> var0, boolean var1) {
      for (GlslTokens.Token var3 : var0) {
         if (var3.isName()) {
            switch (var3.text) {
               case "attribute":
                  var3.text = "in";
                  break;
               case "varying":
                  var3.text = var1 ? "out" : "in";
                  break;
               case "texelFetch2D":
                  var3.text = "texelFetch";
                  break;
               case "texture2DGradARB":
               case "texture3DGradARB":
                  var3.text = "textureGrad";
                  break;
               case "texture2DLodARB":
               case "texture3DLodARB":
                  var3.text = "textureLod";
            }
         }
      }
   }

   static String version(String var0, IrisTransform.Kind var1) {
      String var2 = var0 == null ? "120" : var0.strip();
      String[] var3 = var2.split("\\s+");

      int var4;
      try {
         var4 = Integer.parseInt(var3[0]);
      } catch (NumberFormatException var6) {
         var4 = 120;
      }

      return var1 != IrisTransform.Kind.COMPUTE && var4 < 330 ? "330 compatibility" : var2;
   }

   static void renameBuiltins(List<GlslTokens.Token> var0, Map<String, String> var1) {
      for (GlslTokens.Token var3 : var0) {
         if (var3.isName()) {
            String var4 = (String)var1.get(var3.text);
            if (var4 != null) {
               var3.text = var4;
            }
         }
      }
   }

   private static void ftransform(List<GlslTokens.Token> var0) {
      for (int var1 = 0; var1 < var0.size(); var1++) {
         GlslTokens.Token var2 = (GlslTokens.Token)var0.get(var1);
         if (var2.is("ftransform")) {
            int var3 = GlslTokens.next(var0, var1 + 1);
            if (var3 < var0.size() && ((GlslTokens.Token)var0.get(var3)).is("(")) {
               int var4 = GlslTokens.close(var0, var3);

               for (int var5 = var3; var5 <= var4; var5++) {
                  ((GlslTokens.Token)var0.get(var5)).text = "";
               }

               var2.text = "(vp_ModelViewProjectionMatrix * vp_Vertex)";
            }
         }
      }
   }

   private static void removeInputs(List<GlslTokens.Token> var0, GlslDeclarations var1, IrisTransform.Stage var2) {
      for (GlslDeclarations.Declaration var4 : var1.declarations) {
         if (var4.has("in") || var4.has("attribute") || var4.has("uniform")) {
            ArrayList var5 = new ArrayList();

            for (String var7 : var4.names) {
               if (INPUTS.contains(var7)) {
                  var2.inputs.put(var7, var4.type);
               } else {
                  var5.add(var7);
               }
            }

            if (var5.size() != var4.names.size()) {
               String var8 = var5.isEmpty() ? "" : qualifiers(var0, var4) + var4.type + " " + String.join(", ", var5) + ";";
               blank(var0, var4.first, var4.last);
               ((GlslTokens.Token)var0.get(var4.first)).text = var8;
            }
         }
      }
   }

   private static void carry(List<GlslTokens.Token> var0, GlslDeclarations var1, LightmapFlow.Found var2, String var3) {
      carry(var0, var1, var2, var3, "vp_lm_");
   }

   private static void carry(List<GlslTokens.Token> var0, GlslDeclarations var1, LightmapFlow.Found var2, String var3, String var4) {
      GlslDeclarations.Declaration var5 = var1.global(var2.name(), Set.of("in", "out", "varying"));
      if (var5 != null) {
         String var6 = qualifiers(var0, var5);
         ArrayList var7 = new ArrayList();
         String var8 = "";

         for (int var9 = 0; var9 < var5.names.size(); var9++) {
            if (var5.names.get(var9).equals(var2.name())) {
               var8 = var5.arrays.get(var9);
            } else {
               var7.add(var5.names.get(var9) + var5.arrays.get(var9));
            }
         }

         String var11 = var5.has("varying") ? "varying" : var3;
         StringBuilder var10 = new StringBuilder();
         if (!var7.isEmpty()) {
            var10.append(var6).append(var5.type).append(' ').append(String.join(", ", var7)).append("; ");
         }

         var10.append(var5.type).append(' ').append(var2.name()).append(var8).append("; ");
         var10.append(var2.interpolation()).append(var11).append(' ').append(var5.type).append(' ').append(var4).append(var2.name()).append(var8).append(';');
         blank(var0, var5.first, var5.last);
         ((GlslTokens.Token)var0.get(var5.first)).text = var10.toString();
      }
   }

   private static String qualifiers(List<GlslTokens.Token> var0, GlslDeclarations.Declaration var1) {
      StringBuilder var2 = new StringBuilder();

      for (int var3 = var1.first; var3 <= var1.last; var3++) {
         GlslTokens.Token var4 = (GlslTokens.Token)var0.get(var3);
         if (var4.is(var1.type) && var4.isName()) {
            break;
         }

         var2.append(var4.text);
      }

      String var5 = var2.toString().strip();
      return var5.isEmpty() ? "" : var5 + " ";
   }

   private static void albedo(List<GlslTokens.Token> var0, IrisTransform.Stage var1) {
      GlslDeclarations var2 = GlslDeclarations.read(var0);
      LinkedHashSet var3 = new LinkedHashSet();

      for (GlslDeclarations.Declaration var5 : var2.declarations) {
         if (var5.has("uniform") && var5.type.equals("sampler2D")) {
            for (String var7 : var5.names) {
               if (ALBEDO.contains(var7)) {
                  var3.add(var7);
               }
            }
         }
      }

      AlbedoCalls var10 = AlbedoCalls.rewrite(var0, var3);
      var1.albedoReads = var10.rewritten;
      var1.notes.addAll(new LinkedHashSet<>(var10.notes));
      var2 = GlslDeclarations.read(var0);

      for (GlslDeclarations.Declaration var12 : var2.declarations) {
         if (var12.has("uniform") && var12.type.equals("sampler2D") && var12.names.stream().anyMatch(var3::contains)) {
            ArrayList var13 = new ArrayList<>(var12.names);
            var13.removeAll(var3);
            String var8 = var13.isEmpty() ? "" : qualifiers(var0, var12) + "sampler2D " + String.join(", ", var13) + ";";
            blank(var0, var12.first, var12.last);
            ((GlslTokens.Token)var0.get(var12.first)).text = var8;
         }
      }
   }

   static void renameNewerBuiltins(List<GlslTokens.Token> var0, IrisTransform.Stage var1) {
      if (var1.version.startsWith("330") || var1.version.startsWith("4")) {
         GlslDeclarations var2 = GlslDeclarations.read(var0);
         LinkedHashSet var3 = new LinkedHashSet();

         for (GlslDeclarations.Function var5 : var2.functions) {
            if (NEWER_BUILTINS.contains(var5.name)) {
               var3.add(var5.name);
            }
         }

         if (!var3.isEmpty()) {
            for (GlslTokens.Token var7 : var0) {
               if (var7.isName() && var3.contains(var7.text)) {
                  var7.text = "vp_pack_" + var7.text;
               }
            }

            var1.notes.add("functions named as GLSL 1.30's built-ins renamed: " + var3);
         }
      }
   }

   private static boolean renameMain(List<GlslTokens.Token> var0) {
      GlslDeclarations var1 = GlslDeclarations.read(var0);
      boolean var2 = false;

      for (GlslDeclarations.Function var4 : var1.functions("main")) {
         ((GlslTokens.Token)var0.get(var4.nameToken)).text = "vp_packMain";
         var2 = true;
      }

      GlobalInitializers.defer(var0);
      return var2;
   }

   static void finish(List<GlslTokens.Token> var0, IrisTransform.Stage var1) {
      GlslDeclarations var2 = GlslDeclarations.read(var0);

      for (GlslDeclarations.Declaration var4 : var2.declarations) {
         if (var4.has("uniform")) {
            for (String var6 : var4.names) {
               var1.uniforms.add(var6);
               if (var4.type.startsWith("sampler") || var4.type.contains("image") || var4.type.startsWith("isampler") || var4.type.startsWith("usampler")) {
                  var1.samplers.put(var6, var4.type);
               }
            }
         }

         if (var4.has("out") && !var4.has("in")) {
            for (String var10 : var4.names) {
               var1.outputs.put(var10, location(var0, var4));
            }
         }
      }

      for (GlslTokens.Token var8 : var0) {
         if (var8.isName() && (var8.is("gl_FragData") || var8.is("gl_FragColor"))) {
            var1.fragData = true;
         }
      }

      var1.code = GlslTokens.join(var0);
   }

   private static int location(List<GlslTokens.Token> var0, GlslDeclarations.Declaration var1) {
      for (int var2 = var1.first; var2 < var1.last; var2++) {
         if (((GlslTokens.Token)var0.get(var2)).is("location")) {
            int var3 = GlslTokens.next(var0, var2 + 1);
            int var4 = GlslTokens.next(var0, var3 + 1);

            try {
               return Integer.parseInt(((GlslTokens.Token)var0.get(var4)).text);
            } catch (NumberFormatException var6) {
               return -1;
            }
         }
      }

      return -1;
   }

   private static void blank(List<GlslTokens.Token> var0, int var1, int var2) {
      for (int var3 = var1; var3 <= var2; var3++) {
         GlslTokens.Token var4 = (GlslTokens.Token)var0.get(var3);
         var4.text = var4.kind != GlslTokens.Kind.SPACE && var4.kind != GlslTokens.Kind.COMMENT ? "" : var4.text.replaceAll("[^\n]", "");
      }
   }

   public enum Kind {
      GEOMETRY,
      COMPOSITE,
      COMPUTE;
   }

   public static final class Stage {
      public String version;
      public String code;
      public final List<String> extensions = new ArrayList<>();
      public final Map<String, String> inputs = new LinkedHashMap<>();
      public LightmapFlow.Found lightmap;
      public LightmapFlow.Found colour;
      public boolean mainRenamed;
      public boolean fragData;
      public final Map<String, Integer> outputs = new LinkedHashMap<>();
      public final Map<String, String> samplers = new LinkedHashMap<>();
      public final Set<String> uniforms = new LinkedHashSet<>();
      public int albedoReads;
      public int lampTints;
      public boolean raised;
      public final List<String> notes = new ArrayList<>();
   }
}
