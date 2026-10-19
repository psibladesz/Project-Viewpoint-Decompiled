package viewpoint;

import fmod.fmod.FMODSoundEmitter;
import fmod.fmod.SoundListener;
import viewpoint.assets.Warmup;
import viewpoint.camera.Impulses;
import viewpoint.core.View;
import viewpoint.environment.Lightning;
import viewpoint.game.AnimalSets;
import viewpoint.game.DiagonalWalk;
import viewpoint.game.FrameCaps;
import viewpoint.game.GameFixes;
import viewpoint.game.Hearing;
import viewpoint.game.IsoChunkTextures;
import viewpoint.game.IsoZoom;
import viewpoint.game.LightCheck;
import viewpoint.game.LootRolls;
import viewpoint.game.ProgressBar;
import viewpoint.game.RelativePaths;
import viewpoint.game.SoundLift;
import viewpoint.game.SpottedStack;
import viewpoint.input.ArmsPitch;
import viewpoint.input.Controls;
import viewpoint.input.HeadLook;
import viewpoint.input.Look;
import viewpoint.input.MouseButtons;
import viewpoint.input.PadAim;
import viewpoint.input.PadPan;
import viewpoint.input.PadVanilla;
import viewpoint.interact.LootMenu;
import viewpoint.interact.LootPad;
import viewpoint.interact.LootPanel;
import viewpoint.interact.MouseTargets;
import viewpoint.light.Visibility;
import viewpoint.models.CharacterRate;
import viewpoint.models.ModelCull;
import viewpoint.models.Models;
import viewpoint.platform.CaptureUi;
import viewpoint.platform.FrameTimes;
import viewpoint.platform.Graphics;
import viewpoint.platform.KeyInput;
import viewpoint.platform.MemoryCensus;
import viewpoint.platform.Onboarding;
import viewpoint.platform.PadInput;
import viewpoint.platform.PerformanceOverlay;
import viewpoint.platform.SettingsWindow;
import viewpoint.platform.VideoMemory;
import viewpoint.render.FrameGenPass;
import viewpoint.render.Latency;
import viewpoint.world.ChunkCache;
import zombie.UpdateSchedulerSimulationLevel;
import zombie.ZomboidFileSystem;
import zombie.characters.CharacterJoypadButtonBinding;
import zombie.characters.CharacterStat;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.characters.Stats;
import zombie.core.SpriteRenderer;
import zombie.core.skinnedmodel.ModelManager.ModelSlot;
import zombie.core.skinnedmodel.animation.AnimationPlayer;
import zombie.core.textures.TextureDraw.GenericDrawer;
import zombie.iso.IsoCell;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.iso.PlayerCamera;
import zombie.iso.Vector2;
import zombie.iso.IsoObjectPicker.ClickObject;
import zombie.iso.fboRenderChunk.FBORenderLevels;
import zombie.iso.objects.IsoLightSwitch;
import zombie.iso.objects.IsoWindow;
import zombie.ui.ActionProgressBar;
import zombie.ui.UIElement;
import zombie.vehicles.BaseVehicle;

public final class Hooks {
   public static boolean skipIsoCellRender(IsoCell var0) {
      Compat.hit(6);
      return FP.renderWorld(var0);
   }

   public static boolean skipIsoCursor() {
      Compat.hit(7);
      return View.enabled;
   }

   public static boolean skipWeatherFx() {
      Compat.hit(22);
      return View.enabled;
   }

   public static boolean skipDrawGeneric(GenericDrawer var0) {
      Compat.hit(3);
      return Models.capture(var0);
   }

   public static void levelInvalidated(FBORenderLevels var0, int var1, long var2) {
      Compat.hit(5);
      ChunkCache.invalidated(var0, var1, var2);
   }

   public static void allLevelsInvalidated(FBORenderLevels var0, long var1) {
      Compat.hit(4);
      ChunkCache.invalidated(var0, Integer.MIN_VALUE, var1);
   }

