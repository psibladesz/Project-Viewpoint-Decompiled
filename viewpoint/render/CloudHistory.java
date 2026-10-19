package viewpoint.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Tuning;

final class CloudHistory {
   static final int BLOCK = 4;
   private static final int[] PICK_X = new int[]{0, 2, 2, 0, 1, 3, 3, 1, 1, 3, 3, 1, 0, 2, 2, 0};
   private static final int[] PICK_Y = new int[]{0, 2, 0, 2, 1, 3, 1, 3, 0, 2, 0, 2, 1, 3, 1, 3};
   private final GlProgram resolve = GlProgram.create("cloud resolve", "screen.vert", "cloud_resolve.frag");
   private int turn;
   private int newest;
   private long resolvedFrame = -2L;
   private int resolvedImage;

   CloudHistory() {
      this.resolve.sampler("uTrace", 17);
      this.resolve.sampler("uHistory", 19);
   }

   void trace(GlProgram var1, Targets var2) {
      this.turn = (this.turn + 1) % 16;
      var1.set("uCloudPick", PICK_X[this.turn], PICK_Y[this.turn]);
      var1.set("uCloudSize", var2.cloudWidth, var2.cloudHeight);
      GL30.glBindFramebuffer(36160, var2.cloudTraceFbo);
      GL11.glViewport(0, 0, var2.cloudTraceWidth, var2.cloudTraceHeight);
   }

   void resolve(FrameContext var1, Targets var2) {
      int[] var3 = new int[]{var2.clouds, var2.cloudSpare};
      int[] var4 = new int[]{var2.cloudFbo, var2.cloudSpareFbo};
      boolean var5 = var1.previousValid && this.resolvedFrame == var1.index - 1L && this.resolvedImage == var3[this.newest];
      int var6 = this.newest ^ 1;
      GL30.glBindFramebuffer(36160, var4[var6]);
      GL11.glViewport(0, 0, var2.cloudWidth, var2.cloudHeight);
      this.resolve.use();
      Matrix4f var7 = var1.view;
      float var8 = 1.0F / var1.scene.projection.m00();
      float var9 = 1.0F / var1.scene.projection.m11();
      this.resolve.set("uRight", var7.m00() * var8, var7.m10() * var8, var7.m20() * var8);
      this.resolve.set("uUp", var7.m01() * var9, var7.m11() * var9, var7.m21() * var9);
      this.resolve.set("uForward", -var7.m02(), -var7.m12(), -var7.m22());
      this.resolve.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
      this.resolve.set("uReprojection", var1.reprojection);
      this.resolve.set("uCloudPick", PICK_X[this.turn], PICK_Y[this.turn]);
      this.resolve.set("uHistoryValid", var5 ? 1.0F : 0.0F);
      this.resolve.set("uStretch", var1.pipeline.softClouds ? Tuning.softCloudStretch : 0.0F);
      Gl.bind(17, var2.cloudTrace);
      Gl.bind(19, var3[this.newest]);
      GL13.glActiveTexture(33984);
      Gl.screenTriangle();
      Gl.bind(17, 0);
      Gl.bind(19, 0);
      GL13.glActiveTexture(33984);
      this.newest = var6;
      this.resolvedFrame = var1.index;
      this.resolvedImage = var3[var6];
   }

   int image(Targets var1) {
      return this.newest == 0 ? var1.clouds : var1.cloudSpare;
   }
}
