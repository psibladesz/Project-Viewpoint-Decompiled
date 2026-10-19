package viewpoint;

import viewpoint.assets.Warmup;
import viewpoint.camera.ThirdPerson;
import viewpoint.core.Frame;
import viewpoint.core.SplitScreen;
import viewpoint.core.View;
import viewpoint.environment.Atmosphere;
import viewpoint.environment.Weather;
import viewpoint.far.BlockLight;
import viewpoint.far.CellLight;
import viewpoint.far.FarWorld;
import viewpoint.far.Relief;
import viewpoint.game.IsoChunkTextures;
import viewpoint.game.LightCheck;
import viewpoint.game.Teleport;
import viewpoint.game.WorldText;
import viewpoint.gunplay.Gunplay;
import viewpoint.input.AimAssist;
import viewpoint.input.Controls;
import viewpoint.input.CrosshairAim;
import viewpoint.input.Eye;
import viewpoint.input.FreeCam;
import viewpoint.input.FreeLook;
import viewpoint.input.Look;
import viewpoint.input.PadAim;
import viewpoint.input.PadLook;
import viewpoint.input.PadVanilla;
import viewpoint.input.SeatedView;
import viewpoint.interact.LootMenu;
import viewpoint.interact.LootPad;
import viewpoint.interact.MouseTargets;
import viewpoint.light.Daylight;
import viewpoint.light.Flashlight;
import viewpoint.light.FloorPlans;
import viewpoint.light.Headlights;
import viewpoint.light.LightRecorder;
import viewpoint.light.LightSources;
import viewpoint.light.Lights;
import viewpoint.light.Torches;
import viewpoint.models.Characters;
import viewpoint.models.Corpses;
import viewpoint.models.ModelCull;
import viewpoint.models.ModelMotion;
import viewpoint.models.Models;
import viewpoint.platform.BuildPin;
import viewpoint.platform.Caches;
import viewpoint.platform.CrashReport;
import viewpoint.platform.HotReload;
import viewpoint.platform.Keys;
import viewpoint.platform.Onboarding;
import viewpoint.platform.PadBinds;
import viewpoint.platform.PadInput;
import viewpoint.platform.Profile;
import viewpoint.platform.Settings;
import viewpoint.platform.SettingsWindow;
import viewpoint.platform.Threads;
import viewpoint.render.Latency;
import viewpoint.render.LodView;
import viewpoint.render.Panini;
import viewpoint.render.Retirement;
import viewpoint.render.WorldRenderer;
import viewpoint.visibility.Glass;
import viewpoint.visibility.Rooms;
import viewpoint.world.Blood;
import viewpoint.world.ChunkCache;
import viewpoint.world.Flames;
import viewpoint.world.MeshRecorder;
import zombie.GameTime;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.core.SpriteRenderer;
import zombie.iso.IsoCamera;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunkMap;
import zombie.ui.UIManager;

public final class FP {
   private static final String[] PARTS = new String[]{
      "relief", "chunk cache", "loot menu", "far world", "far light", "characters", "vehicles", "world models", "corpses", "lights", "world text", "caches"
   };
   private static final long[] partNanos = new long[PARTS.length];
   private static int partAt;
   private static long partStart;
   private static boolean cursorMode;
   private static boolean settingsShown;
   private static boolean paused;
   private static boolean cursorBeforePause;
   private static IsoCell lastCell;
   private static long enabledAt;
   private static long frameNumber;
   private static final SplitScreen split = new SplitScreen();
   private static boolean cycleAsked;
   private static final Frame[] frames = new Frame[]{new Frame(), new Frame(), new Frame()};
   private static final SceneDrawer[] drawers = new SceneDrawer[]{new SceneDrawer(frames[0]), new SceneDrawer(frames[1]), new SceneDrawer(frames[2])};
   private static int reloads;

   public static boolean renderWorld(IsoCell var0) {
      int var1 = IsoCamera.frameState.playerIndex;
      if (var1 != 0) {
         return false;
      }

      Threads.game();
      Retirement.building(++frameNumber);
      IsoGameCharacter var2 = IsoCamera.getCameraCharacter();
      devKeys();
      PadLook.update(var2);
      PadAim.update();
      LootPad.frame();
      toggle(var2);
      splitScreen(var2);
      startOverIfStale(var0);
      if (var2 != null) {
         LightRecorder.poll(var0, var1, var2.getX(), var2.getY(), var2.getZ());
         Teleport.poll(var0, var2);
      }

      Onboarding.isoChunks(IsoChunkTextures.update(View.enabled && var2 != null));
      Warmup.frame(var0, var2);
      if (View.enabled && var2 != null) {
         if (enabledAt != 0L && System.nanoTime() - enabledAt > 10000000000L) {
            enabledAt = 0L;
            Compat.reportFired();
         }

         viewInput(var2);
         int var3 = SpriteRenderer.instance.getMainStateIndex();
         Frame var4 = frames[var3];

         try {
            snapshot(var4, var0, var2, var1);
         } catch (Throwable var6) {
            View.enabled = false;
            Look.wantCapture = false;
            Characters.release(var4);
            Corpses.release(var4);
            System.out.println("[Viewpoint] disabled after error while building the scene:");
            var6.printStackTrace(System.out);
            CrashReport.report("building the scene", var6);
            return false;
         }

         SpriteRenderer.instance.drawGeneric(drawers[var3]);
         return true;
      } else {
         Look.wantCapture = false;
         MouseTargets.frame(false);
         CrosshairAim.off();
         Characters.forget();
         return false;
      }
   }

