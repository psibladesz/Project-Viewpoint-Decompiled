package viewpoint.camera;

final class CameraRig {
   static final float LIMIT_HOLD = 0.15F;
   static final float LIMIT_HALF_LIFE = 0.3F;
   static final float SIDE_HALF_LIFE = 0.1F;
   static final float SWAP_CUT = 0.6F;
   static final float SWAP_AFTER = 0.3F;
   static final float SWAP_FREE = 0.85F;
   static final float SWAP_GAIN = 0.25F;
   static final float BACK_AFTER = 1.0F;
   static final float TIGHT_HOLD = 0.15F;
   static final float BLEND_HALF_LIFE = 0.2919F;
   private static final float LN2 = 0.6931472F;
   final float[] weight = new float[15];
   final Profiles.Mix mix = new Profiles.Mix();
   final Follow follow = new Follow();
   final Additive additive = new Additive();
   final Beats.Player beats = new Beats.Player();
   private final float[] target = new float[15];
   private final Spring[] blends = springs(15);
   private final Spring side = new Spring();
   private float limit = Float.MAX_VALUE;
   private float hold;
   private float tightHeld;
   private float tightFor;
   private float cutFor;
   private float freeFor;
   private int sideNow = 1;
   private int lastPreferred = 1;
   private boolean started;

   boolean started() {
      return this.started;
   }

   void start(CameraInputs var1, int var2) {
      this.started = true;
      this.tightHeld = var1.tight;
      Profiles.targets(var1, this.target);

      for (int var3 = 0; var3 < this.weight.length; var3++) {
         this.blends[var3].set(this.target[var3]);
         this.weight[var3] = this.target[var3];
      }

      Profiles.mix(this.weight, var1, this.mix);
      this.follow.reset();
      this.additive.reset();
      this.beats.stop();
      this.sideNow = this.lastPreferred = var2;
      this.side.set(var2);
      this.limit = Float.MAX_VALUE;
      this.hold = this.cutFor = this.freeFor = 0.0F;
   }

   void stop() {
      this.started = false;
   }

   void weights(CameraInputs var1, float var2) {
      var1.tight = this.tight(var1.tight, var2);
      Profiles.targets(var1, this.target);

      for (int var3 = 0; var3 < this.weight.length; var3++) {
         float var4 = this.target[var3] > this.weight[var3] ? Profiles.IN[var3] : Profiles.OUT[var3];
         this.blends[var3].toward(this.target[var3], var4 * 0.2919F, var2);
         this.weight[var3] = Math.max(0.0F, Math.min(1.0F, this.blends[var3].value));
      }

      Profiles.mix(this.weight, var1, this.mix);
   }

   private float tight(float var1, float var2) {
      if (var1 >= this.tightHeld) {
         this.tightHeld = var1;
         this.tightFor = 0.15F;
      } else {
         this.tightFor -= var2;
         if (this.tightFor <= 0.0F) {
            this.tightHeld = var1;
         }
      }

      return this.tightHeld;
   }

   float limit(float var1, float var2) {
      if (var1 <= this.limit) {
         this.limit = var1;
         this.hold = 0.15F;
      } else if (this.hold > 0.0F) {
         this.hold -= var2;
      } else {
         this.limit = var1 - (var1 - this.limit) * (float)Math.exp(-0.6931472F * var2 / 0.3F);
      }

      return this.limit;
   }

   float side(int var1, boolean var2, boolean var3, float var4, float var5, float var6) {
      if (var1 != this.lastPreferred) {
         this.lastPreferred = var1;
         this.sideNow = var1;
         this.cutFor = this.freeFor = 0.0F;
      }

      if (!var2 || var3) {
         this.cutFor = this.freeFor = 0.0F;
         if (!var2) {
            this.sideNow = var1;
         }
      } else if (this.sideNow == var1) {
         boolean var7 = var5 >= 0.85F && var5 >= var4 + 0.25F;
         this.cutFor = var4 < 0.6F && var7 ? this.cutFor + var6 : 0.0F;
         if (this.cutFor >= 0.3F) {
            this.sideNow = -var1;
            this.cutFor = 0.0F;
         }
      } else {
         this.freeFor = var5 >= 0.85F ? this.freeFor + var6 : 0.0F;
         if (this.freeFor >= 1.0F) {
            this.sideNow = var1;
            this.freeFor = 0.0F;
         }
      }

      this.side.toward(this.sideNow, 0.1F, var6);
      return this.side.value;
   }

   int sideNow() {
      return this.sideNow;
   }

   private static Spring[] springs(int var0) {
      Spring[] var1 = new Spring[var0];

      for (int var2 = 0; var2 < var0; var2++) {
         var1[var2] = new Spring();
      }

      return var1;
   }
}
