package viewpoint.far;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import javax.imageio.ImageIO;
import viewpoint.render.FarRelief;

final class ReliefData {
   static final String GRID = "/viewpoint/relief.png";
   static final int BASE_SPACING = 8;
   static final float METRES_A_STEP = 0.1F;
   static final int SMOOTHING_SQUARES = 32;
   static final String REBAKE = "rebake.bin";
   final FarRelief.Field grid;
   final ReliefRules rules;
   final ReliefData.Counts counts;
   final boolean asBuilt;
   private final float[] cellTops;
   private final float[] cellLows;
   private final float[] blockErrors;
   private final int cellsX;
   private final int cellsY;
   private final int blocksX;
   private final int blocksY;

   private ReliefData(FarRelief.Field var1, ReliefRules var2, ReliefData.Counts var3, boolean var4) {
      this.grid = var1;
      this.rules = var2;
      this.counts = var3;
      this.asBuilt = var4;
      byte var5 = 64;
      this.cellsX = (var1.width() - 1 + var5 - 1) / var5;
      this.cellsY = (var1.height() - 1 + var5 - 1) / var5;
      this.cellTops = new float[this.cellsX * this.cellsY];
      this.cellLows = new float[this.cellsX * this.cellsY];

      for (int var6 = 0; var6 < this.cellsY; var6++) {
         for (int var7 = 0; var7 < this.cellsX; var7++) {
            float var8 = -Float.MAX_VALUE;
            float var9 = Float.MAX_VALUE;

            for (int var10 = var6 * var5; var10 <= Math.min(var1.height() - 1, (var6 + 1) * var5); var10++) {
               for (int var11 = var7 * var5; var11 <= Math.min(var1.width() - 1, (var7 + 1) * var5); var11++) {
                  var8 = Math.max(var8, var1.metres(var10 * var1.width() + var11));
                  var9 = Math.min(var9, var1.metres(var10 * var1.width() + var11));
               }
            }

            this.cellTops[var6 * this.cellsX + var7] = var8;
            this.cellLows[var6 * this.cellsX + var7] = var9;
         }
      }

      byte var12 = 16;
      this.blocksX = (var1.width() - 1 + var12 - 1) / var12;
      this.blocksY = (var1.height() - 1 + var12 - 1) / var12;
      this.blockErrors = errors(var1, var12, this.blocksX, this.blocksY, Math.max(1, FarRelief.SPLIT / 4));
   }

   static float[] errors(FarRelief.Field var0, int var1, int var2, int var3, int var4) {
      int var5 = var0.width();
      int var6 = var0.height();
      float[] var7 = new float[var2 * var3];

      for (int var8 = 0; var4 > 1 && var8 < var6; var8++) {
         for (int var9 = 0; var9 < var5; var9++) {
            int var10 = var9 / var4 * var4;
            int var11 = var8 / var4 * var4;
            int var12 = Math.min(var5 - 1, var10 + var4);
            int var13 = Math.min(var6 - 1, var11 + var4);
            float var14 = (float)(var9 - var10) / var4;
            float var15 = (float)(var8 - var11) / var4;
            float var16 = var0.metres(var11 * var5 + var10) * (1.0F - var14) + var0.metres(var11 * var5 + var12) * var14;
            float var17 = var0.metres(var13 * var5 + var10) * (1.0F - var14) + var0.metres(var13 * var5 + var12) * var14;
            float var18 = Math.abs(var0.metres(var8 * var5 + var9) - (var16 * (1.0F - var15) + var17 * var15));
            int var19 = Math.min(var2 - 1, var9 / var1);
            int var20 = Math.min(var3 - 1, var8 / var1);
            var7[var20 * var2 + var19] = Math.max(var7[var20 * var2 + var19], var18);
         }
      }

      return var7;
   }

   float blockError(int var1, int var2) {
      return var1 >= 0 && var2 >= 0 && var1 < this.blocksX && var2 < this.blocksY ? this.blockErrors[var2 * this.blocksX + var1] : 0.0F;
   }

   static ReliefData read(String var0, File var1) throws IOException {
      return read(var0, LotBuildings.headers(new File(var0)), var1);
   }

