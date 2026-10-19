package viewpoint.render;

import java.nio.ByteBuffer;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Profile;
import viewpoint.platform.Tuning;

public final class FarPass {
   private static final float NEAR = 16.0F;
   private static final float DAYLIGHT = 0.8F;
   static final LiveSettings.Number TREES_FADE = LiveSettings.number(
      "lod.treesFadeBlocks", "Far trees fade out from (blocks)", "World/Levels of detail", 0.0F, 64.0F, 1.0F, 12.0F
   );
   static final LiveSettings.Number TREES_GONE = LiveSettings.number(
      "lod.treesGoneBlocks", "Far trees gone at (blocks)", "World/Levels of detail", 0.0F, 64.0F, 1.0F, 36.0F
   );
   private final GlProgram program = GlProgram.create("far", "far.vert", "far.frag");
   private final GlProgram shellProgram = GlProgram.create("far shell", "far_shell.vert", "far_shell.frag");
   private final GlProgram bandProgram = GlProgram.create("far shell band", "far_shell.vert", "gbuffer_shell.frag");
   private final GlProgram treeProgram = GlProgram.create("far trees", "far_tree.vert", "far_tree.frag");
   private final GlProgram prime = GlProgram.create("far prime", "screen.vert", "far_prime.frag");
   private static long commanded;
   private final Targets targets;
   private final ShadowPass shadows;
   private final FarShadow shadow;
   final TreeBaker trees;
   private final FarLamps lamps = new FarLamps();
   private final Matrix4f projection = new Matrix4f();
   private final Matrix4f viewProjection = new Matrix4f();
   private final Matrix4f cullViewProjection = new Matrix4f();
   private final FrustumIntersection frustum = new FrustumIntersection();
   private final ByteBuffer maskPixels = BufferUtils.createByteBuffer(361);
   private final ByteBuffer shellPixels = BufferUtils.createByteBuffer(8427);
   int mask;
   int shellMask;
   private boolean active;

   FarPass(Targets var1, ShadowPass var2, FarShadow var3, TreeBaker var4) {
      this.targets = var1;
      this.shadows = var2;
      this.shadow = var3;
      this.trees = var4;
      this.program.sampler("uTop", 25);
      this.program.sampler("uSide", 26);
      this.program.sampler("uMask", 27);
      this.program.sampler("uMainDepth", 12);
      this.program.sampler("uShellMask", 28);
      ShadowPass.samplers(this.program);
      ShellArena.samplers(this.shellProgram);
      this.shellProgram.sampler("uMask", 27);
      this.shellProgram.sampler("uMainDepth", 12);
      this.prime.sampler("uMainDepth", 12);
      this.shellProgram.sampler("uShellMask", 28);
      ShadowPass.samplers(this.shellProgram);
      this.shellProgram.sampler("uFloorPages", 41);
      this.shellProgram.sampler("uTreeAtlas", 42);
      this.shellProgram.sampler("uCellLight", 46);
      this.shellProgram.sampler("uCellTable", 47);
      this.program.sampler("uCellLight", 46);
      this.program.sampler("uCellTable", 47);
      ShellArena.samplers(this.bandProgram);
      this.bandProgram.sampler("uFloorPages", 41);
      this.bandProgram.sampler("uMask", 27);
      this.bandProgram.sampler("uShellMask", 28);
      this.bandProgram.sampler("uTreeAtlas", 42);
      this.bandProgram.sampler("uLight", 5);
      this.bandProgram.sampler("uBandLight", 45);
      this.bandProgram.sampler("uCellLight", 46);
      this.bandProgram.sampler("uCellTable", 47);
      this.treeProgram.sampler("uTrees", 43);
      this.treeProgram.sampler("uKinds", 44);
      this.treeProgram.sampler("uMask", 27);
      this.treeProgram.sampler("uMainDepth", 12);
      this.treeProgram.sampler("uShellMask", 28);
      ShadowPass.samplers(this.treeProgram);
   }

