package viewpoint.packs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

final class ObjReader {
   private static final int KEY_BITS = 21;
   private static final float CORNER_SLACK = 0.02F;
   private static final float CORNER_FILL = 0.7F;

   static ObjReader.Mesh parse(List<String> var0) {
      ObjReader.Floats var1 = new ObjReader.Floats();
      ObjReader.Floats var2 = new ObjReader.Floats();
      ObjReader.Floats var3 = new ObjReader.Floats();
      ObjReader.Floats var4 = new ObjReader.Floats();
      int[] var5 = new int[64];
      int var6 = 0;
      HashMap var7 = new HashMap();
      String var8 = null;

      for (int var9 = 0; var9 < var0.size(); var9++) {
         String var10 = ((String)var0.get(var9)).strip();
         if (var10.startsWith("v ")) {
            var1.add(numbers(var10, 3));
         } else if (var10.startsWith("vt ")) {
            var2.add(numbers(var10, 2));
         } else if (var10.startsWith("vn ")) {
            var3.add(numbers(var10, 3));
         } else if (var10.startsWith("mtllib ")) {
            var8 = var10.substring(7).strip();
         } else if (var10.startsWith("f ")) {
            int[] var11 = face(var10, var9, var1, var2, var3, var4, var7);
            if (var6 + (var11.length - 2) * 3 > var5.length) {
               var5 = Arrays.copyOf(var5, Math.max(var5.length * 2, var6 + (var11.length - 2) * 3));
            }

            for (int var12 = 1; var12 + 1 < var11.length; var12++) {
               var5[var6++] = var11[0];
               var5[var6++] = var11[var12 + 1];
               var5[var6++] = var11[var12];
            }
         }
      }

      float[] var15 = var4.toArray();
      centreCornerLaid(var15);
      return new ObjReader.Mesh(var15, Arrays.copyOf(var5, var6), var8);
   }

   static void centreCornerLaid(float[] var0) {
      float var1 = Float.MAX_VALUE;
      float var2 = -Float.MAX_VALUE;
      float var3 = Float.MAX_VALUE;
      float var4 = -Float.MAX_VALUE;

      for (int var5 = 0; var5 < var0.length; var5 += 8) {
         var1 = Math.min(var1, var0[var5]);
         var2 = Math.max(var2, var0[var5]);
         var3 = Math.min(var3, var0[var5 + 2]);
         var4 = Math.max(var4, var0[var5 + 2]);
      }

      boolean var7 = var2 <= 0.02F && var4 <= 0.02F && var1 >= -1.02F && var3 >= -1.02F && var2 - var1 > 0.7F && var4 - var3 > 0.7F;

      for (int var6 = 0; var7 && var6 < var0.length; var6 += 8) {
         var0[var6] += 0.5F;
         var0[var6 + 2] = var0[var6 + 2] + 0.5F;
      }
   }

   static String texture(List<String> var0) {
      for (String var2 : var0) {
         String var3 = var2.strip();
         if (var3.startsWith("map_Kd ")) {
            return var3.substring(7).strip();
         }
      }

      return null;
   }

   private static int[] face(
      String var0, int var1, ObjReader.Floats var2, ObjReader.Floats var3, ObjReader.Floats var4, ObjReader.Floats var5, HashMap<Long, Integer> var6
   ) {
      String[] var7 = var0.substring(2).strip().split("\\s+");
      if (var7.length < 3) {
         throw new IllegalArgumentException("line " + (var1 + 1) + ": a face of fewer than three corners");
      }

      int[] var8 = new int[var7.length];
      int[] var9 = new int[var7.length];
      int[] var10 = new int[var7.length];

      for (int var11 = 0; var11 < var7.length; var11++) {
         String[] var12 = var7[var11].split("/", -1);
         var8[var11] = index(var12[0], var2.size() / 3, var1);
         var9[var11] = var12.length > 1 && !var12[1].isEmpty() ? index(var12[1], var3.size() / 2, var1) : -1;
         var10[var11] = var12.length > 2 && !var12[2].isEmpty() ? index(var12[2], var4.size() / 3, var1) : -1;
      }

      float[] var17 = null;

      for (int var18 = 0; var18 < var7.length && var17 == null; var18++) {
         var17 = var10[var18] < 0 ? flatNormal(var2, var8) : null;
      }

      int[] var19 = new int[var7.length];

      for (int var13 = 0; var13 < var7.length; var13++) {
         long var14 = var17 != null ? -1L : ((long)var8[var13] << 21 | var9[var13] + 1L) << 21 | var10[var13] + 1L;
         Integer var16 = var14 < 0L ? null : (Integer)var6.get(var14);
         if (var16 == null) {
            var16 = var5.size() / 8;
            corner(var2, var3, var4, var5, var8[var13], var9[var13], var10[var13], var17);
            if (var14 >= 0L) {
               var6.put(var14, var16);
            }
         }

         var19[var13] = var16;
      }

      return var19;
   }

