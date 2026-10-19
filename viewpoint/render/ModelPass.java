package viewpoint.render;

import java.nio.FloatBuffer;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.HotReload;
import viewpoint.platform.Profile;
import zombie.core.skinnedmodel.model.ModelOutlines;
import zombie.core.textures.ColorInfo;
import zombie.core.textures.Texture;
import zombie.core.textures.TextureFBO;

final class ModelPass {
   private final GlProgram gbuffer = GlProgram.create("model", "model.vert", "model_gbuffer.frag");
   private final GlProgram shadow = GlProgram.create("model shadow", "model.vert", "model_shadow.frag");
   private final GlProgram outline = GlProgram.create("model outline", "model.vert", "model_outline.frag");
   private final GlProgram glass = GlProgram.create("model glass", "model.vert", "model_glass.frag");
   private final ColorInfo outlineColour = new ColorInfo();
   private final FrustumIntersection frustum = new FrustumIntersection();
   private static final float[] STILL = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
   private final Targets targets;
   private ModelDraws draws;
   private ModelMeshes.Held[] held = new ModelMeshes.Held[256];
   private Texture[] drawn = new Texture[256];
   private final ItemTextures items = new ItemTextures();
   final ModelBatches batches = new ModelBatches();
   private final VehiclePass vehicles = new VehiclePass(this);
   private int paletteTexture;
   private int recordTexture;
   private ModelPass.Range palettes;
   private ModelPass.Range records;
   private FloatBuffer upload = MemoryUtil.memAllocFloat(49152);
   private long frameCounter;
   private int reloads;
   private int found;
   private final CharacterTextures characterTextures = new CharacterTextures();
   private final ModelBatches.State runs = new ModelBatches.State() {
      @Override
      public void set(int var1) {
         ModelPass.this.cull(var1);
      }

      @Override
      public void texture(int var1, int var2) {
         ModelPass.this.bind(64 + var1, var2);
      }
   };

   ModelPass(Targets var1) {
      this.targets = var1;

      for (GlProgram var5 : new GlProgram[]{this.gbuffer, this.shadow, this.outline, this.glass}) {
         var5.sampler("uPalettes", 31);
         var5.sampler("uDraws", 40);

         for (int var6 = 0; var6 < 16; var6++) {
            var5.sampler("uTextures[" + var6 + "]", 64 + var6);
         }
      }

      forwardSamplers(this.glass);
   }

   static void samplers(GlProgram var0) {
      var0.sampler("uTexture", 0);
      var0.sampler("uPalettes", 31);
      var0.sampler("uDraws", 40);
   }

   static void forwardSamplers(GlProgram var0) {
      ShadowPass.samplers(var0);
      Spots.sampler(var0);
      LampShadows.sampler(var0);
      MuzzleShadow.sampler(var0);
   }

   static void forwardLight(GlProgram var0, FrameContext var1, ShadowPass var2) {
      var2.sunUniforms(var0, var1);
      SurfacePass.lamps(var0, var1.scene);
      var2.lamps.uniforms(var0, var1.scene);
      SurfacePass.torches(var0, var1.scene);
      var2.muzzle.uniforms(var0);
      var2.spots.uniforms(var0);
      var0.set("uFogColor", var1.scene.fogR, var1.scene.fogG, var1.scene.fogB);
   }

   void prepare(SceneData var1) {
      ModelDraws var2 = var1.models;
      this.draws = var2;
      this.frameCounter++;
      if (this.reloads != HotReload.generation) {
         this.reloads = HotReload.generation;
         ModelMeshes.clear();
      }

      this.characterTextures.ready(var2);
      if (this.held.length < var2.count) {
         this.held = new ModelMeshes.Held[var2.meshes.length];
         this.drawn = new Texture[var2.meshes.length];
      }

      boolean var3 = var2.slotCount > 0;

      for (int var4 = 0; var4 < var2.count; var4++) {
         Texture var5 = var2.items[var4] == null ? null : this.items.make(var2.items[var4]);
         var3 |= var2.items[var4] != null;
         this.drawn[var4] = var5 != null ? var5 : texture(var2, var4);
         this.held[var4] = ModelMeshes.available() ? ModelMeshes.get(var2.meshes[var4], this.frameCounter) : null;
      }

      this.characterTextures.leaveOut(var2, this.held);
      if (var3) {
         Gl.resetState();
      }

      ModelMeshes.makeRoom(this.frameCounter);
      this.batches.order(var2, this.held, this.drawn);
      if (var2.count != 0) {
         if (this.paletteTexture == 0) {
            this.paletteTexture = GL11.glGenTextures();
            this.recordTexture = GL11.glGenTextures();
         }

         this.palettes = this.fill(var2.palettes, var2.bones * 12);
         this.records = this.fill(var2.values, var2.count * 68);
      }
   }

