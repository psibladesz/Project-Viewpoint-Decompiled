package viewpoint.platform;

import java.util.ArrayDeque;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageCallback;

public final class GlDebug {
   private static final int MAX_LOGGED = 20;
   private static GLDebugMessageCallback callback;
   private static boolean groups;
   private static boolean listening;
   private static int logged;
   private static final ArrayDeque<String> labels = new ArrayDeque<>();
   public static volatile int errors;

   public static void install() {
      GLCapabilities var0 = GL.getCapabilities();
      if (!var0.OpenGL43 && !var0.GL_KHR_debug) {
         System.out.println("[Viewpoint] GL debug output not available on this driver");
      } else {
         groups = true;
         if (callback == null) {
            callback = GLDebugMessageCallback.create(
               (var0x, var1, var2, var3, var4, var5, var7) -> {
                  if (listening && (var1 == 33356 || var3 == 37190)) {
                     errors++;
                     if (logged++ < 20) {
                        System.out
                           .println(
                              "[Viewpoint] GL error in "
                                 + (labels.isEmpty() ? "(no pass)" : String.join("/", labels))
                                 + ": "
                                 + GLDebugMessageCallback.getMessage(var4, var5)
                           );
                     }
                  }
               }
            );
            GL43.glDebugMessageCallback(callback, 0L);
            GL11.glEnable(37600);
            GL11.glEnable(33346);
            System.out.println("[Viewpoint] GL debug output on");
         }
      }
   }

   public static void listen(boolean var0) {
      listening = var0;
   }

   public static void push(String var0) {
      labels.addLast(var0);
      if (groups) {
         GL43.glPushDebugGroup(33354, 0, var0);
      }
   }

   public static void pop() {
      if (labels.pollLast() != null && groups) {
         GL43.glPopDebugGroup();
      }
   }

   public static void popAll() {
      while (!labels.isEmpty()) {
         pop();
      }
   }

   private GlDebug() {
   }
}
