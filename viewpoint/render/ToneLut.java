package viewpoint.render;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;

final class ToneLut {
   static final int SIZE = 48;
   private static final String FILE = "/viewpoint/tone/tony_mc_mapface.dds";
   private static final int HEADER = 148;
   private static final int DXGI_FORMAT = 67;
   private static final int DXGI_AT = 128;
   final int texture;

   ToneLut() {
      ByteBuffer var1 = read();
      int var2 = GL11.glGetInteger(32874);
      int var3 = GL11.glGetInteger(35055);
      int var4 = GL11.glGetInteger(3317);
      GL15.glBindBuffer(35052, 0);
      GL11.glPixelStorei(3317, 4);
      this.texture = GL11.glGenTextures();
      GL11.glBindTexture(32879, this.texture);
      GL12.glTexImage3D(32879, 0, 35901, 48, 48, 48, 0, 6407, 35902, var1);
      GL11.glTexParameteri(32879, 10241, 9729);
      GL11.glTexParameteri(32879, 10240, 9729);
      GL11.glTexParameteri(32879, 10242, 33071);
      GL11.glTexParameteri(32879, 10243, 33071);
      GL11.glTexParameteri(32879, 32882, 33071);
      GL11.glBindTexture(32879, var2);
      GL15.glBindBuffer(35052, var3);
      GL11.glPixelStorei(3317, var4);
   }

   void bind() {
      GL13.glActiveTexture(34093);
      GL11.glBindTexture(32879, this.texture);
      GL13.glActiveTexture(33984);
   }

   private static ByteBuffer read() {
      byte[] var0;
      try (InputStream var1 = ToneLut.class.getResourceAsStream("/viewpoint/tone/tony_mc_mapface.dds")) {
         if (var1 == null) {
            throw new IllegalStateException("the tone curve's table is missing: /viewpoint/tone/tony_mc_mapface.dds");
         }

         var0 = var1.readAllBytes();
      } catch (IOException var6) {
         throw new IllegalStateException("the tone curve's table could not be read", var6);
      }

      ByteBuffer var7 = ByteBuffer.wrap(var0).order(ByteOrder.LITTLE_ENDIAN);
      int var2 = 442368;
      if (var0.length == 148 + var2 && var7.getInt(128) == 67) {
         ByteBuffer var3 = BufferUtils.createByteBuffer(var2);
         var3.put(var0, 148, var2).flip();
         return var3;
      } else {
         throw new IllegalStateException("the tone curve's table is not a 48^3 R9G9B9E5 volume: /viewpoint/tone/tony_mc_mapface.dds");
      }
   }
}
