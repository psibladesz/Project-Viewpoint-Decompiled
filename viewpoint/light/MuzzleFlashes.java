package viewpoint.light;

import viewpoint.core.Frame;
import viewpoint.render.SceneData;
import zombie.GameTime;
import zombie.characters.IsoGameCharacter;
import zombie.core.skinnedmodel.model.ModelInstance;
import zombie.iso.IsoCamera;
import zombie.iso.IsoGridSquare;
import zombie.iso.Vector3;
import zombie.scripting.objects.ModelAttachment;
import zombie.scripting.objects.ModelAttachmentId;

public final class MuzzleFlashes {
   private static final long NANOS = 80000000L;
   private static final float REACH = 12.0F;
   private static final float R = 1.0F;
   private static final float G = 0.8F;
   private static final float B = 0.5F;
   private static final float NIGHT_SHARE = 0.8F;
   private static final float LEAST = 0.2F;
   private static final int MAX = 4;
   private static final float HELD_REACH = 0.6F;
   private static final IsoGameCharacter[] shooters = new IsoGameCharacter[4];
   private static final long[] fired = new long[4];
   private static final float[] strength = new float[4];
   private static final Vector3 muzzle = new Vector3();
   private static final Vector3 way = new Vector3();

   public static void fired(IsoGameCharacter var0) {
      int var1 = 0;

      for (int var2 = 0; var2 < 4; var2++) {
         if (shooters[var2] == var0) {
            var1 = var2;
            break;
         }

         var1 = fired[var2] < fired[var1] ? var2 : var1;
      }

      shooters[var1] = var0;
      fired[var1] = System.nanoTime();
      strength[var1] = dark(var0);
   }

   static boolean snapshot(Frame var0, IsoGameCharacter var1) {
      long var2 = System.nanoTime();
      int var4 = -1;
      float var5 = Float.MAX_VALUE;

      for (int var6 = 0; var6 < 4; var6++) {
         if (shooters[var6] != null && var2 - fired[var6] >= 80000000L) {
            shooters[var6] = null;
         }

         if (shooters[var6] != null) {
            float var7 = shooters[var6] == var1 ? -1.0F : distance(shooters[var6], var0);
            if (var7 < var5 && var7 <= 36.0F) {
               var5 = var7;
               var4 = var6;
            }
         }
      }

      if (var4 < 0) {
         return false;
      }

      boolean var8 = put(var0, var4, var2);

      for (int var9 = 0; var9 < 4; var9++) {
         if (var9 != var4 && shooters[var9] != null && distance(shooters[var9], var0) <= 36.0F) {
            put(var0, var9, var2);
         }
      }

      return var8;
   }

   private static boolean put(Frame var0, int var1, long var2) {
      place(shooters[var1]);
      float var4 = 1.0F - (float)(var2 - fired[var1]) / 8.0E7F;
      float var5 = strength[var1] * var4 * var4;
      SceneData var6 = var0.scene;
      float var7 = muzzle.x;
      float var8 = muzzle.y;
      return Torches.round(
         var6,
         -(var7 - var0.camX),
         (muzzle.z - var0.camZ) * 2.4494896F + Lights.lift(var6, var7, var8),
         -(var8 - var0.camY),
         12.0F,
         1.0F * var5,
         0.8F * var5,
         0.5F * var5
      );
   }

   private static void place(IsoGameCharacter var0) {
      ModelInstance var1 = var0.primaryHandModel;
      ModelAttachment var2 = var1 != null && var1.modelScript != null && var1.animPlayer != null
         ? var1.modelScript.getAttachmentById(ModelAttachmentId.MUZZLE)
         : null;
      if (var2 != null) {
         var1.getAttachmentWorldPosition(var2, muzzle, way);
      } else {
         muzzle.set(
            var0.getAimOriginPosX() + var0.getForwardDirectionX() * 0.6F, var0.getAimOriginPosY() + var0.getForwardDirectionY() * 0.6F, var0.getAimOriginPosZ()
         );
      }
   }

   private static float dark(IsoGameCharacter var0) {
      float var1 = GameTime.getInstance().getNight() * 0.8F;
      IsoGridSquare var2 = var0.getCurrentSquare();
      if (var0.isInARoom() && var2 != null) {
         var1 = Math.max(var1, 1.0F - var2.getLightLevel(IsoCamera.frameState.playerIndex));
      }

      return Math.max(var1, 0.2F);
   }

   private static float distance(IsoGameCharacter var0, Frame var1) {
      float var2 = var0.getX() - var1.camX;
      float var3 = var0.getY() - var1.camY;
      float var4 = (var0.getZ() - var1.camZ) * 2.0F;
      return (float)Math.sqrt(var2 * var2 + var3 * var3 + var4 * var4);
   }

   private MuzzleFlashes() {
   }
}
