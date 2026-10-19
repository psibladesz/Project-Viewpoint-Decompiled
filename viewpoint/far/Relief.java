package viewpoint.far;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import viewpoint.core.Frame;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Pool;
import viewpoint.platform.Tuning;
import viewpoint.render.FarRelief;
import viewpoint.render.SceneData;
import zombie.ZomboidFileSystem;
import zombie.iso.IsoCamera;
import zombie.iso.IsoLot;
import zombie.iso.IsoWorld;
import zombie.iso.MapFiles;

public final class Relief {
   static final String MAP = "Muldraugh, KY";
   private static final String KEPT = "viewpoint-relief";
   private static final LiveSettings.Toggle ON = LiveSettings.toggle("lod.reliefEnabled", "Terrain relief", "World/Levels of detail", true);
   private static String map;
   private static boolean started;
   private static FarRelief.Lift current;
   private static volatile ReliefData loaded;
   private static volatile boolean building;
   private static volatile boolean reading;
   private static volatile int version;

   private Relief() {
   }

   static void want(String var0) {
      if (map == null && var0 != null && new File(var0).getName().equals("Muldraugh, KY")) {
         map = var0;
      }
   }

   private static void loaded(ReliefData var0) {
      long var1 = var0.grid.bytes();
      long var3 = Pool.RELIEF.cap();
      if (var1 > var3) {
         throw new IllegalStateException(
            String.format("its field takes %d MiB, over the %d MiB cap of its pool (World, Memory: terrain relief)", var1 >> 20, var3 >> 20)
         );
      }

      loaded = var0;
      FarRelief.walled(var0.grid);
      version++;
   }

   static ReliefData data() {
      return loaded;
   }

   static int version() {
      return version;
   }

   static long bytes() {
      ReliefData var0 = loaded;
      return var0 == null ? 0L : var0.grid.bytes();
   }

   public static void frame(Frame var0) {
      SceneData var1 = var0.scene;
      ReliefData var2 = loaded;
      current = null;
      Pool.RELIEF.use(bytes());
      FarRelief.on(ON.get() && FarWorld.reach() > 0.0F);
      if (ON.get() && !(FarWorld.reach() <= 0.0F)) {
         String var3 = map;
         start();
         buttons(var3);
         if (var2 != null) {
            float var4 = ReliefSettings.HEIGHT.get();
            float var5 = var4 * Tuning.reliefSquaresPerMetre / 2.4494896F;
            current = new FarRelief.Lift(var2.grid, var5, var2.at(var0.camX, var0.camY), var0.camX, var0.camY, bands(var2.rules, var4));
            var1.relief(current);
         }
      }
   }

   public static void prepare() {
      if (!started && ON.get() && !(FarWorld.reach() <= 0.0F) && IsoWorld.instance.currentCell != null) {
         for (MapFiles var1 : IsoLot.MapFiles) {
            want(var1.mapDirectoryAbsolutePath);
         }

         start();
      }
   }

   private static void start() {
      String var0 = map;
      if (!started && var0 != null) {
         started = true;
         reading = true;
         List var1 = headers();
         File var2 = kept();
         work(() -> {
            try {
               loaded(ReliefData.read(var0, var1, var2));
            } finally {
               reading = false;
            }
         });
      }
   }

   public static boolean reading() {
      return reading;
   }

   private static float[] bands(ReliefRules var0, float var1) {
      float var2 = var1 / var0.height();
      return new float[]{var0.crossSlope() * var2, var0.grade() * var2, var0.driveway() * var2, var0.bank() * var2};
   }

