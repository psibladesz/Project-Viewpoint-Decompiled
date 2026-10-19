package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.Arrays;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.Pipeline;
import viewpoint.platform.Profile;
import viewpoint.platform.Tuning;

final class LampShadows {
   static final int FACES = 6;
   static final float[][] FORWARD = new float[][]{
      {1.0F, 0.0F, 0.0F}, {-1.0F, 0.0F, 0.0F}, {0.0F, 1.0F, 0.0F}, {0.0F, -1.0F, 0.0F}, {0.0F, 0.0F, 1.0F}, {0.0F, 0.0F, -1.0F}
   };
   static final float[][] UP = new float[][]{
      {0.0F, 1.0F, 0.0F}, {0.0F, 1.0F, 0.0F}, {0.0F, 0.0F, 1.0F}, {0.0F, 0.0F, 1.0F}, {0.0F, 1.0F, 0.0F}, {0.0F, 1.0F, 0.0F}
   };
   private static final float NEAR = 0.6F;
   private static final float OWN_FIXTURE = 0.5F;
   private static final int FACES_A_FRAME = 16;
   static final int POSE_FRAMES = 4;
   private static final byte LAMP_MESH = -128;
   private static final float FADE_IN = 0.25F;
   private static final float MESH_RADIUS = 8.0F;
   private static final long FREE = -1L;
   private final GlProgram program = GlProgram.create("lamp shadow", "shadow.vert", "shadow.frag");
   private final ModelPass models;
   private final FrustumIntersection frustum = new FrustumIntersection();
   private final Matrix4f projection = new Matrix4f();
   private final Matrix4f faceViewProjection = new Matrix4f();
   private final FloatBuffer slotData = BufferUtils.createFloatBuffer(32);
   private int texture;
   private int fbo;
   private int size;
   private int slots;
   private long[] keys = new long[0];
   private long[] worldSums = new long[0];
   private long[] modelSums = new long[0];
   private float[] shown = new float[0];
   private boolean[] drawn = new boolean[0];
   private int count;
   private final int[] slotOf = new int[8];
   private int[] near = new int[64];
   private int[] nearModels = new int[64];
   private int[] faceModels = new int[16];
   private int nearCount;
   private int nearModelCount;
   private int faceModelCount;
   private boolean facePosed;
   private long tick;
   private long facesDrawn;
   private long worldsDrawn;
   private long posesDrawn;
   private long frames;

   LampShadows(ModelPass var1) {
      this.models = var1;
      this.program.sampler("uTexture", 0);
      Profile.lampShadowsReport = this::report;
   }

   static void sampler(GlProgram var0) {
      var0.sampler("uLampShadow", 49);
   }

   void draw(FrameContext var1) {
      SceneData var2 = var1.scene;
      Pipeline var3 = var1.pipeline;
      this.tick++;
      this.frames++;
      if (var3.lampShadows == 0 && this.texture != 0) {
         this.release();
      }

      this.count = WorldRenderer.off(8) ? 0 : Math.min(Math.min(var2.lampShadowCount, var3.lampShadows), this.slotOf.length);
      if (this.count != 0) {
         GlDebug.push("lamp shadows");
         GpuParts.begin(17);
         this.resize(var3.lampShadows, var3.lampShadowResolution);
         this.assign(var2);
         GL11.glEnable(2929);
         GL11.glDepthFunc(513);
         GL11.glDepthMask(true);
         GL11.glEnable(32823);
         GL11.glPolygonOffset(1.5F, 3.0F);
         int var4 = 16;

         for (int var5 = 0; var5 < this.count; var5++) {
            var4 = this.lamp(var2, var5, var4);
         }

         GL11.glDisable(32823);
         GpuParts.end(17);
         GlDebug.pop();
      }
   }

   void bind(boolean var1) {
      GL13.glActiveTexture(34033);
      GL11.glBindTexture(35866, var1 && this.count > 0 ? this.texture : 0);
      GL13.glActiveTexture(33984);
   }

