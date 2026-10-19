package viewpoint.visibility;

import viewpoint.core.Frame;
import viewpoint.platform.Keys;
import viewpoint.platform.Profile;
import viewpoint.platform.Settings;
import viewpoint.render.ChunkMeshData;
import viewpoint.render.SceneData;
import zombie.iso.IsoChunk;

public final class Rooms {
   public static final int ON = 0;
   public static final int FROZEN = 1;
   public static final int OFF = 2;
   private static final String[] MODES = new String[]{"on", "frozen", "off"};
   private static final float MARGIN = (float)Math.toRadians(25.0);
   private static final int MAX_TESTED_SQUARES = 64;
   private static int mode = 0;
   private static final Topology topology = new Topology();
   private static final Selection selection = new Selection();
   private static final Portals.View view = new Portals.View();
   private static SectorGraph graph;
   private static boolean[] hidden = new boolean[0];
   private static byte[] doors = new byte[0];
   private static final int[] viewers = new int[2];
   private static boolean hiding;
   private static boolean captureFailed;
   private static volatile String state = "off";
   private static volatile long selectNanos;
   private static volatile long hiddenSum;
   private static volatile long culledSum;
   private static volatile long modelsSum;
   private static volatile long madeSum;
   private static volatile long keptSum;
   private static volatile long frames;
   private static int modelsLeftOut;
   private static volatile int sectors;
   private static volatile int hideable;

   public static void captured(IsoChunk var0, int var1) {
      if (Settings.rooms) {
         try {
            topology.capture(var0, var1);
         } catch (RuntimeException var3) {
            if (!captureFailed) {
               captureFailed = true;
               System.out.println("[Viewpoint] rooms: a level's layout could not be read (it hides nothing):");
               var3.printStackTrace(System.out);
            }
         }
      }
   }

   public static void forget(int var0, int var1) {
      topology.forget(var0, var1);
   }

   public static void clear() {
      topology.clear();
      DrawPlans.clear();
      Apertures.clear();
      graph = null;
      hiding = false;
   }

   public static void cycle() {
      mode = (mode + 1) % MODES.length;
      System.out.println("[Viewpoint] rooms' culling " + MODES[mode]);
   }

   public static void select(Frame var0) {
      long var1 = System.nanoTime();
      DrawPlans.plansKept = 0L;
      DrawPlans.plansMade = 0L;
      DrawPlans.culledVertices = 0L;
      if (!Settings.rooms) {
         hiding = false;
         state = "off";
      } else {
         topology.update();
         if (mode == 1 && hiding) {
            state = "frozen";
         } else if (mode == 2) {
            hiding = false;
            state = "off (" + Keys.ROOMS.display() + ")";
         } else {
            hiding = choose(var0);
         }

         selectNanos = selectNanos + (System.nanoTime() - var1);
         frames++;
      }
   }

   private static boolean choose(Frame var0) {
      SectorGraph var1 = topology.graph();
      if (var1 == null) {
         state = topology.levels() == 0 ? "no levels" : "compiling";
         return false;
      }

      look(var0);
      viewers[0] = sector(var1, var0.camX, var0.camY, (int)Math.floor(var0.camZ + 0.01F));
      viewers[1] = sector(var1, view.x, view.y, (int)Math.floor(view.h / 2.4494896F));
      if (viewers[0] < 0 && viewers[1] < 0) {
         state = "eye in no sector";
         return false;
      }

      if (hidden.length < var1.sectors) {
         hidden = new boolean[var1.sectors + var1.sectors / 2];
      }

      if (doors.length < var1.doorLeaf.length) {
         doors = new byte[var1.doorLeaf.length + var1.doorLeaf.length / 2];
      }

      Apertures.resolve(var1, topology, doors, var0.scene, view);
      if (!selection.select(var1, doors, viewers, view, hidden)) {
         state = "budget out";
         return false;
      }

      graph = var1;
      state = selection.narrowed ? "on" : "on, portal budget out";
      sectors = var1.sectors;
      byte var2 = 0;
      byte var3 = 0;

      for (int var4 = 0; var4 < var1.sectors; var4++) {
         var2 += hidden[var4] ? 1 : 0;
         var3 += var1.hideable[var4] ? 1 : 0;
      }

      hiddenSum += var2;
      hideable = var3;
      return true;
   }

