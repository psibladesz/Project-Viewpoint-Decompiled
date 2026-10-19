package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import zombie.core.textures.TextureID;

final class FloorBaker {
   private static final int FLOATS = 16;
   private final GlProgram program = GlProgram.create("floor bake", "floor_bake.vert", "floor_bake.frag");
   private final GlProgram surface = GlProgram.create("floor surface", "screen.vert", "floor_surface.frag");
   private int vao;
   private int framebuffer;
   private final HashMap<Integer, Integer> scratch = new HashMap<>();
   private final HashMap<Integer, int[]> kindScratch = new HashMap<>();
   private FloatBuffer layers = BufferUtils.createFloatBuffer(8192);
   private TextureID[] runPages = new TextureID[64];
   private int[] runFirst = new int[64];
   private int[] runCount = new int[64];
   private int runs;
   private final int[] viewport = new int[4];
   private final float[] clearColour = new float[4];
   int layersBaked;

   FloorBaker() {
      this.surface.sampler("uColour", 0);
      this.surface.sampler("uMaterial", 137);
      this.surface.sampler("uStones", 138);
   }

   void bake(FloorBakeSource var1, FloorSlices var2, int var3) {
      this.bake(var1, var2, var3, 0, 0);
   }

   void bake(FloorBakeSource var1, FloorSlices var2, int var3, int var4, int var5) {
      this.collect(var1);
      if (this.vao == 0) {
         this.init();
      }

      int var6 = GL11.glGetInteger(36006);
      GL11.glGetIntegerv(2978, this.viewport);
      GL11.glGetFloatv(3106, this.clearColour);
      int var7 = GL11.glGetInteger(32969);
      int var8 = GL11.glGetInteger(32968);
      int var9 = GL11.glGetInteger(32971);
      int var10 = GL11.glGetInteger(32970);
      int var11 = this.scratch.computeIfAbsent(var2.size, var1x -> scratchPage(var1x, var2.mips));
      int[] var12 = var2.surfaces ? this.kindScratch.computeIfAbsent(var2.size, var1x -> kindScratch(var1x, var2.mips)) : null;
      GL30.glBindFramebuffer(36009, this.framebuffer);
      attach(var11, var12 == null ? 0 : var12[0], var12 == null ? 0 : var12[1]);
      GL11.glViewport(0, 0, var2.size, var2.size);
      GL11.glDisable(2929);
      GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      GL11.glClear(16384);
      GL11.glEnable(3042);
      GL11.glBlendFunc(1, 771);
      if (this.runs > 0) {
         this.draw(var2, var1.tiles());
      }

      mipmaps(var11);
      if (var12 != null) {
         this.surface(var2, var1.tiles(), var11, var12, var4, var5);
      }

      GL30.glBindFramebuffer(36009, var6);
      copy(var11, var2.texture(var3), var2, var3);
      if (var12 != null) {
         copy(var12[2], var2.surface(var3), var2, var3);
      }

      GL11.glViewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
      GL11.glClearColor(this.clearColour[0], this.clearColour[1], this.clearColour[2], this.clearColour[3]);
      GL14.glBlendFuncSeparate(var7, var8, var9, var10);
      Gl.resetState();
   }

   private void surface(FloorSlices var1, int var2, int var3, int[] var4, int var5, int var6) {
      attach(var4[2], 0, 0);
      GL11.glDisable(3042);
      this.surface.use();
      this.surface.set("uOrigin", var5, var6);
      this.surface.set("uTexelsPerTile", (float)var1.size / var2);
      Gl.bind(137, var4[0]);
      Gl.bind(138, var4[1]);
      Gl.bind(0, var3);
      GL33.glBindSampler(0, 0);
      Gl.screenTriangle();
      Gl.bind(137, 0);
      Gl.bind(138, 0);
      Gl.bind(0, 0);
      mipmaps(var4[2]);
   }

   private static void attach(int var0, int var1, int var2) {
      GL30.glFramebufferTexture2D(36009, 36064, 3553, var0, 0);
      GL30.glFramebufferTexture2D(36009, 36065, 3553, var1, 0);
      GL30.glFramebufferTexture2D(36009, 36066, 3553, var2, 0);
      GL20.glDrawBuffers(var1 == 0 ? new int[]{36064} : new int[]{36064, 36065, 36066});
   }

   private static void mipmaps(int var0) {
      GL11.glBindTexture(3553, var0);
      GL30.glGenerateMipmap(3553);
      GL11.glBindTexture(3553, 0);
   }

   private static void copy(int var0, int var1, FloorSlices var2, int var3) {
      for (int var4 = 0; var4 < var2.mips; var4++) {
         int var5 = Math.max(1, var2.size >> var4);
         GL43.glCopyImageSubData(var0, 3553, var4, 0, 0, 0, var1, 35866, var4, 0, 0, FloorSlices.layer(var3), var5, var5, 1);
      }
   }

