package viewpoint.light;

import java.util.ArrayList;
import java.util.Arrays;
import zombie.GameTime;
import zombie.SandboxOptions;
import zombie.iso.IsoMetaCell;
import zombie.iso.IsoMetaGrid;
import zombie.iso.IsoWorld;
import zombie.iso.RoomDef;
import zombie.iso.RoomDef.RoomRect;

public final class CellLights {
   public static CellLights.Cell cell(int var0, int var1, int[] var2) {
      IsoMetaGrid var3 = IsoWorld.instance.getMetaGrid();
      int var4 = 0;
      int[] var5 = new int[var2.length];
      boolean[] var6 = new boolean[var2.length / 4];
      boolean[] var7 = new boolean[var6.length];
      boolean[] var8 = new boolean[var6.length];

      for (int var9 = 0; var9 + 4 <= var2.length; var9 += 4) {
         int var10 = var2[var9];
         int var11 = var2[var9 + 1];
         int var12 = var2[var9 + 2];
         if (var12 >= 0 && var3.getRoomAt(var10, var11, var12) == null) {
            System.arraycopy(var2, var9, var5, var4 * 4, 4);
            var6[var4] = (var2[var9 + 3] & 1073741824) != 0;
            var7[var4] = MapSources.inBuilding(var3, var10, var11, var12);
            var8[var4] = MapSources.noPower(var3, var10, var11);
            var4++;
         }
      }

      ArrayList<RoomDef> var13 = rooms(var3, var0, var1);
      boolean[] var14 = new boolean[var13.size()];

      for (int var15 = 0; var15 < var14.length; var15++) {
         var14[var15] = MapSources.noPower(var3, (RoomDef)var13.get(var15));
      }

      return new CellLights.Cell(
         var0,
         var1,
         Arrays.copyOf(var5, var4 * 4),
         Arrays.copyOf(var6, var4),
         Arrays.copyOf(var7, var4),
         Arrays.copyOf(var8, var4),
         var13.toArray(new RoomDef[0]),
         var14
      );
   }

   public static CellLights.Inputs decide(CellLights.Cell var0, CellLights.Cell[] var1) {
      boolean var2 = SandboxOptions.instance.doesPowerGridExist();
      boolean var3 = GameTime.getInstance().getNight() >= 0.5F;
      boolean var4 = IsoWorld.instance.isHydroPowerOn();
      int var5 = var0.cellX * 256 - 20;
      int var6 = var5 + 256 + 40;
      int var7 = var0.cellY * 256 - 20;
      int var8 = var7 + 256 + 40;
      CellLights.IntList var9 = new CellLights.IntList();

      for (CellLights.Cell var13 : var1) {
         if (var13 != null) {
            lamps(var13, var2, var3, var5, var7, var6, var8, var9);
         }
      }

      CellLights.IntList var15 = new CellLights.IntList();

      for (int var16 = 0; var16 < var0.rooms.length; var16++) {
         RoomDef var17 = var0.rooms[var16];
         if ((var4 && !var0.roomNoPower[var16] || LightMemory.ownPower(var17.id)) && var17.lightsActive) {
            for (RoomRect var14 : var17.rects) {
               var15.add(new int[]{var14.x, var14.y, var17.level, var14.w, var14.h}, 0, 5);
            }
         }
      }

      return new CellLights.Inputs(var9.array(), var15.array());
   }

   private static void lamps(CellLights.Cell var0, boolean var1, boolean var2, int var3, int var4, int var5, int var6, CellLights.IntList var7) {
      boolean var8 = LightMemory.cellSeen(var0.cellX, var0.cellY);

      for (int var9 = 0; var9 < var0.street.length; var9++) {
         int var10 = var9 * 4;
         int var11 = var0.lamps[var10];
         int var12 = var0.lamps[var10 + 1];
         boolean var13 = var8 && LightMemory.seen(Math.floorDiv(var11, 8), Math.floorDiv(var12, 8));
         boolean var14 = !var13 && MapSources.gridPowered(var1, var2, var0.street[var9], var0.inBuilding[var9], var0.noPower[var9]);
         if (var14 && var11 >= var3 && var11 < var5 && var12 >= var4 && var12 < var6) {
            var7.add(var0.lamps, var10, 4);
         }
      }

      for (LightMemory.Lamp var18 : LightMemory.outdoorLamps(var0.cellX, var0.cellY)) {
         SourceRegister.Lamp var19 = var18.light();
         if (var18.on(var1, var2) && var19.x() >= var3 && var19.x() < var5 && var19.y() >= var4 && var19.y() < var6) {
            var7.add(new int[]{var19.x(), var19.y(), var19.z(), var18.tile()}, 0, 4);
         }
      }
   }

   private static ArrayList<RoomDef> rooms(IsoMetaGrid var0, int var1, int var2) {
      ArrayList var3 = new ArrayList();
      int var4 = var1 - var0.minX;
      int var5 = var2 - var0.minY;
      IsoMetaCell var6 = var4 >= 0 && var5 >= 0 && var4 <= var0.maxX - var0.minX && var5 <= var0.maxY - var0.minY ? var0.getCell(var4, var5) : null;
      if (var6 == null) {
         return var3;
      }

      for (RoomDef var8 : var6.roomList) {
         if (var8.level >= 0 && var8.level < 8 && !var8.isEmptyOutside() && MapSources.hasLights(var8)) {
            var3.add(var8);
         }
      }

      return var3;
   }

   private CellLights() {
   }

   public static final class Cell {
      final int cellX;
      final int cellY;
      final int[] lamps;
      final boolean[] street;
      final boolean[] inBuilding;
      final boolean[] noPower;
      final RoomDef[] rooms;
      final boolean[] roomNoPower;

      private Cell(int var1, int var2, int[] var3, boolean[] var4, boolean[] var5, boolean[] var6, RoomDef[] var7, boolean[] var8) {
         this.cellX = var1;
         this.cellY = var2;
         this.lamps = var3;
         this.street = var4;
         this.inBuilding = var5;
         this.noPower = var6;
         this.rooms = var7;
         this.roomNoPower = var8;
      }
   }

   public record Inputs(int[] lamps, int[] rooms) {
      public boolean same(CellLights.Inputs var1) {
         return var1 != null && Arrays.equals(this.lamps, var1.lamps) && Arrays.equals(this.rooms, var1.rooms);
      }
   }

   private static final class IntList {
      private int[] a = new int[64];
      private int n;

      void add(int[] var1, int var2, int var3) {
         if (this.n + var3 > this.a.length) {
            this.a = Arrays.copyOf(this.a, Math.max(this.a.length * 2, this.n + var3));
         }

         System.arraycopy(var1, var2, this.a, this.n, var3);
         this.n += var3;
      }

      int[] array() {
         return Arrays.copyOf(this.a, this.n);
      }
   }
}
