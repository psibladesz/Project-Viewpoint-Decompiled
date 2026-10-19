package viewpoint.world;

import zombie.iso.sprite.IsoSprite;

final class ShoreBlends {
   private static final String WATER_ART = "blends_natural_02_";
   private static final int LAST = 12;

   private ShoreBlends() {
   }

   static boolean blend(IsoSprite var0) {
      String var1 = var0 == null ? null : var0.name;
      if (var1 != null && var1.startsWith("blends_natural_02_") && var1.length() <= "blends_natural_02_".length() + 2) {
         int var2 = 0;

         for (int var3 = "blends_natural_02_".length(); var3 < var1.length(); var3++) {
            char var4 = var1.charAt(var3);
            if (var4 < '0' || var4 > '9') {
               return false;
            }

            var2 = var2 * 10 + var4 - 48;
         }

         return var1.length() > "blends_natural_02_".length() && var2 <= 12;
      } else {
         return false;
      }
   }
}
