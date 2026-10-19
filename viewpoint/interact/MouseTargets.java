package viewpoint.interact;

import viewpoint.render.MousePick;
import viewpoint.visibility.Edges;
import zombie.characters.IsoPlayer;
import zombie.core.Core;
import zombie.input.Mouse;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.iso.IsoObjectPicker.ClickObject;
import zombie.iso.objects.IsoCurtain;
import zombie.iso.objects.IsoDeadBody;
import zombie.iso.objects.IsoDoor;
import zombie.iso.objects.IsoThumpable;
import zombie.iso.objects.IsoTree;
import zombie.iso.objects.IsoWindow;
import zombie.iso.objects.IsoWindowFrame;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.iso.sprite.IsoSprite;
import zombie.vehicles.BaseVehicle;

public final class MouseTargets {
   public static final int DOOR = 0;
   public static final int WINDOW = 1;
   public static final int WINDOW_FRAME = 2;
   public static final int THUMPABLE = 3;
   public static final int HOPPABLE = 4;
   public static final int CORPSE = 5;
   public static final int TREE = 6;
   private static final float BACK = 0.02F;
   private static final float EDGE = 0.08F;
   private static final float ON_FLOOR = 0.03F;
   private static final float BODY_REACH = 0.6F;
   private static final float BODY_LEVELS = 0.8F;
   private static final float ITEM_REACH = 0.35F;
   private static final int STILL = 3;
   private static final ClickObject click = new ClickObject();
   private static MousePick.Hit lastHit;
   private static IsoObject lastObject;
   private static IsoGridSquare lastSquare;

   public static void frame(boolean var0) {
      if (!var0) {
         MousePick.ask = null;
      } else {
         int var1 = Mouse.getXA();
         int var2 = Mouse.getYA();
         int var3 = Core.getInstance().getScreenWidth();
         int var4 = Core.getInstance().getScreenHeight();
         MousePick.Ask var5 = MousePick.ask;
         if (var5 == null || var5.x() != var1 || var5.y() != var2 || var5.screenWidth() != var3 || var5.screenHeight() != var4) {
            MousePick.ask = new MousePick.Ask(var1, var2, var3, var4);
         }
      }
   }

   public static boolean on() {
      return MousePick.ask != null;
   }

   public static ClickObject contextPick(int var0, int var1) {
      IsoObject var2 = object();
      if (var2 != null && var2.getSquare() != null) {
         click.square = var2.getSquare();
         click.tile = var2;
         click.x = var0;
         click.y = var1;
         click.width = click.height = 1;
         click.lx = click.ly = 0;
         click.scaleX = click.scaleY = 1.0F;
         click.flip = false;
         click.score = 0;
         click.z = click.square.getZ();
         return click;
      } else {
         return null;
      }
   }

   public static IsoObject picked(int var0) {
      IsoObject var1 = object();
      if (var0 == 5) {
         return (IsoObject)(var1 instanceof IsoDeadBody ? var1 : (lastSquare == null ? null : lastSquare.getDeadBody()));
      }

      boolean var2 = switch (var0) {
         case 0 -> {
            if (!(var1 instanceof IsoDoor) && !(var1 instanceof IsoThumpable var3 && var3.isDoor())) {
               yield false;
            }

            yield true;
         }
         case 1 -> var1 instanceof IsoWindow;
         case 2 -> var1 instanceof IsoWindowFrame;
         case 3 -> var1 instanceof IsoThumpable;
         case 4 -> var1 != null && var1.isHoppable();
         default -> false;
         case 6 -> var1 instanceof IsoTree;
      };
      return var2 ? var1 : null;
   }

   public static BaseVehicle vehicle() {
      object();
      return lastSquare == null ? null : lastSquare.getVehicleContainer();
   }

   public static Double world(boolean var0) {
      MousePick.Hit var1 = fresh();
      return var1 == null ? null : var0 ? var1.x() : var1.y();
   }

   private static MousePick.Hit fresh() {
      MousePick.Hit var0 = MousePick.hit;
      return var0 != null && Math.abs(var0.ask().x() - Mouse.getXA()) <= 3 && Math.abs(var0.ask().y() - Mouse.getYA()) <= 3 ? var0 : null;
   }

   private static IsoObject object() {
      MousePick.Hit var0 = fresh();
      if (var0 != lastHit) {
         lastHit = var0;
         lastSquare = null;
         lastObject = var0 == null ? null : find(var0, IsoWorld.instance.getCell());
      }

      return lastObject;
   }

