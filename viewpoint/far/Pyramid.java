package viewpoint.far;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

final class Pyramid {
   private static final ConcurrentHashMap<String, Pyramid> pyramids = new ConcurrentHashMap<>();
   private ZipFile zip;
   private int boundsX;
   private int boundsY;

   static Pyramid of(String var0) {
      return pyramids.computeIfAbsent(var0, Pyramid::new);
   }

   private Pyramid(String var1) {
      try {
         File var2 = new File(var1, "pyramid.zip");
         if (!var2.isFile()) {
            return;
         }

         this.zip = new ZipFile(var2);
         ZipEntry var3 = this.zip.getEntry("pyramid.txt");
         if (var3 != null) {
            try (InputStream var4 = this.zip.getInputStream(var3)) {
               for (String var8 : new String(var4.readAllBytes()).split("\\R")) {
                  if (var8.startsWith("bounds=")) {
                     String[] var9 = var8.substring(7).trim().split("\\s+");
                     this.boundsX = Integer.parseInt(var9[0]);
                     this.boundsY = Integer.parseInt(var9[1]);
                  }
               }
            }
         }
      } catch (Exception var12) {
         System.out.println("[Viewpoint] far world: no map image in " + var1 + ": " + var12);
         this.zip = null;
      }
   }

   int[] image(int var1, int var2) {
      int var3 = var1 * 256 - this.boundsX;
      int var4 = var2 * 256 - this.boundsY;
      if (this.zip != null && var3 >= 0 && var4 >= 0 && var3 % 256 == 0 && var4 % 256 == 0) {
         ZipEntry var5 = this.zip.getEntry("0/tile" + var3 / 256 + "x" + var4 / 256 + ".png");
         if (var5 == null) {
            return null;
         }

         try (InputStream var6 = this.zip.getInputStream(var5)) {
            BufferedImage var7 = ImageIO.read(var6);
            return var7 != null && var7.getWidth() == 256 && var7.getHeight() == 256 ? var7.getRGB(0, 0, 256, 256, null, 0, 256) : null;
         } catch (Exception var11) {
            return null;
         }
      } else {
         return null;
      }
   }

   int[] level(int var1, int var2, int var3) throws IOException {
      if (this.zip != null && this.boundsX == 0 && this.boundsY == 0) {
         int[] var4 = new int[var2 * var3];
         short var5 = 256;

         for (int var6 = 0; var6 * var5 < var3; var6++) {
            for (int var7 = 0; var7 * var5 < var2; var7++) {
               ZipEntry var8 = this.zip.getEntry(var1 + "/tile" + var7 + "x" + var6 + ".png");
               if (var8 != null) {
                  try (InputStream var9 = this.zip.getInputStream(var8)) {
                     BufferedImage var10 = ImageIO.read(var9);
                     int var11 = Math.min(var10.getWidth(), var2 - var7 * var5);
                     int var12 = Math.min(var10.getHeight(), var3 - var6 * var5);
                     var10.getRGB(0, 0, var11, var12, var4, var6 * var5 * var2 + var7 * var5, var2);
                  }
               }
            }
         }

         for (int var15 = 0; var15 < var4.length; var15++) {
            var4[var15] &= 16777215;
         }

         return var4;
      } else {
         return null;
      }
   }
}
