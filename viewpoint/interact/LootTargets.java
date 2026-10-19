package viewpoint.interact;

import java.util.ArrayList;
import viewpoint.core.Frame;
import viewpoint.core.View;
import viewpoint.input.Look;
import viewpoint.visibility.Edges;
import viewpoint.visibility.Owner;
import viewpoint.world.TileMeshes;
import zombie.characters.IsoPlayer;
import zombie.characters.animals.IsoAnimal;
import zombie.core.properties.PropertyContainer;
import zombie.inventory.ItemContainer;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.iso.LosUtil.TestResults;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.areas.SafeHouse;
import zombie.iso.objects.IsoBarricade;
import zombie.iso.objects.IsoCurtain;
import zombie.iso.objects.IsoDeadBody;
import zombie.iso.objects.IsoDoor;
import zombie.iso.objects.IsoLightSwitch;
import zombie.iso.objects.IsoThumpable;
import zombie.iso.objects.IsoTree;
import zombie.iso.objects.IsoWindow;
import zombie.iso.objects.IsoWindowFrame;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.iso.sprite.IsoSprite;
import zombie.network.GameClient;
import zombie.util.list.PZArrayList;
import zombie.vehicles.BaseVehicle;
import zombie.vehicles.VehiclePart;

final class LootTargets {
   static final int GROUND = 0;
   static final int BODY = 1;
   static final int OBJECT = 2;
   static final int VEHICLE = 3;
   static final int THING = 4;
   static final int ANIMAL = 5;
   static final int PERSON = 6;
   private static final float ITEM_HALF = 0.2F;
   private static final float ITEM_TALL = 0.3F;
   private static final float BODY_HALF = 0.6F;
   private static final float BODY_TALL = 0.4F;
   private static final float BEHIND = 0.5F;
   private static final float CHEST = 1.0F;
   private static final float ANIMAL_LEAST = 0.15F;
   private static final float TRUNK_HALF = 0.25F;
   private static final float TRUNK_TALL = 2.4494896F;
   private static final int PERSON_REACH = 2;
   private static final float PERSON_HALF = 0.3F;
   private static final float PERSON_TALL = 1.6F;
   private static final ArrayList<LootTargets.Target> found = new ArrayList<>();
   private static final ArrayList<LootTargets.Target> spare = new ArrayList<>();
   private static final ArrayList<BaseVehicle> vehicles = new ArrayList<>();
   private static final ArrayList<IsoAnimal> squareAnimals = new ArrayList<>();
   private static final float[] ray = new float[6];

   static LootTargets.Target aimed(Frame var0, IsoPlayer var1, LootTargets.Target var2) {
      aim(var0);
      collect(var1);
      byte var3 = -1;

      for (int var4 = 0; var4 < found.size() && var2 != null; var4++) {
         var3 = (byte)(found.get(var4).same(var2) ? var4 : var3);
      }

      int var5 = LootAim.pick(ray, LootBoxes.all(), found.size(), var3, from(var1));
      return var5 < 0 ? null : found.get(var5);
   }

   static int owner(LootTargets.Target var0) {
      int var1 = Math.floorMod(var0.square.x, 8);
      int var2 = Math.floorMod(var0.square.y, 8);
      IsoSprite var3 = var0.object != null && var0.kind != 1 && !(var0.object instanceof IsoTree) ? var0.object.getSprite() : null;
      int var4 = var3 == null ? 0 : Edges.ownerKind(var3);
      return Owner.of(var2 * 8 + var1, var4 == -1 ? 0 : var4);
   }

