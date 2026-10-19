package viewpoint.gunplay;

import java.util.Random;
import viewpoint.core.View;
import viewpoint.input.AimAssist;
import viewpoint.input.CrosshairAim;
import viewpoint.input.Look;
import viewpoint.platform.LiveSettings;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoLivingCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.skills.PerkFactory.Perks;
import zombie.core.physics.BallisticsController;
import zombie.inventory.types.HandWeapon;
import zombie.iso.sprite.IsoReticle;
import zombie.network.fields.hit.AttackVars;
import zombie.network.fields.hit.HitInfo;
import zombie.scripting.objects.CharacterTrait;
import zombie.util.list.PZArrayList;

public final class Gunplay {
   private static final String SECTION = "Controls/Gunplay";
   static final LiveSettings.Toggle ON = LiveSettings.toggle("gunplay.aim", "Gunplay Rework (0.1.6)", "Controls/Gunplay", true);
   static final LiveSettings.Number RANGE = LiveSettings.number("gunplay.rangeScale", "Weapon range", "Controls/Gunplay", 1.0F, 3.0F, 0.1F, 2.0F);
   static final LiveSettings.Toggle RING = LiveSettings.toggle("gunplay.ring", "Show the aim ring", "Controls/Gunplay", true);
   static final LiveSettings.Number RING_OPACITY = LiveSettings.number("gunplay.ringOpacity", "Aim ring opacity", "Controls/Gunplay", 0.1F, 1.0F, 0.05F, 0.5F);
   private static final Sight steady = new Sight();
   private static final Sight line = new Sight();
   private static final Aim aim = new Aim();
   private static final Assist assist = new Assist();
   private static final Candidates recorded = new Candidates();
   private static final Gathering gathering = new Gathering();
   private static final double[] place = new double[3];
   private static final float[] game = new float[3];
   private static final long FRESH = 250000000L;
   private static final double LONGEST_STEP = 0.1;
   private static final Sway sway = new Sway();
   private static final Focus focus = new Focus();
   private static final float SETTLED = 0.5F;
   private static final float LEAD = 1.0F;
   private static final float STILL_KMH = 1.0F;
   private static final float DRAIN = 30.0F;
   private static final float AIMING_QUICK = 0.05F;
   private static final float MARKSMAN_QUICK = 0.1F;
   private static final Random phases = new Random();
   private static boolean aimed;
   private static double ring;
   private static long taking;
   private static long updated;
   private static IsoGameCharacter checking;

   public static void snapshot() {
      if (!aimed) {
         ShotDebug.idle();
         View.swayUp = 0.0F;
         View.swayRight = 0.0F;
         View.crosshairOnTarget = false;
      }

      assist.snapshot();
      aimed = false;
   }

   public static boolean reticle(IsoReticle var0) {
      if (!ON.get() || System.nanoTime() - updated >= 250000000L) {
         return false;
      }

      if (!RING.get()) {
         return true;
      }

      Reticle.set(var0, ring);
      return false;
   }

   public static float[] reticleArm(float var0, float var1, float var2, float var3) {
      return Reticle.arm(var0, var1, var2, var3);
   }

   public static void reticleDrawn() {
      Reticle.drawn();
   }

   public static void shotStarting(IsoGameCharacter var0, HandWeapon var1) {
      checking = var0;
      if (active(var0, var1)) {
         Shot.begin(var0, var1, focus.level());
         focus.reset();
      }
   }

   public static void shotEnded() {
      checking = null;
      Shot.end();
   }

   public static void gather(IsoGameCharacter var0, AttackVars var1, PZArrayList<HitInfo> var2) {
      HandWeapon var3 = var0 instanceof IsoLivingCharacter var4 ? var1.getWeapon(var4) : null;
      if (var3 != null && var3.isRanged() && active(var0, var3)) {
         gathering.add(var0, var3, place, Look.yaw, Look.pitch, var2);
      }
   }

   public static void candidates(IsoGameCharacter var0, HandWeapon var1, PZArrayList<HitInfo> var2) {
      Gathering.dedupe(var2);
      if (Shot.resolving(var0)) {
         Shot.resolve(var2);
      } else if (active(var0, var1)) {
         long var3 = System.nanoTime();
         recorded.take(var2);
         recorded.seated(var0, var1);
         taking = System.nanoTime() - var3;
      } else if (assisting(var0, var1) && AimAssist.wanted()) {
         recorded.take(var2);
      }
   }

   public static void hitListMade(IsoGameCharacter var0) {
      if (Shot.resolving(var0)) {
         Shot.chances(var0.getHitInfoList());
      } else if (var0 instanceof IsoLivingCharacter var1) {
         HandWeapon var2 = var0.getAttackVars().getWeapon(var1);
         boolean var3 = var2 != null && var2.isRangeFalloff() && var2.isAimedFirearm();
         if (var3 && ON.get() && View.enabled && var0 == IsoPlayer.players[0] && var0.getBallisticsController() != null) {
            Pattern.observe(var0.getBallisticsController(), var2);
         }

         if (var2 != null && active(var0, var2)) {
            update(var0, var2);
         } else if (var2 != null && assisting(var0, var2)) {
            assistOnly(var0);
         }
      }

      recorded.clear();
   }