   private static IsoObject find(MousePick.Hit var0, IsoCell var1) {
      double var2 = var0.x() - var0.dirX() * 0.02F;
      double var4 = var0.y() - var0.dirY() * 0.02F;
      double var6 = var0.z() - var0.dirUp() * 0.02F / 2.4494896F;
      int var8 = (int)Math.floor(var2);
      int var9 = (int)Math.floor(var4);
      int var10 = (int)Math.floor(var6);
      IsoGridSquare var11 = var1 == null ? null : var1.getGridSquare(var8, var9, var10);
      lastSquare = var11;
      if (var11 == null) {
         return null;
      } else {
         IsoMovingObject var12 = body(var1, var2, var4, var6, var8, var9, var10);
         if (var12 != null) {
            return var12;
         } else {
            double var13 = var2 - var8;
            double var15 = var4 - var9;
            IsoObject var17 = var13 < 0.08F ? edge(var11, 2) : (var13 > 0.9200000017881393 ? edge(var1.getGridSquare(var8 + 1, var9, var10), 2) : null);
            var17 = var17 != null
               ? var17
               : (var15 < 0.08F ? edge(var11, 1) : (var15 > 0.9200000017881393 ? edge(var1.getGridSquare(var8, var9 + 1, var10), 1) : null));
            if (var17 != null) {
               return var17;
            } else {
               return var6 - var10 > 0.03F ? contents(var11) : onFloor(var11, var2, var4);
            }
         }
      }
   }

   private static IsoMovingObject body(IsoCell var0, double var1, double var3, double var5, int var7, int var8, int var9) {
      IsoMovingObject var10 = null;
      double var11 = 0.36F;

      for (int var13 = -1; var13 <= 1; var13++) {
         for (int var14 = -1; var14 <= 1; var14++) {
            IsoGridSquare var15 = var0.getGridSquare(var7 + var14, var8 + var13, var9);

            for (int var16 = 0; var15 != null && var16 < var15.getMovingObjects().size(); var16++) {
               IsoMovingObject var17 = (IsoMovingObject)var15.getMovingObjects().get(var16);
               double var18 = var17.getX() - var1;
               double var20 = var17.getY() - var3;
               double var22 = var5 - var17.getZ();
               double var24 = var18 * var18 + var20 * var20;
               if (var17 != IsoPlayer.players[0] && !(var17 instanceof BaseVehicle) && var22 >= -0.03F && var22 <= 0.8F && var24 < var11) {
                  var10 = var17;
                  var11 = var24;
               }
            }
         }
      }

      return var10;
   }

   private static IsoObject edge(IsoGridSquare var0, int var1) {
      IsoObject var2 = null;

      for (int var3 = var0 == null ? -1 : var0.getObjects().size() - 1; var3 >= 0; var3--) {
         IsoObject var4 = (IsoObject)var0.getObjects().get(var3);
         IsoSprite var5 = var4.getSprite();
         if (var5 != null && Edges.ownerKind(var5) == var1) {
            if (var4 instanceof IsoDoor
               || var4 instanceof IsoWindow
               || var4 instanceof IsoWindowFrame
               || var4 instanceof IsoCurtain
               || var4 instanceof IsoThumpable) {
               return var4;
            }

            var2 = var2 == null ? var4 : var2;
         }
      }

      return var2;
   }

   private static IsoObject contents(IsoGridSquare var0) {
      for (int var1 = var0.getObjects().size() - 1; var1 >= 0; var1--) {
         IsoObject var2 = (IsoObject)var0.getObjects().get(var1);
         IsoSprite var3 = var2.getSprite();
         if (var3 != null && !var3.solidfloor && Edges.ownerKind(var3) == 0) {
            return var2;
         }
      }

      return floor(var0);
   }

   private static IsoObject onFloor(IsoGridSquare var0, double var1, double var3) {
      IsoWorldInventoryObject var5 = null;
      double var6 = 0.122499995F;

      for (IsoWorldInventoryObject var9 : var0.getWorldObjects()) {
         double var10 = var9.getWorldPosX() - var1;
         double var12 = var9.getWorldPosY() - var3;
         double var14 = var10 * var10 + var12 * var12;
         if (var14 < var6) {
            var5 = var9;
            var6 = var14;
         }
      }

      if (var5 != null) {
         return var5;
      }

      IsoDeadBody var16 = var0.getDeadBody();
      return (IsoObject)(var16 != null ? var16 : floor(var0));
   }

   private static IsoObject floor(IsoGridSquare var0) {
      for (int var1 = 0; var1 < var0.getObjects().size(); var1++) {
         IsoObject var2 = (IsoObject)var0.getObjects().get(var1);
         if (var2.getSprite() != null && var2.getSprite().solidfloor) {
            return var2;
         }
      }

      return var0.getObjects().isEmpty() ? null : (IsoObject)var0.getObjects().get(0);
   }

   private MouseTargets() {
   }
}
