package viewpoint.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.lwjglx.opengl.Display;
import org.lwjglx.opengl.DisplayMode;
import zombie.core.Core;
import zombie.core.PerformanceSettings;
import zombie.core.SpriteRenderer;
import zombie.ui.TextManager;
import zombie.ui.UIFont;

public final class PerformanceOverlay {
   private static final LiveSettings.Choice MODE = LiveSettings.choice(
      "overlay.mode", "Performance overlay", "Debug/Profiling", new String[]{"Full", "Compact", "Graph only", "Off"}, PerformanceOverlay.Mode.OFF.ordinal()
   );
   private static final LiveSettings.Number SPAN = LiveSettings.number(
      "overlay.spanSeconds", "Overlay statistics over (s)", "Debug/Profiling", 1.0F, 30.0F, 1.0F, 5.0F
   );
   private static final PerformanceOverlay.Tint PLAIN = new PerformanceOverlay.Tint(0.92F, 0.92F, 0.92F);
   private static final PerformanceOverlay.Tint GOOD = new PerformanceOverlay.Tint(0.5F, 0.95F, 0.55F);
   private static final PerformanceOverlay.Tint FAIR = new PerformanceOverlay.Tint(1.0F, 0.75F, 0.35F);
   private static final PerformanceOverlay.Tint POOR = new PerformanceOverlay.Tint(1.0F, 0.4F, 0.4F);
   private static final PerformanceOverlay.Tint GPU = new PerformanceOverlay.Tint(0.45F, 0.65F, 1.0F);
   private static final UIFont FONT = UIFont.CodeSmall;
   private static final long REBUILD_NANOS = 250000000L;
   private static final long SECOND = 1000000000L;
   private static final int MARGIN_TOP = 40;
   private static final int MARGIN_RIGHT = 15;
   private static final int INSET = 6;
   private static final int COLUMN = 2;
   private static final int[] COLUMNS = new int[]{120, 90, 90};
   private static final int[] GRAPH_HEIGHT = new int[]{56, 36, 46};
   private static final float BUDGETS_TALL = 2.5F;
   private static final float BACKDROP = 0.6F;
   private static final float AT_CAP = 0.97F;
   private static final float BUSY = 90.0F;
   private static List<PerformanceOverlay.Line> lines = List.of();
   private static String graphLabel = "";
   private static PerformanceOverlay.Tint graphTint = PLAIN;
   private static float budgetMs = 16.666666F;
   private static long builtAt;
   private static boolean broken;

   private PerformanceOverlay() {
   }

   static PerformanceOverlay.Mode mode() {
      return PerformanceOverlay.Mode.values()[MODE.get()];
   }

   static void cycle() {
      MODE.set((MODE.get() + 1) % PerformanceOverlay.Mode.values().length);
   }

   static void loaded() {
      if (!Build.RELEASE && mode() == PerformanceOverlay.Mode.OFF) {
         MODE.set(PerformanceOverlay.Mode.FULL.ordinal());
      }
   }

   public static void draw() {
      if (Keys.OVERLAY.pressed()) {
         cycle();
      }

      PerformanceOverlay.Mode var0 = mode();
      if (var0 != PerformanceOverlay.Mode.OFF && !broken) {
         long var1 = System.nanoTime();
         Utilization.sample(var1);
         if (var1 - builtAt >= 250000000L) {
            builtAt = var1;
            build(var0, var1);
         }

         try {
            paint(var0);
         } catch (RuntimeException var4) {
            broken = true;
            System.out.println("[Viewpoint] performance overlay stopped: " + var4);
         }
      }
   }

   private static void build(PerformanceOverlay.Mode var0, long var1) {
      FrameStats var3 = FrameStats.of(FrameTimes.recent(var1, SPAN.getInt() * 1000000000L));
      int var4 = cap();
      budgetMs = 1000.0F / (var4 > 0 ? var4 : 60);
      if (var3 == null) {
         lines = List.of(new PerformanceOverlay.Line("no frames yet", PLAIN));
         graphLabel = "";
      } else {
         PerformanceOverlay.Tint var5 = rateTint(var3.lastSecond(), var4);
         String var6 = limit(var3.lastSecond(), var4);
         graphLabel = var3.lastSecond() + " fps";
         graphTint = var5;

         lines = switch (var0) {
            case FULL -> full(var3, var4, var5, var6, var1);
            case COMPACT -> List.of(new PerformanceOverlay.Line(compact(var3, var6), var5));
            default -> List.of();
         };
      }
   }

