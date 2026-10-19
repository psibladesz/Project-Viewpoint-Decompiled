package viewpoint.upscale;

import org.lwjgl.vulkan.VkCommandBuffer;

interface Vendor extends AutoCloseable {
   VulkanDevice.Needs needs();

   Vendor.Size create(VulkanDevice var1, int var2, int var3, int var4);

   void record(VkCommandBuffer var1, Vendor.Images var2, UpscaleFrame var3);

   default void submitted() {
   }

   @Override
   void close();

   static int nearest(int[] var0, int var1) {
      int var2 = 0;

      for (int var3 = 1; var3 < var0.length; var3++) {
         if (Math.abs(var0[var3] - var1) < Math.abs(var0[var2] - var1)) {
            var2 = var3;
         }
      }

      return var2;
   }

   record Images(SharedImage color, SharedImage depth, SharedImage motion, SharedImage output) {
      SharedImage[] all() {
         return new SharedImage[]{this.color, this.depth, this.motion, this.output};
      }
   }

   record Size(int width, int height) {
   }
}
