package viewpoint.iris;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CustomUniforms {
   private final List<CustomUniforms.Entry> entries = new ArrayList<>();
   private final Map<String, double[]> values = new HashMap<>();
   private final UniformExpression.State state = new UniformExpression.State();

   private CustomUniforms() {
   }

   public static CustomUniforms parse(ShadersProperties var0, List<String> var1) {
      CustomUniforms var2 = new CustomUniforms();

      for (ShadersProperties.CustomUniform var4 : var0.uniforms) {
         try {
            var2.entries
               .add(
                  new CustomUniforms.Entry(
                     var4, UniformExpression.parse(var4.expression(), (var4.variable() ? "variable." : "uniform.") + var4.type() + "." + var4.name())
                  )
               );
         } catch (IllegalArgumentException var6) {
            var1.add("custom uniform left out: " + var6.getMessage());
         }
      }

      return var2;
   }

   public void evaluate(UniformExpression.Names var1, double var2) {
      this.state.frame(var2);
      this.values.clear();
      UniformExpression.Names var4 = var2x -> {
         double[] var3 = this.values.get(var2x);
         return var3 != null ? var3 : var1.value(var2x);
      };

      for (CustomUniforms.Entry var6 : this.entries) {
         double[] var7 = var6.expression.evaluate(var4, this.state);
         this.values.put(var6.declaration.name(), sized(var7, var6.declaration.type()));
      }
   }

   public List<CustomUniforms.Entry> uniforms() {
      ArrayList var1 = new ArrayList();

      for (CustomUniforms.Entry var3 : this.entries) {
         if (!var3.declaration.variable()) {
            var1.add(var3);
         }
      }

      return var1;
   }

   public double[] value(String var1) {
      return this.values.get(var1);
   }

   private static double[] sized(double[] var0, String var1) {
      byte var2 = switch (var1) {
         case "vec2", "ivec2" -> 2;
         case "vec3", "ivec3" -> 3;
         case "vec4", "ivec4" -> 4;
         default -> 1;
      };
      if (var0.length == var2) {
         return var0;
      }

      double[] var5 = new double[var2];

      for (int var6 = 0; var6 < var2; var6++) {
         var5[var6] = var0[Math.min(var6, var0.length - 1)];
      }

      return var5;
   }

   public record Entry(ShadersProperties.CustomUniform declaration, UniformExpression expression) {
   }
}
