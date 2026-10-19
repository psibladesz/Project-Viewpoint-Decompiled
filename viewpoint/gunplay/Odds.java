package viewpoint.gunplay;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import zombie.CombatManager;
import zombie.characters.IsoGameCharacter;
import zombie.inventory.types.HandWeapon;
import zombie.network.fields.hit.HitInfo;

final class Odds {
   private static final float PERCENT = 100.0F;
   private static final Method CHANCE;
   private static final Field HIT_CHANCE;
   private static HitInfo nowhere;

   static float chance(IsoGameCharacter var0, HandWeapon var1, HitInfo var2) {
      float var3 = var2.distSq;
      float var4 = Gunplay.RANGE.get();

      try {
         var2.distSq = var3 / (var4 * var4);
         return ask(var0, var1, var2);
      } finally {
         var2.distSq = var3;
      }
   }

   static float chance(IsoGameCharacter var0, HandWeapon var1, float var2) {
      return ask(var0, var1, at(var2 / Gunplay.RANGE.get()));
   }

   private static float ask(IsoGameCharacter var0, HandWeapon var1, HitInfo var2) {
      try {
         return HIT_CHANCE.getFloat(CHANCE.invoke(CombatManager.getInstance(), var0, var1, var2)) / 100.0F;
      } catch (IllegalAccessException | InvocationTargetException var4) {
         throw new IllegalStateException("[Viewpoint] gunplay: the game's hit chance failed", var4);
      }
   }

   private static HitInfo at(float var0) {
      if (nowhere == null) {
         nowhere = new HitInfo();
      }

      nowhere.distSq = var0 * var0;
      return nowhere;
   }

   private Odds() {
   }

   static {
      try {
         CHANCE = CombatManager.class.getDeclaredMethod("calculateHitChanceData", IsoGameCharacter.class, HandWeapon.class, HitInfo.class);
         CHANCE.setAccessible(true);
         HIT_CHANCE = CHANCE.getReturnType().getDeclaredField("hitChance");
         HIT_CHANCE.setAccessible(true);
      } catch (ReflectiveOperationException | RuntimeException var1) {
         throw new IllegalStateException("[Viewpoint] gunplay: CombatManager.calculateHitChanceData changed", var1);
      }
   }
}
