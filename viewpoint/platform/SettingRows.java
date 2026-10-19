package viewpoint.platform;

import imgui.ImGui;
import java.util.List;
import java.util.Locale;

final class SettingRows {
   private static final float KEY_WIDTH = 170.0F;
   private static final float COMBO_WIDTH = 210.0F;
   static final float ICON = 15.0F;
   static final float GAP = 7.0F;
   static final float MOST = 0.5F;

   static boolean edit(LiveSettings.Setting var0, float var1) {
      if (!var0.shown()) {
         return false;
      }

      ImGui.pushID(var0.key);
      ImGui.beginDisabled(!var0.enabled());
      boolean var2;
      if (var0 instanceof KeyBind var3) {
         var2 = keyRow(var3, var1);
      } else if (var0 instanceof PadBind var4) {
         BindingRows.pad(var4, var1);
         var2 = false;
      } else if (var0 instanceof LiveSettings.Toggle var5) {
         var2 = toggleRow(var5, var1);
      } else if (var0 instanceof LiveSettings.Number var6) {
         var2 = numberRow(var6, var1);
      } else if (var0 instanceof LiveSettings.Choice var7) {
         var2 = var7.slider() ? stepsRow(var7, var1) : choiceRow(var7, var1);
      } else {
         var2 = false;
      }

      ImGui.endDisabled();
      ImGui.popID();
      NativeNeeds.Need var8 = var0.enabled() ? NativeNeeds.of(var0.key, var0.text()) : null;
      if (var8 != null && NativeNotice.shows(var8)) {
         NativeNotice.draw(var8, var1);
      }

      return var2;
   }

   private static boolean toggleRow(LiveSettings.Toggle var0, float var1) {
      float var2 = ImGui.getCursorScreenPosX();
      float var3 = ImGui.getCursorScreenPosY();
      boolean var4 = var0.get();
      ImGui.pushStyleColor(7, var4 ? Theme.ACCENT : Theme.FRAME);
      ImGui.pushStyleColor(8, var4 ? Theme.ACCENT_HOVER : Theme.FRAME_HOVER);
      ImGui.pushStyleColor(9, Theme.ACCENT_PRESSED);
      ImGui.pushStyleColor(18, Theme.KNOB);
      boolean var5 = ImGui.checkbox(var0.label, var4);
      ImGui.popStyleColor(4);
      tooltip(var0);
      if (var5) {
         var0.set(!var4);
      }

      float var6 = var3 + (ImGui.getFrameHeight() - 15.0F * Theme.scale()) / 2.0F;
      return var5 | reset(var0, var2 + var1, var6, var0.get() != var0.fallback);
   }

   private static boolean numberRow(LiveSettings.Number var0, float var1) {
      String var2 = String.format(Locale.ROOT, var0.step >= 1.0F ? "%.0f" : "%.2f", var0.get());
      boolean var3 = nameAndValue(var0, var1, var2, !var0.matches(Float.toString(var0.fallback)));
      double var4 = var0.max > var0.min ? (var0.get() - var0.min) / (var0.max - var0.min) : 0.0;
      return var3 | Widgets.slider("##slider", var4, var1, var1x -> var0.set((float)(var0.min + var1x * (var0.max - var0.min))));
   }

   private static boolean stepsRow(LiveSettings.Choice var0, float var1) {
      int var2 = Math.max(1, var0.options.length - 1);
      boolean var3 = nameAndValue(var0, var1, var0.labels()[var0.get()], var0.get() != var0.fallback);
      return var3 | Widgets.slider("##slider", (double)var0.get() / var2, var1, var2x -> var0.set((int)Math.round(var2x * var2)));
   }

   private static boolean nameAndValue(LiveSettings.Setting var0, float var1, String var2, boolean var3) {
      float var4 = ImGui.getCursorScreenPosX();
      float var5 = ImGui.getCursorScreenPosY();
      ImGui.textUnformatted(var0.label);
      tooltip(var0);
      float var6 = ImGui.calcTextSize(var2).x;
      boolean var7 = reset(var0, var4 + var1 - var6 - 7.0F * Theme.scale(), var5, var3);
      ImGui.sameLine();
      ImGui.setCursorScreenPos(var4 + var1 - var6, var5);
      ImGui.pushStyleColor(0, Theme.TEXT_DIM);
      ImGui.textUnformatted(var2);
      ImGui.popStyleColor();
      return var7;
   }

