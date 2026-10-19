package viewpoint.gunplay;

import viewpoint.far.Relief;
import viewpoint.render.FarRelief;
import zombie.iso.IsoMovingObject;

final class Ground {
   static double under(IsoMovingObject var0) {
      return at(var0.getX(), var0.getY());
   }

   static double at(double var0, double var2) {
      FarRelief.Lift var4 = Relief.current();
      return var4 == null ? 0.0 : var4.scene(var0, var2);
   }

   private Ground() {
   }
}