   public static void windowToggled(IsoWindow var0) {
      Compat.hit(30);
      ChunkCache.windowChanged(var0);
   }

   public static void windowSmashed(IsoWindow var0) {
      Compat.hit(31);
      ChunkCache.windowChanged(var0);
   }

   public static void windowGlassChanged(IsoWindow var0) {
      Compat.hit(32);
      ChunkCache.windowChanged(var0);
   }

   public static void gameLoaded() {
      Compat.hit(0);
      LootRolls.clear();
      FP.gameLoaded();
      Warmup.gameLoaded();
   }

   public static void thunder(int var0, int var1, boolean var2) {
      Compat.hit(46);
      Lightning.struck(var0, var1, var2);
      Impulses.thunder(var0, var1, var2);
   }

   public static boolean rightHeld(boolean var0) {
      Compat.hit(63);
      return View.enabled ? MouseButtons.rightHeld(var0) : var0;
   }

   public static ClickObject contextPick(int var0, int var1, ClickObject var2) {
      Compat.hit(48);
      return MouseTargets.on() ? MouseTargets.contextPick(var0, var1) : var2;
   }

   public static IsoObject pickDoor(IsoObject var0) {
      return picked(49, 0, var0);
   }

   public static IsoObject pickWindow(IsoObject var0) {
      return picked(50, 1, var0);
   }

   public static IsoObject pickWindowFrame(IsoObject var0) {
      return picked(51, 2, var0);
   }

   public static IsoObject pickThumpable(IsoObject var0) {
      return picked(52, 3, var0);
   }

   public static IsoObject pickHoppable(IsoObject var0) {
      return picked(53, 4, var0);
   }

   public static IsoObject pickCorpse(IsoObject var0) {
      return picked(54, 5, var0);
   }

   public static IsoObject pickTree(IsoObject var0) {
      return picked(55, 6, var0);
   }

   private static IsoObject picked(int var0, int var1, IsoObject var2) {
      Compat.hit(var0);
      return MouseTargets.on() ? MouseTargets.picked(var1) : var2;
   }

   public static BaseVehicle pickVehicle(BaseVehicle var0) {
      Compat.hit(56);
      return MouseTargets.on() ? MouseTargets.vehicle() : var0;
   }

   public static void soundListener(SoundListener var0) {
      Compat.hit(47);
      Hearing.listen(var0);
   }

   public static void soundEmitter(FMODSoundEmitter var0) {
      Compat.hit(66);
      SoundLift.ticked(var0);
   }

   public static float cameraZoom(float var0) {
      Compat.hit(57);
      return Hearing.cameraZoom(var0);
   }

   public static boolean skipZoomScroll(int var0) {
      Compat.hit(75);
      return IsoZoom.held(var0);
   }

   public static boolean skipTargetZoom(int var0) {
      Compat.hit(76);
      return IsoZoom.held(var0);
   }

   public static void lightingUpdateStarted() {
      Compat.hit(9);
      Visibility.lightingUpdate = true;
   }

   public static void lightingUpdateEnded() {
      Visibility.lightingUpdate = false;
      LootRolls.frame();
   }

   public static boolean deferLootRoll(IsoObject var0) {
      Compat.hit(83);
      return LootRolls.defer(var0);
   }

   public static boolean skipLightCheck() {
      Compat.hit(42);
      return LightCheck.skipCheck();
   }

   public static boolean skipSwitchPower(IsoLightSwitch var0) {
      Compat.hit(43);
      return LightCheck.skipPower(var0);
   }

   public static boolean switchPower(IsoLightSwitch var0, boolean var1) {
      return LightCheck.power(var0, var1);
   }

   public static float viewDistance(float var0) {
      Compat.hit(19);
      return Visibility.viewDistance(var0);
   }

   public static float viewDistMax(float var0) {
      Compat.hit(20);
      return Visibility.viewDistance(var0);
   }

