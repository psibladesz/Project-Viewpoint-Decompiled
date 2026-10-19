package viewpoint.visibility;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import viewpoint.platform.LongMap;

final class GraphBuilder {
   private final Fragment[] leaves;
   private final LongMap<Integer> index;
   private final int[] parent;
   private final boolean[] uncertain;
   private final int beyondNode;
   private final SectorGraph.Openings raw = new SectorGraph.Openings();
   private long[] doorLeaf = new long[64];
   private int[] doorSlot = new int[64];
   private boolean[] doorWindow = new boolean[64];
   private float[] doorPlace = new float[320];
   private int doors;

   static SectorGraph compile(Collection<Fragment> var0) {
      GraphBuilder var1 = new GraphBuilder(var0.toArray(new Fragment[0]));
      var1.link();
      return var1.build();
   }

   private GraphBuilder(Fragment[] var1) {
      this.leaves = var1;
      this.index = new LongMap<>(var1.length);

      for (int var2 = 0; var2 < var1.length; var2++) {
         this.index.put(var1[var2].key(), var2);
      }

      this.beyondNode = var1.length * 64;
      this.parent = new int[this.beyondNode + 1];
      this.uncertain = new boolean[this.beyondNode + 1];
      int var3 = 0;

      while (var3 < this.parent.length) {
         this.parent[var3] = var3++;
      }
   }

   private int leaf(int var1, int var2, int var3) {
      Integer var4 = this.index.get(Fragment.key(var1, var2, var3));
      return var4 == null ? -1 : var4;
   }

   private void link() {
      for (int var1 = 0; var1 < this.leaves.length; var1++) {
         Fragment var2 = this.leaves[var1];
         int var3 = this.leaf(var2.chunkX - 1, var2.chunkY, var2.level);
         int var4 = this.leaf(var2.chunkX, var2.chunkY - 1, var2.level);
         int var5 = this.leaf(var2.chunkX + 1, var2.chunkY, var2.level);
         int var6 = this.leaf(var2.chunkX, var2.chunkY + 1, var2.level);
         int var7 = this.leaf(var2.chunkX, var2.chunkY, var2.level + 1);
         int var8 = this.leaf(var2.chunkX, var2.chunkY, var2.level - 1);

         for (int var9 = 0; var9 < 64; var9++) {
            if (var2.has(var2.known, var9)) {
               int var10 = var1 * 64 + var9;
               int var11 = var9 % 8;
               int var12 = var9 / 8;
               this.edge(var2, var10, var9, true, var12 > 0 ? var1 : var4, var12 > 0 ? var9 - 8 : var9 + 64 - 8);
               this.edge(var2, var10, var9, false, var11 > 0 ? var1 : var3, var11 > 0 ? var9 - 1 : var9 + 8 - 1);
               this.uncertain[var10] = this.uncertain[var10] | !this.known(var11 < 7 ? var1 : var5, var11 < 7 ? var9 + 1 : var9 - var11);
               this.uncertain[var10] = this.uncertain[var10] | !this.known(var12 < 7 ? var1 : var6, var12 < 7 ? var9 + 8 : var9 % 8);
               this.above(var2, var10, var9, var7);
               this.below(var2, var10, var9, var8);
            }
         }
      }
   }

   private boolean known(int var1, int var2) {
      return var1 >= 0 && this.leaves[var1].has(this.leaves[var1].known, var2);
   }

