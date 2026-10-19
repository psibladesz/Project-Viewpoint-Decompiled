package viewpoint.light;

import java.nio.ByteBuffer;
import viewpoint.platform.IrisPacks;
import viewpoint.platform.Profile;
import viewpoint.render.BandLight;

public final class OwnedLight {
   private static final float SKY_LEVELS = 15.0F;
   private static final float INDOORS = 0.999F;
   private static final float ROOFED = 0.53333336F;
   private static int sky;
   private static long mainNanos;
   private static final int CHECK_EVERY = 60;
   private static volatile long publishedCaptures;
   private static volatile long publishedCaptureNanos;
   private static volatile long publishedMainNanos;
   private static volatile long publishedSources;
   private static volatile long publishedSourceNanos;
   private static volatile long publishedLevels;
   private static volatile long publishedLevelNanos;
   private static volatile String publishedCheck = "";
   private static volatile int publishedBandReach;
   private static int publishes;
   private static long[] reported = new long[7];

   public static void update(int var0, int var1, int var2, OwnedLight.Grids var3) {
      long var4 = System.nanoTime();
      sky = Daylight.skyLevel();
      LightJobs.update(var0, var1, var2);
      BandGrids.update(var0, var1);

      for (LevelLight var7 : LightJobs.worked) {
         var3.rewrite(var7.chunkX, var7.chunkY, var7.level);
      }

      mainNanos = mainNanos + (System.nanoTime() - var4);
      publish();
   }

   public static boolean write(ByteBuffer var0, int var1, int var2, int var3) {
      LevelLight var4 = LightJobs.light(var1, var2, var3);
      if (var4 == null) {
         return false;
      }

      long var5 = System.nanoTime();
      var0.put(0, var4.grid);
      mainNanos = mainNanos + (System.nanoTime() - var5);
      return true;
   }

   public static int lightAt(int var0, int var1, int var2) {
      LevelLight var3 = LightJobs.light(Math.floorDiv(var0, 8), Math.floorDiv(var1, 8), var2);
      if (var3 == null) {
         return -1;
      }

      int var4 = square(var0, var1);
      return LightModel.combine(sky, var3.steps[var4], var3.light[var4]);
   }

   public static int modelLightAt(int var0, int var1, int var2) {
      if (IrisPacks.active() == null) {
         return lightAt(var0, var1, var2);
      }

      LevelLight var3 = LightJobs.light(Math.floorDiv(var0, 8), Math.floorDiv(var1, 8), var2);
      return var3 == null ? 0 : var3.light[square(var0, var1)];
   }

   public static float modelSky(int var0, int var1, int var2, boolean var3) {
      if (var3) {
         return 1.0F;
      }

      LevelLight var4 = LightJobs.light(Math.floorDiv(var0, 8), Math.floorDiv(var1, 8), var2);
      if (var4 == null) {
         return 0.53333336F;
      }

      int var5 = Math.min(255, 16 * (var4.steps[square(var0, var1)] & 255));
      return Math.min(0.999F, 1.0F - var5 / 240.0F);
   }

   private static int square(int var0, int var1) {
      return Math.floorMod(var1, 8) * 8 + Math.floorMod(var0, 8);
   }

   public static void forget(int var0, int var1) {
      LightLayouts.forget(var0, var1);
   }

   public static void clear() {
      LightLayouts.clear();
      LightJobs.clear();
      BandGrids.clear();
      MapLight.clear();
      MapCheck.clear();
   }

   private static void publish() {
      publishedCaptures = publishedCaptures + LightLayouts.captures;
      publishedCaptureNanos = publishedCaptureNanos + LightLayouts.captureNanos;
      LightLayouts.captures = 0;
      LightLayouts.captureNanos = 0L;
      publishedSources = publishedSources + LightJobs.sourceJobs;
      publishedSourceNanos = publishedSourceNanos + LightJobs.sourceNanos;
      publishedLevels = publishedLevels + LightJobs.levelJobs;
      publishedLevelNanos = publishedLevelNanos + LightJobs.levelNanos;
      LightJobs.levelJobs = 0;
      LightJobs.sourceJobs = 0;
      LightJobs.levelNanos = 0L;
      LightJobs.sourceNanos = 0L;
      publishedMainNanos = mainNanos;
      publishedBandReach = BandGrids.reachChunks();
      if (publishes++ % 60 == 0) {
         publishedCheck = MapCheck.report();
      }
   }

   private static String report() {
      long[] var0 = new long[]{
         publishedCaptures, publishedCaptureNanos, publishedMainNanos, publishedSources, publishedSourceNanos, publishedLevels, publishedLevelNanos
      };
      long[] var1 = new long[var0.length];

      for (int var2 = 0; var2 < var0.length; var2++) {
         var1[var2] = var0[var2] - reported[var2];
      }

      reported = var0;
      return "layouts "
         + var1[0]
         + " ("
         + ms(var1[1])
         + " ms), main "
         + ms(var1[2])
         + " ms, sources "
         + var1[3]
         + " ("
         + ms(var1[4])
         + " ms on workers), levels "
         + var1[5]
         + " ("
         + ms(var1[6])
         + " ms on workers), band grids "
         + BandLight.grids()
         + " of "
         + BandLight.share()
         + " within "
         + publishedBandReach
         + " chunks ("
         + BandLight.turnedAway()
         + " turned away), "
         + publishedCheck;
   }

   private static String ms(long var0) {
      return String.format("%.1f", var0 / 1000000.0);
   }

   private OwnedLight() {
   }

   static {
      Profile.ownedLightReport = OwnedLight::report;
   }

   public interface Grids {
      void rewrite(int var1, int var2, int var3);
   }
}
