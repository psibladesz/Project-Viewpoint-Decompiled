package viewpoint.light;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.SettingsWindow;
import zombie.ZomboidFileSystem;

public final class LampPlaces {
   private static final String PLACE = "Debug/Lamp placement";
   private static final String FILE = "viewpoint-lamp-places.properties";
   static final int MOVES = 6;
   private static final float SPAN = 1.0F;
   private static final float STEP = 0.01F;
   private static final LiveSettings.Number[] SLIDERS = new LiveSettings.Number[]{
      slider("debug.lampHeadApart", "Head lamps apart (squares)"),
      slider("debug.lampHeadUp", "Head lamps up (squares)"),
      slider("debug.lampHeadForward", "Head lamps forward (squares)"),
      slider("debug.lampTailApart", "Tail lights apart (squares)"),
      slider("debug.lampTailUp", "Tail lights up (squares)"),
      slider("debug.lampTailBack", "Tail lights back (squares)")
   };
   private static final SettingsWindow.Button SAVE = SettingsWindow.button(
      "Debug/Lamp placement",
      "Save for this vehicle",
      "Keeps the moves above for the kind of vehicle you are in, or last got out of: every vehicle of that kind has its lamps there."
   );
   private static Map<String, float[]> saved;
   private static String edited;
   private static boolean glass;

   static void frame(String var0, boolean var1) {
      if (var0 != null && (!var0.equals(edited) || var1 != glass)) {
         boolean var2 = !var0.equals(edited);
         edited = var0;
         glass = var1;
         float[] var3 = saved().get(var0);

         for (int var4 = 0; var2 && var4 < 6; var4++) {
            SLIDERS[var4].set(var3 == null ? 0.0F : var3[var4]);
         }

         if (var2) {
            SAVE.take();
         }

         SAVE.status(var0 + (var1 ? ": lamps on its model's glass; these move only a lamp it has none for" : (var3 == null ? ": none saved" : ": saved")));
      }

      if (SAVE.take() && edited != null) {
         float[] var5 = new float[6];

         for (int var6 = 0; var6 < 6; var6++) {
            var5[var6] = SLIDERS[var6].get();
         }

         saved().put(edited, var5);
         write();
         SAVE.status(edited + ": saved");
      }
   }

   static void moves(String var0, boolean var1, float[] var2) {
      int var3 = var1 ? 3 : 0;
      boolean var4 = var0.equals(edited);
      float[] var5 = var4 ? null : saved().get(var0);

      for (int var6 = 0; var6 < 3; var6++) {
         var2[var6] = var4 ? SLIDERS[var3 + var6].get() : (var5 == null ? 0.0F : var5[var3 + var6]);
      }
   }

   static float[] parse(String var0) {
      String[] var1 = var0.split(",");
      if (var1.length != 6) {
         return null;
      }

      float[] var2 = new float[6];

      try {
         for (int var3 = 0; var3 < 6; var3++) {
            var2[var3] = Float.parseFloat(var1[var3].trim());
         }

         return var2;
      } catch (NumberFormatException var4) {
         return null;
      }
   }

   static String text(float[] var0) {
      StringBuilder var1 = new StringBuilder();

      for (int var2 = 0; var2 < 6; var2++) {
         var1.append(var2 == 0 ? "" : ",").append(var0[var2]);
      }

      return var1.toString();
   }

   private static LiveSettings.Number slider(String var0, String var1) {
      return LiveSettings.number(var0, var1, "Debug/Lamp placement", -1.0F, 1.0F, 0.01F, 0.0F);
   }

   private static Map<String, float[]> saved() {
      if (saved == null) {
         saved = new HashMap<>();
         Properties var0 = new Properties();
         if (file().isFile()) {
            try (FileInputStream var1 = new FileInputStream(file())) {
               var0.load(var1);
            } catch (Exception var6) {
               System.out.println("[Viewpoint] could not read " + file() + ": " + var6);
            }
         }

         for (String var2 : var0.stringPropertyNames()) {
            float[] var3 = parse(var0.getProperty(var2));
            if (var3 == null) {
               System.out.println("[Viewpoint] viewpoint-lamp-places.properties: " + var2 + " is not 6 numbers");
            } else {
               saved.put(var2, var3);
            }
         }
      }

      return saved;
   }

   private static void write() {
      Properties var0 = new Properties();
      saved.forEach((var1x, var2) -> var0.setProperty(var1x, text(var2)));

      try (FileOutputStream var1 = new FileOutputStream(file())) {
         var0.store(
            var1,
            "Viewpoint's vehicle lamps moved from the body's box, by vehicle script (Debug, Lamp placement), in squares: head lamps apart, up, forward; tail lights apart, up, back"
         );
      } catch (Exception var6) {
         System.out.println("[Viewpoint] could not write " + file() + ": " + var6);
      }
   }

   private static File file() {
      return new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-lamp-places.properties");
   }

   private LampPlaces() {
   }

   static {
      SLIDERS[0].describe("Moves the head lamps out to the sides, or below 0 in towards the middle.");
      SLIDERS[1].describe("Moves the head lamps up, or below 0 down.");
      SLIDERS[2].describe("Moves the head lamps forward, out of the front, or below 0 back into it.");
      SLIDERS[3].describe("Moves the tail and brake lights out to the sides, or below 0 in towards the middle.");
      SLIDERS[4].describe("Moves the tail and brake lights up, or below 0 down.");
      SLIDERS[5].describe("Moves the tail and brake lights back, out of the rear, or below 0 forward into it.");
      SAVE.status("Get into a vehicle: these move the lamps of its kind (Debug, Views, Mark the vehicles' lamps shows them).");
   }
}
