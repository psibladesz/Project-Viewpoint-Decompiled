package viewpoint.upscale;

import org.lwjgl.system.SharedLibrary;

final class Ffx implements AutoCloseable {
   static final String LIBRARY = "amd_fidelityfx_vk.dll";
   static final int TYPE = 0;
   static final int NEXT = 8;
   static final int BACKEND_SIZE = 40;
   static final int RESOURCE_DESC = 8;
   static final int RESOURCE_STATE = 40;
   static final int TEXTURE_2D = 2;
   static final int USAGE_READ = 0;
   static final int USAGE_UAV = 2;
   static final int STATE_UAV = 2;
   static final int RGBA16F = 4;
   static final int RGBA8 = 10;
   static final int RG16F = 18;
   static final int R32F = 28;
   private static final long CREATE_BACKEND_VK = 3L;
   private static final int BACKEND_DEVICE = 16;
   private static final int BACKEND_PHYSICAL = 24;
   private static final int BACKEND_PROC = 32;
   private final SharedLibrary library = Natives.load("amd_fidelityfx_vk.dll");
   final long create = this.function("ffxCreateContext");
   final long destroy = this.function("ffxDestroyContext");
   final long configure = this.function("ffxConfigure");
   final long dispatch = this.function("ffxDispatch");

   static void backend(NativeStruct var0, VulkanDevice var1) {
      var0.u64(0, 3L).u64(16, var1.device.address()).u64(24, var1.physical.address()).u64(32, var1.deviceProcAddr());
   }

   static void resource(NativeStruct var0, int var1, SharedImage var2, int var3, int var4) {
      var0.u64(var1, var2.image)
         .u32(var1 + 8, 2)
         .u32(var1 + 8 + 4, var3)
         .u32(var1 + 8 + 8, var2.width)
         .u32(var1 + 8 + 12, var2.height)
         .u32(var1 + 8 + 16, 1)
         .u32(var1 + 8 + 20, 1)
         .u32(var1 + 8 + 28, var4)
         .u32(var1 + 40, 2);
   }

   static void check(int var0, String var1) {
      if (var0 != 0) {
         throw new IllegalStateException(var1 + " failed: FFX return code " + var0);
      }
   }

   @Override
   public void close() {
      this.library.free();
   }

   private long function(String var1) {
      long var2 = this.library.getFunctionAddress(var1);
      if (var2 == 0L) {
         throw new IllegalStateException("amd_fidelityfx_vk.dll has no " + var1);
      } else {
         return var2;
      }
   }
}
