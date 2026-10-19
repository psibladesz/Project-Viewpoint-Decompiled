package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Tuning;

final class ReliefView {
   private static final LiveSettings.Choice SHOWN = LiveSettings.choice(
      "debug.relief", "Colour by relief", "Debug/Views", new String[]{"Off", "Height", "Slope"}, 0
   );
   private static final int OFF = 0;
   private static final int SLOPE = 2;
   private final GlProgram program = GlProgram.create("relief view", "screen.vert", "relief_view.frag");

   ReliefView() {
      this.program.sampler("uDepth", 12);
   }

   void draw(FrameContext var1) {
      FarRelief.Lift var2 = var1.scene.relief;
      int var3 = SHOWN.get();
      if (var3 != 0 && var2 != null && (var3 != 2 || var2.bands() != null)) {
         GL11.glEnable(3042);
         GL14.glBlendFuncSeparate(770, 771, 0, 1);
         this.program.use();
         var1.camera(this.program);
         this.program.setInt("uReliefView", var3);
         this.program.set("uReliefCycle", Tuning.reliefViewCycle);
         if (var3 == 2) {
            float[] var4 = var2.bands();
            this.program.set("uReliefBands", var4[0], var4[1], var4[2], var4[3]);
         }

         Gl.screenTriangle();
         GL11.glDisable(3042);
      }
   }
}
