package viewpoint.platform;

import java.util.ArrayList;
import java.util.Arrays;

public final class Caches {
   static final int MIN_AGE = 60;
   private static final int INDEX_BITS = 24;
   private static final ArrayList<Caches.Cache> caches = new ArrayList<>();
   private static final Caches.Survey survey = new Caches.Survey();
   private static long tick;

   public static <T extends Caches.Cache> T register(T var0) {
      caches.add(var0);
      return (T)var0;
   }

   public static long tick() {
      return tick;
   }

   public static void balance() {
      tick++;
      Pool.CPU.use(total());
      if (Pool.CPU.over(0.9)) {
         survey.count = 0;

         for (Caches.Cache var1 : caches) {
            var1.list(survey);
         }

         long var4 = cutoff(survey, Pool.CPU.excess(0.8), tick - 60L);

         for (Caches.Cache var3 : caches) {
            var3.evictBefore(var4);
         }

         Pool.CPU.use(total());
      }
   }

   static long cutoff(Caches.Survey var0, long var1, long var3) {
      Arrays.sort(var0.keys, 0, var0.count);
      long var5 = 0L;
      long var7 = Long.MIN_VALUE;

      for (int var9 = 0; var9 < var0.count && var5 < var1; var9++) {
         long var10 = var0.keys[var9] >> 24;
         if (var10 >= var3) {
            break;
         }

         var5 += var0.sizes[(int)(var0.keys[var9] & 16777215L)];
         var7 = var10 + 1L;
      }

      return var7;
   }

   private static long total() {
      long var0 = 0L;

      for (Caches.Cache var3 : caches) {
         var0 += var3.bytes();
      }

      return var0;
   }

   private Caches() {
   }

   public interface Cache {
      long bytes();

      void list(Caches.Survey var1);

      void evictBefore(long var1);
   }

   public static final class Survey {
      private long[] keys = new long[1024];
      private long[] sizes = new long[1024];
      private int count;

      public void add(long var1, long var3) {
         if (this.count == this.keys.length) {
            this.keys = Arrays.copyOf(this.keys, this.count * 2);
            this.sizes = Arrays.copyOf(this.sizes, this.count * 2);
         }

         this.keys[this.count] = var1 << 24 | this.count;
         this.sizes[this.count] = var3;
         this.count++;
      }
   }
}
