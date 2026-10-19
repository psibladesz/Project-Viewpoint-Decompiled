package viewpoint.platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import org.lwjglx.input.Keyboard;
import zombie.core.Core;
import zombie.input.GameKeyboard;
import zombie.input.Mouse;
import zombie.ui.UITextEntryInterface;

public final class KeyInput {
   public static final int SLOTS = 262;
   private static final boolean[] raw = new boolean[262];
   private static final boolean[] hidden = new boolean[262];
   private static final boolean[] fresh = new boolean[262];
   private static final boolean[] passing = new boolean[262];
   private static final boolean[] latched = new boolean[262];
   private static final boolean[] buttonDown = new boolean[8];
   private static final boolean[] buttonWas = new boolean[8];
   private static int passingFor = -1;
   private static volatile KeyBind target;
   private static volatile boolean appending;

   public static int slot(int var0) {
      if (var0 > 0 && var0 < 256) {
         return var0;
      } else {
         return KeyChord.button(var0) ? 256 + var0 - 10000 - 2 : -1;
      }
   }

   private static int code(int var0) {
      return var0 < 256 ? var0 : var0 - 256 + 10000 + 2;
   }

   public static boolean keyDown(int var0, boolean var1) {
      int var2 = slot(var0);
      if (var2 < 0) {
         return var1;
      }

      boolean var3 = raw[var2];
      if (var1 && !var3 && !othersDown(var2)) {
         Arrays.fill(latched, false);
      }

      boolean var4 = !var1 && var3 && waits(var0);
      latched[var2] = latched[var2] | var4;
      raw[var2] = var1;
      KeyBind var5 = target;
      if (var5 != null && var1 && !var3) {
         fresh[var2] = true;
      } else if (var5 != null && !var1 && var3 && fresh[var2] && !var4) {
         take(var5, var0);
      }

      fresh[var2] = fresh[var2] & var1;
      hidden[var2] = var1 && (hidden[var2] || var5 != null);
      return var1 && !hidden[var2];
   }

   public static void mouseButtons() {
      boolean[] var0 = Mouse.buttonDownStates;

      for (int var1 = 2; var1 <= 7; var1++) {
         boolean var2 = var0 != null && var1 < var0.length;
         if (var2 && !keyDown(10000 + var1, var0[var1])) {
            var0[var1] = false;
         }

         buttonWas[var1] = buttonDown[var1];
         buttonDown[var1] = var2 && Mouse.isButtonDownUICheck(var1);
      }
   }

   private static boolean gameDown(int var0) {
      return KeyChord.button(var0) ? buttonDown[var0 - 10000] : GameKeyboard.isKeyDown(var0);
   }

   private static boolean gamePressed(int var0) {
      if (!KeyChord.button(var0)) {
         return GameKeyboard.isKeyPressed(var0);
      }

      int var1 = var0 - 10000;
      return buttonDown[var1] && !buttonWas[var1];
   }

   public static boolean physicallyDown(int var0) {
      return var0 > 0 && var0 < 256 ? Keyboard.isKeyDown(var0) : KeyChord.button(var0) && org.lwjglx.input.Mouse.isButtonDown(var0 - 10000);
   }

   static boolean pressed(KeyBind var0) {
      if (var0.kind == KeyBind.Kind.PRESS && target == null && GameKeyboard.doLuaKeyPressed) {
         KeyChord var1 = fires(var0, Keys.all(), Settings.dev, KeyInput::gamePressed, KeyInput::gameDown);
         if (var1 == null) {
            return false;
         }

         GameKeyboard.eatKeyPress(var1.key());
         return true;
      } else {
         return false;
      }
   }

   public static KeyBind menu(int var0) {
      UITextEntryInterface var1 = Core.currentTextEntryBox;
      if (target == null && (var1 == null || !var1.isDoingTextEntry() || KeyChord.button(var0))) {
         for (KeyBind var3 : Keys.all()) {
            if (var3.kind == KeyBind.Kind.MENU && fires(var3, Keys.all(), Settings.dev, var1x -> var1x == var0, KeyInput::isDown) != null) {
               return var3;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static KeyChord fires(KeyBind var0, List<KeyBind> var1, boolean var2, IntPredicate var3, IntPredicate var4) {
      if ((!var0.dev || var2) && var0.usable()) {
         for (KeyChord var6 : var0.chords()) {
            if (var3.test(var6.key()) && var6.heldDown(var4) && !outranked(var6, var0.kind, var1, var2, var4)) {
               return var6;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean outranked(KeyChord var0, KeyBind.Kind var1, List<KeyBind> var2, boolean var3, IntPredicate var4) {
      for (KeyBind var6 : var2) {
         if (var6.kind == var1 && (!var6.dev || var3)) {
            for (KeyChord var8 : var6.chords()) {
               if (var8.outranks(var0) && var8.heldDown(var4)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean reachesGameFlying(int var0) {
      int var1 = slot(var0);
      if (var1 < 0) {
         return false;
      }

      if (passingFor != KeyBind.changes()) {
         passes();
      }

      return passing[var1];
   }

   private static void passes() {
      passingFor = KeyBind.changes();
      Arrays.fill(passing, false);
      passing[1] = true;

      for (KeyBind var1 : Keys.all()) {
         if (var1.kind == KeyBind.Kind.PRESS) {
            for (KeyChord var3 : var1.chords()) {
               pass(var3.key());

               for (int var5 : var3.held()) {
                  pass(var5);
                  pass(KeyChord.otherSide(var5));
               }
            }
         }
      }
   }

   private static void pass(int var0) {
      int var1 = slot(var0);
      if (var1 >= 0) {
         passing[var1] = true;
      }
   }

   public static String captured(int var0) {
      return slot(var0) >= 0 ? heldWith(var0).text() : "";
   }

   private static void take(KeyBind var0, int var1) {
      target = null;
      Arrays.fill(fresh, false);
      if (var1 == 1) {
         System.out.println("[Viewpoint] key " + var0.key + " left as it was");
      } else {
         KeyChord var2 = heldWith(var1);
         ArrayList var3 = new ArrayList<>(appending ? var0.chords() : List.of());
         if (!var3.contains(var2)) {
            var3.add(var2);
         }

         Keys.choose(var0, var3);
         SettingsWindow.edited();
      }
   }

   private static KeyChord heldWith(int var0) {
      ArrayList var1 = new ArrayList();

      for (int var2 = 1; var2 < raw.length; var2++) {
         if ((raw[var2] || latched[var2]) && code(var2) != var0) {
            var1.add(code(var2));
         }
      }

      return KeyChord.of(var0, var1);
   }

   public static boolean waits(int var0) {
      int var1 = slot(var0);
      return var1 >= 0 && KeyChord.modifier(KeyChord.canonical(var0)) && othersDown(var1);
   }

   private static boolean othersDown(int var0) {
      for (int var1 = 1; var1 < raw.length; var1++) {
         if (var1 != var0 && raw[var1] && !KeyChord.modifier(KeyChord.canonical(code(var1)))) {
            return true;
         }
      }

      return false;
   }

   private static boolean isDown(int var0) {
      int var1 = slot(var0);
      return var1 >= 0 && raw[var1];
   }

   static void capture(KeyBind var0, boolean var1) {
      appending = var1;
      target = var0;
   }

   static void cancel() {
      target = null;
   }

   static KeyBind capturing() {
      return target;
   }

   private KeyInput() {
   }
}
