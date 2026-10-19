package viewpoint.platform;

import imgui.ImDrawList;
import imgui.ImGui;

final class OnboardingFrame {
   static final float MARGIN = 44.0F;
   static final float DOT = 5.0F;
   static final float DOT_GAP = 24.0F;
   static final float CLOSE = 22.0F;
   static final float BUTTON_HEIGHT = 62.0F;
   private static final float NEXT_WIDTH = 250.0F;
   private static final float BACK_WIDTH = 160.0F;
   private static final float ICON = 22.0F;
   private static final float ROUNDING = 10.0F;
   private static final float LABEL = 21.0F;
   private static final float SKY = 0.15F;
   private static final float FOOT = 0.92F;
   private static final float BEHIND = 0.86F;
   private static final float WASH_FROM = 0.35F;
   private static final float STEP_SKY = 0.3F;
   private static final float STEP_WASH_FROM = 0.1F;
   private static final float STEP_WASH_TO = 0.3F;
   private static final float CARD_ROUNDING = 12.0F;
   private static final float CARD_PAD = 18.0F;
   private static final float CORNER_LOGO = 0.08F;
   private static final float CORNER_TOP = 0.4F;
   private static final int WHITE_FAINT = Theme.abgr(16777215, 0.3F);
   private static final int DONE = Theme.abgr(16345616, 0.45F);
   private static final int GLASS = Theme.abgr(1052946, 0.82F);
   private static final int GLASS_HOVER = Theme.abgr(2763823, 0.9F);
   private static final int GLASS_EDGE = Theme.abgr(16777215, 0.08F);

   static void backdrop(float var0, float var1, float var2, float var3, boolean var4) {
      ImDrawList var5 = ImGui.getWindowDrawList();
      float var6 = ImGui.getStyle().getWindowRounding();
      Pictures.Picture var7 = OnboardingArt.banner();
      var5.addRectFilled(var0, var1, var0 + var2, var1 + var3, Theme.WINDOW, var6);
      if (var7 != null) {
         float var8 = Math.min(1.0F, var2 / var3 / var7.aspect());
         float var9 = Math.min(1.0F, var7.aspect() / (var2 / var3));
         float var10 = (1.0F - var8) / 2.0F;
         float var11 = (1.0F - var9) / 2.0F;
         var5.addImageRounded(var7.texture(), var0, var1, var0 + var2, var1 + var3, var10, var11, 1.0F - var10, 1.0F - var11, -1, var6);
      }

      float var14 = var1 + var3 * (var4 ? 0.35F : 0.1F);
      float var15 = var4 ? var1 + var3 - var6 : var1 + var3 * 0.3F;
      long var16 = Integer.toUnsignedLong(Theme.abgr(0, var4 ? 0.15F : 0.3F));
      long var12 = Integer.toUnsignedLong(Theme.abgr(0, var4 ? 0.92F : 0.86F));
      var5.addRectFilled(var0, var1, var0 + var2, var1 + var6, (int)var16, var6, 48);
      var5.addRectFilledMultiColor(var0, var1 + var6, var0 + var2, var14, var16, var16, var16, var16);
      var5.addRectFilledMultiColor(var0, var14, var0 + var2, var15, var16, var16, var12, var12);
      var5.addRectFilledMultiColor(var0, var15, var0 + var2, var1 + var3 - var6, var12, var12, var12, var12);
      var5.addRectFilled(var0, var1 + var3 - var6, var0 + var2, var1 + var3, (int)var12, var6, 192);
   }

   static float cornerLogo(float var0, float var1, float var2) {
      float var3 = Theme.scale();
      float var4 = var2 * 0.08F;
      float var5 = var0 + 44.0F * var3;
      float var6 = var1 + 44.0F * var3 * 0.4F;
      Pictures.Picture var7 = OnboardingArt.logo();
      float var8 = var7 == null ? var4 / 2.2F : var4 / var7.aspect();
      if (var7 != null) {
         ImGui.getWindowDrawList().addImage(var7.texture(), var5, var6, var5 + var4, var6 + var8);
      }

      return var6 + var8;
   }

   static void beginCard(String var0, float var1, float var2) {
      float var3 = Theme.scale();
      ImGui.pushStyleColor(3, GLASS);
      ImGui.pushStyleColor(5, GLASS_EDGE);
      ImGui.pushStyleVar(7, 12.0F * var3);
      ImGui.pushStyleVar(8, 1.0F);
      ImGui.pushStyleVar(2, 18.0F * var3, 18.0F * var3);
      ImGui.beginChild(var0, var1, var2, true, 65536);
      ImGui.popStyleVar(3);
      ImGui.popStyleColor(2);
   }

