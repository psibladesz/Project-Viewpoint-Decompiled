package viewpoint.interact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import se.krka.kahlua.vm.KahluaTable;
import zombie.characters.IsoPlayer;
import zombie.characters.CharacterTimedActions.BaseAction;
import zombie.inventory.ItemContainer;
import zombie.inventory.types.InventoryContainer;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.sprite.IsoSprite;
import zombie.vehicles.BaseVehicle;
import zombie.vehicles.VehiclePart;

final class InteractActions {
   private static final long REST = 150000000L;
   private static final long PERSON_REST = 400000000L;
   private static final float FUEL_REACH = 4.0F;
   private static final ArrayList<String> labels = new ArrayList<>();
   private static final ArrayList<Boolean> enabled = new ArrayList<>();
   private static final HashSet<String> told = new HashSet<>();
   private static final IdentityHashMap<IsoObject, IsoSprite> passed = new IdentityHashMap<>();
   private static String title;
   private static int version;
   private static IsoGridSquare passedFrom;
   private static int passedCarrying;
   private static Object aimed;
   private static IsoSprite gatheredSprite;
   private static VehiclePart gatheredPart;
   private static VehiclePart gatheredWindow;
   private static IsoPlayer runner;
   private static long aimedAt;
   private static long gatheredAt;
   private static int runAt = -1;
   private static boolean gathered;
   private static boolean ran;
   private static boolean gatheredNear;
   private static boolean tried;

   static void update(IsoPlayer var0, LootTargets.Target var1, long var2) {
      IsoObject var4 = var1 == null ? null : (var1.kind == 0 ? null : var1.object);
      if (var4 != aimed) {
         aimed = var4;
         aimedAt = var2;
         tried = false;
         ran = false;
         gathered = false;
         clear();
      }

      if (var4 != null && var2 - aimedAt >= (var1.kind == 6 ? 400000000L : 150000000L)) {
         IsoSprite var5 = !(var4 instanceof BaseVehicle) && var4 instanceof IsoObject var6 ? var6.getSprite() : null;
         boolean var8 = ran && var0.getCharacterActions().isEmpty();
         if (!gathered || var8 || var5 != gatheredSprite || var4 instanceof BaseVehicle var7 && var2 - gatheredAt >= 150000000L && movedRound(var0, var7)) {
            gathered = gather(var0, var1);
            tried = true;
            gatheredSprite = var5;
            gatheredAt = var2;
            ran = false;
            gatheredWhere(var0, var4);
            if (!gathered) {
               aimedAt = var2;
            }
         }
      }
   }

   private static boolean movedRound(IsoPlayer var0, BaseVehicle var1) {
      return var1.getUseablePart(var0) != gatheredPart || var1.getClosestWindow(var0) != gatheredWindow || var0.DistToProper(var1) < 4.0F != gatheredNear;
   }

   private static void gatheredWhere(IsoPlayer var0, Object var1) {
      BaseVehicle var2 = var1 instanceof BaseVehicle var3 ? var3 : null;
      gatheredPart = var2 == null ? null : var2.getUseablePart(var0);
      gatheredWindow = var2 == null ? null : var2.getClosestWindow(var0);
      gatheredNear = var2 != null && var0.DistToProper(var2) < 4.0F;
   }

   static boolean passes(IsoObject var0) {
      IsoSprite var1 = passed.get(var0);
      return var1 != null && var1 == var0.getSprite();
   }

   static void standing(IsoPlayer var0, IsoGridSquare var1) {
      if (!passed.isEmpty()) {
         if (var1 != passedFrom) {
            passed.keySet().removeIf(var1x -> !near(var1x.getSquare(), var1));
            passedFrom = var1;
         }

         if (var0.getCharacterActions().isEmpty() && carried(var0.getInventory()) != passedCarrying) {
            passed.clear();
         }
      }
   }

   static void forget() {
      aimed = null;
      gatheredSprite = null;
      gatheredWindow = null;
      gatheredPart = null;
      tried = false;
      ran = false;
      gathered = false;
      clear();
   }

   static boolean ready() {
      return aimed == null || tried;
   }

   static void run(IsoPlayer var0, int var1) {
      if (var1 >= 0 && var1 < labels.size() && enabled.get(var1)) {
         LootActions.call("ViewpointInteract.run", var0, (double)(var1 + 1));
         ran = true;
         runner = var0;
         runAt = var1;
      }
   }

   static float progress(int var0) {
      if (var0 == runAt && !runner.getCharacterActions().isEmpty()) {
         BaseAction var1 = (BaseAction)runner.getCharacterActions().get(0);
         return var1.isPathfinding() ? 0.0F : Math.min(1.0F, var1.getJobDelta());
      } else {
         return 0.0F;
      }
   }

   static int count() {
      return labels.size();
   }

   static String label(int var0) {
      return labels.get(var0);
   }

   static boolean enabled(int var0) {
      return enabled.get(var0);
   }

   static String title() {
      return title;
   }

   static int version() {
      return version;
   }

   private static boolean gather(IsoPlayer var0, LootTargets.Target var1) {
      if (!(
              (
                      var1.object instanceof BaseVehicle var3
                              ? LootActions.call("ViewpointInteract.harvestVehicle", var0, var3)
                              : LootActions.call("ViewpointInteract.harvest", var0, var1.object)
              ) instanceof KahluaTable var12
      )) {
         return true;
      } else {
         String var13 = var1.object.getClass().getSimpleName();
         Object var5 = var12.rawget("why");
         if (var5 != null) {
            tell(var13 + var5, var13 + ": none gathered now, " + var5);
            return false;
         }

         clear();
         title = var12.rawget("title") instanceof String var7 ? var7 : null;
         KahluaTable var14 = (KahluaTable)var12.rawget("labels");
         KahluaTable var8 = (KahluaTable)var12.rawget("enabled");

         for (int var9 = 1; var14 != null && var9 <= var14.len(); var9++) {
            labels.add(String.valueOf(var14.rawget(var9)));
            enabled.add(var8 != null && Boolean.TRUE.equals(var8.rawget(var9)));
         }

         int var15 = var12.rawget("seen") instanceof Double var10 ? var10.intValue() : 0;
         tell(
                 (String)(labels.isEmpty() ? var13 + " none" : var13),
                 var13 + ": " + labels.size() + " actions of its menu's " + var15 + " options, first " + labels
         );
         if (labels.isEmpty() && var1.kind == 4) {
            if (passed.isEmpty()) {
               passedCarrying = carried(var0.getInventory());
            }

            passed.put(var1.object, var1.object.getSprite());
         }

         return true;
      }
   }

   private static boolean near(IsoGridSquare var0, IsoGridSquare var1) {
      return var0 != null && var0.z == var1.z && Math.abs(var0.x - var1.x) <= 1 && Math.abs(var0.y - var1.y) <= 1;
   }

   private static int carried(ItemContainer var0) {
      ArrayList var1 = var0.getItems();
      int var2 = var1.size();

      for (int var3 = 0; var3 < var1.size(); var3++) {
         if (var1.get(var3) instanceof InventoryContainer var4 && var4.getInventory() != null) {
            var2 += carried(var4.getInventory());
         }
      }

      return var2;
   }

   private static void tell(String var0, String var1) {
      if (told.add(var0)) {
         System.out.println("[Viewpoint] interaction menu: " + var1);
      }
   }

   private static void clear() {
      labels.clear();
      enabled.clear();
      title = null;
      runner = null;
      runAt = -1;
      version++;
   }

   private InteractActions() {
   }
}