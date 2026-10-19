package viewpoint.far;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import viewpoint.platform.LiveSettings;

final class FarWorkers {
   private static final LiveSettings.Number THREADS = LiveSettings.number("far.workers", "Worker threads", "World/Far world", 1.0F, 8.0F, 1.0F, 2.0F);
   static final AtomicInteger jobs = new AtomicInteger();
   static final ThreadPoolExecutor POOL = new ThreadPoolExecutor(2, 2, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), var0 -> {
      Thread var1 = new Thread(var0, "Viewpoint far world");
      var1.setDaemon(true);
      var1.setPriority(1);
      return var1;
   });

   static void resize() {
      int var0 = THREADS.getInt();
      if (var0 > POOL.getMaximumPoolSize()) {
         POOL.setMaximumPoolSize(var0);
         POOL.setCorePoolSize(var0);
      } else if (var0 < POOL.getCorePoolSize()) {
         POOL.setCorePoolSize(var0);
         POOL.setMaximumPoolSize(var0);
      }
   }

   private FarWorkers() {
   }
}