   void prepare(FrameContext var1, boolean var2) {
      FarGpu.uploadQueued();
      BandLight.update();
      CellLightPages.update();
      SceneData var3 = var1.scene;
      Profile.farDrawn = var3.farCount;
      Profile.farInView = 0;
      Profile.farTrees = 0;
      Profile.shellsInView = 0;
      var1.farNear = var1.farFar = 0.0F;
      this.active = (var3.farCount > 0 || var3.shellCount > 0) && var3.fogEnd > var3.nearEnd();
      if (!this.active || !var2) {
         this.shadow.off();
      }

      if (this.active) {
         float var4 = var3.fogEnd * 1.5F + 256.0F;
         this.projection.set(var3.projection);
         this.projection.m22((var4 + 16.0F) / (16.0F - var4));
         this.projection.m32(2.0F * var4 * 16.0F / (16.0F - var4));
         this.projection.mul(var1.view, this.viewProjection).invert(var1.farInvPlainViewProjection);
         var1.farNear = 16.0F;
         var1.farFar = var4;
         this.projection.set(var1.jitteredProjection);
         this.projection.m22((var4 + 16.0F) / (16.0F - var4));
         this.projection.m32(2.0F * var4 * 16.0F / (16.0F - var4));
         this.frustum
            .set(
               WorldRenderer.cullFromPlayer
                  ? this.projection.mul(WorldRenderer.cullView, this.cullViewProjection)
                  : this.projection.mul(var1.view, this.cullViewProjection)
            );
         this.projection.mul(var1.view, this.viewProjection);
         this.uploadMask(var3);
         this.trees.bake();
         if (var2) {
            this.shadow.draw(var1, this.mask, this.shellMask, this.shadows.quiet(var1));
         }

         GL13.glActiveTexture(33984);
      }
   }

   void gbuffer(FrameContext var1) {
      SceneData var2 = var1.scene;
      if (this.active && var2.shellCount != 0) {
         this.bandProgram.use();
         Wireframe.apply(this.bandProgram);
         FloorFilter.apply(this.bandProgram);
         TextureFilter.SPRITES.apply(this.bandProgram);
         this.bandProgram.set("uViewProjection", var1.viewProjection);
         this.bandProgram.set("uPullScale", 1.0F);
         this.bandProgram.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
         this.bandProgram.set("uFadeNoise", var1.fadeNoise());
         daylight(this.bandProgram, var2);
         this.bandProgram.set("uSkyLevel", var2.skyR, var2.skyG, var2.skyB);
         this.bandProgram.set("uWet", wetness(var1));
         masks(this.bandProgram, var2);
         Gl.bind(27, this.mask);
         Gl.bind(28, this.shellMask);
         MeshArena.bindLight(5, this.bandProgram);
         BandLight.bind(45);
         CellLightPages.bind(46, 47);
         this.trees.bind(true);
         GL13.glActiveTexture(33984);
         GL11.glEnable(2960);
         GL11.glStencilFunc(514, 0, 255);
         GL11.glStencilOp(7680, 7680, 7680);
         ShellArena.begin();
         long var3 = commanded;

         for (int var5 = 0; var5 < var2.shellCount; var5++) {
            FarGpu.BlockDraw var6 = var2.shells[var5];
            if (var6.shell != null && this.blockInView(var6) && distance(var6, var1, false) < 256.0F) {
               command(var6);
            }
         }

         Profile.bandVertices = commanded - var3;
         ShellArena.draw(this.bandProgram);
         GL11.glDisable(2960);
         GL11.glBindTexture(3553, 0);
         Gl.bind(27, 0);
         Gl.bind(28, 0);
         MeshArena.unbindLight(5);
         BandLight.unbind(45);
         CellLightPages.unbind(46, 47);
         this.trees.bind(false);
         GL13.glActiveTexture(33984);
      }
   }

