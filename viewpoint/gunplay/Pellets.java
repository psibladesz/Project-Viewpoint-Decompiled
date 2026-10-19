package viewpoint.gunplay;

import java.lang.reflect.Field;
import java.util.Random;
import org.joml.Vector3f;
import zombie.core.physics.BallisticsController;
import zombie.iso.IsoMovingObject;
import zombie.vehicles.BaseVehicle;

final class Pellets {
   static final int MOST = 9;
   private static final int STRIDE = 4;
   private static final double STEEP = 1.0E-9;
   private static final Field COUNT;
   private final double[] stray = new double[2];
   private final double[] way = new double[3];
   private final int[] part = new int[1];
   private final double[] ends = new double[27];
   private final IsoMovingObject[] met = new IsoMovingObject[9];
   private final Vector3f from = new Vector3f();
   private final Vector3f to = new Vector3f();
   private final Vector3f into = new Vector3f();
   private int fired;

   void fire(
      Random var1,
      double[] var2,
      double[] var3,
      double var4,
      int var6,
      double var7,
      Candidates var9,
      BaseVehicle[] var10,
      int var11,
      Hits var12,
      Obstacles var13
   ) {
      double var14 = Math.hypot(var3[0], var3[1]);
      double var16 = var14 > 1.0E-9 ? -var3[1] / var14 : 0.0;
      double var18 = var14 > 1.0E-9 ? var3[0] / var14 : 1.0;
      double var20 = -var3[2] * var18;
      double var22 = var3[2] * var16;
      double var24 = var3[0] * var18 - var3[1] * var16;
      this.fired = Math.min(var6, 9);

      for (int var26 = 0; var26 < this.fired; var26++) {
         Cone.draw(var1, var4, this.stray);
         double var27 = var3[0] + var16 * this.stray[0] + var20 * this.stray[1];
         double var29 = var3[1] + var18 * this.stray[0] + var22 * this.stray[1];
         double var31 = var3[2] + var24 * this.stray[1];
         double var33 = Math.sqrt(var27 * var27 + var29 * var29 + var31 * var31);
         this.way[0] = var27 / var33;
         this.way[1] = var29 / var33;
         this.way[2] = var31 / var33;
         double var35 = var13.stop(var2[0], var2[1], var2[2], this.way[0], this.way[1], this.way[2], 0.0, var7);
         double var37 = this.first(var26, var2, var35, var9, var10, var11, var12);
         this.ends[var26 * 3] = var2[0] + this.way[0] * var37;
         this.ends[var26 * 3 + 1] = var2[1] + this.way[1] * var37;
         this.ends[var26 * 3 + 2] = var2[2] + this.way[2] * var37;
      }
   }

   void lay(BallisticsController var1, Hits var2) {
      float[] var3 = var1.getBallisticsSpreadData();

      for (int var4 = 0; var4 < this.fired; var4++) {
         int var5 = this.met[var4] == null ? -1 : var2.index(this.met[var4]);
         double var6 = this.met[var4] == null ? Ground.at(this.ends[var4 * 3], this.ends[var4 * 3 + 1]) : Ground.under(this.met[var4]);
         var3[var4 * 4] = var5 >= 0 ? var2.ids[var5] : 0.0F;
         this.met[var4] = null;
         var3[var4 * 4 + 1] = (float)this.ends[var4 * 3];
         var3[var4 * 4 + 2] = (float)(this.ends[var4 * 3 + 2] - var6);
         var3[var4 * 4 + 3] = (float)this.ends[var4 * 3 + 1];
      }

      try {
         COUNT.setInt(var1, this.fired);
      } catch (IllegalAccessException var8) {
         throw new IllegalStateException("[Viewpoint] gunplay: the game's spread data could not be laid out", var8);
      }
   }

   private double first(int var1, double[] var2, double var3, Candidates var5, BaseVehicle[] var6, int var7, Hits var8) {
      double var9 = var3;
      int var11 = -1;
      int var12 = -1;
      int var13 = 11;

      for (int var14 = 0; var14 < var5.count; var14++) {
         double var15 = Parts.meet(var5.capsules[var14], var2[0], var2[1], var2[2], this.way[0], this.way[1], this.way[2], 0.0, this.part);
         if (var15 < var9) {
            var9 = var15;
            var11 = var14;
            var13 = this.part[0];
         }
      }

      for (int var25 = 0; var25 < var7; var25++) {
         double var26 = Ground.under(var6[var25]);
         this.from.set((float)var2[0], (float)var2[1], (float)((var2[2] - var26) / 2.4494896F));
         this.to
            .set((float)(var2[0] + this.way[0] * var9), (float)(var2[1] + this.way[1] * var9), (float)((var2[2] + this.way[2] * var9 - var26) / 2.4494896F));
         if (var6[var25].getIntersectPoint(this.from, this.to, this.into) != null) {
            double var17 = this.into.x - var2[0];
            double var19 = this.into.y - var2[1];
            double var21 = this.into.z * 2.4494896F + var26 - var2[2];
            double var23 = Math.sqrt(var17 * var17 + var19 * var19 + var21 * var21);
            if (var23 < var9) {
               var9 = var23;
               var12 = var25;
            }
         }
      }

      this.met[var1] = (IsoMovingObject)(var12 >= 0 ? var6[var12] : (var11 >= 0 ? var5.characters[var11] : null));
      if (this.met[var1] != null) {
         var8.add(this.met[var1], var9, var12 >= 0 ? 11 : var13, var2, this.way);
      }

      return var9;
   }

   static {
      try {
         COUNT = BallisticsController.class.getDeclaredField("numberOfSpreadData");
         COUNT.setAccessible(true);
      } catch (ReflectiveOperationException | RuntimeException var1) {
         throw new IllegalStateException("[Viewpoint] gunplay: BallisticsController's spread data changed", var1);
      }
   }
}
