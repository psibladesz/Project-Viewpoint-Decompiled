package viewpoint.upscale;

import java.util.List;
import org.lwjgl.system.JNI;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;

final class FsrVendor implements Vendor {
   private static final long CREATE_UPSCALE = 65536L;
   private static final long DISPATCH_UPSCALE = 65537L;
   private static final int CREATE_SIZE = 48;
   private static final int CREATE_FLAGS = 16;
   private static final int MAX_RENDER = 20;
   private static final int MAX_UPSCALE = 28;
   private static final int DISPATCH_SIZE = 432;
   private static final int COMMAND_LIST = 16;
   private static final int COLOR = 24;
   private static final int DEPTH = 72;
   private static final int MOTION = 120;
   private static final int OUTPUT = 312;
   private static final int JITTER = 360;
   private static final int MOTION_SCALE = 368;
   private static final int RENDER_SIZE = 376;
   private static final int UPSCALE_SIZE = 384;
   private static final int FRAME_TIME = 400;
   private static final int PRE_EXPOSURE = 404;
   private static final int RESET = 408;
   private static final int NEAR = 412;
   private static final int FAR = 416;
   private static final int FOV = 420;
   private static final int METRES = 424;
   static final VulkanDevice.Needs NEEDS = new VulkanDevice.Needs() {
      @Override
      public List<String> deviceExtensions(VkInstance var1, VkPhysicalDevice var2) {
         return List.of("VK_KHR_get_memory_requirements2", "VK_KHR_dedicated_allocation");
      }
   };
   private final Ffx ffx = new Ffx();
   private final NativeStruct context = new NativeStruct(8);
   private final NativeStruct desc = new NativeStruct(432);
   private boolean made;

   @Override
   public VulkanDevice.Needs needs() {
      return NEEDS;
   }

   @Override
   public Vendor.Size create(VulkanDevice var1, int var2, int var3, int var4) {
      int var5 = Math.max(1, Math.round(var3 * var2 / 100.0F));
      int var6 = Math.max(1, Math.round(var4 * var2 / 100.0F));

      try (
         NativeStruct var7 = new NativeStruct(40);
         NativeStruct var8 = new NativeStruct(48);
      ) {
         Ffx.backend(var7, var1);
         var8.u64(0, 65536L).u64(8, var7.address).u32(16, 0).u32(20, var5).u32(24, var6).u32(28, var3).u32(32, var4);
         Ffx.check(JNI.callPPPI(this.context.address, var8.address, 0L, this.ffx.create), "ffxCreateContext");
         this.made = true;
      }

      return new Vendor.Size(var5, var6);
   }

   @Override
   public void record(VkCommandBuffer var1, Vendor.Images var2, UpscaleFrame var3) {
      this.desc.clear();
      this.desc.u64(0, 65537L).u64(16, var1.address());
      Ffx.resource(this.desc, 24, var2.color(), 4, 0);
      Ffx.resource(this.desc, 72, var2.depth(), 28, 0);
      Ffx.resource(this.desc, 120, var2.motion(), 18, 0);
      Ffx.resource(this.desc, 312, var2.output(), 4, 2);
      this.desc
         .f32(360, var3.jitterX())
         .f32(364, var3.jitterY())
         .f32(368, var3.motionScaleX())
         .f32(372, var3.motionScaleY())
         .u32(376, var2.color().width)
         .u32(380, var2.color().height)
         .u32(384, var2.output().width)
         .u32(388, var2.output().height)
         .f32(400, var3.frameMs())
         .f32(404, 1.0F)
         .bool(408, var3.reset())
         .f32(412, var3.near())
         .f32(416, var3.far())
         .f32(420, var3.fovY())
         .f32(424, var3.metresPerUnit());
      Ffx.check(JNI.callPPI(this.context.address, this.desc.address, this.ffx.dispatch), "ffxDispatch (upscale)");
   }

   @Override
   public void close() {
      if (this.made) {
         Ffx.check(JNI.callPPI(this.context.address, 0L, this.ffx.destroy), "ffxDestroyContext");
      }

      this.desc.close();
      this.context.close();
      this.ffx.close();
   }
}
