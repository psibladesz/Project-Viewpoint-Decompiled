package viewpoint.render;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import viewpoint.platform.Gl;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.PackPass;
import viewpoint.platform.PackTarget;
import viewpoint.platform.ShaderPacks;

final class PackStages {
   private final Targets targets;
   private final WeatherMap weatherMap;
   private final Map<String, PackStages.Made> made = new HashMap<>();
   private int madeGeneration = -1;
   private int madeWidth;
   private int madeHeight;
   private int madeOutputWidth;
   private int madeOutputHeight;
   private int spare;
   private int spareFbo;
   private final int[] late = new int[2];
   private final int[] lateFbo = new int[2];
   private final Matrix4f matrix = new Matrix4f();

   PackStages(Targets var1, WeatherMap var2) {
      this.targets = var1;
      this.weatherMap = var2;
   }

   boolean any(PackPass.Stage var1) {
      for (PackPass var3 : GlProgram.pack().passes) {
         if (var3.stage == var1) {
            return true;
         }
      }

      return false;
   }

   int run(PackPass.Stage var1, FrameContext var2, int var3) {
      if (!this.any(var1)) {
         return this.passes(var1, var2, var3);
      }

      GlDebug.push("pack passes");
      GpuParts.begin(21 + var1.ordinal());
      int var4 = this.passes(var1, var2, var3);
      GpuParts.end(21 + var1.ordinal());
      GlDebug.pop();
      return var4;
   }

   private int passes(PackPass.Stage var1, FrameContext var2, int var3) {
      ShaderPacks.Active var4 = GlProgram.pack();
      this.prepare(var4, var2);
      if (!this.any(var1)) {
         return var3;
      }

      GL11.glDisable(2929);
      GL11.glDisable(3042);
      GL11.glDisable(2884);
      GL11.glDepthMask(false);
      this.weatherMap.bind(true);
      int var5 = var3;
      byte var6 = 0;

      for (PackPass var8 : var4.passes) {
         if (var8.stage == var1) {
            boolean var9 = !var8.target.equals("color");
            this.draw(var8, var2, var5, var9 ? null : var1, var6);
            if (!var9) {
               if (var1 == PackPass.Stage.FINAL) {
                  var5 = this.late[var6];
                  var6 ^= 1;
               } else {
                  GL43.glCopyImageSubData(this.spare, 3553, 0, 0, 0, 0, this.targets.hdr, 3553, 0, 0, 0, 0, var2.width, var2.height, 1);
               }
            }
         }
      }

      for (int var10 = 0; var10 < 12; var10++) {
         Gl.bind(52 + var10, 0);
      }

      this.weatherMap.bind(false);
      GL13.glActiveTexture(33984);
      GL11.glDepthMask(true);
      return var5;
   }

   private void draw(PackPass var1, FrameContext var2, int var3, PackPass.Stage var4, int var5) {
      int var6 = var2.width;
      int var7 = var2.height;
      if (var4 == null) {
         PackStages.Made var8 = this.made.get(var1.target);
         GL30.glBindFramebuffer(36160, var8.fbo[var8.current]);
         var6 = var8.width;
         var7 = var8.height;
      } else if (var4 == PackPass.Stage.FINAL) {
         GL30.glBindFramebuffer(36160, this.lateFbo[var5]);
         var6 = var2.outputWidth;
         var7 = var2.outputHeight;
      } else {
         GL30.glBindFramebuffer(36160, this.spareFbo);
      }

      GL11.glViewport(0, 0, var6, var7);
      GlProgram var11 = GlProgram.pass(var1.id);
      var11.use();

      for (int var9 = 0; var9 < var1.inputs.size(); var9++) {
         String var10 = var1.inputs.get(var9);
         Gl.bind(52 + var9, this.texture(var10, var3));
         var11.setInt(PackPass.sampler(var10), 52 + var9);
      }

      this.uniforms(var11, var2, var6, var7);
      GL13.glActiveTexture(33984);
      Gl.screenTriangle();
   }

