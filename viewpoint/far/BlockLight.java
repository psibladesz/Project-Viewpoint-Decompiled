package viewpoint.far;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import viewpoint.core.Frame;
import viewpoint.light.MapLayout;
import viewpoint.light.MapLight;
import viewpoint.light.MapSquares;
import viewpoint.light.TileLight;
import viewpoint.platform.Caches;
import viewpoint.platform.LongMap;
import zombie.iso.IsoLot;
import zombie.iso.IsoWorld;
import zombie.iso.MapFiles;
import zombie.iso.RoomDef;

public final class BlockLight implements Caches.Cache {
   private static final float REACH = 293.0F;
   private static final float SQUARES_REACH = 389.0F;
   private static final float HOLD = 32.0F;
   private static final int JOBS = 2;
   private static final long BUDGET_NANOS = 1000000L;
   private static final LongMap<BlockLight.Block> blocks = new LongMap<>(256);
   private static final ArrayList<BlockLight.Block> wanted = new ArrayList<>();
   private static final IdentityHashMap<BiomeTrees.Legend, long[]> tables = new IdentityHashMap<>();
   private static final IdentityHashMap<BiomeTrees.Legend, Integer> filled = new IdentityHashMap<>();
   private static final LongMap<Boolean> mapLacks = new LongMap<>(64);
   private static int running;
   private static int reading;
   private static int generation = -1;
   private static long frames;
   private static boolean broken;
   private static final ConcurrentLinkedQueue<BlockLight.Done> DONE = new ConcurrentLinkedQueue<>();

   public static void update(Frame var0) {
      if (!broken) {
         try {
            updateBlocks(var0);
         } catch (RuntimeException var2) {
            broken = true;
            System.out.println("[Viewpoint] far world's light off after an error: " + var2);
            var2.printStackTrace(System.out);
            clear();
         }
      }
   }

   private static void updateBlocks(Frame var0) {
      if (FarWorld.reach() <= 0.0F) {
         if (!blocks.isEmpty()) {
            clear();
         }
      } else {
         frames++;
         if (MapLight.generation() != generation) {
            generation = MapLight.generation();

            for (BlockLight.Block var2 : blocks.values()) {
               var2.installed = false;
            }
         }

         takeIn();
         long var3 = System.nanoTime() + 1000000L;
         visit(var0.camX, var0.camY);
         startJobs(var3);
         install(var3);
         letGo();
         MapLight.update(IsoWorld.instance.getCell());
      }
   }

   private static void visit(float var0, float var1) {
      byte var2 = 64;
      int var3 = Math.floorDiv((int)Math.floor(var0), var2);
      int var4 = Math.floorDiv((int)Math.floor(var1), var2);
      int var5 = (int)Math.ceil(421.0F / var2);
      wanted.clear();

      for (int var6 = var4 - var5; var6 <= var4 + var5; var6++) {
         for (int var7 = var3 - var5; var7 <= var3 + var5; var7++) {
            float var8 = var7 * var2 - var0;
            float var9 = var8 + var2;
            float var10 = var6 * var2 - var1;
            float var11 = var10 + var2;
            float var12 = Math.max(0.0F, Math.max(var8, -var9));
            float var13 = Math.max(0.0F, Math.max(var10, -var11));
            float var14 = (float)Math.sqrt(var12 * var12 + var13 * var13);
            BlockLight.Block var15 = blocks.get(FarWorld.key(var7, var6));
            if ((var15 != null || !(var14 > 389.0F)) && (var15 == null || !(var14 > 421.0F))) {
               if (var15 == null) {
                  var15 = new BlockLight.Block(var7, var6);
                  blocks.put(FarWorld.key(var7, var6), var15);
               }

               var15.seen = frames;
               var15.distance = var14;
               wanted.add(var15);
            }
         }
      }

      wanted.sort((var0x, var1x) -> Float.compare(var0x.distance, var1x.distance));
   }

