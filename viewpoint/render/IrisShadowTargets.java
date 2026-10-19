package viewpoint.render;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

final class IrisShadowTargets {
   static final int COLORS = 8;
   int size;
   int depth;
   int depthOpaque;
   final int[] colors = new int[8];
   final IrisFormats.Format[] formats = new IrisFormats.Format[8];
   final float[][] clearColors = new float[8][];
   final boolean[] used = new boolean[8];
   final boolean[] clear = new boolean[8];
   final boolean[] mipmap = new boolean[8];
   long bytes;
   private final Map<String, Integer> fbos = new HashMap<>();
   private int copyRead;
   private int copyDraw;

   IrisShadowTargets() {
      for (int var1 = 0; var1 < 8; var1++) {
         this.formats[var1] = IrisFormats.format("RGBA8");
         this.clearColors[var1] = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
         this.clear[var1] = true;
      }
   }

   void ensure(int var1) {
      if (var1 != this.size || this.depth == 0) {
         GL13.glActiveTexture(34079);
         this.release();
         this.size = var1;
         this.depth = IrisTargets.depthTexture(this.size, this.size);
         this.depthOpaque = IrisTargets.depthTexture(this.size, this.size);

         for (int var5 : new int[]{this.depth, this.depthOpaque}) {
            GL11.glBindTexture(3553, var5);
            GL11.glTexParameteri(3553, 10241, 9729);
            GL11.glTexParameteri(3553, 10240, 9729);
         }

         this.bytes = 2L * this.size * this.size * 4L;

         for (int var6 = 0; var6 < 8; var6++) {
            if (this.used[var6]) {
               IrisFormats.Format var7 = this.formats[var6];
               this.colors[var6] = IrisTargets.colour(var7, this.size, this.size, this.mipmap[var6]);
               this.bytes = this.bytes + (long)this.size * this.size * var7.bytes();
            }
         }

         GL11.glBindTexture(3553, 0);
         this.copyRead = GL30.glGenFramebuffers();
         this.copyDraw = GL30.glGenFramebuffers();
         GL13.glActiveTexture(33984);
      }
   }

   int framebuffer(List<Integer> var1) {
      return this.fbos.computeIfAbsent(var1.toString(), var2 -> {
         int var3 = GL30.glGenFramebuffers();
         GL30.glBindFramebuffer(36160, var3);
         GL30.glFramebufferTexture2D(36160, 36096, 3553, this.depth, 0);
         IntBuffer var4 = BufferUtils.createIntBuffer(Math.max(var1.size(), 1));

         for (int var5 = 0; var5 < var1.size(); var5++) {
            int var6 = (Integer)var1.get(var5);
            if (var6 < 8 && this.colors[var6] != 0) {
               GL30.glFramebufferTexture2D(36160, 36064 + var5, 3553, this.colors[var6], 0);
               var4.put(36064 + var5);
            } else {
               var4.put(0);
            }
         }

         var4.flip();
         if (var1.isEmpty()) {
            GL11.glDrawBuffer(0);
         } else {
            GL20.glDrawBuffers(var4);
         }

         return var3;
      });
   }

   void clear() {
      for (int var1 = 0; var1 < 8; var1++) {
         if (this.used[var1] && this.clear[var1]) {
            GL30.glBindFramebuffer(36160, this.framebuffer(List.of(var1)));
            float[] var2 = this.clearColors[var1];
            GL11.glClearColor(var2[0], var2[1], var2[2], var2[3]);
            GL11.glClear(16384);
         }
      }

      GL30.glBindFramebuffer(36160, this.framebuffer(List.of()));
      GL11.glClearDepth(1.0);
      GL11.glClear(256);
   }

   void copyOpaque() {
      GL30.glBindFramebuffer(36008, this.copyRead);
      GL30.glFramebufferTexture2D(36008, 36096, 3553, this.depth, 0);
      GL30.glBindFramebuffer(36009, this.copyDraw);
      GL30.glFramebufferTexture2D(36009, 36096, 3553, this.depthOpaque, 0);
      GL30.glBlitFramebuffer(0, 0, this.size, this.size, 0, 0, this.size, this.size, 256, 9728);
   }

   void mipmaps() {
      GL13.glActiveTexture(34079);

      for (int var1 = 0; var1 < 8; var1++) {
         if (this.used[var1] && this.mipmap[var1]) {
            GL11.glBindTexture(3553, this.colors[var1]);
            GL30.glGenerateMipmap(3553);
         }
      }

      GL11.glBindTexture(3553, 0);
      GL13.glActiveTexture(33984);
   }

   void release() {
      for (int var2 : this.fbos.values()) {
         GL30.glDeleteFramebuffers(var2);
      }

      this.fbos.clear();

      for (int var3 = 0; var3 < 8; var3++) {
         if (this.colors[var3] != 0) {
            GL11.glDeleteTextures(this.colors[var3]);
            this.colors[var3] = 0;
         }
      }

      if (this.depth != 0) {
         GL11.glDeleteTextures(this.depth);
         GL11.glDeleteTextures(this.depthOpaque);
         GL30.glDeleteFramebuffers(this.copyRead);
         GL30.glDeleteFramebuffers(this.copyDraw);
      }

      this.depth = this.depthOpaque = this.copyRead = this.copyDraw = this.size = 0;
   }
}
