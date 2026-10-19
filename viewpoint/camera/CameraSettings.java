package viewpoint.camera;

import viewpoint.platform.LiveSettings;

final class CameraSettings {
   private static final String SECTION = "Controls/Third person";
   private static final String IMPACT = "Controls/Impact";
   private static final String DEBUG = "Debug/Camera";
   static final float[] MOTION_LEVELS = new float[]{0.0F, 0.3F, 0.6F, 1.0F};
   static final LiveSettings.Choice MOTION = LiveSettings.choice(
      "camera.motion", "Camera motion", "Controls/Third person", new String[]{"Off", "Low", "Medium", "High"}, 2
   );
   static final LiveSettings.Number SHAKE = LiveSettings.number("camera.shake", "Camera shake", "Controls/Impact", 0.0F, 1.0F, 0.05F, 1.0F);
   static final LiveSettings.Number RECOIL = LiveSettings.number("camera.recoil", "Camera recoil", "Controls/Impact", 0.0F, 1.0F, 0.05F, 1.0F);
   static final LiveSettings.Number GUN_KICK = LiveSettings.number("camera.gunKick", "Weapon recoil", "Controls/Impact", 0.0F, 1.5F, 0.05F, 1.0F);
   static final LiveSettings.Number MELEE = LiveSettings.number("camera.melee", "Melee impact", "Controls/Impact", 0.0F, 1.5F, 0.05F, 1.2F);
   static final LiveSettings.Number HIT_PAUSE = LiveSettings.number("camera.hitPause", "Melee hit pause", "Controls/Impact", 0.0F, 1.5F, 0.05F, 0.25F);
   static final LiveSettings.Number FOV_EFFECTS = LiveSettings.number(
      "camera.fovEffects", "Field of view effects", "Controls/Third person", 0.0F, 2.0F, 0.05F, 0.8F
   );
   static final LiveSettings.Number DISTANCE = LiveSettings.number("camera.distance", "Camera distance", "Controls/Third person", 0.25F, 3.0F, 0.05F, 0.85F);
   static final LiveSettings.Number HEIGHT = LiveSettings.number("camera.height", "Camera height", "Controls/Third person", -1.0F, 2.0F, 0.05F, 0.0F);
   static final LiveSettings.Number AIM_HEIGHT = LiveSettings.number(
      "camera.aimHeight", "Camera height aiming", "Controls/Third person", -1.0F, 2.0F, 0.05F, 0.0F
   );
   static final LiveSettings.Number SIDE = LiveSettings.number("camera.side", "Camera side offset", "Controls/Third person", -1.5F, 1.5F, 0.05F, 0.25F);
   static final LiveSettings.Number AIM_SIDE = LiveSettings.number(
      "camera.aimSide", "Camera side offset aiming", "Controls/Third person", -1.5F, 1.5F, 0.05F, 0.15F
   );
   static final LiveSettings.Number VEHICLE_DISTANCE = LiveSettings.number(
      "camera.vehicleDistance", "Vehicle camera distance", "Controls/Third person", 0.25F, 4.0F, 0.05F, 0.9F
   );
   static final LiveSettings.Number VEHICLE_HEIGHT = LiveSettings.number(
      "camera.vehicleHeight", "Vehicle camera height", "Controls/Third person", -1.5F, 2.5F, 0.05F, -0.75F
   );
   static final LiveSettings.Number VEHICLE_SIDE = LiveSettings.number(
      "camera.vehicleSide", "Vehicle camera side offset", "Controls/Third person", -3.0F, 3.0F, 0.05F, 0.0F
   );
   static final LiveSettings.Toggle ON_FOOT = LiveSettings.toggle("camera.thirdPersonOnFoot", "Third person on foot", "Controls/Third person", false);
   static final LiveSettings.Toggle SEATED = LiveSettings.toggle("camera.thirdPersonSeated", "Third person in a vehicle", "Controls/Third person", false);
   static final LiveSettings.Choice SHOULDER = LiveSettings.choice("camera.shoulder", "Shoulder", "Controls/Third person", new String[]{"Right", "Left"}, 0);
   static final LiveSettings.Toggle AUTO_SWAP = LiveSettings.toggle("camera.autoSwap", "Automatic shoulder swap", "Controls/Third person", true);
   static final LiveSettings.Toggle VEHICLE_BEHIND = LiveSettings.toggle("camera.vehicleBehind", "Vehicle camera returns behind", "Controls/Third person", true);
   static final LiveSettings.Number AIM_SENSITIVITY = LiveSettings.number(
      "camera.aimSensitivity", "Aim sensitivity", "Controls/Third person", 0.25F, 2.5F, 0.05F, 1.0F
   );
   static final LiveSettings.Toggle DISSOLVE_PLANTS = LiveSettings.toggle(
      "camera.dissolvePlants", "Plants and trees dissolve too", "Controls/Third person", true
   );
   static final LiveSettings.Toggle SHOW_RIG = LiveSettings.toggle("camera.showRig", "Show the rig (from the free camera)", "Debug/Camera", false);

