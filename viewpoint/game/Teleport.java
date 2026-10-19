package viewpoint.game;

import viewpoint.input.FreeCam;
import viewpoint.platform.Keys;
import viewpoint.platform.SettingsWindow;
import zombie.characters.IsoGameCharacter;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;

public final class Teleport {
   private static final SettingsWindow.Button TO_CAMERA = SettingsWindow.button(
      "Debug/Free camera",
      "Teleport to the free camera",
      "You stand where the free camera is, on the first floor at or below it; with none loaded there, on the ground."
   );
   private static final int LOWEST = -32;

   public static void poll(IsoCell var0, IsoGameCharacter var1) {
      if (TO_CAMERA.take()) {
         FreeCam.Place var2 = FreeCam.place;
         if (FreeCam.active && var2 != null) {
            int var3 = (int)Math.floor(var2.x());
            int var4 = (int)Math.floor(var2.y());
            int var5 = floorUnder(var0, var3, var4, (int)Math.floor(var2.z()));
            var1.teleportTo(var3, var4, var5);
            TO_CAMERA.status("at " + var3 + ", " + var4 + ", level " + var5);
         } else {
            TO_CAMERA.status("the free camera is off (" + Keys.FREE_CAMERA.display() + ")");
         }
      }
   }

   private static int floorUnder(IsoCell var0, int var1, int var2, int var3) {
      for (int var4 = var3; var4 >= -32; var4--) {
         IsoGridSquare var5 = var0.getGridSquare(var1, var2, var4);
         if (var5 != null && (var5.isSolidFloor() || var5.TreatAsSolidFloor())) {
            return var4;
         }
      }

      return 0;
   }

   private Teleport() {
   }
}
