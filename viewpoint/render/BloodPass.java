package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.Arrays;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import zombie.core.textures.Texture;

final class BloodPass {
   private static final int PER_DRAW = 36;
   private static final int VERTICES = 6;
   private static final int WRITTEN = 3;
   private final GlProgram program;
   private final Matrix4f viewProjection = new Matrix4f();
   private final FloatBuffer decals = BufferUtils.createFloatBuffer(864);
   private final boolean[] done = new boolean[1024];
   private int batch;

   BloodPass(boolean var1) {
      this.program = var1 ? GlProgram.create("blood over a pack", "blood.vert", "blood_over.frag") : GlProgram.create("blood", "blood.vert", "blood.frag");
      this.program.sampler("uArt", 0);
      if (var1) {
         this.program.sampler("uSceneDepth", 12);
      }
   }

   void draw(FrameContext var1) {
      BloodDraws var2 = var1.scene.blood;
      if (var2.count != 0) {
         this.program.use();
         this.program.set("uViewProjection", var1.viewProjection);
         this.program.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
         GL11.glDepthMask(false);
         GL11.glDepthFunc(515);
         GL11.glDisable(2884);

         for (int var3 = 1; var3 < 3; var3++) {
            GL30.glColorMaski(var3, false, false, false, false);
         }

         GL30.glEnablei(3042, 0);
         GL40.glBlendFuncSeparatei(0, 770, 771, 0, 1);
         this.all(var2);
         GL30.glDisablei(3042, 0);
         GL11.glBlendFunc(770, 771);

         for (int var4 = 1; var4 < 3; var4++) {
            GL30.glColorMaski(var4, true, true, true, true);
         }

         GL11.glDepthMask(true);
         GL11.glDepthFunc(513);
      }
   }

   void over(FrameContext var1, int var2, Matrix4f var3) {
      BloodDraws var4 = var1.scene.blood;
      if (var4.count != 0 && var2 != 0) {
         this.program.use();
         this.program.set("uViewProjection", this.viewProjection.set(var3).mul(var1.view));
         this.program.set("uEye", var1.eyeX, var1.eyeY, var1.eyeZ);
         this.program.set("uOutput", var1.viewport[0], var1.viewport[1], var1.viewport[2], var1.viewport[3]);
         Gl.bind(12, var2);
         GL11.glDisable(2929);
         GL11.glDisable(2884);
         GL11.glEnable(3042);
         GL11.glBlendFunc(774, 0);
         this.all(var4);
         GL11.glBlendFunc(770, 771);
         GL11.glDisable(3042);
         Gl.bind(12, 0);
         GL13.glActiveTexture(33984);
         GL11.glEnable(2929);
      }
   }

   private void all(BloodDraws var1) {
      Arrays.fill(this.done, 0, var1.count, false);

      for (int var2 = 0; var2 < var1.count; var2++) {
         if (!this.done[var2]) {
            this.texture(var1, var1.textures[var2]);
         }
      }

      GL33.glBindSampler(0, 0);
      Texture.lastTextureID = -1;
   }

   private void texture(BloodDraws var1, int var2) {
      this.batch = 0;
      this.decals.clear();

      for (int var3 = 0; var3 < var1.count; var3++) {
         if (!this.done[var3] && var1.textures[var3] == var2) {
            this.done[var3] = true;
            if (var2 > 0) {
               if (this.batch == 0) {
                  bind(var2);
               }

               this.decals.put(var1.values, var3 * 24, 24);
               this.batch++;
               if (this.batch == 36) {
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

   private void flush() {
      if (this.batch != 0) {
         this.decals.flip();
         this.program.setVec4s("uDecals", this.decals);
         Gl.generated(this.batch * 6);
         this.decals.clear();
         this.batch = 0;
      }
   }
}
