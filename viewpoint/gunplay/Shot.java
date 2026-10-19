package viewpoint.gunplay;

import java.util.Random;
import org.joml.Vector3f;
import viewpoint.input.CrosshairAim;
import viewpoint.input.Look;
import zombie.characters.IsoGameCharacter;
import zombie.characters.animals.IsoAnimal;
import zombie.core.physics.BallisticsController;
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoMovingObject;
import zombie.iso.Vector3;
import zombie.iso.objects.IsoWindow;
import zombie.network.fields.hit.HitInfo;
import zombie.util.list.PZArrayList;
import zombie.vehicles.BaseVehicle;

final class Shot {
   private static final long STALE = 200000000L;
   private static final int SURE = 100;
   private static final Random random = new Random();
   private static final Sight line = new Sight();
   private static final Aim aim = new Aim();
   private static final Candidates candidates = new Candidates();
   private static final Hits hits = new Hits();
   private static final Pellets pellets = new Pellets();
   private static final Obstacles world = new Obstacles();
   private static final double[] place = new double[3];
   private static final double[] way = new double[3];
   private static final double[] drawn = new double[2];
   private static final double[] muzzle = new double[3];
   private static final double[] bullet = new double[3];
   private static final int[] part = new int[1];
   private static final float[] end = new float[3];
   private static final BaseVehicle[] vehicles = new BaseVehicle[32];
   private static final Vector3f from = new Vector3f();
   private static final Vector3f to = new Vector3f();
   private static final Vector3f met = new Vector3f();
   private static int vehicleCount;
   private static IsoGameCharacter owner;
   private static HandWeapon weapon;
   private static long began;
   private static double ring;
   private static double focus;
   private static double wall;
   private static int stopper;
   private static boolean resolved;

   static void begin(IsoGameCharacter var0, HandWeapon var1, double var2) {
      focus = var2;
      owner = var0;
      weapon = var1;
      began = System.nanoTime();
      resolved = false;
      hits.clear();
   }

   static boolean resolving(IsoGameCharacter var0) {
      return owner != null && var0 == owner && System.nanoTime() - began < 200000000L;
   }

   static boolean resolving(BallisticsController var0) {
      return owner != null && var0 == owner.getBallisticsController() && System.nanoTime() - began < 200000000L;
   }

   static void resolve(PZArrayList<HitInfo> var0) {
      candidates.take(var0);
      candidates.seated(owner, weapon);
      vehicleCount = 0;

      for (int var1 = 0; var1 < var0.size() && vehicleCount < vehicles.length; var1++) {
         if (((HitInfo)var0.get(var1)).getObject() instanceof BaseVehicle var2 && shootable(var2, (HitInfo)var0.get(var1))) {
            vehicles[vehicleCount++] = var2;
         }
      }

      draw();
      keep(var0);
      candidates.clear();

      for (int var4 = 0; var4 < vehicleCount; var4++) {
         vehicles[var4] = null;
      }

      vehicleCount = 0;
   }

   static void chances(PZArrayList<HitInfo> var0) {
      for (int var1 = 0; var1 < var0.size(); var1++) {
         HitInfo var2 = (HitInfo)var0.get(var1);
         int var3 = hits.index(var2.getObject());
         if (var3 >= 0) {
            var2.chance = 100;
            var2.x = hits.places[var3 * 3];
            var2.y = hits.places[var3 * 3 + 1];
            var2.z = hits.places[var3 * 3 + 2];
         }
      }

      if (weapon.isRangeFalloff() && owner.getBallisticsController() != null) {
         pellets.lay(owner.getBallisticsController(), hits);
      }
   }

   private static boolean shootable(BaseVehicle var0, HitInfo var1) {
      BaseVehicle var2 = owner.getVehicle();
      return var0 != var2 && (var2 == null || Odds.chance(owner, weapon, var1) > 0.0F);
   }

