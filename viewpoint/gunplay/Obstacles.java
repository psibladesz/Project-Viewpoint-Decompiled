package viewpoint.gunplay;

import viewpoint.visibility.Edges;
import viewpoint.world.TileMesh;
import viewpoint.world.TileMeshes;
import viewpoint.world.WorldMesher;
import zombie.core.textures.Texture;
import zombie.iso.IsoCell;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.iso.objects.IsoBarricade;
import zombie.iso.objects.IsoDoor;
import zombie.iso.objects.IsoThumpable;
import zombie.iso.objects.IsoTree;
import zombie.iso.objects.IsoWindow;
import zombie.iso.sprite.IsoSprite;
import zombie.util.list.PZArrayList;

final class Obstacles {
   static final int NOTHING = 0;
   static final int WALL = 1;
   static final int DOOR = 2;
   static final int BARRICADE = 3;
   static final int FLOOR = 4;
   static final int SOLID = 5;
   static final int TRUNK = 6;
   static final String[] NAMES = new String[]{"nothing", "a wall", "a door", "a barricade", "a floor", "a solid square", "a tree's trunk"};
   private static final double STOREY = 2.4494896F;
   private static final float[] WHOLE = new float[]{-0.5F, 0.0F, -0.5F, 0.5F, 2.4494896F, 0.5F};
   private final SquareWalk walk = new SquareWalk();
   int stopped;
   int stoppedX;
   int stoppedY;
   int stoppedZ;
   IsoSprite stoppedSprite;
   IsoWindow window;
   double windowAt;
   private double px;
   private double py;
   private double pu;
   private double fx;
   private double fy;
   private double fu;

   double stop(double var1, double var3, double var5, double var7, double var9, double var11, double var13, double var15) {
      IsoCell var17 = IsoWorld.instance.getCell();
      this.stopped = 0;
      this.stoppedSprite = null;
      this.window = null;
      if (var17 != null && var15 > var13) {
         this.px = var1;
         this.py = var3;
         this.pu = var5;
         this.fx = var7;
         this.fy = var9;
         this.fu = var11;
         this.walk.start(var1, var3, var7, var9, var13, var15);

         while (this.walk.next()) {
            if (this.walk.edge != 0 && this.edge(var17, this.walk.edgeX, this.walk.edgeY, this.walk.edge == 2)) {
               return this.at(this.walk.edgeX, this.walk.edgeY, this.walk.in);
            }

            double var18 = this.inside(var17, this.walk.in, this.walk.out, this.walk.in == var13);
            if (var18 < this.walk.out) {
               return this.at(this.walk.x, this.walk.y, var18);
            }
         }

         return var15;
      } else {
         return var15;
      }
   }

   private boolean edge(IsoCell var1, int var2, int var3, boolean var4) {
      IsoGridSquare var5 = var1.getGridSquare(var2, var3, level(this.height(this.walk.in)));
      if (var5 == null) {
         return false;
      }

      int var6 = Edges.edge(var5, var4);
      this.stopped = var6 == 1 ? 1 : (var6 == 3 && shut(var5.getDoor(Edges.facing(var4))) ? 2 : 0);
      if (this.stopped == 0 && var6 != 0 && barricaded(var1, var5, var4)) {
         this.stopped = 3;
      }

      IsoWindow var7 = var6 == 4 && this.stopped == 0 ? var5.getWindow(Edges.facing(var4)) : null;
      if (this.window == null && var7 != null && !var7.isDestroyed() && !var7.IsOpen()) {
         this.window = var7;
         this.windowAt = this.walk.in;
      }

      return this.stopped != 0;
   }

   private static boolean shut(IsoObject var0) {
      return var0 instanceof IsoDoor var2
         ? !var2.IsOpen() && !var2.isDestroyed()
         : var0 instanceof IsoThumpable var1 && var1.isDoor() && !var1.IsOpen() && !var1.isDestroyed();
   }

   private static boolean barricaded(IsoCell var0, IsoGridSquare var1, boolean var2) {
      IsoGridSquare var3 = var0.getGridSquare(var1.x - (var2 ? 0 : 1), var1.y - (var2 ? 1 : 0), var1.z);
      return blocks(IsoBarricade.GetBarricadeOnSquare(var1, var2 ? IsoDirections.N : IsoDirections.W))
         || blocks(IsoBarricade.GetBarricadeOnSquare(var3, var2 ? IsoDirections.S : IsoDirections.E));
   }

   private static boolean blocks(IsoBarricade var0) {
      return var0 != null && !var0.isDestroyed() && var0.isBlockVision();
   }

