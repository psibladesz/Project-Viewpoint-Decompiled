package viewpoint.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map.Entry;

final class RoofFace {
   private static final int GAP = 6;
   private static final int OLDER_PITCH = 3;
   static final int NEAR = 8;
   private static final float EPS = 0.001F;
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};
   final int facing;
   final int perStorey;
   final int c;
   final float rise;
   final int[] squares;
   final boolean rides;
   int a0;
   int a1;
   int[] top;
   int[] bottom;
   boolean[] stacked;
   boolean hipLow;
   boolean hipHigh;
   float eLow;
   float eHigh;
   private boolean laidLow;
   private boolean laidHigh;
   private int dutchLow;
   private int dutchHigh;

   private RoofFace(int var1, int var2, int var3, int[] var4, boolean var5) {
      this.facing = var1;
      this.perStorey = var2;
      this.c = var3;
      this.rise = 2.4494896F / var2;
      this.squares = var4;
      this.rides = var5;
   }

   static ArrayList<RoofFace> of(int var0, RoofPiece[] var1, RoofFace.Leads var2, int[][] var3) {
      HashMap<Long, ArrayList<Integer>> var4 = new HashMap<>();

      for (int var5 = 0; var5 < var1.length; var5++) {
         for (int var6 = 0; var6 < 4 && var2.leads(var5); var6++) {
            if (has(var1[var5], var6)) {
               long var7 = (long)var6 << 56 | (long)var1[var5].perStorey << 48 | offset(var0, var5, var6, var1[var5].step) & 4294967295L;
               var4.computeIfAbsent(var7, var0x -> new ArrayList<>()).add(var5);
            }
         }
      }

      ArrayList<RoofFace> var9 = new ArrayList<>();

      for (Entry<Long, ArrayList<Integer>> var11 : var4.entrySet()) {
         split(var0, var1, var11.getKey(), var11.getValue(), false, var9, var3);
      }

      return var9;
   }

   private static void split(int var0, RoofPiece[] var1, long var2, ArrayList<Integer> var4, boolean var5, ArrayList<RoofFace> var6, int[][] var7) {
      int var8 = (int)(var2 >>> 56);
      int var9 = (int)(var2 >>> 48 & 255L);
      int var10 = (int)var2;
      Integer[] var11 = var4.toArray(new Integer[0]);
      Arrays.sort(var11, (var2x, var3) -> Integer.compare(along(var0, var2x, var8), along(var0, var3, var8)));
      int var12 = 0;

      for (int var13 = 1; var13 <= var11.length; var13++) {
         boolean var14 = var13 < var11.length && along(var0, var11[var13], var8) - along(var0, var11[var13 - 1], var8) <= 7;
         if (!var14 || over(var0, var1, var8, var9, var10, var11[var13 - 1], var11[var13])) {
            int[] var15 = new int[var13 - var12];

            for (int var16 = var12; var16 < var13; var16++) {
               var15[var16 - var12] = var11[var16];
            }

            add(var0, var1, var2, var15, var5, var6, var7);
            var12 = var13;
         }
      }
   }

   private static void add(int var0, RoofPiece[] var1, long var2, int[] var4, boolean var5, ArrayList<RoofFace> var6, int[][] var7) {
      int var8 = (int)(var2 >>> 56);
      int var9 = (int)(var2 >>> 48 & 255L);
      int var10 = (int)var2;
      RoofFace var11 = new RoofFace(var8, var9, var10, var4, var5);
      var11.measure(var0, var1);
      ArrayList<Integer> var12 = var11.riders(var0);
      if (!var12.isEmpty() && var12.size() < var4.length) {
         int[] var17 = Arrays.stream(var4).filter(var1x -> !var12.contains(var1x)).toArray();
         add(var0, var1, var2, var17, var5, var6, var7);
         split(var0, var1, var2, var12, true, var6, var7);
      } else {
         for (int var16 : var4) {
            var7[var8][var16] = var6.size();
         }

         var6.add(var11);
      }
   }

   private static boolean over(int var0, RoofPiece[] var1, int var2, int var3, int var4, int var5, int var6) {
      int var7 = var1.length / var0;
      int var8 = u(var0, var5, var2);
      int var9 = u(var0, var6, var2);
      float var10 = 2.4494896F / var3;

      for (int var11 = along(var0, var5, var2) + 1; var11 < along(var0, var6, var2); var11++) {
         for (int var12 = Math.min(var8, var9); var12 <= Math.max(var8, var9); var12++) {
            int var13 = var2 != 2 && var2 != 3 ? var12 : -(var12 + 1);
            int var14 = DZ[var2] != 0 ? var11 : var13;
            int var15 = DZ[var2] != 0 ? var13 : var11;
            RoofPiece var16 = var14 >= 0 && var15 >= 0 && var14 < var0 && var15 < var7 ? var1[var15 * var0 + var14] : null;
            float var17 = var10 * (var4 - (DX[var2] * (var14 + 0.5F) + DZ[var2] * (var15 + 0.5F)));
            if (var16 != null && var16.planar() && var16.height(0.0F, 0.0F) < var17 - 0.001F) {
               return true;
            }
         }
      }

      return false;
   }

   private void measure(int var1, RoofPiece[] var2) {
      this.a0 = Integer.MAX_VALUE;
      this.a1 = Integer.MIN_VALUE;

      for (int var6 : this.squares) {
         this.a0 = Math.min(this.a0, along(var1, var6, this.facing));
         this.a1 = Math.max(this.a1, along(var1, var6, this.facing));
      }

      this.top = new int[this.a1 - this.a0 + 1];
      this.bottom = new int[this.a1 - this.a0 + 1];
      Arrays.fill(this.top, Integer.MAX_VALUE);
      Arrays.fill(this.bottom, Integer.MIN_VALUE);

      for (int var20 : this.squares) {
         int var7 = along(var1, var20, this.facing) - this.a0;
         int var8 = u(var1, var20, this.facing);
         this.top[var7] = Math.min(this.top[var7], var8);
         this.bottom[var7] = Math.max(this.bottom[var7], var8 + 1);
      }

      BitSet var11 = new BitSet(var2.length);
      this.stacked = new boolean[this.top.length];

      for (int var24 : this.squares) {
         var11.set(var24);
      }

      for (int var25 : this.squares) {
         int var26 = var25 % var1 - DX[this.facing];
         int var9 = var25 - DX[this.facing] - DZ[this.facing] * var1;
         this.stacked[along(var1, var25, this.facing) - this.a0] |= var26 >= 0 && var26 < var1 && var9 >= 0 && var11.get(var9);
      }

      int var15 = this.top.length - 1;
      int var19 = this.line(var1, var2, 0, 1);
      int var23 = this.line(var1, var2, var15, -1);
      this.laidLow = var19 > 0 && var19 == this.steps(0);
      this.laidHigh = var23 > 0 && var23 == this.steps(var15);
      this.hipLow = this.laidLow || this.hip(0, 1);
      this.hipHigh = this.laidHigh || this.hip(var15, -1);
      this.dutchLow = this.hipLow ? 0 : this.dutch(0, 1, var19);
      this.dutchHigh = this.hipHigh ? 0 : this.dutch(var15, -1, var23);
      this.hipLow = this.hipLow | this.dutchLow > 0;
      this.hipHigh = this.hipHigh | this.dutchHigh > 0;
      this.eLow = this.e(0);
      this.eHigh = this.e(var15);
   }

   private boolean hip(int var1, int var2) {
      int var3 = this.eave(var1);
      int var4 = this.upper(var1);
      int var5 = var3 - 1 - var4;
      byte var6 = -1;
      byte var7 = -1;
      int var8 = var1;

      for (int var9 = 0; var9 <= 8 && var8 >= 0 && var8 < this.top.length; var9++) {
         var6 = (byte)(var6 < 0 && this.bottom[var8] == var3 ? var9 : var6);
         var7 = (byte)(var7 < 0 && this.top[var8] == var4 ? var9 : var7);
         var8 += var2;
      }

      return var5 > 0 && var6 == 0 && var7 == var5;
   }

   private int steps(int var1) {
      return this.eave(var1) - 1 - this.upper(var1);
   }

   private int line(int var1, RoofPiece[] var2, int var3, int var4) {
      int var5 = DZ[this.facing] != 0 ? (var4 > 0 ? 3 : 1) : (var4 > 0 ? 2 : 0);
      int var6 = this.eave(var3);
      int var7 = this.steps(var3);
      int var8 = 0;

      for (int var9 = var3; var8 < var7 && var9 >= 0 && var9 < this.top.length; var8++) {
         int var10 = this.square(var1, var2.length / var1, var9, var6 - 1 - var8);
         RoofPiece var11 = var10 < 0 ? null : var2[var10];
         boolean var12 = var11 != null && var11.perStorey == this.perStorey && offset(var1, var10, this.facing, var11.step) == this.c;
         if (!var12 || var11.kind != 1 || !has(var11, this.facing) || !has(var11, var5)) {
            break;
         }

         var9 += var4;
      }

      return var8;
   }

   private int dutch(int var1, int var2, int var3) {
      int var4 = var1 + var3 * var2;
      return var3 > 0 && var4 >= 0 && var4 < this.top.length && this.top[var4] == this.upper(var1) ? var3 : 0;
   }

   private ArrayList<Integer> riders(int var1) {
      ArrayList<Integer> var2 = new ArrayList<>();

      for (int var6 : this.squares) {
         float var7 = var6 % var1 + 0.5F;
         float var8 = var6 / var1 + 0.5F;
         float var9 = DZ[this.facing] != 0 ? var7 : var8;
         float var10 = this.rise * (this.c - this.u(var7, var8)) - 0.001F;
         boolean var11 = this.laidLow && var10 > this.eLow + this.rise * (var9 - this.a0);
         boolean var12 = this.laidHigh && var10 > this.eHigh + this.rise * (this.a1 + 1 - var9);
         if (var11 || var12) {
            var2.add(var6);
         }
      }

      return var2;
   }

   int upper(int var1) {
      int var2 = Integer.MAX_VALUE;

      for (int var3 = Math.max(0, var1 - 8); var3 <= Math.min(this.top.length - 1, var1 + 8); var3++) {
         var2 = Math.min(var2, this.top[var3]);
      }

      return var2;
   }

   int eave(int var1) {
      int var2 = Integer.MIN_VALUE;

      for (int var3 = Math.max(0, var1 - 8); var3 <= Math.min(this.top.length - 1, var1 + 8); var3++) {
         var2 = Math.max(var2, this.bottom[var3]);
      }

      return var2;
   }

   float e(int var1) {
      return this.rise * (this.c - this.eave(var1));
   }

   float farEave(int var1) {
      return this.perStorey == 3 ? this.e(var1) : 0.0F;
   }

   float height(int var1, float var2, float var3) {
      float var4 = DX[this.facing] * var2 + DZ[this.facing] * var3;
      float var5 = DZ[this.facing] != 0 ? var2 : var3;
      float var6 = this.rise * (this.c - var4);
      if (this.hipLow && (this.dutchLow == 0 || var1 < this.dutchLow)) {
         var6 = Math.min(var6, this.eLow + this.rise * (var5 - this.a0));
      }

      if (this.hipHigh && (this.dutchHigh == 0 || var1 >= this.top.length - this.dutchHigh)) {
         var6 = Math.min(var6, this.eHigh + this.rise * (this.a1 + 1 - var5));
      }

      return var6;
   }

   float u(float var1, float var2) {
      return DX[this.facing] * var1 + DZ[this.facing] * var2;
   }

   int square(int var1, int var2, int var3, int var4) {
      int var5 = this.a0 + var3;
      int var6 = this.facing != 2 && this.facing != 3 ? var4 : -(var4 + 1);
      int var7 = DZ[this.facing] != 0 ? var5 : var6;
      int var8 = DZ[this.facing] != 0 ? var6 : var5;
      return var7 >= 0 && var8 >= 0 && var7 < var1 && var8 < var2 ? var8 * var1 + var7 : -1;
   }

   static boolean has(RoofPiece var0, int var1) {
      return var0.kind == 6 || var0.facing == var1 || var0.kind != 0 && var0.other == var1;
   }

   static int u(int var0, int var1, int var2) {
      return DX[var2] * (var1 % var0) + DZ[var2] * (var1 / var0) + (var2 != 2 && var2 != 3 ? 0 : -1);
   }

   static int along(int var0, int var1, int var2) {
      return DZ[var2] != 0 ? var1 % var0 : var1 / var0;
   }

   private static int offset(int var0, int var1, int var2, int var3) {
      return var3 + u(var0, var1, var2);
   }

   interface Leads {
      boolean leads(int var1);
   }
}