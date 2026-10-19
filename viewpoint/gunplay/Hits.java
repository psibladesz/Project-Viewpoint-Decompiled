package viewpoint.gunplay;

import zombie.characters.IsoGameCharacter;
import zombie.iso.IsoMovingObject;
import zombie.vehicles.BaseVehicle;

final class Hits {
   static final int MOST = 32;
   private static final double INSIDE = 0.05;
   final IsoMovingObject[] objects = new IsoMovingObject[32];
   final int[] ids = new int[32];
   final int[] parts = new int[32];
   final int[] pellets = new int[32];
   final double[] at = new double[32];
   final float[] places = new float[96];
   int count;

   void add(IsoMovingObject var1, double var2, int var4, double[] var5, double[] var6) {
      int var7 = this.index(var1);
      if (var7 >= 0) {
         this.pellets[var7]++;
      } else if (this.count != 32) {
         for (var7 = this.count++; var7 > 0 && this.at[var7 - 1] > var2; var7--) {
            this.move(var7 - 1, var7);
         }

         double var8 = var2 + (var1 instanceof BaseVehicle ? 0.05 : 0.0);
         this.objects[var7] = var1;
         this.at[var7] = var2;
         this.parts[var7] = var4;
         this.pellets[var7] = 1;
         this.places[var7 * 3] = (float)(var5[0] + var6[0] * var8);
         this.places[var7 * 3 + 1] = (float)(var5[1] + var6[1] * var8);
         this.places[var7 * 3 + 2] = (float)((var5[2] + var6[2] * var8 - Ground.under(var1)) / 2.4494896F);
      }
   }

   void cut(int var1) {
      this.count = Math.min(this.count, var1);

      for (int var2 = 0; var2 < this.count; var2++) {
         if (this.objects[var2] instanceof BaseVehicle) {
            this.count = var2 + 1;
         }
      }
   }

   void settle() {
      for (int var1 = 0; var1 < this.count; var1++) {
         this.ids[var1] = this.objects[var1] instanceof BaseVehicle var2 ? var2.getId() : ((IsoGameCharacter)this.objects[var1]).getID();
      }
   }

   int index(IsoMovingObject var1) {
      for (int var2 = 0; var2 < this.count; var2++) {
         if (this.objects[var2] == var1) {
            return var2;
         }
      }

      return -1;
   }

   int indexOf(int var1) {
      for (int var2 = 0; var2 < this.count; var2++) {
         if (this.ids[var2] == var1) {
            return var2;
         }
      }

      return -1;
   }

   void clear() {
      for (int var1 = 0; var1 < this.count; var1++) {
         this.objects[var1] = null;
      }

      this.count = 0;
   }

   private void move(int var1, int var2) {
      this.objects[var2] = this.objects[var1];
      this.at[var2] = this.at[var1];
      this.parts[var2] = this.parts[var1];
      this.pellets[var2] = this.pellets[var1];
      System.arraycopy(this.places, var1 * 3, this.places, var2 * 3, 3);
   }
}
