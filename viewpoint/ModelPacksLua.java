package viewpoint;

import me.zed_0xff.zombie_buddy.Exposer.LuaClass;
import viewpoint.packs.ModelPacks;

@LuaClass(name = "Viewpoint.ModelPacks")
public final class ModelPacksLua {
   public static boolean register(String var0, String var1) {
      return ModelPacks.register(var0, var1);
   }

   private ModelPacksLua() {
   }
}
