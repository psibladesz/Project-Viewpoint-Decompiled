package viewpoint.world;

import java.util.Arrays;
import java.util.Locale;
import zombie.core.textures.TextureID;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.sprite.IsoSprite;

public final class MapRoofs {
   public static byte bits(String var0, boolean var1) {
      return RoofLevels.bits(var0, var1);
   }

   public static byte edges(IsoSprite var0) {
      return (byte)RoofLevels.edges(var0);
   }

   public static boolean roofTile(String var0) {
      return RoofLevels.roofTile(var0);
   }

   public static boolean tiled(byte var0) {
      return (var0 & 1) != 0;
   }

   public static boolean floor(byte var0) {
      return (var0 & 2) != 0;
   }

   public static boolean perBuilding() {
      return WindowLevels.PER_BUILDING.get();
   }

   public static boolean trim(IsoSprite var0) {
      return RoofLevels.trim(var0.name, RoofLevels.roofTyped(var0)) || RoofLevels.eave(var0.name);
   }

   public static boolean vanilla(IsoSprite var0) {
      return RoofGather.VANILLA.get() && RoofLevels.roofPiece(var0.name, RoofLevels.roofTyped(var0), var0.getProperties().has(IsoFlagType.WallOverlay));
   }

   public static boolean outdoors(String var0) {
      return var0 != null && var0.toLowerCase(Locale.ROOT).contains("balcony");
   }

   public static RoofPiece piece(String var0) {
      return RoofLevels.piece(var0);
   }

   public static boolean carries(RoofPiece var0, boolean var1, boolean var2) {
      return RoofLevels.carries(var0, (byte)((var1 ? 2 : 0) | (var2 ? 4 : 0)));
   }

   public static RoofPiece lowered(RoofPiece var0) {
      return RoofLevels.lowered(var0, 1);
   }

   public static boolean flat(String var0) {
      return RoofLevels.flat(var0);
   }

   public static byte lidFlags(boolean var0, boolean var1, boolean var2, boolean var3) {
      byte var4 = (byte)((var1 ? 2 : 0) | (var2 ? 1 : 0));
      return RoofLevels.lidFlags(RoofLevels.lid(var0, var4, var3));
   }

   public static boolean[] place(
      Recipe var0, MapRoofs.Level var1, MapRoofs.Level var2, MapRoofs.Level var3, int var4, int var5, RoofWindow.Inner var6, float var7, RoofSurface var8
   ) {
      int var9 = var4 * var5;
      RoofPiece[] var10 = new RoofPiece[var9];
      byte[] var11 = new byte[var9];
      byte[] var12 = new byte[var9];
      TextureID[] var13 = new TextureID[var9];
      float[][] var14 = new float[var9][];
      boolean[] var15 = new boolean[var9];
      boolean[] var16 = new boolean[var9];
      boolean[] var17 = new boolean[var9];
      boolean var18 = false;

      for (int var19 = 0; var19 < var9; var19++) {
         var16[var19] = square(var19, var1, var2, var3, var10, var11, var12, var13, var14);
         var15[var19] = var14[var19] != null;
         var17[var19] = var2.flat[var19];
         var18 |= var16[var19] || var10[var19] != null || (var11[var19] & 7) == 4;
      }

      if (!var18) {
         return null;
      }

      RoofWindow var32 = RoofWindow.of(var4, var5, var6, var10, var11, var12, var15, var17);
      RoofWindow var20 = var8 == null ? var32 : var32.perBuilding(var8);
      MapRoofs.Placing var21 = new MapRoofs.Placing(var0, var20, var1, var2, var13, var14, var7);
      int var22 = var6.side();
      boolean[] var23 = new boolean[var22 * var22];

      for (int var24 = 0; var24 < var23.length; var24++) {
         int var25 = var20.square(var24);
         int var26 = var20.lenders[var24];
         boolean var27 = var10[var25] != null && var15[var25];
         boolean var28 = var26 >= 0;
         int var29 = var20.ownDraws(var24, var27, var28);
         int var30 = var20.lentDraws(var24, var27, var28);
         var21.dx = var6.originX() + var24 % var22 + 0.5F;
         var21.dy = var6.originZ() + var24 / var22 + 0.5F;
         float var31 = ((var29 | var30) & 2) != 0 ? var21.colour(var25, 1) + 1 : 0.0F;
         if (var29 != 0) {
            var21.place(var13[var25], var14[var25], var29, var31, 6);
         }

         if (var30 != 0) {
            var21.place(var13[var26], var14[var26], var30, var31, 6);
         }

         var21.gables(var25, var27 ? var25 : -1, var26);
         var23[var24] = (var29 | var30) != 0 || var16[var25];
      }

      return var23;
   }