   public static boolean skipAnimalCull(IsoWorld var0) {
      Compat.hit(1);
      return ModelCull.cullAnimals(var0);
   }

   public static void modelSlotUpdating(ModelSlot var0) {
      Compat.hit(44);
      CharacterRate.updating(var0);
   }

   public static void modelSlotUpdated() {
      CharacterRate.updated();
   }

   public static void zombieCullStarted() {
      Compat.hit(27);
      ModelCull.zombieCull(true);
   }

   public static void zombieCullEnded() {
      ModelCull.zombieCull(false);
   }

   public static boolean couldSeeHeadSquare(IsoZombie var0, IsoPlayer var1, boolean var2) {
      Compat.hit(28);
      return ModelCull.headSquareSeen(var0, var1, var2);
   }

   public static int zombieScreenX(IsoZombie var0, int var1, int var2) {
      Compat.hit(23);
      return ModelCull.cullScreenX(var0, var1, var2);
   }

   public static int zombieScreenY(int var0, int var1) {
      Compat.hit(24);
      return ModelCull.cullScreenY(var0, var1);
   }

   public static UpdateSchedulerSimulationLevel simulationLevel(IsoMovingObject var0, UpdateSchedulerSimulationLevel var1) {
      Compat.hit(25);
      return ModelCull.simulationLevel(var0, var1);
   }

   public static void inputMoveVector(IsoPlayer var0, Vector2 var1) {
      Compat.hit(13);
      Controls.moveVector(var0, var1);
   }

   public static void movementStarting(IsoPlayer var0) {
      Compat.hit(12);
      Controls.beforeMovement(var0);
   }

   public static void movementDone(IsoPlayer var0) {
      Controls.afterMovement(var0);
   }

   public static void rootMotion(IsoPlayer var0, Vector2 var1, boolean var2) {
      Compat.hit(60);
      Controls.rootMotion(var0, var1, var2);
   }

   public static float lookAngle(IsoGameCharacter var0, float var1) {
      Compat.hit(10);
      return Controls.lookAngle(var0, var1);
   }

   public static float visionCone(IsoGameCharacter var0, float var1) {
      Compat.hit(21);
      return Controls.visionCone(var0, var1);
   }

   public static boolean running(IsoGameCharacter var0, boolean var1) {
      Compat.hit(16);
      return Controls.running(var0, var1);
   }

   public static boolean sprinting(IsoGameCharacter var0, boolean var1) {
      Compat.hit(17);
      return Controls.running(var0, var1);
   }

   public static boolean strafing(IsoGameCharacter var0, boolean var1) {
      Compat.hit(18);
      return Controls.strafing(var0, var1);
   }

   public static void skeletonPosed(AnimationPlayer var0) {
      Compat.hit(59);
      ArmsPitch.posed(var0);
      HeadLook.posed(var0);
   }

   public static void mouseUpdated() {
      Compat.hit(11);
      MouseButtons.pin();
      SettingsWindow.blockGameMouse();
      LootMenu.mouse();
   }

   public static boolean keyDown(int var0, boolean var1) {
      Compat.hit(29);
      boolean var2 = KeyInput.keyDown(var0, var1);
      return LootMenu.keyDown(var0, GameFixes.keyDown(var0, var2, KeyInput.reachesGameFlying(var0)));
   }

   public static boolean padButtonDown(CharacterJoypadButtonBinding var0, int var1, boolean var2) {
      Compat.hit(88);
      boolean var3 = PadInput.gameSees(PadVanilla.input(var0), var1, var2);
      return PadAim.gameSees(var0, var1, LootPad.gameSees(var0, var1, var3));
   }

   public static void padPan(PlayerCamera var0) {
      Compat.hit(86);
      if (View.enabled) {
         PadPan.updated(var0);
      }
   }

   public static boolean skipStatsGet() {
      Compat.hit(26);
      return GameFixes.statsReady;
   }

