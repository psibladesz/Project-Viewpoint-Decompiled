package viewpoint.render;

import java.nio.IntBuffer;
import java.util.Arrays;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Tuning;

final class TargetOutline {
   private static final String SECTION = "Controls/Loot menu";
   private static final LiveSettings.Number RED = colour("loot.outlineRed", "Outline red", 255.0F);
   private static final LiveSettings.Number GREEN = colour("loot.outlineGreen", "Outline green", 214.0F);
   private static final LiveSettings.Number BLUE = colour("loot.outlineBlue", "Outline blue", 120.0F);
   private static final LiveSettings.Number OPACITY = LiveSettings.number(
      "loot.outlineOpacity", "Outline opacity", "Controls/Loot menu", 0.0F, 1.0F, 0.05F, 0.8F
   );
   private static final LiveSettings.Number WIDTH = LiveSettings.number(
      "loot.outlineWidth", "Outline width (pixels at 1080p)", "Controls/Loot menu", 1.0F, 24.0F, 1.0F, 6.0F
   );
   private static final int MAX_STEPS = 32;
   private static final float WIDTH_AT = 1080.0F;
   private final GlProgram mask = GlProgram.create("target mask", "shadow.vert", "target_mask.frag");
   private final GlProgram blur = GlProgram.create("target blur", "screen.vert", "target_blur.frag");
   private final GlProgram outline = GlProgram.create("target outline", "screen.vert", "target_outline.frag");
   private int width;
   private int height;
   private int maskTexture;
   private int maskFbo;
   private final int[] blurTextures = new int[2];
   private final int[] blurFbos = new int[2];
   private int[] segments = new int[24];
   private boolean masked;

   TargetOutline() {
      this.mask.sampler("uTexture", 0);
      this.mask.sampler("uPlantSlots", 48);
      this.blur.sampler("uMask", 0);
      this.outline.sampler("uGlow", 0);
      this.outline.sampler("uMask", 51);
   }

   private static LiveSettings.Number colour(String var0, String var1, float var2) {
      return LiveSettings.number(var0, var1, "Controls/Loot menu", 0.0F, 255.0F, 1.0F, var2);
   }

   void mask(FrameContext var1, ModelPass var2) {
      SceneData var3 = var1.scene;
      this.masked = false;
      if (!(OPACITY.get() <= 0.0F) && (var3.targetMesh != null || var3.models.targeted)) {
         this.ensure(var1.width, var1.height);
         GL30.glBindFramebuffer(36160, this.maskFbo);
         GL11.glViewport(0, 0, this.width, this.height);
         Gl.resetState();
         GL11.glDisable(2929);
         GL11.glDepthMask(false);
         GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         GL11.glClear(16384);
         this.world(var1);
         var2.targets(var1);
         Gl.resetState();
         this.masked = true;
      }
   }

   void composite(FrameContext var1) {
      if (this.masked) {
         this.masked = false;
         float var2 = Math.min(32.0F, WIDTH.get() * this.height / 1080.0F * 0.5F);
         int var3 = Math.max(1, this.width / 2);
         int var4 = Math.max(1, this.height / 2);
         Gl.resetState();
         GL11.glDisable(2929);
         GL11.glDepthMask(false);
         this.blur.use();
         this.blur.set("uRadius", var2);
         GL11.glViewport(0, 0, var3, var4);
         this.blurPass(this.blurFbos[0], this.maskTexture, 2.0F / this.width, 0.0F);
         this.blurPass(this.blurFbos[1], this.blurTextures[0], 0.0F, 1.0F / var4);
         GL30.glBindFramebuffer(36009, var1.outputDrawFbo);
         GL30.glBindFramebuffer(36008, var1.outputReadFbo);
         GL11.glViewport(var1.viewport[0], var1.viewport[1], var1.viewport[2], var1.viewport[3]);
         GL11.glEnable(3042);
         GL14.glBlendFuncSeparate(770, 771, 0, 1);
         this.outline.use();
         this.outline.set("uColour", RED.get() / 255.0F, GREEN.get() / 255.0F, BLUE.get() / 255.0F);
         this.outline.set("uOpacity", OPACITY.get());
         this.outline.set("uEdge", Tuning.targetOutlineEdge);
         var1.panini.uniforms(this.outline);
         Gl.bind(51, this.maskTexture);
         Gl.bind(0, this.blurTextures[1]);
         Gl.screenTriangle();
         Gl.bind(51, 0);
         Gl.bind(0, 0);
         GL11.glBlendFunc(770, 771);
         GL11.glDisable(3042);
      }
   }

