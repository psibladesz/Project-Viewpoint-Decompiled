package viewpoint.render;

import org.lwjgl.opengl.GL11;
import viewpoint.platform.GlProgram;
import viewpoint.platform.LiveSettings;

final class Wireframe {
   static final String SECTION = "Debug/Views";
   private static final LiveSettings.Toggle LINES = LiveSettings.toggle("debug.wireframe", "Wireframe", "Debug/Views", false);
   private static final LiveSettings.Toggle BARE = LiveSettings.toggle("debug.wireframeOnly", "Wireframe only (untextured)", "Debug/Views", false);
   static final int OFF = 0;
   static final int TEXTURED = 1;
   static final int UNTEXTURED = 2;
   private static int drawing;

   static int mode() {
      return BARE.get() ? 2 : (LINES.get() ? 1 : 0);
   }

   static int drawing() {
      return drawing;
   }

   static void begin() {
      drawing = mode();
      if (drawing != 0) {
         GL11.glPolygonMode(1032, 6913);
      }
   }

   static void end() {
      if (drawing != 0) {
         GL11.glPolygonMode(1032, 6914);
      }

      drawing = 0;
   }

   static void apply(GlProgram var0) {
      if (var0.has("uWireframe")) {
         var0.setInt("uWireframe", drawing);
      }
   }

   private Wireframe() {
   }
}
