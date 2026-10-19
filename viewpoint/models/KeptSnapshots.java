package viewpoint.models;

import java.util.IdentityHashMap;
import java.util.Map;
import viewpoint.core.Frame;
import zombie.core.skinnedmodel.model.ModelSlotRenderData;

final class KeptSnapshots {
   private static final Map<ModelSlotRenderData, int[]> frames = new IdentityHashMap<>();

   static void keep(ModelSlotRenderData var0) {
      synchronized (frames) {
         frames.put(var0, new int[1]);
      }
   }

   static void use(Frame var0, ModelSlotRenderData var1) {
      synchronized (frames) {
         frames.get(var1)[0]++;
      }

      var0.snapshots.add(var1);
   }

   static void replace(ModelSlotRenderData var0) {
      boolean var1;
      synchronized (frames) {
         int[] var3 = frames.get(var0);
         var1 = var3[0] == 0;
         if (var1) {
            frames.remove(var0);
         } else {
            var3[0] = -var3[0] - 1;
         }
      }

      if (var1) {
         var0.postRender();
      }
   }

   static boolean release(ModelSlotRenderData var0) {
      synchronized (frames) {
         int[] var2 = frames.get(var0);
         if (var2 == null) {
            return true;
         }

         if (var2[0] > 0) {
            var2[0]--;
            return false;
         }

         if (var2[0] == 0) {
            throw new IllegalStateException("a kept snapshot released more often than drawn");
         }

         var2[0]++;
         boolean var3 = var2[0] == -1;
         if (var3) {
            frames.remove(var0);
         }

         return var3;
      }
   }

   private KeptSnapshots() {
   }
}
