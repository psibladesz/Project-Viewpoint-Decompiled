package viewpoint.input;

final class EyeMotion {
   static final float FOLLOW = 0.04F;
   static final float STEADY = 0.15F;
   static final float BLEND = 0.2F;
   static final float RESTART = 0.25F;
   float offsetX;
   float offsetZ;
   float height;
   float lean;
   float bob;
   private float steadied;
   private float headX;
   private float headZ;
   private float followHeight;
   private float steadyHeight;
   private float steadyLean;
   private boolean started;

   void update(float var1, float var2, float var3, float var4, float var5, boolean var6, float var7) {
      if (this.started && !(var1 > 0.25F)) {
         this.steadied = Ease.step(this.steadied, var6 ? 1.0F : 0.0F, var1, 5.0F);
         this.headX = Ease.toward(this.headX, var2, var1, 0.04F);
         this.headZ = Ease.toward(this.headZ, var3, var1, 0.04F);
         this.followHeight = Ease.toward(this.followHeight, var4, var1, 0.04F);
         this.steadyHeight = Ease.toward(this.steadyHeight, var4, var1, 0.15F);
         this.steadyLean = Ease.toward(this.steadyLean, var5, var1, 0.15F);
      } else {
         this.started = true;
         this.steadied = var6 ? 1.0F : 0.0F;
         this.headX = var2;
         this.headZ = var3;
         this.followHeight = this.steadyHeight = var4;
         this.steadyLean = var5;
      }

      float var8 = this.steadied * (1.0F - var7);
      this.offsetX = (1.0F - var8) * this.headX;
      this.offsetZ = (1.0F - var8) * this.headZ;
      this.height = var8 * this.steadyHeight + (1.0F - var8) * this.followHeight;
      this.lean = var8 * this.steadyLean;
      this.bob = this.followHeight - this.steadyHeight;
   }
}
