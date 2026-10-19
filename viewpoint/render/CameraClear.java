package viewpoint.render;

import viewpoint.platform.GlProgram;

public final class CameraClear {
   public static final float CAMERA_RADIUS = 0.45F;
   public static final float CHEST_RADIUS = 0.85F;
   public static final float SPHERE_IN = 0.45F;
   public static final float SPHERE_OUT = 1.2F;
   static final float EDGE = 0.3F;
   static final float SELF_IN = 0.12F;
   static final float SELF_OUT = 0.3F;
   static final float END = 0.15F;
   private static boolean on;
   private static boolean plants;
   private static float eyeX;
   private static float eyeY;
   private static float eyeZ;
   private static float chestX;
   private static float chestY;
   private static float chestZ;

   public static void frame(boolean var0, boolean var1, float[] var2, float var3, float var4, float var5) {
      on = var0;
      plants = var1;
      eyeX = var2[0];
      eyeY = var2[1];
      eyeZ = var2[2];
      chestX = var3;
      chestY = var4;
      chestZ = var5;
   }

   static void apply(GlProgram var0) {
      if (var0.has("uClearEye")) {
         var0.set("uClearEye", eyeX, eyeY, eyeZ, on ? 1.0F : 0.0F);
         var0.set("uClearChest", chestX, chestY, chestZ, 0.85F);
         var0.set("uClearShape", 0.45F, 0.45F, 1.2F, 0.3F);
         var0.set("uClearSelf", 0.12F, 0.3F, 0.15F, plants ? 1.0F : 0.0F);
      }
   }

   private CameraClear() {
   }
}
