package viewpoint.gunplay;

import zombie.core.physics.BallisticsController;
import zombie.inventory.types.HandWeapon;
import zombie.iso.Vector3;

final class Pattern {
   private static final double WEIGHT = 0.1;
   private static final int ENOUGH = 18;
   private static final int STRIDE = 4;
   private static final double NEAR = 0.5;
   private static String type;
   private static float projectiles;
   private static float weightCentre;
   private static int count;
   private static int seen;
   private static double variance;

   static void observe(BallisticsController var0, HandWeapon var1) {
      int var2 = var0.getNumberOfSpreadData();
      Vector3 var3 = var0.getMuzzlePosition();
      Vector3 var4 = var0.getMuzzleDirection();
      if (var2 > 0 && var3 != null && var4 != null) {
         if (!matches(var1)) {
            type = var1.getFullType();
            projectiles = var1.getProjectileSpread();
            weightCentre = var1.getProjectileWeightCenter();
            count = var1.getProjectileCount();
            seen = 0;
         }

         double var5 = var4.x;
         double var7 = var4.y;
         double var9 = var4.z * 2.4494896F;
         double var11 = Math.sqrt(var5 * var5 + var7 * var7 + var9 * var9);
         if (!(var11 <= 0.0)) {
            float[] var13 = var0.getBallisticsSpreadData();
            double var14 = 0.0;
            int var16 = 0;

            for (int var17 = 0; var17 < var2; var17++) {
               double var18 = var13[var17 * 4 + 1] - var3.x;
               double var20 = var13[var17 * 4 + 2] - var3.z * 2.4494896F;
               double var22 = var13[var17 * 4 + 3] - var3.y;
               double var24 = (var18 * var5 + var22 * var7 + var20 * var9) / var11;
               if (var24 > 0.5) {
                  var14 += (var18 * var18 + var22 * var22 + var20 * var20 - var24 * var24) / (var24 * var24);
                  var16++;
               }
            }

            if (var16 != 0) {
               double var26 = var14 / var16 / 2.0;
               variance = seen == 0 ? var26 : variance + (var26 - variance) * 0.1;
               seen += var16;
            }
         }
      }
   }

   static double spread(HandWeapon var0) {
      return matches(var0) && seen >= 18 ? Math.sqrt(variance) : Double.NaN;
   }

   private static boolean matches(HandWeapon var0) {
      return var0.getFullType().equals(type)
         && var0.getProjectileSpread() == projectiles
         && var0.getProjectileWeightCenter() == weightCentre
         && var0.getProjectileCount() == count;
   }

   private Pattern() {
   }
}