   private static boolean choiceRow(LiveSettings.Choice var0, float var1) {
      float var2 = ImGui.getCursorScreenPosX();
      float var3 = ImGui.getCursorScreenPosY();
      float var4 = Math.min(210.0F * Theme.scale(), var1 * 0.5F);
      ImGui.alignTextToFramePadding();
      ImGui.textUnformatted(var0.label);
      tooltip(var0);
      float var5 = var3 + (ImGui.getFrameHeight() - 15.0F * Theme.scale()) / 2.0F;
      boolean var6 = reset(var0, var2 + var1 - var4 - 7.0F * Theme.scale(), var5, var0.get() != var0.fallback);
      ImGui.sameLine();
      ImGui.setCursorScreenPos(var2 + var1 - var4, var3);
      ImGui.setNextItemWidth(var4);
      if (ImGui.beginCombo("##choice", var0.labels()[var0.get()])) {
         for (int var7 = 0; var7 < var0.options.length; var7++) {
            if (ImGui.selectable(var0.labels()[var7], var7 == var0.get())) {
               var0.set(var7);
               var6 = true;
            }
         }

         ImGui.endCombo();
      }

      return var6;
   }

   private static boolean keyRow(KeyBind var0, float var1) {
      float var2 = Theme.scale();
      float var3 = 15.0F * var2;
      float var4 = 7.0F * var2;
      float var5 = ImGui.getCursorScreenPosX();
      float var6 = ImGui.getCursorScreenPosY();
      float var7 = Math.min(170.0F * var2, var1 * 0.5F);
      float var8 = 3.0F * var3 + 2.0F * var4;
      ImGui.beginDisabled(!var0.usable());
      ImGui.alignTextToFramePadding();
      ImGui.textUnformatted(var0.label);
      tooltip(var0);
      boolean var9 = KeyInput.capturing() == var0;
      boolean var10 = false;
      ImGui.sameLine();
      ImGui.setCursorScreenPos(var5 + var1 - var8 - 2.0F * var4 - var7, var6);
      ImGui.pushStyleColor(21, var9 ? Theme.ACCENT_SOFT : Theme.FRAME);
      ImGui.pushStyleColor(0, var9 ? Theme.ACCENT : Theme.TEXT);
      if (ImGui.button((var9 ? "Press a key" : (var0.bound() ? var0.windowText() : "None")) + "##keys", var7, 0.0F)) {
         KeyInput.capture(var0, false);
      }

      ImGui.popStyleColor(2);
      if (ImGui.isItemHovered()) {
         ImGui.setTooltip(
            "Click, then press the key, with any it needs held (Shift+O); Esc leaves it as it was. A mouse button can be a key too, but not the left or the right one. Names follow your keyboard layout."
         );
      }

      float var11 = var6 + (ImGui.getFrameHeight() - var3) / 2.0F;
      ImGui.sameLine();
      ImGui.setCursorScreenPos(var5 + var1 - var8, var11);
      if (Icons.button("##add", Icons.Icon.ADD, var3, "Another key for it, besides those it has.")) {
         KeyInput.capture(var0, true);
      }

      ImGui.sameLine(0.0F, var4);
      if (Icons.button("##default", Icons.Icon.RESET, var3, "Back to its default keys.")) {
         Keys.choose(var0, var0.fallback);
         var10 = true;
      }

      ImGui.sameLine(0.0F, var4);
      if (Icons.button("##clear", Icons.Icon.CLEAR, var3, "No key for it.")) {
         Keys.choose(var0, List.of());
         var10 = true;
      }

      ImGui.endDisabled();
      return var10;
   }

   private static boolean reset(LiveSettings.Setting var0, float var1, float var2, boolean var3) {
      if (!var3) {
         return false;
      }

      float var4 = 15.0F * Theme.scale();
      ImGui.sameLine();
      ImGui.setCursorScreenPos(var1 - var4, var2);
      if (!Icons.button("##reset", Icons.Icon.RESET, var4, "Back to its default: " + fallback(var0))) {
         return false;
      }

      LiveSettings.toDefault(var0);
      return true;
   }

   private static String fallback(LiveSettings.Setting var0) {
      if (var0 instanceof LiveSettings.Number var3) {
         return var3.step >= 1.0F ? Integer.toString(Math.round(var3.fallback)) : Float.toString(var3.fallback);
      } else if (var0 instanceof LiveSettings.Toggle var2) {
         return var2.fallback ? "on" : "off";
      } else {
         return var0 instanceof LiveSettings.Choice var1 ? var1.labels()[var1.fallback] : "";
      }
   }

   private static void tooltip(LiveSettings.Setting var0) {
      if (ImGui.isItemHovered(512)) {
         String var1 = var0.tooltip();
         String var2 = (var1.isEmpty() ? "" : var1 + "\n") + var0.key + " (viewpoint-live.properties)";
         ImGui.setTooltip(var2.replace("%", "%%"));
      }
   }

   private SettingRows() {
   }
}