   private ModelPass.Range fill(float[] var1, int var2) {
      if (this.upload.capacity() < var2) {
         MemoryUtil.memFree(this.upload);
         this.upload = MemoryUtil.memAllocFloat(var2 * 2);
      }

      this.upload.clear();
      this.upload.put(var1, 0, var2).flip();
      long var3 = FrameStream.put(this.upload, FrameStream.textureAlignment());
      return new ModelPass.Range(FrameStream.buffer(), var3, var2 * 4L);
   }

   private static void attach(int var0, ModelPass.Range var1) {
      GL11.glBindTexture(35882, var0);
      if (var1 != null && var1.bytes() > 0L) {
         GL43.glTexBufferRange(35882, 34836, var1.buffer(), var1.at(), var1.bytes());
      }
   }

   void gbuffer(FrameContext var1) {
      ModelDraws var2 = var1.scene.models;
      Profile.paletteBones = 0;
      Profile.modelRuns = 0;
      Profile.ownedModels = 0;
      Profile.paletteBytes = 0L;
      Profile.modelTriangles = 0L;
      this.targets.velocity(true);
      GL30.glClearBufferfv(6144, 3, STILL);
      if (var2.count == 0) {
         this.targets.velocity(false);
      } else {
         this.frustum.set(var1.viewProjection);
         this.gbuffer.use();
         Wireframe.apply(this.gbuffer);
         TextureFilter.MODELS.apply(this.gbuffer);
         this.gbuffer.set("uViewProjection", var1.viewProjection);
         views(this.gbuffer, var1);
         this.gbuffer.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
         this.gbuffer.set("uFadeNoise", var1.fadeNoise());
         this.gbuffer.set("uGlassOpaque", 0.85F);
         CameraClear.apply(this.gbuffer);
         this.begin();
         GL11.glEnable(2960);
         GL11.glStencilFunc(519, 1, 255);
         GL11.glStencilOp(7680, 7680, 7681);
         int var3 = this.batches.draw(var2x -> var2.material[var2x] < 0 && this.shown(var2, var2x), this.runs);
         Profile.modelRuns = this.batches.runsDrawn;
         Profile.modelTriangles = this.batches.elementsDrawn / 3L;
         Profile.paletteBones = var2.bones;
         Profile.paletteBytes = var2.bones * 12L * 4L;
         var3 += this.vehicles.gbuffer(var1);
         GL11.glDisable(2960);
         this.targets.velocity(false);
         this.end();
         Profile.ownedModels = var3;
      }
   }

   void shadow(SceneData var1, Matrix4f var2, float var3, float var4) {
      ModelDraws var5 = var1.models;
      this.shadow(var1, var2, null, 0, var3x -> casts(var5, var3x, var3, var4));
   }

   void shadowAhead(SceneData var1, Matrix4f var2, float[] var3, float[] var4, float var5) {
      ModelDraws var6 = var1.models;
      this.shadow(var1, var2, null, 0, var4x -> ahead(var6, var4x, var3, var4, var5));
   }

   void shadow(SceneData var1, Matrix4f var2, int[] var3, int var4) {
      this.shadow(var1, var2, var3, var4, var0 -> true);
   }

   private void shadow(SceneData var1, Matrix4f var2, int[] var3, int var4, ModelBatches.Filter var5) {
      ModelDraws var6 = var1.models;
      if (var6.count != 0) {
         this.shadow.use();
         this.shadow.set("uViewProjection", var2);
         this.shadow.setInt("uShadowPalette", 1);
         this.shadow.set("uFadeNoise", 0.0F);
         this.begin();
         this.batches.draw(var3, var4, var2x -> var6.material[var2x] < 0 && var5.take(var2x), this.runs);
         this.vehicles.shadow(var6, var2, var3, var4, var5, false);
         this.end();
      }
   }