   static float[] missed() {
      if (!resolved) {
         candidates.clear();
         draw();
      }

      return end;
   }

   static boolean target(int var0, boolean var1) {
      return var1 || hits.indexOf(var0) >= 0;
   }

   static int part(int var0, int var1) {
      int var2 = hits.indexOf(var0);
      return var2 >= 0 ? hits.parts[var2] : var1;
   }

   static int pellets(int var0, int var1) {
      int var2 = hits.indexOf(var0);
      return var2 >= 0 ? hits.pellets[var2] : var1;
   }

   static void end() {
      if (owner != null && resolved) {
         String var0 = stopper == 0 ? null : String.format("%s %.1f squares off", Obstacles.NAMES[stopper], wall);
         ShotTrace.shot(aim, ring, focus, drawn, hits, var0);
      }

      owner = null;
      weapon = null;
      hits.clear();
   }

   private static void draw() {
      resolved = true;
      CrosshairAim.camera(place);
      line.set(place, Look.yaw, Look.pitch, 0.0, 0.0);
      double var0 = Gunplay.pattern(weapon);
      aim.find(owner, weapon, line, candidates, var0);
      ring = Gunplay.ring(aim);
      drawn[0] = Gunplay.swayRight();
      drawn[1] = Gunplay.swayUp();
      line.turned(drawn[0], drawn[1], way);
      double var2 = Double.POSITIVE_INFINITY;
      double var4 = weapon.getMaxRange(owner);
      int var6 = -1;
      int var7 = 11;

      for (int var8 = 0; var8 < candidates.count; var8++) {
         double var9 = Parts.meet(candidates.capsules[var8], line.x, line.y, line.up, way[0], way[1], way[2], aim.from, part);
         if (var9 < var2) {
            var2 = var9;
            var6 = var8;
            var7 = part[0];
         }
      }

      double var15 = var6 == -1 ? var4 : var2;
      double var10 = world.stop(line.x, line.y, line.up, way[0], way[1], way[2], aim.from, var15);
      if (var10 < var15) {
         var6 = -1;
         var15 = var10;
      }

      bullet(line.x + way[0] * var15, line.y + way[1] * var15, line.up + way[2] * var15, var4);
      IsoWindow var12 = wall > 0.0 ? world.window : null;
      double var13 = world.windowAt;
      ShotDebug.shot(line.x + way[0] * var15, line.y + way[1] * var15, line.up + way[2] * var15);
      if (weapon.isRangeFalloff()) {
         pellets.fire(random, muzzle, bullet, var0, weapon.getProjectileCount(), wall > 0.0 ? var4 : 0.0, candidates, vehicles, vehicleCount, hits, world);
      } else {
         hits(var6, var7);
      }

      hits.settle();
      ShotTrace.ungathered(owner, line, way, aim.from, var15, candidates);
      if (var12 != null && (hits.count == 0 || var13 < hits.at[0])) {
         var12.addBrokenGlass(owner);
         var12.smashWindow();
      }
   }

   private static void bullet(double var0, double var2, double var4, double var6) {
      BallisticsController var8 = owner.getBallisticsController();
      Vector3 var9 = var8 == null ? null : var8.getMuzzlePosition();
      boolean var10 = var9 != null && (var9.x != 0.0F || var9.y != 0.0F);
      muzzle[0] = var10 ? var9.x : line.x;
      muzzle[1] = var10 ? var9.y : line.y;
      muzzle[2] = var10 ? var9.z * 2.4494896F : line.up;
      double var11 = var0 - muzzle[0];
      double var13 = var2 - muzzle[1];
      double var15 = var4 - muzzle[2];
      double var17 = Math.sqrt(var11 * var11 + var13 * var13 + var15 * var15);
      bullet[0] = var17 > 0.0 ? var11 / var17 : way[0];
      bullet[1] = var17 > 0.0 ? var13 / var17 : way[1];
      bullet[2] = var17 > 0.0 ? var15 / var17 : way[2];
      wall = muzzleOpen() ? world.stop(muzzle[0], muzzle[1], muzzle[2], bullet[0], bullet[1], bullet[2], 0.0, var6) : 0.0;
      stopper = world.stopped;
      double var19 = Math.min(var6, wall);
      end[0] = (float)(muzzle[0] + bullet[0] * var19);
      end[1] = (float)(muzzle[1] + bullet[1] * var19);
      end[2] = (float)((muzzle[2] + bullet[2] * var19 - Ground.at(end[0], end[1])) / 2.4494896F);
   }