   private static List<PerformanceOverlay.Line> full(FrameStats var0, int var1, PerformanceOverlay.Tint var2, String var3, long var4) {
      Runtime var6 = Runtime.getRuntime();
      String var7 = var1 <= 0 ? "none" : (PerformanceSettings.instance.isFramerateUncapped() ? var1 + " Hz display" : var1 + " fps");
      ArrayList var8 = new ArrayList();
      var8.add(new PerformanceOverlay.Line(String.format(Locale.ROOT, "%d fps  %.2f ms mean  cap %s", var0.lastSecond(), var0.meanMs(), var7), var2));
      var8.add(
         new PerformanceOverlay.Line(
            String.format(Locale.ROOT, "median %.1f  99%% %.1f  99.9%% %.1f  worst %.1f ms", var0.medianMs(), var0.p99Ms(), var0.p999Ms(), var0.worstMs()),
            PLAIN
         )
      );
      var8.add(
         new PerformanceOverlay.Line(
            String.format(Locale.ROOT, "slowest 1%% %.0f fps  jitter %.1f ms  spikes %d", var0.lowFps(), var0.jitterMs(), var0.spikes()), PLAIN
         )
      );
      var8.add(
         new PerformanceOverlay.Line(
            String.format(
               Locale.ROOT, "busy: main %.0f%%  render %.0f%%  GPU %s  process %.0f%%", Utilization.game, Utilization.render, gpuBusy(), Utilization.process
            ),
            PLAIN
         )
      );
      if (firstPerson(var4)) {
         double var9 = Profile.lastMainOursMs;
         double var11 = Math.max(0.0, Profile.lastMainMs - var9);
         var8.add(
            new PerformanceOverlay.Line(
               String.format(Locale.ROOT, "first person: main %.1f ms (the game %.1f)  render %.1f ms", var9, var11, Profile.lastRenderMs), PLAIN
            )
         );
      }

      var8.add(
         new PerformanceOverlay.Line(
            String.format(
               Locale.ROOT,
               "%d cores  system %.0f%%  heap %.1f of %.1f GB",
               var6.availableProcessors(),
               Utilization.system,
               (float)(var6.totalMemory() - var6.freeMemory()) / 1.0737418E9F,
               (float)var6.maxMemory() / 1.0737418E9F
            ),
            PLAIN
         )
      );
      var8.add(new PerformanceOverlay.Line(var3, var3.startsWith("held") ? GOOD : (var3.startsWith("nothing") ? POOR : FAIR)));
      return var8;
   }

   private static String compact(FrameStats var0, String var1) {
      return String.format(
         Locale.ROOT,
         "%d fps  %.2f ms  99%% %.1f ms  main %.0f%%  GPU %s  %s",
         var0.lastSecond(),
         var0.meanMs(),
         var0.p99Ms(),
         Utilization.game,
         gpuBusy(),
         var1
      );
   }

   private static String limit(int var0, int var1) {
      if (var1 > 0 && var0 >= var1 * 0.97F) {
         return "held by the cap";
      } else {
         float var2 = Float.isNaN(Utilization.gpu) ? 0.0F : Utilization.gpu;
         float var3 = Math.max(var2, Math.max(Utilization.game, Utilization.render));
         if (var3 < 90.0F) {
            return "nothing saturated: waiting or syncing";
         } else {
            boolean var4 = firstPerson(System.nanoTime());
            if (var3 == Utilization.game) {
               return var4
                  ? String.format(Locale.ROOT, "main thread limits it (first person %.1f ms of %.1f)", Profile.lastMainOursMs, Profile.lastMainMs)
                  : "main thread limits it";
            } else if (var3 == Utilization.render) {
               return var4 ? String.format(Locale.ROOT, "render thread limits it (first person %.1f ms)", Profile.lastRenderMs) : "render thread limits it";
            } else {
               return "GPU limits it";
            }
         }
      }
   }

   private static PerformanceOverlay.Tint rateTint(int var0, int var1) {
      float var2 = var1 > 0 ? (float)var0 / var1 : var0 / 144.0F;
      return var2 >= 0.97F ? GOOD : (var2 >= 0.5F ? FAIR : POOR);
   }