   private double inside(IsoCell var1, double var2, double var4, boolean var6) {
      double var7 = this.height(var2);
      double var9 = this.height(var4);
      int var11 = level(var7);
      int var12 = level(var9);
      int var13 = var12 >= var11 ? 1 : -1;
      int var14 = var11;

      while (true) {
         double var15 = var14 == var11 ? var2 : at(var2, var4, var7, var9, (var13 > 0 ? var14 : var14 + 1) * 2.4494896F);
         double var17 = var14 == var12 ? var4 : at(var2, var4, var7, var9, (var13 > 0 ? var14 + 1 : var14) * 2.4494896F);
         if (var14 != var11 && this.floor(var1, this.walk.x, this.walk.y, var13 > 0 ? var14 : var14 + 1)) {
            this.stopped = 4;
            return var15;
         }

         double var19 = this.contents(var1.getGridSquare(this.walk.x, this.walk.y, var14), var14, var15, var17, var6 && var14 == var11);
         if (var19 < var17 || var14 == var12) {
            return var19 < var17 ? var19 : var4;
         }

         var14 += var13;
      }
   }

   private double contents(IsoGridSquare var1, int var2, double var3, double var5, boolean var7) {
      if (var1 == null) {
         return var5;
      }

      IsoTree var8 = var1.has(IsoObjectType.tree) ? var1.getTree() : null;
      if (var8 != null) {
         return this.trunk(var8, var1.x + 0.5, var1.y + 0.5, var2, var3, var5, var7);
      }

      double var9 = var1.isSolid() ? this.solid(var1, var2, var3, var5) : var5;
      if (var9 < var5) {
         this.stopped = 5;
      }

      return var9;
   }

   private double solid(IsoGridSquare var1, int var2, double var3, double var5) {
      double var7 = var5;
      double var9 = var1.x + 0.5;
      double var11 = var1.y + 0.5;
      double var13 = this.pu - Ground.at(var9, var11) - var2 * 2.4494896F;
      PZArrayList var15 = var1.getObjects();

      for (int var16 = 0; var16 < var15.size(); var16++) {
         IsoObject var17 = (IsoObject)var15.get(var16);
         IsoSprite var18 = var17.getSprite();
         if (var18 != null && var18.getProperties().has(IsoFlagType.solid)) {
            Texture var19 = var18.getTextureForCurrentFrame(var17.getDir(), var17);
            TileMesh var20 = var19 == null ? null : TileMeshes.get(var18, var19, null);
            float[] var21 = var20 != null && var20.vertCount != 0 ? var20.bounds : WHOLE;
            double var22 = box(this.px - var9, var13 - WorldMesher.rise(var17), this.py - var11, this.fx, this.fu, this.fy, var21, var3, var7);
            if (var22 < var7) {
               var7 = var22;
               this.stoppedSprite = var18;
            }
         }
      }

      return var7;
   }

   static double box(double var0, double var2, double var4, double var6, double var8, double var10, float[] var12, double var13, double var15) {
      double var17 = var13;
      double var19 = var15;
      double[] var21 = new double[]{var0, var2, var4};
      double[] var22 = new double[]{var6, var8, var10};

      for (int var23 = 0; var23 < 3; var23++) {
         if (var22[var23] == 0.0) {
            if (var21[var23] < var12[var23] || var21[var23] > var12[var23 + 3]) {
               return var15;
            }
         } else {
            double var24 = (var12[var23] - var21[var23]) / var22[var23];
            double var26 = (var12[var23 + 3] - var21[var23]) / var22[var23];
            var17 = Math.max(var17, Math.min(var24, var26));
            var19 = Math.min(var19, Math.max(var24, var26));
         }
      }

      return var17 <= var19 ? var17 : var15;
   }

   private double trunk(IsoTree var1, double var2, double var4, int var6, double var7, double var9, boolean var11) {
      double var12 = this.fx * this.fx + this.fy * this.fy;
      double var14 = var12 == 0.0 ? var7 : ((var2 - this.px) * this.fx + (var4 - this.py) * this.fy) / var12;
      var14 = Math.max(var7, Math.min(var9, var14));
      double var16 = this.px + this.fx * var14 - var2;
      double var18 = this.py + this.fy * var14 - var4;
      double var20 = var16 * var16 + var18 * var18;
      double var22 = Trunks.radius(var1, this.height(var14) - var6 * 2.4494896F);
      if (var20 >= var22 * var22) {
         return var9;
      }

      double var24 = var12 == 0.0 ? var7 : var14 - Math.sqrt((var22 * var22 - var20) / var12);
      if (var24 < var7 && var11) {
         return var9;
      }

      this.stopped = 6;
      return Math.max(var7, var24);
   }

   private double at(int var1, int var2, double var3) {
      this.stoppedX = var1;
      this.stoppedY = var2;
      this.stoppedZ = level(this.height(var3));
      return var3;
   }

   private boolean floor(IsoCell var1, int var2, int var3, int var4) {
      IsoGridSquare var5 = var1.getGridSquare(var2, var3, var4);
      return var5 != null && var5.TreatAsSolidFloor();
   }

   private double height(double var1) {
      return this.pu + this.fu * var1 - Ground.at(this.px + this.fx * var1, this.py + this.fy * var1);
   }

   private static int level(double var0) {
      return (int)Math.floor(var0 / 2.4494896F);
   }

   private static double at(double var0, double var2, double var4, double var6, double var8) {
      return var6 == var4 ? var0 : var0 + (var8 - var4) / (var6 - var4) * (var2 - var0);
   }
}