   private void draw(FloorSlices var1, int var2) {
      long var3 = FrameStream.put(this.layers, 16);
      this.program.use();
      this.program.setInt("uSource", 0);
      this.program.set("uTexelsPerTile", (float)var1.size / var2);
      this.program.set("uTiles", var2);
      this.program.setInt("uBakeFilter", FloorFilter.bakeFilter());
      GL30.glBindVertexArray(this.vao);
      GL43.glBindVertexBuffer(0, FrameStream.buffer(), var3, 64);

      for (int var5 = 0; var5 < this.runs; var5++) {
         Meshes.bindPage(this.runPages[var5]);
         GL42.glDrawArraysInstancedBaseInstance(4, 0, 6, this.runCount[var5], this.runFirst[var5]);
      }

      GL30.glBindVertexArray(0);
      GL20.glUseProgram(0);

      for (int var6 = 0; var6 < this.runs; var6++) {
         this.runPages[var6] = null;
      }
   }

   private void collect(FloorBakeSource var1) {
      this.layers.clear();
      this.runs = 0;
      int var2 = 0;
      int var3 = var1.tiles();

      for (int var4 = 0; var4 < 2; var4++) {
         boolean var5 = var4 == 0;

         for (int var6 = 0; var6 < var3; var6++) {
            int var7 = var5 ? var3 - 1 - var6 : var6;

            for (int var8 = 0; var8 < var3; var8++) {
               FloorLayers var9 = var1.artAt(var8, var7);
               if (var9 != null) {
                  int var10 = FloorPage.squareAt(var8, var7);

                  for (int var11 = var9.first(var10); var11 < var9.end(var10); var11++) {
                     if (var9.has(var11, 2) == var5) {
                        this.add(var9, var11, var8 - 1, var7 - 1, var2++);
                     }
                  }
               }
            }
         }
      }

      this.layers.flip();
      this.layersBaked += var2;
   }

   private void add(FloorLayers var1, int var2, int var3, int var4, int var5) {
      if (this.layers.remaining() < 16) {
         FloatBuffer var6 = BufferUtils.createFloatBuffer(this.layers.capacity() * 2);
         this.layers.flip();
         this.layers = var6.put(this.layers);
      }

      for (int var7 = 0; var7 <= 3; var7++) {
         this.layers.put(var1.number(var2, var7));
      }

      for (int var8 = 6; var8 <= 13; var8++) {
         this.layers.put(var1.number(var2, var8));
      }

      this.layers.put(var3).put(var4);
      this.layers.put(var1.has(var2, 2) ? 1.0F : 0.0F).put((var1.has(var2, 1) ? 1.0F : 0.0F) + 2.0F * var1.material(var2));
      TextureID var9 = var1.atlas(var2);
      if (this.runs > 0 && this.runPages[this.runs - 1] == var9) {
         this.runCount[this.runs - 1]++;
      } else {
         if (this.runs == this.runPages.length) {
            this.runPages = Arrays.copyOf(this.runPages, this.runs * 2);
            this.runFirst = Arrays.copyOf(this.runFirst, this.runs * 2);
            this.runCount = Arrays.copyOf(this.runCount, this.runs * 2);
         }

         this.runPages[this.runs] = var9;
         this.runFirst[this.runs] = var5;
         this.runCount[this.runs] = 1;
         this.runs++;
      }
   }

   private void init() {
      this.framebuffer = GL30.glGenFramebuffers();
      this.vao = GL30.glGenVertexArrays();
      GL30.glBindVertexArray(this.vao);
      int[] var1 = new int[]{4, 4, 4, 2, 2};
      int var2 = 0;

      for (int var3 = 0; var3 < var1.length; var3++) {
         GL20.glEnableVertexAttribArray(var3);
         GL43.glVertexAttribFormat(var3, var1[var3], 5126, false, var2 * 4);
         GL43.glVertexAttribBinding(var3, 0);
         var2 += var1[var3];
      }

      GL43.glVertexBindingDivisor(0, 1);
      GL30.glBindVertexArray(0);
   }

   private static int scratchPage(int var0, int var1) {
      int var2 = GL11.glGenTextures();
      GL11.glBindTexture(3553, var2);
      GL42.glTexStorage2D(3553, var1, 32856, var0, var0);
      GL11.glTexParameteri(3553, 33085, var1 - 1);
      GL11.glTexParameteri(3553, 10241, 9987);
      GL11.glBindTexture(3553, 0);
      return var2;
   }

   private static int[] kindScratch(int var0, int var1) {
      return new int[]{scratchPage(var0, 1), scratchPage(var0, 1), scratchPage(var0, var1)};
   }

   void clearScratch() {
      for (int var2 : this.scratch.values()) {
         GL11.glDeleteTextures(var2);
      }

      for (int[] var4 : this.kindScratch.values()) {
         GL11.glDeleteTextures(var4);
      }

      this.scratch.clear();
      this.kindScratch.clear();
   }
}