   private static void startJobs(long var0) {
      for (BlockLight.Block var3 : wanted) {
         if (running >= 2 || System.nanoTime() > var0) {
            return;
         }

         if (var3.squares == null && !var3.squaresBusy) {
            squares(var3, var0);
         } else if (var3.layout == null && !var3.layoutBusy && var3.squares != null && var3.distance <= 293.0F && neighbours(var3) != null) {
            layout(var3);
         }
      }
   }

   private static void squares(BlockLight.Block var0, long var1) {
      byte var3 = 4;
      int var4 = Math.floorDiv(var0.bx, var3);
      int var5 = Math.floorDiv(var0.by, var3);
      FarWorld.Slot var6 = FarWorld.slots.get(FarWorld.key(var4, var5));
      if (var6 != null && var6.legend != null && var6.lotpack != null) {
         long[] var7 = table(var6.legend, var1);
         if (var7 != null) {
            String var8 = var6.lotpack;
            byte[] var9 = Lotpacks.get(var8);
            if (var9 != null || reading <= 0) {
               boolean var10 = var9 == null;
               reading += var10 ? 1 : 0;
               int var11 = var6.minLevel;
               int var12 = var6.maxLevel;
               int var13 = var0.bx;
               int var14 = var0.by;
               int var15 = Math.max(0, Math.min(var12, 31)) + 1;
               var0.squaresBusy = true;
               running++;
               FarWorkers.jobs.incrementAndGet();
               FarWorkers.POOL
                  .execute(
                     () -> {
                        MapSquares var13x = null;

                        try {
                           MapSquares var14x = new MapSquares(var13, var14, var15);
                           int var15x = var4 * 256;
                           int var16 = var5 * 256;
                           byte var17 = 8;
                           ShellMesher.read(
                              Lotpacks.read(var8, var9),
                              var11,
                              var12,
                              (var13 - var4 * var3) * var17,
                              (var14 - var5 * var3) * var17,
                              var17,
                              (var4xx, var5xx, var6xx, var7xx) -> var14x.tile(var15x + var5xx, var16 + var6xx, var7xx, var4xx < var7.length ? var7[var4xx] : 0L)
                           );
                           var14x.finish();
                           var13x = var14x;
                        } catch (Throwable var21) {
                           System.out.println("[Viewpoint] far world: the light of block " + var13 + "," + var14 + " failed: " + var21);
                           var13x = new MapSquares(var13, var14, 1);
                           var13x.finish();
                        } finally {
                           DONE.add(new BlockLight.Done(var0, false, var10, var13x, null));
                           FarWorkers.jobs.decrementAndGet();
                        }
                     }
                  );
            }
         }
      }
   }

   private static void layout(BlockLight.Block var0) {
      MapSquares[] var1 = neighbours(var0);
      BlockRooms var2 = BlockRooms.of(var0.bx, var0.by, var0.squares.levels());
      long[][] var3 = var2.ids;
      var0.lit = var2.lit;
      var0.layoutBusy = true;
      running++;
      FarWorkers.jobs.incrementAndGet();
      FarWorkers.POOL.execute(() -> {
         MapLayout.Block var3x = null;

         try {
            var3x = MapLayout.build(var1, var3);
         } catch (Throwable var8) {
            System.out.println("[Viewpoint] far world: the layouts of block " + var0.bx + "," + var0.by + " failed: " + var8);
         } finally {
            DONE.add(new BlockLight.Done(var0, true, false, null, var3x));
            FarWorkers.jobs.decrementAndGet();
         }
      });
   }

   private static MapSquares[] neighbours(BlockLight.Block var0) {
      MapSquares[] var1 = new MapSquares[9];
      byte var2 = 4;

      for (int var3 = -1; var3 <= 1; var3++) {
         for (int var4 = -1; var4 <= 1; var4++) {
            BlockLight.Block var5 = blocks.get(FarWorld.key(var0.bx + var4, var0.by + var3));
            if (var5 != null && var5.squares != null) {
               var1[(var3 + 1) * 3 + var4 + 1] = var5.squares;
            } else if (!mapLacks(Math.floorDiv(var0.bx + var4, var2), Math.floorDiv(var0.by + var3, var2))) {
               return null;
            }
         }
      }

      return var1;
   }

