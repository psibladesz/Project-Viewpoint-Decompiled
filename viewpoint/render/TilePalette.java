package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import zombie.core.textures.Texture;
import zombie.core.textures.TextureID;

public final class TilePalette {
   public static final int SIDE = 256;
   public static final int MOST = 65535;
   private static final ConcurrentLinkedQueue<TilePalette.Entry> QUEUE = new ConcurrentLinkedQueue<>();
   private static final int PER_DRAW = 64;
   private static GlProgram program;
   private static int texture;
   private static int fbo;
   private static final ArrayList<TilePalette.Entry> drawing = new ArrayList<>();
   private static final FloatBuffer rects = BufferUtils.createFloatBuffer(256);
   private static final FloatBuffer numbers = BufferUtils.createFloatBuffer(64);
   private static final int[] viewport = new int[4];

   public static void average(int var0, TextureID var1, float var2, float var3, float var4, float var5) {
      QUEUE.add(new TilePalette.Entry(var0, var1, 0, var2, var3, var4, var5));
   }

   static void averageTexture(int var0, int var1, float var2, float var3, float var4, float var5) {
      QUEUE.add(new TilePalette.Entry(var0, null, var1, var2, var3, var4, var5));
   }

   static void update() {
      if (texture == 0) {
         make();
      }

      drawing.clear();

      TilePalette.Entry var0;
      while ((var0 = QUEUE.poll()) != null) {
         drawing.add(var0);
      }

      if (!drawing.isEmpty()) {
         drawing.sort((var0x, var1x) -> Integer.compare(var0x.order(), var1x.order()));
         int var6 = GL11.glGetInteger(36006);
         GL11.glGetIntegerv(2978, viewport);
         boolean var1 = GL11.glIsEnabled(2929);
         boolean var2 = GL11.glIsEnabled(3042);
         GL11.glDisable(2929);
         GL11.glDisable(3042);
         GL30.glBindFramebuffer(36160, fbo);
         GL11.glViewport(0, 0, 256, 256);
         program.use();
         GL13.glActiveTexture(33984);
         int var3 = 0;

         while (var3 < drawing.size()) {
            TilePalette.Entry var4 = drawing.get(var3);
            int var5 = var3;

            while (var5 < drawing.size() && var5 - var3 < 64 && drawing.get(var5).sameArt(var4)) {
               var5++;
            }

            draw(var4, var3, var5);
            var3 = var5;
         }

         GL33.glBindSampler(0, 0);
         GL11.glBindTexture(3553, 0);
         Texture.lastTextureID = -1;
         GL30.glBindFramebuffer(36160, var6);
         GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
         restore(2929, var1);
         restore(3042, var2);
      }
   }

   private static void draw(TilePalette.Entry var0, int var1, int var2) {
      rects.clear();
      numbers.clear();

      for (int var3 = var1; var3 < var2; var3++) {
         TilePalette.Entry var4 = drawing.get(var3);
         rects.put(var4.x0).put(var4.y0).put(var4.x1).put(var4.y1);
         numbers.put(var4.id);
      }

      rects.flip();
      numbers.flip();
      program.setVec4s("uRects", rects);
      program.setFloats("uIds", numbers);
      if (var0.page != null) {
         Meshes.bindPage(var0.page);
      } else {
         GL11.glBindTexture(3553, var0.texture);
         GL33.glBindSampler(0, 0);
      }

      Gl.generated(6 * (var2 - var1));
   }

   static float[] colour(int var0) {
      FloatBuffer var1 = BufferUtils.createFloatBuffer(262144);
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, texture);
      GL11.glGetTexImage(3553, 0, 6408, 5126, var1);
      GL11.glBindTexture(3553, 0);
      return new float[]{var1.get(var0 * 4), var1.get(var0 * 4 + 1), var1.get(var0 * 4 + 2), var1.get(var0 * 4 + 3)};
   }

   static void bind() {
      Gl.bind(113, texture);
      GL13.glActiveTexture(33984);
   }

   private static void make() {
      program = GlProgram.create("tile palette", "tile_palette.vert", "tile_palette.frag");
      program.sampler("uPage", 0);
      int var0 = GL11.glGetInteger(32873);
      int var1 = GL11.glGetInteger(36006);
      texture = Gl.texture(34842, 256, 256, 6408, 5126, 9728);
      GL11.glBindTexture(3553, var0);
      fbo = BounceField.framebuffer(texture);
      GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      GL11.glClear(16384);
      GL30.glBindFramebuffer(36160, var1);
   }

   private static void restore(int var0, boolean var1) {
      if (var1) {
         GL11.glEnable(var0);
      } else {
         GL11.glDisable(var0);
      }
   }

   private TilePalette() {
   }

   private record Entry(int id, TextureID page, int texture, float x0, float y0, float x1, float y1) {
      boolean sameArt(TilePalette.Entry var1) {
         return this.page == var1.page && this.texture == var1.texture;
      }

      int order() {
         return this.page != null ? System.identityHashCode(this.page) : this.texture;
      }
   }
}
