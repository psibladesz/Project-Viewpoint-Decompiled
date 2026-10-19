package viewpoint.far;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.Executor;

final class FarColours {
   private static volatile HashMap<String, Integer> table;
   private static volatile boolean loading;

   static boolean ready() {
      return table != null;
   }

   static void load(Executor var0) {
      if (!loading) {
         loading = true;
         var0.execute(() -> {
            HashMap var0x = new HashMap(65536);

            try (InputStream var1 = FarColours.class.getResourceAsStream("/viewpoint/far-colours.txt")) {
               if (var1 == null) {
                  System.out.println("[Viewpoint] far-colours.txt missing from the jar: the far world takes the map's colours");
               } else {
                  BufferedReader var2 = new BufferedReader(new InputStreamReader(var1, StandardCharsets.UTF_8));

                  String var3;
                  while ((var3 = var2.readLine()) != null) {
                     String[] var4 = var3.split(" ");
                     if (var4.length == 4) {
                        var0x.putIfAbsent(var4[0], Integer.parseInt(var4[1]) << 16 | Integer.parseInt(var4[2]) << 8 | Integer.parseInt(var4[3]));
                     }
                  }
               }
            } catch (Exception var7) {
               System.out.println("[Viewpoint] far-colours.txt unreadable: " + var7);
            }

            table = var0x;
         });
      }
   }

   static int of(String var0) {
      HashMap var1 = table;
      Integer var2 = var1 == null ? null : (Integer)var1.get(var0);
      return var2 == null ? -1 : var2;
   }

   private FarColours() {
   }
}
