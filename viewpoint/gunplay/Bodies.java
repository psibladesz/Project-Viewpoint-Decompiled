package viewpoint.gunplay;

import zombie.characters.IsoGameCharacter;
import zombie.core.skinnedmodel.animation.AnimationPlayer;
import zombie.core.skinnedmodel.model.Model;
import zombie.iso.Vector3;

final class Bodies {
   private static final float QUARTER_TURN = (float) (Math.PI / 2);
   private static final float BACK = 0.08F;
   private static final Vector3 point = new Vector3();

   static void of(IsoGameCharacter var0, float[] var1, float[] var2) {
      if (!posed(var0, var1)) {
         placed(var0, var1);
      }

      float var3 = (float)Ground.under(var0);

      for (int var4 = 0; var4 < Parts.BONES.length; var4++) {
         var1[var4 * 3 + 2] = var1[var4 * 3 + 2] + var3;
      }

      Parts.capsules(var1, var2);
   }

   private static boolean posed(IsoGameCharacter var0, float[] var1) {
      AnimationPlayer var2 = var0.getAnimationPlayer();
      if (var2 != null && var2.isReady()) {
         for (int var3 = 0; var3 < Parts.BONES.length; var3++) {
            int var4 = var2.getSkinningBoneIndex(Parts.BONES[var3], -1);
            if (var4 < 0) {
               return false;
            }

            Model.boneToWorldCoords(var0, var4, point);
            put(var1, var3);
         }

         return true;
      } else {
         return false;
      }
   }

   private static void placed(IsoGameCharacter var0, float[] var1) {
      float var2 = var0.getDirectionAngleRadians() + (float) (Math.PI / 2);
      boolean var3 = var0.isProne();
      float[] var4 = Reference.BIND;
      float var5 = var4[4];

      for (int var6 = 0; var6 < Parts.BONES.length; var6++) {
         float var7 = var4[var6 * 3];
         float var8 = var4[var6 * 3 + 1];
         float var9 = var4[var6 * 3 + 2];
         if (var3) {
            point.set(var7, var9 + 0.08F, var8 - var5);
         } else {
            point.set(var7, var8, var9);
         }

         Model.vectorToWorldCoords(var0.getX(), var0.getY(), var0.getZ(), var2, point);
         put(var1, var6);
      }
   }

   private static void put(float[] var0, int var1) {
      var0[var1 * 3] = point.x;
      var0[var1 * 3 + 1] = point.y;
      var0[var1 * 3 + 2] = point.z * 2.4494896F;
   }

   private Bodies() {
   }
}
