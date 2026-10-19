package viewpoint.render;

import java.util.ArrayDeque;
import viewpoint.platform.Graphics;
import viewpoint.platform.Profile;

public final class Latency {
   static final int MAX_FENCES = 8;
   static final int LOW_LATENCY_QUEUE = 1;
   private static final Latency ON_GPU = new Latency(new Retirement.GlFences());
   private static volatile long newestSnapshot;
   private final Retirement.Fences fences;
   private final ArrayDeque<Long> presented = new ArrayDeque<>();
   private boolean viewBuilt;
   private boolean lowLatency;
   private int frames;
   private int lookFrames;
   private int maxBehind;
   private double lookMs;
   private double snapshotMs;
   private double newerSum;
   private double behindSum;

   public static void snapshotTaken(long var0) {
      newestSnapshot = var0;
   }

   Latency(Retirement.Fences var1) {
      this.fences = var1;
   }

   public static void viewBuilt(long var0, long var2, long var4, long var6) {
      ON_GPU.measure(var0, var2, var4, var6, newestSnapshot);
   }

   public static void presenting() {
      ON_GPU.fencePresented(Graphics.LOW_LATENCY.get());
   }

   void measure(long var1, long var3, long var5, long var7, long var9) {
      this.viewBuilt = true;
      int var11 = this.behind();
      this.frames++;
      this.snapshotMs += (var1 - var7) * 1.0E-6;
      this.newerSum = this.newerSum + Math.max(0L, var9 - var5);
      this.behindSum += var11;
      this.maxBehind = Math.max(this.maxBehind, var11);
      if (var3 != 0L) {
         this.lookFrames++;
         this.lookMs += (var1 - var3) * 1.0E-6;
      }
   }

   void fencePresented(boolean var1) {
      if (this.viewBuilt) {
         this.viewBuilt = false;
         if (this.presented.size() >= 8) {
            this.fences.delete(this.presented.pollFirst());
         }

         long var2 = this.fences.place();
         if (var2 != 0L) {
            this.presented.addLast(var2);
         }

         this.lowLatency = var1;

         while (var1 && this.presented.size() > 1) {
            long var4 = this.presented.pollFirst();
            this.fences.await(var4);
            this.fences.delete(var4);
         }
      }
   }

   int behind() {
      while (!this.presented.isEmpty() && this.fences.passed(this.presented.peekFirst())) {
         this.fences.delete(this.presented.pollFirst());
      }

      return this.presented.size();
   }

   String report() {
      if (this.frames == 0) {
         return "no frames";
      }

      String var1 = String.format(
         "look %s at the view, snapshot %.1f ms old (%.2f newer taken), GPU %.2f frames behind (at most %d)%s",
         this.lookFrames == 0 ? "not looking" : String.format("%.1f ms old", this.lookMs / this.lookFrames),
         this.snapshotMs / this.frames,
         this.newerSum / this.frames,
         this.behindSum / this.frames,
         this.maxBehind,
         this.lowLatency ? ", low latency" : ""
      );
      this.frames = this.lookFrames = this.maxBehind = 0;
      this.lookMs = this.snapshotMs = this.newerSum = this.behindSum = 0.0;
      return var1;
   }

   static {
      Profile.latencyReport = ON_GPU::report;
   }
}
