package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;

final class BedColours {
   private final GlProgram program = GlProgram.create("bed colours", "screen.vert", "bed_colours.frag");
   private final int texture;
   private final int fbo;
   private final int size;
   private boolean made;

   BedColours(int var1) {
      this.size = var1;
      int var2 = GL11.glGetInteger(32873);
      int var3 = GL11.glGetInteger(36006);
      this.texture = Gl.texture(32856, var1, var1, 6408, 5121, 9729);
      GL11.glTexParameteri(3553, 10242, 10497);
      GL11.glTexParameteri(3553, 10243, 10497);
      GL11.glBindTexture(3553, var2);
      this.fbo = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, this.fbo);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, this.texture, 0);
      GL30.glBindFramebuffer(36160, var3);
      this.program.sampler("uFloorColours", 0);
   }

   void draw(int var1, int var2) {
      GL30.glBindFramebuffer(36160, this.fbo);
      GL11.glViewport(0, 0, this.size, this.size);
      this.program.use();
      this.program.setInt("uRow", var2);
      Gl.bind(0, var1);
      Gl.screenTriangle();
      Gl.bind(0, 0);
      this.made = true;
   }

   void skip() {
      this.made = false;
   }

   void bind() {
      Gl.bind(129, this.made ? this.texture : 0);
      GL13.glActiveTexture(33984);
   }

   boolean made() {
      return this.made;
   }
}
