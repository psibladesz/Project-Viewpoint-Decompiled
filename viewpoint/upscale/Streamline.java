package viewpoint.upscale;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.SharedLibrary;

final class Streamline implements AutoCloseable {
   static final String LIBRARY = "sl.interposer.dll";
   private static final String COMMON = "sl.common.dll";
   static final int DLSS = 0;
   private static final long SDK_VERSION = 563010083094236L;
   private static final int GUID = 8;
   private static final int VERSION = 24;
   private static final int PREFS = 144;
   private static final int LOG_LEVEL = 36;
   private static final int PLUGINS = 40;
   private static final int PLUGIN_COUNT = 48;
   private static final int LOGS = 56;
   private static final int FLAGS = 88;
   private static final int FEATURES = 96;
   private static final int FEATURE_COUNT = 104;
   private static final int ENGINE_VERSION = 120;
   private static final int PROJECT = 128;
   private static final int RENDER_API = 136;
   private static final long MANUAL_HOOKING = 4L;
   private static final long FRAME_TAGGING = 128L;
   private static final long NO_STATE_TRACKING = 1L;
   private static final int LOG_DEFAULT = 1;
   private static final int VULKAN = 2;
   private static final int REQUIREMENTS = 184;
   private static final int COMPUTE_QUEUES = 104;
   private static final int GRAPHICS_QUEUES = 108;
   private static final int DEVICE_COUNT = 112;
   private static final int DEVICE_NAMES = 120;
   private static final int INSTANCE_COUNT = 128;
   private static final int INSTANCE_NAMES = 136;
   private static final int VULKAN_INFO = 96;
   private static final int VK_DEVICE = 32;
   private static final int VK_INSTANCE = 40;
   private static final int VK_PHYSICAL = 48;
   private static final int COMPUTE_INDEX = 56;
   private static final int COMPUTE_FAMILY = 60;
   private static final int GRAPHICS_INDEX = 64;
   private static final int GRAPHICS_FAMILY = 68;
   private static final int ADAPTER = 56;
   private static final int ADAPTER_PHYSICAL = 48;
   private static final int VIEWPORT = 40;
   private static final int VIEWPORT_VALUE = 32;
   private static final String PROJECT_ID = "6f1c2b0e-8a4d-4f7b-9c2e-5d3a1b7e9f40";
   private final SharedLibrary library = Natives.load("sl.interposer.dll");
   private final long init = this.function("slInit");
   private final long shutdown = this.function("slShutdown");
   private final long requirements = this.function("slGetFeatureRequirements");
   private final long vulkanInfo = this.function("slSetVulkanInfo");
   private final long supported = this.function("slIsFeatureSupported");
   private final long featureFunction = this.function("slGetFeatureFunction");
   private final long frameToken = this.function("slGetNewFrameToken");
   private final long constants = this.function("slSetConstants");
   private final long tags = this.function("slSetTagForFrame");
   private final long evaluate = this.function("slEvaluateFeature");
   private final long free = this.function("slFreeResources");
   private final List<ByteBuffer> kept = new ArrayList<>();
   private final NativeStruct preferences = NativeStruct.streamline(144, 480315749, 49038, 17195, -8241192495292482796L, 1);
   final NativeStruct viewport = NativeStruct.streamline(40, 387671093, 39740, 20424, -7379996927759635804L, 1);
   private final NativeStruct token = new NativeStruct(8);
   private final SharedLibrary common;
   private final long beforePresent;
   private final long afterPresent;
   private boolean initialised;

   Streamline() {
      ByteBuffer var1 = this.keep(MemoryUtil.memUTF16(NativeFiles.folder().getAbsolutePath()));
      ByteBuffer var2 = this.keep(MemoryUtil.memAlloc(8));
      var2.putLong(0, MemoryUtil.memAddress(var1));
      ByteBuffer var3 = this.keep(MemoryUtil.memAlloc(4));
      var3.putInt(0, 0);
      String var4 = System.getProperty("viewpoint.streamlineLogs");
      this.preferences
         .u32(36, 1)
         .u64(40, MemoryUtil.memAddress(var2))
         .u32(48, 1)
         .u64(56, var4 == null ? 0L : MemoryUtil.memAddress(this.keep(MemoryUtil.memUTF16(var4))))
         .u64(88, 133L)
         .u64(96, MemoryUtil.memAddress(var3))
         .u32(104, 1)
         .u64(120, MemoryUtil.memAddress(this.keep(MemoryUtil.memUTF8("1"))))
         .u64(128, MemoryUtil.memAddress(this.keep(MemoryUtil.memUTF8("6f1c2b0e-8a4d-4f7b-9c2e-5d3a1b7e9f40"))))
         .u32(136, 2);
      this.viewport.u32(32, 0);
      check(JNI.callPJI(this.preferences.address, 563010083094236L, this.init), "slInit");
      this.initialised = true;
      this.common = Natives.load("sl.common.dll");
      this.beforePresent = this.pluginFunction("slHookVkPresent");
      this.afterPresent = this.pluginFunction("slHookVkAfterPresent");
   }

