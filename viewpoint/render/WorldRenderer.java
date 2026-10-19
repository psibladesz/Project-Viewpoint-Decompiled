package viewpoint.render;

import java.util.function.LongSupplier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import viewpoint.platform.Gl;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.IrisPacks;
import viewpoint.platform.PackPass;
import viewpoint.platform.Pipeline;
import viewpoint.platform.Profile;
import viewpoint.platform.Threads;
import viewpoint.upscale.Upscaling;

public final class WorldRenderer {
   public static final float NEAR = 0.05F;
   public static final float FAR = 400.0F;
   public static final int CASCADES = 3;
   public static final int START_STEPS = 20;
   public static volatile int debugView;
   public static volatile int isolate;
   public static final String[] ISOLATE_NAMES = new String[]{
      "all effects on",
      "TAA off",
      "GI and occlusion off",
      "per-pixel lamps off",
      "volumetrics off",
      "sun and moon shadows off",
      "clouds off",
      "flashlight off",
      "lamp shadows off",
      "far lamps off"
   };
   static final int ISO_TAA = 1;
   static final int ISO_GI = 2;
   static final int ISO_LAMPS = 3;
   static final int ISO_VOLUME = 4;
   static final int ISO_SHADOWS = 5;
   static final int ISO_CLOUDS = 6;
   static final int ISO_FLASH = 7;
   static final int ISO_LAMP_SHADOWS = 8;
   static final int ISO_FAR_LAMPS = 9;
   static LongSupplier clock = System::nanoTime;
   private static final FrameContext frame = new FrameContext();
   static Targets targets;
   static ShadowPass shadows;
   static WeatherMap weatherMap;
   static SkyPass sky;
   static SurfacePass surfaces;
   static TranslucentPass translucent;
   static IndirectPass indirect;
   static WeatherPass weather;
   static PostPass post;
   static PackStages stages;
   static TemporalPass temporal;
   static Rescale rescale;
   static FrameEnd end;
   static FarPass far;
   static ModelPass models;
   static TargetOutline target;
   static FloorBakes floors;
   static ShellGround ground;
   public static volatile boolean cullFromPlayer;
   public static final Matrix4f cullView = new Matrix4f();
   public static volatile boolean freeCamera;
   public static volatile boolean handFromPlayer;
   public static final Matrix4f handView = new Matrix4f();
   private static boolean ready;
   private static boolean failedToStart;
   private static boolean irisDrawn;

   public static float cascadeRadius(int var0, SceneData var1) {
      return ShadowPass.cascadeRadius(var0, var1);
   }

   public static float farShadowRadius(float var0) {
      return ShadowPass.farShadowRadius(var0);
   }

   static boolean off(int var0) {
      int var1 = Wireframe.mode();
      Pipeline var2 = frame.pipeline;
      return isolate == var0
         || var1 != 0 && var0 == 1
         || var1 == 2 && (var0 == 2 || var0 == 4 || var0 == 6)
         || var0 == 1 && !var2.taa && var2.upscaler == 0
         || var0 == 2 && !var2.occlusion && !var2.bounce
         || var0 == 4 && !var2.volumetrics
         || var0 == 5 && !var2.shadows
         || var0 == 6 && !var2.clouds
         || var0 == 8 && (var2.lampShadows == 0 || isolate == 3)
         || var0 == 9 && !var2.farLamps;
   }

