package viewpoint.platform;

import imgui.ImGui;
import java.util.Locale;

final class OnboardingRestart {
   private static final int ERROR = -11905560;
   static final String COLLECTOR_TEXT = "The game uses ZGC to manage its memory. ZGC keeps much memory for itself. Shenandoah keeps less. Viewpoint changes the game's start settings to use it.";

   private OnboardingRestart() {
   }

   static void draw() {
      boolean var0 = Onboarding.compressionNeeded();
      boolean var1 = Onboarding.collectorNeeded();
      OnboardingWindow.title(var0 && var1 ? "Before first person" : (var0 ? "Texture compression" : "Memory manager"));
      ImGui.separator();
      ImGui.spacing();
      Hardware var2 = Onboarding.hardware();
      if (var0 && var2 != null) {
         ImGui.textWrapped(compression(var2));
      }

      Collector.Offer var3 = Collector.offered();
      if (var1 && var3 != null) {
         ImGui.spacing();
         ImGui.textWrapped(
            "The game uses ZGC to manage its memory. ZGC keeps much memory for itself. Shenandoah keeps less. Viewpoint changes the game's start settings to use it."
         );
      }

      ImGui.spacing();
      ImGui.spacing();
      OnboardingWindow.notice("THE GAME WILL CLOSE. THIS IS NOT A CRASH.", "Start it again to play in first person.");
      ImGui.spacing();
      ImGui.spacing();
      error(Onboarding.collectorError());
      error(Onboarding.compressionError());
      if (Onboarding.compressionError() == null) {
         if (Onboarding.collectorError() != null && !var0) {
            if (OnboardingWindow.bigButton("CONTINUE WITHOUT IT")) {
               Onboarding.skipCollector();
            }
         } else if (Onboarding.restarting()) {
            OnboardingWindow.centred("Saving your game and closing it...", 1.0F);
         } else if (OnboardingWindow.bigButton(button(var0, var1 && Onboarding.collectorError() == null))) {
            Onboarding.restart();
         }
      }
   }

   private static void error(String var0) {
      if (var0 != null) {
         ImGui.pushStyleColor(0, -11905560);
         ImGui.textWrapped(var0.replace("%", "%%"));
         ImGui.popStyleColor();
      }
   }

   private static String button(boolean var0, boolean var1) {
      return var0 && var1 ? "MAKE THE CHANGES AND CLOSE THE GAME" : (var0 ? "TURN IT ON AND CLOSE THE GAME" : "SWITCH IT AND CLOSE THE GAME");
   }

   static String compression(Hardware var0) {
      String var1 = var0.vramMiB() > 0
         ? String.format(Locale.ROOT, "Your graphics card has %s.", gib(var0.vramMiB()))
         : String.format(Locale.ROOT, "Your graphics card has %s free.", gib(var0.vramFreeMiB()));
      String var2 = var1 + " With less than 12 GB, Viewpoint needs the game's Texture compression.";
      return var0.gameTexturesMiB() == 0L
         ? var2
         : var2
            + String.format(
               Locale.ROOT,
               " Without it, the game's textures use %s of video memory. With it, they use about %s.",
               gib(var0.gameTexturesMiB()),
               gib(Math.round(var0.gameTexturesMiB() * 0.35))
            );
   }

   private static String gib(long var0) {
      return String.format(Locale.ROOT, "%.1f GB", var0 / 1024.0);
   }
}
