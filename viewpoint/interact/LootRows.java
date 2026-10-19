package viewpoint.interact;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import zombie.characters.IsoPlayer;
import zombie.core.Translator;
import zombie.core.textures.Texture;
import zombie.inventory.InventoryItem;
import zombie.inventory.ItemContainer;
import zombie.iso.IsoGridSquare;
import zombie.iso.objects.IsoWorldInventoryObject;

final class LootRows {
   private static final Comparator<LootRows.Row> BY_NAME = Comparator.comparing(var0 -> var0.name, String.CASE_INSENSITIVE_ORDER);
   private static final String EMPTY_ICON = "media/ui/LootableMaps/map_garbage.png";
   private static String empty;
   private static String interact;
   final ArrayList<LootRows.Row> rows = new ArrayList<>();
   int selected = -1;
   private boolean chosen;
   private final ArrayList<LootRows.Row> spare = new ArrayList<>();
   private final HashMap<String, LootRows.Row> byName = new HashMap<>();
   private final ArrayList<InventoryItem> ground = new ArrayList<>();
   private long signature;
   private boolean built;

   void clear() {
      this.recycle();
      this.selected = -1;
      this.built = false;
      this.chosen = false;
   }

   void refresh(IsoPlayer var1, LootTargets.Target var2) {
      long var3 = signature(var2);
      if (!this.built || var3 != this.signature) {
         String var5 = this.chosen && this.selected >= 0 && this.selected < this.rows.size() ? this.rows.get(this.selected).name : null;
         int var6 = this.chosen ? this.selected : -1;
         this.build(var1, var2);
         this.signature = var3;
         this.built = true;
         this.selected = this.select(var5, var6);
      }
   }

   void move(int var1) {
      for (int var2 = 0; var2 < Math.abs(var1); var2++) {
         int var3 = this.nextSelectable(this.selected, var1 > 0 ? 1 : -1);
         if (var3 < 0) {
            return;
         }

         this.selected = var3;
         this.chosen = true;
      }
   }

   int action() {
      return this.selected >= 0 && this.selected < this.rows.size() ? this.rows.get(this.selected).action : -1;
   }

