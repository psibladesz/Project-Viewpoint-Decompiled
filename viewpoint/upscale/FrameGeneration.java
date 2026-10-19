package viewpoint.upscale;

import org.lwjgl.opengl.GL11;
import org.lwjgl.vulkan.VkCommandBuffer;

public final class FrameGeneration implements AutoCloseable {
   public static final NativeFiles.Set FILES = NativeFiles.Set.FIDELITY_FX;
   public final int screenWidth;
   public final int screenHeight;
   public final int frameWidth;
   public final int frameHeight;
   private final VulkanDevice vk;
   private final Crossing crossing;
   private final FrameGeneration.Images images;
   private final SharedImage[] all;
   private final FfxGeneration ffx;

   private FrameGeneration(int var1, int var2, int var3, int var4) {
      this.screenWidth = var1;
      this.screenHeight = var2;
      this.frameWidth = var3;
      this.frameHeight = var4;
      this.vk = VulkanDevice.open(FsrVendor.NEEDS);
      this.crossing = new Crossing(this.vk);
      this.images = new FrameGeneration.Images(
         new SharedImage(this.vk, 37, 32856, var1, var2),
         new SharedImage(this.vk, 37, 32856, var1, var2),
         new SharedImage(this.vk, 37, 32856, var1, var2),
         new SharedImage(this.vk, 100, 33326, var3, var4),
         new SharedImage(this.vk, 83, 33327, var3, var4)
      );
      this.all = this.images.all();
      this.ffx = new FfxGeneration(this.vk, var1, var2, var3, var4);
      GL11.glFinish();
   }

   public static String unsupported() {
      return VulkanDevice.unavailable();
   }

   public static String unavailable() {
      String var0 = unsupported();
      if (var0 != null) {
         return var0;
      } else {
         return NativeFiles.ready(FILES) ? null : FILES.label + "'s files are not downloaded yet (settings, Graphics)";
      }
   }

   public static FrameGeneration open(int var0, int var1, int var2, int var3) {
      String var4 = unavailable();
      if (var4 != null) {
         throw new IllegalStateException("frame generation cannot run: " + var4);
      } else {
         return new FrameGeneration(var0, var1, var2, var3);
      }
   }

   public int screen() {
      return this.images.screen().texture;
   }

   public int hudless() {
      return this.images.hudless().texture;
   }

   public int generated() {
      return this.images.generated().texture;
   }

   public int depth() {
      return this.images.depth().texture;
   }

   public int motion() {
      return this.images.motion().texture;
   }

   public void generate(UpscaleFrame var1, boolean var2) {
      this.crossing.handOver(this.all);
      VkCommandBuffer var3 = this.vk.begin();
      this.crossing.acquire(var3, this.all);
      this.ffx.record(var3, var1, var2, this.images);
      this.crossing.release(var3, this.all);
      this.crossing.submit(var3);
      this.crossing.takeBack(this.all);
   }

   @Override
   public void close() {
      GL11.glFinish();
      this.vk.idle();
      this.ffx.close();
      this.crossing.close();

      for (SharedImage var4 : this.all) {
         var4.close();
      }

      this.vk.close();
   }

   record Images(SharedImage screen, SharedImage hudless, SharedImage generated, SharedImage depth, SharedImage motion) {
      SharedImage[] all() {
         return new SharedImage[]{this.screen, this.hudless, this.generated, this.depth, this.motion};
      }
   }
}
