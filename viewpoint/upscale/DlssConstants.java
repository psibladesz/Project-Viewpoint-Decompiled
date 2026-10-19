package viewpoint.upscale;

final class DlssConstants implements AutoCloseable {
   private static final int SIZE = 456;
   private static final int VIEW_TO_CLIP = 32;
   private static final int CLIP_TO_VIEW = 96;
   private static final int LENS = 160;
   private static final int TO_PREVIOUS = 224;
   private static final int FROM_PREVIOUS = 288;
   private static final int JITTER = 352;
   private static final int MOTION_SCALE = 360;
   private static final int PINHOLE = 368;
   private static final int POSITION = 376;
   private static final int NEAR = 424;
   private static final int FAR = 428;
   private static final int FOV = 432;
   private static final int ASPECT = 436;
   private static final int MOTION_INVALID = 440;
   private static final int DEPTH_INVERTED = 444;
   private static final int CAMERA_MOTION = 445;
   private static final int MOTION_3D = 446;
   private static final int RESET = 447;
   private static final int ORTHOGRAPHIC = 448;
   private static final int DILATED = 449;
   private static final int JITTERED = 450;
   private static final int SEPARATION = 452;
   private static final float INVALID = Float.MAX_VALUE;
   private static final float LEAST_SEPARATION = 40.0F;
   private final NativeStruct values = NativeStruct.streamline(456, -590128425, 20042, 19373, -6265385846498510082L, 2);

   long address() {
      return this.values.address;
   }

   void fill(UpscaleFrame var1, int var2, int var3) {
      float[] var4 = var1.matrices();
      this.matrix(32, var4, 0);
      this.matrix(96, var4, 16);
      this.matrix(224, var4, 32);
      this.matrix(288, var4, 48);

      for (int var5 = 0; var5 < 4; var5++) {
         this.values.f32(160 + (var5 * 4 + var5) * 4, 1.0F);
      }

      for (int var6 = 0; var6 < 12; var6++) {
         this.values.f32(376 + var6 * 4, var1.camera()[var6]);
      }

      this.values
         .f32(352, var1.jitterX())
         .f32(356, var1.jitterY())
         .f32(360, 1.0F)
         .f32(364, 1.0F)
         .f32(368, 0.0F)
         .f32(372, 0.0F)
         .f32(424, var1.near())
         .f32(428, var1.far())
         .f32(432, var1.fovY())
         .f32(436, (float)var2 / var3)
         .f32(440, Float.MAX_VALUE)
         .bool(444, false)
         .bool(445, true)
         .bool(446, false)
         .bool(447, var1.reset())
         .bool(448, false)
         .bool(449, false)
         .bool(450, false)
         .f32(452, 40.0F);
   }

   @Override
   public void close() {
      this.values.close();
   }

   private void matrix(int var1, float[] var2, int var3) {
      for (int var4 = 0; var4 < 16; var4++) {
         this.values.f32(var1 + var4 * 4, var2[var3 + var4]);
      }
   }
}
