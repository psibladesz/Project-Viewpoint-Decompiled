package viewpoint.far;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Tuning;
import viewpoint.render.FarRelief;

final class ReliefPyramid {
   static final LiveSettings.Toggle SHOWN = LiveSettings.toggle("relief.onMap", "Relief on the map", "Debug/Relief", false);
   static final int ALPHA = 170;
   private static final int TILE = 256;
   private static final int LEVELS = 5;
   private static final float CONTOUR = 5.0F;
   private static final float RELIEF_SHADE = 4.0F;
   private static final float WET = 0.05F;
   private static final float[] WATER = new float[]{0.1F, 0.3F, 0.65F};
   private static final String PREFIX = "relief-";
   private static final String SUFFIX = ".pyramid.zip";
   private static ReliefData asked;
   private static int count;
   private static volatile ReliefPyramid.Built built;

   private ReliefPyramid() {
   }

   static File file() {
      ReliefData var0 = Relief.data();
      ReliefPyramid.Built var1 = built;
      if (!SHOWN.get() || var0 == null) {
         return null;
      }

      if (var1 != null && var1.relief == var0) {
         return var1.file;
      }

      if (asked != var0) {
         asked = var0;
         File var2 = Relief.kept();
         String var3 = "relief-" + ProcessHandle.current().pid() + "-";
         File var4 = new File(var2, var3 + ++count + ".pyramid.zip");
         Relief.work(() -> {
            forget(var2, var3);
            long var4x = System.nanoTime();
            write(var0.grid, var4);
            built = new ReliefPyramid.Built(var0, var4);
            System.out.printf("[Viewpoint] far world: the relief on the map built in %.1f s (%s)%n", (System.nanoTime() - var4x) / 1.0E9, var4.getName());
         });
      }

      return null;
   }

   private static void forget(File var0, String var1) {
      File[] var2 = var0.listFiles((var0x, var1x) -> var1x.startsWith("relief-") && var1x.endsWith(".pyramid.zip"));

      for (File var6 : var2 == null ? new File[0] : var2) {
         if (!var6.getName().startsWith(var1) && !var6.delete()) {
            System.out.println("[Viewpoint] far world: could not delete " + var6 + " (another game has it open?)");
         }
      }
   }

