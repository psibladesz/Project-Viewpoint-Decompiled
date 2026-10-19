package viewpoint.render;

import java.util.Arrays;
import org.joml.Matrix4f;
import zombie.core.textures.TextureID;

public final class SceneData {
   public static final byte VISIBLE = 1;
   public static final byte CASTS_SHADOW = 2;
   public static final byte NEAR = 4;
   public static final byte CASTS_FAR = 8;
   public static final byte CASTS_MID = 16;
   public static final byte RAIN = 64;
   private static final float WHOLE_HI = 2.0F;
   public final Matrix4f projection = new Matrix4f();
   public float panini;
   public double originX;
   public double originY;
   public double originZ;
   public boolean cameraCut;
   public float cameraFocus;
   public float cameraAim;
   public float cameraTrauma;
   public float viewerX;
   public float viewerY;
   public float viewerZ;
   public boolean glassFar;
   public float glassStart;
   public float glassOpaque;
   public float glassStoreys;
   public float shadowReach = 128.0F;
   public float fogR;
   public float fogG;
   public float fogB;
   public float fogStart;
   public float fogEnd;
   public float nearReach;
   public FarGpu.CellDraw[] far = new FarGpu.CellDraw[64];
   public int farCount;
   public FarRelief.Lift relief;
   public static final int FAR_MASK_SIZE = 19;
   public final byte[] farMask = new byte[361];
   public int farMaskX;
   public int farMaskY;
   public int farMaskSize;
   public boolean daylightKnown;
   public float daylightR;
   public float daylightG;
   public float daylightB;
   public float skyR;
   public float skyG;
   public float skyB;
   public FarGpu.BlockDraw[] shells = new FarGpu.BlockDraw[64];
   public int shellCount;
   public static final int MAX_SHELL_BLOCKS = 24;
   public static final int SHELL_GRID = 53;
   public static final int SHELL_MASK_BYTES = 8427;
   public final byte[] shellMask = new byte[8427];
   public int shellMaskX;
   public int shellMaskY;
   public float zenithR;
   public float zenithG;
   public float zenithB;
   public float sunX;
   public float sunY = 1.0F;
   public float sunZ;
   public float sunR;
   public float sunG;
   public float sunB;
   public float sunStrength;
   public float skySunX;
   public float skySunY = 1.0F;
   public float skySunZ;
   public float skyGlow;
   public float skyMoonX;
   public float skyMoonY = -1.0F;
   public float skyMoonZ;
   public float moonDisc;
   public float moonFace;
   public float moonLight;
   public final float[] airLight = new float[]{1.0F, 1.0F, 1.0F};
   public final float[] skyBaseZenith = new float[3];
   public final float[] skyBaseHorizon = new float[3];
   public final float[] airSkyLevel = new float[]{1.0F, 1.0F, 1.0F};
   public float eyeExpected;
   public float eyeWindow;
   public float adapted;
   public float nightLight = 1.0F;
   public final float[] starFrame = new float[]{1.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F};
   public float cloudCover;
   public float night;
   public float time;
   public float cloudTime;
   public float overcast;
   public float cloudType = 0.5F;
   public float dayFraction;
   public float sunSine;
   public float eyeSky = 1.0F;
   public float nightFraction = -1.0F;
   public float sunTrueX;
   public float sunTrueY = 1.0F;
   public float sunTrueZ;
   public int worldDay;
   public float warmth = 0.6F;
   public float humidity = 0.5F;
   public float thunder;
   public int moonPhase;
   public float mist;
   public float rain;
   public float snow;
   public float windX;
   public float windZ;
   public float puddles;
   public float wetGround;
   public float desaturation;
   public float cloudWindX;
   public float cloudWindZ;
   public final float[] cloudSheetMid = new float[]{1.0F, 0.0F, 0.0F, 0.0F};
   public final float[] cloudSheetHigh = new float[]{1.0F, 0.0F, 0.0F, 0.0F};
   public static final int ALONG_SIDE = 48;
   public static final float ALONG_STEP = 512.0F;
   public final float[] coverAlong = new float[97];
   public final float[] typeAlong = new float[97];
   public final float[] rainAlong = new float[97];
   public double cloudCarriedX;
   public double cloudCarriedZ;
   public double windCarriedX;
   public double windCarriedZ;
   public float sheets;
   public float gusts;
   public float lightningX;
   public float lightningY;
   public float lightningZ;
   public float lightning;
   public static final int EXPOSURE_SIZE = 64;
   public static final byte EXPOSURE_PORCH = 100;
   public final byte[] exposure = new byte[4096];
   public float exposureAX;
   public float exposureAY;
   public static final byte ROOM_OUTSIDE = 0;
   public static final byte ROOM_NONE = -1;
   public final byte[] rooms = new byte[4096];
   public final byte[] water = new byte[4096];
   public static final int MAX_LIGHTS = 48;
   public static final int LIGHT_STRIDE = 12;
   public final float[] lights = new float[576];
   public final long[] lightKeys = new long[48];
   public int lightCount;
   public int lampShadowCount;
   public static final int MAX_TORCHES = 8;
   public static final int TORCH_STRIDE = 11;
   public final float[] torches = new float[88];
   public int torchCount;
   public boolean muzzleShadow;
   public static final int MAX_BLOBS = 32;
   public final float[] blobs = new float[128];
   public final FlameDraws flames = new FlameDraws();
   public final BloodDraws blood = new BloodDraws();
   public final PlanSnapshot floorPlan = new PlanSnapshot();
   public final SpotDraws spots = new SpotDraws();
   public int blobCount;
   public final ModelDraws models = new ModelDraws();
   public ChunkMeshData[] meshes = new ChunkMeshData[512];
   public float[] meshOffsets = new float[1536];
   public byte[] meshFlags = new byte[512];
   float[] meshDistance = new float[512];
   public float[] meshFade = new float[1024];
   public int meshCount;
   public int[] meshPlan = new int[512];
   public int[] meshPlanLength = new int[512];
   public long[] meshModelSquares = new long[512];
   public FloorPage[] meshFloor = new FloorPage[512];
   public ChunkMeshData[] meshBelow = new ChunkMeshData[512];
   public ChunkMeshData[] meshAbove = new ChunkMeshData[512];
   public int[] meshCardFirst = new int[512];
   public int[] meshCardCount = new int[512];
   public float[] cards = new float[1792];
   public TextureID[] cardPages = new TextureID[64];
   public int cardCount;
   public int[] plan = new int[3072];
   public int planEntries;
   public ChunkMeshData targetMesh;
   public int targetOwner;
   public int targetSquare = -1;
   private long[] order = new long[512];
   private ChunkMeshData[] sortedMeshes = new ChunkMeshData[512];
   private float[] sortedOffsets = new float[1536];
   private byte[] sortedFlags = new byte[512];
   private float[] sortedFade = new float[1024];
   private int[] sortedPlan = new int[1024];
   private long[] sortedModelSquares = new long[512];
   private FloorPage[] sortedFloor = new FloorPage[512];
   private ChunkMeshData[] sortedBelow = new ChunkMeshData[512];
   private ChunkMeshData[] sortedAbove = new ChunkMeshData[512];
   private int[] sortedCards = new int[1024];