   private static void startOverIfStale(IsoCell var0) {
      if (var0 != lastCell) {
         lastCell = var0;
         resetCaches();
      }

      if (reloads != HotReload.generation) {
         reloads = HotReload.generation;
         resetCaches();
         FarWorld.clear();
         System.out.println("[Viewpoint] hot reload: the caches start over");
      }
   }

   private static void viewInput(IsoGameCharacter var0) {
      cursorMode();
      ThirdPerson.poll(var0);
      FreeLook.update(var0);
      FreeCam.keys();
      if (Keys.SCREENSHOT.pressed() || PadBinds.SCREENSHOT.pressed()) {
         Screenshot.requested = true;
      }

      Look.wantCapture = !cursorMode && !var0.isDead() && !UIManager.isModalVisible();
      MouseTargets.frame(!Look.captured);
   }

   private static void cursorMode() {
      if (Keys.CURSOR.pressed()) {
         cursorMode = !cursorMode;
      }

      if (LootMenu.freeCursor()) {
         cursorMode = true;
      }

      if (LootMenu.lookAgain()) {
         cursorMode = false;
      }

      if (SettingsWindow.shown() && !settingsShown) {
         cursorMode = true;
      }

      settingsShown = SettingsWindow.shown();
      boolean var0 = GameTime.isGamePaused();
      if (var0 && !paused) {
         cursorBeforePause = cursorMode;
         cursorMode = true;
      } else if (!var0 && paused) {
         cursorMode = cursorBeforePause;
      }

      paused = var0;
   }

   private static void devKeys() {
      if (Keys.LOD_VIEW.pressed()) {
         LodView.flip();
      }

      if (Keys.DEBUG_VIEW.pressed()) {
         WorldRenderer.debugView = (WorldRenderer.debugView + 1) % 14;
         System.out
            .println(
               "[Viewpoint] debug view "
                  + WorldRenderer.debugView
                  + " (0 normal, 1 art only, 2 light only, 3 volumetric light x4, 4 the occlusion, 5 lamp shadows, 6 the sun's maps, 7 the sky's share, 8 the sun's or moon's, 9 the lamps', 10 the fog's, 11 the floors' bounce x4, 12 the light a mirror reflects, 13 how sure the reflections' trace is)"
            );
      }

      if (Keys.ISOLATE.pressed()) {
         WorldRenderer.isolate = (WorldRenderer.isolate + 1) % WorldRenderer.ISOLATE_NAMES.length;
         System.out.println("[Viewpoint] isolate: " + WorldRenderer.ISOLATE_NAMES[WorldRenderer.isolate]);
      }

      if (Keys.ROOMS.pressed()) {
         Rooms.cycle();
      }
   }

   private static void toggle(IsoGameCharacter var0) {
      boolean var1 = cycleAsked;
      cycleAsked = false;
      if (var1 && View.enabled && !View.thirdPerson) {
         ThirdPerson.flip();
      } else {
         boolean var2 = Keys.FIRST_PERSON.pressed() || PadBinds.FIRST_PERSON.pressed() || var1;
         if (var2 && Onboarding.blocks()) {
            Onboarding.show();
         } else {
            var2 |= Onboarding.takeEnter() && !View.enabled;
            if (var2 && !BuildPin.supported()) {
               BuildPin.reportRefused();
            } else if (var2 && !View.enabled && SplitScreen.count(IsoPlayer.players) > 1) {
               System.out.println("[Viewpoint] split-screen: the view stays off while more than one player plays here");
            } else if (var2 && var0 != null && !SceneDrawer.failed) {
               if (var1 && !View.enabled) {
                  View.thirdPerson = false;
               }

               switchView(var0, !View.enabled);
            }
         }
      }
   }

   private static void splitScreen(IsoGameCharacter var0) {
      if (var0 != null) {
         boolean var1 = split.update(View.enabled, SplitScreen.count(IsoPlayer.players));
         if (var1 != View.enabled) {
            System.out.println("[Viewpoint] split-screen: the view " + (var1 ? "back on" : "off"));
            switchView(var0, var1);
         }
      }
   }

   private static void switchView(IsoGameCharacter var0, boolean var1) {
      View.enabled = var1;
      cursorMode = false;
      resetCaches();
      Look.yaw = var0.getDirectionAngleRadians();
      Look.pitch = 0.0F;
      if (View.enabled) {
         Compat.check();
         enabledAt = System.nanoTime();
      }
   }

