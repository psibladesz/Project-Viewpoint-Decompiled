package viewpoint.platform;

import java.util.List;

final class NewDefaults {
   private static final List<List<String>> EDITIONS = List.of(
      List.of(
         "camera.distance",
         "camera.side",
         "camera.aimSide",
         "camera.fovEffects",
         "camera.vehicleDistance",
         "camera.vehicleHeight",
         "camera.melee",
         "camera.hitPause",
         "camera.headMovement",
         "movement.model"
      ),
      List.of("pack.default.exposure", "pack.default.contrast")
   );
   private static final LiveSettings.Number TAKEN = LiveSettings.number(
      "settings.defaultsEdition", "Defaults' edition taken", "Debug/Views", 0.0F, 1000.0F, 1.0F, 0.0F
   );

   private NewDefaults() {
   }

   static void take() {
      int var0 = TAKEN.getInt();
      if (var0 < EDITIONS.size()) {
         for (int var1 = var0; var1 < EDITIONS.size(); var1++) {
            EDITIONS.get(var1).forEach(LiveSettings::toDefault);
         }

         TAKEN.set(EDITIONS.size());
         SettingsWindow.edited();
         System.out.println("[Viewpoint] settings: the defaults of editions " + (var0 + 1) + " to " + EDITIONS.size() + " taken");
      }
   }
}
