package viewpoint;

import me.zed_0xff.zombie_buddy.Exposer.LuaClass;
import viewpoint.interact.LootMenu;

@LuaClass(name = "Viewpoint.Loot")
public final class LootLua {
   public static void setEnabled(boolean var0) {
      LootMenu.setEnabled(var0);
   }

   private LootLua() {
   }
}
