package viewpoint.render;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.CrashReport;
import viewpoint.platform.Graphics;
import viewpoint.upscale.FrameGeneration;
import viewpoint.upscale.NativeFiles;
import viewpoint.upscale.UpscaleFrame;

public final class FrameGenPass {
   private static FrameGenPass made;
   static int[] fixedScreen;
   private final VendorInputs inputs;
   private final int[] size = new int[2];
   private final int[] width = new int[1];
   private final int[] height = new int[1];
   private FrameGeneration generation;
   private int inputsFbo;
   private int screenFbo;
   private int hudlessFbo;
   private int generatedFbo;
   private UpscaleFrame described;
   private boolean drawn;
   private boolean copied;
   private boolean joined;
   private boolean kept;
   private boolean refused;
   private int missingAt = -1;

   FrameGenPass(VendorInputs var1) {
      this.inputs = var1;
      made = this;
   }

   public static void hudless() {
      if (made != null) {
         made.copyHudless(0);
      }
   }

   public static void presenting() {
      if (made != null) {
         made.present();
      }
   }

   public static void midframe() {
      if (made != null && made.kept) {
         made.showKept();
      }
   }

   void capture(FrameContext var1) {
      this.drawn = false;
      this.copied = false;
      if (Graphics.FRAME_GENERATION.get() && this.runs()) {
         int[] var2 = this.screenSize();
         if (var2[0] != 0 && var2[1] != 0) {
            this.fit(var2[0], var2[1], var1.width, var1.height);
            this.inputs.draw(var1, this.inputsFbo);
            this.described = this.inputs.describe(var1, !var1.previousValid);
            this.drawn = true;
            GL30.glBindFramebuffer(36009, var1.outputDrawFbo);
            GL30.glBindFramebuffer(36008, var1.outputReadFbo);
            GL11.glViewport(var1.viewport[0], var1.viewport[1], var1.viewport[2], var1.viewport[3]);
         }
      } else {
         this.release();
      }
   }

   void copyHudless(int var1) {
      if (this.drawn) {
         int var2 = GL11.glGetInteger(36010);
         int var3 = GL11.glGetInteger(36006);
         boolean var4 = unscissor();
         this.blit(var1, this.hudlessFbo);
         rescissor(var4);
         GL30.glBindFramebuffer(36008, var2);
         GL30.glBindFramebuffer(36009, var3);
         this.copied = true;
      }
   }

   boolean generate(int var1) {
      boolean var2 = this.drawn && this.copied;
      this.drawn = false;
      this.copied = false;
      if (!var2) {
         this.joined = false;
         return false;
      } else {
         this.blit(var1, this.screenFbo);
         boolean var3 = !this.joined || this.described.reset();
         this.generation.generate(this.described, var3);
         this.joined = true;
         return !var3;
      }
   }

   static FrameGenPass made() {
      return made;
   }

   int generated() {
      return this.generation.generated();
   }

   private void present() {
      this.kept = false;

      try {
         int var1 = GL11.glGetInteger(36010);
         int var2 = GL11.glGetInteger(36006);
         boolean var3 = unscissor();
         if (this.generate(0)) {
            this.blit(this.generatedFbo, 0);
            this.kept = true;
         }

         rescissor(var3);
         GL30.glBindFramebuffer(36008, var1);
         GL30.glBindFramebuffer(36009, var2);
      } catch (Throwable var4) {
         System.out.println("[Viewpoint] frame generation failed, and is off until the game restarts:");
         var4.printStackTrace(System.out);
         CrashReport.report("frame generation", var4);
         this.refused = true;
         this.release();
      }
   }

   private void showKept() {
      int var1 = GL11.glGetInteger(36010);
      int var2 = GL11.glGetInteger(36006);
      boolean var3 = unscissor();
      this.blit(this.screenFbo, 0);
      GLFW.glfwSwapBuffers(GLFW.glfwGetCurrentContext());
      this.kept = false;
      rescissor(var3);
      GL30.glBindFramebuffer(36008, var1);
      GL30.glBindFramebuffer(36009, var2);
   }

   private boolean runs() {
      if (!this.refused && this.missingAt != NativeFiles.installs()) {
         String var1 = FrameGeneration.unavailable();
         if (var1 == null) {
            this.missingAt = -1;
            return true;
         }

         if (FrameGeneration.unsupported() == null) {
            if (this.missingAt < 0) {
               System.out.println("[Viewpoint] frame generation waits for its files (" + var1 + ")");
            }

            this.missingAt = NativeFiles.installs();
            return false;
         } else {
            this.refused = true;
            System.out.println("[Viewpoint] frame generation cannot run on this machine (" + var1 + ")");
            return false;
         }
      } else {
         return false;
      }
   }

   private int[] screenSize() {
      if (fixedScreen != null) {
         return fixedScreen;
      }

      GLFW.glfwGetFramebufferSize(GLFW.glfwGetCurrentContext(), this.width, this.height);
      this.size[0] = this.width[0];
      this.size[1] = this.height[0];
      return this.size;
   }

   private void fit(int var1, int var2, int var3, int var4) {
      if (this.generation == null
         || this.generation.screenWidth != var1
         || this.generation.screenHeight != var2
         || this.generation.frameWidth != var3
         || this.generation.frameHeight != var4) {
         this.release();
         this.generation = FrameGeneration.open(var1, var2, var3, var4);
         this.inputsFbo = VendorInputs.framebuffer(this.generation.depth(), this.generation.motion(), "frame generation's inputs");
         this.screenFbo = colourFbo(this.generation.screen());
         this.hudlessFbo = colourFbo(this.generation.hudless());
         this.generatedFbo = colourFbo(this.generation.generated());
         this.kept = false;
         System.out.println("[Viewpoint] frame generation on: " + var1 + " x " + var2 + " from frames of " + var3 + " x " + var4);
      }
   }

   private void release() {
      if (this.generation != null) {
         this.generation.close();
         GL30.glDeleteFramebuffers(new int[]{this.inputsFbo, this.screenFbo, this.hudlessFbo, this.generatedFbo});
         this.generation = null;
      }

      this.joined = false;
      this.kept = false;
   }

   private void blit(int var1, int var2) {
      GL30.glBindFramebuffer(36008, var1);
      GL30.glBindFramebuffer(36009, var2);
      int var3 = this.generation.screenWidth;
      int var4 = this.generation.screenHeight;
      GL30.glBlitFramebuffer(0, 0, var3, var4, 0, 0, var3, var4, 16384, 9728);
   }

   private static boolean unscissor() {
      boolean var0 = GL11.glIsEnabled(3089);
      GL11.glDisable(3089);
      return var0;
   }

   private static void rescissor(boolean var0) {
      if (var0) {
         GL11.glEnable(3089);
      }
   }

   private static int colourFbo(int var0) {
      int var1 = GL11.glGetInteger(36006);
      int var2 = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, var2);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, var0, 0);
      int var3 = GL30.glCheckFramebufferStatus(36160);
      GL30.glBindFramebuffer(36160, var1);
      if (var3 != 36053) {
         throw new IllegalStateException("frame generation's framebuffer is incomplete: 0x" + Integer.toHexString(var3));
      } else {
         return var2;
      }
   }
}
