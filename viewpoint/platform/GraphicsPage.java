package viewpoint.platform;

import imgui.ImGui;
import java.util.List;

final class GraphicsPage {
   private static final String VANILLA = "Vanilla";
   private static final String MODE = "graphics.mode";
   private static final String LOAD = "load";
   private static final String CHANGES = "changes";
   private static final String APPLY = "apply";
   private static final String[] PAGES = new String[]{"General", "Shader Options"};
   private static final float VALUE_WIDTH = 175.0F;
   private static final float GAP = 12.0F;
   private static final float ROWS_SHARE = 0.57F;
   private static final float BAR_HEIGHT = 0.5F;
   private static final String PRESET_TEXT = "Sets all the settings below. A change to one of them makes it Custom.";
   private static final String MINECRAFT_TEXT = "A Minecraft shader pack has options of its own: see Shader Options.";
   private static final HeldChanges held = new HeldChanges();
   private static final GraphicsRows rows = new GraphicsRows(175.0F, false, false, held::put);
   private static int page;

   static void draw(float var0, Runnable var1) {
      LoadModel.Values var2 = Onboarding.values();
      LoadModel.Values var3 = held.over(var2);
      boolean var4 = "Vanilla".equalsIgnoreCase(var3.get("graphics.mode"));
      int var5 = Widgets.pages("graphics", PAGES, page);
      if (var5 != page) {
         leave(() -> page = var5);
      }

      ImGui.spacing();
      if (page == 0) {
         general(var2, var3, var4, var0);
      } else {
         var1.run();
      }

      held.track();
   }

   static boolean unapplied() {
      return held.unapplied();
   }

   static void leave(Runnable var0) {
      held.leave(var0);
   }

   static void confirm() {
      held.confirm();
   }

   private static void general(LoadModel.Values var0, LoadModel.Values var1, boolean var2, float var3) {
      rows.follow(var1);
      float var4 = Theme.scale();
      float var5 = 12.0F * var4;
      float var6 = Math.round(var3 * 0.57F);
      float var7 = var3 - var6 - var5;
      Widgets.beginCard("quality", "Quality", var6);
      rows.list(var1, var4, Widgets.inner());
      if (!var2 && ShaderPacks.find(var1.get("graphics.pack")) == null) {
         ImGui.spacing();
         dim("A Minecraft shader pack has options of its own: see Shader Options.");
      }

      Widgets.endCard();
      float var8 = ImGui.getItemRectMaxY();
      ImGui.sameLine(0.0F, var5);
      float var9 = ImGui.getCursorScreenPosX();
      float var10 = ImGui.getCursorScreenPosY();
      float var11 = ImGui.getWindowPosY() + ImGui.getStyle().getWindowPaddingY();
      boolean var12 = held.any();
      float var13 = Widgets.height("apply") + var5;
      float var14 = Widgets.height("load") + (var12 ? var5 + Widgets.height("changes") : 0.0F);
      ImGui.beginGroup();
      List var15 = var12 ? rows.changes(var0, var1) : List.of();
      float var16 = Math.max(var10 + var13, Math.min(var11 + var13, var8 - var14));
      ImGui.setCursorScreenPos(var9, var16);
      Widgets.beginCard("load", "", var7);
      panel(var1);
      Widgets.endCard();
      if (var12) {
         ImGui.dummy(var7, var5 - ImGui.getStyle().getItemSpacingY());
         Widgets.beginCard("changes", "Changes", var7);
         changes(var15);
         Widgets.endCard();
      }

      float var17 = Math.max(var10, Math.min(var11, var8 - var13));
      if (var16 < var17 + var13) {
         ImGui.getWindowDrawList().addRectFilled(var9, ImGui.getWindowPosY(), var9 + var7, var17, Theme.WINDOW);
      }

      ImGui.setCursorScreenPos(var9, var17);
      Widgets.beginCard("apply", "", var7);
      held.buttons(var15.size());
      Widgets.endCard();
      ImGui.endGroup();
   }

   private static void changes(List<String> var0) {
      for (String var2 : var0) {
         ImGui.bullet();
         ImGui.sameLine();
         ImGui.textWrapped(var2.replace("%", "%%"));
      }

      if (var0.isEmpty()) {
         dim("Settings the rows above do not show.");
      }
   }

   private static void panel(LoadModel.Values var0) {
      OnboardingRows.Row var1 = rows.pointed();
      Theme.push(18.0F);
      ImGui.textUnformatted(var1 == null ? "Preset" : var1.label());
      Theme.pop();
      dim(var1 == null ? "Sets all the settings below. A change to one of them makes it Custom." : var1.brief());
      PresetAdvice.Advice var2 = Onboarding.advice();
      Hardware var3 = Onboarding.hardware();
      if (var1 == null && var2 != null) {
         float[] var4 = var3 == null ? Theme.channels(Theme.ACCENT) : GraphicsRows.colour(GraphicsRows.stress(var0, var3));
         ImGui.textColored(var4[0], var4[1], var4[2], 1.0F, "Recommended: " + GraphicsPresets.label(var2.preset()));
      }

      ImGui.spacing();
      Widgets.rule();
      Widgets.caption("Estimated load");
      if (var3 == null) {
         dim("It shows once the hardware is checked, as a save loads.");
      } else {
         LoadBars.load(var0, var3, 0.5F, Widgets.inner());
      }
   }

   private static void dim(String var0) {
      ImGui.pushStyleColor(0, Theme.TEXT_DIM);
      ImGui.textWrapped(var0.replace("%", "%%"));
      ImGui.popStyleColor();
   }

   private GraphicsPage() {
   }
}