   static void write(FarRelief.Field var0, File var1) throws IOException {
      int var2 = (var0.width() + 15) / 16 * 16;
      int var3 = (var0.height() + 15) / 16 * 16;
      byte var4 = 4;
      var1.getParentFile().mkdirs();

      try (ZipOutputStream var5 = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(var1)))) {
         var5.putNextEntry(new ZipEntry("pyramid.txt"));
         var5.write(
            String.format("VERSION=1%nbounds=0 0 %d %d%nimageSize=%d %d%nminFilter=linear%nmagFilter=linear%n", var2 * var4, var3 * var4, var2, var3)
               .getBytes(StandardCharsets.UTF_8)
         );
         var5.closeEntry();
         ImageWriter var6 = ImageIO.getImageWritersByFormatName("png").next();
         BufferedImage var7 = new BufferedImage(256, 256, 2);
         int[] var8 = new int[65536];

         for (int var9 = 0; var9 < 5; var9++) {
            int var10 = ((var2 >> var9) + 256 - 1) / 256;
            int var11 = ((var3 >> var9) + 256 - 1) / 256;

            for (int var12 = 0; var12 < var11; var12++) {
               for (int var13 = 0; var13 < var10; var13++) {
                  tile(var0, var9, var13, var12, var8);
                  var7.setRGB(0, 0, 256, 256, var8, 0, 256);
                  var5.putNextEntry(new ZipEntry(var9 + "/tile" + var13 + "x" + var12 + ".png"));
                  png(var6, var7, var5);
                  var5.closeEntry();
               }
            }
         }

         var6.dispose();
      }
   }

   static void tile(FarRelief.Field var0, int var1, int var2, int var3, int[] var4) {
      int var5 = 1 << var1;

      for (int var6 = 0; var6 < 256; var6++) {
         for (int var7 = 0; var7 < 256; var7++) {
            int var8 = var2 * 256 + var7;
            int var9 = var3 * 256 + var6;
            boolean var10 = var8 * var5 < var0.width() && var9 * var5 < var0.height();
            var4[var6 * 256 + var7] = var10 ? colour(var0, (var8 + 0.5) * var5, (var9 + 0.5) * var5, var5) : 0;
         }
      }
   }

   static int colour(FarRelief.Field var0, double var1, double var3, int var5) {
      double var6 = var5 * 0.5;
      float var8 = height(var0, var1, var3);
      float var9 = height(var0, var1 - var6, var3);
      float var10 = height(var0, var1 + var6, var3);
      float var11 = height(var0, var1, var3 - var6);
      float var12 = height(var0, var1, var3 + var6);
      float var13 = var5 * 4 / 4.0F;
      float var14 = shade((var10 - var9) / var13, (var12 - var11) / var13);
      float var15 = Math.min(Math.min(var9, var10), Math.min(var11, var12));
      float var16 = Math.max(Math.max(var9, var10), Math.max(var11, var12));
      float var17 = 5.0F * var5;
      boolean var18 = Math.floor(var15 / var17) != Math.floor(var16 / var17);
      float[] var19 = hue(var8 / Tuning.reliefViewCycle);
      boolean var20 = water(var0, var1, var3) > var8 + 0.05F;

      for (int var21 = 0; var21 < 3; var21++) {
         var19[var21] = var20 ? 0.25F * var19[var21] + WATER[var21] : var19[var21] * var14 * (var18 ? 0.45F : 1.0F);
      }

      return 0xFF000000 | channel(var19[0]) << 16 | channel(var19[1]) << 8 | channel(var19[2]);
   }

   private static float shade(float var0, float var1) {
      float var2 = -0.5F;
      float var3 = -0.5F;
      float var4 = 0.7071F;
      float var5 = (-var0 * var2 - var1 * var3 + var4) / (float)Math.sqrt(var0 * var0 + var1 * var1 + 1.0F);
      return Math.max(0.35F, Math.min(1.3F, var5 / var4));
   }

   static float[] hue(float var0) {
      float[] var1 = new float[3];
      float[] var2 = new float[]{0.0F, 0.6666667F, 0.33333334F};

      for (int var3 = 0; var3 < 3; var3++) {
         float var4 = var0 + var2[var3];
         var4 -= (float)Math.floor(var4);
         var1[var3] = Math.max(0.0F, Math.min(1.0F, Math.abs(var4 * 6.0F - 3.0F) - 1.0F));
      }

      return var1;
   }

   private static float height(FarRelief.Field var0, double var1, double var3) {
      return bilinear(var0.ground(), var0.width(), var0.height(), var1, var3);
   }

   private static float water(FarRelief.Field var0, double var1, double var3) {
      double var5 = 1.0;
      int var7 = FarRelief.Field.waterNodes(var0.width());
      int var8 = FarRelief.Field.waterNodes(var0.height());
      return bilinear(var0.water(), var7, var8, var1 / var5, var3 / var5);
   }

   private static float bilinear(short[] var0, int var1, int var2, double var3, double var5) {
      int var7 = (int)Math.floor(var3);
      int var8 = (int)Math.floor(var5);
      float var9 = (float)(var3 - var7);
      float var10 = (float)(var5 - var8);
      float var11 = at(var0, var1, var2, var7, var8) * (1.0F - var9) + at(var0, var1, var2, var7 + 1, var8) * var9;
      float var12 = at(var0, var1, var2, var7, var8 + 1) * (1.0F - var9) + at(var0, var1, var2, var7 + 1, var8 + 1) * var9;
      return var11 * (1.0F - var10) + var12 * var10;
   }

   private static float at(short[] var0, int var1, int var2, int var3, int var4) {
      int var5 = Math.max(0, Math.min(var1 - 1, var3));
      int var6 = Math.max(0, Math.min(var2 - 1, var4));
      return (var0[var6 * var1 + var5] & 65535) / 100.0F;
   }

   private static int channel(float var0) {
      return Math.max(0, Math.min(255, Math.round(var0 * 255.0F)));
   }

   private static void png(ImageWriter var0, BufferedImage var1, OutputStream var2) throws IOException {
      try (MemoryCacheImageOutputStream var3 = new MemoryCacheImageOutputStream(var2)) {
         var0.setOutput(var3);
         var0.write(var1);
      }
   }

   private record Built(ReliefData relief, File file) {
   }
}