   public float nearEnd() {
      return this.nearReach > 0.0F ? Math.min(this.fogEnd, this.nearReach) : this.fogEnd;
   }

   public void blob(float var1, float var2, float var3, float var4) {
      int var5 = this.blobCount;
      if (this.blobCount == 32) {
         var5 = this.farthestBlob();
         if (var1 * var1 + var2 * var2 + var3 * var3 >= this.distance2(var5)) {
            return;
         }
      } else {
         this.blobCount++;
      }

      this.blobs[var5 * 4] = var1;
      this.blobs[var5 * 4 + 1] = var2;
      this.blobs[var5 * 4 + 2] = var3;
      this.blobs[var5 * 4 + 3] = var4;
   }

   private int farthestBlob() {
      int var1 = 0;

      for (int var2 = 1; var2 < this.blobCount; var2++) {
         if (this.distance2(var2) > this.distance2(var1)) {
            var1 = var2;
         }
      }

      return var1;
   }

   private float distance2(int var1) {
      float var2 = this.blobs[var1 * 4];
      float var3 = this.blobs[var1 * 4 + 1];
      float var4 = this.blobs[var1 * 4 + 2];
      return var2 * var2 + var3 * var3 + var4 * var4;
   }

   public void relief(FarRelief.Lift var1) {
      this.relief = var1;
      this.models.relief = var1;
   }

