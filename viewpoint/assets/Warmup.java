package viewpoint.assets;

import java.util.List;
import viewpoint.far.Relief;
import viewpoint.platform.BuildPin;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Onboarding;
import viewpoint.platform.Overlays;
import viewpoint.render.FarRelief;
import viewpoint.render.WorldRenderer;
import zombie.characters.IsoGameCharacter;
import zombie.iso.IsoCell;

public final class Warmup {
   public static final String SECTION = "Game/Loading ahead";
   static final LiveSettings.Toggle ON = LiveSettings.toggle("assets.prepare", "First person prepared as a save loads", "Game/Loading ahead", true);
   private static final long LOADING_NANOS = 12000000L;
   private static final long PLAYING_NANOS = 3000000L;
   private static volatile List<ModelShaders.Wanted> shaders = List.of();
   private static volatile boolean loading;
   private static volatile boolean rendererWanted;
   private static boolean saveLoaded;
   private static List<ModelShaders.Wanted> making = List.of();
   private static int next;

   public static void gameLoaded() {
      if (BuildPin.supported()) {
         loading = true;
         saveLoaded = true;
         if (ON.get()) {
            WorldRenderer.bakeAhead();
         }

         ModelPrefetch.clear();
         shaders = ModelShaders.ON.get() ? ModelShaders.named() : List.of();
         SoundFiles.gameLoaded();
         WarmupBar.started();
      }
   }

   public static void playStarted() {
      loading = false;
   }

   public static void update() {
      rendererWanted = saveLoaded && ON.get() && BuildPin.supported() && Onboarding.mayPrepare();
      if (rendererWanted) {
         Relief.prepare();
      }
   }

   public static void frame(IsoCell var0, IsoGameCharacter var1) {
      if (var0 != null && var1 != null && BuildPin.supported()) {
         ModelPrefetch.frame(var0, var1);
      }
   }

   public static boolean renderDue() {
      return next < shaders.size() || making != shaders || rendererWanted && (WorldRenderer.preparedShare() < 1.0F || FarRelief.uploadDue());
   }

   public static void renderStep() {
      long var0 = System.nanoTime() + (loading ? 12000000L : 3000000L);
      if (making != shaders) {
         making = shaders;
         next = 0;
      }

      while (next < making.size() && System.nanoTime() < var0) {
         ModelShaders.Wanted var2 = making.get(next++);
         if (!ModelShaders.has(var2)) {
            ModelShaders.make(var2);
         }
      }

      if (next >= making.size() && rendererWanted && System.nanoTime() < var0 && WorldRenderer.prepare(var0)) {
         FarRelief.prepare();
         if (loading) {
            WorldRenderer.prepareMinecraftPack();
         }
      }
   }

   static boolean loading() {
      return loading;
   }

   static boolean rendererWanted() {
      return rendererWanted;
   }

   static int shadersMade() {
      return Math.min(next, making.size());
   }

   static int shadersListed() {
      return shaders.size();
   }

   private Warmup() {
   }

   static {
      ON.describe(
         "Makes the renderer, its shaders and the game's model shaders while the game loads a save, so the first switch to first or third person is quick. A bar at the bottom left shows the progress."
      );
      Overlays.add(WarmupBar::showing, WarmupBar::draw);
   }
}
