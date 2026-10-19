package viewpoint.render;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Profile;
import viewpoint.platform.SettingsWindow;

final class RendererStart {
   static final int STEPS = 20;
   private static final int NOISE_STEP = 1;
   private static final int RAIN_STEP = 4;
   private static int done;
   private static long nanos;
   private static long slowest;
   private static int slowestStep;
   private static TreeBaker trees;
   private static FarShadow farShadow;
   private static BlurPass blur;
   private static VolumePass volume;
   private static volatile CompletableFuture<byte[]> noiseBake;
   private static volatile CompletableFuture<byte[]> rainBake;
   private static boolean baking;

   static synchronized void bakeAhead() {
      if (!baking && done <= 0) {
         baking = true;
         CompletableFuture var0 = new CompletableFuture();
         CompletableFuture var1 = new CompletableFuture();
         noiseBake = var0;
         rainBake = var1;
         Thread var2 = new Thread(() -> {
            bake(var0, Noise3D::values);
            bake(var1, RainTextures::layers);
         }, "Viewpoint renderer bake");
         var2.setDaemon(true);
         var2.start();
      }
   }

   private static void bake(CompletableFuture<byte[]> var0, Supplier<byte[]> var1) {
      try {
         var0.complete((byte[])var1.get());
      } catch (RuntimeException | Error var3) {
         var0.completeExceptionally(var3);
      }
   }

   static boolean run(long var0) {
      do {
         CompletableFuture var2 = done == 1 ? noiseBake : (done == 4 ? rainBake : null);
         if (var2 != null && !var2.isDone() && var0 != Long.MAX_VALUE) {
            return false;
         }

         long var3 = System.nanoTime();
         step(done);
         long var5 = System.nanoTime() - var3;
         nanos += var5;
         if (var5 > slowest) {
            slowest = var5;
            slowestStep = done;
         }

         done++;
      } while (done < 20 && System.nanoTime() < var0);

      if (done < 20) {
         return false;
      }

      trees = null;
      farShadow = null;
      blur = null;
      volume = null;
      System.out
         .printf(
            "[Viewpoint] renderer started in %.0f ms of the render thread (its slowest step, %d of %d: %.0f ms)%n",
            nanos / 1000000.0,
            slowestStep + 1,
            20,
            slowest / 1000000.0
         );
      return true;
   }

   static float share() {
      return done / 20.0F;
   }

   private static void step(int var0) {
      if (var0 <= 4) {
         fixed(var0);
      } else {
         switch (var0) {
            case 5:
               WorldRenderer.models = new ModelPass(WorldRenderer.targets);
               break;
            case 6:
               floors();
               break;
            case 7:
               trees = new TreeBaker();
               farShadow = new FarShadow(trees);
               WorldRenderer.weatherMap = new WeatherMap(WorldRenderer.targets.noise);
               WorldRenderer.shadows = new ShadowPass(WorldRenderer.models, farShadow, WorldRenderer.weatherMap);
               break;
            case 8:
               blur = new BlurPass();
               WorldRenderer.sky = new SkyPass(WorldRenderer.targets, WorldRenderer.weatherMap);
               break;
            case 9:
               WorldRenderer.surfaces = new SurfacePass(WorldRenderer.targets, WorldRenderer.shadows);
               break;
            case 10:
               WorldRenderer.translucent = new TranslucentPass(WorldRenderer.targets);
               break;
            case 11:
               WorldRenderer.indirect = new IndirectPass(WorldRenderer.targets, WorldRenderer.shadows, WorldRenderer.floors);
               break;
            case 12:
               WorldRenderer.weather = new WeatherPass(WorldRenderer.targets, WorldRenderer.shadows, WorldRenderer.weatherMap);
               break;
            case 13:
               volume = new VolumePass(WorldRenderer.targets, WorldRenderer.shadows, blur);
               break;
            case 14:
               WorldRenderer.post = new PostPass(WorldRenderer.targets);
               break;
            case 15:
               WorldRenderer.stages = new PackStages(WorldRenderer.targets, WorldRenderer.weatherMap);
               break;
            case 16:
               WorldRenderer.temporal = new TemporalPass(WorldRenderer.targets);
               WorldRenderer.rescale = new Rescale();
               WorldRenderer.end = new FrameEnd(
                  WorldRenderer.targets,
                  volume,
                  WorldRenderer.post,
                  WorldRenderer.stages,
                  WorldRenderer.temporal,
                  WorldRenderer.rescale,
                  WorldRenderer.weather,
                  WorldRenderer.target
               );
               break;
            case 17:
               WorldRenderer.far = new FarPass(WorldRenderer.targets, WorldRenderer.shadows, farShadow, trees);
               break;
            case 18:
               GlProgram.linkPasses();
               break;
            default:
               PackLinks.start();
         }
      }
   }

   private static void fixed(int var0) {
      Targets var1 = WorldRenderer.targets;
      switch (var0) {
         case 0:
            Gl.init();
            Meshes.init();
            WorldRenderer.targets = new Targets();
            WorldRenderer.targets.createMasks();
            break;
         case 1:
            var1.noise = Noise3D.upload(baked(noiseBake, Noise3D::values));
            noiseBake = null;
            break;
         case 2:
            var1.createSky();
            break;
         case 3:
            var1.lampFields = new LampFields();
            break;
         default:
            var1.air = new AirLuts();
            var1.rainTextures = new RainTextures(baked(rainBake, RainTextures::layers));
            rainBake = null;
      }
   }

   private static byte[] baked(CompletableFuture<byte[]> var0, Supplier<byte[]> var1) {
      return var0 == null ? (byte[])var1.get() : (byte[])var0.join();
   }

   private static void floors() {
      WorldRenderer.target = new TargetOutline();
      WorldRenderer.floors = new FloorBakes();
      Meshes.floors = WorldRenderer.floors;
      Profile.floorsReport = WorldRenderer.floors::report;
      Profile.packsReport = PackModels::report;
      SettingsWindow.panel("World/Floor preview", new FloorPreview(WorldRenderer.floors)::draw);
      WorldRenderer.ground = new ShellGround();
      Profile.shellGroundReport = WorldRenderer.ground::report;
      SettingsWindow.panel("World/Levels of detail", WorldRenderer.ground::drawFigures);
   }

   private RendererStart() {
   }
}
