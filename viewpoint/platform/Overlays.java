package viewpoint.platform;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

public final class Overlays {
   private static final List<Overlays.Overlay> all = new CopyOnWriteArrayList<>();

   public static void add(BooleanSupplier var0, Runnable var1) {
      all.add(new Overlays.Overlay(var0, var1));
   }

   static boolean any() {
      for (Overlays.Overlay var1 : all) {
         if (var1.on().getAsBoolean()) {
            return true;
         }
      }

      return false;
   }

   static void draw() {
      for (Overlays.Overlay var1 : all) {
         if (var1.on().getAsBoolean()) {
            var1.draw().run();
         }
      }
   }

   private Overlays() {
   }

   private record Overlay(BooleanSupplier on, Runnable draw) {
   }
}
