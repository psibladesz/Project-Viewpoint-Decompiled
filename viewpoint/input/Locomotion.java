package viewpoint.input;

final class Locomotion {
   static final int TURN_TO_MOVE = 0;
   static final int STRAFE = 1;
   static final int ORBIT = 2;
   static final float BACKPEDAL_IN = (float)Math.toRadians(110.0);
   static final float BACKPEDAL_OUT = (float)Math.toRadians(95.0);
   static final float STRAFE_IN = (float)Math.toRadians(25.0);
   static final float STRAFE_OUT = (float)Math.toRadians(15.0);
   static final float SIDEWAYS = (float)Math.toRadians(89.0);
   static final float WIDE_TURN = (float)Math.toRadians(150.0);
   static final float SWITCH_TURN = (float)Math.toRadians(60.0);
   static final float SWITCH_SECONDS = 0.4F;
   static final float KEY_TURN = (float)Math.toRadians(20.0);
   static final float KEY_DONE = (float)Math.toRadians(5.0);
   private boolean moving;
   private boolean strafing;
   private boolean backwards;
   private boolean switched;
   private boolean keyTurning;
   private float direction;

   static float turn(float var0, float var1, float var2, float var3) {
      return Math.abs(Ease.between(var0, var1)) > WIDE_TURN ? Ease.wrap(var1) : Ease.angleToward(var0, var1, var2, var3);
   }

   static boolean steersAfterSwitch(float var0, float var1) {
      return Math.abs(Ease.between(var0, var1)) >= SWITCH_TURN;
   }

   void update(int var1, boolean var2, float var3) {
      boolean var4 = this.moving;
      boolean var5 = this.strafing;
      float var6 = this.direction;
      this.moving = var2;
      this.direction = var3;
      if (!var2) {
         this.strafing = this.backwards = this.switched = this.keyTurning = false;
      } else {
         this.keyTurning = this.keyTurning | (!var4 || Math.abs(Ease.between(var6, var3)) > KEY_TURN);
         float var7 = Math.abs(var3);
         if (var1 == 2) {
            this.strafing = this.backwards = false;
         } else if (var1 == 0) {
            this.strafing = var7 > (this.strafing ? BACKPEDAL_OUT : BACKPEDAL_IN);
            this.backwards = this.strafing;
         } else {
            this.strafing = var7 > (this.strafing ? STRAFE_OUT : STRAFE_IN);
            this.backwards = var7 >= SIDEWAYS;
         }

         this.switched = var4 && this.strafing != var5;
      }
   }

   boolean switched() {
      return this.switched;
   }

   boolean strafing() {
      return this.strafing;
   }

   boolean keyTurning() {
      return this.keyTurning;
   }

   void faced(float var1, float var2) {
      this.keyTurning = this.keyTurning & Math.abs(Ease.between(var1, var2)) > KEY_DONE;
   }

   boolean backwards() {
      return this.backwards;
   }
}
