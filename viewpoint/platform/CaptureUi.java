package viewpoint.platform;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import se.krka.kahlua.vm.KahluaTable;
import zombie.ui.UIElement;

public final class CaptureUi {
   private static final int MAX_DEPTH = 64;
   private static final long SECOND_NANOS = 1000000000L;
   private static final CaptureUi.Phase RENDER = new CaptureUi.Phase("render");
   private static final CaptureUi.Phase UPDATE = new CaptureUi.Phase("update");
   private static boolean timing;
   private static Thread owner;
   private static long written;

   public static void renderStarting(UIElement var0) {
      if (timing && Thread.currentThread() == owner) {
         RENDER.enter(var0);
      }
   }

   public static void renderDone(UIElement var0) {
      if (timing && Thread.currentThread() == owner) {
         RENDER.exit(var0);
      }
   }

   public static void updateStarting(UIElement var0) {
      if (timing && Thread.currentThread() == owner) {
         UPDATE.enter(var0);
      }
   }

   public static void updateDone(UIElement var0) {
      if (timing && Thread.currentThread() == owner) {
         UPDATE.exit(var0);
      }
   }

   static void frame(boolean var0) {
      long var1 = System.nanoTime();
      if (var0 != timing) {
         timing = var0;
         owner = Thread.currentThread();
         written = var1;
         RENDER.reset();
         UPDATE.reset();
      } else {
         if (timing && var1 - written >= 1000000000L) {
            written = var1;
            RENDER.write();
            UPDATE.write();
         }
      }
   }

   private CaptureUi() {
   }

   private static final class Phase {
      final String name;
      final UIElement[] elements = new UIElement[64];
      final long[] starts = new long[64];
      final long[] inner = new long[64];
      final boolean[] hidden = new boolean[64];
      final Map<String, long[]> shown = new HashMap<>();
      final Map<String, long[]> unseen = new HashMap<>();
      int depth;

      Phase(String var1) {
         this.name = var1;
      }

      void enter(UIElement var1) {
         if (this.depth < 64) {
            this.elements[this.depth] = var1;
            this.hidden[this.depth] = !var1.isVisible() || this.depth > 0 && this.hidden[this.depth - 1];
            this.inner[this.depth] = 0L;
            this.starts[this.depth] = System.nanoTime();
         }

         this.depth++;
      }

      void exit(UIElement var1) {
         long var2 = System.nanoTime();
         if (this.depth > 64) {
            this.depth--;
         } else {
            int var4 = this.depth - 1;

            while (var4 >= 0 && this.elements[var4] != var1) {
               var4--;
            }

            if (var4 >= 0) {
               long[] var5 = (this.hidden[var4] ? this.unseen : this.shown).computeIfAbsent(type(var1), var0 -> new long[2]);
               var5[0]++;
               var5[1] += var2 - this.starts[var4] - this.inner[var4];
               Arrays.fill(this.elements, var4, this.depth, null);
               this.depth = var4;
               if (var4 > 0) {
                  this.inner[var4 - 1] = this.inner[var4 - 1] + (System.nanoTime() - this.starts[var4]);
               }
            }
         }
      }

      void write() {
         this.write(this.shown, false);
         this.write(this.unseen, true);
      }

      private void write(Map<String, long[]> var1, boolean var2) {
         for (Entry var4 : var1.entrySet()) {
            CaptureEvents.UiTime var5 = new CaptureEvents.UiTime();
            if (!var5.shouldCommit()) {
               break;
            }

            var5.element = (String)var4.getKey();
            var5.phase = this.name;
            var5.hidden = var2;
            var5.calls = (int)((long[])var4.getValue())[0];
            var5.ms = (float)((long[])var4.getValue())[1] * 1.0E-6F;
            var5.commit();
         }

         var1.clear();
      }

      void reset() {
         Arrays.fill(this.elements, null);
         this.depth = 0;
         this.shown.clear();
         this.unseen.clear();
      }

      private static String type(UIElement var0) {
         KahluaTable var1 = var0.getTable();
         return (var1 == null ? null : var1.rawget("Type")) instanceof String var3 ? var3 : var0.getClass().getSimpleName();
      }
   }
}
