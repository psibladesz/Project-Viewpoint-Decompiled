package viewpoint.light;

import java.util.Arrays;
import org.joml.Vector3f;
import viewpoint.core.Frame;
import viewpoint.core.View;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Profile;
import viewpoint.platform.ShaderPacks;
import viewpoint.platform.Tuning;
import viewpoint.render.SceneData;
import viewpoint.render.SpotDraws;
import zombie.characters.IsoGameCharacter;
import zombie.core.Core;
import zombie.iso.IsoCell;
import zombie.vehicles.BaseVehicle;
import zombie.vehicles.VehicleLight;
import zombie.vehicles.VehiclePart;

public final class Headlights {
   private static final int LAMPS_A_CAR = 4;
   private static final int MAX_CARS = 8;
   private static final int SLOTS = 4;
   private static final int FREE = -1;
   private static final float NEAR = 24.0F;
   private static final float GLASS_NEAR = 50.0F;
   private static final float STICK = 3.0F;
   private static final float OWN = -1000000.0F;
   private static final double TURN = Math.toRadians(30.0);
   private static final float BODY = 3.0F;
   private static final float MAP_UP = (float)Math.tan(Math.toRadians(32.0));
   private static final float MAP_SPREADS = 2.2F;
   private static final float LEAST_ACROSS = (float)Math.tan(Math.toRadians(30.0));
   private static final float MOST_ACROSS = (float)Math.tan(Math.toRadians(65.0));
   private static final String PLACE = "Debug/Headlights";
   private static final LiveSettings.Number WIDTH = LiveSettings.number(
      "debug.headlightWidth", "Beam width (degrees each side)", "Debug/Headlights", 8.0F, 45.0F, 1.0F, Tuning.beamWidth
   );
   private static final LiveSettings.Number AIM = LiveSettings.number(
      "debug.headlightAim", "Beam aim (degrees down)", "Debug/Headlights", -1.0F, 6.0F, 0.1F, Tuning.beamAim
   );
   private static final LiveSettings.Number REACH = LiveSettings.number(
      "debug.headlightReach", "Reach (times the game's)", "Debug/Headlights", 0.5F, 4.0F, 0.05F, Tuning.beamReach
   );
   private static final LiveSettings.Number BRIGHTNESS = LiveSettings.number(
      "debug.headlightBrightness", "Head lamps' brightness", "Debug/Headlights", 0.0F, 4.0F, 0.05F, 1.0F
   );
   private static final LiveSettings.Number TAILS = LiveSettings.number("debug.headlightTail", "Tail lights", "Debug/Headlights", 0.0F, 4.0F, 0.05F, 1.0F);
   private static final LiveSettings.Number AIR = LiveSettings.number(
      "debug.headlightBeamLight", "Beams in the air", "Debug/Headlights", 0.0F, 4.0F, 0.05F, 1.0F
   );
   private static final BaseVehicle[] cars = new BaseVehicle[8];
   private static final float[] distance = new float[8];
   private static final float[] range = new float[8];
   private static final int[] slotCar = new int[4];
   private static final float[] slotSide = new float[4];
   private static final Vector3f origin = new Vector3f();
   private static final Vector3f forward = new Vector3f();
   private static final Vector3f up = new Vector3f();
   private static final Vector3f across = new Vector3f();
   private static final float[] lens = new float[3];
   private static final float[] facing = new float[3];
   private static final float[] move = new float[3];
   private static final float[][] lamps = new float[4][24];
   private static final boolean[] rears = new boolean[4];
   private static float size;
   private static int carCount;
   private static int lampCount;
   private static int mapCount;
   private static int drawnMaps;
   private static int lampsHeld;
   private static long nanos;
   private static long frames;
   private static boolean told;
   private static float fromX;
   private static float fromY;
   private static double viewYaw;
   private static double viewHalf;

