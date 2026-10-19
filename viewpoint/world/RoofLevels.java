package viewpoint.world;

import java.util.Arrays;
import viewpoint.platform.Caches;
import viewpoint.platform.LongMap;
import zombie.core.textures.Texture;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoChunkMap;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoMetaGrid;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.iso.sprite.IsoSprite;
import zombie.util.list.PZArrayList;

final class RoofLevels implements Caches.Cache {
   static final byte TILED = 1;
   static final byte FLOOR = 2;
   static final byte ROOM = 4;
   static final byte WALL_N = 8;
   static final byte WALL_W = 16;
   static final byte GABLE_N = 32;
   static final byte GABLE_W = 64;
   static final int END = 128;
   static final byte FLAT_LID = 1;
   static final byte HIGH_LID = 2;
   static final int SQUARES = 64;
   static final int REACH = 2;
   private static final long LEVEL_BYTES = 1408L;
   private static final long WINDOW_BYTES = 48L;
   private static final RoofLevels.Level NOTHING = new RoofLevels.Level();
   private static final LongMap<RoofLevels.Level> levels = new LongMap<>(2048);
   private static final LongMap<Boolean> windows = new LongMap<>(512);
   private static long[] marks = new long[64];
   private static int markCount;

   static RoofLevels.Level captured(IsoChunk var0, int var1) {
      long var2 = key(var0.wx, var0.wy, var1);
      RoofLevels.Level var4 = capture(var0, var1);
      RoofLevels.Level var5 = levels.put(var2, var4);
      if (var5 == null) {
         first(var0.wx, var0.wy, var1);
      } else if (!var4.sameAs(var5)) {
         changed(var0.wx, var0.wy, var1);
      }

      return var4;
   }

   static RoofLevels.Level held(IsoChunk var0, int var1) {
      RoofLevels.Level var2 = levels.get(key(var0.wx, var0.wy, var1));
      return var2 != null ? var2 : captured(var0, var1);
   }

   static RoofLevels.Level get(IsoCell var0, int var1, int var2, int var3) {
      long var4 = key(var1, var2, var3);
      RoofLevels.Level var6 = levels.get(var4);
      if (var6 != null) {
         return var6;
      } else {
         IsoChunk var7 = var0.getChunk(var1, var2);
         if (var7 != null && var7.loaded && var7.wx == var1 && var7.wy == var2) {
            RoofLevels.Level var8 = capture(var7, var3);
            levels.put(var4, var8);
            first(var1, var2, var3);
            return var8;
         } else {
            return null;
         }
      }
   }

   static void windowed(int var0, int var1, int var2, boolean var3, boolean var4) {
      long var5 = key(var0, var1, var2);
      if (var3) {
         windows.put(var5, var4);
      } else {
         windows.remove(var5);
      }
   }

   static void markWaiting(IsoChunkMap var0) {
      for (int var1 = 0; var1 < markCount; var1++) {
         long var2 = marks[var1];
         ChunkCache.markLevel(var0, chunkX(var2), chunkY(var2), level(var2));
      }

      markCount = 0;
   }

   static void forget(int var0, int var1) {
      forget(levels, var0, var1);
      forget(windows, var0, var1);
   }

   static void sweep(LongMap<?> var0) {
      for (int var1 = 0; var1 < levels.capacity(); var1++) {
         long var2 = levels.keyAt(var1);
         if (levels.valueAt(var1) != null && var0.get(chunkKey(chunkX(var2), chunkY(var2))) == null) {
            levels.removeAt(var1);
         }
      }
   }

   static void clear() {
      levels.clear();
      windows.clear();
      markCount = 0;
   }

   private static RoofLevels.Level capture(IsoChunk var0, int var1) {
      if (var1 >= var0.minLevel && var1 <= var0.maxLevel) {
         RoofLevels.Level var2 = new RoofLevels.Level();
         IsoMetaGrid var3 = IsoWorld.instance.getMetaGrid();

         for (int var4 = 0; var4 < 64; var4++) {
            int var5 = var4 % 8;
            int var6 = var4 / 8;
            IsoGridSquare var7 = var0.getGridSquare(var5, var6, var1);
            if (var7 != null) {
               var2.bits[var4] = square(var2, var4, var7);
            }

            var2.porch[var4] = var1 > 0 && var7 != null && porch(var3, var7, var2.bits[var4]);
            var2.lid[var4] = lid(var7 != null && flatRoof(var7), var2.bits[var4], overhead(var0, var5, var6, var1));
         }

         return var2;
      } else {
         return NOTHING;
      }
   }

   private static boolean flatRoof(IsoGridSquare var0) {
      PZArrayList var1 = var0.getObjects();

      for (int var2 = 0; var2 < var1.size(); var2++) {
         IsoSprite var3 = ((IsoObject)var1.get(var2)).getSprite();
         if (var3 != null && flat(var3.name)) {
            return true;
         }
      }

      return false;
   }

