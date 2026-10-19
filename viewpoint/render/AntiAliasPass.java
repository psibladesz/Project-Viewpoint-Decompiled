package viewpoint.render;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;

final class AntiAliasPass {
   private static final String AREA = "/viewpoint/smaa/area.bin";
   private static final String SEARCH = "/viewpoint/smaa/search.bin";
   private static final int AREA_WIDTH = 160;
   private static final int AREA_HEIGHT = 560;
   private static final int SEARCH_WIDTH = 64;
   private static final int SEARCH_HEIGHT = 16;
   private final GlProgram fxaa = GlProgram.create("fxaa", "screen.vert", "fxaa.frag");
   private final GlProgram edges = GlProgram.create("smaa edges", "smaa_edges.vert", "smaa_edges.frag");
   private final GlProgram weights = GlProgram.create("smaa weights", "smaa_weights.vert", "smaa_weights.frag");
   private final GlProgram blend = GlProgram.create("smaa blend", "smaa_blend.vert", "smaa_blend.frag");
   private final Targets targets;
   private final int area;
   private final int search;

   AntiAliasPass(Targets var1) {
      this.targets = var1;
      this.fxaa.sampler("uColor", 0);
      this.edges.sampler("uColor", 0);
      this.weights.sampler("uEdges", 0);
      this.weights.sampler("uArea", 133);
      this.weights.sampler("uSearch", 134);
      this.blend.sampler("uColor", 0);
      this.blend.sampler("uWeights", 135);
      this.area = table("/viewpoint/smaa/area.bin", 33323, 33319, 160, 560, 2);
      this.search = table("/viewpoint/smaa/search.bin", 33321, 6403, 64, 16, 1);
   }

   int over(FrameContext var1, int var2) {
      if (!var1.pipeline.fxaa && !var1.pipeline.smaa) {
         return var2;
      }

      GlDebug.push("fxaa or smaa");
      GpuParts.begin(44);
      int var3 = var1.pipeline.fxaa ? this.fxaa(var1, var2) : this.smaa(var1, var2);
      GpuParts.end(44);
      GlDebug.pop();
      return var3;
   }

   int fxaa(FrameContext var1, int var2) {
      into(this.targets.antialiasedFbo, var1, false);
      this.fxaa.use();
      this.fxaa.set("uTexel", 1.0F / var1.width, 1.0F / var1.height);
      draw(var2);
      return this.targets.antialiased;
   }

   int smaa(FrameContext var1, int var2) {
      into(this.targets.smaaEdgesFbo, var1, true);
      this.edges.use();
      metrics(this.edges, var1);
      draw(var2);
      into(this.targets.smaaWeightsFbo, var1, true);
      this.weights.use();
      metrics(this.weights, var1);
      Gl.bind(133, this.area);
      Gl.bind(134, this.search);
      draw(this.targets.smaaEdges);
      Gl.bind(133, 0);
      Gl.bind(134, 0);
      into(this.targets.antialiasedFbo, var1, false);
      this.blend.use();
      metrics(this.blend, var1);
      Gl.bind(135, this.targets.smaaWeights);
      draw(var2);
      Gl.bind(135, 0);
      GL13.glActiveTexture(33984);
      return this.targets.antialiased;
   }

   private static void into(int var0, FrameContext var1, boolean var2) {
      GL30.glBindFramebuffer(36160, var0);
      GL11.glViewport(0, 0, var1.width, var1.height);
      if (var2) {
         GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         GL11.glClear(16384);
      }
   }

   private static void metrics(GlProgram var0, FrameContext var1) {
      var0.set("uMetrics", 1.0F / var1.width, 1.0F / var1.height, var1.width, var1.height);
   }

   private static void draw(int var0) {
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, var0);
      Gl.screenTriangle();
      GL11.glBindTexture(3553, 0);
   }

   private static int table(String var0, int var1, int var2, int var3, int var4, int var5) {
      byte[] var6;
      try (InputStream var7 = AntiAliasPass.class.getResourceAsStream(var0)) {
         if (var7 == null) {
            throw new IllegalStateException("SMAA's table is missing: " + var0);
         }

         var6 = var7.readAllBytes();
      } catch (IOException var13) {
         throw new IllegalStateException("SMAA's table could not be read: " + var0, var13);
      }

      if (var6.length != var3 * var4 * var5) {
         throw new IllegalStateException("SMAA's table is not " + var3 + " x " + var4 + ": " + var0);
      }

      ByteBuffer var14 = BufferUtils.createByteBuffer(var6.length).put(var6).flip();
      int var8 = GL11.glGetInteger(35055);
      int var9 = GL11.glGetInteger(3317);
      GL15.glBindBuffer(35052, 0);
      GL11.glPixelStorei(3317, 1);
      int var10 = Gl.texture(var1, var3, var4, var2, 5121, 9729);
      GL11.glTexSubImage2D(3553, 0, 0, 0, var3, var4, var2, 5121, var14);
      GL11.glBindTexture(3553, 0);
      GL15.glBindBuffer(35052, var8);
      GL11.glPixelStorei(3317, var9);
      return var10;
   }
}
