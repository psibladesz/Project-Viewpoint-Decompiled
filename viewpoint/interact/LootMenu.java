package viewpoint.interact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import viewpoint.core.Frame;
import viewpoint.core.View;
import viewpoint.input.FreeCam;
import viewpoint.input.Look;
import viewpoint.input.PadLook;
import viewpoint.platform.KeyBind;
import viewpoint.platform.KeyInput;
import viewpoint.platform.Keys;
import viewpoint.platform.PadBind;
import viewpoint.platform.PadBinds;
import viewpoint.visibility.Owner;
import viewpoint.world.ChunkCache;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.input.Mouse;
import zombie.inventory.InventoryItem;
import zombie.inventory.ItemContainer;
import zombie.vehicles.BaseVehicle;

public final class LootMenu {
   private static final int NONE = 0;
   private static final int ONE = 1;
   private static final int ALL = 2;
   private static final int OPEN = 3;
   private static final int CLOSE = 4;
   private static final int UP = 5;
   private static final int DOWN = 6;
   private static final int MAX_NOTCHES = 5;
   private static volatile boolean enabled = true;
   private static final LootTargets.Target target = new LootTargets.Target();
   static final LootRows rows = new LootRows();
   static final Set<InventoryItem> asked = Collections.newSetFromMap(new IdentityHashMap<>());
   private static final ArrayList<InventoryItem> taking = new ArrayList<>();
   private static final boolean[] held = new boolean[262];
   private static final boolean[] kept = new boolean[262];
   private static boolean aimed;
   private static boolean loot;
   private static boolean shown;
   private static boolean panelDue;
   private static boolean opened;
   private static boolean cursorFree;
   private static boolean cursorLook;
   private static int pending;
   private static int scroll;
   private static boolean failed;

   public static void snapshot(Frame var0, IsoGameCharacter var1) {
      var0.lootSquare = null;
      var0.lootVehicle = null;
      var0.lootCharacter = null;
      if (!failed) {
         try {
            show(var0, var1);
         } catch (RuntimeException var3) {
            fail();
            var0.lootSquare = null;
            var0.lootVehicle = null;
            var0.lootCharacter = null;
            var0.scene.targetMesh = null;
            System.out.println("[Viewpoint] loot menu off after an error:");
            var3.printStackTrace(System.out);
         }
      }
   }

   private static void show(Frame var0, IsoGameCharacter var1) {
      window(var1);
      IsoPlayer var2 = player(var1);
      LootTargets.Target var3 = var2 == null ? null : LootTargets.aimed(var0, var2, aimed ? target : null);
      if (var3 == null) {
         close();
      } else {
         if (!aimed || !var3.same(target)) {
            target.set(var3);
            aimed = true;
            rows.clear();
            asked.clear();

            for (ItemContainer var5 : target.containers) {
               LootActions.explore(var2, var5);
            }
         }

         InteractActions.update(var2, target, System.nanoTime());
         if (!InteractActions.ready()) {
            pending = 0;
            scroll = 0;
         } else {
            loot = target.kind == 0 || !target.containers.isEmpty();
            if (!loot && InteractActions.count() == 0) {
               pending = 0;
               scroll = 0;
            } else {
               if (var2.getCharacterActions().isEmpty()) {
                  asked.clear();
               }

               rows.refresh(var2, target);
               rows.move(scroll);
               scroll = 0;
               take(var2);
               shown = true;
               panelDue = true;
               var0.lootSquare = target.square;
               var0.lootVehicle = target.kind == 3 ? (BaseVehicle)target.object : null;
               var0.lootCharacter = target.kind != 5 && target.kind != 6 ? null : (IsoGameCharacter)target.object;
               var0.scene.targetMesh = target.square == null ? null : ChunkCache.meshOn(target.square);
               var0.scene.targetOwner = target.square == null ? 0 : LootTargets.owner(target);
               var0.scene.targetSquare = target.square == null ? -1 : Owner.square(var0.scene.targetOwner);
            }
         }
      }
   }

   private static void window(IsoGameCharacter var0) {
      if (pending == 4) {
         pending = 0;
         opened = false;
         cursorLook = true;
         if (var0 instanceof IsoPlayer var1) {
            LootActions.closeWindow(var1);
         }
      }

      if (opened && Look.wantCapture && !cursorFree) {
         opened = false;
      }
   }

   public static boolean keyDown(int var0, boolean var1) {
      int var2 = KeyInput.slot(var0);
      if (var2 >= 0 && (var1 || held[var2])) {
         boolean var3 = var1 && !held[var2];
         held[var2] = var1;
         if (var3) {
            int var4 = asks(var0);
            kept[var2] = var4 != 0;
            pending = var4 != 0 ? var4 : pending;
         }

         kept[var2] = kept[var2] & var1;
         return var1 && !kept[var2];
      } else {
         return var1;
      }
   }

