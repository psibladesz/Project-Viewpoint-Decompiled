package viewpoint.game;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import zombie.core.skinnedmodel.ModelManager;
import zombie.core.skinnedmodel.animation.AnimationClip;
import zombie.core.skinnedmodel.animation.Keyframe;

public final class DiagonalWalk {
   private static final String RIGHT = "Bob_WalkNoAim1Hand_DiagR";
   private static final String LEFT = "Bob_WalkNoAim1Hand_DiagL";
   private static final String ROOT = "Translation_Data";
   private static AnimationClip fixed;

   public static void fix() {
      AnimationClip var0 = clip("Bob_WalkNoAim1Hand_DiagR");
      AnimationClip var1 = clip("Bob_WalkNoAim1Hand_DiagL");
      if (var0 != fixed) {
         fixed = var0;
         if (Math.signum(sideways(var0.getKeyframes())) != Math.signum(sideways(var1.getKeyframes()))) {
            System.out.println("[Viewpoint] Bob_WalkNoAim1Hand_DiagR already goes right: the game's clip needs no fix");
         } else {
            turnOver(var0.getKeyframes());
            System.out.println("[Viewpoint] Bob_WalkNoAim1Hand_DiagR: its root motion turned to the right");
         }
      }
   }

   private static AnimationClip clip(String var0) {
      AnimationClip var1 = ModelManager.instance.getAnimationClip(var0);
      if (var1 == null) {
         throw new IllegalStateException("the game has no animation clip " + var0);
      } else {
         return var1;
      }
   }

   static float sideways(Keyframe[] var0) {
      float var1 = Float.NaN;
      float var2 = Float.NaN;

      for (Keyframe var6 : var0) {
         if ("Translation_Data".equals(var6.boneName)) {
            var1 = Float.isNaN(var1) ? var6.position.x : var1;
            var2 = var6.position.x;
         }
      }

      if (Float.isNaN(var1)) {
         throw new IllegalStateException("an animation clip without its Translation_Data keys");
      } else {
         return var2 - var1;
      }
   }

   static void turnOver(Keyframe[] var0) {
      Set var1 = Collections.newSetFromMap(new IdentityHashMap());

      for (Keyframe var5 : var0) {
         if ("Translation_Data".equals(var5.boneName) && var1.add(var5.position)) {
            var5.position.x = -var5.position.x;
         }
      }
   }

   private DiagonalWalk() {
   }
}
