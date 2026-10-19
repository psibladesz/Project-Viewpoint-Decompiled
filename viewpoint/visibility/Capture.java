package viewpoint.visibility;

import java.util.Arrays;
import zombie.iso.IsoChunk;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.SpriteModel;
import zombie.iso.objects.IsoWindow;
import zombie.scripting.ScriptManager;

final class Capture {
   static Fragment capture(IsoChunk var0, int var1, IsoObject[] var2, long[] var3) {
      long[] var4 = new long[12];
      long[] var5 = new long[64];
      Arrays.fill(var2, null);
      var3[0] = var3[1] = 0L;

      for (int var6 = 0; var6 < 8; var6++) {
         for (int var7 = 0; var7 < 8; var7++) {
            int var8 = var6 * 8 + var7;
            IsoGridSquare var9 = var0.getGridSquare(var7, var6, var1);
            var5[var8] = -1L;
            if (var9 != null) {
               square(var9, var8, var4, var5, var2, var3);
            }
         }
      }

      return new Fragment(var0.wx, var0.wy, var1, var1 <= var0.minLevel, var4, var5);
   }

   private static void square(IsoGridSquare var0, int var1, long[] var2, long[] var3, IsoObject[] var4, long[] var5) {
      long var6 = 1L << var1;
      var2[0] |= var6;
      if (var0.isSolidFloor() || var0.TreatAsSolidFloor()) {
         var2[1] |= var6;
      }

      if (var0.HasStairs() || var0.HasStairsBelow()) {
         var2[2] |= var6;
      }

      if (var0.haveRoof) {
         var2[3] |= var6;
      }

      if (var0.getRoom() != null && !var0.isOutside()) {
         var3[var1] = var0.getRoomID();
      }

      edge(var0, var1, true, var2, var4, var5);
      edge(var0, var1, false, var2, var4, var5);
   }

   private static void edge(IsoGridSquare var0, int var1, boolean var2, long[] var3, IsoObject[] var4, long[] var5) {
      long var6 = 1L << var1;
      switch (Edges.edge(var0, var2)) {
         case 1:
            var3[var2 ? 4 : 5] = var3[var2 ? 4 : 5] | var6;
            break;
         case 2:
            var3[var2 ? 6 : 7] = var3[var2 ? 6 : 7] | var6;
            break;
         case 3:
            var3[var2 ? 8 : 9] = var3[var2 ? 8 : 9] | var6;
            var4[var1 * 2 + (var2 ? 0 : 1)] = var0.getDoor(Edges.facing(var2));
            break;
         case 4:
            var3[var2 ? 10 : 11] = var3[var2 ? 10 : 11] | var6;
            var5[var2 ? 0 : 1] = var5[var2 ? 0 : 1] | (glazed(var0.getWindow(Edges.facing(var2))) ? var6 : 0L);
      }
   }

   private static boolean glazed(IsoWindow var0) {
      return var0 != null && !var0.IsOpen() && !var0.isSmashed() && !var0.isGlassRemoved() && !var0.isDestroyed() && !drawnAsModel(var0);
   }

   static boolean drawnAsModel(IsoObject var0) {
      SpriteModel var1 = var0.getSpriteModel();
      return var1 != null && var1.modelScriptName != null && ScriptManager.instance.getModelScript(var1.modelScriptName) != null;
   }

   private Capture() {
   }
}
