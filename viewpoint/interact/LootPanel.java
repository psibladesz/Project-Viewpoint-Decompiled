package viewpoint.interact;

import java.util.Locale;
import viewpoint.core.View;
import viewpoint.input.PadLook;
import viewpoint.platform.KeyBind;
import viewpoint.platform.Keys;
import viewpoint.platform.PadBind;
import viewpoint.platform.PadBinds;
import viewpoint.platform.PadChord;
import zombie.characters.IsoPlayer;
import zombie.core.Core;
import zombie.core.SpriteRenderer;
import zombie.core.Translator;
import zombie.ui.TextManager;
import zombie.ui.UIFont;

public final class LootPanel {
   private static final UIFont FONT = UIFont.Small;
   private static final int GAP = 48;
   private static final int PAD = 8;
   private static final int MIN_WIDTH = 220;
   private static final int MAX_WIDTH = 360;
   private static final int COLUMN = 16;
   private static final String CUT = "...";
   private static final int SHOWN = 10;
   private static final float BACK = 0.6F;
   private static final float LIT = 0.18F;
   private static final float DIM = 0.45F;
   private static final float FILL = 0.45F;
   private static final float[] TITLE = new float[]{1.0F, 0.84F, 0.47F};
   private static final float[] TEXT = new float[]{0.86F, 0.86F, 0.86F};
   private static final float[] HINT = new float[]{0.7F, 0.7F, 0.7F};
   private static final float[] GREEN = new float[]{0.3F, 0.85F, 0.4F};
   private static final int PANEL_ROUND = 3;
   private static final int ROW_ROUND = 5;
   private static String hint = "";
   private static Object hintTake;
   private static Object hintAll;
   private static Object hintWindow;
   private static String hintStyle = "";
   private static boolean hintPress;
   private static boolean hintAction;
   private static boolean hintLoot;

   public static void draw() {
      if (LootMenu.panelDue()) {
         try {
            render();
         } catch (RuntimeException var1) {
            LootMenu.fail();
            System.out.println("[Viewpoint] loot menu off after a panel error: " + var1);
         }
      }
   }

   private static void render() {
      TextManager var0 = TextManager.instance;
      LootRows var1 = LootMenu.rows;
      int var2 = var0.getFontHeight(FONT) + 6;
      int var3 = var2 - 4;
      int var4 = Math.max(0, Math.min(var1.selected - 5, var1.rows.size() - 10));
      int var5 = var4;
      String var6 = hint(var1);
      int var7 = Math.max(220, var0.MeasureStringX(FONT, var6) + 16);

      while (var5 < var1.rows.size() && var5 - var4 < 10) {
         var7 = Math.max(var7, rowWidth(var0, var1.rows.get(var5), var3) + 16);
         var5++;
      }

      String var8 = LootMenu.locked() ? "Locked" : null;
      int var9 = var5 - var4 + (var8 != null ? 1 : 0) + (var6.isEmpty() ? 0 : 1);
      int var10 = var9 * var2 + 16;
      int var11 = Core.getInstance().getScreenWidth();
      int var12 = Core.getInstance().getScreenHeight();
      int var13 = View.shoulder < 0 ? Math.max(0, var11 / 2 - 48 - var7) : Math.min(var11 / 2 + 48, var11 - var7);
      int var14 = Math.max(0, var12 / 2 - var10 / 2);
      rounded(var13, var14, var7, var10, var2 / 3, 0.0F, 0.0F, 0.0F, 0.6F);
      int var15 = var14 + 8;

      for (int var16 = var4; var16 < var5; var16++) {
         row(var0, var1, var16, var13, var15, var7, var3);
         var15 += var2;
      }

      if (var8 != null) {
         var0.DrawString(FONT, var13 + 8 + var3 + 8, var15, var8, TEXT[0], TEXT[1], TEXT[2], 0.45F);
         var15 += var2;
      }

      var0.DrawString(FONT, var13 + 8, var15, var6, HINT[0], HINT[1], HINT[2], 1.0);
   }

