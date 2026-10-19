package viewpoint.render;

import java.nio.ByteBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

final class Noise2D {
   static final int SIZE = 128;
   private static final long SEED = 5314517L;

   static int create() {
      ByteBuffer var0 = BufferUtils.createByteBuffer(65536);
      byte[] var1 = new byte[65536];
      new Random(5314517L).nextBytes(var1);
      var0.put(var1).flip();
      int var2 = GL11.glGenTextures();
      GL11.glBindTexture(3553, var2);
      GL11.glTexImage2D(3553, 0, 32856, 128, 128, 0, 6408, 5121, var0);
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      GL11.glTexParameteri(3553, 10242, 10497);
      GL11.glTexParameteri(3553, 10243, 10497);
      GL11.glTexParameteri(3553, 33085, 0);
      return var2;
   }

   private Noise2D() {
   }
}
