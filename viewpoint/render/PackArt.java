package viewpoint.render;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryUtil;

final class PackArt {
   static final int SIZE = 2048;
   static final int GUTTER = 8;
   static final long PAGE_BYTES = pageBytes();
   private static final ArrayList<Integer> textures = new ArrayList<>();
   private static final ArrayList<ArtShelves> shelves = new ArrayList<>();

   static boolean put(PackModel var0, PackData var1, long var2) {
      int var4 = var1.width() + 8;
      int var5 = var1.height() + 8;

      for (int var6 = 0; var6 < shelves.size(); var6++) {
         int[] var7 = shelves.get(var6).take(var4, var5);
         if (var7 != null) {
            upload(var0, var1, var6, var7);
            return true;
         }
      }

      if (var2 >= PAGE_BYTES && var4 <= 2048 && var5 <= 2048) {
         textures.add(page());
         shelves.add(new ArtShelves(2048, 8));
         upload(var0, var1, shelves.size() - 1, shelves.get(shelves.size() - 1).take(var4, var5));
         return true;
      } else {
         return false;
      }
   }

   static void free(PackModel var0) {
      shelves.get(var0.artPage).give(var0.artX, var0.artY, var0.artW + 8);
   }

   static int texture(int var0) {
      return textures.get(var0);
   }

   static long bytes() {
      return textures.size() * PAGE_BYTES;
   }

   static int pages() {
      return textures.size();
   }

   private static void upload(PackModel var0, PackData var1, int var2, int[] var3) {
      var0.artPage = var2;
      var0.artX = var3[0];
      var0.artY = var3[1];
      var0.artW = var1.width();
      var0.artH = var1.height();
      var0.artWidth = var1.artWidth();
      var0.artHeight = var1.artHeight();
      int var4 = var1.width() + 8;
      int var5 = var1.height() + 8;
      ByteBuffer var6 = MemoryUtil.memCalloc(var4 * var5 * 4);

      try {
         GL11.glPixelStorei(3317, 4);

         for (int var7 = 0; var7 < 4; var7++) {
            int var8 = var4 >> var7;
            int var9 = var5 >> var7;
            int var10 = var1.width() >> var7;
            int var11 = var1.height() >> var7;
            byte[] var12 = var1.levels()[var7];
            var6.clear();
            MemoryUtil.memSet(var6, 0);

            for (int var13 = 0; var13 < var11; var13++) {
               var6.position(var13 * var8 * 4);
               var6.put(var12, var13 * var10 * 4, var10 * 4);
            }

            var6.position(0).limit(var8 * var9 * 4);
            GL45.glTextureSubImage2D(textures.get(var2), var7, var3[0] >> var7, var3[1] >> var7, var8, var9, 6408, 5121, var6);
         }
      } finally {
         MemoryUtil.memFree(var6);
      }
   }

   private static int page() {
      int var0 = GL45.glCreateTextures(3553);
      GL45.glTextureStorage2D(var0, 4, 32856, 2048, 2048);
      GL45.glTextureParameteri(var0, 33085, 3);
      System.out.println("[Viewpoint] model packs: art page " + (textures.size() + 1) + " made");
      return var0;
   }

   private static long pageBytes() {
      long var0 = 0L;

      for (int var2 = 0; var2 < 4; var2++) {
         var0 += (long)(2048 >> var2) * (2048 >> var2) * 4L;
      }

      return var0;
   }

   private PackArt() {
   }
}
