package viewpoint.world;

import java.util.Arrays;
import viewpoint.platform.Pool;
import viewpoint.render.BandLight;
import viewpoint.render.ChunkMeshData;

final class ChunkBudget {
   private static final long VERTEX_BYTES = 56L;
   private static long vertices;
   private static int meshes;
   static boolean tight;
   private static ChunkCache.Level[] outOfView = new ChunkCache.Level[256];
   private static long[] keys = new long[256];
   private static int count;

   static void begin() {
      Arrays.fill(outOfView, 0, count, null);
      count = 0;
   }

   static void hold(ChunkMeshData var0) {
      if (var0 != null) {
         vertices = vertices + var0.vertices();
         meshes++;
      }
   }

   static void letGo(ChunkMeshData var0) {
      if (var0 != null) {
         vertices = vertices - var0.vertices();
         meshes--;
         var0.retire();
      }
   }

   static void outOfView(ChunkCache.Level var0, float var1) {
      if (count == outOfView.length) {
         outOfView = Arrays.copyOf(outOfView, count * 2);
         keys = Arrays.copyOf(keys, count * 2);
      }

      keys[count] = (long)Float.floatToIntBits(Math.max(0.0F, var1)) << 32 | count;
      outOfView[count++] = var0;
   }

   static boolean full() {
      count();
      return Pool.NEAR_MESHES.over(0.9) || Pool.LIGHT_GRIDS.over(0.9);
   }

   static void makeRoom() {
      if (full()) {
         Arrays.sort(keys, 0, count);

         for (int var0 = count - 1; var0 >= 0 && (Pool.NEAR_MESHES.over(0.8) || Pool.LIGHT_GRIDS.over(0.8)); var0--) {
            unbuild(outOfView[(int)keys[var0]]);
            count();
         }
      }

      tight = full();
   }

   private static void count() {
      Pool.NEAR_MESHES.use(vertices * 56L);
      Pool.LIGHT_GRIDS.use((meshes + BandLight.grids()) * 3200L);
   }

   private static void unbuild(ChunkCache.Level var0) {
      letGo(var0.data);
      letGo(var0.fading);
      var0.data = var0.fading = null;
      var0.fadeNanos = 0L;
      var0.modelled = var0.fadingModelled = null;
      var0.modelledItems = var0.fadingModelledItems = null;
      var0.bodies = var0.fadingBodies = null;
      var0.fingerprint = 0L;
      var0.version++;
      var0.dirty = true;
      var0.changed = true;
      var0.reason = 0;
      var0.retryFrame = 0L;
      var0.retryDelay = 0;
   }

   private ChunkBudget() {
   }
}