   private int texture(String var1, int var2) {
      return switch (var1) {
         case "color" -> var2;
         case "depth" -> this.targets.depth;
         case "albedo" -> this.targets.albedo;
         case "normal" -> this.targets.normal;
         case "light" -> this.targets.engineLight;
         case "velocity" -> this.targets.velocity;
         case "farDepth" -> this.targets.farDepth;
         case "occlusion" -> this.targets.occlusion;
         case "volume" -> this.targets.volume;
         case "bloom" -> this.targets.bloom[0];
         default -> var1.endsWith(".previous")
            ? this.made.get(var1.substring(0, var1.length() - ".previous".length())).previous()
            : this.made.get(var1).texture[this.made.get(var1).current];
      };
   }

   private void uniforms(GlProgram var1, FrameContext var2, int var3, int var4) {
      SceneData var5 = var2.scene;
      vec(var1, "uResolution", var3, var4);
      vec(var1, "uTexel", 1.0F / var3, 1.0F / var4);
      this.mat(var1, "uView", var2.view);
      this.mat(var1, "uProjection", var2.jitteredProjection);
      this.mat(var1, "uViewProjection", var2.viewProjection);
      this.mat(var1, "uInvViewProjection", var2.invViewProjection);
      this.mat(var1, "uPlainViewProjection", var2.plainViewProjection);
      this.mat(var1, "uInvPlainViewProjection", var2.invPlainViewProjection);
      this.mat(var1, "uPrevious", var2.reprojection);
      if (var1.has("uCamera")) {
         float var6 = 1.0F / var5.projection.m00();
         float var7 = 1.0F / var5.projection.m11();
         var1.set("uCamera", 0.05F, 400.0F, var6, var7);
      }

      vec(var1, "uEye", var2.eyeX, var2.eyeY, var2.eyeZ);
      vec(var1, "uSunDir", var5.sunX, var5.sunY, var5.sunZ);
      vec(var1, "uSunColor", var5.sunR, var5.sunG, var5.sunB);
      vec(var1, "uSkySun", var5.skySunX, var5.skySunY, var5.skySunZ);
      vec(var1, "uFogColor", var5.fogR, var5.fogG, var5.fogB);
      vec(var1, "uFogRange", var5.fogStart, var5.fogEnd);
      vec(var1, "uFarRange", var2.farNear, var2.farFar);
      one(var1, "uSunStrength", var5.sunStrength);
      one(var1, "uSkyGlow", var5.skyGlow);
      one(var1, "uTime", var5.time);
      one(var1, "uFrameSeconds", var2.frameSeconds);
      one(var1, "uPreviousValid", var2.previousValid ? 1.0F : 0.0F);
      one(var1, "uExposure", EyeMeter.exposure(var2));
      SkyPass.cloudUniforms(var1, var5);
      SkyPass.weatherUniforms(var1, var5);
      if (var1.has("uFrame")) {
         var1.setInt("uFrame", (int)var2.index);
      }

      if (var1.has("uWeatherMap")) {
         var1.setInt("uWeatherMap", 14);
      }

      if (var1.has("uCloudNoise")) {
         var1.setInt("uCloudNoise", 18);
      }
   }

   private static void one(GlProgram var0, String var1, float var2) {
      if (var0.has(var1)) {
         var0.set(var1, var2);
      }
   }

   private static void vec(GlProgram var0, String var1, float var2, float var3) {
      if (var0.has(var1)) {
         var0.set(var1, var2, var3);
      }
   }

   private static void vec(GlProgram var0, String var1, float var2, float var3, float var4) {
      if (var0.has(var1)) {
         var0.set(var1, var2, var3, var4);
      }
   }

   private void mat(GlProgram var1, String var2, Matrix4f var3) {
      if (var1.has(var2)) {
         var1.set(var2, this.matrix.set(var3));
      }
   }

