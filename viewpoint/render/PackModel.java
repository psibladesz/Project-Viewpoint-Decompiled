package viewpoint.render;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public final class PackModel {
   public static final int UNLOADED = 0;
   public static final int READING = 1;
   public static final int READ = 2;
   public static final int RESIDENT = 3;
   public static final int FAILED = 4;
   public static final int NO_ROOM = 5;
   public final int index;
   public final File obj;
   private volatile int state;
   private final AtomicInteger holds = new AtomicInteger();
   private volatile long wanted;
   private volatile PackData data;
   private volatile long refused;
   private volatile float[] bounds;
   int meshPage;
   int firstVertex;
   int vertices;
   int firstIndex;
   int indices;
   int artPage;
   int artX;
   int artY;
   int artW;
   int artH;
   int artWidth;
   int artHeight;

   PackModel(int var1, File var2) {
      this.index = var1;
      this.obj = var2;
   }

   public int state() {
      return this.state;
   }

   public void wanted(long var1) {
      this.wanted = var1;
   }

   public long wanted() {
      return this.wanted;
   }

   public void reading() {
      this.state = 1;
   }

   public void read(PackData var1) {
      float[] var2 = var1.vertices();
      float[] var3 = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};

      for (int var4 = 0; var4 < var2.length; var4 += 8) {
         for (int var5 = 0; var5 < 3; var5++) {
            var3[var5] = Math.min(var3[var5], var2[var4 + var5]);
            var3[var5 + 3] = Math.max(var3[var5 + 3], var2[var4 + var5]);
         }
      }

      this.bounds = var3;
      this.data = var1;
      this.state = 2;
      PackModels.READ.add(this);
   }

   public float[] bounds() {
      return this.bounds;
   }

   public void failed() {
      this.state = 4;
      PackModels.SETTLED.add(this);
   }

   public void retry() {
      this.state = 0;
   }

   public long refused() {
      return this.refused;
   }

   void refused(long var1) {
      this.refused = var1;
   }

   public void hold() {
      this.holds.incrementAndGet();
   }

   public void release() {
      this.holds.decrementAndGet();
   }

   boolean held() {
      return this.holds.get() > 0;
   }

   void state(int var1) {
      this.state = var1;
   }

   PackData take() {
      PackData var1 = this.data;
      this.data = null;
      return var1;
   }
}
