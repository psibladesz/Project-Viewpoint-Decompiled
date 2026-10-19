package viewpoint.platform;

import java.util.Arrays;
import java.util.function.Supplier;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

public final class Profile {
   public static final int START = 0;
   public static final int FLOORS = 1;
   public static final int SHADOW = 2;
   public static final int GBUFFER = 3;
   public static final int INDIRECT = 4;
   public static final int LIGHTING = 5;
   public static final int FAR = 6;
   public static final int OUTLINES = 7;
   public static final int TRANSLUCENT = 8;
   public static final int WEATHER = 9;
   public static final int VOLUME = 10;
   public static final int BLOOM = 11;
   public static final int POST = 12;
   private static final String[] NAMES = new String[]{
      "floors", "shadow", "gbuffer", "occlusion", "sky+light", "far", "outlines", "glass", "weather", "air", "bloom", "post+taa"
   };
   private static final int SLOTS = 4;
   private static final int MARKS = 13;
   private static final long LOG_PERIOD_NANOS = 10000000000L;
   private static int[][] queries;
   private static final int[] issued = new int[4];
   private static int slot;
   private static final double[] gpuMs = new double[12];
   private static final double[] cpuMs = new double[12];
   private static final double[] recentGpuMs = new double[12];
   private static final double[] recentCpuMs = new double[12];
   private static final double[] frameGpuMs = new double[12];
   private static final double RECENT = 0.02;
   private static final double[] frameCpuMs = new double[12];
   private static final long[] cpuAt = new long[13];
   private static int gpuFrames;
   private static int frames;
   private static long lastLog = System.nanoTime();
   public static int draws;
   public static int meshDraws;
   public static int ownedModels;
   public static int modelRuns;
   public static int paletteBones;
   public static int modelTexturesMade;
   public static int modelTexturesWaiting;
   public static int looksMade;
   public static int looksWaiting;
   public static int newModelsAsked;
   public static int newModelsWaiting;
   public static long modelTriangles;
   public static long paletteBytes;
   private static double drawSum;
   private static double meshDrawSum;
   public static volatile int farDrawn;
   public static volatile int farInView;
   public static volatile int shellsInView;
   public static volatile int farTrees;
   public static volatile int farLampCards;
   public static volatile int farFullColours;
   public static volatile int farHalfColours;
   public static volatile int farQuarterColours;
   public static volatile long bandVertices;
   public static volatile long shellVerticesDrawn;
   public static volatile Supplier<String> arenaUsage = () -> "";
   public static volatile Supplier<String> roomsReport = () -> "off";
   public static volatile Supplier<String> roofsReport = () -> "none";
   public static volatile Supplier<String> floorsReport = () -> "none";
   public static volatile Supplier<String> shellGroundReport = () -> "none";
   public static volatile Supplier<String> lightSourcesReport = () -> "none";
   public static volatile Supplier<String> ownedLightReport = () -> "none";
   public static volatile Supplier<String> lampShadowsReport = () -> "off";
   public static volatile Supplier<String> farMapsReport = () -> "off";
   public static volatile Supplier<String> cascadeLayersReport = () -> "off";
   public static volatile Supplier<String> headlightsReport = () -> "none";
   public static volatile Supplier<String> packsReport = () -> "none";
   public static volatile Supplier<String> corpsesReport = () -> "none";
   public static volatile Supplier<String> latencyReport = () -> "off";
   public static volatile Supplier<String> eyeReport = () -> "off";
   private static double cacheMs;
   private static double farMs;
   private static double snapshotMs;
   private static double rebuildSum;
   private static double meshSum;
   private static double buildSum;
   private static double buildMs;
   private static double waitingSum;
   static volatile double lastRenderMs;
   static volatile long lastFrameAt;
   public static volatile int shellVertices;
   public static volatile int shellUploads;
   private static long lastMainFrame;
   private static volatile double mainFrameSum;
   private static volatile double zombieSum;
   private static volatile double askedSum;
   private static volatile double offeredSum;
   private static volatile double freshSum;
   private static volatile double keptSum;
   private static volatile double posedSum;
   private static volatile double vehicleFreshSum;
   private static volatile double vehicleKeptSum;
   private static volatile int mainFrames;
   static volatile double lastMainMs;
   static volatile double lastMainOursMs;
   static volatile long cacheNanos;
   static volatile long farNanos;
   static volatile long snapshotNanos;
   private static String[] partNames = new String[0];
   private static long[] parts = new long[0];
   public static volatile String note = "";
   public static volatile int rebuilds;
   public static volatile int meshes;
   public static volatile int cooking;
   private static volatile int chunkBuilds;
   private static volatile int chunkWaiting;
   private static volatile long chunkBuildNanos;
   private static final int[] reasons = new int[5];
   private static volatile int unchangedSum;
   private static volatile int ringSparedSum;
   private static volatile int signalSum;
   private static volatile int overflowSum;
   public static volatile double recentFarMainMs;