   static ReliefData read(String var0, List<File> var1, File var2) throws IOException {
      long var3 = System.nanoTime();
      long var5 = ReliefFile.digest(var0, var1);
      ReliefFile.Fitted var7 = ReliefFile.read(new File(var2, "rebake.bin"));
      if (var7 != null && var7.digest() == var5) {
         return loaded(var7, "as rebaked in game", var3);
      }

      ReliefFile.Fitted var8 = ReliefFile.shipped();
      if (var8 != null && var8.digest() == var5 && var8.rules().equals(ReliefRules.DEFAULTS)) {
         return loaded(var8, "as the build fitted it", var3);
      }

      File var9 = new File(var2, Long.toHexString(var5) + ".bin");
      ReliefFile.Fitted var10 = ReliefFile.read(var9);
      if (var10 != null && var10.digest() == var5 && var10.rules().equals(ReliefRules.DEFAULTS)) {
         return loaded(var10, "as fitted here before for these map files", var3);
      }

      ReliefData var11 = build(var0, var1, ReliefRules.DEFAULTS);
      ReliefFile.write(var11.fitted(var5), var9);
      log(
         var11,
         var8 == null
            ? "fitted here: the mod has none fitted (a dev build's classes), kept for next time"
            : "fitted here: the map's files are not the ones the build fitted it to, kept for next time",
         var3
      );
      return var11;
   }

   static ReliefData rebake(String var0, List<File> var1, ReliefRules var2, File var3) throws IOException {
      long var4 = System.nanoTime();
      ReliefData var6 = build(var0, var1, var2);
      ReliefFile.write(var6.fitted(ReliefFile.digest(var0, var1)), new File(var3, "rebake.bin"));
      log(var6, "rebaked in game", var4);
      return var6;
   }

   static void forgetRebake(File var0) throws IOException {
      Files.deleteIfExists(new File(var0, "rebake.bin").toPath());
   }

   static ReliefData of(FarRelief.Grid var0, String var1, int var2) throws IOException {
      long var3 = System.nanoTime();
      ReliefData var5 = fit(ReliefData.Sources.read(var0, var1), ReliefRules.DEFAULTS.smoothed(var2), true);
      log(var5, "fitted here", var3);
      return var5;
   }

   private static ReliefData build(String var0, List<File> var1, ReliefRules var2) throws IOException {
      return fit(ReliefData.Sources.read(bakedGrid(), var0, var1), var2, true);
   }

   private static ReliefData loaded(ReliefFile.Fitted var0, String var1, long var2) {
      ReliefData var4 = new ReliefData(var0.grid(), var0.rules(), var0.counts(), true);
      log(var4, var1, var2);
      return var4;
   }

   static FarRelief.Grid bakedGrid() throws IOException {
      try (InputStream var0 = ReliefData.class.getResourceAsStream("/viewpoint/relief.png")) {
         if (var0 == null) {
            throw new FileNotFoundException("/viewpoint/relief.png is not in the mod");
         } else {
            return baked(var0);
         }
      }
   }

   private static void log(ReliefData var0, String var1, long var2) {
      System.out
         .printf(
            "[Viewpoint] far world: terrain relief %d x %d nodes smoothed over %d squares at %.1f times its height, %d nodes at the map's water, %d buildings and the lots on %d pads, roads along %d streets, %s, in %.0f ms%n",
            var0.grid.width(),
            var0.grid.height(),
            var0.rules.smoothingSquares(),
            var0.rules.height(),
            var0.counts.water(),
            var0.counts.buildings(),
            var0.counts.pads(),
            var0.counts.streets(),
            var1,
            (System.nanoTime() - var2) / 1000000.0
         );
   }

   ReliefFile.Fitted fitted(long var1) {
      return new ReliefFile.Fitted(this.grid, this.rules, var1, this.counts);
   }

   static ReliefData fit(ReliefData.Sources var0, int var1) {
      return fit(var0, ReliefRules.DEFAULTS.smoothed(var1), false);
   }