   private static void collect(IsoPlayer var0) {
      for (LootTargets.Target var2 : found) {
         var2.clear();
      }

      spare.addAll(found);
      found.clear();
      vehicles.clear();
      IsoGridSquare var5 = var0.getCurrentSquare();
      if (var5 != null) {
         InteractActions.standing(var0, var5);

         for (int var6 = -1; var6 <= 1; var6++) {
            for (int var3 = -1; var3 <= 1; var3++) {
               IsoGridSquare var4 = IsoWorld.instance.currentCell.getGridSquare(var5.x + var3, var5.y + var6, var5.z);
               if (var4 != null && (!GameClient.client || SafeHouse.isSafehouseAllowLoot(var4, var0))) {
                  if (inSight(var5, var4, var3, var6)) {
                     animals(var4);
                  }

                  if (var4 != var5 && !var5.canReachTo(var4)) {
                     if (var3 == 0 && var6 == 1 || var3 == 1 && var6 == 0) {
                        across(var4, var6 == 1 ? 1 : 2);
                     }
                  } else {
                     ground(var4);
                     bodies(var4);
                     objects(var4, var0);
                     vehicle(var4.getVehicleContainer(), var0);
                  }
               }
            }
         }

         people(var0, var5);
      }
   }

   private static void people(IsoPlayer var0, IsoGridSquare var1) {
      for (int var2 = -2; var2 <= 2; var2++) {
         for (int var3 = -2; var3 <= 2; var3++) {
            IsoGridSquare var4 = IsoWorld.instance.currentCell.getGridSquare(var1.x + var3, var1.y + var2, var1.z);
            ArrayList var5 = var4 == null ? null : var4.getMovingObjects();

            for (int var6 = 0; var5 != null && var6 < var5.size(); var6++) {
               if (var5.get(var6) instanceof IsoPlayer var7 && var7 != var0 && !var7.isAnimal() && !var7.isDead() && var0.CanSee(var7)) {
                  float var11 = var7.getX();
                  float var9 = var7.getY();
                  float var10 = var7.getZ() * 2.4494896F;
                  LootBoxes.grow(add(6, null, var7), var11 - 0.3F, var9 - 0.3F, var10, var11 + 0.3F, var9 + 0.3F, var10 + 1.6F);
               }
            }
         }
      }
   }

   private static void ground(IsoGridSquare var0) {
      ArrayList var1 = var0.getWorldObjects();
      int var2 = -1;

      for (int var3 = 0; var3 < var1.size(); var3++) {
         IsoWorldInventoryObject var4 = (IsoWorldInventoryObject)var1.get(var3);
         if (var4.getItem() != null) {
            if (var2 < 0) {
               var2 = add(0, var0, null);
            }

            float var5 = var4.getX() + var4.xoff;
            float var6 = var4.getY() + var4.yoff;
            float var7 = (var4.getZ() + var4.zoff) * 2.4494896F;
            LootBoxes.grow(var2, var5 - 0.2F, var6 - 0.2F, var7, var5 + 0.2F, var6 + 0.2F, var7 + 0.3F);
         }
      }
   }

   private static void bodies(IsoGridSquare var0) {
      ArrayList var1 = var0.getStaticMovingObjects();

      for (int var2 = 0; var2 < var1.size(); var2++) {
         IsoMovingObject var3 = (IsoMovingObject)var1.get(var2);
         if (var3.getContainer() != null) {
            if (var3 instanceof IsoDeadBody var4 && var4.isAnimal()) {
               break;
            }

            int var8 = add(1, var0, var3);
            found.get(var8).containers.add(var3.getContainer());
            float var5 = var3.getX();
            float var6 = var3.getY();
            float var7 = var0.z * 2.4494896F;
            LootBoxes.grow(var8, var5 - 0.6F, var6 - 0.6F, var7, var5 + 0.6F, var6 + 0.6F, var7 + 0.4F);
         }
      }
   }