   private static boolean mapLacks(int var0, int var1) {
      long var2 = FarWorld.key(var0, var1);
      Boolean var4 = mapLacks.get(var2);
      if (var4 == null) {
         String var5 = "world_" + var0 + "_" + var1 + ".lotpack";
         var4 = Boolean.TRUE;

         for (MapFiles var7 : IsoLot.MapFiles) {
            var4 = var4 && !var7.infoFileNames.containsKey(var5);
         }

         mapLacks.put(var2, var4);
      }

      return var4;
   }

   private static void takeIn() {
      BlockLight.Done var0;
      while ((var0 = DONE.poll()) != null) {
         running--;
         reading = reading - (var0.read ? 1 : 0);
         BlockLight.Block var1 = var0.block;
         if (blocks.get(FarWorld.key(var1.bx, var1.by)) == var1) {
            if (var0.layoutJob) {
               var1.layoutBusy = var0.layout == null;
               var1.layout = var0.layout;
            } else {
               var1.squaresBusy = false;
               var1.squares = var0.squares;
            }
         }
      }
   }

   private static void install(long var0) {
      for (BlockLight.Block var3 : wanted) {
         if (System.nanoTime() > var0) {
            return;
         }

         if (var3.layout != null && !var3.installed) {
            MapLight.install(var3.layout, var3.lit);
            var3.installed = true;
         }
      }
   }

   private static void letGo() {
      Iterator var0 = blocks.values().iterator();

      while (var0.hasNext()) {
         BlockLight.Block var1 = (BlockLight.Block)var0.next();
         if (var1.seen != frames) {
            if (var1.installed) {
               MapLight.drop(var1.bx, var1.by);
            }

            var0.remove();
         }
      }
   }

   static long[] table(BiomeTrees.Legend var0, long var1) {
      long[] var3 = tables.get(var0);
      Integer var4 = filled.get(var0);
      if (var3 != null && var4 == null) {
         return var3;
      }

      String[] var5 = var0.names();
      if (var3 == null) {
         var3 = new long[var5.length];
         tables.put(var0, var3);
      }

      int var6 = var4 == null ? 0 : var4;

      while (var6 < var3.length) {
         var3[var6] = TileLight.of(var5[var6]);
         var6++;
         if ((var6 & 31) == 0 && var6 < var3.length && System.nanoTime() > var1) {
            filled.put(var0, var6);
            return null;
         }
      }

      filled.remove(var0);
      return var3;
   }

   public static void clear() {
      MapLight.off();

      for (BlockLight.Block var1 : blocks.values()) {
         if (var1.installed) {
            MapLight.drop(var1.bx, var1.by);
         }
      }

      blocks.clear();
      tables.clear();
      filled.clear();
      mapLacks.clear();
      TileLight.forget();
   }

   @Override
   public long bytes() {
      long var1 = 0L;

      for (BlockLight.Block var4 : blocks.values()) {
         var1 += var4.squares == null ? 0L : var4.squares.bytes();
      }

      return var1;
   }

   @Override
   public void list(Caches.Survey var1) {
   }

   @Override
   public void evictBefore(long var1) {
   }

   private BlockLight() {
   }

   static {
      Caches.register(new BlockLight());
   }

   private static final class Block {
      final int bx;
      final int by;
      MapSquares squares;
      MapLayout.Block layout;
      ArrayList<RoomDef> lit;
      boolean squaresBusy;
      boolean layoutBusy;
      boolean installed;
      long seen;
      float distance;

      Block(int var1, int var2) {
         this.bx = var1;
         this.by = var2;
      }
   }

   private record Done(BlockLight.Block block, boolean layoutJob, boolean read, MapSquares squares, MapLayout.Block layout) {
   }
}
