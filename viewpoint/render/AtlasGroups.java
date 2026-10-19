package viewpoint.render;

import java.util.List;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL33;
import viewpoint.platform.DrawCounts;
import viewpoint.platform.GlProgram;
import viewpoint.platform.LiveSettings;
import zombie.core.textures.TextureID;

final class AtlasGroups {
   static final LiveSettings.Toggle ON = LiveSettings.toggle("debug.atlasGroups", "Atlas pages picked in the shader (D35)", "Debug/Profiling", true);
   private static final String FIRST = "uAtlases[0]";

   static void samplers(GlProgram var0) {
      if (!var0.has("uAtlases[0]")) {
         var0.sampler("uTexture", 0);
      } else {
         for (int var1 = 0; var1 < 8; var1++) {
            var0.sampler("uAtlases[" + var1 + "]", 64 + var1);
         }
      }
   }

   static int unit(GlProgram var0) {
      return var0.has("uAtlases[0]") ? 64 : 0;
   }

   static int size(GlProgram var0) {
      return ON.get() && var0.has("uAtlases[0]") ? 8 : 1;
   }

   static void bind(int var0, List<Object> var1, int var2, int var3, int var4) {
      for (int var5 = 0; var5 < var3; var5++) {
         TextureID var6 = (TextureID)var1.get(var2 + var5);
         GL13.glActiveTexture(33984 + var0 + var5);
         Meshes.bindPage(var0 + var5, var6);
         DrawCounts.page(var4, var6);
      }

      GL13.glActiveTexture(33984);
   }

   static void release(int var0, int var1) {
      for (int var2 = var1 - 1; var2 >= 0; var2--) {
         GL13.glActiveTexture(33984 + var0 + var2);
         GL11.glBindTexture(3553, 0);
         GL33.glBindSampler(var0 + var2, 0);
      }

      GL13.glActiveTexture(33984);
   }

   private AtlasGroups() {
   }
}