   public static boolean begin(SceneData var0, Matrix4f var1, float var2, float var3, float var4) {
      Threads.render();
      if (!ready) {
         prepare(Long.MAX_VALUE);
         if (!ready) {
            return false;
         }
      }

      DevReload.frame();
      PackLinks.follow();
      GlDebug.listen(true);
      GlDebug.push("Viewpoint frame");
      frame.outputDrawFbo = GL11.glGetInteger(36006);
      frame.outputReadFbo = GL11.glGetInteger(36010);
      frame.outputScissor = GL11.glIsEnabled(3089);
      frame.pipeline = GlProgram.pack().pipeline;
      boolean var5 = !off(1);
      frame.upscaled = var5 && IrisPacks.active() == null && end.upscales(frame);
      if (!targetsFit()) {
         GlDebug.popAll();
         return false;
      }

      frame.width = targets.width;
      frame.height = targets.height;
      frame.halfWidth = targets.halfWidth;
      frame.halfHeight = targets.halfHeight;
      Profile.mark(0);
      FrameStream.frameStarted();
      frame.index++;
      Gl.resetState();
      GL11.glClearDepth(1.0);
      frame.scene = var0;
      FarRelief.frame(var0);
      frame.outputWidth = frame.upscaled ? frame.viewport[2] : (var5 ? targets.historyWidth : frame.width);
      frame.outputHeight = frame.upscaled ? frame.viewport[3] : (var5 ? targets.historyHeight : frame.height);
      frame.view = var1;
      frame.viewRotation.set(var1).transpose();
      frame.eyeX = var2;
      frame.eyeY = var3;
      frame.eyeZ = var4;
      temporal.begin(frame);
      FrameUniforms.set(frame, targets);
      frame.sun = var0.sunStrength > 0.02F;
      floors.update(var0, frame.pipeline.floorSurfaces);
      ground.update(var0);
      Profile.mark(1);
      PackModels.upload(System.nanoTime());
      Meshes.prepare(var0);
      models.prepare(var0);
      irisDrawn = IrisMode.draw(frame, shadows, models, far);
      if (!irisDrawn) {
         drawWorld();
      }

      GlDebug.push("models");
      return true;
   }

   private static boolean targetsFit() {
      GL11.glGetIntegerv(2978, frame.viewport);
      Pipeline var0 = frame.pipeline;
      float var1 = IrisPacks.active() != null ? 1.0F : var0.renderScale / (var0.upscaler != 0 ? Math.max(var0.renderScale, 100.0F) : 100.0F);
      int var2 = Math.max(1, Math.round(frame.viewport[2] * var1));
      int var3 = Math.max(1, Math.round(frame.viewport[3] * var1));
      if (frame.upscaled) {
         Upscaling var4 = end.upscaler(frame, frame.viewport[2], frame.viewport[3]);
         var2 = var4.frameWidth;
         var3 = var4.frameHeight;
      }

      int var9 = var0.cloudResolution;
      boolean var5 = var0.taa && var1 < 1.0F;
      int var6 = var5 ? frame.viewport[2] : var2;
      int var7 = var5 ? frame.viewport[3] : var3;
      if (targets.gbufferFbo != 0
         && var2 == targets.width
         && var3 == targets.height
         && var6 == targets.historyWidth
         && var7 == targets.historyHeight
         && var9 == targets.cloudShare) {
         return true;
      }

      boolean var8 = targets.ensure(var2, var3, var6, var7, var9);
      temporal.historyValid = false;
      GL30.glBindFramebuffer(36009, frame.outputDrawFbo);
      GL30.glBindFramebuffer(36008, frame.outputReadFbo);
      return var8;
   }

   private static void drawWorld() {
      GlDebug.push("far shadow");
      far.prepare(frame, true);
      GlDebug.pop();
      shadows.draw(frame);
      Profile.mark(2);
      GlDebug.push("g-buffer");
      Wireframe.begin();
      GpuParts.begin(0);
      surfaces.gbuffer(frame);
      GpuParts.end(0);
      GpuParts.begin(1);
      models.gbuffer(frame);
      GpuParts.end(1);
      GpuParts.begin(2);
      far.gbuffer(frame);
      GpuParts.end(2);
      Wireframe.end();
      GlDebug.pop();
      MousePick.read(frame, targets.gbufferFbo, frame.invViewProjection);
      Profile.mark(3);
      GlDebug.push("occlusion");
      indirect.draw(frame, !off(2) && frame.pipeline.occlusion, !off(2) && frame.pipeline.bounce);
      GlDebug.pop();
      Profile.mark(4);
      sky.prepare(frame);
      GlDebug.push("sky");
      GL30.glBindFramebuffer(36160, targets.lightFbo);
      GL11.glViewport(0, 0, frame.width, frame.height);
      if (Wireframe.mode() == 2) {
         GL11.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
         GL11.glClear(16384);
      } else {
         if (!off(6)) {
            GpuParts.begin(12);
            sky.clouds(frame);
            GpuParts.end(12);
            GL30.glBindFramebuffer(36160, targets.lightFbo);
            GL11.glViewport(0, 0, frame.width, frame.height);
         }

         sky.sky(frame);
      }

      GlDebug.pop();
      GlDebug.push("lighting");
      GpuParts.begin(13);
      surfaces.light(frame);
      GpuParts.end(13);
      GlDebug.pop();
      Profile.mark(5);
      GlDebug.push("far world");
      Wireframe.begin();
      far.draw(frame);
      Wireframe.end();
      GlDebug.pop();
      bindTarget();
      Profile.mark(6);
   }

