package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.LiveSettings;

final class SpotMarks {
   private static final LiveSettings.Toggle ON = LiveSettings.toggle("debug.headlamps", "Mark the vehicles' lamps", "Debug/Views", false);
   private static final int VERTICES = 18;
   private final GlProgram program = GlProgram.create("spot marks", "spot_mark.vert", "spot_mark.frag");

   SpotMarks() {
      Spots.sampler(this.program);
      ON.describe(
         "A yellow dot on each head lamp's lens and a line along its beam, a red one on each tail or brake light, and a short cyan line the way each lamp's glass faces, drawn over everything."
      );
   }

   void draw(FrameContext var1, Spots var2, int var3) {
      if (ON.get() && var3 != 0) {
         Gl.resetState();
         GL30.glBindFramebuffer(36009, var1.outputDrawFbo);
         GL30.glBindFramebuffer(36008, var1.outputReadFbo);
         GL11.glViewport(var1.viewport[0], var1.viewport[1], var1.viewport[2], var1.viewport[3]);
         GL11.glDisable(2929);
         GL11.glDepthMask(false);
         this.program.use();
         var2.uniforms(this.program);
         this.program.set("uViewProjection", var1.plainViewProjection);
         this.program.set("uPixel", 2.0F / var1.viewport[2], 2.0F / var1.viewport[3]);
         var2.bind(true);
         Gl.generated(var3 * 18);
         var2.bind(false);
         GL11.glDepthMask(true);
      }
   }
}
