package viewpoint.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL44;
import viewpoint.platform.DrawCounts;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.Profile;
import viewpoint.platform.Tuning;

final class FarLamps {
   private static final int MAX_CARDS = 16384;
   private static final int CARD_BYTES = 16;
   private static final float NEAR_FADE = 16.0F;
   private final GlProgram program = GlProgram.create("far lamps", "far_lamp.vert", "far_lamp.frag");
   private final IntBuffer cards = BufferUtils.createIntBuffer(65536);
   private final ByteBuffer upload = BufferUtils.createByteBuffer(262144);
   private final Vector3f right = new Vector3f();
   private final Vector3f up = new Vector3f();
   private int buffer;
   private int vao;
   private int count;
   private int written = -1;

   FarLamps() {
      this.program.sampler("uDepth", 12);
   }

   void draw(FrameContext var1, Matrix4f var2) {
      if (!WorldRenderer.off(9) && Wireframe.mode() == 0) {
         this.write();
         if (this.count != 0) {
            SceneData var3 = var1.scene;
            GpuParts.begin(18);
            this.program.use();
            var1.camera(this.program);
            this.program.set("uViewProjection", var2);
            this.program.set("uCameraSquare", (float)(-var3.originX), (float)(-var3.originZ), (float)(var3.originY / 2.4494896F));
            var1.viewRotation.getColumn(0, this.right);
            var1.viewRotation.getColumn(1, this.up);
            this.program.set("uRight", this.right.x, this.right.y, this.right.z);
            this.program.set("uUp", this.up.x, this.up.y, this.up.z);
            this.program.set("uPixel", 2.0F / (var3.projection.m11() * var1.height));
            this.program.set("uCard", Tuning.farLampSize, Tuning.farLampPixels);
            this.program.set("uGlow", Tuning.farLampGlow);
            this.program.set("uNearFade", var3.nearReach, 16.0F);
            this.program.set("uFogRange", var3.fogStart, var3.fogEnd);
            this.program.set("uHaze", Tuning.farHaze, var3.nearEnd());
            FarPass.daylight(this.program, var3);
            GL11.glEnable(2929);
            GL11.glDepthFunc(515);
            GL11.glDepthMask(false);
            GL11.glEnable(3042);
            GL14.glBlendFuncSeparate(1, 1, 0, 1);
            GL30.glBindVertexArray(this.vao);
            GL31.glDrawArraysInstanced(5, 0, 4, this.count);
            GL30.glBindVertexArray(0);
            GL11.glDisable(3042);
            GL11.glDepthMask(true);
            GL11.glDepthFunc(513);
            DrawCounts.draw(2);
            GpuParts.end(18);
         }
      }
   }

   private void write() {
      if (this.written != CellLightPages.cardsChanged()) {
         this.written = CellLightPages.cardsChanged();
         if (this.buffer == 0) {
            this.make();
         }

         this.cards.clear();
         CellLightPages.cards(this.cards);
         this.cards.flip();
         this.count = this.cards.remaining() / 4;
         Profile.farLampCards = this.count;
         this.upload.clear();

         for (int var1 = 0; var1 < this.count; var1++) {
            int var2 = var1 * 4;
            this.upload.putFloat(this.cards.get(var2) + 0.5F).putFloat(this.cards.get(var2 + 1) + 0.5F).putFloat(this.cards.get(var2 + 2));
            this.upload.putInt(this.cards.get(var2 + 3));
         }

         this.upload.flip();
         GL15.glBindBuffer(34962, this.buffer);
         GL15.glBufferSubData(34962, 0L, this.upload);
         GL15.glBindBuffer(34962, 0);
      }
   }

   private void make() {
      this.buffer = GL15.glGenBuffers();
      GL15.glBindBuffer(34962, this.buffer);
      GL44.glBufferStorage(34962, 262144L, 256);
      this.vao = GL30.glGenVertexArrays();
      GL30.glBindVertexArray(this.vao);
      GL20.glEnableVertexAttribArray(0);
      GL20.glVertexAttribPointer(0, 3, 5126, false, 16, 0L);
      GL33.glVertexAttribDivisor(0, 1);
      GL20.glEnableVertexAttribArray(1);
      GL20.glVertexAttribPointer(1, 4, 5121, true, 16, 12L);
      GL33.glVertexAttribDivisor(1, 1);
      GL30.glBindVertexArray(0);
      GL15.glBindBuffer(34962, 0);
   }
}
