package viewpoint.models;

import java.util.Arrays;
import viewpoint.render.ModelDraws;

public final class ModelMotion {
   private static ModelDraws last;
   private static Object[] keys = new Object[2048];
   private static int[] draws = new int[2048];

   public static void begin(ModelDraws var0) {
      Arrays.fill(keys, null);
      if (last != null && last != var0) {
         int var1 = last.count();
         if (var1 * 2 > keys.length) {
            keys = new Object[Integer.highestOneBit(var1 * 2) * 2];
            draws = new int[keys.length];
         }

         for (int var2 = 0; var2 < var1; var2++) {
            Object var3 = last.key(var2);
            if (var3 != null) {
               int var4 = slot(var3);
               if (keys[var4] == null) {
                  keys[var4] = var3;
                  draws[var4] = var2;
               }
            }
         }
      }
   }

   public static void end(ModelDraws var0) {
      last = var0;
   }

   static void follow(ModelDraws var0, int var1, Object var2, int var3) {
      follow(var0, var1, var2, var3, false);
   }

   static void follow(ModelDraws var0, int var1, Object var2, int var3, boolean var4) {
      var0.key(var1, var2);
      if (last != null && last != var0) {
         int var5 = slot(var2);
         if (keys[var5] == var2) {
            var0.motion(var1, last, draws[var5], var3, var4);
         }
      }
   }

   static int lastPose(ModelDraws var0, Object var1, int var2) {
      if (last != null && last != var0) {
         int var3 = slot(var1);
         return keys[var3] == var1 ? var0.lastPose(last, draws[var3], var2) : -1;
      } else {
         return -1;
      }
   }

   private static int slot(Object var0) {
      int var1 = keys.length - 1;
      int var2 = System.identityHashCode(var0) * -1640531527 >>> 7 & var1;

      while (keys[var2] != null && keys[var2] != var0) {
         var2 = var2 + 1 & var1;
      }

      return var2;
   }

   private ModelMotion() {
   }
}
