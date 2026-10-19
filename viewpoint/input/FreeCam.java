package viewpoint.input;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntPredicate;
import viewpoint.core.Frame;
import viewpoint.core.View;
import viewpoint.platform.KeyBind;
import viewpoint.platform.KeyChord;
import viewpoint.platform.KeyInput;
import viewpoint.platform.Keys;

public final class FreeCam {
   public static volatile boolean active;
   public static volatile boolean physical;
   public static volatile FreeCam.Place place;
   static volatile int wheel;
   private static final int NONE = 0;
   private static final int FREE = 1;
   private static final int PHYSICAL = 2;
   private static final AtomicInteger wanted = new AtomicInteger(0);
   private static final AtomicBoolean home = new AtomicBoolean();
   private static final float BASE_SPEED = 12.0F;
   private static final float FAST = 4.0F;
   private static final float MIN_SPEED = 1.0F;
   private static final float MAX_SPEED = 400.0F;
   private static final float SENSITIVITY = 0.0022F;
   private static final float MAX_PITCH = (float)Math.toRadians(89.0);
   private static double x;
   private static double y;
   private static double z;
   private static float yaw;
   private static float pitch;
   private static float speed = 12.0F;
   private static boolean starting;
   private static int[] ignored = new int[0];
   private static long lastNanos;

   public static FreeCam.Place centre() {
      FreeCam.Place var0 = place;
      return active && physical ? var0 : null;
   }

   public static void keys() {
      if (Keys.PHYSICAL_CAMERA.pressed()) {
         wanted.set(2);
      } else if (Keys.FREE_CAMERA.pressed()) {
         wanted.set(1);
      }

      if (active && Keys.CAMERA_HOME.pressed()) {
         home.set(true);
      }
   }

   static void poll() {
      int var0 = wanted.getAndSet(0);
      if (var0 != 0 && View.enabled) {
         switchTo(!active || !physical && var0 != 1, var0 == 2);
      }

      if (!View.enabled) {
         physical = false;
         active = false;
      }

      if (home.getAndSet(false) && active) {
         starting = true;
      }

      long var1 = System.nanoTime();
      float var3 = lastNanos == 0L ? 0.0F : Math.min(0.1F, (float)(var1 - lastNanos) * 1.0E-9F);
      lastNanos = var1;
      if (!active) {
         wheel = 0;
      } else {
         fly(var3);
      }
   }

   private static void fly(float var0) {
      for (int var1 = 0; var1 < ignored.length; var1++) {
         if (ignored[var1] != 0 && !KeyInput.physicallyDown(ignored[var1])) {
            ignored[var1] = 0;
         }
      }

      int var15 = wheel;
      wheel = 0;
      speed = Math.max(1.0F, Math.min(400.0F, speed * (float)Math.pow(1.25, var15)));
      IntPredicate var2 = FreeCam::flies;
      float var3 = speed * var0 * (Keys.CAMERA_FAST.down(var2) ? 4.0F : 1.0F);
      float var4 = axis(Keys.CAMERA_FORWARD, Keys.CAMERA_BACK, var2);
      float var5 = axis(Keys.CAMERA_RIGHT, Keys.CAMERA_LEFT, var2);
      float var6 = axis(Keys.CAMERA_UP, Keys.CAMERA_DOWN, var2);
      double var7 = Math.cos(yaw);
      double var9 = Math.sin(yaw);
      double var11 = Math.cos(pitch);
      double var13 = Math.sin(pitch);
      x += (var7 * var11 * var4 - var9 * var5) * var3;
      y += (var9 * var11 * var4 + var7 * var5) * var3;
      z += (var13 * var4 + var6) * var3 / 2.4494896F;
   }

   private static float axis(KeyBind var0, KeyBind var1, IntPredicate var2) {
      return (var0.down(var2) ? 1.0F : 0.0F) - (var1.down(var2) ? 1.0F : 0.0F);
   }

   private static boolean flies(int var0) {
      for (int var4 : ignored) {
         if (var4 == var0) {
            return false;
         }
      }

      return KeyInput.physicallyDown(var0);
   }

   private static void switchTo(boolean var0, boolean var1) {
      if (var0 && !active) {
         starting = true;
         place = null;
      }

      physical = var0 && var1;
      active = var0;
      ignored = var0 ? held(var1 ? Keys.PHYSICAL_CAMERA : Keys.FREE_CAMERA) : new int[0];
      System.out
              .println(
                      "[Viewpoint] "
                              + (
                              var0
                                      ? (physical ? "physical free camera on (the world centred on it; " : "free camera on (")
                                      + "mouse: look; "
                                      + Keys.CAMERA_FORWARD.display()
                                      + " "
                                      + Keys.CAMERA_LEFT.display()
                                      + " "
                                      + Keys.CAMERA_BACK.display()
                                      + " "
                                      + Keys.CAMERA_RIGHT.display()
                                      + ", up "
                                      + Keys.CAMERA_UP.display()
                                      + ", down "
                                      + Keys.CAMERA_DOWN.display()
                                      + ", fast "
                                      + Keys.CAMERA_FAST.display()
                                      + ", wheel speed; back to the player: "
                                      + Keys.CAMERA_HOME.display()
                                      + "; off: "
                                      + Keys.FREE_CAMERA.display()
                                      + " or "
                                      + Keys.PHYSICAL_CAMERA.display()
                                      + ")"
                                      : "free camera off"
                      )
              );
   }

   private static int[] held(KeyBind var0) {
      ArrayList<Integer> var1 = new ArrayList<>();

      for (KeyChord var3 : var0.chords()) {
         for (int var5 : var3.keys()) {
            if (KeyInput.physicallyDown(var5) && !var1.contains(var5)) {
               var1.add(var5);
            }
         }
      }

      return var1.stream().mapToInt(Integer::intValue).toArray();
   }

   static boolean look(double var0, double var2) {
      if (!active) {
         return false;
      }

      yaw += (float)var0 * 0.0022F;
      yaw = yaw > Math.PI ? yaw - (float) (Math.PI * 2) : (yaw < -Math.PI ? yaw + (float) (Math.PI * 2) : yaw);
      pitch = Math.max(-MAX_PITCH, Math.min(MAX_PITCH, pitch - (float)var2 * 0.0022F));
      return true;
   }

   public static boolean view(Frame var0, float var1, float var2, float var3, float[] var4, float[] var5) {
      if (!active) {
         return false;
      }

      if (starting) {
         starting = false;
         x = var0.camX - var1;
         y = var0.camY - var3;
         z = var0.camZ + var2 / 2.4494896F;
         yaw = Look.yaw;
         pitch = Look.pitch;
      }

      FreeCam.Place var6 = new FreeCam.Place(x, y, z, yaw, pitch);
      place = var6;
      Camera.at(var0, var6, var4, var5);
      return true;
   }

   private FreeCam() {
   }

   public record Place(double x, double y, double z, float yaw, float pitch) {
   }
}