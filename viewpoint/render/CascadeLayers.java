package viewpoint.render;

import java.util.Arrays;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Profile;

final class CascadeLayers {
   static final int COUNT = 2;
   private final CascadeLayer[] layers = new CascadeLayer[]{new CascadeLayer(), new CascadeLayer()};
   private final LayerCasters[] casters = new LayerCasters[]{new LayerCasters(), new LayerCasters()};
   private final GlProgram program;
   private final ModelPass models;
   private final FarShadow farMaps;
   private final Matrix4f carried = new Matrix4f();
   private boolean redrawn;
   private static final int BOX = 0;
   private static final int LOST = 1;
   private static final int FAR = 2;
   private static final int SETTLED = 3;
   private static final int NONE = -1;
   private final long[] drawnLayers = new long[2];
   private final long[] drawnOver = new long[2];
   private final long[] drawnFor = new long[4];
   private long frames;

   CascadeLayers(GlProgram var1, ModelPass var2, FarShadow var3) {
      this.program = var1;
      this.models = var2;
      this.farMaps = var3;
      Profile.cascadeLayersReport = this::report;
   }

   void frame(SceneData var1, int var2) {
      this.frames++;
      this.redrawn = false;

      for (int var3 = var2; var3 < 2; var3++) {
         this.release(var3);
      }
   }

   void draw(SceneData var1, int var2, byte var3, float var4, CascadeLayers.Cascade var5, Matrix4f var6) {
      CascadeLayer var7 = this.layers[var2];
      LayerCasters var8 = this.casters[var2];
      var8.sort(var1, var3);
      float var9 = (float)CascadeLayer.marginSquares(var5.size, var4);
      int var10 = !var7.holds(var1, var5.size, var4, var5.sunDistance)
         ? 0
         : (var8.lost() ? 1 : (var8.farSwitched(this.farMaps.active()) ? 2 : (!this.redrawn && var8.waited() ? this.waiting(var1, var7, var8, var9) : -1)));
      if (var10 != -1) {
         this.drawnFor[var10]++;
         var7.aim(var1, var5.size, var4, var5.lightView, var5.sunDistance, var6);
         var7.begin();
         this.program.set("uViewProjection", var6);
         Meshes.draw(var1, this.program, var3, var8.take(var1, this.farMaps.castersKey(var1, var6, var9, 0.0F, 0.0F)));
         this.farMaps.castInto(var1, var6, var2, var9);
         this.program.use();
         this.drawnLayers[var2]++;
         this.redrawn = true;
      }

      var7.cascade(var1, var6);
      var7.copyTo(var5.texture);
      GL30.glBindFramebuffer(36160, var5.fbo);
      GL11.glViewport(0, 0, var5.size, var5.size);
      if (var8.extras() > 0) {
         this.program.set("uViewProjection", var6);
         Meshes.draw(var1, this.program, var3, var8.extra());
         this.drawnOver[var2] = this.drawnOver[var2] + var8.extras();
      }

      float var11 = var2 == 0 ? 0.0F : 13.200001F;
      float var12 = (var2 == 0 ? 22.0F : 64.0F) + 6.0F;
      this.models.shadow(var1, var6, var11, var12);
      this.program.use();
   }

   private int waiting(SceneData var1, CascadeLayer var2, LayerCasters var3, float var4) {
      return var3.arrived() ? 3 : (var3.farChanged(this.farKey(var1, var2, var4)) ? 2 : -1);
   }

   private long farKey(SceneData var1, CascadeLayer var2, float var3) {
      return this.farMaps.castersKey(var1, var2.drawnWith(var1, this.carried), var3, var2.movedX(var1), var2.movedZ(var1));
   }

   void release() {
      for (int var1 = 0; var1 < 2; var1++) {
         this.release(var1);
      }
   }

   private void release(int var1) {
      this.layers[var1].release();
      this.casters[var1].clear();
   }

   private String report() {
      double var1 = Math.max(this.frames, 1L);
      String var3 = String.format(
         "drawn/frame near %.3f, mid %.3f (for box or sun %.3f, casters gone %.3f, far world %.3f, casters settled %.3f); casters over them/frame near %.1f, mid %.1f",
         this.drawnLayers[0] / var1,
         this.drawnLayers[1] / var1,
         this.drawnFor[0] / var1,
         this.drawnFor[1] / var1,
         this.drawnFor[2] / var1,
         this.drawnFor[3] / var1,
         this.drawnOver[0] / var1,
         this.drawnOver[1] / var1
      );
      this.frames = 0L;
      Arrays.fill(this.drawnLayers, 0L);
      Arrays.fill(this.drawnOver, 0L);
      Arrays.fill(this.drawnFor, 0L);
      return var3;
   }

   static final class Cascade {
      int texture;
      int fbo;
      int size;
      float sunDistance;
      Matrix4f lightView;
   }
}
