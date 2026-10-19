package viewpoint.visibility;

import java.util.Arrays;

final class Selection {
   static final int BUDGET = 65536;
   private final Portals portals = new Portals();
   private boolean[] reached = new boolean[0];
   private boolean[] seen = new boolean[0];
   private int[] queue = new int[0];
   boolean narrowed;

   boolean select(SectorGraph var1, byte[] var2, int[] var3, Portals.View var4, boolean[] var5) {
      if (this.reached.length < var1.sectors) {
         this.reached = new boolean[var1.sectors];
         this.seen = new boolean[var1.sectors];
         this.queue = new int[var1.sectors];
      }

      Arrays.fill(var5, 0, var1.sectors, false);
      if (!this.flood(var1, var2, var3)) {
         return false;
      }

      this.narrowed = this.portals.run(var1, var2, var3, var4, this.seen);

      for (int var6 = 0; var6 < var1.sectors; var6++) {
         boolean var7 = var6 == var3[0] || var6 == var3[1];
         var5[var6] = var1.hideable[var6] && (!this.reached[var6] || this.narrowed && !var7 && !this.seen[var6]);
      }

      return true;
   }

   private boolean flood(SectorGraph var1, byte[] var2, int[] var3) {
      Arrays.fill(this.reached, 0, var1.sectors, false);
      int var4 = 0;
      int var5 = 0;

      for (int var9 : var3) {
         var4 = var9 >= 0 ? this.seed(var9, var4) : var4;
      }

      for (int var11 = 0; var11 < var1.sectors; var11++) {
         if (var1.exterior[var11]) {
            var4 = this.seed(var11, var4);
         }
      }

      for (int var12 = 0; var12 < var1.openings; var12++) {
         if (var1.openDoor[var12] >= 0 && var2[var1.openDoor[var12]] == 2) {
            var4 = this.seed(var1.openA[var12], var4);
            var4 = this.seed(var1.openB[var12], var4);
         }
      }

      for (int var13 = 0; var13 < var4; var13++) {
         int var14 = this.queue[var13];

         for (int var15 = var1.adjacentStart[var14]; var15 < var1.adjacentStart[var14 + 1]; var15++) {
            if (++var5 > 65536) {
               return false;
            }

            int var16 = var1.adjacent[var15];
            if (var1.openDoor[var16] < 0 || var2[var1.openDoor[var16]] != 1) {
               var4 = this.seed(var1.openA[var16] == var14 ? var1.openB[var16] : var1.openA[var16], var4);
            }
         }
      }

      return true;
   }

   private int seed(int var1, int var2) {
      if (this.reached[var1]) {
         return var2;
      }

      this.reached[var1] = true;
      this.queue[var2] = var1;
      return var2 + 1;
   }
}
