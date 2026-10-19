package viewpoint.platform;

import imgui.ImGui;
import imgui.type.ImBoolean;
import java.util.List;

final class BindingRows {
   private static final float KEY_WIDTH = 190.0F;
   private static final float GLYPH = 20.0F;
   private static final int GLYPHS = 2;
   private static final String AIMING = "While aiming";
   private static final float WARNING_R = 1.0F;
   private static final float WARNING_G = 0.75F;
   private static final float WARNING_B = 0.25F;
   private static final ImBoolean aiming = new ImBoolean();

   static void pad(PadBind var0, float var1) {
      float var2 = Theme.scale();
      float var3 = 15.0F * var2;
      float var4 = 7.0F * var2;
      float var5 = 20.0F * var2;
      float var6 = ImGui.getCursorScreenPosX();
      float var7 = ImGui.getCursorScreenPosY();
      boolean var8 = PadInput.capturing() == var0;
      PadChord var9 = var0.chord();
      String var10 = var8 ? "Press a pad button (click: as it was)" : (var9 == null ? "None" : var9.display(PadGlyphs.style()));
      float var11 = ImGui.getStyle().getItemSpacingX();
      float var12 = Math.min(190.0F * var2, var1 * 0.5F);
      float var13 = 2.0F * var3 + var4;
      float var14 = 2.0F * (var5 + var11);
      float var15 = ImGui.getFrameHeight() + ImGui.getStyle().getItemInnerSpacingX() + ImGui.calcTextSize("While aiming").x;
      ImGui.alignTextToFramePadding();
      ImGui.textUnformatted(var0.label);
      tooltip(var0);
      ImGui.sameLine();
      ImGui.setCursorScreenPos(var6 + var1 - var13 - var4 - var14 - var12 - var11 - var15, var7);
      aimingBox(var0, var9);
      ImGui.sameLine();
      if (ImGui.button(var10 + "##" + var0.key, var12, 0.0F)) {
         if (var8) {
            PadInput.cancel();
         } else {
            PadInput.capture(var0, true);
         }
      }

      tooltip(var0);
      glyphs(var9, var5);
      ImGui.sameLine();
      ImGui.setCursorScreenPos(var6 + var1 - var13, var7 + (ImGui.getFrameHeight() - var3) / 2.0F);
      if (Icons.button("##default" + var0.key, Icons.Icon.RESET, var3, "Back to its default button.")) {
         PadBinds.choose(var0, var0.fallback);
         SettingsWindow.edited();
      }

      ImGui.sameLine(0.0F, var4);
      if (Icons.button("##clear" + var0.key, Icons.Icon.CLEAR, var3, "No button for it.")) {
         PadBinds.choose(var0, null);
         SettingsWindow.edited();
      }

      List var16 = PadBinds.warnings(var0);
      if (!var16.isEmpty()) {
         ImGui.textColored(1.0F, 0.75F, 0.25F, 1.0F, "    Also: " + String.join(", ", var16));
      }
   }

   private static void tooltip(PadBind var0) {
      if (ImGui.isItemHovered()) {
         String var1 = var0.tooltip();
         ImGui.setTooltip(
            (var1.isEmpty() ? "" : var1 + "\n")
               + "Click its button, then press a button of the pad you play with, with any others it needs held (LB + Y). The game still sees the buttons: the warning under the row names what else they do.\n"
               + var0.key
               + " (viewpoint-live.properties)"
         );
      }
   }

   private static void glyphs(PadChord var0, float var1) {
      if (var0 != null) {
         for (int var3 : var0.buttons()) {
            ImGui.sameLine();
            PadGlyphs.draw(var3, var1);
         }
      }
   }

   private static void aimingBox(PadBind var0, PadChord var1) {
      boolean var2 = var1 == null || var0.hold;
      aiming.set(var1 != null && var1.aiming());
      if (var2) {
         ImGui.beginDisabled();
      }

      if (ImGui.checkbox("While aiming##" + var0.key, aiming) && !var2) {
         PadBinds.choose(var0, var1.withAiming(aiming.get()));
         SettingsWindow.edited();
      }

      if (var2) {
         ImGui.endDisabled();
      } else if (ImGui.isItemHovered()) {
         ImGui.setTooltip(
            "Only while aiming: the game ends any sprint while aiming, so R3 is free then, unless the game's Toggle to sprint is on (R3 still flips the sprint)."
         );
      }
   }

   private BindingRows() {
   }
}