   private static void row(TextManager var0, LootRows var1, int var2, int var3, int var4, int var5, int var6) {
      LootRows.Row var7 = var1.rows.get(var2);
      if (var7.heading != null) {
         var0.DrawString(FONT, var3 + 8, var4, label(var0, var7, var6), TITLE[0], TITLE[1], TITLE[2], 1.0);
      } else {
         int var8 = var5 - 4;
         int var9 = var6 + 4;
         int var10 = var9 / 5;
         if (var2 == var1.selected) {
            rounded(var3 + 2, var4 - 2, var8, var9, var10, 1.0F, 1.0F, 1.0F, 0.18F);
         }

         float var11 = progress(var7);
         if (var11 > 0.0F) {
            rounded(var3 + 2, var4 - 2, Math.round(var8 * var11), var9, var10, GREEN[0], GREEN[1], GREEN[2], 0.45F);
         }

         if (var7.plain()) {
            float var15 = var7.enabled ? 1.0F : 0.45F;
            if (var7.icon != null) {
               SpriteRenderer.instance.renderi(var7.icon, var3 + 8, var4, var6, var6, 1.0F, 1.0F, 1.0F, var15, null);
            }

            int var16 = var3 + 8 + besides(var0, var7, var6);
            var0.DrawString(FONT, var16, var4, label(var0, var7, var6), TEXT[0], TEXT[1], TEXT[2], var15);
         } else {
            float var12 = left(var7) <= 0 && !(var11 > 0.0F) ? 0.45F : 1.0F;
            if (var7.icon != null) {
               SpriteRenderer.instance.renderi(var7.icon, var3 + 8, var4, var6, var6, 1.0F, 1.0F, 1.0F, var12, null);
            }

            var0.DrawString(FONT, var3 + 8 + var6 + 8, var4, label(var0, var7, var6), TEXT[0], TEXT[1], TEXT[2], var12);
            String var13 = weight(var7);
            int var14 = var3 + var5 - 8 - var0.MeasureStringX(FONT, var13);
            var0.DrawString(FONT, var14, var4, var13, HINT[0], HINT[1], HINT[2], var12);
         }
      }
   }

   private static int rowWidth(TextManager var0, LootRows.Row var1, int var2) {
      return besides(var0, var1, var2) + var0.MeasureStringX(FONT, label(var0, var1, var2));
   }

   private static int besides(TextManager var0, LootRows.Row var1, int var2) {
      if (var1.heading != null || var1.action >= 0) {
         return 0;
      } else {
         return var1.plain() ? var2 + 8 : var2 + 8 + 16 + var0.MeasureStringX(FONT, weight(var1));
      }
   }

   private static float progress(LootRows.Row var0) {
      if (var0.action >= 0) {
         return InteractActions.progress(var0.action);
      }

      float var1 = 0.0F;

      for (int var2 = 0; var2 < var0.items.size(); var2++) {
         var1 = Math.max(var1, var0.items.get(var2).getJobDelta());
      }

      return Math.min(var1, 1.0F);
   }

   private static void rounded(int var0, int var1, int var2, int var3, int var4, float var5, float var6, float var7, float var8) {
      if (var2 > 0 && var3 > 0) {
         int var9 = Math.min(var4, Math.min(var2, var3) / 2);
         SpriteRenderer.instance.renderi(null, var0, var1 + var9, var2, var3 - 2 * var9, var5, var6, var7, var8, null);

         for (int var10 = 0; var10 < var9; var10++) {
            float var11 = var9 - var10 - 0.5F;
            float var12 = var9 - (float)Math.sqrt(var9 * var9 - var11 * var11);
            int var13 = (int)Math.ceil(var12);
            float var14 = var13 - var12;
            strip(var0, var1 + var10, var2, var13, var14, var5, var6, var7, var8);
            strip(var0, var1 + var3 - 1 - var10, var2, var13, var14, var5, var6, var7, var8);
         }
      }
   }

   private static void strip(int var0, int var1, int var2, int var3, float var4, float var5, float var6, float var7, float var8) {
      SpriteRenderer.instance.renderi(null, var0 + var3, var1, var2 - 2 * var3, 1, var5, var6, var7, var8, null);
      if (var3 > 0 && var4 > 0.0F) {
         SpriteRenderer.instance.renderi(null, var0 + var3 - 1, var1, 1, 1, var5, var6, var7, var8 * var4, null);
         SpriteRenderer.instance.renderi(null, var0 + var2 - var3, var1, 1, 1, var5, var6, var7, var8 * var4, null);
      }
   }