   void uniforms(GlProgram var1, SceneData var2) {
      if (var1.has("uLampShadowCount")) {
         var1.setInt("uLampShadowCount", this.count);
         if (this.count != 0) {
            this.slotData.clear();

            for (int var3 = 0; var3 < this.count; var3++) {
               int var4 = this.slotOf[var3];
               int var5 = var3 * 12;
               this.slotData.put(var4 * 6).put(this.shown[var4] * var2.lights[var5 + 9]).put(0.6F).put(far(var2.lights[var5 + 3]));
            }

            this.slotData.flip();
            var1.setVec4s("uLampShadowSlot", this.slotData);
            var1.set("uLampShadowFloor", Tuning.lampShadowFloor);
         }
      }
   }

   private void resize(int var1, int var2) {
      if (var1 != this.slots || var2 != this.size) {
         this.release();
         this.slots = var1;
         this.size = var2;
         int var3 = this.slots * 6 * 2;
         this.texture = GL45.glCreateTextures(35866);
         GL45.glTextureParameteri(this.texture, 10241, 9729);
         GL45.glTextureParameteri(this.texture, 10240, 9729);
         GL45.glTextureParameteri(this.texture, 10242, 33071);
         GL45.glTextureParameteri(this.texture, 10243, 33071);
         GL45.glTextureParameteri(this.texture, 34892, 34894);
         GL45.glTextureParameteri(this.texture, 34893, 515);
         GL45.glTextureStorage3D(this.texture, 1, 33189, this.size, this.size, var3);
         this.fbo = GL45.glCreateFramebuffers();
         GL45.glNamedFramebufferDrawBuffer(this.fbo, 0);
         GL45.glNamedFramebufferReadBuffer(this.fbo, 0);
         this.keys = new long[this.slots];
         Arrays.fill(this.keys, -1L);
         this.shown = new float[this.slots];
         this.worldSums = new long[this.slots * 6];
         this.modelSums = new long[this.slots * 6];
         this.drawn = new boolean[this.slots * 6];
         System.out
            .println(
               "[Viewpoint] lamp shadows: "
                  + this.slots
                  + " cubes of "
                  + this.size
                  + " texels a side, "
                  + ((long)var3 * this.size * this.size * 2L >> 20)
                  + " MB"
            );
      }
   }

   private void release() {
      if (this.texture != 0) {
         GL11.glDeleteTextures(this.texture);
         GL30.glDeleteFramebuffers(this.fbo);
         this.texture = this.fbo = 0;
         this.slots = this.size = 0;
      }
   }

   private void assign(SceneData var1) {
      for (int var2 = 0; var2 < this.slots; var2++) {
         if (this.keys[var2] != -1L && this.light(var1, this.keys[var2]) < 0) {
            this.keys[var2] = -1L;
         }
      }

      for (int var6 = 0; var6 < this.count; var6++) {
         long var3 = var1.lightKeys[var6];
         int var5 = 0;

         while (var5 < this.slots && this.keys[var5] != var3) {
            var5++;
         }

         if (var5 == this.slots) {
            var5 = 0;

            while (this.keys[var5] != -1L) {
               var5++;
            }

            this.keys[var5] = var3;
            this.shown[var5] = 0.0F;
            Arrays.fill(this.drawn, var5 * 6, var5 * 6 + 6, false);
         }

         this.slotOf[var6] = var5;
      }
   }

   private int light(SceneData var1, long var2) {
      for (int var4 = 0; var4 < this.count; var4++) {
         if (var1.lightKeys[var4] == var2) {
            return var4;
         }
      }

      return -1;
   }