   public static void mark(int var0) {
      if (queries == null) {
         queries = new int[4][13];

         for (int[] var4 : queries) {
            GL15.glGenQueries(var4);
         }
      }

      GL33.glQueryCounter(queries[slot][var0], 36392);
      issued[slot] = issued[slot] | 1 << var0;
      long var5 = System.nanoTime();
      if (var0 > 0 && cpuAt[var0 - 1] != 0L) {
         double var6 = (var5 - cpuAt[var0 - 1]) * 1.0E-6;
         cpuMs[var0 - 1] = cpuMs[var0 - 1] + var6;
         frameCpuMs[var0 - 1] = frameCpuMs[var0 - 1] + var6;
         recentCpuMs[var0 - 1] = recentCpuMs[var0 - 1] + (var6 - recentCpuMs[var0 - 1]) * 0.02;
      }

      cpuAt[var0] = var5;
   }

   public static void endFrame() {
      int var0 = draws;
      frames++;
      cacheMs = cacheMs + cacheNanos * 1.0E-6;
      farMs = farMs + farNanos * 1.0E-6;
      snapshotMs = snapshotMs + snapshotNanos * 1.0E-6;
      rebuildSum = rebuildSum + rebuilds;
      meshSum = meshSum + meshes;
      buildSum = buildSum + chunkBuilds;
      buildMs = buildMs + chunkBuildNanos * 1.0E-6;
      waitingSum = waitingSum + chunkWaiting;
      drawSum = drawSum + draws;
      draws = 0;
      meshDrawSum = meshDrawSum + meshDraws;
      meshDraws = 0;
      rebuilds = 0;
      Arrays.fill(cpuAt, 0L);
      GpuParts.frameEnded();
      slot = (slot + 1) % 4;
      if (issued[slot] == 8191 && GL15.glGetQueryObjecti(queries[slot][12], 34919) != 0) {
         long var1 = GL33.glGetQueryObjectui64(queries[slot][0], 34918);

         for (int var3 = 1; var3 < 13; var3++) {
            long var4 = GL33.glGetQueryObjectui64(queries[slot][var3], 34918);
            frameGpuMs[var3 - 1] = (var4 - var1) * 1.0E-6;
            gpuMs[var3 - 1] = gpuMs[var3 - 1] + frameGpuMs[var3 - 1];
            recentGpuMs[var3 - 1] = recentGpuMs[var3 - 1] + (frameGpuMs[var3 - 1] - recentGpuMs[var3 - 1]) * 0.02;
            var1 = var4;
         }

         gpuFrames++;
         Capture.gpuFrame(frameGpuMs);
      }

      issued[slot] = 0;
      long var10 = System.nanoTime();
      Hitches.render(var10, frameCpuMs, NAMES);
      double var11 = 0.0;

      for (double var8 : frameCpuMs) {
         var11 += var8;
      }

      lastRenderMs = var11;
      lastFrameAt = var10;
      Capture.renderFrame(var10, frameCpuMs, var0);
      Arrays.fill(frameCpuMs, 0.0);
      ProcessMemory.sample(var10);
      if (var10 - lastLog >= 10000000000L) {
         log(var10 - lastLog);
         lastLog = var10;
      }
   }

