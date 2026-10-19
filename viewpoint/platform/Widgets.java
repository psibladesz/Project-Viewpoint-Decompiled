package viewpoint.platform;

import imgui.ImDrawList;
import imgui.ImGui;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleConsumer;

final class Widgets {
   private static final float PAD = 14.0F;
   private static final float ROUNDING = 8.0F;
   private static final float SLIDER = 16.0F;
   private static final float TRACK = 4.0F;
   private static final float KNOB = 6.5F;
   private static final float LINE = 2.0F;
   private static final Map<String, Float> heights = new HashMap<>();
   private static final Deque<Widgets.Card> open = new ArrayDeque<>();

   static void beginCard(String var0, String var1, float var2) {
      float var3 = Theme.scale();
      float var4 = 14.0F * var3;
      float var5 = ImGui.getCursorScreenPosX();
      float var6 = ImGui.getCursorScreenPosY();
      float var7 = heights.getOrDefault(var0, 0.0F);
      ImDrawList var8 = ImGui.getWindowDrawList();
      var8.addRectFilled(var5, var6, var5 + var2, var6 + var7, Theme.CARD, 8.0F * var3);
      var8.addRect(var5, var6, var5 + var2, var6 + var7, Theme.BORDER, 8.0F * var3);
      open.push(new Widgets.Card(var0, var5, var6, var2));
      ImGui.beginGroup();
      ImGui.dummy(var2, var4 - ImGui.getStyle().getItemSpacingY());
      ImGui.indent(var4);
      ImGui.pushTextWrapPos(ImGui.getCursorPosX() + var2 - 2.0F * var4);
      if (!var1.isEmpty()) {
         caption(var1);
      }
   }

   static void endCard() {
      Widgets.Card var0 = open.pop();
      float var1 = 14.0F * Theme.scale();
      ImGui.popTextWrapPos();
      ImGui.unindent(var1);
      ImGui.dummy(var0.width(), var1 - ImGui.getStyle().getItemSpacingY());
      ImGui.endGroup();
      heights.put(var0.id(), ImGui.getItemRectMaxY() - var0.y());
   }

   static boolean accentButton(String var0) {
      ImGui.pushStyleColor(21, Theme.ACCENT);
      ImGui.pushStyleColor(22, Theme.ACCENT_HOVER);
      ImGui.pushStyleColor(23, Theme.ACCENT_PRESSED);
      ImGui.pushStyleColor(0, Theme.ON_ACCENT);
      boolean var1 = ImGui.button(var0);
      ImGui.popStyleColor(4);
      return var1;
   }

   static float height(String var0) {
      return heights.getOrDefault(var0, 0.0F);
   }

   static float inner() {
      Widgets.Card var0 = open.peek();
      return var0 == null ? ImGui.getContentRegionAvailX() : var0.width() - 28.0F * Theme.scale();
   }

   static void rule() {
      float var0 = ImGui.getCursorScreenPosX();
      float var1 = ImGui.getCursorScreenPosY();
      float var2 = inner();
      ImGui.getWindowDrawList().addRectFilled(var0, var1, var0 + var2, var1 + 1.0F, Theme.BORDER);
      ImGui.dummy(var2, 1.0F);
   }

   static void caption(String var0) {
      Theme.push(12.5F);
      ImGui.pushStyleColor(0, Theme.TEXT_DIM);
      ImGui.textUnformatted(var0.toUpperCase(Locale.ROOT));
      ImGui.popStyleColor();
      Theme.pop();
   }

   static int pages(String var0, String[] var1, int var2) {
      float var3 = Theme.scale();
      float var4 = 18.0F * var3;
      float var5 = ImGui.getFrameHeight();
      float var6 = ImGui.getCursorScreenPosX();
      float var7 = ImGui.getContentRegionAvailX();
      float var8 = var6;
      ImDrawList var9 = ImGui.getWindowDrawList();
      int var10 = var2;

      for (int var11 = 0; var11 < var1.length; var11++) {
         float var12 = ImGui.calcTextSize(var1[var11]).x;
         boolean var13 = var8 + var4 + var12 <= var6 + var7;
         if (var11 > 0 && var13) {
            ImGui.sameLine(0.0F, var4);
         }

         float var14 = ImGui.getCursorScreenPosX();
         float var15 = ImGui.getCursorScreenPosY();
         if (var11 == 0 || !var13) {
            var9.addRectFilled(var6, var15 + var5 - 1.0F, var6 + var7, var15 + var5, Theme.BORDER);
         }

         if (ImGui.invisibleButton(var0 + var11, var12, var5)) {
            var10 = var11;
         }

         boolean var16 = var11 == var2;
         int var17 = var16 ? Theme.ACCENT : (ImGui.isItemHovered() ? Theme.TEXT : Theme.TEXT_DIM);
         float var18 = var15 + (var5 - ImGui.getTextLineHeight()) / 2.0F - 2.0F * var3;
         var9.addText(var14, var18, var17, var1[var11]);
         var8 = var14 + var12;
         if (var16) {
            var9.addRectFilled(var14, var15 + var5 - 2.0F * var3, var14 + var12, var15 + var5, Theme.ACCENT, 2.0F * var3);
         }
      }

      return var10;
   }

   static boolean slider(String var0, double var1, float var3, DoubleConsumer var4) {
      float var5 = Theme.scale();
      float var6 = 6.5F * var5;
      float var7 = ImGui.getCursorScreenPosX();
      float var8 = ImGui.getCursorScreenPosY();
      float var9 = 16.0F * var5;
      ImGui.invisibleButton(var0, var3, var9);
      float var10 = var7 + var6;
      float var11 = var7 + var3 - var6;
      if (ImGui.isItemActive()) {
         double var12 = Math.max(0.0, Math.min(1.0, (ImGui.getIO().getMousePosX() - var10) / (var11 - var10)));
         var4.accept(var12);
         var1 = var12;
      }

      float var17 = var8 + var9 / 2.0F;
      float var13 = 4.0F * var5 / 2.0F;
      float var14 = var10 + (float)var1 * (var11 - var10);
      boolean var15 = ImGui.isItemHovered() || ImGui.isItemActive();
      ImDrawList var16 = ImGui.getWindowDrawList();
      var16.addRectFilled(var10, var17 - var13, var11, var17 + var13, Theme.TRACK, var13);
      var16.addRectFilled(var10, var17 - var13, var14, var17 + var13, Theme.ACCENT, var13);
      var16.addCircleFilled(var14, var17, var15 ? var6 * 1.15F : var6, Theme.KNOB, 24);
      return ImGui.isItemDeactivated();
   }

   private Widgets() {
   }

   private record Card(String id, float x, float y, float width) {
   }
}
