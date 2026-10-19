package viewpoint.input;

import java.lang.reflect.Field;
import java.util.Arrays;
import viewpoint.platform.KeyInput;
import zombie.core.Core;
import zombie.input.Mouse;

public final class MouseButtons {
   private static boolean attackHeld;
   private static Field mouseX;
   private static Field mouseY;
   private static boolean pinFailed;

   static boolean attackHeld() {
      return attackHeld;
   }

   public static boolean rightHeld(boolean var0) {
      boolean[] var1 = Mouse.buttonDownStates;
      if (!Look.captured) {
         return false;
      } else {
         return !var0 && !FreeCam.active && var1 != null && var1.length >= 2 ? var1[1] && !Mouse.uiCaptured[1] : var0;
      }
   }

   public static void pin() {
      KeyInput.mouseButtons();
      boolean[] var0 = Mouse.buttonDownStates;
      attackHeld = Look.captured && var0 != null && var0.length > 0 && var0[0];
      if (FreeCam.active && Look.captured) {
         FreeCam.wheel = FreeCam.wheel + Mouse.getWheelState();
         if (Mouse.buttonDownStates != null) {
            Arrays.fill(Mouse.buttonDownStates, false);
         }

         if (Mouse.buttonPrevStates != null) {
            Arrays.fill(Mouse.buttonPrevStates, false);
         }
      }

      if (Look.captured && !pinFailed) {
         try {
            if (mouseX == null) {
               mouseX = Mouse.class.getDeclaredField("x");
               mouseY = Mouse.class.getDeclaredField("y");
               mouseX.setAccessible(true);
               mouseY.setAccessible(true);
            }

            mouseX.setInt(null, Core.getInstance().getScreenWidth() / 2);
            mouseY.setInt(null, Core.getInstance().getScreenHeight() / 2);
         } catch (ReflectiveOperationException | RuntimeException var2) {
            pinFailed = true;
            System.out.println("[Viewpoint] cannot pin the mouse position: " + var2);
         }
      }
   }

   private MouseButtons() {
   }
}