   private void prepare(ShaderPacks.Active var1, FrameContext var2) {
      if (var1.generation != this.madeGeneration
         || var2.width != this.madeWidth
         || var2.height != this.madeHeight
         || var2.outputWidth != this.madeOutputWidth
         || var2.outputHeight != this.madeOutputHeight) {
         this.release();
         this.madeGeneration = var1.generation;
         this.madeWidth = var2.width;
         this.madeHeight = var2.height;
         this.madeOutputWidth = var2.outputWidth;
         this.madeOutputHeight = var2.outputHeight;
         this.make(var1.passes, var1.pack.targets, var2);
      }

      for (PackStages.Made var4 : this.made.values()) {
         if (var4.history && var4.frame != var2.index) {
            var4.frame = var2.index;
            var4.current ^= 1;
         }
      }
   }

   private void make(List<PackPass> var1, List<PackTarget> var2, FrameContext var3) {
      int var4 = GL11.glGetInteger(32873);
      HashSet var5 = new HashSet();
      boolean var6 = false;
      boolean var7 = false;

      for (PackPass var9 : var1) {
         var5.add(var9.target);
         var9.inputs.forEach(var1x -> var5.add(var1x.endsWith(".previous") ? var1x.substring(0, var1x.length() - ".previous".length()) : var1x));
         boolean var10 = var9.target.equals("color");
         var6 |= var10 && var9.stage != PackPass.Stage.FINAL;
         var7 |= var10 && var9.stage == PackPass.Stage.FINAL;
      }

      for (PackTarget var13 : var2) {
         if (var5.contains(var13.name)) {
            this.made.put(var13.name, new PackStages.Made(var13, var3.width, var3.height));
         }
      }

      if (var6) {
         this.spare = hdrLike(var3.width, var3.height);
         this.spareFbo = framebuffer("pack spare", this.spare);
      }

      for (int var12 = 0; var7 && var12 < 2; var12++) {
         this.late[var12] = hdrLike(var3.outputWidth, var3.outputHeight);
         this.lateFbo[var12] = framebuffer("pack final " + var12, this.late[var12]);
      }

      GL11.glBindTexture(3553, var4);
   }

   private static int framebuffer(String var0, int var1) {
      int var2 = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, var2);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, var1, 0);
      int var3 = GL30.glCheckFramebufferStatus(36160);
      if (var3 != 36053) {
         throw new IllegalStateException(var0 + " framebuffer incomplete: 0x" + Integer.toHexString(var3));
      } else {
         return var2;
      }
   }

   private static int hdrLike(int var0, int var1) {
      return Gl.texture(34842, var0, var1, 6408, 5126, 9729);
   }

   void release() {
      this.made.values().forEach(PackStages.Made::release);
      this.made.clear();
      this.madeGeneration = -1;
      if (this.spare != 0) {
         GL30.glDeleteFramebuffers(this.spareFbo);
         GL11.glDeleteTextures(this.spare);
         this.spare = 0;
      }

      for (int var1 = 0; var1 < 2; var1++) {
         if (this.late[var1] != 0) {
            GL30.glDeleteFramebuffers(this.lateFbo[var1]);
            GL11.glDeleteTextures(this.late[var1]);
            this.late[var1] = 0;
         }
      }
   }

   private static final class Made {
      final int[] texture = new int[2];
      final int[] fbo = new int[2];
      final boolean history;
      final int width;
      final int height;
      int current;
      long frame = -1L;

      Made(PackTarget var1, int var2, int var3) {
         this.history = var1.history;
         this.width = var1.width(var2);
         this.height = var1.height(var3);

         for (int var4 = 0; var4 < (this.history ? 2 : 1); var4++) {
            this.texture[var4] = Gl.texture(var1.format.internal, this.width, this.height, var1.format.format, var1.format.type, 9729);
            this.fbo[var4] = PackStages.framebuffer("pack target " + var1.name, this.texture[var4]);
            GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            GL11.glClear(16384);
         }
      }

      int previous() {
         return this.history ? this.texture[this.current ^ 1] : this.texture[this.current];
      }

      void release() {
         for (int var1 = 0; var1 < 2; var1++) {
            if (this.texture[var1] != 0) {
               GL30.glDeleteFramebuffers(this.fbo[var1]);
               GL11.glDeleteTextures(this.texture[var1]);
            }
         }
      }
   }
}
