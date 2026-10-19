package viewpoint.iris;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SmoothNormals {
   private static final Set<String> FLOAT_TYPES = Set.of(
      "float", "vec2", "vec3", "vec4", "mat2", "mat3", "mat4", "mat2x2", "mat2x3", "mat2x4", "mat3x2", "mat3x3", "mat3x4", "mat4x2", "mat4x3", "mat4x4"
   );
   private static final List<String> FRAME = List.of("normal", "tangent", "tbn");

   private SmoothNormals() {
   }

   public static String unflatten(String var0) {
      List var1 = GlslTokens.lex(var0);
      boolean var2 = false;

      for (GlslDeclarations.Declaration var4 : GlslDeclarations.read(var1).declarations) {
         if (var4.has("flat")
            && FLOAT_TYPES.contains(var4.type)
            && (var4.has("in") || var4.has("out") || var4.has("varying"))
            && var4.names.stream().allMatch(SmoothNormals::isFrame)) {
            for (int var5 = var4.first; var5 <= var4.last; var5++) {
               if (((GlslTokens.Token)var1.get(var5)).is("flat")) {
                  ((GlslTokens.Token)var1.get(var5)).text = "";
                  var2 = true;
               }
            }
         }
      }

      return var2 ? GlslTokens.join(var1) : var0;
   }

   private static boolean isFrame(String var0) {
      String var1 = var0.toLowerCase(Locale.ROOT);
      return FRAME.stream().anyMatch(var1::contains);
   }
}
