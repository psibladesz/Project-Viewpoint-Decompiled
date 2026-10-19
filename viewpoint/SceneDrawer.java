package viewpoint;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import viewpoint.assets.Warmup;
import viewpoint.camera.CameraDebug;
import viewpoint.camera.CameraView;
import viewpoint.camera.DebugLines;
import viewpoint.core.CameraShot;
import viewpoint.core.Frame;
import viewpoint.core.View;
import viewpoint.gunplay.ShotDebug;
import viewpoint.input.Camera;
import viewpoint.input.FreeCam;
import viewpoint.input.Look;
import viewpoint.input.SeatedView;
import viewpoint.models.Characters;
import viewpoint.models.Corpses;
import viewpoint.models.Models;
import viewpoint.platform.CrashReport;
import viewpoint.platform.Profile;
import viewpoint.platform.Threads;
import viewpoint.render.CameraClear;
import viewpoint.render.Latency;
import viewpoint.render.MousePick;
import viewpoint.render.Panini;
import viewpoint.render.Retirement;
import viewpoint.render.WorldRenderer;
import zombie.core.DefaultShader;
import zombie.core.ShaderHelper;
import zombie.core.SpriteRenderer;
import zombie.core.opengl.GLStateRenderThread;
import zombie.core.textures.Texture;
import zombie.core.textures.TextureDraw.GenericDrawer;

final class SceneDrawer extends GenericDrawer {
   static volatile boolean failed;
   private static final Matrix4f view = new Matrix4f();
   private static final Matrix4f hand = new Matrix4f();
   private static final Matrix4f viewProjection = new Matrix4f();
   private final Frame frame;
   private final float[] eye = new float[3];
   private final float[] dir = new float[3];
   private final float[] up = new float[3];
   private final float[] handEye = new float[3];
   private final float[] handDir = new float[3];
   private final float[] steady = new float[2];
   private final float[] placed = new float[3];
   private final float[] onScreen = new float[2];

   SceneDrawer(Frame var1) {
      this.frame = var1;
   }

   public void render() {
      Threads.render();
      if (!failed) {
         Look.viewDrawn();
         guarded("drawing", this::drawFrame);
      }
   }

   static void prepare() {
      if (!failed && Warmup.renderDue()) {
         int var0 = GL11.glGetInteger(36006);
         int var1 = GL11.glGetInteger(36010);
         int var2 = GL11.glGetInteger(34229);
         guarded("preparing", Warmup::renderStep);
         GL30.glBindFramebuffer(36009, var0);
         GL30.glBindFramebuffer(36008, var1);
         GL30.glBindVertexArray(var2);
      }
   }

   private static void guarded(String var0, Runnable var1) {
      boolean var2 = DefaultShader.isActive;
      int var3 = GL11.glGetInteger(35725);
      int var4 = Texture.lastTextureID;
      GL11.glPushClientAttrib(-1);
      GL11.glPushAttrib(1048575);

      try {
         var1.run();
      } catch (Throwable var6) {
         System.out.println("[Viewpoint] disabled after error while " + var0 + ":");
         var6.printStackTrace(System.out);
         fail(var0, var6);
         WorldRenderer.restoreOutput();
      }

      GL11.glPopAttrib();
      GL11.glPopClientAttrib();
      GL20.glUseProgram(var3);
      ShaderHelper.forgetCurrentlyBound();
      DefaultShader.isActive = var2;
      Texture.lastTextureID = var4;
      GL13.glActiveTexture(33984);
      SpriteRenderer.ringBuffer.restoreVbos = true;
      SpriteRenderer.ringBuffer.restoreBoundTextures = true;
      GLStateRenderThread.startFrame();
   }