   private static boolean porch(IsoMetaGrid var0, IsoGridSquare var1, byte var2) {
      boolean var3 = var0.getEmptyOutsideAt(var1.x, var1.y, var1.z) != null || var1.getRoom() != null && MapRoofs.outdoors(var1.getRoom().getName());
      return var3 && (var2 & 3) == 0;
   }

   private static boolean overhead(IsoChunk var0, int var1, int var2, int var3) {
      for (int var4 = var3 + 1; var4 <= var0.maxLevel; var4++) {
         IsoGridSquare var5 = var0.getGridSquare(var1, var2, var4);
         PZArrayList var6 = var5 == null ? null : var5.getObjects();

         for (int var7 = 0; var6 != null && var7 < var6.size(); var7++) {
            IsoSprite var8 = ((IsoObject)var6.get(var7)).getSprite();
            if (var8 != null && (bits(var8.name, var8.solidfloor) & 3) != 0) {
               return true;
            }
         }
      }

      return false;
   }

   private static byte square(RoofLevels.Level var0, int var1, IsoGridSquare var2) {
      int var3 = var2.getRoom() != null && !MapRoofs.outdoors(var2.getRoom().getName()) ? 4 : 0;
      PZArrayList var4 = var2.getObjects();

      for (int var5 = 0; var5 < var4.size(); var5++) {
         IsoObject var6 = (IsoObject)var4.get(var5);
         IsoSprite var7 = var6.getSprite();
         if (var7 != null) {
            int var8 = edges(var7);
            var3 |= bits(var7.name, var7.solidfloor) | var8;
            RoofPiece var9 = piece(var7.name);
            RoofPiece var10 = var0.pieces[var1];
            if (var9 != null && (var10 == null || var9.over(var10))) {
               var0.pieces[var1] = var9;
               var0.art[var1] = art(var6, var7);
            }

            if ((var8 & 32) != 0) {
               var0.gableN[var1] = art(var6, var7);
            }

            if ((var8 & 64) != 0) {
               var0.gableW[var1] = art(var6, var7);
            }
         }
      }

      return (byte)var3;
   }

   static int edges(IsoSprite var0) {
      int var1 = RoofPieces.gableEnd(var0.name);
      int var2 = (var0.cutN ? 8 : 0) | (var0.cutW ? 16 : 0);
      if (gableTile(var0.name) && var0.cutN || var1 == 2) {
         var2 |= 32 | (var1 == 2 ? 128 : 0);
      }

      if (gableTile(var0.name) && var0.cutW || var1 == 3) {
         var2 |= 64 | (var1 == 3 ? 128 : 0);
      }

      return var2;
   }

   private static Texture art(IsoObject var0, IsoSprite var1) {
      IsoSprite var2 = var0.isUseSnowSprite() ? var1.getSnowSprite() : var1;
      return var2.getTextureForCurrentFrame(var0.getDir(), var0);
   }

   static boolean roofTyped(IsoSprite var0) {
      IsoObjectType var1 = var0.getType();
      return var1 == IsoObjectType.WestRoofB || var1 == IsoObjectType.WestRoofM || var1 == IsoObjectType.WestRoofT;
   }

   static boolean trim(String var0, boolean var1) {
      return var1 && var0 != null && !roofTile(var0) && var0.startsWith("roofs_");
   }

   static boolean roofPiece(String var0, boolean var1, boolean var2) {
      return roofTile(var0) || trim(var0, var1) || (var1 || var2) && RoofPieces.knowsSheet(var0);
   }

   static boolean eave(String var0) {
      RoofPiece var1 = RoofPieces.of(var0);
      return var1 != null && var1.kind == 3;
   }

   static boolean gableTile(String var0) {
      return var0 != null && var0.startsWith("walls_exterior_roofs");
   }

   static byte bits(String var0, boolean var1) {
      int var2 = !var1 || var0 != null && var0.startsWith("ceilings_") ? 0 : 2;
      if (!roofTile(var0)) {
         return (byte)var2;
      } else {
         RoofPiece var3 = RoofPieces.of(var0);
         if (var3 != null && var3.kind == 4) {
            return (byte)(var2 | 2);
         } else {
            return (byte)(var3 != null && var3.kind == 3 ? var2 : (byte)(var2 | 1));
         }
      }
   }

   static boolean flat(String var0) {
      RoofPiece var1 = roofTile(var0) ? RoofPieces.of(var0) : null;
      return var1 != null && var1.kind == 4;
   }

   static RoofPiece piece(String var0) {
      RoofPiece var1 = roofTile(var0) ? RoofPieces.of(var0) : null;
      return var1 != null && var1.planar() ? var1 : null;
   }

   static boolean roofTile(String var0) {
      return var0 != null && (var0.startsWith("roofs_") && !var0.startsWith("roofs_accents") || RoofPieces.of(var0) != null);
   }

