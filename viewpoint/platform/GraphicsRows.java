package viewpoint.platform;

import imgui.ImDrawList;
import imgui.ImGui;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

final class GraphicsRows {
   static final int PRESET = -1;
   private static final String VANILLA = "Vanilla";
   private static final String MODERN = "Modern";
   private static final String MODE = "graphics.mode";
   private static final double EASY = 0.6;
   private static final double TIGHT = 0.9;
   private static final float[] GREEN = new float[]{0.3F, 0.78F, 0.36F};
   private static final float[] YELLOW = new float[]{0.95F, 0.78F, 0.22F};
   private static final float[] RED = new float[]{0.9F, 0.26F, 0.22F};
   private static final float BAND_PAD = 8.0F;
   private static final float BAND_ROUNDING = 5.0F;
   private static final float OUTLINE = 1.5F;
   private static final float CHEVRON = 0.6F;
   private static final int OUTLINED_FILL = Theme.abgr(0, 0.35F);
   private final float valueWidth;
   private final boolean modeRow;
   private final boolean framed;
   private final Consumer<Map<String, String>> choose;
   private List<OnboardingRows.Row> rows = List.of();
   private String rowsFor = "";
   private int pointed = -1;
   private float tableLeft;
   private float tableRight;

   GraphicsRows(float var1, boolean var2, boolean var3, Consumer<Map<String, String>> var4) {
      this.valueWidth = var1;
      this.modeRow = var2;
      this.framed = var3;
      this.choose = var4;
   }

   void follow(LoadModel.Values var1) {
      boolean var2 = "Vanilla".equalsIgnoreCase(var1.get("graphics.mode"));
      ShaderPack var3 = ShaderPacks.find(var2 ? "normal" : var1.get("graphics.pack"));
      String var4 = (var2 ? "vanilla " : "modern ") + (var3 == null ? "" : var3.id);
      if (!var4.equals(this.rowsFor)) {
         this.rows = OnboardingRows.all(var3, var2).stream().filter(var1x -> this.modeRow || !var1x.label().equals("Shader mode")).toList();
         this.rowsFor = var4;
         this.pointed = Math.min(this.pointed, this.rows.size() - 1);
      }
   }

   List<String> changes(LoadModel.Values var1, LoadModel.Values var2) {
      ArrayList var3 = new ArrayList();
      change(var3, "Graphics mode", var1.get("graphics.mode"), var2.get("graphics.mode"));
      change(var3, "Preset", presetLabel(var1), presetLabel(var2));

      for (OnboardingRows.Row var5 : this.rows) {
         if (!var5.off() && !var5.label().equals("Shader mode") && var5.shown().test(var2)) {
            change(var3, var5.label(), this.level(var5.label(), var1), level(var5, var5.current(var2)));
         }
      }

      return var3;
   }

   private String level(String var1, LoadModel.Values var2) {
      for (OnboardingRows.Row var4 : this.rows) {
         if (var4.label().equals(var1) && !var4.off() && var4.shown().test(var2)) {
            return level(var4, var4.current(var2));
         }
      }

      return "-";
   }

   private static String presetLabel(LoadModel.Values var0) {
      return GraphicsPresets.label(GraphicsPresets.index(var0.get("graphics.preset")));
   }

   private static String level(OnboardingRows.Row var0, int var1) {
      return var1 < 0 ? "Custom" : var0.levels().get(var1).label();
   }

   private static void change(List<String> var0, String var1, String var2, String var3) {
      if (!Objects.equals(var2, var3)) {
         var0.add(var1 + ": " + var2 + " -> " + var3);
      }
   }

   OnboardingRows.Row pointed() {
      return this.pointed != -1 && !this.rows.isEmpty() ? this.rows.get(Math.min(this.pointed, this.rows.size() - 1)) : null;
   }

   void list(LoadModel.Values var1, float var2, float var3) {
      this.tableLeft = ImGui.getCursorScreenPosX();
      this.tableRight = this.tableLeft + (var3 > 0.0F ? var3 : ImGui.getContentRegionAvailX());
      ImGui.pushStyleVar(17, ImGui.getStyle().getCellPaddingX(), ImGui.getStyle().getItemSpacingY() / 2.0F);
      if (this.table("settings", var2, var3) && this.rows(var1, var2, var3)) {
         ImGui.endTable();
      }

      ImGui.popStyleVar();
   }

