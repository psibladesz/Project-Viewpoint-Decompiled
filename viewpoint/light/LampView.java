package viewpoint.light;

final class LampView {
   static boolean reaches(float var0, float var1, float var2, double var3, double var5) {
      double var7 = Math.sqrt(var0 * var0 + var1 * var1);
      if (!(var7 <= var2) && !(var5 >= Math.PI)) {
         double var9 = Math.acos(Math.max(-1.0, Math.min(1.0, (var0 * Math.cos(var3) + var1 * Math.sin(var3)) / var7)));
         return var9 - Math.asin(var2 / var7) <= var5;
      } else {
         return true;
      }
   }

   private LampView() {
   }
}
