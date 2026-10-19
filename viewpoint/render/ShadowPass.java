package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.Arrays;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.Pipeline;

final class ShadowPass {
   static final int CASCADES = 3;
   static final int SUN_MAPS = 5;
   static final float NEAR_RADIUS = 22.0F;
   static final float MID_RADIUS = 64.0F;
   static final float PLANT_LIGHT = 100000.0F;
   private static final float CASTS_FROM = 0.3F;
   private static final float FAR_DRIFT = 8.0F;
   private final GlProgram program = GlProgram.create("shadow", "shadow.vert", "shadow.frag");
   private final int[] fbos = new int[3];
   private final int[] textures = new int[3];
   private final ShadowDepthViews depthViews = new ShadowDepthViews();
   private int size;
   private int drawnCount;
   private long frameCount;
   private float drawnReach;
   private float farSunX;
   private float farSunY = 2.0F;
   private float farSunZ;
   private final boolean[] valid = new boolean[3];
   private final double[] origin = new double[9];
   private float sunDistance = 200.0F;
   private final Matrix4f lightView = new Matrix4f();
   private final Matrix4f[] viewProjection = new Matrix4f[]{new Matrix4f(), new Matrix4f(), new Matrix4f()};
   private final Matrix4f[] drawn = new Matrix4f[]{new Matrix4f(), new Matrix4f(), new Matrix4f()};
   private final Matrix4f[] matrix = new Matrix4f[]{new Matrix4f(), new Matrix4f(), new Matrix4f()};
   private final float[] reach = new float[3];
   private long farCasters;
   private final FloatBuffer matrices = BufferUtils.createFloatBuffer(80);
   private final FloatBuffer mapParams = BufferUtils.createFloatBuffer(20);
   private final Matrix4f farMatrix = new Matrix4f();
   final Spots spots;
   final RainMap rain;
   private final ModelPass models;
   final LampShadows lamps;
   final MuzzleShadow muzzle;
   private final FarShadow farMaps;
   private final WeatherMap weatherMap;
   private final CascadeLayers layers;
   private final CascadeLayers.Cascade target = new CascadeLayers.Cascade();

   ShadowPass(ModelPass var1, FarShadow var2, WeatherMap var3) {
      this.models = var1;
      this.farMaps = var2;
      this.weatherMap = var3;
      this.lamps = new LampShadows(var1);
      this.muzzle = new MuzzleShadow(var1);
      this.layers = new CascadeLayers(this.program, var1, var2);
      this.program.sampler("uTexture", 0);
      int var4 = GL11.glGetInteger(32873);
      int var5 = GL11.glGetInteger(36006);
      this.makeCascades(GlProgram.pack().pipeline.shadowResolution);
      this.spots = new Spots(this.program, var1);
      this.rain = new RainMap(this.program, var1);
      GL30.glBindFramebuffer(36160, var5);
      GL11.glBindTexture(3553, var4);
   }

   boolean complete() {
      int var1 = GL11.glGetInteger(36006);
      GL30.glBindFramebuffer(36160, this.fbos[0]);
      int var2 = GL30.glCheckFramebufferStatus(36160);
      GL30.glBindFramebuffer(36160, var1);
      if (var2 != 36053) {
         System.out.println("[Viewpoint] shadow framebuffer incomplete: 0x" + Integer.toHexString(var2));
         return false;
      } else {
         return true;
      }
   }

   static float cascadeRadius(int var0, SceneData var1) {
      return var0 == 0 ? 22.0F : (var0 == 1 ? 64.0F : var1.shadowReach);
   }

   static float farShadowRadius(float var0) {
      return Math.max(96.0F, (float)Math.ceil((var0 + 12.0F) / 16.0F) * 16.0F);
   }

   void draw(FrameContext var1) {
      Pipeline var2 = var1.pipeline;
      if (var1.sun && var2.shadows) {
         GlDebug.push("sun shadows");
         this.resize(var2.shadowResolution);
         this.drawCascades(var1.scene, var2.shadowCascades);
         GlDebug.pop();
      } else if (!var2.shadows) {
         this.layers.release();
      }

      this.drawLights(var1);
   }

   void drawLights(FrameContext var1) {
      this.spots.draw(var1);
      this.lamps.draw(var1);
      this.muzzle.draw(var1);
      this.rain.draw(var1.scene);
   }

   private void resize(int var1) {
      if (var1 != this.size) {
         int var2 = GL11.glGetInteger(32873);
         int var3 = GL11.glGetInteger(36006);
         GL30.glDeleteFramebuffers(this.fbos);
         GL11.glDeleteTextures(this.textures);
         this.makeCascades(var1);
         GL30.glBindFramebuffer(36160, var3);
         GL11.glBindTexture(3553, var2);
      }
   }

