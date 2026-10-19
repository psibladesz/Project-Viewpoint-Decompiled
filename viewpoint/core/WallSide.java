package viewpoint.core;

import zombie.characters.IsoGameCharacter;
import zombie.iso.IsoGridSquare;

public final class WallSide {
   public static void keep(IsoGameCharacter var0, float var1, float var2, float var3, float[] var4) {
      var4[0] = var1;
      var4[1] = var2;
      IsoGridSquare var5 = var0.getCurrentSquare();
      if (var5 != null) {
         int var6 = (int)Math.floor(var1);
         int var7 = (int)Math.floor(var2);
         if (var6 != var5.x && shut(var0, var5, var6, var5.y)) {
            var4[0] = Math.max(var5.x + var3, Math.min(var5.x + 1.0F - var3, var1));
         }

         if (var7 != var5.y && shut(var0, var5, var5.x, var7)) {
            var4[1] = Math.max(var5.y + var3, Math.min(var5.y + 1.0F - var3, var2));
         }
      }
   }

   private static boolean shut(IsoGameCharacter var0, IsoGridSquare var1, int var2, int var3) {
      IsoGridSquare var4 = var0.getCell().getGridSquare(var2, var3, var1.z);
      return var4 == null || var1.isWallTo(var4) || var1.isDoorBlockedTo(var4);
   }

   private WallSide() {
   }
}
