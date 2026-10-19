package viewpoint.models;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import viewpoint.core.Frame;
import viewpoint.core.View;
import viewpoint.input.Eye;
import viewpoint.input.FreeCam;
import viewpoint.visibility.Rooms;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.characters.animals.IsoAnimal;
import zombie.core.PerformanceSettings;
import zombie.core.skinnedmodel.ModelManager.ModelSlot;
import zombie.core.skinnedmodel.animation.AnimationPlayer;
import zombie.core.skinnedmodel.model.ModelSlotRenderData;
import zombie.iso.IsoCell;
import zombie.iso.IsoMovingObject;
import zombie.iso.fboRenderChunk.FBORenderObjectOutline;
import zombie.ui.UIManager;
import zombie.vehicles.BaseVehicle;

public final class Characters {
   private static final float MODEL_RANGE = 124.0F;
   private static final float BEHIND = -0.17F;
   private static final float VEHICLE_ALWAYS = 12.0F;
   private static final float BLOB_RADIUS = 0.45F;
   private static final Matrix4f eyeSeat = new Matrix4f();
   private static final Vector3f head = new Vector3f();
   private static final Vector3f heading = new Vector3f();
   private static final String[] HEAD_BONES = new String[]{"Bip01_Head", "Bip01_HeadNub"};

   public static void snapshot(Frame var0, IsoCell var1, IsoGameCharacter var2) {
      ModelCull.range = Math.min(124.0F, var0.scene.fogEnd);
      CharacterRate.begin();
      CharacterLooks.begin();
      float var3 = (float)Math.cos(var0.viewYaw);
      float var4 = (float)Math.sin(var0.viewYaw);
      float var5 = var0.camX + View.cameraX;
      float var6 = var0.camY + View.cameraY;

      for (IsoMovingObject var8 : var1.getObjectList()) {
         if (var8 instanceof IsoGameCharacter var9
            && var9.hasActiveModel()
            && inRange(var9, var0.camX, var0.camY, ModelCull.range)
            && (var9 == var2 || !behind(var9, var5, var6, var3, var4))) {
            if (var9 != var2 && ModelCull.roomHidden(var9)) {
               Rooms.leftOut();
            } else {
               ModelSlot var10 = var9.legsSprite.modelSlot;
               if (var10.model != null && var10.model.object != null) {
                  float var11 = var9.getX() - var0.camX;
                  float var12 = var9.getY() - var0.camY;
                  float var13 = (float)Math.sqrt(var11 * var11 + var12 * var12);
                  if (CharacterLooks.ready(var9, var10.model, var13)) {
                     ModelCapture.Pose var14 = CharacterRate.pose(var9, var10, var13, var9 == var2);
                     boolean var15 = var14 != ModelCapture.Pose.SNAPSHOT;
                     ModelSlotRenderData var16 = var15 ? CharacterRate.kept(var9) : fresh(var0, var10, var9, var2);
                     float var17 = var15 ? var9.getX() : var16.x;
                     float var18 = var15 ? var9.getY() : var16.y;
                     float var19 = var15 ? var9.getZ() + CharacterRate.lift(var9) : var16.z;
                     float var20 = var15 ? var9.getAnimationPlayer().getRenderedAngle() : var16.animPlayerAngle;
                     if (!var16.inVehicle) {
                        var0.scene.blob(-(var17 - var0.camX), (var19 - var0.camZ) * 2.4494896F, -(var18 - var0.camY), 0.45F);
                     }

                     boolean var21 = var9 == var2 && !FreeCam.active && !var0.cameraShot.on;
                     int var22 = var0.scene.models.count();
                     ModelCapture.capture(var0, var16, var9, var9 == var2, var21, var14, var17, var18, var19, var20);
                     if (var9 != var2 && (!var16.inVehicle || var9.getVehicle() == null || var9.getVehicle() != var2.getVehicle())) {
                        if (var9 instanceof IsoZombie || var9 instanceof IsoPlayer && !(var9 instanceof IsoAnimal)) {
                           var0.scene.models.whole(var22);
                        }
                     } else {
                        var0.scene.models.own(var22);
                     }

                     if (var9 == var0.lootCharacter) {
                        var0.scene.models.target(var22);
                     }

                     CharacterRate.drawn(var14);
                     KeptSnapshots.use(var0, var16);
                  }
               }
            }
         }
      }

      CharacterRate.end();
      CharacterLooks.end();
   }

   private static ModelSlotRenderData fresh(Frame var0, ModelSlot var1, IsoGameCharacter var2, IsoGameCharacter var3) {
      var1.model.updateLights();
      ModelSlotRenderData var4 = ModelSlotRenderData.alloc();
      var4.initModel(var1);
      var4.init(var1);
      var4.alpha = 1.0F;
      var1.renderRefCount++;
      CharacterRate.took(var2, var4);
      if (var2 == var3 && var4.inVehicle) {
         seatedEye(var0, var4, var2);
      }

      return var4;
   }

