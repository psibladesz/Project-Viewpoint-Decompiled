package viewpoint.platform;

import imgui.ImDrawList;
import imgui.ImGui;
import java.util.Locale;

final class LoadBars {
   private static final int GAME = Theme.abgr(5995161, 1.0F);
   private static final int OTHERS = Theme.abgr(4869458, 1.0F);
   private static final float SWATCH = 0.65F;

   static void load(LoadModel.Values var0, Hardware var1, float var2, float var3) {
      LoadModel.Load var4 = LoadModel.estimate(var0, var1);
      LoadModel.Load var5 = LoadModel.budget(var1);

      for (LoadModel.Part var9 : LoadModel.Part.values()) {
         double var10 = LoadModel.stress(var4, var5, var9, var0, var1);
         LoadModel.Shares var12 = LoadModel.shares(var9, var4, var1);
         meter(var9, var12, describe(var9, var4, var5, var10), var10, var2, var3);
      }
   }

   private static String describe(LoadModel.Part var0, LoadModel.Load var1, LoadModel.Load var2, double var3) {
      return switch (var0) {
         case CPU, GPU -> GraphicsRows.weight(var3);
         case MEMORY -> String.format(Locale.ROOT, "%.1f of %.1f GB free", var1.ramMiB() / 1024.0, var2.ramMiB() / 1024.0);
         case VIDEO_MEMORY -> String.format(Locale.ROOT, "%.1f of %.1f GB free", var1.vramMiB() / 1024.0, var2.vramMiB() / 1024.0);
      };
   }

   private static void meter(LoadModel.Part var0, LoadModel.Shares var1, String var2, double var3, float var5, float var6) {
      float[] var7 = GraphicsRows.colour(var3);
      int var8 = ImGui.colorConvertFloat4ToU32(var7[0], var7[1], var7[2], 1.0F);
      float var9 = ImGui.getCursorScreenPosX();
      float var10 = ImGui.getCursorScreenPosY();
      float var11 = var6 > 0.0F ? var6 : ImGui.getContentRegionAvailX();
      float var12 = ImGui.calcTextSize(var2).x;
      ImGui.textUnformatted(var0.label);
      if (ImGui.calcTextSize(var0.label).x + var12 + 2.0F * ImGui.getStyle().getItemSpacingX() <= var11) {
         ImGui.sameLine();
         ImGui.setCursorScreenPos(var9 + var11 - var12, var10);
      }

      ImGui.pushStyleColor(0, var7[0], var7[1], var7[2], 1.0F);
      ImGui.textUnformatted(var2);
      ImGui.popStyleColor();
      bar(var1, var8, var11, ImGui.getTextLineHeight() * var5);
      key(var0, var1, var8, var11);
   }

   private static void bar(LoadModel.Shares var0, int var1, float var2, float var3) {
      ImDrawList var4 = ImGui.getWindowDrawList();
      float var5 = ImGui.getCursorScreenPosX();
      float var6 = ImGui.getCursorScreenPosY();
      float var7 = var5 + var2;
      float var8 = var2 / (float)Math.max(var0.whole(), 0.001);
      var4.addRectFilled(var5, var6, var7, var6 + var3, Theme.TRACK);
      float var9 = step(var4, var5, var7, var6, var3, (float)var0.game() * var8, GAME);
      var9 = step(var4, var9, var7, var6, var3, (float)var0.ours() * var8, var1);
      step(var4, var9, var7, var6, var3, (float)var0.others() * var8, OTHERS);
      ImGui.dummy(var2, var3);
   }

   private static float step(ImDrawList var0, float var1, float var2, float var3, float var4, float var5, int var6) {
      float var7 = Math.min(var2, var1 + Math.max(0.0F, var5));
      if (var7 > var1) {
         var0.addRectFilled(var1, var3, var7, var3 + var4, var6);
      }

      return var7;
   }

   private static void key(LoadModel.Part var0, LoadModel.Shares var1, int var2, float var3) {
      boolean var4 = var0 == LoadModel.Part.CPU || var0 == LoadModel.Part.GPU;
      float var5 = ImGui.getCursorScreenPosX() + var3;
      Theme.push(12.5F);
      ImGui.pushStyleColor(0, Theme.TEXT_DIM);
      boolean var6 = true;
      if (var1.game() > 0.0) {
         var6 = entry(GAME, "Project Zomboid" + amount(var1.game(), var4), var5, var6);
      }

      var6 = entry(var2, "Viewpoint" + amount(var1.ours(), var4), var5, var6);
      if (var1.others() > 0.0) {
         var6 = entry(OTHERS, "Other programs" + amount(var1.others(), var4), var5, var6);
      }

      entry(Theme.TRACK, "Free" + amount(Math.max(0.0, var1.free() - var1.ours()), var4), var5, var6);
      ImGui.popStyleColor();
      Theme.pop();
      ImGui.spacing();
   }

   private static boolean entry(int var0, String var1, float var2, boolean var3) {
      float var4 = ImGui.getTextLineHeight();
      float var5 = var4 * 0.65F;
      float var6 = ImGui.getStyle().getItemInnerSpacingX();
      float var7 = ImGui.getStyle().getItemSpacingX() * 2.0F;
      float var8 = var5 + var6 + ImGui.calcTextSize(var1).x;
      if (!var3 && ImGui.getItemRectMaxX() + var7 + var8 <= var2) {
         ImGui.sameLine(0.0F, var7);
      }

      float var9 = ImGui.getCursorScreenPosX();
      float var10 = ImGui.getCursorScreenPosY() + (var4 - var5) / 2.0F;
      ImDrawList var11 = ImGui.getWindowDrawList();
      var11.addRectFilled(var9, var10, var9 + var5, var10 + var5, var0);
      var11.addRect(var9, var10, var9 + var5, var10 + var5, Theme.TEXT_FAINT);
      ImGui.dummy(var5 + var6, var4);
      ImGui.sameLine(0.0F, 0.0F);
      ImGui.textUnformatted(var1);
      return false;
   }

   private static String amount(double var0, boolean var2) {
      return var2 ? "" : String.format(Locale.ROOT, " %.1f GB", var0 / 1024.0);
   }

   private LoadBars() {
   }
}
