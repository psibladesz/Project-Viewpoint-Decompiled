package viewpoint.upscale;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.SharedLibrary;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;

final class XessVendor implements Vendor {
   static final String LIBRARY = "libxess.dll";
   private static final int VIEW = 0;
   private static final int IMAGE = 8;
   private static final int RANGE = 16;
   private static final int FORMAT = 36;
   private static final int WIDTH = 40;
   private static final int HEIGHT = 44;
   private static final int EXECUTE_SIZE = 360;
   private static final int COLOR = 0;
   private static final int VELOCITY = 48;
   private static final int DEPTH = 96;
   private static final int OUTPUT = 240;
   private static final int JITTER_X = 288;
   private static final int JITTER_Y = 292;
   private static final int EXPOSURE = 296;
   private static final int RESET = 300;
   private static final int INPUT_WIDTH = 304;
   private static final int INPUT_HEIGHT = 308;
   private static final int INIT_SIZE = 64;
   private static final int INIT_OUTPUT = 0;
   private static final int INIT_QUALITY = 8;
   private static final int INIT_FLAGS = 12;
   private static final int INIT_CREATION = 16;
   private static final int INIT_VISIBLE = 20;
   private static final int LDR_INPUT_COLOR = 64;
   private static final int[] QUALITIES = new int[]{106, 105, 104, 103, 102, 101, 100};
   private static final int[] SCALES = new int[]{100, 77, 67, 59, 50, 43, 33};
   private final SharedLibrary library = Natives.load("libxess.dll");
   private final long instanceExtensions = this.function("xessVKGetRequiredInstanceExtensions");
   private final long deviceExtensions = this.function("xessVKGetRequiredDeviceExtensions");
   private final long deviceFeatures = this.function("xessVKGetRequiredDeviceFeatures");
   private final long createContext = this.function("xessVKCreateContext");
   private final long optimalInput = this.function("xessGetOptimalInputResolution");
   private final long init = this.function("xessVKInit");
   private final long execute = this.function("xessVKExecute");
   private final long velocityScale = this.function("xessSetVelocityScale");
   private final long jitterScale = this.function("xessSetJitterScale");
   private final long destroy = this.function("xessDestroyContext");
   private final NativeStruct params = new NativeStruct(360);
   private long context;