   private void edge(Fragment var1, int var2, int var3, boolean var4, int var5, int var6) {
      boolean var7 = var1.has(var4 ? var1.northWindow : var1.westWindow, var3);
      boolean var8 = var7 || var1.has(var4 ? var1.northDoor : var1.westDoor, var3);
      boolean var9 = var8 || var1.has(var4 ? var1.northOpening : var1.westOpening, var3);
      if (var9 || !var1.has(var4 ? var1.northWall : var1.westWall, var3)) {
         Fragment var10 = var5 < 0 ? null : this.leaves[var5];
         int var11 = var10 != null && var10.has(var10.known, var6) ? var5 * 64 + var6 : -1;
         if (var11 < 0 && !var9) {
            this.uncertain[var2] = true;
         } else if (var11 >= 0 && !var9 && var1.room(var3) == var10.room(var6)) {
            this.union(var2, var11);
         } else {
            int var12 = var1.chunkX * 8 + var3 % 8;
            int var13 = var1.chunkY * 8 + var3 / 8;
            int var14 = var8 ? this.door(var1.key(), var3 * 2 + (var4 ? 0 : 1), var7) : -1;
            if (var8) {
               this.placeDoor(var14, var12, var13, var4 ? var12 + 1 : var12, var4 ? var13 : var13 + 1, var1.level);
            }

            int var15 = this.raw.add(var2, var11 < 0 ? this.beyondNode : var11, var14, (byte)(var4 ? 0 : 1));
            this.place(var15, var12, var13, var4 ? var12 + 1 : var12, var4 ? var13 : var13 + 1, var1.level);
         }
      }
   }

   private void above(Fragment var1, int var2, int var3, int var4) {
      Fragment var5 = var4 < 0 ? null : this.leaves[var4];
      boolean var6 = var5 != null && var5.has(var5.known, var3);
      boolean var7 = var1.has(var1.stairs, var3) || var6 && var5.has(var5.stairs, var3);
      if (var7 || (var6 ? !var5.has(var5.floor, var3) : !var1.has(var1.roofed, var3))) {
         if (!var6) {
            this.uncertain[var2] = true;
         } else {
            int var8 = var4 * 64 + var3;
            if (var1.room(var3) == var5.room(var3)) {
               this.union(var2, var8);
            } else {
               int var9 = var1.chunkX * 8 + var3 % 8;
               int var10 = var1.chunkY * 8 + var3 / 8;
               this.place(this.raw.add(var2, var8, -1, (byte)2), var9, var10, var9 + 1, var10 + 1, var1.level + 1);
            }
         }
      }
   }

   private void below(Fragment var1, int var2, int var3, int var4) {
      Fragment var5 = var4 < 0 ? null : this.leaves[var4];
      if (!var1.bottom && (var5 == null || !var5.has(var5.known, var3))) {
         if (!var1.has(var1.floor, var3) || var1.has(var1.stairs, var3)) {
            this.uncertain[var2] = true;
         }
      }
   }

   private void place(int var1, float var2, float var3, float var4, float var5, int var6) {
      int var7 = var1 * 5;
      this.raw.place[var7] = var2;
      this.raw.place[var7 + 1] = var3;
      this.raw.place[var7 + 2] = var4;
      this.raw.place[var7 + 3] = var5;
      this.raw.place[var7 + 4] = var6;
   }

   private int door(long var1, int var3, boolean var4) {
      if (this.doors == this.doorLeaf.length) {
         this.doorLeaf = Arrays.copyOf(this.doorLeaf, this.doors * 2);
         this.doorSlot = Arrays.copyOf(this.doorSlot, this.doors * 2);
         this.doorWindow = Arrays.copyOf(this.doorWindow, this.doors * 2);
         this.doorPlace = Arrays.copyOf(this.doorPlace, this.doors * 10);
      }

      this.doorLeaf[this.doors] = var1;
      this.doorSlot[this.doors] = var3;
      this.doorWindow[this.doors] = var4;
      return this.doors++;
   }

   private void placeDoor(int var1, float var2, float var3, float var4, float var5, int var6) {
      int var7 = var1 * 5;
      this.doorPlace[var7] = var2;
      this.doorPlace[var7 + 1] = var3;
      this.doorPlace[var7 + 2] = var4;
      this.doorPlace[var7 + 3] = var5;
      this.doorPlace[var7 + 4] = var6;
   }

   private int find(int var1) {
      while (this.parent[var1] != var1) {
         this.parent[var1] = this.parent[this.parent[var1]];
         var1 = this.parent[var1];
      }

      return var1;
   }

