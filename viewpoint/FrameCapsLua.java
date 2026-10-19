package viewpoint;

import me.zed_0xff.zombie_buddy.Exposer.LuaClass;
import viewpoint.game.FrameCaps;

@LuaClass(name = "Viewpoint.FrameCaps")
public final class FrameCapsLua {
   private FrameCapsLua() {
   }

   public static int gameCap() {
      int var0 = FrameCaps.gameCap();
      return var0 < 0 ? -1 : var0;
   }

   public static void setGameCap(int var0) {
      FrameCaps.setGameCap(var0);
   }

   public static int menuCap() {
      return FrameCaps.menuCap();
   }

   public static void setMenuCap(int var0) {
      FrameCaps.setMenuCap(var0);
   }

   public static boolean active() {
      return FrameCaps.ON;
   }

   public static int most() {
      return 540;
   }

   public static int refreshRate() {
      return FrameCaps.refreshRate();
   }
}
