package viewpoint.render;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;
import viewpoint.iris.IdTable;
import viewpoint.iris.IrisMacros;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;

final class IrisGeometry {
   private static final String[][] MATERIALS = new String[][]{
      new String[0],
      {"water", "flowing_water"},
      {"glass_pane", "glass", "white_stained_glass_pane"},
      {"short_grass", "grass", "tall_grass", "fern"},
      {"oak_leaves", "leaves", "birch_leaves"},
      new String[0]
   };
   private static final float FLASH_LEVEL = 1.0F;
   private final Matrix4f matrix = new Matrix4f();
   private final Matrix3f normal = new Matrix3f();
   private final float[] blockIds = new float[16];
   private final IrisSky sky = new IrisSky();
   private final IrisWeather weather = new IrisWeather();
   private final IrisLightning lightning = new IrisLightning();

   void init() {
      this.sky.init();
      this.weather.init();
      this.lightning.init();
   }

   void blocks(IdTable var1) {
      Arrays.fill(this.blockIds, -1.0F);

      for (int var2 = 0; var2 < MATERIALS.length; var2++) {
         this.blockIds[var2] = MATERIALS[var2].length == 0 ? -1.0F : var1.id(MATERIALS[var2]);
      }
   }

   void sky(IrisPipeline var1, Map<String, float[]> var2, FrameContext var3) {
      this.sky.update(var1.uniforms.sun.sun, var1.uniforms.sun.moon);
      int var4 = GL11.glGetInteger(32873);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      GL11.glEnable(34383);
      this.sky.bind();
      IrisProgram var5 = var1.programs.draws.get(IrisPrograms.Draw.SKY);
      if (var5 != null) {
         this.begin(var1, var5, var2, var1.uniforms.modelView, var1.uniforms.projection, "SKY", var3, false);
         GL13.glActiveTexture(33984);
         GL11.glBindTexture(3553, var1.textures.white.id());
         this.sky.drawDome();
         GL11.glEnable(3042);
         GL14.glBlendFuncSeparate(770, 1, 1, 1);
         var5.set("renderStage", IrisMacros.renderStage("STARS"));
         this.sky.drawStars();
         GL11.glDisable(3042);
         var1.bindings.unbind(var5);
      }

      IrisProgram var6 = var1.programs.draws.get(IrisPrograms.Draw.SKY_TEXTURED);
      if (var6 != null) {
         this.begin(var1, var6, var2, var1.uniforms.modelView, var1.uniforms.projection, "SUN", var3, false);
         GL11.glEnable(3042);
         GL14.glBlendFuncSeparate(770, 1, 1, 0);
         GL13.glActiveTexture(33984);
         this.sky.drawSun();
         var6.set("renderStage", IrisMacros.renderStage("MOON"));
         this.sky.drawMoon();
         GL11.glDisable(3042);
         var1.bindings.unbind(var6);
      }

      this.sky.unbind();
      GL11.glDisable(34383);
      GL11.glBindTexture(3553, var4);
      GL11.glDepthMask(true);
      GL11.glEnable(2929);
   }

   void surfaces(IrisPipeline var1, Map<String, float[]> var2, FrameContext var3, ShadowPass var4, boolean var5) {
      IrisProgram var6 = var1.programs.draws.get(var5 ? IrisPrograms.Draw.WATER : IrisPrograms.Draw.TERRAIN);
      if (var6 != null) {
         SceneData var7 = var3.scene;
         this.begin(var1, var6, var2, var1.uniforms.modelView, var1.uniforms.projection, var5 ? "TERRAIN_TRANSLUCENT" : "TERRAIN_SOLID", var3, false);
         blend(var1, var6, var5);
         this.viewpoint(var6, var3, var4, var3.eyeX, var3.eyeY, var3.eyeZ, false, var5);
         GL11.glEnable(2929);
         GL11.glDepthFunc(515);
         GL11.glDepthMask(true);
         var4.bind(true);
         Gl.patches(true);
         Meshes.drawPlanned(var7, var6.gl, (byte)(var5 ? 4 : 1), true);
         Gl.patches(false);
         var4.bind(false);
         GL11.glDisable(3042);
         var1.bindings.unbind(var6);
      }
   }

