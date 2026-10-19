package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;

final class BlurPass {
   private final GlProgram program = GlProgram.create("blur", "screen.vert", "volume_blur.frag");

   BlurPass() {
      this.program.sampler("uVolume", 0);
      this.program.sampler("uDepth", 12);
   }

   void run(FrameContext var1, int var2, int var3, int var4, int var5, int var6) {
      this.program.use();
      var1.camera(this.program);
      Gl.bind(12, var2);
      GL13.glActiveTexture(33984);
      int var7 = this.program.location("uStep");
      GL30.glBindFramebuffer(36160, var6);
      GL11.glBindTexture(3553, var3);
      GL20.glUniform2f(var7, 1.0F / var1.halfWidth, 0.0F);
      Gl.screenTriangle();
      GL30.glBindFramebuffer(36160, var4);
      GL11.glBindTexture(3553, var5);
      GL20.glUniform2f(var7, 0.0F, 1.0F / var1.halfHeight);
      Gl.screenTriangle();
   }
}
