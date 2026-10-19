package viewpoint;

import viewpoint.assets.ModelShaders;
import viewpoint.assets.SoundFiles;
import zombie.core.skinnedmodel.model.Model;

public final class AssetHooks {
   public static boolean skipModelShader(Model var0, String var1) {
      Compat.hit(89);
      return ModelShaders.take(var0, var1);
   }

   public static void modelShaderMade(Model var0, String var1) {
      ModelShaders.made(var0, var1);
   }

   public static boolean skipLoadSound(Object[] var0) {
      Compat.hit(90);
      return var0.length == 1 && var0[0] instanceof String var1 && SoundFiles.take(var1) != 0L;
   }

   public static long loadedSound(Object[] var0, long var1) {
      return var1 == 0L && var0.length == 1 && var0[0] instanceof String var3 ? SoundFiles.decoded(var3) : var1;
   }

   private AssetHooks() {
   }
}
