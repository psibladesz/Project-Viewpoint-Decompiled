package viewpoint.gunplay;

import viewpoint.input.AimAssist;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;

final class Assist {
   static final double HALF_WIDTH = 0.25;
   static final double WIDEST_AT = 2.0;
   static final double ACQUIRE = 4.0;
   static final double KEEP = 6.0;
   static final double SWITCH = 1.0;
   static final double SNAP_SECONDS = 0.25;
   static final double SNAP_REACH = Math.toRadians(7.0);
   static final double CHEST_LOW = 0.25;
   static final double CHEST_HIGH = 0.65;
   static final double RATE_TAU = 0.1;
   static final double MOST_RATE = 3.0;
   static final double LONGEST_STEP = 0.25;
   private static final int NONE = -1;
   private static final double NARROWEST = 1.0E-4;
   private static final double POINT = 1.0E-12;
   private final int[] ids = new int[32];
   private final double[][] axes = new double[32][6];
   private int count;
   private int held = -1;
   private double along;
   private double lastYaw;
   private double lastPitch;
   private double lastAt;
   private double yawRate;
   private double pitchRate;
   private double aimingSince = Double.NaN;
   private boolean updated;
   private final double[] aimOrigin = new double[3];
   private final double[] start = new double[3];
   private final double[] near = new double[2];
   private final double[] far = new double[2];
   private double place;
   private double width;
   private double angle;

   void update(IsoGameCharacter var1, Sight var2, Candidates var3, double var4, long var6) {
      double var8 = var6 * 1.0E-9;
      this.updated = true;
      this.aims(var8);
      if (!AimAssist.wanted()) {
         this.lost();
      } else {
         this.take(var3);
         this.aimOrigin[0] = var1.getAimOriginPosX();
         this.aimOrigin[1] = var1.getAimOriginPosY();
         this.aimOrigin[2] = var1.getAimOriginPosZ() * 2.4494896F;
         double var10 = AimAssist.snaps() ? this.snapUntil() : Double.NEGATIVE_INFINITY;
         AimAssist.publish(this.pick(var2, var4, this.aimOrigin, var8, var10));
      }
   }

   void aims(double var1) {
      if (Double.isNaN(this.aimingSince)) {
         this.aimingSince = var1;
      }
   }

   double snapUntil() {
      return this.aimingSince + 0.25;
   }

   void snapshot() {
      if (!this.updated) {
         IsoPlayer var1 = IsoPlayer.players[0];
         this.idle(var1 != null && var1.isAiming());
      }

      this.updated = false;
   }

   void idle(boolean var1) {
      this.lost();
      if (!var1) {
         this.aimingSince = Double.NaN;
      }
   }

   private void lost() {
      this.held = -1;
      AimAssist.publish(null);
   }

   private void take(Candidates var1) {
      this.count = 0;

      for (int var2 = 0; var2 < var1.count; var2++) {
         if (var1.characters[var2] instanceof IsoZombie var3 && !var3.isDead()) {
            this.add(var3.getID(), var1.bones[var2]);
         }
      }
   }

   void add(int var1, float[] var2) {
      double[] var3 = this.axes[this.count];

      for (int var4 = 0; var4 < 3; var4++) {
         var3[var4] = var2[0 + var4];
         var3[3 + var4] = var2[9 + var4];
      }

      this.ids[this.count++] = var1;
   }

   AimAssist.Target pick(Sight var1, double var2, double[] var4, double var5, double var7) {
      boolean var9 = var5 < var7;
      int var10 = -1;
      int var11 = -1;
      double var12 = Double.POSITIVE_INFINITY;
      double var14 = Double.POSITIVE_INFINITY;

      for (int var16 = 0; var16 < this.count; var16++) {
         double var17 = this.score(var1, var2, var16, var9);
         boolean var19 = var17 <= 4.0 || var9 && this.angle <= SNAP_REACH;
         if (this.ids[var16] == this.held && var17 <= 6.0) {
            var11 = var16;
            var14 = var17;
         }

         if (var19 && var17 < var12) {
            var10 = var16;
            var12 = var17;
         }
      }

      byte var20 = (byte)(var11 != -1 && !(var12 + 1.0 < var14) ? var11 : var10);
      AimAssist.Target var21 = var20 == -1 ? null : this.target(var1, var2, var4, var5, var20, var9 ? var7 : 0.0);
      this.count = 0;
      if (var21 == null) {
         this.held = -1;
      }

      return var21;
   }

