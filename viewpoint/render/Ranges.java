package viewpoint.render;

import java.util.TreeMap;
import java.util.Map.Entry;

final class Ranges {
   private final TreeMap<Integer, Integer> free = new TreeMap<>();

   void add(int var1, int var2) {
      Entry<Integer, Integer> var3 = this.free.floorEntry(var1);
      if (var3 != null && var3.getKey() + var3.getValue() == var1) {
         var1 = var3.getKey();
         var2 += var3.getValue();
      }

      Integer var4 = this.free.remove(var1 + var2);
      if (var4 != null) {
         var2 += var4;
      }

      this.free.put(var1, var2);
   }

   int take(int var1) {
      for (Entry<Integer, Integer> var3 : this.free.entrySet()) {
         if (var3.getValue() >= var1) {
            int var4 = var3.getKey();
            int var5 = var3.getValue() - var1;
            this.free.remove(var4);
            if (var5 > 0) {
               this.free.put(var4 + var1, var5);
            }

            return var4;
         }
      }

      return -1;
   }
}