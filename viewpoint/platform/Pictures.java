package viewpoint.platform;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

final class Pictures {
   static Pictures.Texels decode(String var0) throws Exception {
      BufferedImage var1;
      try (InputStream var2 = Pictures.class.getResourceAsStream(var0)) {
         if (var2 == null) {
            throw new IllegalStateException("no " + var0 + " in the JAR");
         }

         var1 = ImageIO.read(var2);
      }

      int var12 = var1.getWidth();
      int var3 = var1.getHeight();
      int[] var4 = var1.getRGB(0, 0, var12, var3, null, 0, var12);
      ByteBuffer var5 = BufferUtils.createByteBuffer(var12 * var3 * 4);

      for (int var9 : var4) {
         var5.put((byte)(var9 >> 16)).put((byte)(var9 >> 8)).put((byte)var9).put((byte)(var9 >>> 24));
      }

      var5.flip();
      return new Pictures.Texels(var5, var12, var3);
   }

   static Pictures.Picture upload(Pictures.Texels var0) {
      int var1 = GL11.glGetInteger(32873);
      int var2 = GL11.glGetInteger(35055);
      int var3 = GL11.glGetInteger(3317);
      int var4 = GL11.glGetInteger(3314);
      GL15.glBindBuffer(35052, 0);
      GL11.glPixelStorei(3317, 4);
      GL11.glPixelStorei(3314, 0);
      int var5 = GL11.glGenTextures();
      GL11.glBindTexture(3553, var5);
      GL11.glTexImage2D(3553, 0, 32856, var0.width(), var0.height(), 0, 6408, 5121, var0.rgba());
      GL30.glGenerateMipmap(3553);
      GL11.glTexParameteri(3553, 10241, 9987);
      GL11.glTexParameteri(3553, 10240, 9729);
      GL11.glTexParameteri(3553, 10242, 33071);
      GL11.glTexParameteri(3553, 10243, 33071);
      GL11.glBindTexture(3553, var1);
      GL11.glPixelStorei(3317, var3);
      GL11.glPixelStorei(3314, var4);
      GL15.glBindBuffer(35052, var2);
      return new Pictures.Picture(var5, var0.width(), var0.height());
   }

   private Pictures() {
   }

   record Picture(int texture, int width, int height) {
      float aspect() {
         return (float)this.width / this.height;
      }
   }

   record Texels(ByteBuffer rgba, int width, int height) {
   }
}