   private static void log(long var0) {
      StringBuilder var2 = new StringBuilder(String.format("[Viewpoint] %.0f fps | gpu ms", frames / (var0 * 1.0E-9)));
      double var3 = 0.0;
      double var5 = 0.0;

      for (int var7 = 0; var7 < gpuMs.length; var7++) {
         double var8 = gpuFrames == 0 ? 0.0 : gpuMs[var7] / gpuFrames;
         var3 += var8;
         var2.append(String.format(" %s %.2f", NAMES[var7], var8));
         gpuMs[var7] = 0.0;
      }

      var2.append(String.format(" (%.2f)", var3)).append(GpuParts.report()).append(" | render cpu ms");

      for (int var10 = 0; var10 < cpuMs.length; var10++) {
         double var12 = cpuMs[var10] / Math.max(frames, 1);
         var5 += var12;
         var2.append(String.format(" %s %.2f", NAMES[var10], var12));
         cpuMs[var10] = 0.0;
      }

      var2.append(
         String.format(
            " (%.2f) | draws/frame %.0f (mesh draws in them %.0f; arena %s)",
            var5,
            drawSum / Math.max(frames, 1),
            meshDrawSum / Math.max(frames, 1),
            arenaUsage.get()
         )
      );
      var2.append(DrawCounts.report(frames));
      logRenderer(var2);
      int var11 = Math.max(mainFrames, 1);
      var2.append(
         String.format(
            " | main thread frame %.2f ms (%.0f fps) | zombies loaded %.0f, asked %.0f, given a model %.0f; characters drawn from fresh snapshots %.0f, kept %.0f, kept in a new pose %.0f; vehicles drawn from fresh snapshots %.0f, kept %.0f",
            mainFrameSum / var11,
            mainFrameSum > 0.0 ? 1000.0 * var11 / mainFrameSum : 0.0,
            zombieSum / var11,
            askedSum / var11,
            offeredSum / var11,
            freshSum / var11,
            keptSum / var11,
            posedSum / var11,
            vehicleFreshSum / var11,
            vehicleKeptSum / var11
         )
      );
      posedSum = 0.0;
      keptSum = 0.0;
      freshSum = 0.0;
      offeredSum = 0.0;
      askedSum = 0.0;
      zombieSum = 0.0;
      mainFrameSum = 0.0;
      vehicleKeptSum = 0.0;
      vehicleFreshSum = 0.0;
      mainFrames = 0;
      var2.append(" | latency ").append(latencyReport.get());
      var2.append(
         String.format(
            " | main ms cache %.2f far %.2f snapshot %.2f | rebuilds/frame %.1f meshes %.0f",
            cacheMs / frames,
            farMs / frames,
            snapshotMs / frames,
            rebuildSum / frames,
            meshSum / frames
         )
      );
      logChunks(var2);
      var2.append(" | rooms ").append(roomsReport.get());
      var2.append(" | roofs ").append(roofsReport.get());
      var2.append(" | floors ").append(floorsReport.get());
      var2.append(" | shell ground ").append(shellGroundReport.get());
      var2.append(" | model packs ").append(packsReport.get());
      var2.append(" | corpses ").append(corpsesReport.get());
      logMemory(var2);
      var2.append(Hitches.report());
      var2.append(" | eye ").append(eyeReport.get());
      System.out.println(var2.append(" | ").append(note));
      gpuFrames = 0;
      frames = 0;
      meshSum = 0.0;
      rebuildSum = 0.0;
      snapshotMs = 0.0;
      farMs = 0.0;
      cacheMs = 0.0;
      meshDrawSum = 0.0;
      drawSum = 0.0;
      waitingSum = 0.0;
      buildMs = 0.0;
      buildSum = 0.0;
   }

   private static void logRenderer(StringBuilder var0) {
      var0.append(
         String.format(
            " | owned model instances drawn %d (%d multi-draws, %.2fM triangles; palettes %d bones, %.1f MB; textures made %d, waiting %d; looks made %d, waiting %d; new objects' models asked %d, waiting %d)",
            ownedModels,
            modelRuns,
            modelTriangles * 1.0E-6,
            paletteBones,
            paletteBytes * 1.0E-6,
            modelTexturesMade,
            modelTexturesWaiting,
            looksMade,
            looksWaiting,
            newModelsAsked,
            newModelsWaiting
         )
      );
      var0.append(" | lamp shadows ").append(lampShadowsReport.get());
      var0.append(" | headlights ").append(headlightsReport.get());
      var0.append(" | far maps ").append(farMapsReport.get());
      var0.append(" | cascade layers ").append(cascadeLayersReport.get());
      var0.append(
         String.format(
            " | far cells in view %d of %d (%.0fk tree cards, %d lamp cards; colours %d full, %d half, %d quarter), shell blocks in view %d (%.1fM vertices held; drawn %.1fM in the band, %.1fM past it)",
            farInView,
            farDrawn,
            farTrees / 1000.0,
            farLampCards,
            farFullColours,
            farHalfColours,
            farQuarterColours,
            shellsInView,
            shellVertices / 1000000.0,
            bandVertices / 1000000.0,
            shellVerticesDrawn / 1000000.0
         )
      );
   }