   static byte flags(byte var0, byte var1, byte var2, byte var3, boolean var4, boolean var5) {
      boolean var6 = (var1 & 2) != 0 || !var4 && covers(var2);
      return (byte)(
         ((var1 & 1) == 0 && !var4 ? 0 : 1)
            | (var6 ? 2 : 0)
            | (var4 ? 0 : lidFlags(var3))
            | ((var0 & 4) != 0 ? 4 : 0)
            | ((var1 & 4) != 0 ? 8 : 0)
            | (var5 && !var4 ? 64 : 0)
      );
   }

   static byte lid(boolean var0, byte var1, boolean var2) {
      return (byte)(var0 ? 1 : (var2 && (var1 & 3) == 0 ? 2 : 0));
   }

   static byte lidFlags(byte var0) {
      return (byte)(var0 == 2 ? 18 : (var0 == 1 ? 32 : 0));
   }

   static boolean covers(byte var0) {
      return (var0 & 1) != 0 || (var0 & 6) == 6;
   }

   static boolean carries(RoofPiece var0, byte var1) {
      return var0 != null && (var1 & 6) == 0;
   }

   static RoofPiece lowered(RoofPiece var0, int var1) {
      return var0.kind == 7
         ? RoofPiece.exact(var0.facing, var0.rise, var0.middle + 2.4494896F * var1)
         : new RoofPiece(var0.kind, var0.facing, var0.other, var0.perStorey, var0.step + var0.perStorey * var1);
   }

   private static void first(int var0, int var1, int var2) {
      for (int var3 = -1; var3 <= 1; var3++) {
         for (int var4 = -2; var4 <= 2; var4++) {
            for (int var5 = -2; var5 <= 2; var5++) {
               long var6 = key(var0 + var5, var1 + var4, var2 + var3);
               if ((var5 != 0 || var4 != 0 || var3 != 0) && Boolean.TRUE.equals(windows.get(var6))) {
                  mark(var6);
               }
            }
         }
      }
   }

   private static void changed(int var0, int var1, int var2) {
      for (int var3 = -1; var3 <= 1; var3++) {
         for (int var4 = -2; var4 <= 2; var4++) {
            for (int var5 = -2; var5 <= 2; var5++) {
               long var6 = key(var0 + var5, var1 + var4, var2 + var3);
               if ((var5 != 0 || var4 != 0 || var3 != 0) && windows.get(var6) != null) {
                  mark(var6);
               }
            }
         }
      }

      mark(key(var0, var1, var2 - 1));
      mark(key(var0, var1, var2 + 1));
      mark(key(var0 - 1, var1, var2));
      mark(key(var0, var1 - 1, var2));
      mark(key(var0 - 1, var1 - 1, var2));
   }

   private static void mark(long var0) {
      if (markCount == marks.length) {
         marks = Arrays.copyOf(marks, markCount * 2);
      }

      marks[markCount++] = var0;
   }

   private static <V> void forget(LongMap<V> var0, int var1, int var2) {
      for (int var3 = 0; var3 < var0.capacity(); var3++) {
         long var4 = var0.keyAt(var3);
         if (var0.valueAt(var3) != null && chunkX(var4) == var1 && chunkY(var4) == var2) {
            var0.removeAt(var3);
         }
      }
   }

   static long key(int var0, int var1, int var2) {
      return chunkKey(var0, var1) << 16 | var2 + 32 & 65535;
   }

   static long chunkKey(int var0, int var1) {
      return (long)(var0 & 16777215) << 24 | var1 & 16777215;
   }

   private static int chunkX(long var0) {
      return (int)(var0 >>> 40) << 8 >> 8;
   }

   private static int chunkY(long var0) {
      return (int)(var0 >>> 16) << 8 >> 8;
   }

   private static int level(long var0) {
      return (int)(var0 & 65535L) - 32;
   }

   @Override
   public long bytes() {
      return levels.size() * 1408L + windows.size() * 48L;
   }

   @Override
   public void list(Caches.Survey var1) {
   }

   @Override
   public void evictBefore(long var1) {
   }

   static {
      Caches.register(new RoofLevels());
   }

   static final class Level {
      final RoofPiece[] pieces = new RoofPiece[64];
      final Texture[] art = new Texture[64];
      final Texture[] gableN = new Texture[64];
      final Texture[] gableW = new Texture[64];
      final byte[] bits = new byte[64];
      final byte[] lid = new byte[64];
      final boolean[] porch = new boolean[64];

      boolean sameAs(RoofLevels.Level var1) {
         for (int var2 = 0; var2 < 64; var2++) {
            if (this.pieces[var2] != var1.pieces[var2]
               || this.art[var2] != var1.art[var2]
               || this.gableN[var2] != var1.gableN[var2]
               || this.gableW[var2] != var1.gableW[var2]) {
               return false;
            }
         }

         return Arrays.equals(this.bits, var1.bits) && Arrays.equals(this.lid, var1.lid) && Arrays.equals(this.porch, var1.porch);
      }
   }
}
