package viewpoint.light;

import java.util.ArrayList;
import java.util.Arrays;
import viewpoint.platform.Caches;
import viewpoint.platform.LongMap;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;

public final class LightLayouts implements Caches.Cache {
   static final int SQUARES = 64;
   private static final long LEVEL_BYTES = 1208L;
   private static final LongMap<LightLayouts.Level> levels = new LongMap<>(2048);
   private static final LongMap<LightLayouts.Level> map = new LongMap<>(8192);
   static int captures;
   static long captureNanos;

   public static void captured(IsoCell var0, IsoChunk var1, int var2) {
      long var3 = System.nanoTime();
      long var5 = key(var1.wx, var1.wy, var2);
      LightLayouts.Level var7 = map.get(var5);
      LightLayouts.Level var8 = levels.get(var5);
      LightLayouts.Level var9 = LayoutCapture.edges(var0, LayoutCapture.level(var1, var2), var7);
      LightLayouts.Level var10 = var8 != null ? var8 : var7;
      boolean var11 = var9 == null ? var8 != null : var8 == null || !var9.sameAs(var8) || !var9.sameWalls(var8);
      MapCheck.layout(var7, var9);
      if (var11) {
         put(var5, var9);
         LightLayouts.Level var12 = var9 != null ? var9 : var7;
         if (var12 == null ? var10 != null : var10 == null || !var12.sameAs(var10)) {
            LightJobs.layoutChanged(var1.wx, var1.wy, var2);
         }

         around(var0, var1.wx, var1.wy, var2);
         if (var8 != null) {
            below(var0, var1, var2 - 1);
         }
      }

      captures++;
      captureNanos = captureNanos + (System.nanoTime() - var3);
   }

   private static void around(IsoCell var0, int var1, int var2, int var3) {
      for (int var4 = -1; var4 <= 1; var4++) {
         for (int var5 = -1; var5 <= 1; var5++) {
            LightLayouts.Level var6 = levels.get(key(var1 + var5, var2 + var4, var3));
            if ((var5 != 0 || var4 != 0) && var6 != null) {
               long var7 = key(var1 + var5, var2 + var4, var3);
               LightLayouts.Level var9 = LayoutCapture.edges(var0, LayoutCapture.facing(var0, var6, var1, var2), map.get(var7));
               if (var9 != var6 && !var9.sameAs(var6)) {
                  put(var7, var9);
                  LightJobs.layoutChanged(var1 + var5, var2 + var4, var3);
               }
            }
         }
      }
   }

   private static void below(IsoCell var0, IsoChunk var1, int var2) {
      long var3 = key(var1.wx, var1.wy, var2);
      LightLayouts.Level var5 = levels.get(var3);
      if (var5 != null) {
         LightLayouts.Level var6 = LayoutCapture.edges(var0, LayoutCapture.level(var1, var2), map.get(var3));
         if (var6 == null || !var6.sameAs(var5)) {
            put(var3, var6);
            if (var6 != null || map.get(var3) == null || !map.get(var3).sameAs(var5)) {
               LightJobs.layoutChanged(var1.wx, var1.wy, var2);
            }
         }
      }
   }

   private static void put(long var0, LightLayouts.Level var2) {
      if (var2 == null) {
         levels.remove(var0);
      } else {
         levels.put(var0, var2);
      }
   }

   static LightLayouts.Level get(int var0, int var1, int var2) {
      long var3 = key(var0, var1, var2);
      LightLayouts.Level var5 = levels.get(var3);
      return var5 != null ? var5 : map.get(var3);
   }

   public static void forget(int var0, int var1) {
      ArrayList<LightLayouts.Level> var2 = new ArrayList<>();
      levels.removeIf(var3 -> {
         boolean var4x = var3.chunkX == var0 && var3.chunkY == var1;
         if (var4x) {
            var2.add(var3);
         }

         return var4x;
      });

      for (LightLayouts.Level var4 : var2) {
         LightLayouts.Level var5 = map.get(key(var4.chunkX, var4.chunkY, var4.level));
         if (var5 == null || !var5.sameAs(var4)) {
            LightJobs.layoutChanged(var4.chunkX, var4.chunkY, var4.level);
         }
      }
   }

   static void mapped(IsoCell var0, LightLayouts.Level var1) {
      long var2 = key(var1.chunkX, var1.chunkY, var1.level);
      LightLayouts.Level var4 = map.get(var2);
      LightLayouts.Level var5 = levels.get(var2);
      map.put(var2, var1);
      if (var5 != null || var4 != null && var4.sameAs(var1)) {
         if (var5 != null && var0 != null) {
            LightLayouts.Level var6 = LayoutCapture.edges(var0, var5, var1);
            if (var6 != var5) {
               levels.put(var2, var6);
               LightJobs.layoutChanged(var1.chunkX, var1.chunkY, var1.level);
            }
         }
      } else {
         LightJobs.layoutChanged(var1.chunkX, var1.chunkY, var1.level);
      }
   }

   static void unmapped(int var0, int var1, int var2) {
      long var3 = key(var0, var1, var2);
      if (map.remove(var3) != null && levels.get(var3) == null) {
         LightJobs.layoutChanged(var0, var1, var2);
      }
   }

   public static void clear() {
      levels.clear();
      map.clear();
   }

   static long key(int var0, int var1, int var2) {
      return (long)(var0 & 16777215) << 40 | (long)(var1 & 16777215) << 16 | var2 + 32 & 65535;
   }

   @Override
   public long bytes() {
      return (levels.size() + map.size()) * 1208L;
   }

   @Override
   public void list(Caches.Survey var1) {
   }

   @Override
   public void evictBefore(long var1) {
   }

   static {
      Caches.register(new LightLayouts());
   }

   static final class Level {
      final int chunkX;
      final int chunkY;
      final int level;
      final byte[] flags;
      final int[] passes;
      final long[] rooms;
      final int[] walls;

      Level(int var1, int var2, int var3, byte[] var4, int[] var5, long[] var6, int[] var7) {
         this.chunkX = var1;
         this.chunkY = var2;
         this.level = var3;
         this.flags = var4;
         this.passes = var5;
         this.rooms = var6;
         this.walls = var7;
      }

      boolean sameAs(LightLayouts.Level var1) {
         return Arrays.equals(this.flags, var1.flags) && Arrays.equals(this.passes, var1.passes) && Arrays.equals(this.rooms, var1.rooms);
      }

      boolean sameWalls(LightLayouts.Level var1) {
         return Arrays.equals(this.walls, var1.walls);
      }
   }
}
