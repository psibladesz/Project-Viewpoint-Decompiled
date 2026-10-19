package viewpoint.assets;

import imgui.ImGui;
import java.util.ArrayList;
import java.util.List;
import viewpoint.far.Relief;
import viewpoint.render.FarRelief;
import viewpoint.render.WorldRenderer;

final class WarmupBar {
   private static final long READY_NANOS = 2500000000L;
   private static final long MODELS_NANOS = 20000000000L;
   private static final float MARGIN = 12.0F;
   private static final float WIDTH = 300.0F;
   private static final float BAR_HEIGHT = 0.6F;
   private static final int FLAGS = 398187;
   private static volatile boolean started;
   private static boolean modelsSettled;
   private static long readySince;
   private static long playSince;

   static void started() {
      started = true;
   }

   static boolean showing() {
      if (!started) {
         return false;
      }

      if (!parts().isEmpty()) {
         readySince = 0L;
         return true;
      }

      long var0 = System.nanoTime();
      if (readySince == 0L) {
         readySince = var0;
      }

      if (var0 - readySince > 2500000000L) {
         started = false;
         playSince = 0L;
         readySince = 0L;
         modelsSettled = false;
         return false;
      } else {
         return true;
      }
   }

   static void draw() {
      List var0 = parts();
      float var1 = ImGui.getIO().getFontGlobalScale();
      float var2 = ImGui.getIO().getDisplaySizeY() - 12.0F * var1;
      ImGui.setNextWindowPos(12.0F * var1, var2, 1, 0.0F, 1.0F);
      ImGui.setNextWindowBgAlpha(0.7F);
      if (ImGui.begin("###viewpointWarmup", 398187)) {
         WarmupBar.Part var3 = var0.isEmpty() ? null : (WarmupBar.Part)var0.get(0);
         ImGui.textUnformatted(var3 == null ? "Viewpoint: first person is ready" : "Viewpoint: getting ready");
         ImGui.progressBar(share(var3), 300.0F * var1, ImGui.getTextLineHeight() * 0.6F, "");
         if (var3 != null) {
            ImGui.textUnformatted(var3.of() == 0 ? var3.what() : var3.what() + ": " + var3.done() + " of " + var3.of());
         }
      }

      ImGui.end();
   }

   private static List<WarmupBar.Part> parts() {
      ArrayList var0 = new ArrayList();
      int var1 = Warmup.shadersMade();
      int var2 = Warmup.shadersListed();
      if (var1 < var2) {
         var0.add(new WarmupBar.Part("Model shaders", var1, var2));
      }

      float var3 = WorldRenderer.preparedShare();
      if (Warmup.rendererWanted() && var3 < 1.0F) {
         var0.add(new WarmupBar.Part("Renderer steps", Math.round(var3 * 20.0F), 20));
      }

      if (Relief.reading() || Warmup.rendererWanted() && FarRelief.uploadDue()) {
         var0.add(new WarmupBar.Part("Terrain relief", 0, 0));
      }

      if (SoundFiles.busy()) {
         var0.add(new WarmupBar.Part("Sound files", SoundFiles.done(), SoundFiles.listed()));
      }

      int var4 = ModelPrefetch.loaded();
      int var5 = ModelPrefetch.done();
      if (!modelsSettled && !Warmup.loading() && ModelPrefetch.ON.get()) {
         long var6 = System.nanoTime();
         playSince = playSince == 0L ? var6 : playSince;
         if ((var4 <= 0 || var5 < var4) && var6 - playSince <= 20000000000L) {
            var0.add(new WarmupBar.Part("Nearby chunks' models", var5, var4));
         } else {
            modelsSettled = true;
         }
      }

      return var0;
   }

   private static float share(WarmupBar.Part var0) {
      return var0 == null ? 1.0F : (var0.of() == 0 ? 0.0F : (float)var0.done() / var0.of());
   }

   private WarmupBar() {
   }

   private record Part(String what, int done, int of) {
   }
}
