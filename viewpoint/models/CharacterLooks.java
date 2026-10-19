package viewpoint.models;

import java.lang.reflect.Field;
import viewpoint.platform.Profile;
import zombie.characters.IsoGameCharacter;
import zombie.core.skinnedmodel.model.ModelInstance;

final class CharacterLooks {
   private static final float ALWAYS = 6.0F;
   private static final long BUDGET_NANOS = 2000000L;
   private static final Field DUE = due();
   private static long spent;
   private static int made;
   private static int waiting;

   static void begin() {
      spent = 0L;
      made = 0;
      waiting = 0;
   }

   static boolean ready(IsoGameCharacter var0, ModelInstance var1, float var2) {
      if (!due(var0)) {
         var0.checkUpdateModelTextures();
         return true;
      } else if (!(var2 <= 6.0F) && spent >= 2000000L) {
         waiting++;
         return var1.tex != null;
      } else {
         long var3 = System.nanoTime();
         var0.checkUpdateModelTextures();
         spent = spent + (System.nanoTime() - var3);
         made++;
         return true;
      }
   }

   static void end() {
      Profile.looksMade = made;
      Profile.looksWaiting = waiting;
   }

   private static boolean due(IsoGameCharacter var0) {
      if (DUE == null) {
         return false;
      }

      try {
         return DUE.getBoolean(var0);
      } catch (IllegalAccessException var2) {
         throw new IllegalStateException("IsoGameCharacter.updateModelTextures could not be read", var2);
      }
   }

   private static Field due() {
      try {
         Field var0 = IsoGameCharacter.class.getDeclaredField("updateModelTextures");
         var0.setAccessible(true);
         return var0;
      } catch (ReflectiveOperationException | RuntimeException var1) {
         return null;
      }
   }

   private CharacterLooks() {
   }
}
