package viewpoint.gunplay;

import zombie.CombatManager;
import zombie.ai.states.FakeDeadZombieState;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoZombie;
import zombie.characters.animals.IsoAnimal;
import zombie.core.physics.BallisticsController;
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoCell;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoUtils;
import zombie.iso.Vector3;
import zombie.network.fields.hit.HitInfo;
import zombie.util.list.PZArrayList;
import zombie.vehicles.BaseVehicle;

final class Gathering {
   private static final double WIDEST = 1.0;
   private static final double BODY_REACH = 1.5;
   private static final double VEHICLE_REACH = 3.0;
   private static final double MIDDLE = 0.9;
   private static final double VEHICLE_MIDDLE = 0.8;
   private static final float LEVEL_SQUARES = 3.0F;
   private final Sight line = new Sight();
   private final IsoMovingObject[] found = new IsoMovingObject[32];
   private final double[] off = new double[32];
   private final double[] ahead = new double[32];
   private int count;

   void add(IsoGameCharacter var1, HandWeapon var2, double[] var3, double var4, double var6, PZArrayList<HitInfo> var8) {
      IsoCell var9 = var1.getCell();
      if (var9 != null) {
         this.line.set(var3, var4, var6, 0.0, 0.0);
         float var10 = var2.getMaxRange(var1) * var2.getRangeMod(var1);
         Vector3 var11 = muzzle(var1);
         this.count = 0;

         for (IsoMovingObject var13 : var9.getObjectList()) {
            if (var13 instanceof IsoGameCharacter var14 && !(var13 instanceof IsoAnimal) && target(var1, var14)) {
               this.consider(var14, var11, var10, Ground.under(var14) + 0.9, 1.5);
            }
         }

         for (BaseVehicle var21 : var9.getVehicles()) {
            if (var21 != var1.getVehicle() && var21.getCurrentSquare() != null && CombatManager.checkPVP(var1, var21, false)) {
               this.consider(var21, var11, var10, Ground.under(var21) + 0.8, 3.0);
            }
         }

         CombatManager var20 = CombatManager.getInstance();

         for (int var22 = 0; var22 < this.count; var22++) {
            IsoMovingObject var23 = this.found[var22];
            float var15 = var23.getX();
            float var16 = var23.getY();
            float var17 = var23.getZ();
            float var18 = IsoUtils.DistanceToSquared(
               var1.getX(), var1.getY(), (float)Math.floor(var1.getZ()) * 3.0F, var15, var16, (float)Math.floor(var17) * 3.0F
            );
            var8.add(((HitInfo)var20.hitInfoPool.alloc()).init(var23, var1.getDotWithForwardDirection(var15, var16), var18, var15, var16, var17));
            this.found[var22] = null;
         }

         this.count = 0;
      }
   }

   static void dedupe(PZArrayList<HitInfo> var0) {
      CombatManager var1 = CombatManager.getInstance();

      for (int var2 = var0.size() - 1; var2 > 0; var2--) {
         HitInfo var3 = (HitInfo)var0.get(var2);
         IsoMovingObject var4 = var3.getObject();

         for (int var5 = 0; var4 != null && var5 < var2; var5++) {
            if (((HitInfo)var0.get(var5)).getObject() == var4) {
               var1.hitInfoPool.release((HitInfo)var0.set(var5, var3));
               var0.remove(var2);
               break;
            }
         }
      }
   }

   private static boolean target(IsoGameCharacter var0, IsoGameCharacter var1) {
      if (var1 != var0 && !var1.isDead() && (!var1.isGodMod() || var1.isZombie()) && var1.getCurrentSquare() != null) {
         return !(var1 instanceof IsoZombie var2 && (var2.isCurrentState(FakeDeadZombieState.instance()) || var2.isReanimatedForGrappleOnly()))
            ? CombatManager.checkPVP(var0, var1, false)
            : false;
      } else {
         return false;
      }
   }

   private void consider(IsoMovingObject var1, Vector3 var2, float var3, double var4, double var6) {
      if (!(var2.distanceTo(var1.getX(), var1.getY(), var1.getZ()) > var3)) {
         double var8 = var1.getZ() * 2.4494896F + var4;
         double var10 = near(this.line, var1.getX(), var1.getY(), var8, var6);
         if (!Double.isNaN(var10)) {
            double var12 = off(this.line, var1.getX(), var1.getY(), var8, var10);
            if (this.count != this.found.length || this.before(var12, var10, this.count - 1)) {
               int var14;
               for (var14 = Math.min(this.count, this.found.length - 1); var14 > 0 && this.before(var12, var10, var14 - 1); var14--) {
                  this.found[var14] = this.found[var14 - 1];
                  this.off[var14] = this.off[var14 - 1];
                  this.ahead[var14] = this.ahead[var14 - 1];
               }

               this.found[var14] = var1;
               this.off[var14] = var12;
               this.ahead[var14] = var10;
               this.count = Math.min(this.count + 1, this.found.length);
            }
         }
      }
   }

   private boolean before(double var1, double var3, int var5) {
      return var1 < this.off[var5] || var1 == this.off[var5] && var3 < this.ahead[var5];
   }

   static double off(Sight var0, double var1, double var3, double var5, double var7) {
      double var9 = var1 - var0.x;
      double var11 = var3 - var0.y;
      double var13 = var5 - var0.up;
      return Math.sqrt(Math.max(0.0, var9 * var9 + var11 * var11 + var13 * var13 - var7 * var7)) / var7;
   }

   static double near(Sight var0, double var1, double var3, double var5, double var7) {
      double var9 = var0.along(var1, var3, var5);
      if (var9 <= 0.0) {
         return Double.NaN;
      }

      double var11 = var1 - var0.x;
      double var13 = var3 - var0.y;
      double var15 = var5 - var0.up;
      double var17 = Math.sqrt(Math.max(0.0, var11 * var11 + var13 * var13 + var15 * var15 - var9 * var9));
      double var19 = 1.0 * var9 + var7;
      return var17 <= var19 ? var9 : Double.NaN;
   }

   private static Vector3 muzzle(IsoGameCharacter var0) {
      BallisticsController var1 = var0.getBallisticsController();
      Vector3 var2 = var1 == null ? null : var1.getMuzzlePosition();
      return var2 == null || var2.x == 0.0F && var2.y == 0.0F ? new Vector3(var0.getX(), var0.getY(), var0.getZ()) : var2;
   }
}
