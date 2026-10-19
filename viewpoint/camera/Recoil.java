package viewpoint.camera;

import java.util.Random;

final class Recoil {
   static final Recoil.Arms HANDGUN_ARMS = new Recoil.Arms(1.0F, 0.5F, 5.0F, 2.0F, 1.0F, 1.5F, 0.025F, 0.5F, 0.02F, 0.1F);
   static final Recoil.Arms MAGNUM_ARMS = new Recoil.Arms(2.0F, 0.8F, 10.0F, 3.0F, 1.5F, 2.5F, 0.04F, 0.5F, 0.025F, 0.15F);
   static final Recoil.Arms RIFLE_ARMS = new Recoil.Arms(3.0F, 1.0F, 1.5F, 0.5F, 0.8F, 1.5F, 0.045F, 0.5F, 0.03F, 0.14F);
   static final Recoil.Arms AUTOMATIC_ARMS = new Recoil.Arms(1.2F, 0.6F, 0.6F, 0.4F, 0.8F, 1.0F, 0.02F, 0.4F, 0.025F, 0.09F);
   static final Recoil.Arms SHOTGUN_ARMS = new Recoil.Arms(4.5F, 1.5F, 2.0F, 1.0F, 1.0F, 2.5F, 0.07F, 0.5F, 0.03F, 0.2F);
   static final Recoil.Gun HANDGUN = new Recoil.Gun(0.8F, 0.3F, 0.0F, 0.25F, 0.5F, 0.02F, 0.35F, 0.04F, 0.3F, 0.025F, 0.11F, HANDGUN_ARMS);
   static final Recoil.Gun MAGNUM = new Recoil.Gun(1.5F, 0.5F, 0.0F, 0.35F, 0.9F, 0.035F, 0.6F, 0.08F, 0.35F, 0.03F, 0.16F, MAGNUM_ARMS);
   static final Recoil.Gun RIFLE = new Recoil.Gun(1.4F, 0.4F, 0.1F, 0.25F, 0.6F, 0.04F, 0.5F, 0.08F, 0.35F, 0.03F, 0.15F, RIFLE_ARMS);
   static final Recoil.Gun AUTOMATIC = new Recoil.Gun(0.45F, 0.25F, 0.08F, 0.3F, 0.3F, 0.015F, 0.15F, 0.03F, 0.2F, 0.03F, 0.22F, AUTOMATIC_ARMS);
   static final Recoil.Gun SHOTGUN = new Recoil.Gun(2.2F, 0.6F, 0.0F, 0.4F, 1.2F, 0.07F, 0.9F, 0.2F, 0.4F, 0.035F, 0.24F, SHOTGUN_ARMS);
   static final float MAGNUM_DAMAGE = 2.0F;
   static final float AIM_STEADIER = 0.25F;
   static final float AUTO_TRAUMA_MOST = 0.3F;

   static Recoil.Gun of(int var0, boolean var1, boolean var2, float var3) {
      if (var0 > 1) {
         return SHOTGUN;
      } else if (var1) {
         return AUTOMATIC;
      } else if (var2) {
         return RIFLE;
      } else {
         return var3 >= 2.0F ? MAGNUM : HANDGUN;
      }
   }

   static Kicks.Kick shot(Recoil.Gun var0, Random var1) {
      float var2 = var0.pitch() + var0.pitchRandom() * var1.nextFloat();
      float var3 = var0.yawBias() + var0.yawRandom() * (2.0F * var1.nextFloat() - 1.0F);
      float var4 = var0.roll() * (2.0F * var1.nextFloat() - 1.0F);
      return new Kicks.Kick(var2, var3, var4, var0.back(), 0.0F, var0.zoom(), var0.hard(), var0.snap(), var0.recover());
   }

   private Recoil() {
   }

   record Arms(float rise, float riseRandom, float flip, float flipRandom, float across, float roll, float back, float hard, float snap, float recover) {
   }

   record Gun(
      float pitch,
      float pitchRandom,
      float yawBias,
      float yawRandom,
      float roll,
      float back,
      float zoom,
      float trauma,
      float hard,
      float snap,
      float recover,
      Recoil.Arms arms
   ) {
   }
}
