package viewpoint.platform;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import java.util.Locale;
import java.util.regex.Pattern;
import org.lwjgl.opengl.GL11;

final class UnsupportedGraphics {
   private static final int RED = -12895233;
   private static final Pattern ARC = Pattern.compile("(?s).*\\bintel\\b.*\\barc\\b.*");
   static final String ARC_WARNING = "Viewpoint does not support Intel Arc graphics cards. There will be problems with the picture and with the game on this computer.";
   static final String MAC_WARNING = "Viewpoint does not support the graphics of macOS. There will be problems with the picture and with the game on this computer.";
   private static final ImVec2 size = new ImVec2();
   private static String warning;
   private static boolean read;

   private UnsupportedGraphics() {
   }

   static String warning() {
      if (!read) {
         read = true;
         warning = warning(String.valueOf(GL11.glGetString(7936)), String.valueOf(GL11.glGetString(7937)), System.getProperty("os.name", ""));
      }

      return warning;
   }

   static String warning(String var0, String var1, String var2) {
      if (var2.toLowerCase(Locale.ROOT).startsWith("mac")) {
         return "Viewpoint does not support the graphics of macOS. There will be problems with the picture and with the game on this computer.";
      } else {
         return ARC.matcher((var0 + " " + var1).toLowerCase(Locale.ROOT)).matches()
            ? "Viewpoint does not support Intel Arc graphics cards. There will be problems with the picture and with the game on this computer."
            : null;
      }
   }

   static void draw(float var0) {
      String var1 = warning();
      if (var1 != null) {
         float var2 = Math.max(1.0F, Theme.scale());
         float var3 = ImGui.getCursorScreenPosX();
         float var4 = ImGui.getCursorScreenPosY();
         float var5 = ImGui.getFontSize();
         ImDrawList var6 = ImGui.getWindowDrawList();
         ImGui.calcTextSize(size, var1, false, var0 - var2);
         var6.addText(ImGui.getFont(), var5, var3, var4, -12895233, var1, var0 - var2);
         var6.addText(ImGui.getFont(), var5, var3 + var2, var4, -12895233, var1, var0 - var2);
         ImGui.dummy(size.x + var2, size.y);
      }
   }
}
