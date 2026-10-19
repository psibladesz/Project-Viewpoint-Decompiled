package viewpoint.platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public final class Build {
   private static final String FILE = "/viewpoint/build.properties";
   private static final Properties PROPERTIES = read();
   public static final boolean RELEASE = Boolean.parseBoolean(PROPERTIES.getProperty("release"));
   public static final String VERSION = PROPERTIES.getProperty("version", "dev").strip();
   public static final String DATE = PROPERTIES.getProperty("date", "").strip();
   public static final int SETUP_EDITION = Integer.parseInt(PROPERTIES.getProperty("setupEdition").strip());

   private static Properties read() {
      Properties var0 = new Properties();

      try (InputStream var1 = Build.class.getResourceAsStream("/viewpoint/build.properties")) {
         if (var1 == null) {
            throw new IllegalStateException("no /viewpoint/build.properties in the mod's JAR");
         }

         var0.load(var1);
         return var0;
      } catch (IOException var6) {
         throw new UncheckedIOException(var6);
      }
   }

   private Build() {
   }
}
