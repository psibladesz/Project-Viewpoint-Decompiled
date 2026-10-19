package viewpoint.camera;

final class Kicks {
   static final int PITCH = 0;
   static final int YAW = 1;
   static final int ROLL = 2;
   static final int BACK = 3;
   static final int UP = 4;
   static final int ZOOM = 5;
   static final int AXES = 6;
   static final float[] MOST = new float[]{(float)Math.toRadians(3.0), (float)Math.toRadians(2.0), (float)Math.toRadians(3.0), 0.15F, 0.15F, 3.0F};
   static final float ACROSS_QUICKER = 0.8F;
   private static final float LN2 = 0.6931472F;
   private static final float INSTANT = 10000.0F;
   private static final float DEG = (float) (Math.PI / 180.0);
   private final float[] most;
   private final float[] target;
   private final float[] view;
   private float snap = 0.03F;
   private float recover = 0.15F;

   Kicks() {
      this(MOST);
   }

   Kicks(float[] var1) {
      this.most = var1;
      this.target = new float[var1.length];
      this.view = new float[var1.length];
   }

   void strike(Kicks.Kick var1, float var2) {
      if (!(var2 <= 0.0F)) {
         this.add(0, var1.pitch() * (float) (Math.PI / 180.0) * var2, var1.hard());
         this.add(1, var1.yaw() * (float) (Math.PI / 180.0) * var2, var1.hard());
         this.add(2, var1.roll() * (float) (Math.PI / 180.0) * var2, var1.hard());
         this.add(3, var1.back() * var2, var1.hard());
         this.add(4, var1.up() * var2, var1.hard());
         this.add(5, var1.zoom() * var2, var1.hard());
         this.pace(var1.snap(), var1.recover());
      }
   }

   void add(int var1, float var2, float var3) {
      float var4 = this.most[var1];
      this.target[var1] = Math.max(-var4, Math.min(var4, this.target[var1] + var2));
      this.view[var1] = Math.max(-var4, Math.min(var4, this.view[var1] + var2 * var3));
   }

   void pace(float var1, float var2) {
      this.snap = var1;
      this.recover = var2;
   }

   void update(float var1) {
      float var2 = rate(this.snap);
      float var3 = (float)Math.exp(-var2 * var1);

      for (int var4 = 0; var4 < this.target.length; var4++) {
         float var5 = rate(var4 == 1 ? this.recover * 0.8F : this.recover);
         float var6 = (float)Math.exp(-var5 * var1);
         float var7 = Math.abs(var2 - var5) < 0.001F ? var2 * var1 * var3 : var2 / (var2 - var5) * (var6 - var3);
         this.view[var4] = var3 * this.view[var4] + this.target[var4] * var7;
         this.target[var4] = this.target[var4] * var6;
      }
   }

   private static float rate(float var0) {
      return var0 <= 0.0F ? 10000.0F : 0.6931472F / var0;
   }

   float at(int var1) {
      return this.view[var1];
   }

   void reset() {
      for (int var1 = 0; var1 < this.target.length; var1++) {
         this.target[var1] = this.view[var1] = 0.0F;
      }
   }

   record Kick(float pitch, float yaw, float roll, float back, float up, float zoom, float hard, float snap, float recover) {
   }
}