   private void union(int var1, int var2) {
      int var3 = this.find(var1);
      int var4 = this.find(var2);
      if (var3 != var4) {
         this.parent[Math.max(var3, var4)] = Math.min(var3, var4);
      }
   }

   private SectorGraph build() {
      int[] var1 = new int[this.beyondNode + 1];
      int[] var2 = new int[this.beyondNode + 1];
      Arrays.fill(var1, -1);
      Arrays.fill(var2, -1);
      int var3 = 0;

      for (int var4 = 0; var4 < this.beyondNode; var4++) {
         if (this.leaves[var4 / 64].has(this.leaves[var4 / 64].known, var4 % 64)) {
            int var5 = this.find(var4);
            var2[var5] = var2[var5] < 0 ? var3++ : var2[var5];
            var1[var4] = var2[var5];
         }
      }

      var1[this.beyondNode] = var3++;
      boolean[] var14 = new boolean[var3];
      boolean[] var15 = new boolean[var3];
      boolean[] var6 = new boolean[var3];
      var14[var3 - 1] = true;

      for (int var7 = 0; var7 < this.beyondNode; var7++) {
         int var8 = var1[var7];
         if (var8 >= 0) {
            boolean var9 = this.leaves[var7 / 64].room(var7 % 64) < 0L;
            var14[var8] |= var9;
            var15[var8] |= !var9;
            var6[var8] |= this.uncertain[var7];
         }
      }

      boolean[] var16 = new boolean[var3];

      for (int var17 = 0; var17 < var3; var17++) {
         var16[var17] = var15[var17] && !var14[var17] && !var6[var17];
      }

      HashMap var18 = new HashMap(this.leaves.length * 2);

      for (Fragment var12 : this.leaves) {
         var18.put(var12.key(), var12);
      }

      return new SectorGraph(
         var18,
         this.index,
         Arrays.copyOf(var1, this.beyondNode),
         var14,
         var16,
         this.merged(var1),
         Arrays.copyOf(this.doorLeaf, this.doors),
         Arrays.copyOf(this.doorSlot, this.doors),
         Arrays.copyOf(this.doorWindow, this.doors),
         Arrays.copyOf(this.doorPlace, this.doors * 5)
      );
   }

   private SectorGraph.Openings merged(int[] var1) {
      SectorGraph.Openings var2 = new SectorGraph.Openings();
      HashMap var3 = new HashMap();

      for (int var4 = 0; var4 < this.raw.count; var4++) {
         int var5 = var1[this.raw.a[var4]];
         int var6 = var1[this.raw.b[var4]];
         if (var5 != var6) {
            int var7 = var4 * 5;
            float[] var8 = this.raw.place;
            byte var9 = this.raw.kind[var4];
            float var10 = var9 == 0 ? var8[var7 + 1] : (var9 == 1 ? var8[var7] : 0.0F);
            GraphBuilder.Span var11 = this.raw.door[var4] >= 0 ? null : new GraphBuilder.Span(var5, var6, var9, var10, var8[var7 + 4]);
            Integer var12 = var11 == null ? null : (Integer)var3.get(var11);
            if (var12 != null) {
               int var13 = var12 * 5;
               var2.place[var13] = Math.min(var2.place[var13], var8[var7]);
               var2.place[var13 + 1] = Math.min(var2.place[var13 + 1], var8[var7 + 1]);
               var2.place[var13 + 2] = Math.max(var2.place[var13 + 2], var8[var7 + 2]);
               var2.place[var13 + 3] = Math.max(var2.place[var13 + 3], var8[var7 + 3]);
            } else {
               int var14 = var2.add(var5, var6, this.raw.door[var4], var9);
               System.arraycopy(var8, var7, var2.place, var14 * 5, 5);
               if (var11 != null) {
                  var3.put(var11, var14);
               }
            }
         }
      }

      return var2;
   }

   private record Span(int a, int b, byte kind, float line, float level) {
   }
}
