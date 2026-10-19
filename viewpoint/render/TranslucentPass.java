package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;

final class TranslucentPass {
   private static final float[] NOTHING = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
   private static final float[] EVERYTHING = new float[]{1.0F, 0.0F, 0.0F, 0.0F};
   private final GlProgram over = GlProgram.create("translucent over", "screen.vert", "translucent_over.frag");
   private final FlamePass flames = new FlamePass(false);
   private final Targets targets;

   TranslucentPass(Targets var1) {
      this.targets = var1;
      this.over.sampler("uTranslucentSum", 86);
      this.over.sampler("uTranslucentReveal", 87);
   }

   void draw(FrameContext var1, SurfacePass var2, ModelPass var3, ShadowPass var4) {
      GpuParts.begin(14);
      GL30.glBindFramebuffer(36160, this.targets.translucentFbo);
      GL30.glClearBufferfv(6144, 0, NOTHING);
      GL30.glClearBufferfv(6144, 1, EVERYTHING);
      GL11.glEnable(3042);
      GL40.glBlendFunci(0, 1, 1);
      GL40.glBlendFunci(1, 0, 769);
      var2.translucent(var1);
      var3.glass(var1, var4);
      this.flames.draw(var1);
      WorldRenderer.bindTarget();
      GL11.glDisable(2929);
      GL11.glBlendFunc(771, 770);
      this.over.use();
      Gl.bind(86, this.targets.translucentSum);
      Gl.bind(87, this.targets.translucentReveal);
      GL13.glActiveTexture(33984);
      Gl.screenTriangle();
      Gl.bind(86, 0);
      Gl.bind(87, 0);
      GL13.glActiveTexture(33984);
      GL11.glBlendFunc(770, 771);
      GL11.glDisable(3042);
      GL11.glEnable(2929);
      GpuParts.end(14);
   }
}