   public static void drawOutlines() {
      GlDebug.push("outlines");
      if (!irisDrawn) {
         models.outlines(frame, rescale.outlineTarget(frame, targets.hdrFbo));
      }

      target.mask(frame, models);
      GlDebug.pop();
   }

   public static void drawTranslucent(SceneData var0) {
      GlDebug.pop();
      Gl.resetState();
      if (!irisDrawn) {
         FrameUniforms.bind(targets);
         bindTarget();
         GlDebug.push("translucent");
         if (Wireframe.mode() == 0) {
            translucent.draw(frame, surfaces, models, shadows);
         }

         GlDebug.pop();
         stages.run(PackPass.Stage.TRANSLUCENT, frame, targets.hdr);
         bindTarget();
         Profile.mark(8);
      }
   }

   public static void drawWeather(SceneData var0, float var1, float var2, float var3) {
      if (!irisDrawn && WeatherPass.any(var0)) {
         GlDebug.push("weather");
         bindTarget();
         weather.draw(frame);
         GlDebug.pop();
         bindTarget();
         Profile.mark(9);
      } else {
         Profile.mark(9);
      }
   }

   public static void finish(SceneData var0) {
      FrameGenPass.midframe();
      if (irisDrawn) {
         target.composite(frame);
         shadows.spots.coronas(frame, 0);
      } else {
         end.draw(frame);
         shadows.spots.coronas(frame, targets.depth);
      }

      models.endFrame();
      GL11.glDepthMask(true);
      Profile.mark(12);
      Profile.endFrame();
      if (frame.outputScissor) {
         GL11.glEnable(3089);
      }

      GlDebug.popAll();
      GlDebug.listen(false);
   }

   static void resetState() {
      Gl.resetState();
   }

   public static void restoreOutput() {
      GlDebug.popAll();
      GlDebug.listen(false);
      if (ready) {
         GL33.glBindSampler(0, 0);
         GL30.glBindVertexArray(0);
         GL30.glBindFramebuffer(36009, frame.outputDrawFbo);
         GL30.glBindFramebuffer(36008, frame.outputReadFbo);
         GL11.glViewport(frame.viewport[0], frame.viewport[1], frame.viewport[2], frame.viewport[3]);
      }
   }

   public static void bindTarget() {
      GL30.glBindFramebuffer(36160, targets.hdrFbo);
      GL11.glViewport(0, 0, frame.width, frame.height);
   }

   static boolean historyValid() {
      return temporal != null && temporal.historyValid;
   }

   static IndirectPass indirect() {
      return indirect;
   }

   static Targets targets() {
      return targets;
   }

   static WeatherMap weatherMap() {
      return weatherMap;
   }

   public static boolean prepare(long var0) {
      if (ready || failedToStart) {
         return true;
      }

      if (!RendererStart.run(var0)) {
         return false;
      }

      ready = GlProgram.allLinked() && shadows.complete();
      failedToStart = !ready;
      return true;
   }

   public static void prepareMinecraftPack() {
      if (ready) {
         IrisMode.prepare();
      }
   }

   public static void bakeAhead() {
      RendererStart.bakeAhead();
   }

   public static float preparedShare() {
      return !ready && !failedToStart ? RendererStart.share() : 1.0F;
   }

   private WorldRenderer() {
   }
}
