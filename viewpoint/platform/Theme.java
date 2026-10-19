package viewpoint.platform;

import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.gl3.ImGuiImplGl3;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.Deque;

final class Theme {
   private static final int ORANGE = 16345616;
   private static final int ORANGE_LIGHT = 16747069;
   private static final int ORANGE_DARK = 13915658;
   private static final int ORANGE_DIM = 6763799;
   static final int ACCENT_RGB = 16345616;
   static final int ACCENT = abgr(16345616, 1.0F);
   static final int ACCENT_HOVER = abgr(16747069, 1.0F);
   static final int ACCENT_PRESSED = abgr(13915658, 1.0F);
   static final int ACCENT_SOFT = abgr(16345616, 0.14F);
   static final int ON_ACCENT = abgr(1707011, 1.0F);
   static final int WINDOW = abgr(1184533, 0.98F);
   static final int SIDEBAR = abgr(855568, 1.0F);
   static final int CARD = abgr(1579291, 1.0F);
   static final int BORDER = abgr(2237478, 1.0F);
   static final int FRAME = abgr(2171943, 1.0F);
   static final int FRAME_HOVER = abgr(2829874, 1.0F);
   static final int TRACK = abgr(2106149, 1.0F);
   static final int KNOB = abgr(15921906, 1.0F);
   static final int TEXT = abgr(14277339, 1.0F);
   static final int TEXT_DIM = abgr(9343382, 1.0F);
   static final int TEXT_FAINT = abgr(6053732, 1.0F);
   static final int DIM = abgr(0, 0.6F);
   static final int FAINT = abgr(16777215, 0.25F);
   static final float BODY = 15.0F;
   static final float CAPTION = 12.5F;
   static final float LARGE = 18.0F;
   static final float TITLE = 26.0F;
   static final float DISPLAY = 40.0F;
   private static final String FONT = "/viewpoint/ui/Roboto-Medium.ttf";
   private static final String FONT_FILE = "viewpoint-Roboto-Medium.ttf";
   private static final float[] SIZES = new float[]{12.5F, 15.0F, 18.0F, 26.0F, 40.0F};
   private static final float BASE_HEIGHT = 1080.0F;
   private static float scale;
   private static String fontFile;
   private static final ImFont[] fonts = new ImFont[SIZES.length];
   private static final Deque<float[]> pushed = new ArrayDeque<>();
   private static final Deque<ImFont> pushedFonts = new ArrayDeque<>();

   static void fit(float var0, ImGuiImplGl3 var1) {
      float var2 = Math.max(1.0F, var0 / 1080.0F);
      if (var2 != scale) {
         scale = var2;
         fonts();
         style();
         var1.updateFontsTexture();
      }
   }

   static float scale() {
      return scale == 0.0F ? 1.0F : scale;
   }

   static void push(float var0) {
      int var1 = 0;

      while (var1 < SIZES.length - 1 && SIZES[var1] < var0) {
         var1++;
      }

      ImFont var2 = fonts[var1];
      pushed.push(new float[]{var2.getScale()});
      pushedFonts.push(var2);
      var2.setScale(var0 / SIZES[var1]);
      ImGui.pushFont(var2);
   }

   static void pop() {
      pushedFonts.pop().setScale(pushed.pop()[0]);
      ImGui.popFont();
   }

   private static void fonts() {
      ImFontAtlas var0 = ImGui.getIO().getFonts();
      var0.clear();
      ImFontConfig var1 = new ImFontConfig();
      var1.setOversampleH(2);
      var1.setOversampleV(1);

      try {
         fonts[1] = var0.addFontFromFileTTF(fontFile(), Math.round(15.0F * scale), var1);

         for (int var2 = 0; var2 < SIZES.length; var2++) {
            if (var2 != 1) {
               fonts[var2] = var0.addFontFromFileTTF(fontFile(), Math.round(SIZES[var2] * scale), var1);
            }
         }
      } finally {
         var1.destroy();
      }

      ImGui.getIO().setFontDefault(fonts[1]);
   }

