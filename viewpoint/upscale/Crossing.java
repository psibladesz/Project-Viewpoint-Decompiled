package viewpoint.upscale;

import java.nio.LongBuffer;
import java.util.Arrays;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.EXTSemaphore;
import org.lwjgl.opengl.EXTSemaphoreWin32;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRExternalSemaphoreWin32;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExportSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreGetWin32HandleInfoKHR;

final class Crossing implements AutoCloseable {
   private static final int[] NONE = new int[0];
   private final VulkanDevice vk;
   private final long glDone;
   private final long vkDone;
   private final int glDoneGl;
   private final int vkDoneGl;
   private final long glDoneExported;
   private final long vkDoneExported;

   Crossing(VulkanDevice var1) {
      this.vk = var1;
      this.glDone = this.semaphore();
      this.vkDone = this.semaphore();
      this.glDoneExported = this.exported(this.glDone);
      this.vkDoneExported = this.exported(this.vkDone);
      this.glDoneGl = imported(this.glDoneExported);
      this.vkDoneGl = imported(this.vkDoneExported);
   }

   void handOver(SharedImage... var1) {
      EXTSemaphore.glSignalSemaphoreEXT(this.glDoneGl, NONE, textures(var1), layouts(var1));
      GL11.glFlush();
   }

   void takeBack(SharedImage... var1) {
      EXTSemaphore.glWaitSemaphoreEXT(this.vkDoneGl, NONE, textures(var1), layouts(var1));
   }

   void acquire(VkCommandBuffer var1, SharedImage... var2) {
      this.transfer(var1, var2, -2, this.vk.family);
   }

   void release(VkCommandBuffer var1, SharedImage... var2) {
      this.transfer(var1, var2, this.vk.family, -2);
   }

   void submit(VkCommandBuffer var1) {
      this.vk.submit(var1, this.glDone, this.vkDone);
   }

   @Override
   public void close() {
      EXTSemaphore.glDeleteSemaphoresEXT(this.glDoneGl);
      EXTSemaphore.glDeleteSemaphoresEXT(this.vkDoneGl);
      VK10.vkDestroySemaphore(this.vk.device, this.glDone, null);
      VK10.vkDestroySemaphore(this.vk.device, this.vkDone, null);
      VulkanDevice.closeHandle(this.glDoneExported);
      VulkanDevice.closeHandle(this.vkDoneExported);
   }

   private void transfer(VkCommandBuffer var1, SharedImage[] var2, int var3, int var4) {
      MemoryStack var5 = MemoryStack.stackPush();

      try {
         VkImageMemoryBarrier.Buffer var6 = VkImageMemoryBarrier.calloc(var2.length, var5);

         for (int var7 = 0; var7 < var2.length; var7++) {
            ((VkImageMemoryBarrier)var6.get(var7))
               .sType$Default()
               .srcAccessMask(98304)
               .dstAccessMask(98304)
               .oldLayout(1)
               .newLayout(1)
               .srcQueueFamilyIndex(var3)
               .dstQueueFamilyIndex(var4)
               .image(var2[var7].image)
               .subresourceRange()
               .aspectMask(1)
               .levelCount(1)
               .layerCount(1);
         }

         VK10.vkCmdPipelineBarrier(var1, 65536, 65536, 0, null, null, var6);
      } catch (Throwable var9) {
         if (var5 != null) {
            try {
               var5.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }
      //gosha rubchinsky)
      if (var5 != null) {
         var5.close();
      }
   }

   private long semaphore() {
      MemoryStack var1 = MemoryStack.stackPush();

      long var4;
      try {
         VkExportSemaphoreCreateInfo var2 = VkExportSemaphoreCreateInfo.calloc(var1).sType$Default().handleTypes(2);
         LongBuffer var3 = var1.mallocLong(1);
         VulkanDevice.check(
            VK10.vkCreateSemaphore(this.vk.device, VkSemaphoreCreateInfo.calloc(var1).sType$Default().pNext(var2.address()), null, var3), "vkCreateSemaphore"
         );
         var4 = var3.get(0);
      } catch (Throwable var7) {
         if (var1 != null) {
            try {
               var1.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (var1 != null) {
         var1.close();
      }

      return var4;
   }

   private long exported(long var1) {
      MemoryStack var3 = MemoryStack.stackPush();

      long var5;
      try {
         PointerBuffer var4 = var3.mallocPointer(1);
         VulkanDevice.check(
            KHRExternalSemaphoreWin32.vkGetSemaphoreWin32HandleKHR(
               this.vk.device, VkSemaphoreGetWin32HandleInfoKHR.calloc(var3).sType$Default().semaphore(var1).handleType(2), var4
            ),
            "vkGetSemaphoreWin32HandleKHR"
         );
         var5 = var4.get(0);
      } catch (Throwable var8) {
         if (var3 != null) {
            try {
               var3.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (var3 != null) {
         var3.close();
      }

      return var5;
   }

   private static int imported(long var0) {
      int var2 = EXTSemaphore.glGenSemaphoresEXT();
      EXTSemaphoreWin32.glImportSemaphoreWin32HandleEXT(var2, 38279, var0);
      return var2;
   }

   private static int[] textures(SharedImage[] var0) {
      int[] var1 = new int[var0.length];

      for (int var2 = 0; var2 < var0.length; var2++) {
         var1[var2] = var0[var2].texture;
      }

      return var1;
   }

   private static int[] layouts(SharedImage[] var0) {
      int[] var1 = new int[var0.length];
      Arrays.fill(var1, 38285);
      return var1;
   }
}
