package viewpoint.platform;

import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;

public final class Gl {
   public static final int UNIT_LIGHT = 5;
   public static final int UNIT_EXPOSURE = 8;
   public static final int UNIT_ALBEDO = 9;
   public static final int UNIT_NORMAL = 10;
   public static final int UNIT_ENGINE_LIGHT = 11;
   public static final int UNIT_DEPTH = 12;
   public static final int UNIT_WEATHER = 14;
   public static final int UNIT_VOLUME = 15;
   public static final int UNIT_BLOOM = 16;
   public static final int UNIT_CLOUDS = 17;
   public static final int UNIT_NOISE = 18;
   public static final int UNIT_HISTORY = 19;
   public static final int UNIT_SPOT_SHADOWS = 21;
   public static final int UNIT_FLASH_DEPTH = 22;
   public static final int UNIT_ROOMS = 23;
   public static final int UNIT_RAIN = 24;
   public static final int UNIT_FAR_TOP = 25;
   public static final int UNIT_FAR_SIDE = 26;
   public static final int UNIT_FAR_MASK = 27;
   public static final int UNIT_FAR_SHELL = 28;
   public static final int UNIT_FAR_DEPTH = 29;
   public static final int UNIT_MODEL_PALETTES = 31;
   public static final int UNIT_VEHICLE = 32;
   public static final int UNIT_VELOCITY = 39;
   public static final int UNIT_MODEL_DRAWS = 40;
   public static final int UNIT_FLOOR_PAGES = 41;
   public static final int UNIT_TREE_ATLAS = 42;
   public static final int UNIT_FAR_TREES = 43;
   public static final int UNIT_TREE_KINDS = 44;
   public static final int UNIT_BAND_LIGHT = 45;
   public static final int UNIT_CELL_LIGHT = 46;
   public static final int UNIT_CELL_TABLE = 47;
   public static final int UNIT_PLANT_SLOTS = 48;
   public static final int UNIT_LAMP_SHADOW = 49;
   public static final int UNIT_TARGET_MASK = 51;
   public static final int UNIT_PASS = 52;
   public static final int UNIT_MODEL_TEXTURES = 64;
   public static final int UNIT_WATER = 80;
   public static final int UNIT_NOISE_2D = 84;
   public static final int UNIT_DISTANT_RAIN = 85;
   public static final int UNIT_TRANSLUCENT_SUM = 86;
   public static final int UNIT_TRANSLUCENT_REVEAL = 87;
   public static final int UNIT_PACK_TABLE = 88;
   public static final int UNIT_MESH_RECORDS = 89;
   public static final int UNIT_CORPSE_CARDS = 90;
   public static final int UNIT_FAR_RELIEF = 91;
   public static final int UNIT_FAR_RELIEF_WATER = 92;
   public static final int UNIT_FAR_RELIEF_WALLS = 93;
   public static final int UNIT_SHELL_RECORDS = 94;
   public static final int UNIT_CLOUD_SHAPE = 95;
   public static final int UNIT_CLOUD_DETAIL = 96;
   public static final int UNIT_CLOUD_CURL = 97;
   public static final int UNIT_CLOUD_SHEETS = 98;
   public static final int UNIT_STARS = 99;
   public static final int UNIT_MOON = 100;
   public static final int UNIT_TRANSMITTANCE = 101;
   public static final int UNIT_MULTIPLE_SCATTERING = 102;
   public static final int UNIT_SKY_VIEW = 103;
   public static final int UNIT_SKY_LIGHT = 104;
   public static final int UNIT_OCCLUSION = 105;
   public static final int UNIT_OCCLUSION_EDGES = 106;
   public static final int UNIT_BOUNCE = 107;
   public static final int UNIT_FLASH_BOUNCE = 108;
   public static final int UNIT_TONE_LUT = 109;
   public static final int UNIT_FLOOR_PLAN = 110;
   public static final int UNIT_LAMP_FIELDS = 111;
   public static final int UNIT_BOUNCE_CASCADE = 112;
   public static final int UNIT_TILE_PALETTE = 113;
   public static final int UNIT_FROXEL_HISTORY = 114;
   public static final int UNIT_FROXEL_VOLUME = 115;
   public static final int UNIT_SUN_SHAFTS = 116;
   public static final int UNIT_SUN_AIR_COORDS = 117;
   public static final int UNIT_SUN_AIR = 118;
   public static final int UNIT_REFLECTION = 119;
   public static final int UNIT_REFLECTION_DEPTH = 120;
   public static final int UNIT_REFLECTION_SCENE = 121;
   public static final int UNIT_WAVES = 122;
   public static final int UNIT_RIPPLES = 123;
   public static final int UNIT_FAR_RELIEF_FLOW = 124;
   public static final int UNIT_WAVE_FIELDS = 125;
   public static final int UNIT_BOUNCE_WALLS = 127;
   public static final int UNIT_BOUNCE_CASCADE_WALLS = 128;
   public static final int UNIT_BED_COLOURS = 129;
   public static final int UNIT_CLOUD_SHADOW = 130;
   public static final int UNIT_SKY_SUMS = 131;
   public static final int UNIT_HISTORY_COUNT = 132;
   public static final int UNIT_SMAA_AREA = 133;
   public static final int UNIT_SMAA_SEARCH = 134;
   public static final int UNIT_SMAA_WEIGHTS = 135;
   public static final int UNIT_FLOOR_SURFACES = 136;
   public static final int UNIT_FLOOR_KINDS = 137;
   public static final int UNIT_FLOOR_STONES = 138;
   public static final int UNIT_MUZZLE_SHADOW = 139;
   public static final int UNIT_SPOTS = 140;
   public static final int UNIT_BOUNCE_RISING = 141;
   public static final int MODEL_TEXTURES = 16;
   public static final int MODEL_SLOT_SHIFT = 24;
   public static final int UNIT_ATLASES = 64;
   public static final int ATLAS_SLOTS = 8;
   public static final int ATLAS_SLOT_SHIFT = 24;
   public static final int[] UNIT_CASCADE = new int[]{6, 20, 7, 30, 50};
   public static final int[] UNIT_SHADOW_DEPTH = new int[]{81, 82, 83};
   private static int emptyVao;
   private static int triangles = 4;

