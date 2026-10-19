package viewpoint;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import zombie.Lua.LuaManager;

final class ModLua {
   private static final String LOADED = "ViewpointLoot";

   static void load() {
      if (LuaManager.env.rawget("ViewpointLoot") == null) {
         File var0;
         try {
            var0 = folder();
         } catch (URISyntaxException var6) {
            System.out.println("[Viewpoint] Lua: the game loaded none of the mod's, and its jar has no path: " + var6);
            return;
         }

         File[] var1 = var0.listFiles((var0x, var1x) -> var1x.toLowerCase(Locale.ENGLISH).endsWith(".lua"));
         if (var1 != null && var1.length != 0) {
            Arrays.sort(var1, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

            for (File var5 : var1) {
               LuaManager.RunLua(var5.getAbsolutePath());
            }

            System.out
               .println(
                  "[Viewpoint] Lua: the game loaded none of the mod's (a server that does not list Viewpoint); ran its " + var1.length + " files from " + var0
               );
         } else {
            System.out.println("[Viewpoint] Lua: the game loaded none of the mod's, and " + var0 + " has none");
         }
      }
   }

   private static File folder() throws URISyntaxException {
      File var0 = new File(ModLua.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      File var1 = var0.getParentFile().getParentFile().getParentFile();
      return new File(var1, "lua" + File.separator + "client");
   }

   private ModLua() {
   }
}
