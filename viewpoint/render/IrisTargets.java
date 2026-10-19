package viewpoint.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL44;

final class IrisTargets {
   static final int COLORTEX = 32;
   final IrisTargets.Buffer[] buffers = new IrisTargets.Buffer[32];
   int width;
   int height;
   int depth;
   int depthOpaque;
   int depthNoHand;
   int farDepth;
   int farDepthOpaque;
   int tint;
   private final Map<String, Integer> fbos = new HashMap<>();
   private int copyRead;
   private int copyDraw;
   long bytes;

   IrisTargets() {
      for (int var1 = 0; var1 < 32; var1++) {
         this.buffers[var1] = new IrisTargets.Buffer();
         this.buffers[var1].clearColor = var1 == 1 ? new float[]{1.0F, 1.0F, 1.0F, 1.0F} : new float[]{0.0F, 0.0F, 0.0F, var1 == 0 ? 1.0F : 0.0F};
      }
   }

   String ensure(int var1, int var2) {
      if (var1 == this.width && var2 == this.height && this.depth != 0) {
         return null;
      }

      GL13.glActiveTexture(34079);
      this.release();
      this.width = var1;
      this.height = var2;
      this.bytes = 0L;
      String var3 = null;

      for (int var4 = 0; var4 < 32; var4++) {
         IrisTargets.Buffer var5 = this.buffers[var4];
         if (var5.used) {
            var5.width = var5.fixed ? (int)var5.scaleX : Math.max(1, Math.round(var1 * var5.scaleX));
            var5.height = var5.fixed ? (int)var5.scaleY : Math.max(1, Math.round(var2 * var5.scaleY));

            for (int var6 = 0; var6 < 2; var6++) {
               var5.texture[var6] = colour(var5.format, var5.width, var5.height, var5.mipmap);
               var3 = var3 != null ? var3 : unmade(var5.texture[var6], var5.width, var5.height, "colortex" + var4);
            }

            var5.read = 0;
            this.bytes = this.bytes + 2L * var5.width * var5.height * var5.format.bytes() * (var5.mipmap ? 4 : 3) / 3L;
         }
      }

      this.depth = depthTexture(var1, var2);
      this.depthOpaque = depthTexture(var1, var2);
      this.depthNoHand = depthTexture(var1, var2);
      this.farDepth = depthTexture(var1, var2);
      this.farDepthOpaque = depthTexture(var1, var2);
      this.tint = colour(IrisFormats.format("RGBA8"), var1, var2, false);
      this.bytes += 5L * var1 * var2 * 4L + (long)var1 * var2 * 4L;
      this.copyRead = GL30.glGenFramebuffers();
      this.copyDraw = GL30.glGenFramebuffers();
      int[] var7 = new int[]{this.depth, this.depthOpaque, this.depthNoHand, this.farDepth, this.farDepthOpaque, this.tint};

      for (int var8 = 0; var8 < var7.length && var3 == null; var8++) {
         var3 = unmade(var7[var8], var1, var2, var8 < var7.length - 1 ? "a depth texture" : "the hue target");
      }

      GL13.glActiveTexture(33984);
      return var3;
   }

   private static String unmade(int var0, int var1, int var2, String var3) {
      GL11.glBindTexture(3553, var0);
      boolean var4 = GL11.glGetTexLevelParameteri(3553, 0, 4096) == var1 && GL11.glGetTexLevelParameteri(3553, 0, 4097) == var2;
      GL11.glBindTexture(3553, 0);
      return var4 ? null : var3 + " (" + var1 + " x " + var2 + ")";
   }

   int framebuffer(List<Integer> var1, boolean var2, int var3, int var4) {
      StringBuilder var5 = new StringBuilder(var2 ? "o" : "c").append(var3).append(':').append(var4);

      for (int var7 : var1) {
         var5.append(',').append(var7).append(var2 ? this.buffers[var7].other() : this.buffers[var7].current());
      }

      return this.fbos.computeIfAbsent(var5.toString(), var5x -> this.make(var1, var2, var3, var4));
   }

   private int make(List<Integer> var1, boolean var2, int var3, int var4) {
      int var5 = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, var5);
      int var6 = Math.max(var1.size(), var4 + 1);
      IntBuffer var7 = BufferUtils.createIntBuffer(Math.max(var6, 1));

      for (int var8 = 0; var8 < var1.size(); var8++) {
         IrisTargets.Buffer var9 = this.buffers[var1.get(var8)];
         int var10 = var2 ? var9.other() : var9.current();
         GL30.glFramebufferTexture2D(36160, 36064 + var8, 3553, var10, 0);
         var7.put(36064 + var8);
      }

      if (var4 >= 0) {
         GL30.glFramebufferTexture2D(36160, 36064 + var4, 3553, this.tint, 0);

         for (int var11 = var1.size(); var11 < var4; var11++) {
            var7.put(0);
         }

         var7.put(36064 + var4);
      }

      int var12 = var3 == 1 ? this.depth : (var3 == 2 ? this.farDepth : 0);
      if (var12 != 0) {
         GL30.glFramebufferTexture2D(36160, 36096, 3553, var12, 0);
      }

      var7.flip();
      if (var7.remaining() == 0) {
         GL11.glDrawBuffer(0);
      } else {
         GL20.glDrawBuffers(var7);
      }

