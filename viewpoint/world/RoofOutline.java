package viewpoint.world;

import java.util.Arrays;

final class RoofOutline {
   private static final int NEAR = 16;
   private static final int WALK = 7;
   private static final int REACH = 6;
   private static final int ACROSS = 3;
   private static final int SHARE = 4;
   private static final int OLDER_PITCH = 3;
   private static final byte UNKNOWN = 0;
   private static final byte EAVE = 1;
   private static final byte NOT = 2;
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};
   private final int width;
   private final int height;
   private final RoofPiece[] pieces;
   private final byte[] flags;
   private final byte[][] edges;
   private final boolean[] shallow;
   private final boolean[] owned;
   private final float[][] eaveHeights;
   private final int[] reached;
   private final int[] steps;
   private final int[] queue;
   private final int[] roof;
   private int walk;
   private int roofWalk;
   private final int[] pitches = new int[128];
   private int pitch;
   private float top;
   private boolean room;
   private boolean capped;
   private int straightSteps;
   private int nearSteps;
   private int straightAt;
   private int nearAt;
   private int shownBy = -1;
   private boolean acrossTop;

   RoofOutline(int var1, int var2, RoofPiece[] var3, byte[] var4, boolean[] var5, boolean[] var6) {
      this.width = var1;
      this.height = var2;
      this.pieces = var3;
      this.flags = var4;
      this.edges = new byte[4][var1 * var2];
      this.eaveHeights = new float[4][var1 * var2];
      this.reached = new int[var4.length];
      this.steps = new int[var4.length];
      this.queue = new int[var4.length];
      this.roof = new int[var4.length];
      this.shallow = var5;
      this.owned = var6;
   }

   void build(RoofSurface var1) {
      float[] var2 = new float[9];
      boolean[] var3 = new boolean[this.flags.length];

      for (int var4 = 0; var4 < this.flags.length; var4++) {
         if (this.outline(var4) && !this.owned[var4]) {
            this.nearby(var4);
            this.pitch = this.pitch == 0 && this.capped ? 3 : this.pitch;
            if (this.pitch != 0 && this.room && (!this.keeps(var4) || this.pitch == this.pieces[var4].perStorey)) {
               float var5 = 2.4494896F / this.pitch;
               int var6 = Math.min(6, (int)Math.ceil(this.top / var5) + 1);

               for (int var7 = 0; var7 < 9; var7++) {
                  float var8 = var4 % this.width + var7 % 3 / 2.0F;
                  float var9 = var4 / this.width + var7 / 3 / 2.0F;
                  float var10 = Math.min(this.top, this.rise(var8, var9, var5, var6));
                  var2[var7] = this.keeps(var4) ? Math.max(var10, var1.height(var4 % this.width, var4 / this.width, var7)) : var10;
               }

               var1.set(var4, (byte)(this.house(var4) ? 1 : 2), var2);
               var3[var4] = !this.keeps(var4);
            }
         }
      }

      for (int var11 = 0; var11 < this.flags.length; var11++) {
         if (var3[var11]) {
            this.meet(var1, var11, var2);
         }
      }
   }

   private void meet(RoofSurface var1, int var2, float[] var3) {
      int var4 = var2 % this.width;
      int var5 = var2 / this.width;
      boolean var6 = false;

      for (int var7 = 0; var7 < 9; var7++) {
         var3[var7] = var1.height(var4, var5, var7);
         float var8 = Float.NaN;

         for (int var9 = 0; var9 < 9; var9++) {
            int var10 = var9 % 3 - 1;
            int var11 = var9 / 3 - 1;
            int var12 = var7 % 3 - 2 * var10;
            int var13 = var7 / 3 - 2 * var11;
            int var14 = var4 + var10;
            int var15 = var5 + var11;
            if ((var10 | var11) != 0
               && var12 >= 0
               && var12 <= 2
               && var13 >= 0
               && var13 <= 2
               && var14 >= 0
               && var15 >= 0
               && var14 < this.width
               && var15 < this.height) {
               int var16 = var15 * this.width + var14;
               float var17 = var1.height(var14, var15, var13 * 3 + var12);
               if (this.keeps(var16) && Math.abs(var17 - var3[var7]) < 2.4494896F / this.pieces[var16].perStorey) {
                  var8 = Float.isNaN(var8) ? var17 : Math.max(var8, var17);
               }
            }
         }

         var6 |= !Float.isNaN(var8);
         var3[var7] = Float.isNaN(var8) ? var3[var7] : var8;
      }

      if (var6) {
         var1.set(var2, var1.source(var4, var5), var3);
      }
   }

   private float rise(float var1, float var2, float var3, int var4) {
      float var5 = var3 * var4;
      int var6 = (int)Math.floor(var1);
      int var7 = (int)Math.floor(var2);

      for (int var8 = Math.max(0, var7 - var4 - 1); var8 <= Math.min(this.height - 1, var7 + var4); var8++) {
         for (int var9 = Math.max(0, var6 - var4 - 1); var9 <= Math.min(this.width - 1, var6 + var4); var9++) {
            int var10 = var8 * this.width + var9;

            for (int var11 = 0; var11 < 4 && this.roof[var10] == this.roofWalk && this.outline(var10); var11++) {
               if (this.edge(var10, var11) == 1) {
                  float var12 = var9 + (DX[var11] + 1) / 2.0F;
                  float var13 = var8 + (DZ[var11] + 1) / 2.0F;
                  float var14 = DX[var11] != 0 ? Math.abs(var1 - var12) : Math.abs(var2 - var13);
                  float var15 = DX[var11] != 0 ? outside(var2, var8) : outside(var1, var9);
                  var5 = Math.min(var5, this.eaveHeights[var11][var10] + var3 * Math.max(var14, var15));
               }
            }
         }
      }

      return var5;
   }

   private byte edge(int var1, int var2) {
      if (this.edges[var2][var1] == 0) {
         this.edges[var2][var1] = this.decide(var1, var2);
         if (this.edges[var2][var1] == 1 && this.leads(var1)) {
            this.eaveHeights[var2][var1] = Math.max(0.0F, this.pieces[var1].height(DX[var2] * 0.5F, DZ[var2] * 0.5F));
         } else if (this.edges[var2][var1] == 1 && this.shownBy >= 0) {
            this.eaveHeights[var2][var1] = this.foot(this.shownBy, var2);
         }
      }

      return this.edges[var2][var1];
   }

   private byte decide(int var1, int var2) {
      int var3 = this.neighbour(var1, var2);
      byte var4 = this.shown(var1, var2);
      if (var4 != 0) {
         return var4;
      } else {
         boolean var5 = this.fallsAcross(var1, (var2 + 1) % 4) || this.fallsAcross(var1, (var2 + 3) % 4);
         this.shownBy = -1;
         if (var5) {
            return 2;
         } else {
            return (byte)(this.walkable(var3) ? 2 : 1);
         }
      }
   }

   private float foot(int var1, int var2) {
      int var3 = (var2 + 2) % 4;
      if (falls(this.pieces[var1], var3) && this.pieces[var1].perStorey == 3) {
         for (int var4 = 0; var4 < 7; var4++) {
            int var5 = this.neighbour(var1, var3);
            if (var5 < 0 || !this.leads(var5) || !falls(this.pieces[var5], var3)) {
               break;
            }

            var1 = var5;
         }

         return this.outline(this.neighbour(var1, var3)) ? 0.0F : Math.max(0.0F, this.pieces[var1].height(DX[var3] * 0.5F, DZ[var3] * 0.5F));
      } else {
         return 0.0F;
      }
   }

   private boolean fallsAcross(int var1, int var2) {
      int var3 = var1;

      for (int var4 = 0; var4 < 3 && this.outline(this.neighbour(var3, var2)); var4++) {
         var3 = this.neighbour(var3, var2);
      }

      return this.outline(this.neighbour(var3, var2)) ? false : this.shown(var3, var2) == 1;
   }

   private byte shown(int var1, int var2) {
      int var3 = this.neighbour(var1, var2);
      int var4 = (var2 + 2) % 4;
      this.shownBy = -1;
      if (var3 < 0 || this.outline(var3) || this.pieces[var3] != null && this.pieces[var3].kind == 5 || (this.flags[var3] & 24) != 0) {
         return 2;
      }

      if (this.leads(var1)) {
         return (byte)(falls(this.pieces[var1], var2) ? 1 : 2);
      }

      RoofPiece var5 = this.straightIn(var1, var4);
      RoofPiece var6 = this.nearest(var1);
      boolean var7 = var5 != null && (this.acrossTop || var6 == null || this.straightSteps <= this.nearSteps);
      RoofPiece var8 = var7 ? var5 : var6;
      if (var8 == null) {
         return 0;
      }

      this.shownBy = var7 ? this.straightAt : this.nearAt;
      return (byte)(!falls(var8, var2) && !falls(var8, var4) ? 2 : 1);
   }

   private RoofPiece straightIn(int var1, int var2) {
      int var3 = var1;
      this.acrossTop = false;

      for (int var4 = 0; var4 < 7 && this.walkable(var3); var4++) {
         RoofPiece var5 = this.pieces[var3];
         if (this.decides(var3) && this.pastFoot(var3, var1)) {
            return null;
         }

         if (this.decides(var3)) {
            this.straightSteps = var4;
            this.straightAt = var3;
            return var5;
         }

         this.acrossTop = this.acrossTop | (var5 != null && var5.kind == 5);
         var3 = this.neighbour(var3, var2);
      }

      return null;
   }

   private RoofPiece nearest(int var1) {
      int var2 = 0;
      int var3 = 0;
      this.walk++;
      this.reached[var1] = this.walk;
      this.steps[var1] = 0;
      this.queue[var3++] = var1;

      while (var2 < var3) {
         int var4 = this.queue[var2++];
         if (this.decides(var4) && !this.pastFoot(var4, var1)) {
            this.nearSteps = this.steps[var4];
            this.nearAt = var4;
            return this.pieces[var4];
         }

         for (int var5 = 0; var5 < 4 && this.steps[var4] < 7; var5++) {
            int var6 = this.neighbour(var4, var5);
            if (this.outline(var6) && this.reached[var6] != this.walk) {
               this.reached[var6] = this.walk;
               this.steps[var6] = this.steps[var4] + 1;
               this.queue[var3++] = var6;
            }
         }
      }

      return null;
   }

   private void nearby(int var1) {
      int var2 = 0;
      int var3 = 0;
      this.roofWalk++;
      this.top = 0.0F;
      this.room = false;
      this.capped = false;
      Arrays.fill(this.pitches, 0);
      this.roof[var1] = this.roofWalk;
      this.steps[var1] = 0;
      this.queue[var3++] = var1;

      while (var2 < var3) {
         int var4 = this.queue[var2++];
         RoofPiece var5 = this.pieces[var4];
         this.room = this.room | (this.flags[var4] & 4) != 0;
         this.capped = this.capped | (this.flags[var4] & 32) != 0;
         if (this.leads(var4)) {
            this.pitches[var5.perStorey]++;
         }

         if (this.house(var4)) {
            this.top = Math.max(this.top, highest(var5));
         }

         if ((this.flags[var4] & 32) != 0) {
            this.top = Math.max(this.top, 2.4494896F);
         }

         for (int var6 = 0; var6 < 4 && this.steps[var4] < 16; var6++) {
            int var7 = this.neighbour(var4, var6);
            if (this.walkable(var7) && this.roof[var7] != this.roofWalk) {
               this.roof[var7] = this.roofWalk;
               this.steps[var7] = this.steps[var4] + 1;
               this.queue[var3++] = var7;
            }
         }
      }

      int var10 = 0;

      for (int var8 : this.pitches) {
         var10 += var8;
      }

      this.pitch = 0;

      for (int var12 = 1; var12 < this.pitches.length && this.pitch == 0; var12++) {
         this.pitch = this.pitches[var12] * 4 >= var10 && this.pitches[var12] > 0 ? var12 : 0;
      }
   }

   private boolean pastFoot(int var1, int var2) {
      int var3 = var2 % this.width - var1 % this.width;
      int var4 = var2 / this.width - var1 / this.width;

      for (int var5 = 0; var5 < 4; var5++) {
         int var6 = DX[var5] * var3 + DZ[var5] * var4;
         if (var6 >= this.pieces[var1].step && falls(this.pieces[var1], var5)) {
            return true;
         }
      }

      return false;
   }

   private static float highest(RoofPiece var0) {
      float var1 = 0.0F;

      for (int var2 = 0; var2 < 9; var2++) {
         var1 = Math.max(var1, var0.height(var2 % 3 / 2.0F - 0.5F, var2 / 3 / 2.0F - 0.5F));
      }

      return var1;
   }

   private boolean outline(int var1) {
      if (var1 >= 0 && !this.porch(var1) && (this.pieces[var1] == null || this.pieces[var1].kind != 5)) {
         int var2 = this.flags[var1] & 6;
         return var2 == 4 || this.house(var1);
      } else {
         return false;
      }
   }

   private boolean walkable(int var1) {
      return var1 >= 0 && this.flags[var1] != 0 && !this.porch(var1);
   }

   private static boolean falls(RoofPiece var0, int var1) {
      return var0.kind == 6 || var0.facing == var1 || var0.kind != 0 && var0.other == var1;
   }

   private static boolean house(RoofPiece var0) {
      return var0 != null && var0.planar() && !var0.porch();
   }

   private boolean keeps(int var1) {
      return house(this.pieces[var1]) && this.pieces[var1].kind != 5;
   }

   private boolean house(int var1) {
      return house(this.pieces[var1]) || this.shallow[var1];
   }

   private boolean leads(int var1) {
      return this.house(var1) && this.pieces[var1].kind != 5;
   }

   private boolean decides(int var1) {
      return this.leads(var1) && this.pieces[var1].kind != 2;
   }

   private boolean porch(int var1) {
      return porch(this.pieces[var1]) && !this.shallow[var1];
   }

   private static boolean porch(RoofPiece var0) {
      return var0 != null && var0.porch();
   }

   private static float outside(float var0, int var1) {
      return Math.max(0.0F, Math.max(var1 - var0, var0 - (var1 + 1)));
   }

   private int neighbour(int var1, int var2) {
      int var3 = var1 % this.width + DX[var2];
      int var4 = var1 / this.width + DZ[var2];
      return var3 >= 0 && var4 >= 0 && var3 < this.width && var4 < this.height ? var4 * this.width + var3 : -1;
   }
}
