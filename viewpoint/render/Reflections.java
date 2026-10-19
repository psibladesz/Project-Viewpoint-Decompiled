package viewpoint.render;

import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.Tuning;

final class Reflections {
   private static final int LEVELS = 8;
   private final GlProgram depth = GlProgram.create("reflection depth", "screen.vert", "reflection_depth.frag");
   private final GlProgram trace = GlProgram.create("reflection trace", "screen.vert", "reflection_trace.frag");
   private final Targets targets;
   private final int[] levelFbo = new int[8];
   private int width;
   private int height;
   private int sceneLevels;
   private int closest;
   private int scene;
   private int result;
   private int sceneFbo;
   private int resultFbo;
   private int lastTargets = -1;
   private float farNearThen;
   private float farFarThen;

   Reflections(Targets var1) {
      this.targets = var1;
      this.depth.sampler("uSource", 0);
      this.depth.sampler("uFarDepth", 29);
      this.trace.sampler("uClosest", 120);
      this.trace.sampler("uScene", 121);
      this.trace.sampler("uAlbedo", 9);
      this.trace.sampler("uNormal", 10);
      this.trace.sampler("uEngineLight", 11);
      this.trace.sampler("uDepth", 12);
   }

   void draw(FrameContext var1) {
      if (!var1.pipeline.reflections) {
         Gl.bind(119, 0);
         Gl.bind(120, 0);
         this.lastTargets = -1;
      } else {
         GlDebug.push("reflections");
         GpuParts.begin(29);
         int var2 = var1.pipeline.reflectionScale;
         this.fit(Math.max(1, var1.width / var2), Math.max(1, var1.height / var2));
         boolean var3 = this.lastTargets == this.targets.generation && var1.previousValid;
         if (var3) {
            GpuParts.begin(31);
            this.lastFrame(var1);
            GpuParts.end(31);
         }

         this.lastTargets = this.targets.generation;
         GpuParts.begin(32);
         this.closestLevels(var1, var3);
         GpuParts.end(32);
         GpuParts.begin(33);
         this.traced(var1, var3);
         GpuParts.end(33);
         this.farNearThen = var1.farNear;
         this.farFarThen = var1.farFar;
         GpuParts.end(29);
         GlDebug.pop();
         Gl.bind(119, this.result);
         Gl.bind(120, this.closest);
         GL13.glActiveTexture(33984);
      }
   }

