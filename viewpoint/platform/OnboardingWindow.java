package viewpoint.platform;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiIO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class OnboardingWindow {
   private static final float SHARE = 0.92F;
   private static final float MAX_WIDTH = 1080.0F;
   private static final float MAX_HEIGHT = 780.0F;
   private static final float TEXT = 1.2F;
   private static final float TITLE = 1.5F;
   private static final float LOGO = 0.42F;
   private static final float LOGO_MIDDLE = 0.29F;
   private static final float WELCOME_TOP = 0.6F;
   private static final float WELCOME_WIDTH = 0.7F;
   private static final float LEAD = 1.15F;
   private static final float PARAGRAPH = 10.0F;
   private static final float COMBO_WIDTH = 230.0F;
   private static final float BIG_TEXT = 1.3F;
   private static final float BIG_WIDTH = 300.0F;
   private static final float BIG_HEIGHT = 1.9F;
   private static final float ROUNDING = 6.0F;
   private static final float NOTICE = 2.4F;
   private static final float NOTICE_LINE = 1.7F;
   private static final float NOTICE_BORDER = 2.0F;
   private static final double PULSE = 3.0;
   private static final float[] ACCENT_DIM = Theme.channels(Theme.ACCENT);
   private static final float[] ACCENT_BRIGHT = Theme.channels(Theme.ACCENT_HOVER);
   private static final float[] ACCENT_HOVER = Theme.channels(Theme.ACCENT_HOVER);
   private static final float[] ACCENT_PRESSED = Theme.channels(Theme.ACCENT_PRESSED);
   private static final float[] ON_ACCENT = Theme.channels(Theme.ON_ACCENT);
   static final int ACCENT_COLOURS = 4;
   private static final int FLAGS = 431;
   static final int DIM = Theme.DIM;
   static final int ACCENT = Theme.ACCENT;
   static final int FAINT = Theme.FAINT;
   private static final int NOTICE_FILL = Theme.abgr(16345616, 0.2F);
   private static final String[] PRESET_TEXT = new String[]{
      "The plain Vanilla look and the shortest view: integrated graphics and old cards.",
      "Shaders with the lightest effects and a short view: a GTX 1050 Ti or RX 570, 2 to 3 GB of video memory.",
      "Shaders out to 1.5 km: a GTX 1650 or RX 580, 4 GB of video memory, 8 GB of memory.",
      "Shaders with sharper shadows, more effects, out to 3 km: an RTX 2060 to 3060, 6 GB of video memory and up.",
      "Everything, out to 4 km: an RTX 3070 or better, 10 GB of video memory and up."
   };
   private static OnboardingWindow.Step step = OnboardingWindow.Step.WELCOME;
   private static float scale = 1.0F;

   static void draw() {
      ImGuiIO var0 = ImGui.getIO();
      float var1 = var0.getDisplaySizeX();
      float var2 = var0.getDisplaySizeY();
      scale = Theme.scale() * 1.2F;
      if (Onboarding.fromStart()) {
         step = OnboardingWindow.Step.WELCOME;
      }

      float var3 = Math.min(var1 * 0.92F, 1080.0F * scale);
      float var4 = Math.min(var2 * 0.92F, 780.0F * scale);
      ImGui.getBackgroundDrawList().addRectFilled(0.0F, 0.0F, var1, var2, DIM);
      ImGui.setNextWindowPos((var1 - var3) / 2.0F, (var2 - var4) / 2.0F, 1);
      ImGui.setNextWindowSize(var3, var4, 1);
      ImGui.pushStyleVar(2, 0.0F, 0.0F);
      boolean var5 = ImGui.begin("Project Viewpoint###viewpoint-setup", 431);
      ImGui.popStyleVar();
      if (var5) {
         float var6 = ImGui.getWindowPosX();
         float var7 = ImGui.getWindowPosY();
         boolean var8 = Onboarding.restartNeeded();
         boolean var9 = step == OnboardingWindow.Step.WELCOME && !var8;
         Theme.push(18.0F);
         OnboardingFrame.backdrop(var6, var7, var3, var4, var9);
         if (OnboardingFrame.top(var6, var7, var3, var8 ? -1 : step.ordinal(), var8 ? 0 : OnboardingWindow.Step.values().length)) {
            OnboardingGraphics.leave(Onboarding::close);
         }

         if (var9) {
            welcome(var6, var7, var3, var4);
         } else {
            page(var6, var7, var3, var4, var8);
         }

         OnboardingGraphics.confirm();
         Theme.pop();
      }

      ImGui.end();
   }

   private static void page(float var0, float var1, float var2, float var3, boolean var4) {
      float var5 = Theme.scale();
      float var6 = 44.0F * var5;
      float var7 = ImGui.getStyle().getItemSpacingY();
      ImGui.setCursorScreenPos(var0 + var6, OnboardingFrame.cornerLogo(var0, var1, var2) + var7);
      if (!var4) {
         OnboardingFrame.title(step.title, "", 40.0F);
      }

      float var8 = ImGui.getCursorScreenPosY() + var7 * 2.0F;
      float var9 = var4 ? var1 + var3 - var6 : var1 + var3 - var6 - 62.0F * var5 - var7 * 3.0F;
      ImGui.setCursorScreenPos(var0 + var6, var8);
      if (step == OnboardingWindow.Step.GRAPHICS && !var4) {
         if (ImGui.beginChild("step", var2 - 2.0F * var6, var9 - var8, false)) {
            content();
         }

         ImGui.endChild();
      } else {
         OnboardingFrame.beginCard("step", var2 - 2.0F * var6, var9 - var8);
         if (var4) {
            OnboardingRestart.draw();
         } else {
            content();
         }

         OnboardingFrame.endCard();
      }

      if (!var4) {
         footer(var0, var1, var2, var3);
      }
   }

   static float scale() {
      return scale;
   }

   static void title(String var0) {
      Theme.push(titlePixels());
      ImGui.textUnformatted(var0);
      Theme.pop();
   }

   static float titlePixels() {
      return 27.0F;
   }

   static void notice(String var0, String var1) {
      ImDrawList var2 = ImGui.getWindowDrawList();
      float var3 = ImGui.getCursorScreenPosX();
      float var4 = ImGui.getCursorScreenPosY();
      float var5 = var3 + ImGui.getContentRegionAvailX();
      float var6 = ImGui.getStyle().getItemSpacingY() * 2.0F;
      var2.channelsSplit(2);
      var2.channelsSetCurrent(1);
      ImGui.dummy(0.0F, var6);
      ImGui.pushStyleColor(0, ACCENT);
      centred(var0, 2.4F);
      centred(var1, 1.7F);
      ImGui.popStyleColor();
      ImGui.dummy(0.0F, var6);
      float var7 = ImGui.getCursorScreenPosY() - ImGui.getStyle().getItemSpacingY();
      var2.channelsSetCurrent(0);
      var2.addRectFilled(var3, var4, var5, var7, NOTICE_FILL, 6.0F * scale);
      var2.addRect(var3, var4, var5, var7, ACCENT, 6.0F * scale, 0, 2.0F * scale);
      var2.channelsMerge();
   }

   static void centred(String var0, float var1) {
      Theme.push(18.0F * var1);
      centre(ImGui.calcTextSize(var0).x);
      ImGui.pushTextWrapPos(0.0F);
      ImGui.textUnformatted(var0);
      ImGui.popTextWrapPos();
      Theme.pop();
   }

   static void bullet(String var0) {
      ImGui.bullet();
      ImGui.sameLine();
      ImGui.textWrapped(var0.replace("%", "%%"));
   }

   private static void content() {
      switch (step) {
         case PRESET:
            preset();
            break;
         case LOOK:
            OnboardingLook.draw();
            break;
         case GRAPHICS:
            OnboardingGraphics.draw();
            break;
         case KEYS:
            OnboardingKeys.draw();
            break;
         case CAMERA:
            OnboardingCamera.draw();
            break;
         case DONE:
            done();
      }
   }

   private static void footer(float var0, float var1, float var2, float var3) {
      float var4 = Theme.scale();
      float var5 = 44.0F * var4;
      float var6 = var0 + var2 - var5;
      float var7 = var1 + var3 - var5;
      OnboardingWindow.Step var8 = step;
      boolean var9 = var8 == OnboardingWindow.Step.PRESET && Onboarding.advice() == null;
      if (OnboardingFrame.next(var8 == OnboardingWindow.Step.DONE ? "Ok" : "Next step", var6, var7, var9, "Check your hardware first.")) {
         if (var8 == OnboardingWindow.Step.DONE) {
            Onboarding.finish();
            step = OnboardingWindow.Step.WELCOME;
         } else {
            go(OnboardingWindow.Step.values()[var8.ordinal() + 1]);
         }
      }

      if (var8 != OnboardingWindow.Step.WELCOME && OnboardingFrame.back(var0 + var5, var7)) {
         go(OnboardingWindow.Step.values()[var8.ordinal() - 1]);
      }
   }

   private static void go(OnboardingWindow.Step var0) {
      if (step == OnboardingWindow.Step.GRAPHICS) {
         OnboardingGraphics.leave(() -> step = var0);
      } else {
         step = var0;
      }
   }

   private static void welcome(float var0, float var1, float var2, float var3) {
      OnboardingFrame.logo(var0, var1, var2, var3, 0.42F, 0.29F);
      float var4 = 44.0F * Theme.scale();
      ImGui.setCursorScreenPos(var0 + var4, var1 + var3 * 0.6F);
      ImGui.pushTextWrapPos(ImGui.getCursorPosX() + var2 * 0.7F);
      ImGui.beginGroup();
      OnboardingFrame.title("Welcome to ", "Project Viewpoint", 40.0F);
      ImGui.dummy(0.0F, 10.0F * Theme.scale());
      if (UnsupportedGraphics.warning() != null) {
         Theme.push(20.699999F);
         UnsupportedGraphics.draw(var2 * 0.7F);
         Theme.pop();
         ImGui.dummy(0.0F, 10.0F * Theme.scale());
      }

      Theme.push(20.699999F);
      ImGui.textUnformatted(
         "Viewpoint turns Project Zomboid into a true 3D game. You can experience the classic gameplay through your character's own eyes or over the shoulder, with the whole world drawn out to the horizon."
      );
      ImGui.dummy(0.0F, 10.0F * Theme.scale());
      ImGui.textUnformatted(
         "Drawing that world asks much more of your computer than the game alone. So before your first step, this setup will check your hardware, pick the graphics settings that better suits it, and help you set up the keys. It only takes a minute, and everything can be changed later in the settings window ("
            + Keys.SETTINGS.windowText()
            + ")."
      );
      Theme.pop();
      ImGui.endGroup();
      ImGui.popTextWrapPos();
      footer(var0, var1, var2, var3);
   }

   private static void preset() {
      ImGui.textWrapped(
         "Quality presets set how far and how finely the world is drawn and which effects run. Click the button below to let us pick the better preset for your computer. Don't worry, you can then choose another if you want to."
      );
      ImGui.spacing();
      ImGui.spacing();
      PresetAdvice.Advice var0 = Onboarding.advice();
      int var1 = GraphicsPresets.current();
      if (var0 == null) {
         if (bigButton("CHECK MY HARDWARE")) {
            check();
         }

         ImGui.spacing();
         ImGui.spacing();
         ImGui.spacing();
         presetLine(null, var1, null);
      } else {
         presetLine(var0, var1, "CHECK AGAIN");
         ImGui.spacing();
         found(var0, var1);
      }
   }

   private static void presetLine(PresetAdvice.Advice var0, int var1, String var2) {
      float var3 = ImGui.getStyle().getItemSpacingX();
      float var4 = ImGui.getStyle().getFramePaddingX();
      float var5 = var2 == null ? 0.0F : var3 + ImGui.calcTextSize(var2).x + 2.0F * var4;
      centre(ImGui.calcTextSize("Preset").x + var3 + 230.0F * scale + var5);
      ImGui.alignTextToFramePadding();
      ImGui.textUnformatted("Preset");
      ImGui.sameLine();
      ImGui.beginDisabled(var0 == null);
      ImGui.setNextItemWidth(230.0F * scale);
      if (ImGui.beginCombo("##preset", presetName(var1, var0))) {
         for (int var6 = 0; var6 < 5; var6++) {
            if (ImGui.selectable(presetName(var6, var0), var6 == var1)) {
               choose(var6);
            }
         }

         ImGui.endCombo();
      }

      ImGui.endDisabled();
      if (var2 != null) {
         ImGui.sameLine();
         pushAccent(1.0F);
         if (ImGui.button(var2)) {
            check();
         }

         ImGui.popStyleColor(4);
      }
   }

   private static void check() {
      ImGuiIO var0 = ImGui.getIO();
      PresetAdvice.Advice var1 = Onboarding.check(
         Math.round(var0.getDisplaySizeX() * var0.getDisplayFramebufferScaleX()), Math.round(var0.getDisplaySizeY() * var0.getDisplayFramebufferScaleY())
      );
      choose(var1.preset());
      Onboarding.put("far.workers", Integer.toString(var1.farWorkers()));
   }

   static boolean bigButton(String var0) {
      Theme.push(23.4F);
      float var1 = Math.max(ImGui.calcTextSize(var0).x + 4.0F * ImGui.getStyle().getFramePaddingX(), 300.0F * scale);
      centre(var1);
      ImGui.pushStyleVar(12, 6.0F * scale);
      pushAccent((float)(0.5 + 0.5 * Math.sin(ImGui.getTime() * 3.0)));
      boolean var2 = ImGui.button(var0, var1, ImGui.getFrameHeight() * 1.9F);
      ImGui.popStyleColor(4);
      ImGui.popStyleVar();
      Theme.pop();
      return var2;
   }

   static void pushAccent(float var0) {
      float[] var1 = new float[3];

      for (int var2 = 0; var2 < var1.length; var2++) {
         var1[var2] = ACCENT_DIM[var2] + (ACCENT_BRIGHT[var2] - ACCENT_DIM[var2]) * var0;
      }

      ImGui.pushStyleColor(21, var1[0], var1[1], var1[2], 1.0F);
      ImGui.pushStyleColor(22, ACCENT_HOVER[0], ACCENT_HOVER[1], ACCENT_HOVER[2], 1.0F);
      ImGui.pushStyleColor(23, ACCENT_PRESSED[0], ACCENT_PRESSED[1], ACCENT_PRESSED[2], 1.0F);
      ImGui.pushStyleColor(0, ON_ACCENT[0], ON_ACCENT[1], ON_ACCENT[2], 1.0F);
   }

   private static void centre(float var0) {
      ImGui.setCursorPosX(ImGui.getCursorPosX() + Math.max(0.0F, (ImGui.getContentRegionAvailX() - var0) / 2.0F));
   }

   private static void centred(String var0) {
      float var1 = ImGui.calcTextSize(var0).x;
      if (var1 > ImGui.getContentRegionAvailX()) {
         ImGui.textWrapped(var0);
      } else {
         centre(var1);
         ImGui.textDisabled(var0);
      }
   }

   private static void found(PresetAdvice.Advice var0, int var1) {
      for (String var3 : var0.reasons()) {
         bullet(var3);
      }

      ImGui.spacing();
      String var5 = "Recommended preset: " + GraphicsPresets.label(var0.preset());
      Hardware var6 = Onboarding.hardware();
      float[] var4 = var6 == null ? Theme.channels(ACCENT) : GraphicsRows.colour(GraphicsRows.stress(Onboarding.values(), var6));
      centre(ImGui.calcTextSize(var5).x);
      ImGui.textColored(var4[0], var4[1], var4[2], 1.0F, var5);
      ImGui.spacing();
      ImGui.textWrapped(presetText(var1));
   }

   static String presetText(int var0) {
      return var0 >= 0 && var0 < PRESET_TEXT.length ? GraphicsPresets.label(var0) + ": " + PRESET_TEXT[var0] : "Custom: your own settings.";
   }

   private static String presetName(int var0, PresetAdvice.Advice var1) {
      String var2 = GraphicsPresets.label(var0);
      return var1 != null && var1.preset() == var0 ? var2 + " (recommended)" : var2;
   }

   static void choose(int var0) {
      Onboarding.putAll(presetWrites(var0));
   }

   static Map<String, String> presetWrites(int var0) {
      LinkedHashMap var1 = new LinkedHashMap();
      var1.put("graphics.preset", GraphicsPresets.NAMES[var0]);

      for (String var3 : List.of("default", "vivid", "normal")) {
         ShaderPack var4 = ShaderPacks.find(var3);
         PackPreset var5 = var4 == null ? null : PresetAdvice.presetNamed(var4, GraphicsPresets.NAMES[var0]);
         if (var5 != null) {
            var1.put(PackPresets.key(var4), var5.label);
         }
      }

      return var1;
   }

   private static void done() {
      title("You're all set!");
      ImGui.textWrapped("Press Ok to save the setup and step into first person.");
      ImGui.spacing();
      bullet(Keys.FIRST_PERSON.windowText() + ": toggle first person");
      bullet(Keys.THIRD_PERSON.windowText() + ": toggle between third/first person");
      bullet(Keys.CURSOR.windowText() + ": toggle between controlling the cursor/camera");
      bullet(Keys.SETTINGS.windowText() + ": toggle the settings menu");
      ImGui.spacing();
      ImGui.textWrapped("Enjoy, but remember: THIS IS HOW YOU DIED.");
   }

   private OnboardingWindow() {
   }

   private enum Step {
      WELCOME("Welcome to Project Viewpoint"),
      PRESET("Graphics Quality"),
      LOOK("Choose a Look"),
      GRAPHICS("Graphics Settings"),
      KEYS("Controls"),
      CAMERA("Camera"),
      DONE("That's it!");

      final String title;

      Step(String nullxx) {
         this.title = nullxx;
      }
   }
}
