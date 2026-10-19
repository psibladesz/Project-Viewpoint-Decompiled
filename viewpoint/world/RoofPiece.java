package viewpoint.world;

import java.util.Locale;

public final class RoofPiece {
   public static final byte SLOPE = 0;
   public static final byte HIP = 1;
   public static final byte VALLEY = 2;
   public static final byte EAVE = 3;
   public static final byte FLAT = 4;
   public static final byte TOP = 5;
   public static final byte PEAK = 6;
   public static final byte PLANE = 7;
   public static final byte S = 0;
   public static final byte E = 1;
   public static final byte N = 2;
   public static final byte W = 3;
   public static final byte NONE = -1;
   public static final int HOUSE_PITCH = 6;
   private static final String[] KINDS = new String[]{"slope", "hip", "valley", "eave", "flat", "top", "peak", "plane"};
   private static final String FACINGS = "SENW";
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};
   public final byte kind;
   public final byte facing;
   public final byte other;
   public final byte perStorey;
   public final byte step;
   public final float rise;
   public final float middle;

   public RoofPiece(int var1, int var2, int var3, int var4, int var5) {
      this(var1, var2, var3, var4, var5, 0.0F, 0.0F);
   }

   private RoofPiece(int var1, int var2, int var3, int var4, int var5, float var6, float var7) {
      this.kind = (byte)var1;
      this.facing = (byte)var2;
      this.other = (byte)var3;
      this.perStorey = (byte)var4;
      this.step = (byte)var5;
      this.rise = var6;
      this.middle = var7;
   }

   public static RoofPiece of(int var0) {
      return new RoofPiece(var0, -1, -1, 0, 0);
   }

   public static RoofPiece exact(int var0, float var1, float var2) {
      int var3 = Math.max(1, Math.min(127, Math.round(2.4494896F / var1)));
      return new RoofPiece(7, var0, -1, var3, 0, var1, var2);
   }

   public boolean planar() {
      return this.kind == 0 || this.kind == 1 || this.kind == 2 || this.kind == 5 || this.kind == 6 || this.kind == 7;
   }

   public boolean porch() {
      return this.planar() && (this.kind == 7 || this.perStorey > 6);
   }

   public boolean over(RoofPiece var1) {
      return this.porch() != var1.porch() ? var1.porch() : var1.kind == 0;
   }

   public boolean unseen() {
      return this.kind == 6 || this.facing == 2 || this.facing == 3 || this.other == 2 || this.other == 3;
   }

   public float height(float var1, float var2) {
      if (this.kind == 5) {
         return 2.4494896F / this.perStorey * this.step;
      }

      if (this.kind == 6) {
         return 2.4494896F / this.perStorey * (this.step - 0.5F - Math.max(Math.abs(var1), Math.abs(var2)));
      }

      if (this.kind == 7) {
         return this.middle - this.rise * (DX[this.facing] * var1 + DZ[this.facing] * var2);
      }

      float var3 = this.plane(this.facing, var1, var2);
      if (this.kind == 0) {
         return var3;
      }

      float var4 = this.plane(this.other, var1, var2);
      return this.kind == 1 ? Math.min(var3, var4) : Math.max(var3, var4);
   }

   public float plane(int var1, float var2, float var3) {
      return 2.4494896F / this.perStorey * (this.step - 0.5F - DX[var1] * var2 - DZ[var1] * var3);
   }

   public static RoofPiece parse(String[] var0, int var1) {
      int var2 = indexOf(KINDS, var0[var1]);
      if (var2 < 0) {
         throw new IllegalArgumentException("not a roof piece: " + var0[var1]);
      } else if (var2 == 3 || var2 == 4) {
         return of(var2);
      } else if (var2 == 7) {
         return exact(facing(var0[var1 + 1].charAt(0)), Float.parseFloat(var0[var1 + 2]), Float.parseFloat(var0[var1 + 3]));
      } else if (var2 != 5 && var2 != 6) {
         String var3 = var0[var1 + 1];
         return new RoofPiece(
            var2, facing(var3.charAt(0)), var3.length() > 1 ? facing(var3.charAt(1)) : -1, Integer.parseInt(var0[var1 + 2]), Integer.parseInt(var0[var1 + 3])
         );
      } else {
         return new RoofPiece(var2, -1, -1, Integer.parseInt(var0[var1 + 1]), Integer.parseInt(var0[var1 + 2]));
      }
   }

   static int facing(char var0) {
      int var1 = "SENW".indexOf(var0);
      if (var1 < 0) {
         throw new IllegalArgumentException("not a facing: " + var0);
      } else {
         return var1;
      }
   }

   private static int indexOf(String[] var0, String var1) {
      for (int var2 = 0; var2 < var0.length; var2++) {
         if (var0[var2].equals(var1)) {
            return var2;
         }
      }

      return -1;
   }

   @Override
   public String toString() {
      if (!this.planar()) {
         return KINDS[this.kind];
      }

      if (this.kind == 7) {
         return String.format(Locale.ROOT, "%s %c %.4f %.4f", KINDS[this.kind], "SENW".charAt(this.facing), this.rise, this.middle);
      }

      if (this.facing < 0) {
         return KINDS[this.kind] + " " + this.perStorey + " " + this.step;
      }

      String var1 = "SENW".charAt(this.facing) + (this.other >= 0 ? String.valueOf("SENW".charAt(this.other)) : "");
      return KINDS[this.kind] + " " + var1 + " " + this.perStorey + " " + this.step;
   }
}