   private static int left(LootRows.Row var0) {
      return var0.items.size() - LootRows.asked(var0, LootMenu.asked);
   }

   private static String label(TextManager var0, LootRows.Row var1, int var2) {
      int var3 = left(var1);
      if (var1.label == null || var1.labelLeft != var3) {
         String var4 = var1.items.size() > 1 ? " (" + var3 + ")" : "";
         int var5 = 344 - besides(var0, var1, var2) - var0.MeasureStringX(FONT, var4);
         String var6 = var1.heading != null ? var1.heading : var1.name;
         var1.labelLeft = var3;
         var1.label = fit(var0, var6.replace('\n', ' '), var5) + var4;
      }

      return var1.label;
   }

   private static String fit(TextManager var0, String var1, int var2) {
      if (var0.MeasureStringX(FONT, var1) <= var2) {
         return var1;
      }

      for (int var3 = var1.length() - 1; var3 > 0; var3--) {
         String var4 = var1.substring(0, var3).stripTrailing() + "...";
         if (var0.MeasureStringX(FONT, var4) <= var2) {
            return var4;
         }
      }

      return "...";
   }

   private static String weight(LootRows.Row var0) {
      if (var0.weightText == null) {
         var0.weightText = String.format(Locale.ROOT, "%.2f", var0.weight);
      }

      return var0.weightText;
   }

   private static String hint(LootRows var0) {
      boolean var1 = PadLook.bind(IsoPlayer.players[0]) >= 0;
      String var2 = var1 ? Core.getInstance().getOptionControllerButtonStyleString() : "";
      Object var3 = var1 ? PadBinds.LOOT_TAKE.chord() : Keys.LOOT_TAKE.chords();
      Object var4 = var1 ? PadBinds.LOOT_TAKE_ALL.chord() : Keys.LOOT_TAKE_ALL.chords();
      Object var5 = var1 ? PadBinds.LOOT_WINDOW.chord() : Keys.LOOT_WINDOW.chords();
      boolean var6 = LootMenu.interacts();
      boolean var7 = var0.action() >= 0;
      boolean var8 = LootMenu.loot();
      if (var3 != hintTake || var4 != hintAll || var5 != hintWindow || !var2.equals(hintStyle) || var6 != hintPress || var7 != hintAction || var8 != hintLoot) {
         hintTake = var3;
         hintAll = var4;
         hintWindow = var5;
         hintStyle = var2;
         hintPress = var6;
         hintAction = var7;
         hintLoot = var8;
         String var9 = var7 ? "IGUI_Controller_Interact" : "ContextMenu_Grab";
         hint = !var6 ? "" : (var1 ? button(PadBinds.LOOT_TAKE, var2, var9) : key(Keys.LOOT_TAKE, var9));
         if (var8) {
            hint = join(
               join(hint, var1 ? button(PadBinds.LOOT_TAKE_ALL, var2, "IGUI_invpage_Loot_all") : key(Keys.LOOT_TAKE_ALL, "IGUI_invpage_Loot_all")),
               var1 ? button(PadBinds.LOOT_WINDOW, var2, "IGUI_Controller_Loot") : key(Keys.LOOT_WINDOW, "IGUI_Controller_Loot")
            );
         }
      }

      return hint;
   }

   private static String key(KeyBind var0, String var1) {
      return var0.bound() ? "[" + var0.display() + "] " + Translator.getText(var1, new Object[0]) : "";
   }

   private static String button(PadBind var0, String var1, String var2) {
      PadChord var3 = var0.chord();
      return var3 == null ? "" : "[" + PadBinds.display(var3.text(), var1) + "] " + Translator.getText(var2, new Object[0]);
   }

   private static String join(String var0, String var1) {
      return !var0.isEmpty() && !var1.isEmpty() ? var0 + "   " + var1 : var0 + var1;
   }

   private LootPanel() {
   }
}
