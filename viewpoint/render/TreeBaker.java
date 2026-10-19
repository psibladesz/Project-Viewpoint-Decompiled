package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import zombie.core.textures.TextureID;

final class TreeBaker {
   private static final int BAKES_PER_FRAME = 32;
   private final GlProgram program = GlProgram.create("tree bake", "tree_bake.vert", "tree_bake.frag");
   private int texture;
   private int framebuffer;
   private final int[] viewport = new int[4];
   private final float[] clearColour = new float[4];

   TreeBaker() {
      this.program.sampler("uSource", 0);
   }

   void bake() {
      if (!TreeAtlas.PENDING.isEmpty()) {
         if (this.texture == 0) {
            this.init();
         }

         int var1 = GL11.glGetInteger(36006);
         GL11.glGetIntegerv(2978, this.viewport);
         GL11.glGetFloatv(3106, this.clearColour);
         int var2 = GL11.glGetInteger(32969);
         int var3 = GL11.glGetInteger(32968);
         int var4 = GL11.glGetInteger(32971);
         int var5 = GL11.glGetInteger(32970);
         GL30.glBindFramebuffer(36009, this.framebuffer);
         GL11.glDisable(2929);
         GL11.glEnable(3089);
         GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         GL11.glEnable(3042);
         GL11.glBlendFunc(1, 771);
         this.program.use();
         GL30.glBindVertexArray(0);

         TreeAtlas.Slot var7;
         for (int var6 = 0; var6 < 32 && (var7 = TreeAtlas.PENDING.poll()) != null; var6++) {
            GL11.glViewport(var7.x(), var7.y(), 64, 128);
            GL11.glScissor(var7.x(), var7.y(), 64, 128);
            GL11.glClear(16384);
            this.sprite(var7, var7.trunkPage, var7.trunkMap);
            if (var7.leavesPage != null) {
               this.sprite(var7, var7.leavesPage, var7.leavesMap);
            }
         }

         GL20.glUseProgram(0);
         GL11.glDisable(3089);
         GL30.glBindFramebuffer(36009, var1);
         GL13.glActiveTexture(34026);
         GL11.glBindTexture(3553, this.texture);
         GL30.glGenerateMipmap(3553);
         GL11.glBindTexture(3553, 0);
         GL13.glActiveTexture(33984);
         GL11.glViewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
         GL11.glClearColor(this.clearColour[0], this.clearColour[1], this.clearColour[2], this.clearColour[3]);
         GL14.glBlendFuncSeparate(var2, var3, var4, var5);
         Gl.resetState();
      }
   }

   private void sprite(TreeAtlas.Slot var1, TextureID var2, float[] var3) {
      float var4 = 0.5F * var3[9];
      float var5 = 0.5F * var3[10];
      float var6 = var3[0] - var4;
      float var7 = var3[1] - var5;
      float var8 = var3[2] + var4;
      float var9 = var3[3] + var5;
      float var10 = 32.0F - var1.width * 0.5F;
      float var11 = 128.0F - var1.height;
      float var12 = var1.height - 16.0F;
      this.program
         .set(
            "uRect",
            ((var6 - var3[4]) / var3[6] - var10) / var1.width,
            ((var7 - var3[5]) / var3[7] - var11) / var12,
            ((var8 - var3[4]) / var3[6] - var10) / var1.width,
            ((var9 - var3[5]) / var3[7] - var11) / var12
         );
      this.program.set("uUv", var6, var7, var8, var9);
      Meshes.bindPage(var2);
      Gl.generated(6);
   }

   void bind(boolean var1) {
      Gl.bind(42, var1 ? this.texture : 0);
   }

   private void init() {
      GL13.glActiveTexture(34026);
      this.texture = GL11.glGenTextures();
      GL11.glBindTexture(3553, this.texture);
      GL42.glTexStorage2D(3553, 7, 32856, 2048, 2048);
      GL11.glTexParameteri(3553, 33085, 6);
      GL11.glTexParameteri(3553, 10241, 9987);
      GL11.glTexParameteri(3553, 10240, 9729);
      GL11.glTexParameteri(3553, 10242, 33071);
      GL11.glTexParameteri(3553, 10243, 33071);
      GL11.glBindTexture(3553, 0);
      GL13.glActiveTexture(33984);
      this.framebuffer = GL30.glGenFramebuffers();
      int var1 = GL11.glGetInteger(36006);
      GL30.glBindFramebuffer(36009, this.framebuffer);
      GL30.glFramebufferTexture2D(36009, 36064, 3553, this.texture, 0);
      GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      GL11.glClear(16384);
      GL30.glBindFramebuffer(36009, var1);
   }
}