   private static ReliefData fit(ReliefData.Sources var0, ReliefRules var1, boolean var2) {
      int var3 = var0.baked().width();
      int var4 = var0.baked().height();
      ReliefFit.Result var5 = ReliefFit.fit(var0, var1);
      if (var2) {
         System.out.println("[Viewpoint] far world: terrain relief built: " + var5.report());
      }

      float[] var6 = var5.bodies().level();
      boolean[] var7 = new boolean[var6.length];

      for (int var8 = 0; var8 < var7.length; var8++) {
         var7[var8] = !Float.isNaN(var6[var8]);
      }

      return new ReliefData(FarRelief.Field.of(var3, var4, var5.ground(), var5.water(), var5.walls(), var5.flow(), var7), var1, var5.counts(), false);
   }

   static FarRelief.Grid baked(InputStream var0) throws IOException {
      BufferedImage var1 = ImageIO.read(var0);
      WritableRaster var2 = var1.getRaster();
      int var3 = var1.getWidth();
      int var4 = var1.getHeight();
      float[] var5 = new float[var3 * var4];

      for (int var6 = 0; var6 < var4; var6++) {
         for (int var7 = 0; var7 < var3; var7++) {
            var5[var6 * var3 + var7] = var2.getSample(var7, var6, 0) * 0.1F;
         }
      }

      return refined(new FarRelief.Grid(var3, var4, var5), 2);
   }

   static FarRelief.Grid refined(FarRelief.Grid var0, int var1) {
      int var2 = (var0.width() - 1) * var1 + 1;
      int var3 = (var0.height() - 1) * var1 + 1;
      float[] var4 = new float[var2 * var3];
      float[] var5 = var0.metres();

      for (int var6 = 0; var6 < var3; var6++) {
         int var7 = var6 / var1;
         int var8 = Math.min(var0.height() - 1, var7 + 1);
         float var9 = (float)(var6 % var1) / var1;

         for (int var10 = 0; var10 < var2; var10++) {
            int var11 = var10 / var1;
            int var12 = Math.min(var0.width() - 1, var11 + 1);
            float var13 = (float)(var10 % var1) / var1;
            float var14 = var5[var7 * var0.width() + var11] * (1.0F - var13) + var5[var7 * var0.width() + var12] * var13;
            float var15 = var5[var8 * var0.width() + var11] * (1.0F - var13) + var5[var8 * var0.width() + var12] * var13;
            var4[var6 * var2 + var10] = var14 * (1.0F - var9) + var15 * var9;
         }
      }

      return new FarRelief.Grid(var2, var3, var4);
   }

   float at(double var1, double var3) {
      return this.grid.metresAt(var1, var3);
   }

   float node(int var1, int var2) {
      int var3 = Math.max(0, Math.min(this.grid.width() - 1, var1));
      int var4 = Math.max(0, Math.min(this.grid.height() - 1, var2));
      return this.grid.metres(var4 * this.grid.width() + var3);
   }

   float top(int var1, int var2) {
      return this.onGrid(var1, var2) ? this.cellTops[var2 * this.cellsX + var1] : Float.NaN;
   }

   float low(int var1, int var2) {
      return this.onGrid(var1, var2) ? this.cellLows[var2 * this.cellsX + var1] : Float.NaN;
   }

   private boolean onGrid(int var1, int var2) {
      return var1 >= 0 && var2 >= 0 && var1 < this.cellsX && var2 < this.cellsY;
   }

   record Counts(int water, int buildings, int pads, int streets) {
   }

   record Sources(FarRelief.Grid baked, ReliefGround ground, int[] buildings, ReliefRoads roads, FlowPoints flows) {
      Sources(FarRelief.Grid var1, ReliefGround var2, int[] var3, ReliefRoads var4) {
         this(var1, var2, var3, var4, FlowPoints.NONE);
      }

      static ReliefData.Sources read(FarRelief.Grid var0, String var1) throws IOException {
         return read(var0, var1, LotBuildings.headers(new File(var1)));
      }

      static ReliefData.Sources read(FarRelief.Grid var0, String var1, List<File> var2) throws IOException {
         byte var3 = 4;
         byte var4 = 4;
         List var5 = LotBuildings.within(var2, (var0.width() - 1) * var4, (var0.height() - 1) * var4);
         ReliefGround var6 = ReliefGround.read(var1, (var0.width() - 1) * var4 / var3, (var0.height() - 1) * var4 / var3);
         File var7 = new File(var1);
         return new ReliefData.Sources(var0, var6, LotBuildings.read(var5), ReliefRoads.read(var7), FlowPoints.read(var7, var5));
      }
   }
}
