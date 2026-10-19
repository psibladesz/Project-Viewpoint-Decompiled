package viewpoint;

import me.zed_0xff.zombie_buddy.Exposer.LuaClass;
import viewpoint.interact.MouseTargets;

@LuaClass(name = "Viewpoint.Mouse")
public final class MouseLua {
   public static Double worldX() {
      return MouseTargets.on() ? MouseTargets.world(true) : null;
   }

   public static Double worldY() {
      return MouseTargets.on() ? MouseTargets.world(false) : null;
   }

   private MouseLua() {
   }
}
