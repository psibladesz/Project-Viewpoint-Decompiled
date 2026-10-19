package viewpoint.models;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import viewpoint.platform.Profile;
import zombie.core.skinnedmodel.ModelManager.ModelSlot;
import zombie.core.skinnedmodel.model.ModelInstance;
import zombie.core.skinnedmodel.model.ModelInstanceRenderData;
import zombie.core.skinnedmodel.model.ModelSlotRenderData;
import zombie.vehicles.BaseVehicle;

final class VehicleRate {
   private static final float NEAR = 8.0F;
   private static final float[] REACH = new float[]{16.0F, 32.0F};
   private static final int[] INTERVAL = new int[]{2, 4, 8};
   private static final float STILL = 0.001F;
   private static final Map<BaseVehicle, VehicleRate.Entry> entries = new IdentityHashMap<>();
   private static final ArrayList<ModelInstance> instances = new ArrayList<>();
   private static long frame;
   private static int fresh;
   private static int again;

   static void begin() {
      frame++;
      again = 0;
      fresh = 0;
   }

   static ModelSlotRenderData snapshot(BaseVehicle var0, ModelSlot var1, float var2, boolean var3) {
      VehicleRate.Entry var4 = entries.computeIfAbsent(var0, var0x -> new VehicleRate.Entry());
      var4.seen = frame;
      int var5 = !var3 && !(var2 <= 8.0F) ? interval(var2) : 1;
      if (var4.kept != null
         && var5 > 1
         && (frame + (System.identityHashCode(var0) & 0xFF)) % var5 != 0L
         && still(var4, var0)
         && var4.kept.modelSlot == var1
         && sameParts(var4.kept, var1)) {
         again++;
         return var4.kept;
      }

      var0.updateLights();
      ModelSlotRenderData var6 = ModelSlotRenderData.alloc();
      var6.initModel(var1);
      var6.init(var1);
      var1.renderRefCount++;
      if (var4.kept != null) {
         KeptSnapshots.replace(var4.kept);
      }

      var4.kept = var6;
      var4.x = var0.getX();
      var4.y = var0.getY();
      var4.z = var0.getZ();
      var4.angle = var0.getAngleY();
      var4.taken = frame;
      KeptSnapshots.keep(var6);
      fresh++;
      return var6;
   }

   private static boolean still(VehicleRate.Entry var0, BaseVehicle var1) {
      return Math.abs(var1.getX() - var0.x) < 0.001F
         && Math.abs(var1.getY() - var0.y) < 0.001F
         && Math.abs(var1.getZ() - var0.z) < 0.001F
         && Math.abs(var1.getAngleY() - var0.angle) < 0.001F;
   }

   private static boolean sameParts(ModelSlotRenderData var0, ModelSlot var1) {
      instances.clear();
      if (ready(var1.model)) {
         instances.add(var1.model);
      }

      listed(var1.sub);
      if (instances.size() != var0.modelData.size()) {
         return false;
      }

      for (int var2 = 0; var2 < instances.size(); var2++) {
         if (((ModelInstanceRenderData)var0.modelData.get(var2)).modelInstance != instances.get(var2)) {
            return false;
         }
      }

      return true;
   }

   private static void listed(ArrayList<ModelInstance> var0) {
      for (int var1 = 0; var1 < var0.size(); var1++) {
         ModelInstance var2 = (ModelInstance)var0.get(var1);
         if (ready(var2)) {
            instances.add(var2);
            listed(var2.sub);
         }
      }
   }

   private static boolean ready(ModelInstance var0) {
      return var0.model.isReady() && (var0.animPlayer == null || var0.animPlayer.isReady());
   }

   static void end() {
      Profile.vehicleSnapshots(fresh, again);
      Iterator var0 = entries.values().iterator();

      while (var0.hasNext()) {
         VehicleRate.Entry var1 = (VehicleRate.Entry)var0.next();
         if (var1.seen != frame) {
            if (var1.kept != null) {
               KeptSnapshots.replace(var1.kept);
            }

            var0.remove();
         }
      }
   }

   static void clear() {
      for (VehicleRate.Entry var1 : entries.values()) {
         if (var1.kept != null) {
            KeptSnapshots.replace(var1.kept);
         }
      }

      entries.clear();
   }

   private static int interval(float var0) {
      int var1 = 0;

      while (var1 < REACH.length && var0 > REACH[var1]) {
         var1++;
      }

      return INTERVAL[var1];
   }

   private VehicleRate() {
   }

   private static final class Entry {
      ModelSlotRenderData kept;
      float x;
      float y;
      float z;
      float angle;
      long taken;
      long seen;
   }
}
