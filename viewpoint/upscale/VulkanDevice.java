package viewpoint.upscale;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.EXTMemoryObject;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.windows.Kernel32;
import org.lwjgl.vulkan.VK;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceIDProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties2;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSubmitInfo;

public final class VulkanDevice implements AutoCloseable {
   private static final int FRAMES = 2;
   private static final List<String> INTEROP = List.of("VK_KHR_external_memory_win32", "VK_KHR_external_semaphore_win32");
   private static final int UUID_BYTES = 16;
   private static int onLoader;
   final VkInstance instance;
   final VkPhysicalDevice physical;
   final VkDevice device;
   final VkQueue queue;
   final int family;
   private final long pool;
   private final VkCommandBuffer[] commands = new VkCommandBuffer[2];
   private final long[] fences = new long[2];
   private final VkPhysicalDeviceMemoryProperties memory = VkPhysicalDeviceMemoryProperties.calloc();
   private int frame;

   private VulkanDevice(VulkanDevice.Needs var1) {
      if (onLoader++ == 0) {
         VK.create();
      }

      this.instance = instance(var1.instanceExtensions());
      this.physical = sameAsGl(this.instance);
      this.family = family(this.physical);
      this.device = this.device(var1);
      VK10.vkGetPhysicalDeviceMemoryProperties(this.physical, this.memory);
      MemoryStack var2 = MemoryStack.stackPush();

      try {
         PointerBuffer var3 = var2.mallocPointer(1);
         VK10.vkGetDeviceQueue(this.device, this.family, 0, var3);
         this.queue = new VkQueue(var3.get(0), this.device);
         LongBuffer var4 = var2.mallocLong(1);
         check(
            VK10.vkCreateCommandPool(this.device, VkCommandPoolCreateInfo.calloc(var2).sType$Default().queueFamilyIndex(this.family).flags(2), null, var4),
            "vkCreateCommandPool"
         );
         this.pool = var4.get(0);
         var3 = var2.mallocPointer(2);
         check(
            VK10.vkAllocateCommandBuffers(
               this.device, VkCommandBufferAllocateInfo.calloc(var2).sType$Default().commandPool(this.pool).level(0).commandBufferCount(2), var3
            ),
            "vkAllocateCommandBuffers"
         );

         for (int var5 = 0; var5 < 2; var5++) {
            this.commands[var5] = new VkCommandBuffer(var3.get(var5), this.device);
            check(VK10.vkCreateFence(this.device, VkFenceCreateInfo.calloc(var2).sType$Default().flags(1), null, var4), "vkCreateFence");
            this.fences[var5] = var4.get(0);
         }
      } catch (Throwable var7) {
         if (var2 != null) {
            try {
               var2.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (var2 != null) {
         var2.close();
      }
   }

   public static String unavailable() {
      GLCapabilities var0 = GL.getCapabilities();
      return var0.GL_EXT_memory_object_win32 && var0.GL_EXT_semaphore_win32 ? null : "the graphics driver's OpenGL cannot share images with Vulkan";
   }

   public static VulkanDevice open(VulkanDevice.Needs var0) {
      String var1 = unavailable();
      if (var1 != null) {
         throw new IllegalStateException(var1);
      } else {
         return new VulkanDevice(var0);
      }
   }

   long deviceProcAddr() {
      return VK.getFunctionProvider().getFunctionAddress("vkGetDeviceProcAddr");
   }

   VkCommandBuffer begin() {
      int var1 = this.frame % 2;
      MemoryStack var2 = MemoryStack.stackPush();

      VkCommandBuffer var4;
      try {
         VK10.vkWaitForFences(this.device, this.fences[var1], true, Long.MAX_VALUE);
         VK10.vkResetFences(this.device, this.fences[var1]);
         VkCommandBuffer var3 = this.commands[var1];
         VK10.vkResetCommandBuffer(var3, 0);
         check(VK10.vkBeginCommandBuffer(var3, VkCommandBufferBeginInfo.calloc(var2).sType$Default().flags(1)), "vkBeginCommandBuffer");
         var4 = var3;
      } catch (Throwable var6) {
         if (var2 != null) {
            try {
               var2.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (var2 != null) {
         var2.close();
      }

      return var4;
   }

   void submit(VkCommandBuffer var1, long var2, long var4) {
      int var6 = this.frame % 2;
      check(VK10.vkEndCommandBuffer(var1), "vkEndCommandBuffer");
      MemoryStack var7 = MemoryStack.stackPush();

      try {
         VkSubmitInfo var8 = VkSubmitInfo.calloc(var7)
            .sType$Default()
            .pCommandBuffers(var7.pointers(var1))
            .waitSemaphoreCount(1)
            .pWaitSemaphores(var7.longs(var2))
            .pWaitDstStageMask(var7.ints(65536))
            .pSignalSemaphores(var7.longs(var4));
         check(VK10.vkQueueSubmit(this.queue, var8, this.fences[var6]), "vkQueueSubmit");
      } catch (Throwable var11) {
         if (var7 != null) {
            try {
               var7.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }
         }

         throw var11;
      }

      if (var7 != null) {
         var7.close();
      }

      this.frame++;
   }

   void idle() {
      VK10.vkDeviceWaitIdle(this.device);
   }

   int memoryType(int var1, int var2) {
      for (int var3 = 0; var3 < this.memory.memoryTypeCount(); var3++) {
         if ((var1 & 1 << var3) != 0 && (this.memory.memoryTypes(var3).propertyFlags() & var2) == var2) {
            return var3;
         }
      }

      throw new IllegalStateException("no Vulkan memory type with properties 0x" + Integer.toHexString(var2));
   }

   @Override
   public void close() {
      VK10.vkDeviceWaitIdle(this.device);

      for (long var4 : this.fences) {
         VK10.vkDestroyFence(this.device, var4, null);
      }

      VK10.vkDestroyCommandPool(this.device, this.pool, null);
      VK10.vkDestroyDevice(this.device, null);
      VK10.vkDestroyInstance(this.instance, null);
      this.memory.free();
      if (--onLoader == 0) {
         VK.destroy();
      }
   }

   static void closeHandle(long var0) {
      if (JNI.callPI(var0, Kernel32.getLibrary().getFunctionAddress("CloseHandle")) == 0) {
         throw new IllegalStateException("CloseHandle failed on a handle Vulkan exported");
      }
   }

   static void check(int var0, String var1) {
      if (var0 != 0) {
         throw new IllegalStateException(var1 + " failed: VkResult " + var0);
      }
   }

   private static VkInstance instance(List<String> var0) {
      MemoryStack var1 = MemoryStack.stackPush();

      VkInstance var5;
      try {
         VkApplicationInfo var2 = VkApplicationInfo.calloc(var1)
            .sType$Default()
            .pApplicationName(var1.UTF8("Viewpoint"))
            .pEngineName(var1.UTF8("Viewpoint"))
            .apiVersion(VK13.VK_API_VERSION_1_3);
         VkInstanceCreateInfo var3 = VkInstanceCreateInfo.calloc(var1).sType$Default().pApplicationInfo(var2).ppEnabledExtensionNames(names(var1, var0));
         PointerBuffer var4 = var1.mallocPointer(1);
         check(VK10.vkCreateInstance(var3, null, var4), "vkCreateInstance");
         var5 = new VkInstance(var4.get(0), var3);
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

      return var5;
   }

   private static VkPhysicalDevice sameAsGl(VkInstance var0) {
      MemoryStack var1 = MemoryStack.stackPush();

      VkPhysicalDevice var12;
      label62: {
         try {
            ByteBuffer var2 = var1.calloc(16);
            EXTMemoryObject.glGetUnsignedBytei_vEXT(38295, 0, var2);
            IntBuffer var3 = var1.mallocInt(1);
            VK10.vkEnumeratePhysicalDevices(var0, var3, null);
            PointerBuffer var4 = var1.mallocPointer(var3.get(0));
            VK10.vkEnumeratePhysicalDevices(var0, var3, var4);

            for (int var5 = 0; var5 < var4.capacity(); var5++) {
               VkPhysicalDevice var6 = new VkPhysicalDevice(var4.get(var5), var0);
               VkPhysicalDeviceIDProperties var7 = VkPhysicalDeviceIDProperties.calloc(var1).sType$Default();
               VK11.vkGetPhysicalDeviceProperties2(var6, VkPhysicalDeviceProperties2.calloc(var1).sType$Default().pNext(var7.address()));
               boolean var8 = true;

               for (int var9 = 0; var9 < 16; var9++) {
                  var8 &= var2.get(var9) == var7.deviceUUID(var9);
               }

               if (var8) {
                  var12 = var6;
                  break label62;
               }
            }
         } catch (Throwable var11) {
            if (var1 != null) {
               try {
                  var1.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }
            }

            throw var11;
         }

         if (var1 != null) {
            var1.close();
         }

         throw new IllegalStateException("no Vulkan device is the one OpenGL draws with (the device UUIDs differ)");
      }

      if (var1 != null) {
         var1.close();
      }

      return var12;
   }

   private static int family(VkPhysicalDevice var0) {
      MemoryStack var1 = MemoryStack.stackPush();

      int var6;
      label48: {
         try {
            IntBuffer var2 = var1.mallocInt(1);
            VK10.vkGetPhysicalDeviceQueueFamilyProperties(var0, var2, null);
            VkQueueFamilyProperties.Buffer var3 = VkQueueFamilyProperties.calloc(var2.get(0), var1);
            VK10.vkGetPhysicalDeviceQueueFamilyProperties(var0, var2, var3);
            byte var4 = 3;

            for (int var5 = 0; var5 < var3.capacity(); var5++) {
               if ((((VkQueueFamilyProperties)var3.get(var5)).queueFlags() & var4) == var4) {
                  var6 = var5;
                  break label48;
               }
            }
         } catch (Throwable var8) {
            if (var1 != null) {
               try {
                  var1.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (var1 != null) {
            var1.close();
         }

         throw new IllegalStateException("the Vulkan device has no graphics and compute queue");
      }

      if (var1 != null) {
         var1.close();
      }

      return var6;
   }

   private VkDevice device(VulkanDevice.Needs var1) {
      MemoryStack var2 = MemoryStack.stackPush();

      VkDevice var8;
      try {
         ArrayList var3 = new ArrayList<>(INTEROP);
         var3.addAll(var1.deviceExtensions(this.instance, this.physical));
         this.requireExtensions(var2, var3);
         FloatBuffer var4 = var2.mallocFloat(1 + var1.extraQueues());

         while (var4.hasRemaining()) {
            var4.put(1.0F);
         }

         VkDeviceQueueCreateInfo.Buffer var5 = VkDeviceQueueCreateInfo.calloc(1, var2)
            .sType$Default()
            .queueFamilyIndex(this.family)
            .pQueuePriorities(var4.flip());
         VkDeviceCreateInfo var6 = VkDeviceCreateInfo.calloc(var2)
            .sType$Default()
            .pNext(var1.features(this.instance, this.physical, this.features(var2).address()))
            .pQueueCreateInfos(var5)
            .ppEnabledExtensionNames(names(var2, var3));
         PointerBuffer var7 = var2.mallocPointer(1);
         check(VK10.vkCreateDevice(this.physical, var6, null, var7), "vkCreateDevice");
         var8 = new VkDevice(var7.get(0), this.physical, var6, VK13.VK_API_VERSION_1_3);
      } catch (Throwable var10) {
         if (var2 != null) {
            try {
               var2.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (var2 != null) {
         var2.close();
      }

      return var8;
   }

   private VkPhysicalDeviceFeatures2 features(MemoryStack var1) {
      VkPhysicalDeviceVulkan13Features var2 = VkPhysicalDeviceVulkan13Features.calloc(var1).sType$Default();
      VkPhysicalDeviceVulkan12Features var3 = VkPhysicalDeviceVulkan12Features.calloc(var1).sType$Default().pNext(var2.address());
      VkPhysicalDeviceVulkan11Features var4 = VkPhysicalDeviceVulkan11Features.calloc(var1).sType$Default().pNext(var3.address());
      VkPhysicalDeviceFeatures2 var5 = VkPhysicalDeviceFeatures2.calloc(var1).sType$Default().pNext(var4.address());
      VK11.vkGetPhysicalDeviceFeatures2(this.physical, var5);
      var5.features().robustBufferAccess(false);
      var2.robustImageAccess(false);
      return var5;
   }

   private void requireExtensions(MemoryStack var1, List<String> var2) {
      IntBuffer var3 = var1.mallocInt(1);
      VK10.vkEnumerateDeviceExtensionProperties(this.physical, (ByteBuffer)null, var3, null);
      VkExtensionProperties.Buffer var4 = VkExtensionProperties.malloc(var3.get(0));

      try {
         VK10.vkEnumerateDeviceExtensionProperties(this.physical, (ByteBuffer)null, var3, var4);

         for (String var6 : var2) {
            boolean var7 = false;

            for (int var8 = 0; var8 < var4.capacity() && !var7; var8++) {
               var7 = ((VkExtensionProperties)var4.get(var8)).extensionNameString().equals(var6);
            }

            if (!var7) {
               throw new IllegalStateException("the Vulkan device lacks the extension " + var6);
            }
         }
      } finally {
         var4.free();
      }
   }

   private static PointerBuffer names(MemoryStack var0, List<String> var1) {
      PointerBuffer var2 = var0.mallocPointer(var1.size());

      for (String var4 : var1) {
         var2.put(var0.UTF8(var4));
      }

      return (PointerBuffer)var2.flip();
   }

   static {
      Configuration.VULKAN_EXPLICIT_INIT.set(true);
   }

   public interface Needs {
      default List<String> instanceExtensions() {
         return List.of();
      }

      default List<String> deviceExtensions(VkInstance var1, VkPhysicalDevice var2) {
         return List.of();
      }

      default long features(VkInstance var1, VkPhysicalDevice var2, long var3) {
         return var3;
      }

      default int extraQueues() {
         return 0;
      }
   }
}
