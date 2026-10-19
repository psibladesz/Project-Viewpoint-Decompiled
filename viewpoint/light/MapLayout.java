package viewpoint.light;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MapLayout {
   private static final int SIZE = 64;
   private static final int NO_ROOM = -1;
   private final MapSquares[] around;
   private final MapSquares centre;
   private final long[][] rooms;
   private final boolean[] roofed;
   private final boolean[] exterior;

   private MapLayout(MapSquares[] var1, long[][] var2) {
      this.around = var1;
      this.centre = var1[4];
      this.rooms = var2;
      this.roofed = new boolean[this.centre.levels * 64 * 64];
      this.exterior = new boolean[this.centre.levels * 64 * 64];
   }

   public static MapLayout.Block build(MapSquares[] var0, long[][] var1) {
      MapLayout var2 = new MapLayout(var0, var1);
      MapSquares var3 = var2.centre;
      var2.roofsAndRooms();
      ArrayList var4 = new ArrayList();

      for (int var5 = 0; var5 < var3.levels; var5++) {
         for (int var6 = 0; var6 < 64; var6 += 8) {
            for (int var7 = 0; var7 < 64; var7 += 8) {
               LightLayouts.Level var8 = var2.level(var7, var6, var5);
               if (var8 != null) {
                  var4.add(var8);
               }
            }
         }
      }

      return new MapLayout.Block(var3.bx, var3.by, var4, Arrays.copyOf(var3.lamps, var3.lampCount * 4));
   }

   private void roofsAndRooms() {
      int var1 = this.centre.levels;

      for (int var2 = 0; var2 < 64; var2++) {
         for (int var3 = 0; var3 < 64; var3++) {
            int var4 = this.centre.bx * 64 + var3;
            int var5 = this.centre.by * 64 + var2;

            for (int var6 = var1 - 1; var6 > 0; var6--) {
               int var7 = this.centre.at(var4, var5, var6);
               if (var7 >= 0 && (var7 & 8196) != 0) {
                  for (int var8 = var6 - 1; var8 >= 0; var8--) {
                     this.roofed[(var8 * 64 + var2) * 64 + var3] = true;
                  }
                  break;
               }
            }

            for (int var9 = 0; var9 < var1; var9++) {
               int var10 = (var9 * 64 + var2) * 64 + var3;
               this.exterior[var10] = this.room(var3, var2, var9) == -1L && !this.roofed[var10];
            }
         }
      }
   }

   private long room(int var1, int var2, int var3) {
      int var4 = this.centre.bits[(var3 * 64 + var2) * 64 + var1];
      return (var4 & 1073741824) != 0 && this.rooms != null && var3 < this.rooms.length && this.rooms[var3] != null ? this.rooms[var3][var2 * 64 + var1] : -1L;
   }

   private LightLayouts.Level level(int var1, int var2, int var3) {
      byte[] var4 = new byte[64];
      int[] var5 = new int[64];
      long[] var6 = new long[64];
      boolean var7 = false;

      for (int var8 = 0; var8 < 64; var8++) {
         int var9 = var1 + var8 % 8;
         int var10 = var2 + var8 / 8;
         int var11 = this.centre.bx * 64 + var9;
         int var12 = this.centre.by * 64 + var10;
         int var13 = this.centre.at(var11, var12, var3);
         var6[var8] = -1L;
         if (var13 >= 0) {
            var7 = true;
            var6[var8] = this.room(var9, var10, var3);
            var4[var8] = (byte)(this.flags(var9, var10, var3) | 1);

            for (int var14 = 0; var14 < 10; var14++) {
               var5[var8] |= this.test(var11, var12, var3, LightLayout.DX[var14], LightLayout.DY[var14], LightLayout.DZ[var14]) << var14 * 3;
            }
         }
      }

      return var7
         ? new LightLayouts.Level(Math.floorDiv(this.centre.bx * 64 + var1, 8), Math.floorDiv(this.centre.by * 64 + var2, 8), var3, var4, var5, var6, null)
         : null;
   }

   private int flags(int var1, int var2, int var3) {
      boolean var4 = this.exterior[(var3 * 64 + var2) * 64 + var1];
      return (this.openAir(var1, var2, var3) ? 2 : 0) | (var4 ? 64 : 0) | (this.room(var1, var2, var3) == -1L ? 128 : 0);
   }

   private boolean openAir(int var1, int var2, int var3) {
      int var4 = this.centre.bx * 64 + var1;
      int var5 = this.centre.by * 64 + var2;

      for (int var6 = var3; var6 < this.centre.levels; var6++) {
         if (var6 > var3 && this.centre.at(var4, var5, var6) < 0) {
            return true;
         }

         if (!this.exterior[(var6 * 64 + var2) * 64 + var1]) {
            return false;
         }
      }

      return true;
   }

   private int test(int var1, int var2, int var3, int var4, int var5, int var6) {
      if (var4 != 0 && var5 != 0) {
         int var7 = this.diagonal(var1, var2, var3, var4, var5);
         if (var7 != 4 && this.at(var1 + var4, var2 + var5, var3) >= 0) {
            var7 = this.diagonal(var1 + var4, var2 + var5, var3, -var4, -var5);
         }

         return var7;
      } else {
         return this.adjacent(var1, var2, var3, var4, var5, var6);
      }
   }

   private int diagonal(int var1, int var2, int var3, int var4, int var5) {
      int var6 = this.adjacent(var1, var2, var3, var4, 0, 0);
      if (var6 == 4) {
         return var6;
      } else {
         int var7 = this.adjacent(var1, var2, var3, 0, var5, 0);
         if (var7 == 4) {
            return var7;
         } else {
            return var6 != 2 && var7 != 2 ? this.adjacent(var1, var2, var3, var4, var5, 0) : 2;
         }
      }
   }

   private int adjacent(int var1, int var2, int var3, int var4, int var5, int var6) {
      int var7 = this.at(var1, var2, var3);
      int var8 = this.at(var1 + var4, var2 + var5, var3 + var6);
      byte var9 = 0;
      if (var8 >= 0 && var6 == 0) {
         int var10 = special(var7, true, var4, var5);
         int var11 = special(var8, false, var4, var5);
         if (var10 == 4 || var11 == 4) {
            return 4;
         }

         var9 = (byte)(var11 != 0 ? var11 : var10);
      } else if (var6 > 0 && var8 >= 0 && this.outside(var1, var2, var3 + var6) && !this.outside(var1, var2, var3)) {
         var9 = 4;
      }

      return this.visionBlocked(var1, var2, var3, var1 + var4, var2 + var5, var3 + var6) ? 4 : var9;
   }

   private static int special(int var0, boolean var1, int var2, int var3) {
      boolean var4 = var1 ? var3 < 0 : var3 > 0;
      boolean var5 = var1 ? var2 < 0 : var2 > 0;
      byte var6 = 0;
      if (var4 && (var0 & 131072) != 0) {
         if ((var0 & 262144) == 0) {
            return 4;
         }

         var6 = (byte)((var0 & 524288) != 0 ? 1 : 3);
      }

      if (var5 && (var0 & 1048576) != 0) {
         if ((var0 & 2097152) == 0) {
            return 4;
         }

         var6 = (byte)((var0 & 4194304) != 0 ? 1 : 3);
      }

      if (var4 && (var0 & 8388608) != 0 || var5 && (var0 & 16777216) != 0) {
         var6 = 2;
      }

      return var6;
   }

   private boolean visionBlocked(int var1, int var2, int var3, int var4, int var5, int var6) {
      int var7 = this.at(var1, var2, var3);
      int var8 = this.at(var4, var5, var6);
      if (var8 >= 0 && var7 >= 0) {
         if (((var7 | var8) & 2) != 0) {
            return false;
         }

         if (var3 != var6 && this.betweenLevels(var7, var8, var1, var2, var3, var4, var5, var6)) {
            return true;
         }

         boolean var9 = var5 < var2 && cut(var7, 32, 128, 32768) && (var7 & 512) == 0
            || var4 < var1 && cut(var7, 64, 256, 65536) && (var7 & 1024) == 0
            || var5 > var2 && cut(var8, 32, 128, 32768) && (var8 & 512) == 0
            || var4 > var1 && cut(var8, 64, 256, 65536) && (var8 & 1024) == 0;
         return !var9 && (var8 & 6144) == 0
            ? var4 != var1
               && var5 != var2
               && (
                  this.visionBlocked(var1, var2, var3, var1, var5, var3)
                     || this.visionBlocked(var1, var2, var3, var4, var2, var3)
                     || this.visionBlocked(var4, var5, var6, var1, var5, var3)
                     || this.visionBlocked(var4, var5, var6, var4, var2, var3)
               )
            : true;
      } else {
         return false;
      }
   }

   private boolean betweenLevels(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      if (var8 > var5) {
         int var10 = this.at(var3, var4, var8);
         return opaqueFloor(var2) || (var1 & 16) != 0 || var10 >= 0 && opaqueFloor(var10);
      } else {
         int var9 = this.at(var6, var7, var5);
         return opaqueFloor(var1) || (var1 & 16) != 0 || var9 >= 0 && opaqueFloor(var9);
      }
   }

   private static boolean cut(int var0, int var1, int var2, int var3) {
      return (var0 & var1) != 0 && ((var0 & var2) == 0 || (var0 & var3) != 0);
   }

   private static boolean opaqueFloor(int var0) {
      return (var0 & 4) != 0 && ((var0 & 8) == 0 || (var0 & 16384) != 0);
   }

   private boolean outside(int var1, int var2, int var3) {
      int var4 = var1 - this.centre.bx * 64;
      int var5 = var2 - this.centre.by * 64;
      return var3 >= 0 && var3 < this.centre.levels && this.exterior[(var3 * 64 + var5) * 64 + var4];
   }

   private int at(int var1, int var2, int var3) {
      int var4 = Math.floorDiv(var1, 64) - this.centre.bx;
      int var5 = Math.floorDiv(var2, 64) - this.centre.by;
      if (var4 >= -1 && var4 <= 1 && var5 >= -1 && var5 <= 1) {
         MapSquares var6 = this.around[(var5 + 1) * 3 + var4 + 1];
         return var6 == null ? -1 : var6.at(var1, var2, var3);
      } else {
         return -1;
      }
   }

   public static final class Block {
      public final int bx;
      public final int by;
      final List<LightLayouts.Level> levels;
      final int[] lamps;

      Block(int var1, int var2, List<LightLayouts.Level> var3, int[] var4) {
         this.bx = var1;
         this.by = var2;
         this.levels = var3;
         this.lamps = var4;
      }
   }
}
