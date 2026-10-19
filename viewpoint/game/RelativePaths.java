package viewpoint.game;

import java.io.File;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Locale;
import viewpoint.platform.BuildPin;
import zombie.ZomboidFileSystem;

public final class RelativePaths {
   private static volatile Boolean underBase;

   public static String relative(ZomboidFileSystem var0, String var1) {
      String var2 = relative(var1);
      return var2 != null && BuildPin.supported() && underBase(var0) ? var2 : null;
   }

   static String relative(String var0) {
      if (!fileName(var0)) {
         return null;
      }

      ArrayDeque var1 = new ArrayDeque();

      for (String var5 : var0.toLowerCase(Locale.ENGLISH).replace('\\', '/').split("/")) {
         if (var5.equals("..")) {
            if (var1.isEmpty()) {
               return null;
            }

            var1.removeLast();
         } else if (!var5.isEmpty() && !var5.equals(".")) {
            var1.addLast(var5);
         }
      }

      return String.join("/", var1);
   }

   static boolean fileName(String var0) {
      if (var0 != null && !var0.isEmpty() && var0.indexOf(58) < 0) {
         char var1 = var0.charAt(0);
         int var2 = Math.max(var0.lastIndexOf(47), var0.lastIndexOf(92));
         String var3 = var0.substring(var2 + 1);
         return var1 != '/' && var1 != '\\' && var3.indexOf(46) >= 0 && !var3.chars().allMatch(var0x -> var0x == 46);
      } else {
         return false;
      }
   }

   private static boolean underBase(ZomboidFileSystem var0) {
      Boolean var1 = underBase;
      if (var1 != null) {
         return var1;
      }

      URI var2 = var0.base.lowercaseUri;
      if (var2 == null) {
         return false;
      }

      URI var3 = new File(new File("").getAbsolutePath().toLowerCase(Locale.ENGLISH)).toURI();
      var1 = var2.equals(var3);
      underBase = var1;
      if (!var1) {
         System.out
            .println("[Viewpoint] the game runs from " + var3 + ", not its own folder " + var2 + ": its file names are looked up on disk, as the game does");
      }

      return var1;
   }

   private RelativePaths() {
   }
}
