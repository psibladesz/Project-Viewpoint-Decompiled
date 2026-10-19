package viewpoint.platform;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiIO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import zombie.core.Core;

// i am sorry ellu :(

final class SettingsPages {
   private static final String CONTROLS = "Controls";
   private static final String KEYS = "Keys";
   private static final String CONTROLLER = "Controller";
   private static final String OPTIONS = "Options";
   private static final String MOUSE_KEYBOARD = "Mouse/Keyboard";
   private static final String KEY_BINDINGS = "Key bindings";
   private static final String WORLD = "World";
   private static final String DEBUG = "Debug";
   private static final String ADVANCED = "Advanced";
   private static final List<SettingsPages.Subtab> CONTROLS_SUBTABS = List.of(
           new SettingsPages.Subtab("Input", List.of("Mouse")),
           new SettingsPages.Subtab("Camera", List.of("View", "Camera", "Third person", "Impact")),
           new SettingsPages.Subtab("Gunplay", List.of("Gunplay")),
           new SettingsPages.Subtab("Movement", List.of("Movement"))
   );
   private static final Map<String, List<SettingsPages.Subtab>> SUBTABS = Map.of(
           "Controller",
           List.of(new SettingsPages.Subtab("Buttons", List.of("Buttons"))),
           "World",
           List.of(
                   new SettingsPages.Subtab("Detail", List.of("Levels of detail")),
                   new SettingsPages.Subtab("Chunks", List.of("Chunks")),
                   new SettingsPages.Subtab("Floors", List.of("Floors", "Floor preview")),
                   new SettingsPages.Subtab("Far world", List.of("Far world")),
                   new SettingsPages.Subtab("Memory", List.of("Memory")),
                   new SettingsPages.Subtab("Surfaces", List.of("Texture filtering", "Glass", "Roofs", "Plants"))
           ),
           "Debug",
           List.of(
                   new SettingsPages.Subtab("Views", List.of("Views")),
                   new SettingsPages.Subtab("Profiling", List.of("Profiling")),
                   new SettingsPages.Subtab("Camera", List.of("Camera", "Camera profiles", "Seated eye"))
           )
   );
   private static final List<SettingsPages.Page> OPTION_PAGES = List.of(
           new SettingsPages.Page("Graphics", "Look, quality and load", Icons.Icon.MONITOR, List.of("Graphics")),
           new SettingsPages.Page("Mouse/Keyboard", "Controls and keys", Icons.Icon.KEYBOARD, List.of("Controls", "Keys")),
           new SettingsPages.Page("Controller", "Buttons, look and aim assist", Icons.Icon.GAMEPAD, List.of("Controller"))
   );
   private static final SettingsPages.Page ABOUT = new SettingsPages.Page("About", "Credits, build and licences (i hate my exgirl)", Icons.Icon.INFO, List.of());
   private static final Map<String, SettingsPages.Page> TABS = Map.of(
           "World",
           new SettingsPages.Page("World", "Reach, detail and memory", Icons.Icon.GLOBE, List.of("World")),
           "Debug",
           new SettingsPages.Page("Debug", "Views and tools", Icons.Icon.BUG, List.of("Debug"))
   );
   private static final float HEIGHT_SHARE = 0.6666667F;
   private static final float SIDEBAR = 210.0F;
   private static final float PAGE = 660.0F;
   private static final float MARGIN = 40.0F;
   private static final float PAD = 18.0F;
   private static final float HEADER = 50.0F;
   private static final float CARD_GAP = 12.0F;
   private static final float ICON = 18.0F;
   private static final float CLOSE = 16.0F;
   private static final float ENTRY = 46.0F;
   private static final float INSET = 10.0F;
   private static final float LOGO = 150.0F;
   private static final float ITEM_WIDTH = 220.0F;
   private static final int FLAGS = 315;
   private static String shown = "Graphics";
   private static boolean minimized;
   private static final Map<String, Integer> subtabShown = new HashMap<>();

