package viewpoint.visibility;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import viewpoint.platform.Caches;
import viewpoint.render.ChunkMeshData;
import viewpoint.render.SceneData;

final class DrawPlans {
   private static final int MAX_COMMANDS = 16;
   private static final long KEEP_FRAMES = 240L;
   private static final IdentityHashMap<ChunkMeshData, DrawPlans.Resolved> resolved = new IdentityHashMap<>();
   private static final int[] found = new int[65];
   private static boolean[] marked = new boolean[0];
   private static long bytes;
   static long culledVertices;
   static long plansMade;
   static long plansKept;

   static void plan(SceneData var0, int var1, ChunkMeshData var2, int var3, int var4, SectorGraph var5, boolean[] var6) {
      if (var2.runs != null || var2.hasModels()) {
         DrawPlans.Resolved var7 = resolved.computeIfAbsent(var2, var0x -> new DrawPlans.Resolved());
         var7.used = Caches.tick();
         if (var7.graph != var5) {
            bytes = bytes - size(var7);
            resolve(var7, var2, var3, var4, var5);
            bytes = bytes + size(var7);
         }

         if (var7.squares != null) {
            var0.meshModelSquares[var1] = visibleSquares(var7.squares, var6);
         }

         if (var2.runs != null && var7.canHide) {
            int var8 = var0.planEntries;
            long var9 = var7.sectors == null ? 0L : hiddenBits(var7.sectors, var6);
            if (var7.planned && var9 == var7.planHidden) {
               for (int var11 = 0; var11 < var7.entryCount * 3; var11 += 3) {
                  var0.addPlanEntry(var7.entries[var11], var7.entries[var11 + 1], var7.entries[var11 + 2]);
               }

               plansKept++;
            } else {
               var7.left = leaveOut(var0, var2, var7, var6);
               keep(var7, var0, var8, var9);
               plansMade++;
            }

            if (var7.left == 0L) {
               var0.planEntries = var8;
            } else {
               var0.meshPlan[var1] = var8;
               var0.meshPlanLength[var1] = var0.planEntries - var8;
               culledVertices = culledVertices + var7.left;
            }
         }
      }
   }

   private static long leaveOut(SceneData var0, ChunkMeshData var1, DrawPlans.Resolved var2, boolean[] var3) {
      long var4 = 0L;

      for (int var6 = 0; var6 < var1.pages.length; var6++) {
         int var7 = var0.planEntries;
         int var8 = 0;
         int var9 = -1;
         int var10 = 0;

         for (int var11 = var1.pageRuns[var6]; var11 < var1.pageRuns[var6 + 1]; var11++) {
            int var12 = var1.runs[var11 * 3 + 1];
            int var13 = var1.runs[var11 * 3 + 2];
            if (var2.a[var11] >= 0 && var3[var2.a[var11]] && var3[var2.b[var11]]) {
               var4 += var13;
            } else if (var9 >= 0 && var9 + var10 == var12) {
               var10 += var13;
            } else {
               var8 += var9 >= 0 ? var0.addPlanEntry(var6, var9, var10) : 0;
               var9 = var12;
               var10 = var13;
            }
         }

         var8 += var9 >= 0 ? var0.addPlanEntry(var6, var9, var10) : 0;
         if (var8 > 16) {
            var0.planEntries = var7;
            var0.addPlanEntry(var6, var1.first[var6], var1.count[var6]);
         }
      }

      return var4;
   }

   private static void keep(DrawPlans.Resolved var0, SceneData var1, int var2, long var3) {
      var0.planned = var0.sectors != null;
      var0.planHidden = var3;
      var0.entryCount = var0.planned && var0.left > 0L ? var1.planEntries - var2 : 0;
      if (var0.entries.length < var0.entryCount * 3) {
         bytes = bytes - size(var0);
         var0.entries = new int[var0.entryCount * 3];
         bytes = bytes + size(var0);
      }

      System.arraycopy(var1.plan, var2 * 3, var0.entries, 0, var0.entryCount * 3);
   }

   static long visibleSquares(int[] var0, boolean[] var1) {
      long var2 = -1L;

      for (int var4 = 0; var4 < var0.length; var4++) {
         var2 &= var0[var4] >= 0 && var1[var0[var4]] ? ~(1L << var4) : -1L;
      }

      return var2;
   }

   private static long hiddenBits(int[] var0, boolean[] var1) {
      long var2 = 0L;

      for (int var4 = 0; var4 < var0.length; var4++) {
         var2 |= var1[var0[var4]] ? 1L << var4 : 0L;
      }

      return var2;
   }

