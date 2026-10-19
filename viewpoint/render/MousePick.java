package viewpoint.render;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

public final class MousePick {
   public static volatile MousePick.Ask ask;
   public static volatile MousePick.Hit hit;
   public static volatile MousePick.Ask aim;
   public static volatile MousePick.Hit aimHit;
   private static final FloatBuffer depth = BufferUtils.createFloatBuffer(1);
   private static final Matrix4f inverse = new Matrix4f();
   private static final Vector4f point = new Vector4f();
   private static final float[] unwarped = new float[2];
   private static int buffer;
   private static long fence;
   private static MousePick.Ask reading;
   private static boolean readingAim;
   private static float pixelX;
   private static float pixelY;
   private static float width;
   private static float height;
   private static float eyeX;
   private static float eyeY;
   private static float eyeZ;
   private static double originX;
   private static double originY;
   private static double originZ;
   private static float shiftShareX;
   private static float shiftShareY;
   private static FarRelief.Lift lift;

   public static void aimShift(float var0, float var1) {
      shiftShareX = var0;
      shiftShareY = var1;
   }

   static void read(FrameContext var0, int var1, Matrix4f var2) {
      collect();
      MousePick.Ask var3 = ask;
      MousePick.Ask var4 = aim;
      if (var3 == null) {
         hit = null;
      }

      if (var4 == null) {
         aimHit = null;
      }

      MousePick.Ask var5 = var3 != null ? var3 : var4;
      if (var5 != null) {
         if (fence == 0L && var5.screenWidth() > 0 && var5.screenHeight() > 0) {
            width = var0.width;
            height = var0.height;
            int var6 = var3 == null ? Math.round(shiftShareX * var0.width / 2.0F) : 0;
            int var7 = var3 == null ? Math.round(shiftShareY * var0.height / 2.0F) : 0;
            MousePick.Ask var8 = var3 != null ? unwarped(var5, var0.panini) : var5;
            int var9 = (int)Math.min(width - 1.0F, (float)Math.max(0L, (long)var8.x() * var0.width / var8.screenWidth() + var6));
            int var10 = (int)Math.min(height - 1.0F, (float)Math.max(0L, var0.height - 1 - (long)var8.y() * var0.height / var8.screenHeight() + var7));
            pixelX = var9 + 0.5F;
            pixelY = var10 + 0.5F;
            inverse.set(var2);
            originX = var0.scene.originX;
            originY = var0.scene.originY;
            originZ = var0.scene.originZ;
            lift = var0.scene.relief;
            eyeX = var0.eyeX;
            eyeY = var0.eyeY;
            eyeZ = var0.eyeZ;
            reading = var5;
            readingAim = var3 == null;
            if (buffer == 0) {
               buffer = GL15.glGenBuffers();
               GL15.glBindBuffer(35051, buffer);
               GL15.glBufferData(35051, 4L, 35041);
            }

            GL30.glBindFramebuffer(36008, var1);
            GL15.glBindBuffer(35051, buffer);
            GL11.glReadPixels(var9, var10, 1, 1, 6402, 5126, 0L);
            GL15.glBindBuffer(35051, 0);
            fence = GL32.glFenceSync(37143, 0);
         }
      }
   }

   private static MousePick.Ask unwarped(MousePick.Ask var0, Panini var1) {
      if (!var1.on()) {
         return var0;
      }

      float var2 = var0.screenWidth();
      float var3 = var0.screenHeight();
      var1.toFrame(2.0F * (var0.x() + 0.5F) / var2 - 1.0F, 1.0F - 2.0F * (var0.y() + 0.5F) / var3, unwarped);
      return new MousePick.Ask((int)((unwarped[0] + 1.0F) * 0.5F * var2), (int)((1.0F - unwarped[1]) * 0.5F * var3), var0.screenWidth(), var0.screenHeight());
   }

   private static void collect() {
      if (fence != 0L) {
         int var0 = GL32.glClientWaitSync(fence, 0, 0L);
         if (var0 == 37146 || var0 == 37148) {
            GL32.glDeleteSync(fence);
            fence = 0L;
            GL15.glBindBuffer(35051, buffer);
            depth.clear();
            GL15.glGetBufferSubData(35051, 0L, depth);
            GL15.glBindBuffer(35051, 0);
            float var1 = depth.get(0);
            if (var1 >= 1.0F) {
               publish(null);
            } else {
               point.set(pixelX / width * 2.0F - 1.0F, pixelY / height * 2.0F - 1.0F, var1 * 2.0F - 1.0F, 1.0F);
               inverse.transform(point);
               float var2 = point.x / point.w;
               float var3 = point.y / point.w;
               float var4 = point.z / point.w;
               float var5 = var2 - eyeX;
               float var6 = var3 - eyeY;
               float var7 = var4 - eyeZ;
               float var8 = (float)Math.sqrt(var5 * var5 + var6 * var6 + var7 * var7);
               if (!(var8 <= 0.0F)) {
                  double var9 = -(var2 + originX);
                  double var11 = -(var4 + originZ);
                  publish(
                     new MousePick.Hit(
                        reading,
                        var9,
                        var11,
                        (var3 - (lift == null ? 0.0F : lift.scene(var9, var11)) + originY) / 2.4494896F,
                        -var5 / var8,
                        -var7 / var8,
                        var6 / var8
                     )
                  );
               }
            }
         }
      }
   }

   private static void publish(MousePick.Hit var0) {
      if (readingAim) {
         aimHit = var0;
      } else {
         hit = var0;
      }
   }

   private MousePick() {
   }

   public record Ask(int x, int y, int screenWidth, int screenHeight) {
   }

   public record Hit(MousePick.Ask ask, double x, double y, double z, float dirX, float dirY, float dirUp) {
   }
}
