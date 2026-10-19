package viewpoint.packs;

import viewpoint.render.PackModel;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.iso.sprite.IsoSprite;

public final class PackBind {
   public final PackModel model;
   public final float cos;
   public final float sin;
   public final float x;
   public final float y;
   public final float z;
   public final float scale;

   public PackBind(PackModel var1, float var2, float var3, float var4, float var5, float var6) {
      double var7 = Math.toRadians(var2);
      this.model = var1;
      this.cos = (float)Math.cos(var7);
      this.sin = (float)Math.sin(var7);
      this.x = var3;
      this.y = var4;
      this.z = var5;
      this.scale = var6;
   }

   public static boolean turns(IsoSprite var0) {
      IsoObjectType var1 = var0.getType();
      return var1 != IsoObjectType.stairsBN
         && var1 != IsoObjectType.stairsMN
         && var1 != IsoObjectType.stairsTN
         && var1 != IsoObjectType.stairsBW
         && var1 != IsoObjectType.stairsMW
         && var1 != IsoObjectType.stairsTW;
   }

   public void place(float var1, float var2, float var3, boolean var4, float[] var5) {
      float var6 = var1 * this.scale;
      float var7 = var3 * this.scale;
      float var8 = var4 ? this.cos : 1.0F;
      float var9 = var4 ? this.sin : 0.0F;
      var5[0] = var6 * var8 - var7 * var9;
      var5[1] = var2 * this.scale;
      var5[2] = var6 * var9 + var7 * var8;
   }
}
