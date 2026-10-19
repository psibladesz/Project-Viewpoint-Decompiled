package viewpoint.camera;

import imgui.ImDrawList;
import imgui.ImGui;
import java.util.Locale;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import viewpoint.core.CameraShot;
import viewpoint.core.Frame;
import viewpoint.input.Camera;
import viewpoint.platform.Overlays;

public final class CameraDebug {
   private static final int WANTED = -12529409;
   private static final int PLACED = -10420384;
   private static final int POINT = -1;
   private static final int PROBE = -24512;
   private static final int CLEAR = -40705;
   private static final int TEXT = -1;
   private static final float LINE = 2.0F;
   private static final float RADIUS = 4.0F;
   private static final float MARGIN = 12.0F;
   private static final float TEXT_LINE = 16.0F;
   private static final int PIVOT = 0;
   private static final int SHOULDER = 1;
   private static final int WANT = 2;
   private static final int PLACE = 3;
   private static final int SIDE = 4;
   private static final int OTHER_SIDE = 5;
   private static final int OVER = 6;
   private static final int CHEST = 7;
   private static final int POINTS = 8;
   private static final int INNER = 0;
   private static final int OUTER = 1;
   private static final int AT_CHEST = 2;
   private static final int SPHERES = 3;
   private static final float LISTED = 0.01F;
   private static final float NO_LIMIT = 100.0F;
   private static final float[][] at = new float[8][3];
   private static final float[][] screen = new float[8][2];
   private static final boolean[] seen = new boolean[8];
   private static final float[][] radius = new float[3][2];
   private static final float[] dir = new float[3];
   private static final float[] offset = new float[3];
   private static final float[] middle = new float[2];
   private static final float[] end = new float[2];
   private static final Vector4f clip = new Vector4f();
   private static volatile boolean shown;
   private static String[] text = new String[0];

   public static void frame(Frame var0, boolean var1, float var2, float var3, float[] var4, Matrix4f var5) {
      CameraShot var6 = var0.cameraShot;
      shown = var1 && var6.on && CameraSettings.SHOW_RIG.get();
      if (shown) {
         float var7 = CameraView.wanted(var0, var2, var3, at[0], at[1], at[2]);
         System.arraycopy(var4, 0, at[3], 0, 3);
         probe(at[1], var2 - Boom.PROBE_YAW, var3, var7, at[4]);
         probe(at[1], var2 + Boom.PROBE_YAW, var3, var7, at[5]);
         probe(at[1], var2, var3 - Boom.PROBE_PITCH, var7, at[6]);
         at[7][0] = var6.chestX;
         at[7][1] = var6.chestY;
         at[7][2] = var6.chestZ;

         for (int var8 = 0; var8 < 8; var8++) {
            seen[var8] = project(var5, at[var8], screen[var8]);
         }

         sphere(var5, at[3], 0.45F, radius[0]);
         sphere(var5, at[3], 1.2F, radius[1]);
         sphere(var5, at[7], 0.85F, radius[2]);
         text = describe(var6);
      }
   }

   private static void probe(float[] var0, float var1, float var2, float var3, float[] var4) {
      Camera.direction(var1, var2, dir);

      for (int var5 = 0; var5 < 3; var5++) {
         var4[var5] = var0[var5] - dir[var5] * var3;
      }
   }

   private static boolean project(Matrix4f var0, float[] var1, float[] var2) {
      var0.transform(clip.set(var1[0], var1[1], var1[2], 1.0F));
      var2[0] = (clip.x / clip.w + 1.0F) / 2.0F;
      var2[1] = (1.0F - clip.y / clip.w) / 2.0F;
      return clip.w > 0.0F;
   }

   private static void sphere(Matrix4f var0, float[] var1, float var2, float[] var3) {
      project(var0, var1, middle);
      var3[0] = var3[1] = 0.0F;

      for (int var4 = 0; var4 < 3; var4++) {
         System.arraycopy(var1, 0, offset, 0, 3);
         offset[var4] = offset[var4] + var2;
         if (project(var0, offset, end)) {
            var3[0] = Math.max(var3[0], Math.abs(end[0] - middle[0]));
            var3[1] = Math.max(var3[1], Math.abs(end[1] - middle[1]));
         }
      }
   }

   private static String[] describe(CameraShot var0) {
      StringBuilder var1 = new StringBuilder("layers:");

      for (int var2 = 0; var2 < 15; var2++) {
         if (var0.weights[var2] >= 0.01F) {
            var1.append(String.format(Locale.ROOT, " %s %.2f", Profiles.NAMES[var2], var0.weights[var2]));
         }
      }

      String var3 = var0.limit > 100.0F ? "none" : String.format(Locale.ROOT, "%.2f", var0.limit);
      return new String[]{
         var1.length() > "layers:".length() ? var1.toString() : "layers: none (standing)",
         String.format(
            Locale.ROOT, "follow trails %.2f across, %.2f up; boom %.2f, limit %s; side %.2f", var0.trailAcross, var0.trailUp, var0.distance, var3, var0.side
         ),
         String.format(
            Locale.ROOT,
            "field of view %.1f; trauma %.2f; aim %.2f; framed at %.2f, %.2f",
            Math.toDegrees(var0.fovY),
            var0.trauma,
            var0.aim,
            var0.fromLeft,
            var0.fromTop
         )
      };
   }

   private static void draw() {
      ImDrawList var0 = ImGui.getForegroundDrawList();
      float var1 = ImGui.getIO().getDisplaySizeX();
      float var2 = ImGui.getIO().getDisplaySizeY();
      line(var0, 0, 1, -12529409, var1, var2);
      line(var0, 1, 2, -12529409, var1, var2);
      line(var0, 1, 3, -10420384, var1, var2);
      line(var0, 1, 4, -24512, var1, var2);
      line(var0, 1, 5, -24512, var1, var2);
      line(var0, 1, 6, -24512, var1, var2);
      line(var0, 3, 7, -40705, var1, var2);
      circle(var0, 3, radius[0], var1, var2);
      circle(var0, 3, radius[1], var1, var2);
      circle(var0, 7, radius[2], var1, var2);

      for (int var3 = 0; var3 < 8; var3++) {
         if (seen[var3]) {
            var0.addCircleFilled(screen[var3][0] * var1, screen[var3][1] * var2, 4.0F, var3 == 3 ? -10420384 : -1);
         }
      }

      String[] var5 = text;

      for (int var4 = 0; var4 < var5.length; var4++) {
         var0.addText(12.0F, var2 - 12.0F - (var5.length - var4) * 16.0F, -1, var5[var4]);
      }
   }

   private static void line(ImDrawList var0, int var1, int var2, int var3, float var4, float var5) {
      if (seen[var1] && seen[var2]) {
         var0.addLine(screen[var1][0] * var4, screen[var1][1] * var5, screen[var2][0] * var4, screen[var2][1] * var5, var3, 2.0F);
      }
   }

   private static void circle(ImDrawList var0, int var1, float[] var2, float var3, float var4) {
      if (seen[var1]) {
         var0.addCircle(screen[var1][0] * var3, screen[var1][1] * var4, Math.max(var2[0] * var3, var2[1] * var4), -40705, 0, 2.0F);
      }
   }

   private CameraDebug() {
   }

   static {
      Overlays.add(() -> shown, CameraDebug::draw);
   }
}
