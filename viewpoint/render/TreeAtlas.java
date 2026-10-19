package viewpoint.render;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import zombie.core.textures.TextureID;

public final class TreeAtlas {
   static final int SIZE = 2048;
   static final int CELL_WIDTH = 64;
   static final int CELL_HEIGHT = 128;
   static final int COLUMNS = 32;
   static final int CELLS = 512;
   static final int MIPS = 7;
   public static final long BYTES = 22369621L;
   private static final HashMap<TreeAtlas.Key, TreeAtlas.Slot> slots = new HashMap<>();
   private static boolean made;
   private static boolean full;
   static final ConcurrentLinkedQueue<TreeAtlas.Slot> PENDING = new ConcurrentLinkedQueue<>();

   public static TreeAtlas.Slot slot(TextureID var0, float[] var1, TextureID var2, float[] var3, float[] var4) {
      TreeAtlas.Key var5 = new TreeAtlas.Key(var0, var1[0], var1[1], var2, var3 == null ? 0.0F : var3[0], var3 == null ? 0.0F : var3[1]);
      TreeAtlas.Slot var6 = slots.get(var5);
      if (var6 == null && !full) {
         if (slots.size() == 512) {
            full = true;
            System.out.println("[Viewpoint] tree atlas full at 512 trees: the rest keep their cards");
            return null;
         } else {
            var6 = new TreeAtlas.Slot(slots.size(), var0, var1, var2, var3, var4);
            slots.put(var5, var6);
            made = true;
            PENDING.add(var6);
            return var6;
         }
      } else {
         return var6;
      }
   }

   public static void reset() {
      slots.clear();
      full = false;
   }

   public static long bytes() {
      return made ? 22369621L : 0L;
   }

   private TreeAtlas() {
   }

   private record Key(TextureID trunk, float trunkU, float trunkV, TextureID leaves, float leavesU, float leavesV) {
   }

   public static final class Slot {
      public final float[] rect;
      public final float halfWidth;
      public final float top;
      final int index;
      final TextureID trunkPage;
      final TextureID leavesPage;
      final float[] trunkMap;
      final float[] leavesMap;
      final float width;
      final float height;

      Slot(int var1, TextureID var2, float[] var3, TextureID var4, float[] var5, float[] var6) {
         this.index = var1;
         this.trunkPage = var2;
         this.trunkMap = var3;
         this.leavesPage = var4;
         this.leavesMap = var5;
         this.width = var6[0];
         this.height = var6[1];
         this.halfWidth = var6[2];
         this.top = var6[3];
         float var7 = var1 % 32 * 64;
         float var8 = var1 / 32 * 128;
         this.rect = new float[]{(var7 + 0.5F) / 2048.0F, (var8 + 0.5F) / 2048.0F, (var7 + 64.0F - 0.5F) / 2048.0F, (var8 + 128.0F - 0.5F) / 2048.0F};
      }

      int x() {
         return this.index % 32 * 64;
      }

      int y() {
         return this.index / 32 * 128;
      }
   }
}
