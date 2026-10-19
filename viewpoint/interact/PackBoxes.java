package viewpoint.interact;

import viewpoint.packs.ModelPacks;
import viewpoint.packs.PackBind;
import viewpoint.world.ChunkCache;
import viewpoint.world.MapRoofs;
import viewpoint.world.WorldMesher;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.sprite.IsoSprite;

final class PackBoxes {
   private static final float[] placed = new float[3];

   static boolean of(IsoObject var0, IsoGridSquare var1, float[] var2) {
      IsoSprite var3 = var0.getSprite();
      PackBind var4 = var3 != null && ChunkCache.packsOn(var1) && !MapRoofs.vanilla(var3) ? ModelPacks.bind(var3) : null;
      float[] var5 = var4 != null && var4.model.state() == 3 ? var4.model.bounds() : null;
      if (var5 == null) {
         return false;
      }

      boolean var6 = PackBind.turns(var3);
      float var7 = var1.x + 0.5F + var4.x;
      float var8 = var1.y + 0.5F + var4.y;
      float var9 = var1.z * 2.4494896F + WorldMesher.rise(var0) + var4.z;
      var2[0] = var2[1] = var2[2] = Float.MAX_VALUE;
      var2[3] = var2[4] = var2[5] = -Float.MAX_VALUE;

      for (int var10 = 0; var10 < 8; var10++) {
         var4.place(var5[(var10 & 1) * 3], var5[1 + (var10 >> 1 & 1) * 3], var5[2 + (var10 >> 2) * 3], var6, placed);
         float[] var11 = new float[]{var7 - placed[0], var8 - placed[2], var9 + placed[1]};

         for (int var12 = 0; var12 < 3; var12++) {
            var2[var12] = Math.min(var2[var12], var11[var12]);
            var2[var12 + 3] = Math.max(var2[var12 + 3], var11[var12]);
         }
      }

      return true;
   }

   private PackBoxes() {
   }
}
