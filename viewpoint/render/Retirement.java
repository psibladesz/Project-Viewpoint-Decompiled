package viewpoint.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

public final class Retirement {
   static final int MAX_FENCES = 64;
   private static final Retirement ON_GPU = new Retirement(new Retirement.GlFences());
   private long building;
   private final ConcurrentLinkedQueue<Retirement.Retired> retired = new ConcurrentLinkedQueue<>();
   private final Retirement.Fences fences;
   private final ArrayDeque<Retirement.Retired> waiting = new ArrayDeque<>();
   private final ArrayDeque<Retirement.Fenced> fenced = new ArrayDeque<>();
   private boolean fencesFailed;

   public static void building(long var0) {
      ON_GPU.stamp(var0);
   }

   void stamp(long var1) {
      this.building = var1;
   }

   public static void retire(Retirement.Freeable var0) {
      ON_GPU.letGo(var0);
   }

   public static void retireDrawing(Retirement.Freeable var0) {
      ON_GPU.retired.add(new Retirement.Retired(var0, Long.MIN_VALUE));
   }

   void letGo(Retirement.Freeable var1) {
      if (var1 != null) {
         this.retired.add(new Retirement.Retired(var1, this.building));
      }
   }

   Retirement(Retirement.Fences var1) {
      this.fences = var1;
   }

   public static void collect() {
      ON_GPU.freePassed();
   }

   public static void drawn(long var0) {
      ON_GPU.fenceDrawn(var0);
   }

   void freePassed() {
      while (!this.fenced.isEmpty() && this.fences.passed(this.fenced.peekFirst().fence())) {
         Retirement.Fenced var1 = this.fenced.pollFirst();
         this.fences.delete(var1.fence());
         free(var1.things());
      }
   }

   void fenceDrawn(long var1) {
      Retirement.Retired var3;
      while ((var3 = this.retired.poll()) != null) {
         this.waiting.addLast(var3);
      }

      ArrayList var7 = new ArrayList();

      while (!this.waiting.isEmpty() && this.waiting.peekFirst().stamp() <= var1) {
         var7.add(this.waiting.pollFirst().thing());
      }

      if (!var7.isEmpty()) {
         long var4 = this.fences.place();
         if (var4 != 0L && this.fenced.size() < 64) {
            this.fenced.addLast(new Retirement.Fenced(var4, var7));
         } else {
            if (!this.fencesFailed) {
               this.fencesFailed = true;
               System.out.println("[Viewpoint] GPU fences failed: freeing after waiting for the GPU instead");
            }

            this.fences.finish();
            if (var4 != 0L) {
               this.fences.delete(var4);
            }

            Retirement.Fenced var6;
            while ((var6 = this.fenced.pollFirst()) != null) {
               this.fences.delete(var6.fence());
               free(var6.things());
            }

            free(var7);
         }
      }
   }

   int pending() {
      int var1 = this.waiting.size() + this.retired.size();

      for (Retirement.Fenced var3 : this.fenced) {
         var1 += var3.things().size();
      }

      return var1;
   }

   private static void free(ArrayList<Retirement.Freeable> var0) {
      for (Retirement.Freeable var2 : var0) {
         var2.free();
      }
   }

   private record Fenced(long fence, ArrayList<Retirement.Freeable> things) {
   }

   interface Fences {
      long place();

      boolean passed(long var1);

      void delete(long var1);

      void await(long var1);

      void finish();
   }

   public interface Freeable {
      void free();
   }

   static final class GlFences implements Retirement.Fences {
      private static final long AWAIT_NANOS = 250000000L;

      @Override
      public long place() {
         return GL32.glFenceSync(37143, 0);
      }

      @Override
      public boolean passed(long var1) {
         int var3 = GL32.glClientWaitSync(var1, 1, 0L);
         return var3 == 37146 || var3 == 37148;
      }

      @Override
      public void delete(long var1) {
         GL32.glDeleteSync(var1);
      }

      @Override
      public void await(long var1) {
         GL32.glClientWaitSync(var1, 1, 250000000L);
      }

      @Override
      public void finish() {
         GL11.glFinish();
      }
   }

   private record Retired(Retirement.Freeable thing, long stamp) {
   }
}
