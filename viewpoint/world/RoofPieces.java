package viewpoint.world;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class RoofPieces {
   private static final Map<String, Integer> ENDS = new HashMap<>();
   private static final Map<String, RoofPiece> TABLE = read();
   private static final Set<String> SHEETS = sheets();

   public static RoofPiece of(String var0) {
      return var0 == null ? null : TABLE.get(var0);
   }

   public static int gableEnd(String var0) {
      return var0 == null ? -1 : ENDS.getOrDefault(var0, -1);
   }

   public static boolean knowsSheet(String var0) {
      return var0 != null && SHEETS.contains(sheet(var0));
   }

   private static String sheet(String var0) {
      int var1 = var0.lastIndexOf(95);
      return var1 < 0 ? var0 : var0.substring(0, var1);
   }

   private static Set<String> sheets() {
      HashSet var0 = new HashSet();

      for (String var2 : TABLE.keySet()) {
         var0.add(sheet(var2));
      }

      for (String var4 : ENDS.keySet()) {
         var0.add(sheet(var4));
      }

      return var0;
   }

   private static Map<String, RoofPiece> read() {
      HashMap var0 = new HashMap(2048);

      try (InputStream var1 = RoofPieces.class.getResourceAsStream("/viewpoint/roof-pieces.txt")) {
         if (var1 == null) {
            throw new IllegalStateException("roof-pieces.txt missing from the jar");
         }

         BufferedReader var2 = new BufferedReader(new InputStreamReader(var1, StandardCharsets.UTF_8));

         for (String var3 = var2.readLine(); var3 != null; var3 = var2.readLine()) {
            if (!var3.isBlank() && !var3.startsWith("#")) {
               String[] var4 = var3.trim().split(" ");
               if (var4[1].equals("end")) {
                  ENDS.put(var4[0], RoofPiece.facing(var4[2].charAt(0)));
               } else {
                  var0.put(var4[0], RoofPiece.parse(var4, 1));
               }
            }
         }

         return var0;
      } catch (IOException var7) {
         throw new UncheckedIOException(var7);
      }
   }

   private RoofPieces() {
   }
}