   void iris(GlProgram var1, SceneData var2, Matrix4f var3, float var4, boolean var5) {
      ModelDraws var6 = var2.models;
      if (var6.count != 0) {
         boolean var7 = var4 > 0.0F;
         var1.setInt("uShadowPalette", var7 ? 1 : 0);
         var1.set("uIrisGlassRange", 0.04F, 0.85F);
         this.frustum.set(var3);
         this.begin();
         this.batches
            .draw(var5x -> var7 ? casts(var6, var5x, 0.0F, var4) : var6.material[var5x] < 0 && block(var6, var5x) == var5 && this.shown(var6, var5x), this.runs);
         this.end();
      }
   }

   void irisGlass(GlProgram var1, SceneData var2, Matrix4f var3) {
      ModelDraws var4 = var2.models;
      if (var4.count != 0) {
         var1.setInt("uShadowPalette", 0);
         var1.setInt("uIrisGlass", 1);
         var1.set("uIrisGlassRange", 0.04F, 0.85F);
         this.frustum.set(var3);
         this.begin();
         GL11.glDepthMask(false);
         this.batches.draw(var2x -> var4.material[var2x] < 0 && (var4.flags[var2x] & 1024) != 0 && this.shown(var4, var2x), this.runs);
         this.end();
      }
   }

   private static boolean block(ModelDraws var0, int var1) {
      return var0.palette[var1] < 0 && var0.items[var1] == null;
   }

   void irisVehicles(GlProgram var1, SceneData var2, boolean var3) {
      if (var2.models.vehicles.count() != 0) {
         var1.setInt("uShadowPalette", 0);
         this.begin();
         this.vehicles.iris(var1, var2.models, var3);
         this.end();
      }
   }

   void rain(SceneData var1, Matrix4f var2, float var3) {
      if (var1.models.vehicles.count() != 0) {
         ModelDraws var4 = var1.models;
         this.begin();
         this.vehicles.shadow(var4, var2, null, 0, var2x -> casts(var4, var2x, 0.0F, var3), true);
         this.end();
      }
   }

   void glass(FrameContext var1, ShadowPass var2) {
      ModelDraws var3 = var1.scene.models;
      if (var3.count != 0) {
         this.frustum.set(var1.viewProjection);
         this.begin();
         this.glass.use();
         this.glass.set("uViewProjection", var1.viewProjection);
         this.glass.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
         this.glass.set("uFadeNoise", var1.fadeNoise());
         this.glass.set("uAlphaRange", 0.04F, 0.85F);
         CameraClear.apply(this.glass);
         forwardLight(this.glass, var1, var2);
         var2.bind(true);
         GL11.glDepthMask(false);
         this.batches.draw(var2x -> var3.material[var2x] < 0 && (var3.flags[var2x] & 1024) != 0 && this.shown(var3, var2x), this.runs);
         GL11.glDepthMask(true);
         var2.bind(false);
         if (var3.vehicles.count() > 0) {
            this.vehicles.glass(var1, var2);
         }

         this.end();
      }
   }

   void outlines(FrameContext var1, int var2) {
      ModelDraws var3 = var1.scene.models;
      if (var3.outlined) {
         int var4 = TextureFBO.lastID;
         TextureFBO.lastID = var2;

         try {
            int var5 = 0;

            while (var5 < var3.count) {
               int var6 = var5 + 1;
               if (var3.values[var5 * 68 + 32 + 3] > 0.0F) {
                  while (var6 < var3.count && sameOutline(var3, var5, var6)) {
                     var6++;
                  }

                  this.silhouettes(var1, var3, var5, var6);
               }

               var5 = var6;
            }
         } finally {
            TextureFBO.lastID = var4;
            Gl.resetState();
         }
      }
   }

   private void silhouettes(FrameContext var1, ModelDraws var2, int var3, int var4) {
      int var5 = var3 * 68 + 32;
      float[] var6 = var2.values;
      this.outlineColour.set(var6[var5], var6[var5 + 1], var6[var5 + 2], var6[var5 + 3]);
      boolean var7 = (var2.flags[var3] & 4) != 0;
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      boolean var8 = ModelOutlines.instance.beginRenderOutline(this.outlineColour, var7, false);
      GL11.glDepthMask(true);
      GL11.glDisable(3089);
      ModelOutlines.instance.fboA.startDrawing(var8, true);
      GL11.glViewport(var1.viewport[0], var1.viewport[1], var1.viewport[2], var1.viewport[3]);
      GL11.glDisable(3042);
      this.outline.use();
      this.outline.set("uViewProjection", var1.plainViewProjection);
      this.outline.set("uColour", var6[var5], var6[var5 + 1], var6[var5 + 2]);
      this.begin();
      this.batches.draw(var3x -> var3x >= var3 && var3x < var4 && (var2.flags[var3x] & 1) != 0, this.runs);
      this.end();
      ModelOutlines.instance.fboA.endDrawing();
   }