   private static void logMemory(StringBuilder var0) {
      var0.append(Pool.report());
      var0.append(ProcessMemory.report());
      var0.append(VideoMemory.report());
   }

   private static void logChunks(StringBuilder var0) {
      var0.append(
         String.format(
            " | builds/frame %.1f (%.2f ms each on the main thread) cooking %d waiting in view %.0f",
            buildSum / frames,
            buildSum == 0.0 ? 0.0 : buildMs / buildSum,
            cooking,
            waitingSum / frames
         )
      );
      var0.append(
         String.format(
            " | builds by cause: new %d, game %d, retry %d, model ring %d (%d crossings needed none), model packs %d; unchanged, not cooked %d",
            reasons[0],
            reasons[1],
            reasons[2],
            reasons[3],
            ringSparedSum,
            reasons[4],
            unchangedSum
         )
      );
      var0.append(String.format(" | signals %d, overflows %d", signalSum, overflowSum));
      var0.append(" | light sources ").append(lightSourcesReport.get());
      var0.append(" | owned light ").append(ownedLightReport.get());
      Arrays.fill(reasons, 0);
      overflowSum = 0;
      signalSum = 0;
      ringSparedSum = 0;
      unchangedSum = 0;
   }

   public static void mainFrame(long var0) {
      if (lastMainFrame != 0L && var0 - lastMainFrame < 1000000000L) {
         double var2 = (var0 - lastMainFrame) * 1.0E-6;
         mainFrameSum += var2;
         mainFrames++;
         lastMainMs = var2;
         lastMainOursMs = (cacheNanos + farNanos + snapshotNanos) * 1.0E-6;
         Capture.mainFrame(var2, cacheNanos, farNanos, snapshotNanos, chunkBuilds);
         if (var2 >= 50.0) {
            Hitches.main(var2, cacheNanos * 1.0E-6, farNanos * 1.0E-6, snapshotNanos * 1.0E-6, chunkBuilds, Hitches.parts(partNames, parts));
         }
      }

      lastMainFrame = var0;
   }

   public static void zombies(int var0, int var1, int var2) {
      zombieSum += var0;
      askedSum += var1;
      offeredSum += var2;
   }

   public static void snapshots(int var0, int var1, int var2) {
      freshSum += var0;
      keptSum += var1;
      posedSum += var2;
   }

   public static void vehicleSnapshots(int var0, int var1) {
      vehicleFreshSum += var0;
      vehicleKeptSum += var1;
   }

   public static double recentGpuMs(int var0) {
      return recentGpuMs[var0 - 1];
   }

   public static double recentCpuMs(int var0) {
      return recentCpuMs[var0 - 1];
   }

   public static void mainParts(String[] var0, long[] var1) {
      partNames = var0;
      parts = var1;
   }

   public static void cpu(long var0, long var2, long var4, int var6) {
      cacheNanos = var0;
      farNanos = var2;
      recentFarMainMs = recentFarMainMs + (var2 * 1.0E-6 - recentFarMainMs) * 0.02;
      snapshotNanos = var4;
      meshes = var6;
   }

   public static void rebuildReason(int var0) {
      reasons[var0]++;
   }

   public static void ringSpared() {
      ringSparedSum++;
   }

   public static void chunkSignals(int var0, boolean var1, int var2) {
      signalSum += var0;
      overflowSum += var1 ? 1 : 0;
      unchangedSum += var2;
   }

   public static void chunks(int var0, long var1, int var3) {
      chunkBuilds = var0;
      chunkBuildNanos = var1;
      chunkWaiting = var3;
   }

   private Profile() {
   }
}
