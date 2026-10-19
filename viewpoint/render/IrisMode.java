package viewpoint.render;

import viewpoint.platform.GlDebug;
import viewpoint.platform.IrisPacks;

final class IrisMode {
   private static final IrisFrame frame = new IrisFrame();
   private static IrisPipeline pipeline;
   private static IrisPacks.Active drawing;
   private static int tried;
   private static boolean ready;

   static boolean prepare() {
      IrisPacks.Active var0 = IrisPacks.active();
      if (var0 == null) {
         return false;
      }

      if (!ready) {
         frame.init();
         ready = true;
      }

      if (var0.generation() != tried) {
         tried = var0.generation();
         link(var0);
      }

      return true;
   }

   static boolean draw(FrameContext var0, ShadowPass var1, ModelPass var2, FarPass var3) {
      if (!prepare()) {
         release();
         return false;
      }

      if (pipeline == null) {
         return false;
      }

      var3.prepare(var0, false);

      try {
         GlDebug.push(drawing.pack().id);
         frame.draw(pipeline, var0, var1, var2, var3);
         GlDebug.pop();
         return true;
      } catch (IllegalStateException var5) {
         GlDebug.pop();
         IrisPacks.failed(drawing, var5.getMessage());
         release();
         return false;
      }
   }

   private static void link(IrisPacks.Active var0) {
      try {
         IrisPipeline var1 = IrisPipeline.create(var0.pack(), var0.values(), IrisPacks.standard());
         if (pipeline != null) {
            pipeline.release();
         }

         pipeline = var1;
         drawing = var0;
         IrisPacks.linked(var0);
         System.out.println("[Viewpoint] Minecraft shader pack " + var0.pack().id + " linked");

         for (String var3 : var1.report) {
            System.out.println("[Viewpoint] Minecraft shader pack " + var0.pack().id + ": " + var3);
         }
      } catch (IllegalStateException var4) {
         IrisPacks.failed(var0, var4.getMessage());
      }
   }

   static IrisPipeline pipeline() {
      return pipeline;
   }

   private static void release() {
      if (pipeline != null) {
         pipeline.release();
         pipeline = null;
         drawing = null;
      }
   }

   private IrisMode() {
   }
}
