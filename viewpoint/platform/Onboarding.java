package viewpoint.platform;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import zombie.core.Core;
import zombie.core.textures.TextureID;

public final class Onboarding {
   private static final LiveSettings.Toggle FINISHED = LiveSettings.toggle("onboarding.finished", "Setup finished", "Debug/Views", false);
   private static final LiveSettings.Number EDITION = LiveSettings.number(
      "onboarding.edition", "Setup's edition finished", "Debug/Views", 0.0F, 1000.0F, 1.0F, 0.0F
   );
   private static final SettingsWindow.Button SHOW = SettingsWindow.button(
      "Debug/Setup",
      "Show the setup",
      "The setup players go through before first person, from its first step. Its Ok saves it as finished and turns first person on."
   );
   private static final SettingsWindow.Button CRASH = SettingsWindow.button(
      "Debug/Setup",
      "Show the crash window",
      "The window a player sees when Viewpoint turns itself off after an error, with a test error in its report. First person stays on."
   );
   private static final LiveSettings.Toggle HOLD_FOR_COMPRESSION = LiveSettings.toggle(
      "debug.holdForCompression", "Hold first person for Texture compression", "Debug/Setup", false
   );
   private static volatile boolean shown;
   private static final ConcurrentLinkedQueue<String[]> edits = new ConcurrentLinkedQueue<>();
   private static final String[] BARRIER = new String[0];
   private static volatile boolean finishing;
   private static final AtomicBoolean restart = new AtomicBoolean();
   private static volatile Hardware hardware;
   private static volatile PresetAdvice.Advice advice;
   private static volatile boolean compressionNeeded;
   private static volatile boolean checkOnLoad;
   private static volatile String compressionError;
   private static volatile boolean collectorNeeded;
   private static volatile boolean collectorOnLoad;
   private static volatile String collectorError;
   private static volatile boolean restartAsked;
   private static volatile boolean restarting;
   private static volatile int isoChunksMiB;
   private static volatile int cardTotalMiB = -1;
   private static boolean enter;
   private static boolean unavailableSaid;
   private static boolean compressionChecked;
   private static boolean collectorChecked;

   public static boolean shown() {
      return shown;
   }

   public static boolean finished() {
      return FINISHED.get() && EDITION.getInt() >= Build.SETUP_EDITION;
   }

   public static boolean blocks() {
      if (finished() && !restartNeeded()) {
         return false;
      }

      if (!ImGuiFrame.available()) {
         if (!unavailableSaid) {
            unavailableSaid = true;
            System.out
               .println(
                  "[Viewpoint] setup cannot show (the game's ImGui is on, or the window failed): first person goes on without it"
                     + (compressionNeeded ? ", Texture compression off" : "")
                     + (collectorNeeded ? ", ZGC kept" : "")
               );
         }

         return false;
      } else {
         return true;
      }
   }

   public static boolean mayPrepare() {
      boolean var0 = TextureID.useCompression || compressionChecked && !checkOnLoad || !HOLD_FOR_COMPRESSION.get() && cardTotalMiB >= 11776;
      boolean var1 = Collector.done() && Collector.offered() == null || collectorChecked && !collectorOnLoad;
      return finished() && var0 && var1 && !restartNeeded();
   }

   public static void playStarted(int var0) {
      isoChunks(var0);
      NewDefaults.take();
      if (!TextureID.useCompression && !compressionChecked) {
         compressionChecked = true;
         checkOnLoad = true;
      }

      if (!collectorChecked) {
         collectorChecked = true;
         collectorOnLoad = true;
      }

      if (!finished()) {
         if (FINISHED.get()) {
            System.out.println("[Viewpoint] setup shown again: this release's edition is " + Build.SETUP_EDITION + ", the one finished " + EDITION.getInt());
         }

         show();
      }
   }

   public static void isoChunks(int var0) {
      isoChunksMiB = var0;
   }

   public static void show() {
      if (!shown) {
         OnboardingArt.preload();
         shown = true;
         System.out.println("[Viewpoint] setup shown" + (finished() ? "" : ": first person waits until it is finished"));
      }
   }

   static boolean update() {
      boolean var0 = false;
      SHOW.status(finished() ? "finished" : "not finished");
      if (SHOW.take()) {
         restart.set(true);
         show();
      }

      if (CRASH.take()) {
         CrashReport.report("testing the crash window", new IllegalStateException("a test from Debug, Setup"));
      }

      for (String[] var1 = edits.poll(); var1 != null && var1 != BARRIER; var1 = edits.poll()) {
         LiveSettings.put(var1[0], var1[1]);
         var0 = true;
      }

      if (finishing) {
         finishing = false;
         FINISHED.set(true);
         EDITION.set(Build.SETUP_EDITION);
         shown = false;
         enter = true;
         var0 = true;
         System.out.println("[Viewpoint] setup finished: first person turns on");
      }

      if (restartAsked) {
         restartAsked = false;
         makeChangesAndQuit();
      }

      return var0;
   }

