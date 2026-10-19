package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;

final class LampFields {
   static final int COLUMNS = 8;
   static final int TEXELS = 84;
   private static final int ROWS = 6;
   private final GlProgram program = GlProgram.create("lamp fields", "screen.vert", "lamp_field.frag");
   private final int texture;
   private final int fbo;
   private final int[] viewport = new int[4];

   LampFields() {
      int var1 = GL11.glGetInteger(32873);
      int var2 = GL11.glGetInteger(36006);
      this.texture = Gl.texture(33321, 672, 504, 6403, 5121, 9729);
      GL11.glBindTexture(3553, var1);
      this.fbo = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, this.fbo);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, this.texture, 0);
      int var3 = GL30.glCheckFramebufferStatus(36160);
      if (var3 != 36053) {
         throw new IllegalStateException("the lamps' fields' target is incomplete: 0x" + Integer.toHexString(var3));
      }

      GL30.glBindFramebuffer(36160, var2);
   }

   void draw(SceneData var1) {
      if (var1.lightCount > 0) {
         int var2 = GL11.glGetInteger(36006);
         GL11.glGetIntegerv(2978, this.viewport);
         boolean var3 = GL11.glIsEnabled(2929);
         boolean var4 = GL11.glIsEnabled(3042);
         GL11.glDisable(2929);
         GL11.glDisable(3042);
         GL30.glBindFramebuffer(36160, this.fbo);
         GL11.glViewport(0, 0, 672, 504);
         this.program.use();
         SurfacePass.lamps(this.program, var1);
         Gl.screenTriangle();
         GL30.glBindFramebuffer(36160, var2);
         GL11.glViewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
         restore(2929, var3);
         restore(3042, var4);
      }

      this.bind();
   }

   void bind() {
      GL13.glActiveTexture(34095);
      GL11.glBindTexture(3553, this.texture);
      GL13.glActiveTexture(33984);
   }

   private static void restore(int var0, boolean var1) {
      if (var1) {
         GL11.glEnable(var0);
      } else {
         GL11.glDisable(var0);
      }
   }
}