   private static boolean square(
      int var0, MapRoofs.Level var1, MapRoofs.Level var2, MapRoofs.Level var3, RoofPiece[] var4, byte[] var5, byte[] var6, TextureID[] var7, float[][] var8
   ) {
      byte var9 = var1 == null ? 0 : var1.bits[var0];
      byte var10 = var3 == null ? 0 : var3.bits[var0];
      if (var1 != null && RoofLevels.carries(var2.pieces[var0], var9)) {
         return true;
      }

      boolean var11 = var3 != null && var2.pieces[var0] == null && RoofLevels.carries(var3.pieces[var0], var2.bits[var0]);
      MapRoofs.Level var12 = var11 ? var3 : var2;
      var4[var0] = var11 ? RoofLevels.lowered(var3.pieces[var0], 1) : var2.pieces[var0];
      var7[var0] = var12.pages[var0];
      var8[var0] = var12.maps[var0];
      var6[var0] = (byte)(var11 ? 1 : 0);
      byte var13 = var3 == null ? 0 : RoofLevels.lid(var3.flat[var0], var3.bits[var0], var3.overhead[var0]);
      var5[var0] = RoofLevels.flags(var9, var2.bits[var0], var10, var13, var11, false);
      return false;
   }

   private MapRoofs() {
   }

   public static final class Level {
      final RoofPiece[] pieces;
      final TextureID[] pages;
      final float[][] maps;
      final byte[] bits;
      final boolean[] flat;
      final boolean[] overhead;
      final TextureID[] gablePageN;
      final TextureID[] gablePageW;
      final float[][] gableMapN;
      final float[][] gableMapW;
      final int[] walls;

      public Level(int var1) {
         this.pieces = new RoofPiece[var1];
         this.pages = new TextureID[var1];
         this.maps = new float[var1][];
         this.bits = new byte[var1];
         this.flat = new boolean[var1];
         this.overhead = new boolean[var1];
         this.gablePageN = new TextureID[var1];
         this.gablePageW = new TextureID[var1];
         this.gableMapN = new float[var1][];
         this.gableMapW = new float[var1][];
         this.walls = new int[var1];
         Arrays.fill(this.walls, -1);
      }

      public void under(MapRoofs.Level var1) {
         for (int var2 = 0; var2 < this.overhead.length; var2++) {
            this.overhead[var2] = var1.overhead[var2] || (var1.bits[var2] & 3) != 0;
         }
      }

      public void tile(int var1, byte var2, boolean var3, RoofPiece var4, TextureID var5, float[] var6, int var7) {
         this.bits[var1] = (byte)(this.bits[var1] | var2);
         this.flat[var1] = this.flat[var1] | var3;
         this.walls[var1] = var7 >= 0 ? var7 : this.walls[var1];
         if ((var2 & 32) != 0) {
            this.gablePageN[var1] = var5;
            this.gableMapN[var1] = var6;
         }

         if ((var2 & 64) != 0) {
            this.gablePageW[var1] = var5;
            this.gableMapW[var1] = var6;
         }

         RoofPiece var8 = this.pieces[var1];
         if (var4 != null && (var8 == null || var4.over(var8))) {
            this.pieces[var1] = var4;
            this.pages[var1] = var5;
            this.maps[var1] = var6;
         }
      }

