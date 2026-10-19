package viewpoint.platform;

import imgui.ImGui;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class OnboardingKeys {
   private static final float WIDE = 820.0F;
   private static final float KEY_WIDTH = 170.0F;
   private static final String[][] MOUSE = new String[][]{
           {"Wheel", "Choose in the loot menu while it shows"}, {"Right button", "The game's own menu on what the cursor points at, with the cursor free"}
   };

   static void draw() {
      ImGui.textWrapped(
              "These are Viewpoint's own keys; the game's are unchanged. Click a key to change it, then press the new one, with Shift, Ctrl or Alt held if you like; Esc keeps it as it was. A mouse button can be a key too, but not the left or the right one. The settings window and the game's Mod Options have them too."
      );
      ImGui.spacing();
      float var0 = OnboardingWindow.scale();
      Map<String, List<KeyBind>> var1 = groups();
      ArrayList<String> var2 = new ArrayList<>(var1.keySet());
      boolean var3 = ImGui.getContentRegionAvailX() >= 820.0F * var0;
      if (var3 && ImGui.beginTable("columns", 2, 32768)) {
         int var4 = (var2.size() + 1) / 2;
         ImGui.tableNextRow();
         ImGui.tableNextColumn();
         var2.subList(0, var4).forEach(var2x -> group(var2x, (List<KeyBind>)var1.get(var2x), var0));
         ImGui.tableNextColumn();
         var2.subList(var4, var2.size()).forEach(var2x -> group(var2x, (List<KeyBind>)var1.get(var2x), var0));
         mouse();
         ImGui.endTable();
      } else {
         var2.forEach(var2x -> group(var2x, (List<KeyBind>)var1.get(var2x), var0));
         mouse();
      }
   }

   private static Map<String, List<KeyBind>> groups() {
      LinkedHashMap<String, List<KeyBind>> var0 = new LinkedHashMap<>();

      for (KeyBind var2 : Keys.all()) {
         if ((!Keys.debug(var2) || Settings.dev) && var2.usable()) {
            var0.computeIfAbsent(SettingsWindow.group(var2.section), var0x -> new ArrayList<>()).add(var2);
         }
      }

      return var0;
   }

   private static void group(String var0, List<KeyBind> var1, float var2) {
      OnboardingWindow.title(var0);
      short var3 = 24768;
      if (ImGui.beginTable(var0, 3, var3)) {
         ImGui.tableSetupColumn("Action", 8);
         ImGui.tableSetupColumn("Key", 16, 170.0F * var2);
         ImGui.tableSetupColumn("Default", 16);

         for (KeyBind var5 : var1) {
            row(var5, var2);
         }

         ImGui.endTable();
      }

      ImGui.spacing();
   }

   private static void row(KeyBind var0, float var1) {
      ImGui.tableNextRow();
      ImGui.tableNextColumn();
      ImGui.alignTextToFramePadding();
      ImGui.textUnformatted(var0.label);
      if (ImGui.isItemHovered() && !var0.tooltip().isEmpty()) {
         ImGui.setTooltip(var0.tooltip());
      }

      ImGui.tableNextColumn();
      boolean var2 = KeyInput.capturing() == var0;
      String var3 = var2 ? "Press a key or a button..." : (var0.bound() ? var0.windowText() : "None");
      if (ImGui.button(var3 + "##" + var0.key, 170.0F * var1, 0.0F)) {
         KeyInput.capture(var0, false);
      }

      ImGui.tableNextColumn();
      boolean var4 = var0.chords().equals(var0.fallback);
      ImGui.beginDisabled(var4);
      if (ImGui.smallButton("Default##" + var0.key)) {
         Keys.choose(var0, var0.fallback);
         SettingsWindow.edited();
      }

      ImGui.endDisabled();
   }

   private static void mouse() {
      OnboardingWindow.title("Mouse");

      for (String[] var3 : MOUSE) {
         OnboardingWindow.bullet(var3[0] + ": " + var3[1]);
      }
   }

   private OnboardingKeys() {
   }
}