   private void makeCascades(int var1) {
      this.size = var1;
      Arrays.fill(this.valid, false);

      for (int var2 = 0; var2 < 3; var2++) {
         this.textures[var2] = depthTexture(this.size);
         this.fbos[var2] = depthFbo(this.textures[var2]);
      }
   }

   private void drawCascades(SceneData var1, int var2) {
      this.sunDistance = var1.shadowReach + 90.0F;
      boolean var3 = Math.abs(var1.sunY) > 0.99F;
      this.lightView
         .setLookAt(
            var1.sunX * this.sunDistance,
            var1.sunY * this.sunDistance,
            var1.sunZ * this.sunDistance,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            var3 ? 0.0F : 1.0F,
            var3 ? 1.0F : 0.0F
         );
      if (var1.shadowReach != this.drawnReach || var2 != this.drawnCount) {
         Arrays.fill(this.valid, false);
         this.drawnCount = var2;
         this.drawnReach = var1.shadowReach;
      }

      GL11.glEnable(2929);
      GL11.glDepthFunc(513);
      GL11.glEnable(32823);
      GL11.glPolygonOffset(2.0F, 4.0F);
      this.program.use();
      casters(this.program);
      this.program.set("uEye", var1.sunX * 100000.0F, var1.sunY * 100000.0F, var1.sunZ * 100000.0F);
      boolean var4 = this.frameCount % 2L == 0L;
      boolean var5 = this.frameCount % 4L == 1L && var2 > 2 && this.farStale(var1);
      this.frameCount++;
      this.layers.frame(var1, var2);
      byte[] var6 = new byte[]{2, 16, 8};

      for (int var7 = 0; var7 < var2; var7++) {
         if (var7 == 0 || !this.valid[var7] || var7 == 1 && var4 || var7 == 2 && var5) {
            GpuParts.begin(3 + var7);
            this.cascade(var1, var7, var6[var7]);
            GpuParts.end(3 + var7);
            if (var7 == 2) {
               this.farCasters = farCascadeCasters(var1);
               this.farSunX = var1.sunX;
               this.farSunY = var1.sunY;
               this.farSunZ = var1.sunZ;
            }

            this.valid[var7] = true;
            this.origin[var7 * 3] = var1.originX;
            this.origin[var7 * 3 + 1] = originY(var1);
            this.origin[var7 * 3 + 2] = var1.originZ;
         }

         this.matrix[var7]
            .set(this.drawn[var7])
            .translate(
               (float)(var1.originX - this.origin[var7 * 3]),
               (float)(originY(var1) - this.origin[var7 * 3 + 1]),
               (float)(var1.originZ - this.origin[var7 * 3 + 2])
            );
      }

      GL11.glDisable(32823);
   }

   private boolean farStale(SceneData var1) {
      double var2 = var1.originX - this.origin[6];
      double var4 = originY(var1) - this.origin[7];
      double var6 = var1.originZ - this.origin[8];
      return var1.sunX != this.farSunX
         || var1.sunY != this.farSunY
         || var1.sunZ != this.farSunZ
         || farCascadeCasters(var1) != this.farCasters
         || var2 * var2 + var4 * var4 + var6 * var6 > 64.0;
   }

   boolean quiet(FrameContext var1) {
      Pipeline var2 = var1.pipeline;
      if (var1.sun && var2.shadows) {
         int var3 = var2.shadowCascades;
         boolean var4 = var2.shadowResolution != this.size || var1.scene.shadowReach != this.drawnReach || var3 != this.drawnCount;

         for (int var5 = 1; var5 < var3; var5++) {
            var4 |= !this.valid[var5];
         }

         boolean var7 = var3 > 1 && this.frameCount % 2L == 0L;
         boolean var6 = var3 > 2 && this.frameCount % 4L == 1L;
         return !var4 && !var7 && !var6;
      } else {
         return true;
      }
   }

   private static long farCascadeCasters(SceneData var0) {
      return casters(var0, (byte)8) * 31L + FarShadow.farSum(var0);
   }

   static double originY(SceneData var0) {
      return var0.originY + FarRelief.sink(var0);
   }

   static long casters(SceneData var0, byte var1) {
      long var2 = 0L;

      for (int var4 = 0; var4 < var0.meshCount; var4++) {
         if ((var0.meshFlags[var4] & var1) != 0) {
            long var5 = System.identityHashCode(var0.meshes[var4]) * -7046029254386353131L
               + Float.floatToIntBits(var0.meshFade[var4 * 2]) * 31L
               + Float.floatToIntBits(var0.meshFade[var4 * 2 + 1]);
            var2 += var5 ^ var5 >>> 29;
         }
      }

      return var2;
   }

