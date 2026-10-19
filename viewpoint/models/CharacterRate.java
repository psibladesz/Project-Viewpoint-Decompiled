package viewpoint.models;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Profile;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoZombie;
import zombie.core.skinnedmodel.ModelManager.ModelSlot;
import zombie.core.skinnedmodel.animation.AnimationPlayer;
import zombie.core.skinnedmodel.model.ModelSlotRenderData;

public final class CharacterRate {
   private static final float NEAR = 8.0F;
   private static final float[] REACH = new float[]{16.0F, 32.0F, 64.0F};
   private static final int[] INTERVAL = new int[]{2, 3, 4, 6};
   private static final LiveSettings.Number FULL = LiveSettings.number(
      "lod.fullRateCharacters", "Characters animated every frame, the nearest", "World/Levels of detail", 0.0F, 256.0F, 8.0F, 32.0F
   );
   private static final LiveSettings.Toggle POSED_KEPT = LiveSettings.toggle(
      "lod.keptSnapshotsPosed", "Zombies drawn in each new pose without a fresh snapshot", "World/Levels of detail", true
   );
   private static final Map<IsoGameCharacter, CharacterRate.Entry> entries = new IdentityHashMap<>();
   private static long frame;
   private static float fullReach = Float.MAX_VALUE;
   private static float[] distances = new float[256];
   private static int distanceCount;
   private static AnimationPlayer skipped;
   private static int fresh;
   private static int again;
   private static int posedAgain;

   static void begin() {
      frame++;
      distanceCount = 0;
      posedAgain = 0;
      again = 0;
      fresh = 0;
   }

   static void drawn(ModelCapture.Pose var0) {
      fresh = fresh + (var0 == ModelCapture.Pose.SNAPSHOT ? 1 : 0);
      again = again + (var0 == ModelCapture.Pose.LAST ? 1 : 0);
      posedAgain = posedAgain + (var0 == ModelCapture.Pose.PLAYER ? 1 : 0);
   }

   static ModelCapture.Pose pose(IsoGameCharacter var0, ModelSlot var1, float var2, boolean var3) {
      CharacterRate.Entry var4 = entries.computeIfAbsent(var0, var0x -> new CharacterRate.Entry());
      var4.seen = frame;
      boolean var5 = var0 instanceof IsoZombie && !var3 && !var0.isSeatedInVehicle() && !var0.isRagdollSimulationActive() && !var0.isOutlineHighlight(0);
      if (var5) {
         distances = distanceCount < distances.length ? distances : Arrays.copyOf(distances, distanceCount * 2);
         distances[distanceCount++] = var2;
      }

      int var6 = var5 && var2 > 8.0F && var2 > fullReach ? interval(var2) : 1;
      if (var4.interval == 1 && var6 > 1) {
         var4.posedAt = frame - (System.identityHashCode(var0) & 0xFF) % var6;
      }

      var4.interval = var6;
      if (var4.kept == null || var4.kept.modelSlot != var1 || var0.getTextureCreator() != null) {
         return ModelCapture.Pose.SNAPSHOT;
      } else if (!var4.posed && var6 > 1) {
         return ModelCapture.Pose.LAST;
      } else if (var5 && POSED_KEPT.get() && ModelCapture.posable(var4.kept, var1)) {
         var4.posed = false;
         return ModelCapture.Pose.PLAYER;
      } else {
         return ModelCapture.Pose.SNAPSHOT;
      }
   }

   static ModelSlotRenderData kept(IsoGameCharacter var0) {
      return entries.get(var0).kept;
   }

   static void took(IsoGameCharacter var0, ModelSlotRenderData var1) {
      CharacterRate.Entry var2 = entries.get(var0);
      if (var2.kept != null) {
         KeptSnapshots.replace(var2.kept);
      }

      var2.kept = var1;
      var2.lift = var1.z - var0.getZ();
      var2.posed = false;
      KeptSnapshots.keep(var1);
   }

   static float lift(IsoGameCharacter var0) {
      return entries.get(var0).lift;
   }

   static void end() {
      Profile.snapshots(fresh, again, posedAgain);
      int var0 = FULL.getInt();
      if (distanceCount > var0) {
         Arrays.sort(distances, 0, distanceCount);
         fullReach = var0 == 0 ? 0.0F : distances[var0 - 1];
      } else {
         fullReach = Float.MAX_VALUE;
      }

      Iterator var1 = entries.values().iterator();

      while (var1.hasNext()) {
         CharacterRate.Entry var2 = (CharacterRate.Entry)var1.next();
         if (var2.seen != frame) {
            if (var2.kept != null) {
               KeptSnapshots.replace(var2.kept);
            }

            var1.remove();
         }
      }
   }

   public static void clear() {
      for (CharacterRate.Entry var1 : entries.values()) {
         if (var1.kept != null) {
            KeptSnapshots.replace(var1.kept);
         }
      }

      entries.clear();
      fullReach = Float.MAX_VALUE;
   }

   public static void updating(ModelSlot var0) {
      CharacterRate.Entry var1 = var0.character == null ? null : entries.get(var0.character);
      if (var1 != null) {
         long var2 = frame + 1L;
         if (var1.interval != 1 && var2 - var1.posedAt < var1.interval) {
            AnimationPlayer var4 = var0.character.getAnimationPlayer();
            if (var4 != null && var4.updateBones && !var4.isRagdolling() && !var4.getMultiTrack().containsAnyRagdollTracks()) {
               var4.updateBones = false;
               skipped = var4;
            }
         } else {
            var1.posedAt = var2;
            var1.posed = true;
         }
      }
   }

   public static void updated() {
      if (skipped != null) {
         skipped.updateBones = true;
         skipped = null;
      }
   }

   private static int interval(float var0) {
      int var1 = 0;

      while (var1 < REACH.length && var0 > REACH[var1]) {
         var1++;
      }

      return INTERVAL[var1];
   }

   private CharacterRate() {
   }

   private static final class Entry {
      int interval = 1;
      long posedAt;
      boolean posed;
      long seen;
      ModelSlotRenderData kept;
      float lift;
   }
}
