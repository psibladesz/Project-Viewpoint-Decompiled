package viewpoint.iris;

import java.util.Map.Entry;

public final class IrisAssembler {
   public static final int LIBRARY_SOURCE = 999;

   private IrisAssembler() {
   }

   public static String vertex(IrisTransform.Stage var0, String var1, IrisAssembler.Source var2) {
      StringBuilder var3 = header(var0, var1, var2 == IrisAssembler.Source.QUAD && isOld(var0.version));
      inputs(var3, var0);
      var3.append(var0.code).append("\n#line 1 ").append(999).append("\nvoid main() {\n");
      vertexMain(var3, var0, var2);
      return var3.append("}\n").toString();
   }

   static void inputs(StringBuilder var0, IrisTransform.Stage var1) {
      for (Entry var3 : var1.inputs.entrySet()) {
         var0.append((String)var3.getValue()).append(' ').append((String)var3.getKey()).append(";\n");
      }
   }

   static void vertexMain(StringBuilder var0, IrisTransform.Stage var1, IrisAssembler.Source var2) {
      var0.append(switch (var2) {
         case MESH -> "  vp_fetchMesh();\n";
         case MODEL, VEHICLE -> "  vp_fetchModel();\n";
         case PLAIN -> "  vp_fetchPlain();\n";
         case WEATHER -> "  vp_fetchWeather();\n";
         case FAR_BOX -> "  vp_fetchFarBox();\n";
         case FAR_TREE -> "  vp_fetchFarTree();\n";
         case FAR_SHELL -> "  vp_fetchFarShell();\n";
         case QUAD -> "  vp_fetchQuad();\n";
      });
      if (var2 != IrisAssembler.Source.QUAD) {
         for (Entry var4 : var1.inputs.entrySet()) {
            var0.append("  ")
               .append((String)var4.getKey())
               .append(" = ")
               .append((String)var4.getValue())
               .append('(')
               .append(value((String)var4.getKey()))
               .append(");\n");
         }
      }

      var0.append(var1.mainRenamed ? "  vp_packMain();\n" : "");
      if (var1.lightmap != null) {
         var0.append("  vp_lm_").append(var1.lightmap.name()).append(" = ").append(var1.lightmap.name()).append(";\n");
      }

      if (var1.colour != null) {
         String var5 = var1.colour.name();
         var0.append("  vp_col_").append(var5).append(" = ").append(var5).append(";\n");
      }

      if (var2 != IrisAssembler.Source.QUAD) {
         var0.append("  if (vp_hiddenVertex) gl_Position = vec4(2.0, 2.0, 2.0, 1.0);\n");
      }
   }

   public static String fragment(IrisTransform.Stage var0, String var1, IrisAssembler.Source var2, int var3) {
      StringBuilder var4 = header(var0, var1, false);
      if (var3 >= 0 && !var0.fragData) {
         var4.append("layout(location = ").append(var3).append(") out vec4 vp_tintOut;\n");
      }

      var4.append(var0.code).append("\n#line 1 ").append(999).append("\nvoid main() {\n");
      var4.append("  vp_prologue();\n");
      LightmapFlow.Found var5 = var0.lightmap;
      if (var5 != null) {
         String var6 = var5.name();
         var4.append("  ").append(var6).append(" = vp_lm_").append(var6).append(";\n");
         var4.append("  ")
            .append(var6)
            .append('.')
            .append(var5.components())
            .append(" = clamp(")
            .append(var6)
            .append('.')
            .append(var5.components())
            .append(" + vp_lightmapDelta(), 0.0, 1.0);\n");
      }

      if (var0.colour != null) {
         String var7 = var0.colour.name();
         var4.append("  ").append(var7).append(" = vp_col_").append(var7).append(";\n");
         var4.append("  ").append(var7).append(".rgb = vp_carriedColour(").append(var7).append(".rgb);\n");
      }

      var4.append(var0.mainRenamed ? "  vp_packMain();\n" : "");
      if (var3 >= 0) {
         var4.append(var0.fragData ? "  gl_FragData[" + var3 + "] = vp_tintOutput();\n" : "  vp_tintOut = vp_tintOutput();\n");
      }

      return var4.append("}\n").toString();
   }

   public static String plain(IrisTransform.Stage var0, String var1) {
      StringBuilder var2 = header(var0, var1, false);
      return var2.append(var0.code).append('\n').toString();
   }

   static StringBuilder header(IrisTransform.Stage var0, String var1, boolean var2) {
      StringBuilder var3 = new StringBuilder(var1.length() + var0.code.length() + 1024);
      var3.append("#version ").append(var0.version).append('\n');

      for (String var5 : var0.extensions) {
         var3.append(var5).append('\n');
      }

      String var6 = var1;
      if (isOld(var0.version)) {
         var6 = var6.replace("texture(", "texture2D(");
      }

      if (var2) {
         var6 = var6.replaceAll("(?m)^in ", "attribute ");
      }

      var3.append("#line 1 ").append(999).append('\n').append(var6).append('\n');
      return var3;
   }

   static boolean isOld(String var0) {
      try {
         return Integer.parseInt(var0.strip().split("\\s+")[0]) < 130;
      } catch (NumberFormatException var2) {
         return true;
      }
   }

   private static String value(String var0) {
      return switch (var0) {
         case "mc_Entity" -> "vp_entity";
         case "mc_midTexCoord" -> "vp_midTexCoord";
         case "at_tangent" -> "vp_tangent";
         case "at_midBlock" -> "vp_midBlock";
         case "at_velocity" -> "vec4(vp_velocity, 0.0)";
         case "vaPosition" -> "vp_Vertex";
         case "vaColor" -> "vp_Color";
         case "vaUV0" -> "vp_MultiTexCoord0";
         case "vaUV2" -> "vp_MultiTexCoord1";
         case "vaNormal" -> "vec4(vp_Normal, 0.0)";
         case "dhMaterialId" -> "vp_entity";
         case "mc_chunkFade" -> "vec4(1.0)";
         default -> "vec4(0.0)";
      };
   }

   public enum Source {
      MESH,
      MODEL,
      VEHICLE,
      PLAIN,
      WEATHER,
      FAR_BOX,
      FAR_TREE,
      FAR_SHELL,
      QUAD;
   }
}