   private static void corner(
      ObjReader.Floats var0, ObjReader.Floats var1, ObjReader.Floats var2, ObjReader.Floats var3, int var4, int var5, int var6, float[] var7
   ) {
      float[] var8 = var0.data;
      float[] var9 = var1.data;
      float[] var10 = var2.data;
      float var11 = var7 != null ? var7[0] : var10[var6 * 3];
      float var12 = var7 != null ? var7[1] : var10[var6 * 3 + 1];
      float var13 = var7 != null ? var7[2] : var10[var6 * 3 + 2];
      var3.add(-var8[var4 * 3], var8[var4 * 3 + 2], -var8[var4 * 3 + 1]);
      var3.add(-var11, var13, -var12);
      var3.add(var5 < 0 ? 0.0F : var9[var5 * 2], var5 < 0 ? 0.0F : 1.0F - var9[var5 * 2 + 1]);
   }

   private static float[] flatNormal(ObjReader.Floats var0, int[] var1) {
      float[] var2 = var0.data;
      float var3 = var2[var1[1] * 3] - var2[var1[0] * 3];
      float var4 = var2[var1[1] * 3 + 1] - var2[var1[0] * 3 + 1];
      float var5 = var2[var1[1] * 3 + 2] - var2[var1[0] * 3 + 2];
      float var6 = var2[var1[2] * 3] - var2[var1[0] * 3];
      float var7 = var2[var1[2] * 3 + 1] - var2[var1[0] * 3 + 1];
      float var8 = var2[var1[2] * 3 + 2] - var2[var1[0] * 3 + 2];
      float var9 = var4 * var8 - var5 * var7;
      float var10 = var5 * var6 - var3 * var8;
      float var11 = var3 * var7 - var4 * var6;
      float var12 = (float)Math.sqrt(var9 * var9 + var10 * var10 + var11 * var11);
      return var12 > 0.0F ? new float[]{var9 / var12, var10 / var12, var11 / var12} : new float[]{0.0F, 0.0F, 1.0F};
   }

   private static int index(String var0, int var1, int var2) {
      int var3;
      try {
         var3 = Integer.parseInt(var0);
      } catch (NumberFormatException var5) {
         throw new IllegalArgumentException("line " + (var2 + 1) + ": not an index: " + var0);
      }

      int var4 = var3 < 0 ? var1 + var3 : var3 - 1;
      if (var4 >= 0 && var4 < var1) {
         return var4;
      } else {
         throw new IllegalArgumentException("line " + (var2 + 1) + ": no element " + var0);
      }
   }

   private static float[] numbers(String var0, int var1) {
      String[] var2 = var0.split("\\s+");
      float[] var3 = new float[var1];

      for (int var4 = 0; var4 < var1 && var4 + 1 < var2.length; var4++) {
         var3[var4] = Float.parseFloat(var2[var4 + 1]);
      }

      return var3;
   }

   private static final class Floats {
      float[] data = new float[256];
      int size;

      void add(float... var1) {
         if (this.size + var1.length > this.data.length) {
            this.data = Arrays.copyOf(this.data, Math.max(this.data.length * 2, this.size + var1.length));
         }

         System.arraycopy(var1, 0, this.data, this.size, var1.length);
         this.size += var1.length;
      }

      int size() {
         return this.size;
      }

      float[] toArray() {
         return Arrays.copyOf(this.data, this.size);
      }
   }

   record Mesh(float[] vertices, int[] indices, String mtllib) {
   }
}