   private boolean table(String var1, float var2, float var3) {
      short var4 = 24640;
      if (!ImGui.beginTable(var1, 2, var4, var3, 0.0F)) {
         return false;
      }

      ImGui.tableSetupColumn("Setting", 8);
      ImGui.tableSetupColumn("Value", 16, this.valueWidth * var2);
      return true;
   }

   private boolean rows(LoadModel.Values var1, float var2, float var3) {
      if (!this.modeRow) {
         this.mode(var1);
      }

      int var4 = GraphicsPresets.index(var1.get("graphics.preset"));
      this.name(-1, "Preset", false);
      this.picker(presets(), var4 == 5 ? -1 : var4, var1x -> this.choose.accept(OnboardingWindow.presetWrites(var1x)), var2, false);
      ImGui.popID();

      for (int var5 = 0; var5 < this.rows.size(); var5++) {
         OnboardingRows.Row var6 = this.rows.get(var5);
         if (var6.shown().test(var1)) {
            boolean var7 = var6.disabled().test(var1);
            this.name(var5, var6.label(), var6.off() || var7);
            if (var6.off()) {
               ImGui.alignTextToFramePadding();
               ImGui.textDisabled("Modern only");
            } else {
               List var8 = var6.levels().stream().map(OnboardingRows.Level::label).toList();
               ImGui.beginDisabled(var7);
               this.picker(var8, var6.current(var1), var2x -> this.choose.accept(var6.levels().get(var2x).writes()), var2, var7);
               ImGui.endDisabled();
            }

            ImGui.popID();
            NativeNeeds.Need var9 = need(var6, var1);
            if (var9 != null && NativeNotice.shows(var9)) {
               ImGui.endTable();
               NativeNotice.draw(var9, this.tableRight - this.tableLeft);
               if (!this.table("settings after " + var5, var2, var3)) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private static NativeNeeds.Need need(OnboardingRows.Row var0, LoadModel.Values var1) {
      if (!var0.off() && !var0.levels().isEmpty() && !var0.disabled().test(var1)) {
         for (String var3 : var0.levels().get(0).writes().keySet()) {
            NativeNeeds.Need var4 = NativeNeeds.of(var3, var1.get(var3));
            if (var4 != null) {
               return var4;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private void mode(LoadModel.Values var1) {
      boolean var2 = "Vanilla".equalsIgnoreCase(var1.get("graphics.mode"));
      ImGui.tableNextRow(0, ImGui.getFrameHeight() + ImGui.getStyle().getItemSpacingY());
      ImGui.tableNextColumn();
      ImGui.alignTextToFramePadding();
      ImGui.textUnformatted("Graphics mode");
      ImGui.tableNextColumn();
      if (ImGui.radioButton("Vanilla", var2) && !var2) {
         this.choose.accept(Map.of("graphics.mode", "Vanilla"));
      }

      ImGui.sameLine();
      if (ImGui.radioButton("Modern", !var2) && var2) {
         this.choose.accept(Map.of("graphics.mode", "Modern"));
      }
   }

   private void name(int var1, String var2, boolean var3) {
      float var4 = ImGui.getFrameHeight();
      float var5 = ImGui.getStyle().getItemSpacingY();
      ImGui.pushID(var1);
      ImGui.tableNextRow(0, var4 + var5);
      ImGui.tableNextColumn();
      float var6 = ImGui.getCursorScreenPosY() - var5 / 2.0F;
      float var7 = var6 + var4 + var5;
      float var8 = 8.0F * Theme.scale();
      boolean var9 = ImGui.isWindowHovered() && ImGui.isMouseHoveringRect(this.tableLeft, var6, this.tableRight, var7);
      if (this.pointed == var1 || var9) {
         this.band(this.tableLeft - var8, var6, this.tableRight + var8, var7);
      }

      byte var10 = 18;
      ImGui.beginDisabled(var3);
      ImGui.pushStyleVar(24, 0.0F, 0.5F);
      ImGui.pushStyleColor(24, 0);
      ImGui.pushStyleColor(25, 0);
      ImGui.pushStyleColor(26, 0);
      if (ImGui.selectable(var2, false, var10, 0.0F, var4)) {
         this.pointed = var1;
      }

      ImGui.popStyleColor(3);
      ImGui.popStyleVar();
      ImGui.endDisabled();
      if (ImGui.isItemHovered(512)) {
         this.pointed = var1;
      }

      ImGui.tableNextColumn();
   }

   private void band(float var1, float var2, float var3, float var4) {
      float var5 = ImGui.getWindowPosY();
      float var6 = var5 + ImGui.getWindowHeight();
      float var7 = 5.0F * Theme.scale();
      ImGui.pushClipRect(var1, Math.max(var2, var5), var3, Math.min(var4, var6), false);
      ImDrawList var8 = ImGui.getWindowDrawList();
      if (this.framed) {
         var8.addRectFilled(var1, var2, var3, var4, OUTLINED_FILL, var7);
         var8.addRect(var1 + 1.0F, var2 + 1.0F, var3 - 1.0F, var4 - 1.0F, Theme.ACCENT, var7, 0, 1.5F * Theme.scale());
      } else {
         var8.addRectFilled(var1, var2, var3, var4, Theme.ACCENT_SOFT, var7);
      }

      ImGui.popClipRect();
   }

   private static List<String> presets() {
      return List.of(Arrays.copyOf(GraphicsPresets.LABELS, 5));
   }

   private void picker(List<String> var1, int var2, IntConsumer var3, float var4, boolean var5) {
      int var6 = var1.size() - 1;
      int var7 = var6 / 2;
      if (arrow("less", Icons.Icon.LESS, !var5 && var2 != 0)) {
         var3.accept(var2 < 0 ? var7 : var2 - 1);
      }

      ImGui.sameLine();
      String var8 = var2 < 0 ? "Custom" : (String)var1.get(var2);
      float var9 = ImGui.getFrameHeight();
      float var10 = this.valueWidth * var4 - 2.0F * (var9 + ImGui.getStyle().getItemSpacingX());
      float var11 = ImGui.calcTextSize(var8).x;
      ImGui.setCursorPosX(ImGui.getCursorPosX() + Math.max(0.0F, (var10 - var11) / 2.0F));
      ImGui.alignTextToFramePadding();
      ImGui.textUnformatted(var8);
      ImGui.sameLine(this.valueWidth * var4 - var9 + ImGui.getStyle().getCellPaddingX());
      if (arrow("more", Icons.Icon.MORE, !var5 && var2 != var6)) {
         var3.accept(var2 < 0 ? var7 : var2 + 1);
      }
   }

   private static boolean arrow(String var0, Icons.Icon var1, boolean var2) {
      float var3 = ImGui.getFrameHeight();
      float var4 = ImGui.getCursorScreenPosX();
      float var5 = ImGui.getCursorScreenPosY();
      boolean var6 = ImGui.invisibleButton(var0, var3, var3) && var2;
      boolean var7 = var2 && ImGui.isItemHovered();
      ImDrawList var8 = ImGui.getWindowDrawList();
      var8.addRectFilled(var4, var5, var4 + var3, var5 + var3, var7 ? Theme.FRAME_HOVER : Theme.FRAME, ImGui.getStyle().getFrameRounding());
      float var9 = var3 * 0.6F;
      int var10 = var2 ? Theme.ACCENT : Theme.TEXT_FAINT;
      Icons.draw(var8, var1, var4 + (var3 - var9) / 2.0F, var5 + (var3 - var9) / 2.0F, var9, var10);
      return var6;
   }

   static String weight(double var0) {
      return var0 <= 0.6 ? "Light" : (var0 <= 0.9 ? "Moderate" : (var0 <= 1.0 ? "Heavy" : "Too heavy"));
   }

   static float[] colour(double var0) {
      if (var0 <= 0.6) {
         return (float[])GREEN.clone();
      }

      boolean var2 = var0 <= 0.9;
      float var3 = (float)Math.min(1.0, var2 ? (var0 - 0.6) / 0.30000000000000004 : (var0 - 0.9) / 0.09999999999999998 / 2.0);
      float[] var4 = var2 ? GREEN : YELLOW;
      float[] var5 = var2 ? YELLOW : RED;
      float[] var6 = new float[3];

      for (int var7 = 0; var7 < 3; var7++) {
         var6[var7] = var4[var7] + (var5[var7] - var4[var7]) * var3;
      }

      return var6;
   }

   static double stress(LoadModel.Values var0, Hardware var1) {
      LoadModel.Load var2 = LoadModel.estimate(var0, var1);
      LoadModel.Load var3 = LoadModel.budget(var1);
      double var4 = 0.0;

      for (LoadModel.Part var9 : LoadModel.Part.values()) {
         var4 = Math.max(var4, LoadModel.stress(var2, var3, var9, var0, var1));
      }

      return var4;
   }
}
