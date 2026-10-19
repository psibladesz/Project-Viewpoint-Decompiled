package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;

final class RadianceCascades {
   static final int SIDE = 128;
   static final int COUNT = 4;
   private final GlProgram cascade = GlProgram.create("bounce cascade", "screen.vert", "bounce_cascade.frag");
   private final GlProgram ways = GlProgram.create("bounce ways", "screen.vert", "bounce_ways.frag");
   private final int[] textures = new int[2];
   private final int[] walls = new int[2];
   private final int[] fbos = new int[2];

   RadianceCascades() {
      this.cascade.sampler("uEmit", 0);
      this.cascade.sampler("uEmitWalls", 127);
      this.cascade.sampler("uUpper", 112);
      this.cascade.sampler("uUpperWalls", 128);
      this.ways.sampler("uCascade0", 0);
      this.ways.sampler("uCascade0Walls", 128);
      int var1 = GL11.glGetInteger(32873);
      int var2 = GL11.glGetInteger(36006);

      for (int var3 = 0; var3 < 2; var3++) {
         this.textures[var3] = Gl.texture(34842, 128, 128, 6408, 5126, 9728);
         this.walls[var3] = Gl.texture(34842, 128, 128, 6408, 5126, 9728);
         this.fbos[var3] = BounceField.framebuffer(this.textures[var3], this.walls[var3]);
      }

      GL11.glBindTexture(3553, var1);
      GL30.glBindFramebuffer(36160, var2);
   }

   void draw(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      GL11.glViewport(0, 0, 128, 128);
      this.cascade.use();
      this.cascade.set("uCorner", var6, var7);
      this.cascade.setInt("uPlanLevel", var8);
      Gl.bind(127, var2);
      Gl.bind(0, var1);
      int var9 = -1;

      for (int var10 = 3; var10 >= 0; var10--) {
         int var11 = var10 % 2;
         GL30.glBindFramebuffer(36160, this.fbos[var11]);
         this.cascade.setInt("uCascade", var10);
         Gl.bind(112, var9 < 0 ? 0 : this.textures[var9]);
         Gl.bind(128, var9 < 0 ? 0 : this.walls[var9]);
         Gl.screenTriangle();
         var9 = var11;
      }

      Gl.bind(127, 0);
      Gl.bind(112, 0);
      GL30.glBindFramebuffer(36160, var3);
      GL11.glViewport(0, var5 * var4, var4 * 6, var4);
      this.ways.use();
      Gl.bind(128, this.walls[var9]);
      Gl.bind(0, this.textures[var9]);
      Gl.screenTriangle();
      Gl.bind(128, 0);
      Gl.bind(0, 0);
   }
}
