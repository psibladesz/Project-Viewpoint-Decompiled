package viewpoint.upscale;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import zombie.ZomboidFileSystem;

public final class NativeFiles {
   private static final String FIDELITY_FX_URL = "https://raw.githubusercontent.com/GPUOpen-LibrariesAndSDKs/FidelityFX-SDK/v1.1.4/PrebuiltSignedDLL/amd_fidelityfx_vk.dll";
   private static final String XESS_URL = "https://raw.githubusercontent.com/intel/xess/v3.0.2/bin/libxess.dll";
   private static final String DLSS_URL = "https://raw.githubusercontent.com/NVIDIA/DLSS/v310.9.1/lib/Windows_x86_64/rel/nvngx_dlss.dll";
   private static final String STREAMLINE_URL = "https://github.com/NVIDIA-RTX/Streamline/releases/download/v2.14.1/streamline-sdk-v2.14.1.zip";
   private static final String PROPERTY = "viewpoint.natives";
   private static final String FOLDER = "viewpoint-natives";
   private static final ExecutorService DOWNLOADS = Executors.newSingleThreadExecutor(var0 -> {
      Thread var1 = new Thread(var0, "Viewpoint downloads");
      var1.setDaemon(true);
      return var1;
   });
   private static final Map<NativeFiles.Set, NativeFiles.Status> statuses = new ConcurrentHashMap<>();
   private static volatile int installs;

   private NativeFiles() {
   }

   public static File folder() {
      String var0 = System.getProperty("viewpoint.natives");
      return var0 != null ? new File(var0) : new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-natives");
   }

   public static NativeFiles.State state(NativeFiles.Set var0) {
      NativeFiles.Status var1 = status(var0);
      if (var1.state == null) {
         var1.state = present(var0) ? NativeFiles.State.READY : NativeFiles.State.MISSING;
      }

      return var1.state;
   }

   public static boolean ready(NativeFiles.Set var0) {
      return state(var0) == NativeFiles.State.READY;
   }

   public static float progress(NativeFiles.Set var0) {
      return (float)Math.min(1.0, (double)status(var0).done / var0.bytes());
   }

   public static boolean downloaded(NativeFiles.Set var0) {
      return state(var0) == NativeFiles.State.READY && status(var0).done > 0L;
   }

   public static String failure(NativeFiles.Set var0) {
      return status(var0).failure;
   }

   public static int installs() {
      return installs;
   }

   public static void download(NativeFiles.Set var0) {
      NativeFiles.Status var1 = status(var0);
      synchronized (var1) {
         if (state(var0) == NativeFiles.State.DOWNLOADING || state(var0) == NativeFiles.State.READY) {
            return;
         }

         var1.done = 0L;
         var1.failure = null;
         var1.state = NativeFiles.State.DOWNLOADING;
      }

      DOWNLOADS.execute(() -> fetch(var0, var1));
   }

   static File file(String var0) {
      return new File(folder(), var0);
   }

   private static void fetch(NativeFiles.Set var0, NativeFiles.Status var1) {
      Path var2 = folder().toPath();

      try {
         for (NativeFiles.Library var4 : var0.libraries) {
            if (present(var4)) {
               var1.done = var1.done + var4.size();
            } else {
               NativeDownload.fetch(var4, var2, var1x -> var1.done += var1x);
            }
         }

         var1.state = NativeFiles.State.READY;
         installs++;
         System.out.println("[Viewpoint] " + var0.label + ": its files downloaded from " + var0.maker + " into " + var2);
      } catch (IOException | RuntimeException var5) {
         failed(var0, var1, var5);
      } catch (InterruptedException var6) {
         Thread.currentThread().interrupt();
         failed(var0, var1, var6);
      }
   }

   private static void failed(NativeFiles.Set var0, NativeFiles.Status var1, Exception var2) {
      System.out.println("[Viewpoint] " + var0.label + ": the download from " + var0.maker + " failed:");
      var2.printStackTrace(System.out);
      var1.failure = var2.getMessage() == null ? var2.toString() : var2.getMessage();
      var1.state = NativeFiles.State.FAILED;
   }

   private static boolean present(NativeFiles.Set var0) {
      return var0.libraries.stream().allMatch(NativeFiles::present);
   }

   private static boolean present(NativeFiles.Library var0) {
      File var1 = file(var0.name());
      return var1.isFile() && var1.length() == var0.size();
   }

   private static NativeFiles.Status status(NativeFiles.Set var0) {
      return statuses.computeIfAbsent(var0, var0x -> new NativeFiles.Status());
   }

   private static NativeFiles.Library streamline(String var0, long var1, String var3) {
      return new NativeFiles.Library(
         var0, var1, var3, "https://github.com/NVIDIA-RTX/Streamline/releases/download/v2.14.1/streamline-sdk-v2.14.1.zip", "bin/x64/" + var0
      );
   }

   record Library(String name, long size, String sha256, String url, String entry) {
   }

   public enum Set {
      FIDELITY_FX(
         "AMD FSR 3",
         "AMD",
         List.of(
            new NativeFiles.Library(
               "amd_fidelityfx_vk.dll",
               9332432L,
               "a1624cc4238fef046f30c4d80ce3f47be63fc5f5373f49e3ee9edb9960f54c78",
               "https://raw.githubusercontent.com/GPUOpen-LibrariesAndSDKs/FidelityFX-SDK/v1.1.4/PrebuiltSignedDLL/amd_fidelityfx_vk.dll",
               null
            )
         )
      ),
      XESS(
         "Intel XeSS",
         "Intel",
         List.of(
            new NativeFiles.Library(
               "libxess.dll",
               77795704L,
               "251659dd84a3e84de67c886a4186e01f3eca49b00641906fe38bb6b807e5d5b7",
               "https://raw.githubusercontent.com/intel/xess/v3.0.2/bin/libxess.dll",
               null
            )
         )
      ),
      DLSS(
         "NVIDIA DLSS",
         "NVIDIA",
         List.of(
            NativeFiles.streamline("sl.interposer.dll", 652928L, "8c87c9499461da561edd529aa9bf7831d67d7b94ebb1c1a5ed54ef4934e1ea4c"),
            NativeFiles.streamline("sl.common.dll", 843392L, "82924a8954dd671e09351c5de0eb87ad0eb25b944cc9f9ab955ca1d9950de15d"),
            NativeFiles.streamline("sl.dlss.dll", 422016L, "73bf52c0cfaa5900a8f3f4a91306e4625e7cca696dfb305aae44c9f97b582e1f"),
            new NativeFiles.Library(
               "nvngx_dlss.dll",
               58956912L,
               "3975567b8943c53acce397f2b72380092f84f162d00b0d2c7d08a1025c563983",
               "https://raw.githubusercontent.com/NVIDIA/DLSS/v310.9.1/lib/Windows_x86_64/rel/nvngx_dlss.dll",
               null
            )
         )
      );

      public final String label;
      public final String maker;
      final List<NativeFiles.Library> libraries;

      Set(String nullxx, String nullxxx, List<NativeFiles.Library> nullxxxx) {
         this.label = nullxx;
         this.maker = nullxxx;
         this.libraries = nullxxxx;
      }

      public long bytes() {
         return this.libraries.stream().mapToLong(NativeFiles.Library::size).sum();
      }
   }

   public enum State {
      MISSING,
      DOWNLOADING,
      READY,
      FAILED;
   }

   private static final class Status {
      volatile NativeFiles.State state;
      volatile long done;
      volatile String failure;
   }
}
