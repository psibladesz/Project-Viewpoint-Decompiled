package viewpoint.input;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.lwjgl.util.vector.Matrix4f;
import viewpoint.core.CameraShot;
import viewpoint.core.Frame;
import viewpoint.platform.LiveSettings;
import zombie.ZomboidFileSystem;
import zombie.characters.IsoPlayer;
import zombie.core.skinnedmodel.animation.AnimationPlayer;
import zombie.core.skinnedmodel.model.Model;
import zombie.iso.Vector3;

public final class HeadLookDebug {
   static final LiveSettings.Toggle ON = LiveSettings.toggle("camera.headDebug", "Show and trace the head look", "Debug/Camera", false).unsaved();
   static final int BODY = 0;
   static final int GAZE = 1;
   static final int OFF = 2;
   static final int ACROSS = 3;
   static final int WEIGHT = 4;
   static final int VALUES = 5;
   private static final int FACING = -1;
   private static final int LOOKING = -32704;
   private static final int AIMED = -12517377;
   private static final int TURNED = -48897;
   private static final int AXIS_X = -12566273;
   private static final int AXIS_Y = -12517568;
   private static final int AXIS_Z = -192;
   private static final float LONG = 0.8F;
   private static final float SHORT = 0.3F;
   private static final String FOLDER = "viewpoint-camera-traces";
   private static final float DEG = (float) (180.0 / Math.PI);
   private static final int MATRIX = 12;
   private static final float[] headBefore = new float[12];
   private static final float[] neckBefore = new float[12];
   private static final float[] headAfter = new float[12];
   private static final float[] neckAfter = new float[12];
   private static final Vector3 v = new Vector3();
   private static final float[] directions = new float[48];
   private static final float[] lengths = new float[16];
   private static final int[] colours = new int[16];
   private static float headX;
   private static float headY;
   private static float headZ;
   private static int lines;
   private static String[] text = new String[0];
   private static boolean wasOn;
   private static BufferedWriter out;
   private static long startNanos;

   public static void declare() {
      ON.get();
   }

   static boolean tracing() {
      boolean var0 = ON.get();
      if (var0 != wasOn) {
         wasOn = var0;
         if (var0) {
            open();
         } else {
            close();
         }
      }

      return var0;
   }

   static void before(AnimationPlayer var0, int var1, int var2) {
      copy(var0.getModelTransformAt(var2), headBefore);
      copy(var0.getModelTransformAt(var1), neckBefore);
   }

   static void after(AnimationPlayer var0, IsoPlayer var1, int var2, int var3, float[] var4, boolean var5, boolean var6) {
      copy(var0.getModelTransformAt(var3), headAfter);
      copy(var0.getModelTransformAt(var2), neckAfter);
      float var7 = var0.getRenderedAngle();
      v.set(headAfter[9], headAfter[10], headAfter[11]);
      Model.vectorToWorldCoords(var1.getX(), var1.getY(), var1.getZ(), var7, v);
      headX = v.x;
      headY = v.y;
      headZ = v.z;
      lines(var0, var4);
      String var8 = var1.getCurrentActionContextStateName();
      text = text(var0, var1, var4, var5, var6, var8);
      write(var0, var1, var4, var5, var6, var8);
   }

   private static void lines(AnimationPlayer var0, float[] var1) {
      float var2 = var0.getRenderedAngle();
      lines = 0;
      angle(var1[0], 0.0F, 0.8F, -1);
      angle(Look.yaw, Look.pitch, 0.8F, -32704);
      angle(var1[0] + var1[3], 0.0F, 0.8F, -12517377);
      axis(headAfter, 2, var2, 0.8F, -48897);
      axis(headBefore, 0, var2, 0.3F, -12566273);
      axis(headBefore, 1, var2, 0.3F, -12517568);
      axis(headBefore, 2, var2, 0.3F, -192);
   }

