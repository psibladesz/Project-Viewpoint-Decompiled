package viewpoint.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.TreeSet;
import viewpoint.platform.LiveSettings;
import zombie.characters.action.ActionGroup;
import zombie.characters.animals.AnimalDefinitions;
import zombie.core.skinnedmodel.advancedanimation.AnimationSet;

public final class AnimalSets {
   public static final LiveSettings.Toggle ON = LiveSettings.toggle("game.animalSetsWithGame", "Animals' animations loaded with the game", "Debug/Fixes", true);

   public static void load() {
      if (ON.get()) {
         long var0 = System.nanoTime();
         ArrayList var2 = new ArrayList();

         for (AnimalDefinitions var4 : AnimalDefinitions.getAnimalDefsArray()) {
            var2.add(var4.animset);
         }

         TreeSet<String> var6 = sets(var2);

         for (String var5 : var6) {
            AnimationSet.GetAnimationSet(var5, false);
            ActionGroup.getActionGroup(var5);
         }

         System.out
            .printf(
               Locale.ROOT,
               "[Viewpoint] animals: %d animation sets and action groups loaded with the game in %.0f ms%n",
               var6.size(),
               (System.nanoTime() - var0) * 1.0E-6
            );
      }
   }

   static TreeSet<String> sets(Collection<String> var0) {
      TreeSet var1 = new TreeSet();

      for (String var3 : var0) {
         if (var3 != null) {
            var1.add(var3);
         }
      }

      return var1;
   }

   private AnimalSets() {
   }
}
