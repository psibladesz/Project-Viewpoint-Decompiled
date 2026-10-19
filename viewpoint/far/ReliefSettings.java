package viewpoint.far;

import viewpoint.platform.LiveSettings;
import viewpoint.platform.SettingsWindow;

final class ReliefSettings {
   static final String PLACE = "Debug/Relief";
   private static final ReliefRules D = ReliefRules.DEFAULTS;
   static final LiveSettings.Number HEIGHT = LiveSettings.number("lod.relief", "Height (times its own)", "Debug/Relief", 0.1F, 10.0F, 0.1F, D.height());
   private static final LiveSettings.Number SMOOTHING = LiveSettings.number(
      "lod.reliefSmoothSquares", "Smoothing in the open (squares)", "Debug/Relief", 0.0F, 256.0F, 8.0F, D.smoothingSquares()
   );
   private static final LiveSettings.Number TOWN_SMOOTHING = LiveSettings.number(
      "relief.townSmoothSquares", "Smoothing in towns (squares)", "Debug/Relief", 0.0F, 256.0F, 8.0F, D.townSmoothingSquares()
   );
   private static final LiveSettings.Number CROSS = LiveSettings.number(
      "relief.crossSlope", "Road cross slope, at most (%)", "Debug/Relief", 0.0F, 10.0F, 0.5F, percent(D.crossSlope())
   );
   private static final LiveSettings.Number GRADE = LiveSettings.number(
      "relief.grade", "Road grade, at most (%)", "Debug/Relief", 1.0F, 30.0F, 0.5F, percent(D.grade())
   );
   private static final LiveSettings.Number CHANGE = LiveSettings.number(
      "relief.gradeChange", "Road grade change, at most (% each 8 squares)", "Debug/Relief", 0.1F, 10.0F, 0.1F, percent(D.gradeChange())
   );
   private static final LiveSettings.Number BANK = LiveSettings.number(
      "relief.bank", "Bank slope, at most (rise over run)", "Debug/Relief", 0.1F, 2.0F, 0.05F, D.bank()
   );
   private static final LiveSettings.Number MARGIN = LiveSettings.number(
      "relief.padMargin", "Foundation margin (squares)", "Debug/Relief", 0.0F, 8.0F, 1.0F, D.padMargin()
   );
   private static final LiveSettings.Number DRIVEWAY = LiveSettings.number(
      "relief.driveway", "Driveway grade, at most (%)", "Debug/Relief", 1.0F, 40.0F, 1.0F, percent(D.driveway())
   );
   private static final LiveSettings.Number FREEBOARD = LiveSettings.number(
      "relief.freeboard", "Foundation over water, at least (m)", "Debug/Relief", 0.0F, 5.0F, 0.1F, D.freeboard()
   );
   private static final LiveSettings.Number CREEK = LiveSettings.number(
      "relief.creekFall", "Creek fall, at most (%)", "Debug/Relief", 0.0F, 10.0F, 0.5F, percent(D.creekFall())
   );
   private static final LiveSettings.Number TOWN_SLOPE = LiveSettings.number(
      "relief.townSlope", "Town slope, at most (%)", "Debug/Relief", 0.5F, 20.0F, 0.5F, percent(D.townSlope())
   );
   private static final LiveSettings.Number LOT = LiveSettings.number(
      "relief.lotSquares", "Parking lot, at least (squares)", "Debug/Relief", 16.0F, 1024.0F, 16.0F, D.lotSquares()
   );
   static final LiveSettings.Number LOD_PIXELS = LiveSettings.number(
      "relief.lodPixels", "Level of detail error, at most (pixels)", "Debug/Relief", 0.25F, 8.0F, 0.25F, 2.0F
   );
   static final SettingsWindow.Button REBAKE = SettingsWindow.button(
      "Debug/Relief", "Rebake", "Builds the terrain relief again to the rules above and the height, on a worker (seconds), and keeps it for later sessions."
   );
   static final SettingsWindow.Button SHIPPED = SettingsWindow.button(
      "Debug/Relief", "Back to shipped", "Puts the rules above back to the build's and draws the relief the build made again."
   );

   private ReliefSettings() {
   }

   static ReliefRules rules() {
      return new ReliefRules(
         SMOOTHING.getInt(),
         TOWN_SMOOTHING.getInt(),
         HEIGHT.get(),
         CROSS.get() / 100.0F,
         GRADE.get() / 100.0F,
         CHANGE.get() / 100.0F,
         BANK.get(),
         MARGIN.get(),
         DRIVEWAY.get() / 100.0F,
         FREEBOARD.get(),
         CREEK.get() / 100.0F,
         TOWN_SLOPE.get() / 100.0F,
         LOT.getInt()
      );
   }

   static void reset() {
      HEIGHT.set(D.height());
      SMOOTHING.set(D.smoothingSquares());
      TOWN_SMOOTHING.set(D.townSmoothingSquares());
      CROSS.set(percent(D.crossSlope()));
      GRADE.set(percent(D.grade()));
      CHANGE.set(percent(D.gradeChange()));
      BANK.set(D.bank());
      MARGIN.set(D.padMargin());
      DRIVEWAY.set(percent(D.driveway()));
      FREEBOARD.set(D.freeboard());
      CREEK.set(percent(D.creekFall()));
      TOWN_SLOPE.set(percent(D.townSlope()));
      LOT.set(D.lotSquares());
   }

   private static float percent(float var0) {
      return var0 * 100.0F;
   }

   static {
      HEIGHT.describe(
         "How tall the relief stands, times the real ground's. The roads' and banks' limits hold at the height the relief was built at: rebake to hold them at another."
      );
      SMOOTHING.describe("Squares the real ground is smoothed over before anything is built on it, away from towns: the hills keep their shape.");
      TOWN_SMOOTHING.describe(
         "The same in towns, where much of the ground about is built on: gentler ground under the streets and houses, blended to the open's at a town's edge."
      );
      TOWN_SLOPE.describe(
         "The steepest a town's ground lies, so its houses' flat foundations meet with banks rather than cliffs; steeper as the town thins out, and no limit in the open."
      );
   }
}
