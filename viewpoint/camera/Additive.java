package viewpoint.camera;

final class Additive {
   private static final float DEG = (float) (Math.PI / 180.0);
   static final float TRAUMA_DECAY = 0.8F;
   static final float SHAKE_YAW = 0.02617994F;
   static final float SHAKE_PITCH = 0.02617994F;
   static final float SHAKE_ROLL = 0.04363323F;
   static final float SHAKE_HERTZ = 18.0F;
   static final float HAND_PITCH = 0.002094395F;
   static final float HAND_YAW = 0.0013962634F;
   static final float HAND_ROLL = 8.726646E-4F;
   static final float LEAN_PER = 0.004F;
   static final float LEAN_MOST = 0.02094395F;
   static final float LEAN_HALF_LIFE = 0.15F;
   static final float PANIC_MORE = 1.0F;
   static final float TIRED_MORE = 0.5F;
   static final float TIRED_SLOWER = 0.3F;
   private static final int YAW = 11;
   private static final int PITCH = 23;
   private static final int ROLL = 37;
   private static final int SHAKE = 101;
   final Kicks kicks = new Kicks();
   private final Spring lean = new Spring();
   private float trauma;
   private float handPhase;
   private float shakePhase;
   float yaw;
   float pitch;
   float roll;
   float back;
   float up;
   float zoom;

   void reset() {
      this.trauma = 0.0F;
      this.kicks.reset();
      this.lean.set(0.0F);
   }

   void trauma(float var1) {
      this.trauma = Math.min(1.0F, this.trauma + Math.max(0.0F, var1));
   }

   float trauma() {
      return this.trauma;
   }

   void update(float var1, Profiles.Mix var2, CameraInputs var3, float var4, float var5) {
      this.trauma = Math.max(0.0F, this.trauma - 0.8F * var1);
      this.handPhase = this.handPhase + var1 * var2.hertz * (1.0F - 0.3F * var3.tired);
      this.shakePhase += var1 * 18.0F;
      float var6 = var2.handheld * var4 * (1.0F + 1.0F * var3.panic) * (1.0F + 0.5F * var3.tired);
      float var7 = this.trauma * this.trauma * var5;
      this.kicks.update(var1);
      float var8 = Math.max(-0.02094395F, Math.min(0.02094395F, var3.yawRate * 0.004F)) * var2.lean * var4;
      this.lean.toward(var8, 0.15F, var1);
      this.yaw = var6 * 0.0013962634F * Noise.layered(this.handPhase, 11) + var7 * 0.02617994F * Noise.at(this.shakePhase, 112) + this.kicks.at(1);
      this.pitch = var6 * 0.002094395F * Noise.layered(this.handPhase, 23) + var7 * 0.02617994F * Noise.at(this.shakePhase, 124) + this.kicks.at(0);
      this.roll = var6 * 8.726646E-4F * Noise.layered(this.handPhase, 37)
         + var7 * 0.04363323F * Noise.at(this.shakePhase, 138)
         + this.kicks.at(2)
         + this.lean.value;
      this.back = this.kicks.at(3);
      this.up = var2.gait * var3.headBob * var4 + this.kicks.at(4);
      this.zoom = this.kicks.at(5);
   }
}
