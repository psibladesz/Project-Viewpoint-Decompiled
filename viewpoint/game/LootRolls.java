package viewpoint.game;

import java.util.ArrayList;
import viewpoint.platform.LiveSettings;
import zombie.characters.IsoPlayer;
import zombie.core.logger.ExceptionLogger;
import zombie.inventory.ItemContainer;
import zombie.inventory.ItemPickerJava;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.network.GameClient;
import zombie.network.GameServer;

public final class LootRolls {
   static final LiveSettings.Toggle ON = LiveSettings.toggle("game.spreadLootRolls", "Loot rolled over frames", "Debug/Fixes", true);
   private static final long BUDGET_NANOS = 1000000L;
   private static final float STOREY = 3.0F;
   private static final ArrayList<IsoObject> waiting = new ArrayList<>();

   private LootRolls() {
   }

   public static boolean defer(IsoObject var0) {
      ItemContainer var1 = var0.getContainer();
      if (ON.get() && !GameClient.client && !GameServer.server && var1 != null && !var1.isExplored()) {
         waiting.add(var0);
         return true;
      } else {
         return false;
      }
   }

   public static void frame() {
      IsoPlayer var0 = IsoPlayer.getInstance();
      if (!waiting.isEmpty() && var0 != null) {
         long var1 = System.nanoTime() + 1000000L;

         while (!waiting.isEmpty() && System.nanoTime() < var1) {
            roll(take(nearest(var0)), var0);
         }
      }
   }

   public static void clear() {
      waiting.clear();
   }

   private static int nearest(IsoPlayer var0) {
      int var1 = 0;
      float var2 = Float.MAX_VALUE;

      for (int var3 = 0; var3 < waiting.size(); var3++) {
         IsoGridSquare var4 = waiting.get(var3).getSquare();
         if (var4 == null) {
            return var3;
         }

         float var5 = var4.getX() - var0.getX();
         float var6 = var4.getY() - var0.getY();
         float var7 = (var4.getZ() - var0.getZ()) * 3.0F;
         float var8 = var5 * var5 + var6 * var6 + var7 * var7;
         if (var8 < var2) {
            var2 = var8;
            var1 = var3;
         }
      }

      return var1;
   }

   private static IsoObject take(int var0) {
      IsoObject var1 = waiting.get(var0);
      waiting.set(var0, waiting.get(waiting.size() - 1));
      waiting.remove(waiting.size() - 1);
      return var1;
   }

   private static void roll(IsoObject var0, IsoPlayer var1) {
      IsoGridSquare var2 = var0.getSquare();
      ItemContainer var3 = var0.getContainer();
      if (var2 != null
         && IsoWorld.instance.currentCell.getGridSquare(var2.getX(), var2.getY(), var2.getZ()) == var2
         && var0.getObjectIndex() >= 0
         && var3 != null
         && !var3.isExplored()) {
         try {
            ItemPickerJava.fillContainer(var3, var1);
            var3.setExplored(true);
            if (!var3.isEmpty()) {
               ItemPickerJava.updateOverlaySprite(var0);
            }
         } catch (RuntimeException var5) {
            ExceptionLogger.logException(var5);
         }
      }
   }

   static {
      ON.describe(
         "Rolls the loot of the containers a chunk loads with over the next frames, nearest first, not all in the frame it loads: no hitch as you walk or drive into a town. Single player only."
      );
   }
}
