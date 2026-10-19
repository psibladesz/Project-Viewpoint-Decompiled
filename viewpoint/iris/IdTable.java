package viewpoint.iris;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class IdTable {
   public static final IdTable EMPTY = new IdTable();
   private final Map<String, Integer> ids = new HashMap<>();

   private IdTable() {
   }

   public static IdTable parse(String var0, String var1) {
      IdTable var2 = new IdTable();
      if (var0 == null) {
         return var2;
      }

      String var3 = var1 + ".";

      for (String var7 : var0.split("\n")) {
         String var8 = Macros.stripComments(var7).strip();
         int var9 = var8.indexOf(61);
         if (var8.startsWith(var3) && var9 >= 0) {
            int var10;
            try {
               var10 = Integer.parseInt(var8.substring(var3.length(), var9).strip());
            } catch (NumberFormatException var15) {
               continue;
            }

            for (String var12 : ShadersProperties.words(var8.substring(var9 + 1))) {
               String var13 = plain(var12);
               var2.ids.putIfAbsent(var13, var10);
               int var14 = var13.indexOf(58);
               if (var14 > 0) {
                  var2.ids.putIfAbsent(var13.substring(0, var14), var10);
               }
            }
         }
      }

      return var2;
   }

   public int id(String... var1) {
      for (String var5 : var1) {
         String var6 = plain(var5);
         Integer var7 = this.ids.get(var6);
         if (var7 != null) {
            return var7;
         }
      }

      return -1;
   }

   public int size() {
      return this.ids.size();
   }

   private static String plain(String var0) {
      String var1 = var0.toLowerCase(Locale.ROOT);
      return var1.startsWith("minecraft:") ? var1.substring(10) : var1;
   }
}
