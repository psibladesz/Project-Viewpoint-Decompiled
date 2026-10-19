package viewpoint.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record PadChord(int button, List<Integer> held, boolean aiming) {
   public static final int COUNT = 17;
   public static final int GUIDE = 10;
   public static final int LT = 15;
   public static final int RT = 16;
   public static final List<String> STYLES = List.of("XBOX", "PS4", "STEAMDECK");
   private static final String AIM = "AIM";
   private static final String NONE = "NONE";
   private static final List<String> NAMES = List.of(
      "A", "B", "X", "Y", "L3", "R3", "LB", "RB", "BACK", "START", "GUIDE", "LEFT", "RIGHT", "UP", "DOWN", "LT", "RT"
   );
   private static final List<String> GLYPHS = List.of(
      "A",
      "B",
      "X",
      "Y",
      "AnalogueL",
      "AnalogueR",
      "LB",
      "RB",
      "View",
      "Menu",
      "",
      "DPad_Left",
      "DPad_Right",
      "DPad_Up",
      "DPad_Down",
      "LeftTrigger",
      "RightTrigger"
   );
   private static final List<List<String>> SHOWN = List.of(
      List.of("A", "B", "X", "Y", "LS", "RS", "LB", "RB", "View", "Menu", "Guide", "Left", "Right", "Up", "Down", "LT", "RT"),
      List.of("Cross", "Circle", "Square", "Triangle", "L3", "R3", "L1", "R1", "Share", "Options", "PS", "Left", "Right", "Up", "Down", "L2", "R2"),
      List.of("A", "B", "X", "Y", "L3", "R3", "L1", "R1", "View", "Menu", "Steam", "Left", "Right", "Up", "Down", "L2", "R2")
   );

   public PadChord(int button, List<Integer> held, boolean aiming) {
      held = List.copyOf(held);
      this.button = button;
      this.held = held;
      this.aiming = aiming;
   }

   static PadChord of(int var0, List<Integer> var1, boolean var2) {
      ArrayList var3 = new ArrayList();

      for (int var5 : var1) {
         if (var5 != var0 && !var3.contains(var5)) {
            var3.add(var5);
         }
      }

      var3.sort(null);
      return new PadChord(var0, var3, var2);
   }

   public static PadChord parse(String var0) {
      String var1 = var0.trim().toUpperCase(Locale.ROOT);
      if (!var1.isEmpty() && !var1.equals("NONE")) {
         String[] var2 = var1.split("\\+", -1);
         ArrayList var3 = new ArrayList();
         boolean var4 = false;

         for (int var5 = 0; var5 < var2.length - 1; var5++) {
            String var6 = var2[var5].trim();
            if (var6.equals("AIM")) {
               var4 = true;
            } else {
               var3.add(index(var6));
            }
         }

         return of(index(var2[var2.length - 1].trim()), var3, var4);
      } else {
         return null;
      }
   }

   public PadChord withAiming(boolean var1) {
      return new PadChord(this.button, this.held, var1);
   }

   public String text() {
      StringBuilder var1 = new StringBuilder(this.aiming ? "AIM+" : "");

      for (int var3 : this.held) {
         var1.append(NAMES.get(var3)).append('+');
      }

      return var1.append(NAMES.get(this.button)).toString();
   }

   public String display(String var1) {
      List var2 = SHOWN.get(Math.max(0, STYLES.indexOf(var1)));
      StringBuilder var3 = new StringBuilder();

      for (int var5 : this.held) {
         var3.append((String)var2.get(var5)).append(" + ");
      }

      return var3.append((String)var2.get(this.button)).append(this.aiming ? " while aiming" : "").toString();
   }

   public static String glyph(String var0, int var1) {
      String var2 = STYLES.contains(var0) ? var0 : STYLES.get(0);
      return "media/ui/controller/" + var2 + "_" + GLYPHS.get(var1) + ".png";
   }

   public List<Integer> buttons() {
      ArrayList var1 = new ArrayList<>(this.held);
      var1.add(this.button);
      return var1;
   }

   boolean fires(boolean[] var1, boolean[] var2, boolean var3) {
      if (var1[this.button] && (!this.aiming || var3)) {
         for (int var5 : this.held) {
            if (!var2[var5]) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   boolean holds(boolean[] var1) {
      if (!var1[this.button]) {
         return false;
      }

      for (int var3 : this.held) {
         if (!var1[var3]) {
            return false;
         }
      }

      return true;
   }

   boolean outranks(PadChord var1) {
      int var2 = this.held.size() + (this.aiming ? 1 : 0);
      int var3 = var1.held.size() + (var1.aiming ? 1 : 0);
      return this.button == var1.button && var2 > var3 && this.held.containsAll(var1.held) && (this.aiming || !var1.aiming);
   }

   boolean clashes(PadChord var1) {
      return this.button == var1.button && !this.outranks(var1) && !var1.outranks(this);
   }

   private static int index(String var0) {
      int var1 = NAMES.indexOf(var0);
      if (var1 >= 0 && var1 != 10) {
         return var1;
      } else {
         throw new IllegalArgumentException("no pad button is called " + var0);
      }
   }
}
