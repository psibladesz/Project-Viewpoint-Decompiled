package viewpoint.platform;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import java.nio.ByteBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;
import org.lwjglx.opengl.Display;
import zombie.network.DesktopBrowser;

final class CrashWindow {
   private static final float SHARE = 0.8F;
   private static final float MAX_WIDTH = 1000.0F;
   private static final float MAX_HEIGHT = 760.0F;
   private static final int TITLE = -10848001;
   private static final float BUTTON_LINES = 1.6F;
   private static final int FLAGS = 288;
   private static final int BOTTOM_FRAMES = 2;
   private static final ImBoolean open = new ImBoolean(true);
   private static String console;
   private static String copied = "";
   private static int toBottom = 2;

   static void draw() {
      ImGuiIO var0 = ImGui.getIO();
      float var1 = var0.getDisplaySizeX();
      float var2 = var0.getDisplaySizeY();
      float var3 = Theme.scale();
      float var4 = Math.min(var1 * 0.8F, 1000.0F * var3);
      float var5 = Math.min(var2 * 0.8F, 760.0F * var3);
      ImGui.setNextWindowPos((var1 - var4) / 2.0F, (var2 - var5) / 2.0F, 8);
      ImGui.setNextWindowSize(var4, var5, 8);
      open.set(true);
      if (console == null) {
         console = CrashReport.console(131072);
      }

      if (ImGui.begin("Viewpoint crashed###viewpoint-crash", open, 288)) {
         words();
         float var6 = ImGui.getFrameHeightWithSpacing() * 1.6F;
         consoleText(var6);
         buttons();
      }

      ImGui.end();
      if (!open.get()) {
         CrashReport.close();
      }
   }

   private static void words() {
      ImGui.pushStyleColor(0, -10848001);
      ImGui.textWrapped("Viewpoint ran into an error and turned itself off.");
      ImGui.popStyleColor();
      ImGui.textWrapped("The game goes on in its own view. To bring first person back, load the save again or restart the game.");
      ImGui.textWrapped(
         "Please tell us about it, so we can fix it: press Copy report, open our Discord server, go to the #bug-report channel, open a new bug report and paste the report into it (Ctrl+V). Say what you were doing when it happened. The report holds the game's log below, your Viewpoint settings, your graphics card and the error."
      );
      ImGui.spacing();
   }

   private static void consoleText(float var0) {
      ImGui.beginChildFrame(ImGui.getID("console"), -1.0F, -var0, 2048);
      ImGui.textUnformatted(console);
      if (toBottom > 0) {
         ImGui.setScrollHereY(1.0F);
         toBottom--;
      }

      ImGui.endChildFrame();
   }

   private static void buttons() {
      if (ImGui.button("Copy report")) {
         String var0 = CrashReport.text();
         ByteBuffer var1 = MemoryUtil.memUTF8(var0, true);

         try {
            GLFW.glfwSetClipboardString(Display.getWindow(), var1);
         } finally {
            MemoryUtil.memFree(var1);
         }

         copied = "Copied: " + var0.length() / 1024 + " KB.";
      }

      ImGui.sameLine();
      if (ImGui.button("Open our Discord server")) {
         DesktopBrowser.openURL("https://discord.gg/6hXWv7Usgk");
      }

      ImGui.sameLine();
      if (ImGui.button("Close")) {
         CrashReport.close();
      }

      if (!copied.isEmpty()) {
         ImGui.sameLine();
         ImGui.text(copied);
      }
   }

   private CrashWindow() {
   }
}
