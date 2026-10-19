package viewpoint.gunplay;

import viewpoint.input.CrosshairAim;
import zombie.characters.IsoGameCharacter;
import zombie.inventory.types.HandWeapon;

final class Aim {
   static final int NONE = -1;
   private static final double BEHIND = 0.3;
   private static final double REACH = 1.5;
   private static final double NEAREST = 1.0;
   static final double FLOOR = 0.9;
   static final double SOONER_LEAST = 0.1;
   static final double SOONER_MOST = 0.5;
   private static final double NO_RADIUS = 1.0E-9;
   int target;
   float chance;
   double radius;
   double distance;
   double from;
   double pellet;
   private final int[] part = new int[1];
   private final double[] offset = new double[2];
   private final Obstacles world = new Obstacles();

   void find(IsoGameCharacter var1, HandWeapon var2, Sight var3, Candidates var4, double var5) {
      this.pellet = var5;
      this.from = firstFrom(var1, var3);
      this.target = this.met(var3, var4);
      if (this.target != -1) {
         this.chance = Odds.chance(var1, var2, var4.entries[this.target]);
         this.distance = distance(var3, var4, this.target);
         this.radius = this.radius(var3, var4, this.target, this.chance, this.distance);
      } else if (!this.nearest(var1, var2, var3, var4)) {
         this.aimless(var1, var2, var3);
      }
   }

   static double firstFrom(IsoGameCharacter var0, Sight var1) {
      return Math.max(CrosshairAim.pastVehicle(), var1.along(var0.getAimOriginPosX(), var0.getAimOriginPosY(), var0.getAimOriginPosZ() * 2.4494896F) - 0.3);
   }

   int met(Sight var1, Candidates var2) {
      int var3 = -1;
      double var4 = Double.POSITIVE_INFINITY;

      for (int var6 = 0; var6 < var2.count; var6++) {
         double var7 = Parts.meet(var2.capsules[var6], var1.x, var1.y, var1.up, var1.fx, var1.fy, var1.fu, this.from, this.part);
         if (var7 < var4) {
            var4 = var7;
            var3 = var6;
         }
      }

      double var9 = var3 == -1 ? var4 : this.world.stop(var1.x, var1.y, var1.up, var1.fx, var1.fy, var1.fu, this.from, var4);
      if (var9 < var4) {
         ShotTrace.aimStopped(var2.characters[var3], var4, this.world, var9);
         return -1;
      } else {
         return var3;
      }
   }

   private boolean nearest(IsoGameCharacter var1, HandWeapon var2, Sight var3, Candidates var4) {
      double var5 = 1.5;
      this.target = -1;

      for (int var7 = 0; var7 < var4.count; var7++) {
         if (var3.offset(var4.middleX(var7), var4.middleY(var7), var4.middleUp(var7), this.offset)
            && !(var3.along(var4.middleX(var7), var4.middleY(var7), var4.middleUp(var7)) <= this.from)) {
            float var8 = Odds.chance(var1, var2, var4.entries[var7]);
            double var9 = distance(var3, var4, var7);
            double var11 = this.radius(var3, var4, var7, var8, var9);
            double var13 = Math.hypot(this.offset[0], this.offset[1]) / Math.max(var11 + this.pellet, 1.0E-9);
            if (var13 <= var5) {
               var5 = var13;
               this.target = var7;
               this.chance = var8;
               this.radius = var11;
               this.distance = var9;
            }
         }
      }

      return this.target != -1;
   }

   private void aimless(IsoGameCharacter var1, HandWeapon var2, Sight var3) {
      double var4 = CrosshairAim.depth();
      if (Double.isInfinite(var4)) {
         var4 = this.from + (var2.getMinSightRange(var1) + var2.getMaxSightRange(var1)) / 2.0 * Gunplay.RANGE.get();
      }

      var4 = Math.max(var4, this.from + 1.0);
      double var6 = var3.x + var3.fx * var4 - var1.getAimOriginPosX();
      double var8 = var3.y + var3.fy * var4 - var1.getAimOriginPosY();
      this.distance = var4;
      this.chance = Odds.chance(var1, var2, (float)Math.hypot(var6, var8));
      double var10 = -Math.atan2(var3.fu, Math.hypot(var3.fx, var3.fy));
      this.radius = this.cone(0, var10, var4, this.chance);
   }

   private static double distance(Sight var0, Candidates var1, int var2) {
      double var3 = var1.middleX(var2) - var0.x;
      double var5 = var1.middleY(var2) - var0.y;
      double var7 = var1.middleUp(var2) - var0.up;
      return Math.sqrt(var3 * var3 + var5 * var5 + var7 * var7);
   }

   private double radius(Sight var1, Candidates var2, int var3, float var4, double var5) {
      double var7 = Math.hypot(var2.middleX(var3) - var1.x, var2.middleY(var3) - var1.y);
      double var9 = var2.middleUp(var3) - var1.up;
      int var11 = var2.characters[var3].isProne() ? 1 : 0;
      return this.cone(var11, Math.atan2(-var9, var7), var5, var4);
   }

   double floor(IsoGameCharacter var1, HandWeapon var2, int var3) {
      double var4 = floorReach(var2.getMaxSightRange(var1) * Gunplay.RANGE.get(), var3);
      return this.cone(0, 0.0, var4, 0.9);
   }

   static double floorReach(double var0, int var2) {
      double var3 = Focus.skill(var2);
      return Math.max(1.0, var0 * (1.1 + 0.4 * var3 * var3));
   }

   private double cone(int var1, double var2, double var4, double var6) {
      double var8 = this.pellet > 0.0 ? Reference.centre(var1, var2, var4, var6, this.pellet * var4) : Reference.radius(var1, var2, var4, var6);
      return var8 / var4;
   }
}
