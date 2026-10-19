package viewpoint;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import me.zed_0xff.zombie_buddy.Patch;

public final class Compat {
   public static final int GAME_LOADED = 0;
   public static final int CULL_ANIMALS = 1;
   public static final int DISPLAY_CURSOR = 2;
   public static final int DRAW_GENERIC = 3;
   public static final int INVALIDATE_ALL = 4;
   public static final int INVALIDATE_LEVEL = 5;
   public static final int ISO_CELL = 6;
   public static final int ISO_CURSOR = 7;
   public static final int ISO_RETICLE = 8;
   public static final int LIGHTING_UPDATE = 9;
   public static final int LOOK_ANGLE = 10;
   public static final int MOUSE_UPDATE = 11;
   public static final int MOVEMENT = 12;
   public static final int MOVE_VECTOR = 13;
   public static final int RETICLE_X = 14;
   public static final int RETICLE_Y = 15;
   public static final int RUNNING = 16;
   public static final int SPRINTING = 17;
   public static final int STRAFING = 18;
   public static final int VIEW_DISTANCE = 19;
   public static final int VIEW_DIST_MAX = 20;
   public static final int VISION_CONE = 21;
   public static final int WEATHER_FX = 22;
   public static final int ZOMBIE_SCREEN_X = 23;
   public static final int ZOMBIE_SCREEN_Y = 24;
   public static final int SIMULATION_LEVEL = 25;
   public static final int STATS_GET = 26;
   public static final int ZOMBIE_CULL = 27;
   public static final int HEAD_SQUARE = 28;
   public static final int KEY_DOWN = 29;
   public static final int WINDOW_TOGGLE = 30;
   public static final int WINDOW_SMASH = 31;
   public static final int WINDOW_GLASS = 32;
   public static final int WINDOW_SYNC = 33;
   public static final int FRAME_SWAP = 34;
   public static final int GAME_RENDER = 35;
   public static final int UI_FRAME_END = 36;
   public static final int PLAY_STARTED = 37;
   public static final int LOCK_FPS = 38;
   public static final int FRAMERATE_UNCAPPED = 39;
   public static final int LOAD_OPTIONS = 40;
   public static final int SAVE_OPTIONS = 41;
   public static final int CHECK_LIGHTS = 42;
   public static final int SWITCH_POWER = 43;
   public static final int MODEL_SLOT_UPDATE = 44;
   public static final int LINE_OF_SIGHT = 45;
   public static final int THUNDER = 46;
   public static final int SOUND_LISTENER = 47;
   public static final int CONTEXT_PICK = 48;
   public static final int PICK_DOOR = 49;
   public static final int PICK_WINDOW = 50;
   public static final int PICK_WINDOW_FRAME = 51;
   public static final int PICK_THUMPABLE = 52;
   public static final int PICK_HOPPABLE = 53;
   public static final int PICK_CORPSE = 54;
   public static final int PICK_TREE = 55;
   public static final int PICK_VEHICLE = 56;
   public static final int CAMERA_ZOOM = 57;
   public static final int AIM_FROM_RETICLE = 58;
   public static final int SKELETON_POSED = 59;
   public static final int ROOT_MOTION = 60;
   public static final int MUZZLE_FLASH = 61;
   public static final int EXPLOSION = 62;
   public static final int RIGHT_DELAY = 63;
   public static final int ANIMATION_TIME = 64;
   public static final int ZOMBIE_HIT = 65;
   public static final int SOUND_EMITTER = 66;
   public static final int SHOT = 67;
   public static final int SHOT_CANDIDATES = 68;
   public static final int HIT_LIST = 69;
   public static final int MISSED_SHOT = 70;
   public static final int CAMERA_TARGET = 71;
   public static final int TARGETED_PART = 72;
   public static final int BARREL_TARGET = 73;
   public static final int PELLET_COUNT = 74;
   public static final int ZOOM_SCROLL = 75;
   public static final int TARGET_ZOOM = 76;
   public static final int PROGRESS_PLACE = 77;
   public static final int PROGRESS_DRAW = 78;
   public static final int UI_RENDER = 79;
   public static final int UI_UPDATE = 80;
   public static final int RELATIVE_FILE = 81;
   public static final int OFFSCREEN_RENDERED = 82;
   public static final int LOOT_ROLL = 83;
   public static final int RETICLE_ARM = 84;
   public static final int AIM_AXIS = 85;
   public static final int PAD_PAN = 86;
   public static final int PRECISION_AIM = 87;
   public static final int PAD_BUTTON = 88;
   public static final int MODEL_SHADER = 89;
   public static final int LOAD_SOUND = 90;
   public static final int SHOT_GATHER = 91;
   private static final Class<?>[] PATCHES = new Class[]{
           Patch_GameLoaded.class,
           Patch_CullAnimals.class,
           Patch_DisplayCursor.class,
           Patch_DrawGeneric.class,
           Patch_InvalidateAll.class,
           Patch_InvalidateLevel.class,
           Patch_IsoCell.class,
           Patch_IsoCursor.class,
           Patch_IsoReticle.class,
           Patch_LightingUpdate.class,
           Patch_LookAngle.class,
           Patch_MouseUpdate.class,
           Patch_Movement.class,
           Patch_MoveVector.class,
           Patch_ReticleX.class,
           Patch_ReticleY.class,
           Patch_Running.class,
           Patch_Sprinting.class,
           Patch_Strafing.class,
           Patch_ViewDistance.class,
           Patch_ViewDistMax.class,
           Patch_VisionCone.class,
           Patch_WeatherFX.class,
           Patch_ZombieScreenX.class,
           Patch_ZombieScreenY.class,
           Patch_SimulationLevel.class,
           Patch_StatsGet.class,
           Patch_ZombieCull.class,
           Patch_HeadSquare.class,
           Patch_KeyDown.class,
           Patch_WindowToggle.class,
           Patch_WindowSmash.class,
           Patch_WindowGlass.class,
           Patch_WindowSync.class,
           Patch_FrameSwap.class,
           Patch_GameRender.class,
           Patch_UiFrameEnd.class,
           Patch_PlayStarted.class,
           Patch_LockFps.class,
           Patch_FramerateUncapped.class,
           Patch_LoadOptions.class,
           Patch_SaveOptions.class,
           Patch_CheckLights.class,
           Patch_SwitchPower.class,
           Patch_ModelSlotUpdate.class,
           Patch_LineOfSight.class,
           Patch_Thunder.class,
           Patch_SoundListener.class,
           Patch_ContextPick.class,
           Patch_PickDoor.class,
           Patch_PickWindow.class,
           Patch_PickWindowFrame.class,
           Patch_PickThumpable.class,
           Patch_PickHoppable.class,
           Patch_PickCorpse.class,
           Patch_PickTree.class,
           Patch_PickVehicle.class,
           Patch_CameraZoom.class,
           Patch_AimFromReticle.class,
           Patch_SkeletonPosed.class,
           Patch_RootMotion.class,
           Patch_MuzzleFlash.class,
           Patch_Explosion.class,
           Patch_RightDelay.class,
           Patch_AnimationTime.class,
           Patch_ZombieHit.class,
           Patch_SoundEmitter.class,
           Patch_Shot.class,
           Patch_ShotCandidates.class,
           Patch_HitList.class,
           Patch_MissedShot.class,
           Patch_CameraTarget.class,
           Patch_TargetedPart.class,
           Patch_BarrelTarget.class,
           Patch_PelletCount.class,
           Patch_ZoomScroll.class,
           Patch_TargetZoom.class,
           Patch_ProgressBarPlace.class,
           Patch_ProgressBarDraw.class,
           Patch_UiRender.class,
           Patch_UiUpdate.class,
           Patch_RelativeFile.class,
           Patch_OffscreenRendered.class,
           Patch_LootRoll.class,
           Patch_ReticleArm.class,
           Patch_AimAxis.class,
           Patch_PadPan.class,
           Patch_PrecisionAim.class,
           Patch_PadButton.class,
           Patch_ModelShader.class,
           Patch_LoadSound.class,
           Patch_ShotGather.class
   };
   private static final String[] WHEN = new String[]{
           "when a game loads",
           "every frame",
           "every frame",
           "every frame",
           "when a chunk changes",
           "when a chunk changes",
           "every frame",
           "when the game draws its cursor",
           "while aiming",
           "every frame",
           "every frame",
           "every frame",
           "every frame on foot",
           "while moving on foot",
           "while aiming",
           "while aiming",
           "every frame",
           "every frame",
           "every frame",
           "every frame",
           "every frame",
           "every frame",
           "every frame",
           "with zombies around",
           "with zombies around",
           "every frame",
           "every frame",
           "every frame",
           "with zombies out of sight nearby",
           "every frame",
           "when a window opens or closes",
           "when a window is smashed",
           "when broken glass is taken out of a window",
           "when another player changes a window (multiplayer)",
           "every frame",
           "every frame",
           "every frame",
           "when play starts",
           "every frame",
           "every frame",
           "when the game reads its options",
           "when the game writes its options",
           "every frame",
           "every frame with a switched lamp in the loaded chunks",
           "every frame with a character that has a model",
           "every frame",
           "when lightning strikes or thunder rumbles, in a storm",
           "every frame",
           "when the mouse moves over the world",
           "when something in the world is right-clicked",
           "when something in the world is right-clicked",
           "when something in the world is right-clicked",
           "when something in the world is right-clicked",
           "when something in the world is right-clicked",
           "when a world menu opens",
           "when something in the world is right-clicked",
           "when a vehicle is right-clicked",
           "every frame",
           "while aiming a firearm",
           "every frame",
           "every frame on foot",
           "when a gun fires",
           "when something explodes",
           "while the right mouse button is held",
           "every frame",
           "when a zombie is struck",
           "while a sound plays",
           "when a weapon swings or fires",
           "while aiming a firearm with someone in its range",
           "while aiming a firearm",
           "when a shot hits nothing",
           "while aiming a firearm with someone in its range",
           "when a shot hits someone",
           "while aiming a firearm with someone or a vehicle in its range",
           "while aiming a shotgun with someone in its range",
           "when the mouse wheel zooms the isometric view, in first or third person",
           "when the isometric view zooms by itself (driving, running), in first or third person",
           "while a timed action of the player's runs",
           "while a timed action of the player's runs",
           "whenever the game draws its UI",
           "whenever the game updates its UI",
           "the first time the game looks up a file's name (a model's texture, a zombie's clothes)",
           "every frame in game",
           "when a chunk with containers never looked into loads, in single player",
           "every frame (the game's cursor, moodles and reticle)",
           "every frame while player 0 plays on a pad",
           "every frame",
           "every frame",
           "every frame while player 0 plays on a pad",
           "the first time the game makes a model (an object, an item, a vehicle or clothes first shown)",
           "the first time a sound file plays (a mod's sound, or the tutorial's voice)",
           "while aiming a firearm with the crosshair"
   };
   private static final String[][] MEMBERS = new String[][]{
           {"zombie.core.skinnedmodel.model.VertexBufferObject", "handle", "owned models (ModelMeshes)"},
           {"zombie.iso.fboRenderChunk.FBORenderChunkManager", "sizeChunkStore", "isometric chunk textures let go in first person (IsoChunkTextures)"},
           {"zombie.core.skinnedmodel.model.VertexBufferObject", "vertexFormat", "owned models (ModelMeshes)"},
           {"zombie.core.skinnedmodel.model.ModelSlotRenderData", "rendered", "owned models' textures (CharacterTextures.Snapshot)"},
           {"zombie.characters.IsoGameCharacter", "updateModelTextures", "characters' looks spread over frames (CharacterLooks); every look made at once"},
           {"zombie.core.skinnedmodel.model.ModelSlotRenderData", "characterOutline", "owned characters' outlines (ModelCapture)"},
           {"zombie.core.skinnedmodel.model.ModelSlotRenderData", "outlineColor", "owned characters' outlines (ModelCapture)"},
           {"zombie.core.skinnedmodel.model.ModelSlotRenderData", "outlineBehindPlayer", "owned characters' outlines (ModelCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "x", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "y", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "z", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "transform", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "model", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.WorldItemModelDrawer", "renderer", "owned ground items (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "modelScript", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "texture", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "matrixPalette", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "tintR", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "tintG", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "tintB", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "hue", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "ambientR", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "ambientG", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.IsoObjectModelDrawer", "ambientB", "owned object models (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "model", "owned ground items (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "weaponParts", "owned ground items (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "transform", "owned ground items (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "cullFace", "owned ground items (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "tintMask", "owned ground items (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "tintR", "owned ground items (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "tintG", "owned ground items (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "tintB", "owned ground items (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "hue", "owned ground items (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "ambientR", "owned ground items (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "ambientG", "owned ground items (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "ambientB", "owned ground items (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer$WeaponPartParams", "model", "owned ground items' weapon parts (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer$WeaponPartParams", "transform", "owned ground items' weapon parts (RigidCapture)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "checkSmartTexture()", "owned ground items' tint masks, blood and fluid (ItemTextures)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "modelTextureName", "owned ground items' tint masks, blood and fluid (ItemTextures)"},
           {"zombie.core.skinnedmodel.model.ItemModelRenderer", "smartTexture", "owned ground items' tint masks, blood and fluid (ItemTextures)"},
           {"zombie.core.skinnedmodel.DeadBodyAtlas$BodyTexture", "entry", "corpse images waiting for their offsets (WorldMesher)"},
           {"zombie.input.Mouse", "x", "the mouse parked while looking (Controls)"},
           {"zombie.input.Mouse", "y", "the mouse parked while looking (Controls)"},
           {"zombie.characters.Stats", "stats", "the game's stat lookup without boxing (GameFixes)"},
           {"zombie.erosion.ErosionMain", "noiseMain", "the erosion's trees in the far world (Erosion)"},
           {"zombie.erosion.ErosionMain", "noiseMoisture", "the erosion's trees in the far world (Erosion)"},
           {"zombie.erosion.ErosionMain", "noiseMinerals", "the erosion's trees in the far world (Erosion)"},
           {"zombie.erosion.ErosionMain", "soilTable", "the erosion's trees in the far world (Erosion)"},
           {"zombie.erosion.utils.Noise2D", "layers", "the erosion's trees in the far world (Erosion)"},
           {"zombie.erosion.utils.Noise2D", "perm", "the erosion's trees in the far world (Erosion)"},
           {"zombie.erosion.utils.Noise2D$Layer", "freq", "the erosion's trees in the far world (Erosion)"},
           {"zombie.erosion.utils.Noise2D$Layer", "amp", "the erosion's trees in the far world (Erosion)"},
           {"zombie.erosion.utils.Noise2D$Layer", "p", "the erosion's trees in the far world (Erosion)"},
           {"zombie.erosion.categories.NatureTrees", "trees", "the erosion's trees in the far world (Erosion)"},
           {"zombie.erosion.categories.NatureTrees", "soilRef", "the erosion's trees in the far world (Erosion)"},
           {"zombie.erosion.categories.NatureTrees", "spawnChance", "the erosion's trees in the far world (Erosion)"},
           {"zombie.erosion.categories.NatureBush", "soilRef", "the erosion's plants in the far world (ErosionPlants)"},
           {"zombie.erosion.categories.NatureBush", "spawnChance", "the erosion's plants in the far world (ErosionPlants)"},
           {"zombie.erosion.categories.NaturePlants", "soilRef", "the erosion's plants in the far world (ErosionPlants)"},
           {"zombie.erosion.categories.NaturePlants", "spawnChance", "the erosion's plants in the far world (ErosionPlants)"},
           {"zombie.erosion.categories.NatureTrees$TreeInit", "tilesetName", "the erosion's trees in the far world (Erosion)"},
           {"fmod.fmod.FMODSoundEmitter", "instances", "sounds raised on the terrain relief (SoundLift)"},
           {"fmod.fmod.FMODSoundEmitter$EventSound", "eventInstance", "sounds raised on the terrain relief (SoundLift)"},
           {"fmod.fmod.FMODSoundEmitter$FileSound", "channel", "sounds raised on the terrain relief (SoundLift)"},
           {"fmod.fmod.FMODSoundEmitter$FileSound", "is3d", "sounds raised on the terrain relief (SoundLift)"},
           {"zombie.CombatManager", "calculateHitChanceData()", "shots where the aim sends them (gunplay.Odds)"},
           {"zombie.CombatManager$HitChanceData", "hitChance", "shots where the aim sends them (gunplay.Odds)"},
           {"zombie.iso.sprite.IsoReticle", "targetChance", "the reticle's arms at the shot's cone (gunplay.Reticle)"},
           {"zombie.iso.sprite.IsoReticle", "targetAimPenalty", "the reticle's arms at the shot's cone (gunplay.Reticle)"},
           {"zombie.core.physics.BallisticsController", "numberOfSpreadData", "a shotgun's tracers along its pellets (gunplay.Pellets)"},
           {"zombie.iso.PlayerCamera", "rightClickXf", "a pad's camera pan held off in the view (PadPan)"},
           {"zombie.iso.PlayerCamera", "rightClickYf", "a pad's camera pan held off in the view (PadPan)"}
   };
   private static final boolean[] fired = new boolean[PATCHES.length];
   private static boolean checked;
   private static boolean reported;

   static Set<String> patchNames() {
      TreeSet<String> var0 = new TreeSet<>();

      for (Class<?> var4 : PATCHES) {
         var0.add(var4.getSimpleName());
      }

      return var0;
   }

   public static void hit(int var0) {
      if (!fired[var0]) {
         fired[var0] = true;
         if (reported) {
            System.out.println("[Viewpoint] patches seen working: " + seen() + " of " + PATCHES.length + ", now also " + name(var0));
         }
      }
   }

   public static int check() {
      if (checked) {
         return 0;
      }

      checked = true;
      ArrayList<String> var0 = new ArrayList<>();

      for (Class<?> var4 : PATCHES) {
         Patch var5 = var4.getAnnotation(Patch.class);
         if (var5 == null) {
            var0.add(var4.getSimpleName() + " (no @Patch)");
         } else if (!hasMethod(var5.className(), var5.methodName())) {
            var0.add(var5.className() + "." + var5.methodName() + " (" + var4.getSimpleName() + ")");
         }
      }

      for (String[] var11 : MEMBERS) {
         if (!hasMember(var11[0], var11[1])) {
            var0.add(var11[0] + "." + var11[1] + " - " + var11[2] + " stays off");
         }
      }

      if (var0.isEmpty()) {
         System.out.println("[Viewpoint] compat: all " + PATCHES.length + " patch targets and " + MEMBERS.length + " private members found");
      } else {
         System.out.println("[Viewpoint] compat: the game has changed since the mod was written; missing:");

         for (String var9 : var0) {
            System.out.println("[Viewpoint]   " + var9);
         }
      }

      return var0.size();
   }

   public static void reportFired() {
      if (!reported) {
         reported = true;
         StringBuilder var0 = new StringBuilder();

         for (int var1 = 0; var1 < PATCHES.length; var1++) {
            if (!fired[var1]) {
               var0.append(var0.length() == 0 ? "" : ", ").append(name(var1)).append(" (").append(WHEN[var1]).append(')');
            }
         }

         System.out
                 .println(
                         "[Viewpoint] patches seen working: "
                                 + seen()
                                 + " of "
                                 + PATCHES.length
                                 + (
                                 var0.length() == 0
                                         ? ""
                                         : "; not yet: " + var0 + ". Each is logged when it first runs; one still missing after its situation came up was not applied."
                         )
                 );
      }
   }

   private static int seen() {
      int var0 = 0;

      for (boolean var4 : fired) {
         if (var4) {
            var0++;
         }
      }

      return var0;
   }

   private static String name(int var0) {
      Patch var1 = PATCHES[var0].getAnnotation(Patch.class);
      return var1 == null ? PATCHES[var0].getSimpleName() : var1.className().substring(var1.className().lastIndexOf(46) + 1) + "." + var1.methodName();
   }

   private static boolean hasMethod(String var0, String var1) {
      try {
         for (Class var2 = Class.forName(var0, false, Compat.class.getClassLoader()); var2 != null; var2 = var2.getSuperclass()) {
            for (Method var6 : var2.getDeclaredMethods()) {
               if (var6.getName().equals(var1)) {
                  return true;
               }
            }
         }

         return false;
      } catch (ClassNotFoundException | LinkageError var7) {
         return false;
      }
   }

   private static boolean hasMember(String var0, String var1) {
      if (var1.endsWith("()")) {
         return hasMethod(var0, var1.substring(0, var1.length() - 2));
      }

      try {
         for (Class var2 = Class.forName(var0, false, Compat.class.getClassLoader()); var2 != null; var2 = var2.getSuperclass()) {
            for (Field var6 : var2.getDeclaredFields()) {
               if (var6.getName().equals(var1)) {
                  return true;
               }
            }
         }

         return false;
      } catch (ClassNotFoundException | LinkageError var7) {
         return false;
      }
   }

   private Compat() {
   }
}