   @Override
   public VulkanDevice.Needs needs() {
      return new VulkanDevice.Needs() {
         @Override
         public List<String> instanceExtensions() {
            MemoryStack var1 = MemoryStack.stackPush();

            List var8;
            try {
               long var2 = var1.ncalloc(4, 1, 4);
               long var4 = var1.ncalloc(8, 1, 8);
               long var6 = var1.ncalloc(4, 1, 4);
               XessVendor.check(JNI.callPPPI(var2, var4, var6, XessVendor.this.instanceExtensions), "xessVKGetRequiredInstanceExtensions");
               var8 = XessVendor.names(var2, var4);
            } catch (Throwable var10) {
               if (var1 != null) {
                  try {
                     var1.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (var1 != null) {
               var1.close();
            }

            return var8;
         }

         @Override
         public List<String> deviceExtensions(VkInstance var1, VkPhysicalDevice var2) {
            MemoryStack var3 = MemoryStack.stackPush();

            List var8;
            try {
               long var4 = var3.ncalloc(4, 1, 4);
               long var6 = var3.ncalloc(8, 1, 8);
               XessVendor.check(
                  JNI.callPPPPI(var1.address(), var2.address(), var4, var6, XessVendor.this.deviceExtensions), "xessVKGetRequiredDeviceExtensions"
               );
               var8 = XessVendor.names(var4, var6);
            } catch (Throwable var10) {
               if (var3 != null) {
                  try {
                     var3.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (var3 != null) {
               var3.close();
            }

            return var8;
         }

         @Override
         public long features(VkInstance var1, VkPhysicalDevice var2, long var3) {
            MemoryStack var5 = MemoryStack.stackPush();

            long var8;
            try {
               long var6 = var5.nmalloc(8, 8);
               MemoryUtil.memPutAddress(var6, var3);
               XessVendor.check(JNI.callPPPI(var1.address(), var2.address(), var6, XessVendor.this.deviceFeatures), "xessVKGetRequiredDeviceFeatures");
               var8 = MemoryUtil.memGetAddress(var6);
            } catch (Throwable var11) {
               if (var5 != null) {
                  try {
                     var5.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }
               }

               throw var11;
            }

            if (var5 != null) {
               var5.close();
            }

            return var8;
         }
      };
   }

   @Override
   public Vendor.Size create(VulkanDevice var1, int var2, int var3, int var4) {
      int var5 = QUALITIES[Vendor.nearest(SCALES, var2)];
      MemoryStack var6 = MemoryStack.stackPush();

      Vendor.Size var11;
      try (NativeStruct var7 = new NativeStruct(64)) {
         long var8 = var6.ncalloc(8, 1, 8);
         check(JNI.callPPPPI(var1.instance.address(), var1.physical.address(), var1.device.address(), var8, this.createContext), "xessVKCreateContext");
         this.context = MemoryUtil.memGetAddress(var8);
         Vendor.Size var10 = this.input(var6, var5, var3, var4);
         var7.u32(0, var3).u32(4, var4).u32(8, var5).u32(12, 64).u32(16, 1).u32(20, 1);
         check(JNI.callPPI(this.context, var7.address, this.init), "xessVKInit");
         var11 = var10;
      } catch (Throwable var15) {
         if (var6 != null) {
            try {
               var6.close();
            } catch (Throwable var12) {
               var15.addSuppressed(var12);
            }
         }

         throw var15;
      }

      if (var6 != null) {
         var6.close();
      }

      return var11;
   }

   @Override
   public void record(VkCommandBuffer var1, Vendor.Images var2, UpscaleFrame var3) {
      check(JNI.callPI(this.context, var3.motionScaleX(), var3.motionScaleY(), this.velocityScale), "xessSetVelocityScale");
      check(JNI.callPI(this.context, 1.0F, 1.0F, this.jitterScale), "xessSetJitterScale");
      SharedImage[] var4 = new SharedImage[]{var2.color(), var2.motion(), var2.depth()};
      layout(var1, var4, 1, 5);
      this.params.clear();
      this.view(0, var2.color());
      this.view(48, var2.motion());
      this.view(96, var2.depth());
      this.view(240, var2.output());
      this.params
         .f32(288, var3.jitterX())
         .f32(292, var3.jitterY())
         .f32(296, 1.0F)
         .u32(300, var3.reset() ? 1 : 0)
         .u32(304, var2.color().width)
         .u32(308, var2.color().height);
      check(JNI.callPPPI(this.context, var1.address(), this.params.address, this.execute), "xessVKExecute");
      layout(var1, var4, 5, 1);
   }

   @Override
   public void close() {
      if (this.context != 0L) {
         check(JNI.callPI(this.context, this.destroy), "xessDestroyContext");
      }

      this.params.close();
      this.library.free();
   }

   private Vendor.Size input(MemoryStack var1, int var2, int var3, int var4) {
      long var5 = var1.ncalloc(8, 1, 8);
      long var7 = var1.ncalloc(8, 1, 8);
      long var9 = var1.ncalloc(8, 1, 8);
      long var11 = var1.ncalloc(8, 1, 8);
      MemoryUtil.memPutInt(var5, var3);
      MemoryUtil.memPutInt(var5 + 4L, var4);
      check(JNI.callPPPPPI(this.context, var5, var2, var7, var9, var11, this.optimalInput), "xessGetOptimalInputResolution");
      return new Vendor.Size(MemoryUtil.memGetInt(var7), MemoryUtil.memGetInt(var7 + 4L));
   }

   private void view(int var1, SharedImage var2) {
      this.params
         .u64(var1 + 0, var2.view)
         .u64(var1 + 8, var2.image)
         .u32(var1 + 16, 1)
         .u32(var1 + 16 + 8, 1)
         .u32(var1 + 16 + 16, 1)
         .u32(var1 + 36, var2.vkFormat)
         .u32(var1 + 40, var2.width)
         .u32(var1 + 44, var2.height);
   }

   private static void layout(VkCommandBuffer var0, SharedImage[] var1, int var2, int var3) {
      MemoryStack var4 = MemoryStack.stackPush();

      try {
         VkImageMemoryBarrier.Buffer var5 = VkImageMemoryBarrier.calloc(var1.length, var4);

         for (int var6 = 0; var6 < var1.length; var6++) {
            ((VkImageMemoryBarrier)var5.get(var6))
               .sType$Default()
               .srcAccessMask(96)
               .dstAccessMask(96)
               .oldLayout(var2)
               .newLayout(var3)
               .srcQueueFamilyIndex(-1)
               .dstQueueFamilyIndex(-1)
               .image(var1[var6].image)
               .subresourceRange()
               .aspectMask(1)
               .levelCount(1)
               .layerCount(1);
         }

         VK10.vkCmdPipelineBarrier(var0, 65536, 65536, 0, null, null, var5);
      } catch (Throwable var8) {
         if (var4 != null) {
            try {
               var4.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (var4 != null) {
         var4.close();
      }
   }

   private static List<String> names(long var0, long var2) {
      ArrayList var4 = new ArrayList();
      long var5 = MemoryUtil.memGetAddress(var2);

      for (int var7 = 0; var7 < MemoryUtil.memGetInt(var0); var7++) {
         var4.add(MemoryUtil.memUTF8(MemoryUtil.memGetAddress(var5 + var7 * 8L)));
      }

      return var4;
   }

   private long function(String var1) {
      long var2 = this.library.getFunctionAddress(var1);
      if (var2 == 0L) {
         throw new IllegalStateException("libxess.dll has no " + var1);
      } else {
         return var2;
      }
   }

   private static void check(int var0, String var1) {
      if (var0 < 0) {
         throw new IllegalStateException(var1 + " failed: xess_result_t " + var0);
      }
   }
}