   public static float statsGet(Stats var0, CharacterStat var1, float var2) {
      return GameFixes.statsReady ? GameFixes.stat(var0, var1) : var2;
   }

   public static void lineOfSightStarting(IsoPlayer var0) {
      Compat.hit(45);
      SpottedStack.use(var0);
   }

   public static void playStarting() {
      ModLua.load();
   }

   public static void playStarted() {
      Compat.hit(37);
      Warmup.playStarted();
      AnimalSets.load();
      DiagonalWalk.fix();
      Onboarding.playStarted(IsoChunkTextures.heldMiB());
   }

   public static int lockFps(int var0) {
      Compat.hit(38);
      return FrameCaps.ON ? FrameCaps.lockNow(var0) : var0;
   }

   public static boolean framerateUncapped(boolean var0) {
      Compat.hit(39);
      return FrameCaps.ON ? FrameCaps.uncappedNow(var0) : var0;
   }

   public static void optionsLoading() {
      Compat.hit(40);
      if (FrameCaps.ON) {
         FrameCaps.optionsLoading();
      }
   }

   public static void optionsLoaded() {
      if (FrameCaps.ON) {
         FrameCaps.optionsLoaded();
      }
   }

   public static void optionsSaving() {
      Compat.hit(41);
      if (FrameCaps.ON) {
         FrameCaps.optionsSaving();
      }
   }

   public static void optionsSaved() {
      if (FrameCaps.ON) {
         FrameCaps.optionsSaved();
      }
   }

   public static void uiFrameEnding() {
      Compat.hit(36);
      SettingsWindow.update();
      Warmup.update();
      LootPanel.draw();
      PerformanceOverlay.draw();
   }

   public static void progressBarPlaced(ActionProgressBar var0, int var1) {
      Compat.hit(77);
      ProgressBar.placed(var0, var1);
   }

   public static boolean skipProgressBar(ActionProgressBar var0) {
      Compat.hit(78);
      return ProgressBar.drawn(var0);
   }

   public static void uiRenderStarting(UIElement var0) {
      Compat.hit(79);
      CaptureUi.renderStarting(var0);
   }

   public static void uiRenderDone(UIElement var0) {
      CaptureUi.renderDone(var0);
   }

   public static void uiUpdateStarting(UIElement var0) {
      Compat.hit(80);
      CaptureUi.updateStarting(var0);
   }

   public static void uiUpdateDone(UIElement var0) {
      CaptureUi.updateDone(var0);
   }

   public static boolean skipRelativeFile(ZomboidFileSystem var0, String var1) {
      Compat.hit(81);
      return RelativePaths.relative(var0, var1) != null;
   }

   public static String relativeFile(ZomboidFileSystem var0, String var1, String var2) {
      String var3 = RelativePaths.relative(var0, var1);
      return var3 != null ? var3 : var2;
   }

   public static void offscreenRendered() {
      Compat.hit(82);
      if (View.enabled && Graphics.FRAME_GENERATION.get()) {
         SpriteRenderer.instance.drawGeneric(HudlessDrawer.ONE);
      }
   }

   public static boolean skipMouseCursorUpdate() {
      Compat.hit(2);
      return Look.onUpdateMouseCursor();
   }

   public static void frameSwapping() {
      SettingsWindow.render();
      FrameGenPass.presenting();
      Latency.presenting();
   }

   public static void frameSwapped() {
      Compat.hit(34);
      FrameTimes.swapped();
      Look.framePresented();
      SceneDrawer.prepare();
   }

   public static void gameRenderStarted() {
      Compat.hit(35);
      FrameTimes.renderStarted();
      VideoMemory.sample(System.nanoTime());
      MemoryCensus.renderFrame(System.nanoTime());
      Onboarding.renderFrame();
   }

   public static void gameRenderEnded() {
      FrameTimes.renderEnded();
   }

   public static void windowSynced(IsoWindow var0) {
      Compat.hit(33);
      ChunkCache.windowChanged(var0);
   }

   private Hooks() {
   }
}