   static void draw() {
      var var0 = SettingsWindow.byPlace();
      var var1 = SettingsWindow.byTab(var0.keySet());
      var var2 = more(var1);
      SettingsPages.Page var3 = find(var2);
      ImGuiIO var4 = ImGui.getIO();
      float var5 = Theme.scale();
      float var6 = Math.round(var4.getDisplaySizeY() * 0.6666667F);
      float var7 = Math.min(870.0F * var5, var4.getDisplaySizeX() - 80.0F * var5);
      ImGui.setNextWindowPos(40.0F * var5, (var4.getDisplaySizeY() - var6) / 2.0F, 4);
      ImGui.setNextWindowSize(var7, minimized ? 50.0F * var5 : var6, 1);
      ImGui.pushStyleVar(2, 0.0F, 0.0F);
      boolean var8 = ImGui.begin("Project Viewpoint###viewpoint", 315);
      ImGui.popStyleVar();
      if (var8 && minimized) {
         header(var3, ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), var7, true);
      } else if (var8) {
         sidebar(var2, var3, var6);
         ImGui.sameLine(0.0F, 0.0F);
         page(var3, var1, var0, var7 - 210.0F * var5, var6);
         if (SettingsWindow.takeCloseAsked()) {
            GraphicsPage.leave(SettingsWindow::close);
         }

         GraphicsPage.confirm();
      }