   InventoryItem next(Set<InventoryItem> var1) {
      if (this.selected >= 0 && this.selected < this.rows.size()) {
         for (InventoryItem var3 : this.rows.get(this.selected).items) {
            if (!var1.contains(var3)) {
               return var3;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   void all(Set<InventoryItem> var1, List<InventoryItem> var2) {
      for (LootRows.Row var4 : this.rows) {
         for (InventoryItem var6 : var4.items) {
            if (!var1.contains(var6)) {
               var2.add(var6);
            }
         }
      }
   }

   static int asked(LootRows.Row var0, Set<InventoryItem> var1) {
      byte var2 = 0;

      for (InventoryItem var4 : var0.items) {
         var2 += var1.contains(var4) ? 1 : 0;
      }

      return var2;
   }

   static String title(ItemContainer var0) {
      String var1 = var0.getVehiclePart() == null ? null : Translator.getTextOrNull("IGUI_VehiclePart" + var0.getType(), new Object[0]);
      if (var1 != null) {
         return var1;
      }

      if (var0.getCustomName() != null) {
         return var0.getCustomName();
      }

      String var2 = Translator.getTextOrNull("IGUI_ContainerTitle_" + var0.getType(), new Object[0]);
      return var2 != null ? var2 : var0.getType();
   }

   static String groundTitle() {
      String var0 = Translator.getTextOrNull("IGUI_ContainerTitle_floor", new Object[0]);
      return var0 != null ? var0 : "Ground";
   }

   private void build(IsoPlayer var1, LootTargets.Target var2) {
      this.recycle();
      if (var2.kind == 0) {
         this.heading(groundTitle());
         this.group(var1, this.groundItems(var2.square));
         this.ground.clear();
      }

      for (ItemContainer var4 : var2.containers) {
         this.heading(title(var4));
         this.group(var1, var4.getItems());
      }

      this.actions();
   }

   private void actions() {
      if (InteractActions.count() != 0) {
         if (interact == null) {
            interact = Translator.getText("IGUI_Controller_Interact", new Object[0]);
         }

         this.heading(InteractActions.title() != null ? InteractActions.title() : interact);

         for (int var1 = 0; var1 < InteractActions.count(); var1++) {
            LootRows.Row var2 = this.row();
            var2.name = InteractActions.label(var1);
            var2.action = var1;
            var2.enabled = InteractActions.enabled(var1);
            this.rows.add(var2);
         }
      }
   }

   private void heading(String var1) {
      LootRows.Row var2 = this.row();
      var2.heading = var1;
      this.rows.add(var2);
   }

   private void group(IsoPlayer var1, List<InventoryItem> var2) {
      int var3 = this.rows.size();
      this.byName.clear();

      for (int var4 = 0; var4 < var2.size(); var4++) {
         InventoryItem var5 = (InventoryItem)var2.get(var4);
         if (var5 != null && !var5.isHidden()) {
            String var6 = var5.getName(var1);
            LootRows.Row var7 = this.byName.get(var6);
            if (var7 == null) {
               var7 = this.row();
               var7.name = var6;
               var7.icon = var5.getTex();
               this.byName.put(var6, var7);
               this.rows.add(var7);
            }

            var7.items.add(var5);
            var7.weight = var7.weight + var5.getUnequippedWeight();
         }
      }

      this.rows.subList(var3, this.rows.size()).sort(BY_NAME);
      this.byName.clear();
      if (this.rows.size() == var3) {
         if (empty == null) {
            empty = Translator.getText("ContextMenu_Empty", new Object[0]);
         }

         LootRows.Row var8 = this.row();
         var8.name = empty;
         var8.icon = Texture.getSharedTexture("media/ui/LootableMaps/map_garbage.png");
         var8.enabled = false;
         this.rows.add(var8);
      }
   }

   private List<InventoryItem> groundItems(IsoGridSquare var1) {
      this.ground.clear();
      ArrayList var2 = var1.getWorldObjects();

      for (int var3 = 0; var3 < var2.size(); var3++) {
         if (((IsoWorldInventoryObject)var2.get(var3)).getItem() != null) {
            this.ground.add(((IsoWorldInventoryObject)var2.get(var3)).getItem());
         }
      }

      return this.ground;
   }

   private static long signature(LootTargets.Target var0) {
      long var1 = var0.kind * 31L + InteractActions.version();
      if (var0.kind == 0) {
         ArrayList var7 = var0.square.getWorldObjects();

         for (int var8 = 0; var8 < var7.size(); var8++) {
            InventoryItem var9 = ((IsoWorldInventoryObject)var7.get(var8)).getItem();
            var1 = var1 * 31L + (var9 == null ? 0 : var9.getID());
         }

         return var1 * 31L + var7.size();
      } else {
         for (ItemContainer var4 : var0.containers) {
            ArrayList var5 = var4.getItems();

            for (int var6 = 0; var6 < var5.size(); var6++) {
               var1 = var1 * 31L + ((InventoryItem)var5.get(var6)).getID();
            }

            var1 = var1 * 31L + var5.size();
         }

         return var1;
      }
   }

   int select(String var1, int var2) {
      for (int var3 = 0; var1 != null && var3 < this.rows.size(); var3++) {
         if (this.rows.get(var3).selectable() && var1.equals(this.rows.get(var3).name)) {
            return var3;
         }
      }

      int var5 = Math.max(0, Math.min(var2, this.rows.size() - 1));
      if (var5 < this.rows.size() && this.rows.get(var5).selectable()) {
         return var5;
      }

      int var4 = this.nextSelectable(var5, 1);
      return var4 >= 0 ? var4 : this.nextSelectable(var5, -1);
   }

   private int nextSelectable(int var1, int var2) {
      for (int var3 = var1 + var2; var3 >= 0 && var3 < this.rows.size(); var3 += var2) {
         if (this.rows.get(var3).selectable()) {
            return var3;
         }
      }

      return -1;
   }

   private LootRows.Row row() {
      LootRows.Row var1 = this.spare.isEmpty() ? new LootRows.Row() : this.spare.remove(this.spare.size() - 1);
      var1.heading = null;
      var1.name = null;
      var1.icon = null;
      var1.weight = 0.0F;
      var1.action = -1;
      var1.enabled = true;
      var1.items.clear();
      var1.label = null;
      var1.weightText = null;
      return var1;
   }

   private void recycle() {
      for (LootRows.Row var2 : this.rows) {
         var2.items.clear();
         var2.icon = null;
         this.spare.add(var2);
      }

      this.rows.clear();
   }

   static final class Row {
      String heading;
      String name;
      Texture icon;
      float weight;
      int action = -1;
      boolean enabled = true;
      final ArrayList<InventoryItem> items = new ArrayList<>();
      String label;
      String weightText;
      int labelLeft;

      boolean selectable() {
         return this.heading == null && this.enabled;
      }

      boolean plain() {
         return this.heading == null && this.items.isEmpty();
      }
   }
}
