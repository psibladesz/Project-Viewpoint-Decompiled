package viewpoint.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;

final class CloudShadowMap {
   static final int SIZE = 512;
   private static final double TEXEL = 16.0;
   private static final double SPAN = 8192.0;
   private final GlProgram program = GlProgram.create("cloud shadow map", "screen.vert", "cloud_shadow_map.frag");
   private final int texture;
   private final int fbo;
   private double cornerX;
   private double cornerZ;
   private double plane;

   CloudShadowMap() {
      int var1 = GL11.glGetInteger(32873);
      int var2 = GL11.glGetInteger(36006);
      this.texture = Gl.texture(33325, 512, 512, 6403, 5126, 9729);
      GL11.glBindTexture(3553, var1);
      this.fbo = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, this.fbo);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, this.texture, 0);
      GL11.glClearColor(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glClear(16384);
      GL30.glBindFramebuffer(36160, var2);
   }

   void draw(FrameContext var1) {
      SceneData var2 = var1.scene;
      if (!(var2.sunY <= 0.0F)) {
         this.cornerX = Math.floor((var2.originX - 4096.0) / 16.0) * 16.0;
         this.cornerZ = Math.floor((var2.originZ - 4096.0) / 16.0) * 16.0;
         this.plane = Math.floor(var2.originY / 16.0) * 16.0;
         GpuParts.begin(43);
         GL30.glBindFramebuffer(36160, this.fbo);
         GL11.glViewport(0, 0, 512, 512);
         this.program.use();
         unit(this.program, "uWeatherMap", 14);
         unit(this.program, "uCloudNoise", 18);
         this.place(this.program, var2);
         this.program.set("uSunDir", var2.sunX, var2.sunY, var2.sunZ);
         SkyPass.cloudUniforms(this.program, var2);
         SkyPass.weatherUniforms(this.program, var2);
         Gl.screenTriangle();
         GpuParts.end(43);
      }
   }

   void bind(boolean var1) {
      Gl.bind(130, var1 ? this.texture : 0);
   }

   void place(GlProgram var1, SceneData var2) {
      unit(var1, "uCloudShadow", 130);
      if (var1.has("uCloudShadowPlace")) {
         var1.set(
            "uCloudShadowPlace", (float)(this.cornerX - var2.originX), (float)(this.cornerZ - var2.originZ), (float)(this.plane - var2.originY), 1.2207031E-4F
         );
      }
   }

   private static void unit(GlProgram var0, String var1, int var2) {
      if (var0.has(var1)) {
         var0.setInt(var1, var2);
      }
   }
}
