package viewpoint.input;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.opengl.Display;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.SettingsWindow;

public final class Look {
   public static volatile float yaw;
   public static volatile float pitch;
   public static volatile boolean wantCapture;
   public static volatile boolean captured;
   public static volatile float zoom = 1.0F;
   public static volatile float aimScale = 1.0F;
   private static volatile float turning = Float.NaN;
   private static volatile float behindYaw = Float.NaN;
   private static volatile float behindPitch = Float.NaN;
   private static volatile boolean chase;
   private static final String SECTION = "Controls/Mouse";
   private static final LiveSettings.Number SPEED = LiveSettings.number("mouse.sensitivity", "Sensitivity", "Controls/Mouse", 0.1F, 4.0F, 0.05F, 1.0F);
   private static final LiveSettings.Toggle INVERT_Y = LiveSettings.toggle("mouse.invertY", "Invert vertical look", "Controls/Mouse", false);
   private static final LiveSettings.Toggle FRESH = LiveSettings.toggle("mouse.readBeforeDrawing", "Read the mouse just before drawing", "Controls/Mouse", true);
   private static final float SENSITIVITY = 0.0022F;
   private static final float MAX_PITCH = (float)Math.toRadians(85.0);
   private static final int FRAMES_WITHOUT_VIEW = 3;
   private static final float TURN_TAU = 0.08F;
   private static final float TURNED = 0.01F;
   private static final float BEHIND_IDLE = 2.0F;
   private static final float BEHIND_TAU = 0.3F;
   private static final float CHASE_IDLE = 0.75F;
   private static final float CHASE_TAU = 0.3F;
   private static final float CHASE_PITCH_TAU = 0.6F;
   private static final double[] px = new double[1];
   private static final double[] py = new double[1];
   private static final float[] stick = new float[2];
   private static final float[] assisted = new float[2];
   private static volatile long movedNanos;
   private static volatile float heldYaw = Float.NaN;
   private static volatile float heldPitch = Float.NaN;
   public static long readNanos;
   private static boolean hasLast;
   private static double lastX;
   private static double lastY;
   private static int framesWithoutView = 4;

   public static void viewDrawn() {
      framesWithoutView = 0;
   }

   public static void framePresented() {
      framesWithoutView = Math.min(framesWithoutView + 1, 4);
   }

   public static boolean onUpdateMouseCursor() {
      long var0 = Display.getWindow();
      FreeCam.poll();
      MouseUse.sample(var0, captured);
      boolean var2 = framesWithoutView <= 3;
      boolean var3 = wantCapture && var2 && GLFW.glfwGetWindowAttrib(var0, 131073) == 1;
      if (!var3) {
         if (captured) {
            captured = false;
            SettingsWindow.cursorHeld(false);
            GLFW.glfwSetInputMode(var0, 208897, 212993);
            GLFW.glfwSetCursorPos(var0, Display.getWidth() / 2.0, Display.getHeight() / 2.0);
         }

         if (var2 && PadLook.holds()) {
            read(var0, false);
         }

         return false;
      } else {
         if (!captured || GLFW.glfwGetInputMode(var0, 208897) != 212995) {
            GLFW.glfwSetInputMode(var0, 208897, 212995);
            if (GLFW.glfwRawMouseMotionSupported()) {
               GLFW.glfwSetInputMode(var0, 208901, 1);
            }

            captured = true;
            SettingsWindow.cursorHeld(true);
            hasLast = false;
         }

         read(var0, true);
         return true;
      }
   }

   public static void readBeforeDrawing() {
      if (FRESH.get()) {
         if (captured) {
            GLFW.glfwPollEvents();
            read(Display.getWindow(), true);
         } else if (PadLook.holds()) {
            read(Display.getWindow(), false);
         }
      }
   }

   private static void read(long var0, boolean var2) {
      long var3 = System.nanoTime();
      float var5 = readNanos == 0L ? 0.0F : Math.min(0.1F, (float)(var3 - readNanos) * 1.0E-9F);
      readNanos = var3;
      double var6 = 0.0;
      double var8 = 0.0;
      if (var2) {
         GLFW.glfwGetCursorPos(var0, px, py);
         double var10 = SPEED.get();
         var6 = hasLast ? (px[0] - lastX) * var10 : 0.0;
         var8 = hasLast ? (py[0] - lastY) * var10 : 0.0;
         lastX = px[0];
         lastY = py[0];
         hasLast = true;
      }

      if (INVERT_Y.get()) {
         var8 = -var8;
      }

      if (!FreeCam.look(var6, var8)) {
         float var15 = zoom;
         float var11 = var15 * aimScale;
         boolean var12 = var5 > 0.0F && PadLook.turn(var3 - (long)(var5 * 1.0E9F), var3, stick);
         float var13 = var12 ? stick[0] * var11 : 0.0F;
         float var14 = var12 ? stick[1] * var11 : 0.0F;
         if (AimAssist.turn(var3, var5, yaw, pitch, var13, var14, assisted)) {
            var13 = assisted[0];
            var14 = assisted[1];
         }

         yaw = wrap(yaw + (float)var6 * 0.0022F * var15 + var13);
         pitch = Math.max(-MAX_PITCH, Math.min(MAX_PITCH, pitch - (float)var8 * 0.0022F * var15 + var14));
         if (var6 != 0.0 || var8 != 0.0 || var12) {
            movedNanos = var3;
            turning = Float.NaN;
            heldYaw = Float.NaN;
         }

         held();
         turn(var3, var5);
         yaw = FreeLook.clamp(yaw);
      }
   }

   private static void turn(long var0, float var2) {
      float var3 = turning;
      float var4 = behindYaw;
      if (!Float.isNaN(var3)) {
         yaw = Ease.angleToward(yaw, var3, var2, 0.08F);
         if (Math.abs(Ease.between(yaw, var3)) < 0.01F) {
            turning = Float.NaN;
         }
      } else if (!Float.isNaN(var4) && (float)(var0 - movedNanos) * 1.0E-9F > (chase ? 0.75F : 2.0F)) {
         yaw = Ease.angleToward(yaw, var4, var2, chase ? 0.3F : 0.3F);
         float var5 = behindPitch;
         if (!Float.isNaN(var5)) {
            pitch = Ease.toward(pitch, var5, var2, 0.6F);
         }
      }
   }

   private static void held() {
      float var0 = heldYaw;
      if (!Float.isNaN(var0)) {
         heldYaw = Float.NaN;
         yaw = wrap(var0);
         pitch = Math.max(-MAX_PITCH, Math.min(MAX_PITCH, heldPitch));
      }
   }

   public static void hold(float var0, float var1) {
      heldPitch = var1;
      heldYaw = var0;
   }

   public static long movedNanos() {
      return movedNanos;
   }

   public static boolean looking() {
      return captured || PadLook.holds();
   }

   public static void turnTo(float var0) {
      turning = var0;
   }

   public static void behind(float var0, float var1, boolean var2) {
      behindYaw = var0;
      behindPitch = var1;
      chase = var2;
   }

   public static float wrap(float var0) {
      float var1 = (float) (Math.PI * 2);
      return var0 > Math.PI ? var0 - var1 : (var0 < -Math.PI ? var0 + var1 : var0);
   }

   private Look() {
   }

   static {
      SPEED.describe("How far the view turns for a move of the mouse: 1 is the mod's own speed.");
      INVERT_Y.describe("Moving the mouse forward looks down, and back looks up.");
      FRESH.describe(
         "The view turns with the mouse as it is when the frame is drawn, not as it was a frame before: less lag, most when the game holds the frame rate back."
      );
   }
}