   private static String fontFile() {
      if (fontFile != null) {
         return fontFile;
      }

      try (InputStream var0 = Theme.class.getResourceAsStream("/viewpoint/ui/Roboto-Medium.ttf")) {
         if (var0 == null) {
            throw new IllegalStateException("no /viewpoint/ui/Roboto-Medium.ttf in the JAR");
         }

         byte[] var1 = var0.readAllBytes();
         Path var2 = Path.of(System.getProperty("java.io.tmpdir"), "viewpoint-Roboto-Medium.ttf");
         if (!Files.isRegularFile(var2) || Files.size(var2) != var1.length) {
            Path var3 = Files.createTempFile(var2.getParent(), "viewpoint-Roboto-Medium.ttf", ".part");
            Files.write(var3, var1);
            Files.move(var3, var2, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         }

         fontFile = var2.toString();
      } catch (IOException var6) {
         throw new IllegalStateException("/viewpoint/ui/Roboto-Medium.ttf could not be written for ImGui", var6);
      }

      return fontFile;
   }

   private static void style() {
      ImGuiStyle var0 = ImGui.getStyle();
      colours(var0);
      sizes(var0);
   }

   private static void colours(ImGuiStyle var0) {
      colour(var0, 0, TEXT);
      colour(var0, 1, TEXT_FAINT);
      colour(var0, 2, WINDOW);
      colour(var0, 3, 0);
      colour(var0, 4, abgr(1382428, 0.98F));
      colour(var0, 5, BORDER);
      colour(var0, 6, 0);
      colour(var0, 7, FRAME);
      colour(var0, 8, FRAME_HOVER);
      colour(var0, 9, abgr(3159614, 1.0F));
      colour(var0, 10, SIDEBAR);
      colour(var0, 11, SIDEBAR);
      colour(var0, 12, SIDEBAR);
      colour(var0, 13, CARD);
      colour(var0, 14, 0);
      colour(var0, 15, abgr(2830391, 1.0F));
      colour(var0, 16, abgr(3554372, 1.0F));
      colour(var0, 17, ACCENT);
      colour(var0, 18, ACCENT);
      colour(var0, 19, ACCENT);
      colour(var0, 20, ACCENT_HOVER);
      colour(var0, 21, FRAME);
      colour(var0, 22, FRAME_HOVER);
      colour(var0, 23, ACCENT_PRESSED);
      colour(var0, 24, ACCENT_SOFT);
      colour(var0, 25, abgr(16345616, 0.24F));
      colour(var0, 26, abgr(16345616, 0.34F));
      colour(var0, 27, BORDER);
      colour(var0, 28, ACCENT);
      colour(var0, 29, ACCENT);
      colour(var0, 30, 0);
      colour(var0, 31, ACCENT_SOFT);
      colour(var0, 32, ACCENT);
      colour(var0, 33, CARD);
      colour(var0, 34, FRAME_HOVER);
      colour(var0, 35, abgr(6763799, 1.0F));
      colour(var0, 36, CARD);
      colour(var0, 37, abgr(6763799, 1.0F));
      colour(var0, 40, TEXT_DIM);
      colour(var0, 41, ACCENT);
      colour(var0, 42, ACCENT);
      colour(var0, 43, ACCENT_HOVER);
      colour(var0, 44, CARD);
      colour(var0, 45, BORDER);
      colour(var0, 46, abgr(1974822, 1.0F));
      colour(var0, 47, 0);
      colour(var0, 48, abgr(16777215, 0.02F));
      colour(var0, 49, abgr(16345616, 0.35F));
      colour(var0, 51, ACCENT);
      colour(var0, 54, DIM);
   }

   private static void sizes(ImGuiStyle var0) {
      float var1 = scale;
      var0.setWindowPadding(12.0F * var1, 12.0F * var1);
      var0.setFramePadding(8.0F * var1, 5.0F * var1);
      var0.setCellPadding(6.0F * var1, 4.0F * var1);
      var0.setItemSpacing(8.0F * var1, 7.0F * var1);
      var0.setItemInnerSpacing(6.0F * var1, 5.0F * var1);
      var0.setIndentSpacing(16.0F * var1);
      var0.setScrollbarSize(10.0F * var1);
      var0.setGrabMinSize(10.0F * var1);
      var0.setWindowBorderSize(1.0F);
      var0.setChildBorderSize(1.0F);
      var0.setPopupBorderSize(1.0F);
      var0.setFrameBorderSize(0.0F);
      var0.setWindowRounding(10.0F * var1);
      var0.setChildRounding(8.0F * var1);
      var0.setFrameRounding(5.0F * var1);
      var0.setPopupRounding(6.0F * var1);
      var0.setScrollbarRounding(6.0F * var1);
      var0.setGrabRounding(5.0F * var1);
      var0.setTabRounding(5.0F * var1);
   }

   private static void colour(ImGuiStyle var0, int var1, int var2) {
      var0.setColor(var1, var2);
   }

   static int abgr(int var0, float var1) {
      int var2 = Math.round(var1 * 255.0F);
      return var2 << 24 | (var0 & 0xFF) << 16 | var0 & 0xFF00 | var0 >> 16 & 0xFF;
   }

   static float[] channels(int var0) {
      return new float[]{(var0 & 0xFF) / 255.0F, (var0 >> 8 & 0xFF) / 255.0F, (var0 >> 16 & 0xFF) / 255.0F, (var0 >>> 24) / 255.0F};
   }

   private Theme() {
   }
}
