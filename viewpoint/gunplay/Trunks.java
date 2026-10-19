package viewpoint.gunplay;

import java.util.function.IntPredicate;
import viewpoint.world.TileMeshes;
import zombie.core.textures.Texture;
import zombie.iso.objects.IsoTree;
import zombie.iso.sprite.IsoSprite;

final class Trunks {
   private static final int NEAR = 2;
   private static boolean unmasked;

   static double radius(IsoTree var0, double var1) {
      IsoSprite var3 = var0.getSprite();
      Texture var4 = var3 == null ? null : var3.getTextureForCurrentFrame(var0.getDir(), var0);
      if (var4 == null) {
         return 0.0;
      }

      if (var4.getMask() == null) {
         if (!unmasked) {
            unmasked = true;
            System.out.println("[Viewpoint] gunplay: tree art " + var3.name + " has no mask: its trunk stops nothing");
         }

         return 0.0;
      } else {
         int var5 = (int)Math.floor(TileMeshes.plantFoot(var4) - var1 / TileMeshes.plantUp(var4));
         int var6 = (int)var4.getOffsetX();
         int var7 = (int)var4.getOffsetY();
         if (!(var1 < 0.0) && var5 >= var7 && var5 < var7 + var4.getHeight()) {
            int var8 = (int)(var4.getWidthOrig() * 0.5F);
            return halfWidth(var2 -> var4.isMaskSet(var2, var5), var8, var6, var6 + var4.getWidth()) * TileMeshes.plantAcross(var4);
         } else {
            return 0.0;
         }
      }
   }

   static double halfWidth(IntPredicate var0, int var1, int var2, int var3) {
      int var4 = -1;

      for (int var5 = 0; var5 <= 2 && var4 < 0; var5++) {
         if (inside(var1 - var5, var2, var3) && var0.test(var1 - var5)) {
            var4 = var1 - var5;
         } else if (inside(var1 + var5, var2, var3) && var0.test(var1 + var5)) {
            var4 = var1 + var5;
         }
      }

      if (var4 < 0) {
         return 0.0;
      }

      int var7 = var4;
      int var6 = var4;

      while (inside(var7 - 1, var2, var3) && var0.test(var7 - 1)) {
         var7--;
      }

      while (inside(var6 + 1, var2, var3) && var0.test(var6 + 1)) {
         var6++;
      }

      return (var6 - var7 + 1) / 2.0;
   }

   private static boolean inside(int var0, int var1, int var2) {
      return var0 >= var1 && var0 < var2;
   }

   private Trunks() {
   }
}