   public static void init() {
      emptyVao = GL30.glGenVertexArrays();
   }

   public static void patches(boolean var0) {
      if (var0) {
         GL40.glPatchParameteri(36466, 3);
      }

      triangles = var0 ? 14 : 4;
   }

   public static int triangles() {
      return triangles;
   }

   public static void bind(int var0, int var1) {
      GL13.glActiveTexture(33984 + var0);
      GL11.glBindTexture(3553, var1);
   }

   public static void screenTriangle() {
      GL30.glBindVertexArray(emptyVao);
      GL11.glDrawArrays(4, 0, 3);
      GL30.glBindVertexArray(0);
      DrawCounts.draw(5);
   }

   public static void generated(int var0) {
      generated(0, var0);
   }

   public static void generated(int var0, int var1) {
      GL30.glBindVertexArray(emptyVao);
      GL11.glDrawArrays(4, var0, var1);
      GL30.glBindVertexArray(0);
      DrawCounts.draw(5);
   }

   public static int texture(int var0, int var1, int var2, int var3, int var4, int var5) {
      int var6 = GL11.glGenTextures();
      GL11.glBindTexture(3553, var6);
      GL11.glTexImage2D(3553, 0, var0, var1, var2, 0, var3, var4, (ByteBuffer)null);
      GL11.glTexParameteri(3553, 10241, var5);
      GL11.glTexParameteri(3553, 10240, var5);
      GL11.glTexParameteri(3553, 10242, 33071);
      GL11.glTexParameteri(3553, 10243, 33071);
      return var6;
   }

   public static void resetState() {
      GL11.glDisable(3089);
      GL11.glDisable(3042);
      GL14.glBlendEquation(32774);
      GL11.glDisable(3008);
      GL11.glDisable(2884);
      GL11.glFrontFace(2305);
      GL11.glDisable(2960);
      GL11.glStencilMask(255);
      GL11.glDisable(32823);
      GL11.glDisable(32926);
      GL11.glDisable(36281);

      for (int var0 = 0; var0 < 8; var0++) {
         GL11.glDisable(12288 + var0);
      }

      GL11.glColorMask(true, true, true, true);
      GL11.glDepthRange(0.0, 1.0);
      GL11.glDepthMask(true);
      GL11.glEnable(2929);
      GL11.glDepthFunc(513);
      GL33.glBindSampler(0, 0);
      GL30.glBindVertexArray(0);
      GL13.glActiveTexture(33984);
   }

   public static float wrap(double var0, double var2) {
      return (float)(var0 - Math.floor(var0 / var2) * var2);
   }

   private Gl() {
   }
}