   private static void objects(IsoGridSquare var0, IsoPlayer var1) {
      PZArrayList var2 = var0.getObjects();

      for (int var3 = 0; var3 < var2.size(); var3++) {
         IsoObject var4 = (IsoObject)var2.get(var3);
         int var5 = var4.getContainerCount();
         if (var5 == 0) {
            if (var4 instanceof IsoTree) {
               tree(var0, var4);
            } else if (aimable(var4)) {
               LootBoxes.shape(add(4, var0, var4), var4, var0);
            }
         } else {
            int var6 = add(2, var0, var4);
            LootTargets.Target var7 = found.get(var6);

            for (int var8 = 0; var8 < var5; var8++) {
               var7.containers.add(var4.getContainerByIndex(var8));
            }

            var7.locked = var4 instanceof IsoThumpable var9 && var9.isLockedToCharacter(var1);
            LootBoxes.shape(var6, var4, var0);
         }
      }
   }

   private static void tree(IsoGridSquare var0, IsoObject var1) {
      float var2 = var0.x + 0.5F;
      float var3 = var0.y + 0.5F;
      float var4 = var0.z * 2.4494896F;
      if (!InteractActions.passes(var1) && (!(Math.abs(ray[0] - var2) < 0.25F) || !(Math.abs(ray[1] - var3) < 0.25F))) {
         LootBoxes.grow(add(4, var0, var1), var2 - 0.25F, var3 - 0.25F, var4, var2 + 0.25F, var3 + 0.25F, var4 + 2.4494896F);
      }
   }

   private static void across(IsoGridSquare var0, int var1) {
      PZArrayList var2 = var0.getObjects();

      for (int var3 = 0; var3 < var2.size(); var3++) {
         IsoObject var4 = (IsoObject)var2.get(var3);
         if (var4.getContainerCount() == 0 && aimable(var4) && twoSided(var4) && Edges.ownerKind(var4.getSprite()) == var1) {
            LootBoxes.shape(add(4, var0, var4), var4, var0);
         }
      }
   }

   private static boolean twoSided(IsoObject var0) {
      PropertyContainer var1 = var0.getSprite().getProperties();
      return var0 instanceof IsoDoor
         || var0 instanceof IsoWindow
         || var0 instanceof IsoWindowFrame
         || var0 instanceof IsoThumpable
         || var1.has(IsoFlagType.HoppableN)
         || var1.has(IsoFlagType.HoppableW)
         || var1.has(IsoFlagType.TallHoppableN)
         || var1.has(IsoFlagType.TallHoppableW);
   }

   private static void vehicle(BaseVehicle var0, IsoPlayer var1) {
      if (var0 != null && !vehicles.contains(var0)) {
         vehicles.add(var0);
         int var2 = add(3, null, var0);

         for (int var3 = 0; var3 < var0.getPartCount(); var3++) {
            VehiclePart var4 = var0.getPartByIndex(var3);
            if (var4.getItemContainer() != null && var0.canAccessContainer(var3, var1)) {
               found.get(var2).containers.add(var4.getItemContainer());
            }
         }

         LootBoxes.footprint(var2, var0);
      }
   }

   private static boolean inSight(IsoGridSquare var0, IsoGridSquare var1, int var2, int var3) {
      return var1 == var0 || var0.testVisionAdjacent(var2, var3, 0, true, false) != TestResults.Blocked;
   }

   private static void animals(IsoGridSquare var0) {
      var0.getAnimals(squareAnimals);

      for (int var1 = 0; var1 < squareAnimals.size(); var1++) {
         IsoAnimal var2 = squareAnimals.get(var1);
         if (!var2.isDead()) {
            int var3 = add(5, null, var2);
            float var4 = Math.max(0.15F, var2.getWidth());
            LootBoxes.animal(var3, var2.getX(), var2.getY(), var2.getZ() * 2.4494896F, var4, var2.getForwardDirectionX(), var2.getForwardDirectionY());
         }
      }

      squareAnimals.clear();
   }

   private static boolean aimable(IsoObject var0) {
      return usable(var0) && !InteractActions.passes(var0);
   }