   private static boolean muzzleOpen() {
      double var0 = owner.getAimOriginPosX();
      double var2 = owner.getAimOriginPosY();
      double var4 = owner.getAimOriginPosZ() * 2.4494896F;
      double var6 = muzzle[0] - var0;
      double var8 = muzzle[1] - var2;
      double var10 = muzzle[2] - var4;
      double var12 = Math.sqrt(var6 * var6 + var8 * var8 + var10 * var10);
      return var12 == 0.0 || world.stop(var0, var2, var4, var6 / var12, var8 / var12, var10 / var12, 0.0, var12) >= var12;
   }

   private static void hits(int var0, int var1) {
      for (int var2 = 0; var2 < candidates.count; var2++) {
         double var3 = Parts.meet(candidates.capsules[var2], muzzle[0], muzzle[1], muzzle[2], bullet[0], bullet[1], bullet[2], 0.0, part);
         if (var3 == Double.POSITIVE_INFINITY && var2 == var0) {
            double var5 = candidates.middleX(var2) - muzzle[0];
            double var7 = candidates.middleY(var2) - muzzle[1];
            double var9 = candidates.middleUp(var2) - muzzle[2];
            var3 = Math.sqrt(var5 * var5 + var7 * var7 + var9 * var9);
            part[0] = var1;
         }

         if (var3 < wall) {
            hits.add(candidates.characters[var2], var3, part[0], muzzle, bullet);
         }
      }

      for (int var11 = 0; var11 < vehicleCount; var11++) {
         double var12 = vehicle(vehicles[var11]);
         if (var12 < wall) {
            hits.add(vehicles[var11], var12, 11, muzzle, bullet);
         }
      }

      hits.cut(weapon.isPiercingBullets() ? Math.max(1, weapon.getMaxHitCount()) : 1);
   }

   private static double vehicle(BaseVehicle var0) {
      double var1 = weapon.getMaxRange(owner);
      double var3 = Ground.under(var0);
      from.set((float)muzzle[0], (float)muzzle[1], (float)((muzzle[2] - var3) / 2.4494896F));
      to.set((float)(muzzle[0] + bullet[0] * var1), (float)(muzzle[1] + bullet[1] * var1), (float)((muzzle[2] + bullet[2] * var1 - var3) / 2.4494896F));
      if (var0.getIntersectPoint(from, to, met) == null) {
         return Double.POSITIVE_INFINITY;
      }

      double var5 = met.x - muzzle[0];
      double var7 = met.y - muzzle[1];
      double var9 = met.z * 2.4494896F + var3 - muzzle[2];
      return Math.sqrt(var5 * var5 + var7 * var7 + var9 * var9);
   }

   private static void keep(PZArrayList<HitInfo> var0) {
      for (int var1 = var0.size() - 1; var1 >= 0; var1--) {
         IsoMovingObject var2 = ((HitInfo)var0.get(var1)).getObject();
         boolean var3 = var2 instanceof BaseVehicle || var2 instanceof IsoGameCharacter && !(var2 instanceof IsoAnimal);
         boolean var4 = var3 ? hits.index(var2) >= 0 : !(var2 instanceof IsoAnimal) || hits.count == 0;
         if (!var4) {
            var0.remove(var1);
         }
      }
   }

   private Shot() {
   }
}
