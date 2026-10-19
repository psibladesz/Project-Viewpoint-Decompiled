package viewpoint.assets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import viewpoint.models.Models;
import viewpoint.platform.LiveSettings;
import viewpoint.world.WorldMesher;
import zombie.characters.IsoGameCharacter;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoChunkMap;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.objects.IsoWorldInventoryObject;

public final class ModelPrefetch {
   static final LiveSettings.Toggle ON = LiveSettings.toggle("assets.modelsAhead", "Models of what is near loaded ahead", "Game/Loading ahead", true);
   private static final long BUDGET_NANOS = 1000000L;
   private static final int AGAIN_FRAMES = 15;
   private static final int MOST_PASSES = 40;
   private static final int PLAYER = 0;
   private static final int REACH = 6;
   private static final int BASEMENT_RING = 3;
   static final int[][] ORDER = order();
   private static final Map<IsoChunk, Long> settled = new IdentityHashMap<>();
   private static final ArrayList<ModelPrefetch.Job> jobs = new ArrayList<>();
   private static long frame;
   private static volatile int loaded;
   private static volatile int done;
   private static long began;
   private static int asks;
   private static boolean reported;

   static void clear() {
      settled.clear();
      jobs.clear();
      asks = 0;
      done = 0;
      loaded = 0;
      began = 0L;
      reported = false;
   }

   static int loaded() {
      return loaded;
   }

   static int done() {
      return done;
   }

   static void frame(IsoCell var0, IsoGameCharacter var1) {
      if (ON.get()) {
         frame++;
         long var2 = System.nanoTime() + 1000000L;
         began = began == 0L ? System.nanoTime() : began;
         gatherNext(var0.getChunkMap(0), var1);
         report();
         Iterator var4 = jobs.iterator();

         while (var4.hasNext() && System.nanoTime() < var2) {
            ModelPrefetch.Job var5 = (ModelPrefetch.Job)var4.next();
            if (var5.chunk.wx == var5.wx && var5.chunk.wy == var5.wy && var5.chunk.loaded) {
               if (var5.due <= frame && ask(var5, var2)) {
                  if (!var5.left.isEmpty() && ++var5.passes < 40) {
                     var5.due = frame + 15L;
                  } else {
                     settled.put(var5.chunk, place(var5.wx, var5.wy));
                     var4.remove();
                  }
               }
            } else {
               var4.remove();
            }
         }
      }
   }

   private static boolean ask(ModelPrefetch.Job var0, long var1) {
      Iterator var3 = var0.left.iterator();

      while (var3.hasNext()) {
         if (System.nanoTime() >= var1) {
            return false;
         }

         Object var4 = var3.next();
         asks++;
         boolean var5;
         if (var4 instanceof IsoObject var6) {
            IsoGridSquare var7 = var6.getSquare();
            var5 = var7 != null && var6.getSpriteModel() != null && Models.askAhead(var6, var7, 0);
         } else {
            IsoWorldInventoryObject var9 = (IsoWorldInventoryObject)var4;
            IsoGridSquare var8 = var9.getSquare();
            var5 = var8 != null && var9.getItem() != null && Models.askAhead(var9, var8);
         }

         if (!var5) {
            var3.remove();
         }
      }

      return true;
   }

   private static void gatherNext(IsoChunkMap var0, IsoGameCharacter var1) {
      int var2 = Math.floorDiv((int)Math.floor(var1.getX()), 8);
      int var3 = Math.floorDiv((int)Math.floor(var1.getY()), 8);
      int var4 = var1.getZ() < 0.0F ? (int)Math.floor(var1.getZ()) - 1 : 0;
      int var5 = 0;
      int var6 = 0;
      IsoChunk var7 = null;
      int var8 = 0;

      for (int[] var12 : ORDER) {
         IsoChunk var13 = var0.getChunkForGridSquare((var2 + var12[0]) * 8, (var3 + var12[1]) * 8);
         if (var13 != null && var13.loaded) {
            var5++;
            Long var14 = settled.get(var13);
            if (var14 != null && var14 == place(var13.wx, var13.wy)) {
               var6++;
            } else if (var7 == null && !asked(var13)) {
               var7 = var13;
               var8 = Math.max(Math.abs(var12[0]), Math.abs(var12[1]));
            }
         }
      }

      loaded = var5;
      done = var6;
      if (var7 != null) {
         jobs.add(gather(var7, var8 <= 3 ? var7.minLevel : Math.max(var4, var7.minLevel)));
      }
   }

   private static void report() {
      if (!reported && loaded > 0 && done >= loaded) {
         reported = true;
         System.out
            .printf("[Viewpoint] nearby models loaded ahead: %d chunks, %d asks, in %.1f s of play%n", loaded, asks, (System.nanoTime() - began) / 1.0E9);
      }
   }

   private static boolean asked(IsoChunk var0) {
      for (int var1 = 0; var1 < jobs.size(); var1++) {
         if (jobs.get(var1).chunk == var0) {
            return true;
         }
      }

      return false;
   }

   private static ModelPrefetch.Job gather(IsoChunk var0, int var1) {
      ModelPrefetch.Job var2 = new ModelPrefetch.Job(var0);

      for (int var3 = var1; var3 <= var0.maxLevel; var3++) {
         for (int var4 = 0; var4 < 8; var4++) {
            for (int var5 = 0; var5 < 8; var5++) {
               IsoGridSquare var6 = var0.getGridSquare(var5, var4, var3);
               if (var6 != null) {
                  things(var6, var2.left);
               }
            }
         }
      }

      return var2;
   }

   private static void things(IsoGridSquare var0, ArrayList<Object> var1) {
      for (int var2 = 0; var2 < var0.getObjects().size(); var2++) {
         IsoObject var3 = (IsoObject)var0.getObjects().get(var2);
         if (WorldMesher.hasModel(var3)) {
            var1.add(var3);
         }
      }

      for (int var4 = 0; var4 < var0.getWorldObjects().size(); var4++) {
         IsoWorldInventoryObject var5 = (IsoWorldInventoryObject)var0.getWorldObjects().get(var4);
         if (WorldMesher.hasModel(var5.getItem())) {
            var1.add(var5);
         }
      }
   }

   private static long place(int var0, int var1) {
      return (long)var0 << 32 | var1 & 4294967295L;
   }

   private static int[][] order() {
      byte var0 = 13;
      int[][] var1 = new int[var0 * var0][];

      for (int var2 = 0; var2 < var1.length; var2++) {
         var1[var2] = new int[]{var2 % var0 - 6, var2 / var0 - 6};
      }

      Arrays.sort(var1, Comparator.comparingInt(var0x -> var0x[0] * var0x[0] + var0x[1] * var0x[1]));
      return var1;
   }

   private ModelPrefetch() {
   }

   static {
      ON.describe(
         "Asks the game for the 3D models of the objects and items in the loaded area before they first show, a little each frame, the nearest first: no object appears late, and the first switch to first person finds them loaded."
      );
   }

   private static final class Job {
      final IsoChunk chunk;
      final int wx;
      final int wy;
      final ArrayList<Object> left = new ArrayList<>();
      long due;
      int passes;

      Job(IsoChunk var1) {
         this.chunk = var1;
         this.wx = var1.wx;
         this.wy = var1.wy;
      }
   }
}
