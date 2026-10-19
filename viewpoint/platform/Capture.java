package viewpoint.platform;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jdk.jfr.Category;
import jdk.jfr.Configuration;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import jdk.jfr.StackTrace;
import zombie.ZomboidFileSystem;

public final class Capture {
   private static final LiveSettings.Number SECONDS = LiveSettings.number("capture.seconds", "Capture length (s)", "Debug/Profiling", 5.0F, 120.0F, 5.0F, 30.0F);
   private static final String DIRECTORY = "viewpoint-captures";
   private static final long SAMPLE_MS = 1L;
   private static final long MEMORY_NANOS = 1000000000L;
   private static volatile boolean requested;
   private static volatile String status = "";
   private static Recording recording;
   private static Path file;
   private static long lastRenderFrame;
   private static long lastMemory;

   static void request() {
      requested = true;
   }

   static String status() {
      return status;
   }

   static void update() {
      if (recording != null) {
         follow();
      }

      if (requested) {
         requested = false;
         if (recording == null) {
            start();
         }
      }

      CaptureUi.frame(recording != null);
   }

   private static void start() {
      try {
         File var0 = new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-captures");
         var0.mkdirs();
         String var1 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
         file = new File(var0, "capture-" + var1 + ".jfr").toPath();
         recording = new Recording(Configuration.getConfiguration("profile"));
         recording.enable("jdk.ExecutionSample").withPeriod(Duration.ofMillis(1L));
         recording.enable("jdk.NativeMethodSample").withPeriod(Duration.ofMillis(1L));
         recording.setName("Viewpoint capture " + var1);
         recording.setDuration(Duration.ofSeconds(SECONDS.getInt()));
         recording.setDestination(file);
         recording.start();
         status = "recording " + SECONDS.getInt() + " s: keep moving";
         System.out.println("[Viewpoint] capture started, " + SECONDS.getInt() + " s, to " + file);
      } catch (Exception | LinkageError var2) {
         recording = null;
         status = "could not start: " + var2.getMessage();
         System.out.println("[Viewpoint] capture could not start: " + var2);
      }
   }

   private static void follow() {
      RecordingState var0 = recording.getState();
      if (var0 != RecordingState.STOPPED && var0 != RecordingState.CLOSED) {
         long var1 = SECONDS.getInt() - Duration.between(recording.getStartTime(), Instant.now()).toSeconds();
         status = "recording, " + Math.max(0L, var1) + " s left: keep moving";
      } else {
         recording.close();
         recording = null;
         status = "saved " + file.getFileName() + " (in Zomboid/viewpoint-captures)";
         System.out.println("[Viewpoint] capture saved to " + file);
      }
   }

   static void renderFrame(long var0, double[] var2, int var3) {
      long var4 = lastRenderFrame;
      lastRenderFrame = var0;
      Capture.RenderFrame var6 = new Capture.RenderFrame();
      if (var4 != 0L && var6.shouldCommit()) {
         float var7 = 0.0F;

         for (double var11 : var2) {
            var7 += (float)var11;
         }

         var6.frameMs = (float)(var0 - var4) * 1.0E-6F;
         var6.oursMs = var7;
         var6.floors = (float)var2[0];
         var6.shadow = (float)var2[1];
         var6.gbuffer = (float)var2[2];
         var6.light = (float)var2[3];
         var6.far = (float)var2[4];
         var6.outlines = (float)var2[5];
         var6.glass = (float)var2[6];
         var6.bounce = (float)var2[7];
         var6.weather = (float)var2[8];
         var6.air = (float)var2[9];
         var6.bloom = (float)var2[10];
         var6.post = (float)var2[11];
         var6.draws = var3;
         var6.commit();
         if (var0 - lastMemory >= 1000000000L) {
            lastMemory = var0;
            memory();
         }
      }
   }

   static void gpuFrame(double[] var0) {
      CaptureEvents.GpuFrame var1 = new CaptureEvents.GpuFrame();
      if (var1.shouldCommit()) {
         float var2 = 0.0F;

         for (double var6 : var0) {
            var2 += (float)var6;
         }

         var1.oursMs = var2;
         var1.floors = (float)var0[0];
         var1.shadow = (float)var0[1];
         var1.gbuffer = (float)var0[2];
         var1.light = (float)var0[3];
         var1.far = (float)var0[4];
         var1.outlines = (float)var0[5];
         var1.glass = (float)var0[6];
         var1.bounce = (float)var0[7];
         var1.weather = (float)var0[8];
         var1.air = (float)var0[9];
         var1.bloom = (float)var0[10];
         var1.post = (float)var0[11];
         var1.commit();
      }
   }

   private static void memory() {
      CaptureEvents.Memory var0 = new CaptureEvents.Memory();
      if (var0.shouldCommit()) {
         var0.videoFreeMiB = VideoMemory.freeMiB();
         var0.videoTotalMiB = VideoMemory.totalMiB();
         var0.videoEvictions = VideoMemory.evictions();
         var0.committedMiB = ProcessMemory.committed() >> 20;
         Runtime var1 = Runtime.getRuntime();
         var0.heapUsedMiB = var1.totalMemory() - var1.freeMemory() >> 20;
         var0.heapCommittedMiB = var1.totalMemory() >> 20;
         var0.commit();

         for (Pool var5 : Pool.ALL) {
            CaptureEvents.PoolUse var6 = new CaptureEvents.PoolUse();
            var6.pool = var5.name;
            var6.usedMiB = (float)var5.used() / 1048576.0F;
            var6.capMiB = (float)var5.cap() / 1048576.0F;
            var6.commit();
         }
      }
   }

   static void mainFrame(double var0, long var2, long var4, long var6, int var8) {
      Capture.MainFrame var9 = new Capture.MainFrame();
      if (var9.shouldCommit()) {
         var9.frameMs = (float)var0;
         var9.cacheMs = (float)var2 * 1.0E-6F;
         var9.farMs = (float)var4 * 1.0E-6F;
         var9.snapshotMs = (float)var6 * 1.0E-6F;
         var9.builds = var8;
         var9.commit();
      }
   }

   private Capture() {
   }

   @Name("Viewpoint.MainFrame")
   @Label("Viewpoint main frame")
   @Category("Viewpoint")
   @StackTrace(false)
   static final class MainFrame extends Event {
      @Label("Frame (ms)")
      float frameMs;
      @Label("Chunk cache (ms)")
      float cacheMs;
      @Label("Far world (ms)")
      float farMs;
      @Label("Snapshot (ms)")
      float snapshotMs;
      @Label("Level builds")
      int builds;
   }

   @Name("Viewpoint.RenderFrame")
   @Label("Viewpoint render frame")
   @Category("Viewpoint")
   @StackTrace(false)
   static final class RenderFrame extends Event {
      @Label("Frame (ms)")
      float frameMs;
      @Label("Ours (ms)")
      float oursMs;
      @Label("Floor bakes")
      float floors;
      @Label("Shadow")
      float shadow;
      @Label("G-buffer")
      float gbuffer;
      @Label("Sky and light")
      float light;
      @Label("Far world")
      float far;
      @Label("Outlines")
      float outlines;
      @Label("Glass")
      float glass;
      @Label("Bounce light")
      float bounce;
      @Label("Weather")
      float weather;
      @Label("Air")
      float air;
      @Label("Bloom")
      float bloom;
      @Label("Post and TAA")
      float post;
      @Label("Draw calls")
      int draws;
   }
}
