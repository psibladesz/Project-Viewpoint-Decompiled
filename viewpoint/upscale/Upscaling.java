package viewpoint.upscale;

import java.util.function.Supplier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.vulkan.VkCommandBuffer;

public final class Upscaling implements AutoCloseable {
   public final Upscaling.Kind kind;
   public final int scale;
   public final int frameWidth;
   public final int frameHeight;
   public final int outputWidth;
   public final int outputHeight;
   private final Vendor vendor;
   private final VulkanDevice vk;
   private final Crossing crossing;
   private final Vendor.Images images;
   private final SharedImage[] all;

   private Upscaling(Upscaling.Kind var1, int var2, int var3, int var4) {
      this.kind = var1;
      this.scale = var2;
      this.outputWidth = var3;
      this.outputHeight = var4;
      this.vendor = var1.vendor.get();
      this.vk = VulkanDevice.open(this.vendor.needs());
      this.crossing = new Crossing(this.vk);
      Vendor.Size var5 = this.vendor.create(this.vk, var2, var3, var4);
      this.frameWidth = var5.width();
      this.frameHeight = var5.height();
      this.images = new Vendor.Images(
         new SharedImage(this.vk, 97, 34842, this.frameWidth, this.frameHeight),
         new SharedImage(this.vk, 100, 33326, this.frameWidth, this.frameHeight),
         new SharedImage(this.vk, 83, 33327, this.frameWidth, this.frameHeight),
         new SharedImage(this.vk, 97, 34842, var3, var4)
      );
      this.all = this.images.all();
      GL11.glFinish();
   }

   public static Upscaling open(Upscaling.Kind var0, int var1, int var2, int var3) {
      String var4 = var0.unavailable();
      if (var4 != null) {
         throw new IllegalStateException(var0.label + " cannot run: " + var4);
      } else {
         return new Upscaling(var0, var1, var2, var3);
      }
   }

   public int color() {
      return this.images.color().texture;
   }

   public int depth() {
      return this.images.depth().texture;
   }

   public int motion() {
      return this.images.motion().texture;
   }

   public int output() {
      return this.images.output().texture;
   }

   public void run(UpscaleFrame var1) {
      this.crossing.handOver(this.all);
      VkCommandBuffer var2 = this.vk.begin();
      this.crossing.acquire(var2, this.all);
      this.vendor.record(var2, this.images, var1);
      this.crossing.release(var2, this.all);
      this.crossing.submit(var2);
      this.vendor.submitted();
      this.crossing.takeBack(this.all);
   }

   @Override
   public void close() {
      GL11.glFinish();
      this.vk.idle();
      this.vendor.close();
      this.crossing.close();

      for (SharedImage var4 : this.all) {
         var4.close();
      }

      this.vk.close();
   }

   public enum Kind {
      FSR3("FSR 3", NativeFiles.Set.FIDELITY_FX, FsrVendor::new),
      XESS("XeSS", NativeFiles.Set.XESS, XessVendor::new),
      DLSS("DLSS", NativeFiles.Set.DLSS, DlssVendor::new);

      public final String label;
      public final NativeFiles.Set files;
      private final Supplier<Vendor> vendor;

      Kind(String nullxx, NativeFiles.Set nullxxx, Supplier<Vendor> nullxxxx) {
         this.label = nullxx;
         this.files = nullxxx;
         this.vendor = nullxxxx;
      }

      public String unsupported() {
         String var1 = VulkanDevice.unavailable();
         if (var1 != null) {
            return var1;
         } else {
            return this == DLSS ? DlssVendor.unavailable() : null;
         }
      }

      public String unavailable() {
         String var1 = this.unsupported();
         if (var1 != null) {
            return var1;
         } else {
            return NativeFiles.ready(this.files) ? null : this.files.label + "'s files are not downloaded yet (settings, Graphics)";
         }
      }
   }
}