   private static int asks(int var0) {
      if (var0 > 0 && (shown || opened)) {
         KeyBind var1 = KeyInput.menu(var0);
         if (!shown) {
            return var1 == Keys.LOOT_WINDOW ? 4 : 0;
         } else if (loot && var1 == Keys.LOOT_TAKE_ALL) {
            return 2;
         } else if (loot && var1 == Keys.LOOT_WINDOW) {
            return 3;
         } else {
            return var1 == Keys.LOOT_TAKE && interacts() ? 1 : 0;
         }
      } else {
         return 0;
      }
   }

   static boolean padAsks(PadBind var0) {
      return padWhat(var0) != 0;
   }

   static void padAsk(PadBind var0) {
      int var1 = padWhat(var0);
      if (var1 == 5 || var1 == 6) {
         scroll += var1 == 5 ? -1 : 1;
      } else if (var1 != 0) {
         pending = var1;
      }
   }

   private static int padWhat(PadBind var0) {
      if (!shown) {
         return opened && var0 == PadBinds.LOOT_WINDOW ? 4 : 0;
      } else if (var0 == PadBinds.LOOT_UP || var0 == PadBinds.LOOT_DOWN) {
         return var0 == PadBinds.LOOT_UP ? 5 : 6;
      } else if (loot && var0 == PadBinds.LOOT_TAKE_ALL) {
         return 2;
      } else if (loot && var0 == PadBinds.LOOT_WINDOW) {
         return 3;
      } else {
         return var0 == PadBinds.LOOT_TAKE && interacts() ? 1 : 0;
      }
   }

   static boolean interacts() {
      return rows.action() >= 0 || !target.locked && rows.next(asked) != null;
   }

   public static void mouse() {
      boolean[] var0 = Mouse.buttonDownStates;

      for (int var1 = 2; var0 != null && var1 <= 7 && var1 < var0.length; var1++) {
         if (!keyDown(10000 + var1, var0[var1])) {
            var0[var1] = false;
         }
      }

      int var2 = Mouse.getWheelState();
      if (shown && var2 != 0) {
         Mouse.wheelDelta = 0;
         scroll = scroll - Integer.signum(var2) * Math.min(Math.abs(var2), 5);
      }
   }

   public static boolean freeCursor() {
      boolean var0 = cursorFree;
      cursorFree = false;
      return var0;
   }

   public static boolean lookAgain() {
      boolean var0 = cursorLook;
      cursorLook = false;
      return var0;
   }

   public static void setEnabled(boolean var0) {
      enabled = var0;
   }

   static boolean panelDue() {
      boolean var0 = panelDue;
      panelDue = false;
      shown = var0;
      return var0;
   }

   static boolean locked() {
      return target.locked;
   }

   static boolean loot() {
      return loot;
   }

   private static IsoPlayer player(IsoGameCharacter var0) {
      if (enabled && View.enabled && !FreeCam.active && Look.wantCapture && var0 instanceof IsoPlayer var1) {
         boolean var2 = var1 == IsoPlayer.players[0]
            && var1.isLocalPlayer()
            && !var1.isDead()
            && var1.getVehicle() == null
            && !var1.isAiming()
            && (PadLook.bind(var1) < 0 || var1.isJoypadButtonsActive());
         return var2 ? var1 : null;
      } else {
         return null;
      }
   }

   private static void take(IsoPlayer var0) {
      int var1 = pending;
      pending = 0;
      if (var1 == 1 && rows.action() >= 0) {
         InteractActions.run(var0, rows.action());
      } else if (var1 == 3) {
         LootActions.openWindow(var0, target.kind == 0 ? null : target.containers.get(0));
         boolean var3 = PadLook.bind(var0) >= 0;
         opened = !var3;
         cursorFree = !var3;
      } else if ((var1 == 1 || var1 == 2) && !target.locked) {
         if (var1 == 1) {
            InventoryItem var2 = rows.next(asked);
            if (var2 != null) {
               asked.add(var2);
               LootActions.take(var0, var2);
            }
         } else {
            taking.clear();
            rows.all(asked, taking);
            if (!taking.isEmpty()) {
               asked.addAll(taking);
               LootActions.takeAll(var0, taking);
            }

            taking.clear();
         }
      }
   }

   static void fail() {
      failed = true;
      panelDue = false;
      opened = false;
      close();
   }

   private static void close() {
      shown = false;
      loot = false;
      pending = 0;
      scroll = 0;
      if (aimed) {
         aimed = false;
         target.clear();
         rows.clear();
         asked.clear();
         InteractActions.forget();
      }
   }

   private LootMenu() {
   }
}
