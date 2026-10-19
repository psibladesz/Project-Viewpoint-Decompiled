package viewpoint.visibility;

import java.util.Arrays;
import java.util.HashMap;
import viewpoint.platform.LongMap;

final class SectorGraph {
   static final byte NORTH_EDGE = 0;
   static final byte WEST_EDGE = 1;
   static final byte FLOOR = 2;
   final HashMap<Long, Fragment> sources;
   private final LongMap<Integer> leafIndex;
   private final int[] sectorOf;
   final int sectors;
   final int beyond;
   final boolean[] exterior;
   final boolean[] hideable;
   final int openings;
   final int[] openA;
   final int[] openB;
   final int[] openDoor;
   final byte[] openKind;
   final float[] openPlace;
   final int[] adjacentStart;
   final int[] adjacent;
   final long[] doorLeaf;
   final int[] doorSlot;
   final boolean[] doorWindow;
   final float[] doorPlace;

   SectorGraph(
      HashMap<Long, Fragment> var1,
      LongMap<Integer> var2,
      int[] var3,
      boolean[] var4,
      boolean[] var5,
      SectorGraph.Openings var6,
      long[] var7,
      int[] var8,
      boolean[] var9,
      float[] var10
   ) {
      this.sources = var1;
      this.leafIndex = var2;
      this.sectorOf = var3;
      this.sectors = var4.length;
      this.beyond = this.sectors - 1;
      this.exterior = var4;
      this.hideable = var5;
      this.openings = var6.count;
      this.openA = var6.a;
      this.openB = var6.b;
      this.openDoor = var6.door;
      this.openKind = var6.kind;
      this.openPlace = var6.place;
      this.doorLeaf = var7;
      this.doorSlot = var8;
      this.doorWindow = var9;
      this.doorPlace = var10;
      this.adjacentStart = new int[this.sectors + 1];

      for (int var11 = 0; var11 < this.openings; var11++) {
         this.adjacentStart[this.openA[var11] + 1]++;
         this.adjacentStart[this.openB[var11] + 1]++;
      }

      for (int var13 = 0; var13 < this.sectors; var13++) {
         this.adjacentStart[var13 + 1] = this.adjacentStart[var13 + 1] + this.adjacentStart[var13];
      }

      this.adjacent = new int[this.adjacentStart[this.sectors]];
      int[] var14 = (int[])this.adjacentStart.clone();

      for (int var12 = 0; var12 < this.openings; this.adjacent[var14[this.openB[var12]]++] = var12++) {
         this.adjacent[var14[this.openA[var12]]++] = var12;
      }
   }

   int sector(long var1, int var3) {
      Integer var4 = this.leafIndex.get(var1);
      return var4 == null ? -1 : this.sectorOf[var4 * 64 + var3];
   }

   static final class Openings {
      int count;
      int[] a = new int[256];
      int[] b = new int[256];
      int[] door = new int[256];
      byte[] kind = new byte[256];
      float[] place = new float[1280];

      int add(int var1, int var2, int var3, byte var4) {
         if (this.count == this.a.length) {
            this.a = Arrays.copyOf(this.a, this.count * 2);
            this.b = Arrays.copyOf(this.b, this.count * 2);
            this.door = Arrays.copyOf(this.door, this.count * 2);
            this.kind = Arrays.copyOf(this.kind, this.count * 2);
            this.place = Arrays.copyOf(this.place, this.count * 10);
         }

         this.a[this.count] = var1;
         this.b[this.count] = var2;
         this.door[this.count] = var3;
         this.kind[this.count] = var4;
         return this.count++;
      }
   }
}
