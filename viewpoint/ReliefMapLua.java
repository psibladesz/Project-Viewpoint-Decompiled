package viewpoint;

import me.zed_0xff.zombie_buddy.Exposer.LuaClass;
import viewpoint.far.Relief;

@LuaClass(name = "Viewpoint.ReliefMap")
public final class ReliefMapLua {
   public static String file() {
      return Relief.mapImage();
   }

   public static int alpha() {
      return Relief.mapAlpha();
   }

   private ReliefMapLua() {
   }
}
