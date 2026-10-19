package viewpoint.game;

import java.util.IdentityHashMap;
import java.util.Iterator;
import viewpoint.core.View;
import viewpoint.light.Visibility;
import viewpoint.platform.Caches;
import viewpoint.platform.LiveSettings;
import zombie.MainThread;
import zombie.iso.objects.IsoLightSwitch;

public final class LightCheck {
   public static final LiveSettings.Toggle KEPT = LiveSettings.toggle(
      "game.switchPowerKept", "Light switches' power asked 4 times a second", "Debug/Fixes", true
   );
   public static final LiveSettings.Toggle THROTTLED = LiveSettings.toggle(
      "debug.lightCheckThrottled", "The game's light check 10 times a second", "Debug/Profiling", false
   );
   private static final long KEEP_NANOS = 250000000L;
   private static final long THROTTLE_NANOS = 100000000L;
   private static final long KEEP_FRAMES = 240L;
   private static final long ENTRY_BYTES = 64L;
   private static final int NONE = -1;
   private static volatile long lastCheck;
   private static final IdentityHashMap<IsoLightSwitch, LightCheck.Kept> kept = new IdentityHashMap<>();
   private static int answer = -1;
   private static IsoLightSwitch asking;
   private static long nextSweep;

   public static boolean skipCheck() {
      long var0 = System.nanoTime();
      if (THROTTLED.get() && View.enabled && var0 - lastCheck < 100000000L) {
         return true;
      }

      lastCheck = var0;
      return false;
   }

   public static boolean skipPower(IsoLightSwitch var0) {
      if (Thread.currentThread() != MainThread.mainThread) {
         return false;
      }

      answer = -1;
      asking = null;
      if (KEPT.get() && View.enabled && Visibility.lightingUpdate) {
         sweepWhenDue();
         LightCheck.Kept var1 = kept.get(var0);
         long var2 = System.nanoTime();
         if (var1 != null && var2 - var1.asked < 250000000L) {
            var1.used = Caches.tick();
            answer = var1.powered ? 1 : 0;
            return true;
         } else {
            asking = var0;
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean power(IsoLightSwitch var0, boolean var1) {
      if (Thread.currentThread() != MainThread.mainThread) {
         return var1;
      }

      if (answer != -1) {
         boolean var2 = answer == 1;
         answer = -1;
         return var2;
      }

      if (asking == var0) {
         asking = null;
         keep(var0, var1);
      }

      return var1;
   }

   public static void clear() {
      kept.clear();
      answer = -1;
      asking = null;
   }

   private static void keep(IsoLightSwitch var0, boolean var1) {
      long var2 = System.nanoTime();
      LightCheck.Kept var4 = kept.get(var0);
      if (var4 == null) {
         var4 = new LightCheck.Kept();
         var4.asked = var2 - (System.identityHashCode(var0) & 65535) * 3814L;
         kept.put(var0, var4);
      } else {
         var4.asked = var2;
      }

      var4.powered = var1;
      var4.used = Caches.tick();
   }

   private static void sweepWhenDue() {
      if (Caches.tick() >= nextSweep) {
         nextSweep = Caches.tick() + 240L;
         sweep(Caches.tick() - 240L);
      }
   }

   private static void sweep(long var0) {
      Iterator var2 = kept.values().iterator();

      while (var2.hasNext()) {
         if (((LightCheck.Kept)var2.next()).used < var0) {
            var2.remove();
         }
      }
   }

   private LightCheck() {
   }

   static {
      KEPT.describe(
         "In first person, the game's light check asks each lamp's switch whether power reaches it every frame; with the grid off that searches round every lamp. Kept for a quarter of a second, power turning on or off reaches the lamps that much later."
      );
      THROTTLED.describe(
         "Measuring only: in first person, the game's whole light check at most every 0.1 s. Switches flipped and shots' flashes reach the lamps (and the zombies' sight) up to that late."
      );
      Caches.register(new Caches.Cache() {
         @Override
         public long bytes() {
            return LightCheck.kept.size() * 64L;
         }

         @Override
         public void list(Caches.Survey var1) {
            for (LightCheck.Kept var3 : LightCheck.kept.values()) {
               var1.add(var3.used, 64L);
            }
         }

         @Override
         public void evictBefore(long var1) {
            LightCheck.sweep(var1);
         }
      });
   }

   private static final class Kept {
      boolean powered;
      long asked;
      long used;
   }
}
