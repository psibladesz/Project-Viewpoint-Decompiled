package viewpoint.platform;

import viewpoint.upscale.FrameGeneration;
import viewpoint.upscale.NativeFiles;
import viewpoint.upscale.Upscaling;

final class NativeNeeds {
   private static final String PACK = "pack.";
   private static final String UPSCALING = "." + Pipeline.Knob.UPSCALING.key;

   private NativeNeeds() {
   }

   static NativeNeeds.Need of(String var0, String var1) {
      if (var1 == null) {
         return null;
      }

      if (var0.equals(Graphics.FRAME_GENERATION.key)) {
         return Boolean.parseBoolean(var1)
            ? new NativeNeeds.Need("Frame generation", FrameGeneration.FILES, FrameGeneration.unsupported(), "frame generation stays off")
            : null;
      }

      if (var0.startsWith("pack.") && var0.endsWith(UPSCALING)) {
         for (Upscaling.Kind var5 : Upscaling.Kind.values()) {
            if (var5.name().equalsIgnoreCase(var1)) {
               return new NativeNeeds.Need(var5.label, var5.files, var5.unsupported(), "the game uses TAA");
            }
         }
      }

      return null;
   }

   record Need(String feature, NativeFiles.Set files, String unsupported, String meanwhile) {
   }
}
