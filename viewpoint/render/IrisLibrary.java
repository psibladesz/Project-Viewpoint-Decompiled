package viewpoint.render;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import viewpoint.iris.GlslNamespace;
import viewpoint.iris.IrisAssembler;
import viewpoint.iris.Preprocessor;
import viewpoint.platform.ShaderLibrary;

final class IrisLibrary {
   private final Map<String, String> pieces = new HashMap<>();
   private final Map<String, String> macros = ShaderLibrary.pipelineMacros();

   String vertex(IrisAssembler.Source var1) {
      return switch (var1) {
         case MESH -> this.piece("iris/common.glsl") + this.piece("iris/mesh_vertex.glsl");
         case MODEL, VEHICLE -> this.piece("iris/common.glsl") + this.piece("iris/model_vertex.glsl");
         case PLAIN -> this.piece("iris/common.glsl") + this.piece("iris/plain_vertex.glsl");
         case WEATHER -> this.piece("iris/common.glsl") + this.piece("iris/weather_vertex.glsl");
         case FAR_BOX, FAR_TREE -> this.piece("iris/common.glsl") + this.piece("iris/far_vertex.glsl");
         case FAR_SHELL -> this.piece("iris/common.glsl") + this.piece("iris/far_shell_vertex.glsl");
         case QUAD -> this.piece("iris/common.glsl") + this.piece("iris/quad_vertex.glsl");
      };
   }

   String geometry() {
      return this.piece("iris/common.glsl");
   }

   String fragment(IrisAssembler.Source var1) {
      return this.piece("iris/common.glsl") + switch (var1) {
         case MESH -> this.piece("iris/mesh_fragment.glsl");
         case MODEL -> this.piece("iris/model_fragment.glsl");
         case VEHICLE -> this.piece("iris/vehicle_fragment.glsl");
         default -> this.piece("iris/plain_fragment.glsl");
         case FAR_BOX, FAR_TREE -> this.piece("iris/far_fragment.glsl");
         case FAR_SHELL -> this.piece("iris/far_shell_fragment.glsl");
      };
   }

   String composite(boolean var1) {
      return this.piece("iris/common.glsl") + (var1 ? this.piece("iris/composite_fragment.glsl") : "");
   }

   private String piece(String var1) {
      return this.pieces.computeIfAbsent(var1, this::load);
   }

   private String load(String var1) {
      try {
         Preprocessor var2 = new Preprocessor(ShaderLibrary::read, true, this.macros, Map.of(), Set.of());
         String var3 = var2.run(var1).text().replaceAll("(?m)^#line.*$", "");
         return GlslNamespace.prefixed(var3) + "\n";
      } catch (IOException | IllegalArgumentException var4) {
         throw new IllegalStateException("the bridge's " + var1 + ": " + var4.getMessage(), var4);
      }
   }
}