   void draw(FrameContext var1) {
      SceneData var2 = var1.scene;
      if (this.active) {
         GL30.glBindFramebuffer(36160, this.targets.farFbo);
         GL11.glViewport(0, 0, var1.width, var1.height);
         GL11.glDepthMask(true);
         GL11.glClear(1280);
         Gl.bind(27, this.mask);
         Gl.bind(28, this.shellMask);
         Gl.bind(12, this.targets.depth);
         this.prime();
         this.shadows.bind(true);
         this.trees.bind(true);
         CellLightPages.bind(46, 47);
         GpuParts.begin(9);
         this.drawShells(var1, var2);
         GpuParts.end(9);
         GpuParts.begin(10);
         this.drawBoxes(var1, var2);
         GpuParts.end(10);
         GpuParts.begin(11);
         this.drawTrees(var1, var2);
         GpuParts.end(11);
         GL11.glDisable(2960);
         this.lamps.draw(var1, this.viewProjection);
         CellLightPages.unbind(46, 47);
         FarRelief.unbind();
         GL30.glBindVertexArray(0);
         Gl.bind(25, 0);
         Gl.bind(26, 0);
         Gl.bind(27, 0);
         Gl.bind(28, 0);
         Gl.bind(12, 0);
         this.shadows.bind(false);
         this.trees.bind(false);
         GL13.glActiveTexture(33984);
      }
   }

   private void prime() {
      boolean var1 = GL11.glIsEnabled(2929);
      GL11.glDisable(2929);
      GL11.glColorMask(false, false, false, false);
      GL11.glDepthMask(false);
      GL11.glEnable(2960);
      GL11.glStencilFunc(519, 1, 255);
      GL11.glStencilOp(7680, 7680, 7681);
      this.prime.use();
      Gl.screenTriangle();
      GL11.glStencilFunc(514, 0, 255);
      GL11.glStencilOp(7680, 7680, 7680);
      GL11.glColorMask(true, true, true, true);
      GL11.glDepthMask(true);
      if (var1) {
         GL11.glEnable(2929);
      }
   }

   private void drawShells(FrameContext var1, SceneData var2) {
      this.shellProgram.use();
      Wireframe.apply(this.shellProgram);
      FloorFilter.apply(this.shellProgram);
      TextureFilter.SPRITES.apply(this.shellProgram);
      this.common(this.shellProgram, var1);
      this.shellProgram.set("uPullScale", 0.003125F);
      this.shellProgram.set("uWet", wetness(var1));
      GL13.glActiveTexture(33984);
      ShellArena.begin();

      for (int var3 = 0; var3 < var2.shellCount; var3++) {
         FarGpu.BlockDraw var4 = var2.shells[var3];
         if (var4.shell != null && this.blockInView(var4)) {
            Profile.shellsInView++;
            if (distance(var4, var1, true) >= 256.0F) {
               command(var4);
            }
         }
      }

      Profile.shellVerticesDrawn = commanded;
      commanded = 0L;
      ShellArena.draw(this.shellProgram);
      GL11.glBindTexture(3553, 0);
   }

   static void command(FarGpu.BlockDraw var0) {
      int var1 = var0.shell.arenaFirst();
      if (var1 >= 0) {
         int var2 = ShellArena.record(var0.originX, var0.originY, var0.originZ, var0.worldX, var0.worldY, var0.groundLayer, var0.fadeLo, var0.fadeHi);

         for (int var3 = 0; var3 < var0.shell.pages.length; var3++) {
            commanded = commanded + var0.shell.count[var3];
            if (var0.shell.pages[var3] != null) {
               ShellArena.command(var0.shell, var3, var1 + var0.shell.first[var3], var0.shell.count[var3], var2);
            } else if (var0.groundArray != null) {
               ShellArena.ground(var0.groundArray, var0.groundTexture, var1 + var0.shell.first[var3], var0.shell.count[var3], var2);
            }
         }
      }
   }

