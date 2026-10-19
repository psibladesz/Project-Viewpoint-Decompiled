package viewpoint.world;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import zombie.core.PerformanceSettings;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.fboRenderChunk.FBORenderSnow;

final class SnowGrids {
   private static final Method UPDATE;

   static void made(IsoCell var0, IsoChunk var1, int var2) {
      int var3 = var0.getSnowTarget();
      if (var3 > 0 && PerformanceSettings.fboRenderChunk) {
         update(var1, var2, var3);
         update(var0.getChunk(var1.wx - 1, var1.wy), var2, var3);
         update(var0.getChunk(var1.wx + 1, var1.wy), var2, var3);
         update(var0.getChunk(var1.wx, var1.wy - 1), var2, var3);
         update(var0.getChunk(var1.wx, var1.wy + 1), var2, var3);
      }
   }

   private static void update(IsoChunk var0, int var1, int var2) {
      if (var0 != null) {
         try {
            UPDATE.invoke(FBORenderSnow.getInstance(), var0, var1, var2);
         } catch (IllegalAccessException var4) {
            throw new IllegalStateException(var4);
         } catch (InvocationTargetException var5) {
            throw new IllegalStateException("the game's snow grid failed", var5.getCause());
         }
      }
   }

   private SnowGrids() {
   }

   static {
      try {
         UPDATE = FBORenderSnow.class.getDeclaredMethod("updateSnow", IsoChunk.class, int.class, int.class);
         UPDATE.setAccessible(true);
      } catch (ReflectiveOperationException var1) {
         throw new IllegalStateException("FBORenderSnow.updateSnow not found", var1);
      }
   }
}
