package viewpoint.light;

import java.util.ArrayList;
import java.util.Stack;
import viewpoint.core.Frame;
import viewpoint.platform.Tuning;
import viewpoint.render.SceneData;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.CharacterTimedActions.BaseAction;
import zombie.characters.IsoGameCharacter.TorchInfo;
import zombie.core.skinnedmodel.model.Model;
import zombie.inventory.InventoryItem;
import zombie.iso.LightingJNI;
import zombie.iso.Vector3;
import zombie.network.GameClient;
import zombie.scripting.objects.ItemTag;

public final class Torches {
   private static final int PER_PLAYER = 4;
   private static final int VEHICLE_IDS = 4096;
   private static final float HAND_HEIGHT = 1.1F;
   static final float NEAR = 24.0F;
   private static final float NARROWEST = 0.97F;
   private static final float WIDEST = 0.2F;
   private static final int EMBER = -1;
   private static final String SECOND_HAND = "Bip01_Prop2";
   private static final String FIRST_HAND = "Bip01_Prop1";
   private static final ArrayList<TorchInfo> listed = new ArrayList<>();
   private static final TorchInfo[] kept = new TorchInfo[8];
   private static final float[] distance = new float[8];
   private static final TorchInfo[] embers = new TorchInfo[8];
   private static final Vector3 hand = new Vector3();

   public static void snapshot(Frame var0, IsoGameCharacter var1, int var2) {
      SceneData var3 = var0.scene;
      var3.torchCount = 0;
      var3.muzzleShadow = MuzzleFlashes.snapshot(var0, var1);
      LightingJNI.getTorches(listed);
      int var4 = var1 instanceof IsoPlayer var5 && GameClient.client ? var5.onlineId + 1 : var2;
      int var11 = 0;

      for (int var6 = 0; var6 < listed.size(); var6++) {
         TorchInfo var7 = listed.get(var6);
         boolean var8 = var7.cone && var7.id > var4 * 4 && var7.id <= (var4 + 1) * 4;
         float var9 = beyond(var7, var0);
         if (!var8 && var7.id < 4096 && var7.dist > 0.0F && var9 <= 24.0F) {
            var11 = keep(var7, var9, var11);
         }
      }

      listed.clear();
      int var12 = 0;

      for (IsoPlayer var10 : IsoPlayer.players) {
         var12 = ember(var10, var12);
      }

      if (GameClient.client) {
         for (IsoPlayer var18 : GameClient.IDToPlayerMap.values()) {
            var12 = var18.isLocalPlayer() ? var12 : ember(var18, var12);
         }
      }

      for (int var15 = 0; var15 < var12; var15++) {
         float var19 = beyond(embers[var15], var0);
         var11 = var19 <= 24.0F ? keep(embers[var15], var19, var11) : var11;
      }

      for (int var16 = 0; var16 < var11; var16++) {
         if (var3.torchCount < 8) {
            write(var3, kept[var16], var0);
         }

         kept[var16] = null;
      }
   }

   private static float beyond(TorchInfo var0, Frame var1) {
      float var2 = var0.x - var1.camX;
      float var3 = var0.y - var1.camY;
      float var4 = (var0.z - var1.camZ) * 2.0F;
      return (float)Math.sqrt(var2 * var2 + var3 * var3 + var4 * var4) - var0.dist;
   }

   private static int ember(IsoPlayer var0, int var1) {
      if (var1 != embers.length && var0 != null && !var0.isDead() && var0.hasActiveModel()) {
         Stack var2 = var0.getCharacterActions();
         BaseAction var3 = var2.isEmpty() ? null : (BaseAction)var2.get(0);
         if (var3 != null && var3.overrideHandModels) {
            boolean var4 = smoked(var3.getSecondaryHandItem());
            if (!var4 && !smoked(var3.getPrimaryHandItem())) {
               return var1;
            }

            int var5 = var0.getAnimationPlayer().getSkinningBoneIndex(var4 ? "Bip01_Prop2" : "Bip01_Prop1", -1);
            if (var5 < 0) {
               return var1;
            }

            Model.boneToWorldCoords(var0, var5, hand);
            TorchInfo var6 = embers[var1] != null ? embers[var1] : new TorchInfo();
            embers[var1] = var6;
            var6.id = -1;
            var6.x = hand.x;
            var6.y = hand.y;
            var6.z = hand.z;
            var6.dist = Tuning.emberReach;
            var6.cone = false;
            var6.r = Tuning.emberR;
            var6.g = Tuning.emberG;
            var6.b = Tuning.emberB;
            var6.strength = Tuning.emberLight;
            return var1 + 1;
         } else {
            return var1;
         }
      } else {
         return var1;
      }
   }

   private static boolean smoked(InventoryItem var0) {
      return var0 != null && var0.hasTag(ItemTag.SMOKABLE);
   }

   static boolean round(SceneData var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      if (var0.torchCount == 8) {
         return false;
      }

      int var8 = var0.torchCount * 11;
      var0.torches[var8] = var1;
      var0.torches[var8 + 1] = var2;
      var0.torches[var8 + 2] = var3;
      var0.torches[var8 + 3] = var4;
      var0.torches[var8 + 4] = 0.0F;
      var0.torches[var8 + 5] = 0.0F;
      var0.torches[var8 + 6] = 0.0F;
      var0.torches[var8 + 7] = -1.0F;
      var0.torches[var8 + 8] = var5;
      var0.torches[var8 + 9] = var6;
      var0.torches[var8 + 10] = var7;
      var0.torchCount++;
      return true;
   }

   private static int keep(TorchInfo var0, float var1, int var2) {
      int var3 = var2;

      while (var3 > 0 && distance[var3 - 1] > var1) {
         var3--;
      }

      if (var3 == 8) {
         return var2;
      }

      int var4 = Math.min(var2, 7);
      System.arraycopy(distance, var3, distance, var3 + 1, var4 - var3);
      System.arraycopy(kept, var3, kept, var3 + 1, var4 - var3);
      distance[var3] = var1;
      kept[var3] = var0;
      return var4 + 1;
   }

   private static void write(SceneData var0, TorchInfo var1, Frame var2) {
      int var3 = var0.torchCount * 11;
      float var4 = var1.id == -1 ? 0.0F : 1.1F;
      float var5 = (float)Math.sqrt(var1.angleX * var1.angleX + var1.angleY * var1.angleY);
      boolean var6 = var1.cone && var5 > 0.0F;
      var0.torches[var3] = -(var1.x - var2.camX);
      var0.torches[var3 + 1] = (var1.z - var2.camZ) * 2.4494896F + var4 + Lights.lift(var0, var1.x, var1.y);
      var0.torches[var3 + 2] = -(var1.y - var2.camY);
      var0.torches[var3 + 3] = var1.dist;
      var0.torches[var3 + 4] = var6 ? -var1.angleX / var5 : 0.0F;
      var0.torches[var3 + 5] = 0.0F;
      var0.torches[var3 + 6] = var6 ? -var1.angleY / var5 : 0.0F;
      var0.torches[var3 + 7] = var6 ? Math.max(0.2F, Math.min(0.97F, var1.dot)) : -1.0F;
      var0.torches[var3 + 8] = var1.r * var1.strength;
      var0.torches[var3 + 9] = var1.g * var1.strength;
      var0.torches[var3 + 10] = var1.b * var1.strength;
      var0.torchCount++;
   }

   private Torches() {
   }
}