   public static boolean takeEnter() {
      boolean var0 = enter;
      enter = false;
      return var0;
   }

   private static void makeChangesAndQuit() {
      if (collectorNeeded && collectorError == null) {
         try {
            Collector.apply();
         } catch (IOException | RuntimeException var2) {
            collectorError = "The game's start settings could not be changed (" + var2 + "). The game keeps ZGC.";
            System.out.println("[Viewpoint] setup: " + collectorError);
            restarting = false;
            return;
         }
      }

      if (compressionNeeded) {
         try {
            Core.getInstance().setOptionTextureCompression(true);
            Core.getInstance().saveOptions();
            System.out.println("[Viewpoint] setup: the game's Texture compression turned on");
         } catch (IOException | RuntimeException var1) {
            compressionError = "The game could not save its options ("
               + var1
               + "). Turn on Texture compression in the game options (Display). Then quit the game and start it again.";
            System.out.println("[Viewpoint] setup: " + compressionError);
            return;
         }
      }

      System.out.println("[Viewpoint] setup: the game saves and quits, to start with the changes");
      Core.getInstance().quitToDesktop();
   }

   static void put(String var0, String var1) {
      edits.add(new String[]{var0, var1});
   }

   static void putAll(Map<String, String> var0) {
      var0.forEach(Onboarding::put);
   }

   static void barrier() {
      edits.add(BARRIER);
   }

   public static void renderFrame() {
      if (cardTotalMiB < 0 && GraphicsMemory.done()) {
         cardTotalMiB = Hardware.cardTotalMiB();
      }

      if (collectorOnLoad && Collector.done()) {
         collectorOnLoad = false;
         if (Collector.offered() != null) {
            collectorNeeded = true;
            System.out.println("[Viewpoint] setup: first person waits for the memory manager's switch");
            OnboardingArt.preload();
            shown = true;
         }
      }

      if (checkOnLoad && GraphicsMemory.done()) {
         checkOnLoad = false;
         check(Core.getInstance().getScreenWidth(), Core.getInstance().getScreenHeight());
         boolean var0 = PresetAdvice.compressionNeeded(hardware);
         if (var0 || HOLD_FOR_COMPRESSION.get()) {
            compressionNeeded = true;
            System.out.println("[Viewpoint] setup: first person waits for the game's Texture compression" + (var0 ? "" : " (held by the debug setting)"));
            OnboardingArt.preload();
            shown = true;
         }
      }
   }

   static PresetAdvice.Advice check(int var0, int var1) {
      Hardware var2 = Hardware.probe(var0, var1, isoChunksMiB);
      PresetAdvice.Advice var3 = PresetAdvice.of(var2);
      hardware = var2;
      advice = var3;
      System.out
         .println(
            "[Viewpoint] setup's hardware check: "
               + var2.describe()
               + "; advised "
               + GraphicsPresets.NAMES[var3.preset()]
               + ", far world workers "
               + var3.farWorkers()
         );
      return var3;
   }

   static Hardware hardware() {
      return hardware;
   }

   static PresetAdvice.Advice advice() {
      return advice;
   }

   static boolean compressionNeeded() {
      return compressionNeeded;
   }

   static String compressionError() {
      return compressionError;
   }

   static boolean collectorNeeded() {
      return collectorNeeded;
   }

   static String collectorError() {
      return collectorError;
   }

   static boolean restartNeeded() {
      return compressionNeeded || collectorNeeded;
   }

   static void restart() {
      restarting = true;
      restartAsked = true;
   }

   static boolean restarting() {
      return restarting;
   }

   static void skipCollector() {
      collectorNeeded = false;
      System.out.println("[Viewpoint] setup: first person goes on with ZGC");
   }

   static boolean fromStart() {
      return restart.getAndSet(false);
   }

   static void close() {
      shown = false;
   }

   static void finish() {
      finishing = true;
   }

   static LoadModel.Values values() {
      HashMap var0 = new HashMap();

      for (LiveSettings.Setting var2 : LiveSettings.all()) {
         var0.put(var2.key, var2.text());
      }

      return var1 -> {
         String var2x = var0.containsKey(var1) ? (String)var0.get(var1) : LiveSettings.value(var1);
         String[] var3 = var2x == null ? GraphicsPresets.TABLE.get(var1) : null;
         return var3 != null ? var3[2] : var2x;
      };
   }

   private Onboarding() {
   }

   static {
      FINISHED.shownWhen(() -> false);
      EDITION.shownWhen(() -> false);
      HOLD_FOR_COMPRESSION.describe(
         "At the next load of a save with the game's Texture compression off, the setup holds first person for it as on a card too small, whatever this card has. Its button turns compression on and quits the game."
      );
   }
}