   public static void snapshot(Frame var0, IsoCell var1, IsoGameCharacter var2) {
      SpotDraws var3 = var0.scene.spots;
      var3.lampCount = 0;
      Arrays.fill(var3.carMaps, -1.0F);
      LampBakes.frame(var3);
      long var4 = System.nanoTime();
      frames++;
      view(var0);
      BaseVehicle var6 = var2.getVehicle();
      LampPlaces.frame(var6 == null ? null : var6.getScript().getFullName(), var6 != null && LampGlass.any(LampBakes.of(var6, var3)));
      carCount = 0;

      for (BaseVehicle var8 : var1.getVehicles()) {
         if (var8.vehicleId != -1) {
            float var9 = reachOf(var8);
            float var10 = fromCamera(var0, var8);
            if (var10 < 50.0F) {
               LampBakes.of(var8, var3);
            }

            if (var9 >= 0.0F && var10 - var9 <= 24.0F && seen(var8.getX(), var8.getY(), var9 + 3.0F)) {
               keep(var8, var10 - var9, var10);
            }
         }
      }

      int var11 = Math.min(ShaderPacks.drawn().pipeline.headlightShadows, 4);
      pickMaps(var11, var6);
      drawnMaps = 0;

      for (int var12 = 0; var12 < carCount; var12++) {
         car(var0, cars[var12], range[var12], cars[var12] == var6);
         cars[var12] = null;
      }

      var3.carMapCount = var11;
      lampCount = var3.lampCount;
      nanos = System.nanoTime() - var4;
      Profile.headlightsReport = Headlights::describe;
   }

   private static void view(Frame var0) {
      fromX = var0.camX;
      fromY = var0.camY;
      double var1 = (double)Core.getInstance().getScreenWidth() / Math.max(1, Core.getInstance().getScreenHeight());
      viewHalf = Math.atan(Math.tan(View.fovY(View.thirdPerson) / 2.0) * var1) + TURN;
      viewYaw = var0.viewYaw;
   }

   private static boolean seen(float var0, float var1, float var2) {
      return LampView.reaches(var0 - fromX, var1 - fromY, var2, viewYaw, viewHalf);
   }

   private static float fromCamera(Frame var0, BaseVehicle var1) {
      float var2 = var1.getX() - var0.camX;
      float var3 = var1.getY() - var0.camY;
      float var4 = (var1.getZ() - var0.camZ) * 2.0F;
      return (float)Math.sqrt(var2 * var2 + var3 * var3 + var4 * var4);
   }

   private static float reach(VehiclePart var0) {
      return var0.getLightDistance() * (rear(var0) ? Tuning.tailReach : REACH.get());
   }

   private static void keep(BaseVehicle var0, float var1, float var2) {
      int var3 = carCount;

      while (var3 > 0 && distance[var3 - 1] > var1) {
         var3--;
      }

      if (var3 != 8) {
         int var4 = Math.min(carCount, 7);
         System.arraycopy(distance, var3, distance, var3 + 1, var4 - var3);
         System.arraycopy(range, var3, range, var3 + 1, var4 - var3);
         System.arraycopy(cars, var3, cars, var3 + 1, var4 - var3);
         distance[var3] = var1;
         range[var3] = var2;
         cars[var3] = var0;
         carCount = var4 + 1;
      }
   }

   private static float reachOf(BaseVehicle var0) {
      boolean var1 = braking(var0);
      float var2 = -1.0F;

      for (int var3 = 0; var3 < var0.getLightCount(); var3++) {
         VehiclePart var4 = var0.getLightByIndex(var3);
         VehicleLight var5 = var4 == null ? null : var4.getLight();
         if (var5 != null && (var5.getActive() || var1 && rear(var4))) {
            var2 = Math.max(var2, reach(var4));
         }
      }

      return var2;
   }

