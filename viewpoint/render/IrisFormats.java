package viewpoint.render;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class IrisFormats {
   private static final Map<String, IrisFormats.Format> FORMATS = new HashMap<>();
   private static final Map<String, Integer> PIXEL_FORMATS = new HashMap<>();
   private static final Map<String, Integer> PIXEL_TYPES = new HashMap<>();

   private static void put(String var0, int var1, int var2, int var3, int var4) {
      FORMATS.put(var0, new IrisFormats.Format(var1, var2, var3, var4, false));
   }

   private static void integer(String var0, int var1, int var2, int var3, int var4) {
      FORMATS.put(var0, new IrisFormats.Format(var1, var2, var3, var4, true));
   }

   static IrisFormats.Format format(String var0) {
      return var0 == null ? null : FORMATS.get(var0.strip().toUpperCase(Locale.ROOT));
   }

   static int pixelFormat(String var0) {
      return PIXEL_FORMATS.getOrDefault(var0.strip().toUpperCase(Locale.ROOT), -1);
   }

   static int pixelType(String var0) {
      return PIXEL_TYPES.getOrDefault(var0.strip().toUpperCase(Locale.ROOT), -1);
   }

   private IrisFormats() {
   }

   static {
      short var0 = 5126;
      short var1 = 5121;
      put("R8", 33321, 6403, var1, 1);
      put("RG8", 33323, 33319, var1, 2);
      put("RGB8", 32849, 6407, var1, 3);
      put("RGBA8", 32856, 6408, var1, 4);
      put("R8_SNORM", 36756, 6403, var0, 1);
      put("RG8_SNORM", 36757, 33319, var0, 2);
      put("RGB8_SNORM", 36758, 6407, var0, 3);
      put("RGBA8_SNORM", 36759, 6408, var0, 4);
      put("R16", 33322, 6403, var0, 2);
      put("RG16", 33324, 33319, var0, 4);
      put("RGB16", 32852, 6407, var0, 6);
      put("RGBA16", 32859, 6408, var0, 8);
      put("R16_SNORM", 36760, 6403, var0, 2);
      put("RG16_SNORM", 36761, 33319, var0, 4);
      put("RGB16_SNORM", 36762, 6407, var0, 6);
      put("RGBA16_SNORM", 36763, 6408, var0, 8);
      put("R16F", 33325, 6403, var0, 2);
      put("RG16F", 33327, 33319, var0, 4);
      put("RGB16F", 34843, 6407, var0, 6);
      put("RGBA16F", 34842, 6408, var0, 8);
      put("R32F", 33326, 6403, var0, 4);
      put("RG32F", 33328, 33319, var0, 8);
      put("RGB32F", 34837, 6407, var0, 12);
      put("RGBA32F", 34836, 6408, var0, 16);
      put("R11F_G11F_B10F", 35898, 6407, var0, 4);
      put("RGB9_E5", 35901, 6407, var0, 4);
      put("RGB10_A2", 32857, 6408, var0, 4);
      put("RGB5_A1", 32855, 6408, var0, 2);
      put("R3_G3_B2", 10768, 6407, var0, 1);
      integer("R8I", 33329, 36244, 5120, 1);
      integer("RG8I", 33335, 33320, 5120, 2);
      integer("RGBA8I", 36238, 36249, 5120, 4);
      integer("R8UI", 33330, 36244, var1, 1);
      integer("RG8UI", 33336, 33320, var1, 2);
      integer("RGBA8UI", 36220, 36249, var1, 4);
      integer("R16I", 33331, 36244, 5122, 2);
      integer("RG16I", 33337, 33320, 5122, 4);
      integer("RGBA16I", 36232, 36249, 5122, 8);
      integer("R16UI", 33332, 36244, 5123, 2);
      integer("RG16UI", 33338, 33320, 5123, 4);
      integer("RGBA16UI", 36214, 36249, 5123, 8);
      integer("R32I", 33333, 36244, 5124, 4);
      integer("RG32I", 33339, 33320, 5124, 8);
      integer("RGBA32I", 36226, 36249, 5124, 16);
      integer("R32UI", 33334, 36244, 5125, 4);
      integer("RG32UI", 33340, 33320, 5125, 8);
      integer("RGBA32UI", 36208, 36249, 5125, 16);
      integer("RGB10_A2UI", 36975, 36249, 5125, 4);
      String[][] var2 = new String[][]{
         {"RED", "6403"},
         {"RG", "33319"},
         {"RGB", "6407"},
         {"BGR", "32992"},
         {"RGBA", "6408"},
         {"BGRA", "32993"},
         {"RED_INTEGER", "36244"},
         {"RG_INTEGER", "33320"},
         {"RGB_INTEGER", "36248"},
         {"BGR_INTEGER", "36250"},
         {"RGBA_INTEGER", "36249"},
         {"BGRA_INTEGER", "36251"}
      };

      for (String[] var6 : var2) {
         PIXEL_FORMATS.put(var6[0], Integer.parseInt(var6[1]));
      }

      String[][] var8 = new String[][]{
         {"BYTE", "5120"},
         {"SHORT", "5122"},
         {"INT", "5124"},
         {"HALF_FLOAT", "5131"},
         {"FLOAT", "5126"},
         {"UNSIGNED_BYTE", "5121"},
         {"UNSIGNED_SHORT", "5123"},
         {"UNSIGNED_INT", "5125"}
      };

      for (String[] var7 : var8) {
         PIXEL_TYPES.put(var7[0], Integer.parseInt(var7[1]));
      }
   }

   record Format(int internal, int pixels, int type, int bytes, boolean integer) {
   }
}
