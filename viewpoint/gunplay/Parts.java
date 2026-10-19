package viewpoint.gunplay;

final class Parts {
   static final float MODEL_UNIT = 1.5F;
   static final String[] BONES = new String[]{
      "Bip01_Pelvis",
      "Bip01_Spine1",
      "Bip01_Neck",
      "Bip01_Head",
      "Bip01_L_UpperArm",
      "Bip01_L_Forearm",
      "Bip01_L_Hand",
      "Bip01_R_UpperArm",
      "Bip01_R_Forearm",
      "Bip01_R_Hand",
      "Bip01_L_Thigh",
      "Bip01_L_Calf",
      "Bip01_L_Foot",
      "Bip01_R_Thigh",
      "Bip01_R_Calf",
      "Bip01_R_Foot"
   };
   static final int PELVIS = 0;
   static final int SPINE1 = 1;
   static final int NECK = 2;
   static final int HEAD = 3;
   static final int L_UPPER_ARM = 4;
   static final int L_FOREARM = 5;
   static final int L_HAND = 6;
   static final int R_UPPER_ARM = 7;
   static final int R_FOREARM = 8;
   static final int R_HAND = 9;
   static final int L_THIGH = 10;
   static final int L_CALF = 11;
   static final int L_FOOT = 12;
   static final int R_THIGH = 13;
   static final int R_CALF = 14;
   static final int R_FOOT = 15;
   static final int PART_PELVIS = 0;
   static final int PART_SPINE = 1;
   static final int PART_HEAD = 2;
   static final int PART_LEFT_UPPER_LEG = 3;
   static final int PART_LEFT_LOWER_LEG = 4;
   static final int PART_RIGHT_UPPER_LEG = 5;
   static final int PART_RIGHT_LOWER_LEG = 6;
   static final int PART_LEFT_UPPER_ARM = 7;
   static final int PART_LEFT_LOWER_ARM = 8;
   static final int PART_RIGHT_UPPER_ARM = 9;
   static final int PART_RIGHT_LOWER_ARM = 10;
   static final int NO_PART = 11;
   private static final int ALONE = -1;
   private static final float[][] SPEC = new float[][]{
      {0.0F, 0.0F, 1.0F, 0.1F, 0.7F, 10.0F, 13.0F, 0.02F, 0.07F},
      {1.0F, 1.0F, 2.0F, 0.25F, 0.9F, 4.0F, 7.0F, 0.04F, 0.06F},
      {2.0F, 1.0F, 3.0F, 1.34F, 1.58F, -1.0F, -1.0F, 0.0F, 0.06F},
      {3.0F, 10.0F, 11.0F, 0.39F, 0.78F, -1.0F, -1.0F, 0.0F, 0.0525F},
      {4.0F, 11.0F, 12.0F, 0.25F, 1.11F, -1.0F, -1.0F, 0.0F, 0.052F},
      {5.0F, 13.0F, 14.0F, 0.39F, 0.78F, -1.0F, -1.0F, 0.0F, 0.0525F},
      {6.0F, 14.0F, 15.0F, 0.25F, 1.11F, -1.0F, -1.0F, 0.0F, 0.052F},
      {7.0F, 4.0F, 5.0F, 0.43F, 0.65F, -1.0F, -1.0F, 0.0F, 0.044F},
      {8.0F, 5.0F, 6.0F, 0.45F, 1.63F, -1.0F, -1.0F, 0.0F, 0.04F},
      {9.0F, 7.0F, 8.0F, 0.43F, 0.65F, -1.0F, -1.0F, 0.0F, 0.044F},
      {10.0F, 8.0F, 9.0F, 0.45F, 1.63F, -1.0F, -1.0F, 0.0F, 0.04F}
   };
   static final int CAPSULES = 13;
   static final int STRIDE = 8;
   private static final double AXIAL = 1.0E-9;