   private static void pickMaps(int var0, BaseVehicle var1) {
      for (int var2 = 0; var2 < 4; var2++) {
         if (slotCar[var2] != -1 && (var2 >= var0 || order(slotCar[var2], var1) == Float.POSITIVE_INFINITY)) {
            slotCar[var2] = -1;
         }
      }

      for (int var6 = 0; var6 < carCount; var6++) {
         BaseVehicle var3 = cars[var6];
         float var4 = order(var3.vehicleId, var1);
         if (var4 != Float.POSITIVE_INFINITY && slotOf(var3) < 0) {
            int var5 = freeSlot(var0);
            if (var5 < 0) {
               var5 = farthestSlot(var0, var1);
               if (var5 < 0 || var4 + 3.0F >= order(slotCar[var5], var1)) {
                  continue;
               }
            }

            slotCar[var5] = var3.vehicleId;
            slotSide[var5] = 0.0F;
         }
      }

      mapCount = 0;

      for (int var7 = 0; var7 < var0; var7++) {
         mapCount = mapCount + (slotCar[var7] == -1 ? 0 : 1);
      }
   }

   private static float order(int var0, BaseVehicle var1) {
      for (int var2 = 0; var2 < carCount; var2++) {
         if (cars[var2].vehicleId == var0) {
            if (!headLit(cars[var2])) {
               return Float.POSITIVE_INFINITY;
            }

            return cars[var2] == var1 ? -1000000.0F : (range[var2] > Tuning.beamShadowReach ? Float.POSITIVE_INFINITY : distance[var2]);
         }
      }

      return Float.POSITIVE_INFINITY;
   }

   private static int freeSlot(int var0) {
      for (int var1 = 0; var1 < var0; var1++) {
         if (slotCar[var1] == -1) {
            return var1;
         }
      }

      return -1;
   }

   private static int farthestSlot(int var0, BaseVehicle var1) {
      int var2 = -1;
      float var3 = Float.NEGATIVE_INFINITY;

      for (int var4 = 0; var4 < var0; var4++) {
         float var5 = slotCar[var4] == -1 ? Float.NEGATIVE_INFINITY : order(slotCar[var4], var1);
         if (slotCar[var4] != -1 && var5 > var3) {
            var3 = var5;
            var2 = var4;
         }
      }

      return var2;
   }

   private static int slotOf(BaseVehicle var0) {
      for (int var1 = 0; var1 < 4; var1++) {
         if (slotCar[var1] == var0.vehicleId) {
            return var1;
         }
      }

      return -1;
   }

   private static void car(Frame var0, BaseVehicle var1, float var2, boolean var3) {
      var1.getWorldPos(0.0F, 0.0F, 0.0F, origin);
      var1.getForwardVector(forward);
      var1.getUpVector(up);
      across.set(up).cross(forward);
      boolean var4 = braking(var1);
      LampGlass.Glass[] var5 = LampBakes.of(var1, var0.scene.spots);
      String var6 = var1.getScript().getFullName();
      int var7 = slotOf(var1);
      lampsHeld = 0;

      for (int var8 = 0; var8 < var1.getLightCount() && lampsHeld < 4; var8++) {
         VehiclePart var9 = var1.getLightByIndex(var8);
         VehicleLight var10 = var9 == null ? null : var9.getLight();
         boolean var11 = var10 != null && rear(var9);
         if (var10 != null && (var10.getActive() || var4 && var11)) {
            int var12 = glassOf(var9);
            place(var1, var6, var10, var11, var5 != null && var12 >= 0 ? var5[var12] : null);
            if (write(lamps[lampsHeld], var0, var9, var10, var11, var4, var7)) {
               rears[lampsHeld++] = var11;
            }

            tell(var10, var11);
         }
      }

      if (var2 > Tuning.lampMerge) {
         lampsHeld = CarLamps.merge(lamps, rears, lampsHeld);
      }

      SpotDraws var13 = var0.scene.spots;

      for (int var14 = 0; var14 < lampsHeld && var13.lampCount < 32; var14++) {
         System.arraycopy(lamps[var14], 0, var13.lamps, var13.lampCount++ * 24, 24);
      }

      if (var7 >= 0) {
         mapView(var13, var7, var2, var3);
      }
   }