   public void reset() {
      this.models.reset();
      Arrays.fill(this.meshes, 0, this.meshCount, null);
      Arrays.fill(this.meshFloor, 0, this.meshCount, null);
      Arrays.fill(this.meshBelow, 0, this.meshCount, null);
      Arrays.fill(this.meshAbove, 0, this.meshCount, null);
      this.meshCount = 0;
      Arrays.fill(this.cardPages, 0, this.cardCount, null);
      this.cardCount = 0;
      this.planEntries = 0;
      this.targetMesh = null;
      this.targetSquare = -1;

      for (int var1 = 0; var1 < this.farCount; var1++) {
         this.far[var1].mesh = null;
         this.far[var1].colours = null;
      }

      this.farCount = 0;
      this.farMaskSize = 0;
      this.relief = null;

      for (int var2 = 0; var2 < this.shellCount; var2++) {
         this.shells[var2].shell = null;
         this.shells[var2].inside = null;
      }

      this.shellCount = 0;
      Arrays.fill(this.shellMask, (byte)0);
      this.blobCount = 0;
   }

   public void addMesh(ChunkMeshData var1, float var2, float var3, float var4, byte var5, float var6) {
      this.addMesh(var1, var2, var3, var4, var5, var6, 0.0F, 2.0F);
   }

   public void addMesh(ChunkMeshData var1, float var2, float var3, float var4, byte var5, float var6, float var7, float var8) {
      if (this.meshCount == this.meshes.length) {
         this.meshes = Arrays.copyOf(this.meshes, this.meshCount * 2);
         this.meshOffsets = Arrays.copyOf(this.meshOffsets, this.meshCount * 6);
         this.meshFlags = Arrays.copyOf(this.meshFlags, this.meshCount * 2);
         this.meshDistance = Arrays.copyOf(this.meshDistance, this.meshCount * 2);
         this.meshFade = Arrays.copyOf(this.meshFade, this.meshCount * 4);
         this.meshPlan = Arrays.copyOf(this.meshPlan, this.meshCount * 2);
         this.meshPlanLength = Arrays.copyOf(this.meshPlanLength, this.meshCount * 2);
         this.meshModelSquares = Arrays.copyOf(this.meshModelSquares, this.meshCount * 2);
         this.meshFloor = Arrays.copyOf(this.meshFloor, this.meshCount * 2);
         this.meshBelow = Arrays.copyOf(this.meshBelow, this.meshCount * 2);
         this.meshAbove = Arrays.copyOf(this.meshAbove, this.meshCount * 2);
         this.meshCardFirst = Arrays.copyOf(this.meshCardFirst, this.meshCount * 2);
         this.meshCardCount = Arrays.copyOf(this.meshCardCount, this.meshCount * 2);
      }

      this.meshCardFirst[this.meshCount] = 0;
      this.meshCardCount[this.meshCount] = 0;
      this.meshPlan[this.meshCount] = -1;
      this.meshPlanLength[this.meshCount] = 0;
      this.meshModelSquares[this.meshCount] = -1L;
      this.meshFloor[this.meshCount] = null;
      this.meshBelow[this.meshCount] = null;
      this.meshAbove[this.meshCount] = null;
      this.meshFade[this.meshCount * 2] = var7;
      this.meshFade[this.meshCount * 2 + 1] = var8;
      this.meshes[this.meshCount] = var1;
      this.meshOffsets[this.meshCount * 3] = var2;
      this.meshOffsets[this.meshCount * 3 + 1] = var3;
      this.meshOffsets[this.meshCount * 3 + 2] = var4;
      this.meshFlags[this.meshCount] = var5;
      this.meshDistance[this.meshCount] = var6;
      this.meshCount++;
   }

   public void addCard(int var1, float[] var2, int var3, TextureID var4, float var5) {
      if (this.cardCount == this.cardPages.length) {
         this.cards = Arrays.copyOf(this.cards, this.cards.length * 2);
         this.cardPages = Arrays.copyOf(this.cardPages, this.cardCount * 2);
      }

      if (this.meshCardCount[var1] == 0) {
         this.meshCardFirst[var1] = this.cardCount;
      }

      int var6 = this.cardCount * 28;
      System.arraycopy(var2, var3, this.cards, var6, 28);
      this.cards[var6 + 24] = var5;
      this.cards[var6 + 24 + 1] = 2.0F;
      this.cardPages[this.cardCount++] = var4;
      this.meshCardCount[var1]++;
   }

