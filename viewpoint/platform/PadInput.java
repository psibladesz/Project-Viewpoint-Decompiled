package viewpoint.platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import zombie.input.JoypadButton;
import zombie.input.JoypadManager;
import zombie.input.JoypadManager.Joypad;

public final class PadInput {
   private static final JoypadButton[] BUTTONS = JoypadButton.values();
   static final float TRIGGER_DOWN = -0.8F;
   private static final boolean[] down = new boolean[17];
   private static final boolean[] pressed = new boolean[17];
   private static final boolean[] fresh = new boolean[17];
   private static final boolean[] read = new boolean[17];
   private static final boolean[] alone = new boolean[17];
   private static boolean aiming;
   private static boolean buttonsActive;
   private static final boolean[] tookHeld = new boolean[17];
   private static int tookPad = -1;
   private static volatile PadBind target;
   private static volatile boolean chooses;
   private static volatile PadChord taken;

   public static void update(int var0, boolean var1, boolean var2) {
      sample(var0, read);
      letGo(var0, read);
      step(read, var1, var2);
   }

   public static void took(int var0) {
      sample(var0, read);
      took(var0, read);
   }

   static void took(int var0, boolean[] var1) {
      System.arraycopy(var1, 0, tookHeld, 0, 17);
      tookPad = var0;
   }

   static void letGo(int var0, boolean[] var1) {
      for (int var2 = 0; var2 < 17; var2++) {
         tookHeld[var2] = tookHeld[var2] & (var0 == tookPad && var1[var2]);
         var1[var2] &= !tookHeld[var2];
      }
   }

   public static boolean gameSees(int var0, int var1, boolean var2) {
      return var2 && (var0 < 0 || var1 != tookPad || !tookHeld[var0]);
   }

   public static boolean isDown(int var0) {
      return down[var0];
   }

   public static void sample(int var0, boolean[] var1) {
      Joypad var2 = var0 >= 0 ? JoypadManager.instance.getFromControllerID(var0) : null;

      for (int var3 = 0; var3 < 17; var3++) {
         var1[var3] = var0 >= 0 && var3 != 10 && down(var2, var0, var3);
      }
   }

   private static boolean down(Joypad var0, int var1, int var2) {
      return var2 < 15 ? BUTTONS[var2].isDown(var1) : var0 != null && (var2 == 15 ? var0.getLTValue() : var0.getRTValue()) > -0.8F;
   }

   public static PadBind firing(int var0, boolean[] var1, boolean var2, List<PadBind> var3) {
      if (target != null) {
         return null;
      }

      Arrays.fill(alone, false);
      alone[var0] = true;

      for (PadBind var5 : var3) {
         if (fires(var5, PadBinds.all(), alone, var1, var2) != null) {
            return var5;
         }
      }

      return null;
   }

   static void step(boolean[] var0, boolean var1, boolean var2) {
      aiming = var1;
      buttonsActive = var2;
      PadBind var3 = target;

      for (int var4 = 0; var4 < 17; var4++) {
         pressed[var4] = var0[var4] && !down[var4];
         fresh[var4] = var3 != null && (fresh[var4] || pressed[var4]);
      }

      for (int var5 = 0; var3 != null && var5 < 17; var5++) {
         if (down[var5] && !var0[var5] && fresh[var5]) {
            take(var3, var5, var0);
            break;
         }
      }

      System.arraycopy(var0, 0, down, 0, 17);
   }

   static boolean pressed(PadBind var0) {
      return target == null && buttonsActive && fires(var0, PadBinds.all(), pressed, down, aiming) != null;
   }

   static boolean held(PadBind var0) {
      PadChord var1 = var0.chord();
      return target == null && buttonsActive && var1 != null && var1.holds(down);
   }

   static PadChord fires(PadBind var0, List<PadBind> var1, boolean[] var2, boolean[] var3, boolean var4) {
      PadChord var5 = var0.chord();
      if (var5 != null && var5.fires(var2, var3, var4)) {
         for (PadBind var7 : var1) {
            PadChord var8 = var7.chord();
            if (var7 != var0 && var8 != null && var8.outranks(var5) && var8.fires(var2, var3, var4)) {
               return null;
            }
         }

         return var5;
      } else {
         return null;
      }
   }

   private static void take(PadBind var0, int var1, boolean[] var2) {
      target = null;
      Arrays.fill(fresh, false);
      ArrayList var3 = new ArrayList();

      for (int var4 = 0; var4 < 17; var4++) {
         if (var2[var4] && var4 != var1) {
            var3.add(var4);
         }
      }

      PadChord var6 = var0.chord() != null ? var0.chord() : var0.fallback;
      PadChord var5 = PadChord.of(var1, var3, var6 != null && var6.aiming());
      if (chooses) {
         PadBinds.choose(var0, var5);
         SettingsWindow.edited();
      } else {
         taken = var5;
      }
   }

   public static void capture(PadBind var0, boolean var1) {
      taken = null;
      chooses = var1;
      target = var0;
   }

   public static void cancel() {
      target = null;
   }

   public static void windowClosed() {
      if (chooses) {
         target = null;
      }
   }

   public static void gameLoaded() {
      if (!chooses) {
         target = null;
      }
   }

   public static PadBind capturing() {
      return target;
   }

   public static PadChord caught() {
      PadChord var0 = taken;
      taken = null;
      return var0;
   }

   private PadInput() {
   }
}
