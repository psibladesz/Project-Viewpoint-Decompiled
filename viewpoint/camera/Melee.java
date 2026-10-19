package viewpoint.camera;

import zombie.characters.IsoGameCharacter;
import zombie.inventory.types.HandWeapon;

final class Melee {
   static final Melee.Swing ACROSS = new Melee.Swing(
      new Kicks.Kick(-0.3F, -1.6F, -1.8F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1F, 0.28F),
      new Kicks.Kick(0.5F, 0.7F, 0.8F, -0.035F, 0.0F, 0.6F, 0.6F, 0.02F, 0.13F),
      0.12F,
      0.055F
   );
   static final Melee.Swing OVERHEAD = new Melee.Swing(
      new Kicks.Kick(-2.5F, -0.6F, -1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.14F, 0.32F),
      new Kicks.Kick(1.2F, 0.3F, 0.6F, -0.05F, 0.0F, 1.0F, 0.65F, 0.025F, 0.16F),
      0.25F,
      0.085F
   );
   static final Melee.Swing THRUST = new Melee.Swing(
      new Kicks.Kick(-0.3F, 0.0F, 0.0F, -0.04F, 0.0F, 0.0F, 0.0F, 0.08F, 0.22F),
      new Kicks.Kick(0.4F, 0.0F, 0.3F, 0.02F, 0.0F, 0.5F, 0.6F, 0.02F, 0.12F),
      0.08F,
      0.04F
   );
   static final Melee.Swing OTHER = new Melee.Swing(
      new Kicks.Kick(0.0F, -0.6F, -0.8F, 0.0F, 0.0F, 0.0F, 0.0F, 0.08F, 0.22F),
      new Kicks.Kick(0.3F, 0.3F, 0.3F, -0.02F, 0.0F, 0.3F, 0.6F, 0.02F, 0.12F),
      0.06F,
      0.035F
   );
   static final float HEAVY_WEIGHT = 3.0F;
   static final float HEAVIER = 1.25F;
   static final float KILL_PAUSE = 0.025F;

   static Melee.Swing of(String var0) {
      if (var0 == null) {
         return OTHER;
      }

      return switch (var0) {
         case "Bat" -> ACROSS;
         case "Heavy" -> OVERHEAD;
         case "Stab", "Spear" -> THRUST;
         default -> OTHER;
      };
   }

   static float weight(HandWeapon var0) {
      boolean var1 = var0 != null && (var0.isTwoHandWeapon() || var0.getWeight() >= 3.0F);
      return var1 ? 1.25F : 1.0F;
   }

   static void swing(Additive var0, IsoGameCharacter var1) {
      HandWeapon var2 = var1.getUseHandWeapon();
      var0.kicks.strike(of(var2 == null ? null : var2.getSwingAnim()).swing(), scale(var2));
   }

   static void landed(IsoGameCharacter var0, HandWeapon var1, IsoGameCharacter var2, boolean var3) {
      Melee.Swing var4 = of(var1.getSwingAnim());
      float var5 = (var4.pause() * weight(var1) + (var3 ? 0.025F : 0.0F)) * CameraSettings.HIT_PAUSE.get();
      HitStop.hold(var5, var0, var2);
   }

   static void impact(Additive var0, HandWeapon var1) {
      Melee.Swing var2 = of(var1.getSwingAnim());
      float var3 = scale(var1);
      var0.kicks.strike(var2.impact(), var3);
      var0.trauma(var2.trauma() * var3);
   }

   private static float scale(HandWeapon var0) {
      return weight(var0) * CameraSettings.MELEE.get();
   }

   private Melee() {
   }

   record Swing(Kicks.Kick swing, Kicks.Kick impact, float trauma, float pause) {
   }
}
