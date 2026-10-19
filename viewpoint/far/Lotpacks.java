package viewpoint.far;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import viewpoint.platform.Caches;

final class Lotpacks implements Caches.Cache {
   private static final int KEEP = 4;
   private static final LinkedHashMap<String, Lotpacks.Entry> kept = new LinkedHashMap<>(8, 0.75F, true);
   private static long held;
   private static final ConcurrentLinkedQueue<Lotpacks.Read> READ = new ConcurrentLinkedQueue<>();

   static byte[] get(String var0) {
      Lotpacks.Entry var1 = kept.get(var0);
      if (var1 == null) {
         return null;
      }

      var1.used = Caches.tick();
      return var1.bytes;
   }

   static byte[] read(String var0, byte[] var1) throws IOException {
      if (var1 != null) {
         return var1;
      }

      byte[] var2 = Files.readAllBytes(new File(var0).toPath());
      READ.add(new Lotpacks.Read(var0, var2));
      return var2;
   }

   static void takeIn() {
      Lotpacks.Read var0;
      while ((var0 = READ.poll()) != null) {
         if (!kept.containsKey(var0.path)) {
            kept.put(var0.path, new Lotpacks.Entry(var0.bytes));
            held = held + var0.bytes.length;
         }
      }

      Iterator var1 = kept.values().iterator();

      while (kept.size() > 4 && var1.hasNext()) {
         held = held - ((Lotpacks.Entry)var1.next()).bytes.length;
         var1.remove();
      }
   }

   static void clear() {
      kept.clear();
      READ.clear();
      held = 0L;
   }

   @Override
   public long bytes() {
      return held;
   }

   @Override
   public void list(Caches.Survey var1) {
      for (Lotpacks.Entry var3 : kept.values()) {
         var1.add(var3.used, var3.bytes.length);
      }
   }

   @Override
   public void evictBefore(long var1) {
      Iterator var3 = kept.values().iterator();

      while (var3.hasNext()) {
         Lotpacks.Entry var4 = (Lotpacks.Entry)var3.next();
         if (var4.used < var1) {
            held = held - var4.bytes.length;
            var3.remove();
         }
      }
   }

   static {
      Caches.register(new Lotpacks());
   }

   private static final class Entry {
      final byte[] bytes;
      long used;

      Entry(byte[] var1) {
         this.bytes = var1;
         this.used = Caches.tick();
      }
   }

   private record Read(String path, byte[] bytes) {
   }
}