   static float motion() {
      return MOTION_LEVELS[Math.max(0, Math.min(MOTION_LEVELS.length - 1, MOTION.get()))];
   }

   private CameraSettings() {
   }

   static {
      ON_FOOT.describe(
         "Whether the view is behind the shoulder or at the eyes on foot. The key that switches the camera sets it, and a vehicle keeps a choice of its own."
      );
      SEATED.describe("Whether the view is behind the vehicle or at the eyes while seated in one. The key that switches the camera sets it while seated.");
      MOTION.describe(
         "How much the camera moves on its own: the drift of a camera held by hand, the bob of the steps and the lean into turns. Lower it if the motion makes you feel unwell."
      );
      SHAKE.describe("How much the camera shakes when you are hit, bitten or land hard, and with explosions and thunder near, in first and third person.");
      RECOIL.describe("How much a shot kicks the camera, in first and third person. Where the shot goes does not change.");
      GUN_KICK.describe("How much a shot kicks the gun and the arms that hold it, in first and third person. Where the shot goes does not change.");
      MELEE.describe("How much a melee swing moves the camera, and how hard a blow jolts it when it lands, in first and third person.");
      HIT_PAUSE.describe(
         "How long your character's animation pauses when a melee blow lands, so the blow has weight. The time is made up at once after, so attacks last as long as without it. 0 turns it off."
      );
      FOV_EFFECTS.describe("How much the field of view changes: wider when running, narrower when aiming.");
      DISTANCE.describe(
         "How far behind the character the camera stands, in every state. The character keeps its size on the screen whatever the third-person field of view."
      );
      HEIGHT.describe(
         "How far above its usual height the camera stands, in every state (squares; below 0, lower). The camera still looks where you look, so the character sits lower on the screen."
      );
      AIM_HEIGHT.describe("The same while aiming: the camera moves between the two as aiming starts and ends.");
      SIDE.describe(
         "How much further out over the shoulder the camera stands (squares; below 0, nearer the middle). It follows the shoulder when the camera swaps."
      );
      AIM_SIDE.describe("The same while aiming: the camera moves between the two as aiming starts and ends.");
      VEHICLE_DISTANCE.describe(
         "In a vehicle: how far behind it the camera stands. The vehicle keeps its size on the screen whatever the third-person field of view."
      );
      VEHICLE_HEIGHT.describe("In a vehicle: how far above its usual height the camera stands (squares).");
      VEHICLE_SIDE.describe("In a vehicle: how far to the right of the middle the camera stands (squares; below 0, to the left).");
      SHOULDER.describe("Which shoulder the camera looks over. The shoulder key swaps it while playing.");
      AUTO_SWAP.describe("The camera moves to the other shoulder when a wall is in the way, and back when it clears. Never while aiming.");
      VEHICLE_BEHIND.describe(
         "Driving, in first and third person, the view goes back to the way the vehicle goes when the mouse has been still a while. Moving the mouse takes it back at once."
      );
      AIM_SENSITIVITY.describe(
         "How fast the view turns while aiming, on top of the zoom's own: 1 keeps a turn the same across the screen however far the view is zoomed in. A pad's stick takes it in first person too."
      );
      DISSOLVE_PLANTS.describe("Bushes and trees between the camera and the character dissolve, as models do.");
      SHOW_RIG.describe("With the free camera on, draws the third-person camera's rig: the pivot, the shoulder, the camera wanted and the camera placed.");
   }
}
