package viewpoint.light;

import viewpoint.platform.ShaderPacks;
import viewpoint.render.SceneData;
import zombie.GameTime;

public final class Daylight {
   private static int level;
   private static boolean fromAir;

   public static void prepare(SceneData var0) {
      int var1 = GameTime.getInstance().getSkyLightLevel();
      fromAir = ShaderPacks.drawn().pipeline.atmosphere;
      float[] var2 = var0.airSkyLevel;
      level = fromAir ? GridTexels.pack(var2[0], var2[1], var2[2]) : var1;
   }

   public static void snapshot(SceneData var0) {
      float var1 = (level & 0xFF) / 255.0F;
      float var2 = (level >> 8 & 0xFF) / 255.0F;
      float var3 = (level >> 16 & 0xFF) / 255.0F;
      if (fromAir) {
         var1 = var0.airSkyLevel[0];
         var2 = var0.airSkyLevel[1];
         var3 = var0.airSkyLevel[2];
      }

      var0.daylightKnown = true;
      var0.daylightR = var1;
      var0.daylightG = var2;
      var0.daylightB = var3;
      var0.skyR = var1;
      var0.skyG = var2;
      var0.skyB = var3;
   }

   static int skyLevel() {
      return level;
   }

   private Daylight() {
   }
}
