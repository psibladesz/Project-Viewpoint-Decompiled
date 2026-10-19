package viewpoint.visibility;

public final class Owner {
   public static final int CONTENTS = 0;
   public static final int NORTH = 1;
   public static final int WEST = 2;
   public static final int FLOOR = 3;
   public static final int NONE = -1;

   public static int of(int var0, int var1) {
      return var1 == -1 ? -1 : var0 << 2 | var1;
   }

   public static int square(int var0) {
      return var0 >> 2;
   }

   public static int kind(int var0) {
      return var0 & 3;
   }

   private Owner() {
   }
}
