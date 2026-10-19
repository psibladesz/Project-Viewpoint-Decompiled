package viewpoint.light;

public final class VehicleLamps {
   static final float INSET = 0.1F;

   public static void lens(float var0, boolean var1, float var2, float var3, float var4, float var5, float var6, float var7, float[] var8) {
      var8[0] = Math.signum(var0) * var6 * var2 * 0.5F;
      var8[1] = var7 * var3 - var5;
      var8[2] = (var1 ? -1.0F : 1.0F) * (var4 * 0.5F - 0.1F);
   }

   public static void move(float var0, boolean var1, float[] var2, float[] var3) {
      var3[0] += Math.signum(var0) * var2[0];
      var3[1] += var2[1];
      var3[2] += (var1 ? -1.0F : 1.0F) * var2[2];
   }

   public static float overFloor(float var0, float var1) {
      return (var0 - (float)Math.floor(var0 + 0.001F)) * var1;
   }

   private VehicleLamps() {
   }
}