   private void drawBoxes(FrameContext var1, SceneData var2) {
      this.program.use();
      this.common(this.program, var1);
      this.program.set("uWet", wetness(var1));
      this.program.set("uNearReach", var2.nearReach);
      this.program.set("uWindowGlow", Tuning.farWindowGlow);
      this.program.set("uInterior", 1.0F);

      for (int var3 = 0; var3 < var2.shellCount; var3++) {
         FarGpu.BlockDraw var4 = var2.shells[var3];
         if (var4.inside != null && var4.inside.vertices != 0 && this.blockInView(var4)) {
            this.program.set("uOrigin", var4.originX, var4.originY, var4.originZ);
            this.program.set("uCellWorld", var4.worldX, var4.worldY);
            this.program.set("uFade", var4.fadeLo, var4.fadeHi);
            FarGpu.draw(var4.inside);
         }
      }

      this.program.set("uInterior", 0.0F);

      for (int var6 = 0; var6 < var2.farCount; var6++) {
         FarGpu.CellDraw var7 = var2.far[var6];
         float var5 = var7.originY + var7.maxHeight * 2.4494896F;
         if (this.frustum.testAab(var7.originX - 256.0F, var7.bottom(), var7.originZ - 256.0F, var7.originX, var5, var7.originZ)) {
            Profile.farInView++;
            FarGpu.textures(var7.colours);
            Gl.bind(25, var7.colours.topTexture);
            Gl.bind(26, var7.colours.sideTexture);
            this.program.set("uOrigin", var7.originX, var7.originY, var7.originZ);
            this.program.set("uCellWorld", var7.worldX, var7.worldY);
            this.program.set("uFade", var7.fadeLo, var7.fadeHi);
            FarGpu.draw(var7.mesh);
         }
      }
   }

   private void drawTrees(FrameContext var1, SceneData var2) {
      if (TreeKinds.bind()) {
         float var3 = TREES_GONE.get() * 64.0F;
         this.treeProgram.use();
         this.common(this.treeProgram, var1);
         TreeKinds.uniforms(this.treeProgram);
         this.treeProgram.set("uNearReach", var2.nearReach);
         this.treeProgram.set("uPixel", 2.0F / (this.projection.m11() * var1.height));
         this.treeProgram.set("uTreeFade", Math.min(TREES_FADE.get() * 64.0F, var3), var3);
         this.treeProgram.set("uTreeVariation", Tuning.farTreeVariation, Tuning.farTreeRelief);
         this.treeProgram.set("uBareCrown", Tuning.farBareCrown);

         for (int var4 = 0; var4 < var2.farCount; var4++) {
            FarGpu.CellDraw var5 = var2.far[var4];
            float var6 = var5.originY + var5.maxHeight * 2.4494896F;
            float var7 = Math.max(0.0F, Math.max(var5.originX - 256.0F - var1.eyeX, var1.eyeX - var5.originX));
            float var8 = Math.max(0.0F, Math.max(var5.originZ - 256.0F - var1.eyeZ, var1.eyeZ - var5.originZ));
            if (var5.trees != null
               && var5.trees.count != 0
               && !(var7 * var7 + var8 * var8 >= var3 * var3)
               && this.frustum.testAab(var5.originX - 256.0F, var5.bottom(), var5.originZ - 256.0F, var5.originX, var6, var5.originZ)) {
               int var9 = var5.trees.kept(var5.treeKeep);
               if (var9 != 0) {
                  this.treeProgram.set("uGrow", var5.treeGrow);
                  this.treeProgram.set("uOrigin", var5.originX, var5.originY, var5.originZ);
                  this.treeProgram.set("uCellWorld", var5.worldX, var5.worldY);
                  this.treeProgram.set("uFade", var5.fadeLo, var5.fadeHi);
                  FarGpu.bindTrees(var5.trees);
                  Gl.generated(var9 * 6);
                  Profile.farTrees += var9;
               }
            }
         }

         TreeKinds.unbind();
         GL13.glActiveTexture(34027);
         GL11.glBindTexture(35882, 0);
         GL13.glActiveTexture(33984);
      }
   }

