package viewpoint.packs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import viewpoint.render.PackData;
import viewpoint.render.PackModel;
import viewpoint.render.PackModels;

public final class PackLoads {
   private static final int IN_FLIGHT = 8;
   private static final long RETRY_NANOS = 2000000000L;
   private static final long STALE_NANOS = 5000000000L;
   private static final ThreadPoolExecutor WORKER = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), var0 -> {
      Thread var1 = new Thread(var0, "Viewpoint model packs");
      var1.setDaemon(true);
      var1.setPriority(1);
      return var1;
   });
   private static final ArrayDeque<PackModel> asked = new ArrayDeque<>();
   private static int reading;

   public static boolean want(PackModel var0, long var1) {
      var0.wanted(var1);
      int var3 = var0.state();
      if (var3 == 5 && !PackModels.full() && var1 - var0.refused() > 2000000000L) {
         var0.retry();
         var3 = 0;
      }

      if (var3 == 0) {
         var0.reading();
         asked.add(var0);
      }

      return var3 == 3;
   }

   public static void frame() {
      reading = PackModels.onTheirWay();
      long var0 = System.nanoTime();

      while (reading < 8 && !asked.isEmpty() && !PackModels.full()) {
         PackModel var2 = asked.poll();
         if (var0 - var2.wanted() > 5000000000L) {
            PackModels.dropped(var2);
         } else {
            PackModels.onItsWay();
            reading++;
            WORKER.execute(() -> read(var2));
         }
      }
   }

   public static void clear() {
      for (PackModel var1 : asked) {
         var1.retry();
      }

      asked.clear();
   }

   private static void read(PackModel var0) {
      try {
         var0.read(load(var0.obj));
      } catch (IOException | RuntimeException var2) {
         System.out.println("[Viewpoint] model pack: " + var0.obj + " unreadable, its sprites keep their art: " + var2);
         var0.failed();
         PackModels.offItsWay();
      }
   }

   static PackData load(File var0) throws IOException {
      ObjReader.Mesh var1 = ObjReader.parse(lines(var0));
      if (var1.indices().length == 0) {
         throw new IOException("no triangles");
      }

      if (var1.mtllib() == null) {
         throw new IOException("no mtllib");
      }

      File var2 = new File(var0.getParentFile(), var1.mtllib());
      String var3 = ObjReader.texture(lines(var2));
      if (var3 == null) {
         throw new IOException("no map_Kd in " + var2.getName());
      }

      ArtReader.Art var4 = ArtReader.read(new File(var2.getParentFile(), var3));
      return new PackData(var1.vertices(), var1.indices(), var4.width(), var4.height(), var4.paddedWidth(), var4.paddedHeight(), var4.levels());
   }

   private static List<String> lines(File var0) throws IOException {
      return new String(Files.readAllBytes(var0.toPath()), StandardCharsets.UTF_8).lines().toList();
   }

   private PackLoads() {
   }
}