   private static String gpuBusy() {
      return Float.isNaN(Utilization.gpu) ? "?" : String.format(Locale.ROOT, "%.0f%%", Utilization.gpu);
   }

   private static boolean firstPerson(long var0) {
      return var0 - Profile.lastFrameAt <= 1000000000L;
   }

   private static int cap() {
      if (!PerformanceSettings.instance.isFramerateUncapped()) {
         return PerformanceSettings.getLockFPS();
      }

      try {
         DisplayMode var0 = Display.getDesktopDisplayMode();
         return var0 == null ? 0 : var0.getFrequency();
      } catch (RuntimeException var1) {
         return 0;
      }
   }

   private static void paint(PerformanceOverlay.Mode var0) {
      TextManager var1 = TextManager.instance;
      SpriteRenderer var2 = SpriteRenderer.instance;
      int var3 = var1.getFontHeight(FONT) + 2;
      int var4 = COLUMNS[var0.ordinal()];
      int var5 = GRAPH_HEIGHT[var0.ordinal()];
      int var6 = var4 * 2;

      for (PerformanceOverlay.Line var8 : lines) {
         var6 = Math.max(var6, var1.MeasureStringX(FONT, var8.text()));
      }

      int var14 = lines.size() * var3 + var5 + (lines.isEmpty() ? 0 : 6);
      int var15 = Core.getInstance().getScreenWidth() - 15;
      int var9 = var15 - var6 - 12;
      var2.renderi(null, var9, 40, var15 - var9, var14 + 12, 0.0F, 0.0F, 0.0F, 0.6F, null);
      int var10 = 46;

      for (PerformanceOverlay.Line var12 : lines) {
         PerformanceOverlay.Tint var13 = var12.tint();
         var1.DrawString(FONT, var15 - 6 - var1.MeasureStringX(FONT, var12.text()), var10, var12.text(), var13.r(), var13.g(), var13.b(), 1.0);
         var10 += var3;
      }

      var10 += lines.isEmpty() ? 0 : 6;
      graph(var2, var15 - 6 - var4 * 2, var10, var4, var5);
      if (var0 == PerformanceOverlay.Mode.GRAPH_ONLY && !graphLabel.isEmpty()) {
         var1.DrawString(FONT, var15 - 6 - var4 * 2 + 3, var10 + 2, graphLabel, graphTint.r(), graphTint.g(), graphTint.b(), 1.0);
      }
   }

   private static void graph(SpriteRenderer var0, int var1, int var2, int var3, int var4) {
      float var5 = var4 / (2.5F * budgetMs);
      long var6 = FrameTimes.count;
      int var8 = (int)Math.min(var3, Math.min(var6, 4064L));

      for (int var9 = 0; var9 < var8; var9++) {
         int var10 = (int)(var6 - var8 + var9 & 4095L);
         float var11 = FrameTimes.intervalMs[var10];
         float var12 = FrameTimes.gpuMs[var10];
         PerformanceOverlay.Tint var13 = var11 <= budgetMs * 1.05F ? GOOD : (var11 <= budgetMs * 2.0F ? FAIR : POOR);
         int var14 = Math.max(1, Math.min(var4, Math.round(var11 * var5)));
         int var15 = var1 + (var3 - var8 + var9) * 2;
         var0.renderi(null, var15, var2 + var4 - var14, 2, var14, var13.r(), var13.g(), var13.b(), 0.85F, null);
         if (var12 > 0.0F) {
            int var16 = Math.max(1, Math.min(var4, Math.round(var12 * var5)));
            var0.renderi(null, var15 + 2 - 1, var2 + var4 - var16, 1, var16, GPU.r(), GPU.g(), GPU.b(), 0.9F, null);
         }
      }

      int var17 = var2 + var4 - Math.round(budgetMs * var5);
      var0.renderi(null, var1, var17, var3 * 2, 1, 1.0F, 1.0F, 1.0F, 0.6F, null);
   }

   static {
      MODE.describe(
         "Frame times, how busy each thread and the GPU are, and what holds the frame rate back, in the top right corner: all of it, one line, the graph alone, or off."
      );
   }

   private record Line(String text, PerformanceOverlay.Tint tint) {
   }

   enum Mode {
      FULL,
      COMPACT,
      GRAPH_ONLY,
      OFF;
   }

   private record Tint(float r, float g, float b) {
   }
}