   void targets(FrameContext var1) {
      ModelDraws var2 = var1.scene.models;
      byte var3 = 17;
      if (var2.targeted) {
         this.outline.use();
         this.outline.set("uViewProjection", var1.plainViewProjection);
         this.outline.set("uColour", 1.0F, 1.0F, 1.0F);
         this.begin();
         this.batches.draw(var2x -> (var2.flags[var2x] & var3) == var3, this.runs);
         this.end();
      }
   }

   void endFrame() {
      this.items.release();
   }

   private static boolean sameOutline(ModelDraws var0, int var1, int var2) {
      for (int var3 = 0; var3 < 4; var3++) {
         if (var0.values[var1 * 68 + 32 + var3] != var0.values[var2 * 68 + 32 + var3]) {
            return false;
         }
      }

      return (var0.flags[var1] & 4) == (var0.flags[var2] & 4);
   }

   boolean shown(ModelDraws var1, int var2) {
      int var3 = var2 * 68 + 24;
      float[] var4 = var1.values;
      return (var1.flags[var2] & 1) != 0 && this.frustum.testSphere(var4[var3], var4[var3 + 1], var4[var3 + 2], var4[var3 + 3]);
   }

   static boolean casts(ModelDraws var0, int var1, float var2, float var3) {
      int var4 = var1 * 68 + 24;
      float var5 = var0.values[var4];
      float var6 = var0.values[var4 + 2];
      float var7 = (float)Math.sqrt(var5 * var5 + var6 * var6);
      return (var0.flags[var1] & 2) != 0 && var7 >= var2 && var7 <= var3;
   }

   static boolean ahead(ModelDraws var0, int var1, float[] var2, float[] var3, float var4) {
      int var5 = var1 * 68 + 24;
      float var6 = var0.values[var5] - var2[0];
      float var7 = var0.values[var5 + 1] - var2[1];
      float var8 = var0.values[var5 + 2] - var2[2];
      float var9 = var6 * var3[0] + var7 * var3[1] + var8 * var3[2];
      return (var0.flags[var1] & 2) != 0 && var9 > 0.0F && var9 <= var4;
   }

   static void views(GlProgram var0, FrameContext var1) {
      var0.set("uPlainViewProjection", var1.plainViewProjection);
      var0.set("uPrevViewProjection", var1.previousViewProjection);
      var0.setInt("uMotionPass", 1);
   }

   void surface(int var1) {
      this.cull(var1);
      this.bind(0, var1);
   }

   private void cull(int var1) {
      if (this.draws.cull[var1] == 0) {
         GL11.glDisable(2884);
      } else {
         GL11.glEnable(2884);
         GL11.glCullFace(this.draws.cull[var1]);
      }
   }

   private void bind(int var1, int var2) {
      GL13.glActiveTexture(33984 + var1);
      Texture.lastTextureID = -1;
      this.drawn[var2].bind();
      Texture.lastTextureID = -1;
      GL33.glBindSampler(var1, TextureFilter.MODELS.sampler(this.drawn[var2].getID()));
   }

   private static Texture texture(ModelDraws var0, int var1) {
      return var0.instances[var1] != null && var0.instances[var1].tex != null ? var0.instances[var1].tex : var0.textures[var1];
   }

   private void begin() {
      GL13.glActiveTexture(34015);
      attach(this.paletteTexture, this.palettes);
      GL13.glActiveTexture(34024);
      attach(this.recordTexture, this.records);
      GL13.glActiveTexture(33984);
      this.found = GL11.glGetInteger(32873);
      GL11.glEnable(2929);
      GL11.glDepthFunc(513);
      GL11.glDepthMask(true);
   }

   private void end() {
      GL30.glBindVertexArray(0);
      GL11.glDisable(2884);

      for (int var4 : new int[]{31, 40}) {
         GL13.glActiveTexture(33984 + var4);
         GL11.glBindTexture(35882, 0);
      }

      for (int var5 = 0; var5 < 16; var5++) {
         GL33.glBindSampler(64 + var5, 0);
      }

      GL13.glActiveTexture(33984);
      GL33.glBindSampler(0, 0);
      GL11.glBindTexture(3553, this.found);
      Texture.lastTextureID = -1;
   }

   private record Range(int buffer, long at, long bytes) {
   }
}
