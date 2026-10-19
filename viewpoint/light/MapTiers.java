package viewpoint.light;

import viewpoint.platform.Tuning;

final class MapTiers {
   static final float NEAR = 0.5F;
   static final float MIDDLE = 0.25F;
   static final float FAR = 0.125F;

   static float side(float var0, boolean var1) {
      return !var1 && !(var0 <= Tuning.mapNear) ? (var0 <= Tuning.mapMiddle ? 0.25F : 0.125F) : 0.5F;
   }

   static int period(float var0) {
      return var0 >= 0.5F ? 1 : (var0 >= 0.25F ? 2 : 4);
   }

   static boolean due(long var0, int var2, int var3) {
      return (var0 + var2) % var3 == 0L;
   }

   private MapTiers() {
   }
}
