package viewpoint.input;

import java.util.List;
import viewpoint.platform.Build;
import zombie.characters.IsoPlayer;
import zombie.core.skinnedmodel.advancedanimation.AdvancedAnimator;
import zombie.core.skinnedmodel.advancedanimation.AnimLayer;
import zombie.core.skinnedmodel.advancedanimation.LiveAnimNode;

final class StrafePace {
   private static final String WALK_STATE = "movement";
   private static final String STRAFE_STATE = "strafe";
   private static final float ALONE = 0.99F;
   private final Stride stride = new Stride();
   private float walkSeen;
   private float strafeSeen;

   void update(IsoPlayer var1, float var2, float var3, boolean var4, boolean var5, float var6) {
      AdvancedAnimator var7 = var1.getAdvancedAnimator();
      AnimLayer var8 = var7 == null ? null : var7.getRootLayer();
      if (var8 != null && !(var3 <= 0.0F)) {
         int var9 = var1.isSneaking() ? 1 : 0;
         float var10 = var2 / var3;
         LiveAnimNode var11 = var4 ? strongest(var8, "movement") : null;
         if (var11 != null && alone(var11)) {
            this.stride.walked(var9, var10, var3);
            this.walkSeen = var10;
         }

         LiveAnimNode var12 = strongest(var8, "strafe");
         if (var5 && var12 != null) {
            float var13 = this.stride.factor();
            if (alone(var12)) {
               float var14 = var12.getSourceNode().getSpeedScale(var1);
               float var15 = var14 > 0.0F ? var12.getMainAnimationTrackAt(0).getSpeedDelta() / var14 : 1.0F;
               var13 = this.stride.strafed(var9, var10, var15, var3, var6);
               this.strafeSeen = var10;
            }

            pace(var1, var8, var13);
         } else {
            if (this.stride.factor() != 1.0F) {
               this.report();
               this.stride.stop();
               pace(var1, var8, 1.0F);
            }
         }
      }
   }

   private void report() {
      if (!Build.RELEASE) {
         System.out
            .printf("[Viewpoint] strafe pace: played at %.2f, strafing %.2f squares/s, walking %.2f%n", this.stride.factor(), this.strafeSeen, this.walkSeen);
      }
   }

   private static void pace(IsoPlayer var0, AnimLayer var1, float var2) {
      List var3 = var1.getLiveAnimNodes();

      for (int var4 = 0; var4 < var3.size(); var4++) {
         LiveAnimNode var5 = (LiveAnimNode)var3.get(var4);
         if (in(var5, "strafe")) {
            float var6 = var5.getSourceNode().getSpeedScale(var0) * var2;

            for (int var7 = 0; var7 < var5.getMainAnimationTracksCount(); var7++) {
               var5.getMainAnimationTrackAt(var7).setSpeedDelta(var6);
            }
         }
      }
   }

   private static boolean alone(LiveAnimNode var0) {
      return var0.getWeight() >= 0.99F && var0.getMainAnimationTracksCount() > 0;
   }

   private static LiveAnimNode strongest(AnimLayer var0, String var1) {
      List var2 = var0.getLiveAnimNodes();
      LiveAnimNode var3 = null;

      for (int var4 = 0; var4 < var2.size(); var4++) {
         LiveAnimNode var5 = (LiveAnimNode)var2.get(var4);
         if (var5.isActive() && in(var5, var1) && (var3 == null || var5.getWeight() > var3.getWeight())) {
            var3 = var5;
         }
      }

      return var3;
   }

   private static boolean in(LiveAnimNode var0, String var1) {
      return var0.getSourceNode() != null && var0.getSourceNode().parentState != null && var1.equals(var0.getSourceNode().parentState.name);
   }
}
