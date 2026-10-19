package viewpoint.platform;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.util.Locale;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import zombie.core.textures.TextureID;

record Hardware(
   String renderer,
   String vendor,
   int vramMiB,
   int vramFreeMiB,
   long ramMiB,
   long ramFreeMiB,
   long heapMiB,
   int threads,
   int width,
   int height,
   long gameTexturesMiB,
   boolean textureCompression,
   int isoChunksMiB,
   long processMiB
) {
   private static final long MIB = 1048576L;
   private static final long KIB = 1024L;

   Hardware(String var1, String var2, int var3, int var4, long var5, long var7, long var9, int var11, int var12, int var13) {
      this(var1, var2, var3, var4, var5, var7, var9, var11, var12, var13, 0L, true, 0, 0L);
   }

   static Hardware probe(int var0, int var1, int var2) {
      GLCapabilities var3 = GL.getCapabilities();
      String var4 = String.valueOf(GL11.glGetString(7937));
      int var5 = cardTotalMiB();
      int var6 = 0;
      if (var3.GL_NVX_gpu_memory_info) {
         var6 = (int)(GL11.glGetInteger(36937) / 1024L);
      } else if (var3.GL_ATI_meminfo) {
         int[] var7 = new int[4];
         GL11.glGetIntegerv(34812, var7);
         var6 = (int)(var7[0] / 1024L);
      }

      OperatingSystemMXBean var8 = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
      return new Hardware(
         var4,
         String.valueOf(GL11.glGetString(7936)),
         var5,
         var6,
         var8.getTotalMemorySize() / 1048576L,
         var8.getFreeMemorySize() / 1048576L,
         Runtime.getRuntime().maxMemory() / 1048576L,
         Runtime.getRuntime().availableProcessors(),
         var0,
         var1,
         (long)(TextureID.totalMemUsed / 1048576.0F),
         TextureID.useCompression,
         var2,
         var8.getCommittedVirtualMemorySize() / 1048576L
      );
   }

   static int cardTotalMiB() {
      int var0 = GL.getCapabilities().GL_NVX_gpu_memory_info ? (int)(GL11.glGetInteger(36935) / 1024L) : 0;
      return var0 > 0 ? var0 : GraphicsMemory.total(String.valueOf(GL11.glGetString(7937)));
   }

   double gpuScore() {
      return GpuScore.of(this.renderer);
   }

   boolean integrated() {
      return GpuScore.integrated(this.renderer);
   }

   double pixels() {
      return Math.max(0.25, (double)this.width * this.height / 2073600.0);
   }

   String describe() {
      return String.format(
         Locale.ROOT,
         "%s (%s), video memory %s, the game's textures %d MiB (compression %s), its isometric chunk textures %d MiB, memory %d MiB (%d free, the game's process %d), heap %d MiB, %d threads, %d x %d",
         this.renderer,
         this.vendor,
         this.vramMiB > 0
            ? this.vramMiB + " MiB (" + (this.vramFreeMiB > 0 ? this.vramFreeMiB + " free" : "free not told") + ")"
            : (this.vramFreeMiB > 0 ? this.vramFreeMiB + " MiB free" : "?"),
         this.gameTexturesMiB,
         this.textureCompression ? "on" : "off",
         this.isoChunksMiB,
         this.ramMiB,
         this.ramFreeMiB,
         this.processMiB,
         this.heapMiB,
         this.threads,
         this.width,
         this.height
      );
   }
}
