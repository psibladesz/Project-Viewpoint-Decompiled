package viewpoint.upscale;

import java.nio.LongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.EXTMemoryObject;
import org.lwjgl.opengl.EXTMemoryObjectWin32;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRExternalMemoryWin32;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkExportMemoryAllocateInfo;
import org.lwjgl.vulkan.VkExternalMemoryImageCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryDedicatedAllocateInfo;
import org.lwjgl.vulkan.VkMemoryGetWin32HandleInfoKHR;
import org.lwjgl.vulkan.VkMemoryRequirements;

final class SharedImage implements AutoCloseable {
   static final int USAGE = 31;
   final int width;
   final int height;
   final int vkFormat;
   final long image;
   final long memory;
   final long view;
   final int texture;
   private final VulkanDevice vk;
   private final int glMemory;
   private final long exported;

   SharedImage(VulkanDevice var1, int var2, int var3, int var4, int var5) {
      this.vk = var1;
      this.width = var4;
      this.height = var5;
      this.vkFormat = var2;
      MemoryStack var6 = MemoryStack.stackPush();

      try {
         VkImageCreateInfo var7 = VkImageCreateInfo.calloc(var6)
            .sType$Default()
            .pNext(VkExternalMemoryImageCreateInfo.calloc(var6).sType$Default().handleTypes(2).address())
            .imageType(1)
            .format(var2)
            .mipLevels(1)
            .arrayLayers(1)
            .samples(1)
            .tiling(0)
            .usage(31)
            .sharingMode(0)
            .initialLayout(0);
         var7.extent().width(var4).height(var5).depth(1);
         LongBuffer var8 = var6.mallocLong(1);
         VulkanDevice.check(VK10.vkCreateImage(var1.device, var7, null, var8), "vkCreateImage");
         this.image = var8.get(0);
         VkMemoryRequirements var9 = VkMemoryRequirements.calloc(var6);
         VK10.vkGetImageMemoryRequirements(var1.device, this.image, var9);
         this.memory = this.allocate(var6, var9);
         VulkanDevice.check(VK10.vkBindImageMemory(var1.device, this.image, this.memory, 0L), "vkBindImageMemory");
         this.view = this.view(var6);
         this.glMemory = EXTMemoryObject.glCreateMemoryObjectsEXT();
         EXTMemoryObject.glMemoryObjectParameteriEXT(this.glMemory, 38273, 1);
         this.exported = this.handle(var6);
         EXTMemoryObjectWin32.glImportMemoryWin32HandleEXT(this.glMemory, var9.size(), 38279, this.exported);
         int var10 = GL11.glGetInteger(32873);
         this.texture = GL11.glGenTextures();
         GL11.glBindTexture(3553, this.texture);
         EXTMemoryObject.glTexStorageMem2DEXT(3553, 1, var3, var4, var5, this.glMemory, 0L);
         GL11.glTexParameteri(3553, 10241, 9729);
         GL11.glTexParameteri(3553, 10240, 9729);
         GL11.glBindTexture(3553, var10);
      } catch (Throwable var12) {
         if (var6 != null) {
            try {
               var6.close();
            } catch (Throwable var11) {
               var12.addSuppressed(var11);
            }
         }

         throw var12;
      }

      if (var6 != null) {
         var6.close();
      }
   }

   @Override
   public void close() {
      GL11.glDeleteTextures(this.texture);
      EXTMemoryObject.glDeleteMemoryObjectsEXT(this.glMemory);
      VK10.vkDestroyImageView(this.vk.device, this.view, null);
      VK10.vkDestroyImage(this.vk.device, this.image, null);
      VK10.vkFreeMemory(this.vk.device, this.memory, null);
      VulkanDevice.closeHandle(this.exported);
   }

   private long allocate(MemoryStack var1, VkMemoryRequirements var2) {
      VkMemoryDedicatedAllocateInfo var3 = VkMemoryDedicatedAllocateInfo.calloc(var1).sType$Default().image(this.image);
      VkExportMemoryAllocateInfo var4 = VkExportMemoryAllocateInfo.calloc(var1).sType$Default().pNext(var3.address()).handleTypes(2);
      VkMemoryAllocateInfo var5 = VkMemoryAllocateInfo.calloc(var1)
         .sType$Default()
         .pNext(var4.address())
         .allocationSize(var2.size())
         .memoryTypeIndex(this.vk.memoryType(var2.memoryTypeBits(), 1));
      LongBuffer var6 = var1.mallocLong(1);
      VulkanDevice.check(VK10.vkAllocateMemory(this.vk.device, var5, null, var6), "vkAllocateMemory");
      return var6.get(0);
   }

   private long view(MemoryStack var1) {
      VkImageViewCreateInfo var2 = VkImageViewCreateInfo.calloc(var1).sType$Default().image(this.image).viewType(1).format(this.vkFormat);
      var2.subresourceRange().aspectMask(1).baseMipLevel(0).levelCount(1).baseArrayLayer(0).layerCount(1);
      LongBuffer var3 = var1.mallocLong(1);
      VulkanDevice.check(VK10.vkCreateImageView(this.vk.device, var2, null, var3), "vkCreateImageView");
      return var3.get(0);
   }

   private long handle(MemoryStack var1) {
      PointerBuffer var2 = var1.mallocPointer(1);
      VulkanDevice.check(
         KHRExternalMemoryWin32.vkGetMemoryWin32HandleKHR(
            this.vk.device, VkMemoryGetWin32HandleInfoKHR.calloc(var1).sType$Default().memory(this.memory).handleType(2), var2
         ),
         "vkGetMemoryWin32HandleKHR"
      );
      return var2.get(0);
   }
}
