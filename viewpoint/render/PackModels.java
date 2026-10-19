package viewpoint.render;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL44;
import viewpoint.platform.Pool;

public final class PackModels {
   static final int TABLE_TEXELS = 2;
   private static final long UPLOAD_BYTES = 4194304L;
   private static final long KEEP_NANOS = 10000000000L;
   private static final int ROOM_CHECK_FRAMES = 30;
   private static final HashMap<File, PackModel> byFile = new HashMap<>();
   private static final ArrayList<PackModel> made = new ArrayList<>();
   private static volatile PackModel[] byIndex = new PackModel[0];
   static final ConcurrentLinkedQueue<PackModel> READ = new ConcurrentLinkedQueue<>();
   static final ConcurrentLinkedQueue<PackModel> SETTLED = new ConcurrentLinkedQueue<>();
   private static final AtomicInteger onTheirWay = new AtomicInteger();
   private static volatile boolean full;
   private static volatile int roomAgain;
   private static final ArrayList<PackModel> resident = new ArrayList<>();
   private static int tableBuffer;
   private static int tableTexture;
   private static int tableModels;
   private static int turnedAway;
   private static int fullFrames;
   private static final FloatBuffer entry = BufferUtils.createFloatBuffer(8);

   public static PackModel of(File var0) {
      PackModel var1 = byFile.get(var0);
      if (var1 == null) {
         var1 = new PackModel(made.size(), var0);
         byFile.put(var0, var1);
         made.add(var1);
      }

      return var1;
   }

   public static void publish() {
      if (made.size() != byIndex.length) {
         byIndex = made.toArray(new PackModel[0]);
      }
   }

   public static PackModel get(int var0) {
      PackModel[] var1 = byIndex;
      return var0 >= 0 && var0 < var1.length ? var1[var0] : null;
   }

   public static void onItsWay() {
      onTheirWay.incrementAndGet();
   }

   public static void offItsWay() {
      onTheirWay.decrementAndGet();
   }

   public static int onTheirWay() {
      return onTheirWay.get();
   }

   public static PackModel settled() {
      return SETTLED.poll();
   }

   public static void dropped(PackModel var0) {
      var0.retry();
      SETTLED.add(var0);
   }

   public static boolean full() {
      return full;
   }

   public static int roomAgain() {
      return roomAgain;
   }

   static void upload(long var0) {
      long var2 = 0L;

      PackModel var4;
      while (var2 < 4194304L && (var4 = READ.poll()) != null) {
         PackData var5 = var4.take();
         var2 += var5.bytes();
         onTheirWay.decrementAndGet();
         if (place(var4, var5, var0)) {
            var4.state(3);
            resident.add(var4);
         } else {
            var4.refused(var0);
            var4.state(5);
            turnedAway++;
            full = true;
         }

         SETTLED.add(var4);
      }

      if (full && ++fullFrames % 30 == 0 && room(var0)) {
         full = false;
         roomAgain++;
      }

      Pool.PACKS.use(PackArena.bytes() + PackArt.bytes());
   }

   private static boolean room(long var0) {
      if (Pool.PACKS.cap() - PackArena.bytes() - PackArt.bytes() >= PackArt.PAGE_BYTES) {
         return true;
      }

      for (PackModel var3 : resident) {
         if (!var3.held() && var0 - var3.wanted() > 10000000000L) {
            return true;
         }
      }

      return false;
   }

   static int table() {
      return tableTexture;
   }

   private static boolean place(PackModel var0, PackData var1, long var2) {
      do {
         long var4 = Pool.PACKS.cap() - PackArena.bytes() - PackArt.bytes();
         if (PackArena.put(var0, var1, var4)) {
            var4 = Pool.PACKS.cap() - PackArena.bytes() - PackArt.bytes();
            if (PackArt.put(var0, var1, var4)) {
               tableEntry(var0);
               return true;
            }

            PackArena.free(var0);
         }
      } while (letGoOne(var2));

      return false;
   }

   private static boolean letGoOne(long var0) {
      PackModel var2 = null;

      for (PackModel var4 : resident) {
         if (!var4.held() && var0 - var4.wanted() > 10000000000L && (var2 == null || var4.wanted() < var2.wanted())) {
            var2 = var4;
         }
      }

      if (var2 == null) {
         return false;
      }

      resident.remove(var2);
      PackArena.free(var2);
      PackArt.free(var2);
      var2.state(0);
      return true;
   }

   private static void tableEntry(PackModel var0) {
      int var1 = Math.max(var0.index + 1, byIndex.length);
      if (var1 > tableModels) {
         growTable(Math.max(var1, tableModels * 2));
      }

      float var2 = 2048.0F;
      entry.clear();
      entry.put(var0.artX / var2).put(var0.artY / var2);
      entry.put(var0.artWidth / var2).put(var0.artHeight / var2);
      entry.put((var0.artX + var0.artW) / var2).put((var0.artY + var0.artH) / var2);
      entry.put(var0.artPage).put(0.0F).flip();
      GL15.glBindBuffer(35882, tableBuffer);
      GL15.glBufferSubData(35882, var0.index * 2L * 16L, entry);
      GL15.glBindBuffer(35882, 0);
   }

   private static void growTable(int var0) {
      int var1 = GL15.glGenBuffers();
      GL15.glBindBuffer(36663, var1);
      GL44.glBufferStorage(36663, var0 * 2L * 16L, 256);
      if (tableBuffer != 0) {
         GL15.glBindBuffer(36662, tableBuffer);
         GL31.glCopyBufferSubData(36662, 36663, 0L, 0L, tableModels * 2L * 16L);
         GL15.glBindBuffer(36662, 0);
         int var2 = tableBuffer;
         int var3 = tableTexture;
         Retirement.retireDrawing(() -> {
            GL11.glDeleteTextures(var3);
            GL15.glDeleteBuffers(var2);
         });
      }

      GL15.glBindBuffer(36663, 0);
      tableBuffer = var1;
      tableModels = var0;
      tableTexture = GL11.glGenTextures();
      GL11.glBindTexture(35882, tableTexture);
      GL31.glTexBuffer(35882, 34836, tableBuffer);
      GL11.glBindTexture(35882, 0);
   }

   static String report() {
      String var0 = resident.size()
         + " models on the GPU ("
         + PackArena.pages()
         + " mesh pages, "
         + PackArt.pages()
         + " art pages), "
         + onTheirWay.get()
         + " on their way, "
         + turnedAway
         + " turned away";
      turnedAway = 0;
      return var0;
   }

   private PackModels() {
   }
}
