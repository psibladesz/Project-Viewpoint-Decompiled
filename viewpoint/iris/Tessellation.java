package viewpoint.iris;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Tessellation {
   static final float STEP = 1.0F;
   static final float SPREAD = 0.125F;
   static final int MOST = 64;
   private static final Set<String> VARYING = Set.of("aPos", "aUv", "aNormal", "aFill", "aVertex");
   private static final String EXTENSION = "#extension GL_ARB_tessellation_shader : require\n";
   private static final Pattern ATTRIBUTE = Pattern.compile("layout\\s*\\(\\s*location\\s*=\\s*\\d+\\s*\\)\\s*in\\s+(\\w+)\\s+(\\w+)\\s*;");
   private static final Pattern VERTEX_ID = Pattern.compile("\\bgl_VertexID\\b");
   private static final Pattern INSTANCE_ID = Pattern.compile("\\bgl_InstanceID\\b");

   private Tessellation() {
   }

   static List<Tessellation.Attribute> attributes(String var0) {
      ArrayList var1 = new ArrayList();
      Matcher var2 = ATTRIBUTE.matcher(var0);

      while (var2.find()) {
         var1.add(new Tessellation.Attribute(var2.group(1), var2.group(2)));
      }

      return var1;
   }

   public static String vertex(String var0, IrisAssembler.Source var1) {
      StringBuilder var2 = new StringBuilder("#version 330 compatibility\n").append(var0).append('\n');

      for (Tessellation.Attribute var4 : attributes(var0)) {
         var2.append(var4.declared("out", "_v"));
      }

      var2.append("out vec3 vp_place_v;\nflat out int vp_hidden_v;\nflat out int vp_vertexId_v;\n")
         .append("flat out int vp_instanceId_v;\nvoid main() {\n  ")
         .append(fetch(var1))
         .append(";\n");

      for (Tessellation.Attribute var6 : attributes(var0)) {
         var2.append("  ").append(var6.name).append("_v = ").append(var6.name).append(";\n");
      }

      return var2.append("  vp_place_v = vp_Vertex.xyz;\n  vp_hidden_v = vp_hiddenVertex ? 1 : 0;\n")
         .append("  vp_vertexId_v = gl_VertexID;\n  vp_instanceId_v = gl_InstanceID;\n")
         .append("  gl_Position = vp_Vertex;\n}\n")
         .toString();
   }

   public static String control(String var0) {
      StringBuilder var1 = new StringBuilder("#version 330 core\n").append("#extension GL_ARB_tessellation_shader : require\n");
      var1.append("layout(vertices = 3) out;\n");

      for (Tessellation.Attribute var3 : attributes(var0)) {
         var1.append(var3.declared("in", "_v[]")).append(var3.declared("out", "_c[]"));
      }

      var1.append("in vec3 vp_place_v[];\nflat in int vp_hidden_v[];\nflat in int vp_vertexId_v[];\n")
         .append("flat in int vp_instanceId_v[];\nflat out int vp_vertexId_c[];\nflat out int vp_instanceId_c[];\n")
         .append("uniform int uPlants;\nuniform int uModels;\n")
         .append("float vp_pieces(vec3 a, vec3 b) {\n")
         .append("  float step = max(")
         .append(1.0F)
         .append(", 0.5 * length(a + b) * ")
         .append(0.125F)
         .append(");\n  return clamp(ceil(distance(a, b) / step), 1.0, ")
         .append(64)
         .append(".0);\n}\nvoid main() {\n");

      for (Tessellation.Attribute var5 : attributes(var0)) {
         var1.append("  ").append(var5.name).append("_c[gl_InvocationID] = ").append(var5.name);
         var1.append("_v[gl_InvocationID];\n");
      }

      return var1.append("  vp_vertexId_c[gl_InvocationID] = vp_vertexId_v[gl_InvocationID];\n")
         .append("  vp_instanceId_c[gl_InvocationID] = vp_instanceId_v[gl_InvocationID];\n")
         .append("  if (gl_InvocationID != 0) return;\n")
         .append("  float keep = vp_hidden_v[0] != 0 ? 0.0 : 1.0;\n")
         .append("  bool whole = uPlants != 0 || uModels != 0;\n")
         .append("  gl_TessLevelOuter[0] = keep * (whole ? 1.0 : vp_pieces(vp_place_v[1], vp_place_v[2]));\n")
         .append("  gl_TessLevelOuter[1] = keep * (whole ? 1.0 : vp_pieces(vp_place_v[2], vp_place_v[0]));\n")
         .append("  gl_TessLevelOuter[2] = keep * (whole ? 1.0 : vp_pieces(vp_place_v[0], vp_place_v[1]));\n")
         .append("  gl_TessLevelInner[0] = max(gl_TessLevelOuter[0], max(gl_TessLevelOuter[1],")
         .append(" gl_TessLevelOuter[2]));\n}\n")
         .toString();
   }

   public static String evaluation(IrisTransform.Stage var0, String var1, IrisAssembler.Source var2) {
      List<Tessellation.Attribute> var3 = attributes(var1);
      String var4 = ATTRIBUTE.matcher(var1).replaceAll("$1 $2;");
      var4 = INSTANCE_ID.matcher(VERTEX_ID.matcher(var4).replaceAll("vp_vertexId")).replaceAll("vp_instanceId");
      StringBuilder var5 = IrisAssembler.header(var0, "int vp_vertexId;\nint vp_instanceId;\n" + var4, false);
      var5.insert(var5.indexOf("\n") + 1, "#extension GL_ARB_tessellation_shader : require\n");
      var5.append("layout(triangles, equal_spacing) in;\n");

      for (Tessellation.Attribute var7 : var3) {
         var5.append(var7.declared("in", "_c[]"));
      }

      var5.append("flat in int vp_vertexId_c[];\nflat in int vp_instanceId_c[];\n");
      IrisAssembler.inputs(var5, var0);
      String var11 = VERTEX_ID.matcher(var0.code).replaceAll("vp_vertexId");
      var5.append(INSTANCE_ID.matcher(var11).replaceAll("vp_instanceId"));
      var5.append("\n#line 1 ").append(999).append("\nvoid main() {\n");
      var5.append("  vec3 t = gl_TessCoord;\n");

      for (Tessellation.Attribute var8 : var3) {
         String var9 = var8.name + "_c";
         var5.append("  ").append(var8.name).append(" = ");
         var5.append(var8.varying() ? "t.x * " + var9 + "[0] + t.y * " + var9 + "[1] + t.z * " + var9 + "[2];\n" : var9 + "[0];\n");
      }

      var5.append("  vp_vertexId = t.x == 1.0 ? vp_vertexId_c[0] : t.y == 1.0 ? vp_vertexId_c[1]")
         .append(" : t.z == 1.0 ? vp_vertexId_c[2] : -1;\n  vp_instanceId = vp_instanceId_c[0];\n");
      IrisAssembler.vertexMain(var5, var0, var2);
      return var5.append("}\n").toString();
   }

   private static String fetch(IrisAssembler.Source var0) {
      return switch (var0) {
         case MESH -> "vp_fetchMesh()";
         case FAR_BOX -> "vp_fetchFarBox()";
         default -> throw new IllegalArgumentException(var0 + " is not tessellated");
      };
   }

   record Attribute(String type, String name) {
      boolean varying() {
         String var1 = this.name.startsWith("vp_") ? this.name.substring("vp_".length()) : this.name;
         return Tessellation.VARYING.contains(var1);
      }

      String declared(String var1, String var2) {
         boolean var3 = this.type.startsWith("int") || this.type.startsWith("uint") || this.type.matches("[iu]vec\\d");
         return (var3 ? "flat " : "") + var1 + " " + this.type + " " + this.name + var2 + ";\n";
      }
   }
}
