package viewpoint.core;

import viewpoint.platform.LiveSettings;
import zombie.core.Core;
import zombie.iso.IsoChunkMap;

public final class View {
   private static final String SECTION = "Controls/View";
   private static final float MIN_FOV = 30.0F;
   private static final float MAX_FOV = 140.0F;
   private static final float DEFAULT_FOV = 103.0F;
   private static final float DEFAULT_THIRD_PERSON_FOV = 60.0F;
   private static final LiveSettings.Number FOV = LiveSettings.number("view.verticalFov", "Field of view", "Controls/View", 30.0F, 140.0F, 1.0F, 103.0F);
   private static final LiveSettings.Number THIRD_PERSON_FOV = LiveSettings.number(
      "view.thirdPersonVerticalFov", "Field of view in third person", "Controls/View", 30.0F, 140.0F, 1.0F, 60.0F
   );
   private static final LiveSettings.Toggle CROSSHAIR = LiveSettings.toggle("view.crosshair", "Crosshair dot", "Controls/View", true);
   public static volatile boolean enabled;
   public static volatile boolean thirdPerson;
   public static int shoulder;
   public static volatile float crosshairX;
   public static volatile float crosshairY;
   public static volatile float ringX;
   public static volatile float ringY;
   public static volatile float swayRight;
   public static volatile float swayUp;
   public static volatile boolean crosshairOnTarget;
   public static volatile boolean crosshairShown = true;
   public static final float[] ON_TARGET = new float[]{0.95F, 0.1F, 0.1F};
   public static float cameraX;
   public static float cameraY;
   public static float cameraUp;
   public static float frameFovY = (float)Math.toRadians(103.0);

   public static void crosshairSnapshot(boolean var0) {
      crosshairShown = CROSSHAIR.get() && var0;
   }

   public static float fovY(boolean var0) {
      return (float)Math.toRadians((var0 ? THIRD_PERSON_FOV : FOV).get());
   }

   public static double halfDiagonal() {
      return halfDiagonal(frameFovY);
   }

   public static double halfDiagonal(float var0) {
      double var1 = (double)Core.getInstance().getScreenWidth() / Math.max(1, Core.getInstance().getScreenHeight());
      return Math.atan(Math.tan(var0 / 2.0) * Math.sqrt(1.0 + var1 * var1));
   }

   public static int radiusChunks() {
      return Math.min(IsoChunkMap.chunkGridWidth, 19) / 2;
   }

   private View() {
   }

   static {
      FOV.describe("How tall the first-person view is, in degrees from the screen's bottom to its top.");
      THIRD_PERSON_FOV.describe("How tall the third-person view is, in degrees from the screen's bottom to its top.");
      CROSSHAIR.describe("Shows the dot that marks where you look and aim. The game's Hide all UI also hides it.");
   }
}