   private static boolean usable(IsoObject var0) {
      IsoSprite var1 = var0.getSprite();
      if (var1 != null
         && !var1.solidfloor
         && !(var0 instanceof IsoWorldInventoryObject)
         && !(var0 instanceof IsoTree)
         && !(var0 instanceof IsoCurtain)
         && !(var0 instanceof IsoBarricade)
         && !(var0 instanceof IsoWindowFrame var2 && var2.hasWindow())
         && !TileMeshes.isPlant(var1)) {
         PropertyContainer var3 = var1.getProperties();
         if (var3.has(IsoFlagType.solidfloor)
            || var3.has(IsoFlagType.FloorOverlay)
            || var3.has(IsoFlagType.transparentFloor)
            || var3.has(IsoFlagType.attachedFloor)
            || var3.has(IsoFlagType.IsFloorAttached)) {
            return false;
         } else {
            return !(var0 instanceof IsoWindow)
                  && !(var0 instanceof IsoDoor)
                  && !(var0 instanceof IsoThumpable)
                  && !(var0 instanceof IsoLightSwitch)
                  && !var3.has(IsoFlagType.WindowN)
                  && !var3.has(IsoFlagType.WindowW)
                  && !var3.has(IsoFlagType.HoppableN)
                  && !var3.has(IsoFlagType.HoppableW)
               ? !var3.has(IsoFlagType.WallN)
                  && !var3.has(IsoFlagType.WallW)
                  && !var3.has(IsoFlagType.WallNW)
                  && !var3.has(IsoFlagType.WallSE)
                  && !var3.has(IsoFlagType.WallNTrans)
                  && !var3.has(IsoFlagType.WallWTrans)
                  && !var3.has(IsoFlagType.WallOverlay)
                  && !var3.has(IsoFlagType.DoorWallN)
                  && !var3.has(IsoFlagType.DoorWallW)
               : true;
         }
      } else {
         return false;
      }
   }

   private static int add(int var0, IsoGridSquare var1, IsoObject var2) {
      LootTargets.Target var3 = spare.isEmpty() ? new LootTargets.Target() : spare.remove(spare.size() - 1);
      var3.clear();
      var3.kind = var0;
      var3.square = var1;
      var3.object = var2;
      var3.locked = false;
      found.add(var3);
      int var4 = found.size() - 1;
      LootBoxes.empty(var4);
      return var4;
   }

   private static void aim(Frame var0) {
      float var1 = Look.yaw;
      float var2 = Look.pitch;
      float var3 = (float)Math.cos(var2);
      ray[0] = var0.camX - var0.eyeX + View.cameraX;
      ray[1] = var0.camY - var0.eyeZ + View.cameraY;
      ray[2] = var0.camZ * 2.4494896F + var0.eyeY + View.cameraUp;
      ray[3] = (float)Math.cos(var1) * var3;
      ray[4] = (float)Math.sin(var1) * var3;
      ray[5] = (float)Math.sin(var2);
   }

   private static float from(IsoPlayer var0) {
      float var1 = var0.getX() - ray[0];
      float var2 = var0.getY() - ray[1];
      float var3 = var0.getZ() * 2.4494896F + 1.0F - ray[2];
      return Math.max(0.0F, var1 * ray[3] + var2 * ray[4] + var3 * ray[5] - 0.5F);
   }

   private LootTargets() {
   }

   static final class Target {
      int kind;
      IsoGridSquare square;
      IsoObject object;
      boolean locked;
      final ArrayList<ItemContainer> containers = new ArrayList<>();

      boolean same(LootTargets.Target var1) {
         return var1 != null && this.kind == var1.kind && this.square == var1.square && this.object == var1.object;
      }

      void set(LootTargets.Target var1) {
         this.kind = var1.kind;
         this.square = var1.square;
         this.object = var1.object;
         this.locked = var1.locked;
         this.containers.clear();
         this.containers.addAll(var1.containers);
      }

      void clear() {
         this.square = null;
         this.object = null;
         this.containers.clear();
      }
   }
}
