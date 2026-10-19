package viewpoint.light;

import java.util.ArrayList;
import viewpoint.core.Frame;
import viewpoint.core.WallSide;
import viewpoint.render.SceneData;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.core.skinnedmodel.model.Model;
import zombie.inventory.InventoryItem;
import zombie.iso.Vector3;

public final class Flashlight {
   static boolean active;
   private static final ArrayList<InventoryItem> items = new ArrayList<>();
   private static final String FIRST_HAND = "Bip01_Prop1";
   private static final String SECOND_HAND = "Bip01_Prop2";
   private static final String CHEST = "Bip01_Spine1";
   private static final float WALL_MARGIN = 0.05F;
   private static final Vector3 at = new Vector3();
   private static final float[] kept = new float[2];

   public static void snapshot(Frame var0, IsoGameCharacter var1) {
      SceneData var2 = var0.scene;
      var2.spots.flashOn = false;
      var2.spots.flashAtItem = false;
      items.clear();
      var1.getActiveLightItems(items);
      InventoryItem var3 = null;

      for (int var4 = 0; var4 < items.size(); var4++) {
         InventoryItem var5 = items.get(var4);
         if (var5.isTorchCone() && (var3 == null || var5.getLightStrength() > var3.getLightStrength())) {
            var3 = var5;
         }
      }

      items.clear();
      boolean var6 = var1 instanceof IsoPlayer var7 && var7.getVehicle() != null && !var7.isAiming();
      active = var3 != null && !var1.isDead() && !var6;
      if (active) {
         var2.spots.flashOn = true;
         var2.spots.flashStrength = var3.getLightStrength();
         var2.spots.flashRange = Math.max(4.0F, var3.getLightDistance());
         var2.spots.flashCos = Math.max(0.2F, Math.min(0.97F, var3.getTorchDot()));
         var2.spots.flashAtItem = place(var0, var1, var3);
      }
   }

   private static boolean place(Frame var0, IsoGameCharacter var1, InventoryItem var2) {
      if (!var1.hasActiveModel()) {
         return false;
      }

      String var3 = var1.getPrimaryHandItem() == var2 ? "Bip01_Prop1" : (var1.getSecondaryHandItem() == var2 ? "Bip01_Prop2" : "Bip01_Spine1");
      int var4 = var1.getAnimationPlayer().getSkinningBoneIndex(var3, -1);
      if (var4 < 0) {
         return false;
      }

      Model.boneToWorldCoords(var1, var4, at);
      WallSide.keep(var1, at.x, at.y, 0.05F, kept);
      at.x = kept[0];
      at.y = kept[1];
      SceneData var5 = var0.scene;
      var5.spots.flashItemX = -(at.x - var0.camX);
      var5.spots.flashItemY = (at.z - var0.camZ) * 2.4494896F + Lights.lift(var5, var1.getX(), var1.getY());
      var5.spots.flashItemZ = -(at.y - var0.camY);
      return true;
   }

   private Flashlight() {
   }
}