   private static void angle(float var0, float var1, float var2, int var3) {
      float var4 = (float)Math.cos(var1);
      line((float)Math.cos(var0) * var4, (float)Math.sin(var0) * var4, (float)Math.sin(var1), var2, var3);
   }

   private static void axis(float[] var0, int var1, float var2, float var3, int var4) {
      world(var0, var1, var2);
      float var5 = (float)Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
      if (var5 > 0.0F) {
         line(v.x / var5, v.y / var5, v.z / var5, var3, var4);
      }
   }

   private static void line(float var0, float var1, float var2, float var3, int var4) {
      if (lines != 16) {
         directions[lines * 3] = var0;
         directions[lines * 3 + 1] = var1;
         directions[lines * 3 + 2] = var2;
         lengths[lines] = var3;
         colours[lines++] = var4;
      }
   }

   private static void world(float[] var0, int var1, float var2) {
      v.set(var0[var1], var0[3 + var1], var0[6 + var1]);
      Model.vectorToWorldCoords(0.0F, 0.0F, 0.0F, var2, v);
      v.z *= 2.4494896F;
   }

   private static float facing(float[] var0, int var1, float var2) {
      world(var0, var1, var2);
      return (float)Math.atan2(v.y, v.x) * (float) (180.0 / Math.PI);
   }

   private static float upward(float[] var0, int var1, float var2) {
      world(var0, var1, var2);
      return v.z / (float)Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
   }

   private static String[] text(AnimationPlayer var0, IsoPlayer var1, float[] var2, boolean var3, boolean var4, String var5) {
      float var6 = var0.getRenderedAngle();
      return new String[]{
         String.format(
            Locale.ROOT,
            "head look %s, weight %.2f; turning %s, turning around %s; %s",
            var3 ? "on" : "off",
            var2[4],
            var4 ? "yes" : "no",
            var1.isTurningAround() ? "yes" : "no",
            var5
         ),
         String.format(
            Locale.ROOT,
            "body %.1f, target %.1f, rendered %.1f, twist %.1f; look %.1f, pitch %.1f; gaze %.1f",
            var2[0] * (float) (180.0 / Math.PI),
            var0.getTargetAngle() * (float) (180.0 / Math.PI),
            var6 * (float) (180.0 / Math.PI),
            var0.getTwistAngle() * (float) (180.0 / Math.PI),
            Look.yaw * (float) (180.0 / Math.PI),
            Look.pitch * (float) (180.0 / Math.PI),
            var2[1] * (float) (180.0 / Math.PI)
         ),
         String.format(
            Locale.ROOT,
            "off %.1f, across %.1f; head as posed: +Z %.1f, +X %.1f, +Y %.1f (up %.2f); as turned +Z %.1f",
            var2[2] * (float) (180.0 / Math.PI),
            var2[3] * (float) (180.0 / Math.PI),
            facing(headBefore, 2, var6),
            facing(headBefore, 0, var6),
            facing(headBefore, 1, var6),
            upward(headBefore, 1, var6),
            facing(headAfter, 2, var6)
         )
      };
   }

   public static void capture(Frame var0, boolean var1) {
      CameraShot var2 = var0.cameraShot;
      boolean var3 = var1 && ON.get() && lines > 0;
      var2.debugLineCount = var3 ? lines : 0;
      var2.debugText = var3 ? text : new String[0];
      if (var3) {
         float var4 = -(headX - var0.camX);
         float var5 = (headZ - var0.camZ) * 2.4494896F;
         float var6 = -(headY - var0.camY);

         for (int var7 = 0; var7 < lines; var7++) {
            float var8 = lengths[var7];
            var2.debugLines[var7 * 6] = var4;
            var2.debugLines[var7 * 6 + 1] = var5;
            var2.debugLines[var7 * 6 + 2] = var6;
            var2.debugLines[var7 * 6 + 3] = var4 - directions[var7 * 3] * var8;
            var2.debugLines[var7 * 6 + 4] = var5 + directions[var7 * 3 + 2] * var8;
            var2.debugLines[var7 * 6 + 5] = var6 - directions[var7 * 3 + 1] * var8;
            var2.debugColours[var7] = colours[var7];
         }
      }
   }

