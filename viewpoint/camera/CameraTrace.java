package viewpoint.camera;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import viewpoint.core.CameraSquares;
import viewpoint.core.Frame;
import viewpoint.platform.Keys;
import zombie.ZomboidFileSystem;

public final class CameraTrace {
   static final String FOLDER = "viewpoint-camera-traces";
   static final String SUFFIX = ".camtrace";
   private static final String PART = " | ";
   private static BufferedWriter out;

   static void poll() {
      if (Keys.CAMERA_TRACE.pressed()) {
         try {
            if (out == null) {
               Path var0 = new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-camera-traces").toPath();
               Files.createDirectories(var0);
               Path var1 = var0.resolve("trace-" + System.currentTimeMillis() + ".camtrace");
               out = Files.newBufferedWriter(var1, StandardCharsets.UTF_8);
               System.out.println("[Viewpoint] camera trace recording to " + var1);
            } else {
               out.close();
               out = null;
               System.out.println("[Viewpoint] camera trace stopped");
            }
         } catch (IOException var2) {
            out = null;
            System.out.println("[Viewpoint] camera trace failed: " + var2);
         }
      }
   }

   static void record(Frame var0, CameraInputs var1, double[] var2, float var3, float var4) {
      if (out != null) {
         try {
            out.write(line(var0, var1, var2, var3, var4));
            out.newLine();
         } catch (IOException var6) {
            System.out.println("[Viewpoint] camera trace stopped: " + var6);
            out = null;
         }
      }
   }

   static String line(Frame var0, CameraInputs var1, double[] var2, float var3, float var4) {
      StringBuilder var5 = new StringBuilder(1024);
      floats(var5, var3, var4, var0.viewYaw, var0.viewPitch, var0.camX, var0.camY, var0.camZ);
      floats(var5, var0.eyeX, var0.eyeY, var0.eyeZ, (float)var2[0], (float)var2[1], (float)var2[2]);
      var5.append(" | ");
      floats(var5, var1.speed, flag(var1.moving), flag(var1.running), flag(var1.sprinting), flag(var1.sneaking));
      floats(var5, flag(var1.attacking), var1.aim, var1.special, var1.fallTime, flag(var1.seated), var1.vehicleSpeed);
      floats(var5, var1.vehicleYaw, var1.threat, var1.panic, var1.tired, var1.headBob, var1.facing, var1.health);
      var5.append(var1.state.isEmpty() ? "-" : var1.state).append(" | ");
      CameraSquares var6 = var0.cameraSquares;
      var5.append(var6.x).append(' ').append(var6.y).append(' ').append(var6.level).append(' ').append(var6.side).append(' ');

      for (int var7 = 0; var7 < var6.side * var6.side * 2; var7++) {
         var5.append(Character.forDigit(var6.blocks[var7] & 15, 16));
      }

      return var5.toString();
   }

   static float[] read(String var0, Frame var1, CameraInputs var2, double[] var3) {
      String[] var4 = var0.split(" \\| ");
      String var5 = var4[1].strip();
      int var6 = var5.lastIndexOf(32);
      float[] var7 = numbers(var4[0]);
      float[] var8 = numbers(var5.substring(0, var6));
      var1.viewYaw = var7[2];
      var1.viewPitch = var7[3];
      var1.camX = var7[4];
      var1.camY = var7[5];
      var1.camZ = var7[6];
      var1.eyeX = var7[7];
      var1.eyeY = var7[8];
      var1.eyeZ = var7[9];
      var3[0] = var7[10];
      var3[1] = var7[11];
      var3[2] = var7[12];
      inputs(var8, var5.substring(var6 + 1), var2);
      squares(var4[2], var1.cameraSquares);
      return new float[]{var7[0], var7[1]};
   }

   private static void inputs(float[] var0, String var1, CameraInputs var2) {
      var2.speed = var0[0];
      var2.moving = var0[1] != 0.0F;
      var2.running = var0[2] != 0.0F;
      var2.sprinting = var0[3] != 0.0F;
      var2.sneaking = var0[4] != 0.0F;
      var2.attacking = var0[5] != 0.0F;
      var2.aim = (int)var0[6];
      var2.special = (int)var0[7];
      var2.fallTime = var0[8];
      var2.seated = var0[9] != 0.0F;
      var2.vehicleSpeed = var0[10];
      var2.vehicleYaw = var0[11];
      var2.threat = (int)var0[12];
      var2.panic = var0[13];
      var2.tired = var0[14];
      var2.headBob = var0[15];
      var2.facing = var0[16];
      var2.health = var0[17];
      var2.state = "-".equals(var1) ? "" : var1;
   }

   static void squares(String var0, CameraSquares var1) {
      String[] var2 = var0.strip().split(" ");
      var1.on = true;
      var1.x = Integer.parseInt(var2[0]);
      var1.y = Integer.parseInt(var2[1]);
      var1.level = Integer.parseInt(var2[2]);
      var1.side = Integer.parseInt(var2[3]);
      var1.blocks = new byte[var2[4].length()];

      for (int var3 = 0; var3 < var1.blocks.length; var3++) {
         var1.blocks[var3] = (byte)Character.digit(var2[4].charAt(var3), 16);
      }
   }

   private static float[] numbers(String var0) {
      String[] var1 = var0.strip().split(" ");
      float[] var2 = new float[var1.length];

      for (int var3 = 0; var3 < var1.length; var3++) {
         var2[var3] = Float.parseFloat(var1[var3]);
      }

      return var2;
   }

   private static void floats(StringBuilder var0, float... var1) {
      for (float var5 : var1) {
         var0.append(String.format(Locale.ROOT, "%.9g", var5)).append(' ');
      }
   }

   private static float flag(boolean var0) {
      return var0 ? 1.0F : 0.0F;
   }

   private CameraTrace() {
   }
}
