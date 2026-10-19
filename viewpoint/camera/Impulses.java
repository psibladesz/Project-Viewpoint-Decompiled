package viewpoint.camera;

import java.util.Random;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.inventory.types.HandWeapon;

public final class Impulses {
   static final float BLAST_TRAUMA = 0.8F;
   static final float BLAST_REACH = 25.0F;
   static final float THUNDER_TRAUMA = 0.3F;
   static final float THUNDER_REACH = 50.0F;
   private static final int MOST = 16;
   private static final Random random = new Random();
   private static final float[] at = new float[48];
   private static final float[] strength = new float[16];
   private static final float[] reach = new float[16];
   private static Recoil.Gun ownKick;
   private static boolean ownAutomatic;
   private static boolean ownBlow;
   private static HandWeapon blowWeapon;
   private static int count;

   public static void shot(IsoGameCharacter var0) {
      if (var0 != null && var0 == IsoPlayer.players[0]) {
         HandWeapon var1 = var0.getUseHandWeapon();
         boolean var2 = var1 != null && "Auto".equals(var1.getFireMode());
         ownKick = var1 == null ? Recoil.HANDGUN : Recoil.of(var1.getProjectileCount(), var2, var1.isTwoHandWeapon(), var1.getMaxDamage());
         ownAutomatic = var2;
      }
   }

   public static void blow(IsoGameCharacter var0, IsoGameCharacter var1, HandWeapon var2, boolean var3) {
      if (var1 != null && var1 == IsoPlayer.players[0] && var2 != null && var2.isMelee() && !var3) {
         Melee.landed(var1, var2, var0, var0.isDead() || var0.getHealth() <= 0.0F);
         ownBlow = true;
         blowWeapon = var2;
      }
   }

   public static void explosion(int var0, int var1, int var2) {
      add(var0 + 0.5F, var1 + 0.5F, var2, 0.8F, 25.0F);
   }

   public static void thunder(int var0, int var1, boolean var2) {
      if (var2) {
         add(var0 + 0.5F, var1 + 0.5F, 0.0F, 0.3F, 50.0F);
      }
   }

   static void apply(Additive var0, GunKick var1, float var2, float var3, float var4, float var5) {
      if (ownKick != null) {
         Recoil.Gun var6 = ownKick;
         float var7 = 1.0F - 0.25F * var5;
         var0.kicks.strike(Recoil.shot(var6, random), var7 * CameraSettings.RECOIL.get());
         var0.trauma(ownAutomatic ? Math.min(var6.trauma(), 0.3F - var0.trauma()) : var6.trauma());
         var1.shot(var6.arms(), random, CameraSettings.GUN_KICK.get());
         ownKick = null;
      }

      if (ownBlow) {
         Melee.impact(var0, blowWeapon);
         ownBlow = false;
         blowWeapon = null;
      }

      for (int var10 = 0; var10 < count; var10++) {
         float var11 = at[var10 * 3] - var2;
         float var8 = at[var10 * 3 + 1] - var3;
         float var9 = (at[var10 * 3 + 2] - var4) * 2.4494896F;
         var0.trauma(strength[var10] * soft((float)Math.sqrt(var11 * var11 + var8 * var8 + var9 * var9) / reach[var10]));
      }

      count = 0;
   }

   static float soft(float var0) {
      if (var0 >= 1.0F) {
         return 0.0F;
      }

      float var1 = Math.max(0.0F, var0);
      return 1.0F - var1 * var1 * (3.0F - 2.0F * var1);
   }

   private static void add(float var0, float var1, float var2, float var3, float var4) {
      if (count != 16) {
         at[count * 3] = var0;
         at[count * 3 + 1] = var1;
         at[count * 3 + 2] = var2;
         strength[count] = var3;
         reach[count] = var4;
         count++;
      }
   }

   private Impulses() {
   }
}
