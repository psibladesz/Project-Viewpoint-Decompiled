package viewpoint.visibility;

import java.util.Arrays;
import viewpoint.render.SceneData;
import zombie.core.TilePropertyAliasMap;
import zombie.core.properties.PropertyContainer;
import zombie.core.skinnedmodel.model.IsoObjectAnimations;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.objects.IsoDoor;
import zombie.iso.objects.IsoThumpable;
import zombie.util.list.PZArrayList;

final class Apertures {
   static final byte OPEN = 0;
   static final byte CLOSED = 1;
   static final byte UNKNOWN = 2;
   private static Topology.Leaf[] leaves = new Topology.Leaf[0];
   private static SectorGraph leavesOf;
   private static int leavesAt;
   static int windows;
   static int windowsClosed;
   private static int doorGlass;

   static void resolve(SectorGraph var0, Topology var1, byte[] var2, SceneData var3, Portals.View var4) {
      index(var0, var1);
      windowsClosed = 0;
      windows = 0;
      doorGlass = TilePropertyAliasMap.instance.getIDFromPropertyName("doorTrans");

      for (int var5 = 0; var5 < var0.doorLeaf.length; var5++) {
         Topology.Leaf var6 = leaves[var5];
         int var7 = var0.doorSlot[var5];
         boolean var8 = Glass.opaque(var3, var0.doorPlace, var5 * 5, var4);
         if (var0.doorWindow[var5]) {
            boolean var9 = var8 && var6 != null && var6.glazed(var7);
            windows++;
            windowsClosed += var9 ? 1 : 0;
            var2[var5] = (byte)(var9 ? 1 : 0);
         } else {
            var2[var5] = state(var6 == null ? null : var6.doors[var7], var8);
         }
      }
   }

   static void clear() {
      Arrays.fill(leaves, null);
      leavesOf = null;
   }

   private static void index(SectorGraph var0, Topology var1) {
      if (var0 != leavesOf || var1.generation() != leavesAt) {
         int var2 = var0.doorLeaf.length;
         leaves = leaves.length >= var2 ? leaves : new Topology.Leaf[var2 + var2 / 2];
         Arrays.fill(leaves, var2, leaves.length, null);

         for (int var3 = 0; var3 < var2; var3++) {
            leaves[var3] = var1.leaf(var0.doorLeaf[var3]);
         }

         leavesOf = var0;
         leavesAt = var1.generation();
      }
   }

   static byte state(IsoObject var0, boolean var1) {
      try {
         if (var0 == null || !inWorld(var0)) {
            return 2;
         } else if (var0 instanceof IsoDoor var4) {
            return (byte)(!var4.IsOpen() && !var4.isDestroyed() && !seeThrough(var0, var1) && !swinging(var0) ? 1 : 0);
         } else if (!(var0 instanceof IsoThumpable var2)) {
            return 2;
         } else {
            return (byte)(var2.isDoor() && !var2.IsOpen() && !var2.isDestroyed() && !seeThrough(var0, var1) && !swinging(var0) ? 1 : 0);
         }
      } catch (RuntimeException var3) {
         return 2;
      }
   }

   private static boolean swinging(IsoObject var0) {
      IsoObjectAnimations var1 = IsoObjectAnimations.getInstance();
      if (var1.getAnimationPlayer(var0) != null) {
         return true;
      }

      int var2 = IsoDoor.getDoubleDoorIndex(var0);
      if (var2 != 2 && var2 != 3) {
         return false;
      }

      IsoObject var3 = IsoDoor.getDoubleDoorObject(var0, var2 == 2 ? 1 : 4);
      return var3 != null && var1.getAnimationPlayer(var3) != null;
   }

   private static boolean inWorld(IsoObject var0) {
      IsoGridSquare var1 = var0.getSquare();
      if (var1 == null) {
         return false;
      }

      PZArrayList var2 = var1.getObjects();

      for (int var3 = 0; var3 < var2.size(); var3++) {
         if (var2.get(var3) == var0) {
            return true;
         }
      }

      return false;
   }

   private static boolean seeThrough(IsoObject var0, boolean var1) {
      PropertyContainer var2 = var0.getProperties();
      boolean var3 = doorGlass >= 0 && var2 != null && var2.containsKey((short)doorGlass);
      return var3 && (!var1 || Capture.drawnAsModel(var0));
   }

   private Apertures() {
   }
}