   Streamline.Requirements requirements() {
      try (NativeStruct var1 = NativeStruct.streamline(184, 1718698135, 44141, 19398, -8568909664792168929L, 2)) {
         check(JNI.callPI(0, var1.address, this.requirements), "slGetFeatureRequirements");
         return new Streamline.Requirements(
            names(var1.getU32(128), var1.getU64(136)), names(var1.getU32(112), var1.getU64(120)), var1.getU32(108), var1.getU32(104)
         );
      }
   }

   void vulkan(VulkanDevice var1, int var2) {
      try (NativeStruct var3 = NativeStruct.streamline(96, 250441685, 33485, 17321, -4776833052637706794L, 3)) {
         var3.u64(32, var1.device.address())
            .u64(40, var1.instance.address())
            .u64(48, var1.physical.address())
            .u32(68, var1.family)
            .u32(64, 1)
            .u32(60, var1.family)
            .u32(56, 1 + var2);
         check(JNI.callPI(var3.address, this.vulkanInfo), "slSetVulkanInfo");
      }
   }

   String unsupported(VulkanDevice var1) {
      try (NativeStruct var2 = NativeStruct.streamline(56, 108474719, 42822, 17554, -6970785654623321132L, 1)) {
         var2.u64(48, var1.physical.address());
         int var3 = JNI.callPI(0, var2.address, this.supported);
         return var3 == 0 ? null : "DLSS is not supported on this GPU or driver (Streamline result " + var3 + ")";
      }
   }

   long feature(String var1) {
      try (NativeStruct var2 = new NativeStruct(8)) {
         ByteBuffer var3 = MemoryUtil.memUTF8(var1);

         try {
            check(JNI.callPPI(0, MemoryUtil.memAddress(var3), var2.address, this.featureFunction), "slGetFeatureFunction");
         } finally {
            MemoryUtil.memFree(var3);
         }

         return var2.getU64(0);
      }
   }

   long frame() {
      check(JNI.callPPI(this.token.address, 0L, this.frameToken), "slGetNewFrameToken");
      return this.token.getU64(0);
   }

   void constants(long var1, long var3) {
      check(JNI.callPPPI(var1, var3, this.viewport.address, this.constants), "slSetConstants");
   }

   void tag(long var1, long var3, int var5, long var6) {
      check(JNI.callPPPPI(var1, this.viewport.address, var3, var5, var6, this.tags), "slSetTagForFrame");
   }

   void evaluate(long var1, long var3) {
      try (NativeStruct var5 = new NativeStruct(8)) {
         var5.u64(0, this.viewport.address);
         check(JNI.callPPPPPI(0L, var1, var5.address, 1L, var3, this.evaluate), "slEvaluateFeature");
      }
   }

   void present() {
      try (NativeStruct var1 = new NativeStruct(8)) {
         check(JNI.callPPPI(0L, 0L, var1.address, this.beforePresent), "slHookVkPresent");
      }

      check(JNI.callI(this.afterPresent), "slHookVkAfterPresent");
   }

   @Override
   public void close() {
      if (this.initialised) {
         JNI.callPI(0, this.viewport.address, this.free);
         check(JNI.callI(this.shutdown), "slShutdown");
      }

      this.preferences.close();
      this.viewport.close();
      this.token.close();
      this.kept.forEach(MemoryUtil::memFree);
      this.common.free();
      this.library.free();
   }

   private ByteBuffer keep(ByteBuffer var1) {
      this.kept.add(var1);
      return var1;
   }

   private static List<String> names(int var0, long var1) {
      ArrayList var3 = new ArrayList();

      for (int var4 = 0; var4 < var0; var4++) {
         var3.add(MemoryUtil.memUTF8(MemoryUtil.memGetAddress(var1 + var4 * 8L)));
      }

      return var3;
   }

   private long pluginFunction(String var1) {
      ByteBuffer var2 = MemoryUtil.memUTF8(var1);

      try {
         long var3 = JNI.callPP(MemoryUtil.memAddress(var2), this.common.getFunctionAddress("slGetPluginFunction"));
         if (var3 == 0L) {
            throw new IllegalStateException("sl.common.dll has no " + var1);
         } else {
            return var3;
         }
      } finally {
         MemoryUtil.memFree(var2);
      }
   }

   private long function(String var1) {
      long var2 = this.library.getFunctionAddress(var1);
      if (var2 == 0L) {
         throw new IllegalStateException("sl.interposer.dll has no " + var1);
      } else {
         return var2;
      }
   }

   static void check(int var0, String var1) {
      if (var0 != 0) {
         throw new IllegalStateException(var1 + " failed: Streamline result " + var0);
      }
   }

   record Requirements(List<String> instanceExtensions, List<String> deviceExtensions, int graphicsQueues, int computeQueues) {
   }
}
