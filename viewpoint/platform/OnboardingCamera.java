package viewpoint.platform;

import imgui.ImGui;
import java.util.Locale;

final class OnboardingCamera {
   static final String FOV = "view.verticalFov";
   static final String THIRD_PERSON_FOV = "view.thirdPersonVerticalFov";
   static final String HEAD_MOVEMENT = "camera.headMovement";
   static final String CAMERA_MOTION = "camera.motion";
   static final float[] LEVELS = new float[]{0.0F, 0.33F, 0.66F, 1.0F};
   static final String[] LEVEL_NAMES = new String[]{"Off", "Low", "Medium", "High"};
   private static final float NEAR = 0.005F;
   private static final float SLIDER_WIDTH = 320.0F;
   private static final float LEVEL_WIDTH = 110.0F;
   private static final int[] degrees = new int[1];

   static void draw() {
      float var0 = OnboardingWindow.scale();
      ImGui.textWrapped(
         "How much you see, and how the view moves as you walk. All can be changed later in the settings window ("
            + Keys.SETTINGS.windowText()
            + "), under Controls."
      );
      ImGui.spacing();
      OnboardingWindow.title("Field of view");
      ImGui.textWrapped(
         "How tall the view is, in degrees from the bottom of the screen to its top. Wider shows more round you and makes things look farther away; narrower brings them closer."
      );
      ImGui.spacing();
      fov(number("view.verticalFov"), var0);
      fov(number("view.thirdPersonVerticalFov"), var0);
      ImGui.spacing();
      ImGui.spacing();
      OnboardingWindow.title("Head movement");
      ImGui.textWrapped(
         "How much the view moves with your character's head as they stand, walk and turn. Off keeps it steady; High follows every step and sway. Lower it if the motion makes you feel unwell."
      );
      ImGui.spacing();
      head(number("camera.headMovement"), var0);
      ImGui.spacing();
      ImGui.spacing();
      OnboardingWindow.title("Camera motion");
      ImGui.textWrapped(
         "How much the camera moves by itself in third person: the sway of a camera held by hand, the bob of the steps and the lean into turns. Lower it if the motion makes you feel unwell."
      );
      ImGui.spacing();
      motion(choice("camera.motion"), var0);
   }

   private static void motion(LiveSettings.Choice var0, float var1) {
      for (int var2 = 0; var2 < LEVEL_NAMES.length; var2++) {
         if (var2 > 0) {
            ImGui.sameLine();
         }

         boolean var3 = var2 == var0.get();
         if (var3) {
            OnboardingWindow.pushAccent(1.0F);
         }

         if (ImGui.button(LEVEL_NAMES[var2] + "##motion", 110.0F * var1, 0.0F) && !var3) {
            var0.set(var2);
            SettingsWindow.edited();
         }

         if (var3) {
            ImGui.popStyleColor(4);
         }
      }
   }

   private static void fov(LiveSettings.Number var0, float var1) {
      degrees[0] = var0.getInt();
      ImGui.setNextItemWidth(320.0F * var1);
      if (ImGui.sliderInt(var0.label + "##" + var0.key, degrees, (int)var0.min, (int)var0.max)) {
         var0.set(degrees[0]);
      }

      if (ImGui.isItemDeactivatedAfterEdit()) {
         SettingsWindow.edited();
      }

      ImGui.sameLine();
      ImGui.beginDisabled(var0.getInt() == Math.round(var0.fallback));
      if (ImGui.smallButton("Default##" + var0.key)) {
         var0.set(var0.fallback);
         SettingsWindow.edited();
      }

      ImGui.endDisabled();
   }

   private static void head(LiveSettings.Number var0, float var1) {
      int var2 = level(var0.get());

      for (int var3 = 0; var3 < LEVELS.length; var3++) {
         if (var3 > 0) {
            ImGui.sameLine();
         }

         boolean var4 = var3 == var2;
         if (var4) {
            OnboardingWindow.pushAccent(1.0F);
         }

         if (ImGui.button(LEVEL_NAMES[var3] + "##head", 110.0F * var1, 0.0F) && !var4) {
            var0.set(LEVELS[var3]);
            SettingsWindow.edited();
         }

         if (var4) {
            ImGui.popStyleColor(4);
         }
      }

      if (var2 < 0) {
         ImGui.sameLine();
         ImGui.alignTextToFramePadding();
         ImGui.textDisabled(String.format(Locale.ROOT, "Custom (%.2f)", var0.get()));
      }
   }

   static int level(float var0) {
      for (int var1 = 0; var1 < LEVELS.length; var1++) {
         if (Math.abs(var0 - LEVELS[var1]) < 0.005F) {
            return var1;
         }
      }

      return -1;
   }

   private static LiveSettings.Choice choice(String var0) {
      if (LiveSettings.find(var0) instanceof LiveSettings.Choice var1) {
         return var1;
      } else {
         throw new IllegalStateException(var0 + " is not declared as the setup draws");
      }
   }

   private static LiveSettings.Number number(String var0) {
      if (LiveSettings.find(var0) instanceof LiveSettings.Number var1) {
         return var1;
      } else {
         throw new IllegalStateException(var0 + " is not declared as the setup draws");
      }
   }

   private OnboardingCamera() {
   }
}
