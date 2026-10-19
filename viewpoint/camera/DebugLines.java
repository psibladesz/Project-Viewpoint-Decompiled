package viewpoint.camera;

import imgui.ImDrawList;
import imgui.ImGui;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import viewpoint.core.CameraShot;
import viewpoint.core.Frame;
import viewpoint.platform.Overlays;
import viewpoint.render.Panini;

public final class DebugLines {
   private static final float LINE = 2.0F;
   private static final float MARGIN = 12.0F;
   private static final float TEXT_LINE = 16.0F;
   private static final float TEXT_TOP = 0.55F;
   private static final int TEXT = -1;
   private static final float[] screen = new float[64];
   private static final boolean[] seen = new boolean[16];
   private static final int[] colours = new int[16];
   private static final Vector4f clip = new Vector4f();
   private static final float[] onScreen = new float[2];
   private static Panini panini = Panini.NONE;
   private static volatile boolean shown;
   private static int count;
   private static String[] text = new String[0];

   public static void frame(Frame var0, boolean var1, Matrix4f var2) {
      CameraShot var3 = var0.cameraShot;
      shown = var1 && (var3.debugLineCount > 0 || var3.debugText.length > 0);
      panini = Panini.of(var0.scene.projection, var0.scene.panini);
      if (shown) {
         count = var3.debugLineCount;

         for (int var4 = 0; var4 < count; var4++) {
            float[] var5 = var3.debugLines;
            seen[var4] = project(var2, var5[var4 * 6], var5[var4 * 6 + 1], var5[var4 * 6 + 2], var4 * 4)
               & project(var2, var5[var4 * 6 + 3], var5[var4 * 6 + 4], var5[var4 * 6 + 5], var4 * 4 + 2);
            colours[var4] = var3.debugColours[var4];
         }

         text = var3.debugText;
      }
   }

   private static boolean project(Matrix4f var0, float var1, float var2, float var3, int var4) {
      var0.transform(clip.set(var1, var2, var3, 1.0F));
      panini.toScreen(clip.x / clip.w, clip.y / clip.w, onScreen);
      screen[var4] = (onScreen[0] + 1.0F) / 2.0F;
      screen[var4 + 1] = (1.0F - onScreen[1]) / 2.0F;
      return clip.w > 0.0F;
   }

   private static void draw() {
      ImDrawList var0 = ImGui.getForegroundDrawList();
      float var1 = ImGui.getIO().getDisplaySizeX();
      float var2 = ImGui.getIO().getDisplaySizeY();

      for (int var3 = 0; var3 < count; var3++) {
         if (seen[var3]) {
            int var4 = var3 * 4;
            var0.addLine(screen[var4] * var1, screen[var4 + 1] * var2, screen[var4 + 2] * var1, screen[var4 + 3] * var2, colours[var3], 2.0F);
         }
      }

      String[] var5 = text;

      for (int var6 = 0; var6 < var5.length; var6++) {
         var0.addText(12.0F, var2 * 0.55F + var6 * 16.0F, -1, var5[var6]);
      }
   }

   private DebugLines() {
   }

   static {
      Overlays.add(() -> shown, DebugLines::draw);
   }
}
