package viewpoint.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.upscale.UpscaleFrame;

final class VendorInputs {
   private final GlProgram inputs = GlProgram.create("upscale inputs", "screen.vert", "upscale_inputs.frag");
   private final Targets targets;
   private final float[] matrices = new float[64];
   private final float[] camera = new float[12];
   private final Matrix4f scratch = new Matrix4f();

   VendorInputs(Targets var1) {
      this.targets = var1;
      this.inputs.sampler("uDepth", 12);
      this.inputs.sampler("uFarDepth", 29);
      this.inputs.sampler("uVelocity", 39);
   }

   static int framebuffer(int var0, int var1, String var2) {
      int var3 = GL11.glGetInteger(36006);
      int var4 = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, var4);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, var0, 0);
      GL30.glFramebufferTexture2D(36160, 36065, 3553, var1, 0);
      GL20.glDrawBuffers(new int[]{36064, 36065});
      int var5 = GL30.glCheckFramebufferStatus(36160);
      GL30.glBindFramebuffer(36160, var3);
      if (var5 != 36053) {
         throw new IllegalStateException(var2 + " framebuffer is incomplete: 0x" + Integer.toHexString(var5));
      } else {
         return var4;
      }
   }

   void draw(FrameContext var1, int var2) {
      GL30.glBindFramebuffer(36160, var2);
      GL11.glViewport(0, 0, var1.width, var1.height);
      this.inputs.use();
      var1.camera(this.inputs);
      this.inputs.set("uUnproject", var1.invPlainViewProjection);
      this.inputs.set("uPrevious", var1.reprojection);
      this.inputs.set("uFarUnproject", var1.farInvPlainViewProjection);
      this.inputs.set("uFarRange", var1.farNear, var1.farFar);
      Gl.bind(12, this.targets.depth);
      Gl.bind(29, this.targets.farDepth);
      Gl.bind(39, this.targets.velocity);
      Gl.screenTriangle();
      Gl.bind(12, 0);
      Gl.bind(29, 0);
      Gl.bind(39, 0);
      GL13.glActiveTexture(33984);
   }

   UpscaleFrame describe(FrameContext var1, boolean var2) {
      float var3 = 2.0F * (float)Math.atan(1.0 / var1.scene.projection.m11());
      return new UpscaleFrame(
         -var1.jitterX,
         -var1.jitterY,
         var1.width,
         var1.height,
         var2,
         var1.frameSeconds * 1000.0F,
         0.05F,
         400.0F,
         var3,
         1.2247449F,
         this.matrices(var1),
         this.camera(var1)
      );
   }

   private float[] matrices(FrameContext var1) {
      var1.scene.projection.get(this.matrices, 0);
      this.scratch.set(var1.scene.projection).invert().get(this.matrices, 16);
      var1.reprojection.mul(var1.invPlainViewProjection, this.scratch).get(this.matrices, 32);
      this.scratch.invert().get(this.matrices, 48);
      return this.matrices;
   }

   private float[] camera(FrameContext var1) {
      Matrix4f var2 = var1.view;
      float[] var3 = new float[]{
         var1.eyeX, var1.eyeY, var1.eyeZ, var2.m01(), var2.m11(), var2.m21(), var2.m00(), var2.m10(), var2.m20(), -var2.m02(), -var2.m12(), -var2.m22()
      };
      System.arraycopy(var3, 0, this.camera, 0, var3.length);
      return this.camera;
   }
}