      ImGui.end();
   }

   private static List<SettingsPages.Page> more(Map<String, Set<String>> var0) {
      LinkedHashSet<String> var1 = new LinkedHashSet<>();
      OPTION_PAGES.forEach(var1x -> var1.addAll(var1x.tabs()));
      ArrayList<SettingsPages.Page> var2 = new ArrayList<>();

      for (String var4 : var0.keySet()) {
         if (!var1.contains(var4) && (Core.debug || !var4.equals("Debug"))) {
            var2.add(TABS.getOrDefault(var4, new SettingsPages.Page(var4, "", Icons.Icon.FOLDER, List.of(var4))));
         }
      }

      return var2;
   }

   private static SettingsPages.Page find(List<SettingsPages.Page> var0) {
      for (List<SettingsPages.Page> var2 : List.of(OPTION_PAGES, var0, List.of(ABOUT))) {
         for (SettingsPages.Page var4 : var2) {
            if (var4.name().equals(shown)) {
               return var4;
            }
         }
      }

      shown = "Graphics";
      return OPTION_PAGES.get(0);
   }

   private static void sidebar(List<SettingsPages.Page> var0, SettingsPages.Page var1, float var2) {
      float var3 = Theme.scale();
      float var4 = 210.0F * var3;
      float var5 = ImGui.getCursorScreenPosX();
      float var6 = ImGui.getCursorScreenPosY();
      ImDrawList var7 = ImGui.getWindowDrawList();
      float var8 = ImGui.getStyle().getWindowRounding();
      var7.addRectFilled(var5, var6, var5 + var4, var6 + var2, Theme.SIDEBAR, var8, 80);
      var7.addRectFilled(var5 + var4 - 1.0F, var6, var5 + var4, var6 + var2, Theme.BORDER);
      ImGui.beginGroup();
      ImGui.dummy(var4, 18.0F * var3);
      logo(var5, var4);
      section("Options", OPTION_PAGES, var1, var5, var4);
      section("Advanced", var0, var1, var5, var4);
      section("", List.of(ABOUT), var1, var5, var4);
      float var9 = var6 + var2 - 18.0F * var3 - ImGui.getTextLineHeight();
      if (ImGui.getCursorScreenPosY() <= var9) {
         ImGui.setCursorScreenPos(var5 + 18.0F * var3, var9);
         Theme.push(12.5F);
         ImGui.pushStyleColor(0, Theme.TEXT_FAINT);
         ImGui.textUnformatted("Version " + Build.VERSION);
         ImGui.popStyleColor();
         Theme.pop();
      }

      ImGui.endGroup();
   }

   private static void logo(float var0, float var1) {
      float var2 = Theme.scale();
      float var3 = Math.min(150.0F * var2, var1 - 36.0F * var2);
      Pictures.Picture var4 = OnboardingArt.logo();
      float var5 = var4 == null ? var3 / 2.2F : var3 / var4.aspect();
      float var6 = var0 + (var1 - var3) / 2.0F;
      float var7 = ImGui.getCursorScreenPosY();
      if (var4 != null) {
         ImGui.getWindowDrawList().addImage(var4.texture(), var6, var7, var6 + var3, var7 + var5, 0.0F, 0.0F, 1.0F, 1.0F, Theme.TEXT);
      }

      ImGui.dummy(var1, var5 + 18.0F * var2);
   }

   private static void section(String var0, List<SettingsPages.Page> var1, SettingsPages.Page var2, float var3, float var4) {
      if (!var1.isEmpty()) {
         float var5 = Theme.scale();
         if (!var0.isEmpty()) {
            ImGui.setCursorScreenPos(var3 + 18.0F * var5, ImGui.getCursorScreenPosY());
            Widgets.caption(var0);
         }

         for (SettingsPages.Page var7 : var1) {
            if (entry(var7, var7 == var2, var3, var4)) {
               String var8 = var7.name();
               GraphicsPage.leave(() -> shown = var8);
            }
         }

         ImGui.dummy(var4, 10.0F * var5);
      }
   }

   private static boolean entry(SettingsPages.Page var0, boolean var1, float var2, float var3) {
      float var4 = Theme.scale();
      float var5 = 10.0F * var4;
      float var6 = 46.0F * var4;
      float var7 = 18.0F * var4;
      float var8 = ImGui.getCursorScreenPosY();
      float var9 = var2 + var5;
      float var10 = var2 + var3 - var5;
      ImGui.setCursorScreenPos(var9, var8);
      boolean var11 = ImGui.invisibleButton("##page " + var0.name(), var10 - var9, var6);
      boolean var12 = ImGui.isItemHovered();
      ImDrawList var13 = ImGui.getWindowDrawList();
      float var14 = ImGui.getStyle().getFrameRounding();
      if (var1 || var12) {
         var13.addRectFilled(var9, var8, var10, var8 + var6, var1 ? Theme.ACCENT_SOFT : Theme.abgr(16777215, 0.04F), var14);
      }

      if (var1) {
         var13.addRectFilled(var9, var8 + var6 * 0.25F, var9 + 3.0F * var4, var8 + var6 * 0.75F, Theme.ACCENT, 1.5F * var4);
      }

      float var15 = var9 + var5 + var7 + var5;
      Icons.draw(var13, var0.icon(), var9 + var5, var8 + (var6 - var7) / 2.0F, var7, var1 ? Theme.ACCENT : Theme.TEXT_DIM);
      float var16 = ImGui.getTextLineHeight() + (var0.line().isEmpty() ? 0.0F : 12.5F * var4);
      float var17 = var8 + (var6 - var16) / 2.0F;
      var13.addText(var15, var17, var1 ? Theme.ACCENT : Theme.TEXT, var0.name());
      if (!var0.line().isEmpty()) {
         ImGui.setCursorScreenPos(var15, var17 + ImGui.getTextLineHeight());
         Theme.push(12.5F);
         ImGui.pushStyleColor(0, Theme.TEXT_FAINT);
         ImGui.textUnformatted(var0.line());
         ImGui.popStyleColor();
         Theme.pop();
      }

      ImGui.setCursorScreenPos(var2, var8 + var6 + 2.0F * var4);
      return var11;
   }

   private static void header(SettingsPages.Page var0, float var1, float var2, float var3, boolean var4) {
      float var5 = Theme.scale();
      float var6 = 50.0F * var5;
      float var7 = 18.0F * var5;
      float var8 = 16.0F * var5;
      float var9 = 0.75F * var7;
      ImDrawList var10 = ImGui.getWindowDrawList();
      String var11 = var4 ? "Project Viewpoint" : (OPTION_PAGES.contains(var0) ? "Options" : (var0 == ABOUT ? "" : "Advanced"));
      float var12 = var2 + (var6 - ImGui.getTextLineHeight()) / 2.0F;
      float var13 = var1 + var7;
      if (!var11.isEmpty()) {
         String var14 = var11 + "  /  ";
         var10.addText(var13, var12, Theme.TEXT_DIM, var14);
         var13 += ImGui.calcTextSize(var14).x;
      }

      var10.addText(var13, var12, Theme.TEXT, var0.name());
      if (!var4) {
         var10.addRectFilled(var1, var2 + var6 - 1.0F, var1 + var3, var2 + var6, Theme.BORDER);
      }

      boolean var15 = ImGui.getMousePosX() > var1 + var3 - var7 - 2.0F * var8 - var9;
      if (ImGui.isWindowHovered() && !var15 && ImGui.getMousePosY() < var2 + var6 && ImGui.isMouseDoubleClicked(0)) {
         minimized = !var4;
      }

      ImGui.setCursorScreenPos(var1 + var3 - var7 - 2.0F * var8 - var9, var2 + (var6 - var8) / 2.0F);
      if (Icons.windowButton("##minimize", var4, var8, var4 ? "Restore" : "Minimize")) {
         minimized = !var4;
      }

      ImGui.setCursorScreenPos(var1 + var3 - var7 - var8, var2 + (var6 - var8) / 2.0F);
      if (Icons.button("##close", Icons.Icon.CLOSE, var8, "Close (" + Keys.SETTINGS.windowText() + ")")) {
         GraphicsPage.leave(SettingsWindow::close);
      }
   }

   private static void page(SettingsPages.Page var0, Map<String, Set<String>> var1, Map<String, List<LiveSettings.Setting>> var2, float var3, float var4) {
      float var5 = Theme.scale();
      float var6 = 50.0F * var5;
      float var7 = 18.0F * var5;
      float var8 = ImGui.getCursorScreenPosX();
      float var9 = ImGui.getCursorScreenPosY();
      ImGui.beginGroup();
      header(var0, var8, var9, var3, false);
      ImGui.setCursorScreenPos(var8, var9 + var6);
      ImGui.pushStyleVar(2, var7, var7);
      ImGui.pushStyleColor(3, 0);
      if (ImGui.beginChild("##body " + var0.name(), var3, var4 - var6, false, 65536)) {
         ImGui.pushItemWidth(220.0F * var5);
         body(var0, var1, var2, ImGui.getContentRegionAvailX());
         ImGui.popItemWidth();
      }

      ImGui.endChild();
      ImGui.popStyleColor();
      ImGui.popStyleVar();
      ImGui.endGroup();
   }

   private static void body(SettingsPages.Page var0, Map<String, Set<String>> var1, Map<String, List<LiveSettings.Setting>> var2, float var3) {
      if (var0 == ABOUT) {
         AboutPage.draw(var3);
      } else if (var0.name().equals("Graphics")) {
         LinkedHashSet<String> var10 = new LinkedHashSet<>(var1.getOrDefault("Graphics", Set.of()));
         var10.remove("Graphics/General");
         GraphicsPage.draw(var3, () -> cards(var10, var2, var3));
      } else if (var0.name().equals("Mouse/Keyboard")) {
         Set<String> var9 = var1.getOrDefault("Keys", Set.of());
         Runnable var11 = var9.isEmpty() ? null : () -> keyBindings(var9, var2, var3);
         subtabs("Mouse/Keyboard", CONTROLS_SUBTABS, var1.getOrDefault("Controls", Set.of()), var2, var3, var11);
      } else if (SUBTABS.containsKey(var0.name())) {
         Set<String> var8 = var1.getOrDefault(var0.name(), Set.of());
         subtabs(var0.name(), SUBTABS.get(var0.name()), var8, var2, var3, null);
      } else {
         boolean var4 = false;

         for (String var6 : var0.tabs()) {
            Set<String> var7 = var1.getOrDefault(var6, Set.of());
            var4 |= !var7.isEmpty();
            cards(var7, var2, var3);
         }

         if (!var4) {
            none(var0.name(), var3);
         }
      }
   }

   private static void subtabs(
           String var0, List<SettingsPages.Subtab> var1, Set<String> var2, Map<String, List<LiveSettings.Setting>> var3, float var4, Runnable var5
   ) {
      LinkedHashMap<String, String> var6 = new LinkedHashMap<>();

      for (String var8 : var2) {
         if (SettingsWindow.parent(var8) == null) {
            var6.put(SettingsWindow.leaf(var8), var8);
         }
      }

      ArrayList<SettingsPages.Subtab> var17 = new ArrayList<>();
      HashSet<String> var18 = new HashSet<>();

      for (SettingsPages.Subtab var10 : var1) {
         var18.addAll(var10.groups());
         if (var10.groups().stream().anyMatch(var6::containsKey) || var10 == var1.get(0) && var5 != null) {
            var17.add(var10);
         }
      }

      var6.keySet().stream().filter(var1x -> !var18.contains(var1x)).forEach(var1x -> var17.add(new SettingsPages.Subtab(var1x, List.of(var1x))));
      if (var17.isEmpty()) {
         none(var0, var4);
      } else {
         String[] var19 = var17.stream().map(SettingsPages.Subtab::name).toArray(String[]::new);
         int var20 = Widgets.pages(var0, var19, Math.min(subtabShown.getOrDefault(var0, 0), var19.length - 1));
         subtabShown.put(var0, var20);
         ImGui.spacing();
         SettingsPages.Subtab var11 = (SettingsPages.Subtab)var17.get(var20);
         List<String> var12 = var11.groups().stream().filter(var6::containsKey).toList();
         boolean var13 = var11 == var1.get(0) && var5 != null;
         boolean var14 = var12.size() + (var13 ? 1 : 0) > 1;

         for (String var16 : var12) {
            card((String)var6.get(var16), var14 ? var16 : "", var2, var3, var4);
         }

         if (var13) {
            var5.run();
         }
      }
   }

   private static void keyBindings(Set<String> var0, Map<String, List<LiveSettings.Setting>> var1, float var2) {
      Widgets.beginCard("Keys", "Key bindings", var2);
      boolean var3 = true;

      for (String var5 : var0) {
         if (SettingsWindow.parent(var5) == null) {
            if (!var3) {
               ImGui.spacing();
            }

            var3 = false;
            ImGui.pushStyleColor(0, Theme.TEXT_DIM);
            ImGui.textUnformatted(SettingsWindow.leaf(var5));
            ImGui.popStyleColor();
            SettingsWindow.body(var5, var1.getOrDefault(var5, List.of()), Widgets.inner());
            parts(var0, var1, var5, 1);
         }
      }

      Widgets.endCard();
   }

   private static void cards(Set<String> var0, Map<String, List<LiveSettings.Setting>> var1, float var2) {
      for (String var4 : var0) {
         if (SettingsWindow.parent(var4) == null) {
            card(var4, SettingsWindow.leaf(var4), var0, var1, var2);
         }
      }
   }

   private static void card(String var0, String var1, Set<String> var2, Map<String, List<LiveSettings.Setting>> var3, float var4) {
      Widgets.beginCard(var0, var1, var4);
      SettingsWindow.body(var0, var3.getOrDefault(var0, List.of()), Widgets.inner());
      parts(var2, var3, var0, 1);
      Widgets.endCard();
      ImGui.dummy(var4, 12.0F * Theme.scale() - ImGui.getStyle().getItemSpacingY());
   }

   private static void none(String var0, float var1) {
      Widgets.beginCard("none " + var0, "", var1);
      ImGui.pushStyleColor(0, Theme.TEXT_DIM);
      ImGui.textWrapped(var0 + " has no settings in this version.");
      ImGui.popStyleColor();
      Widgets.endCard();
   }

   private static void parts(Set<String> var0, Map<String, List<LiveSettings.Setting>> var1, String var2, int var3) {
      for (String var5 : var0) {
         if (Objects.equals(SettingsWindow.parent(var5), var2) && ImGui.treeNode(SettingsWindow.leaf(var5) + "##" + var5)) {
            float var6 = ImGui.getStyle().getIndentSpacing() * var3;
            SettingsWindow.body(var5, var1.getOrDefault(var5, List.of()), Widgets.inner() - var6);
            parts(var0, var1, var5, var3 + 1);
            ImGui.treePop();
         }
      }
   }

   private SettingsPages() {
   }

   private record Page(String name, String line, Icons.Icon icon, List<String> tabs) {
   }

   private record Subtab(String name, List<String> groups) {
   }
}