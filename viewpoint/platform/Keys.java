package viewpoint.platform;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import zombie.Lua.LuaManager;
import zombie.core.Core;

public final class Keys {
   private static final String VIEWS = "Keys/Views";
   private static final String LOOT = "Keys/Loot menu";
   private static final String CAMERA = "Keys/Free camera";
   private static final String DEBUG = "Keys/Debug";
   private static final List<KeyBind> ALL = new ArrayList<>();
   public static final KeyBind FIRST_PERSON = press("keys.firstPerson", "First person on or off", "Keys/Views", "O", "The first-person view on or off.");
   public static final KeyBind THIRD_PERSON = press(
      "keys.thirdPerson", "First or third person", "Keys/Views", "SHIFT+O", "While the view is on: the camera over the shoulder, or back to the eyes."
   );
   public static final KeyBind SHOULDER_SWAP = press(
      "keys.shoulderSwap", "Third person: swap shoulders", "Keys/Views", "SEMICOLON", "In third person: the camera over the other shoulder."
   );
   public static final KeyBind CAMERA_BEHIND = press(
      "keys.cameraBehind", "Third person: camera behind", "Keys/Views", "SHIFT+SEMICOLON", "In third person: the camera turns back behind the character."
   );
   public static final KeyBind FREE_LOOK = hold(
      "keys.freeLook",
      "Free look (hold)",
      "Keys/Views",
      "MOUSE4",
      "Held on foot: the view turns without turning the body, which keeps walking where it went; in first person within an angle (Controls, Camera). Let go, the view turns back."
   );
   public static final KeyBind CURSOR = press(
      "keys.cursor",
      "Cursor free or locked",
      "Keys/Views",
      "MOUSE3",
      "While the view is on: frees the cursor to click on things, or locks it to look around again."
   );
   public static final KeyBind SETTINGS = press("keys.settings", "Viewpoint settings window", "Keys/Views", "DELETE", "Shows or hides this window.");
   public static final KeyBind SCREENSHOT = press("keys.screenshot", "Viewpoint screenshot", "Keys/Views", "F9", "Saves the view to Zomboid/viewpoint-shots.");
   public static final KeyBind OVERLAY = press(
      "keys.overlay", "Performance overlay", "Keys/Views", "APOSTROPHE", "The overlay's next mode: full, compact, the graph alone, off."
   );
   public static final KeyBind LOOT_TAKE = menu(
      "keys.lootTake",
      "Loot menu: take or do",
      "F",
      "Takes the item selected in the loot menu, or does the action selected; the wheel selects. The game's Interact stays the game's."
   );
   public static final KeyBind LOOT_TAKE_ALL = menu("keys.lootTakeAll", "Loot menu: take all", "R", "Takes everything the loot menu shows.");
   public static final KeyBind LOOT_WINDOW = menu(
      "keys.lootWindow",
      "Loot menu: open the loot window",
      "TAB",
      "Opens the game's own loot window on what the loot menu shows, with the cursor free; again to close it."
   );
   public static final KeyBind FREE_CAMERA = press(
      "keys.freeCamera",
      "Free camera on or off",
      "Keys/Free camera",
      "INSERT+O",
      "While the view is on: a camera flying anywhere, the world still round the player."
   );
   public static final KeyBind PHYSICAL_CAMERA = press(
         "keys.physicalCamera",
         "Physical free camera on or off",
         "Keys/Free camera",
         "SHIFT+INSERT+O",
         "In the game's debug mode (-debug) only: a free camera the world is centred on; what is drawn, its detail and the sound follow it."
      )
      .usableWhen(() -> Core.debug);
   public static final KeyBind CAMERA_FORWARD = hold("keys.cameraForward", "Free camera: forward", "W");
   public static final KeyBind CAMERA_BACK = hold("keys.cameraBack", "Free camera: back", "S");
   public static final KeyBind CAMERA_LEFT = hold("keys.cameraLeft", "Free camera: left", "A");
   public static final KeyBind CAMERA_RIGHT = hold("keys.cameraRight", "Free camera: right", "D");
   public static final KeyBind CAMERA_UP = hold("keys.cameraUp", "Free camera: up", "SPACE, E");
   public static final KeyBind CAMERA_DOWN = hold("keys.cameraDown", "Free camera: down", "LSHIFT, Q");
   public static final KeyBind CAMERA_FAST = hold("keys.cameraFast", "Free camera: four times as fast", "LMENU");
   public static final KeyBind CAMERA_HOME = press(
      "keys.cameraHome", "Free camera: back to the player", "Keys/Free camera", "NEXT", "The free camera back at the player's eyes."
   );
   public static final KeyBind LOD_VIEW = debug("keys.lodView", "Level-of-detail view", "END", "Every surface tinted by its level of detail (Debug, Views).");
   public static final KeyBind DEBUG_VIEW = dev(
      "keys.debugView",
      "Next debug view",
      "F9",
      "Normal, the art only, the light only, the volumetric light, the bounce light, the lamps' shadows, the sun's maps."
   );
   public static final KeyBind ISOLATE = dev("keys.isolate", "Next effect off", "F10", "The renderer's effects off one at a time.");
   public static final KeyBind ROOMS = dev("keys.rooms", "Rooms' culling", "F11", "The culling of the rooms no one can see: on, frozen, off.");
   public static final KeyBind MESH_RECORD = dev("keys.meshRecord", "Record the mesher", "F8", "Records the mesher round the player (TESTING.md).");
   public static final KeyBind MESH_VERIFY = dev(
      "keys.meshVerify", "Check the mesher", "SHIFT+F8", "Checks the meshes round the player against the newest recording (TESTING.md)."
   );
   public static final KeyBind CAMERA_TRACE = dev(
      "keys.cameraTrace",
      "Record a camera trace",
      "CTRL+F8",
      "Starts and stops recording the third-person camera's frames, for the offline checks (TESTING.md)."
   );
   private static final Map<String, KeyBind> MOD_OPTIONS = Map.of("lootInteract", LOOT_TAKE, "lootTakeAll", LOOT_TAKE_ALL, "lootOpenWindow", LOOT_WINDOW);
   private static boolean started;

