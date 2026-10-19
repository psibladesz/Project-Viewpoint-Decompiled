package viewpoint.gunplay;

import imgui.ImDrawList;
import imgui.ImGui;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import viewpoint.core.Frame;
import viewpoint.core.View;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Overlays;
import viewpoint.render.Panini;
import zombie.core.Core;

public final class ShotDebug {
   static final LiveSettings.Toggle SHOW = LiveSettings.toggle("gunplay.showCone", "Show the shot's cone", "Debug/Shooting", false);
   private static final long SHOT_SHOWN = 2000000000L;
   private static final float LINE = 1.5F;
   private static final float DOT = 3.0F;
   private static final int RING = -1056964609;
   private static final int DOT_COLOUR = -14663425;
   private static volatile float ring;
   private static volatile float fovY;
   private static volatile float centreScale = 1.0F;
   private static volatile double shotX;
   private static volatile double shotY;
   private static volatile double shotUp;
   private static volatile long shotNanos;
   private static final Matrix4f viewProjection = new Matrix4f();
   private static final Vector4f clip = new Vector4f();
   private static final float[] onScreen = new float[2];
   private static boolean seen;
   private static float screenX;
   private static float screenY;

   static void aiming(double var0) {
      ring = (float)var0;
      fovY = View.frameFovY;
      float var2 = (float)Core.getInstance().getScreenWidth() / Math.max(1, Core.getInstance().getScreenHeight());
      centreScale = Panini.ofView(fovY, var2, Panini.strength()).centreScale();
   }

   static void idle() {
      ring = 0.0F;
   }

   static void shot(double var0, double var2, double var4) {
      shotX = var0;
      shotY = var2;
      shotUp = var4;
      shotNanos = System.nanoTime();
   }

   public static void frame(Frame var0, Matrix4f var1) {
      seen = false;
      if (SHOW.get() && recent()) {
         var0.scene.projection.mul(var1, viewProjection);
         float var2 = (float)(-(shotX - var0.camX));
         float var3 = (float)(shotUp - var0.camZ * 2.4494896F);
         viewProjection.transform(clip.set(var2, var3, (float)(-(shotY - var0.camY)), 1.0F));
         seen = clip.w > 0.0F;
         Panini.of(var0.scene.projection, var0.scene.panini).toScreen(clip.x / clip.w, clip.y / clip.w, onScreen);
         screenX = (onScreen[0] + 1.0F) / 2.0F;
         screenY = (1.0F - onScreen[1]) / 2.0F;
      }
   }

   private static boolean recent() {
      return shotNanos != 0L && System.nanoTime() - shotNanos < 2000000000L;
   }

   private static void draw() {
      ImDrawList var0 = ImGui.getForegroundDrawList();
      float var1 = ImGui.getIO().getDisplaySizeX();
      float var2 = ImGui.getIO().getDisplaySizeY();
      float var3 = var1 * (1.0F + View.ringX) / 2.0F;
      float var4 = var2 * (1.0F - View.ringY) / 2.0F;
      float var5 = centreScale * var2 / 2.0F / (float)Math.tan(fovY / 2.0F);
      if (ring > 0.0F) {
         var0.addCircle(var3, var4, ring * var5, -1056964609, 0, 1.5F);
      }

      if (seen) {
         var0.addCircleFilled(screenX * var1, screenY * var2, 3.0F, -14663425);
      }
   }

   private ShotDebug() {
   }

   static {
      SHOW.describe(
         "Draws the ring the crosshair wanders within, from the game's hit chance, and where each shot went in the world. For checking the aim; nothing is drawn while not aiming a firearm."
      );
      Overlays.add(() -> SHOW.get() && View.enabled && (ring > 0.0F || recent()), ShotDebug::draw);
   }
}
