package viewpoint.light;

import java.util.ArrayList;
import java.util.HashMap;
import viewpoint.render.TilePalette;
import zombie.core.textures.Texture;
import zombie.core.textures.TextureID;
import zombie.iso.IsoDirections;
import zombie.iso.sprite.IsoSprite;

final class TileNumbers {
   private static final HashMap<String, Integer> ids = new HashMap<>();
   private static final ArrayList<IsoSprite> waiting = new ArrayList<>();
   private static final ArrayList<Integer> waitingIds = new ArrayList<>();
   private static boolean full;

   static int of(IsoSprite var0) {
      if (var0 != null && var0.name != null) {
         Integer var1 = ids.get(var0.name);
         if (var1 != null) {
            return var1;
         }

         if (ids.size() >= 65535) {
            if (!full) {
               full = true;
               System.out.println("[Viewpoint] tile palette full at 65535 tiles: the rest take a neutral colour");
            }

            return 0;
         } else {
            int var2 = ids.size() + 1;
            ids.put(var0.name, var2);
            if (!handOver(var0, var2)) {
               waiting.add(var0);
               waitingIds.add(var2);
            }

            return var2;
         }
      } else {
         return 0;
      }
   }

   static void retry() {
      for (int var0 = waiting.size() - 1; var0 >= 0; var0--) {
         if (handOver(waiting.get(var0), waitingIds.get(var0))) {
            waiting.remove(var0);
            waitingIds.remove(var0);
         }
      }
   }

   private static boolean handOver(IsoSprite var0, int var1) {
      Texture var2 = var0.getTextureForCurrentFrame(IsoDirections.N);
      if (var2 == null) {
         return true;
      } else {
         TextureID var3 = var2.getTextureId();
         if (var2.isReady() && var3 != null) {
            TilePalette.average(var1, var3, var2.getXStart(), var2.getYStart(), var2.getXEnd(), var2.getYEnd());
            return true;
         } else {
            return false;
         }
      }
   }

   private TileNumbers() {
   }
}
