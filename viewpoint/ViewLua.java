package viewpoint;

import me.zed_0xff.zombie_buddy.Exposer.LuaClass;

@LuaClass(name = "Viewpoint.View")
public final class ViewLua {
   public static String next() {
      return FP.nextView();
   }

   public static void cycle() {
      FP.cycleView();
   }

   private ViewLua() {
   }
}