   public static void vehicles(Frame var0, IsoCell var1, IsoGameCharacter var2) {
      float var3 = (float)Math.cos(var0.viewYaw);
      float var4 = (float)Math.sin(var0.viewYaw);
      float var5 = var0.camX + View.cameraX;
      float var6 = var0.camY + View.cameraY;
      VehicleRate.begin();

      for (BaseVehicle var8 : var1.getVehicles()) {
         if (var8.sprite != null
            && var8.sprite.hasActiveModel()
            && inRange(var8, var0.camX, var0.camY, var0.scene.fogEnd)
            && (inRange(var8, var0.camX, var0.camY, 12.0F) || !behind(var8, var5, var6, var3, var4))) {
            ModelSlot var9 = var8.sprite.modelSlot;
            if (var9 != null && var9.model != null && var9.model.object != null) {
               float var10 = var8.getX() - var0.camX;
               float var11 = var8.getY() - var0.camY;
               boolean var12 = var8 == var2.getVehicle() || var8 == var0.lootVehicle;
               ModelSlotRenderData var13 = VehicleRate.snapshot(var8, var9, (float)Math.sqrt(var10 * var10 + var11 * var11), var12);
               int var14 = var0.scene.models.count();
               VehicleCapture.capture(var0, var13, var8);
               KeptSnapshots.use(var0, var13);
               if (var8 == var2.getVehicle()) {
                  var0.scene.models.own(var14);
               }

               if (var8 == var0.lootVehicle) {
                  var0.scene.models.target(var14);
               }
            }
         }
      }

      VehicleRate.end();
   }

   public static void outlinesShown(IsoCell var0, int var1) {
      FBORenderObjectOutline var2 = FBORenderObjectOutline.getInstance();

      for (IsoMovingObject var4 : var0.getObjectList()) {
         if (var4.isOutlineHighlight(var1)) {
            if (PerformanceSettings.fboRenderChunk) {
               long var5 = var2.getDuringUIRenderTime(var1, var4);
               long var7 = var2.getDuringUIUpdateTime(var1, var4);
               if (var5 != 0L && var5 == UIManager.uiRenderTimeMS || var7 != 0L && var7 == UIManager.uiUpdateTimeMS) {
                  continue;
               }
            }

            var4.setOutlineHighlight(var1, false);
         }
      }
   }

   private static boolean behind(IsoMovingObject var0, float var1, float var2, float var3, float var4) {
      float var5 = var0.getX() - var1;
      float var6 = var0.getY() - var2;
      float var7 = (float)Math.sqrt(var5 * var5 + var6 * var6);
      return var7 > 3.0F && (var5 * var3 + var6 * var4) / var7 < -0.17F;
   }

   private static boolean inRange(IsoMovingObject var0, float var1, float var2, float var3) {
      float var4 = var0.getX() - var1;
      float var5 = var0.getY() - var2;
      return var4 * var4 + var5 * var5 <= var3 * var3;
   }

   private static void seatedEye(Frame var0, ModelSlotRenderData var1, IsoGameCharacter var2) {
      AnimationPlayer var3 = var2.getAnimationPlayer();
      int var4 = var3 == null ? -1 : var3.getSkinningBoneIndex(HEAD_BONES[0], -1);
      if (var4 >= 0) {
         org.lwjgl.util.vector.Matrix4f var5 = var3.getModelTransformAt(var4);
         head.set(var5.m03 * 1.5F, var5.m13 * 1.5F, var5.m23 * 1.5F);
         BaseVehicle var6 = var2.getVehicle();
         seatMatrix(eyeSeat, var1, var0.camX, var0.camY, var0.camZ);
         VehicleTilt.onRelief(eyeSeat, var0, var6, var1.x, var1.y, var1.z).transformPosition(head);
         Eye.atHead(var2, var0, head.x, head.y, head.z);
         if (var6 != null) {
            var6.getForwardVector(heading);
            VehicleTilt.seatView(var0, var6, var1.x, var1.y, heading.x, heading.z);
         }
      }
   }

   static Matrix4f seatMatrix(Matrix4f var0, ModelSlotRenderData var1, float var2, float var3, float var4) {
      return var0.translation(-(var1.x - var2), (var1.z - var4) * 2.4494896F + BaseVehicle.centerOfMassMagic, -(var1.y - var3))
         .scale(-1.0F, 1.0F, 1.0F)
         .rotateY((float) Math.PI)
         .translate(0.0F, var1.centerOfMassY, 0.0F)
         .rotateZ((float)Math.toRadians(var1.vehicleAngleZ))
         .rotateY((float)Math.toRadians(var1.vehicleAngleY))
         .rotateX((float)Math.toRadians(var1.vehicleAngleX))
         .rotateY((float) Math.PI)
         .translate(var1.inVehicleX, var1.inVehicleY, -var1.inVehicleZ);
   }

   public static void forget() {
      CharacterRate.clear();
      VehicleRate.clear();
   }

   public static void release(Frame var0) {
      for (int var1 = 0; var1 < var0.snapshots.size(); var1++) {
         ModelSlotRenderData var2 = var0.snapshots.get(var1);
         if (KeptSnapshots.release(var2)) {
            var2.postRender();
         }
      }

      var0.snapshots.clear();
   }

   private Characters() {
   }
}
