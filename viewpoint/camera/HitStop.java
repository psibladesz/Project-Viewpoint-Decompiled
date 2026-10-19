package viewpoint.camera;

import java.util.Arrays;
import zombie.characters.IsoGameCharacter;

public final class HitStop {
   static final float PAYBACK = 0.3F;
   static final float MOST = 0.12F;
   private static final int TARGETS = 4;
   private static final long STALE = 250000000L;
   private static final IsoGameCharacter[] targets = new IsoGameCharacter[4];
   private static IsoGameCharacter striker;
   private static float freeze;
   private static float owed;
   private static volatile float factor = 1.0F;
   private static volatile long tickedNanos;

   static void hold(float var0, IsoGameCharacter var1, IsoGameCharacter var2) {
      if (!(var0 <= 0.0F)) {
         freeze = Math.max(freeze, Math.min(0.12F, var0));
         striker = var1;

         for (int var3 = 0; var3 < 4; var3++) {
            if (targets[var3] == var2) {
               return;
            }

            if (targets[var3] == null) {
               targets[var3] = var2;
               return;
            }
         }
      }
   }

   static void tick(float var0) {
      tickedNanos = System.nanoTime();
      if (!(var0 <= 0.0F)) {
         float var1 = Math.min(freeze, var0);
         float var2 = var0 - var1;
         float var3 = Math.min(owed, 0.3F * var2);
         freeze -= var1;
         owed += var1 - var3;
         factor = (var2 + var3) / var0;
         if (freeze <= 0.0F && owed <= 0.0F) {
            striker = null;
            Arrays.fill(targets, null);
         }
      }
   }

   static float factor() {
      return factor;
   }

   public static float delta(IsoGameCharacter var0, float var1) {
      return factor != 1.0F && System.nanoTime() - tickedNanos <= 250000000L && held(var0) ? var1 * factor : var1;
   }

   private static boolean held(IsoGameCharacter var0) {
      if (var0 == striker) {
         return true;
      }

      for (IsoGameCharacter var4 : targets) {
         if (var4 == var0) {
            return true;
         }
      }

      return false;
   }

   private HitStop() {
   }
}
