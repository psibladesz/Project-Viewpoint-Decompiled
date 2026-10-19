package viewpoint.visibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import zombie.iso.IsoChunk;
import zombie.iso.IsoObject;

final class Topology {
   private static final long MIN_INTERVAL = 200000000L;
   private final HashMap<Long, Topology.Leaf> leaves = new HashMap<>();
   private long lastStart;
   private final IsoObject[] captured = new IsoObject[128];
   private final long[] capturedGlazed = new long[2];
   private int generation;
   private boolean changed;
   private boolean edited;
   private SectorGraph published;
   private Future<SectorGraph> job;
   volatile long compileNanos;
   private static volatile boolean compileFailed;
   private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(var0 -> {
      Thread var1 = new Thread(var0, "Viewpoint rooms");
      var1.setDaemon(true);
      var1.setPriority(4);
      return var1;
   });

   void capture(IsoChunk var1, int var2) {
      long var3 = Fragment.key(var1.wx, var1.wy, var2);
      Topology.Leaf var5 = this.leaves.get(var3);
      if (var5 == null) {
         var5 = new Topology.Leaf();
         this.leaves.put(var3, var5);
         this.generation++;
      }

      Fragment var6 = Capture.capture(var1, var2, this.captured, this.capturedGlazed);
      System.arraycopy(this.captured, 0, var5.doors, 0, this.captured.length);
      System.arraycopy(this.capturedGlazed, 0, var5.glazed, 0, this.capturedGlazed.length);
      if (!var6.sameLayout(var5.fragment)) {
         if (this.published != null && var5.fragment != null && this.published.sources.get(var3) == var5.fragment) {
            this.edited = true;
         }

         var5.fragment = var6;
         this.changed = true;
      }
   }

   void forget(int var1, int var2) {
      Iterator var3 = this.leaves.values().iterator();

      while (var3.hasNext()) {
         Fragment var4 = ((Topology.Leaf)var3.next()).fragment;
         if (var4 != null && var4.chunkX == var1 && var4.chunkY == var2) {
            var3.remove();
            this.changed = true;
            this.generation++;
         }
      }
   }

   void clear() {
      this.leaves.clear();
      this.generation++;
      this.published = null;
      this.edited = false;
      this.changed = false;
      if (this.job != null) {
         this.job.cancel(false);
         this.job = null;
      }
   }

   void update() {
      if (this.job != null && this.job.isDone()) {
         SectorGraph var1 = this.finished();
         if (var1 != null && this.current(var1)) {
            this.published = var1;
            this.edited = false;
         } else {
            this.changed = true;
         }

         this.job = null;
      }

      if (this.changed && this.job == null && System.nanoTime() - this.lastStart >= 200000000L) {
         this.changed = false;
         this.lastStart = System.nanoTime();
         ArrayList var4 = new ArrayList(this.leaves.size());

         for (Topology.Leaf var3 : this.leaves.values()) {
            if (var3.fragment != null) {
               var4.add(var3.fragment);
            }
         }

         this.job = WORKER.submit(() -> {
            long var2 = System.nanoTime();
            SectorGraph var4x = GraphBuilder.compile(var4);
            this.compileNanos = System.nanoTime() - var2;
            return var4x;
         });
      }
   }

   private SectorGraph finished() {
      try {
         return this.job.get();
      } catch (Exception var2) {
         if (!compileFailed) {
            compileFailed = true;
            System.out.println("[Viewpoint] rooms: the sector graph failed to compile (nothing is hidden):");
            var2.printStackTrace(System.out);
         }

         return null;
      }
   }

   private boolean current(SectorGraph var1) {
      for (Fragment var3 : var1.sources.values()) {
         Topology.Leaf var4 = this.leaves.get(var3.key());
         if (var4 != null && var4.fragment != var3) {
            return false;
         }
      }

      return true;
   }

   SectorGraph graph() {
      return this.edited ? null : this.published;
   }

   Topology.Leaf leaf(long var1) {
      return this.leaves.get(var1);
   }

   int generation() {
      return this.generation;
   }

   int levels() {
      return this.leaves.size();
   }

   static final class Leaf {
      Fragment fragment;
      final IsoObject[] doors = new IsoObject[128];
      final long[] glazed = new long[2];

      boolean glazed(int var1) {
         return (this.glazed[var1 & 1] >>> (var1 >> 1) & 1L) != 0L;
      }
   }
}
