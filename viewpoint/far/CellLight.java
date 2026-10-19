package viewpoint.far;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import viewpoint.core.Frame;
import viewpoint.light.CellLightPage;
import viewpoint.light.CellLights;
import viewpoint.platform.LongMap;
import viewpoint.render.CellLightPages;
import zombie.GameTime;
import zombie.SandboxOptions;
import zombie.iso.IsoWorld;

public final class CellLight {
   private static final float REACH = 1024.0F;
   private static final float HOLD = 128.0F;
   private static final long DECIDE_NANOS = 1000000000L;
   private static final int JOBS = 2;
   private static final long BUDGET_NANOS = 500000L;
   private static final LongMap<CellLight.Cell> cells = new LongMap<>(128);
   private static final ArrayList<CellLight.Cell> wanted = new ArrayList<>();
   private static int running;
   private static long frames;
   private static long decided;
   private static boolean broken;
   private static boolean grid;
   private static boolean lampGrid;
   private static boolean night;
   private static final ConcurrentLinkedQueue<CellLight.Done> DONE = new ConcurrentLinkedQueue<>();

   public static void update(Frame var0) {
      if (!broken) {
         try {
            updateCells(var0);
         } catch (RuntimeException var2) {
            broken = true;
            System.out.println("[Viewpoint] far world's light pages off after an error: " + var2);
            var2.printStackTrace(System.out);
            clear();
         }
      }
   }

   private static void updateCells(Frame var0) {
      if (FarWorld.reach() <= 0.0F) {
         if (!cells.isEmpty()) {
            clear();
         }
      } else {
         frames++;
         takeIn();
         visit(var0.camX, var0.camY);
         long var1 = System.nanoTime();
         long var3 = var1 + 500000L;
         if (turned() || var1 - decided > 1000000000L) {
            decided = var1;
            decide();
         }

         for (CellLight.Cell var6 : wanted) {
            if (running >= 2) {
               break;
            }

            if (var6.lights == null && !var6.scanning) {
               scan(var6, var3);
            } else if (var6.next != null && var6.making == null) {
               page(var6);
            }
         }

         letGo();
      }
   }

   private static boolean turned() {
      boolean var0 = IsoWorld.instance.isHydroPowerOn();
      boolean var1 = SandboxOptions.instance.doesPowerGridExist();
      boolean var2 = GameTime.getInstance().getNight() >= 0.5F;
      boolean var3 = var0 != grid || var1 != lampGrid || var2 != night;
      grid = var0;
      lampGrid = var1;
      night = var2;
      return var3;
   }

   private static void decide() {
      CellLights.Cell[] var0 = new CellLights.Cell[9];

      for (CellLight.Cell var2 : wanted) {
         if (var2.lights != null) {
            for (int var3 = -1; var3 <= 1; var3++) {
               for (int var4 = -1; var4 <= 1; var4++) {
                  CellLight.Cell var5 = cells.get(FarWorld.key(var2.cellX + var4, var2.cellY + var3));
                  var0[(var3 + 1) * 3 + var4 + 1] = var5 == null ? null : var5.lights;
               }
            }

            CellLights.Inputs var6 = CellLights.decide(var2.lights, var0);
            boolean var7 = var2.making != null ? var6.same(var2.making) : var6.same(var2.made);
            var2.next = var7 ? null : var6;
         }
      }
   }

   private static void visit(float var0, float var1) {
      short var2 = 256;
      int var3 = Math.floorDiv((int)Math.floor(var0), var2);
      int var4 = Math.floorDiv((int)Math.floor(var1), var2);
      int var5 = (int)Math.ceil(1152.0F / var2);
      wanted.clear();

      for (int var6 = var4 - var5; var6 <= var4 + var5; var6++) {
         for (int var7 = var3 - var5; var7 <= var3 + var5; var7++) {
            float var8 = Math.max(0.0F, Math.max(var7 * var2 - var0, var0 - (var7 + 1) * var2));
            float var9 = Math.max(0.0F, Math.max(var6 * var2 - var1, var1 - (var6 + 1) * var2));
            float var10 = (float)Math.sqrt(var8 * var8 + var9 * var9);
            CellLight.Cell var11 = cells.get(FarWorld.key(var7, var6));
            if ((var11 != null || !(var10 > 1024.0F)) && (var11 == null || !(var10 > 1152.0F))) {
               if (var11 == null) {
                  var11 = new CellLight.Cell(var7, var6);
                  cells.put(FarWorld.key(var7, var6), var11);
               }

               var11.seen = frames;
               var11.distance = var10;
               wanted.add(var11);
            }
         }
      }

      wanted.sort((var0x, var1x) -> Float.compare(var0x.distance, var1x.distance));
   }