   private int lamp(SceneData var1, int var2, int var3) {
      int var4 = this.slotOf[var2];
      int var5 = var2 * 12;
      float var6 = var1.lights[var5];
      float var7 = var1.lights[var5 + 1];
      float var8 = var1.lights[var5 + 2];
      float var9 = var1.lights[var5 + 3];
      this.gather(var1, var6, var7, var8, var9);
      this.projection.setPerspective((float)Math.toRadians(90.0), 1.0F, 0.6F, far(var9));
      boolean var10 = true;

      for (int var11 = 0; var11 < 6; var11++) {
         int var12 = var4 * 6 + var11;
         this.faceViewProjection
            .set(this.projection)
            .lookAt(var6, var7, var8, var6 + FORWARD[var11][0], var7 + FORWARD[var11][1], var8 + FORWARD[var11][2], UP[var11][0], UP[var11][1], UP[var11][2]);
         this.frustum.set(this.faceViewProjection);
         long var13 = this.mark(var1);
         long var15 = this.modelsHeld(var1);
         boolean var17 = !this.drawn[var12] || this.worldSums[var12] != var13;
         boolean var18 = this.modelSums[var12] != var15;
         if (due(var17, var18, this.facePosed, this.tick, var12)) {
            if (var3 == 0) {
               var10 &= this.drawn[var12];
            } else {
               this.face(var1, var12, var17, var6, var7, var8);
               this.posesDrawn += !var17 && !var18 ? 1L : 0L;
               this.drawn[var12] = true;
               this.worldSums[var12] = var13;
               this.modelSums[var12] = var15;
               var3--;
            }
         }
      }

      this.unmark(var1);
      this.shown[var4] = var10 ? Math.min(1.0F, this.shown[var4] + 0.25F) : this.shown[var4];
      return var3;
   }

   static boolean due(boolean var0, boolean var1, boolean var2, long var3, int var5) {
      return var0 || var1 || var2 && (var3 + var5) % 4L == 0L;
   }

   private void face(SceneData var1, int var2, boolean var3, float var4, float var5, float var6) {
      int var7 = this.slots * 6 + var2;
      if (var3) {
         this.target(var7);
         GL11.glClear(256);
         this.program.use();
         ShadowPass.casters(this.program);
         this.program.set("uViewProjection", this.faceViewProjection);
         this.program.set("uEye", var4, var5, var6);
         this.program.set("uOwnFixture", 0.5F);
         Meshes.draw(var1, this.program, (byte)-128);
         this.worldsDrawn++;
      }

      GL43.glCopyImageSubData(this.texture, 35866, 0, 0, 0, var7, this.texture, 35866, 0, 0, 0, var2, this.size, this.size, 1);
      if (this.faceModelCount > 0) {
         this.target(var2);
         this.models.shadow(var1, this.faceViewProjection, this.faceModels, this.faceModelCount);
      }

      this.facesDrawn++;
   }

   private void target(int var1) {
      GL45.glNamedFramebufferTextureLayer(this.fbo, 36096, this.texture, 0, var1);
      GL30.glBindFramebuffer(36160, this.fbo);
      GL11.glViewport(0, 0, this.size, this.size);
   }

   private void gather(SceneData var1, float var2, float var3, float var4, float var5) {
      this.nearCount = 0;

      for (int var6 = 0; var6 < var1.meshCount; var6++) {
         float var7 = var1.meshOffsets[var6 * 3] - 4.0F - var2;
         float var8 = var1.meshOffsets[var6 * 3 + 1] + var1.meshes[var6].level * 2.4494896F + 1.5F - var3;
         float var9 = var1.meshOffsets[var6 * 3 + 2] - 4.0F - var4;
         float var10 = var5 + 8.0F;
         if (var7 * var7 + var8 * var8 + var9 * var9 < var10 * var10) {
            this.near = this.nearCount == this.near.length ? Arrays.copyOf(this.near, this.nearCount * 2) : this.near;
            this.near[this.nearCount++] = var6;
         }
      }

      ModelDraws var13 = var1.models;
      this.nearModelCount = 0;

      for (int var14 = 0; var14 < var13.count; var14++) {
         int var15 = var14 * 68 + 24;
         float var16 = var13.values[var15] - var2;
         float var17 = var13.values[var15 + 1] - var3;
         float var11 = var13.values[var15 + 2] - var4;
         float var12 = var5 + var13.values[var15 + 3];
         if ((var13.flags[var14] & 2) != 0 && var16 * var16 + var17 * var17 + var11 * var11 < var12 * var12) {
            this.nearModels = this.nearModelCount == this.nearModels.length ? Arrays.copyOf(this.nearModels, this.nearModelCount * 2) : this.nearModels;
            this.nearModels[this.nearModelCount++] = var14;
         }
      }
   }

