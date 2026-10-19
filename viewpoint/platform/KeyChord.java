package viewpoint.platform;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import org.lwjglx.input.Keyboard;

public record KeyChord(int key, List<Integer> held) {
   static final int ESCAPE = 1;
   static final int LEFT_SHIFT = 42;
   static final int RIGHT_SHIFT = 54;
   static final int LEFT_CTRL = 29;
   static final int RIGHT_CTRL = 157;
   static final int LEFT_ALT = 56;
   static final int RIGHT_ALT = 184;
   public static final int MOUSE = 10000;
   public static final int FIRST_BUTTON = 2;
   public static final int LAST_BUTTON = 7;
   private static final int MIDDLE = 2;
   private static final String NONE = "NONE";
   private static final String BUTTON = "MOUSE";
   private static final String[] NAMES = new String[256];
   private static final Map<String, Integer> CODES = new HashMap<>();
   private static final Comparator<Integer> ORDER;

   public KeyChord(int key, List<Integer> held) {
      held = List.copyOf(held);
      this.key = key;
      this.held = held;
   }

   static KeyChord of(int var0, List<Integer> var1) {
      ArrayList var2 = new ArrayList();

      for (int var4 : var1) {
         int var5 = canonical(var4);
         if (var5 != canonical(var0) && !var2.contains(var5)) {
            var2.add(var5);
         }
      }

      var2.sort(ORDER);
      return new KeyChord(var0, var2);
   }

   public boolean heldDown(IntPredicate var1) {
      for (int var3 : this.held) {
         if (!down(var3, var1)) {
            return false;
         }
      }

      return true;
   }

   public List<Integer> keys() {
      ArrayList var1 = new ArrayList();
      var1.add(this.key);

      for (int var3 : this.held) {
         var1.add(var3);
         if (otherSide(var3) != 0) {
            var1.add(otherSide(var3));
         }
      }

      return var1;
   }

   boolean outranks(KeyChord var1) {
      return this.key == var1.key && this.held.size() > var1.held.size() && this.held.containsAll(var1.held);
   }

   String text() {
      StringBuilder var1 = new StringBuilder();

      for (int var3 : this.held) {
         var1.append(heldName(var3)).append('+');
      }

      return var1.append(name(this.key)).toString();
   }

   String display(IntFunction<String> var1) {
      StringBuilder var2 = new StringBuilder();

      for (int var4 : this.held) {
         var2.append(modifier(var4) ? heldName(var4) : (String)var1.apply(var4)).append(" + ");
      }

      return var2.append((String)var1.apply(this.key)).toString();
   }

   static String text(List<KeyChord> var0) {
      StringBuilder var1 = new StringBuilder();

      for (KeyChord var3 : var0) {
         var1.append(var1.length() == 0 ? "" : ", ").append(var3.text());
      }

      return var1.toString();
   }

   static String display(List<KeyChord> var0, IntFunction<String> var1) {
      StringBuilder var2 = new StringBuilder();

      for (KeyChord var4 : var0) {
         var2.append(var2.length() == 0 ? "" : " / ").append(var4.display(var1));
      }

      return var2.toString();
   }

   static List<KeyChord> parse(String var0) {
      ArrayList var1 = new ArrayList();

      for (String var5 : var0.split(",")) {
         String var6 = var5.trim().toUpperCase(Locale.ROOT);
         if (!var6.isEmpty() && !var6.equals("NONE")) {
            ArrayList var7 = new ArrayList();

            for (String var11 : var6.split("\\+", -1)) {
               var7.add(code(var11.trim()));
            }

            KeyChord var12 = of((Integer)var7.get(var7.size() - 1), var7.subList(0, var7.size() - 1));
            if (!var1.contains(var12)) {
               var1.add(var12);
            }
         }
      }

      return List.copyOf(var1);
   }

   public static boolean button(int var0) {
      return var0 >= 10002 && var0 <= 10007;
   }

   static String gameName(int var0) {
      if (button(var0)) {
         return var0 - 10000 == 2 ? "Middle mouse" : "Mouse " + (var0 - 10000 + 1);
      }

      String var1 = var0 > 0 && var0 < NAMES.length ? Keyboard.getKeyName(var0) : null;
      return var1 != null && !var1.isEmpty() ? var1 : name(var0);
   }

   static String latinName(int var0) {
      String var1 = gameName(var0);

      for (int var2 = 0; var2 < var1.length(); var2++) {
         if (var1.charAt(var2) > 255) {
            return name(var0);
         }
      }

      return var1;
   }

   static String name(int var0) {
      if (button(var0)) {
         return "MOUSE" + (var0 - 10000 + 1);
      }

      String var1 = var0 > 0 && var0 < NAMES.length ? NAMES[var0] : null;
      return var1 != null ? var1 : Integer.toString(var0);
   }

   static boolean modifier(int var0) {
      return rank(var0) < 3;
   }

   static int canonical(int var0) {
      return var0 == 54 ? 42 : (var0 == 157 ? 29 : (var0 == 184 ? 56 : var0));
   }

   static boolean down(int var0, IntPredicate var1) {
      return var1.test(var0) || var0 == 42 && var1.test(54) || var0 == 29 && var1.test(157) || var0 == 56 && var1.test(184);
   }

   static int otherSide(int var0) {
      return var0 == 42 ? 54 : (var0 == 29 ? 157 : (var0 == 56 ? 184 : 0));
   }

   private static String heldName(int var0) {
      return switch (var0) {
         case 29 -> "CTRL";
         case 42 -> "SHIFT";
         case 56 -> "ALT";
         default -> name(var0);
      };
   }

   private static int rank(int var0) {
      return switch (var0) {
         case 29 -> 1;
         case 42 -> 0;
         case 56 -> 2;
         default -> 3;
      };
   }

   private static int code(String var0) {
      Integer var1 = CODES.get(var0);
      String var2 = var0.startsWith("MOUSE") ? var0.substring("MOUSE".length()) : var0;
      if (var1 == null && !var2.isEmpty() && var2.length() < 6 && var2.chars().allMatch(Character::isDigit)) {
         var1 = var0.startsWith("MOUSE") ? 10000 + Integer.parseInt(var2) - 1 : Integer.parseInt(var2);
      }

      if (var1 != null && (button(var1) || var1 > 0 && var1 < NAMES.length)) {
         return var1;
      } else {
         throw new IllegalArgumentException("no key is called " + var0);
      }
   }

   private static int constant(Field var0) {
      try {
         return var0.getInt(null);
      } catch (IllegalAccessException var2) {
         return 0;
      }
   }

   static {
      for (Field var3 : Keyboard.class.getFields()) {
         int var4 = var3.getModifiers();
         String var5 = var3.getName();
         if (Modifier.isStatic(var4) && var3.getType() == int.class && var5.startsWith("KEY_") && !var5.endsWith("WIN")) {
            int var6 = constant(var3);
            if (var6 > 0 && var6 < NAMES.length) {
               NAMES[var6] = var5.substring(4);
               CODES.put(var5.substring(4), var6);
            }
         }
      }

      CODES.putAll(Map.of("SHIFT", 42, "CTRL", 29, "ALT", 56, "LALT", 56, "RALT", 184, "PAGEUP", 201, "PAGEDOWN", 209, "ENTER", 28, "BACKSPACE", 14));
      ORDER = Comparator.comparingInt(KeyChord::rank).thenComparingInt(Integer::intValue);
   }
}
