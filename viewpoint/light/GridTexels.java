package viewpoint.light;

import java.nio.ByteBuffer;

public final class GridTexels {
   private static final int[] COL = new int[]{0, 1, 1, 0};
   private static final int[] ROW = new int[]{0, 0, 1, 1};

   static void light(ByteBuffer var0, int var1, int var2, int[] var3, boolean var4) {
      for (int var5 = 0; var5 < 4; var5++) {
         put(var0, var1 * 2 + COL[var5], var2 * 2 + ROW[var5], var3[var5], var4 ? 255 : 0);
      }
   }

   static void sky(ByteBuffer var0, int var1, int var2, int[] var3) {
      for (int var4 = 0; var4 < 4; var4++) {
         put(var0, var1 * 2 + COL[var4], var2 * 2 + ROW[var4] + 20, var3[var4], 0);
      }
   }

   static void noSky(ByteBuffer var0, int var1, int var2) {
      for (int var3 = 0; var3 < 4; var3++) {
         put(var0, var1 * 2 + COL[var3], var2 * 2 + ROW[var3] + 20, 255, 0);
      }
   }

   public static void uniform(ByteBuffer var0, float var1, float var2, float var3, boolean var4) {
      int var5 = pack(var1, var2, var3);
      int[] var6 = new int[]{var5, var5, var5, var5};

      for (int var7 = 0; var7 < 10; var7++) {
         for (int var8 = 0; var8 < 10; var8++) {
            light(var0, var8, var7, var6, var4);
            noSky(var0, var8, var7);
         }
      }
   }

   public static void flatSquare(ByteBuffer var0, int var1, int var2, float var3, float var4, float var5, boolean var6, int var7) {
      int var8 = pack(var3, var4, var5);
      light(var0, var1, var2, new int[]{var8, var8, var8, var8}, var6);
      sky(var0, var1, var2, new int[]{var7, var7, var7, var7});
   }

   public static void openSky(ByteBuffer var0) {
      int[] var1 = new int[]{0, 0, 0, 0};

      for (int var2 = 0; var2 < 10; var2++) {
         for (int var3 = 0; var3 < 10; var3++) {
            sky(var0, var3, var2, var1);
         }
      }
   }

   static int pack(float var0, float var1, float var2) {
      return clamp(var0) | clamp(var1) << 8 | clamp(var2) << 16;
   }

   private static int clamp(float var0) {
      return Math.max(0, Math.min(255, Math.round(var0 * 255.0F)));
   }

   private static void put(ByteBuffer var0, int var1, int var2, int var3, int var4) {
      int var5 = (var2 * 20 + var1) * 4;
      var0.put(var5, (byte)(var3 & 0xFF));
      var0.put(var5 + 1, (byte)(var3 >> 8 & 0xFF));
      var0.put(var5 + 2, (byte)(var3 >> 16 & 0xFF));
      var0.put(var5 + 3, (byte)var4);
   }

   private GridTexels() {
   }
}