   private void drawFrame() {
      Retirement.collect();
      Look.readBeforeDrawing();
      float var1 = Look.yaw;
      float var2 = Look.pitch;
      Camera.direction(var1, var2, this.dir);
      Camera.eye(this.frame, var1, this.eye);
      this.upright();
      SeatedView.turn(this.frame, this.dir);
      SeatedView.turn(this.frame, this.up);
      this.lookAlong();
      WorldRenderer.handView.set(view);
      boolean var3 = CameraView.place(this.frame, var1, var2, this.eye);
      boolean var4 = var3 || this.frame.cameraShot.kicked;
      if (var3) {
         System.arraycopy(this.eye, 0, this.placed, 0, 3);
         this.hand();
      }

      if (var4) {
         CameraView.direction(this.frame.cameraShot, var1, var2, this.dir, this.up);
         this.lookAlong();
      }

      boolean var5 = FreeCam.view(this.frame, this.frame.eyeX, this.frame.eyeY, this.frame.eyeZ, this.eye, this.dir);
      if (var5) {
         WorldRenderer.cullView.set(view);
         this.upright();
         this.lookAlong();
      }

      if (var3) {
         this.frame.scene.projection.mul(view, viewProjection);
         CameraDebug.frame(this.frame, var5, var1, var2, this.placed, viewProjection);
      }

      DebugLines.frame(this.frame, var3, viewProjection);
      float var6 = this.eye[0];
      float var7 = this.eye[1];
      float var8 = this.eye[2];
      WorldRenderer.cullFromPlayer = var5 && !this.frame.onCamera;
      WorldRenderer.freeCamera = var5;
      WorldRenderer.handFromPlayer = var3 || var5;
      CameraShot var9 = this.frame.cameraShot;
      CameraClear.frame(var3 && !var5, var9.clearPlants, this.eye, var9.chestX, var9.chestY, var9.chestZ);
      this.crosshair(var4 && !var5, !var5, var1, var2);
      ShotDebug.frame(this.frame, view);
      Latency.viewBuilt(System.nanoTime(), Look.captured ? Look.readNanos : 0L, this.frame.number, this.frame.takenNanos);
      if (!WorldRenderer.begin(this.frame.scene, view, var6, var7, var8)) {
         fail("starting the renderer", new IllegalStateException("the renderer could not start"));
      } else {
         Models.release(this.frame);
         WorldRenderer.drawOutlines();
         Profile.mark(7);
         WorldRenderer.drawTranslucent(this.frame.scene);
         WorldRenderer.drawWeather(this.frame.scene, var6, var7, var8);
         WorldRenderer.finish(this.frame.scene);
         Screenshot.captureIfRequested();
         boolean var10 = var3 && var9.seated && var9.aim == 0.0F;
         if (Look.looking() && !FreeCam.active && !var10 && View.crosshairShown) {
            this.drawCrosshair();
         }
      }

      Retirement.drawn(this.frame.number);
   }

   private void lookAlong() {
      view.setLookAt(
         this.eye[0],
         this.eye[1],
         this.eye[2],
         this.eye[0] + this.dir[0],
         this.eye[1] + this.dir[1],
         this.eye[2] + this.dir[2],
         this.up[0],
         this.up[1],
         this.up[2]
      );
   }

   private void upright() {
      this.up[0] = 0.0F;
      this.up[1] = 1.0F;
      this.up[2] = 0.0F;
   }

   private void hand() {
      CameraShot var1 = this.frame.cameraShot;
      Camera.direction(var1.handYaw, var1.handPitch, this.handDir);
      Camera.eye(this.frame, var1.handYaw, this.handEye);
      float var2 = this.handEye[0];
      float var3 = this.handEye[1];
      float var4 = this.handEye[2];
      hand.setLookAt(var2, var3, var4, var2 + this.handDir[0], var3 + this.handDir[1], var4 + this.handDir[2], 0.0F, 1.0F, 0.0F);
      WorldRenderer.handView.set(hand);
   }

   private void crosshair(boolean var1, boolean var2, float var3, float var4) {
      float var5 = var2 ? View.swayRight : 0.0F;
      float var6 = var2 ? View.swayUp : 0.0F;
      boolean var7 = var5 != 0.0F || var6 != 0.0F;
      if (var1 || var7) {
         this.frame.scene.projection.mul(view, viewProjection);
      }

      Panini var8 = Panini.of(this.frame.scene.projection, this.frame.scene.panini);
      this.place(var1, var3, var4, 0.0F, 0.0F);
      MousePick.aimShift(this.steady[0], this.steady[1]);
      var8.toScreen(this.steady[0], this.steady[1], this.onScreen);
      View.ringX = this.onScreen[0];
      View.ringY = this.onScreen[1];
      this.place(var1 || var7, var3, var4, var5, var6);
      var8.toScreen(this.steady[0], this.steady[1], this.onScreen);
      View.crosshairX = this.onScreen[0];
      View.crosshairY = this.onScreen[1];
   }

   private void place(boolean var1, float var2, float var3, float var4, float var5) {
      if (var1 && CameraView.steady(viewProjection, this.eye, var2, var3, var4, var5, this.steady)) {
         this.steady[0] = Math.max(-1.0F, Math.min(1.0F, this.steady[0]));
         this.steady[1] = Math.max(-1.0F, Math.min(1.0F, this.steady[1]));
      } else {
         this.steady[0] = this.steady[1] = 0.0F;
      }
   }

   public void postRender() {
      Characters.release(this.frame);
      Corpses.release(this.frame);
      Models.release(this.frame);
   }

   private void drawCrosshair() {
      int[] var1 = new int[4];
      GL11.glGetIntegerv(2978, var1);
      int var2 = var1[0] + Math.round(var1[2] * (1.0F + View.crosshairX) / 2.0F);
      int var3 = var1[1] + Math.round(var1[3] * (1.0F + View.crosshairY) / 2.0F);
      GL11.glEnable(3089);
      GL11.glScissor(var2 - 2, var3 - 2, 4, 4);
      if (View.crosshairOnTarget) {
         GL11.glClearColor(View.ON_TARGET[0], View.ON_TARGET[1], View.ON_TARGET[2], 1.0F);
      } else {
         GL11.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
      }

      GL11.glClear(16384);
      GL11.glScissor(var2 - 1, var3 - 1, 2, 2);
      GL11.glClearColor(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glClear(16384);
   }

   private static void fail(String var0, Throwable var1) {
      failed = true;
      View.enabled = false;
      CrashReport.report(var0, var1);
   }
}
