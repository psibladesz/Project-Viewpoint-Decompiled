package viewpoint.platform;

import imgui.ImGui;
import zombie.core.Core;
import zombie.core.textures.Texture;

final class PadGlyphs {
   private static volatile String style = "";
   private static volatile Texture[] textures = new Texture[0];

   static void update() {
      String var0 = Core.getInstance().getOptionControllerButtonStyleString();
      if (!var0.equals(style)) {
         Texture[] var1 = new Texture[17];

         for (int var2 = 0; var2 < 17; var2++) {
            var1[var2] = var2 == 10 ? null : Texture.getSharedTexture(PadChord.glyph(var0, var2));
         }

         textures = var1;
         style = var0;
      }
   }

   static String style() {
      String var0 = style;
      return var0.isEmpty() ? PadChord.STYLES.get(0) : var0;
   }

   static boolean draw(int var0, float var1) {
      Texture[] var2 = textures;
      Texture var3 = var0 < var2.length ? var2[var0] : null;
      if (var3 != null && var3.getID() > 0) {
         ImGui.image(var3.getID(), var1, var1, var3.getXStart(), var3.getYStart(), var3.getXEnd(), var3.getYEnd());
         return true;
      } else {
         return false;
      }
   }

   private PadGlyphs() {
   }
}
