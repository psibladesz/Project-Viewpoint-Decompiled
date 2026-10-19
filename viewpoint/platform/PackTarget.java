package viewpoint.platform;

public final class PackTarget {
   private static final int MAX_TEXELS = 4096;
   public final String name;
   public final PackTarget.Format format;
   private final float scale;
   private final int width;
   private final int height;
   public final boolean history;

   private PackTarget(String var1, PackTarget.Format var2, float var3, int var4, int var5, boolean var6) {
      this.name = var1;
      this.format = var2;
      this.scale = var3;
      this.width = var4;
      this.height = var5;
      this.history = var6;
   }

   static PackTarget parse(String var0, String var1) {
      String var2 = "target." + var0;
      if (!PackOption.ID.matcher(var0).matches()) {
         throw new IllegalArgumentException(var2 + ": a target's name is letters and digits");
      }

      String[] var3 = var1.trim().split("\\s+");
      if (var3.length >= 2 && var3.length <= 3 && (var3.length != 3 || var3[2].equals("history"))) {
         PackTarget.Format var4 = null;

         for (PackTarget.Format var8 : PackTarget.Format.values()) {
            var4 = var8.key.equals(var3[0]) ? var8 : var4;
         }

         if (var4 == null) {
            throw new IllegalArgumentException(var2 + ": " + var3[0] + " is not rgba8, rgba16f, rg16f, r16f or r32f");
         }

         boolean var9 = var3.length == 3;
         int var10 = var3[1].indexOf(120);
         if (var10 < 0) {
            float var12 = number(var2, var3[1]);
            if (!(var12 <= 0.0F) && !(var12 > 1.0F)) {
               return new PackTarget(var0, var4, var12, 0, 0, var9);
            } else {
               throw new IllegalArgumentException(var2 + ": a share of the frame is over 0 and at most 1");
            }
         } else {
            int var11 = (int)number(var2, var3[1].substring(0, var10));
            int var13 = (int)number(var2, var3[1].substring(var10 + 1));
            if (var11 >= 1 && var13 >= 1 && var11 <= 4096 && var13 <= 4096) {
               return new PackTarget(var0, var4, 0.0F, var11, var13, var9);
            } else {
               throw new IllegalArgumentException(var2 + ": fixed texels are 1 to 4096 a side");
            }
         }
      } else {
         throw new IllegalArgumentException(var2 + " is [format] [size] [history], not " + var1);
      }
   }

   private static float number(String var0, String var1) {
      try {
         return Float.parseFloat(var1);
      } catch (NumberFormatException var3) {
         throw new IllegalArgumentException(var0 + ": " + var1 + " is not a size (0.5, 1, 256x256)", var3);
      }
   }

   public int width(int var1) {
      return this.scale > 0.0F ? Math.max(1, Math.round(var1 * this.scale)) : this.width;
   }

   public int height(int var1) {
      return this.scale > 0.0F ? Math.max(1, Math.round(var1 * this.scale)) : this.height;
   }

   @Override
   public String toString() {
      return this.name;
   }

   public enum Format {
      RGBA8("rgba8", 32856, 6408, 5121),
      RGBA16F("rgba16f", 34842, 6408, 5126),
      RG16F("rg16f", 33327, 33319, 5126),
      R16F("r16f", 33325, 6403, 5126),
      R32F("r32f", 33326, 6403, 5126);

      final String key;
      public final int internal;
      public final int format;
      public final int type;

      Format(String nullxx, int nullxxx, int nullxxxx, int nullxxxxx) {
         this.key = nullxx;
         this.internal = nullxxx;
         this.format = nullxxxx;
         this.type = nullxxxxx;
      }
   }
}
