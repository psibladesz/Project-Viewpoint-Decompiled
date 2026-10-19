package viewpoint.render;

import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.Settings;
import viewpoint.platform.Tuning;

final class DevReload {
   private static boolean debugInstalled;
   private static long nextShaderPoll;

   static void frame() {
      if (Settings.dev) {
         if (!debugInstalled) {
            debugInstalled = true;
            GlDebug.install();
         }

         long var0 = System.nanoTime();
         Tuning.poll(var0);
         if (var0 >= nextShaderPoll) {
            nextShaderPoll = var0 + 1000000000L;
            GlProgram.reloadChanged();
         }
      }
   }

   private DevReload() {
   }
}
