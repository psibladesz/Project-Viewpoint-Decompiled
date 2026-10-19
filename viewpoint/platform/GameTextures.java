package viewpoint.platform;

import java.io.IOException;
import zombie.core.Core;

final class GameTextures {
   static final LiveSettings.Toggle HALF = LiveSettings.toggle("graphics.halfTextures", "Half-resolution textures", "Graphics/General", false);
   private static Boolean applied;

   static boolean follow() {
      boolean var0 = !Core.getInstance().getOptionTexture2x();
      if (applied == null) {
         applied = var0;
         if (HALF.get() != var0) {
            HALF.set(var0);
            return true;
         } else {
            return false;
         }
      } else {
         if (HALF.get() == applied) {
            return false;
         }

         applied = HALF.get();
         Core.getInstance().setOptionTexture2x(!applied);

         try {
            Core.getInstance().saveOptions();
            System.out.println("[Viewpoint] the game's art set to " + (applied ? "1x" : "2x") + " in its options: it takes it as it starts");
            return false;
         } catch (IOException var2) {
            throw new IllegalStateException("the game's options could not be saved with its art at " + (applied ? "1x" : "2x"), var2);
         }
      }
   }

   private GameTextures() {
   }

   static {
      HALF.describe(
         "Loads the game's 1x art in place of its 2x art: a quarter of the texels, far less video memory and system memory, softer up close. Takes effect after the game starts again."
      );
   }
}
