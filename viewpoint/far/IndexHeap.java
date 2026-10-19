package viewpoint.far;

import java.util.Arrays;

final class IndexHeap {
   private int[] at;
   private float[] keys;
   private int size;

   IndexHeap(int var1) {
      this.at = new int[Math.max(16, var1)];
      this.keys = new float[this.at.length];
   }

   boolean empty() {
      return this.size == 0;
   }

   float peekKey() {
      return this.keys[0];
   }

   void push(int var1, float var2) {
      if (this.size == this.at.length) {
         this.at = Arrays.copyOf(this.at, this.size * 2);
         this.keys = Arrays.copyOf(this.keys, this.size * 2);
      }

      int var3;
      for (var3 = this.size++; var3 > 0 && this.keys[(var3 - 1) / 2] > var2; var3 = (var3 - 1) / 2) {
         this.at[var3] = this.at[(var3 - 1) / 2];
         this.keys[var3] = this.keys[(var3 - 1) / 2];
      }

      this.at[var3] = var1;
      this.keys[var3] = var2;
   }

   int pop() {
      int var1 = this.at[0];
      int var2 = this.at[--this.size];
      float var3 = this.keys[this.size];
      int var4 = 0;

      while (2 * var4 + 1 < this.size) {
         int var5 = 2 * var4 + 1;
         var5 = var5 + 1 < this.size && this.keys[var5 + 1] < this.keys[var5] ? var5 + 1 : var5;
         if (this.keys[var5] >= var3) {
            break;
         }

         this.at[var4] = this.at[var5];
         this.keys[var4] = this.keys[var5];
         var4 = var5;
      }

      this.at[var4] = var2;
      this.keys[var4] = var3;
      return var1;
   }
}
