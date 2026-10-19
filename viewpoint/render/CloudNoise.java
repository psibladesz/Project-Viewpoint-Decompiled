package viewpoint.render;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;

final class CloudNoise {
   static final int SHAPE = 128;
   static final int DETAIL = 32;
   static final int CURL = 128;
   static final int SHEETS = 1024;
   private static final String SHAPE_FILE = "/viewpoint/clouds/shape.png";
   private static final String DETAIL_FILE = "/viewpoint/clouds/detail.png";
   private static final String CURL_FILE = "/viewpoint/clouds/curl.png";
   private static final String SHEETS_FILE = "/viewpoint/clouds/sheets.png";
   private static final float SHEETS_ANISOTROPY = 16.0F;
   static boolean waits;
   private volatile ByteBuffer[] read;
   private volatile Exception failed;
   final int shape;
   final int detail;
   final int curl;
   final int sheets;
   private final Thread reader;
   private boolean uploaded;

   CloudNoise() {
      int var1 = GL11.glGetInteger(32873);
      int var2 = GL11.glGetInteger(32874);
      this.shape = volume(128);
      this.detail = volume(32);
      GL11.glBindTexture(32879, var2);
      this.curl = Gl.texture(33323, 128, 128, 33319, 5121, 9729);
      GL11.glTexParameteri(3553, 10242, 10497);
      GL11.glTexParameteri(3553, 10243, 10497);
      this.sheets = sheetsTexture();
      GL11.glBindTexture(3553, var1);
      this.reader = new Thread(this::readAll, "Viewpoint clouds' noise");
      this.reader.setDaemon(true);
      this.reader.start();
   }

   void update() {
      if (!this.uploaded) {
         if (waits) {
            try {
               this.reader.join();
            } catch (InterruptedException var8) {
               Thread.currentThread().interrupt();
               throw new IllegalStateException("interrupted waiting for the clouds' noise", var8);
            }
         }

         if (this.failed != null) {
            throw new IllegalStateException("the clouds' noise could not be read", this.failed);
         }

         ByteBuffer[] var1 = this.read;
         if (var1 != null) {
            int var2 = GL11.glGetInteger(35055);
            int var3 = GL11.glGetInteger(3317);
            int var4 = GL11.glGetInteger(3314);
            int var5 = GL11.glGetInteger(32878);
            GL15.glBindBuffer(35052, 0);
            GL11.glPixelStorei(3317, 4);
            GL11.glPixelStorei(3314, 0);
            GL11.glPixelStorei(32878, 0);
            int var6 = GL11.glGetInteger(32873);
            int var7 = GL11.glGetInteger(32874);
            fill(this.shape, 128, var1[0]);
            fill(this.detail, 32, var1[1]);
            GL11.glBindTexture(3553, this.curl);
            GL11.glTexSubImage2D(3553, 0, 0, 0, 128, 128, 6408, 5121, var1[2]);
            GL11.glBindTexture(3553, this.sheets);
            GL11.glTexSubImage2D(3553, 0, 0, 0, 1024, 1024, 6408, 5121, var1[3]);
            GL30.glGenerateMipmap(3553);
            GL11.glBindTexture(3553, var6);
            GL11.glBindTexture(32879, var7);
            GL15.glBindBuffer(35052, var2);
            GL11.glPixelStorei(3317, var3);
            GL11.glPixelStorei(3314, var4);
            GL11.glPixelStorei(32878, var5);
            this.read = null;
            this.uploaded = true;
            System.out.println("[Viewpoint] the clouds' noise uploaded");
         }
      }
   }

   void bind() {
      GL13.glActiveTexture(34079);
      GL11.glBindTexture(32879, this.shape);
      GL13.glActiveTexture(34080);
      GL11.glBindTexture(32879, this.detail);
      Gl.bind(97, this.curl);
      Gl.bind(98, this.sheets);
      GL13.glActiveTexture(33984);
   }

   private static void fill(int var0, int var1, ByteBuffer var2) {
      GL11.glBindTexture(32879, var0);
      GL12.glTexSubImage3D(32879, 0, 0, 0, 0, var1, var1, var1, 6408, 5121, var2);
      GL30.glGenerateMipmap(32879);
   }

   private static int sheetsTexture() {
      int var0 = Gl.texture(32856, 1024, 1024, 6408, 5121, 9729);
      GL11.glTexParameteri(3553, 10241, 9987);
      GL11.glTexParameteri(3553, 10242, 10497);
      GL11.glTexParameteri(3553, 10243, 10497);
      GL30.glGenerateMipmap(3553);
      if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
         float var1 = GL11.glGetFloat(34047);
         GL11.glTexParameterf(3553, 34046, Math.min(16.0F, var1));
      }

      return var0;
   }

   private static int volume(int var0) {
      int var1 = GL11.glGenTextures();
      GL11.glBindTexture(32879, var1);
      GL12.glTexImage3D(32879, 0, 32856, var0, var0, var0, 0, 6408, 5121, (ByteBuffer)null);
      GL11.glTexParameteri(32879, 10241, 9987);
      GL11.glTexParameteri(32879, 10240, 9729);
      GL11.glTexParameteri(32879, 10242, 10497);
      GL11.glTexParameteri(32879, 10243, 10497);
      GL11.glTexParameteri(32879, 32882, 10497);
      return var1;
   }

   private void readAll() {
      try {
         ByteBuffer[] var1 = new ByteBuffer[]{
            rgba("/viewpoint/clouds/shape.png", 128, 16384),
            rgba("/viewpoint/clouds/detail.png", 32, 1024),
            rgba("/viewpoint/clouds/curl.png", 128, 128),
            rgba("/viewpoint/clouds/sheets.png", 1024, 1024)
         };
         this.read = var1;
      } catch (Exception var2) {
         this.failed = var2;
      }
   }

   private static ByteBuffer rgba(String var0, int var1, int var2) throws Exception {
      BufferedImage var3;
      try (InputStream var4 = CloudNoise.class.getResourceAsStream(var0)) {
         if (var4 == null) {
            throw new IllegalStateException("no " + var0 + " in the JAR");
         }

         var3 = ImageIO.read(var4);
      }

      if (var3.getWidth() == var1 && var3.getHeight() == var2) {
         int[] var12 = var3.getRGB(0, 0, var1, var2, null, 0, var1);
         ByteBuffer var5 = BufferUtils.createByteBuffer(var1 * var2 * 4);

         for (int var9 : var12) {
            var5.put((byte)(var9 >> 16)).put((byte)(var9 >> 8)).put((byte)var9).put((byte)(var9 >>> 24));
         }

         return var5.flip();
      } else {
         throw new IllegalStateException(var0 + " is " + var3.getWidth() + "x" + var3.getHeight() + ", not " + var1 + "x" + var2);
      }
   }
}