   static void endCard() {
      ImGui.endChild();
   }

   static void logo(float var0, float var1, float var2, float var3, float var4, float var5) {
      Pictures.Picture var6 = OnboardingArt.logo();
      if (var6 != null) {
         float var7 = var2 * var4;
         float var8 = var7 / var6.aspect();
         float var9 = var0 + (var2 - var7) / 2.0F;
         float var10 = var1 + var3 * var5 - var8 / 2.0F;
         ImGui.getWindowDrawList().addImage(var6.texture(), var9, var10, var9 + var7, var10 + var8);
      }
   }

   static boolean top(float var0, float var1, float var2, int var3, int var4) {
      float var5 = Theme.scale();
      float var6 = 22.0F * var5;
      float var7 = var0 + var2 - 44.0F * var5;
      float var8 = var1 + 44.0F * var5;
      float var9 = 24.0F * var5;
      float var10 = var7 - var6 - var9 * (var4 + 0.5F);
      ImDrawList var11 = ImGui.getWindowDrawList();

      for (int var12 = 0; var12 < var4; var12++) {
         int var13 = var12 == var3 ? Theme.ACCENT : (var12 < var3 ? DONE : WHITE_FAINT);
         var11.addCircleFilled(var10 + var9 * var12, var8, 5.0F * var5, var13, 16);
      }

      ImGui.setCursorScreenPos(var7 - var6, var8 - var6 / 2.0F);
      return Icons.button("##close setup", Icons.Icon.CLOSE, var6, "Close the setup: it shows again at the next load.");
   }

   static void title(String var0, String var1, float var2) {
      ImGui.beginGroup();
      Theme.push(var2);
      ImGui.textUnformatted(var0);
      if (!var1.isEmpty()) {
         ImGui.sameLine(0.0F, 0.0F);
         ImGui.pushStyleColor(0, Theme.ACCENT);
         ImGui.textUnformatted(var1);
         ImGui.popStyleColor();
      }

      Theme.pop();
      ImGui.endGroup();
   }

   static boolean next(String var0, float var1, float var2, boolean var3, String var4) {
      float var5 = Theme.scale();
      float var6 = 250.0F * var5;
      float var7 = 62.0F * var5;
      int var8 = var3 ? Theme.abgr(16345616, 0.35F) : Theme.ACCENT;
      return button("##next", var0, Icons.Icon.NEXT, var1 - var6, var2 - var7, var6, var7, var8, Theme.ACCENT_HOVER, var3, var4);
   }

   static boolean back(float var0, float var1) {
      float var2 = Theme.scale();
      float var3 = 160.0F * var2;
      float var4 = 62.0F * var2;
      boolean var5 = button("##back", "Back", Icons.Icon.BACK, var0, var1 - var4, var3, var4, GLASS, GLASS_HOVER, false, "");
      ImGui.getWindowDrawList().addRect(var0, var1 - var4, var0 + var3, var1, GLASS_EDGE, 10.0F * var2);
      return var5;
   }

   private static boolean button(
      String var0, String var1, Icons.Icon var2, float var3, float var4, float var5, float var6, int var7, int var8, boolean var9, String var10
   ) {
      float var11 = Theme.scale();
      float var12 = 22.0F * var11;
      ImGui.setCursorScreenPos(var3, var4);
      boolean var13 = ImGui.invisibleButton(var0, var5, var6) && !var9;
      boolean var14 = !var9 && ImGui.isItemHovered();
      ImDrawList var15 = ImGui.getWindowDrawList();
      var15.addRectFilled(var3, var4, var3 + var5, var4 + var6, var14 ? var8 : var7, 10.0F * var11);
      Theme.push(21.0F);
      float var16 = ImGui.calcTextSize(var1).x;
      float var17 = 12.0F * var11;
      float var18 = var3 + (var5 - var12 - var17 - var16) / 2.0F;
      int var19 = var9 ? Theme.abgr(16777215, 0.5F) : Theme.abgr(16777215, 1.0F);
      Icons.draw(var15, var2, var18, var4 + (var6 - var12) / 2.0F, var12, var19);
      var15.addText(var18 + var12 + var17, var4 + (var6 - ImGui.getTextLineHeight()) / 2.0F, var19, var1);
      Theme.pop();
      if (var9 && !var10.isEmpty() && ImGui.isItemHovered(512)) {
         ImGui.setTooltip(var10);
      }

      return var13;
   }

   private OnboardingFrame() {
   }
}
