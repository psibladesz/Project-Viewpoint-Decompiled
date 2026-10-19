package viewpoint.platform;

final class Hitches {
   static final double SLOW_MS = 50.0;
   private static final int MAX_LINES = 10;
   private static final long GAP_NANOS = 1000000000L;
   private static long lastRender;
   private static int renderSlow;
   private static int renderLines;
   private static double renderWorst;
   private static volatile int mainSlow;
   private static volatile int mainLines;
   private static volatile double mainWorst;
   private static final double PART_MS = 1.0;

   static void render(long var0, double[] var2, String[] var3) {
      long var4 = var0 - lastRender;
      boolean var6 = lastRender != 0L && var4 < 1000000000L;
      lastRender = var0;
      if (var6 && !(var4 * 1.0E-6 < 50.0)) {
         double var7 = var4 * 1.0E-6;
         double var9 = 0.0;
         StringBuilder var11 = new StringBuilder();

         for (int var12 = 0; var12 < var2.length; var12++) {
            var9 += var2[var12];
            if (var2[var12] >= 0.5) {
               var11.append(String.format(" %s %.1f", var3[var12], var2[var12]));
            }
         }

         renderSlow++;
         renderWorst = Math.max(renderWorst, var7);
         if (renderLines++ < 10) {
            System.out
               .println(
                  String.format(
                     "[Viewpoint] slow frame on the render thread: %.1f ms, our passes %.1f (%s), the rest %.1f (the game's own drawing, or waiting for the main thread)",
                     var7,
                     var9,
                     var11.length() == 0 ? "none over 0.5" : var11.substring(1),
                     var7 - var9
                  )
               );
         }
      }
   }

   static void main(double var0, double var2, double var4, double var6, int var8, String var9) {
      mainSlow++;
      mainWorst = Math.max(mainWorst, var0);
      double var10 = var2 + var4 + var6;
      if (mainLines++ < 10) {
         System.out
            .println(
               String.format(
                  "[Viewpoint] slow frame on the main thread: %.1f ms, ours %.1f (chunk cache %.1f: %d builds; far world %.1f; snapshot %.1f; by part: %s), the rest %.1f (the game's update, the collector's pauses)",
                  var0,
                  var10,
                  var2,
                  var8,
                  var4,
                  var6,
                  var9,
                  var0 - var10
               )
            );
      }
   }

   static String parts(String[] var0, long[] var1) {
      StringBuilder var2 = new StringBuilder();

      for (int var3 = 0; var3 < var1.length; var3++) {
         double var4 = var1[var3] * 1.0E-6;
         if (var4 >= 1.0) {
            var2.append(var2.length() == 0 ? "" : ", ").append(String.format("%s %.1f", var0[var3], var4));
         }
      }

      return var2.length() == 0 ? "none over 1 ms" : var2.toString();
   }

   static String report() {
      String var0 = String.format(" | frames over %.0f ms: render %d (worst %.0f), main %d (worst %.0f)", 50.0, renderSlow, renderWorst, mainSlow, mainWorst);
      renderLines = 0;
      renderSlow = 0;
      renderWorst = 0.0;
      mainLines = 0;
      mainSlow = 0;
      mainWorst = 0.0;
      return var0;
   }

   private Hitches() {
   }
}
