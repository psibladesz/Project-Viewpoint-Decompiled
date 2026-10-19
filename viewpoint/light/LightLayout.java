package viewpoint.light;

import java.util.Arrays;

final class LightLayout {
   static final int[] DX = new int[]{0, 1, 1, 1, 0, -1, -1, -1, 0, 0};
   static final int[] DY = new int[]{-1, -1, 0, 1, 1, 1, 0, -1, 0, 0};
   static final int[] DZ = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, -1};
   static final int[] REVERSE = new int[]{4, 5, 6, 7, 0, 1, 2, 3, 9, 8};
   static final int DIRECTIONS = 10;
   static final int UP = 8;
   static final int DOWN = 9;
   static final int CLEAR = 0;
   static final int OPEN_DOOR = 1;
   static final int WINDOW = 2;
   static final int CLOSED_DOOR = 3;
   static final int BLOCKED = 4;
   static final int EXISTS = 1;
   static final int OPEN_AIR = 2;
   static final int CURTAIN_N = 4;
   static final int CURTAIN_E = 8;
   static final int CURTAIN_S = 16;
   static final int CURTAIN_W = 32;
   static final int OUTDOORS = 64;
   static final int EXTERIOR = 128;
   int x0;
   int y0;
   int z0;
   int width;
   int height;
   int levels;
   private final byte[] flags;
   private final int[] passes;

   LightLayout(int var1, int var2, int var3, int var4, int var5, int var6) {
      this(var4 * var5 * var6);
      this.box(var1, var2, var3, var4, var5, var6);
   }

   LightLayout(int var1) {
      this.flags = new byte[var1];
      this.passes = new int[var1];
   }

   void box(int var1, int var2, int var3, int var4, int var5, int var6) {
      if (var4 * var5 * var6 > this.flags.length) {
         throw new IllegalArgumentException("a light layout of " + var4 + " x " + var5 + " x " + var6 + " squares is past its capacity of " + this.flags.length);
      }

      this.x0 = var1;
      this.y0 = var2;
      this.z0 = var3;
      this.width = var4;
      this.height = var5;
      this.levels = var6;
      Arrays.fill(this.flags, 0, this.size(), (byte)0);
   }

   int size() {
      return this.width * this.height * this.levels;
   }

   int capacity() {
      return this.flags.length;
   }

   int index(int var1, int var2, int var3) {
      int var4 = var1 - this.x0;
      int var5 = var2 - this.y0;
      int var6 = var3 - this.z0;
      return var4 >= 0 && var5 >= 0 && var6 >= 0 && var4 < this.width && var5 < this.height && var6 < this.levels
         ? (var6 * this.height + var5) * this.width + var4
         : -1;
   }

   int neighbour(int var1, int var2) {
      int var3 = var1 % this.width;
      int var4 = var1 / this.width % this.height;
      int var5 = var1 / (this.width * this.height);
      return this.index(this.x0 + var3 + DX[var2], this.y0 + var4 + DY[var2], this.z0 + var5 + DZ[var2]);
   }

   void set(int var1, int var2, int var3, int var4, int var5) {
      int var6 = this.index(var1, var2, var3);
      if (var6 >= 0) {
         this.flags[var6] = (byte)(var4 | 1);
         this.passes[var6] = var5;
      }
   }

   void chunk(LightLayouts.Level var1) {
      int var2 = var1.chunkX * 8;
      int var3 = var1.chunkY * 8;

      for (int var4 = 0; var4 < 64; var4++) {
         if ((var1.flags[var4] & 1) != 0) {
            this.set(var2 + var4 % 8, var3 + var4 / 8, var1.level, var1.flags[var4], var1.passes[var4]);
         }
      }
   }

   boolean exists(int var1) {
      return (this.flags[var1] & 1) != 0;
   }

   boolean openAir(int var1) {
      return (this.flags[var1] & 2) != 0;
   }

   boolean has(int var1, int var2) {
      return (this.flags[var1] & var2) != 0;
   }

   int pass(int var1, int var2) {
      return passIn(this.passes[var1], var2);
   }

   static int passIn(int var0, int var1) {
      return var0 >> var1 * 3 & 7;
   }
}
