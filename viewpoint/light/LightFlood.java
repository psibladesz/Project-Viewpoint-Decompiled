package viewpoint.light;

import java.util.Arrays;

final class LightFlood {
   static final double STRAIGHT = 1.0;
   static final double DIAGONAL = Math.sqrt(2.0);
   static final double LEVEL = 3.0;
   static final double CLOSED_DOOR = 0.15;
   static final double CURTAIN = 0.33;
   private final LightLayout layout;
   private final double[] value;
   private final double[] cost;
   private final double[] transmission;
   private final int[] touched;
   private int touchedCount;
   private int[] heap = new int[256];
   private int[] entrySquare = new int[256];
   private double[] entryValue = new double[256];
   private int heapSize;
   private int entries;

   LightFlood(LightLayout var1) {
      this.layout = var1;
      this.value = new double[var1.capacity()];
      this.cost = new double[var1.capacity()];
      this.transmission = new double[var1.capacity()];
      this.touched = new int[var1.capacity()];
      Arrays.fill(this.value, -1.0);
   }

   void run(int[] var1, int var2, double var3) {
      for (int var5 = 0; var5 < this.touchedCount; var5++) {
         this.value[this.touched[var5]] = -1.0;
      }

      this.touchedCount = this.heapSize = this.entries = 0;

      for (int var8 = 0; var8 < var2; var8++) {
         int var6 = var1[var8];
         if (var6 >= 0 && this.layout.exists(var6) && this.value[var6] < 1.0) {
            this.reach(var6, 0.0, 1.0, 1.0);
         }
      }

      while (this.heapSize > 0) {
         int var9 = this.pop();
         int var10 = this.entrySquare[var9];
         if (!(this.entryValue[var9] < this.value[var10])) {
            for (int var7 = 0; var7 < 10; var7++) {
               this.step(var10, var7, var3);
            }
         }
      }
   }

   int touchedCount() {
      return this.touchedCount;
   }

   int touched(int var1) {
      return this.touched[var1];
   }

   double cost(int var1) {
      return this.cost[var1];
   }

   double transmission(int var1) {
      return this.transmission[var1];
   }

   private void step(int var1, int var2, double var3) {
      int var5 = this.layout.neighbour(var1, var2);
      if (var5 >= 0 && this.layout.exists(var5)) {
         int var6 = this.layout.pass(var1, var2);
         int var7 = this.layout.pass(var5, LightLayout.REVERSE[var2]);
         if (var6 != 4 || var7 != 4) {
            double var8 = this.cost[var1] + (var2 >= 8 ? 3.0 : ((var2 & 1) != 0 ? DIAGONAL : 1.0));
            if (!(var8 >= var3)) {
               int var10 = var6 != 4 ? var6 : var7;
               double var11 = this.transmission[var1] * (var10 == 3 ? 0.15 : 1.0) * this.curtain(var1, var5, var2);
               double var13 = var11 * (1.0 - var8 / var3);
               if (var13 > this.value[var5]) {
                  this.reach(var5, var8, var11, var13);
               }
            }
         }
      }
   }

   private double curtain(int var1, int var2, int var3) {
      boolean var4 = switch (var3) {
         case 0 -> this.layout.has(var1, 4) || this.layout.has(var2, 16);
         default -> false;
         case 2 -> this.layout.has(var1, 8) || this.layout.has(var2, 32);
         case 4 -> this.layout.has(var1, 16) || this.layout.has(var2, 4);
         case 6 -> this.layout.has(var1, 32) || this.layout.has(var2, 8);
      };
      return var4 ? 0.33 : 1.0;
   }

   private void reach(int var1, double var2, double var4, double var6) {
      if (this.value[var1] < 0.0) {
         this.touched[this.touchedCount++] = var1;
      }

      this.value[var1] = var6;
      this.cost[var1] = var2;
      this.transmission[var1] = var4;
      this.push(var1, var6);
   }

   private void push(int var1, double var2) {
      if (this.entries == this.entrySquare.length) {
         this.entrySquare = Arrays.copyOf(this.entrySquare, this.entries * 2);
         this.entryValue = Arrays.copyOf(this.entryValue, this.entries * 2);
      }

      if (this.heapSize == this.heap.length) {
         this.heap = Arrays.copyOf(this.heap, this.heapSize * 2);
      }

      int var4 = this.entries++;
      this.entrySquare[var4] = var1;
      this.entryValue[var4] = var2;

      int var5;
      for (var5 = this.heapSize++; var5 > 0 && this.entryValue[this.heap[(var5 - 1) / 2]] < var2; var5 = (var5 - 1) / 2) {
         this.heap[var5] = this.heap[(var5 - 1) / 2];
      }

      this.heap[var5] = var4;
   }

   private int pop() {
      int var1 = this.heap[0];
      int var2 = this.heap[--this.heapSize];
      int var3 = 0;

      while (true) {
         int var4 = var3 * 2 + 1;
         if (var4 >= this.heapSize) {
            break;
         }

         if (var4 + 1 < this.heapSize && this.entryValue[this.heap[var4 + 1]] > this.entryValue[this.heap[var4]]) {
            var4++;
         }

         if (this.entryValue[this.heap[var4]] <= this.entryValue[var2]) {
            break;
         }

         this.heap[var3] = this.heap[var4];
         var3 = var4;
      }

      this.heap[var3] = var2;
      return var1;
   }
}
