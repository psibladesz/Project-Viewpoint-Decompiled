package viewpoint.camera;

import java.util.Random;
import viewpoint.input.ArmsPitch;

final class GunKick {
   static final int RISE = 0;
   static final int ACROSS = 1;
   static final int ROLL = 2;
   static final int BACK = 3;
   static final int FLIP = 4;
   private static final float DEG = (float) (Math.PI / 180.0);
   static final float[] MOST = new float[]{(float) (Math.PI / 18), 0.06981317F, 0.10471976F, 0.08F, (float) (Math.PI / 9)};
   private final Kicks kicks = new Kicks(MOST);

   void shot(Recoil.Arms var1, Random var2, float var3) {
      if (!(var3 <= 0.0F)) {
         float var4 = var1.hard();
         this.kicks.add(0, (var1.rise() + var1.riseRandom() * var2.nextFloat()) * (float) (Math.PI / 180.0) * var3, var4);
         this.kicks.add(4, (var1.flip() + var1.flipRandom() * var2.nextFloat()) * (float) (Math.PI / 180.0) * var3, var4);
         this.kicks.add(1, var1.across() * (2.0F * var2.nextFloat() - 1.0F) * (float) (Math.PI / 180.0) * var3, var4);
         this.kicks.add(2, var1.roll() * (2.0F * var2.nextFloat() - 1.0F) * (float) (Math.PI / 180.0) * var3, var4);
         this.kicks.add(3, var1.back() * var3, var4);
         this.kicks.pace(var1.snap(), var1.recover());
      }
   }

   void update(float var1) {
      this.kicks.update(var1);
      ArmsPitch.kick(this.at(0), this.at(1), this.at(2), this.at(3), this.at(4));
   }

   float at(int var1) {
      return this.kicks.at(var1);
   }
}
