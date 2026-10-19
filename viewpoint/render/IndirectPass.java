package viewpoint.render;

import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.Tuning;
import zombie.core.textures.Texture;

final class IndirectPass {
   private static final float RADIUS_MULTIPLIER = 1.457F;
   private static final int LEVELS = 5;
   private final GlProgram depth = GlProgram.create("gtao depth", "screen.vert", "gtao_depth.frag");
   private final GlProgram main = GlProgram.create("gtao", "screen.vert", "gtao.frag");
   private final GlProgram denoise = GlProgram.create("gtao denoise", "screen.vert", "gtao_denoise.frag");
   private final Targets targets;
   private final BounceField bounce;
   private final FlashBounce flash;
   private final Reflections reflections;
   private final WaveTile waves = new WaveTile();
   private int width;
   private int height;
   private int viewDepth;
   private int term;
   private int edges;
   private int occlusion;
   private final int[] levelFbo = new int[5];
   private int termFbo;
   private int occlusionFbo;
   private boolean cleared;

   IndirectPass(Targets var1, ShadowPass var2, FloorBakes var3) {
      this.targets = var1;
      this.bounce = new BounceField(var1, var2, var3);
      this.flash = new FlashBounce(var1, var2);
      this.reflections = new Reflections(var1);
      this.depth.sampler("uSource", 0);
      this.depth.sampler("uAlbedo", 9);
      this.main.sampler("uViewDepth", 0);
      this.main.sampler("uNormal", 10);
      this.denoise.sampler("uTerm", 0);
      this.denoise.sampler("uEdges", 106);
   }

   void draw(FrameContext var1, boolean var2, boolean var3) {
      this.fit(var1.width, var1.height);
      GL11.glDisable(2929);
      GL11.glDisable(3042);
      GL11.glDepthMask(false);
      Gl.bind(105, 0);
      if (var2) {
         GpuParts.begin(41);
         this.levels(var1);
         this.term(var1);
         this.denoised(var1);
         GpuParts.end(41);
         this.cleared = false;
      } else if (!this.cleared) {
         GL30.glBindFramebuffer(36160, this.occlusionFbo);
         GL11.glClearColor(0.5F, 0.5F, 0.5F, 1.0F);
         GL11.glClear(16384);
         this.cleared = true;
      }

      Gl.bind(105, this.occlusion);
      this.waves.draw(var1);
      this.reflections.draw(var1);
      this.bounce.draw(var1, var3);
      this.flash.draw(var1, var3);
      GL11.glViewport(0, 0, var1.width, var1.height);
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, 0);
      Texture.lastTextureID = -1;
      GL11.glDepthMask(true);
      GL11.glEnable(2929);
   }

   private void levels(FrameContext var1) {
      this.depth.use();
      var1.camera(this.depth);
      this.depth.set("uRadius", radius());
      Gl.bind(9, this.targets.albedo);
      GL13.glActiveTexture(33984);

      for (int var2 = 0; var2 < 5; var2++) {
         GL30.glBindFramebuffer(36160, this.levelFbo[var2]);
         GL11.glViewport(0, 0, Math.max(1, this.width >> var2), Math.max(1, this.height >> var2));
         this.depth.setInt("uLevel", var2);
         if (var2 == 0) {
            GL11.glBindTexture(3553, this.targets.depth);
         } else {
            GL11.glBindTexture(3553, this.viewDepth);
            GL11.glTexParameteri(3553, 33084, var2 - 1);
            GL11.glTexParameteri(3553, 33085, var2 - 1);
         }

         Gl.screenTriangle();
      }

      GL11.glTexParameteri(3553, 33084, 0);
      GL11.glTexParameteri(3553, 33085, 4);
      Gl.bind(9, 0);
   }

   private void term(FrameContext var1) {
      GL30.glBindFramebuffer(36160, this.termFbo);
      GL11.glViewport(0, 0, this.width, this.height);
      this.main.use();
      this.main.set("uViewRot", var1.viewRotation);
      this.main.set("uTanHalf", 1.0F / var1.scene.projection.m00(), 1.0F / var1.scene.projection.m11());
      this.main.set("uPixel", 1.0F / this.width, 1.0F / this.height);
      this.main.set("uRadius", radius());
      this.main.setInt("uNoiseIndex", var1.noiseIndex());
      Gl.bind(10, this.targets.normal);
      Gl.bind(0, this.viewDepth);
      Gl.screenTriangle();
      Gl.bind(10, 0);
   }

   private void denoised(FrameContext var1) {
      GL30.glBindFramebuffer(36160, this.occlusionFbo);
      this.denoise.use();
      this.denoise.set("uViewRot", var1.viewRotation);
      Gl.bind(106, this.edges);
      Gl.bind(0, this.term);
      Gl.screenTriangle();
      Gl.bind(106, 0);
   }

   BounceField bounce() {
      return this.bounce;
   }

   WaveTile waves() {
      return this.waves;
   }

   private static float radius() {
      return Tuning.occlusionRadius * 1.457F;
   }

   private void fit(int var1, int var2) {
      if (var1 != this.width || var2 != this.height) {
         this.release();
         this.width = var1;
         this.height = var2;
         this.viewDepth = GL11.glGenTextures();
         GL11.glBindTexture(3553, this.viewDepth);

         for (int var3 = 0; var3 < 5; var3++) {
            GL11.glTexImage2D(3553, var3, 33326, Math.max(1, var1 >> var3), Math.max(1, var2 >> var3), 0, 6403, 5126, (ByteBuffer)null);
         }

         GL11.glTexParameteri(3553, 10241, 9984);
         GL11.glTexParameteri(3553, 10240, 9728);
         GL11.glTexParameteri(3553, 10242, 33071);
         GL11.glTexParameteri(3553, 10243, 33071);
         GL11.glTexParameteri(3553, 33085, 4);

         for (int var4 = 0; var4 < 5; var4++) {
            this.levelFbo[var4] = framebuffer(this.viewDepth, var4);
         }

         this.term = Gl.texture(32856, var1, var2, 6408, 5121, 9728);
         this.edges = Gl.texture(33321, var1, var2, 6403, 5121, 9728);
         this.termFbo = framebuffer(this.term, 0);
         GL30.glFramebufferTexture2D(36160, 36065, 3553, this.edges, 0);
         GL20.glDrawBuffers(new int[]{36064, 36065});
         complete("term");
         this.occlusion = Gl.texture(32856, var1, var2, 6408, 5121, 9728);
         this.occlusionFbo = framebuffer(this.occlusion, 0);
         this.targets.occlusion = this.occlusion;
         GL11.glBindTexture(3553, 0);
         this.cleared = false;
      }
   }

   private static int framebuffer(int var0, int var1) {
      int var2 = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, var2);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, var0, var1);
      complete("level " + var1);
      return var2;
   }

   private static void complete(String var0) {
      int var1 = GL30.glCheckFramebufferStatus(36160);
      if (var1 != 36053) {
         throw new IllegalStateException("the occlusion's " + var0 + " target is incomplete: 0x" + Integer.toHexString(var1));
      }
   }

   private void release() {
      if (this.width != 0) {
         GL11.glDeleteTextures(new int[]{this.viewDepth, this.term, this.edges, this.occlusion});
         GL30.glDeleteFramebuffers(this.levelFbo);
         GL30.glDeleteFramebuffers(new int[]{this.termFbo, this.occlusionFbo});
      }
   }
}
