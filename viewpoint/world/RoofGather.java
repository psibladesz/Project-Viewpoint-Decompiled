package viewpoint.world;

import java.util.Arrays;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Profile;
import viewpoint.visibility.Owner;
import zombie.core.textures.Texture;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoWorld;
import zombie.iso.sprite.IsoSprite;

final class RoofGather {
   static final LiveSettings.Toggle BUILT = LiveSettings.toggle("roofs.built", "Roofs built from their tiles", "World/Roofs", true);
   static final LiveSettings.Toggle VANILLA = LiveSettings.toggle("roofs.vanilla", "Vanilla roofs (no model pack's)", "World/Roofs", true);
   static final LiveSettings.Toggle TRIMMED = LiveSettings.toggle("roofs.trims", "Gutters and barge boards", "World/Roofs", false);
   private static final int SIDE = 8;
   private static final int SQUARES = 64;
   private static final int CHUNKS = 5;
   private static final int MIDDLE = 2;
   private static final int SIDES = 4;
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};
   private static final int ACROSS_SIDES = 5;
   private static final int ALONG_SIDES = 10;
   private static final int FACADE_REACH = 4;
   private static final Texture[] NO_FACADE = new Texture[2];
   private static RoofWindow window;
   private static IsoChunk chunk;
   private static int level;
   private static final Texture[] own = new Texture[64];
   private static final Texture[] lent = new Texture[64];
   private static final int[] ownDraws = new int[64];
   private static final int[] lentDraws = new int[64];
   private static final int[] gables = new int[64];
   private static final Texture[][] gableArt = new Texture[64][4];
   private static final boolean[] leftBelow = new boolean[64];
   private static final boolean[] packed = new boolean[64];
   private static final RoofLevels.Level[][][] around = new RoofLevels.Level[3][5][5];
   private static final int LEVELS = 0;
   private static final int MISSING = 1;
   private static final int OWN = 2;
   private static final int LENT = 3;
   private static final int REPLACED = 4;
   private static final int WAITING = 5;
   private static final int GABLES = 6;
   private static final int PAINTED = 7;
   private static final int TRIMS = 8;
   private static final char TRIM = 'T';
   private static final char ROOF = 'R';
   private static final char DRAWN = 'D';
   private static final int[] counts = new int[9];
   private static final int[] reported = new int[9];

   static void begin(IsoChunk var0, int var1, boolean var2) {
      window = null;
      chunk = var0;
      level = var1;
      Arrays.fill(ownDraws, 0);
      Arrays.fill(lentDraws, 0);
      Arrays.fill(gables, 0);
      Arrays.fill(leftBelow, false);
      Arrays.fill(packed, false);
      IsoCell var3 = IsoWorld.instance.getCell();
      RoofLevels.Level var4 = var2 ? RoofLevels.captured(var0, var1) : RoofLevels.held(var0, var1);
      boolean var5 = BUILT.get() && roofed(var3, var0, var1, var4);
      boolean var6 = var5 && collect(var3, var0.wx, var0.wy, var1);
      if (var5) {
         assemble();
         counts[0]++;
         counts[1] = counts[1] + (var6 ? 1 : 0);
      }

      RoofLevels.windowed(var0.wx, var0.wy, var1, var5, var6);
   }

   static boolean replaces(int var0, IsoSprite var1) {
      char var2 = fate(var0, var1);
      counts[8] = counts[8] + (var2 == 'T' ? 1 : 0);
      counts[4] = counts[4] + (var2 == 'R' ? 1 : 0);
      return var2 != 'D';
   }

   static char fate(int var0, IsoSprite var1) {
      boolean var2 = !TRIMMED.get() && RoofLevels.trim(var1.name, RoofLevels.roofTyped(var1));
      if (!BUILT.get() || !var2 && !RoofLevels.eave(var1.name)) {
         if (var0 >= 0 && window != null && ((ownDraws[var0] | lentDraws[var0]) != 0 || leftBelow[var0])) {
            return (char)(RoofLevels.piece(var1.name) != null ? 'R' : 'D');
         } else {
            return 'D';
         }
      } else {
         return 'T';
      }
   }

   static void packed(int var0, IsoSprite var1) {
      packed[var0] = packed[var0] | (var1 != null && window != null && RoofLevels.piece(var1.name) != null);
   }

   static void emit(int var0, float var1, float var2, float var3, boolean[] var4) {
      if (window != null && !packed[var0]) {
         WorldMesher.owner = Owner.of(var0, 0);
         boolean var5 = ((ownDraws[var0] | lentDraws[var0]) & 2) != 0;
         Texture[] var6 = !var5 && gables[var0] == 0 ? NO_FACADE : facades(var0, var4);
         int var7 = var5 ? RoofGables.inner(window, window.square(var0)) & sides(var6) : 0;
         float var8 = var5 ? facade(var0) + 1.0F : 0.0F;
         int var9 = var7 << 3;
         counts[2] = counts[2] + (emit(own[var0], walls(ownDraws[var0], var9), var1, var2, var3, var8, var4) ? 1 : 0);
         counts[3] = counts[3] + (emit(lent[var0], walls(lentDraws[var0], var9), var1, var2, var3, var8, var4) ? 1 : 0);
         if (gables[var0] != 0 || var7 != 0) {
            emitGables(var0, var1, var2, var3, var6, var7, var4);
         }
      }
   }

   private static int walls(int var0, int var1) {
      return var0 == 0 ? 0 : var0 | var1;
   }

   private static Texture[] facades(int var0, boolean[] var1) {
      IsoGridSquare var2 = roomUnder(var0);
      if (var2 == null) {
         return NO_FACADE;
      }

      Texture[] var3 = new Texture[2];

      for (int var4 = 0; var4 < 2; var4++) {
         IsoSprite var5 = Facades.plainFacade(var2, var4 == 1);
         Texture var6 = var5 == null ? null : var5.getTextureForCurrentFrame(IsoDirections.N);
         boolean var7 = var6 != null && var6.isReady() && var6.getTextureId() != null;
         var1[0] |= var6 != null && !var7;
         var3[var4] = var7 ? var6 : null;
      }

      return var3;
   }

   private static IsoGridSquare roomUnder(int var0) {
      IsoCell var1 = IsoWorld.instance.getCell();
      int var2 = chunk.wx * 8 + var0 % 8;
      int var3 = chunk.wy * 8 + var0 / 8;
      int var4 = window.square(var0);
      int var5 = var4 % window.width;
      int var6 = var4 / window.width;
      byte var7 = 15;
      IsoGridSquare var8 = room(var1.getGridSquare(var2, var3, level - 1));

      for (int var9 = 1; var9 <= 4 && var8 == null; var9++) {
         for (int var10 = 0; var10 < 4 && var8 == null; var10++) {
            int var11 = var5 + DX[var10] * var9;
            int var12 = var6 + DZ[var10] * var9;
            boolean var13 = var11 >= 0 && var12 >= 0 && var11 < window.width && var12 < window.height;
            var7 &= var13 && RoofGables.roofed(window, var12 * window.width + var11) ? -1 : ~(1 << var10);
            var8 = (var7 & 1 << var10) == 0 ? null : room(var1.getGridSquare(var2 + DX[var10] * var9, var3 + DZ[var10] * var9, level - 1));
         }
      }

      return var8;
   }

   private static IsoGridSquare room(IsoGridSquare var0) {
      return var0 != null && var0.getRoom() != null ? var0 : null;
   }

   private static int sides(Texture[] var0) {
      return (var0[1] != null ? 5 : 0) | (var0[0] != null ? 10 : 0);
   }

   static RoofWindow end() {
      RoofWindow var0 = window;
      window = null;
      chunk = null;
      Arrays.fill(own, null);
      Arrays.fill(lent, null);

      for (Texture[] var4 : gableArt) {
         Arrays.fill(var4, null);
      }

      for (RoofLevels.Level[][] var12 : around) {
         for (RoofLevels.Level[] var8 : var12) {
            Arrays.fill(var8, null);
         }
      }

      return var0;
   }

   private static boolean emit(Texture var0, int var1, float var2, float var3, float var4, float var5, boolean[] var6) {
      if (var1 == 0 || var0 == null) {
         return false;
      } else if (var0.isReady() && var0.getTextureId() != null) {
         Recipe.Op var7 = WorldMesher.op(WorldMesher.batch(var0.getTextureId()), 6, null, var0, var2, var3, var4);
         var7.roof = window;
         var7.face = var1;
         var7.fill = (var1 & 2) != 0 ? var5 : 0.0F;
         return true;
      } else {
         var6[0] = true;
         counts[5]++;
         return false;
      }
   }

   private static int facade(int var0) {
      return RoofGables.colour(IsoWorld.instance.getCell(), chunk.wx * 8 + var0 % 8, chunk.wy * 8 + var0 / 8, level, 1);
   }

   private static void emitGables(int var0, float var1, float var2, float var3, Texture[] var4, int var5, boolean[] var6) {
      int var7 = gables[var0];
      int var8 = 0;
      byte var9 = 0;
      Texture[] var10 = gableArt[var0];

      for (int var11 = 0; var11 < 4; var11++) {
         if ((var7 & ~var9 & 1 << var11) != 0) {
            if (var10[var11] == null) {
               var8 |= 1 << var11;
            } else {
               byte var12 = 0;

               for (int var13 = var11; var13 < 4; var13++) {
                  var12 |= (var7 & 1 << var13) != 0 && var10[var13] == var10[var11] ? 1 << var13 : 0;
               }

               var9 |= var12;
               counts[6] = counts[6] + gable(var10[var11], var12, 0.0F, var1, var2, var3, var6);
            }
         }
      }

      for (int var16 = 0; var16 < 2; var16++) {
         int var18 = var16 == 1 ? 5 : 10;
         int var20 = (var8 | var5) & var18;
         if (var4[var16] != null && var20 != 0) {
            int var14 = var20 | ((var5 & var18) != 0 ? 64 : 0);
            counts[6] = counts[6] + gable(var4[var16], var14, 0.0F, var1, var2, var3, var6);
            var8 &= ~var18;
         }
      }

      if (var8 != 0) {
         RoofPiece var17 = window.pieces[window.square(var0)];
         boolean var19 = lent[var0] != null;
         Texture var21 = var19 ? lent[var0] : (var17 != null && !var17.unseen() ? own[var0] : null);
         int var22 = RoofGables.colour(IsoWorld.instance.getCell(), chunk.wx * 8 + var0 % 8, chunk.wy * 8 + var0 / 8, level, var8);
         int var15 = var8 | (var19 ? 16 : 32);
         counts[7] = counts[7] + gable(var21, var15, var22 + 1.0F, var1, var2, var3, var6);
      }
   }

   private static int gable(Texture var0, int var1, float var2, float var3, float var4, float var5, boolean[] var6) {
      if (var0 == null) {
         return 0;
      } else if (var0.isReady() && var0.getTextureId() != null) {
         Recipe.Op var7 = WorldMesher.op(WorldMesher.batch(var0.getTextureId()), 7, null, var0, var3, var4, var5);
         var7.roof = window;
         var7.face = var1;
         var7.fill = var2;
         return Integer.bitCount(var1 & 15);
      } else {
         var6[0] = true;
         counts[5]++;
         return 0;
      }
   }

   private static String report() {
      int[] var0 = new int[counts.length];

      for (int var1 = 0; var1 < var0.length; var1++) {
         int var2 = counts[var1];
         var0[var1] = var2 - reported[var1];
         reported[var1] = var2;
      }

      return String.format(
         "%s: levels %d (%d missing a chunk), squares %d in their own art and %d in lent art, tiles replaced %d, waiting for art %d, triangles cooked %d, gables' sides %d in their far end's art and %d painted, trims left out %d",
         BUILT.get() ? "built" : "the game's",
         var0[0],
         var0[1],
         var0[2],
         var0[3],
         var0[4],
         var0[5],
         RoofCook.takeTriangles(),
         var0[6],
         var0[7],
         var0[8]
      );
   }

   static String draws(int var0) {
      return (ownDraws[var0] != 0 ? "o" : "") + (lentDraws[var0] != 0 ? "l" : "") + (gables[var0] != 0 ? "g" : "");
   }

   static RoofWindow window() {
      return window;
   }

   private static boolean roofed(IsoCell var0, IsoChunk var1, int var2, RoofLevels.Level var3) {
      RoofLevels.Level var4 = RoofLevels.get(var0, var1.wx, var1.wy, var2 - 1);
      RoofLevels.Level var5 = RoofLevels.get(var0, var1.wx, var1.wy, var2 + 1);

      for (int var6 = 0; var6 < 64; var6++) {
         boolean var7 = (var3.bits[var6] & 3) != 0 || var5 != null && (RoofLevels.covers(var5.bits[var6]) || var5.lid[var6] == 2);
         if (var3.pieces[var6] != null
            || var4 != null && (var4.bits[var6] & 4) != 0 && !var7
            || var5 != null && RoofLevels.carries(var5.pieces[var6], var3.bits[var6])) {
            return true;
         }
      }

      RoofLevels.Level var10 = RoofLevels.get(var0, var1.wx + 1, var1.wy, var2);
      RoofLevels.Level var11 = RoofLevels.get(var0, var1.wx, var1.wy + 1, var2);
      RoofLevels.Level var8 = RoofLevels.get(var0, var1.wx + 1, var1.wy + 1, var2);

      for (int var9 = 0; var9 < 8; var9++) {
         if (var10 != null && beyond(var10.pieces[var9 * 8]) || var11 != null && beyond(var11.pieces[var9])) {
            return true;
         }
      }

      return var8 != null && beyond(var8.pieces[0]);
   }

   private static boolean beyond(RoofPiece var0) {
      return var0 != null && (var0.kind == 5 || ridge(var0));
   }

   private static boolean collect(IsoCell var0, int var1, int var2, int var3) {
      boolean var4 = false;

      for (int var5 = 0; var5 < 3; var5++) {
         for (int var6 = 0; var6 < 5; var6++) {
            for (int var7 = 0; var7 < 5; var7++) {
               RoofLevels.Level var8 = RoofLevels.get(var0, var1 + var7 - 2, var2 + var6 - 2, var3 - 1 + var5);
               around[var5][var6][var7] = var8;
               var4 |= var8 == null;
            }
         }
      }

      return var4;
   }

   private static void assemble() {
      int var0 = 0;
      int var1 = 4;
      int var2 = 0;
      int var3 = 4;

      while (var0 < 2 && unloaded(var0, true)) {
         var0++;
      }

      while (var1 > 2 && unloaded(var1, true)) {
         var1--;
      }

      while (var2 < 2 && unloaded(var2, false)) {
         var2++;
      }

      while (var3 > 2 && unloaded(var3, false)) {
         var3--;
      }

      int var4 = (var1 - var0 + 1) * 8;
      int var5 = (var3 - var2 + 1) * 8;
      RoofPiece[] var6 = new RoofPiece[var4 * var5];
      byte[] var7 = new byte[var4 * var5];
      byte[] var8 = new byte[var4 * var5];
      byte[] var9 = new byte[var4 * var5];
      Texture[] var10 = new Texture[var4 * var5];
      Texture[] var11 = new Texture[var4 * var5];
      Texture[] var12 = new Texture[var4 * var5];
      boolean[] var13 = new boolean[var4 * var5];
      boolean[] var14 = new boolean[var4 * var5];
      boolean[] var15 = new boolean[var4 * var5];

      for (int var16 = 0; var16 < var6.length; var16++) {
         int var17 = var16 % var4;
         int var18 = var16 / var4;
         int var19 = var18 % 8 * 8 + var17 % 8;
         RoofLevels.Level var20 = around[0][var2 + var18 / 8][var0 + var17 / 8];
         RoofLevels.Level var21 = around[1][var2 + var18 / 8][var0 + var17 / 8];
         RoofLevels.Level var22 = around[2][var2 + var18 / 8][var0 + var17 / 8];
         if (var21 != null) {
            var14[var16] = square(var16, var19, var20, var21, var22, var6, var7, var8, var10);
            var13[var16] = var10[var16] != null;
            var15[var16] = var21.lid[var19] == 1;
            var9[var16] = var21.bits[var19];
            var11[var16] = var21.gableN[var19];
            var12[var16] = var21.gableW[var19];
         }
      }

      RoofWindow.Inner var23 = RoofWindow.Inner.chunk((2 - var0) * 8, (2 - var2) * 8);
      RoofWindow var24 = RoofWindow.of(var4, var5, var23, var6, var7, var8, var13, var15);
      window = WindowLevels.attach(var24, level, chunk, var0 - 2, var2 - 2, var1 - var0 + 1, var3 - var2 + 1);

      for (int var25 = 0; var25 < 64; var25++) {
         leftBelow[var25] = var14[window.square(var25)];
         int var26 = window.square(var25);
         int var27 = window.lenders[var25];
         own[var25] = var6[var26] != null ? var10[var26] : null;
         lent[var25] = var27 >= 0 ? var10[var27] : null;
         ownDraws[var25] = window.ownDraws(var25, own[var25] != null, lent[var25] != null);
         lentDraws[var25] = window.lentDraws(var25, own[var25] != null, lent[var25] != null);
      }

      openGables(var9, var11, var12);
   }

   private static void openGables(byte[] var0, Texture[] var1, Texture[] var2) {
      int[] var3 = new int[4];

      for (int var4 = 0; var4 < 64; var4++) {
         gables[var4] = RoofGables.open(window, var0, window.square(var4), var3);

         for (int var5 = 0; var5 < 4; var5++) {
            int var6 = var3[var5];
            gableArt[var4][var5] = var6 < 0 ? null : (RoofGables.across(var5) ? var1 : var2)[var6];
         }
      }
   }

   private static boolean square(
      int var0, int var1, RoofLevels.Level var2, RoofLevels.Level var3, RoofLevels.Level var4, RoofPiece[] var5, byte[] var6, byte[] var7, Texture[] var8
   ) {
      byte var9 = var2 == null ? 0 : var2.bits[var1];
      byte var10 = var4 == null ? 0 : var4.bits[var1];
      if (var2 != null && RoofLevels.carries(var3.pieces[var1], var9)) {
         return true;
      }

      boolean var11 = var4 != null && var3.pieces[var1] == null && RoofLevels.carries(var4.pieces[var1], var3.bits[var1]);
      var5[var0] = var11 ? RoofLevels.lowered(var4.pieces[var1], 1) : var3.pieces[var1];
      var8[var0] = var11 ? var4.art[var1] : var3.art[var1];
      var7[var0] = (byte)(var11 ? 1 : 0);
      var6[var0] = RoofLevels.flags(var9, var3.bits[var1], var10, var4 == null ? 0 : var4.lid[var1], var11, var3.porch[var1]);
      return false;
   }

   private static boolean unloaded(int var0, boolean var1) {
      for (int var2 = 0; var2 < 5; var2++) {
         if ((var1 ? around[1][var2][var0] : around[1][var0][var2]) != null) {
            return false;
         }
      }

      return true;
   }

   private static boolean ridge(RoofPiece var0) {
      return var0 != null && var0.kind == 1 && (var0.facing + 2) % 4 == var0.other;
   }

   private RoofGather() {
   }

   static {
      Profile.roofsReport = RoofGather::report;
   }
}
