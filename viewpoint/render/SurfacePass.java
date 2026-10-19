package viewpoint.render;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import zombie.core.textures.Texture;

final class SurfacePass {
   static final float OPAQUE_ALPHA = 0.85F;
   static final float TRANSLUCENT_ALPHA = 0.04F;
   private static final double GLASS_GRID = 64.0;
   private static final FloatBuffer lightData = BufferUtils.createFloatBuffer(192);
   private static final FloatBuffer blobData = BufferUtils.createFloatBuffer(128);
   private final GlProgram gbuffer = GlProgram.create("gbuffer", "mesh.vert", "gbuffer.frag");
   private final GlProgram light = GlProgram.create("light", "screen.vert", "light.frag");
   private final GlProgram forward = GlProgram.create("forward", "mesh.vert", "forward.frag");
   private final BloodPass blood = new BloodPass(false);
   private final Targets targets;
   private final ShadowPass shadows;

   SurfacePass(Targets var1, ShadowPass var2) {
      this.targets = var1;
      this.shadows = var2;
      this.gbuffer.sampler("uTexture", 0);
      this.gbuffer.sampler("uLight", 5);
      this.forward.sampler("uTexture", 0);
      this.forward.sampler("uLight", 5);
      ShadowPass.samplers(this.forward);
      Spots.sampler(this.forward);
      LampShadows.sampler(this.forward);
      MuzzleShadow.sampler(this.forward);
      this.light.sampler("uAlbedo", 9);
      this.light.sampler("uNormal", 10);
      this.light.sampler("uEngineLight", 11);
      this.light.sampler("uDepth", 12);
      ShadowPass.samplers(this.light);
      Spots.sampler(this.light);
      LampShadows.sampler(this.light);
      MuzzleShadow.sampler(this.light);
   }

   void gbuffer(FrameContext var1) {
      GL30.glBindFramebuffer(36160, this.targets.gbufferFbo);
      GL11.glViewport(0, 0, var1.width, var1.height);
      GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      GL11.glClear(17664);
      GL11.glEnable(2929);
      GL11.glDepthFunc(513);
      this.gbuffer.use();
      this.gbuffer.set("uViewProjection", var1.viewProjection);
      this.gbuffer.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
      this.gbuffer.set("uAlphaRange", 0.85F, 2.0F);
      this.gbuffer.setInt("uAlphaHash", var1.plantsHashed() ? 1 : 0);
      this.gbuffer.set("uAlphaHashShift", var1.alphaHashShift());
      this.gbuffer.set("uFadeNoise", var1.fadeNoise());
      CameraClear.apply(this.gbuffer);
      this.gbuffer.set("uSkyLevel", var1.scene.skyR, var1.scene.skyG, var1.scene.skyB);
      glass(this.gbuffer, var1.scene);
      GL11.glEnable(2960);
      GL11.glStencilFunc(519, 1, 255);
      GL11.glStencilOp(7680, 7680, 7681);
      Meshes.drawPlanned(var1.scene, this.gbuffer, (byte)1, true);
      GL11.glDisable(2960);
      this.blood.draw(var1);
   }

   void light(FrameContext var1) {
      SceneData var2 = var1.scene;
      this.light.use();
      var1.camera(this.light);
      this.shadows.sunUniforms(this.light, var1);
      this.shadows.spots.uniforms(this.light);
      this.light.set("uFogColor", var2.fogR, var2.fogG, var2.fogB);
      this.light.setInt("uDebugView", WorldRenderer.debugView);
      if (this.light.has("uReflection")) {
         this.light.setInt("uReflection", 119);
         this.light.setInt("uReflectionDepth", 120);
      }

      if (this.light.has("uBedColours")) {
         this.light.setInt("uBedColours", 129);
         this.light.set("uBedColoursOn", WorldRenderer.indirect().bounce().bedColours() ? 1.0F : 0.0F);
      }

      lamps(this.light, var2);
      this.shadows.lamps.uniforms(this.light, var2);
      torches(this.light, var2);
      this.shadows.muzzle.uniforms(this.light);
      blobs(this.light, var2);
      Gl.bind(9, this.targets.albedo);
      Gl.bind(10, this.targets.normal);
      Gl.bind(11, this.targets.engineLight);
      Gl.bind(12, this.targets.depth);
      this.shadows.bind(true);
      GL13.glActiveTexture(33984);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      Gl.screenTriangle();
      Gl.bind(9, 0);
      Gl.bind(10, 0);
      Gl.bind(11, 0);
      Gl.bind(12, 0);
      this.shadows.bind(false);
      GL13.glActiveTexture(33984);
      GL11.glDepthMask(true);
      GL11.glEnable(2929);
   }