   public int addPlanEntry(int var1, int var2, int var3) {
      if (this.planEntries * 3 == this.plan.length) {
         this.plan = Arrays.copyOf(this.plan, this.plan.length * 2);
      }

      this.plan[this.planEntries * 3] = var1;
      this.plan[this.planEntries * 3 + 1] = var2;
      this.plan[this.planEntries * 3 + 2] = var3;
      this.planEntries++;
      return 1;
   }

   public void sortFrontToBack() {
      if (this.order.length < this.meshCount) {
         this.order = new long[this.meshes.length];
         this.sortedMeshes = new ChunkMeshData[this.meshes.length];
         this.sortedOffsets = new float[this.meshes.length * 3];
         this.sortedFlags = new byte[this.meshes.length];
         this.sortedFade = new float[this.meshes.length * 2];
         this.sortedPlan = new int[this.meshes.length * 2];
         this.sortedModelSquares = new long[this.meshes.length];
         this.sortedFloor = new FloorPage[this.meshes.length];
         this.sortedBelow = new ChunkMeshData[this.meshes.length];
         this.sortedAbove = new ChunkMeshData[this.meshes.length];
         this.sortedCards = new int[this.meshes.length * 2];
      }

      for (int var1 = 0; var1 < this.meshCount; var1++) {
         this.order[var1] = (long)Float.floatToIntBits(Math.max(0.0F, this.meshDistance[var1])) << 32 | var1;
      }

      Arrays.sort(this.order, 0, this.meshCount);

      for (int var3 = 0; var3 < this.meshCount; var3++) {
         int var2 = (int)this.order[var3];
         this.sortedMeshes[var3] = this.meshes[var2];
         this.sortedOffsets[var3 * 3] = this.meshOffsets[var2 * 3];
         this.sortedOffsets[var3 * 3 + 1] = this.meshOffsets[var2 * 3 + 1];
         this.sortedOffsets[var3 * 3 + 2] = this.meshOffsets[var2 * 3 + 2];
         this.sortedFlags[var3] = this.meshFlags[var2];
         this.sortedFade[var3 * 2] = this.meshFade[var2 * 2];
         this.sortedFade[var3 * 2 + 1] = this.meshFade[var2 * 2 + 1];
         this.sortedPlan[var3 * 2] = this.meshPlan[var2];
         this.sortedPlan[var3 * 2 + 1] = this.meshPlanLength[var2];
         this.sortedModelSquares[var3] = this.meshModelSquares[var2];
         this.sortedFloor[var3] = this.meshFloor[var2];
         this.sortedBelow[var3] = this.meshBelow[var2];
         this.sortedAbove[var3] = this.meshAbove[var2];
         this.sortedCards[var3 * 2] = this.meshCardFirst[var2];
         this.sortedCards[var3 * 2 + 1] = this.meshCardCount[var2];
      }

      System.arraycopy(this.sortedMeshes, 0, this.meshes, 0, this.meshCount);
      System.arraycopy(this.sortedOffsets, 0, this.meshOffsets, 0, this.meshCount * 3);
      System.arraycopy(this.sortedFlags, 0, this.meshFlags, 0, this.meshCount);
      System.arraycopy(this.sortedFade, 0, this.meshFade, 0, this.meshCount * 2);
      System.arraycopy(this.sortedModelSquares, 0, this.meshModelSquares, 0, this.meshCount);
      System.arraycopy(this.sortedFloor, 0, this.meshFloor, 0, this.meshCount);
      System.arraycopy(this.sortedBelow, 0, this.meshBelow, 0, this.meshCount);
      System.arraycopy(this.sortedAbove, 0, this.meshAbove, 0, this.meshCount);

      for (int var4 = 0; var4 < this.meshCount; var4++) {
         this.meshPlan[var4] = this.sortedPlan[var4 * 2];
         this.meshPlanLength[var4] = this.sortedPlan[var4 * 2 + 1];
         this.meshCardFirst[var4] = this.sortedCards[var4 * 2];
         this.meshCardCount[var4] = this.sortedCards[var4 * 2 + 1];
      }

      Arrays.fill(this.sortedMeshes, 0, this.meshCount, null);
      Arrays.fill(this.sortedFloor, 0, this.meshCount, null);
      Arrays.fill(this.sortedBelow, 0, this.meshCount, null);
   }
}