   private void common(GlProgram var1, FrameContext var2) {
      SceneData var3 = var2.scene;
      var1.set("uViewProjection", this.viewProjection);
      var1.set("uEye", var2.eyeX, var2.eyeY, var2.eyeZ);
      var1.set("uFadeNoise", var2.fadeNoise());
      var1.set("uFogColor", var3.fogR, var3.fogG, var3.fogB);
      var1.set("uFogRange", var3.fogStart, var3.fogEnd);
      var1.set("uHaze", Tuning.farHaze, var3.nearEnd());
      SkyPass.fogUniforms(var1, var3);
      daylight(var1, var3);
      masks(var1, var3);
      this.shadows.sunUniforms(var1, var2);
      var1.setInt("uDebugView", WorldRenderer.debugView);
   }

   private static float wetness(FrameContext var0) {
      return var0.pipeline.puddles ? var0.scene.wetGround : 0.0F;
   }

   static void daylight(GlProgram var0, SceneData var1) {
      var0.set("uDaylight", var1.daylightKnown ? var1.daylightR : 0.8F, var1.daylightKnown ? var1.daylightG : 0.8F, var1.daylightKnown ? var1.daylightB : 0.8F);
   }

   private static void masks(GlProgram var0, SceneData var1) {
      var0.set("uMaskGrid", var1.farMaskX, var1.farMaskY, var1.farMaskSize);
      LodView.apply(var0);
      var0.set("uShellGrid", var1.shellMaskX, var1.shellMaskY, 53.0F);
   }

   private boolean blockInView(FarGpu.BlockDraw var1) {
      float var2 = var1.originX - var1.blockX;
      float var3 = var2 - 64.0F;
      float var4 = var1.originZ - var1.blockY;
      float var5 = var4 - 64.0F;
      return this.frustum.testAab(var3, var1.bottom(), var5, var2, var1.originY + var1.top * 2.4494896F, var4);
   }

   private static float distance(FarGpu.BlockDraw var0, FrameContext var1, boolean var2) {
      float var3 = var0.originX - var0.blockX - var1.eyeX;
      float var4 = var3 - 64.0F;
      float var5 = var0.originZ - var0.blockY - var1.eyeZ;
      float var6 = var5 - 64.0F;
      float var7 = var0.bottom() - var1.eyeY;
      float var8 = var0.originY + var0.top * 2.4494896F - var1.eyeY;
      float var9 = var2 ? Math.max(-var4, var3) : Math.max(0.0F, Math.max(var4, -var3));
      float var10 = var2 ? Math.max(Math.abs(var7), Math.abs(var8)) : Math.max(0.0F, Math.max(var7, -var8));
      float var11 = var2 ? Math.max(-var6, var5) : Math.max(0.0F, Math.max(var6, -var5));
      return (float)Math.sqrt(var9 * var9 + var10 * var10 + var11 * var11);
   }

   private void uploadMask(SceneData var1) {
      int var2 = var1.farMaskSize;
      if (this.mask == 0) {
         GL13.glActiveTexture(34011);
         this.mask = Gl.texture(33321, 19, 19, 6403, 5121, 9728);
         GL13.glActiveTexture(34012);
         this.shellMask = Gl.texture(32849, 53, 53, 6407, 5121, 9728);
      }

      this.shellPixels.clear();
      this.shellPixels.put(var1.shellMask).flip();
      GL13.glActiveTexture(34012);
      GL11.glBindTexture(3553, this.shellMask);
      GL11.glPixelStorei(3317, 1);
      GL11.glTexSubImage2D(3553, 0, 0, 0, 53, 53, 6407, 5121, this.shellPixels);
      GL11.glPixelStorei(3317, 4);
      if (var2 > 0) {
         this.maskPixels.clear();
         this.maskPixels.put(var1.farMask, 0, var2 * var2).flip();
         GL13.glActiveTexture(34011);
         GL11.glBindTexture(3553, this.mask);
         GL11.glPixelStorei(3317, 1);
         GL11.glTexSubImage2D(3553, 0, 0, 0, var2, var2, 6403, 5121, this.maskPixels);
         GL11.glPixelStorei(3317, 4);
      }

      GL11.glBindTexture(3553, 0);
      GL13.glActiveTexture(33984);
   }
}
