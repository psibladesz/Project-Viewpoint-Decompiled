package viewpoint.core;

public final class SplitScreen {
   private boolean holding;

   public boolean update(boolean var1, int var2) {
      if (var2 > 1) {
         this.holding |= var1;
         return false;
      } else {
         boolean var3 = this.holding;
         this.holding = false;
         return var1 || var3;
      }
   }

   public boolean holding() {
      return this.holding;
   }

   public static int count(Object[] var0) {
      int var1 = 0;

      for (Object var5 : var0) {
         if (var5 != null) {
            var1++;
         }
      }

      return var1;
   }
}
