package viewpoint.platform;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ShaderLibrary {
   public static String read(String var0) {
      try {
         return Shaders.read(var0);
      } catch (IllegalStateException var2) {
         return null;
      }
   }

   public static Map<String, String> pipelineMacros() {
      LinkedHashMap var0 = new LinkedHashMap();

      for (String var4 : GlProgram.pack().defines.split("\n")) {
         String[] var5 = var4.strip().split("\\s+", 3);
         if (var5.length >= 2 && var5[0].equals("#define")) {
            var0.put(var5[1], var5.length > 2 ? var5[2] : "");
         }
      }

      return var0;
   }

   private ShaderLibrary() {
   }
}
