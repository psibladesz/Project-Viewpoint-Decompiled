package viewpoint.platform;

import imgui.ImDrawList;
import imgui.ImGui;
import java.util.Map;

final class OnboardingLook {
   private static final float STRIP = 0.62222224F;
   private static final float GAP = 28.0F;
   private static final float LEAST_WIDTH = 300.0F;
   private static final float LEAST_HEIGHT = 260.0F;
   private static final float ROUNDING = 12.0F;
   private static final float BORDER = 3.0F;
   private static final float PAD = 18.0F;
   private static final float FADE = 90.0F;
   private static final float BEHIND = 0.75F;
   private static final int HEAVY = Theme.abgr(16758093, 1.0F);
   private static final OnboardingLook.Look[] LOOKS = new OnboardingLook.Look[]{
      new OnboardingLook.Look("default", "Default", "Much heavier: about 1.6 times Vivid's work for the graphics card."),
      new OnboardingLook.Look("vivid", "Vivid", "Lighter: runs well on most cards that can run the Modern mode.")
   };

   static void draw() {
      LoadModel.Values var0 = Onboarding.values();
      if ("Vanilla".equals(var0.get("graphics.mode"))) {
         ImGui.pushStyleColor(0, Theme.ACCENT);
         ImGui.textWrapped("Your preset uses the Vanilla mode, which draws neither look. The look you choose here shows when you turn on the Modern mode.");
         ImGui.popStyleColor();
         ImGui.spacing();
         ImGui.spacing();
      }

      float var1 = OnboardingWindow.scale();
      float var2 = 28.0F * var1;
      float var3 = ImGui.getContentRegionAvailX();
      float var4 = ImGui.getContentRegionAvailY();
      float var5 = Math.max(300.0F * var1, Math.min((var3 - var2) / 2.0F, var4 * 0.62222224F));
      float var6 = Math.min(var5 / 0.62222224F, Math.max(260.0F * var1, var4));
      float var7 = ImGui.getCursorPosX() + Math.max(0.0F, (var3 - 2.0F * var5 - var2) / 2.0F);
      float var8 = ImGui.getCursorPosY();
      String var9 = var0.get("graphics.pack");
      PresetAdvice.Advice var10 = Onboarding.advice();
      String var11 = var10 == null ? null : PresetAdvice.look(var10);

      for (int var12 = 0; var12 < LOOKS.length; var12++) {
         ImGui.setCursorPos(var7 + var12 * (var5 + var2), var8);
         boolean var13 = LOOKS[var12].pack.equals(var9);
         boolean var14 = LOOKS[var12].pack.equals(var11);
         card(LOOKS[var12], var5, var6, var1, var13, var14);
      }
   }

   private static void card(OnboardingLook.Look var0, float var1, float var2, float var3, boolean var4, boolean var5) {
      float var6 = ImGui.getCursorScreenPosX();
      float var7 = ImGui.getCursorScreenPosY();
      float var8 = 18.0F * var3;
      float var9 = 12.0F * var3;
      float var10 = var1 - 2.0F * var8;
      boolean var11 = ImGui.invisibleButton("look-" + var0.pack, var1, var2);
      boolean var12 = ImGui.isItemHovered();
      ImDrawList var13 = ImGui.getWindowDrawList();
      Pictures.Picture var14 = OnboardingArt.look(var0.pack.equals("default"));
      var13.addRectFilled(var6, var7, var6 + var1, var7 + var2, Theme.CARD, var9);
      if (var14 != null) {
         float var15 = 1.0F - var2 / (var1 / 0.62222224F);
         var13.addImageRounded(var14.texture(), var6, var7, var6 + var1, var7 + var2, 0.0F, var15, 1.0F, 1.0F, -1, var9);
      }

      float var19 = ImGui.getStyle().getItemSpacingY();
      Theme.push(OnboardingWindow.titlePixels());
      float var16 = ImGui.getTextLineHeight() + var19;
      Theme.pop();
      var16 += var5 ? ImGui.getTextLineHeight() + var19 : 0.0F;
      var16 += ImGui.calcTextSize(var0.cost, false, var10).y;
      float var17 = var7 + var8 + var16;
      behind(var13, var6, var7, var1, var17 + var8, var17 + var8 + 90.0F * var3, var9);
      ImGui.setCursorScreenPos(var6 + var8, var7 + var8);
      ImGui.beginGroup();
      ImGui.pushTextWrapPos(ImGui.getCursorPosX() + var10);
      OnboardingWindow.title(var0.name);
      if (var5) {
         ImGui.pushStyleColor(0, Theme.ACCENT);
         ImGui.textUnformatted("Recommended for your computer");
         ImGui.popStyleColor();
      }

      ImGui.pushStyleColor(0, var0.pack.equals("default") ? HEAVY : Theme.TEXT);
      ImGui.textWrapped(var0.cost);
      ImGui.popStyleColor();
      ImGui.popTextWrapPos();
      ImGui.endGroup();
      if (var4 || var12) {
         int var18 = var4 ? Theme.ACCENT : Theme.FAINT;
         var13.addRect(var6, var7, var6 + var1, var7 + var2, var18, var9, 0, 3.0F * var3);
      }

      if (var11 && !var4) {
         Onboarding.putAll(Map.of("graphics.pack", var0.pack));
      }
   }

   private static void behind(ImDrawList var0, float var1, float var2, float var3, float var4, float var5, float var6) {
      long var7 = Integer.toUnsignedLong(Theme.abgr(0, 0.0F));
      long var9 = Integer.toUnsignedLong(Theme.abgr(0, 0.75F));
      var0.addRectFilled(var1, var2, var1 + var3, var2 + var6, (int)var9, var6, 48);
      var0.addRectFilledMultiColor(var1, var2 + var6, var1 + var3, var4, var9, var9, var9, var9);
      var0.addRectFilledMultiColor(var1, var4, var1 + var3, var5, var9, var9, var7, var7);
   }

   private OnboardingLook() {
   }

   private record Look(String pack, String name, String cost) {
   }
}