      return var5;
   }

   void flip(int var1) {
      IrisTargets.Buffer var2 = this.buffers[var1];
      var2.read = 1 - var2.read;
      this.mipmaps(var1);
   }

   void mipmaps(int var1) {
      GL13.glActiveTexture(34079);
      IrisTargets.Buffer var2 = this.buffers[var1];
      if (var2.mipmap) {
         GL11.glBindTexture(3553, var2.current());
         GL30.glGenerateMipmap(3553);
         GL11.glBindTexture(3553, 0);
      }

      GL13.glActiveTexture(33984);
   }

   void clear(float var1, float var2, float var3) {
      for (int var4 = 0; var4 < 32; var4++) {
         IrisTargets.Buffer var5 = this.buffers[var4];
         if (var5.used && var5.clear) {
            float[] var6 = var4 == 0 && var5.clearColor[0] == 0.0F && var5.clearColor[1] == 0.0F && var5.clearColor[2] == 0.0F
               ? new float[]{var1, var2, var3, 1.0F}
               : var5.clearColor;
            GL30.glBindFramebuffer(36160, this.framebuffer(List.of(var4), false, 0, -1));
            GL11.glViewport(0, 0, var5.width, var5.height);
            if (var5.format.integer()) {
               GL30.glClearBufferuiv(6144, 0, new int[]{(int)var6[0], (int)var6[1], (int)var6[2], (int)var6[3]});
            } else {
               GL11.glClearColor(var6[0], var6[1], var6[2], var6[3]);
               GL11.glClear(16384);
            }
         }
      }
   }

   void copyDepth(int var1, int var2) {
      GL30.glBindFramebuffer(36008, this.copyRead);
      GL30.glFramebufferTexture2D(36008, 36096, 3553, var1, 0);
      GL30.glBindFramebuffer(36009, this.copyDraw);
      GL30.glFramebufferTexture2D(36009, 36096, 3553, var2, 0);
      GL30.glBlitFramebuffer(0, 0, this.width, this.height, 0, 0, this.width, this.height, 256, 9728);
   }

   void release() {
      for (int var2 : this.fbos.values()) {
         GL30.glDeleteFramebuffers(var2);
      }

      this.fbos.clear();

      for (IrisTargets.Buffer var4 : this.buffers) {
         for (int var5 = 0; var5 < 2; var5++) {
            if (var4.texture[var5] != 0) {
               GL11.glDeleteTextures(var4.texture[var5]);
               var4.texture[var5] = 0;
            }
         }
      }

      for (int var11 : new int[]{this.depth, this.depthOpaque, this.depthNoHand, this.farDepth, this.farDepthOpaque, this.tint}) {
         if (var11 != 0) {
            GL11.glDeleteTextures(var11);
         }
      }

      if (this.copyRead != 0) {
         GL30.glDeleteFramebuffers(this.copyRead);
         GL30.glDeleteFramebuffers(this.copyDraw);
      }

      this.depth = this.depthOpaque = this.depthNoHand = this.farDepth = this.farDepthOpaque = this.tint = this.copyRead = this.copyDraw = 0;
      this.width = this.height = 0;
   }

   static int colour(IrisFormats.Format var0, int var1, int var2, boolean var3) {
      int var4 = GL11.glGenTextures();
      GL11.glBindTexture(3553, var4);
      GL11.glTexImage2D(3553, 0, var0.internal(), var1, var2, 0, var0.pixels(), var0.type(), (ByteBuffer)null);
      GL44.glClearTexImage(var4, 0, var0.pixels(), var0.type(), (ByteBuffer)null);
      int var5 = var0.integer() ? 9728 : 9729;
      GL11.glTexParameteri(3553, 10241, var3 && !var0.integer() ? 9987 : var5);
      GL11.glTexParameteri(3553, 10240, var5);
      GL11.glTexParameteri(3553, 10242, 33071);
      GL11.glTexParameteri(3553, 10243, 33071);
      if (var3) {
         GL30.glGenerateMipmap(3553);
      }

      GL11.glBindTexture(3553, 0);
      return var4;
   }

   static int depthTexture(int var0, int var1) {
      int var2 = GL11.glGenTextures();
      GL11.glBindTexture(3553, var2);
      GL11.glTexImage2D(3553, 0, 36012, var0, var1, 0, 6402, 5126, (ByteBuffer)null);
      GL11.glTexParameteri(3553, 10241, 9728);
      GL11.glTexParameteri(3553, 10240, 9728);
      GL11.glTexParameteri(3553, 10242, 33071);
      GL11.glTexParameteri(3553, 10243, 33071);
      GL11.glTexParameteri(3553, 34892, 0);
      GL11.glBindTexture(3553, 0);
      return var2;
   }

   static final class Buffer {
      final int[] texture = new int[2];
      int read;
      IrisFormats.Format format = IrisFormats.format("RGBA8");
      float scaleX = 1.0F;
      float scaleY = 1.0F;
      boolean fixed;
      int width;
      int height;
      boolean clear = true;
      float[] clearColor;
      boolean mipmap;
      boolean used;

      int current() {
         return this.texture[this.read];
      }

      int other() {
         return this.texture[1 - this.read];
      }
   }
}
