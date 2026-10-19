package viewpoint.platform;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.gl3.ImGuiImplGl3;
import imgui.internal.ImGuiContext;
import java.util.concurrent.atomic.AtomicInteger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjglx.opengl.Display;
import zombie.core.Core;

final class ImGuiFrame {
   private static ImGuiContext context;
   private static ImGuiImplGl3 backend;
   private static volatile boolean failed;
   private static double lastTime;
   private static final int[] w = new int[1];
   private static final int[] h = new int[1];
   private static final int[] fw = new int[1];
   private static final int[] fh = new int[1];
   private static final double[] cx = new double[1];
   private static final double[] cy = new double[1];
   static volatile boolean wantsMouse;
   static volatile boolean cursorHeld;
   private static final AtomicInteger wheel = new AtomicInteger();

   static void scrolled(int var0) {
      wheel.addAndGet(var0);
   }

   static boolean available() {
      return !failed && !Core.isImGui();
   }

   static void draw(ImGuiFrame.Contents var0) {
      if (!failed && !Core.isImGui()) {
         ImGuiContext var1 = ImGui.getCurrentContext();

         try {
            if (context == null) {
               create();
            } else {
               ImGui.setCurrentContext(context);
            }

            input();
            ImGui.newFrame();
            var0.draw();
            ImGui.render();
            wantsMouse = ImGui.getIO().getWantCaptureMouse();
            int var2 = GL11.glGetInteger(36006);
            GL30.glBindFramebuffer(36009, 0);
            backend.renderDrawData(ImGui.getDrawData());
            GL30.glBindFramebuffer(36009, var2);
         } catch (Throwable var6) {
            failed = true;
            wantsMouse = false;
            System.out.println("[Viewpoint] settings window off after an error: " + var6);
         } finally {
            if (var1 != null && var1.ptr != 0L) {
               ImGui.setCurrentContext(var1);
            }
         }
      } else {
         wantsMouse = false;
      }
   }

   private static void create() {
      context = ImGui.createContext();
      ImGui.setCurrentContext(context);
      ImGuiIO var0 = ImGui.getIO();
      var0.setIniFilename(null);
      backend = new ImGuiImplGl3();
      backend.init("#version 130");
      lastTime = GLFW.glfwGetTime();
      System.out.println("[Viewpoint] settings window: ImGui ready");
   }

   private static void input() {
      long var0 = Display.getWindow();
      ImGuiIO var2 = ImGui.getIO();
      GLFW.glfwGetWindowSize(var0, w, h);
      GLFW.glfwGetFramebufferSize(var0, fw, fh);
      var2.setDisplaySize(w[0], h[0]);
      if (w[0] > 0 && h[0] > 0) {
         var2.setDisplayFramebufferScale((float)fw[0] / w[0], (float)fh[0] / h[0]);
      }

      Theme.fit(h[0], backend);
      double var3 = GLFW.glfwGetTime();
      var2.setDeltaTime((float)Math.max(1.0E-4, var3 - lastTime));
      lastTime = var3;
      boolean var5 = Core.getInstance().getOptionLockCursorToWindow();
      boolean var6 = !cursorHeld && (var5 || GLFW.glfwGetInputMode(var0, 208897) != 212995);
      var2.setMouseDrawCursor(var6 && var5 && wantsMouse);
      GLFW.glfwGetCursorPos(var0, cx, cy);
      var2.setMousePos(var6 ? (float)cx[0] : -Float.MAX_VALUE, var6 ? (float)cy[0] : -Float.MAX_VALUE);

      for (int var7 = 0; var7 < 3; var7++) {
         var2.setMouseDown(var7, var6 && GLFW.glfwGetMouseButton(var0, var7) == 1);
      }

      int var8 = wheel.getAndSet(0);
      var2.setMouseWheel(var6 ? var8 : 0.0F);
   }

   private ImGuiFrame() {
   }

   interface Contents {
      void draw();
   }
}