   private void lastFrame(FrameContext var1) {
      GL30.glBindFramebuffer(36008, this.targets.hdrFbo);
      GL30.glBindFramebuffer(36009, this.sceneFbo);
      GL30.glBlitFramebuffer(0, 0, var1.width, var1.height, 0, 0, this.width, this.height, 16384, 9729);
      GL30.glBindFramebuffer(36160, this.sceneFbo);
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, this.scene);
      GL30.glGenerateMipmap(3553);
      GL11.glBindTexture(3553, 0);
   }

   private void closestLevels(FrameContext var1, boolean var2) {
      this.depth.use();
      var1.camera(this.depth);
      this.depth.set("uPrevious", var1.reprojection);
      this.depth.set("uFarThen", var2 ? this.farNearThen : 0.0F, var2 ? this.farFarThen : 0.0F);
      this.depth.setInt("uScale", var1.pipeline.reflectionScale);
      Gl.bind(29, this.targets.farDepth);
      GL13.glActiveTexture(33984);

      for (int var3 = 0; var3 < 8; var3++) {
         GL30.glBindFramebuffer(36160, this.levelFbo[var3]);
         GL11.glViewport(0, 0, Math.max(1, this.width >> var3), Math.max(1, this.height >> var3));
         this.depth.setInt("uFirst", var3 == 0 ? 1 : 0);
         if (var3 == 0) {
            GL11.glBindTexture(3553, this.targets.depth);
         } else {
            GL11.glBindTexture(3553, this.closest);
            GL11.glTexParameteri(3553, 33084, var3 - 1);
            GL11.glTexParameteri(3553, 33085, var3 - 1);
         }

         Gl.screenTriangle();
      }

      GL11.glTexParameteri(3553, 33084, 0);
      GL11.glTexParameteri(3553, 33085, 7);
      GL11.glBindTexture(3553, 0);
      Gl.bind(29, 0);
      GL13.glActiveTexture(33984);
   }

   private void traced(FrameContext var1, boolean var2) {
      GL30.glBindFramebuffer(36160, this.resultFbo);
      GL11.glViewport(0, 0, this.width, this.height);
      this.trace.use();
      var1.camera(this.trace);
      this.trace.set("uViewProjection", var1.viewProjection);
      this.trace.set("uPrevious", var1.reprojection);
      this.trace.set("uSize", this.width, this.height);
      this.trace.setInt("uLevels", 8);
      this.trace.setInt("uSteps", var1.pipeline.reflectionSteps);
      this.trace.setInt("uScale", var1.pipeline.reflectionScale);
      this.trace.set("uSceneLevels", this.sceneLevels);
      this.trace.set("uHistory", var2 ? 1.0F : 0.0F);
      this.trace.set("uTolerance", Tuning.reflectionTolerance);
      Gl.bind(120, this.closest);
      Gl.bind(121, this.scene);
      Gl.bind(9, this.targets.albedo);
      Gl.bind(10, this.targets.normal);
      Gl.bind(11, this.targets.engineLight);
      Gl.bind(12, this.targets.depth);
      Gl.screenTriangle();
      Gl.bind(121, 0);
      Gl.bind(9, 0);
      Gl.bind(10, 0);
      Gl.bind(11, 0);
      Gl.bind(12, 0);
   }

   private void fit(int var1, int var2) {
      if (var1 != this.width || var2 != this.height) {
         this.release();
         this.width = var1;
         this.height = var2;
         this.closest = this.levelled(33326, 6403, 8, 9984, 9728);

         for (int var3 = 0; var3 < 8; var3++) {
            this.levelFbo[var3] = framebuffer(this.closest, var3);
         }

         this.sceneLevels = 32 - Integer.numberOfLeadingZeros(Math.max(var1, var2));
         this.scene = this.levelled(34842, 6408, this.sceneLevels, 9987, 9729);
         this.sceneFbo = framebuffer(this.scene, 0);
         this.result = Gl.texture(34842, var1, var2, 6408, 5126, 9728);
         this.resultFbo = framebuffer(this.result, 0);
         GL11.glBindTexture(3553, 0);
         this.lastTargets = -1;
      }
   }

   private int levelled(int var1, int var2, int var3, int var4, int var5) {
      int var6 = GL11.glGenTextures();
      GL11.glBindTexture(3553, var6);

      for (int var7 = 0; var7 < var3; var7++) {
         GL11.glTexImage2D(3553, var7, var1, Math.max(1, this.width >> var7), Math.max(1, this.height >> var7), 0, var2, 5126, (ByteBuffer)null);
      }

      GL11.glTexParameteri(3553, 10241, var4);
      GL11.glTexParameteri(3553, 10240, var5);
      GL11.glTexParameteri(3553, 10242, 33071);
      GL11.glTexParameteri(3553, 10243, 33071);
      GL11.glTexParameteri(3553, 33085, var3 - 1);
      return var6;
   }

   private static int framebuffer(int var0, int var1) {
      int var2 = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, var2);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, var0, var1);
      int var3 = GL30.glCheckFramebufferStatus(36160);
      if (var3 != 36053) {
         throw new IllegalStateException("the reflections' target at level " + var1 + " is incomplete: 0x" + Integer.toHexString(var3));
      } else {
         return var2;
      }
   }

   private void release() {
      if (this.width != 0) {
         GL11.glDeleteTextures(new int[]{this.closest, this.scene, this.result});
         GL30.glDeleteFramebuffers(this.levelFbo);
         GL30.glDeleteFramebuffers(new int[]{this.sceneFbo, this.resultFbo});
      }
   }
}
