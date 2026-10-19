package viewpoint.gunplay;

import zombie.characters.IsoGameCharacter;
import zombie.characters.animals.IsoAnimal;
import zombie.inventory.types.HandWeapon;
import zombie.network.fields.hit.HitInfo;
import zombie.util.list.PZArrayList;

final class Candidates {
   static final int MOST = 32;
   final HitInfo[] entries = new HitInfo[32];
   final IsoGameCharacter[] characters = new IsoGameCharacter[32];
   final float[][] bones = new float[32][Parts.BONES.length * 3];
   final float[][] capsules = new float[32][104];
   int count;

   void take(PZArrayList<HitInfo> var1) {
      this.count = 0;

      for (int var2 = 0; var2 < var1.size() && this.count < 32; var2++) {
         HitInfo var3 = (HitInfo)var1.get(var2);
         if (var3.getObject() instanceof IsoGameCharacter var4 && !(var4 instanceof IsoAnimal)) {
            this.entries[this.count] = var3;
            this.characters[this.count] = var4;
            Bodies.of(var4, this.bones[this.count], this.capsules[this.count]);
            this.count++;
         }
      }
   }

   void seated(IsoGameCharacter var1, HandWeapon var2) {
      if (var1.getVehicle() != null) {
         int var3 = 0;

         for (int var4 = 0; var4 < this.count; var4++) {
            if (Odds.chance(var1, var2, this.entries[var4]) > 0.0F) {
               this.swap(var4, var3++);
            }
         }

         for (int var5 = var3; var5 < this.count; var5++) {
            this.entries[var5] = null;
            this.characters[var5] = null;
         }

         this.count = var3;
      }
   }

   void clear() {
      for (int var1 = 0; var1 < this.count; var1++) {
         this.entries[var1] = null;
         this.characters[var1] = null;
      }

      this.count = 0;
   }

   double middleX(int var1) {
      return this.bones[var1][3];
   }

   double middleY(int var1) {
      return this.bones[var1][4];
   }

   double middleUp(int var1) {
      return this.bones[var1][5];
   }

   private void swap(int var1, int var2) {
      HitInfo var3 = this.entries[var1];
      IsoGameCharacter var4 = this.characters[var1];
      float[] var5 = this.bones[var1];
      float[] var6 = this.capsules[var1];
      this.entries[var1] = this.entries[var2];
      this.characters[var1] = this.characters[var2];
      this.bones[var1] = this.bones[var2];
      this.capsules[var1] = this.capsules[var2];
      this.entries[var2] = var3;
      this.characters[var2] = var4;
      this.bones[var2] = var5;
      this.capsules[var2] = var6;
   }
}
