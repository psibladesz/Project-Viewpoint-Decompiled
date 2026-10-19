package viewpoint.platform;

import imgui.ImDrawList;
import imgui.ImGui;
import java.util.concurrent.atomic.AtomicBoolean;

final class Icons {
   static final String STRIP = "/viewpoint/ui/icons.png";
   private static volatile Pictures.Texels read;
   private static final AtomicBoolean started = new AtomicBoolean();
   private static Pictures.Picture strip;

   static void draw(Icons.Icon var0, float var1, int var2) {
      float var3 = ImGui.getCursorScreenPosX();
      float var4 = ImGui.getCursorScreenPosY();
      ImGui.dummy(var1, var1);
      draw(ImGui.getWindowDrawList(), var0, var3, var4, var1, var2);
   }

   static void draw(ImDrawList var0, Icons.Icon var1, float var2, float var3, float var4, int var5) {
      Pictures.Picture var6 = strip();
      if (var6 != null) {
         float var7 = Icons.Icon.values().length;
         float var8 = var1.ordinal() / var7;
         float var9 = (var1.ordinal() + 1) / var7;
         var0.addImage(var6.texture(), var2, var3, var2 + var4, var3 + var4, var8, 0.0F, var9, 1.0F, var5);
      }
   }

   static boolean button(String var0, Icons.Icon var1, float var2, String var3) {
      float var4 = ImGui.getCursorScreenPosX();
      float var5 = ImGui.getCursorScreenPosY();
      boolean var6 = ImGui.invisibleButton(var0, var2, var2);
      int var7 = ImGui.isItemActive() ? Theme.ACCENT : (ImGui.isItemHovered() ? Theme.TEXT : Theme.TEXT_DIM);
      draw(ImGui.getWindowDrawList(), var1, var4, var5, var2, var7);
      if (ImGui.isItemHovered() && !var3.isEmpty()) {
         ImGui.setTooltip(var3.replace("%", "%%"));
      }

      return var6;
   }

   static boolean windowButton(String var0, boolean var1, float var2, String var3) {
      float var4 = ImGui.getCursorScreenPosX();
      float var5 = ImGui.getCursorScreenPosY();
      boolean var6 = ImGui.invisibleButton(var0, var2, var2);
      int var7 = ImGui.isItemActive() ? Theme.ACCENT : (ImGui.isItemHovered() ? Theme.TEXT : Theme.TEXT_DIM);
      ImDrawList var8 = ImGui.getWindowDrawList();
      float var9 = var2 * 0.2F;
      float var10 = Math.max(1.5F, var2 * 0.1F);
      if (var1) {
         var8.addRect(var4 + var9, var5 + var9, var4 + var2 - var9, var5 + var2 - var9, var7, 0.0F, 0, var10);
      } else {
         var8.addLine(var4 + var9, var5 + var2 * 0.5F, var4 + var2 - var9, var5 + var2 * 0.5F, var7, var10);
      }

      if (ImGui.isItemHovered() && !var3.isEmpty()) {
         ImGui.setTooltip(var3.replace("%", "%%"));
      }

      return var6;
   }

   private static Pictures.Picture strip() {
      if (strip != null) {
         return strip;
      }

      if (started.compareAndSet(false, true)) {
         Thread var0 = new Thread(Icons::readStrip, "Viewpoint icons");
         var0.setDaemon(true);
         var0.start();
      }

      Pictures.Texels var1 = read;
      if (var1 != null && var1.width() > 0) {
         strip = Pictures.upload(var1);
      }

      return strip;
   }

   private static void readStrip() {
      try {
         read = Pictures.decode("/viewpoint/ui/icons.png");
      } catch (Exception var1) {
         read = new Pictures.Texels(null, 0, 0);
         System.out.println("[Viewpoint] settings window's icons not read: " + var1);
      }
   }

   private Icons() {
   }

   enum Icon {
      MONITOR,
      KEYBOARD,
      GAMEPAD,
      GLOBE,
      BUG,
      FOLDER,
      CLOSE,
      RESET,
      ADD,
      CLEAR,
      INFO,
      NEXT,
      BACK,
      LESS,
      MORE;
   }
}
