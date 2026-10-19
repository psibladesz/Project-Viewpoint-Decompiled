package viewpoint.render;

import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

final class ColourShrink {
   private static int read;
   private static int draw;

   static int shrink(int var0, int var1, int var2) {
      if (read == 0) {
         read = GL30.glGenFramebuffers();
         draw = GL30.glGenFramebuffers();
      }

      int var3 = Integer.numberOfTrailingZeros(var1 / var2);
      int var4 = GL11.glGenTextures();
      GL11.glBindTexture(3553, var4);
      GL11.glTexImage2D(3553, 0, 32856, var2, var2, 0, 6408, 5121, (ByteBuffer)null);
      int var5 = GL11.glGetInteger(36010);
      int var6 = GL11.glGetInteger(36006);
      boolean var7 = GL11.glIsEnabled(3089);
      GL11.glDisable(3089);
      GL30.glBindFramebuffer(36008, read);
      GL30.glFramebufferTexture2D(36008, 36064, 3553, var0, var3);
      GL30.glBindFramebuffer(36009, draw);
      GL30.glFramebufferTexture2D(36009, 36064, 3553, var4, 0);
      GL30.glBlitFramebuffer(0, 0, var2, var2, 0, 0, var2, var2, 16384, 9728);
      GL30.glFramebufferTexture2D(36008, 36064, 3553, 0, 0);
      GL30.glFramebufferTexture2D(36009, 36064, 3553, 0, 0);
      GL30.glBindFramebuffer(36008, var5);
      GL30.glBindFramebuffer(36009, var6);
      if (var7) {
         GL11.glEnable(3089);
      }

      FarGpu.colourMips();
      return var4;
   }

   private ColourShrink() {
   }
}