   private static boolean write(float[] var0, Frame var1, VehiclePart var2, VehicleLight var3, boolean var4, boolean var5, int var6) {
      float var7 = origin.x + across.x * lens[0] + up.x * lens[1] + forward.x * lens[2];
      float var8 = across.y * lens[0] + up.y * lens[1] + forward.y * lens[2];
      float var9 = origin.y + across.z * lens[0] + up.z * lens[1] + forward.z * lens[2];
      float var10 = reach(var2);
      if (!seen(var7, var9, var10)) {
         return false;
      }

      float var11 = var4 ? -1.0F : 1.0F;
      float var12 = var4 ? 0.0F : (float)Math.tan(Math.toRadians(AIM.get()));
      float var13 = var11 * forward.x - var12 * up.x;
      float var14 = var11 * forward.y - var12 * up.y;
      float var15 = var11 * forward.z - var12 * up.z;
      float var16 = (float)Math.sqrt(var13 * var13 + var14 * var14 + var15 * var15);
      float var17 = var4 ? (var3.getActive() ? var2.getLightIntensity() : 0.0F) + (var5 ? Tuning.brakeIntensity : 0.0F) : var2.getLightIntensity();
      float var18 = var17 * (var4 ? Tuning.tailIntensity * TAILS.get() : Tuning.headlampIntensity * BRIGHTNESS.get());
      var0[0] = -(var7 - var1.camX);
      var0[1] = (origin.z - var1.camZ) * 2.4494896F + var8 + Lights.lift(var1.scene, var7, var9);
      var0[2] = -(var9 - var1.camY);
      var0[3] = var10;
      var0[4] = -var13 / var16;
      var0[5] = var14 / var16;
      var0[6] = -var15 / var16;
      var0[7] = var4 ? 2.0F : 1.0F;
      var0[8] = var3.r * var18;
      var0[9] = var3.g * var18;
      var0[10] = var3.b * var18;
      var0[11] = !var4 && var6 >= 0 ? var6 + 1 : -1.0F;
      var0[12] = -up.x;
      var0[13] = up.y;
      var0[14] = -up.z;
      var0[15] = var4 ? 0.0F : (float)Math.tan(Math.toRadians(WIDTH.get()));
      var0[16] = -(across.x * facing[0] + up.x * facing[1] + forward.x * facing[2]);
      var0[17] = across.y * facing[0] + up.y * facing[1] + forward.y * facing[2];
      var0[18] = -(across.z * facing[0] + up.z * facing[1] + forward.z * facing[2]);
      var0[19] = size / 2.0F;
      var0[20] = 0.0F;
      var0[21] = 1.0F - var2.getCondition() / 100.0F;
      var0[22] = var4 ? Tuning.flashScatter : Tuning.flashScatter * AIR.get();
      var0[23] = var6 >= 0 && !var4 ? 0.0F : (outside(var1, var7, var9) ? 1.0F : 0.0F);
      return true;
   }

   private static boolean outside(Frame var0, float var1, float var2) {
      SceneData var3 = var0.scene;
      byte var4 = 64;
      int var5 = (int)Math.floor(var1 - (var0.camX - var3.exposureAX));
      int var6 = (int)Math.floor(var2 - (var0.camY - var3.exposureAY));
      return var5 >= 0 && var6 >= 0 && var5 < var4 && var6 < var4 ? (var3.exposure[var6 * var4 + var5] & 255) >= 100 : true;
   }

   private static void mapView(SpotDraws var0, int var1, float var2, boolean var3) {
      int var4 = 0;
      int var5 = (var1 + 1) * 11;
      float[] var6 = var0.carMaps;

      for (int var7 = 0; var7 < lampsHeld; var7++) {
         if (!rears[var7]) {
            float[] var8 = lamps[var7];

            for (int var9 = 0; var9 < 3; var9++) {
               var6[var5 + 2 + var9] = (var4 == 0 ? 0.0F : var6[var5 + 2 + var9]) + var8[0 + var9];
               var6[var5 + 5 + var9] = var8[4 + var9];
            }

            var6[var5 + 8] = Math.max(var4 == 0 ? 0.0F : var6[var5 + 8], var8[3]);
            var4++;
         }
      }

      if (var4 == 0) {
         var6[var5] = -1.0F;
      } else {
         float var10 = MapTiers.side(var2, var3);
         boolean var11 = var10 != slotSide[var1] || MapTiers.due(frames, var1, MapTiers.period(var10));
         slotSide[var1] = var10;
         drawnMaps += var11 ? 1 : 0;
         var6[var5] = var11 ? 1.0F : 0.0F;
         var6[var5 + 1] = var10;

         for (int var12 = 0; var12 < 3; var12++) {
            var6[var5 + 2 + var12] = var6[var5 + 2 + var12] / var4;
         }

         float var13 = (float)Math.tan(Math.toRadians(WIDTH.get()));
         var6[var5 + 9] = Math.max(LEAST_ACROSS, Math.min(MOST_ACROSS, 2.2F * var13));
         var6[var5 + 10] = MAP_UP;
      }
   }

