package viewpoint.platform;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class ShaderWatch {
   private static final long PERIOD_MS = 1000L;
   private static volatile Set<String> files = Set.of();
   private static volatile Map<String, Long> stamps = Map.of();
   private static boolean started;

   static void watch(Set<String> var0) {
      files = Set.copyOf(var0);
      if (!started) {
         started = true;
         Thread var1 = new Thread(ShaderWatch::run, "Viewpoint shader watch");
         var1.setDaemon(true);
         var1.start();
      }
   }

   static Map<String, Long> stamps() {
      return stamps;
   }

   private static void run() {
      try {
         while (true) {
            HashMap var0 = new HashMap();
            ShaderPacks.Active var1 = ShaderPacks.drawn();

            for (String var3 : files) {
               var0.put(var3, Shaders.modified(var3, var1));
            }

            stamps = var0;
            Thread.sleep(1000L);
         }
      } catch (InterruptedException var4) {
         Thread.currentThread().interrupt();
      }
   }

   private ShaderWatch() {
   }
}