   private void cascade(SceneData var1, int var2, byte var3) {
      float var4 = cascadeRadius(var2, var1);
      this.reach[var2] = var4;
      if (var2 < 2) {
         this.target.texture = this.textures[var2];
         this.target.fbo = this.fbos[var2];
         this.target.size = this.size;
         this.target.sunDistance = this.sunDistance;
         this.target.lightView = this.lightView;
         this.layers.draw(var1, var2, var3, var4, this.target, this.viewProjection[var2]);
         this.drawn[var2].translation(0.5F, 0.5F, 0.5F).scale(0.5F).mul(this.viewProjection[var2]);
      } else {
         double var5 = 2.0 * var4 / this.size;
         double var7 = originY(var1);
         double var9 = this.lightView.m00() * var1.originX + this.lightView.m10() * var7 + this.lightView.m20() * var1.originZ;
         double var11 = this.lightView.m01() * var1.originX + this.lightView.m11() * var7 + this.lightView.m21() * var1.originZ;
         float var13 = (float)(var9 - Math.floor(var9 / var5) * var5);
         float var14 = (float)(var11 - Math.floor(var11 / var5) * var5);
         Matrix4f var15 = this.viewProjection[var2];
         var15.setOrtho(-var4 - var13, var4 - var13, -var4 - var14, var4 - var14, 1.0F, this.sunDistance * 2.0F).mul(this.lightView);
         this.drawn[var2].translation(0.5F, 0.5F, 0.5F).scale(0.5F).mul(var15);
         GL30.glBindFramebuffer(36160, this.fbos[var2]);
         GL11.glViewport(0, 0, this.size, this.size);
         GL11.glClear(256);
         this.program.set("uViewProjection", var15);
         Meshes.draw(var1, this.program, var3);
         this.farMaps.castInto(var1, var15, var2, 0.0F);
         this.program.use();
      }
   }

   void bind(boolean var1) {
      this.weatherMap.bind(var1);

      for (int var2 = 0; var2 < 3; var2++) {
         Gl.bind(Gl.UNIT_CASCADE[var2], var1 ? this.textures[var2] : 0);
      }

      this.depthViews.bind(this.textures, var1);
      this.farMaps.bind(var1);
      this.spots.bind(var1);
      this.lamps.bind(var1);
      this.muzzle.bind(var1);
   }

   static void samplers(GlProgram var0) {
      for (int var1 = 0; var1 < 5; var1++) {
         var0.sampler("uShadowMap" + var1, Gl.UNIT_CASCADE[var1]);
      }

      WeatherMap.samplers(var0);
   }

   static void casters(GlProgram var0) {
      var0.set("uAlphaRange", 0.3F, 2.0F);
      var0.setInt("uAlphaHash", 1);
      var0.set("uFadeNoise", 0.0F);
   }

   void sunUniforms(GlProgram var1, FrameContext var2) {
      SceneData var3 = var2.scene;
      this.matrices.clear();
      this.mapParams.clear();
      int var4 = var2.pipeline.shadowCascades;
      float var5 = this.farMaps.cascadeReach(var3);

      for (int var6 = 0; var6 < 3; var6++) {
         this.matrix[var6].get(var6 * 16, this.matrices);
         float var7 = var6 < var4 ? 2.0F * this.reach[var6] / this.size : 0.0F;
         this.mapParams.put(var7).put(this.sunDistance * 2.0F - 1.0F).put(1.0F / this.size).put(var5);
      }

      for (int var8 = 0; var8 < 2; var8++) {
         FarShadowMap var9 = this.farMaps.map(var8);
         var9.textureMatrix(this.farMatrix).get((3 + var8) * 16, this.matrices);
         this.mapParams.put(var9.texelSquares()).put(var9.depth).put(var9.texelStep()).put(0.0F);
      }

      this.mapParams.flip();
      var1.setMatrices("uShadowMatrix", this.matrices);
      var1.setVec4s("uShadowMaps", this.mapParams);
      var1.set("uSunDir", var3.sunX, var3.sunY, var3.sunZ);
      var1.set("uSunColor", var3.sunR, var3.sunG, var3.sunB);
      var1.set("uSunStrength", var2.sun ? var3.sunStrength : 0.0F);
      var1.set("uShadowsOff", WorldRenderer.off(5) ? 1.0F : 0.0F);
      var1.set("uShadowPhase", var2.noiseIndex());
      SkyPass.cloudUniforms(var1, var3);
      SkyPass.weatherUniforms(var1, var3);
      this.weatherMap.cloudShadow(var1, var3);
   }

   static int depthTexture(int var0) {
      int var1 = Gl.texture(33190, var0, var0, 6402, 5126, 9729);
      GL11.glTexParameteri(3553, 34892, 34894);
      GL11.glTexParameteri(3553, 34893, 515);
      return var1;
   }

   static int depthFbo(int var0) {
      int var1 = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, var1);
      GL30.glFramebufferTexture2D(36160, 36096, 3553, var0, 0);
      GL11.glDrawBuffer(0);
      GL11.glReadBuffer(0);
      return var1;
   }
}