   private AimAssist.Target target(Sight var1, double var2, double[] var4, double var5, int var7, double var8) {
      this.score(var1, var2, var7, var5 < var8);
      double[] var10 = this.axes[var7];
      double var11 = var5 - this.lastAt;
      if (this.ids[var7] == this.held && var11 > 0.0 && var11 <= 0.25) {
         double var13 = 1.0 - Math.exp(-var11 / 0.1);
         double var15 = yawFrom(var4, var10, this.along);
         double var17 = pitchFrom(var4, var10, this.along);
         this.yawRate = this.yawRate + (rate(Math.IEEEremainder(var15 - this.lastYaw, Math.PI * 2), var11) - this.yawRate) * var13;
         this.pitchRate = this.pitchRate + (rate(var17 - this.lastPitch, var11) - this.pitchRate) * var13;
      } else {
         this.yawRate = this.pitchRate = 0.0;
      }

      this.held = this.ids[var7];
      this.along = this.place;
      this.lastYaw = yawFrom(var4, var10, this.place);
      this.lastPitch = pitchFrom(var4, var10, this.place);
      this.lastAt = var5;
      this.start[0] = var1.x;
      this.start[1] = var1.y;
      this.start[2] = var1.up;
      return new AimAssist.Target(
         yawFrom(this.start, var10, this.place), pitchFrom(this.start, var10, this.place), this.width, this.yawRate, this.pitchRate, var5, var8
      );
   }

   private double score(Sight var1, double var2, int var4, boolean var5) {
      double[] var6 = this.axes[var4];
      if (var1.offset(var6[0], var6[1], var6[2], this.near) && var1.offset(var6[3], var6[4], var6[5], this.far)) {
         double var7 = this.far[0] - this.near[0];
         double var9 = this.far[1] - this.near[1];
         double var11 = var7 * var7 + var9 * var9;
         double var13 = var11 > 1.0E-12 ? -(this.near[0] * var7 + this.near[1] * var9) / var11 : 0.5;
         var13 = Math.max(var5 ? 0.25 : 0.0, Math.min(var5 ? 0.65 : 1.0, var13));
         double var15 = var1.along(var6[0] + var13 * (var6[3] - var6[0]), var6[1] + var13 * (var6[4] - var6[1]), var6[2] + var13 * (var6[5] - var6[2]));
         if (var15 <= var2) {
            return Double.POSITIVE_INFINITY;
         }

         this.place = var13;
         this.width = Math.max(1.0E-4, 0.25 / Math.max(var15, 2.0));
         this.angle = Math.hypot(this.near[0] + var13 * var7, this.near[1] + var13 * var9);
         return this.angle / this.width;
      } else {
         return Double.POSITIVE_INFINITY;
      }
   }

   private static double yawFrom(double[] var0, double[] var1, double var2) {
      return Math.atan2(var1[1] + var2 * (var1[4] - var1[1]) - var0[1], var1[0] + var2 * (var1[3] - var1[0]) - var0[0]);
   }

   private static double pitchFrom(double[] var0, double[] var1, double var2) {
      double var4 = var1[0] + var2 * (var1[3] - var1[0]) - var0[0];
      double var6 = var1[1] + var2 * (var1[4] - var1[1]) - var0[1];
      return Math.atan2(var1[2] + var2 * (var1[5] - var1[2]) - var0[2], Math.hypot(var4, var6));
   }

   private static double rate(double var0, double var2) {
      return Math.max(-3.0, Math.min(3.0, var0 / var2));
   }
}
