package viewpoint.render;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;

final class StarField {
   static final int MOON_WIDTH = 512;
   static final int MOON_HEIGHT = 256;
   private static final String STARS_FILE = "/viewpoint/sky/stars.csv";
   private static final String MOON_FILE = "/viewpoint/sky/moon.png";
   private volatile FloatBuffer starTexels;
   private volatile ByteBuffer moonTexels;
   private volatile Exception failed;
   final int stars;
   final int moon;
   private final Thread reader;
   private boolean uploaded;

   StarField() {
      int var1 = GL11.glGetInteger(32873);
      this.stars = Gl.texture(34836, 512, 192, 6408, 5126, 9728);
      this.moon = Gl.texture(32856, 512, 256, 6408, 5121, 9729);
      GL11.glTexParameteri(3553, 10241, 9987);
      GL11.glTexParameteri(3553, 10242, 10497);
      GL30.glGenerateMipmap(3553);
      GL11.glBindTexture(3553, var1);
      this.reader = new Thread(this::readAll, "Viewpoint night sky");
      this.reader.setDaemon(true);
      this.reader.start();
   }

   void update() {
      if (!this.uploaded) {
         if (CloudNoise.waits) {
            try {
               this.reader.join();
            } catch (InterruptedException var7) {
               Thread.currentThread().interrupt();
               throw new IllegalStateException("interrupted waiting for the night sky", var7);
            }
         }

         if (this.failed != null) {
            throw new IllegalStateException("the night sky could not be read", this.failed);
         }

         FloatBuffer var1 = this.starTexels;
         ByteBuffer var2 = this.moonTexels;
         if (var1 != null && var2 != null) {
            int var3 = GL11.glGetInteger(35055);
            int var4 = GL11.glGetInteger(3317);
            int var5 = GL11.glGetInteger(3314);
            int var6 = GL11.glGetInteger(32873);
            GL15.glBindBuffer(35052, 0);
            GL11.glPixelStorei(3317, 4);
            GL11.glPixelStorei(3314, 0);
            GL11.glBindTexture(3553, this.stars);
            GL11.glTexSubImage2D(3553, 0, 0, 0, 512, 192, 6408, 5126, var1);
            GL11.glBindTexture(3553, this.moon);
            GL11.glTexSubImage2D(3553, 0, 0, 0, 512, 256, 6408, 5121, var2);
            GL30.glGenerateMipmap(3553);
            GL11.glBindTexture(3553, var6);
            GL15.glBindBuffer(35052, var3);
            GL11.glPixelStorei(3317, var4);
            GL11.glPixelStorei(3314, var5);
            this.starTexels = null;
            this.moonTexels = null;
            this.uploaded = true;
            System.out.println("[Viewpoint] the night sky uploaded");
         }
      }
   }

   void bind() {
      Gl.bind(99, this.stars);
      Gl.bind(100, this.moon);
      GL13.glActiveTexture(33984);
   }

   private void readAll() {
      try {
         float[] var1 = StarCells.image(readStars());
         FloatBuffer var2 = BufferUtils.createFloatBuffer(var1.length).put(var1);
         var2.flip();
         ByteBuffer var3 = readMoon();
         this.moonTexels = var3;
         this.starTexels = var2;
      } catch (Exception var4) {
         this.failed = var4;
      }
   }

   private static List<StarCells.Star> readStars() throws Exception {
      ArrayList var0 = new ArrayList();

      try (InputStream var1 = open("/viewpoint/sky/stars.csv")) {
         BufferedReader var2 = new BufferedReader(new InputStreamReader(var1, StandardCharsets.UTF_8));

         for (String var3 = var2.readLine(); var3 != null; var3 = var2.readLine()) {
            if (!var3.isBlank() && !var3.startsWith("#")) {
               String[] var4 = var3.split(",");
               var0.add(StarCells.Star.at(Double.parseDouble(var4[0]), Double.parseDouble(var4[1]), Double.parseDouble(var4[2]), Double.parseDouble(var4[3])));
            }
         }
      }

      return var0;
   }

   private static ByteBuffer readMoon() throws Exception {
      BufferedImage var0;
      try (InputStream var1 = open("/viewpoint/sky/moon.png")) {
         var0 = ImageIO.read(var1);
      }

      if (var0.getWidth() == 512 && var0.getHeight() == 256) {
         int[] var9 = var0.getRGB(0, 0, 512, 256, null, 0, 512);
         ByteBuffer var2 = BufferUtils.createByteBuffer(var9.length * 4);

         for (int var6 : var9) {
            var2.put((byte)(var6 >> 16)).put((byte)(var6 >> 8)).put((byte)var6).put((byte)-1);
         }

         return var2.flip();
      } else {
         throw new IllegalStateException("/viewpoint/sky/moon.png is " + var0.getWidth() + "x" + var0.getHeight());
      }
   }

   private static InputStream open(String var0) {
      InputStream var1 = StarField.class.getResourceAsStream(var0);
      if (var1 == null) {
         throw new IllegalStateException("no " + var0 + " in the JAR");
      } else {
         return var1;
      }
   }
}
