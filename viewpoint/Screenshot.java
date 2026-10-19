package viewpoint;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import zombie.ZomboidFileSystem;

final class Screenshot {
   static volatile boolean requested;

   static void captureIfRequested() {
      if (requested) {
         requested = false;
         int[] var0 = new int[2];
         ByteBuffer var1 = read(var0);
         Thread var2 = new Thread(() -> write(var1, var0[0], var0[1]), "Viewpoint screenshot");
         var2.setDaemon(true);
         var2.start();
      }
   }

   static ByteBuffer read(int[] var0) {
      int[] var1 = new int[4];
      GL11.glGetIntegerv(2978, var1);
      var0[0] = var1[2];
      var0[1] = var1[3];
      ByteBuffer var2 = BufferUtils.createByteBuffer(var0[0] * var0[1] * 4);
      GL11.glPixelStorei(3333, 1);
      GL11.glReadPixels(var1[0], var1[1], var0[0], var0[1], 6408, 5121, var2);
      return var2;
   }

   static BufferedImage image(ByteBuffer var0, int var1, int var2) {
      BufferedImage var3 = new BufferedImage(var1, var2, 1);

      for (int var4 = 0; var4 < var2; var4++) {
         for (int var5 = 0; var5 < var1; var5++) {
            int var6 = (var4 * var1 + var5) * 4;
            int var7 = (var0.get(var6) & 255) << 16 | (var0.get(var6 + 1) & 255) << 8 | var0.get(var6 + 2) & 255;
            var3.setRGB(var5, var2 - 1 - var4, var7);
         }
      }

      return var3;
   }

   static File folder() {
      File var0 = new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-shots");
      var0.mkdirs();
      return var0;
   }

   private static void write(ByteBuffer var0, int var1, int var2) {
      try {
         File var3 = new File(folder(), "shot-" + System.currentTimeMillis() + ".png");
         ImageIO.write(image(var0, var1, var2), "png", var3);
         System.out.println("[Viewpoint] screenshot " + var3);
      } catch (Exception var4) {
         System.out.println("[Viewpoint] screenshot failed: " + var4);
      }
   }

   private Screenshot() {
   }
}