   static void capsules(float[] var0, float[] var1) {
      int var2 = 0;

      for (float[] var6 : SPEC) {
         int var7 = (int)var6[1] * 3;
         int var8 = (int)var6[2] * 3;
         float var9 = var0[var8] - var0[var7];
         float var10 = var0[var8 + 1] - var0[var7 + 1];
         float var11 = var0[var8 + 2] - var0[var7 + 2];
         float var12 = var0[var7] + var9 * var6[3];
         float var13 = var0[var7 + 1] + var10 * var6[3];
         float var14 = var0[var7 + 2] + var11 * var6[3];
         float var15 = var0[var7] + var9 * var6[4];
         float var16 = var0[var7 + 1] + var10 * var6[4];
         float var17 = var0[var7 + 2] + var11 * var6[4];
         float var18 = var6[8] * 1.5F;
         if (var6[5] == -1.0F) {
            var2 = put(var1, var2, var6[0], var12, var13, var14, var15, var16, var17, var18);
         } else {
            int var19 = (int)var6[5] * 3;
            int var20 = (int)var6[6] * 3;
            float var21 = var0[var19] - var0[var20];
            float var22 = var0[var19 + 1] - var0[var20 + 1];
            float var23 = var0[var19 + 2] - var0[var20 + 2];
            float var24 = var9 * var9 + var10 * var10 + var11 * var11;
            float var25 = var24 > 0.0F ? (var21 * var9 + var22 * var10 + var23 * var11) / var24 : 0.0F;
            var21 -= var25 * var9;
            var22 -= var25 * var10;
            var23 -= var25 * var11;
            float var26 = (float)Math.sqrt(var21 * var21 + var22 * var22 + var23 * var23);
            float var27 = var26 > 0.0F ? var6[7] * 1.5F / var26 : 0.0F;
            var21 *= var27;
            var22 *= var27;
            var23 *= var27;
            var2 = put(var1, var2, var6[0], var12 + var21, var13 + var22, var14 + var23, var15 + var21, var16 + var22, var17 + var23, var18);
            var2 = put(var1, var2, var6[0], var12 - var21, var13 - var22, var14 - var23, var15 - var21, var16 - var22, var17 - var23, var18);
         }
      }
   }

   static double meet(float[] var0, double var1, double var3, double var5, double var7, double var9, double var11, double var13, int[] var15) {
      double var16 = Double.POSITIVE_INFINITY;
      var15[0] = 11;

      for (int var18 = 0; var18 < 104; var18 += 8) {
         double var19 = entry(var0, var18, var1, var3, var5, var7, var9, var11);
         if (var19 >= var13 && var19 < var16) {
            var16 = var19;
            var15[0] = (int)var0[var18];
         }
      }

      return var16;
   }

   private static double entry(float[] var0, int var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      double var14 = var0[var1 + 1];
      double var16 = var0[var1 + 2];
      double var18 = var0[var1 + 3];
      double var20 = var0[var1 + 7];
      double var22 = var0[var1 + 4] - var14;
      double var24 = var0[var1 + 5] - var16;
      double var26 = var0[var1 + 6] - var18;
      double var28 = var2 - var14;
      double var30 = var4 - var16;
      double var32 = var6 - var18;
      double var34 = var22 * var22 + var24 * var24 + var26 * var26;
      double var36 = var22 * var8 + var24 * var10 + var26 * var12;
      double var38 = var22 * var28 + var24 * var30 + var26 * var32;
      double var40 = var34 - var36 * var36;
      if (var40 <= 1.0E-9 * var34) {
         return Math.min(sphere(var28, var30, var32, var20, var8, var10, var12), sphere(var28 - var22, var30 - var24, var32 - var26, var20, var8, var10, var12));
      } else {
         double var42 = var8 * var28 + var10 * var30 + var12 * var32;
         double var44 = var28 * var28 + var30 * var30 + var32 * var32;
         double var46 = var34 * var42 - var38 * var36;
         double var48 = var46 * var46 - var40 * (var34 * var44 - var38 * var38 - var20 * var20 * var34);
         if (var48 < 0.0) {
            return Double.POSITIVE_INFINITY;
         } else {
            double var50 = (-var46 - Math.sqrt(var48)) / var40;
            double var52 = var38 + var50 * var36;
            if (var52 > 0.0 && var52 < var34) {
               return var50;
            } else {
               return var52 <= 0.0
                  ? sphere(var28, var30, var32, var20, var8, var10, var12)
                  : sphere(var28 - var22, var30 - var24, var32 - var26, var20, var8, var10, var12);
            }
         }
      }
   }

   private static double sphere(double var0, double var2, double var4, double var6, double var8, double var10, double var12) {
      double var14 = var8 * var0 + var10 * var2 + var12 * var4;
      double var16 = var14 * var14 - (var0 * var0 + var2 * var2 + var4 * var4 - var6 * var6);
      return var16 < 0.0 ? Double.POSITIVE_INFINITY : -var14 - Math.sqrt(var16);
   }

   private static int put(float[] var0, int var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
      var0[var1] = var2;
      var0[var1 + 1] = var3;
      var0[var1 + 2] = var4;
      var0[var1 + 3] = var5;
      var0[var1 + 4] = var6;
      var0[var1 + 5] = var7;
      var0[var1 + 6] = var8;
      var0[var1 + 7] = var9;
      return var1 + 8;
   }

   private Parts() {
   }
}
