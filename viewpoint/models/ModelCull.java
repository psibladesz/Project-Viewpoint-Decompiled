package viewpoint.models;

import viewpoint.core.View;
import viewpoint.input.FreeCam;
import viewpoint.input.Look;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Profile;
import viewpoint.visibility.Rooms;
import zombie.UpdateSchedulerSimulationLevel;
import zombie.ai.states.FakeDeadZombieState;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.characters.animals.IsoAnimal;
import zombie.core.Core;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoWorld;

public final class ModelCull {
   static volatile float range = 80.0F;
   private static final float MODEL_CLOSE = 12.0F;
   private static final double TURN_MARGIN = Math.toRadians(30.0);
   private static final float SIGHT_FREE = 30.0F;
   private static final float BODY_RADIUS = 0.4F;
   private static final float BODY_LEVELS = 0.8F;
   private static final float[] AWARE = new float[]{30.0F, 60.0F, 80.0F};
   private static final float[] UNAWARE = new float[]{12.0F, 24.0F, 48.0F};
   private static final UpdateSchedulerSimulationLevel[] LEVELS = new UpdateSchedulerSimulationLevel[]{
      UpdateSchedulerSimulationLevel.HALF, UpdateSchedulerSimulationLevel.QUARTER, UpdateSchedulerSimulationLevel.EIGHTH
   };
   private static final LiveSettings.Toggle THIN_UNAWARE = LiveSettings.toggle(
      "lod.unawareZombiesThinned", "Zombies without a target updated less often from 12 squares", "World/Levels of detail", true
   );
   private static boolean zombieCull;
   private static int asked;
   private static int offered;

   public static int cullScreenX(IsoZombie var0, int var1, int var2) {
      IsoPlayer var3 = IsoPlayer.players[0];
      if (View.enabled && var1 == 0 && var3 != null) {
         return wanted(var0, var3) ? 0 : -1000;
      } else {
         return var2;
      }
   }

   public static void zombieCull(boolean var0) {
      zombieCull = var0;
   }

   public static boolean headSquareSeen(IsoZombie var0, IsoPlayer var1, boolean var2) {
      return var2 || View.enabled && zombieCull && var1 == IsoPlayer.players[0] && sightFree(var0, var1);
   }

   private static boolean sightFree(IsoMovingObject var0, IsoPlayer var1) {
      return var0.DistToSquared(var1) <= 900.0F;
   }

   private static boolean wanted(IsoMovingObject var0, IsoPlayer var1) {
      asked++;
      FreeCam.Place var2 = FreeCam.centre();
      float var3 = var2 != null ? (float)var2.x() : var1.getX() + View.cameraX;
      float var4 = var2 != null ? (float)var2.y() : var1.getY() + View.cameraY;
      float var5 = var0.getX() - var3;
      float var6 = var0.getY() - var4;
      float var7 = var5 * var5 + var6 * var6;
      if (var7 > range * range) {
         return false;
      }

      boolean var8 = var7 <= 144.0F;
      boolean var9 = var8;
      if (!var8) {
         double var10 = (double)Core.getInstance().getScreenWidth() / Math.max(1, Core.getInstance().getScreenHeight());
         double var12 = Math.atan(Math.tan(View.frameFovY / 2.0) * var10) + TURN_MARGIN;
         double var14 = var2 != null ? var2.yaw() : Look.yaw;
         double var16 = Math.sqrt(var7);
         var8 = (var5 * Math.cos(var14) + var6 * Math.sin(var14)) / var16 >= Math.cos(Math.min(var12, Math.PI));
      }

      if (var8 && !var9 && roomHidden(var0)) {
         return false;
      }

      if (var8) {
         offered++;
      }

      return var8;
   }

   static boolean roomHidden(IsoMovingObject var0) {
      float var1 = var0.getX();
      float var2 = var0.getY();
      float var3 = var0.getZ();
      return Rooms.hides(var1 - 0.4F, var2 - 0.4F, var1 + 0.4F, var2 + 0.4F, (int)Math.floor(var3), (int)Math.floor(var3 + 0.8F));
   }

   public static UpdateSchedulerSimulationLevel simulationLevel(IsoMovingObject var0, UpdateSchedulerSimulationLevel var1) {
      IsoPlayer var2 = IsoPlayer.players[0];
      if (View.enabled && var2 != null && var0 != var2 && var1 != null) {
         float var3 = var0.DistTo(var2);
         boolean var4 = THIN_UNAWARE.get() && var0 instanceof IsoZombie var5 && var5.getTarget() == null;
         float[] var8 = var4 ? UNAWARE : AWARE;
         UpdateSchedulerSimulationLevel var6 = UpdateSchedulerSimulationLevel.FULL;

         for (int var7 = 0; var7 < var8.length && var3 > var8[var7]; var7++) {
            var6 = LEVELS[var7];
         }

         return var1.ordinal() <= var6.ordinal() ? var1 : var6.max(var0.getMinimumSimulationLevel());
      } else {
         return var1;
      }
   }

   public static void reportCull(IsoCell var0) {
      Profile.zombies(var0.getZombieList().size(), asked, offered);
      offered = 0;
      asked = 0;
   }

   public static int cullScreenY(int var0, int var1) {
      return View.enabled && var0 == 0 ? 0 : var1;
   }

   public static boolean cullAnimals(IsoWorld var0) {
      IsoPlayer var1 = IsoPlayer.players[0];
      if (View.enabled && var1 != null) {
         for (IsoMovingObject var3 : var0.getCell().getObjectList()) {
            if (var3 instanceof IsoAnimal var4) {
               IsoGridSquare var5 = var4.getCurrentSquare();
               boolean var6 = var5 != null
                  && wanted(var4, var1)
                  && (sightFree(var4, var1) || var4.getAlpha(0) != 0.0F && var4.legsSprite.def.alpha != 0.0F || var5.isCouldSee(0))
                  && !var4.isCurrentState(FakeDeadZombieState.instance());
               if (var6) {
                  var4.setSceneCulled(false);
               } else if (var4.hasActiveModel()) {
                  var4.setSceneCulled(true);
               }

               if (var4.hasAnimationPlayer()) {
                  var4.getAnimationPlayer().doBlending = var6;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private ModelCull() {
   }
}
