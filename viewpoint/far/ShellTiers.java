package viewpoint.far;

final class ShellTiers {
   static final float HOLD = 32.0F;

   private ShellTiers() {
   }

   static boolean shelled(float var0, float var1, boolean var2, float var3, float var4, float var5) {
      float var6 = var2 ? 32.0F : 0.0F;
      return var0 <= var5 + var6 && var0 < var4 && var1 >= var3 - var6;
   }

   static int tier(float var0, int var1, float var2, float var3, boolean var4) {
      float var5 = var1 == 0 ? var3 + 32.0F : var3;
      return (var0 < var5 || var4) && var0 < var2 ? 0 : 1;
   }

   static boolean exact(float var0, float var1, boolean var2) {
      return var0 < var1 + 64.0F + (var2 ? 32.0F : 0.0F);
   }
}
