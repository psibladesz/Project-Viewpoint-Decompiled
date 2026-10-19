package viewpoint.world;

import viewpoint.platform.LiveSettings;
import zombie.characters.IsoPlayer;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.iso.sprite.IsoSprite;
import zombie.util.list.PZArrayList;

final class RoofDebug {
   static final LiveSettings.Toggle DUMP = LiveSettings.toggle("debug.roofDump", "Dump the roofs round the player (console, once)", "Debug/Views", false);
   private static final int SIDE = 8;
   private static final int SQUARES = 64;
   private static final int[] EDGE_MIDDLES = new int[]{1, 3, 5, 7};
   private static final String[] PLAIN = new String[]{"floors_", "blends_", "overlay_grime", "ceilings_"};

   static void update(int var0) {
      if (DUMP.get()) {
         DUMP.set(false);
         IsoPlayer var1 = IsoPlayer.players[var0];
         IsoGridSquare var2 = var1 == null ? null : var1.getCurrentSquare();
         if (var2 == null) {
            System.out.println("[Viewpoint] roofs: no square under the player to dump round");
         } else {
            System.out.printf("[Viewpoint] roofs: the player at %d,%d,%d%n", var2.x, var2.y, var2.z);
            IsoCell var3 = IsoWorld.instance.getCell();
            int var4 = Math.floorDiv(var2.x, 8);
            int var5 = Math.floorDiv(var2.y, 8);

            for (int var6 = -1; var6 <= 1; var6++) {
               for (int var7 = -1; var7 <= 1; var7++) {
                  IsoChunk var8 = var3.getChunk(var4 + var7, var5 + var6);

                  for (int var9 = 1; var8 != null && var9 <= var8.maxLevel; var9++) {
                     System.out.println(describe(var8, var9));
                  }
               }
            }
         }
      }
   }

   static String describe(IsoChunk var0, int var1) {
      RoofGather.begin(var0, var1, true);
      RoofWindow var2 = RoofGather.window();
      StringBuilder var3 = new StringBuilder(String.format("[Viewpoint] roofs: chunk %d,%d level %d: ", var0.wx, var0.wy, var1));
      if (var2 == null) {
         RoofGather.end();
         return var3.append(RoofGather.BUILT.get() ? "no roof" : "roofs off").toString();
      }

      RoofSurface var4 = var2.build();
      var3.append(String.format("window %d x %d, the chunk at %d,%d", var2.width, var2.height, var2.inner.x(), var2.inner.z()));

      for (int var5 = 0; var5 < 64; var5++) {
         int var6 = var2.square(var5);
         var3.append(var5 % 8 == 0 ? "\n[Viewpoint] roofs:   " : " ");
         var3.append(
            String.format(
               "%-14s",
               flag(var2.flags[var6])
                  + (var2.pieces[var6] == null ? "" : var2.pieces[var6].toString().replace(" ", ""))
                  + "/"
                  + RoofGather.draws(var5)
                  + "/"
                  + "-DIML".charAt(var4.source(var6 % var2.width, var6 / var2.width))
            )
         );
      }

      var3.append("\n[Viewpoint] roofs:   heights at the middles of each square's north, west, east and south edges:");

      for (int var9 = 0; var9 < 64; var9++) {
         int var10 = var2.square(var9);
         int var7 = var10 % var2.width;
         int var8 = var10 / var2.width;
         var3.append(var9 % 8 == 0 ? "\n[Viewpoint] roofs:   " : " ");
         var3.append(
            Float.isNaN(var4.height(var7, var8, EDGE_MIDDLES[0]))
               ? String.format("%-19s", "-")
               : String.format(
                  "%4.2f %4.2f %4.2f %4.2f",
                  var4.height(var7, var8, EDGE_MIDDLES[0]),
                  var4.height(var7, var8, EDGE_MIDDLES[1]),
                  var4.height(var7, var8, EDGE_MIDDLES[2]),
                  var4.height(var7, var8, EDGE_MIDDLES[3])
               )
         );
      }

      objects(var3, var0, var1);
      RoofGather.end();
      return var3.toString();
   }

   private static void objects(StringBuilder var0, IsoChunk var1, int var2) {
      var0.append("\n[Viewpoint] roofs:   objects (T a trim left out, R a roof tile the roof draws, D drawn otherwise):");

      for (int var3 = var2 - 1; var3 <= var2; var3++) {
         for (int var4 = 0; var4 < 64; var4++) {
            IsoGridSquare var5 = var1.getGridSquare(var4 % 8, var4 / 8, var3);
            StringBuilder var6 = new StringBuilder();
            PZArrayList var7 = var5 == null ? null : var5.getObjects();

            for (int var8 = 0; var7 != null && var8 < var7.size(); var8++) {
               IsoSprite var9 = ((IsoObject)var7.get(var8)).getSprite();
               if (var9 != null && var9.name != null && !plain(var9.name)) {
                  var6.append(' ').append(var9.name).append('/').append(RoofGather.fate(var3 == var2 ? var4 : -1, var9));
               }
            }

            if (!var6.isEmpty()) {
               int var10 = var1.wx * 8 + var4 % 8;
               int var11 = var1.wy * 8 + var4 / 8;
               var0.append(String.format("\n[Viewpoint] roofs:     %d,%d,%d:", var10, var11, var3)).append(var6);
            }
         }
      }
   }

   private static boolean plain(String var0) {
      for (String var4 : PLAIN) {
         if (var0.startsWith(var4)) {
            return true;
         }
      }

      return false;
   }

   private static char flag(byte var0) {
      if ((var0 & 1) != 0) {
         return 't';
      } else if ((var0 & 16) != 0) {
         return 'i';
      } else if ((var0 & 2) != 0) {
         return 'c';
      } else {
         return (char)((var0 & ' ') != 0 ? 'f' : ((var0 & 4) != 0 ? 'u' : '.'));
      }
   }

   private RoofDebug() {
   }
}
