package viewpoint.platform;

import imgui.ImGui;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import zombie.input.Mouse;

public final class SettingsWindow {
   static final String NESTED = " > ";
   private static final List<String> ORDER = List.of(
           "Graphics/General",
           "Graphics/Shaders",
           "Graphics/Advanced",
           "Controls/Mouse",
           "Controls/View",
           "Controls/Camera",
           "Controls/Third person",
           "Controls/Impact",
           "Controls/Gunplay",
           "Controls/Movement",
           "Controls/Loot menu",
           "Keys/Views",
           "Keys/Loot menu",
           "Keys/Free camera",
           "Keys/Debug",
           "Controller/Look",
           "Controller/Aim assist",
           "Controller/Buttons",
           "Controller/Loot menu",
           "World/Chunks",
           "World/Floors",
           "World/Floor preview",
           "World/Levels of detail",
           "World/Far world",
           "World/Memory",
           "Debug/Views",
           "Debug/Profiling",
           "Debug/Camera",
           "Debug/Camera profiles",
           "Debug/Seated eye",
           "Debug/Relief",
           "Debug/Shooting",
           "Debug/Corpses",
           "Debug/Fixes",
           "Debug/Setup",
           "Debug/Free camera"
   );
   private static final Map<String, List<Runnable>> panels = new LinkedHashMap<>();
   private static final List<SettingsWindow.Button> buttons = new CopyOnWriteArrayList<>();
   private static volatile boolean shown;
   private static volatile boolean dirty;
   private static volatile boolean closeAsked;
   private static boolean wasDrawn;

   public static boolean shown() {
      return shown;
   }

   public static void update() {
      Graphics.start();
      Keys.start();
      boolean var0 = LiveSettings.load();
      Graphics.update();
      if (var0) {
         PerformanceOverlay.loaded();
      }

      if (Keys.SETTINGS.pressed()) {
         if (shown && GraphicsPage.unapplied()) {
            closeAsked = true;
         } else {
            shown = !shown;
         }
      }

      if (shown) {
         PadGlyphs.update();
         PadBinds.refresh();
      }

      boolean var1 = Onboarding.update();
      if (var0 || dirty || var1) {
         boolean var2 = dirty || var1;
         dirty = false;
         if (Graphics.changed() || var2) {
            LiveSettings.save();
         }
      }

      Capture.update();
   }

   public static void edited() {
      dirty = true;
   }

   public static void cursorHeld(boolean var0) {
      ImGuiFrame.cursorHeld = var0;
   }

   public static void blockGameMouse() {
      boolean var0 = Onboarding.shown() && ImGuiFrame.available();
      if (var0 || shown && ImGuiFrame.wantsMouse) {
         Arrays.fill(Mouse.buttonDownStates, false);
         Arrays.fill(Mouse.buttonPrevStates, false);
         ImGuiFrame.scrolled(Mouse.wheelDelta);
         Mouse.wheelDelta = 0;
      }
   }

   public static void panel(String var0, Runnable var1) {
      panels.computeIfAbsent(var0, var0x -> new ArrayList<>()).add(var1);
   }

   public static SettingsWindow.Button button(String var0, String var1, String var2) {
      SettingsWindow.Button var3 = new SettingsWindow.Button(var0, var1, var2);
      buttons.add(var3);
      return var3;
   }

   public static void render() {
      boolean var0 = shown && !Onboarding.shown() && !CrashReport.shown();
      if (wasDrawn && !var0) {
         PadInput.windowClosed();
      }

      wasDrawn = var0;
      if (CrashReport.shown()) {
         ImGuiFrame.draw(CrashWindow::draw);
      } else if (Onboarding.shown()) {
         ImGuiFrame.draw(OnboardingWindow::draw);
      } else if (shown) {
         ImGuiFrame.draw(() -> {
            SettingsPages.draw();
            Overlays.draw();
         });
      } else {
         ImGuiFrame.wantsMouse = false;
         KeyInput.cancel();
         if (Overlays.any()) {
            ImGuiFrame.draw(Overlays::draw);
         }
      }
   }

   static void close() {
      shown = false;
   }

   static boolean takeCloseAsked() {
      boolean var0 = closeAsked;
      closeAsked = false;
      return var0;
   }

   static void body(String var0, List<LiveSettings.Setting> var1, float var2) {
      for (LiveSettings.Setting var4 : var1) {
         SettingsWindow.Button var5 = var4.shown() ? besideOf(var4) : null;
         float var6 = ImGui.getCursorScreenPosX();
         float var7 = ImGui.getCursorScreenPosY();
         float var8 = var5 == null ? 0.0F : ImGui.calcTextSize(var5.label).x + 2.0F * ImGui.getStyle().getFramePaddingX() + ImGui.getStyle().getItemSpacingX();
         if (SettingRows.edit(var4, var2 - var8)) {
            dirty = true;
         }

         if (var5 != null) {
            ImGui.sameLine();
            ImGui.setCursorScreenPos(var6 + var2 - var8 + ImGui.getStyle().getItemSpacingX(), var7);
            press(var5);
         }
      }

      for (SettingsWindow.Button var11 : buttons) {
         if (var11.place.equals(var0) && var11.beside == null && var11.shown.getAsBoolean()) {
            draw(var11);
         }
      }

      for (Runnable var12 : panels.getOrDefault(var0, List.of())) {
         var12.run();
      }
   }

