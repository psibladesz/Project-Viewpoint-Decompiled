package viewpoint.environment;

import viewpoint.core.Frame;
import viewpoint.render.SceneData;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoPuddles;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.weather.ClimateManager;

public final class Weather {
   private static final int ROWS_PER_FRAME = 8;
   private static final float EYE_SKY_PORCH = 0.8F;
   private static final float EYE_SKY_INSIDE = 0.5F;
   public static final float WIND_MOST = 6.0F;
   private static final float SHEETS_CALM = 0.35F;
   private static final float SHEETS_WINDY = 0.8F;
   private static final float GUSTS_CALM = 0.3F;
   private static final float MAX_STEP_SECONDS = 0.25F;
   private static final byte[] exposure = new byte[4096];
   private static final byte[] rooms = new byte[exposure.length];
   private static final byte[] water = new byte[exposure.length];
   private static final byte[] oldExposure = new byte[exposure.length];
   private static final byte[] oldRooms = new byte[exposure.length];
   private static final byte[] oldWater = new byte[exposure.length];
   private static final byte[] shore = new byte[exposure.length];
   private static boolean waterChanged = true;
   private static int gridX;
   private static int gridY;
   private static int gridZ = Integer.MIN_VALUE;
   private static int nextRow;
   private static double windCarriedX;
   private static double windCarriedZ;
   private static long carriedNanos;

   public static void snapshot(Frame var0, IsoCell var1) {
      SceneData var2 = var0.scene;
      ClimateManager var3 = ClimateManager.getInstance();
      var2.rain = clamp01(var3.getRainIntensity());
      var2.snow = clamp01(var3.getSnowIntensity());
      float var4 = clamp01(var3.getWindIntensity());
      float var5 = 6.0F * var4;
      var2.windX = -((float)Math.cos(var3.getWindAngleRadians())) * var5;
      var2.windZ = -((float)Math.sin(var3.getWindAngleRadians())) * var5;
      var2.sheets = sheets(var4);
      var2.gusts = gusts(var4);
      carry(var2);
      Lightning.snapshot(var2, var0.camX, var0.camY);
      IsoPuddles var6 = IsoPuddles.getInstance();
      var2.puddles = clamp01(var6.getPuddlesSizeFinalValue());
      var2.wetGround = clamp01(var6.getWetGroundFinalValue());
      byte var7 = 64;
      int var8 = var7 / 2;
      int var9 = (int)Math.floor(var0.camX) - var8;
      int var10 = (int)Math.floor(var0.camY) - var8;
      int var11 = (int)Math.floor(var0.camZ);
      var2.exposureAX = var0.camX - var9;
      var2.exposureAY = var0.camY - var10;
      move(var1, var9, var10, var11);

      for (int var12 = 0; var12 < 8; nextRow = (nextRow + 1) % var7) {
         for (int var13 = 0; var13 < var7; var13++) {
            read(var1, var13, nextRow);
         }

         var12++;
      }

      System.arraycopy(exposure, 0, var2.exposure, 0, exposure.length);
      System.arraycopy(rooms, 0, var2.rooms, 0, rooms.length);
      if (waterChanged) {
         waterChanged = false;
         ShoreField.compute(water, shore);
      }

      System.arraycopy(shore, 0, var2.water, 0, shore.length);
      byte var14 = exposure[var8 * var7 + var8];
      var2.eyeSky = var14 == -1 ? 1.0F : (var14 == 100 ? 0.8F : 0.5F);
   }

   public static float sheets(float var0) {
      return 0.35F + 0.45000002F * var0;
   }

   public static float gusts(float var0) {
      return 0.3F + 0.7F * var0;
   }

   private static void carry(SceneData var0) {
      long var1 = System.nanoTime();
      float var3 = carriedNanos == 0L ? 0.0F : (float)(var1 - carriedNanos) * 1.0E-9F;
      carriedNanos = var1;
      if (var3 > 0.0F && var3 <= 0.25F) {
         windCarriedX = windCarriedX + var0.windX * var3;
         windCarriedZ = windCarriedZ + var0.windZ * var3;
      }

      var0.windCarriedX = windCarriedX;
      var0.windCarriedZ = windCarriedZ;
   }

   private static void move(IsoCell var0, int var1, int var2, int var3) {
      byte var4 = 64;
      int var5 = var1 - gridX;
      int var6 = var2 - gridY;
      if (var3 != gridZ || var5 != 0 || var6 != 0) {
         boolean var7 = var3 == gridZ && Math.abs(var5) < var4 && Math.abs(var6) < var4;
         System.arraycopy(exposure, 0, oldExposure, 0, exposure.length);
         System.arraycopy(rooms, 0, oldRooms, 0, rooms.length);
         System.arraycopy(water, 0, oldWater, 0, water.length);
         waterChanged = true;
         gridX = var1;
         gridY = var2;
         gridZ = var3;

         for (int var8 = 0; var8 < var4; var8++) {
            for (int var9 = 0; var9 < var4; var9++) {
               int var10 = var9 + var5;
               int var11 = var8 + var6;
               if (var7 && var10 >= 0 && var11 >= 0 && var10 < var4 && var11 < var4) {
                  exposure[var8 * var4 + var9] = oldExposure[var11 * var4 + var10];
                  rooms[var8 * var4 + var9] = oldRooms[var11 * var4 + var10];
                  water[var8 * var4 + var9] = oldWater[var11 * var4 + var10];
               } else {
                  read(var0, var9, var8);
               }
            }
         }
      }
   }

   private static void read(IsoCell var0, int var1, int var2) {
      int var3 = var2 * 64 + var1;
      IsoGridSquare var4 = var0.getGridSquare(gridX + var1, gridY + var2, gridZ);
      boolean var5 = var4 != null && var4.isOutside();
      exposure[var3] = (byte)(var4 != null && (!var5 || var4.haveRoof) ? (var5 ? 100 : 0) : -1);
      rooms[var3] = var4 == null ? -1 : room(var4.getRoomID());
      IsoGridSquare var6 = gridZ == 0 ? var4 : var0.getGridSquare(gridX + var1, gridY + var2, 0);
      int var7 = var6 != null && var6.getProperties().has(IsoFlagType.water) ? -1 : 0;
      waterChanged = waterChanged | water[var3] != var7;
      water[var3] = (byte)var7;
   }

   private static byte room(long var0) {
      if (var0 < 0L) {
         return 0;
      }

      int var2 = (int)(var0 ^ var0 >>> 32) * -1640531535;
      return (byte)(1 + (var2 >>> 8) % 254);
   }

   private static float clamp01(float var0) {
      return var0 < 0.0F ? 0.0F : Math.min(var0, 1.0F);
   }

   private Weather() {
   }
}