   private static void buttons(String var0) {
      boolean var1 = ReliefSettings.REBAKE.take();
      boolean var2 = ReliefSettings.SHIPPED.take();
      if (var0 != null && !building && (var1 || var2)) {
         building = true;
         ReliefRules var3 = ReliefSettings.rules();
         List var4 = headers();
         File var5 = kept();
         if (var2) {
            ReliefSettings.reset();
         }

         ReliefSettings.REBAKE.status(var2 ? "back to the build's..." : "rebaking...");
         long var6 = System.nanoTime();
         work(() -> {
            try {
               if (var2) {
                  ReliefData.forgetRebake(var5);
                  loaded(ReliefData.read(var0, var4, var5));
               } else {
                  loaded(ReliefData.rebake(var0, var4, var3, var5));
               }

               ReliefSettings.REBAKE.status(String.format("%s in %.1f s", var2 ? "the build's" : "rebaked", (System.nanoTime() - var6) / 1.0E9));
            } catch (Exception | LinkageError var11) {
               ReliefSettings.REBAKE.status("failed: " + var11);
               throw var11;
            } finally {
               building = false;
            }
         });
      }
   }

   static File kept() {
      return new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-relief");
   }

   private static List<File> headers() {
      LinkedHashMap var0 = new LinkedHashMap();

      for (MapFiles var2 : IsoLot.MapFiles) {
         for (Entry var4 : var2.infoFileNames.entrySet()) {
            if (((String)var4.getKey()).endsWith(".lotheader")) {
               var0.putIfAbsent((String)var4.getKey(), new File((String)var4.getValue()));
            }
         }
      }

      return new ArrayList<>(var0.values());
   }

   public static String mapImage() {
      File var0 = ReliefPyramid.file();
      return var0 == null ? null : var0.getAbsolutePath();
   }

   public static int mapAlpha() {
      return 170;
   }

   public static FarRelief.Lift current() {
      return current;
   }

   static boolean coarseShows(SceneData var0, int var1, int var2, float var3) {
      ReliefData var4 = loaded;
      FarRelief.Lift var5 = var0.relief;
      if (var4 != null && var5 != null) {
         float var6 = var4.blockError(var1, var2) * var5.levels() * 2.4494896F;
         float var7 = var0.projection.m11() * IsoCamera.getScreenHeight(0) * 0.5F;
         return var6 > 0.0F && var6 / Math.max(var3, 1.0F) * var7 > ReliefSettings.LOD_PIXELS.get();
      } else {
         return false;
      }
   }

   static float top(SceneData var0, int var1, int var2) {
      return topOf(loaded, var0, var1, var2);
   }

   static float low(SceneData var0, int var1, int var2) {
      return lowOf(loaded, var0, var1, var2);
   }

   static float topOf(ReliefData var0, SceneData var1, int var2, int var3) {
      FarRelief.Lift var4 = var1.relief;
      return var0 != null && var4 != null && !Float.isNaN(var0.top(var2, var3)) ? (var0.top(var2, var3) - var4.ground()) * var4.levels() : 0.0F;
   }

   static float lowOf(ReliefData var0, SceneData var1, int var2, int var3) {
      FarRelief.Lift var4 = var1.relief;
      return var0 != null && var4 != null && !Float.isNaN(var0.low(var2, var3)) ? Math.min(0.0F, var0.low(var2, var3) - var4.ground()) * var4.levels() : 0.0F;
   }

   static void work(Relief.Job var0) {
      FarWorkers.jobs.incrementAndGet();
      FarWorkers.POOL.execute(() -> {
         try {
            var0.run();
         } catch (Exception | LinkageError var5) {
            System.out.println("[Viewpoint] far world: terrain relief left as it was after an error: " + var5);
            var5.printStackTrace(System.out);
         } finally {
            FarWorkers.jobs.decrementAndGet();
         }
      });
   }

   static {
      ON.describe("The ground rising and falling as the real Kentucky does, the hills on the horizon where they stand.");
      Pool.RELIEF.alsoIn("World/Levels of detail");
      Pool.RELIEF
         .describe(
            "The terrain relief's field, held whole: about 96 MiB for the game's own map. A field over the cap is not read; lowered, it holds from the next read (a new game or a rebake)."
         );
   }

   interface Job {
      void run() throws Exception;
   }
}