   void weather(IrisPipeline var1, Map<String, float[]> var2, FrameContext var3, ShadowPass var4) {
      IrisProgram var5 = var1.programs.draws.get(IrisPrograms.Draw.WEATHER);
      float[] var6 = (float[])var2.get("frameTimeCounter");
      if (var5 != null && this.weather.update(var3.scene, var3.eyeX, var3.eyeZ, var6 == null ? 0.0 : var6[0])) {
         this.begin(var1, var5, var2, var1.uniforms.modelView, var1.uniforms.projection, "RAIN_SNOW", var3, false);
         blend(var1, var5, true);
         var5.gl.set("uIrisEye", var3.eyeX, var3.eyeY, var3.eyeZ);
         var4.rain.uniforms(var5.gl, true);
         GL11.glEnable(2929);
         GL11.glDepthFunc(515);
         GL11.glDepthMask(false);
         GL11.glDisable(2884);
         this.weather.draw();
         GL11.glDepthMask(true);
         var4.rain.uniforms(var5.gl, false);
         GL11.glDisable(3042);
         var1.bindings.unbind(var5);
      }
   }

   void lightning(IrisPipeline var1, Map<String, float[]> var2, FrameContext var3) {
      IrisProgram var4 = var1.programs.draws.get(IrisPrograms.Draw.LIGHTNING);
      if (var4 != null && this.lightning.update(var3.scene, var3.eyeX, var3.eyeY, var3.eyeZ)) {
         this.begin(var1, var4, var2, var1.uniforms.modelView, var1.uniforms.projection, "NONE", var3, false);
         var4.set("uIrisLevels", 15.0F, 15.0F);
         if (var1.configured.properties().blend.containsKey(var4.name)) {
            blend(var1, var4, true);
         } else {
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 1);
         }

         GL11.glEnable(2929);
         GL11.glDepthFunc(515);
         GL11.glDepthMask(false);
         GL11.glDisable(2884);
         GL13.glActiveTexture(33984);
         int var5 = GL11.glGetInteger(32873);
         GL11.glBindTexture(3553, var1.textures.white.id());
         this.lightning.draw();
         GL11.glBindTexture(3553, var5);
         GL11.glDepthMask(true);
         GL11.glDisable(3042);
         var1.bindings.unbind(var4);
      }
   }

   void models(IrisPipeline var1, Map<String, float[]> var2, FrameContext var3, ShadowPass var4, ModelPass var5) {
      IrisProgram var6 = var1.programs.draws.get(IrisPrograms.Draw.ENTITIES);
      IrisProgram var7 = var1.programs.draws.get(IrisPrograms.Draw.VEHICLES);
      if (var6 != null && var7 != null) {
         this.entities(var1, var2, var3, var4, var6, false, () -> var5.iris(var6.gl, var3.scene, var3.viewProjection, 0.0F, false));
         IrisProgram var8 = var1.programs.draws.get(IrisPrograms.Draw.BLOCKS);
         if (var8 != null) {
            this.entities(var1, var2, var3, var4, var8, false, () -> var5.iris(var8.gl, var3.scene, var3.viewProjection, 0.0F, true));
         }

         this.entities(var1, var2, var3, var4, var7, false, () -> var5.irisVehicles(var7.gl, var3.scene, false));
      }
   }

   void vehicleGlass(IrisPipeline var1, Map<String, float[]> var2, FrameContext var3, ShadowPass var4, ModelPass var5) {
      IrisProgram var6 = var1.programs.draws.get(IrisPrograms.Draw.VEHICLE_GLASS);
      if (var6 != null) {
         this.entities(var1, var2, var3, var4, var6, true, () -> var5.irisVehicles(var6.gl, var3.scene, true));
      }
   }

   void doorGlass(IrisPipeline var1, Map<String, float[]> var2, FrameContext var3, ShadowPass var4, ModelPass var5) {
      IrisProgram var6 = var1.programs.draws.get(IrisPrograms.Draw.BLOCK_GLASS);
      if (var6 != null) {
         this.entities(var1, var2, var3, var4, var6, true, () -> var5.irisGlass(var6.gl, var3.scene, var3.viewProjection));
      }
   }

   private void entities(IrisPipeline var1, Map<String, float[]> var2, FrameContext var3, ShadowPass var4, IrisProgram var5, boolean var6, Runnable var7) {
      this.begin(var1, var5, var2, var1.uniforms.modelView, var1.uniforms.projection, "ENTITIES", var3, false);
      blend(var1, var5, var6);
      this.viewpoint(var5, var3, var4, var3.eyeX, var3.eyeY, var3.eyeZ, false, var6);
      var5.gl.setInt("uIrisEntity", -1);
      CameraClear.apply(var5.gl);
      GL11.glEnable(2929);
      GL11.glDepthFunc(515);
      GL11.glDepthMask(!var6);
      var4.bind(true);
      var7.run();
      var4.bind(false);
      GL11.glDepthMask(true);
      GL11.glDisable(3042);
      var1.bindings.unbind(var5);
   }

   void shadow(IrisPipeline var1, Map<String, float[]> var2, FrameContext var3, ShadowPass var4, ModelPass var5) {
      IrisProgram var6 = var1.programs.draws.get(IrisPrograms.Draw.SHADOW);
      double[] var7 = var1.uniforms.sun.sunLights ? var1.uniforms.sun.sun : var1.uniforms.sun.moon;
      float var8 = var3.eyeX - (float)var7[0] * 1000.0F;
      float var9 = var3.eyeY + (float)var7[1] * 1000.0F;
      float var10 = var3.eyeZ - (float)var7[2] * 1000.0F;
      this.begin(var1, var6, var2, var1.uniforms.shadowView, var1.uniforms.shadowProjection, "NONE", var3, true);
      this.viewpoint(var6, var3, var4, var8, var9, var10, true, false);
      GL11.glEnable(2929);
      GL11.glDepthFunc(515);
      GL11.glDepthMask(true);
      GL11.glDisable(2884);
      Gl.patches(true);
      Meshes.draw(var3.scene, var6.gl, (byte)27);
      Gl.patches(false);
      var1.bindings.unbind(var6);
      IrisProgram var11 = var1.programs.draws.get(IrisPrograms.Draw.SHADOW_MODELS);
      if (var11 != null) {
         this.begin(var1, var11, var2, var1.uniforms.shadowView, var1.uniforms.shadowProjection, "ENTITIES", var3, true);
         this.viewpoint(var11, var3, var4, var8, var9, var10, true, false);
         var11.gl.setInt("uIrisEntity", -1);
         var5.iris(var11.gl, var3.scene, var3.viewProjection, var1.shadowDistance, false);
         var1.bindings.unbind(var11);
      }
   }

   void begin(IrisPipeline var1, IrisProgram var2, Map<String, float[]> var3, Matrix4f var4, Matrix4f var5, String var6, FrameContext var7, boolean var8) {
      var2.use();
      var2.set(var3);
      this.matrices(var2, var4, var5);
      var2.set("renderStage", IrisMacros.renderStage(var6));
      var2.set("uIrisLevels", 0.0F, 15.0F);
      var1.bindings.bind(var2, var8 ? "shadow" : "gbuffers", Set.of());
   }

   private void matrices(IrisProgram var1, Matrix4f var2, Matrix4f var3) {
      float[] var4 = new float[16];
      float[] var5 = new float[16];
      float[] var6 = new float[16];
      float[] var7 = new float[16];
      var2.get(var4);
      var3.get(var5);
      this.matrix.set(var3).mul(var2).get(var6);
      float[] var8 = new float[48];
      new Matrix4f().get(var8, 0);
      new Matrix4f().scale(0.00390625F).translate(8.0F, 8.0F, 0.0F).get(var8, 16);
      new Matrix4f().get(var8, 32);
      var2.normal(this.normal);
      float[] var9 = new float[9];
      this.normal.get(var9);

      for (String var13 : new String[]{"vp_", ""}) {
         var1.set(var13 + (var13.isEmpty() ? "modelViewMatrix" : "ModelViewMatrix"), var4);
         var1.set(var13 + (var13.isEmpty() ? "projectionMatrix" : "ProjectionMatrix"), var5);
         var1.set(var13.isEmpty() ? "normalMatrix" : "vp_NormalMatrix", var9);
         var1.set(var13.isEmpty() ? "textureMatrix" : "vp_TextureMatrix", var8);
      }

      var1.set("vp_ModelViewProjectionMatrix", var6);
      new Matrix4f(var2).invert().get(var7);
      var1.set("vp_ModelViewMatrixInverse", var7);
      var1.set("modelViewMatrixInverse", var7);
      new Matrix4f(var3).invert().get(var7);
      var1.set("vp_ProjectionMatrixInverse", var7);
      var1.set("projectionMatrixInverse", var7);
   }

   private void viewpoint(IrisProgram var1, FrameContext var2, ShadowPass var3, float var4, float var5, float var6, boolean var7, boolean var8) {
      SceneData var9 = var2.scene;
      GlProgram var10 = var1.gl;
      var10.set("uEye", var4, var5, var6);
      var10.set("uIrisEye", var2.eyeX, var2.eyeY, var2.eyeZ);
      var10.setInt("uIrisShadow", var7 ? 1 : 0);
      var10.setInt("uIrisTranslucent", var8 ? 1 : 0);
      var1.set("uBlockIds", this.blockIds);
      var10.set("uIrisLampColours", 1.0F);
      var10.set("uIrisFlashLevel", 1.0F);
      var10.set("uAlphaRange", var8 ? 0.04F : 0.85F, var8 ? 0.85F : 2.0F);
      var10.set("uFadeNoise", 0.0F);
      var10.set("uSkyLevel", var9.skyR, var9.skyG, var9.skyB);
      SurfacePass.glass(var10, var9);
      if (!var7) {
         SurfacePass.lamps(var10, var9);
         var3.lamps.uniforms(var10, var9);
         SurfacePass.torches(var10, var9);
         var3.spots.uniforms(var10);
      }
   }

   static void blend(IrisPipeline var0, IrisProgram var1, boolean var2) {
      Map var3 = var0.configured.properties().blend;
      String var4 = (String)var3.get(var1.name);
      if (var4 == null ? var2 : !var4.strip().equalsIgnoreCase("off")) {
         GL11.glEnable(3042);
         int[] var5 = var4 == null ? new int[]{770, 771, 1, 771} : factors(var4);
         GL14.glBlendFuncSeparate(var5[0], var5[1], var5[2], var5[3]);
      } else {
         GL11.glDisable(3042);
      }

      List var9 = var1.drawBuffers;

      for (int var6 = 0; var6 < var9.size(); var6++) {
         String var7 = (String)var3.get(var1.name + ".colortex" + var9.get(var6));
         if (var7 != null) {
            if (var7.strip().equalsIgnoreCase("off")) {
               GL30.glDisablei(3042, var6);
            } else {
               GL30.glEnablei(3042, var6);
               int[] var8 = factors(var7);
               GL40.glBlendFuncSeparatei(var6, var8[0], var8[1], var8[2], var8[3]);
            }
         }
      }

      if (var1.tintOutput >= 0) {
         GL30.glDisablei(3042, var1.tintOutput);
      }
   }

   private static int[] factors(String var0) {
      String[] var1 = var0.strip().split("\\s+");
      int[] var2 = new int[4];

      for (int var3 = 0; var3 < 4; var3++) {
         var2[var3] = factor(var1[Math.min(var3, var1.length - 1)]);
      }

      return var2;
   }

   private static int factor(String var0) {
      return switch (var0.toUpperCase(Locale.ROOT)) {
         case "ZERO" -> 0;
         case "ONE" -> 1;
         case "SRC_COLOR" -> 768;
         case "ONE_MINUS_SRC_COLOR" -> 769;
         case "DST_COLOR" -> 774;
         case "ONE_MINUS_DST_COLOR" -> 775;
         case "SRC_ALPHA" -> 770;
         case "ONE_MINUS_SRC_ALPHA" -> 771;
         case "DST_ALPHA" -> 772;
         case "ONE_MINUS_DST_ALPHA" -> 773;
         case "SRC_ALPHA_SATURATE" -> 776;
         default -> 1;
      };
   }

   void release() {
      this.sky.release();
      this.weather.release();
      this.lightning.release();
   }

   static int lightUnit() {
      return 5;
   }
}