   private void blurPass(int var1, int var2, float var3, float var4) {
      GL30.glBindFramebuffer(36160, var1);
      this.blur.set("uStep", var3, var4);
      Gl.bind(0, var2);
      Gl.screenTriangle();
   }

   private void world(FrameContext var1) {
      SceneData var2 = var1.scene;
      ChunkMeshData var3 = var2.targetMesh;
      int var4 = record(var2, var3);
      if (var4 >= 0 && var3.arenaFirst >= 0) {
         if (var2.targetSquare >= 0) {
            this.mask.use();
            this.mask.set("uViewProjection", var1.plainViewProjection);
            if (var3.hasModels()) {
               PackDraws.drawTarget(var2, this.mask, var4, 1L << var2.targetSquare);
            }

            CorpseCards.drawTarget(var2, this.mask, var4, 1L << var2.targetSquare);
         }

         if (var3.runs != null) {
            byte var5 = 0;

            for (int var6 = 0; var6 < var3.runs.length; var6 += 3) {
               var5 += var3.runs[var6] == var2.targetOwner ? 1 : 0;
            }

            if (var5 != 0) {
               int var8 = this.commands(var3, var2.targetOwner, var4, MeshArena.commands(var5));
               MeshArena.beginDraws();
               this.mask.use();
               this.mask.set("uViewProjection", var1.plainViewProjection);

               for (int var7 = 0; var7 < var8; var7++) {
                  Meshes.bindPage(var3.pages[this.segments[var7 * 3]]);
                  MeshArena.multiDraw(this.segments[var7 * 3 + 1], this.segments[var7 * 3 + 2]);
               }

               MeshArena.endDraws();
               GL33.glBindSampler(0, 0);
            }
         }
      }
   }

   private int commands(ChunkMeshData var1, int var2, int var3, IntBuffer var4) {
      int var5 = 0;

      for (int var6 = 0; var6 < var1.pages.length; var6++) {
         int var7 = var4.position() / 4;

         for (int var8 = var1.pageRuns[var6]; var8 < var1.pageRuns[var6 + 1]; var8++) {
            if (var1.runs[var8 * 3] == var2) {
               var4.put(var1.runs[var8 * 3 + 2]).put(1).put(var1.arenaFirst + var1.runs[var8 * 3 + 1]);
               var4.put(var3);
            }
         }

         int var9 = var4.position() / 4 - var7;
         if (var9 > 0 && var1.pages[var6] != null) {
            if (this.segments.length < (var5 + 1) * 3) {
               this.segments = Arrays.copyOf(this.segments, this.segments.length * 2);
            }

            this.segments[var5 * 3] = var6;
            this.segments[var5 * 3 + 1] = var7;
            this.segments[var5 * 3 + 2] = var9;
            var5++;
         }
      }

      return var5;
   }

   private static int record(SceneData var0, ChunkMeshData var1) {
      for (int var2 = 0; var2 < var0.meshCount; var2++) {
         if (var0.meshes[var2] == var1) {
            return var2;
         }
      }

      return -1;
   }

   private void ensure(int var1, int var2) {
      if (this.maskFbo == 0 || var1 != this.width || var2 != this.height) {
         this.release();
         this.width = var1;
         this.height = var2;
         this.maskTexture = Gl.texture(33321, var1, var2, 6403, 5121, 9729);
         this.maskFbo = fbo(this.maskTexture);

         for (int var3 = 0; var3 < 2; var3++) {
            this.blurTextures[var3] = Gl.texture(33321, Math.max(1, var1 / 2), Math.max(1, var2 / 2), 6403, 5121, 9729);
            this.blurFbos[var3] = fbo(this.blurTextures[var3]);
         }

         GL11.glBindTexture(3553, 0);
      }
   }

   private static int fbo(int var0) {
      int var1 = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, var1);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, var0, 0);
      return var1;
   }

   private void release() {
      if (this.maskFbo != 0) {
         GL30.glDeleteFramebuffers(this.maskFbo);
         GL11.glDeleteTextures(this.maskTexture);

         for (int var1 = 0; var1 < 2; var1++) {
            GL30.glDeleteFramebuffers(this.blurFbos[var1]);
            GL11.glDeleteTextures(this.blurTextures[var1]);
         }

         this.maskFbo = 0;
      }
   }
}
