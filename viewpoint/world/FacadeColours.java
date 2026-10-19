package viewpoint.world;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public final class FacadeColours {
   private static HashMap<String, Integer> table;

   public static int of(String var0) {
      if (table == null) {
         load();
      }

      Integer var1 = table.get(var0);
      return var1 == null ? -1 : var1;
   }

   private static void load() {
      table = new HashMap<>();

      try {
         label67: {
            try (InputStream var0 = FacadeColours.class.getResourceAsStream("/viewpoint/facade-colours.txt")) {
               if (var0 != null) {
                  BufferedReader var1 = new BufferedReader(new InputStreamReader(var0, StandardCharsets.UTF_8));

                  String var2;
                  while ((var2 = var1.readLine()) != null) {
                     String[] var3 = var2.split(" ");
                     if (var3.length == 4) {
                        table.put(var3[0], Integer.parseInt(var3[1]) << 16 | Integer.parseInt(var3[2]) << 8 | Integer.parseInt(var3[3]));
                     }
                  }
                  break label67;
               }

               System.out.println("[Viewpoint] facade-colours.txt missing from the jar: hidden wall faces get a neutral grey");
            }

            return;
         }
      } catch (Exception var6) {
         System.out.println("[Viewpoint] facade-colours.txt unreadable: " + var6);
      }
   }

   private FacadeColours() {
   }
}
