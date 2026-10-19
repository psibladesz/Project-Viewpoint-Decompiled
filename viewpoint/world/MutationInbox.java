package viewpoint.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

final class MutationInbox {
   static final int GEOMETRY = 1;
   static final int BLOOD = 2;
   static final int ALL_LEVELS = Integer.MIN_VALUE;
   static final int CAPACITY = 4096;
   private volatile Thread owner;
   private final ConcurrentLinkedQueue<int[]> foreign = new ConcurrentLinkedQueue<>();
   private final AtomicInteger foreignCount = new AtomicInteger();
   private final AtomicBoolean foreignOverflow = new AtomicBoolean();
   private final HashMap<Long, MutationInbox.Waiting> waiting = new HashMap<>();
   private MutationInbox.Waiting last;
   private long stamp;
   private boolean overflow;
   private int signals;

   void record(int var1, int var2, int var3, int var4) {
      if (Thread.currentThread() == this.owner) {
         this.add(var1, var2, var3, var4);
      } else if (this.foreignCount.incrementAndGet() <= 4096) {
         this.foreign.add(new int[]{var1, var2, var3, var4});
      } else {
         this.foreignCount.decrementAndGet();
         this.foreignOverflow.set(true);
      }
   }

   long stamp() {
      return this.stamp;
   }

   boolean drain(ArrayList<MutationInbox.Waiting> var1) {
      this.owner = Thread.currentThread();

      int[] var2;
      while ((var2 = this.foreign.poll()) != null) {
         this.foreignCount.decrementAndGet();
         this.add(var2[0], var2[1], var2[2], var2[3]);
      }

      boolean var3 = !this.foreignOverflow.getAndSet(false) && !this.overflow;
      var1.clear();
      if (var3) {
         var1.addAll(this.waiting.values());
      }

      this.waiting.clear();
      this.last = null;
      this.overflow = false;
      return var3;
   }

   void clear() {
      this.foreign.clear();
      this.foreignCount.set(0);
      this.foreignOverflow.set(false);
      this.waiting.clear();
      this.last = null;
      this.overflow = false;
      this.signals = 0;
   }

   int takeSignalCount() {
      int var1 = this.signals;
      this.signals = 0;
      return var1;
   }

   private void add(int var1, int var2, int var3, int var4) {
      this.stamp++;
      this.signals++;
      if (!this.overflow) {
         MutationInbox.Waiting var5 = this.last != null && this.last.chunkX == var1 && this.last.chunkY == var2 && this.last.level == var3
            ? this.last
            : this.waiting.get(key(var1, var2, var3));
         if (var5 == null && this.waiting.size() == 4096) {
            this.overflow = true;
            this.waiting.clear();
            this.last = null;
         } else {
            if (var5 == null) {
               var5 = new MutationInbox.Waiting(var1, var2, var3);
               this.waiting.put(key(var1, var2, var3), var5);
            }

            var5.kinds |= var4;
            if ((var4 & 1) != 0) {
               var5.geometryStamp = this.stamp;
            }

            this.last = var5;
         }
      }
   }

   private static long key(int var0, int var1, int var2) {
      return (var0 & 16777215L) << 36 | (var1 & 16777215L) << 12 | (var2 == Integer.MIN_VALUE ? 4095L : var2 & 2047L);
   }

   static final class Waiting {
      final int chunkX;
      final int chunkY;
      final int level;
      int kinds;
      long geometryStamp;

      Waiting(int var1, int var2, int var3) {
         this.chunkX = var1;
         this.chunkY = var2;
         this.level = var3;
      }
   }
}
