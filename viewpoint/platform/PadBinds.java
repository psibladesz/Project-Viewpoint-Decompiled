package viewpoint.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class PadBinds {
   public static final String SECTION = "Controller/Buttons";
   public static final String MENU = "Controller/Loot menu";
   public static final String LOOK = "Controller/Look";
   public static final String ASSIST = "Controller/Aim assist";
   public static final String SWITCHING = "Controller/Switching";
   private static final List<PadBind> ALL = new ArrayList<>();
   private static final List<PadBind> LOOT = new ArrayList<>();
   public static final PadBind AIM = add(
           new PadBind("pad.aim", "Aim (hold)", "Controller/Buttons", "LT", true),
           "Held, aims as the mouse's right button does, on foot in first and third person; the right stick still turns the view. The game does not see its button then: on LT, that is the game's Shove, which you move to another button in the game's own Options, Controller."
   );
   public static final PadBind FIRST_PERSON = of(Keys.FIRST_PERSON, "");
   public static final PadBind THIRD_PERSON = of(Keys.THIRD_PERSON, "");
   public static final PadBind SHOULDER_SWAP = of(Keys.SHOULDER_SWAP, "AIM+R3");
   public static final PadBind CAMERA_BEHIND = of(Keys.CAMERA_BEHIND, "");
   public static final PadBind SCREENSHOT = of(Keys.SCREENSHOT, "");
   public static final PadBind LOOT_TAKE = menu(
           "pad.lootTake",
           Keys.LOOT_TAKE.label,
           "A",
           "While the loot menu shows: takes the item selected, or does the action selected. The game does not see the press then."
   );
   public static final PadBind LOOT_TAKE_ALL = menu(
           "pad.lootTakeAll", Keys.LOOT_TAKE_ALL.label, "X", "While the loot menu shows: takes everything it lists. The game does not see the press then."
   );
   public static final PadBind LOOT_WINDOW = menu(
           "pad.lootWindow",
           Keys.LOOT_WINDOW.label,
           "Y",
           "While the loot menu shows: opens the game's loot window on it, the pad in it (B closes it). The game does not see the press then."
   );
   public static final PadBind LOOT_UP = menu(
           "pad.lootUp", "Loot menu: select up", "UP", "While the loot menu shows: selects the row above, as the mouse wheel does; held, it steps on."
   );
   public static final PadBind LOOT_DOWN = menu(
           "pad.lootDown", "Loot menu: select down", "DOWN", "While the loot menu shows: selects the row below, as the mouse wheel does; held, it steps on."
   );
   private static volatile PadBinds.Uses game = (var0, var1) -> List.of();
   private static volatile Map<PadBind, List<String>> warnings = Map.of();

   private static PadBind of(KeyBind var0, String var1) {
      String var2 = "pad." + var0.key.substring(var0.key.indexOf(46) + 1);
      return add(new PadBind(var2, var0.label, "Controller/Buttons", var1, false), var0.tooltip());
   }

   private static PadBind menu(String var0, String var1, String var2, String var3) {
      PadBind var4 = add(new PadBind(var0, var1, "Controller/Loot menu", var2, false), var3);
      LOOT.add(var4);
      return var4;
   }

   private static PadBind add(PadBind var0, String var1) {
      LiveSettings.add(var0);
      var0.describe(var1);
      ALL.add(var0);
      return var0;
   }

   public static List<PadBind> all() {
      return Collections.unmodifiableList(ALL);
   }

   public static List<PadBind> loot() {
      return Collections.unmodifiableList(LOOT);
   }

   public static void uses(PadBinds.Uses var0) {
      game = var0;
   }

   static void choose(PadBind var0, PadChord var1) {
      var0.set(var1);
      System.out.println("[Viewpoint] pad " + var0.key + ": " + (var1 == null ? "none" : var1.text()));
   }

   static List<String> warnings(PadBind var0) {
      return warnings.getOrDefault(var0, List.of());
   }

   static List<String> conflicts(PadBind var0, PadChord var1, List<PadBind> var2, PadBinds.Uses var3) {
      ArrayList<String> var4 = new ArrayList<>();
      if (var1 == null) {
         return var4;
      }

      for (PadBind var6 : var2) {
         PadChord var7 = var6.chord();
         if (var6 != var0 && var7 != null && var1.clashes(var7)) {
            var4.add(var6.label);
         }
      }

      for (int var10 : LOOT.contains(var0) ? List.<Integer>of() : var1.buttons()) {
         if (!var0.hold || var10 != var1.button()) {
            for (String var8 : var3.of(var10, var1.aiming())) {
               if (!var4.contains(var8)) {
                  var4.add(var8);
               }
            }
         }
      }

      return var4;
   }

   public static void refresh() {
      IdentityHashMap<PadBind, List<String>> var0 = new IdentityHashMap<>();

      for (PadBind var2 : ALL) {
         var0.put(var2, List.copyOf(conflicts(var2, var2.chord(), ALL, game)));
      }

      warnings = var0;
   }

   public static int count() {
      return ALL.size();
   }

   public static String id(int var0) {
      return ALL.get(var0).key;
   }

   public static String label(String var0) {
      return find(var0).label;
   }

   public static String tooltip(String var0) {
      return find(var0).tooltip();
   }

   public static String text(String var0) {
      return find(var0).text();
   }

   public static String fallbackText(String var0) {
      PadChord var1 = find(var0).fallback;
      return var1 == null ? "" : var1.text();
   }

   public static String display(String var0, String var1) {
      PadChord var2 = PadChord.parse(var0);
      return var2 == null ? "NONE" : var2.display(var1);
   }

   public static String glyph(String var0, String var1) {
      PadChord var2 = PadChord.parse(var0);
      return var2 == null ? "" : PadChord.glyph(var1, var2.button());
   }

   public static boolean hold(String var0) {
      return find(var0).hold;
   }

   public static boolean aiming(String var0) {
      PadChord var1 = PadChord.parse(var0);
      return var1 != null && var1.aiming();
   }

   public static String withAiming(String var0, boolean var1) {
      PadChord var2 = PadChord.parse(var0);
      return var2 == null ? "" : var2.withAiming(var1).text();
   }

   public static String warning(String var0, String var1) {
      List<String> var2 = conflicts(find(var0), PadChord.parse(var1), ALL, game);
      return var2.isEmpty() ? "" : "Also: " + String.join(", ", var2);
   }

   public static void set(String var0, String var1) {
      choose(find(var0), PadChord.parse(var1));
      SettingsWindow.edited();
   }

   public static void capture(String var0) {
      PadInput.capture(find(var0), false);
   }

   public static boolean capturing(String var0) {
      PadBind var1 = PadInput.capturing();
      return var1 != null && var1.key.equals(var0);
   }

   public static String caught() {
      PadChord var0 = PadInput.caught();
      return var0 == null ? "" : var0.text();
   }

   public static int lookCount() {
      return look().size();
   }

   public static String lookId(int var0) {
      return look().get(var0).key;
   }

   public static String lookLabel(String var0) {
      return findLook(var0).label;
   }

   public static String lookTooltip(String var0) {
      return findLook(var0).tooltip();
   }

   public static boolean lookToggle(String var0) {
      return findLook(var0) instanceof LiveSettings.Toggle;
   }

   public static float lookMin(String var0) {
      return findLook(var0) instanceof LiveSettings.Number var1 ? var1.min : 0.0F;
   }

   public static float lookMax(String var0) {
      return findLook(var0) instanceof LiveSettings.Number var1 ? var1.max : 1.0F;
   }

   public static float lookStep(String var0) {
      return findLook(var0) instanceof LiveSettings.Number var1 ? var1.step : 1.0F;
   }

   public static float lookValue(String var0) {
      LiveSettings.Setting var1 = findLook(var0);
      if (var1 instanceof LiveSettings.Toggle var3) {
         return var3.get() ? 1.0F : 0.0F;
      } else {
         return var1 instanceof LiveSettings.Number var2 ? var2.get() : 0.0F;
      }
   }

   public static void lookSet(String var0, float var1) {
      LiveSettings.Setting var2 = findLook(var0);
      if (var2 instanceof LiveSettings.Toggle var3) {
         var3.set(var1 != 0.0F);
      } else if (var2 instanceof LiveSettings.Number var4) {
         var4.set(var1);
      }

      SettingsWindow.edited();
   }

   private static List<LiveSettings.Setting> look() {
      ArrayList<LiveSettings.Setting> var0 = new ArrayList<>();

      for (LiveSettings.Setting var2 : LiveSettings.all()) {
         if (var2.section.equals("Controller/Look") || var2.section.equals("Controller/Aim assist") || var2.section.equals("Controller/Switching")) {
            var0.add(var2);
         }
      }

      return var0;
   }

   private static LiveSettings.Setting findLook(String var0) {
      for (LiveSettings.Setting var2 : look()) {
         if (var2.key.equals(var0)) {
            return var2;
         }
      }

      throw new IllegalArgumentException("no look setting " + var0);
   }

   private static PadBind find(String var0) {
      for (PadBind var2 : ALL) {
         if (var2.key.equals(var0)) {
            return var2;
         }
      }

      throw new IllegalArgumentException("no pad binding " + var0);
   }

   private PadBinds() {
   }

   public interface Uses {
      List<String> of(int var1, boolean var2);
   }
}