      public void room(int var1) {
         this.bits[var1] = (byte)(this.bits[var1] | 4);
      }
   }

   private static final class Placing {
      private static final int SIDES = 4;
      private static final int REACH = 16;
      private static final int[] DX = new int[]{0, 1, 0, -1};
      private static final int[] DZ = new int[]{1, 0, -1, 0};
      final Recipe recipe;
      final RoofWindow window;
      final MapRoofs.Level below;
      final MapRoofs.Level mine;
      final TextureID[] pages;
      final float[][] maps;
      final float base;
      final int[] lenders = new int[4];
      float dx;
      float dy;

      Placing(Recipe var1, RoofWindow var2, MapRoofs.Level var3, MapRoofs.Level var4, TextureID[] var5, float[][] var6, float var7) {
         this.recipe = var1;
         this.window = var2;
         this.below = var3;
         this.mine = var4;
         this.pages = var5;
         this.maps = var6;
         this.base = var7;
      }

      void place(TextureID var1, float[] var2, int var3, float var4, int var5) {
         Recipe.placeRoof(this.recipe, this.window, var1, var2, this.dx, this.dy, this.base, var3, var4, var5);
      }

      void gables(int var1, int var2, int var3) {
         int var4 = RoofGables.open(this.window, this.mine.bits, var1, this.lenders);
         int var5 = 0;
         byte var6 = 0;

         for (int var7 = 0; var7 < 4; var7++) {
            int var8 = this.lenders[var7];
            if ((var4 & ~var6 & 1 << var7) != 0) {
               if (var8 >= 0 && this.gableMap(var8, var7) != null) {
                  byte var9 = 0;

                  for (int var10 = var7; var10 < 4; var10++) {
                     int var11 = this.lenders[var10];
                     var9 |= (var4 & 1 << var10) != 0 && var11 >= 0 && this.gableMap(var11, var10) == this.gableMap(var8, var7) ? 1 << var10 : 0;
                  }

                  var6 |= var9;
                  this.place(this.gablePage(var8, var7), this.gableMap(var8, var7), var9, 0.0F, 7);
               } else {
                  var5 |= 1 << var7;
               }
            }
         }

         boolean var12 = var2 >= 0 && !this.window.pieces[var2].unseen();
         int var13 = var3 >= 0 ? var3 : (var12 ? var2 : -1);
         if (var5 != 0 && var13 >= 0 && this.maps[var13] != null) {
            int var14 = var5 | (var3 >= 0 ? 16 : 32);
            this.place(this.pages[var13], this.maps[var13], var14, this.colour(var1, var5) + 1, 7);
         }
      }

      private TextureID gablePage(int var1, int var2) {
         return RoofGables.across(var2) ? this.mine.gablePageN[var1] : this.mine.gablePageW[var1];
      }

      private float[] gableMap(int var1, int var2) {
         return RoofGables.across(var2) ? this.mine.gableMapN[var1] : this.mine.gableMapW[var1];
      }

      int colour(int var1, int var2) {
         if (this.below == null) {
            return 9210501;
         }

         int var3 = Integer.numberOfTrailingZeros(var2);
         int var4 = (var3 + 2) % 4;
         int var5 = var1 % this.window.width;
         int var6 = var1 / this.window.width;
         int var7 = var3 != 0 && var3 != 1 ? 0 : -1;

         for (int var8 = var7; var8 <= 16; var8++) {
            int var9 = var5 + DX[var4] * var8;
            int var10 = var6 + DZ[var4] * var8;
            if (var9 < 0 || var10 < 0 || var9 >= this.window.width || var10 >= this.window.height) {
               break;
            }

            int var11 = this.below.walls[var10 * this.window.width + var9];
            if (var11 >= 0) {
               return var11;
            }
         }

         return 9210501;
      }
   }
}