   private static KeyBind press(String var0, String var1, String var2, String var3, String var4) {
      return declare(var0, var1, var2, KeyBind.Kind.PRESS, false, var3, var4);
   }

   private static KeyBind menu(String var0, String var1, String var2, String var3) {
      return declare(var0, var1, "Keys/Loot menu", KeyBind.Kind.MENU, false, var2, var3);
   }

   private static KeyBind hold(String var0, String var1, String var2) {
      return hold(var0, var1, "Keys/Free camera", var2, "Held while the free camera flies.");
   }

   private static KeyBind hold(String var0, String var1, String var2, String var3, String var4) {
      return declare(var0, var1, var2, KeyBind.Kind.HOLD, false, var3, var4);
   }

   private static KeyBind debug(String var0, String var1, String var2, String var3) {
      return gameDebug(declare(var0, var1, "Keys/Debug", KeyBind.Kind.PRESS, false, unlessRelease(var2, Build.RELEASE), var3));
   }

   private static KeyBind dev(String var0, String var1, String var2, String var3) {
      return gameDebug(declare(var0, var1, "Keys/Debug", KeyBind.Kind.PRESS, true, unlessRelease(var2, Build.RELEASE), "Dev mode only. " + var3));
   }

   private static KeyBind gameDebug(KeyBind var0) {
      var0.shownWhen(() -> Core.debug);
      return var0.usableWhen(() -> Core.debug);
   }

   static String unlessRelease(String var0, boolean var1) {
      return var1 ? "" : var0;
   }

   static boolean debug(KeyBind var0) {
      return var0.section.equals("Keys/Debug");
   }

   private static KeyBind declare(String var0, String var1, String var2, KeyBind.Kind var3, boolean var4, String var5, String var6) {
      KeyBind var7 = LiveSettings.add(new KeyBind(var0, var1, var2, var3, var4, var5));
      var7.describe(var6);
      ALL.add(var7);
      return var7;
   }

   static List<KeyBind> all() {
      return ALL;
   }

   static void choose(KeyBind var0, List<KeyChord> var1) {
      for (KeyBind var3 : ALL) {
         List var4 = without(var0, var1, var3, var3.chords());
         if (var3 != var0 && !var4.equals(var3.chords())) {
            var3.set(var4);
            System.out.println("[Viewpoint] key " + var3.key + " freed of " + KeyChord.text(var1) + ": " + (var4.isEmpty() ? "none" : KeyChord.text(var4)));
         }
      }

      var0.set(var1);
      System.out.println("[Viewpoint] key " + var0.key + ": " + (var1.isEmpty() ? "none" : KeyChord.text(var1)));
   }

