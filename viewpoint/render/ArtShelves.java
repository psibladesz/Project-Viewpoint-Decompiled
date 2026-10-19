package viewpoint.render;

import java.util.ArrayList;

final class ArtShelves {
   private final int size;
   private final int grain;
   private final ArrayList<ArtShelves.Shelf> shelves = new ArrayList<>();
   private int top;

   ArtShelves(int var1, int var2) {
      this.size = var1;
      this.grain = var2;
   }

   int[] take(int var1, int var2) {
      if (var1 <= this.size && var2 <= this.size && var1 % this.grain == 0 && var2 % this.grain == 0) {
         for (ArtShelves.Shelf var4 : this.shelves) {
            if (var4.height >= var2 && var4.height * 3 <= var2 * 4) {
               int var5 = var4.take(var1);
               if (var5 >= 0) {
                  return new int[]{var5, var4.y};
               }
            }
         }

         if (this.top + var2 > this.size) {
            return null;
         }

         ArtShelves.Shelf var6 = new ArtShelves.Shelf(this.top, var2, this.size);
         this.shelves.add(var6);
         this.top += var2;
         return new int[]{var6.take(var1), var6.y};
      } else {
         return null;
      }
   }

   void give(int var1, int var2, int var3) {
      for (int var4 = 0; var4 < this.shelves.size(); var4++) {
         ArtShelves.Shelf var5 = this.shelves.get(var4);
         if (var5.y == var2) {
            var5.give(var1, var3);

            while (!this.shelves.isEmpty() && this.shelves.get(this.shelves.size() - 1).empty(this.size)) {
               this.top = this.top - this.shelves.remove(this.shelves.size() - 1).height;
            }

            return;
         }
      }

      throw new IllegalArgumentException("no shelf at y " + var2);
   }

   boolean empty() {
      return this.shelves.isEmpty();
   }

   private static final class Shelf {
      final int y;
      final int height;
      final ArrayList<int[]> free = new ArrayList<>();

      Shelf(int var1, int var2, int var3) {
         this.y = var1;
         this.height = var2;
         this.free.add(new int[]{0, var3});
      }

      int take(int var1) {
         for (int var2 = 0; var2 < this.free.size(); var2++) {
            int[] var3 = this.free.get(var2);
            if (var3[1] >= var1) {
               int var4 = var3[0];
               var3[0] += var1;
               var3[1] -= var1;
               if (var3[1] == 0) {
                  this.free.remove(var2);
               }

               return var4;
            }
         }

         return -1;
      }

      void give(int var1, int var2) {
         int var3 = 0;

         while (var3 < this.free.size() && ((int[])this.free.get(var3))[0] < var1) {
            var3++;
         }

         this.free.add(var3, new int[]{var1, var2});
         if (var3 + 1 < this.free.size() && var1 + var2 == this.free.get(var3 + 1)[0]) {
            this.free.get(var3)[1] += ((int[])this.free.remove(var3 + 1))[1];
         }

         if (var3 > 0 && this.free.get(var3 - 1)[0] + this.free.get(var3 - 1)[1] == var1) {
            this.free.get(var3 - 1)[1] += ((int[])this.free.remove(var3))[1];
         }
      }

      boolean empty(int var1) {
         return this.free.size() == 1 && this.free.get(0)[0] == 0 && this.free.get(0)[1] == var1;
      }
   }
}
