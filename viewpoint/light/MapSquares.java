package viewpoint.light;

import java.util.Arrays;

public final class MapSquares {
   static final int SIZE = 64;
   static final int LAMP_INTS = 4;
   final int bx;
   final int by;
   int levels;
   int[] bits;
   int[] lamps = new int[32];
   int lampCount;

   public MapSquares(int var1, int var2, int var3) {
      this.bx = var1;
      this.by = var2;
      this.levels = Math.max(1, var3);
      this.bits = new int[this.levels * 64 * 64];
   }

   public void tile(int var1, int var2, int var3, long var4) {
      int var6 = var1 - this.bx * 64;
      int var7 = var2 - this.by * 64;
      if (var6 >= 0 && var7 >= 0 && var6 < 64 && var7 < 64 && var3 >= 0 && var3 < this.levels) {
         this.bits[(var3 * 64 + var7) * 64 + var6] = this.bits[(var3 * 64 + var7) * 64 + var6] | (int)var4 | 1;
         int var8 = (int)(var4 >>> 32);
         if (var8 != 0) {
            if ((this.lampCount + 1) * 4 > this.lamps.length) {
               this.lamps = Arrays.copyOf(this.lamps, this.lamps.length * 2);
            }

            int var9 = this.lampCount++ * 4;
            this.lamps[var9] = var1;
            this.lamps[var9 + 1] = var2;
            this.lamps[var9 + 2] = var3;
            this.lamps[var9 + 3] = var8;
         }
      }
   }

   public int levels() {
      return this.levels;
   }

   public void finish() {
      byte var1 = 0;

      for (int var2 = this.bits.length - 1; var2 >= 0 && var1 == 0; var2--) {
         var1 = (byte)((this.bits[var2] & 1) != 0 ? var2 / 4096 : 0);
      }

      if (var1 + 1 < this.levels) {
         this.levels = var1 + 1;
         this.bits = Arrays.copyOf(this.bits, this.levels * 64 * 64);
      }

      for (int var4 = 0; var4 < 64; var4 += 8) {
         for (int var3 = 0; var3 < 64; var3 += 8) {
            this.chunk(var3, var4);
         }
      }
   }

   public long bytes() {
      return this.bits.length * 4L + this.lamps.length * 4L + 64L;
   }

   int at(int var1, int var2, int var3) {
      int var4 = var1 - this.bx * 64;
      int var5 = var2 - this.by * 64;
      if (var4 >= 0 && var5 >= 0 && var4 < 64 && var5 < 64 && var3 >= 0 && var3 < this.levels) {
         int var6 = this.bits[(var3 * 64 + var5) * 64 + var4];
         return (var6 & 536870912) != 0 ? var6 : -1;
      } else {
         return -1;
      }
   }

   private void chunk(int var1, int var2) {
      for (int var3 = 0; var3 < this.levels; var3++) {
         for (int var4 = var2; var4 < var2 + 8; var4++) {
            for (int var5 = var1; var5 < var1 + 8; var5++) {
               int var6 = this.bits[(var3 * 64 + var4) * 64 + var5];
               if (var3 == 0) {
                  this.make(var5, var4, 0, false);
               }

               if ((var6 & 1) != 0) {
                  this.bits[(var3 * 64 + var4) * 64 + var5] = this.bits[(var3 * 64 + var4) * 64 + var5] | 1073741824;
                  this.around(var1, var2, var5, var4, var3, false);

                  for (int var7 = var3 - 1; var7 >= 1; var7--) {
                     this.around(var1, var2, var5, var4, var7, true);
                  }
               }
            }
         }
      }
   }

   private void around(int var1, int var2, int var3, int var4, int var5, boolean var6) {
      for (int var7 = Math.max(var2, var4 - 1); var7 <= Math.min(var2 + 8 - 1, var4 + 1); var7++) {
         for (int var8 = Math.max(var1, var3 - 1); var8 <= Math.min(var1 + 8 - 1, var3 + 1); var8++) {
            this.make(var8, var7, var5, var6);
         }
      }
   }

   private void make(int var1, int var2, int var3, boolean var4) {
      int var5 = (var3 * 64 + var2) * 64 + var1;
      if ((this.bits[var5] & 536870912) == 0) {
         this.bits[var5] = this.bits[var5] | 536870912 | (var4 ? 1073741824 : 0);
      }
   }
}