   private static void scan(CellLight.Cell var0, long var1) {
      FarWorld.Slot var3 = FarWorld.slots.get(FarWorld.key(var0.cellX, var0.cellY));
      if (var3 != null && var3.legend != null && var3.lotpack != null) {
         long[] var4 = BlockLight.table(var3.legend, var1);
         if (var4 != null) {
            String var5 = var3.lotpack;
            byte[] var6 = Lotpacks.get(var5);
            int var7 = var3.minLevel;
            int var8 = var3.maxLevel;
            int var9 = var0.cellX;
            int var10 = var0.cellY;
            var0.scanning = true;
            running++;
            FarWorkers.jobs.incrementAndGet();
            FarWorkers.POOL.execute(() -> {
               int[] var8x = new int[0];

               try {
                  int[][] var9x = new int[][]{new int[64]};
                  int[] var10x = new int[]{0};
                  int var11 = var9 * 256;
                  int var12 = var10 * 256;
                  ShellMesher.read(Lotpacks.read(var5, var6), var7, var8, 0, 0, 32, (var5xx, var6xx, var7xx, var8xx) -> {
                     int var9xx = var5xx < var4.length ? (int)(var4[var5xx] >>> 32) : 0;
                     if (var9xx != 0) {
                        if (var10x[0] + 4 > var9x[0].length) {
                           var9x[0] = Arrays.copyOf(var9x[0], var9x[0].length * 2);
                        }

                        int var10xx = var10x[0];
                        var9x[0][var10xx] = var11 + var6xx;
                        var9x[0][var10xx + 1] = var12 + var7xx;
                        var9x[0][var10xx + 2] = var8xx;
                        var9x[0][var10xx + 3] = var9xx;
                        var10x[0] += 4;
                     }
                  });
                  var8x = Arrays.copyOf(var9x[0], var10x[0]);
               } catch (Throwable var16) {
                  System.out.println("[Viewpoint] far world: the lamps of cell " + var9 + "," + var10 + " failed: " + var16);
               } finally {
                  DONE.add(new CellLight.Done(var0, var8x, null, null, null));
                  FarWorkers.jobs.decrementAndGet();
               }
            });
         }
      }
   }

   private static void page(CellLight.Cell var0) {
      CellLights.Inputs var1 = var0.next;
      int var2 = var0.cellX;
      int var3 = var0.cellY;
      var0.next = null;
      var0.making = var1;
      running++;
      FarWorkers.jobs.incrementAndGet();
      FarWorkers.POOL.execute(() -> {
         byte[] var4 = null;
         int[] var5 = null;

         try {
            var4 = CellLightPage.build(var2, var3, var1.lamps(), var1.rooms());
            var5 = CellLightPage.cards(var2, var3, var1.lamps());
         } catch (Throwable var10) {
            System.out.println("[Viewpoint] far world: the light page of cell " + var2 + "," + var3 + " failed: " + var10);
         } finally {
            DONE.add(new CellLight.Done(var0, null, var1, var4, var5));
            FarWorkers.jobs.decrementAndGet();
         }
      });
   }

   private static void takeIn() {
      CellLight.Done var0;
      while ((var0 = DONE.poll()) != null) {
         running--;
         CellLight.Cell var1 = var0.cell;
         if (cells.get(FarWorld.key(var1.cellX, var1.cellY)) == var1) {
            if (var0.lamps != null) {
               var1.scanning = false;
               var1.lights = CellLights.cell(var1.cellX, var1.cellY, var0.lamps);
               decided = 0L;
            } else {
               var1.making = null;
               var1.made = var0.inputs;
               if (var0.page != null) {
                  CellLightPages.show(var1.cellX, var1.cellY, var0.page, var0.cards);
               }
            }
         }
      }
   }

   private static void letGo() {
      Iterator var0 = cells.values().iterator();

      while (var0.hasNext()) {
         CellLight.Cell var1 = (CellLight.Cell)var0.next();
         if (var1.seen != frames) {
            CellLightPages.hide(var1.cellX, var1.cellY);
            var0.remove();
         }
      }
   }

   public static void clear() {
      cells.clear();
      CellLightPages.clear();
   }

   private CellLight() {
   }

   private static final class Cell {
      final int cellX;
      final int cellY;
      CellLights.Cell lights;
      CellLights.Inputs made;
      CellLights.Inputs making;
      CellLights.Inputs next;
      boolean scanning;
      long seen;
      float distance;

      Cell(int var1, int var2) {
         this.cellX = var1;
         this.cellY = var2;
      }
   }

   private record Done(CellLight.Cell cell, int[] lamps, CellLights.Inputs inputs, byte[] page, int[] cards) {
   }
}
