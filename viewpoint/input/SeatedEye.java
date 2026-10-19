package viewpoint.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.SettingsWindow;
import zombie.ZomboidFileSystem;

public final class SeatedEye {
   private static final String PLACE = "Debug/Seated eye";
   private static final String FILE = "viewpoint-seated-eye.properties";
   private static final LiveSettings.Number HEIGHT = LiveSettings.number("debug.seatedEye", "Eye up or down", "Debug/Seated eye", -1.0F, 1.0F, 0.01F, 0.0F);
   private static final SettingsWindow.Button SAVE = SettingsWindow.button(
      "Debug/Seated eye", "Save for this vehicle", "Keeps the height above for the kind of vehicle you sit in: you sit at it whenever you get into one again."
   );
   private static Properties saved;
   private static String vehicle;
   private static long seatedFrame = Long.MIN_VALUE;

   public static float offset(String var0, long var1) {
      if (!var0.equals(vehicle) || var1 != seatedFrame + 1L) {
         vehicle = var0;
         float var3 = savedHeight(var0);
         HEIGHT.set(Float.isNaN(var3) ? 0.0F : var3);
         SAVE.take();
         SAVE.status(var0 + (Float.isNaN(var3) ? ": none saved" : ": saved " + var3));
      }

      seatedFrame = var1;
      if (SAVE.take()) {
         String var4 = Float.toString(HEIGHT.get());
         saved().setProperty(var0, var4);
         write();
         SAVE.status(var0 + ": saved " + var4);
      }

      return HEIGHT.get();
   }

   private static float savedHeight(String var0) {
      String var1 = saved().getProperty(var0);

      try {
         return var1 == null ? Float.NaN : Float.parseFloat(var1.trim());
      } catch (NumberFormatException var3) {
         return Float.NaN;
      }
   }

   private static Properties saved() {
      if (saved == null) {
         saved = new Properties();
         File var0 = file();
         if (var0.isFile()) {
            try (FileInputStream var1 = new FileInputStream(var0)) {
               saved.load(var1);
            } catch (Exception var6) {
               System.out.println("[Viewpoint] could not read " + var0 + ": " + var6);
            }
         }
      }

      return saved;
   }

   private static void write() {
      try (FileOutputStream var0 = new FileOutputStream(file())) {
         saved.store(var0, "Viewpoint's seated eye: scene units up from the head, by vehicle script (Debug, Seated eye)");
      } catch (Exception var5) {
         System.out.println("[Viewpoint] could not write " + file() + ": " + var5);
      }
   }

   private static File file() {
      return new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-seated-eye.properties");
   }

   private SeatedEye() {
   }

   static {
      HEIGHT.describe(
         "Seated in first person, how far the view is moved up (or down) from your head. Sitting in a vehicle sets it to the height saved for that kind of vehicle, or to 0."
      );
   }
}
