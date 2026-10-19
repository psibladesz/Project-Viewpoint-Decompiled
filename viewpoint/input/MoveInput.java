package viewpoint.input;

final class MoveInput {
   static final float REVERSAL = (float)Math.toRadians(135.0);
   private boolean moving;
   private float direction;

   void update(float var1, float var2, float var3, float var4) {
      if (var2 == 0.0F && var3 == 0.0F) {
         this.moving = false;
      } else {
         float var5 = (float)Math.atan2(var3, var2);
         boolean var6 = !this.moving || Math.abs(Ease.between(this.direction, var5)) > REVERSAL;
         this.direction = var6 ? var5 : Ease.angleToward(this.direction, var5, var1, var4);
         this.moving = true;
      }
   }

   boolean moving() {
      return this.moving;
   }

   float direction() {
      return this.direction;
   }
}