   private long mark(SceneData var1) {
      long var2 = 0L;

      for (int var4 = 0; var4 < this.nearCount; var4++) {
         int var5 = this.near[var4];
         float var6 = var1.meshOffsets[var5 * 3] - 4.0F;
         float var7 = var1.meshOffsets[var5 * 3 + 2] - 4.0F;
         float var8 = var1.meshOffsets[var5 * 3 + 1] + var1.meshes[var5].level * 2.4494896F + 1.5F;
         boolean var9 = var1.meshes[var5].pages.length > 0 && var1.meshes[var5].pages[0] == null;
         var1.meshFlags[var5] = (byte)(var1.meshFlags[var5] & 127);
         if (var9 || this.frustum.testSphere(var6, var8, var7, 8.0F)) {
            var1.meshFlags[var5] = (byte)(var1.meshFlags[var5] | -128);
            long var10 = System.identityHashCode(var1.meshes[var5]) * -7046029254386353131L
               + Float.floatToIntBits(var1.meshFade[var5 * 2]) * 31L
               + Float.floatToIntBits(var1.meshFade[var5 * 2 + 1]);
            var2 += var10 ^ var10 >>> 29;
         }
      }

      return var2;
   }

   private void unmark(SceneData var1) {
      for (int var2 = 0; var2 < this.nearCount; var2++) {
         var1.meshFlags[this.near[var2]] = (byte)(var1.meshFlags[this.near[var2]] & 127);
      }
   }

   private long modelsHeld(SceneData var1) {
      ModelDraws var2 = var1.models;
      long var3 = 0L;
      this.faceModelCount = 0;
      this.facePosed = false;

      for (int var5 = 0; var5 < this.nearModelCount; var5++) {
         int var6 = this.nearModels[var5];
         if (this.takes(var2, var6)) {
            this.faceModels = this.faceModelCount == this.faceModels.length ? Arrays.copyOf(this.faceModels, this.faceModelCount * 2) : this.faceModels;
            this.faceModels[this.faceModelCount++] = var6;
            int var7 = var6 * 68;
            this.facePosed = this.facePosed | (var2.palette[var6] >= 0 && (var2.flags[var6] & 32) == 0);
            long var8 = Math.round((var2.values[var7 + 12] + var1.originX) * 256.0);
            var8 = var8 * 31L + Math.round((var2.values[var7 + 13] + var1.originY) * 256.0);
            var8 = var8 * 31L + Math.round((var2.values[var7 + 14] + var1.originZ) * 256.0);

            for (int var10 = 0; var10 < 11; var10++) {
               var8 = var8 * 31L + Float.floatToIntBits(var2.values[var7 + var10]);
            }

            var8 = var8 * 31L + Float.floatToIntBits(var2.values[var7 + 56]);
            var8 = var8 * 31L + Float.floatToIntBits(var2.values[var7 + 56 + 1]);
            var8 *= -7046029254386353131L;
            var3 += var8 ^ var8 >>> 29;
         }
      }

      return var3;
   }

   private boolean takes(ModelDraws var1, int var2) {
      int var3 = var2 * 68 + 24;
      return (var1.flags[var2] & 2) != 0 && this.frustum.testSphere(var1.values[var3], var1.values[var3 + 1], var1.values[var3 + 2], var1.values[var3 + 3]);
   }

   private static float far(float var0) {
      return var0 + 1.0F;
   }

   private String report() {
      double var1 = this.frames == 0L ? 0.0 : 1.0 / this.frames;
      String var3 = String.format(
         "%d of %d (%d texels), faces drawn/frame %.2f (world %.2f, poses %.2f)",
         this.count,
         this.slots,
         this.size,
         this.facesDrawn * var1,
         this.worldsDrawn * var1,
         this.posesDrawn * var1
      );
      this.facesDrawn = 0L;
      this.worldsDrawn = 0L;
      this.posesDrawn = 0L;
      this.frames = 0L;
      return var3;
   }
}
