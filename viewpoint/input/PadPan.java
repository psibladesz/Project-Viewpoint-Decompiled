package viewpoint.input;

import java.lang.reflect.Field;
import viewpoint.core.View;
import zombie.GameWindow;
import zombie.characters.IsoPlayer;
import zombie.core.Core;
import zombie.input.JoypadManager;
import zombie.iso.PlayerCamera;
import zombie.vehicles.BaseVehicle;

public final class PadPan {
   private static final Field EASED_X;
   private static final Field EASED_Y;

   public static void updated(PlayerCamera var0) {
      IsoPlayer var1 = IsoPlayer.players[var0.playerIndex];
      if (var1 != null && View.enabled && var0.playerIndex == 0) {
         int var2 = var1.getJoypadBind();
         BaseVehicle var3 = var1.getVehicle();
         boolean var4 = GameWindow.activatedJoyPad != null && var2 != -1;
         boolean var5 = Core.getInstance().getOptionPanCameraWhileDriving() && var3 != null && var3.getCurrentSpeedKmHour() > 1.0F;
         boolean var6 = var1.isAiming() || var1.isLookingWhileInVehicle();
         boolean var7 = var4 && JoypadManager.instance.isRBPressed(var2);
         if (held(true, var4, var5, var6, var7, var1.isJoypadIgnoreAimUntilCentered())) {
            centre(var0);
         }
      }
   }

   static boolean held(boolean var0, boolean var1, boolean var2, boolean var3, boolean var4, boolean var5) {
      return var0 && var1 && !var2 && var3 && var4 && !var5;
   }

   static void centre(PlayerCamera var0) {
      var0.rightClickTargetX = 0.0F;
      var0.rightClickTargetY = 0.0F;
      var0.rightClickX = 0.0F;
      var0.rightClickY = 0.0F;

      try {
         EASED_X.setFloat(var0, 0.0F);
         EASED_Y.setFloat(var0, 0.0F);
      } catch (IllegalAccessException var2) {
         throw new IllegalStateException("[Viewpoint] PlayerCamera's pan could not be set", var2);
      }
   }

   private PadPan() {
   }

   static {
      try {
         EASED_X = PlayerCamera.class.getDeclaredField("rightClickXf");
         EASED_Y = PlayerCamera.class.getDeclaredField("rightClickYf");
         EASED_X.setAccessible(true);
         EASED_Y.setAccessible(true);
      } catch (ReflectiveOperationException | RuntimeException var1) {
         throw new IllegalStateException("[Viewpoint] PlayerCamera's pan changed", var1);
      }
   }
}
