package viewpoint.world;

import java.util.ArrayList;
import viewpoint.core.Frame;
import viewpoint.render.FlameDraws;
import viewpoint.render.SceneData;
import zombie.characters.IsoGameCharacter;
import zombie.core.textures.Texture;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.objects.IsoFire;
import zombie.iso.objects.IsoFireManager;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteInstance;

public final class Flames {
   private static final float REACH = 72.0F;
   private static final float ACROSS = (float)(Math.sqrt(2.0) / 128.0);
   private static final float UP = 0.01275776F;
   private static final float CHARACTER_LIFT = 0.25F;
   private static final float CHARACTER_PULL = 0.35F;
   private static final float[] foot = new float[3];
   private static final float[] rect = new float[4];
   private static final float[] uv = new float[4];
   private static final float[] tint = new float[4];

   public static void snapshot(Frame var0, IsoCell var1) {
      FlameDraws var2 = var0.scene.flames;
      var2.clear();

      for (int var3 = 0; var3 < IsoFireManager.FireStack.size() && !var2.full(); var3++) {
         fire(var0, var2, (IsoFire)IsoFireManager.FireStack.get(var3));
      }

      ArrayList var7 = var1.getStaticUpdaterObjectList();

      for (int var4 = 0; var4 < var7.size() && !var2.full(); var4++) {
         if (var7.get(var4) instanceof IsoFire var5) {
            fire(var0, var2, var5);
         }
      }

      ArrayList var8 = new ArrayList(IsoFireManager.CharactersOnFire_Stack);

      for (int var9 = 0; var9 < var8.size() && !var2.full(); var9++) {
         IsoGameCharacter var10 = (IsoGameCharacter)var8.get(var9);
         if (var10 != null && var10.isOnFire() && var10.getCurrentSquare() != null) {
            place(var0, var10.getX(), var10.getY(), var10.getZ(), 0.25F);
            cards(var2, var10, 0.35F);
         }
      }
   }

   private static void fire(Frame var0, FlameDraws var1, IsoFire var2) {
      IsoGridSquare var3 = var2 == null ? null : var2.getSquare();
      if (var3 != null && var2.getObjectIndex() >= 0) {
         place(var0, var3.x + 0.5F, var3.y + 0.5F, var3.z, 0.0F);
         cards(var1, var2, 0.0F);
      }
   }

   private static void place(Frame var0, float var1, float var2, float var3, float var4) {
      SceneData var5 = var0.scene;
      float var6 = var1 - var0.camX;
      float var7 = var2 - var0.camY;
      boolean var8 = var6 * var6 + var7 * var7 < 5184.0F;
      foot[0] = var8 ? -var6 : Float.NaN;
      foot[1] = (var3 - var0.camZ) * 2.4494896F + var4 + (var5.relief == null ? 0.0F : var5.relief.scene(var1, var2));
      foot[2] = -var7;
   }

   private static void cards(FlameDraws var0, IsoObject var1, float var2) {
      ArrayList var3 = var1.getAttachedAnimSprite();

      for (int var4 = 0; var3 != null && var4 < var3.size() && !Float.isNaN(foot[0]); var4++) {
         IsoSpriteInstance var5 = (IsoSpriteInstance)var3.get(var4);
         IsoSprite var6 = var5 == null ? null : var5.parentSprite;
         int var7 = var6 != null && var6.hasAnimation() ? var6.getFrameCount() : 0;
         int var8 = Math.max(0, Math.min(var7 - 1, (int)var5.frame));
         Texture var9 = var7 == 0 ? null : var6.getTextureForFrame(var8, var1.getDir());
         String var10 = var9 == null ? null : var9.getName();
         boolean var11 = var10 != null && var10.toLowerCase().contains("fire");
         if (var10 != null && (var11 || var10.toLowerCase().contains("smoke")) && var9.getWidthOrig() > 0) {
            float var12 = (var9.getOffsetX() - var9.getWidthOrig() * 0.5F) * ACROSS;
            float var13 = (var9.getHeightOrig() - var9.getOffsetY() - var9.getHeight()) * 0.01275776F;
            rect[0] = var12;
            rect[1] = var13;
            rect[2] = var12 + var9.getWidth() * ACROSS;
            rect[3] = var13 + var9.getHeight() * 0.01275776F;
            uv[0] = var9.getXStart();
            uv[1] = var9.getYStart();
            uv[2] = var9.getXEnd();
            uv[3] = var9.getYEnd();
            tint[0] = var6.tintMod.r;
            tint[1] = var6.tintMod.g;
            tint[2] = var6.tintMod.b;
            tint[3] = var11 ? IsoFireManager.fireAlpha : IsoFireManager.smokeAlpha;
            var0.add(var9, uv, foot, rect, var2, tint, var11);
         }
      }
   }

   private Flames() {
   }
}
