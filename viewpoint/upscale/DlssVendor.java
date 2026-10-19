package viewpoint.upscale;

import java.util.List;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;

final class DlssVendor implements Vendor {
   private static final int OPTIONS = 88;
   private static final int MODE = 32;
   private static final int OUTPUT_WIDTH = 36;
   private static final int OUTPUT_HEIGHT = 40;
   private static final int PRE_EXPOSURE = 48;
   private static final int EXPOSURE_SCALE = 52;
   private static final int HDR = 56;
   private static final int OPTIMAL = 64;
   private static final int OPTIMAL_WIDTH = 32;
   private static final int OPTIMAL_HEIGHT = 36;
   private static final int[] MODES = new int[]{6, 3, 2, 1, 4};
   private static final int[] SCALES = new int[]{100, 67, 58, 50, 33};
   private static final int RESOURCE = 112;
   private static final int NATIVE = 40;
   private static final int MEMORY = 48;
   private static final int VIEW = 56;
   private static final int STATE = 64;
   private static final int RESOURCE_WIDTH = 68;
   private static final int RESOURCE_HEIGHT = 72;
   private static final int FORMAT = 76;
   private static final int MIPS = 80;
   private static final int LAYERS = 84;
   private static final int USAGE = 100;
   private static final int TAG = 64;
   private static final int TAG_RESOURCE = 32;
   private static final int TAG_TYPE = 40;
   private static final int TAG_LIFECYCLE = 44;
   private static final int EXTENT = 48;
   private static final int DEPTH = 0;
   private static final int MOTION = 1;
   private static final int INPUT = 3;
   private static final int OUTPUT = 4;
   private static final int UNTIL_EVALUATE = 2;
   private final Streamline sl = new Streamline();
   private final Streamline.Requirements requirements = this.sl.requirements();
   private final NativeStruct options = NativeStruct.streamline(88, 1791502052, 19553, 16641, -6256234848997845064L, 3);
   private final NativeStruct resources = new NativeStruct(448);
   private final NativeStruct tags = new NativeStruct(256);
   private final DlssConstants constants = new DlssConstants();
   private long setOptions;

   static String unavailable() {
      String var0 = GL11.glGetString(7937);
      return var0 != null && var0.contains("RTX") ? null : "DLSS needs an NVIDIA RTX graphics card";
   }

   @Override
   public VulkanDevice.Needs needs() {
      return new VulkanDevice.Needs() {
         @Override
         public List<String> instanceExtensions() {
            return DlssVendor.this.requirements.instanceExtensions();
         }

         @Override
         public List<String> deviceExtensions(VkInstance var1, VkPhysicalDevice var2) {
            return DlssVendor.this.requirements.deviceExtensions();
         }

         @Override
         public int extraQueues() {
            return DlssVendor.this.requirements.graphicsQueues() + DlssVendor.this.requirements.computeQueues();
         }
      };
   }

   @Override
   public Vendor.Size create(VulkanDevice var1, int var2, int var3, int var4) {
      this.sl.vulkan(var1, this.requirements.graphicsQueues());
      String var5 = this.sl.unsupported(var1);
      if (var5 != null) {
         throw new IllegalStateException(var5);
      }

      this.setOptions = this.sl.feature("slDLSSSetOptions");
      this.options.u32(32, MODES[Vendor.nearest(SCALES, var2)]).u32(36, var3).u32(40, var4).f32(48, 1.0F).f32(52, 1.0F).bool(56, false);
      return this.optimal(this.sl.feature("slDLSSGetOptimalSettings"));
   }

   @Override
   public void record(VkCommandBuffer var1, Vendor.Images var2, UpscaleFrame var3) {
      long var4 = this.sl.frame();
      Streamline.check(JNI.callPPI(this.sl.viewport.address, this.options.address, this.setOptions), "slDLSSSetOptions");
      this.constants.fill(var3, var2.color().width, var2.color().height);
      this.sl.constants(this.constants.address(), var4);
      this.tag(0, var2.depth(), 0);
      this.tag(1, var2.motion(), 1);
      this.tag(2, var2.color(), 3);
      this.tag(3, var2.output(), 4);
      this.sl.tag(var4, this.tags.address, 4, var1.address());
      this.sl.evaluate(var4, var1.address());
   }

   @Override
   public void submitted() {
      this.sl.present();
   }

   @Override
   public void close() {
      this.sl.close();
      this.options.close();
      this.resources.close();
      this.tags.close();
      this.constants.close();
   }

   private Vendor.Size optimal(long var1) {
      try (NativeStruct var3 = NativeStruct.streamline(64, -283309737, 64856, 19959, -5403040366214878346L, 1)) {
         Streamline.check(JNI.callPPI(this.options.address, var3.address, var1), "slDLSSGetOptimalSettings");
         return new Vendor.Size(var3.getU32(32), var3.getU32(36));
      }
   }

   private void tag(int var1, SharedImage var2, int var3) {
      long var4 = this.resources.address + var1 * 112L;
      long var6 = this.tags.address + var1 * 64L;
      MemoryUtil.memSet(var4, 0, 112L);
      NativeStruct.header(var4, 983396559, 9240, 19314, -8966363425330662815L, 1);
      MemoryUtil.memPutLong(var4 + 40L, var2.image);
      MemoryUtil.memPutLong(var4 + 48L, var2.memory);
      MemoryUtil.memPutLong(var4 + 56L, var2.view);
      MemoryUtil.memPutInt(var4 + 64L, 1);
      MemoryUtil.memPutInt(var4 + 68L, var2.width);
      MemoryUtil.memPutInt(var4 + 72L, var2.height);
      MemoryUtil.memPutInt(var4 + 76L, var2.vkFormat);
      MemoryUtil.memPutInt(var4 + 80L, 1);
      MemoryUtil.memPutInt(var4 + 84L, 1);
      MemoryUtil.memPutInt(var4 + 100L, 31);
      MemoryUtil.memSet(var6, 0, 64L);
      NativeStruct.header(var6, 1282038445, 46149, 18796, -8647163126328072621L, 1);
      MemoryUtil.memPutLong(var6 + 32L, var4);
      MemoryUtil.memPutInt(var6 + 40L, var3);
      MemoryUtil.memPutInt(var6 + 44L, 2);
      MemoryUtil.memPutInt(var6 + 48L + 8L, var2.width);
      MemoryUtil.memPutInt(var6 + 48L + 12L, var2.height);
   }
}
