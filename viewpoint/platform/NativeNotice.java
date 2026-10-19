package viewpoint.platform;

import imgui.ImGui;
import java.util.Locale;
import viewpoint.upscale.NativeFiles;

final class NativeNotice {
   private static final double MB = 1000000.0;

   private NativeNotice() {
   }

   static boolean shows(NativeNeeds.Need var0) {
      return var0.unsupported() != null || NativeFiles.state(var0.files()) != NativeFiles.State.READY || NativeFiles.downloaded(var0.files());
   }

   static void draw(NativeNeeds.Need var0, float var1) {
      NativeFiles.Set var2 = var0.files();
      ImGui.pushID(var0.feature());
      ImGui.pushTextWrapPos(ImGui.getCursorPosX() + var1);
      if (var0.unsupported() != null) {
         dim(var0.feature() + " cannot run on this computer: " + var0.unsupported() + ". Meanwhile, " + var0.meanwhile() + ".");
      } else {
         switch (NativeFiles.state(var2)) {
            case MISSING:
               missing(var0, var2);
               break;
            case DOWNLOADING:
               progress(var2, var1);
               break;
            case FAILED:
               failed(var2);
               break;
            case READY:
               dim("Downloaded from " + var2.maker + ". " + var0.feature() + " is ready.");
         }
      }

      ImGui.popTextWrapPos();
      ImGui.popID();
   }

   private static void missing(NativeNeeds.Need var0, NativeFiles.Set var1) {
      ImGui.textWrapped(var0.feature() + " needs files from " + var1.maker + " to work properly. Download them directly using the button below.");
      if (Widgets.accentButton("Download (" + megabytes(var1) + ")")) {
         NativeFiles.download(var1);
      }
   }

   private static void progress(NativeFiles.Set var0, float var1) {
      float var2 = NativeFiles.progress(var0);
      String var3 = String.format(Locale.ROOT, "Downloading from %s: %.0f %% of %s", var0.maker, var2 * 100.0F, megabytes(var0));
      ImGui.pushStyleColor(42, Theme.ACCENT);
      ImGui.pushStyleColor(7, Theme.FRAME);
      ImGui.progressBar(var2, var1, ImGui.getFrameHeight(), var3);
      ImGui.popStyleColor(2);
   }

   private static void failed(NativeFiles.Set var0) {
      String var1 = "The download from " + var0.maker + " failed: " + NativeFiles.failure(var0) + ".";
      ImGui.textWrapped(var1.replace("%", "%%"));
      if (Widgets.accentButton("Try again")) {
         NativeFiles.download(var0);
      }
   }

   private static String megabytes(NativeFiles.Set var0) {
      return String.format(Locale.ROOT, "%.0f MB", var0.bytes() / 1000000.0);
   }

   private static void dim(String var0) {
      ImGui.pushStyleColor(0, Theme.TEXT_DIM);
      ImGui.textWrapped(var0.replace("%", "%%"));
      ImGui.popStyleColor();
   }
}
