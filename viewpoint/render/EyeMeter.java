package viewpoint.render;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import viewpoint.platform.Gl;
import viewpoint.platform.Profile;
import viewpoint.platform.Tuning;

final class EyeMeter {
   static final int WIDTH = 128;
   static final int HEIGHT = 72;
   private static final float LONGEST_FRAME = 0.25F;
   private static final EyeAdaptation adaptation = new EyeAdaptation();
   private static final FloatBuffer samples = BufferUtils.createFloatBuffer(18432);
   private static int texture;
   private static int fbo;
   private static int buffer;
   private static long fence;
   private static float mean = Float.NaN;
   private static float correction;
   private static float key;
   private static float window;
   static boolean held;

   static float exposure(FrameContext var0) {
      return var0.pipeline.eye ? Tuning.exposure * (float)Math.pow(2.0, correction) : Tuning.exposure;
   }

   static void collect() {
      if (fence != 0L) {
         int var0 = GL32.glClientWaitSync(fence, 0, 0L);
         if (var0 == 37146 || var0 == 37148) {
            GL32.glDeleteSync(fence);
            fence = 0L;
            GL15.glBindBuffer(35051, buffer);
            samples.clear();
            GL15.glGetBufferSubData(35051, 0L, samples);
            GL15.glBindBuffer(35051, 0);
            mean = adaptation.mean(samples, 9216);
         }
      }
   }

   static boolean begin() {
      if (texture == 0) {
         make();
      }

      if (fence != 0L) {
         return false;
      }

      GL30.glBindFramebuffer(36160, fbo);
      GL11.glViewport(0, 0, 128, 72);
      return true;
   }

   static void read() {
      GL15.glBindBuffer(35051, buffer);
      GL11.glReadPixels(0, 0, 128, 72, 33319, 5126, 0L);
      GL15.glBindBuffer(35051, 0);
      fence = GL32.glFenceSync(37143, 0);
   }

   static void adapt(FrameContext var0) {
      SceneData var1 = var0.scene;
      key = Tuning.eyeKey + var1.eyeExpected;
      window = var1.eyeWindow;
      if (!held && !Float.isNaN(mean)) {
         float var2 = EyeAdaptation.aim(mean, key, window);
         float var3 = Math.min(var0.frameSeconds, 0.25F);
         correction = EyeAdaptation.step(correction, var2, var3, Tuning.eyeOpening, Tuning.eyeClosing);
         correction = Math.max(-window, Math.min(window, correction));
      } else {
         correction = 0.0F;
      }
   }

   static float mean() {
      return mean;
   }

   static void off() {
      correction = 0.0F;
      mean = Float.NaN;
   }

   private static void make() {
      texture = Gl.texture(33328, 128, 72, 33319, 5126, 9728);
      GL11.glBindTexture(3553, 0);
      fbo = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, fbo);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, texture, 0);
      int var0 = GL30.glCheckFramebufferStatus(36160);
      if (var0 != 36053) {
         throw new IllegalStateException("the eye's meter target is incomplete: 0x" + Integer.toHexString(var0));
      }

      buffer = GL15.glGenBuffers();
      GL15.glBindBuffer(35051, buffer);
      GL15.glBufferData(35051, 73728L, 35041);
      GL15.glBindBuffer(35051, 0);
   }

   private static String report() {
      return Float.isNaN(mean) ? "off" : String.format("mean %.2f key %.2f window %.2f correction %+.2f", mean, key, window, correction);
   }

   private EyeMeter() {
   }

   static {
      Profile.eyeReport = EyeMeter::report;
   }
}
