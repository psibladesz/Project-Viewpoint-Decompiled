package viewpoint;

import viewpoint.camera.HitStop;
import viewpoint.camera.Impulses;
import viewpoint.core.View;
import viewpoint.gunplay.Gunplay;
import viewpoint.input.Controls;
import viewpoint.input.PadAim;
import viewpoint.light.MuzzleFlashes;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.characters.component.CharacterInputComponent;
import zombie.core.physics.BallisticsController;
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoGridSquare;
import zombie.iso.sprite.IsoReticle;
import zombie.network.fields.hit.AttackVars;
import zombie.network.fields.hit.HitInfo;
import zombie.util.list.PZArrayList;

public final class CombatHooks {
   public static boolean skipIsoReticle(IsoReticle var0) {
      Compat.hit(8);
      boolean var1 = Controls.skipReticle(var0 == IsoReticle.getInstance(0));
      if (!var1 && View.enabled) {
         var1 = Gunplay.reticle(var0);
      }

      return var1;
   }

   public static float[] reticleArm(float var0, float var1, float var2, float var3) {
      Compat.hit(84);
      return Gunplay.reticleArm(var0, var1, var2, var3);
   }

   public static void isoReticleDone() {
      Controls.reticleDone();
      Gunplay.reticleDrawn();
   }

   public static int reticleX(int var0, int var1) {
      Compat.hit(14);
      return Controls.reticle(var0, var1, true);
   }

   public static int reticleY(int var0, int var1) {
      Compat.hit(15);
      return Controls.reticle(var0, var1, false);
   }

   public static void aimedFromReticle(IsoPlayer var0) {
      Compat.hit(58);
      Controls.aim(var0);
   }

   public static void muzzleFlash(IsoGameCharacter var0) {
      Compat.hit(61);
      if (View.enabled) {
         Impulses.shot(var0);
         MuzzleFlashes.fired(var0);
      }
   }

   public static void zombieHit(IsoZombie var0, HandWeapon var1, IsoGameCharacter var2, boolean var3) {
      Compat.hit(65);
      if (View.enabled) {
         Impulses.blow(var0, var2, var1, var3);
      }
   }

   public static void shotStarting(IsoGameCharacter var0, HandWeapon var1) {
      Compat.hit(67);
      Gunplay.shotStarting(var0, var1);
   }

   public static void shotEnded() {
      Gunplay.shotEnded();
   }

   public static void shotGather(IsoGameCharacter var0, AttackVars var1, PZArrayList<HitInfo> var2) {
      Compat.hit(91);
      Gunplay.gather(var0, var1, var2);
   }

   public static void shotCandidates(IsoGameCharacter var0, HandWeapon var1, PZArrayList<HitInfo> var2) {
      Compat.hit(68);
      Gunplay.candidates(var0, var1, var2);
   }

   public static void hitListMade(IsoGameCharacter var0) {
      Compat.hit(69);
      Gunplay.hitListMade(var0);
   }

   public static float[] missedShot(IsoGameCharacter var0, float var1, float var2, float var3) {
      Compat.hit(70);
      return Gunplay.missedShot(var0, var1, var2, var3);
   }

   public static boolean cameraTarget(BallisticsController var0, int var1, boolean var2) {
      Compat.hit(71);
      return Gunplay.cameraTarget(var0, var1, var2);
   }

   public static boolean barrelTarget(BallisticsController var0, int var1, boolean var2) {
      Compat.hit(73);
      return Gunplay.cameraTarget(var0, var1, var2);
   }

   public static int pelletCount(BallisticsController var0, int var1, int var2) {
      Compat.hit(74);
      return Gunplay.pelletCount(var0, var1, var2);
   }

   public static int targetedPart(BallisticsController var0, int var1, int var2) {
      Compat.hit(72);
      return Gunplay.targetedPart(var0, var1, var2);
   }

   public static void explosion(IsoGridSquare var0) {
      Compat.hit(62);
      if (View.enabled && var0 != null) {
         Impulses.explosion(var0.x, var0.y, var0.z);
      }
   }

   public static float animationTime(IsoGameCharacter var0, float var1) {
      Compat.hit(64);
      return View.enabled ? HitStop.delta(var0, var1) : var1;
   }

   private CombatHooks() {
   }

   public static boolean aimAxisApplied(CharacterInputComponent var0, boolean var1) {
      Compat.hit(85);
      return PadAim.axisApplied(var0, var1);
   }

   public static boolean precisionAimDown(CharacterInputComponent var0, boolean var1) {
      Compat.hit(87);
      return PadAim.precisionAimDown(var0, var1);
   }
}