   static String nextView() {
      return !View.enabled ? "first" : (View.thirdPerson ? "isometric" : "third");
   }

   static void cycleView() {
      cycleAsked = true;
   }

   private static void snapshot(Frame var0, IsoCell var1, IsoGameCharacter var2, int var3) {
      var0.number = frameNumber;
      var0.scene.reset();
      var0.modelObjects.clear();
      var0.modelItems.clear();
      Characters.release(var0);
      Corpses.release(var0);
      Models.release(var0);
      float var4 = (float)IsoCamera.getScreenWidth(var3) / IsoCamera.getScreenHeight(var3);
      var0.scene.projection.setPerspective(View.fovY(false), var4, 0.05F, 400.0F);
      var0.scene.panini = Panini.strength();
      FreeCam.Place var5 = FreeCam.centre();
      var0.onCamera = var5 != null;
      var0.camX = var0.onCamera ? (float)var5.x() : var2.getX();
      var0.camY = var0.onCamera ? (float)var5.y() : var2.getY();
      var0.camZ = var0.onCamera ? (float)var5.z() : var2.getZ();
      var0.viewYaw = var0.onCamera ? var5.yaw() : Look.yaw;
      var0.viewPitch = var0.onCamera ? var5.pitch() : Look.pitch;
      var0.scene.originX = -var0.camX;
      var0.scene.originY = var0.camZ * 2.4494896F;
      var0.scene.originZ = -var0.camY;
      Eye.place(var2, var0);
      SeatedView.level(var0);
      ThirdPerson.capture(var0, var1, var2, var4);
      CrosshairAim.snapshot(var0, var2, Relief.current());
      Gunplay.snapshot();
      View.crosshairSnapshot(UIManager.visibleAllUi);
      Glass.snapshot(var0);
      Controls.traceState(var2);
      Weather.snapshot(var0, var1);
      Atmosphere.apply(var0.scene);
      Daylight.prepare(var0.scene);
      Flashlight.snapshot(var0, var2);
      captureWorld(var0, var1, var2, var3);
   }

   private static void captureWorld(Frame var0, IsoCell var1, IsoGameCharacter var2, int var3) {
      long var4 = System.nanoTime();
      Profile.mainFrame(var4);
      partAt = 0;
      partStart = var4;
      ModelCull.reportCull(var1);
      Relief.frame(var0);
      part();
      ChunkCache.update(var0, var1, var3);
      MeshRecorder.poll(var1, var3, var0.camX, var0.camY, var0.camZ);
      part();
      LootMenu.snapshot(var0, var2);
      part();
      long var6 = partStart;
      FarWorld.update(var0);
      part();
      BlockLight.update(var0);
      CellLight.update(var0);
      part();
      long var8 = partStart;
      ModelMotion.begin(var0.scene.models);
      Characters.snapshot(var0, var1, var2);
      part();
      Characters.vehicles(var0, var1, var2);
      part();
      Characters.outlinesShown(var1, var3);
      Models.snapshot(var0, var3);
      part();
      Corpses.capture(var0);
      ModelMotion.end(var0.scene.models);
      part();
      Lights.snapshot(var0, var1);
      FloorPlans.snapshot(var0, var2);
      Torches.snapshot(var0, var2, var3);
      Headlights.snapshot(var0, var1, var2);
      Flames.snapshot(var0, var1);
      Blood.snapshot(var0, var1);
      Daylight.snapshot(var0.scene);
      part();
      WorldText.snapshot(var0, var1, var3);
      part();
      Caches.balance();
      part();
      var0.takenNanos = partStart;
      Latency.snapshotTaken(var0.number);
      Profile.cpu(var6 - var4, var8 - var6, var0.takenNanos - var8, var0.scene.meshCount);
      Profile.mainParts(PARTS, partNanos);
   }

   private static void part() {
      long var0 = System.nanoTime();
      partNanos[partAt++] = var0 - partStart;
      partStart = var0;
   }

   public static void gameLoaded() {
      if (BuildPin.supported()) {
         Settings.load();
         ThirdPerson.declare();
         Eye.declare();
         PadLook.declare();
         AimAssist.declare();
         PadVanilla.install();
         PadInput.gameLoaded();
         FarWorld.clear();
         Atmosphere.gameLoaded();
         System.out
            .println(
               "[Viewpoint] chunk grid "
                  + IsoChunkMap.chunkGridWidth
                  + " x "
                  + IsoChunkMap.chunkGridWidth
                  + " (the game's own; the near world reaches "
                  + View.radiusChunks() * 8
                  + " squares)"
            );
      }
   }

   private static void resetCaches() {
      ChunkCache.clear();
      LightSources.forget();
      LightCheck.clear();
      Characters.forget();
      Corpses.forget();
   }

   private FP() {
   }
}
