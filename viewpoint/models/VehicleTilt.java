package viewpoint.models;

import java.util.IdentityHashMap;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import viewpoint.core.Frame;
import viewpoint.input.SeatedView;
import viewpoint.platform.LiveSettings;
import viewpoint.render.FarRelief;
import zombie.scripting.objects.VehicleScript;
import zombie.vehicles.BaseVehicle;

final class VehicleTilt {
   private static final String LODS = "World/Levels of detail";
   private static final LiveSettings.Number MOST = LiveSettings.number(
      "lod.vehicleTilt", "Vehicle tilt, at most (degrees)", "World/Levels of detail", 0.0F, 45.0F, 1.0F, 20.0F
   );
   private static final float REACH = 1.5F;
   private static final float IN_LINE = 1.0E-4F;
   private static final Matrix4f tilt = new Matrix4f();
   private static final Quaternionf turn = new Quaternionf();
   private static final Vector3f wheel = new Vector3f();
   private static final Map<BaseVehicle, float[]> tilts = new IdentityHashMap<>();
   private static long tiltsFrame = Long.MIN_VALUE;

   private VehicleTilt() {
   }

   static Matrix4f onRelief(Matrix4f var0, Frame var1, BaseVehicle var2, float var3, float var4, float var5) {
      FarRelief.Lift var6 = var1.scene.relief;
      if (var6 == null) {
         return var0;
      }

      float[] var7 = tilt(var1, var6, var2, var3, var4);
      upright(var7[0], var7[1]);
      float var8 = -(var3 - var1.camX);
      float var9 = (var5 - var1.camZ) * 2.4494896F;
      float var10 = -(var4 - var1.camY);
      tilt.translation(var8, var9 + var7[2], var10).rotate(turn).translate(-var8, -var9, -var10);
      return var0.mulLocal(tilt);
   }

   static void seatView(Frame var0, BaseVehicle var1, float var2, float var3, float var4, float var5) {
      FarRelief.Lift var6 = var0.scene.relief;
      int var7 = SeatedView.follow();
      if (var6 != null && var7 != 2) {
         float[] var8 = tilt(var0, var6, var1, var2, var3);
         float var9 = var8[0];
         float var10 = var8[1];
         if (var7 == 1) {
            float var11 = (float)Math.sqrt(var4 * var4 + var5 * var5);
            float var12 = var11 > 0.0F ? var4 / var11 : 1.0F;
            float var13 = var11 > 0.0F ? var5 / var11 : 0.0F;
            float var14 = var9 * var12 + var10 * var13;
            var9 = var12 * var14;
            var10 = var13 * var14;
         }

         upright(var9, var10);
         var0.seatTilt[0] = turn.x;
         var0.seatTilt[1] = turn.y;
         var0.seatTilt[2] = turn.z;
         var0.seatTilt[3] = turn.w;
      }
   }

   private static float[] tilt(Frame var0, FarRelief.Lift var1, BaseVehicle var2, float var3, float var4) {
      if (var0.number != tiltsFrame) {
         tilts.clear();
         tiltsFrame = var0.number;
      }

      float[] var5 = var2 == null ? null : tilts.get(var2);
      if (var5 == null) {
         var5 = new float[3];
         measure(var1, var2, var3, var4, var5);
         clamp(var5);
         if (var2 != null) {
            tilts.put(var2, var5);
         }
      }

      return var5;
   }

   private static void measure(FarRelief.Lift var0, BaseVehicle var1, float var2, float var3, float[] var4) {
      VehicleScript var5 = var1 == null ? null : var1.getScript();
      int var6 = var5 == null ? 0 : var5.getWheelCount();
      float[] var7 = new float[var6];
      float[] var8 = new float[var6];
      float[] var9 = new float[var6];

      for (int var10 = 0; var10 < var6; var10++) {
         Vector3f var11 = var5.getWheel(var10).offset;
         var1.getWorldPos(var11.x, var11.y, var11.z, wheel);
         var7[var10] = wheel.x;
         var8[var10] = wheel.y;
         var9[var10] = var0.scene(wheel.x, wheel.y);
      }

      if (!plane(var7, var8, var9, var2, var3, var4)) {
         var4[0] = (var0.scene(var2 + 1.5F, var3) - var0.scene(var2 - 1.5F, var3)) / 3.0F;
         var4[1] = (var0.scene(var2, var3 + 1.5F) - var0.scene(var2, var3 - 1.5F)) / 3.0F;
         var4[2] = var0.scene(var2, var3);
      }
   }

   static boolean plane(float[] var0, float[] var1, float[] var2, float var3, float var4, float[] var5) {
      int var6 = var0.length;
      if (var6 < 3) {
         return false;
      }

      float var7 = 0.0F;
      float var8 = 0.0F;
      float var9 = 0.0F;

      for (int var10 = 0; var10 < var6; var10++) {
         var7 += var0[var10] / var6;
         var8 += var1[var10] / var6;
         var9 += var2[var10] / var6;
      }

      float var19 = 0.0F;
      float var11 = 0.0F;
      float var12 = 0.0F;
      float var13 = 0.0F;
      float var14 = 0.0F;

      for (int var15 = 0; var15 < var6; var15++) {
         float var16 = var0[var15] - var7;
         float var17 = var1[var15] - var8;
         float var18 = var2[var15] - var9;
         var19 += var16 * var16;
         var11 += var16 * var17;
         var12 += var17 * var17;
         var13 += var16 * var18;
         var14 += var17 * var18;
      }

      float var20 = var19 * var12 - var11 * var11;
      if (Math.abs(var20) < 1.0E-4F) {
         return false;
      }

      var5[0] = (var13 * var12 - var14 * var11) / var20;
      var5[1] = (var14 * var19 - var13 * var11) / var20;
      var5[2] = var9 + var5[0] * (var3 - var7) + var5[1] * (var4 - var8);
      return true;
   }

   private static void clamp(float[] var0) {
      float var1 = (float)Math.tan(Math.toRadians(MOST.get()));
      float var2 = (float)Math.sqrt(var0[0] * var0[0] + var0[1] * var0[1]);
      if (var2 > var1) {
         float var3 = var2 > 0.0F ? var1 / var2 : 0.0F;
         var0[0] *= var3;
         var0[1] *= var3;
      }
   }

   private static void upright(float var0, float var1) {
      float var2 = (float)Math.sqrt(var0 * var0 + 1.0F + var1 * var1);
      turn.rotationTo(0.0F, 1.0F, 0.0F, var0 / var2, 1.0F / var2, var1 / var2);
   }

   static {
      MOST.describe(
         "A vehicle on a slope of the terrain relief is drawn tilted to the ground under its wheels, up to this; its physics, and what everyone else's game sees of it, stay the game's own."
      );
   }
}
