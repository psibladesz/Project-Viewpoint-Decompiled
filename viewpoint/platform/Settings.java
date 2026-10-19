package viewpoint.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import zombie.ZomboidFileSystem;

public final class Settings {
   public static final String FILE = "viewpoint.properties";
   public static final String DEV = "dev";
   public static final String SHADER_DIR = "shaderDir";
   public static final String ROOMS = "rooms";
   private static final String OLD_FILE = "pzfp.properties";
   private static final String OLD_WIDTH = "chunkGridWidth";
   private static final String OLD_FAR = "farRadius";
   public static volatile boolean dev;
   public static volatile boolean rooms = true;
   public static volatile String shaderDir;

   public static synchronized void load() {
      File var0 = new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint.properties");
      File var1 = new File(ZomboidFileSystem.instance.getCacheDir(), "pzfp.properties");
      if (!var0.exists() && var1.isFile()) {
         carryOver(var1, var0);
      }

      Properties var2 = new Properties();
      if (var0.isFile()) {
         try (FileInputStream var3 = new FileInputStream(var0)) {
            var2.load(var3);
         } catch (Exception var12) {
            System.out.println("[Viewpoint] could not read " + var0 + ": " + var12);
         }
      }

      if (!var0.exists()) {
         try (FileOutputStream var13 = new FileOutputStream(var0)) {
            var2.store(
               var13,
               "Viewpoint. dev=true: developer mode; shaderDir: where dev mode reads the shaders from (see Settings.java). The memory pools' caps are in the settings window (Delete)."
            );
         } catch (Exception var9) {
            System.out.println("[Viewpoint] could not write " + var0 + ": " + var9);
         }
      }

      if (var2.getProperty("chunkGridWidth") != null) {
         System.out
            .println(
               "[Viewpoint] chunkGridWidth in viewpoint.properties is no longer read: the game keeps its own chunk grid, and the far world covers the distance"
            );
      }

      if (var2.getProperty("farRadius") != null) {
         System.out
            .println(
               "[Viewpoint] farRadius in viewpoint.properties is no longer read: the far world's reach is in the settings window (World, Levels of detail)"
            );
      }

      for (Pool var6 : Pool.ALL) {
         var6.carryOver(var2.getProperty(var6.setting));
      }

      dev = Boolean.parseBoolean(System.getProperty("viewpoint.dev", var2.getProperty("dev", "false")).trim());
      rooms = Boolean.parseBoolean(var2.getProperty("rooms", "true").trim());
      String var15 = System.getProperty("viewpoint.shaderDir", var2.getProperty("shaderDir"));
      shaderDir = var15 != null && !var15.isBlank() ? var15.trim() : null;
      if (dev) {
         System.out.println("[Viewpoint] dev mode on" + (shaderDir == null ? "" : ", shaders from " + shaderDir));
      }
   }

   private static void carryOver(File var0, File var1) {
      try {
         Files.copy(var0.toPath(), var1.toPath());
         System.out.println("[Viewpoint] settings carried over from " + var0);
      } catch (IOException var3) {
         System.out.println("[Viewpoint] could not copy " + var0 + ": " + var3);
      }
   }

   private Settings() {
   }
}