   private static void copy(Matrix4f var0, float[] var1) {
      var1[0] = var0.m00;
      var1[1] = var0.m01;
      var1[2] = var0.m02;
      var1[3] = var0.m10;
      var1[4] = var0.m11;
      var1[5] = var0.m12;
      var1[6] = var0.m20;
      var1[7] = var0.m21;
      var1[8] = var0.m22;
      var1[9] = var0.m03;
      var1[10] = var0.m13;
      var1[11] = var0.m23;
   }

   private static void open() {
      try {
         Path var0 = new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-camera-traces").toPath();
         Files.createDirectories(var0);
         Path var1 = var0.resolve("headlook-" + System.currentTimeMillis() + ".txt");
         out = Files.newBufferedWriter(var1, StandardCharsets.UTF_8);
         out.write(
            "ms animation state on turning turningAround aiming weight body target rendered twist targetTwist excessTwist deferred forward look lookPitch gaze off across | head as posed: rows, place | head as turned | neck as posed | neck as turned"
         );
         out.newLine();
         startNanos = System.nanoTime();
         System.out.println("[Viewpoint] head look trace recording to " + var1);
      } catch (IOException var2) {
         out = null;
         System.out.println("[Viewpoint] head look trace failed: " + var2);
      }
   }

   private static void close() {
      if (out != null) {
         try {
            out.close();
            System.out.println("[Viewpoint] head look trace stopped");
         } catch (IOException var1) {
            System.out.println("[Viewpoint] head look trace failed to close: " + var1);
         }

         out = null;
      }
   }

   private static void write(AnimationPlayer var0, IsoPlayer var1, float[] var2, boolean var3, boolean var4, String var5) {
      if (out != null) {
         StringBuilder var6 = new StringBuilder(640);
         var6.append(String.format(Locale.ROOT, "%.2f ", (System.nanoTime() - startNanos) * 1.0E-6));
         var6.append(Integer.toHexString(System.identityHashCode(var0))).append(' ');
         var6.append(var5.isEmpty() ? "-" : var5).append(' ');
         var6.append(var3 ? 1 : 0).append(' ').append(var4 ? 1 : 0).append(' ');
         var6.append(var1.isTurningAround() ? 1 : 0).append(' ').append(var1.isAiming() ? 1 : 0).append(' ');
         floats(var6, var2[4]);
         degrees(var6, var2[0], var0.getTargetAngle(), var0.getRenderedAngle());
         degrees(var6, var0.getTwistAngle(), var0.getTargetTwistAngle(), var0.getExcessTwistAngle());
         degrees(var6, var0.getDeferredAngleDelta(), var1.getDirectionAngleRadians(), Look.yaw, Look.pitch);
         degrees(var6, var2[1], var2[2], var2[3]);

         for (float[] var10 : new float[][]{headBefore, headAfter, neckBefore, neckAfter}) {
            var6.append("| ");
            floats(var6, var10);
         }

         try {
            out.write(var6.toString());
            out.newLine();
         } catch (IOException var11) {
            System.out.println("[Viewpoint] head look trace stopped: " + var11);
            out = null;
         }
      }
   }

   private static void degrees(StringBuilder var0, float... var1) {
      for (float var5 : var1) {
         var0.append(String.format(Locale.ROOT, "%.2f ", var5 * (float) (180.0 / Math.PI)));
      }
   }

   private static void floats(StringBuilder var0, float... var1) {
      for (float var5 : var1) {
         var0.append(String.format(Locale.ROOT, "%.4f ", var5));
      }
   }

   private HeadLookDebug() {
   }

   static {
      ON.describe(
         "In third person, draws the head look over the character and writes every pose's numbers into Zomboid/viewpoint-camera-traces, to find what turns the head wrong."
      );
   }
}
