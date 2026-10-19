package viewpoint.input;

import java.util.concurrent.atomic.AtomicBoolean;
import org.lwjgl.glfw.GLFW;

public final class MouseUse {
   static final double TRAVEL = 8.0;
   private static final int BUTTONS = 3;
   private static final AtomicBoolean used = new AtomicBoolean();
   private static final double[] cursorX = new double[1];
   private static final double[] cursorY = new double[1];
   private static final boolean[] down = new boolean[3];
   private static final MouseUse watch = new MouseUse();
   private static boolean wasFocused;
   private static boolean wasCaptured;
   private double lastX;
   private double lastY;
   private double travel;
   private boolean hasLast;

   static void sample(long var0, boolean var2) {
      boolean var3 = GLFW.glfwGetWindowAttrib(var0, 131073) == 1;
      GLFW.glfwGetCursorPos(var0, cursorX, cursorY);
      boolean var4 = false;

      for (int var5 = 0; var5 < 3; var5++) {
         boolean var6 = var3 && GLFW.glfwGetMouseButton(var0, var5) == 1;
         var4 |= var6 && !down[var5];
         down[var5] = var6;
      }

      boolean var7 = var3 && wasFocused && var2 == wasCaptured;
      wasFocused = var3;
      wasCaptured = var2;
      if (watch.step(cursorX[0], cursorY[0], var7, var4)) {
         used.set(true);
      }
   }

   boolean step(double var1, double var3, boolean var5, boolean var6) {
      double var7 = var5 && this.hasLast ? Math.abs(var1 - this.lastX) + Math.abs(var3 - this.lastY) : 0.0;
      this.lastX = var1;
      this.lastY = var3;
      this.hasLast = true;
      this.travel = var7 > 0.0 ? this.travel + var7 : 0.0;
      if (!var6 && !(this.travel >= 8.0)) {
         return false;
      }

      this.travel = 0.0;
      return true;
   }

   public static boolean used() {
      return used.getAndSet(false);
   }
}