   public static float[] missedShot(IsoGameCharacter var0, float var1, float var2, float var3) {
      if (Shot.resolving(var0)) {
         return Shot.missed();
      }

      game[0] = var1;
      game[1] = var2;
      game[2] = var3;
      return game;
   }

   public static boolean cameraTarget(BallisticsController var0, int var1, boolean var2) {
      return Shot.resolving(var0) ? Shot.target(var1, var2) : var2;
   }

   public static int pelletCount(BallisticsController var0, int var1, int var2) {
      return Shot.resolving(var0) ? Shot.pellets(var1, var2) : var2;
   }

   public static int targetedPart(BallisticsController var0, int var1, int var2) {
      return Shot.resolving(var0) ? Shot.part(var1, var2) : var2;
   }

   static boolean active(IsoGameCharacter var0, HandWeapon var1) {
      return ON.get() && aimsFirearm(var0, var1) && !Double.isNaN(pattern(var1)) && CrosshairAim.camera(place);
   }

   private static boolean assisting(IsoGameCharacter var0, HandWeapon var1) {
      return !ON.get() && checking != var0 && var0.getVehicle() == null && aimsFirearm(var0, var1) && CrosshairAim.camera(place);
   }

   private static boolean aimsFirearm(IsoGameCharacter var0, HandWeapon var1) {
      return View.enabled && var0 == IsoPlayer.players[0] && var1.isAimedFirearm();
   }

   static double pattern(HandWeapon var0) {
      return var0.isRangeFalloff() ? Pattern.spread(var0) * 2.0 : 0.0;
   }

   private static void update(IsoGameCharacter var0, HandWeapon var1) {
      long var2 = System.nanoTime();
      steady.set(place, Look.yaw, Look.pitch, 0.0, 0.0);
      aim.find(var0, var1, steady, recorded, pattern(var1));
      boolean var4 = var2 - updated >= 250000000L;
      assist.update(var0, steady, recorded, aim.from, var2);
      double var5 = var4 ? 0.0 : Math.min(0.1, (var2 - updated) * 1.0E-9);
      if (var4) {
         focus.reset();
      }

      int var7 = var0.getPerkLevel(Perks.Aiming);
      focus.advance(var5, var7, settled(var0));
      double var8 = focus.tighten(aim.radius, aim.floor(var0, var1, var7));
      if (var4) {
         sway.start(phases, var8);
      } else {
         sway.advance(var5, var8, focus.level());
      }

      updated = var2;
      line.set(place, Look.yaw, Look.pitch, sway.right(), sway.up());
      ring = sway.radius() + aim.pellet;
      View.crosshairOnTarget = aim.met(line, recorded) != -1;
      View.swayRight = (float)sway.right();
      View.swayUp = (float)sway.up();
      ShotDebug.aiming(ring);
      ShotTrace.updated(System.nanoTime() - var2 + taking);
      taking = 0L;
      aimed = true;
   }

   private static void assistOnly(IsoGameCharacter var0) {
      steady.set(place, Look.yaw, Look.pitch, 0.0, 0.0);
      assist.update(var0, steady, recorded, Aim.firstFrom(var0, steady), System.nanoTime());
   }

   private static boolean settled(IsoGameCharacter var0) {
      float var1 = 1.0F + 0.05F * var0.getPerkLevel(Perks.Aiming) + (var0.hasTrait(CharacterTrait.MARKSMAN) ? 0.1F : 0.0F);
      return var0.getAimingDelay() <= 0.5F + 30.0F * var1
         && var0.getBeenMovingFor() <= 30.5F
         && !(var0 instanceof IsoPlayer var2 && var2.isPlayerMoving())
         && (var0.getVehicle() == null || Math.abs(var0.getVehicle().getCurrentSpeedKmHour()) < 1.0F);
   }

   static double swayRight() {
      return fresh() ? sway.right() : 0.0;
   }

   static double swayUp() {
      return fresh() ? sway.up() : 0.0;
   }

   static double ring(Aim var0) {
      return fresh() ? ring : var0.radius + var0.pellet;
   }

   private static boolean fresh() {
      return System.nanoTime() - updated < 250000000L;
   }

   private Gunplay() {
   }

   static {
      ON.describe(
         "A shot goes exactly where the crosshair is, and hits what it meets, on the body part it meets. The crosshair wanders within the ring round the screen's middle, wider the lower your character's skill and the game's hit chance. Out of a vehicle's window it works too, and the game's own rules for such a shot still hold: only to the window's side, slower and wider while the vehicle moves. In third person the camera comes over your shoulder while you aim from the seat. When it is off, the game uses its original aiming system and hit chance."
      );
      RANGE.describe(
         "How much farther than in the isometric view a weapon's ranges reach in first and third person: its best range, how its hit chance falls off past it, and the point blank. The isometric view shows distances short, and the game's ranges are made for it. 1 is the game's own."
      );
      RING.describe(
         "Shows the ring round the screen's middle. The crosshair moves only inside this ring, and a smaller ring is a more accurate shot. Off, only the crosshair shows."
      );
      RING_OPACITY.describe("How opaque the aim ring is. 1 is fully opaque.");
   }
}
