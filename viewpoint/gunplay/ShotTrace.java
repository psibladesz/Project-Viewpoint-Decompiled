package viewpoint.gunplay;

import zombie.characters.IsoGameCharacter;
import zombie.characters.animals.IsoAnimal;
import zombie.core.physics.RagdollBodyPart;
import zombie.iso.IsoCell;
import zombie.iso.IsoMovingObject;
import zombie.iso.LosUtil;
import zombie.iso.LosUtil.TestResults;
import zombie.iso.sprite.IsoSprite;
import zombie.vehicles.BaseVehicle;

final class ShotTrace {
   private static final long EVERY_MS = 1000L;
   private static final double NEAR_RAY = 1.5;
   private static final double MIDDLE = 0.9;
   private static final float[] bones = new float[Parts.BONES.length * 3];
   private static final float[] capsules = new float[104];
   private static final int[] part = new int[1];
   private static long shotAt;
   private static long ungatheredAt;
   private static long aimStoppedAt;
   private static long aimNanos;
   private static int aims;

   static void updated(long var0) {
      aimNanos += var0;
      aims++;
   }

   static void shot(Aim var0, double var1, double var3, double[] var5, Hits var6, String var7) {
      long var8 = System.currentTimeMillis();
      if (var8 - shotAt >= 1000L) {
         shotAt = var8;
         StringBuilder var10 = new StringBuilder();

         for (int var11 = 0; var11 < var6.count; var11++) {
            var10.append(var11 == 0 ? "hit " : ", ");
            if (var6.objects[var11] instanceof BaseVehicle var12) {
               var10.append("vehicle ").append(var12.getId());
            } else {
               var10.append(((IsoGameCharacter)var6.objects[var11]).getID())
                  .append(' ')
                  .append(RagdollBodyPart.values()[var6.parts[var11]].name().replace("BODYPART_", ""));
            }

            if (var6.pellets[var11] > 1) {
               var10.append(" x").append(var6.pellets[var11]);
            }
         }

         System.out
            .printf(
               "[Viewpoint] shot: p %.2f, focus %.2f, ring %.2f deg, crosshair %.2f,%.2f deg, %.1f squares, %s; aim %.3f ms an update%n",
               var0.chance,
               var3,
               degrees(var1),
               degrees(var5[0]),
               degrees(var5[1]),
               var0.distance,
               var6.count > 0 ? var10 : (var7 == null ? "miss" : "miss, stopped by " + var7),
               aims == 0 ? 0.0 : aimNanos * 1.0E-6 / aims
            );
         aimNanos = 0L;
         aims = 0;
      }
   }

   static void ungathered(IsoGameCharacter var0, Sight var1, double[] var2, double var3, double var5, Candidates var7) {
      IsoCell var8 = var0.getCell();
      long var9 = System.currentTimeMillis();
      if (var8 != null && var9 - ungatheredAt >= 1000L) {
         IsoGameCharacter var11 = null;
         double var12 = var5;

         for (IsoMovingObject var15 : var8.getObjectList()) {
            if (var15 instanceof IsoGameCharacter var16 && var16 != var0 && !var16.isDead() && !(var16 instanceof IsoAnimal)) {
               double var17 = var16.getX() - var1.x;
               double var19 = var16.getY() - var1.y;
               double var21 = var16.getZ() * 2.4494896F + Ground.under(var16) + 0.9 - var1.up;
               double var23 = var17 * var2[0] + var19 * var2[1] + var21 * var2[2];
               if (!(var23 <= var3) && !(var23 >= var12) && !(var17 * var17 + var19 * var19 + var21 * var21 - var23 * var23 > 2.25) && !gathered(var16, var7)) {
                  Bodies.of(var16, bones, capsules);
                  double var25 = Parts.meet(capsules, var1.x, var1.y, var1.up, var2[0], var2[1], var2[2], var3, part);
                  if (var25 < var12) {
                     var12 = var25;
                     var11 = var16;
                  }
               }
            }
         }

         if (var11 != null) {
            ungatheredAt = var9;
            TestResults var27 = LosUtil.lineClear(
               var8,
               (int)Math.floor(var0.getX()),
               (int)Math.floor(var0.getY()),
               (int)Math.floor(var0.getZ()),
               (int)Math.floor(var11.getX()),
               (int)Math.floor(var11.getY()),
               (int)Math.floor(var11.getZ()),
               false
            );
            System.out
               .printf(
                  "[Viewpoint] shot: the ray met %d %.1f squares off, which the game had not gathered (squares between: %s)%n", var11.getID(), var12, var27
               );
         }
      }
   }

   static void aimStopped(IsoGameCharacter var0, double var1, Obstacles var3, double var4) {
      long var6 = System.currentTimeMillis();
      if (var6 - aimStoppedAt >= 1000L) {
         aimStoppedAt = var6;
         IsoSprite var8 = var3.stoppedSprite;
         System.out
            .printf(
               "[Viewpoint] aim: %d under the crosshair %.1f squares off, behind %s%s at %d,%d,%d %.1f squares off%n",
               var0.getID(),
               var1,
               Obstacles.NAMES[var3.stopped],
               var8 == null ? "" : " (" + var8.getName() + ")",
               var3.stoppedX,
               var3.stoppedY,
               var3.stoppedZ,
               var4
            );
      }
   }

   private static boolean gathered(IsoGameCharacter var0, Candidates var1) {
      for (int var2 = 0; var2 < var1.count; var2++) {
         if (var1.characters[var2] == var0) {
            return true;
         }
      }

      return false;
   }

   private static double degrees(double var0) {
      return Math.toDegrees(Math.atan(var0));
   }

   private ShotTrace() {
   }
}