   private static void place(BaseVehicle var0, String var1, VehicleLight var2, boolean var3, LampGlass.Glass var4) {
      if (var4 != null) {
         lens[0] = var4.x();
         lens[1] = var4.y();
         lens[2] = var4.z();
         facing[0] = var4.nx();
         facing[1] = var4.ny();
         facing[2] = var4.nz();
         size = var4.size();
      } else {
         Vector3f var5 = var0.getScript().getExtents();
         float var6 = VehicleLamps.overFloor(origin.z, 2.4494896F);
         VehicleLamps.lens(var2.offset.x, var3, var5.x, var5.y, var5.z, var6, Tuning.lampAcross, Tuning.lampHeight, lens);
         LampPlaces.moves(var1, var3, move);
         VehicleLamps.move(var2.offset.x, var3, move, lens);
         facing[0] = facing[1] = 0.0F;
         facing[2] = var3 ? -1.0F : 1.0F;
         size = Tuning.lampGlass;
      }
   }

   private static int glassOf(VehiclePart var0) {
      return switch (var0.getId()) {
         case "HeadlightLeft" -> 0;
         case "HeadlightRight" -> 1;
         case "HeadlightRearLeft" -> 2;
         case "HeadlightRearRight" -> 3;
         default -> -1;
      };
   }

   private static void tell(VehicleLight var0, boolean var1) {
      if (!told && !var1) {
         told = true;
         System.out
            .printf(
               "[Viewpoint] headlights: the game's head lamp cone is a cosine of %.2f (%.0f degrees each side); drawn %.0f degrees each side (Debug, Headlights, Beam width)%n",
               var0.dot,
               Math.toDegrees(Math.acos(Math.min(1.0F, var0.dot))),
               WIDTH.get()
            );
      }
   }

   private static boolean braking(BaseVehicle var0) {
      return var0.getStoplightsOn() && var0.getBatteryCharge() > 0.0F;
   }

   private static boolean rear(VehiclePart var0) {
      return var0.getId().contains("Rear");
   }

   private static boolean headLit(BaseVehicle var0) {
      for (int var1 = 0; var1 < var0.getLightCount(); var1++) {
         VehiclePart var2 = var0.getLightByIndex(var1);
         if (var2 != null && var2.getLight() != null && var2.getLight().getActive() && !rear(var2)) {
            return true;
         }
      }

      return false;
   }

   private static String describe() {
      return String.format(
         "%d cars, %d lamps, maps %d of %d drawn, %s, %.0f us", carCount, lampCount, drawnMaps, mapCount, LampBakes.describe(), nanos / 1000.0
      );
   }

   private Headlights() {
   }

   static {
      WIDTH.describe("How wide a head lamp's beam spreads each side of its axis. The game's own lamps spread 41 degrees, the cone of its old square light.");
      AIM.describe(
         "How far below level the head lamps point, in degrees: higher brings the bright part of the beam on the road nearer the car. Real low beams are aimed about half a degree to a degree down."
      );
      REACH.describe("Where the head lamps' beams end, against the game's own reach (36 squares for a new lamp).");
      BRIGHTNESS.describe("How bright the head lamps are, against a sealed halogen low beam's.");
      TAILS.describe("How bright the tail and brake lights are; 0 none.");
      AIR.describe("How strongly the head lamps' beams show in the air, in dust, mist and rain; 0 none.");
      Arrays.fill(slotCar, -1);
   }
}
