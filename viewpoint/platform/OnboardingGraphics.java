package viewpoint.platform;

import imgui.ImDrawList;
import imgui.ImGui;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class OnboardingGraphics {
   private static final float WIDE = 760.0F;
   private static final float VALUE_WIDTH = 200.0F;
   private static final float ROWS_SHARE = 0.56F;
   private static final float SWATCH = 5.0F;
   private static final float BAR_HEIGHT = 0.6F;
   private static final HeldChanges held = new HeldChanges();
   private static final GraphicsRows rows = new GraphicsRows(200.0F, true, true, held::put);

   static void draw() {
      LoadModel.Values var0 = Onboarding.values();
      LoadModel.Values var1 = held.over(var0);
      rows.follow(var1);
      float var2 = OnboardingWindow.scale();
      float var3 = ImGui.getContentRegionAvailX();
      if (var3 >= 760.0F * var2) {
         OnboardingFrame.beginCard("rows", var3 * 0.56F, 0.0F);
         rows.list(var1, var2, 0.0F);
         OnboardingFrame.endCard();
         ImGui.sameLine();
         OnboardingFrame.beginCard("panel", 0.0F, 0.0F);
         apply(var0, var1);
         panel(var1);
         OnboardingFrame.endCard();
      } else {
         OnboardingFrame.beginCard("rows and panel", 0.0F, 0.0F);
         rows.list(var1, var2, 0.0F);
         ImGui.separator();
         apply(var0, var1);
         panel(var1);
         OnboardingFrame.endCard();
      }

      held.track();
   }

   static void leave(Runnable var0) {
      held.leave(var0);
   }

   static void confirm() {
      held.confirm();
   }

   private static void apply(LoadModel.Values var0, LoadModel.Values var1) {
      List<String> var2 = held.any() ? rows.changes(var0, var1) : List.of();
      held.buttons(var2.size());

      for (String var4 : var2) {
         ImGui.bullet();
         ImGui.sameLine();
         ImGui.textWrapped(var4.replace("%", "%%"));
      }

      ImGui.spacing();
      ImGui.separator();
      ImGui.spacing();
   }

   private static void panel(LoadModel.Values var0) {
      Hardware var1 = Onboarding.hardware();
      OnboardingRows.Row var2 = rows.pointed();
      if (var2 == null) {
         preset(var0, var1);
      } else {
         OnboardingWindow.title(var2.label());
         ImGui.textWrapped(var2.description());
         ImGui.spacing();
         OnboardingRows.Level var3 = var2.levels().isEmpty() ? null : var2.levels().get(0);
         LoadModel.Values var4 = var3 == null ? null : var2x -> var3.writes().getOrDefault(var2x, var0.get(var2x));
         parts(var2.parts(), var4, var0, var1, "at this level");
      }

      ImGui.spacing();
      ImGui.separator();
      ImGui.textUnformatted("Estimated load:");
      if (var1 == null) {
         ImGui.textDisabled("Check your hardware on the step before to see it.");
      } else {
         LoadBars.load(var0, var1, 0.6F, 0.0F);
         ImGui.spacing();
         ImGui.textWrapped(String.format(Locale.ROOT, "Estimated for %d x %d from our measurements on Ryzen 5 5600 / RTX 3060.", var1.width(), var1.height()));
      }
   }

   private static void preset(LoadModel.Values var0, Hardware var1) {
      OnboardingWindow.title("Preset");
      ImGui.textWrapped("Sets every setting below at once, and changing any of them makes it Custom.");
      ImGui.spacing();
      ImGui.textWrapped(OnboardingWindow.presetText(GraphicsPresets.current()));
      PresetAdvice.Advice var2 = Onboarding.advice();
      if (var2 != null) {
         float[] var3 = var1 == null ? Theme.channels(Theme.ACCENT) : GraphicsRows.colour(GraphicsRows.stress(var0, var1));
         ImGui.textColored(var3[0], var3[1], var3[2], 1.0F, "Recommended: " + GraphicsPresets.label(var2.preset()));
      }

      ImGui.spacing();
      LoadModel.Values var5 = PresetAdvice.values(0);
      LoadModel.Values var4 = var2x -> var5.get(var2x) != null ? var5.get(var2x) : var0.get(var2x);
      parts(EnumSet.allOf(LoadModel.Part.class), var4, var0, var1, "over " + GraphicsPresets.label(0));
   }

   private static void parts(Set<LoadModel.Part> var0, LoadModel.Values var1, LoadModel.Values var2, Hardware var3, String var4) {
      ImDrawList var5 = ImGui.getWindowDrawList();
      float var6 = 5.0F * OnboardingWindow.scale();
      LoadModel.Load var7 = null;
      LoadModel.Load var8 = null;
      if (var3 != null && var1 != null) {
         var7 = LoadModel.estimate(var2, var3);
         var8 = LoadModel.estimate(var1, var3);
      }

      for (LoadModel.Part var12 : LoadModel.Part.values()) {
         boolean var13 = var0.contains(var12);
         float var14 = ImGui.getCursorScreenPosX() + var6;
         float var15 = ImGui.getCursorScreenPosY() + ImGui.getTextLineHeight() / 2.0F;
         var5.addRectFilled(var14 - var6, var15 - var6, var14 + var6, var15 + var6, var13 ? OnboardingWindow.ACCENT : OnboardingWindow.FAINT);
         ImGui.dummy(var6 * 2.0F, ImGui.getTextLineHeight());
         ImGui.sameLine();
         String var16 = var7 != null && var13 ? added(var12, var7.of(var12) - var8.of(var12), var4) : "";
         if (var13) {
            ImGui.textUnformatted(var12.label + var16);
         } else {
            ImGui.textDisabled(var12.label);
         }
      }
   }

   private static String added(LoadModel.Part var0, double var1, String var3) {
      if (Math.abs(var1) < 0.05) {
         return "";
      } else {
         return var0 != LoadModel.Part.CPU && var0 != LoadModel.Part.GPU
            ? String.format(Locale.ROOT, ": %+.0f MB %s", var1, var3)
            : (var1 > 0.0 ? ": heavier " : ": lighter ") + var3;
      }
   }

   private OnboardingGraphics() {
   }
}