   private static void capture() {
      if (ImGui.button("Capture profile")) {
         Capture.request();
      }

      if (ImGui.isItemHovered()) {
         ImGui.setTooltip(
                 "A Java Flight Recorder recording of every thread and our frames (its length above), saved to Zomboid/viewpoint-captures. Start moving first."
         );
      }

      ImGui.sameLine();
      ImGui.textUnformatted(Capture.status());
   }

   private static void draw(SettingsWindow.Button var0) {
      press(var0);
      ImGui.sameLine();
      ImGui.textUnformatted(var0.status);
   }

   private static void press(SettingsWindow.Button var0) {
      if (ImGui.button(var0.label + "##" + var0.place)) {
         var0.press();
      }

      if (ImGui.isItemHovered()) {
         String var1 = var0.beside != null && !var0.status.isEmpty() ? var0.tooltip + "\n" + var0.status : var0.tooltip;
         ImGui.setTooltip(var1.replace("%", "%%"));
      }
   }

   private static SettingsWindow.Button besideOf(LiveSettings.Setting var0) {
      for (SettingsWindow.Button var2 : buttons) {
         if (var0.key.equals(var2.beside) && var2.shown.getAsBoolean()) {
            return var2;
         }
      }

      return null;
   }

   static String tab(String var0) {
      int var1 = var0.indexOf(47);
      return var1 < 0 ? var0 : var0.substring(0, var1);
   }

   static String group(String var0) {
      return var0.substring(var0.indexOf(47) + 1);
   }

   static Map<String, Set<String>> byTab(Collection<String> var0) {
      LinkedHashMap<String, Set<String>> var1 = new LinkedHashMap<>();

      for (String var3 : var0) {
         Set<String> var4 = var1.computeIfAbsent(tab(var3), var0x -> new LinkedHashSet<>());

         for (int var5 = var3.indexOf(" > ", var3.indexOf(47) + 1); var5 >= 0; var5 = var3.indexOf(" > ", var5 + " > ".length())) {
            var4.add(var3.substring(0, var5));
         }

         var4.add(var3);
      }

      return var1;
   }

   static String parent(String var0) {
      int var1 = var0.lastIndexOf(" > ");
      return var1 > var0.indexOf(47) ? var0.substring(0, var1) : null;
   }

   static String leaf(String var0) {
      String var1 = group(var0);
      int var2 = var1.lastIndexOf(" > ");
      return var2 < 0 ? var1 : var1.substring(var2 + " > ".length());
   }

   static Map<String, List<LiveSettings.Setting>> byPlace() {
      LinkedHashSet<String> var0 = new LinkedHashSet<>(ORDER);
      LinkedHashMap<String, List<LiveSettings.Setting>> var1 = new LinkedHashMap<>();
      List<LiveSettings.Setting> var2 = LiveSettings.all();

      for (LiveSettings.Setting var4 : var2) {
         if (var4.shown()) {
            var0.add(var4.section);
            var1.computeIfAbsent(var4.section, var0x -> new ArrayList<>()).add(var4);
         }
      }

      for (LiveSettings.Setting var9 : var2) {
         if (var9.shown()) {
            for (String var6 : var9.also()) {
               var0.add(var6);
               var1.computeIfAbsent(var6, var0x -> new ArrayList<>()).add(var9);
            }
         }
      }

      LinkedHashSet<String> var8 = new LinkedHashSet<>();

      for (SettingsWindow.Button var12 : buttons) {
         if (var12.shown.getAsBoolean()) {
            var8.add(var12.place);
         }
      }

      var0.addAll(panels.keySet());
      var0.addAll(var8);
      LinkedHashMap<String, List<LiveSettings.Setting>> var11 = new LinkedHashMap<>();

      for (String var14 : var0) {
         if (var1.containsKey(var14) || panels.containsKey(var14) || var8.contains(var14)) {
            var11.put(var14, var1.getOrDefault(var14, List.of()));
         }
      }

      return var11;
   }

   private SettingsWindow() {
   }

   static {
      panel("Debug/Profiling", SettingsWindow::capture);
   }

   public static final class Button {
      final String place;
      final String label;
      final String tooltip;
      private final AtomicBoolean pressed = new AtomicBoolean();
      private volatile String status = "";
      private volatile BooleanSupplier shown = () -> true;
      private volatile String beside;

      Button(String var1, String var2, String var3) {
         this.place = var1;
         this.label = var2;
         this.tooltip = var3;
      }

      public boolean take() {
         return this.pressed.getAndSet(false);
      }

      void press() {
         this.pressed.set(true);
      }

      public void status(String var1) {
         this.status = var1;
      }

      public void shownWhen(BooleanSupplier var1) {
         this.shown = var1;
      }

      public void beside(String var1) {
         this.beside = var1;
      }
   }
}