   private static int sector(SectorGraph var0, float var1, float var2, int var3) {
      int var4 = (int)Math.floor(var1);
      int var5 = (int)Math.floor(var2);
      long var6 = Fragment.key(Math.floorDiv(var4, 8), Math.floorDiv(var5, 8), var3);
      return var0.sector(var6, Math.floorMod(var5, 8) * 8 + Math.floorMod(var4, 8));
   }

   private static void look(Frame var0) {
      view.x = var0.camX - var0.scene.viewerX;
      view.y = var0.camY - var0.scene.viewerZ;
      view.h = var0.camZ * 2.4494896F + var0.scene.viewerY;
      view.relief = var0.scene.relief;
      view.look(var0.viewYaw, var0.viewPitch);
      view.tanX = widen(1.0F / var0.scene.projection.m00());
      view.tanY = widen(1.0F / var0.scene.projection.m11());
   }

   private static float widen(float var0) {
      return (float)Math.tan(Math.min(Math.atan(var0) + MARGIN, Math.toRadians(85.0)));
   }

   public static boolean hides(float var0, float var1, float var2, float var3, int var4, int var5) {
      int var6 = (int)Math.floor(var0);
      int var7 = (int)Math.floor(var1);
      int var8 = (int)Math.floor(var2);
      int var9 = (int)Math.floor(var3);
      if (hiding && (long)(var8 - var6 + 1) * (var9 - var7 + 1) * (var5 - var4 + 1) <= 64L) {
         for (int var10 = var4; var10 <= var5; var10++) {
            for (int var11 = var7; var11 <= var9; var11++) {
               for (int var12 = var6; var12 <= var8; var12++) {
                  int var13 = sector(graph, var12, var11, var10);
                  if (var13 < 0 || var13 >= hidden.length || !hidden[var13]) {
                     return false;
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static void leftOut() {
      modelsLeftOut++;
   }

   public static void plan(SceneData var0, int var1, ChunkMeshData var2, int var3, int var4) {
      if (hiding) {
         DrawPlans.plan(var0, var1, var2, var3, var4, graph, hidden);
      }
   }

   public static void sweep() {
      DrawPlans.sweep();
   }

   public static void endFrame() {
      culledSum = culledSum + DrawPlans.culledVertices;
      madeSum = madeSum + DrawPlans.plansMade;
      keptSum = keptSum + DrawPlans.plansKept;
      modelsSum = modelsSum + modelsLeftOut;
      modelsLeftOut = 0;
   }

   private static String report() {
      long var0 = Math.max(frames, 1L);
      String var2 = String.format(
         "%s; levels %d, sectors %d (%d may hide), hidden %.0f, vertices left out %.0fk, models left out %.0f, plans made %.1f kept %.1f, windows closed %d of %d, select %.2f ms, compile %.1f ms",
         state,
         topology.levels(),
         sectors,
         hideable,
         (double)hiddenSum / var0,
         culledSum / 1000.0 / var0,
         (double)modelsSum / var0,
         (double)madeSum / var0,
         (double)keptSum / var0,
         Apertures.windowsClosed,
         Apertures.windows,
         selectNanos * 1.0E-6 / var0,
         topology.compileNanos * 1.0E-6
      );
      frames = 0L;
      keptSum = 0L;
      madeSum = 0L;
      modelsSum = 0L;
      culledSum = 0L;
      hiddenSum = 0L;
      selectNanos = 0L;
      return var2;
   }

   private Rooms() {
   }

   static {
      Profile.roomsReport = Rooms::report;
   }
}
