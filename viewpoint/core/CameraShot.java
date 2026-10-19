package viewpoint.core;

public final class CameraShot {
   public static final int LAYERS = 16;
   public static final int DEBUG_LINES = 16;
   public boolean on;
   public boolean cut;
   public boolean seated;
   public boolean kicked;
   public float pivotX;
   public float pivotY;
   public float pivotZ;
   public float fromLeft;
   public float fromTop;
   public float distance;
   public float side;
   public float limit;
   public float addYaw;
   public float addPitch;
   public float addRoll;
   public float addBack;
   public float addUp;
   public float addRight;
   public float fovY;
   public float chestX;
   public float chestY;
   public float chestZ;
   public boolean clearModels;
   public boolean clearPlants;
   public float handYaw;
   public float handPitch;
   public float trauma;
   public float aim;
   public final float[] weights = new float[16];
   public float trailAcross;
   public float trailUp;
   public final float[] debugLines = new float[96];
   public final int[] debugColours = new int[16];
   public int debugLineCount;
   public String[] debugText = new String[0];

   public void clear() {
      this.on = this.cut = this.seated = this.kicked = false;
      this.addYaw = this.addPitch = this.addRoll = this.addBack = this.addUp = this.addRight = 0.0F;
      this.trauma = this.aim = 0.0F;
      this.clearModels = this.clearPlants = false;
   }
}
