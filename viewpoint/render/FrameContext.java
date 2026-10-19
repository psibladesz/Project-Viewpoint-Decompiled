package viewpoint.render;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Pipeline;

public final class FrameContext {
   private static final float GOLDEN_FRACTION = 0.618034F;
   private static final int NOISE_MASK = 63;
   public SceneData scene;
   public Pipeline pipeline = Pipeline.LIBRARY;
   public Matrix4f view;
   public float eyeX;
   public float eyeY;
   public float eyeZ;
   final Matrix3f viewRotation = new Matrix3f();
   public final Matrix4f jitteredProjection = new Matrix4f();
   public final Matrix4f viewProjection = new Matrix4f();
   public final Matrix4f invViewProjection = new Matrix4f();
   final Matrix4f plainViewProjection = new Matrix4f();
   final Matrix4f invPlainViewProjection = new Matrix4f();
   final Matrix4f previousViewProjection = new Matrix4f();
   final Matrix4f reprojection = new Matrix4f();
   float frameSeconds;
   boolean previousValid;
   public float farNear;
   public float farFar;
   public final Matrix4f farInvPlainViewProjection = new Matrix4f();
   long index;
   int jitterPhase;
   int noiseFrame;
   float jitterX;
   float jitterY;
   public boolean sun;
   public int width;
   public int height;
   public int halfWidth;
   public int halfHeight;
   public int outputWidth;
   public int outputHeight;
   boolean upscaled;
   public Panini panini = Panini.NONE;
   int outputDrawFbo;
   int outputReadFbo;
   boolean outputScissor;
   final int[] viewport = new int[4];

   int noiseIndex() {
      return WorldRenderer.off(1) ? 0 : this.noiseFrame & 63;
   }

   float fadeNoise() {
      return this.noiseIndex() * 5.588238F;
   }

   boolean plantsHashed() {
      return !WorldRenderer.off(1);
   }

   float alphaHashShift() {
      return this.noiseIndex() * 0.618034F % 1.0F;
   }

   void camera(GlProgram var1) {
      var1.set("uInvViewProjection", this.invViewProjection);
      var1.set("uCamera", 0.05F, 400.0F, 1.0F / this.scene.projection.m00(), 1.0F / this.scene.projection.m11());
      var1.set("uEye", this.eyeX, this.eyeY, this.eyeZ);
   }

   int storey() {
      return (int)Math.floor(this.scene.originY / 2.4494896F + 1.0E-4);
   }

   float floorY() {
      return (float)(this.storey() * 2.4494896F - this.scene.originY);
   }
}