   void translucent(FrameContext var1) {
      SceneData var2 = var1.scene;
      GL11.glEnable(2929);
      GL11.glDepthFunc(515);
      GL11.glDepthMask(false);
      GL11.glDisable(2884);
      GL11.glDisable(3008);
      GL11.glDisable(3089);
      this.forward.use();
      this.forward.set("uViewProjection", var1.viewProjection);
      this.forward.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
      this.shadows.sunUniforms(this.forward, var1);
      lamps(this.forward, var2);
      this.shadows.lamps.uniforms(this.forward, var2);
      torches(this.forward, var2);
      this.shadows.muzzle.uniforms(this.forward);
      this.shadows.spots.uniforms(this.forward);
      this.forward.set("uFogColor", var2.fogR, var2.fogG, var2.fogB);
      this.forward.set("uFogRange", 1000000.0F, 2000000.0F);
      this.forward.setInt("uDebugView", WorldRenderer.debugView);
      this.forward.set("uAlphaRange", 0.04F, 0.85F);
      this.forward.set("uFadeNoise", var1.fadeNoise());
      this.forward.set("uSkyLevel", var2.skyR, var2.skyG, var2.skyB);
      glass(this.forward, var2);
      this.shadows.bind(true);
      GL13.glActiveTexture(33984);
      Texture.lastTextureID = -1;
      boolean var3 = var1.plantsHashed();
      this.forward.setInt("uNoFoliage", var3 ? 1 : 0);
      Meshes.drawPlanned(var2, this.forward, (byte)4, !var3);
      this.forward.setInt("uNoFoliage", 0);
      this.shadows.bind(false);
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, 0);
      Texture.lastTextureID = -1;
      GL11.glDepthMask(true);
      GL11.glDepthFunc(513);
   }

   static void lamps(GlProgram var0, SceneData var1) {
      int var2 = WorldRenderer.off(3) ? 0 : Math.min(var1.lightCount, 48);
      var0.setInt("uLightCount", var2);
      if (var2 != 0) {
         lightData.clear();

         for (int var3 = 0; var3 < var2; var3++) {
            int var4 = var3 * 12;
            lightData.put(var1.lights[var4]).put(var1.lights[var4 + 1]).put(var1.lights[var4 + 2]).put(var1.lights[var4 + 3]);
         }

         lightData.flip();
         var0.setVec4s("uLightPos", lightData);
         perLight(var0, var1, var2, "uLightOutdoors", 8);
         perLight(var0, var1, var2, "uLightRise", 10);
         perLight(var0, var1, var2, "uLightLevel", 11);
         if (var0.has("uLightColor")) {
            lightData.clear();

            for (int var6 = 0; var6 < var2; var6++) {
               int var7 = var6 * 12;
               float var5 = var1.lights[var7 + 7];
               lightData.put(var1.lights[var7 + 4] * var5).put(var1.lights[var7 + 5] * var5).put(var1.lights[var7 + 6] * var5);
            }

            lightData.flip();
            var0.setVec3s("uLightColor", lightData);
         }
      }
   }

   private static void perLight(GlProgram var0, SceneData var1, int var2, String var3, int var4) {
      if (var0.has(var3)) {
         lightData.clear();

         for (int var5 = 0; var5 < var2; var5++) {
            lightData.put(var1.lights[var5 * 12 + var4]);
         }

         lightData.flip();
         var0.setFloats(var3, lightData);
      }
   }

   static void glass(GlProgram var0, SceneData var1) {
      var0.setInt("uGlassFar", var1.glassFar ? 1 : 0);
      var0.set("uGlassRange", var1.glassStart, var1.glassOpaque, var1.glassStoreys, 0.04F);
      var0.set("uGlassEye", var1.viewerX, var1.viewerY, var1.viewerZ);
      var0.set("uGlassOrigin", Gl.wrap(var1.originX, 64.0), Gl.wrap(var1.originY, 156.76733F), Gl.wrap(var1.originZ, 64.0));
   }

   private static void blobs(GlProgram var0, SceneData var1) {
      if (var0.has("uBlobCount")) {
         var0.setInt("uBlobCount", var1.blobCount);
         blobData.clear();
         blobData.put(var1.blobs, 0, var1.blobCount * 4).flip();
         if (var1.blobCount > 0) {
            var0.setVec4s("uBlobs", blobData);
         }
      }
   }

   static void torches(GlProgram var0, SceneData var1) {
      int var2 = Math.min(var1.torchCount, 8);
      var0.setInt("uTorchCount", var2);
      if (var2 != 0) {
         for (int var3 = 0; var3 < 2; var3++) {
            lightData.clear();

            for (int var4 = 0; var4 < var2; var4++) {
               int var5 = var4 * 11 + var3 * 4;
               lightData.put(var1.torches[var5]).put(var1.torches[var5 + 1]).put(var1.torches[var5 + 2]).put(var1.torches[var5 + 3]);
            }

            lightData.flip();
            var0.setVec4s(var3 == 0 ? "uTorchPos" : "uTorchDir", lightData);
         }

         lightData.clear();

         for (int var6 = 0; var6 < var2; var6++) {
            int var7 = var6 * 11 + 8;
            lightData.put(var1.torches[var7]).put(var1.torches[var7 + 1]).put(var1.torches[var7 + 2]);
         }

         lightData.flip();
         var0.setVec3s("uTorchColor", lightData);
      }
   }
}
