package viewpoint.packs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import zombie.core.textures.PNGDecoder;
import zombie.core.textures.PNGDecoder.Format;

final class ArtReader {
   static ArtReader.Art read(File var0) throws IOException {
      PNGDecoder var1 = new PNGDecoder(new ByteArrayInputStream(Files.readAllBytes(var0.toPath())), false);
      int var2 = var1.getWidth();
      int var3 = var1.getHeight();
      int var4 = padded(var2);
      int var5 = padded(var3);
      byte[] var6 = new byte[var4 * var5 * 4];
      var1.decode(ByteBuffer.wrap(var6), var4 * 4, var3, Format.RGBA, 1229209940);
      return levels(var6, var2, var3);
   }

   static ArtReader.Art levels(int[] var0, int var1, int var2) {
      int var3 = padded(var1);
      byte[] var4 = new byte[var3 * padded(var2) * 4];

      for (int var5 = 0; var5 < var2; var5++) {
         for (int var6 = 0; var6 < var1; var6++) {
            int var7 = var0[var5 * var1 + var6];
            int var8 = (var5 * var3 + var6) * 4;
            var4[var8] = (byte)(var7 >> 16);
            var4[var8 + 1] = (byte)(var7 >> 8);
            var4[var8 + 2] = (byte)var7;
            var4[var8 + 3] = (byte)(var7 >>> 24);
         }
      }

      return levels(var4, var1, var2);
   }

   private static ArtReader.Art levels(byte[] var0, int var1, int var2) {
      int var3 = padded(var1);
      int var4 = padded(var2);

      for (int var5 = 0; var5 < var0.length; var5 += 4) {
         if (var0[var5 + 3] == 0) {
            var0[var5] = var0[var5 + 1] = var0[var5 + 2] = 0;
         }
      }

      byte[][] var7 = new byte[4][];
      var7[0] = var0;

      for (int var6 = 1; var6 < 4; var6++) {
         var7[var6] = half(var7[var6 - 1], var3 >> var6 - 1, var4 >> var6 - 1);
      }

      return new ArtReader.Art(var1, var2, var3, var4, var7);
   }

   private static byte[] half(byte[] var0, int var1, int var2) {
      int var3 = var1 / 2;
      int var4 = var2 / 2;
      byte[] var5 = new byte[var3 * var4 * 4];

      for (int var6 = 0; var6 < var4; var6++) {
         for (int var7 = 0; var7 < var3; var7++) {
            for (int var8 = 0; var8 < 4; var8++) {
               int var9 = (var6 * 2 * var1 + var7 * 2) * 4 + var8;
               int var10 = var9 + var1 * 4;
               int var11 = (var0[var9] & 255) + (var0[var9 + 4] & 255) + (var0[var10] & 255) + (var0[var10 + 4] & 255);
               var5[(var6 * var3 + var7) * 4 + var8] = (byte)((var11 + 2) / 4);
            }
         }
      }

      return var5;
   }

   private static int padded(int var0) {
      return (Math.max(var0, 1) + 8 - 1) / 8 * 8;
   }

   record Art(int width, int height, int paddedWidth, int paddedHeight, byte[][] levels) {
   }
}