   static List<KeyChord> without(KeyBind var0, List<KeyChord> var1, KeyBind var2, List<KeyChord> var3) {
      if (var2 != var0 && rivals(var0, var2)) {
         ArrayList var4 = new ArrayList(var3);
         var4.removeAll(var1);
         return List.copyOf(var4);
      } else {
         return var3;
      }
   }

   static boolean rivals(KeyBind var0, KeyBind var1) {
      boolean var2 = var0.kind == KeyBind.Kind.MENU && var1.kind == KeyBind.Kind.HOLD || var0.kind == KeyBind.Kind.HOLD && var1.kind == KeyBind.Kind.MENU;
      return var0.dev == var1.dev && !var2;
   }

   static void start() {
      if (!started) {
         started = true;
         File var0 = new File(LuaManager.getLuaCacheDir(), "ModOptions.ini");
         if (var0.isFile()) {
            try {
               for (Entry var2 : carried(Files.readAllLines(var0.toPath(), StandardCharsets.UTF_8)).entrySet()) {
                  LiveSettings.carryOver(((KeyBind)var2.getKey()).key, (String)var2.getValue());
               }
            } catch (IOException | RuntimeException var3) {
               System.out.println("[Viewpoint] could not read the loot menu's keys from " + var0 + ": " + var3);
            }
         }
      }
   }

   static Map<KeyBind, String> carried(List<String> var0) {
      LinkedHashMap var1 = new LinkedHashMap();

      for (String var3 : var0) {
         String[] var4 = var3.trim().split("\\|");
         boolean var5 = var4.length == 4 && var4[0].equals("keybind") && var4[1].equals("Viewpoint");
         KeyBind var6 = var5 ? MOD_OPTIONS.get(var4[2]) : null;
         if (var6 != null && var4[3].matches("\\d{1,3}")) {
            int var7 = Integer.parseInt(var4[3]);
            var1.put(var6, var7 == 0 ? "" : KeyChord.name(var7));
         }
      }

      return var1;
   }

   public static int optionCount() {
      return options().size();
   }

   public static String option(int var0) {
      return options().get(var0).key;
   }

   public static String label(String var0) {
      return find(var0).label;
   }

   public static String tooltip(String var0) {
      return find(var0).tooltip();
   }

   public static String group(String var0) {
      return SettingsWindow.group(find(var0).section);
   }

   public static boolean loot(String var0) {
      return find(var0).kind == KeyBind.Kind.MENU;
   }

   public static String text(String var0) {
      return find(var0).text();
   }

   public static String fallbackText(String var0) {
      return KeyChord.text(find(var0).fallback);
   }

   public static String display(String var0) {
      List var1 = KeyChord.parse(var0);
      return var1.isEmpty() ? "NONE" : KeyChord.display(var1, KeyChord::gameName);
   }

   public static int trigger(String var0) {
      List var1 = KeyChord.parse(var0);
      return var1.isEmpty() ? 0 : ((KeyChord)var1.get(0)).key();
   }

   public static boolean holds(String var0, String var1) {
      List var2 = KeyChord.parse(var0);
      return !var2.isEmpty() && ((KeyChord)var2.get(0)).held().contains(KeyChord.parse(var1).get(0).key());
   }

   public static String without(String var0, String var1, String var2, String var3) {
      return KeyChord.text(without(find(var0), KeyChord.parse(var1), find(var2), KeyChord.parse(var3)));
   }

   public static void set(String var0, String var1) {
      choose(find(var0), KeyChord.parse(var1));
      SettingsWindow.edited();
   }

   private static List<KeyBind> options() {
      ArrayList var0 = new ArrayList();

      for (KeyBind var2 : ALL) {
         if (!debug(var2) && var2.usable()) {
            var0.add(var2);
         }
      }

      return var0;
   }

   private static KeyBind find(String var0) {
      for (KeyBind var2 : ALL) {
         if (var2.key.equals(var0)) {
            return var2;
         }
      }

      throw new IllegalArgumentException("no key binding " + var0);
   }

   private Keys() {
   }
}
