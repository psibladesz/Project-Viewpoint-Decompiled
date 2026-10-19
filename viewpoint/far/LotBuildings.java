package viewpoint.far;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

final class LotBuildings {
   static final int INTS = 4;
   private static final String EMPTY_OUTSIDE = "emptyoutside";
   private static final byte[] MAGIC = new byte[]{76, 79, 84, 72};
   private final byte[] bytes;
   private int at;

   private LotBuildings(byte[] var1) {
      this.bytes = var1;
   }

   static int[] read(File var0) throws IOException {
      return read(headers(var0));
   }

   static List<File> headers(File var0) {
      File[] var1 = var0.listFiles((var0x, var1x) -> var1x.endsWith(".lotheader"));
      ArrayList var2 = new ArrayList<>(Arrays.asList(var1 == null ? new File[0] : var1));
      var2.sort(Comparator.comparing(File::getName));
      return var2;
   }

   static List<File> within(List<File> var0, int var1, int var2) {
      ArrayList var3 = new ArrayList();

      for (File var5 : var0) {
         String[] var6 = var5.getName().substring(0, var5.getName().length() - ".lotheader".length()).split("_");
         int var7 = Integer.parseInt(var6[0]);
         int var8 = Integer.parseInt(var6[1]);
         if (var7 >= 0 && var8 >= 0 && var7 * 256 < var1 && var8 * 256 < var2) {
            var3.add(var5);
         }
      }

      return var3;
   }

   static int[] read(List<File> var0) throws IOException {
      int[] var1 = new int[0];
      int var2 = 0;

      for (File var4 : var0) {
         String[] var5 = var4.getName().substring(0, var4.getName().length() - ".lotheader".length()).split("_");
         int[] var6 = footprints(Files.readAllBytes(var4.toPath()), Integer.parseInt(var5[0]), Integer.parseInt(var5[1]));
         if (var2 + var6.length > var1.length) {
            var1 = Arrays.copyOf(var1, Math.max(var1.length * 2, var2 + var6.length));
         }

         System.arraycopy(var6, 0, var1, var2, var6.length);
         var2 += var6.length;
      }

      return Arrays.copyOf(var1, var2);
   }

   static int[] footprints(byte[] var0, int var1, int var2) {
      LotBuildings var3 = new LotBuildings(var0);
      var3.at = Arrays.equals(Arrays.copyOf(var0, MAGIC.length), MAGIC) ? MAGIC.length : 0;
      int var4 = var3.integer();
      if (var4 >= 0 && var4 <= 1) {
         for (int var5 = var3.integer(); var5 > 0; var5--) {
            var3.line();
         }

         var3.at += var4 == 0 ? 1 : 0;
         var3.at += 8;
         var3.at += (var4 == 0 ? 1 : 2) * 4;
         int var8 = var3.integer();
         int[][] var6 = new int[var8][];

         for (int var7 = 0; var7 < var8; var7++) {
            var6[var7] = var3.room(var1 * 256, var2 * 256);
         }

         return var3.buildings(var6);
      } else {
         throw new IllegalArgumentException("a lotheader of version " + var4);
      }
   }

   private int[] room(int var1, int var2) {
      boolean var3 = this.line().trim().equals("emptyoutside");
      int var4 = this.integer();
      int[] var5 = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};

      for (int var6 = this.integer(); var6 > 0; var6--) {
         int var7 = this.integer() + var1;
         int var8 = this.integer() + var2;
         int var9 = this.integer();
         int var10 = this.integer();
         var5[0] = Math.min(var5[0], var7);
         var5[1] = Math.min(var5[1], var8);
         var5[2] = Math.max(var5[2], var7 + var9);
         var5[3] = Math.max(var5[3], var8 + var10);
      }

      int var11 = this.integer();
      this.at += var11 * 3 * 4;
      return !var3 && var4 >= 0 && var5[0] <= var5[2] ? var5 : null;
   }

   private int[] buildings(int[][] var1) {
      int var2 = this.integer();
      int[] var3 = new int[var2 * 4];
      int var4 = 0;

      for (int var5 = 0; var5 < var2; var5++) {
         int[] var6 = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};

         for (int var7 = this.integer(); var7 > 0; var7--) {
            int[] var8 = var1[this.integer()];
            if (var8 != null) {
               var6[0] = Math.min(var6[0], var8[0]);
               var6[1] = Math.min(var6[1], var8[1]);
               var6[2] = Math.max(var6[2], var8[2]);
               var6[3] = Math.max(var6[3], var8[3]);
            }
         }

         if (var6[0] < var6[2]) {
            System.arraycopy(var6, 0, var3, var4++ * 4, 4);
         }
      }

      return Arrays.copyOf(var3, var4 * 4);
   }

   private int integer() {
      int var1 = this.bytes[this.at] & 255
         | (this.bytes[this.at + 1] & 255) << 8
         | (this.bytes[this.at + 2] & 255) << 16
         | (this.bytes[this.at + 3] & 255) << 24;
      this.at += 4;
      return var1;
   }

   private String line() {
      int var1 = this.at;

      while (this.bytes[this.at] != 10) {
         this.at++;
      }

      int var2 = this.at > var1 && this.bytes[this.at - 1] == 13 ? this.at - 1 : this.at;
      this.at++;
      return new String(this.bytes, var1, var2 - var1, StandardCharsets.UTF_8);
   }
}
