package viewpoint.platform;

import java.util.Arrays;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

public final class GpuParts {
   public static final int WORLD = 0;
   public static final int MODELS = 1;
   public static final int BAND = 2;
   public static final int NEAR_CASCADE = 3;
   public static final int MID_CASCADE = 4;
   public static final int FAR_CASCADE = 5;
   public static final int FLASH_MAP = 6;
   public static final int RAIN_MAP = 7;
   public static final int FAR_MAP = 8;
   public static final int FAR_SHELLS = 9;
   public static final int FAR_BOXES = 10;
   public static final int FAR_TREES = 11;
   public static final int CLOUDS = 12;
   public static final int LIGHT = 13;
   public static final int GLASS = 14;
   public static final int COMPOSITE = 15;
   public static final int TAA = 16;
   public static final int LAMP_MAPS = 17;
   public static final int FAR_LAMPS = 18;
   public static final int FAR_WIDE_MAP = 19;
   public static final int WEATHER_MAP = 20;
   public static final int PACK_PASSES = 21;
   public static final int DISTANT_RAIN = 24;
   public static final int BOUNCE = 25;
   public static final int AIR = 26;
   public static final int SHAFTS = 27;
   public static final int SUN_AIR = 28;
   public static final int REFLECTIONS = 29;
   public static final int WAVES = 30;
   public static final int REFLECTION_COPY = 31;
   public static final int REFLECTION_DEPTH = 32;
   public static final int REFLECTION_TRACE = 33;
   public static final int SUN_AIR_PLACES = 34;
   public static final int SUN_AIR_MARCH = 35;
   public static final int SUN_AIR_FILL = 36;
   public static final int CLOUD_TRACE = 37;
   public static final int CLOUD_RESOLVE = 38;
   public static final int SKY_VIEW = 39;
   public static final int SKY_LIGHT = 40;
   public static final int GTAO = 41;
   public static final int FAR_UPLOADS = 42;
   public static final int CLOUD_SHADOW = 43;
   public static final int ANTIALIAS = 44;
   public static final int MUZZLE_MAP = 45;
   public static final int HEADLIGHT_MAPS = 46;
   private static final String[] NAMES = new String[]{
      "world",
      "models",
      "band",
      "near cascade",
      "mid cascade",
      "far cascade",
      "flashlight map",
      "rain map",
      "far map",
      "far shells",
      "far boxes",
      "far trees",
      "clouds",
      "light",
      "glass",
      "composite",
      "taa",
      "lamp maps",
      "far lamps",
      "far wide map",
      "weather map",
      "pack translucent",
      "pack composite",
      "pack final",
      "distant rain",
      "bounce",
      "air",
      "shafts",
      "sun air",
      "reflections",
      "waves",
      "reflection copy",
      "reflection depth",
      "reflection trace",
      "sun air places",
      "sun air march",
      "sun air fill",
      "cloud trace",
      "cloud resolve",
      "sky view",
      "sky light",
      "gtao",
      "far uploads",
      "cloud shadow",
      "fxaa or smaa",
      "muzzle map",
      "headlight maps"
   };
   private static final int COUNT = NAMES.length;
   private static final int SLOTS = 4;
   private static int[][] queries;
   private static final long[] begun = new long[4];
   private static final long[] ended = new long[4];
   private static int slot;
   private static int frames;
   private static final double[] ms = new double[COUNT];
   private static final int[] runs = new int[COUNT];

   public static void begin(int var0) {
      if (queries == null) {
         queries = new int[4][COUNT * 2];

         for (int[] var4 : queries) {
            GL15.glGenQueries(var4);
         }
      }

      GL33.glQueryCounter(queries[slot][var0 * 2], 36392);
      begun[slot] = begun[slot] | 1L << var0;
   }

   public static void end(int var0) {
      if ((begun[slot] & 1L << var0) != 0L) {
         GL33.glQueryCounter(queries[slot][var0 * 2 + 1], 36392);
         ended[slot] = ended[slot] | 1L << var0;
      }
   }

   static void frameEnded() {
      if (queries != null) {
         slot = (slot + 1) % 4;
         long var0 = ended[slot];
         if (available(var0)) {
            for (int var2 = 0; var2 < COUNT; var2++) {
               if ((var0 & 1L << var2) != 0L) {
                  long var3 = GL33.glGetQueryObjectui64(queries[slot][var2 * 2], 34918);
                  long var5 = GL33.glGetQueryObjectui64(queries[slot][var2 * 2 + 1], 34918);
                  ms[var2] = ms[var2] + (var5 - var3) * 1.0E-6;
                  runs[var2]++;
               }
            }

            frames++;
         }

         begun[slot] = 0L;
         ended[slot] = 0L;
      }
   }

   private static boolean available(long var0) {
      for (int var2 = 0; var2 < COUNT; var2++) {
         if ((var0 & 1L << var2) != 0L && GL15.glGetQueryObjecti(queries[slot][var2 * 2 + 1], 34919) == 0) {
            return false;
         }
      }

      return true;
   }

   public static String report() {
      StringBuilder var0 = new StringBuilder(" | gpu parts ms/frame:");
      boolean var1 = false;

      for (int var2 = 0; var2 < COUNT; var2++) {
         if (runs[var2] != 0 && frames != 0) {
            var0.append(var1 ? ", " : " ").append(String.format("%s %.2f", NAMES[var2], ms[var2] / frames));
            var1 = true;
            if (runs[var2] < frames) {
               var0.append(String.format(" (%.2f x %.0f%%)", ms[var2] / runs[var2], 100.0 * runs[var2] / frames));
            }
         }
      }

      Arrays.fill(ms, 0.0);
      Arrays.fill(runs, 0);
      frames = 0;
      return var1 ? var0.toString() : "";
   }

   private GpuParts() {
   }
}
