package viewpoint.platform;

import java.util.Arrays;
import java.util.IdentityHashMap;

public final class DrawCounts {
   public static final int NEAR = 0;
   public static final int SHELL = 1;
   public static final int FAR = 2;
   public static final int MODELS = 3;
   public static final int PACKS = 4;
   public static final int PLAIN = 5;
   private static final String[] NAMES = new String[]{"near", "shell", "far boxes, insides and lamps", "models", "packs", "full-screen and generated"};
   private static final int[] draws = new int[NAMES.length];
   private static final int[] passes = new int[NAMES.length];
   private static final int[] pages = new int[NAMES.length];
   private static final IdentityHashMap<Object, Boolean> seen = new IdentityHashMap<>();

   public static void draw(int var0) {
      draws[var0]++;
      Profile.draws++;
   }

   public static void pass(int var0) {
      passes[var0]++;
      seen.clear();
   }

   public static void page(int var0, Object var1) {
      if (seen.put(var1, Boolean.TRUE) == null) {
         pages[var0]++;
      }
   }

   static String report(int var0) {
      StringBuilder var1 = new StringBuilder(" | draws/frame by source");
      double var2 = Math.max(var0, 1);

      for (int var4 = 0; var4 < NAMES.length; var4++) {
         var1.append(var4 == 0 ? " " : ", ").append(NAMES[var4]).append(String.format(" %.0f", draws[var4] / var2));
         if (passes[var4] > 0) {
            var1.append(String.format(" (%.1f passes, %.1f atlas pages a pass)", passes[var4] / var2, (double)pages[var4] / passes[var4]));
         }
      }

      Arrays.fill(draws, 0);
      Arrays.fill(passes, 0);
      Arrays.fill(pages, 0);
      return var1.toString();
   }

   private DrawCounts() {
   }
}
