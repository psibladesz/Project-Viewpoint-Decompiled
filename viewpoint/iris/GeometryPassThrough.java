package viewpoint.iris;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GeometryPassThrough {
   private static final String SUFFIX = "_g";
   private static final Pattern OUTPUT = Pattern.compile("(?m)^\\s*(flat\\s+|noperspective\\s+|smooth\\s+)?out\\s+(\\w+)\\s+(vp_\\w+)\\s*;");

   private GeometryPassThrough() {
   }

   public static IrisTransform.Stage stage(Preprocessor.Result var0) {
      IrisTransform.Stage var1 = IrisTransform.start(var0, IrisTransform.Kind.GEOMETRY);
      List var2 = GlslTokens.lex(var0.text());
      IrisTransform.renameBuiltins(var2, IrisTransform.MATRICES);
      IrisTransform.modernise(var2, false);
      IrisTransform.renameBuiltins(var2, Map.of("EmitVertex", "vp_emitVertex", "EndPrimitive", "vp_endPrimitive"));
      IrisTransform.renameNewerBuiltins(var2, var1);
      IrisTransform.finish(var2, var1);
      return var1;
   }

   static List<GeometryPassThrough.Varying> outputs(String var0) {
      ArrayList var1 = new ArrayList();
      Matcher var2 = OUTPUT.matcher(var0);

      while (var2.find()) {
         String var3 = var2.group(1) == null ? "" : var2.group(1).strip() + " ";
         var1.add(new GeometryPassThrough.Varying(var3, var2.group(2), var2.group(3)));
      }

      return var1;
   }

   public static String renamed(String var0) {
      String var1 = var0;

      for (GeometryPassThrough.Varying var3 : outputs(var0)) {
         var1 = var1.replaceAll("\\b" + var3.name + "\\b", var3.name + "_g");
      }

      return var1;
   }

   public static String geometry(IrisTransform.Stage var0, String var1, String var2) {
      List<GeometryPassThrough.Varying> var3 = outputs(var2);
      StringBuilder var4 = IrisAssembler.header(var0, var1, false);
      var4.append("void vp_emitVertex();\nvoid vp_endPrimitive();\n");
      var4.append(var0.code).append("\n#line 1 ").append(999).append('\n');

      for (GeometryPassThrough.Varying var6 : var3) {
         var4.append(var6.qualifier).append("in ").append(var6.type).append(' ').append(var6.name + "_g").append("[];\n");
         var4.append(var6.qualifier).append("out ").append(var6.type).append(' ').append(var6.name).append(";\n");
      }

      var4.append("int vp_emitted = 0;\nvoid vp_emitVertex() {\n  int k = vp_emitted % gl_in.length();\n");

      for (GeometryPassThrough.Varying var8 : var3) {
         var4.append("  ").append(var8.name).append(" = ").append(var8.name + "_g").append("[k];\n");
      }

      return var4.append("  vp_emitted++;\n  EmitVertex();\n}\n").append("void vp_endPrimitive() {\n  vp_emitted = 0;\n  EndPrimitive();\n}\n").toString();
   }

   record Varying(String qualifier, String type, String name) {
   }
}
