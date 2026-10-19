package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.Arrays;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL33;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Tuning;

final class FlamePass {
   private static final int PER_DRAW = 48;
   private static final int VERTICES = 6;
   private final GlProgram program;
   private final FloatBuffer cards = BufferUtils.createFloatBuffer(960);
   private final boolean[] done = new boolean[192];
   private final Integer[] order = new Integer[192];
   private final float[] distance = new float[192];
   private final Matrix4f viewProjection = new Matrix4f();
   private int batch;

   FlamePass(boolean var1) {
      this.program = var1 ? GlProgram.create("flames over a pack", "flame.vert", "flame_over.frag") : GlProgram.create("flames", "flame.vert", "flame.frag");
      this.program.sampler("uArt", 0);
      if (var1) {
         this.program.sampler("uSceneDepth", 12);
      }
   }

   void draw(FrameContext var1) {
      FlameDraws var2 = var1.scene.flames;
      if (var2.count != 0) {
         this.begin(var1, var1.viewProjection);
         this.program.set("uFlameGlow", Tuning.flameGlow);
         GL11.glDepthMask(false);
         GL11.glDisable(2884);
         Arrays.fill(this.done, 0, var2.count, false);

         for (int var3 = 0; var3 < var2.count; var3++) {
            if (!this.done[var3]) {
               this.texture(var2, var2.textures[var3].getID());
            }
         }

         GL33.glBindSampler(0, 0);
         GL11.glDepthMask(true);
      }
   }

   void over(FrameContext var1, int var2, Matrix4f var3) {
      FlameDraws var4 = var1.scene.flames;
      if (var4.count != 0 && var2 != 0) {
         this.begin(var1, this.viewProjection.set(var3).mul(var1.view));
         this.program.set("uOutput", var1.viewport[0], var1.viewport[1], var1.viewport[2], var1.viewport[3]);
         Gl.bind(12, var2);
         GL11.glDisable(2929);
         GL11.glDisable(2884);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         this.farthestFirst(var1, var4);
         int var5 = 0;

         for (int var6 = 0; var6 < var4.count; var6++) {
            int var7 = this.order[var6];
            int var8 = var4.textures[var7].getID();
            if (var8 > 0) {
               if (var8 != var5 || this.batch == 48) {
                  this.flush();
                  bind(var8);
                  var5 = var8;
               }

               this.add(var4, var7);
            }
         }

         this.flush();
         GL33.glBindSampler(0, 0);
         Gl.bind(12, 0);
         GL13.glActiveTexture(33984);
         GL11.glDisable(3042);
         GL11.glEnable(2929);
      }
   }

   private void begin(FrameContext var1, Matrix4f var2) {
      this.program.use();
      this.program.set("uViewProjection", var2);
      this.program.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
      float var3 = var1.view.m00();
      float var4 = var1.view.m20();
      float var5 = (float)Math.hypot(var3, var4);
      this.program.set("uRight", var5 > 0.0F ? var3 / var5 : 1.0F, 0.0F, var5 > 0.0F ? var4 / var5 : 0.0F);
      this.batch = 0;
      this.cards.clear();
   }

   private void texture(FlameDraws var1, int var2) {
      for (int var3 = 0; var3 < var1.count; var3++) {
         if (!this.done[var3] && var1.textures[var3].getID() == var2) {
            this.done[var3] = true;
            if (var2 > 0) {
               if (this.batch == 0) {
                  bind(var2);
               }

               this.add(var1, var3);
               if (this.batch == 48) {
                  this.flush();
               }
            }
         }
      }

      this.flush();
   }

   private static void bind(int var0) {
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, var0);
      GL33.glBindSampler(0, TextureFilter.SPRITES.sampler(var0));
   }

   private void add(FlameDraws var1, int var2) {
      this.cards.put(var1.values, var2 * 20, 20);
      this.batch++;
   }

   private void flush() {
      if (this.batch != 0) {
         this.cards.flip();
         this.program.setVec4s("uCards", this.cards);
         Gl.generated(this.batch * 6);
         this.cards.clear();
         this.batch = 0;
      }
   }

   private void farthestFirst(FrameContext var1, FlameDraws var2) {
      for (int var3 = 0; var3 < var2.count; var3++) {
         int var4 = var3 * 20;
         float var5 = var2.values[var4] - var1.eyeX;
         float var6 = var2.values[var4 + 1] - var1.eyeY;
         float var7 = var2.values[var4 + 2] - var1.eyeZ;
         this.distance[var3] = var5 * var5 + var6 * var6 + var7 * var7;
         this.order[var3] = var3;
      }

      Arrays.sort(this.order, 0, var2.count, (var1x, var2x) -> Float.compare(this.distance[var2x], this.distance[var1x]));
   }
}
