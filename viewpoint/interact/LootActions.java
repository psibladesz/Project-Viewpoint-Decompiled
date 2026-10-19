package viewpoint.interact;

import java.util.ArrayList;
import zombie.Lua.LuaManager;
import zombie.characters.IsoPlayer;
import zombie.inventory.InventoryItem;
import zombie.inventory.ItemContainer;

final class LootActions {
   private static boolean reported;

   static void explore(IsoPlayer var0, ItemContainer var1) {
      call("ViewpointLoot.explore", var0, var1);
   }

   static void take(IsoPlayer var0, InventoryItem var1) {
      call("ViewpointLoot.take", var0, var1);
   }

   static void takeAll(IsoPlayer var0, ArrayList<InventoryItem> var1) {
      call("ViewpointLoot.takeAll", var0, var1);
   }

   static void openWindow(IsoPlayer var0, ItemContainer var1) {
      call("ViewpointLoot.openWindow", var0, var1);
   }

   static void closeWindow(IsoPlayer var0) {
      call("ViewpointLoot.closeWindow", var0);
   }

   static Object call(String var0, Object... var1) {
      Object var2 = LuaManager.getFunctionObject(var0);
      if (var2 == null) {
         report(var0, "not found");
         return null;
      } else {
         Object[] var3 = LuaManager.caller.pcall(LuaManager.thread, var2, var1);
         if (var3 != null && var3.length > 0 && Boolean.FALSE.equals(var3[0])) {
            report(var0, var3.length > 1 ? String.valueOf(var3[1]) : "failed");
            return null;
         } else {
            return var3 != null && var3.length > 1 ? var3[1] : null;
         }
      }
   }

   private static void report(String var0, String var1) {
      if (!reported) {
         reported = true;
         System.out.println("[Viewpoint] loot and interaction menu: " + var0 + " " + var1);
      }
   }

   private LootActions() {
   }
}
