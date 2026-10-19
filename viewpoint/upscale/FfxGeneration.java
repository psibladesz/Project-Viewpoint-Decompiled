package viewpoint.upscale;

import org.lwjgl.system.JNI;
import org.lwjgl.vulkan.VkCommandBuffer;

final class FfxGeneration implements AutoCloseable {
   private static final long CREATE = 131073L;
   private static final long CONFIGURE = 131074L;
   private static final long DISPATCH = 131075L;
   private static final long PREPARE = 131076L;
   private static final long CAMERA = 131082L;
   private static final int CREATE_SIZE = 40;
   private static final int CREATE_FLAGS = 16;
   private static final int DISPLAY = 20;
   private static final int MAX_RENDER = 28;
   private static final int BACK_BUFFER = 36;
   private static final int CONFIGURE_SIZE = 144;
   private static final int ENABLED = 56;
   private static final int HUDLESS = 64;
   private static final int CONFIGURE_FLAGS = 112;
   private static final int CONFIGURE_FRAME = 136;
   private static final int PREPARE_SIZE = 184;
   private static final int PREPARE_FRAME = 16;
   private static final int PREPARE_COMMANDS = 32;
   private static final int RENDER_SIZE = 40;
   private static final int JITTER = 48;
   private static final int MOTION_SCALE = 56;
   private static final int FRAME_TIME = 64;
   private static final int NEAR = 72;
   private static final int FAR = 76;
   private static final int FOV = 80;
   private static final int METRES = 84;
   private static final int DEPTH = 88;
   private static final int MOTION = 136;
   private static final int CAMERA_SIZE = 64;
   private static final int CAMERA_VALUES = 16;
   private static final int DISPATCH_SIZE = 312;
   private static final int DISPATCH_COMMANDS = 16;
   private static final int PRESENT = 24;
   private static final int OUTPUT = 72;
   private static final int GENERATED = 264;
   private static final int RESET = 268;
   private static final int TRANSFER = 272;
   private static final int DISPATCH_FRAME = 304;
   private static final int NO_SWAPCHAIN = 8;
   private static final int SRGB = 0;
   private final Ffx ffx = new Ffx();
   private final NativeStruct context = new NativeStruct(8);
   private final NativeStruct configure = new NativeStruct(144);
   private final NativeStruct prepare = new NativeStruct(184);
   private final NativeStruct camera = new NativeStruct(64);
   private final NativeStruct dispatch = new NativeStruct(312);
   private long frameId;
   private boolean made;

   FfxGeneration(VulkanDevice var1, int var2, int var3, int var4, int var5) {
      try (
         NativeStruct var6 = new NativeStruct(40);
         NativeStruct var7 = new NativeStruct(40);
      ) {
         Ffx.backend(var6, var1);
         var7.u64(0, 131073L).u64(8, var6.address).u32(16, 0).u32(20, var2).u32(24, var3).u32(28, var4).u32(32, var5).u32(36, 10);
         Ffx.check(JNI.callPPPI(this.context.address, var7.address, 0L, this.ffx.create), "ffxCreateContext (frame generation)");
         this.made = true;
      }
   }

   void record(VkCommandBuffer var1, UpscaleFrame var2, boolean var3, FrameGeneration.Images var4) {
      this.configure.clear();
      this.configure.u64(0, 131074L).bool(56, true);
      Ffx.resource(this.configure, 64, var4.hudless(), 10, 0);
      this.configure.u32(112, 8).u64(136, this.frameId);
      Ffx.check(JNI.callPPI(this.context.address, this.configure.address, this.ffx.configure), "ffxConfigure (frame generation)");
      this.prepare(var1, var2, var4);
      this.dispatch.clear();
      this.dispatch.u64(0, 131075L).u64(16, var1.address());
      Ffx.resource(this.dispatch, 24, var4.screen(), 10, 0);
      Ffx.resource(this.dispatch, 72, var4.generated(), 10, 2);
      this.dispatch.u32(264, 1).bool(268, var3).u32(272, 0).u64(304, this.frameId);
      Ffx.check(JNI.callPPI(this.context.address, this.dispatch.address, this.ffx.dispatch), "ffxDispatch (frame generation)");
      this.frameId++;
   }

   @Override
   public void close() {
      if (this.made) {
         Ffx.check(JNI.callPPI(this.context.address, 0L, this.ffx.destroy), "ffxDestroyContext (frame generation)");
      }

      this.context.close();
      this.configure.close();
      this.prepare.close();
      this.camera.close();
      this.dispatch.close();
      this.ffx.close();
   }

   private void prepare(VkCommandBuffer var1, UpscaleFrame var2, FrameGeneration.Images var3) {
      this.camera.clear();
      this.camera.u64(0, 131082L);

      for (int var4 = 0; var4 < var2.camera().length; var4++) {
         this.camera.f32(16 + var4 * 4, var2.camera()[var4]);
      }

      this.prepare.clear();
      this.prepare
         .u64(0, 131076L)
         .u64(8, this.camera.address)
         .u64(16, this.frameId)
         .u64(32, var1.address())
         .u32(40, var3.depth().width)
         .u32(44, var3.depth().height)
         .f32(48, var2.jitterX())
         .f32(52, var2.jitterY())
         .f32(56, var2.motionScaleX())
         .f32(60, var2.motionScaleY())
         .f32(64, var2.frameMs())
         .f32(72, var2.near())
         .f32(76, var2.far())
         .f32(80, var2.fovY())
         .f32(84, var2.metresPerUnit());
      Ffx.resource(this.prepare, 88, var3.depth(), 28, 0);
      Ffx.resource(this.prepare, 136, var3.motion(), 18, 0);
      Ffx.check(JNI.callPPI(this.context.address, this.prepare.address, this.ffx.dispatch), "ffxDispatch (frame generation prepare)");
   }
}
