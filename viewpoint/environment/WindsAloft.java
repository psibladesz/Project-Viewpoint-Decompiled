package viewpoint.environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class WindsAloft {
   static final int LOW = 0;
   static final int MID = 1;
   static final int HIGH = 2;
   static final int LEVELS = 3;
   private static final String FILE = "/viewpoint/weather/winds-aloft.csv";
   private static final int FIRST_WIND = 3;
   private static final float[][][] MONTHS = read();

   static int soundings(int var0) {
      return MONTHS[var0].length;
   }

   static float from(int var0, int var1, int var2) {
      return MONTHS[var0][var1][2 * var2];
   }

   static float speed(int var0, int var1, int var2) {
      return MONTHS[var0][var1][2 * var2 + 1];
   }

   private static float[][][] read() {
      ArrayList<List<float[]>> var0 = new ArrayList<>();

      for (int var1 = 0; var1 < 12; var1++) {
         var0.add(new ArrayList<>());
      }

      try (InputStream var10 = WindsAloft.class.getResourceAsStream("/viewpoint/weather/winds-aloft.csv")) {
         if (var10 == null) {
            throw new IllegalStateException("no /viewpoint/weather/winds-aloft.csv in the JAR");
         }

         BufferedReader var2 = new BufferedReader(new InputStreamReader(var10, StandardCharsets.UTF_8));

         for (String var3 = var2.readLine(); var3 != null; var3 = var2.readLine()) {
            if (!var3.isBlank() && !var3.startsWith("#")) {
               String[] var4 = var3.split(",");
               float[] var5 = new float[6];

               for (int var6 = 0; var6 < var5.length; var6++) {
                  var5[var6] = Float.parseFloat(var4[3 + var6]);
               }

               var0.get(Integer.parseInt(var4[1].substring(5, 7)) - 1).add(var5);
            }
         }
      } catch (IOException var9) {
         throw new UncheckedIOException("reading /viewpoint/weather/winds-aloft.csv", var9);
      }

      float[][][] var11 = new float[12][][];

      for (int var12 = 0; var12 < 12; var12++) {
         if (var0.get(var12).isEmpty()) {
            throw new IllegalStateException("/viewpoint/weather/winds-aloft.csv has no sounding for month " + (var12 + 1));
         }

         var11[var12] = var0.get(var12).toArray(new float[0][]);
      }

      return var11;
   }

   private WindsAloft() {
   }
}