   private static void resolve(DrawPlans.Resolved var0, ChunkMeshData var1, int var2, int var3, SectorGraph var4) {
      int var5 = var1.runs == null ? 0 : var1.runs.length / 3;
      int var6 = var1.level;
      long var7 = Fragment.key(var2, var3, var6);
      var0.graph = var4;
      var0.squares = var1.hasModels() ? squares(var0.squares, var7, var4) : null;
      var0.a = var0.a != null && var0.a.length == var5 ? var0.a : new int[var5];
      var0.b = var0.b != null && var0.b.length == var5 ? var0.b : new int[var5];
      var0.canHide = false;

      for (int var9 = 0; var9 < var5; var9++) {
         int var10 = var1.runs[var9 * 3];
         int var11 = Owner.square(var10);
         int var12 = var11 % 8;
         int var13 = var11 / 8;
         int var14 = var10 == -1 ? -1 : var4.sector(var7, var11);
         int var15 = var14;
         switch (var10 == -1 ? 0 : Owner.kind(var10)) {
            case 1:
               var15 = var13 > 0 ? var4.sector(var7, var11 - 8) : var4.sector(Fragment.key(var2, var3 - 1, var6), var11 + 64 - 8);
               break;
            case 2:
               var15 = var12 > 0 ? var4.sector(var7, var11 - 1) : var4.sector(Fragment.key(var2 - 1, var3, var6), var11 + 8 - 1);
               break;
            case 3:
               int var16 = var4.sector(Fragment.key(var2, var3, var6 - 1), var11);
               var15 = var16 >= 0 ? var16 : var14;
         }

         boolean var17 = var14 >= 0 && var15 >= 0;
         var0.a[var9] = var17 ? var14 : -1;
         var0.b[var9] = var17 ? var15 : -1;
         var0.canHide = var0.canHide | (var17 && var4.hideable[var14] && var4.hideable[var15]);
      }

      var0.sectors = var0.canHide ? hideBy(var0, var4) : null;
      var0.planned = false;
   }

   private static int[] squares(int[] var0, long var1, SectorGraph var3) {
      int[] var4 = var0 != null ? var0 : new int[64];

      for (int var5 = 0; var5 < 64; var5++) {
         int var6 = var3.sector(var1, var5);
         var4[var5] = var6 >= 0 && var3.hideable[var6] ? var6 : -1;
      }

      return var4;
   }

   private static int[] hideBy(DrawPlans.Resolved var0, SectorGraph var1) {
      if (marked.length < var1.sectors) {
         marked = new boolean[var1.sectors];
      }

      int var2 = 0;

      for (int var3 = 0; var3 < var0.a.length && var2 < found.length; var3++) {
         int var4 = var0.a[var3];
         int var5 = var0.b[var3];
         if (var4 >= 0 && var1.hideable[var4] && var1.hideable[var5]) {
            var2 = mark(var5, mark(var4, var2));
         }
      }

      for (int var6 = 0; var6 < var2; var6++) {
         marked[found[var6]] = false;
      }

      return var2 <= 64 ? Arrays.copyOf(found, var2) : null;
   }

   private static int mark(int var0, int var1) {
      if (var1 != found.length && !marked[var0]) {
         marked[var0] = true;
         found[var1] = var0;
         return var1 + 1;
      } else {
         return var1;
      }
   }

   private static void sweep(long var0) {
      Iterator var2 = resolved.values().iterator();

      while (var2.hasNext()) {
         DrawPlans.Resolved var3 = (DrawPlans.Resolved)var2.next();
         if (var3.used < var0) {
            bytes = bytes - size(var3);
            var2.remove();
         }
      }
   }

   static void sweep() {
      sweep(Caches.tick() - 240L);
   }

   static void clear() {
      resolved.clear();
      bytes = 0L;
   }

   private static long size(DrawPlans.Resolved var0) {
      long var1 = var0.sectors == null ? 0L : 4L * var0.sectors.length;
      long var3 = var0.squares == null ? 0L : 4L * var0.squares.length;
      return (var0.a == null ? 0L : 8L * var0.a.length) + var1 + var3 + 4L * var0.entries.length + 112L;
   }

   private DrawPlans() {
   }

   static {
      Caches.register(new Caches.Cache() {
         @Override
         public long bytes() {
            return DrawPlans.bytes;
         }

         @Override
         public void list(Caches.Survey var1) {
            for (DrawPlans.Resolved var3 : DrawPlans.resolved.values()) {
               var1.add(var3.used, DrawPlans.size(var3));
            }
         }

         @Override
         public void evictBefore(long var1) {
            DrawPlans.sweep(var1);
         }
      });
   }

   private static final class Resolved {
      SectorGraph graph;
      int[] a;
      int[] b;
      int[] sectors;
      boolean canHide;
      long used;
      boolean planned;
      long planHidden;
      long left;
      int[] entries = new int[0];
      int entryCount;
      int[] squares;
   }
}
