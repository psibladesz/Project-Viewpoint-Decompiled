package viewpoint.render;

import org.lwjgl.opengl.GL33;
import viewpoint.platform.Gl;

final class ShadowDepthViews {
   private final int sampler = GL33.glGenSamplers();

   ShadowDepthViews() {
      GL33.glSamplerParameteri(this.sampler, 34892, 0);
      GL33.glSamplerParameteri(this.sampler, 10241, 9728);
      GL33.glSamplerParameteri(this.sampler, 10240, 9728);
      GL33.glSamplerParameteri(this.sampler, 10242, 33071);
      GL33.glSamplerParameteri(this.sampler, 10243, 33071);
   }

   void bind(int[] var1, boolean var2) {
      for (int var3 = 0; var3 < var1.length; var3++) {
         Gl.bind(Gl.UNIT_SHADOW_DEPTH[var3], var2 ? var1[var3] : 0);
         GL33.glBindSampler(Gl.UNIT_SHADOW_DEPTH[var3], var2 ? this.sampler : 0);
      }
   }
}
